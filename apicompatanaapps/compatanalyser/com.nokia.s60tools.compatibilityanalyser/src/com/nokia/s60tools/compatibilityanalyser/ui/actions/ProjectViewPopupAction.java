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
* Description: Action class for the menu item,
* "Run static analysis for this project..."
*
*/
package com.nokia.s60tools.compatibilityanalyser.ui.actions;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.DefaultViewConfiguration;
import com.nokia.carbide.cdt.builder.EpocEngineHelper;
import com.nokia.carbide.cdt.builder.EpocEnginePathHelper;
import com.nokia.carbide.cdt.builder.ICarbideBuildManager;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.epoc.engine.model.BldInfModelFactory;
import com.nokia.carbide.cpp.epoc.engine.model.bldinf.IBldInfData;
import com.nokia.carbide.cpp.epoc.engine.model.bldinf.IBldInfOwnedModel;
import com.nokia.carbide.cpp.epoc.engine.model.bldinf.IBldInfView;
import com.nokia.carbide.cpp.epoc.engine.model.bldinf.IExport;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.AnalysisWizard;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;

/**
 * This is the action class bound with the popup menu action
 * "Run static analysis for this project..."
 * 
 */
public class ProjectViewPopupAction implements IWorkbenchWindowActionDelegate {

	//private ICarbideProjectInfo prjInfo;
	
	private CompatibilityAnalyserEngine engine;
	private ISelection selection;
	private boolean isCancelled = false; 
		
	public ProjectViewPopupAction() {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		
		engine = new CompatibilityAnalyserEngine();
		ProductSdkData currentSdk = new ProductSdkData();
		engine.setCurrentSdkData(currentSdk);
		isCancelled = false;
			
		IRunnableWithProgress projProgInfo = new IRunnableWithProgress(){

			public void run(IProgressMonitor monitor) {
				
				try{
					getSettingsForGivenSelection(selection, engine, monitor);
					
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
					
			}
					
		};
		
		try
    	{
			new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, projProgInfo);
		}catch(Exception ex){
		   ex.printStackTrace();
		} 
			
		if(isCancelled)
			return ;
	
		if(engine.isLibraryAnalysisChecked())
		{
			currentSdk.analyzeAllLibs = false;
		}
		engine.setCurrentSdkData(currentSdk);
		
		Runnable showWizardRunnable = new Runnable(){
			public void run(){
	  		  WizardDialog wizDialog;
	  		  //CompatibilityAnalyserEngine engine = new CompatibilityAnalyserEngine();
	  		
			  AnalysisWizard wiz = new AnalysisWizard(engine);
	       	  wizDialog = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
			  wizDialog.create();		
			  wizDialog.getShell().setSize(wizDialog.getShell().getSize().x, wizDialog.getShell().getSize().y+70);
			  wizDialog.addPageChangedListener(wiz);
			  try{
			  wizDialog.open();
			  }catch(Exception e)
			  {				  
				  e.printStackTrace();
			  }
			 }
		   };
		   
		  Display.getDefault().syncExec(showWizardRunnable); 
				  
	}
	/**
	 * Adds .dso file to the list,If the corresponding dso file of dll project exists. 
	 * @param filePath path of dll file.
	 */
	public static boolean addLibIfThisMMPisDll(IPath filePath,ICarbideProjectInfo prjInfo, ArrayList<String> libsList)
	{	
		String epocRoot = FileMethods.convertForwardToBackwardSlashes(prjInfo.getDefaultConfiguration().getSDK().getEPOCROOT());
		
		String targetPath = FileMethods.appendPathSeparator(epocRoot)+ CompatibilityAnalyserEngine.ARMV5_PATH;
		targetPath = FileMethods.appendPathSeparator(targetPath);
		
		java.io.File armv5lib=new File(targetPath + filePath.removeFileExtension().addFileExtension(Messages.getString("ProjectViewPopupAction.dso")).toFile().getName()); //$NON-NLS-1$
				
		if(filePath.getFileExtension().equalsIgnoreCase(Messages.getString("ProjectViewPopupAction.dll"))) //$NON-NLS-1$
		{
			if(armv5lib.exists())
				libsList.add(armv5lib.getName());
			
			return true;
		}
		else
			return false;
		
	}
	
	private void getSettingsForGivenSelection(ISelection selection, CompatibilityAnalyserEngine engine, IProgressMonitor monitor)
	{
		ProductSdkData currentSdk = engine.getCurrentSdkData();
		//isCancelled = false;
		
		IProject project = null;
		ICarbideProjectInfo prjInfo = null;
		
		IPath mmpPath = null;
		
		ArrayList <String> usrIncList = new ArrayList<String>();
		ArrayList<String> sysIncList = new ArrayList<String>();
		ArrayList<String> hdrsList = new ArrayList<String>();
		ArrayList<String> libsList = new ArrayList<String>();
		
		 if(selection instanceof IStructuredSelection)
		{
			Object firstElem = ((IStructuredSelection)selection).getFirstElement();
			
			if(firstElem instanceof IProject)
			{		
				project = (IProject)firstElem;
			}
			else if(firstElem instanceof IFolder) {
				project = ((IFolder)firstElem).getProject();
			}
			else if(firstElem instanceof IFile)
			{
				project = ((IFile)firstElem).getProject();
								
				mmpPath = ((IFile)firstElem).getProjectRelativePath();
			}
		}
		
		ICarbideBuildManager mngr = CarbideBuilderPlugin.getBuildManager();
		prjInfo = mngr.getProjectInfo(project);
		if(project != null && !project.isOpen())
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.getString("ProjectViewPopupAction.CompatibilityAnalyserError"), "Selected Project is not opened.");
			return;
		}
		if(project!=null && mngr.isCarbideProject(project))
			prjInfo = mngr.getProjectInfo(project);
		else
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.getString("ProjectViewPopupAction.CompatibilityAnalyserError"), Messages.getString("ProjectViewPopupAction.CouldNotReadInfoFromProject"));
			return;
		}
		
		if(monitor != null)
			monitor.beginTask("Reading Project Data ...", 10);
		
		if(selection instanceof TextSelection || ((selection instanceof IStructuredSelection) && (((IStructuredSelection)selection).getFirstElement() instanceof IFile))) {
			
			getIncludesFromThisMMP(prjInfo, mmpPath, usrIncList, sysIncList);
			
			if(monitor != null && monitor.isCanceled()){
				isCancelled = true;
				monitor.done();
				return;
			}
			else if(monitor != null)
				monitor.worked(2);
			
			getHeadersFromUsrIncPaths(usrIncList, hdrsList, monitor);
			if(containsDllsAsTargets(prjInfo, mmpPath, libsList))
				engine.setLibraryAnalysis(true);
			
			if(monitor != null){
				if(monitor.isCanceled()){
					isCancelled = true;
					monitor.done();
				}
				else
					monitor.worked(8);
			}
		}
		else
		{
			getIncludesFromThisProject(prjInfo, usrIncList, sysIncList);
			
			if(monitor != null && monitor.isCanceled()){
				isCancelled = true;
				monitor.done();
				return;
			}
			else if(monitor != null)
				monitor.worked(2);
			
			getHeadersFromUsrIncPaths(usrIncList, hdrsList, monitor);
			if(projectContainsDllsAsTargets(prjInfo, libsList))
				engine.setLibraryAnalysis(true);
			
			if(monitor != null){
				if(monitor.isCanceled()){
					isCancelled = true;
					monitor.done();
				}
				else
					monitor.worked(8);
			}
			
			getExportPathsAndFiles(prjInfo, usrIncList, hdrsList);
			if(monitor != null){
				if(monitor.isCanceled()){
					isCancelled = true;
					monitor.done();
				}
				else
					monitor.worked(10);
			}
		}
		 
		 if(monitor != null)
		 {
			monitor.done();
			
		 }
		 currentSdk.isOpenedFromProject = true;
		 currentSdk.productSdkName = prjInfo.getDefaultConfiguration().getSDK().getUniqueId();
		 currentSdk.projectName = project.getName();
		 currentSdk.forcedHeaders = CompatibilityAnalyserEngine.getIncludesFromSrcFiles(project);
		 currentSdk.currentHeaderDir = usrIncList.toArray(new String[0]);
		 Collections.sort(hdrsList);
		 currentSdk.HeaderFilesList = hdrsList.toArray(new String[0]);
		 currentSdk.currentIncludes  = sysIncList.toArray(new String[0]);

		 currentSdk.defaultHeaderDir = false;
		 currentSdk.analyseAll = false;
		 currentSdk.useRecursive = true;
		 currentSdk.usePlatformData = false;
			
		 if(currentSdk.forcedHeaders != null)
			 currentSdk.isForcedProvided = true;
			
		 engine.setHeaderAnalysis(true);
		 
		 if(engine.isLibraryAnalysisChecked())
			currentSdk.libraryFilesList = libsList.toArray(new String [0]);
	}
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;	
	}
	private static void getIncludesFromThisMMP(ICarbideProjectInfo prjInfo, IPath mmpPath, ArrayList<String> usrIncList, ArrayList<String> sysIncList)
	{
		ArrayList<File> usrIncFiles = new ArrayList<File>();
		ArrayList<File> sysFiles = new ArrayList<File>();
				
		EpocEngineHelper.getMMPIncludePaths(prjInfo.getProject(), mmpPath, prjInfo.getDefaultConfiguration(), usrIncFiles, sysFiles);
						
		Iterator<File> i= usrIncFiles.iterator();
		
		if(usrIncFiles.size() > 0)
		{
			while(i.hasNext())
			{
				File usrInc = i.next();
				
				if(usrInc.exists())
				{
					String userDir = FileMethods.convertForwardToBackwardSlashes(usrInc.toString());
					if(!usrIncList.contains(userDir))
						usrIncList.add(userDir);
				}
			}
		}
    
		i= sysFiles.iterator();
	
		String parentProjPath = FileMethods.convertForwardToBackwardSlashes(prjInfo.getProject().getLocation().toString());
		
		while(i.hasNext())
	    {
			File sysInc = i.next();
			
			if(sysInc.exists())
			{
				String sysPath = FileMethods.convertForwardToBackwardSlashes(sysInc.toString());
			
				if(sysPath.startsWith(parentProjPath) && !usrIncList.contains(sysPath))
					usrIncList.add(sysPath);
			
				sysIncList.add(sysPath);
			}
	    }
	
	}
	
	private void getHeadersFromUsrIncPaths(ArrayList<String> inputPaths, ArrayList<String> filesList, IProgressMonitor monitor)
	{
		for(String input:inputPaths)
		{
			if(monitor.isCanceled())
			{
				monitor.done();
				isCancelled = true;
				return;
			}
			ArrayList<String> prjHdrs = CompatibilityAnalyserEngine.getHeaderFilesFromDir(input);
		
			for(String s:prjHdrs){
				if(!filesList.contains(s))
					filesList.add(s);
			}
			processSubDirs(input, filesList, monitor);
		}
		monitor.worked(6);
	}
	private static boolean containsDllsAsTargets(ICarbideProjectInfo prjInfo, IPath mmpPath, ArrayList<String> libsList)
	{
		EpocEnginePathHelper helper=new EpocEnginePathHelper(prjInfo.getProject());
		
		boolean dllExists = false;
		
		IPath dllPath = EpocEngineHelper.getTargetPathForExecutable(prjInfo.getDefaultConfiguration(), helper.convertToFilesystem(mmpPath));
		
		if(dllPath!=null)
			dllExists = addLibIfThisMMPisDll(dllPath,prjInfo,libsList);
		
		return dllExists;
	}
	
	private static boolean projectContainsDllsAsTargets(ICarbideProjectInfo prjInfo, ArrayList<String> libsList)
	{
		boolean dllExists = false;
			
		List<IPath> mmpList=EpocEngineHelper.getMMPFilesForProject(prjInfo);
	    Iterator<IPath> mmpitr=mmpList.iterator();
	 
	    while(mmpitr.hasNext())
	    {
	    	if(containsDllsAsTargets(prjInfo, mmpitr.next(), libsList))
	    		dllExists = true;
	    }
	    return dllExists;
	}
	public static void getIncludesFromThisProject(ICarbideProjectInfo prjInfo, ArrayList<String> usrIncList, ArrayList<String> sysIncList)
	{
		ArrayList<File> usrFiles = new ArrayList<File>();
		ArrayList<File> sysFiles = new ArrayList<File>();
		
		EpocEngineHelper.getProjectIncludePaths(prjInfo,prjInfo.getDefaultConfiguration(), usrFiles, sysFiles);
		
		Iterator<File> i=usrFiles.iterator();
		
		if(usrFiles.size() > 0)
		{
			while(i.hasNext())
			{
				File usrIncFile = i.next();
				
				if(usrIncFile.exists())
				{
					String userDir = FileMethods.convertForwardToBackwardSlashes(usrIncFile.toString());
					if(!usrIncList.contains(userDir))
						usrIncList.add(userDir);
				}
			}
		}
		
		i= sysFiles.iterator();
		
		String parentProjPath = FileMethods.convertForwardToBackwardSlashes(prjInfo.getProject().getLocation().toString());
				
		while(i.hasNext())
	    {
			File sysInc = i.next();
			
			if(sysInc.exists())
			{
				String sysPath = FileMethods.convertForwardToBackwardSlashes(sysInc.toString());
			
				if(sysPath.startsWith(parentProjPath) && !usrIncList.contains(sysPath))
					usrIncList.add(sysPath);
				
				sysIncList.add(sysPath);
			}
	    }
	}
	
	private static void getExportPathsAndFiles(ICarbideProjectInfo prjInfo, ArrayList<String> usrIncList, ArrayList<String> hdrsList)
	{
		DefaultViewConfiguration viewConfig = new DefaultViewConfiguration(prjInfo);
	   	  
	    EpocEnginePathHelper pathHelper = new EpocEnginePathHelper(prjInfo.getProject());
	    IDocument doc = viewConfig.getViewParserConfiguration().getModelDocumentProvider().getDocument(prjInfo.getAbsoluteBldInfPath().toFile());
	   	    
	 	BldInfModelFactory factory = new BldInfModelFactory();
	    
	   	IPath wsPath = prjInfo.getAbsoluteBldInfPath();
	   	IBldInfOwnedModel bldInfModel = factory.createModel(wsPath, doc);
	   
	   	bldInfModel.parse();
	   	
	    IBldInfView view = bldInfModel.createView(viewConfig);
	 	   
	    IBldInfData data = view.getData();
	  
	    List<IExport> exports = data.getExports();
	   	  	    	    
	    for(IExport xport:exports)
	    {
	    	IPath exportPath = pathHelper.convertToFilesystem(xport.getSourcePath());
	    	
	    	if(exportPath.toFile().exists())
	    	{
	    		String parentPath = exportPath.toFile().getParent();
	    		String fileName = exportPath.toFile().getName();
	    	
	    		if(!usrIncList.contains(parentPath))
	    			usrIncList.add(parentPath);
	    		if(!hdrsList.contains(fileName) && CompatibilityAnalyserEngine.isSupportedType(fileName))
	    			hdrsList.add(fileName);
	    	}
    	
	   } 
	}
		
	private void processSubDirs(String folderPath, ArrayList<String> filesList, IProgressMonitor monitor)
	{
		if(monitor != null && monitor.isCanceled())
		{
			isCancelled = true;
			monitor.done();
			return;
		}
	
		File selectedFolder = new File(folderPath);
		
		if(selectedFolder.exists())
		{
			File [] subFolders = selectedFolder.listFiles(new FilenameFilter(){

				public boolean accept(File file, String name) {
					File temp = new File(FileMethods.appendPathSeparator(file.getAbsolutePath())+ name);
					if(temp.isDirectory())
						return true;
					
					return false;
				}
				
			});
			
			if(subFolders != null){
				
				for(File file:subFolders)
				{
					ArrayList<String> fileNames = CompatibilityAnalyserEngine.getHeaderFilesFromDir(file.getAbsolutePath());
										
					for(String s:fileNames)
					{
						if(!filesList.contains(s))
							filesList.add(s);
					}
					
					processSubDirs(file.getAbsolutePath(), filesList, monitor);
				}
			}
			
		}
	}
}
