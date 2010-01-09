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
package com.nokia.s60tools.compatibilityanalyser.ui.wizards;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import com.nokia.s60tools.compatibilityanalyser.ui.dialogs.ShowFilesListDialog;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

/**
 * Wizard page in Analysis wizard, used to provide system includes  
 * and forced headers
 */
public class PlatformHeadersPage extends S60ToolsWizardPage implements SelectionListener,FeedbackHandler {

	private Composite composite;
	private Button addPath;
	private Button remove;
	
	private Group forcedGrp;
	private Button forced_addBtn;
	private Button forced_removeBtn;
	private Button forced_removeAllBtn;
	private ShowFilesListDialog addDialog;
	private ArrayList<String> displayFiles;
	private String absolutePath;
	private ArrayList<String> numOfFiles;
	private int invalidHeadersCount =0;
	private Color invalidColor = new Color(Display.getDefault(), 255,0,0);
	Font invalidFile_Font=new Font(Display.getCurrent(),"Tahoma",8,SWT.ITALIC);	
	
	Button userIncludes;
	Table dirsList;
	Button usePlatformData;
	Table forced_hdrs_list;
	Button forced_hdrs_check;

	private IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();

	private ProductSdkData currentSdk;
	private ArrayList<String> children;
	private ArrayList<String> subChildren;
	private boolean isMonitorCancelled;
	private ProgressMonitorDialog progDlg;
	private String[] allSysIncHdrPaths;

	public PlatformHeadersPage(CompatibilityAnalyserEngine engine)
	{
		super(Messages.getString("PlatformHeadersPage.wizardtitile")); //$NON-NLS-1$

		setTitle(Messages.getString("PlatformHeadersPage.wizardpageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("PlatformHeadersPage.descreption")); //$NON-NLS-1$

		setPageComplete(false);
		currentSdk = engine.getCurrentSdkData();
	}
	public void recalculateButtonStates() {
	}

	@Override
	public void setInitialFocus() {
	}
	
	/**
	 * Constructs the UI elements of the page on given Composite
	 */
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		GridData gdC = new GridData();
		composite.setLayout(gl);
		composite.setLayoutData(gdC);
		Group grp = new Group(composite, SWT.NONE);

		GridLayout grpGL = new GridLayout(2,false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		grp.setLayout(grpGL);
		grp.setLayoutData(gd);
		grp.setVisible(true);
		grp.setText(Messages.getString("PlatformHeadersPage.sysPaths")); //$NON-NLS-1$

		dirsList = new Table(grp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		GridData listGD = new GridData(GridData.FILL_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL);
		listGD.verticalSpan = 2;
		listGD.heightHint=100;
		dirsList.setEnabled(true);
		dirsList.setLayoutData(listGD);
		dirsList.setHeaderVisible(true);

		TableColumn col = new TableColumn(dirsList, SWT.FILL|SWT.FULL_SELECTION);
		col.setText("Platform headers");
		col.setWidth(400);
		
		if(currentSdk.currentIncludes != null)
		{
			for(String path: currentSdk.currentIncludes)
			{
				TableItem item = new TableItem(dirsList, SWT.NONE);
				item.setText(0, path);
				if(!(new File(path).exists()))
				{
					item.setForeground(invalidColor);
					item.setFont(invalidFile_Font);
				}
			}
			dirsList.select(0);
		}

		addPath = new Button(grp, SWT.PUSH);
		listGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addPath.setText(Messages.getString("PlatformHeadersPage.Add")); //$NON-NLS-1$
		addPath.setLayoutData(listGD);
		addPath.addSelectionListener(this);

		remove = new Button(grp, SWT.PUSH);
		listGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove.setText(Messages.getString("PlatformHeadersPage.Remove")); //$NON-NLS-1$
		remove.setLayoutData(listGD);
		remove.addSelectionListener(this);

		forcedGrp = new Group(grp,SWT.NONE);
		forcedGrp.setText("Forced headers");  
		GridData forcedData=new GridData(GridData.FILL_BOTH);
		forcedData.horizontalSpan=2;
		forcedData.horizontalAlignment=GridData.FILL;
		forcedGrp.setLayoutData(forcedData);
		forcedGrp.setLayout(new GridLayout(2,false));

		forced_hdrs_check = new Button(forcedGrp,SWT.CHECK);
		forced_hdrs_check.setText("Add more forced headers from the above selected system includes");  
		forced_hdrs_check.setToolTipText("");  
		GridData innerData3=new GridData(GridData.FILL);
		innerData3.horizontalSpan=2;
		innerData3.horizontalAlignment=GridData.FILL;
		forced_hdrs_check.setLayoutData(innerData3);
		forced_hdrs_check.setSelection(currentSdk.isForcedProvided);
		forced_hdrs_check.addSelectionListener(this);

		GridData forcedlist1data=new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL|GridData.FILL_BOTH);
		forcedlist1data.verticalSpan=3;
		forcedlist1data.heightHint = 50;

		forced_hdrs_list = new Table(forcedGrp,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL| SWT.FULL_SELECTION);
		forced_hdrs_list.setLayoutData(forcedlist1data);
		forced_hdrs_list.addSelectionListener(this);
		forced_hdrs_list.setEnabled(forced_hdrs_check.getSelection());

		TableColumn col1=new TableColumn(forced_hdrs_list,SWT.LEFT|SWT.FILL);
		col1.setText(Messages.getString("HeaderFilesPage.Column1_Title")); //$NON-NLS-1$
		col1.setWidth(320);

		if(currentSdk.forcedHeaders != null)
		{
			for(String s: currentSdk.forcedHeaders)
			{
				TableItem item = new TableItem(forced_hdrs_list, SWT.NONE);
				item.setText(s);
			}
		}
		GridData forcedbtnData=new GridData(GridData.VERTICAL_ALIGN_CENTER);
		forcedbtnData.widthHint=100;
		forced_addBtn = new Button(forcedGrp,SWT.PUSH);
		forced_addBtn.setText("Add...");  
		forced_addBtn.setLayoutData(forcedbtnData);
		forced_addBtn.setEnabled(false);
		forced_addBtn.addSelectionListener(this);

		forced_removeBtn = new Button(forcedGrp,SWT.PUSH);
		forced_removeBtn.setText(Messages.getString("BaselineEditor.Remove"));  
		forced_removeBtn.setLayoutData(forcedbtnData);
		forced_removeBtn.setEnabled(false);
		forced_removeBtn.addSelectionListener(this);

		forced_removeAllBtn = new Button(forcedGrp,SWT.PUSH);
		forced_removeAllBtn.setText("Remove all");  
		forced_removeAllBtn.setLayoutData(forcedbtnData);
		forced_removeAllBtn.setEnabled(false);
		forced_removeAllBtn.addSelectionListener(this);

		usePlatformData = new Button(composite, SWT.CHECK);
		GridData checkGD = new GridData(GridData.VERTICAL_ALIGN_BEGINNING|GridData.HORIZONTAL_ALIGN_BEGINNING);
		checkGD.verticalIndent=15;
		usePlatformData.setSelection(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.USEPLATFORMDATA_LAST_SELECTION));
		usePlatformData.setLayoutData(checkGD);

		Label lbl=new Label(composite,SWT.WRAP);
		lbl.setText(Messages.getString("PlatformHeadersPage.UseDependencies")); //$NON-NLS-1$
		GridData lblGD=new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		lblGD.verticalIndent=15;
		lbl.setLayoutData(lblGD);

		composite.pack();
		setHelp();
		setControl(composite);

		if(currentSdk.isOpenedFromProject)
			usePlatformData.setSelection(false);
	}

	/**
	 * Validates the values provided by user for various fields in the page.
	 * If those values are invalid, an error message will be shown and returns false.
	 * Otherwise returns true.
	 */
	public boolean canFlipToNextPage()
	{
		this.setErrorMessage(null);
		this.setMessage(null);

		if(forced_hdrs_check.getSelection())
		{
			forced_hdrs_list.setEnabled(true);
			forced_addBtn.setEnabled(true);
			
			if(forced_hdrs_list.getItemCount() > 0)
			{
				forced_removeBtn.setEnabled(true);
				forced_removeAllBtn.setEnabled(true);
			}
			else
			{
				forced_removeBtn.setEnabled(false);
				forced_removeAllBtn.setEnabled(false);
			}
			if(invalidHeadersCount >0)
				this.setMessage("Files Shown in Red color do not exist in any of the selected directories \n So they will be ignored during the analysis." , DialogPage.WARNING);
		}
		else
		{
			forced_hdrs_list.setEnabled(false);
			forced_addBtn.setEnabled(false);
			forced_removeBtn.setEnabled(false);
			forced_removeAllBtn.setEnabled(false);
		}
		return true;
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	
	public void widgetSelected(SelectionEvent e) {
		if(e.widget == addPath)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
			ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this).getPreviousPage();
			dirDialog.setFilterPath(productPage.epocDirectory.getText()+"epoc32"+File.separator+"include");
			String newDir = dirDialog.open();
			if(newDir != null)
			{
				TableItem item = new TableItem(dirsList, SWT.NONE);
				item.setText(0, newDir);
			}

			dirsList.select(0);
			validateForcedHeaders();
			this.getContainer().updateButtons();
		}
		else if(e.widget == remove)
		{
			dirsList.remove(dirsList.getSelectionIndices());
			dirsList.select(0);
			validateForcedHeaders();
			this.getContainer().updateButtons();
		}
		else if(e.widget==forced_hdrs_check)
		{
			if(forced_hdrs_check.getSelection())
				validateForcedHeaders();
			this.getContainer().updateButtons();
		}
		else if(e.widget==forced_addBtn)
		{
			numOfFiles = new ArrayList<String>();
			displayFiles = new ArrayList<String>();
			children = new ArrayList<String>();
			subChildren = new ArrayList<String>();
			isMonitorCancelled = false;

			if(dirsList.getItemCount() > 0)
			{
				ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this).getPreviousPage();
				addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), forced_hdrs_list, this, "", true); 
				ArrayList<String> paths = new ArrayList<String>();				
				for(int i =0 ; i < dirsList.getItems().length; i++)
					paths.add(dirsList.getItem(i).getText(0));
				paths.add(productPage.epocDirectory.getText()+"epoc32"+File.separator+"include");
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
			}
			else
			{
				ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this).getPreviousPage();
				addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), forced_hdrs_list, this, productPage.epocDirectory.getText()+"epoc32"+File.separator+"include", true);
				absolutePath=productPage.epocDirectory.getText()+"epoc32"+File.separator+"include";
				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						try {
							monitor.beginTask("Getting files from "+ absolutePath, 10);
							getFiles(absolutePath,monitor); 
							monitor.worked(5);
							getFilesFromSubDirs(absolutePath,monitor);
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
			this.getContainer().updateButtons();
		}
		else if(e.widget==forced_removeBtn)
		{
			forced_hdrs_list.remove(forced_hdrs_list.getSelectionIndices());
			forced_hdrs_list.select(0);
			this.getContainer().updateButtons();
		}
		else if(e.widget==forced_removeAllBtn)
		{
			forced_hdrs_list.removeAll();
			this.getContainer().updateButtons();
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
		for(TableItem item:forced_hdrs_list.getItems())
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

		if(forced_hdrs_check.getSelection() && forced_hdrs_list.getItemCount()>0)
		{
			numOfFiles = new ArrayList<String>();
			children = new ArrayList<String>();
			subChildren = new ArrayList<String>();
			
			if(dirsList.getItemCount() >0)
			{
				ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this).getPreviousPage();
				ArrayList<String> paths = new ArrayList<String>();				
				for(int i =0 ; i < dirsList.getItems().length; i++)
					paths.add(dirsList.getItem(i).getText(0));
				paths.add(productPage.epocDirectory.getText()+"epoc32"+File.separator+"include");

				for(String s:paths)
				{
					getFiles(s, null);
					getFilesFromSubDirs(s, null);
				}

				for(TableItem item:forced_hdrs_list.getItems())
				{
					if(!numOfFiles.contains(item.getText()))
					{
						item.setForeground(invalidColor);
						invalidHeadersCount++;
					}
					else
						item.setForeground(null);
				}
			}
			else
			{
				ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this).getPreviousPage();
				String[] defIncPaths={
						productPage.epocDirectory.getText()+"epoc32"+File.separator+"include",
				};

				for(String s:defIncPaths)
				{
					getFiles(s, null);
					getFilesFromSubDirs(s, null);
				}


				for(TableItem item:forced_hdrs_list.getItems())
				{
					if(!numOfFiles.contains(item.getText()))
					{
						item.setForeground(invalidColor);
						invalidHeadersCount++;
					}
					else
						item.setForeground(null);
				}
				
			}
		}
	}

	public String[] getSelectedPlatformHdrPaths()
	{
		ArrayList<String> paths = new ArrayList<String>();
		for(TableItem item:dirsList.getItems())
			if(!item.getForeground().equals(invalidColor))
				paths.add(item.getText(0));
		return paths.toArray(new String[0]);
	}
	/**
	 * Sets Context sensitive Help for this page
	 *
	 */
	private void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
				HelpContextIDs.INCLUDES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dirsList,
				HelpContextIDs.INCLUDES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(usePlatformData,
				HelpContextIDs.INCLUDES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(addPath,
				HelpContextIDs.INCLUDES_PAGE);
	}
	public void UpdateFilesList() {
		forced_hdrs_list.select(0);
	}
	public void UpdatePage() {
		this.getContainer().updateButtons();
	}
}
