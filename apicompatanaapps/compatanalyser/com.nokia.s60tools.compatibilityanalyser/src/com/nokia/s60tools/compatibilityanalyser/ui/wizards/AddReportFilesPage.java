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

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.LastUsedKnownissues;
import com.nokia.s60tools.compatibilityanalyser.data.SavingUserData;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.dialogs.KnownissuesDilaog;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;

import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

/**
 * Wizard page for Report Filter wizard 
 */
 
public class AddReportFilesPage extends S60ToolsWizardPage implements
		SelectionListener , ModifyListener {

	private Button issuesBtn;
	private Button browseBtn;
	private Button removeBtn;
	private Button selectAllBtn;
	private Button addBtn;
	public Combo dirCmb;
	public List list;
	private Label label3;
	String[] files = null;
	
	public AddReportFilesPage(String[] files) {
		super(Messages.getString("AddReportFilesPage.ReportFilterWizard")); //$NON-NLS-1$
		this.files=files;
		setTitle(Messages.getString("AddReportFilesPage.ReportFiles")); //$NON-NLS-1$
		setDescription(Messages.getString("AddReportFilesPage.Descreption")); //$NON-NLS-1$
		setPageComplete(true);
	}
	@Override
	public void recalculateButtonStates() {

	}

	@Override
	public void setInitialFocus() {
		list.setFocus();
	}

	public void widgetDefaultSelected(SelectionEvent e) {

	}

	public void widgetSelected(SelectionEvent e) {
		if(e.widget==issuesBtn)
		{
			
			Runnable showDialogRunnable = new Runnable(){
				public void run(){
					IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
					String serverUrl = prefStore.getString(CompatibilityAnalyserPreferencesConstants.KNOWNISSUES_URL);
					KnownissuesDilaog dlg=new KnownissuesDilaog(Display.getCurrent().getActiveShell(),serverUrl);
					dlg.open();
				}
			};
			Display.getDefault().syncExec(showDialogRunnable);
			
			showTextKnownissuesLabel();
			this.getContainer().updateButtons();
		}
		else if(e.widget==browseBtn)
		{
			DirectoryDialog dirDlg=new DirectoryDialog(Display.getCurrent().getActiveShell());
			String dirName=null;
			if((dirName=dirDlg.open())!=null)
			{
				dirCmb.setText(dirName);
			}
			else
				return;
			
			this.getContainer().updateButtons();
		}
		else if(e.widget==addBtn)
		{
			FileDialog fileSelector = new FileDialog(this.getShell(), SWT.MULTI);
			fileSelector.setText("Add files"); //$NON-NLS-1$
			String[] filterExt = { "*.xml","*.*"};  //$NON-NLS-1$ //$NON-NLS-2$
			fileSelector.setFilterExtensions(filterExt);
			fileSelector.open();
			
			String [] files = fileSelector.getFileNames();
			String filterPath = fileSelector.getFilterPath();
			
			for(String s: files)
			{
				if(!filterPath.endsWith(File.separator))
				   filterPath+=File.separator;
				
				list.add(filterPath+s);
			}
			list.select(0);
			this.getContainer().updateButtons();
		}
		else if(e.widget==removeBtn)
		{
			list.remove(list.getSelectionIndices());
			this.getContainer().updateButtons();
		}
		else if(e.widget==selectAllBtn)
		{
			list.removeAll();
			this.getContainer().updateButtons();
		}
	}

	/**
	 * Constructs the page
	 */
	public void createControl(Composite parent) {
		
		Composite composite=new Composite(parent,SWT.NONE);
		
		GridLayout layout=new GridLayout(6,false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lbl1=new Label(composite,SWT.NONE);
		lbl1.setText(Messages.getString("AddReportFilesPage.ListofReportFiles")); //$NON-NLS-1$
		
		GridData data1=new GridData(GridData.FILL_HORIZONTAL);
		data1.horizontalSpan=6;
		lbl1.setLayoutData(data1);
		
		list = new List(composite,SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
		list.setToolTipText(Messages.getString("AddReportFilesPage.AddReportFiles")); //$NON-NLS-1$
		GridData data2=new GridData(GridData.FILL_HORIZONTAL);
		data2.horizontalSpan=6;
		data2.heightHint=200;
		list.setLayoutData(data2);
		
		if(this.files!=null)
		{
			list.setItems(files);
		}
		GridData data3=new GridData(GridData.FILL_HORIZONTAL);
		data3.verticalIndent=10;
		data3.horizontalSpan=2;
		addBtn = new Button(composite,SWT.PUSH);
		addBtn.setText(Messages.getString("AddReportFilesPage.AddFiles")); //$NON-NLS-1$
		addBtn.setLayoutData(data3);
		addBtn.addSelectionListener(this);
		
		removeBtn = new Button(composite,SWT.PUSH);
		removeBtn.setText(Messages.getString("AddReportFilesPage.Remove")); //$NON-NLS-1$
		removeBtn.setLayoutData(data3);
		removeBtn.addSelectionListener(this);
		
		selectAllBtn = new Button(composite,SWT.PUSH);
		selectAllBtn.setText(Messages.getString("AddReportFilesPage.RemoveAll")); //$NON-NLS-1$
		selectAllBtn.setLayoutData(data3);
		selectAllBtn.addSelectionListener(this);
		
		
		Label lbl2=new Label(composite,SWT.NONE);
		GridData data4=new GridData(GridData.FILL);
		data4.verticalIndent=10;
		data4.horizontalSpan=6;
		lbl2.setText(Messages.getString("AddReportFilesPage.OutputFilesDirectory")); //$NON-NLS-1$
		lbl2.setLayoutData(data4);
		
		dirCmb = new Combo(composite,SWT.BORDER|SWT.DROP_DOWN);
		GridData data5=new GridData(GridData.FILL_HORIZONTAL);
		data5.horizontalSpan=5;
		dirCmb.setLayoutData(data5);
		
		SavingUserData data = new SavingUserData();
		String[] lastUsedDirs = data.getPreviousValues(SavingUserData.ValueTypes.REPORT_PATH);
		
		if (lastUsedDirs != null) {
			dirCmb.setItems(lastUsedDirs);
			dirCmb.select(0);
		 }
		else
		{
			IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
			if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_TOOLS))
			{
				dirCmb.setText("C:\\temp"); //$NON-NLS-1$
			}
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS))
			{
				dirCmb.setText(prefStore.getString(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH) + File.separator + Messages.getString("AddReportFilesPage.reports")); //$NON-NLS-1$
			}
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS))
			{
				dirCmb.setText(prefStore.getString(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS_PATH) + File.separator + Messages.getString("AddReportFilesPage.reports")); //$NON-NLS-1$
			}
			else
				dirCmb.setText("C:\\temp"); //$NON-NLS-1$
		}
		dirCmb.addModifyListener(this);
		
		browseBtn = new Button(composite,SWT.PUSH);
		GridData data8=new GridData(GridData.FILL_HORIZONTAL);
		browseBtn.setText(Messages.getString("AddReportFilesPage.Browse")); //$NON-NLS-1$
		browseBtn.setLayoutData(data8);
		browseBtn.addSelectionListener(this);
		
		Group grp=new Group(composite,SWT.NONE);
		grp.setLayout(new GridLayout(2,false));
		grp.setText(Messages.getString("AddReportFilesPage.Knownissues")); //$NON-NLS-1$
		GridData data9=new GridData(GridData.FILL_HORIZONTAL);
		data9.horizontalSpan=6;
		
		grp.setLayoutData(data9);
		
		label3 = new Label(grp,SWT.WRAP|SWT.VERTICAL);
		GridData data6=new GridData(GridData.FILL_HORIZONTAL);
		data6.horizontalSpan=2;
		showTextKnownissuesLabel();
		label3.setLayoutData(data6);
		
		Label lbl5=new Label(grp,SWT.WRAP|SWT.SHADOW_OUT);	
		lbl5.setText(""); //$NON-NLS-1$
		
		issuesBtn = new Button(grp,SWT.PUSH);
		issuesBtn.setText(Messages.getString("AddReportFilesPage.ConfigureKnownissuesFile")); //$NON-NLS-1$
		issuesBtn.setToolTipText(Messages.getString("AddReportFilesPage.Tooltip_OPensKnownissuesDialog")); //$NON-NLS-1$
		issuesBtn.addSelectionListener(this);
		
		setHelp();
		setControl(composite);
		
	}

	/**
	 * Shows the selected knowissues filename in the wizard.
	 *
	 */
	private void showTextKnownissuesLabel()
	{
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_ISSUES))
		{
			label3.setText(Messages.getString("AddReportFilesPage.DefaultIssuesWillbeused")); //$NON-NLS-1$
		}
		
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.LATEST_ISSUES))
		{
			label3.setText(Messages.getString("AddReportFilesPage.LatestWillbeUsed")); //$NON-NLS-1$
		}
		
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES))
		{
			label3.setText(Messages.getString("AddReportFilesPage.SelectedIssuesWillBeUsed")); //$NON-NLS-1$
		}
		
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_ISSUES))
		{
			label3.setText(Messages.getString("AddReportFilesPage.SelectedFromLocal")); //$NON-NLS-1$
		}
		
	}
	/**
	 * Checkes the status of the page, whether can be finished or not.
	 * @return boolean 
	 */
	public boolean canFinish()
	{
		boolean value=true;
		setErrorMessage(null);
		removeBtn.setEnabled(list.getItemCount()>0);
		selectAllBtn.setEnabled(list.getItemCount()>0);
		if(list.getItemCount()==0)
		{
			value=false;
			setErrorMessage(Messages.getString("AddReportFilesPage.Error_AddMinOneReport")); //$NON-NLS-1$
		}
		if(dirCmb.getText().length()==0)
		{
			value=false;
			setErrorMessage(Messages.getString("AddReportFilesPage.SelectOutPutDir")); //$NON-NLS-1$
		}
		
		File dir=new File(dirCmb.getText());
		if(!dir.isDirectory())
		{
			value=false;
			setErrorMessage(Messages.getString("AddReportFilesPage.InvalidDirectory")); //$NON-NLS-1$
		}
		 
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		String urlInPref=store.getString(CompatibilityAnalyserPreferencesConstants.KNOWNISSUES_URL);
		
		LastUsedKnownissues data=new LastUsedKnownissues();
		String[] urlInDialog=data.getPreviousValues(LastUsedKnownissues.ValueTypes.WEBSERVER_MAIN_URL);
		if(urlInPref!=null && urlInDialog!=null && store.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES))
		{
			if(!urlInPref.endsWith("/")) //$NON-NLS-1$
				urlInPref=urlInPref+"/"; //$NON-NLS-1$
			if(!urlInDialog[0].endsWith("/")) //$NON-NLS-1$
				urlInDialog[0]=urlInPref+"/"; //$NON-NLS-1$
			
			if(!urlInPref.equalsIgnoreCase(urlInDialog[0]))
			{
				setMessage(Messages.getString("AddReportFilesPage.URLmismatch"),DialogPage.WARNING); //$NON-NLS-1$
			}
			else
				setMessage(null);
		}
		
		return value;
	}
	public void modifyText(ModifyEvent e) {
		if(e.widget==dirCmb)
		{
			this.getContainer().updateButtons();
		}
	}
	/**
	 * Sets context sensitive help for this page
	 *
	 */
	private void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(issuesBtn,
				HelpContextIDs.REPORTFILTER_WIZARD);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dirCmb,
				HelpContextIDs.REPORTFILTER_WIZARD);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(list,
				HelpContextIDs.REPORTFILTER_WIZARD);
	}
}
