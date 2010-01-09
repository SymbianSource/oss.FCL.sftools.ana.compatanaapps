/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: Job class to run HeaderAnalysis. Also, responsible
* to parse HeaderAnalysis output and to inform the progress to user.
*
*/
package com.nokia.s60tools.compatibilityanalyser.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.LastUsedKnownissues;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.ReportData;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.compatibilityanalyser.utils.BaselineProfileUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalysisConsole;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.util.cmdline.CmdLineCommandExecutorFactory;
import com.nokia.s60tools.util.cmdline.ICmdLineCommandExecutor;
import com.nokia.s60tools.util.cmdline.ICmdLineCommandExecutorObserver;
import com.nokia.s60tools.util.cmdline.ICustomLineReader;
import com.nokia.s60tools.util.cmdline.UnsupportedOSException;
import com.nokia.s60tools.util.python.PythonUtilities;

/**
 * Job class for HeaderAnalysis.
 * This class parses the output from core HeaderAnalysis process 
 * and informs progress to the user. 
 *
 */
public class HeaderAnalyserEngine extends Job implements ICmdLineCommandExecutorObserver{

	private CompatibilityAnalyserEngine engine;
	private IProgressMonitor monitor;

	private int previousProgress = 0;
	private int perviousPercent = 0;
	private int totalIssueFiles = 0;
	private int issuesProcessed = 0;

	private boolean filterationNeeded;
	private boolean isCancelled;
	private String reportName;

	private AnalysisFeedbackHandler feedbackHandler;

	private BaselineSdkData baselineData;
	private ICustomLineReader stdOutReader;

	private String pythonPath;
	private HeaderAnalysisProgressLineReader progressReader;
	private File headersFile;
	private File configFile;
	private File currentForcedHeaders;
	private File baselineForcedHeaders;
	private String config_folder = null;
	private String temp_folder;
	

	/**
	 * Constructor of the Job.
	 * @param engine contains the currentsdk and baselinesdk data needed for HA
	 * @param feedbackHandler will be notified of header analysis completion.
	 */
	public HeaderAnalyserEngine(CompatibilityAnalyserEngine engine, AnalysisFeedbackHandler feedbackHandler){
		super(Messages.getString("HeaderAnalyserEngine.AnalysingHeaders")); //$NON-NLS-1$
		this.engine = engine;
		this.feedbackHandler = feedbackHandler;
		isCancelled = false;
		filterationNeeded = engine.currentSdk.filterNeeded;		
	}


	/**
	 * This method validates the Current SDK and Baseline SDK data and
	 * throws back error messages back to UI. 
	 * Also, it prepares the config file used in invoking HA through Checkbc
	 * @param currentData specifies the Current SDK data
	 * @param baselineData specifies the Baseline SDK data
	 * @returns any error in the Input
	 */
	public String readAndCheckInput(ProductSdkData currentData, BaselineSdkData baselineData)
	{
		pythonPath = PythonUtilities.getPythonPath();
		
		if(pythonPath == null)
			return Messages.getString("HeaderAnalyserEngine.PythonNotFoundError"); //$NON-NLS-1$
		
		// Validating the python version
		boolean python_validity = PythonUtilities.validatePython(CompatibilityAnalyserEngine.SUPPORTED_PYTHON_VERSION);
		if(!python_validity)
			return Messages.getString("CompatibilityAnalyserEngine.InstallValidPython") + CompatibilityAnalyserEngine.SUPPORTED_PYTHON_VERSION; //$NON-NLS-1$
			    
		if(baselineData == null){
			return Messages.getString("HeaderAnalyserEngine.BaselineNotExistError");
		}

		String coreToolsExtraction = null;
		if(currentData.useWebServerCoreTools)
		{
			if(CompatibilityAnalyserEngine.isDownloadAndExtractionNeeded(currentData.urlPathofCoreTools))
			{
				IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
				//If core tools were already downloaded from web server earlier, they will be deleted. 
				String previousContents = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR);
				if(previousContents != null && !previousContents.equals("")) //$NON-NLS-1$
				{
					File oldContents = new File(previousContents);
					if(oldContents.exists())
						FileMethods.deleteFolder(previousContents);
				}
				String targetPath = FileMethods.appendPathSeparator(CompatibilityAnalyserEngine.getWorkspacePath()) + Messages.getString("HeaderAnalyserEngine.WebServerContentsNew"); //$NON-NLS-1$
				coreToolsExtraction = CompatibilityAnalyserEngine.readAndDownloadSupportedCoretools(currentData.urlPathofCoreTools, targetPath, monitor);
				if(coreToolsExtraction == null)
				{
					prefStore.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_URL, currentData.urlPathofCoreTools);
					prefStore.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH, CompatibilityAnalyserEngine.getWebServerToolsPath());
					prefStore.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR, targetPath);
					currentData.coreToolsPath = CompatibilityAnalyserEngine.getWebServerToolsPath();
				}
			}
			else
			{
				IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
				currentData.coreToolsPath = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH);
			}
		}
		
		if(currentData.useDefaultCoreTools)
		{
			String corePath = CompatibilityAnalyserPlugin.getInstallPathOfToolsPlugin();

			if(corePath == null || corePath.equalsIgnoreCase("")) //$NON-NLS-1$
			{
				return Messages.getString("HeaderAnalyserEngine.CouldnotfindToolsPluginError"); //$NON-NLS-1$
			}
			else
			{
				File toolsFolder = new File(FileMethods.appendPathSeparator(corePath) + Messages.getString("HeaderAnalyserEngine.BCTools")); //$NON-NLS-1$
				if(!toolsFolder.exists())
					return new String(Messages.getString("HeaderAnalyserEngine.ToolsPluginDoesNotContainCoreComponents")); //$NON-NLS-1$

				currentData.coreToolsPath = FileMethods.appendPathSeparator(corePath) + Messages.getString("HeaderAnalyserEngine.BCTools") + File.separator; 
			}
		}

		if(coreToolsExtraction == null)
		{
			try 
			{
				//Validating the DATA VERSION
				String dv = CompatibilityAnalyserEngine.getDataVersion(currentData.coreToolsPath);
				int num = Integer.parseInt(dv);
				if(num != CompatibilityAnalyserPlugin.DATA_VERSION)
				{
					if(!currentData.coreToolsPath.equalsIgnoreCase(CompatibilityAnalyserPlugin.getDefaltCoretoolsPath()))
					{
						feedbackHandler.reStartAnalysisUsingSameData("Compatibility Analyser - Header Analysis", Messages.getString("HeaderAnalyserEngine.CoreToolsAreNotCompatible") + CompatibilityAnalyserPlugin.DATA_VERSION + ".\n\nWould you like to run analysis with the default core tools?");
						return "";
					}
					return "Please update the " +  CompatibilityAnalyserPlugin.CORE_COMPONENTS_PLUGIN_ID + " plugin. The one being used is incompatible with the tool."; 
				}
			} catch (Exception e1) {
				return Messages.getString("HeaderAnalyserEngine.InvalidDataVersion"); //$NON-NLS-1$
			}
			
			try 
			{
				config_folder = engine.getCurrentSdkData().getconfigfolder();
				configFile = new File(config_folder+File.separator+Messages.getString("HeaderAnalyserEngine.carbide_ha.cf")); //$NON-NLS-1$
				temp_folder = config_folder + File.separator + "temp";
				if(!configFile.getParentFile().exists())
					return Messages.getString("HeaderAnalyserEngine.InvalidCoreToolsPathError"); //$NON-NLS-1$

				if(configFile.exists())
					configFile.delete();

				if(!configFile.createNewFile())
				{
					return Messages.getString("HeaderAnalyserEngine.ErrorInConfigFileCreation"); 
				}

				FileOutputStream outStr = new FileOutputStream(configFile);
				outStr.write((Messages.getString("HeaderAnalyserEngine.BASELINE_NAME") + baselineData.baselineSdkName + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.BASELINE_SDK_DIR") + baselineData.epocRoot + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.BASELINE_SDK_S60_VERSION") + baselineData.baselineSdkVersion + "\n").getBytes());  //$NON-NLS-2$

				outStr.write((Messages.getString("HeaderAnalyserEngine.CURRENT_NAME") + currentData.productSdkName + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.CURRENT_SDK_DIR") + currentData.epocRoot + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.CURRENT_SDK_S60_VERSION") + currentData.productSdkVersion + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.TEMP") + temp_folder + "\n").getBytes());  //$NON-NLS-2$ //$NON-NLS-3$
				outStr.write((Messages.getString("HeaderAnalyserEngine.BASELINE_HEADERS") + FileMethods.convertToOneString(baselineData.baselineHeaderDir) + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.CURRENT_HEADERS") + FileMethods.convertToOneString(currentData.currentHeaderDir) + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.BASELINE_SYSTEMINCLUDEDIR") + baselineData.baselineIncludes + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.CURRENT_SYSTEMINCLUDEDIR") + currentData.readIncludePaths() + "\n").getBytes());  //$NON-NLS-2$
				
				outStr.write((Messages.getString("HeaderAnalyserEngine.USE_PLATFORM_DATA") + currentData.usePlatformData + "\n").getBytes());  //$NON-NLS-2$
				outStr.write((Messages.getString("HeaderAnalyserEngine.RECURSIVE_HEADERS") + currentData.useRecursive + "\n").getBytes());  //$NON-NLS-2$

				if(currentData.replaceSet != null && currentData.replaceSet.size() >0){
					StringBuffer sb = new StringBuffer(""); 
					
					for(String s:currentData.replaceSet)
					{
						sb.append(s + ";"); 
					}
					outStr.write((Messages.getString("HeaderAnalyserEngine.REPLACE_HEADERS") + sb.toString() + "\n").getBytes());  //$NON-NLS-2$
				}
				
				if(currentData.forcedHeaders != null && currentData.forcedHeaders.size() >0)
				{
					currentForcedHeaders = new File(config_folder+File.separator + "carbide_currentforced_headers.h");
					if(currentForcedHeaders.exists())
						currentForcedHeaders.delete();

					if(!currentForcedHeaders.createNewFile())
					{
						return "Unable to prepare Forced Headers List. CoreTools path is write protected."; 
					}
					FileOutputStream forcedOutStr = new FileOutputStream(currentForcedHeaders);
					
					for(String s:currentData.forcedHeaders)
					{
						forcedOutStr.write(("#include <" + s + ">" + "\n").getBytes());
					}
					if(forcedOutStr != null)
						forcedOutStr.close();
					outStr.write(("CURRENT_FORCED_HEADERS=" + currentForcedHeaders.getAbsolutePath() + "\n").getBytes());
				}
				if(baselineData.forcedHeaders != null && baselineData.forcedHeaders.length > 0)
				{
					baselineForcedHeaders = new File(config_folder+File.separator+ "carbide_baselineforced_headers.h");
					if(baselineForcedHeaders.exists())
						baselineForcedHeaders.delete();

					if(!baselineForcedHeaders.createNewFile())
					{
						return "Unable to prepare Forced Headers List. CoreTools path is write protected."; 
					}
					FileOutputStream forcedOutStr = new FileOutputStream(baselineForcedHeaders);

					for(String s:baselineData.forcedHeaders)
					{
						forcedOutStr.write(("#include <" + s +">" +  "\n").getBytes());
					}
					if(forcedOutStr != null)
						forcedOutStr.close();

					outStr.write(("BASELINE_FORCED_HEADERS=" + baselineForcedHeaders.getAbsolutePath() + "\n").getBytes());
				}
				reportName = FileMethods.appendPathSeparator(currentData.reportPath) + Messages.getString("HeaderAnalyserEngine.Headers_") + currentData.reportName;	 
				outStr.write((Messages.getString("HeaderAnalyserEngine.REPORT_FILE_HEADERS") + currentData.reportPath + File.separator + Messages.getString("HeaderAnalyserEngine.Headers_") + currentData.reportName + "\n").getBytes());  //$NON-NLS-2$ //$NON-NLS-3$

				if(outStr != null)
					outStr.close();

				if(currentData.filterNeeded)
					prepareKnownIssuesPath();

			}catch(Exception e){
				e.printStackTrace();
				return e.getMessage();
			}
		}
		else
			return coreToolsExtraction;

		return null;
	}


	/**
	 * This method prepares all arguments used to invoke HA through Checkbc.
	 * @param currentSdk specifies the data related to Current SDK.
	 * @return an array of strings used as arguments to Checkbc
	 */
	public String [] prepareHAArguments(ProductSdkData currentSdk)
	{
		Vector<String> argsList = new Vector<String>();
		
		FileOutputStream outStr;
		argsList.add(pythonPath);
		argsList.add("\"" + FileMethods.appendPathSeparator(currentSdk.coreToolsPath) + Messages.getString("HeaderAnalyserEngine.Checkbc.py") + "\"");  //$NON-NLS-2$ //$NON-NLS-3$
		argsList.add("\"" + currentSdk.config_folder+File.separator+ Messages.getString("HeaderAnalyserEngine.carbide_ha.cf") + "\"");  //$NON-NLS-2$ //$NON-NLS-3$
		argsList.add("-c"); 

		if(currentSdk.analyseAll)
		{
			if(currentSdk.allTypes)
				argsList.add("-ha"); 
			else 
			{
				headersFile = new File(currentSdk.config_folder+File.separator + Messages.getString("HeaderAnalyserEngine.carbide_headers.txt")); 
				if(prepareFilefromSelectedTypes(headersFile, currentSdk))
				{
					argsList.add("-hm"); 
					argsList.add("\"" + headersFile.getAbsolutePath() + "\"");  //$NON-NLS-2$
				}
				else
				{
					return null;
				}
			}
		}
		else if(currentSdk.HeaderFilesList.length == 1)
		{
			argsList.add("-hs"); 
			String name = currentSdk.HeaderFilesList[0];
			
			argsList.add(name);
		}
		else
		{
			argsList.add("-hm"); 
			try
			{
				headersFile = new File(currentSdk.config_folder+File.separator + Messages.getString("HeaderAnalyserEngine.carbide_headers.txt")); 
				if(headersFile.exists())
					headersFile.delete();

				if(!headersFile.createNewFile())
				{
					return null;
				}
				else
				{
					outStr = new FileOutputStream(headersFile);
				}

				if(currentSdk.allTypes)
				{
					for(String s: currentSdk.HeaderFilesList)
					{
						outStr.write(s.getBytes());
						outStr.write("\n".getBytes());
					}
				}
				else
				{
					for(String s:currentSdk.HeaderFilesList)
					{
						if(isValidFile(s, currentSdk))
						{
							outStr.write((s + "\n").getBytes());
						}
					}
				}
				if(outStr != null)
					outStr.close();

				argsList.add("\"" + headersFile.getAbsolutePath() +"\""); 

			}catch(Exception e){
				return null;
			}
		}

		FileMethods.copyBBCResultsFileIfNotExists(currentSdk.reportPath,currentSdk.coreToolsPath);
		if(currentSdk.filterNeeded)
		{
			argsList.add("-f"); 
		}
		return (String [])argsList.toArray(new String[argsList.size()]);
	}

	/**
	 * This method validates given header file based on user selected Header Types.
	 */
	private boolean isValidFile(String name, ProductSdkData currentSdk)
	{
		if(name.endsWith(CompatibilityAnalyserEngine.H_EXTN) && currentSdk.hTypes) 
			return true;
		else if(name.endsWith(CompatibilityAnalyserEngine.HRH_EXTN) && currentSdk.hrhTypes) 
			return true;
		else if(name.endsWith(CompatibilityAnalyserEngine.RSG_EXTN) && currentSdk.rsgTypes) 
			return true;
		else if(name.endsWith(CompatibilityAnalyserEngine.MBG_EXTN) && currentSdk.mbgTypes) 
			return true;
		else if(name.endsWith(CompatibilityAnalyserEngine.HPP_EXTN) && currentSdk.hppTypes) 
			return true;
		else if(name.endsWith(CompatibilityAnalyserEngine.PAN_EXTN) && currentSdk.panTypes) 
			return true;
			
		return false;
	}

	/**
	 * This method prepares the text file, used in invoking HA through Checkbc,
	 * with the list of selected file types.
	 * 
	 */
	private boolean prepareFilefromSelectedTypes(File headersFile, ProductSdkData currentSdk)
	{
		FileOutputStream outStr = null;

		try
		{			
			if(headersFile.exists())
				headersFile.delete();

			if(!headersFile.createNewFile())
			{
				return false;
			}
			else
				outStr = new FileOutputStream(headersFile);


			if(currentSdk.hTypes)
				outStr.write((Messages.getString("HeaderAnalyserEngine.AllHeaders") + "\n").getBytes());  //$NON-NLS-2$
			if(currentSdk.hrhTypes)
				outStr.write((Messages.getString("HeaderAnalyserEngine.AllHRHfiles") + "\n").getBytes());  //$NON-NLS-2$
			if(currentSdk.rsgTypes)
				outStr.write((Messages.getString("HeaderAnalyserEngine.AllRSGfiles") + "\n").getBytes());  //$NON-NLS-2$
			if(currentSdk.mbgTypes)
				outStr.write((Messages.getString("HeaderAnalyserEngine.AllMBGfiles") + "\n").getBytes());  //$NON-NLS-2$
			if(currentSdk.hppTypes)
				outStr.write((Messages.getString("HeaderAnalyserEngine.AllHPPfiles") + "\n").getBytes());
			if(currentSdk.panTypes)
				outStr.write((Messages.getString("HeaderAnalyserEngine.AllPANfiles") + "\n").getBytes());
			
			if(outStr != null)
				outStr.close();

		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;	
	}


	/**
	 * This method invokes all helper methods, to read BaselineProfile data, to validate
	 * given input, and to prepare all arguments used to invoke HA through Checkbc.
	 * And it invokes the actual Execution command to run the analysis.
	 */
	protected IStatus run(IProgressMonitor monitor) {
		
		this.monitor = monitor;
		
		try
		{
			Object obj = BaselineProfileUtils.getBaselineProfileData(engine.getBaselineProfile());
			
			if(!(obj instanceof BaselineProfile)) {
				feedbackHandler.HandleFeedback("Compatibility Analyser - Header Analysis",Messages.getString("HeaderAnalyserEngine.GivenBaselineProfileDoesNotExist")); 
				return Status.OK_STATUS;
			}
			else
			{
				baselineData = engine.readBaselineSdkData((BaselineProfile)obj);
			}

			String status = readAndCheckInput(engine.currentSdk, baselineData);

			if(status != null)
			{
				feedbackHandler.HandleFeedback("Compatibility Analyser - Header Analysis",status);
				return Status.OK_STATUS;
			}
			else
			{
				String [] args = prepareHAArguments(engine.currentSdk);
				
				if(args == null)
				{
					feedbackHandler.HandleFeedback("Compatibility Analyser - Header Analysis",Messages.getString("HeaderAnalyserEngine.ErrorPreparingArguments")); 
					done(Status.OK_STATUS);
				}
				ICmdLineCommandExecutor cmdLineExecutor = CmdLineCommandExecutorFactory.CreateOsDependentCommandLineExecutor(this, CompatibilityAnalysisConsole.getInstance());
				stdOutReader = new HeaderAnalysisProgressLineReader();
				progressReader = (HeaderAnalysisProgressLineReader)stdOutReader;
				cmdLineExecutor.runCommand(args, stdOutReader, null, this);
			}
		}catch(UnsupportedOSException e){
			e.printStackTrace();
			feedbackHandler.HandleFeedback("Compatibility Analyser - Header Analysis",e.getMessage());
			done(Status.OK_STATUS);
		}

		return Job.ASYNC_FINISH;
	}

	/**
	 * This method is from the interface ICmdLineCommandExecutorObserver
	 * This gets invoked when the Header Analysis process is completed.
	 */
	public void completed(int status) {
		deleteTempFiles();
		monitor.done();
		done(Status.OK_STATUS);
	}

	public void interrupted(String arg0) {
	}

	public void processCreated(Process pr) {
	}

	public void beginTask(int steps)
	{
		monitor.beginTask(Messages.getString("HeaderAnalyserEngine.RunningHeaderAnalyser"), steps); 
		monitor.subTask("");

		checkForCancellation();
	}
	public void endTask(int exitCode)
	{
		monitor.done();
		
		if(exitCode == 0 || exitCode == 1)
		{
			File reportFile = new File(reportName + ".xml"); 
			
			ArrayList<CompatibilityIssueFileData> headerIssues = ReportFileParser.parseHeaderAnalysisIssues(reportFile.getAbsolutePath());
			int index = CompatibilityAnalyserEngine.reportNames.indexOf(reportFile.getAbsolutePath());

			/*
			 * If report is created with new name, it will be added to the list.
			 * Else if report name already exists in the list, its filteration status 
			 * will be set to 'false', which is the default status for a report.
			 * 
			 */
			if(index ==-1 && reportFile.exists())
			{
				CompatibilityAnalyserEngine.reportNames.add(reportName + ".xml"); 
				ReportData reportData = new ReportData(reportName + ".xml");
				reportData.setTYPE(ReportData.FILE_TYPE.HEADER);
				reportData.setHeaderIssues(headerIssues);
				
				CompatibilityAnalyserEngine.reportObjects.add(reportData);
			}
			else if (index != -1)
			{
				ReportData reportData = CompatibilityAnalyserEngine.reportObjects.get(index);
				reportData.setTYPE(ReportData.FILE_TYPE.HEADER);
				reportData.setFilterationStatus(false);
				reportData.setHeaderIssues(headerIssues);
			}

		}
		else
		{
			File reportFile = new File(reportName + ".xml"); 
			if(reportFile.exists())
				reportFile.delete();
		}

		if(filterationNeeded)
			return;

		deleteTempFiles();
		feedbackHandler.HandleFeedback("Compatibility Analyser - Header Analysis","");
		done(Status.OK_STATUS);
	}

	public void beginFilteration()
	{
		checkForCancellation();

		if(isCancelled)
			return;
		
		monitor.beginTask(Messages.getString("HeaderAnalyserEngine.Filtering.."), 100); 
		monitor.subTask(""); 
	}

	public void progressFilteration(int percent)
	{
		checkForCancellation();

		if(isCancelled)
			return;	
		
		monitor.subTask(percent + Messages.getString("HeaderAnalyserEngine.PercentComplete")); 
		
		monitor.worked(percent - perviousPercent);
		perviousPercent = percent;
	}

	public void endFilteration()
	{
		monitor.done();
		if(++issuesProcessed == totalIssueFiles)
		{
			deleteTempFiles();
			done(Status.OK_STATUS);
			
			int index = CompatibilityAnalyserEngine.reportNames.indexOf(reportName + ".xml");
			if(index != -1)
			{
				ReportData report = (ReportData)CompatibilityAnalyserEngine.reportObjects.get(index);
				report.setFilterationStatus(true);
			}
			feedbackHandler.HandleFeedback("Compatibility Analyser - Header Analysis",""); 
		}
	}
	
	/**
	 * This method checks if monitor is cancelled by user.
	 * If cancelled, it kills the background process and returns;
	 *
	 */
	 void checkForCancellation()
	{
		if(!isCancelled)
			isCancelled = monitor.isCanceled();

		if(isCancelled)
		{
			try 
			{
				int p = progressReader.getPid();
				
				//Cancelling the process using the PID
				String [] args = new String []{"cmd", "/c", "taskkill", "-F", "-PID", Integer.toString(p)};  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				
				Runtime.getRuntime().exec(args);
		
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	/** 
	 * This method is from the Interface ICmdLineCommandExecutorObserver. 
	 * This method is invoked from HeaderAnalyserProgressLineReader class to inform the
	 * progress of the actual Analysis Process. This method inturn updates the progress monitor,
	 * to inform the progress to the user.
	 */
	public void progress(int percentage) {
		
		checkForCancellation();

		if (isCancelled)
			return;
		
		if(progressReader.getCurrentLine() != null)
			monitor.subTask(progressReader.getCurrentLine());

		monitor.worked(percentage - previousProgress);
		previousProgress = percentage;

	}

	/**
	 * This starts the HeaderAnalysis Job. 
	 */
	public void startHA()
	{
		setPriority(Job.SHORT);
		setUser(true);
		schedule();
	}
	/**
	 * This method gets invoked if user selects Filter option.
	 * This method reads the selecetd KnownIssues configuration from the preferences
	 * and writes the KnownIssues path in the given configuration file. 
	 * @param report specifies name of the report being filtered. 
	 */
	private void prepareKnownIssuesPath()
	{
		IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		LastUsedKnownissues issuesData=new LastUsedKnownissues();
		String knownIssuesPath = null;

		try
		{
			if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_ISSUES))
			{
				String[] issuesArr=issuesData.getPreviousValues(LastUsedKnownissues.ValueTypes.LOCAL_ISSUES_PATH);
				totalIssueFiles = issuesArr.length;
				if(issuesArr!=null && issuesArr.length!=0)
				{
					String all=issuesArr[0];
					for (int i = 1; i < issuesArr.length; i++) {
						all = all + ";" + issuesArr[i]; 
					}
					knownIssuesPath = all;
				}
			}

			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_ISSUES))
			{
				totalIssueFiles = 1;
				String corePath = CompatibilityAnalyserPlugin.getInstallPathOfToolsPlugin();
				
				if(corePath == null || corePath.equalsIgnoreCase("")) 
				{
					return;
				}
				else
				{
					File toolsFolder = new File(FileMethods.appendPathSeparator(corePath) + Messages.getString("HeaderAnalyserEngine.BCTools")); 
					if(!toolsFolder.exists())
						return;
					else
						knownIssuesPath = FileMethods.appendPathSeparator(toolsFolder.getAbsolutePath())+ Messages.getString("HeaderAnalyserEngine.data") + File.separator + Messages.getString("HeaderAnalyserEngine.knownissues.xml");   //$NON-NLS-3$
				}
			}
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES))
			{
				String[] issuesArr=issuesData.getPreviousValues(LastUsedKnownissues.ValueTypes.ISSUES_URL);
				totalIssueFiles = issuesArr.length;

				if(issuesArr!=null && issuesArr.length!=0)
				{
					String all=issuesArr[0];
					for (int i = 1; i < issuesArr.length; i++) {
						all=all+";"+issuesArr[i]; 
					}
					knownIssuesPath = all;
				}

			}
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.LATEST_ISSUES))
			{
				totalIssueFiles = 1;
				knownIssuesPath = prefStore.getString(CompatibilityAnalyserPreferencesConstants.KNOWNISSUES_URL);
			}

			FileOutputStream outStream = new FileOutputStream(configFile, true);
			outStream.write((Messages.getString("HeaderAnalyserEngine.ISSUES_FILE") + knownIssuesPath + "\n").getBytes());  

			if(outStream != null)
				outStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	/*
	 * This method deletes config file and headers file
	 * created by the tool
	 */
	private void deleteTempFiles()
	{
		if(configFile != null && configFile.exists())
			configFile.delete();
		if(headersFile != null && headersFile.exists())
			headersFile.delete();
		if(currentForcedHeaders != null && currentForcedHeaders.exists())
			currentForcedHeaders.delete();
		if(baselineForcedHeaders != null && baselineForcedHeaders.exists())
			baselineForcedHeaders.delete(); 
		FileMethods.deleteFolder(temp_folder);
		
		
		//If there is any library analysis files like libs txt file, we should not remove the folder.
		if(config_folder != null)
		{
			File configDir = new File(config_folder);
			
			if(configDir != null && configDir.list().length == 0)
				FileMethods.deleteFolder(config_folder);
		}
	}
	
}

