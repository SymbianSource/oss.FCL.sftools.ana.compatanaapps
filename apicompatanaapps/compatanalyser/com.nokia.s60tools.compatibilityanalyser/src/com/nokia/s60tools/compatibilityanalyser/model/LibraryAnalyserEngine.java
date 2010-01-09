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
* Description: Job class to run OrdinalAnalysis. Also, responsible
* to parse OrdinalAnalysis output and to inform the progress to user.
*
*/
package com.nokia.s60tools.compatibilityanalyser.model;

import java.io.File;
import java.io.FileNotFoundException;
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
 * Job class for OrdinalAnalysis.
 * This class parses the output from core OrdinalAnalysis process 
 * and informs progress to the user. 
 *
 */
public class LibraryAnalyserEngine extends Job implements ICmdLineCommandExecutorObserver
{

	private CompatibilityAnalyserEngine engine;
	private IProgressMonitor monitor;
	private int previousProgress = 0;
	private int perviousPercent = 0;
	private int totalIssueFiles = 0;	
	private int issuesProcessed = 0;
	
	private boolean isCancelled = false;
	private boolean filterationNeeded = false;
	
	private String reportName;
	private ICmdLineCommandExecutor cmdLineExecutor;
	
	private String pythonPath;
	private AnalysisFeedbackHandler feedbackHandler;
	private BaselineSdkData baselineData;
	
	private ICustomLineReader stdOutReader;
	private LibraryAnalysisProgressLineReader progressReader;
	private File configFile;
	private File libFiles;
	private String config_folder = null;
	private String temp_folder;
	
	/**
	 * Constructor of the Job.
	 * @param engine contains the currentsdk and baselinesdk data needed for HA
	 * @param feedbackHandler will be notified of ordinal analysis completion.
	 */
	public LibraryAnalyserEngine (CompatibilityAnalyserEngine engine, AnalysisFeedbackHandler feedbackHandler){
		super(Messages.getString("LibraryAnalyserEngine.AnalysingLibraries")); //$NON-NLS-1$
		this.engine = engine;
		this.feedbackHandler = feedbackHandler;
		isCancelled = false;
		filterationNeeded = engine.currentSdk.filterNeeded;		
	}
	
	/**
	 * This method validates the Current SDK and Baseline SDK data and
	 * throws back error messages to UI.
	 * Also, it prepares the config file used in invoking OC through Checkbc
	 * @param currentData specifies the Current SDK data
	 * @param baselineData specifies the Baseline SDK data
	 * @returns any error in the Input
	 */
	public String readAndCheckInput(ProductSdkData currentData, BaselineSdkData baselineData){
		
		pythonPath = PythonUtilities.getPythonPath();
		
		if(pythonPath == null)
			return Messages.getString("LibraryAnalyserEngine.PythonNotFoundError"); //$NON-NLS-1$
		
		boolean python_validity = PythonUtilities.validatePython(CompatibilityAnalyserEngine.SUPPORTED_PYTHON_VERSION);
	    
	    if(!python_validity)
		    return Messages.getString("CompatibilityAnalyserEngine.CompatibilityAnalyserEngine.InstallValidPython") + CompatibilityAnalyserEngine.SUPPORTED_PYTHON_VERSION;
	 	
	    if(baselineData == null)
			return Messages.getString("LibraryAnalyserEngine.BaselineProfileNotFoundError"); //$NON-NLS-1$
			
		String coreToolsExtraction = null;
		
		if(currentData.useWebServerCoreTools)
		{
			if(CompatibilityAnalyserEngine.isDownloadAndExtractionNeeded(currentData.urlPathofCoreTools))
			{
				IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
				
				//If coretools are downloaded earlier from webserver, they will be deleted
				String previousContents = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR);
				if(previousContents != null && !previousContents.equals("")) //$NON-NLS-1$
				{
					File oldContents = new File(previousContents);
					if(oldContents.exists())
						FileMethods.deleteFolder(previousContents);
				}
				
				String targetPath = CompatibilityAnalyserEngine.getWorkspacePath() + File.separator + Messages.getString("LibraryAnalyserEngine.WebServerContentsNew"); //$NON-NLS-1$
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
			
			if(corePath.equalsIgnoreCase("")) //$NON-NLS-1$
			{
				return Messages.getString("LibraryAnalyserEngine.ToolsPluginNotFoundError"); //$NON-NLS-1$
			}
			else
			{
				File toolsFolder = new File(FileMethods.appendPathSeparator(corePath) + Messages.getString("LibraryAnalyserEngine.Bctools")); //$NON-NLS-1$
				if(!toolsFolder.exists())
					return new String(Messages.getString("LibraryAnalyserEngine.CoreComponentsNotFoundInPluginError")); //$NON-NLS-1$
				currentData.coreToolsPath = FileMethods.appendPathSeparator(corePath) + Messages.getString("LibraryAnalyserEngine.Bctools") + File.separator; //$NON-NLS-1$
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
						feedbackHandler.reStartAnalysisUsingSameData("Compatibility Analyser - Library Analysis", Messages.getString("HeaderAnalyserEngine.CoreToolsAreNotCompatible") + CompatibilityAnalyserPlugin.DATA_VERSION + ".\n\nWould you like to run analysis with the default core tools?");
						return "";
					}
					return "Please update the " + CompatibilityAnalyserPlugin.CORE_COMPONENTS_PLUGIN_ID + " plugin. The one being used is incompatible with the tool.";
				}
			} catch (Exception e1) {
				return Messages.getString("LibraryAnalyserEngine.InvalidDataVersion"); //$NON-NLS-1$
			}
		    
			
			try 
			{
				config_folder = engine.getCurrentSdkData().getconfigfolder();
				configFile = new File(config_folder+File.separator+ Messages.getString("LibraryAnalyserEngine.carbide_oc.cf"));
				temp_folder = config_folder + File.separator + "temp";
				if(configFile.exists())
					configFile.delete();
			
				if(!configFile.createNewFile())
				{
					return Messages.getString("LibraryAnalyserEngine.CouldNotFindConfigFile"); //$NON-NLS-1$
				}
				FileOutputStream outStr = new FileOutputStream(configFile);
			
				outStr.write((Messages.getString("LibraryAnalyserEngine.CURRENT_NAME") + currentData.productSdkName + "\n").getBytes());
				outStr.write((Messages.getString("LibraryAnalyserEngine.CURRENT_SDK_DIR") + currentData.epocRoot + "\n").getBytes());   
				outStr.write((Messages.getString("LibraryAnalyserEngine.CURRENT_SDK_S60_VERSION") + currentData.productSdkVersion + "\n").getBytes());   
			
				outStr.write((Messages.getString("LibraryAnalyserEngine.BASELINE_NAME") + baselineData.baselineSdkName + "\n").getBytes());   
				outStr.write((Messages.getString("LibraryAnalyserEngine.BASELINE_SDK_DIR") + baselineData.epocRoot + "\n").getBytes());   
				outStr.write((Messages.getString("LibraryAnalyserEngine.BASELINE_SDK_S60_VERSION") + baselineData.baselineSdkVersion + "\n").getBytes());   
			
				outStr.write((Messages.getString("LibraryAnalyserEngine.TOOLCHAIN") + currentData.toolChain +  "\n").getBytes());   
							
				outStr.write((Messages.getString("LibraryAnalyserEngine.CURRENT_IMPORTLIBRARIES") + FileMethods.convertToOneString(currentData.currentLibsDir) + "\n").getBytes());
				outStr.write((Messages.getString("LibraryAnalyserEngine.CURRENT_IMPORTDLLS") + FileMethods.convertToOneString(currentData.currentDllDir) + "\n").getBytes());
				outStr.write((Messages.getString("LibraryAnalyserEngine.BASELINE_IMPORTLIBRARIES") + FileMethods.convertToOneString(baselineData.baselineLibsDir) + "\n").getBytes());
				outStr.write((Messages.getString("LibraryAnalyserEngine.BASELINE_IMPORTDLLS") + FileMethods.convertToOneString(baselineData.baselineDllDir) + "\n").getBytes());
				outStr.write((Messages.getString("LibraryAnalyserEngine.TOOLCHAIN_PATH") + currentData.toolChainPath + "\n").getBytes());   
				reportName = currentData.reportPath + File.separator + Messages.getString("LibraryAnalyserEngine.Libraries_") + currentData.reportName; 
				outStr.write((Messages.getString("LibraryAnalyserEngine.REPORT_FILE_LIBRARIES") + currentData.reportPath + File.separator + Messages.getString("LibraryAnalyserEngine.Libraries_") + currentData.reportName + "\n").getBytes());    //$NON-NLS-3$
				outStr.write((Messages.getString("LibraryAnalyserEngine.TEMP") + temp_folder + "\n").getBytes());    //$NON-NLS-3$
				
				if(outStr != null)
					outStr.close();
			
				if(currentData.filterNeeded)
					getKnonwIssuesData();
				
			}catch (FileNotFoundException e) {
				e.printStackTrace();
				return e.getMessage();
			}catch(IOException e1){
				e1.printStackTrace();
				return e1.getMessage();
			}catch(Exception e2){
				e2.printStackTrace();
				return e2.getMessage();
			}
		}
		else
			return coreToolsExtraction;
		
		return null;
	}
	
	/**
	 * This method prepares all arguments used to invoke OC throigh Checkbc.
	 * @param currentSdk specifies the data related to Current SDK.
	 * @return an array of strings used as arguments to Checkbc
	 */
 	public String [] prepareOCArguments(ProductSdkData currentData)
	{
		Vector<String> argsList = new Vector<String>();
			
		argsList.add("cmd"); 
		argsList.add("/c"); 
		
		argsList.add(pythonPath);
		argsList.add("\"" + FileMethods.appendPathSeparator(currentData.coreToolsPath) + Messages.getString("LibraryAnalyserEngine.checkbc.py") + "\"");    //$NON-NLS-3$
		argsList.add("\"" + config_folder+File.separator+ Messages.getString("LibraryAnalyserEngine.carbide_oc.cf") + "\"");    //$NON-NLS-3$
		argsList.add("-c"); 
						
		if(currentData.analyzeAllLibs)
			argsList.add("-la"); 
		else if(currentData.libraryFilesList.length == 1)
		{
			argsList.add("-ls"); 
			argsList.add(currentData.libraryFilesList[0]);
		}
		else
		{
			argsList.add("-lm"); 
			try
			{
				libFiles = new File(config_folder+File.separator + Messages.getString("LibraryAnalyserEngine.carbide_libs.txt"));
				if(libFiles.exists())
					libFiles.delete();
			
				if(!libFiles.createNewFile())
				{
					return null;
				}
				else
				{
					FileOutputStream outStr = new FileOutputStream(libFiles);
					for(String s: currentData.libraryFilesList)
						outStr.write((s + "\n").getBytes()); 
					
					if(outStr != null)
						outStr.close();
				}
				
				argsList.add("\"" + libFiles.getAbsolutePath() + "\"");   
								
			}catch(Exception e){
				return null;
			}
		}
		FileMethods.copyBBCResultsFileIfNotExists(currentData.reportPath,currentData.coreToolsPath);
		if(currentData.filterNeeded)
		{
			argsList.add("-f"); 
		}
				
		return (String [])argsList.toArray(new String[argsList.size()]);
	}

 	/*
	 * This method gets invoked when the process related to Ordinal Analysis gets completed.
	 * This is due to the implementation of the Interface ICmdLineCommandExecutorObserver.
	 */
	public void completed(int status) {
		deleteTempFiles();
		monitor.done();
		done(Status.OK_STATUS);
	}

	public void interrupted(String message) {
	}

	public void processCreated(Process proc) {
	}

	public void beginTask(int steps)
	{
		checkForCancellation();
				
		if(isCancelled)
		{
			done(Status.OK_STATUS);
			return;
		}
		monitor.beginTask(Messages.getString("LibraryAnalyserEngine.RunningLibraryAnalysis"), steps); 
		monitor.subTask("");
	}
	public void endTask(int exitCode)
	{
		if(!isCancelled)
			isCancelled = monitor.isCanceled();
				
		monitor.done();
				
		if(exitCode == 0)
		{
			File reportFile = new File(reportName + ".xml"); 
			int index = CompatibilityAnalyserEngine.reportNames.indexOf(reportName+".xml");
			
			ArrayList<CompatibilityIssueFileData> libraryIssues = ReportFileParser.parseLibraryAnalysisIssues(reportFile.getAbsolutePath());
			
			/*
			 * If report is created with new name, it will be added to the list.
			 * Else if report name already exists in the list, its filteration status 
			 * will be set to 'false', which is the default status for a report.
			 * 
			 */
			if(index == -1 && reportFile.exists()) 
			{
				CompatibilityAnalyserEngine.reportNames.add(reportName + ".xml"); 
				ReportData reportData = new ReportData(reportName + ".xml");
				reportData.setTYPE(ReportData.FILE_TYPE.LIBRARY);
				reportData.setLibraryIssues(libraryIssues);
				
				CompatibilityAnalyserEngine.reportObjects.add(reportData);
			}
			else
			{
				ReportData reportData = CompatibilityAnalyserEngine.reportObjects.get(index);
				reportData.setTYPE(ReportData.FILE_TYPE.LIBRARY);
				reportData.setFilterationStatus(false);
				reportData.setLibraryIssues(libraryIssues);
			}
			
		}
		else
		{
			File reportFile = new File(reportName + Messages.getString("LibraryAnalyserEngine.82")); 
			if(reportFile.exists())
				reportFile.delete();
		}
		
		if(filterationNeeded)
			return;
		
		deleteTempFiles();
		feedbackHandler.HandleFeedback("Compatibility Analyser - Library Analysis",""); 
		if(engine.isHeaderAnalysisChecked())
			feedbackHandler.startHA(engine);
		
		done(Status.OK_STATUS);
	}
	
	public void beginFilteration()
	{
		checkForCancellation();
		
		if(isCancelled)
			return;
		
		monitor.beginTask(Messages.getString("LibraryAnalyserEngine.FilteringReportFile.."), 100); 
		monitor.subTask(""); 
	}
	
	public void progressFilteration(int percent)
	{
		checkForCancellation();
		
		if(isCancelled)
			return;
		
		monitor.subTask(percent + Messages.getString("LibraryAnalyserEngine.PercentComplete")); 
    
		monitor.worked(percent - perviousPercent);
		perviousPercent = percent;
	}
	public void endFilteration()
	{
		monitor.done();
		
		if(++issuesProcessed == totalIssueFiles)
		{
			deleteTempFiles();
		
			int index = CompatibilityAnalyserEngine.reportNames.indexOf(reportName + ".xml");
			if(index != -1)
			{
				ReportData report = (ReportData)CompatibilityAnalyserEngine.reportObjects.get(index);
				report.setFilterationStatus(true);
			}
			feedbackHandler.HandleFeedback("Compatibility Analyser - Library Analysis",""); 
			if(engine.isHeaderAnalysisChecked())
				feedbackHandler.startHA(engine);
			done(Status.OK_STATUS);
		}
	}
	
	/* 
	 * This method is invoked from LibraryAnalyserProgressLineReader class to inform the
	 * progress of the actual Analysis Process. This method inturn updates the progress monitor,
	 * to inform the progress to the user.
	 * This is due to the implementation of ICmdLineCommandExecutorObserver Interface.
	 */
	public void progress(int progress) {
		checkForCancellation();
		
		if (isCancelled)
       		return;
               
        if(progressReader.getCurrentLine() != null)
        	monitor.subTask(progressReader.getCurrentLine());
        
		monitor.worked(progress - previousProgress);
		previousProgress = progress;
		
     }
		
	private void checkForCancellation()
	{
		if(!isCancelled)
			isCancelled = monitor.isCanceled();
				
        if (isCancelled)
        {
        	try 
        	{
        		int p = progressReader.getPid();
        		        	
        		String [] args = new String []{"cmd", "/c", "taskkill", "-F", "-PID", Integer.toString(p)};    //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        	
				Runtime.getRuntime().exec(args);
			        	
        	} catch (IOException e) {
        		e.printStackTrace();
			}
        }
	}	
	/**
	 * This method invokes all helper methods, to read BaselineProfile data, to validate
	 * given input, and to prepare all arguments used to invoke OC through Checkbc.
	 * And it invokes the actual Execution command to run the analysis.
	 */
	protected IStatus run(IProgressMonitor monitor) {
		
		this.monitor = monitor;
		
		try
		{			
			Object obj = BaselineProfileUtils.getBaselineProfileData(engine.getBaselineProfile());
			
			if(!(obj instanceof BaselineProfile)) {
				feedbackHandler.HandleFeedback("Compatibility Analyser - Library Analysis",Messages.getString("LibraryAnalyserEngine.GivenBaselineProfileDoesnotExist")); 
				return Status.OK_STATUS;
			}
			else
			{
				baselineData = engine.readBaselineSdkData((BaselineProfile)obj);
			}
			
			String status = readAndCheckInput(engine.currentSdk, baselineData);
			
			if(status != null)
			{
				feedbackHandler.HandleFeedback("Compatibility Analyser - Library Analysis",status);
				return Status.OK_STATUS;
			}
			else
			{
				String [] args = prepareOCArguments(engine.currentSdk);
			
				if(args == null)
				{
					feedbackHandler.HandleFeedback("Compatibility Analyser - Library Analysis",Messages.getString("LibraryAnalyserEngine.ErrorPreparingArguments")); 
					done(Status.OK_STATUS);
				}
						
				cmdLineExecutor = CmdLineCommandExecutorFactory.CreateOsDependentCommandLineExecutor(this, CompatibilityAnalysisConsole.getInstance());
				stdOutReader = new LibraryAnalysisProgressLineReader();
				progressReader = (LibraryAnalysisProgressLineReader)stdOutReader;
				cmdLineExecutor.runCommand(args, stdOutReader, null, this);
			}
						
		}catch(UnsupportedOSException e){
			e.printStackTrace();
			feedbackHandler.HandleFeedback("Compatibility Analyser - Library Analysis",e.getMessage());
			done(Status.OK_STATUS);
		}
		return Job.ASYNC_FINISH;
	}
	
	/**
	 * This starts the OrdinalAnalysis Job. 
	 */
	public void startOC()
	{
		setPriority(Job.SHORT);
		setUser(true);
		schedule();
		
	}

	/**
	 * This method gets invoked after the Ordinal Analysis, if user selects Filter option.
	 * This method filters the given report file.
	 * This method reads the selecetd KnownIssues configuration from the preferences
	 * and prepares the config file with Report Path and KnownIssues path. 
	 * @param report specifies name of the report being filtered. 
	 */
	private void getKnonwIssuesData()
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
					File toolsFolder = new File(FileMethods.appendPathSeparator(corePath) + Messages.getString("LibraryAnalyserEngine.Bctools")); 
					if(!toolsFolder.exists())
						return;
					else
						knownIssuesPath = FileMethods.appendPathSeparator(toolsFolder.getAbsolutePath()) + Messages.getString("LibraryAnalyserEngine.data") + File.separator + Messages.getString("LibraryAnalyserEngine.knownissues.xml");    //$NON-NLS-3$
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
		outStream.write((Messages.getString("LibraryAnalyserEngine.ISSUES_FILE") + knownIssuesPath + "\n").getBytes());   
		
		if(outStream != null)
			outStream.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteTempFiles()
	{
		if(configFile != null && configFile.exists())
			configFile.delete();
		FileMethods.deleteFolder(temp_folder);			
		
		if(!engine.isHeaderAnalysisChecked())
		{
			if(config_folder != null && new File(config_folder).list().length == 0)
			{
				FileMethods.deleteFolder(config_folder);
			}
		}
		
		//In OC report file, names of files being analysed are not displayed.
		//So, if this temporary file is deleted soon after the analysis, then
		//user cannot check what are all the files analysed.
		/*if(libFiles != null && libFiles.exists())
			libFiles.delete(); */
	}
}

