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
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.compatibilityanalyser.data.LastUsedKnownissues;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.ToolChain;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.dialogs.ShowFilesListDialog;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

public class LibraryFilesPage extends S60ToolsWizardPage implements SelectionListener, ModifyListener, FeedbackHandler {

	private Composite composite;
	private Group dirGroup;
	
	//Select default build target
	private Button default_build_target_radioBtn;
	
	//Select build target
	Button buildtarget_radioBtn;
	Button userDirs_radioBtn;
	List buildTarget_list;
	
	//or Select directory
	private TabFolder dllPaths_Folder;
	private TabItem dsoPaths_tab;
	private TabItem dllDir_tab;
	Table userDsoPath_list;
	Table userDllPath_list;
	private Button addDllDir_Btn;
	private Button removeDllDir_Btn;
	private Button removeAllDllDir_Btn;
	private Button addDsoDir_Btn;
	private Button removeDsoDir_Btn;
	private Button removeAllDsoDir_Btn;
	
	//Analyse all files radio button
	Button analyseAllFiles_radioBtn;
	//Analyse only selected files radio button
	private Button analyseSelected_radioBtn;

	//List to add dso files
	Table dsoFiles_list;
	
	//Add, Remove, Remove all, Remove non existing buttons
	private Button addDso_Btn;
	private Button removeDso_Btn;
	private Button removeAllDso_Btn;
	private Button removeNonExistingDso_Btn;

	//Drop down for tool chain selection
	private Combo toolChain_dropdown;
	
	//Dialog to select the dso files
	private ShowFilesListDialog addDialog;
	
	//Invalid dso files in the list will shown in red color
	Font invalidFile_Font=new Font(Display.getCurrent(),"Tahoma",8,SWT.ITALIC);	
	Color invalidColor = new Color(Display.getDefault(), 255,0,0);
	
	ToolChain selectedToolChain;
	private Vector<ToolChain> toolChains;
	private ArrayList<String> filesNamesList;
	String [] selectedPlatform;
	String releaseRoot;
	private String absolutePath;
	private ProductSdkData currentSdk;
	private int invalidLibsCount;
	
	private ArrayList<String> displayFiles;
	private ArrayList<String> children;
	private ArrayList<String> subChildren;
	boolean  isMonitorCancelled;
	protected String[] allLibsPaths;

	public LibraryFilesPage(CompatibilityAnalyserEngine engine)
	{
		super(Messages.getString("LibraryFilesPage.WindowTitle")); //$NON-NLS-1$
		setTitle(Messages.getString("LibraryFilesPage.PageTitle")); //$NON-NLS-1$
		
		setDescription(Messages.getString("LibraryFilesPage.Descreption")); //$NON-NLS-1$
		setPageComplete(false);
		currentSdk = engine.getCurrentSdkData();
	}
	
	public void recalculateButtonStates() {
	}

	@Override
	public void setInitialFocus() {
	}

	/**
	 * Constructs the UI elements of the page on given composite
	 */
	public void createControl(Composite parent) {
		
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		
		createRootPathGroup();
		createLibrariesSelection();
		
		Label toolChainLabel = new Label(composite, SWT.NONE);
		toolChainLabel.setText(Messages.getString("LibraryFilesPage.SelectToolChains")); //$NON-NLS-1$
		
		toolChain_dropdown = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		toolChain_dropdown.setLayoutData(gd);
		toolChain_dropdown.setEnabled(true);
		
		toolChains = CompatibilityAnalyserEngine.getToolChains();
		int size = toolChains.size();
		
		if(size > 0)
		{
			for(int i=0;i<size; i++)
			{
				ToolChain t = toolChains.elementAt(i);
				toolChain_dropdown.add(t.getDescription(),i);
			}
			toolChain_dropdown.select(0);
			selectedToolChain = toolChains.elementAt(0);
		}
		
		if(currentSdk.toolChain != null)
		{
			int index = -1;
			for(int i=0; i<toolChains.size(); i++)
			{
				if(toolChains.elementAt(i).getName().equals(currentSdk.toolChain))
				{
					index = i;
					break;
				}
			}
			if(index != -1){
				toolChain_dropdown.select(index);
				selectedToolChain = toolChains.elementAt(index);
			}
			else
			{
				toolChain_dropdown.setText(currentSdk.toolChain);
			}
		}
		toolChain_dropdown.addModifyListener(this);
		
		//For the first time disable the dso/dll path tabs
		GridData data2 = (GridData)dllPaths_Folder.getLayoutData();
		data2.exclude = !userDirs_radioBtn.getSelection();
		dllPaths_Folder.setVisible(userDirs_radioBtn.getSelection());
	
		GridData data3 = (GridData)buildTarget_list.getLayoutData();
		data3.exclude = !buildtarget_radioBtn.getSelection();
		buildTarget_list.setVisible(buildtarget_radioBtn.getSelection());
		
		setHelp();	
		setControl(composite);
	}

	/**
	 * Checks the values provided by user for various fields and validates them
	 * If any invalid values are given, this sets an error message and return false.
	 * Otherwise it return true
	 */
	public boolean canFlipToNextPage()
	{
		this.setErrorMessage(null);
		this.setMessage(null);
		
		if(buildtarget_radioBtn.getSelection() && buildTarget_list.getItemCount()== 0 ){
			this.setErrorMessage(Messages.getString("LibraryFilesPage.40")); //$NON-NLS-1$
			return false;
		}
		if(analyseSelected_radioBtn.getSelection())
		{
			addDso_Btn.setEnabled(default_build_target_radioBtn.getSelection() || buildtarget_radioBtn.getSelection() || 
					(userDirs_radioBtn.getSelection() && userDsoPath_list.getItemCount()!=0));
			
			if(analyseSelected_radioBtn.getSelection() && dsoFiles_list.getItemCount()>0)
				validateFileSet();
			
			if(dsoFiles_list.getItemCount() == 0)
			{
				removeDso_Btn.setEnabled(false);
				removeAllDso_Btn.setEnabled(false);
				removeNonExistingDso_Btn.setEnabled(false);
				this.setErrorMessage(Messages.getString("LibraryFilesPage.NoLibsSelected")); //$NON-NLS-1$
				return false;
			}
			else if (dsoFiles_list.getItemCount() >0)
			{
				removeDso_Btn.setEnabled(true);
				removeAllDso_Btn.setEnabled(true);
				
				if(invalidLibsCount >0)
				{
					removeNonExistingDso_Btn.setEnabled(true);
										
					if(invalidLibsCount == dsoFiles_list.getItemCount())
					{
						this.setErrorMessage("None of the selected Libraries exist in the provided header directories."); //$NON-NLS-1$
						return false;
					}
					else if(invalidLibsCount >0)
						this.setMessage("Files shown in Red color do not exist in any of the selected directories \n So they will not be analysed." , DialogPage.WARNING); //$NON-NLS-1$)
				}
				else
					removeNonExistingDso_Btn.setEnabled(false);
			}
			if(userDirs_radioBtn.getSelection() && userDsoPath_list.getItemCount() == 0)
			{
				this.setErrorMessage(Messages.getString("LibraryFilesPage.RootDirisNull")); //$NON-NLS-1$
				return false;
			}
			if(userDirs_radioBtn.getSelection() && userDllPath_list.getItemCount() == 0)
			{
				this.setErrorMessage(Messages.getString("LibraryFilesPage.DllRootDirisNull")); //$NON-NLS-1$
				return false;
			}
			if(userDirs_radioBtn.getSelection() && getSelectedDSOPaths().length == 0)
			{
				this.setErrorMessage("Provided DSO paths are invalid. Please provide valid paths.");
				return false;
			}
			if(userDirs_radioBtn.getSelection() && getSelectedDLLPaths().length == 0)
			{
				this.setErrorMessage("Provided DLL paths are invalid. Please provide valid paths.");
				return false;
			}
		}
		else if(analyseAllFiles_radioBtn.getSelection() && userDirs_radioBtn.getSelection() && userDsoPath_list.getItemCount() == 0)
		{
			setErrorMessage(Messages.getString("LibraryFilesPage.RootDirisNull"));
			return false;
		}
		else if(analyseAllFiles_radioBtn.getSelection() && userDirs_radioBtn.getSelection() && userDllPath_list.getItemCount() == 0)
		{
			setErrorMessage(Messages.getString("LibraryFilesPage.DllRootDirisNull"));
			return false;
		}
		else if(analyseAllFiles_radioBtn.getSelection() && userDirs_radioBtn.getSelection() && getSelectedDSOPaths().length == 0)
		{
			setErrorMessage("Provided DSO paths are invalid. Please provide valid paths.");
			return false;
		}
		else if(analyseAllFiles_radioBtn.getSelection() && userDirs_radioBtn.getSelection() && getSelectedDLLPaths().length == 0)
		{
			setErrorMessage("Provided DLL paths are invalid. Please provide valid paths.");
			return false;
		}
		
		if(toolChain_dropdown.getItemCount() ==0)
		{
			this.setErrorMessage(Messages.getString("LibraryFilesPage.NoToolchains")); //$NON-NLS-1$
			return false;
		}
		else if(toolChain_dropdown.indexOf(toolChain_dropdown.getText()) == -1)
		{
			this.setErrorMessage("Given ToolChain is not installed.");
			return false;
		}
		
		return true;
	}
	private void createRootPathGroup()
	{
		dirGroup = new Group(composite, SWT.NONE);
		dirGroup.setLayout(new GridLayout(1,false));
		dirGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dirGroup.setText(Messages.getString("LibraryFilesPage.RootDirOfLibs")); //$NON-NLS-1$
		
		default_build_target_radioBtn = new Button(dirGroup, SWT.RADIO);
		default_build_target_radioBtn.setText(Messages.getString("LibraryFilesPage.DefaultBuildTarget")); //$NON-NLS-1$
		default_build_target_radioBtn.setToolTipText(Messages.getString("LibraryFilesPage.tooltip_default_target")); //$NON-NLS-1$
		default_build_target_radioBtn.setSelection(currentSdk.default_platfrom_selection);
		default_build_target_radioBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		default_build_target_radioBtn.addSelectionListener(this);
		
		buildtarget_radioBtn = new Button(dirGroup, SWT.RADIO);
		buildtarget_radioBtn.setText(Messages.getString("LibraryFilesPage.SelectBuildTarget")); //$NON-NLS-1$
		buildtarget_radioBtn.setToolTipText(Messages.getString("LibraryFilesPage.tooltip_target")); //$NON-NLS-1$
		buildtarget_radioBtn.setSelection(currentSdk.platfromSelection);
		buildtarget_radioBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		buildTarget_list = new List(dirGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData listGD = new GridData(GridData.FILL_HORIZONTAL);
		listGD.heightHint=50;
		//buildTarget_list.setEnabled(currentSdk.platfromSelection);
		buildTarget_list.setLayoutData(listGD);
		buildTarget_list.addSelectionListener(this);
					
		userDirs_radioBtn = new Button(dirGroup, SWT.RADIO);
		userDirs_radioBtn.setText(Messages.getString("LibraryFilesPage.AddFilesBelowDirectory")); //$NON-NLS-1$
		userDirs_radioBtn.setToolTipText(Messages.getString("LibraryFilesPage.tooltip_UserDirectory")); //$NON-NLS-1$
		userDirs_radioBtn.setSelection(currentSdk.selected_library_dirs);
		userDirs_radioBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userDirs_radioBtn.addSelectionListener(this);
 		
		dllPaths_Folder = new TabFolder(dirGroup, SWT.NONE);
		dllPaths_Folder.setLayout(new GridLayout(2, false));
		dllPaths_Folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		dsoPaths_tab = new TabItem(dllPaths_Folder, SWT.NONE);
		dsoPaths_tab.setText("DSO Paths");
		
		Composite dsoComposite = new Composite(dllPaths_Folder, SWT.NONE);
		dsoComposite.setLayout(new GridLayout(2, false));
		dsoComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		userDsoPath_list = new Table(dsoComposite, SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
		GridData comboGD = new GridData(GridData.FILL_HORIZONTAL);
		comboGD.verticalSpan=4;
		comboGD.heightHint = 100;
		
		TableColumn dsoColumn = new TableColumn(userDsoPath_list, SWT.FULL_SELECTION);
		dsoColumn.setText("DSO Paths");
		
		LastUsedKnownissues data = new LastUsedKnownissues();
		String[] lastUsedDsoDirs = data.getPreviousValues(LastUsedKnownissues.ValueTypes.LIBRARY_DIRECTORIES);
		
		if (lastUsedDsoDirs != null) {
				for(String path: lastUsedDsoDirs)
				{
					TableItem item = new TableItem(userDsoPath_list, SWT.NONE);
					item.setText(0, path);
					
					if(!(new File(path).exists()))
					{
						changeColorFont(item);
					}
				}
				userDsoPath_list.select(0);
		}
		
		if(currentSdk.selected_library_dirs) 
		{
			if(currentSdk.currentLibsDir != null)
			{
				userDsoPath_list.removeAll();
				for(String path: currentSdk.currentLibsDir)
				{
					TableItem item = new TableItem(userDsoPath_list, SWT.NONE);
					item.setText(0, path);
					if(!(new File(path).exists()))
					{
						changeColorFont(item);
					}
				}
				userDsoPath_list.select(0);
			}
		}
		userDsoPath_list.setLayoutData(comboGD);
		dsoColumn.setWidth(300);
		
		
		GridData btnData1=new GridData(GridData.VERTICAL_ALIGN_END);
		btnData1.widthHint=100;

		addDsoDir_Btn = new Button(dsoComposite, SWT.PUSH);
		addDsoDir_Btn.setText(Messages.getString("LibraryFilesPage.Browse")); //$NON-NLS-1$
		addDsoDir_Btn.setLayoutData(btnData1);
		addDsoDir_Btn.addSelectionListener(this);
		
		removeDsoDir_Btn = new Button(dsoComposite, SWT.PUSH);
		removeDsoDir_Btn.setText("Remove");
		removeDsoDir_Btn.setEnabled(userDirs_radioBtn.getSelection());
		removeDsoDir_Btn.setLayoutData(btnData1);
		removeDsoDir_Btn.addSelectionListener(this);
		
		removeAllDsoDir_Btn = new Button(dsoComposite, SWT.PUSH);
		removeAllDsoDir_Btn.setText("Remove all");
		removeAllDsoDir_Btn.setEnabled(userDirs_radioBtn.getSelection());
		removeAllDsoDir_Btn.setLayoutData(btnData1);
		removeAllDsoDir_Btn.addSelectionListener(this);
		
		dsoPaths_tab.setControl(dsoComposite);
		
		dllDir_tab = new TabItem(dllPaths_Folder, SWT.NONE);
		dllDir_tab.setText("DLL Paths");
		
		Composite dllComposite = new Composite(dllPaths_Folder, SWT.NONE);
		dllComposite.setLayout(new GridLayout(2, false));
		dllComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		userDllPath_list = new Table(dllComposite, SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
		GridData dllGD = new GridData(GridData.FILL_HORIZONTAL);
		dllGD.verticalSpan=4;
		dllGD.heightHint = 100;
		
		TableColumn dllColumn = new TableColumn(userDllPath_list, SWT.FULL_SELECTION);
		dllColumn.setText("DLL Paths");
		
		dllPaths_Folder.pack();

		String[] lastUsedDllDirs = data.getPreviousValues(LastUsedKnownissues.ValueTypes.DLL_DIRECTORIES);
		
		if (lastUsedDllDirs != null) {
				for (String path: lastUsedDllDirs)
				{
					TableItem item = new TableItem(userDllPath_list, SWT.NONE);
					item.setText(0, path);
					if(!(new File(path).exists()))
					{
						changeColorFont(item);
					}
				}
				userDllPath_list.select(0);
		}
		if(currentSdk.selected_library_dirs)
		{
			if(currentSdk.currentDllDir != null)
			{
				userDllPath_list.removeAll();
				for (String path: currentSdk.currentDllDir)
				{
					TableItem item = new TableItem(userDllPath_list, SWT.NONE);
					item.setText(0, path);
					if(!(new File(path).exists()))
					{
						changeColorFont(item);
					}
				}
				userDllPath_list.select(0);
			}
		}
		
		dllColumn.setWidth(300);
		
		userDllPath_list.setLayoutData(dllGD);
		
		GridData btnData2=new GridData(GridData.HORIZONTAL_ALIGN_END);
		btnData2.widthHint=100;

		addDllDir_Btn = new Button(dllComposite, SWT.PUSH);
		addDllDir_Btn.setText(Messages.getString("LibraryFilesPage.Browse")); //$NON-NLS-1$
		addDllDir_Btn.setLayoutData(btnData2);
		addDllDir_Btn.addSelectionListener(this);
		
		removeDllDir_Btn = new Button(dllComposite, SWT.PUSH);
		removeDllDir_Btn.setText("Remove");
		removeDllDir_Btn.setEnabled(userDirs_radioBtn.getSelection());
		removeDllDir_Btn.setLayoutData(btnData2);
		removeDllDir_Btn.addSelectionListener(this);
		
		removeAllDllDir_Btn = new Button(dllComposite, SWT.PUSH);
		removeAllDllDir_Btn.setText("Remove all");
		removeAllDllDir_Btn.setEnabled(userDirs_radioBtn.getSelection());
		removeAllDllDir_Btn.setLayoutData(btnData2);
		removeAllDllDir_Btn.addSelectionListener(this);
	
		//dsoColumn.pack();
		//dllColumn.pack();
		//dsoColumn.setWidth(300);
		//dllColumn.setWidth(300);
		userDllPath_list.pack();
		userDsoPath_list.pack();
		dsoComposite.pack();
		dllComposite.pack();
		
		dllDir_tab.setControl(dllComposite);
		

	}
	
	private void createLibrariesSelection()
	{
		Group grp = new Group(composite, SWT.NONE);
		grp.setLayout(new GridLayout(2,false));
		grp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grp.setText(Messages.getString("LibraryFilesPage.GrpName_LibrarySelection")); //$NON-NLS-1$
		
		analyseAllFiles_radioBtn = new Button(grp, SWT.RADIO);
		GridData buttonGD = new GridData(GridData.FILL_HORIZONTAL);
		buttonGD.horizontalSpan = 2;
		analyseAllFiles_radioBtn.setText(Messages.getString("LibraryFilesPage.name_allfiles")); //$NON-NLS-1$
		analyseAllFiles_radioBtn.setToolTipText(Messages.getString("LibraryFilesPage.tooltip_allfiles")); //$NON-NLS-1$
		analyseAllFiles_radioBtn.setSelection(currentSdk.analyzeAllLibs);
		analyseAllFiles_radioBtn.setLayoutData(buttonGD);
		
		analyseSelected_radioBtn = new Button(grp, SWT.RADIO);
		analyseSelected_radioBtn.setText(Messages.getString("LibraryFilesPage.18")); //$NON-NLS-1$
		analyseSelected_radioBtn.setToolTipText(Messages.getString("LibraryFilesPage.19")); //$NON-NLS-1$
		analyseSelected_radioBtn.setSelection(!currentSdk.analyzeAllLibs);
		buttonGD = new GridData(GridData.FILL_HORIZONTAL);
		buttonGD.horizontalSpan = 2;
		analyseSelected_radioBtn.setLayoutData(buttonGD);
		analyseSelected_radioBtn.addSelectionListener(this);
		
		dsoFiles_list = new Table(grp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData listGD = new GridData(GridData.FILL_BOTH|GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL);
		listGD.verticalSpan = 4;
		listGD.heightHint=100;
		dsoFiles_list.setEnabled(analyseSelected_radioBtn.getSelection());
		dsoFiles_list.setLayoutData(listGD);
		
		if(currentSdk.libraryFilesList != null)
		{
			for(String s:currentSdk.libraryFilesList)
			{
				TableItem item = new TableItem(dsoFiles_list, SWT.NONE);
				item.setText(s);
			}
		}
		
		addDso_Btn = new Button(grp, SWT.PUSH);
		listGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addDso_Btn.setText(Messages.getString("LibraryFilesPage.Add")); //$NON-NLS-1$
		addDso_Btn.setLayoutData(listGD);
		addDso_Btn.setEnabled(analyseSelected_radioBtn.getSelection());
		addDso_Btn.addSelectionListener(this);
				
		removeDso_Btn = new Button(grp, SWT.PUSH);
		listGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		removeDso_Btn.setText(Messages.getString("LibraryFilesPage.Remove")); //$NON-NLS-1$
		removeDso_Btn.setLayoutData(listGD);
		removeDso_Btn.setEnabled(analyseSelected_radioBtn.getSelection() && dsoFiles_list.getItemCount() >0);
		removeDso_Btn.addSelectionListener(this);
		
		removeAllDso_Btn = new Button(grp, SWT.PUSH);
		listGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		removeAllDso_Btn.setText(Messages.getString("LibraryFilesPage.RemoveAll")); //$NON-NLS-1$
		removeAllDso_Btn.setLayoutData(listGD);
		removeAllDso_Btn.setEnabled(analyseSelected_radioBtn.getSelection() && dsoFiles_list.getItemCount() >0);
		removeAllDso_Btn.addSelectionListener(this);
		
		removeNonExistingDso_Btn = new Button(grp, SWT.PUSH);
		listGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		removeNonExistingDso_Btn.setText("Remove non-existing"); //$NON-NLS-1$
		removeNonExistingDso_Btn.setLayoutData(listGD);
		removeNonExistingDso_Btn.setEnabled(false);
		removeNonExistingDso_Btn.addSelectionListener(this);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if(e.widget == userDirs_radioBtn || e.widget == default_build_target_radioBtn)
		{
			removeDsoDir_Btn.setEnabled(userDirs_radioBtn.getSelection() && userDsoPath_list.getItemCount() != 0);
			removeAllDsoDir_Btn.setEnabled(userDirs_radioBtn.getSelection() && userDsoPath_list.getItemCount() != 0);
	
			removeDllDir_Btn.setEnabled(userDirs_radioBtn.getSelection() && userDllPath_list.getItemCount() != 0);
			removeAllDllDir_Btn.setEnabled(userDirs_radioBtn.getSelection() && userDllPath_list.getItemCount() != 0);
			
			GridData data = (GridData)buildTarget_list.getLayoutData();
			data.exclude = !buildtarget_radioBtn.getSelection();
			buildTarget_list.setVisible(buildtarget_radioBtn.getSelection());
			
			GridData data2 = (GridData)dllPaths_Folder.getLayoutData();
			data2.exclude = !userDirs_radioBtn.getSelection();
			dllPaths_Folder.setVisible(userDirs_radioBtn.getSelection());
			
			composite.getParent().setSize(composite.getParent().getSize().x+1, composite.getParent().getSize().y);
			
			this.getContainer().updateButtons();
		}
		else if(e.widget == addDsoDir_Btn)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
			dirDialog.setFilterPath(releaseRoot);
			String newDir = dirDialog.open();
			if(newDir != null)
			{
				new TableItem(userDsoPath_list,SWT.NONE).setText(0, newDir);
				userDsoPath_list.select(0);
			}
			removeDsoDir_Btn.setEnabled(userDsoPath_list.getItemCount() != 0);
			removeAllDsoDir_Btn.setEnabled(userDsoPath_list.getItemCount() != 0);
			this.getContainer().updateButtons();
		}
		else if(e.widget == addDllDir_Btn)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
			dirDialog.setFilterPath(releaseRoot);
			String newDir = dirDialog.open();
			if(newDir != null)
			{
				new TableItem(userDllPath_list,SWT.NONE).setText(0, newDir);
				userDllPath_list.select(0);
			}
			removeDllDir_Btn.setEnabled(userDllPath_list.getItemCount() != 0);
			removeAllDllDir_Btn.setEnabled(userDllPath_list.getItemCount() != 0);
			this.getContainer().updateButtons();
		}
		else if(e.widget == buildTarget_list)
		{
			selectedPlatform = buildTarget_list.getSelection();
		}
		if(e.widget == analyseSelected_radioBtn)
		{
			dsoFiles_list.setEnabled(analyseSelected_radioBtn.getSelection());
			addDso_Btn.setEnabled(analyseSelected_radioBtn.getSelection() && userDsoPath_list.getItemCount()!=0);
			removeDso_Btn.setEnabled(analyseSelected_radioBtn.getSelection() && userDsoPath_list.getItemCount()!=0);
			removeAllDso_Btn.setEnabled(analyseSelected_radioBtn.getSelection() && userDsoPath_list.getItemCount()!=0);
			removeNonExistingDso_Btn.setEnabled(analyseSelected_radioBtn.getSelection() && userDsoPath_list.getItemCount()!=0);
			this.getContainer().updateButtons();
		}
		if(e.widget == addDso_Btn)
		{
			filesNamesList = new ArrayList<String>();
			displayFiles = new ArrayList<String>();
			children = new ArrayList<String>();
			subChildren = new ArrayList<String>();
			isMonitorCancelled = false;
			
			if(userDirs_radioBtn.getSelection() && userDsoPath_list.getItemCount() == 1)
			{
				absolutePath = userDsoPath_list.getItem(0).getText(0);
				absolutePath = FileMethods.convertForwardToBackwardSlashes(absolutePath);
				
				addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), dsoFiles_list, this, absolutePath, false);

				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						try {
							monitor.beginTask("Getting files from "+ absolutePath, 10);
							getFiles(absolutePath, monitor); 
							monitor.worked(5);
							getFilesFromSubDirs(absolutePath, monitor);
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
					ProgressMonitorDialog progDlg = new ProgressMonitorDialog(shell);
					progDlg.run(true, true, op);
					progDlg.setBlockOnOpen(true);
				} catch (InvocationTargetException err) {
					err.printStackTrace();
				} catch (InterruptedException err) {
					err.printStackTrace();
				}

			}
			else if(userDirs_radioBtn.getSelection() && userDsoPath_list.getItemCount() > 1)
			{
				addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), dsoFiles_list, this, "", false);

				String[] paths = new String[userDsoPath_list.getItems().length];
				for(int i =0 ; i < userDsoPath_list.getItems().length; i++)
					paths[i] =  userDsoPath_list.getItem(i).getText(0);
				allLibsPaths = paths;

				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						try {
							int i=1;
							for(String path: allLibsPaths)
							{
								absolutePath = FileMethods.convertForwardToBackwardSlashes(path);
								monitor.beginTask("Getting files from "+ absolutePath, allLibsPaths.length);
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
					ProgressMonitorDialog progDlg = new ProgressMonitorDialog(shell);
					progDlg.run(true, true, op);
					progDlg.setBlockOnOpen(true);
				} catch (InvocationTargetException err) {
					err.printStackTrace();
				} catch (InterruptedException err) {
					err.printStackTrace();
				}
			}
			else if(buildtarget_radioBtn.getSelection())
			{
				if(releaseRoot != null)
				{
					addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), dsoFiles_list, this, "", false);
					String [] paths = new String[buildTarget_list.getSelectionCount()];
					
					for(int i=0; i < buildTarget_list.getSelectionCount(); i++)
					{
						paths[i] =  FileMethods.appendPathSeparator(releaseRoot) + buildTarget_list.getSelection()[i] + File.separator + Messages.getString("LibraryFilesPage.lib"); //$NON-NLS-1$ //$NON-NLS-2$;
					}
					
					allLibsPaths = paths;

					IRunnableWithProgress op = new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) {
							try {
								int i=1;
								for(String path: allLibsPaths)
								{
									absolutePath = FileMethods.convertForwardToBackwardSlashes(path);
									monitor.beginTask("Getting files from "+ absolutePath, allLibsPaths.length);
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
						ProgressMonitorDialog progDlg = new ProgressMonitorDialog(shell);
						progDlg.run(true, true, op);
						progDlg.setBlockOnOpen(true);
					} catch (InvocationTargetException err) {
						err.printStackTrace();
					} catch (InterruptedException err) {
						err.printStackTrace();
					}
				}
				
			}
			else if(default_build_target_radioBtn.getSelection())
			{
				String buildTarget = CompatibilityAnalyserEngine.getDefaultBuildPlatform(releaseRoot, currentSdk.productSdkVersion);
				
				absolutePath = FileMethods.appendPathSeparator(releaseRoot) + buildTarget +  File.separator + Messages.getString("LibraryFilesPage.lib"); //$NON-NLS-1$ //$NON-NLS-2$
				absolutePath = FileMethods.convertForwardToBackwardSlashes(absolutePath);
				addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), dsoFiles_list, this, absolutePath, false);

				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						try {
							monitor.beginTask("Getting files from "+ absolutePath, 10);
							getFiles(absolutePath, monitor); 
							monitor.worked(5);
							getFilesFromSubDirs(absolutePath, monitor);
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
					ProgressMonitorDialog progDlg = new ProgressMonitorDialog(shell);
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
				if(filesNamesList.size()!=dsoFiles_list.getItemCount())
					for(String name : filesNamesList){
						if(!isPreviouslySelected(name))
							displayFiles.add(name);
						else
						{
							if(subChildren.contains(name))
								subChildren.remove(name);
							if(children.contains(name))
								children.remove(name);
						}
					}
				Collections.sort(displayFiles);
				addDialog.children = children;
				addDialog.subChildren = subChildren;
			
				if(displayFiles.size() == 0 && filesNamesList.size() !=0)
				{
					Runnable showMessageRunnable = new Runnable(){
						public void run(){
							MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
									Messages.getString("HeaderFilesPage.CompatibilityAnalyser"), "All libraries in the root directory are already selected"); //$NON-NLS-1$ //$NON-NLS-2$
						}
					};
					Display.getDefault().asyncExec(showMessageRunnable); 
				}				
				else if(filesNamesList.size() == 0)
				{
					Runnable showMessageRunnable = new Runnable(){
						public void run(){
							MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
									Messages.getString("HeaderFilesPage.CompatibilityAnalyser"), "No libraries exist under the given directory path"); //$NON-NLS-1$ //$NON-NLS-2$
						}
					};
					Display.getDefault().asyncExec(showMessageRunnable); 
				}	
				else{
					addDialog.open();
					addDialog.filesList.setItems(displayFiles.toArray(new String[displayFiles.size()]));
					addDialog.filesList.select(0);
					addDialog.fileNamesList = addDialog.filesList.getItems();
				}
							
			}
			this.getContainer().updateButtons();
		}
		else if(e.widget == removeDso_Btn)
		{
			dsoFiles_list.remove(dsoFiles_list.getSelectionIndices());
			dsoFiles_list.select(0);
			this.getContainer().updateButtons();
		}
		else if(e.widget == removeAllDso_Btn)
		{
			dsoFiles_list.removeAll();
			this.getContainer().updateButtons();
		}
		else if(e.widget == removeNonExistingDso_Btn)
		{
			int [] invalidIndices = new int[invalidLibsCount];
			int j = 0;
			
			for(int i=0; i<dsoFiles_list.getItemCount();i++)
			{
				System.out.println("Item Color is " + dsoFiles_list.getItem(i).getForeground() + " And InvalidColor is " + invalidColor);
				if(dsoFiles_list.getItem(i).getForeground().equals(invalidColor))
				{
					invalidIndices[j] = i;
					j++;
				}
         	}
			dsoFiles_list.remove(invalidIndices);
			invalidLibsCount = 0;
	
			this.getContainer().updateButtons();
		}
		else if(e.widget == removeDsoDir_Btn)
		{
			userDsoPath_list.remove(userDsoPath_list.getSelectionIndices());
			userDsoPath_list.select(0);
			removeDsoDir_Btn.setEnabled(userDsoPath_list.getItemCount() != 0);
			removeAllDsoDir_Btn.setEnabled(userDsoPath_list.getItemCount() != 0);
			this.getContainer().updateButtons();
		}
		else if(e.widget == removeAllDsoDir_Btn)
		{
			userDsoPath_list.removeAll();
			removeDsoDir_Btn.setEnabled(userDsoPath_list.getItemCount() != 0);
			removeAllDsoDir_Btn.setEnabled(userDsoPath_list.getItemCount() != 0);
			this.getContainer().updateButtons();
		}
		else if(e.widget == removeDllDir_Btn)
		{
			userDllPath_list.remove(userDllPath_list.getSelectionIndices());
			userDllPath_list.select(0);
			removeDllDir_Btn.setEnabled(userDllPath_list.getItemCount() != 0);
			removeAllDllDir_Btn.setEnabled(userDllPath_list.getItemCount() != 0);
			this.getContainer().updateButtons();
		}
		else if(e.widget == removeAllDllDir_Btn)
		{
			userDllPath_list.removeAll();
			removeDllDir_Btn.setEnabled(userDllPath_list.getItemCount() != 0);
			removeAllDllDir_Btn.setEnabled(userDllPath_list.getItemCount() != 0);
			this.getContainer().updateButtons();
		}
	}
	public void getFiles(String path, IProgressMonitor monitor)
	{
		if(path == null || path.length() == 0)
			return;

		File rootDir = new File(path);

		if(rootDir.isDirectory())
		{
			String [] fileNames = rootDir.list(new FilenameFilter()
			{

				public boolean accept(File file, String name) {
					File temp = new File(FileMethods.appendPathSeparator(file.getAbsolutePath())+ name);

					if(temp.isFile() && name.endsWith(".dso") && !name.contains("{") && !name.contains("}"))
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
					filesNamesList.clear();
					displayFiles.clear();
					children.clear();
					subChildren.clear();
					monitor.done();
					return;
				}

				if(!filesNamesList.contains(name.toLowerCase()))
					filesNamesList.add(name.toLowerCase());

				if(path.equalsIgnoreCase(absolutePath))
					children.add(name.toLowerCase());
				else
					subChildren.add(name.toLowerCase());
			}
		}
		return;
	}
	
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
						filesNamesList.clear();
						displayFiles.clear();
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
	private boolean isPreviouslySelected(String fileName)
	{
		for(TableItem s:dsoFiles_list.getItems())
		{
			if(s.getText().equalsIgnoreCase(fileName))
				return true;
		}
		return false;
	}
	
	private void validateFileSet()
	{
		invalidLibsCount = 0;

		if(userDirs_radioBtn.getSelection())
		{
			if(userDsoPath_list.getItemCount() == 0)
			{
				if(analyseSelected_radioBtn.getSelection() && dsoFiles_list.getItemCount()>0)
				{
					for(TableItem item:dsoFiles_list.getItems())
					{
						changeColorFont(item);
						invalidLibsCount++;
					}
				}
			}
			else if(userDsoPath_list.getItemCount() >0)
			{
				if(analyseSelected_radioBtn.getSelection() && dsoFiles_list.getItemCount()>0)
				{
					filesNamesList = new ArrayList<String>();
					children = new ArrayList<String>();
					subChildren = new ArrayList<String>();
					
					for(TableItem s:userDsoPath_list.getItems())
					{
						getFiles(s.getText(0),null);
						//if(useRecursive.getSelection())
						getFilesFromSubDirs(s.getText(0),null);
					}

					for(TableItem item:dsoFiles_list.getItems())
					{
						if(!filesNamesList.contains(item.getText(0).toLowerCase()))
						{
							changeColorFont(item);
							invalidLibsCount++;
						}
						else
						{
							item.setForeground(null);
							item.setFont(null);
						}
					}
				}
			}
		}
		else if(buildtarget_radioBtn.getSelection())
		{
			if(analyseSelected_radioBtn.getSelection() && dsoFiles_list.getItemCount()>0)
			{
				String path = FileMethods.appendPathSeparator(releaseRoot) + Messages.getString("LibraryFilesPage.armv5") + File.separator + Messages.getString("LibraryFilesPage.lib"); //$NON-NLS-1$ //$NON-NLS-2$
				path = FileMethods.convertForwardToBackwardSlashes(path);
				
				filesNamesList = new ArrayList<String>();
				subChildren = new ArrayList<String>();
				children = new ArrayList<String>();
				
				getFiles(path,null);
				getFilesFromSubDirs(path,null);

				for(TableItem item:dsoFiles_list.getItems())
				{
					if(!filesNamesList.contains(item.getText(0).toLowerCase()))
					{
						changeColorFont(item);
						invalidLibsCount++;
					}
					else
					{
						item.setForeground(null);
						item.setFont(null);
					}
				}
			}
		}
		else if(default_build_target_radioBtn.getSelection())
		{
			if(analyseSelected_radioBtn.getSelection() && dsoFiles_list.getItemCount()>0 && currentSdk.productSdkVersion!=null)
			{
				String buildTarget = CompatibilityAnalyserEngine.getDefaultBuildPlatform(releaseRoot, currentSdk.productSdkVersion);
				String libs_Path = FileMethods.appendPathSeparator(releaseRoot) + buildTarget +  File.separator + Messages.getString("LibraryFilesPage.lib");
				libs_Path = FileMethods.convertForwardToBackwardSlashes(libs_Path);
				
				filesNamesList = new ArrayList<String>();
				subChildren = new ArrayList<String>();
				children = new ArrayList<String>();
				
				getFiles(libs_Path,null);
				getFilesFromSubDirs(libs_Path,null);

				for(TableItem item:dsoFiles_list.getItems())
				{
					if(!filesNamesList.contains(item.getText(0).toLowerCase()))
					{
						changeColorFont(item);
						invalidLibsCount++;
					}
					else
					{
						item.setForeground(null);
						item.setFont(null);
					}
				}
			}
		}
		
	}
	public void modifyText(ModifyEvent e) {
		if(e.widget == toolChain_dropdown)
		{
			int index = toolChain_dropdown.getSelectionIndex();
			if(index != -1)
				selectedToolChain = toolChains.elementAt(index);
		}
		this.getContainer().updateButtons();
		
	}

	private void changeColorFont(TableItem item)
	{
		item.setForeground(invalidColor);
		item.setFont(invalidFile_Font);
	}
	public boolean canFinish()
	{
		return this.canFlipToNextPage();
	}
	public void UpdatePage() {
		this.getContainer().updateButtons();
	}
	
	public String[] getSelectedDSOPaths()
	{
		ArrayList<String> paths = new ArrayList<String>();
		for(TableItem item:userDsoPath_list.getItems())
			if(!item.getForeground().equals(invalidColor))
				paths.add(item.getText(0));
		
		return paths.toArray(new String[0]);
	}
	
	public String[] getSelectedDLLPaths()
	{
		ArrayList<String> paths = new ArrayList<String>();
		for(TableItem item:userDllPath_list.getItems())
			if(!item.getForeground().equals(invalidColor))
				paths.add(item.getText(0));
		
		return paths.toArray(new String[0]);
	}

	public void UpdateFilesList() {
	}
	public void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(userDirs_radioBtn,
				HelpContextIDs.LIBRARIES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(userDsoPath_list,
				HelpContextIDs.LIBRARIES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dsoFiles_list,
				HelpContextIDs.LIBRARIES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(analyseSelected_radioBtn,
				HelpContextIDs.LIBRARIES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buildtarget_radioBtn,
				HelpContextIDs.LIBRARIES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buildTarget_list,
				HelpContextIDs.LIBRARIES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(toolChain_dropdown,
				HelpContextIDs.LIBRARIES_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(analyseAllFiles_radioBtn,
				HelpContextIDs.LIBRARIES_PAGE);
	}

}
