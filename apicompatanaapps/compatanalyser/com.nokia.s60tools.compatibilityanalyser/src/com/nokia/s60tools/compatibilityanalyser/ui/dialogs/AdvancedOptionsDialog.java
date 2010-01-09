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
* Description: Class to display KnownIssues settings
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
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.FeedbackHandler;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;

public class AdvancedOptionsDialog extends TrayDialog implements
		SelectionListener, FeedbackHandler {

	private Composite parent;
	//System include controls
	private Table syspath_table;
	private Button add_sys_button;
	private Button remove_sys_button;
	private Button remove_all_sys_button;
	
	//Forced headers Controls
	private Table forced_table;
	private Button forced_addBtn;
	private Button forced_removeBtn;
	private Button forced_removeAllBtn;
	
	//Use Platform Data check box
	private Button usePlatformData;
	
	private IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
	private ProductSdkData currentData;
	private Color invalidColor = new Color(Display.getDefault(), 255,0,0);
	Font invalidFile_Font=new Font(Display.getDefault(),"Tahoma",8,SWT.ITALIC);
	private String epocRoot;	
	private ArrayList<String> children;
	private ArrayList<String> subChildren;
	private boolean isMonitorCancelled;
	private ProgressMonitorDialog progDlg;
	private String[] allSysIncHdrPaths;
	private ShowFilesListDialog addDialog;
	private ArrayList<String> displayFiles;
	private String absolutePath;
	private ArrayList<String> numOfFiles;
	private int invalidHeadersCount =0;
	
	public AdvancedOptionsDialog(Shell shell, ProductSdkData currentData, String epocRoot) {
		super(shell);
		setShellStyle(getShellStyle()|SWT.RESIZE);
		this.currentData = currentData;
		this.epocRoot = epocRoot;
	}

	protected Control createContents(Composite composite) {
		
		parent = new Composite(composite, SWT.NONE);
		parent.setLayout(new GridLayout(1, true));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		parent.getShell().setText("Advanced options...");
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		Group system_inc_grp = new Group(parent, SWT.NONE);
		system_inc_grp.setText("System include paths");
		system_inc_grp.setLayout(new GridLayout(2, false));
		system_inc_grp.setLayoutData(gd);
		
		syspath_table = new Table(system_inc_grp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 4;
		gd.heightHint = 100;
		syspath_table.setLayoutData(gd);
		
		TableColumn col = new TableColumn(syspath_table, SWT.FILL|SWT.FULL_SELECTION);
		col.setText("Platform headers");
		col.setWidth(400);
		
		add_sys_button = new Button(system_inc_grp, SWT.PUSH);
		add_sys_button.setText("Add include path...");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		add_sys_button.setLayoutData(gd);
		add_sys_button.addSelectionListener(this);
		
		remove_sys_button = new Button(system_inc_grp, SWT.PUSH);
		remove_sys_button.setText("Remove");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove_sys_button.setLayoutData(gd);
		remove_sys_button.addSelectionListener(this);
		
		remove_all_sys_button = new Button(system_inc_grp, SWT.PUSH);
		remove_all_sys_button.setText("Remove all");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove_all_sys_button.setLayoutData(gd);
		remove_all_sys_button.addSelectionListener(this);
		
		gd = new GridData(GridData.FILL_BOTH);
		Group forced_hdrs_grp = new Group(parent, SWT.NONE);
		forced_hdrs_grp.setText("Forced headers");
		forced_hdrs_grp.setLayout(new GridLayout(2, false));
		forced_hdrs_grp.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 4;
		gd.heightHint = 100;
		forced_table = new Table(forced_hdrs_grp,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL| SWT.FULL_SELECTION);
		forced_table.setLayoutData(gd);
		forced_table.addSelectionListener(this);
		
		TableColumn col1=new TableColumn(forced_table,SWT.LEFT|SWT.FILL);
		col1.setText(Messages.getString("HeaderFilesPage.Column1_Title"));
		col1.setWidth(320);
		
		forced_addBtn = new Button(forced_hdrs_grp,SWT.PUSH);
		forced_addBtn.setText("Add...");  
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 100;
		forced_addBtn.setLayoutData(gd);
		forced_addBtn.addSelectionListener(this);

		forced_removeBtn = new Button(forced_hdrs_grp,SWT.PUSH);
		forced_removeBtn.setText(Messages.getString("BaselineEditor.Remove"));  
		forced_removeBtn.setEnabled(false);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 100;
		forced_removeBtn.setLayoutData(gd);
		forced_removeBtn.addSelectionListener(this);

		forced_removeAllBtn = new Button(forced_hdrs_grp,SWT.PUSH);
		forced_removeAllBtn.setText("Remove all");  
		forced_removeAllBtn.setEnabled(false);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 100;
		forced_removeAllBtn.setLayoutData(gd);
		forced_removeAllBtn.addSelectionListener(this);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		Group dependency_data_grp = new Group(parent, SWT.NONE);
		dependency_data_grp.setText("Dependency data");
		dependency_data_grp.setLayout(new GridLayout(2, false));
		dependency_data_grp.setLayoutData(gd);
		
		usePlatformData = new Button(dependency_data_grp, SWT.CHECK);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING|GridData.HORIZONTAL_ALIGN_BEGINNING);
		usePlatformData.setSelection(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.USEPLATFORMDATA_LAST_SELECTION));
		usePlatformData.setLayoutData(gd);

		Label lbl=new Label(dependency_data_grp,SWT.WRAP);
		lbl.setText(Messages.getString("PlatformHeadersPage.UseDependencies"));
		gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		lbl.setLayoutData(gd);
		
		setDialogHelpAvailable(false);
		setHelpAvailable(true);
		setHelp();
		setCurrentData(currentData);
		dialogChanged();
		return super.createContents(parent);
	}
	
	private void setCurrentData(ProductSdkData cData) {
		syspath_table.removeAll();
		//Set the system include paths
		if(cData.currentIncludes != null) {
			for(String path: cData.currentIncludes)	{
				TableItem item = new TableItem(syspath_table, SWT.NONE);
				item.setText(0, path);
				if(!(new File(path).exists())) {
					item.setForeground(invalidColor);
					item.setFont(invalidFile_Font);
				}
			}
			syspath_table.select(0);
		}
		
		//Set forced headers
		if(cData.forcedHeaders != null)
		{
			for(String s: cData.forcedHeaders)
			{
				TableItem item = new TableItem(forced_table, SWT.NONE);
				item.setText(s);
			}
		}
		
		//Use Platform Data
		if(cData.isOpenedFromProject)
			usePlatformData.setSelection(false);
	}

	private void dialogChanged() {
		remove_sys_button.setEnabled(syspath_table.getItemCount() > 0);
		remove_all_sys_button.setEnabled(syspath_table.getItemCount() > 0);
		
		forced_removeBtn.setEnabled(forced_table.getItemCount() > 0);
		forced_removeAllBtn.setEnabled(forced_table.getItemCount() > 0);
	}

	protected void okPressed() {
		
		if(usePlatformData.getSelection())
		{
			currentData.usePlatformData = true;
		}

		if(!currentData.isOpenedFromProject)
			prefStore.setValue(CompatibilityAnalyserPreferencesConstants.USEPLATFORMDATA_LAST_SELECTION, usePlatformData.getSelection());

		if(syspath_table.getItemCount() > 0){
			currentData.currentIncludes = getSelectedPlatformHdrPaths();
		}
		if(forced_table.getItemCount() > 0)
		{
			currentData.forcedHeaders = new ArrayList<String>();
			for(TableItem item:forced_table.getItems())
			{
				if(!item.getForeground().equals(invalidColor))
					currentData.forcedHeaders.add(item.getText());
			}
		}		
		super.okPressed();
	}
	
	public void widgetDefaultSelected(SelectionEvent arg0) {

	}

	public void widgetSelected(SelectionEvent event) {

		if(event.widget == add_sys_button)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
			dirDialog.setFilterPath(epocRoot+"epoc32"+File.separator+"include");
			String newDir = dirDialog.open();
			if(newDir != null)
			{
				TableItem item = new TableItem(syspath_table, SWT.NONE);
				item.setText(0, newDir);
			}
			syspath_table.select(0);
			validateForcedHeaders();
			dialogChanged();
		}
		else if(event.widget == remove_all_sys_button)
		{
			syspath_table.removeAll();
			validateForcedHeaders();
			dialogChanged();
		}
		else if(event.widget == remove_sys_button)
		{
			syspath_table.remove(syspath_table.getSelectionIndices());
			syspath_table.select(0);
			validateForcedHeaders();
			dialogChanged();
		}
		else if(event.widget == forced_addBtn)
		{
			if(syspath_table.getItemCount() > 0)
			{
				openFilesListDialog();
			}
			else
			{
				openFilesListDialog();
			}
		}
		else if(event.widget == forced_removeBtn)
		{
			forced_table.remove(forced_table.getSelectionIndices());
			forced_table.select(0);
			dialogChanged();
		}
		else if(event.widget == forced_removeAllBtn)
		{
			forced_table.removeAll();
			dialogChanged();
		}			
	}

	private void setHelp() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				HelpContextIDs.INCLUDES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(syspath_table,
				HelpContextIDs.INCLUDES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(forced_table,
				HelpContextIDs.INCLUDES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(usePlatformData,
				HelpContextIDs.INCLUDES_PAGE);
	}

	public void UpdateFilesList() {
		forced_table.select(0);
	}

	public void UpdatePage() {
		dialogChanged();
	}
	
	private void openFilesListDialog()
	{
		numOfFiles = new ArrayList<String>();
		displayFiles = new ArrayList<String>();
		children = new ArrayList<String>();
		subChildren = new ArrayList<String>();
		isMonitorCancelled = false;
		
		addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), forced_table, this, "", true);
		ArrayList<String> paths = new ArrayList<String>();				
		for(int i =0 ; i < syspath_table.getItems().length; i++)
		{
			if(!paths.contains(syspath_table.getItem(i).getText(0)))
				paths.add(syspath_table.getItem(i).getText(0));
		}
		if(!paths.contains(epocRoot+"epoc32"+File.separator+"include"))
			paths.add(epocRoot+"epoc32"+File.separator+"include");
		
		allSysIncHdrPaths = paths.toArray(new String[paths.size()]);
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					int i=1;
					for(String path: allSysIncHdrPaths)
					{
						absolutePath = FileMethods.convertForwardToBackwardSlashes(path);
						monitor.beginTask("Getting files from "+ absolutePath, allSysIncHdrPaths.length);
						getFiles(absolutePath,monitor); 
						getFilesFromSubDirs(absolutePath,monitor);
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
			for(String name : numOfFiles)
				if(!isPreviouslySelected(name))
					displayFiles.add(name);

			if(displayFiles.size() != 0)
			{
				Collections.sort(displayFiles);
				addDialog.children = children;
				addDialog.subChildren = subChildren;					
				addDialog.open();
				addDialog.filesList.setItems(displayFiles.toArray(new String[displayFiles.size()]));
				addDialog.filesList.select(0);
			}
		}
		if(isMonitorCancelled)
		{
		}
		else if(displayFiles.size() == 0 && numOfFiles.size() !=0)
		{
			Runnable showMessageRunnable = new Runnable(){
				public void run(){
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
							Messages.getString("HeaderFilesPage.CompatibilityAnalyser"), Messages.getString("HeaderFilesPage.hdrsAlreadyExists")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			};
			Display.getDefault().asyncExec(showMessageRunnable); 
		}

		else if(numOfFiles.size() == 0)
		{
			Runnable showMessageRunnable = new Runnable(){
				public void run(){
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
							Messages.getString("HeaderFilesPage.CompatibilityAnalyser"), Messages.getString("HeaderFilesPage.NoHdrsExists")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			};
			Display.getDefault().asyncExec(showMessageRunnable); 
		}	
		else{
			addDialog.fileNamesList = addDialog.filesList.getItems();
		}
		
	}
	
	
	/**
	 * Get supported files from the given directory.
	 * @param path
	 * @param monitor
	 */
	private void getFiles(String path, IProgressMonitor monitor)
	{
		File rootDir = new File(path);

		if(rootDir.isDirectory())
		{
			String [] fileNames = rootDir.list(new FilenameFilter()
			{

				public boolean accept(File file, String name) {
					File temp = new File(FileMethods.appendPathSeparator(file.getAbsolutePath())+ name);

					if(temp.isFile() && CompatibilityAnalyserEngine.isSupportedType(name))
						return true;
					
					return false;
				}

			});

			if(fileNames == null)
				return;

			for(String name:fileNames)
			{
				if(monitor!=null && monitor.isCanceled())
				{
					isMonitorCancelled = true;
					numOfFiles.clear();
					children.clear();
					subChildren.clear();
					monitor.done();
					return;
				}

				if(!numOfFiles.contains(name))
					numOfFiles.add(name);
				if(path.equalsIgnoreCase(absolutePath)){
					children.add(name);
				}
				else
					subChildren.add(name);
			}
		}
	}

	/**
	 * Get supported files from subdirectories of given directory 
	 * @param path
	 * @param monitor
	 */
	private void getFilesFromSubDirs(String path, IProgressMonitor monitor)
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
					getFiles(FileMethods.appendPathSeparator(path)+ name, monitor);
					if(monitor!=null && monitor.isCanceled())
					{
						isMonitorCancelled = true;
						numOfFiles.clear();
						children.clear();
						subChildren.clear();
						monitor.done();
						return;
					}
				}
				for(String name:dirNames)
				{
					getFilesFromSubDirs(FileMethods.appendPathSeparator(path) + name, monitor);
				}
			}
		}
	}
	/**
	 * Checks if the given file is already present in the forced headers list
	 * @param fileName
	 * @return
	 */
	private boolean isPreviouslySelected(String fileName)
	{
		for(TableItem item:forced_table.getItems())
		{
			if(item.getText().equalsIgnoreCase(fileName))
				return true;
		}
		return false;
	}

	/**
	 * This method gets invoked when a system include path is added or removed.
	 * The method fetches header files from all provided system include paths and
	 * if files provided in Forced headers do not exist in this list, those files 
	 * will be shown in Red color, meaning they will not be included during analysis.
	 */
	private void validateForcedHeaders()
	{
		invalidHeadersCount = 0;

		if(forced_table.getItemCount()>0)
		{
			numOfFiles = new ArrayList<String>();
			children = new ArrayList<String>();
			subChildren = new ArrayList<String>();

			ArrayList<String> paths = new ArrayList<String>();				
			for(int i =0 ; i < syspath_table.getItems().length; i++)
			{
				if(!paths.contains(syspath_table.getItem(i).getText(0)))
					paths.add(syspath_table.getItem(i).getText(0));
			}
			if(!paths.contains(epocRoot+"epoc32"+File.separator+"include"))
				paths.add(epocRoot+"epoc32"+File.separator+"include");

			for(String s:paths)
			{
				getFiles(s, null);
				getFilesFromSubDirs(s, null);
			}

			for(TableItem item:forced_table.getItems())
			{
				if(!numOfFiles.contains(item.getText()))
				{
					item.setForeground(invalidColor);
					item.setFont(invalidFile_Font);
					invalidHeadersCount++;
				}
				else
					item.setForeground(null);
			}			
		}
	}
	
	public String[] getSelectedPlatformHdrPaths()
	{
		ArrayList<String> paths = new ArrayList<String>();
		for(TableItem item:syspath_table.getItems())
			if(!item.getForeground().equals(invalidColor))
				paths.add(item.getText(0));
		return paths.toArray(new String[0]);
	}
}
