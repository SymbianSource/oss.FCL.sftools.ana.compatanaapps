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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.LastUsedKnownissues;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.SavingUserData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.dialogs.KnownissuesDilaog;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

/**
 * Wizard page of Report filter wizard, wherein set of report files
 * and output path for filtered files can be provided. 
 *
 */
public class ReportFileInfoPage extends S60ToolsWizardPage implements SelectionListener, ModifyListener {

	private Composite composite;
	private Button configure;
	Text fileName;
	Combo pathCombo;
	private Button browse;
	Button yes;
	Button no;
	private Label label;
	boolean isHeaderSelected = false;
	boolean isLibrarySelected = false;

	IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
	
	private ProductSdkData currentSdk;
	
	private final String DEFAULT_REPORT_NAME = "CompatibilityReport";
	
	public ReportFileInfoPage(CompatibilityAnalyserEngine engine)
	{
		super(Messages.getString("ReportFileInfoPage.CompatibilityAnalyserWizard")); //$NON-NLS-1$
		setTitle(Messages.getString("ReportFileInfoPage.ReportFiles")); //$NON-NLS-1$
		
		setDescription(Messages.getString("ReportFileInfoPage.ProvideReportFileInformation.")); //$NON-NLS-1$
		
		currentSdk = engine.getCurrentSdkData();
		setPageComplete(false);
	}
	public void recalculateButtonStates() {
	}

	@Override
	public void setInitialFocus() {
		fileName.setFocus();
	}

	public void createControl(Composite parent) {
		
		composite = new Composite(parent, SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		composite.setLayout(gl);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		
		Label nameLabel = new Label(composite, SWT.LEFT);
		nameLabel.setText(Messages.getString("ReportFileInfoPage.ReportFileName")); //$NON-NLS-1$
		
		fileName = new Text(composite, SWT.BORDER);
		fileName.setToolTipText(Messages.getString("ReportFileInfoPage.CreateReportFileWithGivenName")); //$NON-NLS-1$
		GridData textGD = new GridData(GridData.FILL_HORIZONTAL);
		textGD.horizontalSpan = 2;
		fileName.setLayoutData(textGD);
		fileName.setEnabled(true);
				
		if(currentSdk.reportName != null)
			fileName.setText(currentSdk.reportName);
		else
		{
			if(currentSdk.isOpenedFromProject)
				fileName.setText(currentSdk.projectName + "_" + DEFAULT_REPORT_NAME); //$NON-NLS-1$
			else
				fileName.setText(DEFAULT_REPORT_NAME); //$NON-NLS-1$
		}
		
	    fileName.addModifyListener(this);
		Label pathLabel = new Label(composite, SWT.LEFT);
		
		pathLabel.setText(Messages.getString("ReportFileInfoPage.ReportFilePath")); //$NON-NLS-1$
		pathLabel.setToolTipText(Messages.getString("ReportFileInfoPage.ToolTip_whereYouWantToSaveTheReportFile")); //$NON-NLS-1$
		pathCombo = new Combo(composite, SWT.BORDER);
		GridData pathGD = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		pathCombo.setLayoutData(pathGD);
		pathCombo.setEnabled(true);
		
		
		browse = new Button(composite, SWT.PUSH);
		pathGD = new GridData(GridData.HORIZONTAL_ALIGN_END);
		browse.setLayoutData(pathGD);
		browse.setText(Messages.getString("ReportFileInfoPage.Browse")); //$NON-NLS-1$
		browse.addSelectionListener(this);
		
		IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
				
		SavingUserData data = new SavingUserData();
		String[] lastUsedDirs = data.getPreviousValues(SavingUserData.ValueTypes.REPORT_PATH);
		
		if (lastUsedDirs != null) {
			  pathCombo.setItems(lastUsedDirs);
			  pathCombo.select(0);
		 }
		else{
			if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_TOOLS))
			{
				pathCombo.setText(Messages.getString("ReportFileInfoPage.tempPath")); //$NON-NLS-1$
			}
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS))
			{
				pathCombo.setText(prefStore.getString(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH) + File.separator + Messages.getString("ReportFileInfoPage.reports")); //$NON-NLS-1$
			}
			else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS))
			{
				pathCombo.setText(prefStore.getString(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS_PATH) + File.separator + Messages.getString("ReportFileInfoPage.reports")); //$NON-NLS-1$
			}
			else
				pathCombo.setText(Messages.getString("ReportFileInfoPage.tempPath")); //$NON-NLS-1$
		}
		
		if(currentSdk.reportPath != null)
		{
			if(pathCombo.getItemCount() >0)
			{
				for(String s:pathCombo.getItems())
				{
					if(currentSdk.reportPath.equals(s))
					{
						pathCombo.select(pathCombo.indexOf(s));
					}
				}
			}
			else
				pathCombo.setText(currentSdk.reportPath);
		}
		
		pathCombo.addModifyListener(this);
		createFilterGroup();
		
		setHelp();
		setControl(composite);
		
	}
	
	private void createFilterGroup()
	{
		Group grp = new Group(composite, SWT.NONE);
		
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		grp.setLayout(gl);
		
		GridData grpGD = new GridData(GridData.FILL_HORIZONTAL);
        grpGD.horizontalSpan = 2; 
		grp.setLayoutData(grpGD);
		grp.setText(Messages.getString("ReportFileInfoPage.FilterReportWithSelectedIssuesFile")); //$NON-NLS-1$
		grp.setVisible(true);
		
		yes = new Button(grp, SWT.RADIO);
		yes.setToolTipText(Messages.getString("ReportFileInfoPage.ToolTip_FilterTheReportFile")); //$NON-NLS-1$
		yes.setText(Messages.getString("ReportFileInfoPage.Yes")); //$NON-NLS-1$
		yes.setSelection(currentSdk.filterNeeded);
		yes.addSelectionListener(this);
		
		no = new Button(grp, SWT.RADIO);
		no.setToolTipText(Messages.getString("ReportFileInfoPage.ToolTip_DoNotFilterReportFile")); //$NON-NLS-1$
		no.setText(Messages.getString("ReportFileInfoPage.No")); //$NON-NLS-1$
		no.setSelection(!currentSdk.filterNeeded);
		no.addSelectionListener(this);
		
		label = new Label(grp, SWT.NONE);
		GridData labelGrid = new GridData(GridData.FILL_HORIZONTAL);
		labelGrid.horizontalSpan = 2;
		label.setText(Messages.getString("ReportFileInfoPage.Knownissues")); //$NON-NLS-1$
		label.setToolTipText(Messages.getString("ReportFileInfoPage.ToolTip_IfYesFilterWithTheFile")); //$NON-NLS-1$
		label.setLayoutData(labelGrid) ;
		
		showTextKnownissuesLabel();
		
		configure = new Button(grp, SWT.PUSH);
		configure.setToolTipText(Messages.getString("ReportFileInfoPage.ToolTip_OpenKnownIssuesDialog")); //$NON-NLS-1$
		configure.setText(Messages.getString("ReportFileInfoPage.Configure_knownIssues")); //$NON-NLS-1$
		configure.setEnabled(false);
		configure.addSelectionListener(this);
		
		yes.setSelection(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.FILTER_REPORTS_LAST_SELECTION));
		no.setSelection(!prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.FILTER_REPORTS_LAST_SELECTION));
		configure.setEnabled(yes.getSelection());
		
		
	}
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	public void widgetSelected(SelectionEvent e) {
		if(e.widget == configure)
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
			getWizard().getContainer().updateButtons();
		}
		else if(e.widget == browse)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
			String newDir = dirDialog.open();
			if(newDir != null)
				pathCombo.setText(newDir);
			
			getWizard().getContainer().updateButtons();
		}
		else if(e.widget==yes)
		{
			configure.setEnabled(true);
			getWizard().getContainer().updateButtons();
		}
		else if(e.widget==no)
		{
			configure.setEnabled(false);
			getWizard().getContainer().updateButtons();
		}
	}
	private void showTextKnownissuesLabel()
	{
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_ISSUES))
		{
			label.setText(Messages.getString("ReportFileInfoPage.Knownissues_Default")); //$NON-NLS-1$
		}
		
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.LATEST_ISSUES))
		{
			label.setText(Messages.getString("ReportFileInfoPage.Knownissues_Latest")); //$NON-NLS-1$
		}
		
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES))
		{
			label.setText(Messages.getString("ReportFileInfoPage.Knownissues_Webserver")); //$NON-NLS-1$
		}
		
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_ISSUES))
		{
			label.setText(Messages.getString("ReportFileInfoPage.Knownissues_Local")); //$NON-NLS-1$
		}
	}
	public boolean canFinish()
	{
		this.canFlipToNextPage();
		if(this.getErrorMessage() == null)
			return true;
		else
			return false;
	}
	
	public void modifyText(ModifyEvent e) {
		if(e.widget == fileName)
		{
			getWizard().getContainer().updateButtons();
		}
		if(e.widget == pathCombo)
		{
			getWizard().getContainer().updateButtons();
		}
		
	}
	public boolean canFlipToNextPage()
	{
		this.setErrorMessage(null);
		this.setMessage(null);
		
		if(fileName.getText().length()==0)
		{
			this.setMessage(Messages.getString("ReportFileInfoPage.ReportFileNameCanNotBeNull"), DialogPage.WARNING); //$NON-NLS-1$
		}
		if(pathCombo.getText().length()==0)
		{
			this.setErrorMessage(Messages.getString("ReportFileInfoPage.ReportFilePathCanNotBeNull")); //$NON-NLS-1$
			return false;
		}
		if(pathCombo.getText().length() > 0)
		{
			File file = new File(pathCombo.getText());
			
			if(!(file.exists() && file.isDirectory()))
			{
				this.setErrorMessage(Messages.getString("ReportFileInfoPage.InvalidPath")); //$NON-NLS-1$
				return false;
			}
			else
			{
				String fullPath = null;
				ProductSDKSelectionPage productPage = (ProductSDKSelectionPage)this.getWizard().getStartingPage();
				isHeaderSelected = productPage.isHeaderAnalysisSelected();
				isLibrarySelected = productPage.isLibraryAnalysisSelected();
				
				if(isHeaderSelected){
					fullPath = pathCombo.getText()+File.separator+ Messages.getString("HeaderAnalyserEngine.Headers_")+fileName.getText()+".xml";
				}
				else if(isLibrarySelected){
					fullPath = pathCombo.getText()+File.separator+ Messages.getString("LibraryAnalyserEngine.Libraries_")+fileName.getText()+".xml";
				}
				if(fullPath!=null && new File(fullPath).exists())
					this.setMessage(Messages.getString("ReportFilterLineReader.ReortFile_AlreadyExist_Warning"), DialogPage.WARNING); //$NON-NLS-1$)
			}
		}
		
		if(yes.getSelection())
		{
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
					setMessage(Messages.getString("ReportFileInfoPage.URLmismatch"),DialogPage.WARNING); //$NON-NLS-1$
				}
				else
					setMessage(null);
			}
		}
		
		if(currentSdk.isOpenedFromConfigFile)
			return false;
		
		return true;
	}
	public void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(configure,
				HelpContextIDs.REPORT_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fileName,
				HelpContextIDs.REPORT_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(pathCombo,
				HelpContextIDs.REPORT_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(yes,
				HelpContextIDs.REPORT_PAGE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(no,
				HelpContextIDs.REPORT_PAGE);
	}
}
