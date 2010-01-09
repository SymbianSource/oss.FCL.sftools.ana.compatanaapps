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
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Event;
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
import com.nokia.s60tools.compatibilityanalyser.data.LastUsedKnownissues;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.dialogs.AdvancedOptionsDialog;
import com.nokia.s60tools.compatibilityanalyser.ui.dialogs.RenameDialog;
import com.nokia.s60tools.compatibilityanalyser.ui.dialogs.ShowFilesListDialog;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

/**
 * Header selection page.
 * 
 */
public class HeaderFilesPage extends S60ToolsWizardPage implements SelectionListener, ModifyListener, FeedbackHandler {

	private static boolean prevFilesSelection = false;
	
	private Composite composite;
	private Button rootDir;
	public Table currentHdrs_list;
	private Button browse;
	private Button filesSelection;
	
	private Button addFiles;
	private Button remove;
	private Button selectAll;
	private Button replace;
	private Button removeNonExisting;
	private Button selectedTypes;
	private int lastSearchIndex = -1;
	private TreeMap<String, String> replaceFileSet = new TreeMap<String, String>();
	private TreeMap<String, String> headersFileSet = new TreeMap<String, String>();

	private ArrayList<String> duplicates = new ArrayList<String>();
	private ArrayList<String> fileNamesList;
	private ArrayList<String> displayFiles;
	private ArrayList<String> children;
	private ArrayList<String> subChildren;

	private String absolutePath;
	private ShowFilesListDialog addDialog;

	private Button removeBtn_hdrGrp;
	private Button removeAllBtn_hdrGrp;
	private RenameDialog dialog;

	private int invalidHeadersCount = 0;
	private Group hdr_Grp;
	private Group fileTypes_Grp;
	private Label table_purpose_label;
	private Font invalidFile_Font=new Font(Display.getCurrent(),"Tahoma",8,SWT.ITALIC);	
	
	private Composite header_dir_options_comp;
	private Composite headers_files_options_comp;
	private Composite show_hide_button_comp;
	private Button show_btn;
	private Button hide_btn;
	

	private Button advanced_options_button;
	private ProgressMonitorDialog progDlg;
	protected String[] allHdrPaths;
	private boolean  isMonitorCancelled;
	
	ProductSdkData currentSdkData;
	
	Button allFiles;
	Button hType;
	Button hrhType;
	Button rsgType;
	Button mbgType;
	Button hppType;
	Button panType;
	
	Button useRecursive;
    Button defaultRootDir;
	Button allTypes;
	Table filesList;
	
	Color invalidColor = new Color(Display.getDefault(), 255,0,0);
	ArrayList<String> replaceSet = new ArrayList<String>();
	IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();

	public HeaderFilesPage(CompatibilityAnalyserEngine engine)
	{
		super(Messages.getString("HeaderFilesPage.WizardWindowTitle"));
		setTitle(Messages.getString("HeaderFilesPage.PageTitle"));
		setDescription(Messages.getString("HeaderFilesPage.PageDescreption"));
		setPageComplete(false);
		currentSdkData = engine.getCurrentSdkData();
	}

	public void recalculateButtonStates() {
	}

	@Override
	public void setInitialFocus() {
		composite.setFocus();
	}

	public void createControl(Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1,false);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH|GridData.GRAB_VERTICAL));
		composite.setLayout(gl);

		createHeaderDirectoryGroup();
		createHeaderFilesSelectionGroup();
		
		fileTypes_Grp.setVisible(true);

		Composite advanced_comp = new Composite(composite, SWT.NONE);
		advanced_comp.setLayout(new GridLayout(2,false));
		advanced_comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label advanced_options_label = new Label(advanced_comp, SWT.NONE);
		advanced_options_label.setText("");//Description of the button
		advanced_options_label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		advanced_options_button = new Button(advanced_comp, SWT.PUSH);
		advanced_options_button.setText("Advanced options...");
		advanced_options_button.addSelectionListener(this);
		
		setHelp();
		setControl(composite);
		if(currentSdkData.isOpenedFromProject)
			useRecursive.setSelection(true);

		composite.getParent().layout();
	
		prevFilesSelection = filesSelection.getSelection();
	}

	private void createHeaderDirectoryGroup()
	{
		Group grp = new Group(composite, SWT.NONE);

		GridLayout gl = new GridLayout(1, true);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

		grp.setLayout(gl);
		grp.setLayoutData(gridData);
		grp.setText(Messages.getString("HeaderFilesPage.GrpName_RootDir")); //$NON-NLS-1$

		defaultRootDir = new Button(grp, SWT.RADIO);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		defaultRootDir.setText(Messages.getString("HeaderFilesPage.Name_DefaultRootDir")); //$NON-NLS-1$
		defaultRootDir.setToolTipText(Messages.getString("HeaderFilesPage.ToolTip_DefRootDir")); //$NON-NLS-1$
		defaultRootDir.setSelection(currentSdkData.defaultHeaderDir);
		defaultRootDir.setLayoutData(gridData);

		rootDir = new Button(grp, SWT.RADIO);
		rootDir.setText(Messages.getString("HeaderFilesPage.Name_RootDir")); //$NON-NLS-1$
		rootDir.setToolTipText(Messages.getString("HeaderFilesPage.Tooltip_RootDir")); //$NON-NLS-1$
		rootDir.setSelection(!currentSdkData.defaultHeaderDir);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		rootDir.setLayoutData(gridData);
		rootDir.addSelectionListener(this);

		header_dir_options_comp = new Composite(grp, SWT.NONE);
		header_dir_options_comp.setLayout(new GridLayout(2, false));
		header_dir_options_comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		currentHdrs_list = new Table(header_dir_options_comp, SWT.BORDER|SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL);
		currentHdrs_list.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.verticalSpan=3;
		gridData.heightHint=60;
		currentHdrs_list.setLayoutData(gridData);
		currentHdrs_list.setEnabled(rootDir.getSelection());
		
		TableColumn col = new TableColumn(currentHdrs_list, SWT.FULL_SELECTION);
		col.setText("Header paths");
		col.setWidth(300);
		
		if(!currentSdkData.defaultHeaderDir && currentSdkData.currentHeaderDir != null)
		{
			for(String path: currentSdkData.currentHeaderDir)
			{
				TableItem item = new TableItem(currentHdrs_list, SWT.NONE);
				item.setText(0, path);
				if(!(new File(path).exists()))
				{
					item.setForeground(invalidColor);
					item.setFont(invalidFile_Font);
				}
			}
			currentHdrs_list.select(0);
		}

		else
		{
			LastUsedKnownissues data = new LastUsedKnownissues();
			String[] lastUsedDirs = data.getPreviousValues(LastUsedKnownissues.ValueTypes.CURRENT_HEADERS);

			if (lastUsedDirs != null) {
				for(String path: lastUsedDirs)
				{
					TableItem item = new TableItem(currentHdrs_list, SWT.NONE);
					item.setText(0, path);
					if(!(new File(path).exists()))
					{
						item.setForeground(invalidColor);
						item.setFont(invalidFile_Font);
					}
				}
				currentHdrs_list.select(0);
			}
		}

		gridData = new GridData(GridData.VERTICAL_ALIGN_END);
		gridData.widthHint=100;

		browse = new Button(header_dir_options_comp, SWT.PUSH);
		browse.setText(Messages.getString("HeaderFilesPage.Browse")); //$NON-NLS-1$
		browse.setEnabled(rootDir.getSelection());
		browse.setLayoutData(gridData);
		browse.addSelectionListener(this); 	

		removeBtn_hdrGrp = new Button(header_dir_options_comp,SWT.PUSH);
		removeBtn_hdrGrp.setText("Remove");
		removeBtn_hdrGrp.setLayoutData(gridData);
		removeBtn_hdrGrp.setEnabled(false);
		removeBtn_hdrGrp.addSelectionListener(this);

		removeAllBtn_hdrGrp = new Button(header_dir_options_comp,SWT.PUSH);
		removeAllBtn_hdrGrp.setText("Remove all");
		removeAllBtn_hdrGrp.setLayoutData(gridData);
		removeAllBtn_hdrGrp.setEnabled(false);
		removeAllBtn_hdrGrp.addSelectionListener(this);
		
		GridData data = (GridData)header_dir_options_comp.getLayoutData();
		data.exclude = !rootDir.getSelection();
		header_dir_options_comp.setVisible(rootDir.getSelection());

	}
	
	private void createHeaderFilesSelectionGroup()
	{
		hdr_Grp = new Group(composite, SWT.NONE);
		GridLayout gl = new GridLayout(2,false);
		GridData header_files_griddata = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL);

		hdr_Grp.setLayout(gl);
		hdr_Grp.setLayoutData(header_files_griddata);
		hdr_Grp.setVisible(true);
		hdr_Grp.setText(Messages.getString("HeaderFilesPage.Grp_name_SelectionOfHdrs")); //$NON-NLS-1$

		allFiles = new Button(hdr_Grp, SWT.RADIO);
		header_files_griddata = new GridData(GridData.FILL_HORIZONTAL);
		header_files_griddata.horizontalSpan = 2;
		allFiles.setText(Messages.getString("HeaderFilesPage.Name_AllFile")); //$NON-NLS-1$
		allFiles.setToolTipText(Messages.getString("HeaderFilesPage.ToolTip_Allfiles")); //$NON-NLS-1$
		allFiles.setSelection(currentSdkData.analyseAll);
		allFiles.setLayoutData(header_files_griddata);
		allFiles.addSelectionListener(this);

		filesSelection = new Button(hdr_Grp, SWT.RADIO);
		filesSelection.setText(Messages.getString("HeaderFilesPage.Name_FileSelection")); //$NON-NLS-1$
		filesSelection.setToolTipText(Messages.getString("HeaderFilesPage.Tooltip_FileSelection")); //$NON-NLS-1$
		filesSelection.setSelection(!currentSdkData.analyseAll);
		header_files_griddata = new GridData(GridData.FILL_HORIZONTAL);
		header_files_griddata.horizontalSpan = 2;
		filesSelection.setLayoutData(header_files_griddata);
		filesSelection.addSelectionListener(this);

		Label show_hide_lbl = new Label(hdr_Grp, SWT.NONE);
		show_hide_lbl.setText("Show/Hide options");
		show_hide_lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.HORIZONTAL_ALIGN_END));
		
		show_hide_button_comp = new Composite(hdr_Grp, SWT.NONE);
		show_hide_button_comp.setLayout(new StackLayout());
		header_files_griddata = new GridData(GridData.FILL);
		header_files_griddata.widthHint = 20;
		show_hide_button_comp.setLayoutData(header_files_griddata);
		
		show_btn = new Button(show_hide_button_comp, SWT.ARROW|SWT.DOWN);
		show_btn.addSelectionListener(this);
		
		hide_btn = new Button(show_hide_button_comp, SWT.ARROW|SWT.UP);
		hide_btn.addSelectionListener(this);
		
		((StackLayout)show_hide_button_comp.getLayout()).topControl = hide_btn;
		show_hide_button_comp.layout();
		
		headers_files_options_comp = new Composite(hdr_Grp, SWT.NONE);
		headers_files_options_comp.setLayout(new GridLayout(2, false));
		header_files_griddata = new GridData(GridData.FILL_HORIZONTAL);
		header_files_griddata.horizontalSpan = 2;
		headers_files_options_comp.setLayoutData(header_files_griddata);
		
		table_purpose_label = new Label(headers_files_options_comp,SWT.NONE);
		table_purpose_label.setLayoutData(header_files_griddata);

		filesList = new Table(headers_files_options_comp,SWT.MULTI|SWT.BORDER| SWT.FULL_SELECTION);
		filesList.setHeaderVisible(true);
		header_files_griddata = new GridData(GridData.FILL_BOTH|GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL);
		header_files_griddata.verticalSpan = 5;
		header_files_griddata.heightHint=100;
		filesList.setLayoutData(header_files_griddata);

		TableColumn col1=new TableColumn(filesList,SWT.LEFT|SWT.FILL);
		col1.setText(Messages.getString("HeaderFilesPage.Column1_Title")); //$NON-NLS-1$
		col1.setWidth(320);
		TableColumn col2=new TableColumn(filesList,SWT.LEFT);
		col2.setText(Messages.getString("HeaderFilesPage.Column2_Title")); //$NON-NLS-1$
		col2.setWidth(80);

		if(!currentSdkData.analyseAll && currentSdkData.HeaderFilesList != null)
		{
			for(String s:currentSdkData.HeaderFilesList)
			{
				TableItem item = new TableItem(filesList, SWT.NONE);
				item.setText(new String[] {s, ""});
			}
		}

		if(currentSdkData.replaceSet.size() >0)
		{
			if(filesList.getItemCount() >0)
			{
				for(int i=0; i<currentSdkData.replaceSet.size(); i++)
				{
					String replaceStr = currentSdkData.replaceSet.get(i);

					String currentFile = replaceStr.substring(replaceStr.indexOf(":")+1);
					String baseFile = replaceStr.substring(0, replaceStr.indexOf(":"));

					for(int j=0; j<filesList.getItemCount(); j++)
					{		
						if(filesList.getItem(j).getText(0).equals(currentFile))						
						{
							filesList.getItem(j).setText(1, baseFile);
							break;
						}
					}
				}
			}
			else
			{
				for(int i=0; i<currentSdkData.replaceSet.size(); i++)
				{
					String replaceStr = currentSdkData.replaceSet.get(i);

					String baselineFile = replaceStr.substring(0, replaceStr.indexOf(":"));
					String currentFile = replaceStr.substring(replaceStr.indexOf(":") + 1);

					TableItem item = new TableItem(filesList, SWT.NONE);
					item.setText(new String [] {currentFile, baselineFile});
				}
			}
		}
		
		addFiles = new Button(headers_files_options_comp, SWT.PUSH);
		header_files_griddata = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addFiles.setText(Messages.getString("HeaderFilesPage.Name_AddFiles"));
		addFiles.setToolTipText(Messages.getString("HeaderFilesPage.ToolTip_addfiles"));
		addFiles.setLayoutData(header_files_griddata);
		addFiles.setVisible(false);
		addFiles.addSelectionListener(this);		

		replace = new Button(headers_files_options_comp, SWT.PUSH);
		header_files_griddata = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		replace.setText(Messages.getString("HeaderFilesPage.ReplaceFile"));
		replace.setToolTipText(Messages.getString("HeaderFilesPage.ToolTip_replaceBtn"));
		replace.setLayoutData(header_files_griddata);
		replace.addSelectionListener(this);

		remove = new Button(headers_files_options_comp, SWT.PUSH);
		header_files_griddata = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove.setText(Messages.getString("HeaderFilesPage.Remove")); //$NON-NLS-1$
		remove.setLayoutData(header_files_griddata);
		remove.addSelectionListener(this);

		selectAll = new Button(headers_files_options_comp, SWT.PUSH);
		header_files_griddata = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		selectAll.setText(Messages.getString("HeaderFilesPage.RemoveAll")); //$NON-NLS-1$
		selectAll.setLayoutData(header_files_griddata);
		selectAll.addSelectionListener(this);

		removeNonExisting = new Button(headers_files_options_comp, SWT.PUSH);
		header_files_griddata = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		removeNonExisting.setText("Remove non-existing"); //$NON-NLS-1$
		removeNonExisting.setLayoutData(header_files_griddata);
		removeNonExisting.setVisible(false);
		removeNonExisting.setEnabled(false);
		removeNonExisting.addSelectionListener(this);

		useRecursive = new Button(headers_files_options_comp, SWT.CHECK);
		useRecursive.setText(Messages.getString("HeaderFilesPage.UseRecursion")); //$NON-NLS-1$
		useRecursive.setToolTipText(Messages.getString("HeaderFilesPage.Tooltip_userecursion")); //$NON-NLS-1$
		useRecursive.setEnabled(true);
		header_files_griddata = new GridData(GridData.FILL_HORIZONTAL);
		header_files_griddata.horizontalSpan=2;
		header_files_griddata.verticalIndent=5;
		useRecursive.setLayoutData(header_files_griddata);
		useRecursive.setSelection(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.USERECURSION_LAST_SELECTION));
		useRecursive.addSelectionListener(this);
		
		createFileTypesGroup();
	}
	
	private void createFileTypesGroup()
	{
		fileTypes_Grp = new Group(headers_files_options_comp, SWT.NONE);
		GridLayout gl = new GridLayout(1,false);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL);
		gd.horizontalSpan=2;
		fileTypes_Grp.setLayout(gl);
		fileTypes_Grp.setLayoutData(gd);
		fileTypes_Grp.setVisible(true);
		fileTypes_Grp.setText(Messages.getString("HeaderFilesPage.GrpName_SelectionofFileTypes")); //$NON-NLS-1$

		allTypes = new Button(fileTypes_Grp, SWT.RADIO);
		allTypes.setText(Messages.getString("HeaderFilesPage.Name_AllFileType")); //$NON-NLS-1$
		allTypes.setToolTipText(Messages.getString("HeaderFilesPage.ToolTip_AllTypes")); //$NON-NLS-1$
		allTypes.setSelection(currentSdkData.allTypes);

		selectedTypes = new Button(fileTypes_Grp, SWT.RADIO);
		selectedTypes.setText(Messages.getString("HeaderFilesPage.Name_SelectedTypes")); //$NON-NLS-1$
		selectedTypes.setToolTipText(Messages.getString("HeaderFilesPage.ToolTip_SelectedTypes")); //$NON-NLS-1$
		selectedTypes.setSelection(!currentSdkData.allTypes);
		selectedTypes.addSelectionListener(this);

		Composite typesGrp = new Composite(fileTypes_Grp, SWT.NONE);
		GridLayout typesGL = new GridLayout();
		typesGL.numColumns = 6;
		gd = new GridData(GridData.FILL_HORIZONTAL);

		typesGrp.setLayout(typesGL);
		typesGrp.setLayoutData(gd);
		typesGrp.setVisible(true);

		hType = new Button(typesGrp, SWT.CHECK);
		hType.setText(Messages.getString("HeaderFilesPage.30")); //$NON-NLS-1$
		hType.setToolTipText(Messages.getString("HeaderFilesPage.HeaderFile")); //$NON-NLS-1$
		hType.setEnabled(!currentSdkData.allTypes);
		hType.setSelection(currentSdkData.hTypes);
		hType.addSelectionListener(this);

		hrhType = new Button(typesGrp, SWT.CHECK);
		hrhType.setText(Messages.getString("HeaderFilesPage.32")); //$NON-NLS-1$
		hrhType.setEnabled(!currentSdkData.allTypes);
		hrhType.setSelection(currentSdkData.hrhTypes);
		hrhType.addSelectionListener(this);

		rsgType = new Button(typesGrp, SWT.CHECK);
		rsgType.setText(Messages.getString("HeaderFilesPage.33")); //$NON-NLS-1$
		rsgType.setEnabled(!currentSdkData.allTypes);
		rsgType.setSelection(currentSdkData.rsgTypes);
		rsgType.addSelectionListener(this);

		mbgType = new Button(typesGrp, SWT.CHECK);
		mbgType.setText(Messages.getString("HeaderFilesPage.34")); //$NON-NLS-1$
		mbgType.setEnabled(!currentSdkData.allTypes);
		mbgType.setSelection(currentSdkData.mbgTypes);
		mbgType.addSelectionListener(this);
		
		hppType = new Button(typesGrp, SWT.CHECK);
		hppType.setText(Messages.getString("HeaderFilesPage.35")); //$NON-NLS-1$
		hppType.setEnabled(!currentSdkData.allTypes);
		hppType.setSelection(currentSdkData.hppTypes);
		hppType.addSelectionListener(this);
		
		panType = new Button(typesGrp, SWT.CHECK);
		panType.setText(Messages.getString("HeaderFilesPage.36")); //$NON-NLS-1$
		panType.setEnabled(!currentSdkData.allTypes);
		panType.setSelection(currentSdkData.panTypes);
		panType.addSelectionListener(this);
		
	}
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if(e.widget == rootDir)
		{
			GridData data = (GridData)header_dir_options_comp.getLayoutData();
			data.exclude = !rootDir.getSelection();
			header_dir_options_comp.setVisible(rootDir.getSelection());
			header_dir_options_comp.getParent().getParent().layout();
			
			currentHdrs_list.setEnabled(rootDir.getSelection());
			browse.setEnabled(rootDir.getSelection());
			validateFileSet();
			this.getContainer().updateButtons();
		}	
		else if(e.widget == browse)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
			ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this);
			dirDialog.setFilterPath(productPage.epocDirectory.getText());
			String newDir = dirDialog.open();
			if(newDir != null)
			{
				TableItem item = new TableItem(currentHdrs_list, SWT.NONE);
				item.setText(newDir);
				currentHdrs_list.select(0);
			}
			validateFileSet();
			this.getContainer().updateButtons();
		}
		else if(e.widget==removeBtn_hdrGrp)
		{
			currentHdrs_list.remove(currentHdrs_list.getSelectionIndices());
			currentHdrs_list.select(0);
			validateFileSet();
			this.getContainer().updateButtons();
		}
		else if(e.widget==removeAllBtn_hdrGrp)
		{
			currentHdrs_list.removeAll();
			validateFileSet();
			this.getContainer().updateButtons();
		}
		else if(e.widget == filesSelection)
		{
			if(filesSelection.getSelection() && !prevFilesSelection)
			{
				show_btn.notifyListeners(SWT.Selection, new Event());
				
				replaceFileSet.clear();
				for(TableItem item:filesList.getItems())
				{
					replaceFileSet.put(item.getText(0), item.getText(1));
				}
				filesList.removeAll();
				duplicates.clear();
				if(headersFileSet.size()>0)
				{
					String [] keys = headersFileSet.keySet().toArray(new String[0]);
					for(String s:keys)
					{
						String value = headersFileSet.get(s);
						TableItem item = new TableItem(filesList, SWT.NONE);
						item.setText(new String []{s,value});
						if(doesBaseOccursManyTimes(value) && !duplicates.contains(value))
						{
							duplicates.add(value);
						}
					}
				}
				prevFilesSelection = true;
			}
			else if(!filesSelection.getSelection() && prevFilesSelection)
			{
				headersFileSet.clear();
				for(TableItem item:filesList.getItems())
				{
					headersFileSet.put(item.getText(0), item.getText(1));
				}
				filesList.removeAll();
				duplicates.clear();
				if(replaceFileSet.size()>0)
				{
					String [] keys = replaceFileSet.keySet().toArray(new String[0]);
					for(String s:keys)
					{
						String value = replaceFileSet.get(s);
						TableItem item = new TableItem(filesList, SWT.NONE);
						item.setText(new String[]{s, value});
						if(doesBaseOccursManyTimes(value) && !duplicates.contains(value))
						{
							duplicates.add(value);
						}
					}
				}
				prevFilesSelection = false;
			}
			replace.setEnabled(filesSelection.getSelection());
			//Disabling the controls
			fileTypes_Grp.setVisible(!filesSelection.getSelection());
			useRecursive.setVisible(!filesSelection.getSelection());
			addFiles.setVisible(filesSelection.getSelection());
			removeNonExisting.setVisible(filesSelection.getSelection());
			validateFileSet();
			this.getContainer().updateButtons();
		}
		else if(e.widget == selectedTypes)
		{
			hrhType.setEnabled(selectedTypes.getSelection());
			hType.setEnabled(selectedTypes.getSelection());
			rsgType.setEnabled(selectedTypes.getSelection());
			mbgType.setEnabled(selectedTypes.getSelection());
			hppType.setEnabled(selectedTypes.getSelection());
			panType.setEnabled(selectedTypes.getSelection());
			
			this.getContainer().updateButtons();
		}
		else if(e.widget == addFiles)
		{
			fileNamesList = new ArrayList<String>();
			displayFiles = new ArrayList<String>();
			children = new ArrayList<String>();
			subChildren = new ArrayList<String>();
			isMonitorCancelled = false;

			if(rootDir.getSelection() && currentHdrs_list.getItemCount() >0)
			{
				if(currentHdrs_list.getItemCount() == 1)
				{
					absolutePath = new String(currentHdrs_list.getItem(0).getText(0));
					absolutePath = FileMethods.convertForwardToBackwardSlashes(absolutePath);
					addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), filesList, this, absolutePath, true);

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
					addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), filesList, this, "", true);

					String[] paths = new String[currentHdrs_list.getItems().length];
					for(int i =0 ; i < currentHdrs_list.getItems().length; i++)
						paths[i] =  currentHdrs_list.getItem(i).getText(0);
					allHdrPaths = paths;

					IRunnableWithProgress op = new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) {
							try {
								int i=1;
								for(String path: allHdrPaths)
								{
									absolutePath = FileMethods.convertForwardToBackwardSlashes(path);
									monitor.beginTask("Getting files from "+ absolutePath, allHdrPaths.length);
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
			}
			else
			{
				ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this);
				absolutePath = new String(FileMethods.appendPathSeparator(productPage.epocDirectory.getText()) + Messages.getString("HeaderFilesPage.epoc32Include")); //$NON-NLS-1$
				absolutePath = FileMethods.convertForwardToBackwardSlashes(absolutePath);
				addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), filesList, this, absolutePath, true);

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
				for(String name : fileNamesList){
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
				
				if(displayFiles.size() == 0 && fileNamesList.size() !=0)
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
				else{
					addDialog.open();
					addDialog.filesList.setItems(displayFiles.toArray(new String[displayFiles.size()]));
					addDialog.filesList.select(0);
					addDialog.fileNamesList = addDialog.filesList.getItems();
				}
							
			}
		
			this.getContainer().updateButtons();
		}
		else if(e.widget == remove)
		{			
			filesList.remove(filesList.getSelectionIndices());
			filesList.select(0);
			validateFileSet();
			validateBaseFiles();
			this.getContainer().updateButtons();
		}
		else if(e.widget == replace)
		{	
			if(filesList.getItemCount() >0 && filesList.getSelectionIndex() != -1)
			{
				String currentFile = filesList.getItem(filesList.getSelectionIndices()[0]).getText(0);
				String baseFile = filesList.getItem(filesList.getSelectionIndices()[0]).getText(1);
				dialog = new RenameDialog(Display.getCurrent().getActiveShell(), SWT.OPEN, currentFile, baseFile, this);
			}
			else
				dialog = new RenameDialog(Display.getCurrent().getActiveShell(), SWT.OPEN, null, null,this);

			ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this);

			if(defaultRootDir.getSelection())
			{
				String epocPath = new String(FileMethods.appendPathSeparator(productPage.epocDirectory.getText()) + Messages.getString("HeaderFilesPage.epoc32Include")); //$NON-NLS-1$
				epocPath = FileMethods.convertForwardToBackwardSlashes(epocPath);
				dialog.setCurrentFiles(new String []{epocPath});
			}
			else
			{
				String[] paths = new String[currentHdrs_list.getItems().length];
				for(int i =0 ; i < currentHdrs_list.getItems().length; i++)
					paths[i] =  currentHdrs_list.getItem(i).getText(0);
				dialog.setCurrentFiles(paths);
			}
			dialog.setBaselineProfile(productPage.profileCombo.getText());
			dialog.open();
		}
		else if(e.widget == removeNonExisting)
		{
			int [] invalidIndices = new int[invalidHeadersCount];
			int j = 0;

			for(int i=0; i<filesList.getItemCount();i++)
			{
				if(filesList.getItem(i).getForeground().equals(invalidColor))
				{
					invalidIndices[j] = i;
					j++;
				}
			}
			filesList.remove(invalidIndices);
			invalidHeadersCount = 0;
			this.getContainer().updateButtons();
		}
		else if(e.widget == selectAll)
		{
			filesList.removeAll();
			if(duplicates.size() >0)
				duplicates.clear();

			this.getContainer().updateButtons();
		}
		else if(e.widget == filesList)
		{
			this.getContainer().updateButtons();
		}				
		else if((e.widget == hType) ||(e.widget == hrhType) || (e.widget == rsgType)
				||(e.widget == mbgType) || (e.widget == hppType) || (e.widget == panType))
		{
			this.getContainer().updateButtons();
		}
		else if(e.widget == show_btn)
		{
			GridData data = (GridData)headers_files_options_comp.getLayoutData();
			data.exclude = false;
			headers_files_options_comp.setVisible(true);
			headers_files_options_comp.getParent().getParent().layout();
			
			((StackLayout)show_hide_button_comp.getLayout()).topControl = hide_btn;
			show_hide_button_comp.layout();
			headers_files_options_comp.setVisible(true);
		}
		else if(e.widget == hide_btn)
		{
			GridData data = (GridData)headers_files_options_comp.getLayoutData();
			data.exclude = true;
			headers_files_options_comp.setVisible(false);
			headers_files_options_comp.getParent().getParent().layout();
			
			((StackLayout)show_hide_button_comp.getLayout()).topControl = show_btn;
			show_hide_button_comp.layout();
			headers_files_options_comp.setVisible(false);			
		}
		else if(e.widget == advanced_options_button)
		{
			ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this);
			AdvancedOptionsDialog adv_opt = new AdvancedOptionsDialog(this.getShell(),currentSdkData,FileMethods.appendPathSeparator(productPage.epocDirectory.getText()));
			adv_opt.open();
		}
	}

	/**
	 * This method gets invoked when a header directory is added or removed.
	 * The method fetches header files from all provided Header directories and
	 * if files provided in Headers list do not exist in this list, those files 
	 * will be shown in Red color, meaning they will not be considered for analysis.
	 */
	private void validateFileSet()
	{
		invalidHeadersCount = 0;

		if(rootDir.getSelection())
		{
			if(currentHdrs_list.getItemCount() == 0)
			{
				if(filesSelection.getSelection() && filesList.getItemCount()>0)
				{
					for(TableItem item:filesList.getItems())
					{
						changeColorFont(item);
						invalidHeadersCount++;
					}
				}
			}
			else if(currentHdrs_list.getItemCount() >0)
			{
				if(filesSelection.getSelection() && filesList.getItemCount()>0)
				{
					fileNamesList = new ArrayList<String>();
					children = new ArrayList<String>();
					subChildren = new ArrayList<String>();
					
					for(TableItem s:currentHdrs_list.getItems())
					{
						getFiles(s.getText(0),null);
						getFilesFromSubDirs(s.getText(0),null);
					}

					for(TableItem item:filesList.getItems())
					{
						if(!fileNamesList.contains(item.getText(0).toLowerCase()))
						{
							changeColorFont(item);
							invalidHeadersCount++;
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
		else if(defaultRootDir.getSelection())
		{
			if(filesSelection.getSelection() && filesList.getItemCount()>0)
			{
				ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getPreviousPage(this);
				String epocPath = new String(FileMethods.appendPathSeparator(productPage.epocDirectory.getText()) + Messages.getString("HeaderFilesPage.epoc32Include")); //$NON-NLS-1$
				epocPath = FileMethods.convertForwardToBackwardSlashes(epocPath);

				fileNamesList = new ArrayList<String>();
				subChildren = new ArrayList<String>();
				children = new ArrayList<String>();
				
				getFiles(epocPath,null);
				getFilesFromSubDirs(epocPath,null);

				for(TableItem item:filesList.getItems())
				{
					if(!fileNamesList.contains(item.getText(0).toLowerCase()))
					{
						changeColorFont(item);
						invalidHeadersCount++;
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
	private void changeColorFont(TableItem item)
	{
		item.setForeground(invalidColor);
		item.setFont(invalidFile_Font);
	}
	public boolean canFlipToNextPage()
	{
		this.setErrorMessage(null);
		this.setMessage(null);

		if(filesSelection.getSelection())
		{
			fileTypes_Grp.setVisible(!filesSelection.getSelection());
			useRecursive.setVisible(!filesSelection.getSelection());
			table_purpose_label.setText("List of header files");
			addFiles.setVisible(filesSelection.getSelection());
			removeNonExisting.setVisible(filesSelection.getSelection());
			removeNonExisting.setEnabled(false);
			
			if(rootDir.getSelection() && currentHdrs_list.getItemCount()==0)
			{
				addFiles.setEnabled(false);
				replace.setEnabled(false);
			}
			else
			{				
				addFiles.setEnabled(true);
				replace.setEnabled(true);
			}
			if(filesList.getItemCount() == 0)
			{
				remove.setEnabled(false);
				selectAll.setEnabled(false);
				this.setErrorMessage(Messages.getString("HeaderFilesPage.NoHdrsSelected")); //$NON-NLS-1$
			}
			else
			{
				if(currentSdkData.isOpenedFromConfigFile)
					validateFileSet();
				if(invalidHeadersCount == filesList.getItemCount())
				{
					removeNonExisting.setEnabled(true);
					this.setErrorMessage("None of the selected Headers exist in the provided header directories."); //$NON-NLS-1$
				}
				else if(invalidHeadersCount >0)
				{
					removeNonExisting.setEnabled(true);
					this.setMessage("Files shown in Red color do not exist in any of the selected directories \n So they will not be analysed." , DialogPage.WARNING); //$NON-NLS-1$
				}
			}
		}
		else 
		{
			table_purpose_label.setText("Replace file set");
			if(rootDir.getSelection() && currentHdrs_list.getItemCount() == 0)
				replace.setEnabled(false);
			else
				replace.setEnabled(true);
		}
		if(filesList.getItemCount() == 0)
		{
			remove.setEnabled(false);
			selectAll.setEnabled(false);
		}
		else
		{
			remove.setEnabled(true);
			selectAll.setEnabled(true);
		}
		if(rootDir.getSelection())
		{
			if(currentHdrs_list.getItemCount() == 0)
			{
				removeBtn_hdrGrp.setEnabled(false);
				removeAllBtn_hdrGrp.setEnabled(false);
				this.setErrorMessage(Messages.getString("HeaderFilesPage.RootdirectoryCanNotBeNull")); //$NON-NLS-1$
			}
			else if(getSelectedHdrPaths().length == 0)
			{
				removeBtn_hdrGrp.setEnabled(true);
				removeAllBtn_hdrGrp.setEnabled(true);	
				this.setErrorMessage("Provided directories are invalid."); //$NON-NLS-1$
			}
			else if(currentHdrs_list.getItemCount() != getSelectedHdrPaths().length)
			{
				removeBtn_hdrGrp.setEnabled(true);
				removeAllBtn_hdrGrp.setEnabled(true);	
				this.setMessage("Paths shown in Red color do not exist. So they will not be analysed.", DialogPage.WARNING);
			}
			else
			{
				removeBtn_hdrGrp.setEnabled(true);
				removeAllBtn_hdrGrp.setEnabled(true);				
			}
		}
		else
		{
			removeBtn_hdrGrp.setEnabled(false);
			removeAllBtn_hdrGrp.setEnabled(false);
		}
			
		if(selectedTypes.isVisible() && selectedTypes.getSelection() && (!hType.getSelection()) && (!hrhType.getSelection())
				&& (!mbgType.getSelection()) && (!rsgType.getSelection()) && (!hppType.getSelection()) && (!panType.getSelection()))
		{
			this.setErrorMessage(Messages.getString("HeaderFilesPage.SelectHdrTypes")); //$NON-NLS-1$
		}
		
		if(duplicates.size() >0)
		{
			this.setMessage("Some Baseline files are replaced with more than one current headers.", DialogPage.WARNING); //$NON-NLS-1$
		}
		if(this.getErrorMessage() != null)
			return false;

		return true;
	}
	
	boolean canFinish()
	{
		return this.canFlipToNextPage();
	}
	
	public void modifyText(ModifyEvent e) {
		if(e.widget == currentHdrs_list)
		{
			this.getContainer().updateButtons();
		}

	}
	
	/**
	 * Get files from the given directory and from the sub directories.
	 * @param path to be scanned
	 * @param monitor to be updated with the status of scanning
	 */
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
					fileNamesList.clear();
					displayFiles.clear();
					children.clear();
					subChildren.clear();
					monitor.done();
					return;
				}

				if(!fileNamesList.contains(name.toLowerCase()))
					fileNamesList.add(name.toLowerCase());

				if(path.equalsIgnoreCase(absolutePath))
					children.add(name.toLowerCase());
				else
					subChildren.add(name.toLowerCase());
			}
		}
		return;
	}

	/**
	 * Check if the given file name already added in the list
	 * @param fileName
	 * @return bbolean 
	 */
	private boolean isPreviouslySelected(String fileName)
	{
		for(int i=0; i<filesList.getItemCount(); i++)
		{
			String s = filesList.getItem(i).getText(0);

			if(s.equalsIgnoreCase(fileName))
			{
				lastSearchIndex = i;
				return true;
			}
		}
		return false;
	}

	/**
	 * Checkes whether any baseline filename present many times in list.
	 * @param fileName 
	 * @return true if occures many times
	 */
	private boolean doesBaseOccursManyTimes(String fileName)
	{
		int occurance = 0;

		if(fileName.equals(""))
			return false;

		for(int i=0; i<filesList.getItemCount(); i++)
		{
			String s = filesList.getItem(i).getText(1);

			if(s.equalsIgnoreCase(fileName))
			{
				occurance++;
				if(occurance >1)
					break;
			}

		}
		if(occurance >1)
			return true;
		else
			return false;
	}
	
	/**
	 * Get supported header files from sub directories in given path. 
	 * @param path Path of the main directory
	 * @param monitor Progress monitor
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
						fileNamesList.clear();
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
	public void UpdatePage() {
		this.getContainer().updateButtons();
	}
	
	/**
	 * This method is from the interface FeedbackHandler
	 * This updates the list in this page based on the selection made
	 * in "Set Baseline name..." dialog
	 */
	public void UpdateFilesList() {
		String[] newName = dialog.getSelectedNames();

		if(isPreviouslySelected(newName[0]) && lastSearchIndex != -1)
		{
			TableItem item = filesList.getItem(lastSearchIndex);
			item.setText(newName);
		}
		else{
			TableItem item=new TableItem(filesList,SWT.NONE);
			item.setText(newName);
		}

		if(doesBaseOccursManyTimes(newName[1]) && !duplicates.contains(newName[1]))
		{
			duplicates.add(newName[1]);
		}
		replaceSet.add(newName[1]+":"+newName[0]);
		this.getContainer().updateButtons();
	}
	public void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
				HelpContextIDs.HEADERS_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(rootDir,
				HelpContextIDs.HEADERS_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(currentHdrs_list,
				HelpContextIDs.HEADERS_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(allFiles,
				HelpContextIDs.HEADERS_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(useRecursive,
				HelpContextIDs.HEADERS_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(defaultRootDir,
				HelpContextIDs.HEADERS_PAGE);


	}

	/**
	 * Validate and removes duplicate files from the header list.
	 *
	 */
	private void validateBaseFiles()
	{
		for(int i=0;i<duplicates.size();i++)
		{
			int occurance = 0;
			for(TableItem item:filesList.getItems())
			{
				if(duplicates.get(i).equalsIgnoreCase(item.getText(1)))
				{
					occurance++;

					if(occurance >1)
						break;
				}
			}

			if(occurance <=1)
				duplicates.remove(i);
		}
	}

	public String[] getSelectedHdrPaths()
	{
		ArrayList<String> paths = new ArrayList<String>();
		for(TableItem item:currentHdrs_list.getItems())
			if(!item.getForeground().equals(invalidColor))
				paths.add(item.getText(0));
		return paths.toArray(new String[0]);
	}
	
}
