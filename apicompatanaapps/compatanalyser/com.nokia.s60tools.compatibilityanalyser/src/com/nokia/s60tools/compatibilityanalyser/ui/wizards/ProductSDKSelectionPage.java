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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.SavingUserData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.dialogs.BaselineEditor;
import com.nokia.s60tools.compatibilityanalyser.utils.BaselineProfileUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalyserUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.GlobalDataReader;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

/**
 * 
 * Wizard Page of Analysis wizard, wherein Baseline profile
 * and Current SDK details can be given
 *
 */
public class ProductSDKSelectionPage extends S60ToolsWizardPage implements ModifyListener, SelectionListener {

	private Composite composite;
	private String [] sdkNames;
	private String [] sdkVersions;
	private int size = 0;
	private List<ISymbianSDK> loadedSdks;
	
	private CompatibilityAnalyserEngine engine;
	public Combo profileCombo;
	private SavingUserData prevData=new SavingUserData();
	private String[] basics;
	private Button configure;
	private IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();

	Label epocDirectory;
	Combo sdkNameCombo;
	Combo versionCombo;
	Button headersButton;
	Button libsButton;
	ISymbianSDK selectedSDK;
	
	private String targetPath=null;
	private String targetURL=null;
	private String downloadStatus;
	
	public ProductSDKSelectionPage(CompatibilityAnalyserEngine engine) {
		super(Messages.getString("ProductSDKSelectionPage.title")); //$NON-NLS-1$

		setTitle(Messages.getString("ProductSDKSelectionPage.PageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("ProductSDKSelectionPage.Descreption")); //$NON-NLS-1$
		
		setPageComplete(false);
		this.engine = engine;
	}

	@Override
	public void recalculateButtonStates() {

	}

	@Override
	public void setInitialFocus() {

	}

	/**
	 * Constructs the UI elements of this page on given Composite
	 */
	public void createControl(Composite parent) {

		composite = new Composite(parent, SWT.NULL);
		GridLayout g = new GridLayout();
		g.numColumns = 1;
		composite.setLayout(g);

		Group filetypeGroup = new Group(composite, SWT.NONE) ; 

		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		GridData productGD = new GridData(GridData.FILL_HORIZONTAL);

		filetypeGroup.setLayout(gl);
		filetypeGroup.setLayoutData(productGD);
		filetypeGroup.setVisible(true);
		filetypeGroup.setText(Messages.getString("ProductSDKSelectionPage.AnalysisType")); 

		//Creation of Group for File Types Selection
		headersButton = new Button(filetypeGroup, SWT.CHECK);
		GridData headerGD = new GridData(GridData.FILL_HORIZONTAL);
		headerGD.horizontalSpan = 2;
		headersButton.setLayoutData(headerGD);
		headersButton.setSelection(engine.isHeaderAnalysisChecked());
		headersButton.setText(Messages.getString("ProductSDKSelectionPage.HeaderAnalysis")); 
		headersButton.setToolTipText(Messages.getString("ProductSDKSelectionPage.ToolTip_checkbox")); 
		headersButton.addSelectionListener(this);

		libsButton = new Button(filetypeGroup, SWT.CHECK);
		GridData libsGD = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		libsGD.horizontalSpan = 2;
		libsButton.setLayoutData(libsGD);
		libsButton.setSelection(engine.isLibraryAnalysisChecked());
		libsButton.setText(Messages.getString("ProductSDKSelectionPage.LibraryAnalysis")); 
		libsButton.setToolTipText(Messages.getString("ProductSDKSelectionPage.Tooltip_libsbutton")); 
		libsButton.addSelectionListener(this);

		createBaselineGroup();
		createProductGroup();

		setHelp();
		setControl(composite);
	}

	/**
	 * Validates the given values by user for various fields.
	 * If invalid, shows an error message and returns false.
	 * Otherwise, this returns true.
	 */
	public boolean canFlipToNextPage()
	{
		this.setErrorMessage(null);

		if(sdkNameCombo.getText().length() == 0)
		{
			this.setErrorMessage("Please provide a valid SDK"); 
			return false;
		}   
		else if(sdkNameCombo.indexOf(sdkNameCombo.getText()) == -1)
		{
			this.setErrorMessage("Invalid SDK.");
			return false;
		}
		if((!headersButton.getSelection()) && (!libsButton.getSelection()))
		{
			this.setErrorMessage(Messages.getString("ProductSDKSelectionPage.NoFileTypes")); 
			return false;
		}
		if(versionCombo.getText().length() == 0)
		{
			this.setErrorMessage(Messages.getString("ProductSDKSelectionPage.VersionStringCannotbeNull")); 
			return false;
		}
		
		if(selectedSDK != null)
		{
			File filepath = new File(selectedSDK.getEPOCROOT());
			if(!filepath.exists()){
				setErrorMessage(Messages.getString("LibraryFilesPage.28"));
				return false;
			}
		}
		if(profileCombo.getSelectionIndex() == 0 || profileCombo.getText().length() == 0)
		{
			profileCombo.select(0);
			setErrorMessage("Select profile from the list");
			return false;
		}
		else if(profileCombo.getText().length() >0 && profileCombo.indexOf(profileCombo.getText()) == -1)
		{
			setErrorMessage("Given profile does not exist. Please select a valid profile");
			return false;
		}
		else
			configure.setEnabled(true);
		
		return true;
	}
	public boolean canFinish()
	{
		return this.canFlipToNextPage();
	}
	private void createBaselineGroup()
	{
		Group baselineGrp=new Group(composite,SWT.NONE);
		baselineGrp.setText("Baseline Profile Configuration");
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		baselineGrp.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		baselineGrp.setLayoutData(gd);

		Label selectLabel = new Label(baselineGrp, SWT.NONE);
		selectLabel.setText(Messages.getString("BaselineSelectionPage.SelectBaseline"));
		
		profileCombo = new Combo(baselineGrp,SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
		GridData comboGD = new GridData(GridData.FILL_HORIZONTAL);
		profileCombo.setLayoutData(comboGD);
		profileCombo.addSelectionListener(this);
		profileCombo.addModifyListener(this);
		basics = new String[]{"Select profile here..."};
		profileCombo.setItems(basics);
		
		String[] array = BaselineProfileUtils.getAllBaselinesProfiles();
		for(String profile:array)
			if(profileCombo.indexOf(profile)==-1)
				profileCombo.add(profile);
		profileCombo.select(0);
		
		String lastUsedProfile=prefStore.getString(CompatibilityAnalyserPreferencesConstants.LAST_USED_BASELINE_PROFILE);
		if(lastUsedProfile!=null)
			profileCombo.select(profileCombo.indexOf(lastUsedProfile));
		
		Label editLabel = new Label(baselineGrp, SWT.NONE);
		editLabel.setText(Messages.getString("BaselineSelectionPage.EditBaseline"));

		configure = new Button(baselineGrp, SWT.PUSH);
		configure.setText(Messages.getString("BaselineSelectionPage.ConfigureBaseline"));
		configure.setToolTipText(Messages.getString("BaselineSelectionPage.OpenBaseline"));
		configure.addSelectionListener(this);

	}
	private void createProductGroup()
	{
		Group productGroup = new Group(composite, SWT.NONE) ; 

		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		GridData productGD = new GridData(GridData.FILL_HORIZONTAL);

		productGroup.setLayout(gl);
		productGroup.setLayoutData(productGD);
		productGroup.setVisible(true);
		productGroup.setText(Messages.getString("ProductSDKSelectionPage.CurrentSDKConfiguration")); 


		Label nameLabel = new Label(productGroup, SWT.LEFT);
		nameLabel.setText(Messages.getString("ProductSDKSelectionPage.SDKName")); 
		GridData nameGD = new GridData(GridData.FILL_HORIZONTAL);
		nameLabel.setLayoutData(nameGD);

		sdkNameCombo = new Combo(productGroup, SWT.BORDER | SWT.DROP_DOWN);
		GridData comboGD = new GridData(GridData.FILL_HORIZONTAL );
		sdkNameCombo.setLayoutData(comboGD);
		sdkNameCombo.setToolTipText(Messages.getString("ProductSDKSelectionPage.Tooltip_sdkCombo")); 
		sdkNameCombo.addModifyListener(this);

		loadedSdks = CompatibilityAnalyserPlugin.fetchLoadedSdkList();
		size = loadedSdks.size();

		Label versionLabel = new Label(productGroup, SWT.LEFT);
		versionLabel.setText(Messages.getString("ProductSDKSelectionPage.CurrentSDKVersion")); //$NON-NLS-1$

		GridData versionGD = new GridData(GridData.FILL_HORIZONTAL);
		versionLabel.setLayoutData(versionGD);
		
		versionCombo = new Combo(productGroup, SWT.BORDER);
		GridData textGD = new GridData(GridData.FILL_HORIZONTAL);
		versionCombo.setLayoutData(textGD);
		GlobalDataReader reader = CompatibilityAnalyserUtils.getGlobalDataReader();
		if(reader != null)
		{
			String[] sVersions = reader.readSupportedSDKVersions();
			if(sVersions!=null)
				versionCombo.setItems(sVersions);
		}
		versionCombo.setToolTipText(Messages.getString("ProductSDKSelectionPage.ToolTipForVersionCombo"));
		versionCombo.addModifyListener(this);

		Label epocRootLabel = new Label(productGroup, SWT.LEFT);
		epocRootLabel.setText(Messages.getString("ProductSDKSelectionPage.EpocRootLabel"));
		GridData epocGD = new GridData(GridData.FILL_HORIZONTAL);
		epocRootLabel.setLayoutData(epocGD);

		epocDirectory = new Label(productGroup, SWT.NONE);
		epocDirectory.setToolTipText(Messages.getString("ProductSDKSelectionPage.ToolTipForepocRootDir"));
		epocDirectory.setLayoutData(epocGD);

		if(size > 0)
		{
			sdkNames = new String[size];
			sdkVersions = new String[size];
			for(int i = 0; i<size; i++)
			{
				sdkNames[i] = loadedSdks.get(i).getUniqueId();
				String s = loadedSdks.get(i).getSDKVersion().toString();
				sdkVersions[i] = s.substring(0,3);
				sdkNameCombo.add(sdkNames[i], i);
			}
			try
			{
				sdkNameCombo.setItems(sdkNames);

				ProductSdkData sdkData = engine.getCurrentSdkData();

				if(sdkData != null && sdkData.productSdkName != null){
					if(sdkNameCombo.indexOf(sdkData.productSdkName) != -1)
						sdkNameCombo.select(sdkNameCombo.indexOf(sdkData.productSdkName));
					else
						sdkNameCombo.setText(sdkData.productSdkName);
				}
				else
					sdkNameCombo.select(0);

				int index = sdkNameCombo.getSelectionIndex();

				if(index != -1){
					selectedSDK = loadedSdks.get(index);

					if(versionCombo.indexOf(sdkVersions[index])!=-1)
						versionCombo.select(versionCombo.indexOf(sdkVersions[index]));
					else
					{
						SavingUserData userData = new SavingUserData();
						String lastVersion = userData.getLastSdkVersion(sdkNameCombo.getText());
						if(lastVersion!= null)
							versionCombo.setText(lastVersion);
						else
							versionCombo.setText(""); 
					}
					epocDirectory.setText(selectedSDK.getEPOCROOT());
					sdkData.productSdkName = sdkNameCombo.getText();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else
			this.setErrorMessage(Messages.getString("ProductSDKSelectionPage.NoSDKsInstalled")); 

	}

	public void modifyText(ModifyEvent e) {
		try
		{
			if(e.widget == sdkNameCombo)
			{
				int i = sdkNameCombo.indexOf(sdkNameCombo.getText());

				if(i != -1)
				{
					epocDirectory.setText(loadedSdks.get(i).getEPOCROOT());
					if(versionCombo.indexOf(sdkVersions[i])!=-1)
						versionCombo.select(versionCombo.indexOf(sdkVersions[i]));
					else
					{
						SavingUserData userData = new SavingUserData();
						String lastVersion = userData.getLastSdkVersion(sdkNameCombo.getText());
						if(lastVersion!= null)
							versionCombo.setText(lastVersion);
						else
							versionCombo.setText(""); 
					}
					selectedSDK = loadedSdks.get(i);
					engine.getCurrentSdkData().productSdkName = sdkNameCombo.getText();
				}
				this.getContainer().updateButtons();
			}
			else if(e.widget == versionCombo)
			{
				this.getContainer().updateButtons();
			}
			else if(e.widget == profileCombo){
				engine.setBaselineProfile(profileCombo.getText());
				this.getContainer().updateButtons();
			}

		}catch(Exception exception){
			System.out.println(exception.getMessage());
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {

	}

	public void widgetSelected(SelectionEvent e) {

		if((e.widget == headersButton) || (e.widget == libsButton))
		{
			this.getContainer().updateButtons();
		}
		else if(e.widget==configure)
		{
			BaselineEditor dd=null;
			if(profileCombo.getSelectionIndex() == 0)
				dd=new BaselineEditor(Display.getDefault().getActiveShell(),null);
			else
				dd=new BaselineEditor(Display.getDefault().getActiveShell(),profileCombo.getText());
			dd.setBlockOnOpen(true);
			
			int status = dd.open();
			if( status == IDialogConstants.OK_ID)
			{
				profileCombo.setItems(basics);
				String[] updatedProfiles = BaselineProfileUtils.getAllBaselinesProfiles();
				if(updatedProfiles!=null)
				{
					for (int i = 0; i < updatedProfiles.length; i++) 
						if(profileCombo.indexOf(updatedProfiles[i])==-1)
						{
							profileCombo.add(updatedProfiles[i]);
						}
					if(updatedProfiles.length>0)
					{
						if(profileCombo.indexOf(updatedProfiles[0])!=-1)
						{
							Object obj = BaselineProfileUtils.getBaselineProfileData(updatedProfiles[0]);
							if(obj instanceof BaselineProfile && ((BaselineProfile)obj).isUpdated())
								profileCombo.select(profileCombo.indexOf(updatedProfiles[0]));
							else
								profileCombo.select(0);
						}
					}
				}
				else
					profileCombo.select(0);
			}
			else if( status == IDialogConstants.IGNORE_ID)
			{
				profileCombo.setItems(basics);
				String[] updatedProfiles = BaselineProfileUtils.getAllBaselinesProfiles();
				if(updatedProfiles!=null)
					for (int i = 0; i < updatedProfiles.length; i++) 
						if(profileCombo.indexOf(updatedProfiles[i])==-1)
							profileCombo.add(updatedProfiles[i]);
				profileCombo.select(0);
			}
			
			engine.setBaselineProfile(profileCombo.getText());
			this.getContainer().updateButtons();
		}
		else if(e.widget == profileCombo)
		{
			this.setMessage(null);
			
			Object obj = BaselineProfileUtils.getBaselineProfileData(profileCombo.getText());
			if(obj instanceof BaselineProfile)
			{
				BaselineProfile pro = (BaselineProfile)obj;
				if(pro.isPredefined())
				{
					if(!pro.isUpdated())
					{
						targetPath = "c:\\apps\\ca-baselines\\"+pro.getProfileName();
						targetURL = pro.getSdkUrl();
						
						IRunnableWithProgress op = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) {
								if(new File(targetPath).exists())
									FileMethods.deleteFolder(targetPath);
								downloadStatus=CompatibilityAnalyserEngine.downloadAndExtractFileFromWebServer(targetURL, targetPath, CompatibilityAnalyserEngine.ElementTypes.FolderType, "include", monitor);
							}
						};
						IWorkbench wb = PlatformUI.getWorkbench();
						IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
						Shell shell = win != null ? win.getShell() : null;
						try {
							new ProgressMonitorDialog(shell).run(true, true, op);
						} catch (InvocationTargetException err) {
							err.printStackTrace();
						} catch (InterruptedException err) {
							err.printStackTrace();
						}
						
						if(downloadStatus == null)
						{
							File epocRoot=new File(CompatibilityAnalyserEngine.getEpocFolderPath());
							BaselineProfile profile = (BaselineProfile)obj;
							profile.setSdkName(profile.getProfileName());
							profile.setRadio_default_hdr(true);
							profile.setRadio_dir_hdr(false);
							profile.setRadio_default_build_target(true);
							profile.setRadio_build_target(false);
							profile.setRadio_dir_libs(false);
							profile.setSdkEpocRoot(FileMethods.appendPathSeparator(epocRoot.getParentFile().getAbsolutePath()));
							profile.setUpdated(true);
							BaselineProfileUtils.saveProfileOnFileSystem(profile);
							prevData.saveValue(SavingUserData.ValueTypes.PROFILENAME, profile.getProfileName());
						}
						else
						{
							profileCombo.select(0);
							MessageDialog.openError(this.getShell(), "Compatibility Analyser", downloadStatus);
							downloadStatus=null;    				
						}
					}
				}
				engine.setBaselineProfile(profileCombo.getText());
			}
			this.getContainer().updateButtons();
		}

	}

	public void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(headersButton,
				HelpContextIDs.CURRENT_SDK);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(libsButton,
				HelpContextIDs.CURRENT_SDK);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sdkNameCombo,
				HelpContextIDs.CURRENT_SDK);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(versionCombo,
				HelpContextIDs.CURRENT_SDK);
	}
	public boolean isHeaderAnalysisSelected()
	{
		return headersButton.getSelection();
	}
	public boolean isLibraryAnalysisSelected()
	{
		return libsButton.getSelection();
	}
}
