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
* Description: Dialog used to specify baseline file name, if it is different from the current file name.
*
*/
package com.nokia.s60tools.compatibilityanalyser.ui.dialogs;


import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineSdkData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.FeedbackHandler;
import com.nokia.s60tools.compatibilityanalyser.utils.BaselineProfileUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;

/**
 * This dialog gets launched when "Set baseline name..." button is selected
 * in Header Files Selection page, of Analysis wizard.  
 *
 */
public class RenameDialog extends Dialog implements SelectionListener,ModifyListener {

	private Button buttonOK;
	private Button buttonCancel;
	private Text baselineFile_Txt;
	private Shell shell;
	private FeedbackHandler feedbackHandler;
	private Button currentFile_Browse;
	private Button baselineFile_Browse;
	private String currentFile="";
	private String baselineFile="";
	private Text currentFile_Txt;
	private String given_currentFile;
	private String given_baselineFile;

	private BaselineProfile baseProfile;
	private ArrayList<String> currentHeaderFiles;
	private ArrayList<String> fileNamesList;
	private ArrayList<String> filesList;
	private ShowAllFilesListDialog filesDialog;
	private String currentPath;
	private ArrayList<String> children;
	private ArrayList<String> subChildren;
	private ArrayList<String> headerDirectories;
	private ProgressMonitorDialog progDlg;
	private boolean isMonitorCancelled;
	
	public RenameDialog(Shell parent, int style, String currentFile, String baseFile, FeedbackHandler fh) {
		super(parent, style);
		this.feedbackHandler = fh;

		if(currentFile != null)
			this.currentFile = currentFile;
		if(baseFile != null)
			this.baselineFile = baseFile;
	}
	public void open()
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL|SWT.BORDER);
		shell.setSize(400,190);
		shell.setLocation(500, 300);
		shell.setLayout(new GridLayout(2, false));
		shell.setText(Messages.getString("RenameDialog.Rename"));
	
		GridData griddata=new GridData(GridData.FILL_HORIZONTAL);
		griddata.horizontalSpan=2;
		griddata.horizontalAlignment=GridData.FILL;

		Label currentFile_Lbl=new Label(shell,SWT.NULL);
		currentFile_Lbl.setText("Current file");
		currentFile_Lbl.setLayoutData(griddata);

		currentFile_Txt = new Text(shell,SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		currentFile_Txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		currentFile_Txt.addModifyListener(this);

		currentFile_Browse = new Button(shell,SWT.PUSH);
		currentFile_Browse.setText("Browse...");
		currentFile_Browse.addSelectionListener(this);

		Label baselineFile_Lbl = new Label(shell, SWT.NULL);
		baselineFile_Lbl.setText(Messages.getString("RenameDialog.PleaseProvideCorrespondingFileNameInBaseline")); 
		baselineFile_Lbl.setToolTipText(Messages.getString("RenameDialog.ProvideTheOriginalFileNameInBaseline")); 
		baselineFile_Lbl.setLayoutData(griddata);

		baselineFile_Txt = new Text(shell, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		baselineFile_Txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		baselineFile_Txt.addModifyListener(this);

		baselineFile_Browse = new Button(shell,SWT.PUSH);
		baselineFile_Browse.setText("Browse...");
		baselineFile_Browse.addSelectionListener(this);

		GridData griddata2=new GridData(GridData.FILL_HORIZONTAL);
		griddata2.horizontalSpan=2;
		griddata2.verticalIndent=4;
		Composite btnComp=new Composite(shell,SWT.NONE);
		btnComp.setLayout(new GridLayout(2,false));
		btnComp.setLayoutData(griddata2);

		GridData d1=new GridData(GridData.FILL_HORIZONTAL);
		d1.widthHint=100;
		d1.horizontalAlignment=SWT.END;
		buttonOK = new Button(btnComp, SWT.PUSH);
		buttonOK.setText(Messages.getString("RenameDialog.Ok"));
		buttonOK.setLayoutData(d1);
		buttonOK.addSelectionListener(this);

		GridData d2=new GridData(GridData.FILL_HORIZONTAL);
		d2.widthHint=100;
		d2.horizontalAlignment=SWT.BEGINNING;
		buttonCancel = new Button(btnComp, SWT.PUSH);
		buttonCancel.setText(Messages.getString("RenameDialog.Cancel"));
		buttonCancel.setLayoutData(d2);
		buttonCancel.addSelectionListener(this);

		currentFile_Txt.setText(currentFile);
		baselineFile_Txt.setText(baselineFile);

		shell.open();
		
	}
	public void setBaselineProfile(String baseProfileName)
	{
		Object obj = BaselineProfileUtils.getBaselineProfileData(baseProfileName);

		if(obj == null || !(obj instanceof BaselineProfile)) {
			return;
		}
		else
		{
			this.baseProfile = (BaselineProfile)obj;
		}

	}
	public void setCurrentFiles(String [] currentFiles)
	{
		this.currentHeaderFiles = new ArrayList<String>();
		if(currentFiles != null)
		{
			for(String s:currentFiles)
				currentHeaderFiles.add(s);
		}
	}
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}
	
	public void widgetSelected(SelectionEvent e) {
		if(e.widget == buttonOK)
		{
			given_currentFile = currentFile_Txt.getText();
			given_baselineFile = baselineFile_Txt.getText();
			if(shell!=null)
				shell.close();

			feedbackHandler.UpdateFilesList();
		}
		else if(e.widget == buttonCancel)
		{
			if(shell!=null)
				shell.close();
		}
		else if(e.widget == currentFile_Browse)
		{
			String title = null;
			if(currentHeaderFiles.size() == 0)
				return;
			else if(currentHeaderFiles.size() == 1)
				title = new String("List of files in " + currentHeaderFiles.get(0) + " directory");
			else 
				title = new String("List of files from all current Header directory");

			filesDialog = new ShowAllFilesListDialog(this.shell, title, currentFile_Txt);
			getFilesFromAllPaths(currentHeaderFiles);
			updateButtons();			
		}
		else if(e.widget == baselineFile_Browse)
		{
			if(baseProfile != null)
			{
				CompatibilityAnalyserEngine engine = new CompatibilityAnalyserEngine();
				BaselineSdkData baseSdkData = engine.readBaselineSdkData(baseProfile);
				if(baseSdkData != null)
				{
					ArrayList<String> baseHeaderDirs = new ArrayList<String>();
					for(String s:baseSdkData.baselineHeaderDir)
						baseHeaderDirs.add(s);

					String title = null;
					if(baseHeaderDirs.size() == 0)
						return;
					else if(baseHeaderDirs.size() == 1)
						title = new String("List of files in " + baseHeaderDirs.get(0) + " directory");
					else 
						title = new String("List of files from all baseline Header directory");

					filesDialog = new ShowAllFilesListDialog(this.shell, title, baselineFile_Txt);
					getFilesFromAllPaths(baseHeaderDirs);
				}
				
			}		
		
			updateButtons();
		}
	}

	public String[] getSelectedNames()
	{
		return new String []{given_currentFile, given_baselineFile};		
	}
	public void modifyText(ModifyEvent e) {
		if(e.widget == currentFile_Txt || e.widget == baselineFile_Txt)
		{
			if(currentFile_Txt.getText().length() == 0 || baselineFile_Txt.getText().length() == 0)
				buttonOK.setEnabled(false);
			else
				buttonOK.setEnabled(true);	
		}			
	}
	private void getFilesFromAllPaths(ArrayList<String> headerDirs)
	{
		headerDirectories = headerDirs;
		if(headerDirs == null || headerDirs.size() == 0 || filesDialog == null)
			return;

		isMonitorCancelled = false;
		fileNamesList = new ArrayList<String>();
		filesList = new ArrayList<String>();
		children = new ArrayList<String>();
		subChildren = new ArrayList<String>();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					int i=1;
					for(String path: headerDirectories)
					{
						currentPath = FileMethods.convertForwardToBackwardSlashes(path);
						monitor.beginTask("Getting files from "+ currentPath, headerDirectories.size());
						getAndSetFiles(currentPath, monitor); 
						processSubDirs(currentPath, monitor);
						monitor.worked(i++);										
					}
					monitor.done();

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}	    		
		};
		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			
			Shell shell = win != null ? win.getShell() : null;
			progDlg = new ProgressMonitorDialog(shell);
			progDlg.run(true, true, op);
			progDlg.setBlockOnOpen(true);
		
		} catch (InvocationTargetException err) {
			err.printStackTrace();
		} catch (InterruptedException err) {
			err.printStackTrace();
		}

		if(!isMonitorCancelled)
		{
			Collections.sort(filesList);
			filesDialog.children = children;
			filesDialog.subChildren = subChildren;
			filesDialog.open();
			filesDialog.filesList.setItems(filesList.toArray(new String[filesList.size()]));
		}

		if(isMonitorCancelled)
		{
			//System.out.println("Cancelled the process");
		}
		else if((filesDialog.filesList == null || filesDialog.filesList.getItemCount() == 0)
				&& fileNamesList.size() !=0)
		{
			Runnable showMessageRunnable = new Runnable(){
				public void run(){
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
							Messages.getString("HeaderFilesPage.CompatibilityAnalyser"), Messages.getString("HeaderFilesPage.hdrsAlreadyExists")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			};
			Display.getDefault().asyncExec(showMessageRunnable); 
		}

		else if(fileNamesList.size() == 0)
		{
			Runnable showMessageRunnable = new Runnable(){
				public void run(){
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
							Messages.getString("HeaderFilesPage.CompatibilityAnalyser"), Messages.getString("HeaderFilesPage.NoHdrsExists")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			};
			Display.getDefault().asyncExec(showMessageRunnable); 
		}	
		else			
			filesDialog.fileNamesList = filesDialog.filesList.getItems();
	}
	private void getAndSetFiles(String path, IProgressMonitor monitor)
	{
		if(path == null)
			return;

		File rootDir = new File(path);

		if(rootDir.isDirectory())
		{
			String [] fileNames = rootDir.list(new FilenameFilter()
			{

				public boolean accept(File file, String name) {
					File temp = new File(FileMethods.appendPathSeparator(file.getAbsolutePath())+ name);

					if(temp.isFile() && isSupportedType(name))
						return true;
					return false;
				}

			});

			if(fileNames == null || fileNames.length == 0)
				return;

			for(String name:fileNames)
			{
				if(monitor!=null && monitor.isCanceled())
				{
					isMonitorCancelled = true;
					fileNamesList.clear();
					filesList.clear();
					children.clear();
					subChildren.clear();
					monitor.done();
					return;
				}
				if(!fileNamesList.contains(name.toLowerCase()))
				{
					fileNamesList.add(name.toLowerCase());
					filesList.add(name);	
				}
				if(path.equalsIgnoreCase(currentPath))
					children.add(name);
				else
					subChildren.add(name);
			}
		}
	}

	private void processSubDirs(String path, IProgressMonitor monitor)
	{
		File rootDir = new File(path);
		
		if(rootDir.isDirectory())
		{
			String [] dirNames = rootDir.list(new FilenameFilter()
			{

				public boolean accept(File file, String name) {
					File temp = new File(FileMethods.appendPathSeparator(file.getAbsolutePath())+ name);
					if(temp.isDirectory())
						return true;

					return false;
				}

			});

			if(dirNames != null)
			{
				for(String name:dirNames)
				{
					getAndSetFiles(FileMethods.appendPathSeparator(path)+ name, monitor);
					if(monitor!=null && monitor.isCanceled())
					{
						isMonitorCancelled = true;
						fileNamesList.clear();
						filesList.clear();
						children.clear();
						subChildren.clear();
						monitor.done();
						return;
					}
				}
				for(String name:dirNames)
				{
					processSubDirs(FileMethods.appendPathSeparator(path) + name, monitor);
				}
			}
		}
	}

	private boolean isSupportedType(String name)
	{
		if(name.endsWith(Messages.getString("HeaderAnalyserEngine.h")) || 
				name.endsWith(Messages.getString("HeaderAnalyserEngine.hrh")) ||
				name.endsWith(Messages.getString("HeaderAnalyserEngine.rsg")) ||
				name.endsWith(Messages.getString("HeaderAnalyserEngine.mbg")))
			return true;
		else
			return false;
	}
	private void updateButtons()
	{
		if(!currentFile_Txt.getText().equals("") && !baselineFile_Txt.getText().equals(""))
		{
			buttonOK.setEnabled(true);
		}
		else
			buttonOK.setEnabled(false);
	}

}
