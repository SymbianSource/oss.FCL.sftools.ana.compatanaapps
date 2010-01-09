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
* Description:
*
*/
package com.nokia.s60tools.compatibilityanalyser.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.LastUsedKnownissues;
import com.nokia.s60tools.compatibilityanalyser.data.ReportData;
import com.nokia.s60tools.compatibilityanalyser.data.ReportWizardData;
import com.nokia.s60tools.compatibilityanalyser.data.ReportData.FILE_TYPE;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalysisConsole;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.util.cmdline.CmdLineCommandExecutorFactory;
import com.nokia.s60tools.util.cmdline.ICmdLineCommandExecutor;
import com.nokia.s60tools.util.cmdline.ICmdLineCommandExecutorObserver;
import com.nokia.s60tools.util.cmdline.ICustomLineReader;
import com.nokia.s60tools.util.cmdline.UnsupportedOSException;
import com.nokia.s60tools.util.python.PythonUtilities;

public class ReportFilterEngine extends Job implements ICmdLineCommandExecutorObserver{
 
	private ReportWizardData data=null;
	private IProgressMonitor monitor=null;
	
	private Process proc;
	private AnalysisFeedbackHandler feedbackHandler;
	private File configFile;
	private ReportFilterLineReader progressReader;
	private ICustomLineReader stdOutReader;
	private String config_folder = null;
	private int perviousPercent = 0;
		
	public ReportFilterEngine(ReportWizardData data,AnalysisFeedbackHandler feedbackHandler) {
		super(Messages.getString("ReportFilterEngine.RunningBcfilter")); //$NON-NLS-1$
		this.data=data;
		this.feedbackHandler = feedbackHandler;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		this.monitor=monitor;
		
		monitor.setTaskName(Messages.getString("ReportFilterEngine.FilteringReports")); //$NON-NLS-1$
		if(data.useDefaultCoreTools)
		{
			data.bcFilterPath = CompatibilityAnalyserPlugin.getDefaltCoretoolsPath();
			if(data.bcFilterPath == null) //$NON-NLS-1$
			{
				feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration",Messages.getString("ReportFilterEngine.ToolsPluginDoesNotContainCoreComponents")); //$NON-NLS-1$
				return Status.OK_STATUS;				
			}					
			monitor.worked(1);			
		}
		else if(data.useWebServerCoreTools)
		{
			String coreToolsExtraction = null;
			
			IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
			String currentUrl = store.getString(CompatibilityAnalyserPreferencesConstants.CORETOOLS_URL);
			if(CompatibilityAnalyserEngine.isDownloadAndExtractionNeeded(currentUrl))
			{
				monitor.setTaskName(Messages.getString("ReportFilterEngine.ExtractingCoretoolsFromWebServer")); //$NON-NLS-1$
				//IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
				
				String previousContents = store.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR);
				if(previousContents != null && !previousContents.equals("")) //$NON-NLS-1$
				{
					File oldContents = new File(previousContents);
					if(oldContents.exists())
						FileMethods.deleteFolder(previousContents);
				}
				
				String targetPath = CompatibilityAnalyserEngine.getWorkspacePath() + File.separator + Messages.getString("ReportFilterEngine.WebServerContentsNew"); //$NON-NLS-1$
				coreToolsExtraction = CompatibilityAnalyserEngine.readAndDownloadSupportedCoretools(currentUrl, targetPath, monitor);
				
				if(coreToolsExtraction == null)
				{
					
					store.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_URL, currentUrl);
					store.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH, CompatibilityAnalyserEngine.getWebServerToolsPath());
					store.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR, targetPath);
					data.bcFilterPath = CompatibilityAnalyserEngine.getWebServerToolsPath() + File.separator;
				}
				else 
				{
					feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration",coreToolsExtraction);
					return Status.OK_STATUS;
				}
				monitor.worked(10);
			}
			else
			{
				IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
				data.bcFilterPath = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH) +File.separator;
			}
		}
		if(!data.bcFilterPath.endsWith("\\"))
			data.bcFilterPath=data.bcFilterPath+"\\";
		
		String pythonPath = null;
		
		pythonPath = PythonUtilities.getPythonPath();
		
		if(pythonPath == null){
			feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration", Messages.getString("ReportFilterEngine.PythonNotFoundError")); //$NON-NLS-1$
			return Status.OK_STATUS;
		}
		
		//Validating the python
		boolean python_validity = PythonUtilities.validatePython(CompatibilityAnalyserEngine.SUPPORTED_PYTHON_VERSION);
		
		if(!python_validity)
	    {
	    	feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration", Messages.getString("CompatibilityAnalyserEngine.InvalidPythonVersion") + CompatibilityAnalyserEngine.SUPPORTED_PYTHON_VERSION);
		    return Status.OK_STATUS;
	    }
	    
	    try {
			//Validating the DATA VERSION of core tools
	    	String dv=CompatibilityAnalyserEngine.getDataVersion(data.bcFilterPath);
			int num = Integer.parseInt(dv);
			if(num != CompatibilityAnalyserPlugin.DATA_VERSION)
			{
				if(!data.bcFilterPath.equalsIgnoreCase(CompatibilityAnalyserPlugin.getDefaltCoretoolsPath()))
					feedbackHandler.reStartFilterationUsingSameData("Compatibility Analyser - Report Filteration",Messages.getString("HeaderAnalyserEngine.CoreToolsAreNotCompatible") + CompatibilityAnalyserPlugin.DATA_VERSION + ".\n\nWould you like to run analysis with the default core tools?");
				else
					feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration","Please update the " + CompatibilityAnalyserPlugin.CORE_COMPONENTS_PLUGIN_ID + " plugin. The one being used is incompatible with the tool.");
			    
				return Status.OK_STATUS;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration","Invalid data version of coretools. Please check the coretools."); //$NON-NLS-1$
			return Status.OK_STATUS;
		}
	    
	    
		data.knownissuesPath = getKnownissuesPath();	
		FileMethods.copyBBCResultsFileIfNotExists(data.outputDir,data.bcFilterPath);
		if(data.bcFilterPath !=null && data.knownissuesPath!=null)
		{
			config_folder = data.getconfigfolder();
			configFile = new File(config_folder+File.separator+ Messages.getString("ReportFilterEngine.bcfilter_config.cf")); //$NON-NLS-1$
			if(configFile.exists())
				configFile.delete();
			try {
				FileOutputStream outStream = new FileOutputStream(configFile, true);
				
				outStream.write((Messages.getString("ReportFilterEngine.TEMP")+ data.bcFilterPath + Messages.getString("ReportFilterEngine.temp")).getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
				
				Iterator<String> itr=data.reportFiles.iterator();
				
				StringBuffer inputfiles = new StringBuffer() ; //$NON-NLS-1$
				StringBuffer outputfiles = new StringBuffer(); //$NON-NLS-1$
				while(itr.hasNext())
				{
						File file=new File(itr.next());
						outputfiles = outputfiles.append(data.outputDir+File.separator+file.getName());
						outputfiles = outputfiles.append(";"); //$NON-NLS-1$
						inputfiles = inputfiles.append(file.getAbsolutePath());
						inputfiles = inputfiles.append(";"); //$NON-NLS-1$
				}
				inputfiles.deleteCharAt(inputfiles.lastIndexOf(";"));
				outputfiles.deleteCharAt(outputfiles.lastIndexOf(";"));
				
				outStream.write((Messages.getString("ReportFilterEngine.REPORT_FILE_FILTER")+ inputfiles + "\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
				outStream.write((Messages.getString("ReportFilterEngine.OUTPUT_FILE_FILTER")+ outputfiles + "\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
				outStream.write((Messages.getString("ReportFilterEngine.ISSUES_FILE")+ data.knownissuesPath + "\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
				
				String [] args = {pythonPath, "\""+data.bcFilterPath + Messages.getString("ReportFilterEngine.Checkbc.py")+"\"", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"\""+configFile.getAbsolutePath()+"\"","-c" ,"-f"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				
				ICmdLineCommandExecutor cmdLineExecutor = CmdLineCommandExecutorFactory.CreateOsDependentCommandLineExecutor(this, CompatibilityAnalysisConsole.getInstance());
				stdOutReader = new ReportFilterLineReader();
				progressReader = (ReportFilterLineReader)stdOutReader;
				cmdLineExecutor.runCommand(args, stdOutReader, null, this);
				
				
				
			} catch (FileNotFoundException e) {
				feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration",e.getMessage());
			} catch (IOException e) {
				feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration",e.getMessage());
			} catch (UnsupportedOSException e) {
				feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration",e.getMessage());
			} 
			
		return Job.ASYNC_FINISH;
		}
		else
		{
			return Status.OK_STATUS;
		}
	}
	
	/**
	 * This method invokes the Report Filteration Job.
	 *
	 */
	public void startAnalysis()
	{
		setPriority(Job.SHORT);
		setUser(true);
		schedule();
	}

	public void completed(int status) {

		feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration",""); //$NON-NLS-1$
		deleteTempFiles();
		monitor.done();
		
		if(monitor.isCanceled())
			done(Status.CANCEL_STATUS);
		else
			done(Status.OK_STATUS);
	}

	public void interrupted(String arg0) {
	}

	public void processCreated(Process arg0) {
		proc = arg0;		
	}

	public void progress(int percentage) {
	}
	
	public void beginFilteration()
	{
		//monitor.done();
		if(monitor.isCanceled())
		{
			try{
			int p = ((ReportFilterLineReader)stdOutReader).getPid();
		
			String [] args = new String []{"cmd", "/c", "taskkill", "-F", "-PID", Integer.toString(p)}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			
			Runtime.getRuntime().exec(args);
			done(Status.OK_STATUS);
			}catch(Exception e){
				e.printStackTrace();			
			}
		}
		
		if(progressReader.currentFile!=null)
			monitor.beginTask(Messages.getString("ReportFilterEngine.Filtering")+progressReader.currentFile, 100); //$NON-NLS-1$
		monitor.subTask(""); //$NON-NLS-1$
	}
	
	public void progressFilteration(int percent)
	{
		if(monitor.isCanceled())
		{
			try{
				int p = ((ReportFilterLineReader)stdOutReader).getPid();
			
				String [] args = new String []{"cmd", "/c", "taskkill", "-F", "-PID", Integer.toString(p)}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				
				Runtime.getRuntime().exec(args);
				done(Status.OK_STATUS);
				}catch(Exception e){
					e.printStackTrace();			
				}
			
			proc.destroy();
			feedbackHandler.HandleFeedback("Compatibility Analyser - Report Filteration",""); //$NON-NLS-1$
		}
		monitor.subTask(percent + Messages.getString("ReportFilterEngine.PercentComplete")); //$NON-NLS-1$
		monitor.worked(percent - perviousPercent);
		perviousPercent = percent;
	}
	public void endFilteration(String code)
	{
		monitor.done();
		
		if(code.endsWith("0")) //$NON-NLS-1$
		{
			deleteTempFiles();
			int index = CompatibilityAnalyserEngine.reportNames.indexOf(progressReader.outputFile);
			
			ArrayList<CompatibilityIssueFileData> compatibilityIssues = null;
			
			FILE_TYPE fileType = ReportFileParser.fetchReportFileType(progressReader.outputFile);
			
			if(fileType == FILE_TYPE.HEADER) 
				compatibilityIssues = ReportFileParser.parseHeaderAnalysisIssues(progressReader.outputFile);
			else if(fileType == FILE_TYPE.LIBRARY)
				compatibilityIssues	 = ReportFileParser.parseLibraryAnalysisIssues(progressReader.outputFile);
			
			if(index == -1)
			{
				CompatibilityAnalyserEngine.reportNames.add(progressReader.outputFile);
				
				ReportData report = new ReportData(progressReader.outputFile);
				report.setTYPE(fileType);
				report.setFilterationStatus(true);
				
				if(report.getTYPE() == FILE_TYPE.HEADER && compatibilityIssues != null)
					report.setHeaderIssues(compatibilityIssues);
				else if(report.getTYPE() == FILE_TYPE.LIBRARY && compatibilityIssues != null)
					report.setLibraryIssues(compatibilityIssues);
				
				CompatibilityAnalyserEngine.reportObjects.add(report);
			
			}
			else
			{
				ReportData report = CompatibilityAnalyserEngine.reportObjects.get(index);
				report.setFilterationStatus(true);
				
				if(report.getTYPE() == FILE_TYPE.HEADER && compatibilityIssues != null)
					report.setHeaderIssues(compatibilityIssues);
				else if(report.getTYPE() == FILE_TYPE.LIBRARY && compatibilityIssues != null)
					report.setLibraryIssues(compatibilityIssues);
			}
			
		}
				
		progressReader.currentFile=null;
		
	}
	public void forcedShutdown() {
		proc.destroy();
		deleteTempFiles();
		done(Status.OK_STATUS);
	}
	/**
	 * This method returns the path of the selected knownissues from the knownissues dialog.
	 * i.e, If default is selected, returns the knownissues path from the core tools provided by the tool
	 * And if the local issues is selected, returns the path provided in the combo
	 * And if the web server issues is selected, returns the path of the issues files which is extracted from webserver
	 * And if latest issues is selected, returns the path of the latest issues file which is extracted from webserver    
	 * @return string path of the issues file. 
	 */
	public String getKnownissuesPath()
	{
			LastUsedKnownissues issuesData=new LastUsedKnownissues();
			
			IPreferenceStore prefStore=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
			if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_ISSUES))
			{
				String[] issuesArr=issuesData.getPreviousValues(LastUsedKnownissues.ValueTypes.LOCAL_ISSUES_PATH);
				if(issuesArr!=null && issuesArr.length!=0)
				{
					String all=issuesArr[0];
					for (int i = 1; i < issuesArr.length; i++) {
						all=all+";"+issuesArr[i]; //$NON-NLS-1$
					}
			    	data.knownissuesPath = all;
			    
				}
			}
			
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_ISSUES))
			{
				String corePath = CompatibilityAnalyserPlugin.getInstallPathOfToolsPlugin();
				
				data.knownissuesPath = corePath + File.separator + Messages.getString("ReportFilterEngine.Bctools") + File.separator + Messages.getString("ReportFilterEngine.data") + File.separator + Messages.getString("ReportFilterEngine.knownissues.xml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES))
			{
				String[] issuesArr=issuesData.getPreviousValues(LastUsedKnownissues.ValueTypes.ISSUES_URL);
				if(issuesArr!=null && issuesArr.length!=0)
				{
					String all = issuesArr[0];
					for (int i = 1; i < issuesArr.length; i++) {
						all = all+";"+issuesArr[i]; //$NON-NLS-1$
					}
					data.knownissuesPath = all;
				}
				
			}
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.LATEST_ISSUES))
			{
				data.knownissuesPath = prefStore.getString(CompatibilityAnalyserPreferencesConstants.KNOWNISSUES_URL);
			}
		
		return data.knownissuesPath;
	}
	private void deleteTempFiles()
	{
		//Delete config file
		if(configFile != null && configFile.exists())
			configFile.delete();
		//Delete config folder
		FileMethods.deleteFolder(config_folder);
	}
	
}
