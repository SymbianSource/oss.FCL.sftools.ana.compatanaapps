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
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.data.SavingUserData;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;

/**
 * Compatibility Analyser preferences class 
 */
public class CompatibilityAnalyserPreferences extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {

	private Text webLink;
	private Text knowniss_webLink;
	private Button defaultcoreBtn;
	private Button localcoreBtn;
	private Button browse1;
	private Combo sdkCmb;
	private Combo localCmb;
	private Button webcoreBtn;
	private Button sdkcoreBtn;
	private Vector<ISymbianSDK> rndSdkList;
	private Label coretoolsVersion;
	private ISymbianSDK[] sdkItems;
	private Button refreshBtn;
	private Composite composite;
	private String currentWebserverPath;
	
	private String extractionStatus;
	private String targetPath;
	
	private boolean isRefreshInvoked = false;
	private SavingUserData userData= new SavingUserData();
	private Text baselineTxt;;
	
	private static final String BASELINES_WEBSERVER_URL = Messages.getString("BaselineSDK.URL");
	private static final String CORETOOLS_WEBSERVER_URL = Messages.getString("Coretools.URL");
	private static final String KNOWNISSUES_WEBSERVER_URL = Messages.getString("Knownissues.URL");
	
	public CompatibilityAnalyserPreferences() {
	}

	public CompatibilityAnalyserPreferences(String title) {
		super(title);
	}

	public CompatibilityAnalyserPreferences(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		
		composite = new Composite(parent,SWT.NONE);
		GridLayout gd=new GridLayout(1,false);
		composite.setLayout(gd);		
		GridData data=new GridData(GridData.FILL_BOTH);
		data.verticalIndent=5;
		composite.setLayoutData(data);
		
		Group coreToolGrp=new Group(composite,SWT.NONE);
		coreToolGrp.setText(Messages.getString("CompatibilityAnalyserPreferences.Configurecoretools")); //$NON-NLS-1$
		
		coreToolGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gd1=new GridLayout(3,false);
		coreToolGrp.setLayout(gd1);
		
		defaultcoreBtn = new Button(coreToolGrp,SWT.RADIO);
		defaultcoreBtn.setText(Messages.getString("CompatibilityAnalyserPreferences.UseDefaultTools")); //$NON-NLS-1$
		GridData gd11=new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd11.horizontalSpan=3;
		gd11.horizontalAlignment=GridData.FILL_HORIZONTAL;
		defaultcoreBtn.setLayoutData(gd11);
		defaultcoreBtn.setFocus();
		defaultcoreBtn.setSelection(true);
		defaultcoreBtn.addSelectionListener(this);
		
		localcoreBtn = new Button(coreToolGrp,SWT.RADIO);
		localcoreBtn.setText(Messages.getString("CompatibilityAnalyserPreferences.UseLocalTools")); //$NON-NLS-1$
		GridData gd12=new GridData(GridData.BEGINNING);
		gd12.horizontalSpan=3;
		gd12.horizontalAlignment=GridData.FILL_HORIZONTAL;
		localcoreBtn.setLayoutData(gd12);	
		localcoreBtn.addSelectionListener(this);
			
		localCmb = new Combo(coreToolGrp,SWT.DROP_DOWN|SWT.BORDER);
		GridData gd20=new GridData(GridData.FILL_HORIZONTAL);
		gd20.horizontalSpan=2;
		localCmb.setLayoutData(gd20);
		localCmb.setEnabled(false);
		String[] lastUsedDirs = userData.getPreviousValues(SavingUserData.ValueTypes.CORE_PATH);
		if (lastUsedDirs != null) {
			localCmb.setItems(lastUsedDirs);
			localCmb.select(0);
		}
		localCmb.addSelectionListener(this);
		
		browse1 = new Button(coreToolGrp,SWT.PUSH);
		browse1.setText(Messages.getString("CompatibilityAnalyserPreferences.Browse")); //$NON-NLS-1$
		browse1.setEnabled(false);
		browse1.addSelectionListener(this);
		
		webcoreBtn = new Button(coreToolGrp,SWT.RADIO);
		webcoreBtn.setText(Messages.getString("CompatibilityAnalyserPreferences.UseWebTools")); //$NON-NLS-1$
		GridData gd13=new GridData(GridData.BEGINNING);
		gd13.horizontalSpan=3;
		gd13.horizontalAlignment=GridData.FILL_HORIZONTAL;
		webcoreBtn.setLayoutData(gd13);	
		webcoreBtn.addSelectionListener(this);
		
		webLink = new Text(coreToolGrp,SWT.BORDER|SWT.DROP_DOWN);
		GridData gd14=new GridData(GridData.FILL_HORIZONTAL);
		gd14.horizontalSpan=3;
		webLink.setText(CORETOOLS_WEBSERVER_URL); //$NON-NLS-1$
		webLink.setLayoutData(gd14);
		webLink.setEnabled(false);
		
		sdkcoreBtn = new Button(coreToolGrp,SWT.RADIO);
		sdkcoreBtn.setText(Messages.getString("CompatibilityAnalyserPreferences.UseSDKTools")); //$NON-NLS-1$
		sdkcoreBtn.addSelectionListener(this);
		
		GridData gd15=new GridData(GridData.FILL_HORIZONTAL);
		gd15.horizontalSpan=2;		
		sdkCmb = new Combo(coreToolGrp,SWT.DROP_DOWN|SWT.BORDER|SWT.READ_ONLY);
		sdkCmb.setLayoutData(gd15);
		sdkCmb.setEnabled(false);
		
		//retrieving loaded Rnd sdk's only and adding them to the List box
		sdkItems = CompatibilityAnalyserPlugin.fetchLoadedSdkList().toArray(new ISymbianSDK[0]);
		rndSdkList = new Vector<ISymbianSDK>();
		
		if(sdkItems.length == 0)
		{
			MessageDialog.openError(this.getShell(), "SDK Error", "No SDK's installed");
		}
		else
		{
				int i = 0;
				for(int k= 0; k<sdkItems.length; k++)
				{
				    File bctoolsFolder=new File(sdkItems[k].getEPOCROOT()+File.separator+Messages.getString("CompatibilityAnalyserPreferences.epoc32")+File.separator+Messages.getString("CompatibilityAnalyserPreferences.tools")+File.separator+ //$NON-NLS-1$ //$NON-NLS-2$
				    												Messages.getString("CompatibilityAnalyserPreferences.s60rndtools")+File.separator+Messages.getString("CompatibilityAnalyserPreferences.bctools")); //$NON-NLS-1$ //$NON-NLS-2$
				    if(bctoolsFolder.exists())
				    {
				    	  rndSdkList.add(i, sdkItems[k]);
						  sdkCmb.add(sdkItems[k].getUniqueId(), i);
						  i++;
				    }
					
				}
		}
		sdkCmb.addSelectionListener(this);

		Group verinfoGrp=new Group(coreToolGrp,SWT.NONE);
		GridData verinfodata=new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		verinfodata.horizontalSpan=3;
		verinfoGrp.setLayoutData(verinfodata);
		
		verinfoGrp.setLayout(new GridLayout(3,false));
		
		coretoolsVersion = new Label(verinfoGrp,SWT.RADIO);
		coretoolsVersion.setText(Messages.getString("CompatibilityAnalyserPreferences.CoretoolsNotAvailable")); //$NON-NLS-1$
		GridData vergd13=new GridData(GridData.FILL_HORIZONTAL);
		vergd13.horizontalSpan=2;
		coretoolsVersion.setLayoutData(vergd13);
		
		refreshBtn = new Button(verinfoGrp,SWT.PUSH);
		refreshBtn.setText(Messages.getString("CompatibilityAnalyserPreferences.RefreshVersion")); //$NON-NLS-1$
		refreshBtn.addSelectionListener(this);
		
		Group issuesGrp=new Group(composite,SWT.NONE);
		issuesGrp.setText(Messages.getString("CompatibilityAnalyserPreferences.ConfigureKnownIssues")); //$NON-NLS-1$
		issuesGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		issuesGrp.setLayout(new GridLayout(2,false));
		
		Label lbl=new Label(issuesGrp,SWT.NONE);
		lbl.setText(Messages.getString("CompatibilityAnalyserPreferences.URLofIssuesWebserver")); //$NON-NLS-1$
		GridData gd17=new GridData(GridData.FILL_HORIZONTAL);
		gd17.horizontalSpan=2;
		lbl.setLayoutData(gd17);
		
		knowniss_webLink = new Text(issuesGrp,SWT.BORDER|SWT.DROP_DOWN);
		GridData gd16=new GridData(GridData.FILL_HORIZONTAL);
		gd16.horizontalSpan=2;
		knowniss_webLink.setLayoutData(gd16);
		knowniss_webLink.setText(KNOWNISSUES_WEBSERVER_URL); //$NON-NLS-1$
		
		Group predefinedBaselineGrp=new Group(composite,SWT.NONE);
		predefinedBaselineGrp.setText("Configure URL for predefined baseline profiles");
		predefinedBaselineGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));	
		predefinedBaselineGrp.setLayout(new GridLayout(2,false));
	
		Label lbl4=new Label(predefinedBaselineGrp,SWT.NONE);
		lbl4.setText("");
		
		baselineTxt = new Text(predefinedBaselineGrp,SWT.BORDER);
		baselineTxt.setText(BASELINES_WEBSERVER_URL);
		baselineTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		baselineTxt.addSelectionListener(this);
		
		getPrefsStoreValues();
		
		setHelp();
		return composite;
	}

	/**
	 * Fetches the default values from the preference store object.
	 */
	protected void performDefaults() {
		
		defaultcoreBtn.setSelection(true);
		
		localcoreBtn.setSelection(false);
		localCmb.setEnabled(false);
		browse1.setEnabled(false);
		
		webcoreBtn.setSelection(false);
		webLink.setEnabled(false);
		
		sdkcoreBtn.setSelection(false);
		sdkCmb.setEnabled(false);
		
		webLink.setText(CORETOOLS_WEBSERVER_URL); //$NON-NLS-1$
		knowniss_webLink.setText(KNOWNISSUES_WEBSERVER_URL); //$NON-NLS-1$
		baselineTxt.setText(BASELINES_WEBSERVER_URL);
		
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		store.setValue(CompatibilityAnalyserPreferencesConstants.DEFAULT_ISSUES, true);
		store.setValue(CompatibilityAnalyserPreferencesConstants.LATEST_ISSUES, false);
		store.setValue(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES, false);
		store.setValue(CompatibilityAnalyserPreferencesConstants.LOCAL_ISSUES, false);
				
		super.performDefaults();
	}
	/**
	 * Stores the latest values to preference store object.
	 */
	public boolean performOk() {
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		SavingUserData userData = new SavingUserData();
		
		String url2=knowniss_webLink.getText();
		
		if(url2.length()!=0)
		{
			store.setValue(CompatibilityAnalyserPreferencesConstants.KNOWNISSUES_URL, url2);
		}
		/*else
		{
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.25"), Messages.getString("CompatibilityAnalyserPreferences.26")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}*/
		if(localcoreBtn.getSelection()==true)
		{
			File checkbc=null;
			if(localCmb.getText().length()!=0 )
			{
				checkbc = new File(FileMethods.appendPathSeparator(localCmb.getText()) + Messages.getString("CompatibilityAnalyserPreferences.27")); //$NON-NLS-1$
			}
			else 
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.28"), Messages.getString("CompatibilityAnalyserPreferences.29")); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
			
			//check whether the checkbc.py exists or not 
			if(checkbc.exists() && checkbc.isFile())
			{
				try {
					String dv = CompatibilityAnalyserEngine.getDataVersion(checkbc.getParentFile().getAbsolutePath());
					int num = Integer.parseInt(dv);
					if(num != CompatibilityAnalyserPlugin.DATA_VERSION)
					{
						if(!checkbc.getParentFile().getAbsolutePath().equalsIgnoreCase(CompatibilityAnalyserPlugin.getDefaltCoretoolsPath()))
							MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.42"), "Invalid data version of coretools. Please check the coretools.\nPlease select the coretools of data version "+CompatibilityAnalyserPlugin.DATA_VERSION+".");
					    return false;
					}
				} catch (NumberFormatException e) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.42"), "Invalid data version of coretools. Please check the coretools.\nPlease select the coretools of data version "+CompatibilityAnalyserPlugin.DATA_VERSION+".");
					return false;
				} catch (Exception e) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.42"), "Invalid data version of coretools. Please check the coretools.\nPlease select the coretools of data version "+CompatibilityAnalyserPlugin.DATA_VERSION+".");
					return false;
				}
				store.setValue(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH, localCmb.getText());
				userData.saveValue(SavingUserData.ValueTypes.CORE_PATH, localCmb.getText());
			}
			else 
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.30"), Messages.getString("CompatibilityAnalyserPreferences.31")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return false;
			}
		}
		if(webcoreBtn.getSelection()==true)
		{
			String url1=webLink.getText();
			if(url1.length()!=0)
			{
				if(isRefreshInvoked)
				{
					String prevFolder = store.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR);
					
					FileMethods.deleteFolder(prevFolder);
					
					File currentFolder = new File(targetPath);
					currentFolder.renameTo(new File(CompatibilityAnalyserEngine.getWorkspacePath() + File.separator + Messages.getString("CompatibilityAnalyserPreferences.WebServerContentsNew"))); //$NON-NLS-1$
					
					String corePath = CompatibilityAnalyserEngine.getWebServerToolsPath();
					
					if(corePath != null && corePath.equalsIgnoreCase(targetPath))
					{
						store.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH, CompatibilityAnalyserEngine.getWorkspacePath() + File.separator + Messages.getString("CompatibilityAnalyserPreferences.WebServerContentsNew"));
					}
					else if(!corePath.equalsIgnoreCase(targetPath))
					{
						String coreRoot = CompatibilityAnalyserEngine.getWebServerToolsPath().substring(targetPath.length() + 1);
						store.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH, CompatibilityAnalyserEngine.getWorkspacePath() + File.separator + Messages.getString("CompatibilityAnalyserPreferences.WebServerContentsNew") + File.separator + coreRoot);
					}
						
					store.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_URL, url1);
					store.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR, CompatibilityAnalyserEngine.getWorkspacePath() + File.separator + Messages.getString("CompatibilityAnalyserPreferences.WebServerContentsNew")); //$NON-NLS-1$
					
					isRefreshInvoked = false;
					
				}
				store.setValue(CompatibilityAnalyserPreferencesConstants.CORETOOLS_URL, url1);
			}
			else
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.38"), Messages.getString("CompatibilityAnalyserPreferences.39")); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		}
		if(sdkcoreBtn.getSelection()==true)
		{
			if(sdkCmb.getText().length() != 0)
			{
				store.setValue(CompatibilityAnalyserPreferencesConstants.SDK_NAME, sdkCmb.getText());
				ISymbianSDK selectedSdk = rndSdkList.elementAt(sdkCmb.getSelectionIndex());
				String bctoolsPath = FileMethods.appendPathSeparator(selectedSdk.getEPOCROOT()) + Messages.getString("CompatibilityAnalyserPreferences.40"); //$NON-NLS-1$
				File cbc=new File(bctoolsPath+File.separator+Messages.getString("CompatibilityAnalyserPreferences.41")); //$NON-NLS-1$
				if(!cbc.exists())
				{
					MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.42"), bctoolsPath + Messages.getString("CompatibilityAnalyserPreferences.43")); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
				
				try {
					String dv = CompatibilityAnalyserEngine.getDataVersion(bctoolsPath);
					int num = Integer.parseInt(dv);
					if(num != CompatibilityAnalyserPlugin.DATA_VERSION)
					{
						if(!bctoolsPath.equalsIgnoreCase(CompatibilityAnalyserPlugin.getDefaltCoretoolsPath()))
							MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.42"), "Invalid data version of coretools. Please check the coretools.\nPlease select the coretools of data version "+CompatibilityAnalyserPlugin.DATA_VERSION+".");
					    return false;
					}
				} catch (NumberFormatException e) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.42"), "Invalid data version of coretools. Please check the coretools.\nPlease select the coretools of data version "+CompatibilityAnalyserPlugin.DATA_VERSION+".");
					return false;
				} catch (Exception e) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.42"), "Invalid data version of coretools. Please check the coretools.\nPlease select the coretools of data version "+CompatibilityAnalyserPlugin.DATA_VERSION+".");
					return false;
				}
				
				store.setValue(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS_PATH, bctoolsPath);
			}
			else 
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.44"), "Please provide a R&D SDK for coretools"); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		}
		
		store.setValue(CompatibilityAnalyserPreferencesConstants.DEFAULT_TOOLS, defaultcoreBtn.getSelection());
		store.setValue(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS, localcoreBtn.getSelection());
		store.setValue(CompatibilityAnalyserPreferencesConstants.WEB_TOOLS, webcoreBtn.getSelection());
		store.setValue(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS, sdkcoreBtn.getSelection());
		
		store.setValue(CompatibilityAnalyserPreferencesConstants.BASELINES_URL, baselineTxt.getText());
		
		return super.performOk();
	}
	/**
	 * Get and set the previous values to the preferences page. 
	 *
	 */
	private void getPrefsStoreValues() {
				
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		
		defaultcoreBtn.setSelection(store.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_TOOLS));
		localcoreBtn.setSelection(store.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS));
		webcoreBtn.setSelection(store.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_TOOLS));
		sdkcoreBtn.setSelection(store.getBoolean(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS));
		if(defaultcoreBtn.getSelection()==true)
		{
			localCmb.setEnabled(false);
			browse1.setEnabled(false);
			webLink.setEnabled(false);
			sdkCmb.setEnabled(false);
			
		}
		else if(localcoreBtn.getSelection()==true)
		{
			localCmb.setEnabled(true);
			String[] lastUsedDirs = userData.getPreviousValues(SavingUserData.ValueTypes.CORE_PATH);
			
			if (lastUsedDirs != null) {
				localCmb.setItems(lastUsedDirs);
				localCmb.select(0);
			}
						
			browse1.setEnabled(true);
			webLink.setEnabled(false);
			sdkCmb.setEnabled(false);
			
			if(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH != null && 
					!CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH.equals(""))
				localCmb.setText(store.getString(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH));
			
		}
		
		else if(webcoreBtn.getSelection()==true)
		{
			localCmb.setEnabled(false);
			browse1.setEnabled(false);
			webLink.setEnabled(true);
			sdkCmb.setEnabled(false);
			webLink.setText(store.getString(CompatibilityAnalyserPreferencesConstants.CORETOOLS_URL));
			
			String toolsPath=store.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH);
			displayVersion(toolsPath,Messages.getString("CompatibilityAnalyserPreferences.49")); //$NON-NLS-1$
		}
		
		else if(sdkcoreBtn.getSelection()==true)
		{
			localCmb.setEnabled(false);
			browse1.setEnabled(false);
			webLink.setEnabled(false);
			sdkCmb.setEnabled(true);
			String prevSDK=store.getString(CompatibilityAnalyserPreferencesConstants.SDK_NAME);
			
			if(sdkCmb.indexOf(prevSDK)!=-1)
			{
				sdkCmb.select(sdkCmb.indexOf(prevSDK));
			}
		}
		
		String url2=store.getString(CompatibilityAnalyserPreferencesConstants.KNOWNISSUES_URL);
		knowniss_webLink.setText(url2);
		
		baselineTxt.setText(store.getString(CompatibilityAnalyserPreferencesConstants.BASELINES_URL));
	}
	public void init(IWorkbench workbench) {
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if(e.widget==browse1)
		{
			DirectoryDialog dirDlg=new DirectoryDialog(Display.getCurrent().getActiveShell());
			String dirName=null;
			if((dirName=dirDlg.open())!=null)
			{
				localCmb.setText(dirName);
				displayVersion(localCmb.getText(), Messages.getString("CompatibilityAnalyserPreferences.50")); //$NON-NLS-1$
			}
			else
				return;
		}
		else if(e.widget == defaultcoreBtn && defaultcoreBtn.getSelection())
		{
			localCmb.setEnabled(false);
			browse1.setEnabled(false);
			webLink.setEnabled(false);
			sdkCmb.setEnabled(false);
			String corePath = FileMethods.appendPathSeparator(CompatibilityAnalyserPlugin.getInstallPathOfToolsPlugin()) + Messages.getString("CompatibilityAnalyserPreferences.51"); //$NON-NLS-1$
			displayVersion(corePath, Messages.getString("CompatibilityAnalyserPreferences.52")); //$NON-NLS-1$
			
		}
		else if(e.widget == localcoreBtn && localcoreBtn.getSelection())
		{
			localCmb.setEnabled(true);
			
			SavingUserData userData = new SavingUserData();
			String[] lastUsedDirs = userData.getPreviousValues(SavingUserData.ValueTypes.CORE_PATH);
			
			if (lastUsedDirs != null) {
				localCmb.setItems(lastUsedDirs);
				localCmb.select(0);
			}
			
			browse1.setEnabled(true);
			webLink.setEnabled(false);
			sdkCmb.setEnabled(false);
			displayVersion(localCmb.getText(), Messages.getString("CompatibilityAnalyserPreferences.53")); //$NON-NLS-1$
			
		}
		else if(e.widget == sdkcoreBtn && sdkcoreBtn.getSelection())
		{
			localCmb.setEnabled(false);
			browse1.setEnabled(false);
			webLink.setEnabled(false);
			sdkCmb.setEnabled(true);
			ISymbianSDK[] sdks=rndSdkList.toArray(new ISymbianSDK[0]);
			String toolsPath=null;
			if(sdks.length!=0)
			{
				sdkCmb.select(0);
				String epocRoot = sdks[sdkCmb.getSelectionIndex()].getEPOCROOT();
				
				toolsPath = FileMethods.appendPathSeparator(epocRoot) + Messages.getString("CompatibilityAnalyserPreferences.epoc32")+ File.separator + Messages.getString("CompatibilityAnalyserPreferences.tools")+File.separator+ //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("CompatibilityAnalyserPreferences.s60rndtools")+File.separator+Messages.getString("CompatibilityAnalyserPreferences.bctools"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			displayVersion(toolsPath, Messages.getString("CompatibilityAnalyserPreferences.58")); //$NON-NLS-1$
			
		}
		else if(e.widget==webcoreBtn && webcoreBtn.getSelection())
		{
			localCmb.setEnabled(false);
			browse1.setEnabled(false);
			webLink.setEnabled(true);
			sdkCmb.setEnabled(false);
			
			IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
			String toolsPath=store.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH);
			displayVersion(toolsPath,Messages.getString("CompatibilityAnalyserPreferences.60"));		 //$NON-NLS-1$
			
		}
		else if(e.widget==sdkCmb)
		{
			ISymbianSDK[] sdks=rndSdkList.toArray(new ISymbianSDK[0]);
			String epocRoot = sdks[sdkCmb.getSelectionIndex()].getEPOCROOT();
			
			String toolsPath = FileMethods.appendPathSeparator(epocRoot) + Messages.getString("CompatibilityAnalyserPreferences.epoc32")+File.separator+Messages.getString("CompatibilityAnalyserPreferences.tools")+File.separator+ //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("CompatibilityAnalyserPreferences.s60rndtools")+File.separator+Messages.getString("CompatibilityAnalyserPreferences.bctools"); //$NON-NLS-1$ //$NON-NLS-2$
			displayVersion(toolsPath, Messages.getString("CompatibilityAnalyserPreferences.65")); //$NON-NLS-1$
			
		}	
		else if(e.widget == refreshBtn)
		{
			showWebToolsProgressBar();			
		}

	}
	/**
	 * Starts downloading coretools from the URL given.
	 * Displays the version of coretools after downloading the tools 
	 *
	 */
	public void showWebToolsProgressBar()
	{
		if(webcoreBtn.getSelection())
		{
		   currentWebserverPath=webLink.getText();
	       if(CompatibilityAnalyserEngine.isDownloadAndExtractionNeeded(currentWebserverPath))
		   {
			   // if true download latest
		       IRunnableWithProgress op = new IRunnableWithProgress() {
		    		public void run(IProgressMonitor monitor) {
				    	targetPath = CompatibilityAnalyserEngine.getWorkspacePath() + File.separator + Messages.getString("CompatibilityAnalyserPreferences.WebServerLatest"); //$NON-NLS-1$
				    	extractionStatus = CompatibilityAnalyserEngine.readAndDownloadSupportedCoretools(currentWebserverPath, targetPath, monitor);
				      }
				   };
				   IWorkbench wb = PlatformUI.getWorkbench();
				   IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				   Shell shell = win != null ? win.getShell() : null;
				   try {
					new ProgressMonitorDialog(shell).run(true, true, op);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				////After download check the version and display
				if(extractionStatus == null)
				{
					isRefreshInvoked = true;
					displayVersion(CompatibilityAnalyserEngine.getWebServerToolsPath(),Messages.getString("CompatibilityAnalyserPreferences.67")); //$NON-NLS-1$
				}
				else{
				
					if(targetPath!=null)
					{
						File latest=new File(targetPath);
						if(latest.exists())
						{
							FileMethods.deleteFolder(latest.getAbsolutePath());
						}
					}
					MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.44"), "Error in Extracting CoreTools: " + extractionStatus);
				}
		   }
		   else
		   {
			   MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.69"), Messages.getString("CompatibilityAnalyserPreferences.70")); //$NON-NLS-1$ //$NON-NLS-2$
			   IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
			   String toolsPath=store.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH);
			   displayVersion(toolsPath,Messages.getString("CompatibilityAnalyserPreferences.71"));		 //$NON-NLS-1$
		   }
		}
		else if(localcoreBtn.getSelection())
		{
			displayVersion(localCmb.getText(), Messages.getString("CompatibilityAnalyserPreferences.72")); //$NON-NLS-1$
		}
		else if(sdkcoreBtn.getSelection())
		{
			ISymbianSDK[] sdks=rndSdkList.toArray(new ISymbianSDK[0]);
			String toolsPath=null;
			if(sdks.length!=0 && sdkCmb.getSelectionIndex() != -1)
			{
				String epocRoot = sdks[sdkCmb.getSelectionIndex()].getEPOCROOT();
				
				toolsPath = FileMethods.appendPathSeparator(epocRoot) + Messages.getString("CompatibilityAnalyserPreferences.epoc32")+File.separator+Messages.getString("CompatibilityAnalyserPreferences.tools")+File.separator+ //$NON-NLS-1$ //$NON-NLS-2$
					Messages.getString("CompatibilityAnalyserPreferences.s60rndtools")+File.separator+Messages.getString("CompatibilityAnalyserPreferences.bctools"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			displayVersion(toolsPath, Messages.getString("CompatibilityAnalyserPreferences.77")); //$NON-NLS-1$
		}
		else if(defaultcoreBtn.getSelection())
		{
			String corePath = FileMethods.appendPathSeparator(CompatibilityAnalyserPlugin.getInstallPathOfToolsPlugin()) + Messages.getString("CompatibilityAnalyserPreferences.78"); //$NON-NLS-1$
			displayVersion(corePath, Messages.getString("CompatibilityAnalyserPreferences.79")); //$NON-NLS-1$
		}
		
	}
	
	public void displayVersion(String toolsPath,String lblText)
	{
		if(toolsPath != null)
		{
			File checkbc=new File(FileMethods.appendPathSeparator(toolsPath) + Messages.getString("CompatibilityAnalyserPreferences.80")); //$NON-NLS-1$
			if(checkbc.exists() && checkbc.isFile())
			{
				try 
				{
					String ver=CompatibilityAnalyserEngine.getCoreToolsVersion(toolsPath);
				
					if(ver == null)
					{
						MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("CompatibilityAnalyserPreferences.44"), "Error in getting coretools version. Please check the coretools");
					}
					else if(ver.length()<8)
						coretoolsVersion.setText(lblText+" : "+ver); //$NON-NLS-1$
					else
						coretoolsVersion.setText(lblText+Messages.getString("CompatibilityAnalyserPreferences.82")); //$NON-NLS-1$
					
					return;
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		
		coretoolsVersion.setText(lblText+Messages.getString("CompatibilityAnalyserPreferences.83")); //$NON-NLS-1$
	}
	public boolean performCancel() {
		if(targetPath!=null)
		{
			File latest=new File(targetPath);
			if(latest.exists())
			{
				FileMethods.deleteFolder(latest.getAbsolutePath());
			}
		}
		return super.performCancel();
	}
	/**
	 * Sets context sensitive help for this page.
	 *
	 */
	public void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(webLink,
				HelpContextIDs.CONFIGURE_PREFERENCES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(knowniss_webLink,
				HelpContextIDs.CONFIGURE_PREFERENCES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(localCmb,
				HelpContextIDs.CONFIGURE_PREFERENCES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(localcoreBtn,
				HelpContextIDs.CONFIGURE_PREFERENCES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sdkCmb,
				HelpContextIDs.CONFIGURE_PREFERENCES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sdkcoreBtn,
				HelpContextIDs.CONFIGURE_PREFERENCES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(defaultcoreBtn,
				HelpContextIDs.CONFIGURE_PREFERENCES);
	}

}
