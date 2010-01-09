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
package com.nokia.s60tools.compatibilityanalyser.ui.actions;

import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.compatibilityanalyser.data.BCAndSCIssues;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.ReportData.FILE_TYPE;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueFileData;
import com.nokia.s60tools.compatibilityanalyser.model.LibraryIssueData;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalysisConsole;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.util.sourcecode.CannotFoundFileException;
import com.nokia.s60tools.util.sourcecode.IProjectFinder;
import com.nokia.s60tools.util.sourcecode.ISourceFinder;
import com.nokia.s60tools.util.sourcecode.ISourcesFinder;
import com.nokia.s60tools.util.sourcecode.ProjectFinderFactory;
import com.nokia.s60tools.util.sourcecode.SourceFileLocation;
import com.nokia.s60tools.util.sourcecode.SourceFinderFactory;

/** 
 * Implementation for open action in the issues viewer section.
 *
 */
public class OpenSourceFileIssuesViewAction extends Action {

	private TreeViewer issuesViewer;
	private ProductSdkData currentSdkData;
	
	public OpenSourceFileIssuesViewAction(TreeViewer issuesViewer)
	{
		this.issuesViewer = issuesViewer;
		this.setToolTipText("Double click to view the issues file");
	}
	
	public void run()
	{
		
		BCAndSCIssues issues = ((BCAndSCIssues)issuesViewer.getInput());
		
		IStructuredSelection selectedElem = (IStructuredSelection)issuesViewer.getSelection();
										
		//If selected issues are related to library analysis, we need to
		//fetch the relevant data to open the corresponding source file.
		
		if(issues.getType() == FILE_TYPE.LIBRARY)
		{
			if(selectedElem.getFirstElement() instanceof LibraryIssueData) 
			{
				LibraryIssueData issue = (LibraryIssueData)(selectedElem.getFirstElement());
				int error_id = issue.getError_typeid();
				
				if(error_id == 3 || error_id == 5 || error_id == 6 || error_id == 7)
				{
					String funcName = issue.getFunctionName();
					String ordinalNo = issue.getOrdinalPosition();
							
					String epocRootPath = currentSdkData.epocRoot;
					String componentName = issue.getCurrentFileName();
			
					List<String> dllPathsList = issue.getDllDirPaths();
							
					try{
					 ISourceFinder finder = SourceFinderFactory.createSourceFinder(CompatibilityAnalysisConsole.getInstance());
					 SourceFileLocation location = finder.findSourceFile(ordinalNo, funcName, componentName+".dll", epocRootPath, dllPathsList);
					
					 FileMethods.openFileAndHighLightGivenLine(location.getSourceFileLocation(), location.getMethodOffset());
						
					}catch(CannotFoundFileException e){
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Compatibility Analyser", "Source file cannot be found");	
					}
								
				 }
				else if(error_id == 8 || error_id == 9 || error_id == 10 || error_id == 11 || error_id == 12)
				{
					final String epocRootPath = currentSdkData.epocRoot;
					final String componentName = issue.getCurrentFileName();
					final List<String> dllPathsList = issue.getDllDirPaths();
																	
					IWorkspaceRunnable runnable = new IWorkspaceRunnable()
					{
						String mmpFile = null;
						
						public void run(IProgressMonitor monitor) throws CoreException {
									
						try{	
							ISourcesFinder sourcesFinder = SourceFinderFactory.createSourcesFinder(CompatibilityAnalysisConsole.getInstance());
							String [] sourceFiles = sourcesFinder.findSourceFiles(componentName + ".dll", epocRootPath, dllPathsList);
							IProjectFinder finder = ProjectFinderFactory.createProjectFinder(CompatibilityAnalysisConsole.getInstance(), monitor);
							
							System.out.println("No of sources for " + componentName + " are " + sourceFiles.length);
							for(String file:sourceFiles)
							{
								System.out.println("Getting mmp for " + file);
								if(finder.findMMPFile(file) != null){
								mmpFile = finder.findMMPFile(file);
								break;
							}
							}
							System.out.println("Opening the mmp file " + mmpFile);		
							FileMethods.openFileAndHighLightGivenLine(mmpFile, 0);	
						}catch(CannotFoundFileException  e){
							MessageDialog.openError(Display.getDefault().getActiveShell(), "Compatibility Analyser", "Source file cannot be found");	
						}
						}
					};
							
					try{
						ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
					}catch(CoreException e){
						e.printStackTrace();
					}
				}
				//We are not interested in other type of library issues.
				else
					return; 
			}
		}	
		else{
			if(selectedElem.getFirstElement() instanceof CompatibilityIssueFileData) {
				String issueFile = ((CompatibilityIssueFileData)selectedElem.getFirstElement()).getCurrentIssueFile();
				
				FileMethods.openFileAndHighLightGivenLine(issueFile, 0);
			
			}
			else if(selectedElem.getFirstElement() instanceof CompatibilityIssueData){
				CompatibilityIssueData issue = (CompatibilityIssueData)(selectedElem.getFirstElement());
				String fileName = issue.getCurrentIssueFile();
				long offsetPos = FileMethods.getOffsetPosition(fileName, issue.getLineNumber());
				
				FileMethods.openFileAndHighLightGivenLine(fileName, offsetPos);
			}
		}
	}

	public void setCurrentSdkData(ProductSdkData currentSdkData) {
		this.currentSdkData = currentSdkData;
	}
	
}
