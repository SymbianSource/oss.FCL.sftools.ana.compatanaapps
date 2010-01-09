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
* "Run static analysis for this directory in project..."
*
*/
package com.nokia.s60tools.compatibilityanalyser.ui.actions;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.EpocEngineHelper;
import com.nokia.carbide.cdt.builder.ICarbideBuildManager;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.AnalysisWizard;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;

/**
 * This is the action class bound with the popup menu action
 * "Run static analysis for this directory in project..."
 * 
 */
 
public class AnalyseDirOnlyAction implements IWorkbenchWindowActionDelegate{

	private CompatibilityAnalyserEngine engine;
	private String folderPath = null;
	private ArrayList<String> filesList;
	private ArrayList<String> libsList;
	private boolean isCancelled = false;
	private ICarbideProjectInfo prjInfo=null;
	private ProductSdkData currentSdk;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	/**
	 * This method gets invoked when user selects the option,
	 * 'Run static analysis for this directory in project...' from
	 * the submenu of a folder.
	 */
	public void run(IAction action) {
				
		IWorkbenchWindow window=CompatibilityAnalyserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		ISelection selection= (ISelection)window.getSelectionService().getSelection();
		
		engine = new CompatibilityAnalyserEngine();
		currentSdk = new ProductSdkData();
		
		currentSdk.isOpenedFromProject = true;
				
		try
		{
			isCancelled = false;
			engine.setHeaderAnalysis(true);
			if(selection instanceof TreeSelection)
			{
				IStructuredSelection treeSelection = (IStructuredSelection)selection;
		
				//In Carbide, some folders are instances of ICContainer class. 
				if(treeSelection.getFirstElement() instanceof IFolder || treeSelection.getFirstElement() instanceof ICContainer)
				{
					IFolder selectedFolder = null;		 
					ICContainer container = null;
					IProject parentProj = null;
									
					if(treeSelection.getFirstElement() instanceof IFolder)
					{
						selectedFolder = (IFolder)treeSelection.getFirstElement();
						parentProj = selectedFolder.getProject();
						folderPath = FileMethods.convertForwardToBackwardSlashes(selectedFolder.getLocation().toString());
					}
					else if(treeSelection.getFirstElement() instanceof ICContainer)
					{
						container = (ICContainer)treeSelection.getFirstElement();
						parentProj = container.getCProject().getProject();
						folderPath = FileMethods.convertForwardToBackwardSlashes(container.getResource().getLocation().toString());
					}
				
					ICarbideBuildManager mngr=CarbideBuilderPlugin.getBuildManager();
					
					//Selected folder path is added to the list of
					//directories, in Headers page of the wizard.
					
					if(folderPath != null)
					{
						currentSdk.defaultHeaderDir = false;
						currentSdk.currentHeaderDir = new String[]{folderPath};
						currentSdk.analyseAll = false;
					}	
					else
					{
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.getString("ProjectViewPopupAction.CompatibilityAnalyserError"), "Unable to read selected Resource");
						currentSdk.isOpenedFromProject = false;
						return;
					}
					if(parentProj!=null && mngr.isCarbideProject(parentProj))
					{
						prjInfo = mngr.getProjectInfo(parentProj);
					}
					else
					{
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.getString("ProjectViewPopupAction.CompatibilityAnalyserError"), Messages.getString("ProjectViewPopupAction.CouldNotReadInfoFromProject"));
						currentSdk.isOpenedFromProject = false;
						return;
					}
				
					currentSdk.projectName = parentProj.getName();
					currentSdk.productSdkName = prjInfo.getDefaultConfiguration().getSDK().getUniqueId();
				
					IRunnableWithProgress projProgInfo = new IRunnableWithProgress(){

						public void run(IProgressMonitor monitor) {
							
							try{
								monitor.beginTask("Reading folder data...", 10);
								filesList = getHeaderFilesFromFolder(folderPath, monitor);
								
								ArrayList<String> sysIncList = new ArrayList<String>();
								
								getSystemIncludesFromThisProject(prjInfo, sysIncList, monitor);
								
								currentSdk.HeaderFilesList = filesList.toArray(new String [0]);
								monitor.worked(6);
								currentSdk.currentIncludes = sysIncList.toArray(new String [0]);
								
								if(!isCancelled)
								{
									libsList = getCorrespondingDsos(filesList, monitor);
									monitor.worked(9);
								}
								if(libsList.size() != 0){
									engine.setLibraryAnalysis(true);
									currentSdk.analyzeAllLibs = false;
									currentSdk.libraryFilesList = libsList.toArray(new String[0]);
								}
								
							}catch(Exception e){
								e.printStackTrace();
							}
							
							monitor.done();
						}
					};
					
					new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, projProgInfo);
				}
			}
					
			engine.setCurrentSdkData(currentSdk);
		
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(isCancelled)
			return;
		
		Runnable showWizardRunnable = new Runnable(){
			public void run(){
	  		  WizardDialog wizDialog;
	  		  AnalysisWizard wiz = new AnalysisWizard(engine);
	       	  wizDialog = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
			  wizDialog.create();		
			  //wizDialog.getShell().setSize(550, 620);
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
	 * The method fetches system include paths for given project.
	 * @param prjInfo specifies the details of selected carbide project.
	 */
	private void getSystemIncludesFromThisProject(ICarbideProjectInfo prjInfo, ArrayList<String> sysIncList, IProgressMonitor monitor)
	{
		if(monitor != null)
			monitor.subTask("Getting System Include paths");
		
		ArrayList<File> usrFiles = new ArrayList<File>();
		ArrayList<File> sysFiles = new ArrayList<File>();
			
		EpocEngineHelper.getProjectIncludePaths(prjInfo,prjInfo.getDefaultConfiguration(), usrFiles, sysFiles);
		
		Iterator i= sysFiles.iterator();
		
		while(i.hasNext())
	    {
			String sysPath = i.next().toString();
	    	sysIncList.add(FileMethods.convertForwardToBackwardSlashes(sysPath));
	    }
	}
	public void selectionChanged(IAction action, ISelection selection) {
	}

	private ArrayList<String> getHeaderFilesFromFolder(String folderPath, IProgressMonitor monitor)
	{
		if(monitor != null)
			monitor.subTask("Getting Headers List...");
		
		ArrayList<String> hdrsList = CompatibilityAnalyserEngine.getHeaderFilesFromDir(folderPath);
		processSubDirs(folderPath, hdrsList, monitor);
		
		if(monitor != null && !isCancelled)
			monitor.worked(5);
		
		return hdrsList;
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
	
	private ArrayList<String> getCorrespondingDsos(ArrayList<String> filesList, IProgressMonitor monitor)
	{
		ArrayList<String> libsList = new ArrayList<String>();
	
		if(monitor != null)
			monitor.subTask("Getting Target Dso files...");
		
		for(String s:filesList)
		{
		
			if(monitor != null && monitor.isCanceled())
			{
				isCancelled = true;
				monitor.done();
				return libsList;
			}
			ArrayList<String> targetDsos = CompatibilityAnalyserEngine.getTargetDsoForHeader(prjInfo, s);
			
			for(String dso:targetDsos){
				if(!libsList.contains(dso))
					libsList.add(dso);
			}
			
		}
		
		return libsList;
	}
}
