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
* Description: Class for Baseline Editor Dialog
*
*/
package com.nokia.s60tools.compatibilityanalyser.ui.dialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.DefaultViewConfiguration;
import com.nokia.carbide.cdt.builder.EpocEngineHelper;
import com.nokia.carbide.cdt.builder.EpocEnginePathHelper;
import com.nokia.carbide.cdt.builder.ICarbideBuildManager;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectModifier;
import com.nokia.carbide.cpp.epoc.engine.model.BldInfModelFactory;
import com.nokia.carbide.cpp.epoc.engine.model.bldinf.IBldInfData;
import com.nokia.carbide.cpp.epoc.engine.model.bldinf.IBldInfOwnedModel;
import com.nokia.carbide.cpp.epoc.engine.model.bldinf.IBldInfView;
import com.nokia.carbide.cpp.epoc.engine.model.bldinf.IExport;
import com.nokia.carbide.cpp.project.core.ProjectCorePlugin;
import com.nokia.carbide.cpp.sdk.core.ISymbianBuildContext;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.SavingUserData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageKeys;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.FeedbackHandler;
import com.nokia.s60tools.compatibilityanalyser.utils.BaselineProfileUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalyserUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.GlobalDataReader;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;

/**
 * Class for Baseline Editor Dialog
 */
public class BaselineEditor extends TrayDialog implements SelectionListener , FeedbackHandler, ModifyListener{

	private Composite shell;

	//Profile Group
	private Combo profileCmb;
	private Combo sdkCmb;
	private Combo versionCmb;
	private Label sdkrootLbl;
	private String [] sdkNames;
	public  ArrayList<BaselineProfile> profileList=null;
	private ISymbianSDK[] sdkItems;

	//Current Workspace project Group
	private Button infCheck;
	private Combo bldInfPath;

	//Header Group
	private Button radio_default_Hdr;
	private Button radio_Hdr_dir;
	private List hdr_dir_list;
	private Button addBtn_hdrGrp;
	private Button removeBtn_hdrGrp;
	private Button removeAllBtn_hdrGrp;
	private Group hdrGrp;

	//System Includes Group
	private List list_systemInc;
	private Button addBtn_sysIncGrp;
	private Button removeBtn_sysIncGrp;
	private Button removeAllBtn_sysIncGrp;

	//Forced Headers Group
	private Group forcedGrp;
	//private Button forced_hdrs_check;
	private List forced_hdrs_list;
	private Button forced_addBtn;
	private Button forced_removeBtn;
	private Button forced_removeAllBtn;

	//Libraries Group
	private Group libGrp;
	private List list_build_Config;
	private Button radio_build_target;
	private Button radio_default_build_target;
	private Button radio_dir_Libs;
	private TabFolder dllPaths_Folder;
	private TabItem dsoPaths_tab;
	private List dso_dir_list;
	private Button addDsoDir_btn;
	private Button removeDsoDir_Btn;
	private Button removeAllDsoDirs_Btn;
	private TabItem dllPaths_tab;
	private List dll_dir_list;
	private Button addDllDir_btn;
	private Button removeDllDir_btn;
	private Button removeAllDllDir_Btn;

	//Baseline Editor Controls
	private Button saveBtn;
	private Button deleteBtn;
	private Button cancelBtn;

	public SavingUserData prevData=new SavingUserData();;
	public String selectedProfile;

	private ShowFilesListDialog addDialog;
	private String absolutePath;

	private ArrayList<String> numOfFiles;
	private ArrayList<String> displayFiles; 

	private Button browseBld;
	private IProject selectedProj;
	private String bldPath;
	private ISymbianSDK selectedSdk;
	private ArrayList<File> userFiles;
	private ArrayList<File> systemFiles;
	private String [] srcRoots;
	private String projName;
	private String projLocation;
	private boolean isCancelled;

	private boolean projExists;
	private boolean projDeletionReq;

	private String targetPath=null;
	private String targetURL=null;
	private String downloadStatus;
	
	private String[] allSysIncHdrPaths;
	private ArrayList<String> children;
	private ArrayList<String> subChildren;
	private boolean isMonitorCancelled;
	private ProgressMonitorDialog progDlg;
	IProgressMonitor addFiles_monitor = null;

	private Composite hdr_dirs_comp;
	private Composite adv_options_comp;
	private Button show_btn;
	private Button hide_btn;
	private Composite show_hide_button_comp;
	private TabFolder tab_control;

	public BaselineEditor(Shell shell,String selectedProfile) {
		super(shell);
		setShellStyle(getShellStyle()|SWT.RESIZE);
		this.selectedProfile=selectedProfile;
	}
	@Override
	protected Control createContents(Composite parent) {
		shell = new Composite(parent,SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE );
		shell.setLayout(new GridLayout(4,false));
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));
		shell.getShell().setText(Messages.getString("BaselineEditor.BaselineEditor-CreateNew"));
		
		Group profileGrp=new Group(shell,SWT.NONE);
		profileGrp.setText("Profile");
		GridData profilegriddataGrp=new GridData(GridData.FILL_HORIZONTAL);
		profilegriddataGrp.horizontalSpan = 4;
		profileGrp.setLayoutData(profilegriddataGrp);
		profileGrp.setLayout(new GridLayout(4,false));
		
		Label profileLbl=new Label(profileGrp,SWT.NONE);
		profileLbl.setText(Messages.getString("BaselineEditor.ProfileName")); //$NON-NLS-1$
		GridData griddatalbl=new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		//griddatalbl.horizontalAlignment=GridData.BEGINNING;
		profileLbl.setLayoutData(griddatalbl);

		profileCmb = new Combo(profileGrp,SWT.DROP_DOWN);
		profileCmb.setToolTipText(Messages.getString("BaselineEditor.CreateProfileWithGivenName"));  
		GridData griddataNM=new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
		griddataNM.horizontalSpan=3;
		//griddataNM.horizontalAlignment=GridData.FILL;
		profileCmb.setLayoutData(griddataNM);
		profileCmb.addSelectionListener(this);

		String[] basics = BaselineProfileUtils.getAllBaselinesProfiles();
		if(basics!=null && basics.length > 0)
			profileCmb.setItems(basics);
		else
			shell.getShell().setText(Messages.getString("BaselineEditor.BaselineEditor-CreateNew")); //$NON-NLS-1$

		Label sdkLbl=new Label(profileGrp,SWT.NONE);
		sdkLbl.setText(Messages.getString("BaselineEditor.SDKName")); //$NON-NLS-1$

		GridData griddata=new GridData(GridData.FILL_HORIZONTAL);
		griddata.widthHint = 150;

		sdkCmb = new Combo(profileGrp,SWT.READ_ONLY|SWT.DROP_DOWN);
		sdkCmb.setToolTipText(Messages.getString("BaselineEditor.ReadBaselineFilesFromThisSDK")); //$NON-NLS-1$
		sdkCmb.setLayoutData(griddata);
		sdkCmb.addSelectionListener(this);

		Label versionLbl=new Label(profileGrp,SWT.NONE);
		versionLbl.setText(Messages.getString("BaselineEditor.S60VersionNumber")); //$NON-NLS-1$

		versionCmb = new Combo(profileGrp,SWT.NONE);
		versionCmb.setToolTipText(Messages.getString("BaselineEditor.tooltipOfVersionCombo"));  
		GridData gddata=new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
		gddata.widthHint = 90;
		versionCmb.setLayoutData(gddata);
		GlobalDataReader reader = CompatibilityAnalyserUtils.getGlobalDataReader();
		if(reader != null)
		{
			String[] sVersions = reader.readSupportedSDKVersions();
			if(sVersions!=null)
				versionCmb.setItems(sVersions);
		}
		versionCmb.addSelectionListener(this);

		Label sdkRoot=new Label(profileGrp,SWT.NONE);
		sdkRoot.setText(Messages.getString("BaselineEditor.SDKEpocRootDirectory")+" : "); //$NON-NLS-1$

		sdkrootLbl = new Label(profileGrp,SWT.NONE);
		GridData sdkrootgddata=new GridData(GridData.CENTER);
		sdkrootgddata.horizontalSpan=3;
		sdkrootgddata.horizontalAlignment=GridData.FILL;
		sdkrootLbl.setLayoutData(sdkrootgddata);

		sdkItems = CompatibilityAnalyserPlugin.fetchLoadedSdkList().toArray(new ISymbianSDK[0]);
		if(sdkItems.length == 0 && !BaselineProfileUtils.isPredefinedProfile(profileCmb.getText()))
		{
			MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.getString("BaselineEditor.BaselineEditor-SDKError"), Messages.getString("BaselineEditor.NoSDKinstalledinYourPC")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			sdkNames = new String[sdkItems.length];

			for(int k= 0; k<sdkItems.length; k++)
			{
				sdkNames[k] = sdkItems[k].getUniqueId();
			}
			sdkCmb.setItems(sdkNames);
		}

		GridData hdrData=new GridData(GridData.FILL_HORIZONTAL);
		hdrData.horizontalSpan = 4;
		infCheck = new Button(shell,SWT.CHECK);
		infCheck.setText("Use includes and forced headers from the selected bld.inf");
		infCheck.setToolTipText("Select baseline project to fill all the include paths automatically");
		infCheck.setLayoutData(hdrData);
		infCheck.addSelectionListener(this);
		
		hdrData = new GridData(GridData.FILL_HORIZONTAL);
		hdrData.horizontalSpan = 3;
		bldInfPath = new Combo(shell, SWT.READ_ONLY|SWT.DROP_DOWN|SWT.BORDER);
		bldInfPath.setLayoutData(hdrData);
		bldInfPath.setEnabled(false);
		SavingUserData userData = new SavingUserData();
		String [] infPaths = userData.getPreviousValues(SavingUserData.ValueTypes.BLDINF_PATH);
		if(infPaths != null && infPaths.length > 0){
			bldInfPath.setItems(infPaths);
			bldInfPath.select(0);
		}
		bldInfPath.addSelectionListener(this);
		
		browseBld = new Button(shell, SWT.PUSH);
		browseBld.setText("Browse...");
		browseBld.setEnabled(false);
		browseBld.addSelectionListener(this);
		
		tab_control = new TabFolder(shell,SWT.NONE);
		GridData tabGD=new GridData(GridData.FILL_HORIZONTAL);
		tabGD.horizontalSpan = 4;
		tab_control.setLayoutData(tabGD);

		TabItem headersTab = new TabItem(tab_control,SWT.NONE);
		headersTab.setText("  Headers  ");

		hdrGrp = new Group(tab_control,SWT.NONE);
		hdrGrp.setText(Messages.getString("BaselineEditor.Headers"));  
		GridData griddataGrp=new GridData(GridData.FILL_HORIZONTAL);
		griddataGrp.horizontalSpan=4;
		griddataGrp.horizontalAlignment=GridData.FILL;
		hdrGrp.setLayoutData(griddataGrp);
		hdrGrp.setLayout(new GridLayout(6,false));
		headersTab.setControl(hdrGrp);
		
		hdrData=new GridData(GridData.VERTICAL_ALIGN_END);
		hdrData.horizontalSpan=6;
		
		radio_default_Hdr = new Button(hdrGrp,SWT.RADIO);
		radio_default_Hdr.setText(Messages.getString("BaselineEditor.UseHeadersUnderInclude")); //$NON-NLS-1$
		radio_default_Hdr.setToolTipText(Messages.getString("BaselineEditor.ToolTipForRadioDefaultHdr")); //$NON-NLS-1$
		radio_default_Hdr.setLayoutData(hdrData);
		radio_default_Hdr.setSelection(true);
		radio_default_Hdr.addSelectionListener(this);

		radio_Hdr_dir = new Button(hdrGrp,SWT.RADIO);
		radio_Hdr_dir.setText(Messages.getString("BaselineEditor.UseHeadersUnderDir"));  
		radio_Hdr_dir.setToolTipText(Messages.getString("BaselineEditor.ToolTipForRadioHdrDir"));  
		radio_Hdr_dir.setLayoutData(hdrData);
		radio_Hdr_dir.setSelection(false);
		radio_Hdr_dir.addSelectionListener(this);

		hdr_dirs_comp = new Composite(hdrGrp,SWT.NONE);
		hdr_dirs_comp.setLayout(new GridLayout(2, false));
		hdrData = new GridData(GridData.FILL_HORIZONTAL);
		hdrData.horizontalSpan = 6;
		hdr_dirs_comp.setLayoutData(hdrData);
		
		hdr_dir_list = new List(hdr_dirs_comp,SWT.MULTI|SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
		hdr_dir_list.setToolTipText(Messages.getString("BaselineEditor.ToolTipForHdrDirCombo"));  
		GridData hdrListdata=new GridData(GridData.FILL_BOTH);
		hdrListdata.verticalSpan=4;
		hdrListdata.heightHint = 80;
		hdr_dir_list.setLayoutData(hdrListdata);
		hdr_dir_list.addSelectionListener(this);
		hdr_dir_list.setEnabled(false);

		GridData btnData1=new GridData(GridData.FILL);
		btnData1.widthHint=100;
		btnData1.verticalIndent=0;
		addBtn_hdrGrp = new Button(hdr_dirs_comp,SWT.PUSH);
		addBtn_hdrGrp.setText(Messages.getString("BaselineEditor.Browse"));  
		addBtn_hdrGrp.setLayoutData(btnData1);
		addBtn_hdrGrp.setEnabled(false);
		addBtn_hdrGrp.addSelectionListener(this);

		removeBtn_hdrGrp = new Button(hdr_dirs_comp,SWT.PUSH);
		removeBtn_hdrGrp.setText("Remove");
		removeBtn_hdrGrp.setLayoutData(btnData1);
		removeBtn_hdrGrp.setEnabled(false);
		removeBtn_hdrGrp.addSelectionListener(this);

		removeAllBtn_hdrGrp = new Button(hdr_dirs_comp,SWT.PUSH);
		removeAllBtn_hdrGrp.setText("Remove all");
		removeAllBtn_hdrGrp.setLayoutData(btnData1);
		removeAllBtn_hdrGrp.setEnabled(false);
		removeAllBtn_hdrGrp.addSelectionListener(this);

		hdrData = new GridData(GridData.FILL_HORIZONTAL|GridData.HORIZONTAL_ALIGN_END);
		hdrData.horizontalSpan = 5;
		Label show_hide_lbl = new Label(hdrGrp, SWT.NONE);
		show_hide_lbl.setText("Show/Hide options");
		show_hide_lbl.setLayoutData(hdrData);
		
		show_hide_button_comp = new Composite(hdrGrp, SWT.NONE);
		show_hide_button_comp.setLayout(new StackLayout());
		hdrData = new GridData(GridData.FILL);
		show_hide_button_comp.setLayoutData(hdrData);
		
		show_btn = new Button(show_hide_button_comp, SWT.ARROW|SWT.DOWN);
		show_btn.addSelectionListener(this);
		
		hide_btn = new Button(show_hide_button_comp, SWT.ARROW|SWT.UP);
		hide_btn.addSelectionListener(this);
		
		((StackLayout)show_hide_button_comp.getLayout()).topControl = hide_btn;
		show_hide_button_comp.layout();
		
		hdrData = new GridData(GridData.FILL_HORIZONTAL);
		hdrData.horizontalSpan = 6;
		adv_options_comp = new Composite(hdrGrp, SWT.NONE);
		adv_options_comp.setLayout(new GridLayout(1, true));
		adv_options_comp.setLayoutData(hdrData);
		
		hdrData = new GridData(GridData.FILL_HORIZONTAL);
		Group innerBtnGrp=new Group(adv_options_comp,SWT.NONE);
		innerBtnGrp.setText(Messages.getString("BaselineEditor.SystemIncludes"));  
		innerBtnGrp.setLayoutData(hdrData);
		innerBtnGrp.setLayout(new GridLayout(6,false));

		GridData innerData2=new GridData(GridData.FILL);
		innerData2.horizontalSpan=3;
		innerData2.widthHint=200;

		GridData innerData3=new GridData(GridData.FILL);
		innerData3.horizontalSpan=3;
		innerData3.widthHint=280;

		GridData list1data=new GridData(GridData.FILL_BOTH);
		list1data.verticalSpan=3;
		list1data.horizontalSpan=5;
		list_systemInc = new List(innerBtnGrp,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
		list1data.heightHint = 50;
		list1data.widthHint=300;
		list_systemInc.setLayoutData(list1data);
		list_systemInc.addSelectionListener(this);

		GridData btnData=new GridData(GridData.VERTICAL_ALIGN_CENTER);
		btnData.widthHint=100;
		addBtn_sysIncGrp = new Button(innerBtnGrp,SWT.PUSH);
		addBtn_sysIncGrp.setText(Messages.getString("BaselineEditor.AddIncludes"));  
		addBtn_sysIncGrp.setLayoutData(btnData);
		addBtn_sysIncGrp.addSelectionListener(this);

		removeBtn_sysIncGrp = new Button(innerBtnGrp,SWT.PUSH);
		removeBtn_sysIncGrp.setText(Messages.getString("BaselineEditor.Remove"));  
		removeBtn_sysIncGrp.setLayoutData(btnData);
		removeBtn_sysIncGrp.setEnabled(false);	
		removeBtn_sysIncGrp.addSelectionListener(this);

		removeAllBtn_sysIncGrp = new Button(innerBtnGrp,SWT.PUSH);
		removeAllBtn_sysIncGrp.setText("Remove all");  
		removeAllBtn_sysIncGrp.setLayoutData(btnData);
		removeAllBtn_sysIncGrp.setEnabled(false);
		removeAllBtn_sysIncGrp.addSelectionListener(this);

		forcedGrp = new Group(adv_options_comp,SWT.NONE);
		forcedGrp.setText("Forced headers");  
		hdrData=new GridData(GridData.FILL_HORIZONTAL);
		forcedGrp.setLayoutData(hdrData);
		forcedGrp.setLayout(new GridLayout(2,false));

		GridData forcedlist1data=new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_FILL|GridData.FILL_BOTH);
		forcedlist1data.verticalSpan=3;
		forced_hdrs_list = new List(forcedGrp,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
		forcedlist1data.heightHint = 50;
		forced_hdrs_list.setLayoutData(forcedlist1data);
		forced_hdrs_list.addSelectionListener(this);
		
		GridData forcedbtnData=new GridData(GridData.VERTICAL_ALIGN_CENTER);
		forcedbtnData.widthHint=100;
		forced_addBtn = new Button(forcedGrp,SWT.PUSH);
		forced_addBtn.setText("Add...");  
		forced_addBtn.setLayoutData(forcedbtnData);
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

		TabItem librariesTab = new TabItem(tab_control,SWT.NONE);
		librariesTab.setText("  Libraries  ");

		libGrp = new Group(tab_control,SWT.NONE);
		libGrp.setText(Messages.getString("BaselineEditor.Libraries"));  
		GridData libdata=new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_VERTICAL);
		libGrp.setLayoutData(libdata);

		libGrp.setLayout(new GridLayout(1,false));

		librariesTab.setControl(libGrp);

		GridData lib1data=new GridData(GridData.FILL_HORIZONTAL);
		
		Label selectBuild=new Label(libGrp,SWT.NONE);
		selectBuild.setText(Messages.getString("BaselineEditor.SelectBuildConfiguration")); //$NON-NLS-1$
		selectBuild.setLayoutData(lib1data);

		radio_default_build_target = new Button(libGrp, SWT.RADIO);
		radio_default_build_target.setText(Messages.getString("BaselineEditor.UseLibraryUnderDefaultBuildTarget"));  
		radio_default_build_target.setToolTipText(Messages.getString("BaselineEditor.ToolTipForRadioDefaultBuildTarget"));  
		radio_default_build_target.setLayoutData(lib1data);
		radio_default_build_target.addSelectionListener(this);
		radio_default_build_target.setSelection(true);
		
		radio_build_target = new Button(libGrp,SWT.RADIO);
		radio_build_target.setText(Messages.getString("BaselineEditor.UseLibraryUnderBuildConfig"));  
		radio_build_target.setToolTipText(Messages.getString("BaselineEditor.ToolTipForRadioDefaultLibs"));  
		radio_build_target.setLayoutData(lib1data);
		radio_build_target.addSelectionListener(this);
		radio_build_target.setSelection(false);

		list_build_Config = new List(libGrp,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
		GridData lib2data=new GridData(GridData.FILL_HORIZONTAL);
		lib2data.heightHint=50;
		list_build_Config.setLayoutData(lib2data);
		list_build_Config.addSelectionListener(this);
		
		String [] selectedItems = list_build_Config.getSelection();
		
		if(selectedItems != null && selectedItems.length > 0){
			
			if(selectedItems.length == 1)
				radio_build_target.setText(Messages.getString("BaselineEditor.UseLibrariesUnder")+ selectedItems[0] + Messages.getString("BaselineEditor.BuildConfiguration")); //$NON-NLS-1$ //$NON-NLS-2$
			else
				radio_build_target.setText("Use libraries under selected build configurations");
		}

		radio_dir_Libs = new Button(libGrp,SWT.RADIO);
		radio_dir_Libs.setText(Messages.getString("BaselineEditor.SpecifyTheDirectory")); //$NON-NLS-1$
		radio_dir_Libs.setToolTipText(Messages.getString("BaselineEditor.ToolTipForRadioDirLibs")); //$NON-NLS-1$
		radio_dir_Libs.setLayoutData(lib1data);
		radio_dir_Libs.addSelectionListener(this);		

		dllPaths_Folder = new TabFolder(libGrp, SWT.NONE);
		dllPaths_Folder.setLayout(new GridLayout(2, false));
		dllPaths_Folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		dsoPaths_tab = new TabItem(dllPaths_Folder, SWT.NONE);
		dsoPaths_tab.setText("DSO Paths");
		
		Composite dsoComposite = new Composite(dllPaths_Folder, SWT.NONE);
		dsoComposite.setLayout(new GridLayout(2, false));
		dsoComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		GridData gridData =new GridData(GridData.FILL_HORIZONTAL);
		gridData.verticalSpan=4;
		gridData.heightHint=100;
		dso_dir_list = new List(dsoComposite, SWT.MULTI | SWT.BORDER| SWT.H_SCROLL | SWT.V_SCROLL);
		dso_dir_list.setLayoutData(gridData);
		dso_dir_list.addSelectionListener(this);
		
		GridData libsBtnData =new GridData(GridData.VERTICAL_ALIGN_CENTER);
		libsBtnData.widthHint=100;
		addDsoDir_btn = new Button(dsoComposite,SWT.PUSH);
		addDsoDir_btn.setText(Messages.getString("BaselineEditor.Browse"));  
		addDsoDir_btn.setLayoutData(libsBtnData);
		addDsoDir_btn.addSelectionListener(this);
		
		removeDsoDir_Btn = new Button(dsoComposite,SWT.PUSH);
		removeDsoDir_Btn.setText("Remove");
		removeDsoDir_Btn.setLayoutData(libsBtnData);
		removeDsoDir_Btn.setEnabled(false);
		removeDsoDir_Btn.addSelectionListener(this);

		removeAllDsoDirs_Btn = new Button(dsoComposite,SWT.PUSH);
		removeAllDsoDirs_Btn.setText("Remove all");
		removeAllDsoDirs_Btn.setLayoutData(libsBtnData);
		removeAllDsoDirs_Btn.setEnabled(false);
		removeAllDsoDirs_Btn.addSelectionListener(this);

		dsoPaths_tab.setControl(dsoComposite);
		
		dllPaths_tab = new TabItem(dllPaths_Folder, SWT.NONE);
		dllPaths_tab.setText("DLL Paths");
		
		Composite dllComposite = new Composite(dllPaths_Folder, SWT.NONE);
		dllComposite.setLayout(new GridLayout(2, false));
		dllComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		dll_dir_list = new List(dllComposite, SWT.MULTI | SWT.BORDER| SWT.H_SCROLL | SWT.V_SCROLL);
		dll_dir_list.setLayoutData(gridData);
		dll_dir_list.addSelectionListener(this);
		
		addDllDir_btn = new Button(dllComposite,SWT.PUSH);
		addDllDir_btn.setText(Messages.getString("BaselineEditor.Browse"));  
		addDllDir_btn.setLayoutData(libsBtnData);
		addDllDir_btn.addSelectionListener(this);
		
		removeDllDir_btn = new Button(dllComposite,SWT.PUSH);
		removeDllDir_btn.setText("Remove");
		removeDllDir_btn.setLayoutData(libsBtnData);
		removeDllDir_btn.setEnabled(false);
		removeDllDir_btn.addSelectionListener(this);

		removeAllDllDir_Btn = new Button(dllComposite,SWT.PUSH);
		removeAllDllDir_Btn.setText("Remove all");
		removeAllDllDir_Btn.setLayoutData(libsBtnData);
		removeAllDllDir_Btn.setEnabled(false);
		removeAllDllDir_Btn.addSelectionListener(this);
		
		dllPaths_tab.setControl(dllComposite);
		
		//set icon image for the dialog
		shell.getShell().setImage(ImageResourceManager.getImage(ImageKeys.BASELINE_EDITOR_ICON));

		profileCmb.addModifyListener(this);
		
		shell.getShell().setSize(550,700);
		setDialogHelpAvailable(false);
		setHelpAvailable(true);
		setHelp();
		return super.createContents(shell);
	}

	protected void createButtonsForButtonBar(Composite parent) {

		saveBtn=this.createButton(parent, IDialogConstants.OK_ID, Messages.getString("BaselineEditor.SaveOK"), true); //$NON-NLS-1$
		saveBtn.addSelectionListener(this);
		deleteBtn=this.createButton(parent, IDialogConstants.IGNORE_ID, Messages.getString("BaselineEditor.Delete"), false); //$NON-NLS-1$
		deleteBtn.addSelectionListener(this);
		cancelBtn=this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("BaselineEditor.Cancel"), false); //$NON-NLS-1$
		cancelBtn.addSelectionListener(this);

		//It shows the information of the profile using the given profile name.
		//If that profile name is not valid then it is deleted from the list.

		if(selectedProfile!=null)
		{
			openBaselineProfileIfExists(selectedProfile);
		}
		else
		{
			openBaselineProfileIfExists(profileCmb.getText());	
		}
		
		//Exclude hdr directories list
		GridData d = (GridData)hdr_dirs_comp.getLayoutData();
		d.exclude = !radio_Hdr_dir.getSelection();
		hdr_dirs_comp.setVisible(radio_Hdr_dir.getSelection());
		
		//Exclude list of build targets for the first time
		GridData data = (GridData)list_build_Config.getLayoutData();
		data.exclude = !radio_build_target.getSelection();
		list_build_Config.setVisible(radio_build_target.getSelection());
		
		//Exlude dll/dso path tabs for the first time
		GridData exData = (GridData)dllPaths_Folder.getLayoutData();
		exData.exclude = !radio_dir_Libs.getSelection();
		dllPaths_Folder.setVisible(radio_dir_Libs.getSelection());
		
		if(hdrGrp!=null && shell !=null){
			hdrGrp.layout();
			shell.layout();
		}
	}
	
	protected void buttonPressed(int buttonId) {
		if(buttonId == IDialogConstants.OK_ID)
		{
			setReturnCode(IDialogConstants.OK_ID);
		}
		else if(buttonId == IDialogConstants.CANCEL_ID)
		{
			setReturnCode(IDialogConstants.CANCEL_ID);
		}
		else if(buttonId == IDialogConstants.IGNORE_ID)
		{
			//For Delete button, IGNORE_ID is been used.
			setReturnCode(IDialogConstants.IGNORE_ID);
		}
	}
	
	public boolean close()
	{
		try{
			if(selectedProj != null && selectedProj.exists() && projDeletionReq)
			{
				selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
				projDeletionReq = false;
			}
			}catch(Exception ex){
				ex.printStackTrace();
			}

		return super.close();
	}
	/**
	 * Opens the profile in editor with given profilename.
	 * @param name of the profile
	 * @return true if exists
	 */
	public boolean openBaselineProfileIfExists(String name)
	{
		boolean val=true;
		SavingUserData prevData=new SavingUserData();;
		if(name != null && name.length()!=0)
		{
			Object obj = BaselineProfileUtils.getBaselineProfileData(name);
			if(obj != null && obj instanceof BaselineProfile)
				restoreProfile((BaselineProfile)obj);										
			else
				prevData.deleteProfile(name);
		}
		else
		{
			clearAllShowDefaultEditor();
			val = false;
		}
		return val;
	}
	
	private void clearAllShowDefaultEditor()
	{
		//Set empty text
		profileCmb.setText("");
		
		//Select SDK, version, epoc root
		if(sdkNames.length!=0)
		{
			sdkCmb.select(0);
			if(versionCmb.indexOf(sdkItems[0].getSDKVersion().toString().substring(0, 3))!=-1)
				versionCmb.select(versionCmb.indexOf(sdkItems[0].getSDKVersion().toString().substring(0, 3)));
			else
				versionCmb.setText("");
			sdkrootLbl.setText(sdkItems[0].getEPOCROOT());
		}
		
		//bld inf option
		infCheck.setSelection(false);
		bldInfPath.setText("");
		infCheck.notifyListeners(SWT.Selection, new Event());
		
		//Headers tab
		//Select default header radio option
		radio_default_Hdr.setSelection(true);
		radio_default_Hdr.notifyListeners(SWT.Selection, new Event());
		hdr_dir_list.removeAll();
		radio_Hdr_dir.setSelection(false);
		radio_Hdr_dir.notifyListeners(SWT.Selection, new Event());
		
		//System includes
		removeAllBtn_sysIncGrp.notifyListeners(SWT.Selection, new Event());
		
		//Forced headers
		forced_removeAllBtn.notifyListeners(SWT.Selection, new Event());
		
		//Libraries 
		//build target list (2nd option)
		String[] targets = CompatibilityAnalyserUtils.getPlatformList(sdkItems[sdkCmb.getSelectionIndex()]);
		if(targets != null)
			list_build_Config.setItems(targets);	
		int i = list_build_Config.indexOf(ProductSdkData.DEFAULT_TARGET_PLATFORM);
		if(i != -1)
			list_build_Config.select(i);
		else
			list_build_Config.select(0);
		//Clear all DLL & DSO list (3rd option)
		removeAllDllDir_Btn.notifyListeners(SWT.Selection, new Event());
		removeAllDsoDirs_Btn.notifyListeners(SWT.Selection, new Event());
		//First option
		radio_default_build_target.setSelection(true);
		radio_default_build_target.notifyListeners(SWT.Selection, new Event());
		
		deleteBtn.setEnabled(false);
	}
	
	/**
     *This displays the details of the given profile object in the editor. 
	 */
	public void restoreProfile(BaselineProfile profile)
	{		
		if(profile==null)
			return;
		
		//Clear and show the default values in the editor
		clearAllShowDefaultEditor();
		
		//Set Profile name
		profileCmb.setText(profile.getProfileName());		
		
		if(profile.isPredefined())
		{
			if(sdkCmb.getItemCount() != 0)
				sdkCmb.select(0);
			sdkrootLbl.setText(profile.getSdkEpocRoot());
			versionCmb.setText(profile.getS60version());
			
			list_build_Config.removeAll();
			list_build_Config.add("armv5");
			
			radio_default_Hdr.setSelection(profile.isRadio_default_hdr());
			radio_default_Hdr.notifyListeners(SWT.Selection, new Event());
			radio_Hdr_dir.setSelection(!profile.isRadio_default_hdr());
			radio_Hdr_dir.notifyListeners(SWT.Selection, new Event());
			
			setEditorEnabled(false);
			
			return;
		}
	
		if(sdkCmb.indexOf(profile.getSdkName()) != -1)
		{		
			sdkCmb.select(sdkCmb.indexOf(profile.getSdkName()));
			versionCmb.setText(profile.getS60version());
			sdkrootLbl.setText(profile.getSdkEpocRoot());
			String [] platformsList = CompatibilityAnalyserUtils.getPlatformList(sdkItems[sdkCmb.getSelectionIndex()]);
			if(platformsList != null)
				list_build_Config.setItems(platformsList);
		}
		
		radio_default_Hdr.setSelection(profile.isRadio_default_hdr());
		radio_default_Hdr.notifyListeners(SWT.Selection, new Event());
		radio_Hdr_dir.setSelection(!profile.isRadio_default_hdr());
		radio_Hdr_dir.notifyListeners(SWT.Selection, new Event());
		hdr_dir_list.setEnabled(!profile.isRadio_default_hdr());
		if(hdr_dir_list.getEnabled())
			hdr_dir_list.setItems(profile.getHdr_dir_path());
		addBtn_hdrGrp.setEnabled(!profile.isRadio_default_hdr());
		removeBtn_hdrGrp.setEnabled(!profile.isRadio_default_hdr());
		removeAllBtn_hdrGrp.setEnabled(!profile.isRadio_default_hdr());
	
		if(profile.getSystemInc_dirs() != null)
		{
			list_systemInc.setItems(profile.getSystemInc_dirs());
			list_systemInc.select(0);
		}
		
		if(profile.getForced_headers()!=null)
		{
			forced_hdrs_list.setItems(profile.getForced_headers());
			forced_hdrs_list.select(0);
		}
		
		if(profile.isRadio_default_build_target())
		{
			//Do nothing
		}
		else if(profile.isRadio_build_target())
		{
			//set first option to false
			radio_default_build_target.setSelection(false);
			radio_default_build_target.notifyListeners(SWT.Selection, new Event());
			
			//Select 2nd option
			radio_build_target.setSelection(true);
			radio_build_target.notifyListeners(SWT.Selection, new Event());
			
			//Filter valid targets from previous list of targets
			ArrayList<Integer> targetIndices = new ArrayList<Integer>();
			if(profile.getBuild_config() != null)
				for(String s:profile.getBuild_config())
				{
					int index = list_build_Config.indexOf(s);
					if(index != -1)
						targetIndices.add(index);
				}
			//Set the filtered list
			int[] indices = new int[targetIndices.size()];
			for(int i=0; i<targetIndices.size(); i++)
				indices[i] = targetIndices.get(i);
			list_build_Config.select(indices);
			list_build_Config.notifyListeners(SWT.Selection, new Event());
		}
		else
		{
			radio_dir_Libs.setSelection(true);
			radio_dir_Libs.notifyListeners(SWT.Selection, new Event());
			if(profile.getLib_dir()!=null)
				dso_dir_list.setItems(profile.getLib_dir());
			if(profile.getDll_dir()!=null)
				dll_dir_list.setItems(profile.getDll_dir());
			dso_dir_list.select(0);
			dll_dir_list.select(0);
		}
		updateButtons();
	}
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * To check the details entered by the user are correct or not.
	 * @return
	 */
	public boolean isProfileDataCorrect()
	{
		boolean value=true;


		if(sdkCmb.getItemCount() == 0)
		{
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Warning"), Messages.getString("BaselineEditor.NOSDKsInstalledError"));			 //$NON-NLS-1$ //$NON-NLS-2$
			value=false;			
		}
		else if(sdkCmb.getText().length() == 0)
		{
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Warning"), "No SDK selected. Please select a SDK from the list");			 //$NON-NLS-1$ //$NON-NLS-2$
			value=false;			
		}
		else if(versionCmb.getText().length()==0)
		{
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Warning"), Messages.getString("BaselineEditor.SelectS60Version"));
			value=false;
		}
		else if(radio_Hdr_dir.getSelection()==true && hdr_dir_list.getItemCount()==0)
		{
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Warning"), Messages.getString("BaselineEditor.InvalidHdrDirectory"));
			value=false;
		}
		else if(radio_build_target.getSelection() && list_build_Config.getSelectionCount()==0)
		{
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Warning"), Messages.getString("BaselineEditor.SelectBuildConfig"));
			value=false;
		}
		else if(radio_dir_Libs.getSelection()==true && dso_dir_list.getItemCount() ==0)
		{
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Warning"), Messages.getString("BaselineEditor.InvalidLibraryDirectory"));
			value=false;
		}
		else if(radio_dir_Libs.getSelection()==true && dll_dir_list.getItemCount() ==0)
		{
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Warning"), Messages.getString("BaselineEditor.InvalidDllDirectory"));
			value=false;
		}
		return value;
	}

	/**
	 * To check the given path is valid or not
	 * @param path
	 * @return
	 */
	public boolean isCorrectPath(String path)
	{
		boolean val=true;

		Path temp=new Path(path);

		if(!temp.toFile().isDirectory())
			val=false;

		return val;
	}

	/**
	 * It retrieves all the details entered by the user. And stores them in local file system using
	 * seriealization.
	 *
	 */
	public void saveProfile()
	{
		BaselineProfile profile=new BaselineProfile();

		if(profileCmb.getText().length()==0)
		{
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Warning"), Messages.getString("BaselineEditor.InvalidProfileName")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		profile.setProfileName(profileCmb.getText());
		profile.setSdkName(sdkCmb.getText());
		profile.setS60version(versionCmb.getText());

		profile.setSdkEpocRoot(sdkItems[sdkCmb.getSelectionIndex()].getEPOCROOT());

		if(radio_default_Hdr.getSelection()==true)
		{
			profile.setRadio_default_hdr(radio_default_Hdr.getSelection());
		}
		else
		{
			profile.setRadio_default_hdr(false);
			profile.setRadio_dir_hdr(radio_Hdr_dir.getSelection());
			profile.setHdr_dir_path(hdr_dir_list.getItems());			
		}

		if(list_systemInc.getItemCount() > 0)
			profile.setSystemInc_dirs(list_systemInc.getItems());

		if(forced_hdrs_list.getItemCount()>0)
			profile.setForced_headers(forced_hdrs_list.getItems());
		
		if(radio_default_build_target.getSelection())
		{
			profile.setRadio_default_build_target(true);
			profile.setRadio_build_target(false);
			profile.setRadio_dir_libs(false);
		}
		else if(radio_build_target.getSelection())
		{
			profile.setRadio_build_target(true);
			profile.setRadio_default_build_target(false);
			profile.setRadio_dir_libs(false);
			
			int selected_platforms = list_build_Config.getSelectionCount();
			
			if(selected_platforms > 0)
			{
				profile.setBuild_config(list_build_Config.getSelection());
				ISymbianSDK selectedSdk = CompatibilityAnalyserPlugin.fetchLoadedSdkList().get(sdkCmb.getSelectionIndex());
				
				String [] lib_dirs = new String[selected_platforms];
				String [] dll_dirs = new String[selected_platforms];
				
				for(int i = 0; i<selected_platforms; i++)
				{
					lib_dirs[i] = CompatibilityAnalyserEngine.getLibsPathFromPlatform(selectedSdk, profile.getBuild_config()[i]);
					dll_dirs[i] = CompatibilityAnalyserEngine.getDllPathFromPlatform(selectedSdk, profile.getBuild_config()[i]);
				}
				profile.setLib_dir(lib_dirs);
				profile.setDll_dir(dll_dirs);
			}
		}
		else
		{
			profile.setRadio_default_build_target(false);
			profile.setRadio_build_target(false);
			profile.setRadio_dir_libs(radio_dir_Libs.getSelection());
			profile.setLib_dir(dso_dir_list.getItems());
			profile.setDll_dir(dll_dir_list.getItems());
		}

		BaselineProfileUtils.saveProfileOnFileSystem(profile);		
	}

	public void widgetSelected(SelectionEvent e) {
		
		if(e.widget==saveBtn)
		{
			if(profileCmb.getText().length()==0)
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.getString("BaselineEditor.Error"), Messages.getString("BaselineEditor.ProfileNameMustNotBeEmpty")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			
			String profileName = profileCmb.getText();
			Object profile = BaselineProfileUtils.getBaselineProfileData(profileName);
			if(profile instanceof BaselineProfile && ((BaselineProfile)profile).isPredefined())
			{
				prevData.saveValue(SavingUserData.ValueTypes.PROFILENAME, profileCmb.getText());
				shell.getParent().dispose();
				return;
			}
				
			if(isProfileDataCorrect())
			{
				if(profileCmb.indexOf(profileCmb.getText())!=-1)
				{
					String[] str={Messages.getString("BaselineEditor.Yes"),Messages.getString("BaselineEditor.69"),Messages.getString("BaselineEditor.No")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					MessageDialog dlg=new MessageDialog(Display.getCurrent().getActiveShell(),Messages.getString("BaselineEditor.Confirmation"),null,Messages.getString("BaselineEditor.ThisProfileAlreadyExists"),MessageDialog.QUESTION,str,0); //$NON-NLS-1$ //$NON-NLS-2$
					dlg.create();
					int res=dlg.open();
					if(res==0)
					{
						saveProfile();
						prevData.saveValue(SavingUserData.ValueTypes.PROFILENAME, profileCmb.getText());
					}
				}
				else
				{
					saveProfile();
					prevData.saveValue(SavingUserData.ValueTypes.PROFILENAME, profileCmb.getText());
				}
				
				//Delete the project, if the profile is created by using blf.inf 
				try{
					if(selectedProj != null){
						prevData.saveValue(SavingUserData.ValueTypes.BLDINF_PATH, bldInfPath.getText());
						if(selectedProj.exists() && projDeletionReq){
							selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
							projDeletionReq = false;
						}
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
				shell.getParent().dispose();
			}			
		}
		else if(e.widget==deleteBtn)
		{
			String[] str={Messages.getString("BaselineEditor.Yes"),Messages.getString("BaselineEditor.No2"),Messages.getString("BaselineEditor.Cancel")};
			MessageDialog dlg=new MessageDialog(Display.getCurrent().getActiveShell(),Messages.getString("BaselineEditor.Confirmation"),null,Messages.getString("BaselineEditor.DoUWantToDeleteTheProfile")+profileCmb.getText()+"'?",MessageDialog.QUESTION,str,0);
			dlg.create();
			int res=dlg.open();
			if(res==0)
			{
				BaselineProfileUtils.deleteProfile(profileCmb.getText());
				shell.getParent().dispose();
			}
		}
		else if(e.widget==cancelBtn)
		{
			try{
				if(selectedProj != null && selectedProj.exists() && projDeletionReq)
				{
					selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
					projDeletionReq = false;
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			shell.getParent().dispose();
		}
		else if(e.widget==profileCmb)
		{
			Object obj = BaselineProfileUtils.getBaselineProfileData(profileCmb.getText());
			if(obj instanceof BaselineProfile)
			{
				BaselineProfile pro = (BaselineProfile)obj;
				if(!pro.isPredefined())
					openBaselineProfileIfExists(profileCmb.getText());
				else
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
							openBaselineProfileIfExists(profileCmb.getText());
							prevData.saveValue(SavingUserData.ValueTypes.PROFILENAME, profile.getProfileName());
						}
						else
						{
							openBaselineProfileIfExists(null);
							MessageDialog.openError(this.getShell(), "Compatibility Analyser", downloadStatus);
							downloadStatus=null;    				
						}
					}
					openBaselineProfileIfExists(pro.getProfileName());
				}
			}
			hdrGrp.layout();
			shell.layout();
		}
		else if(e.widget==sdkCmb)
		{
			sdkrootLbl.setText(sdkItems[sdkCmb.getSelectionIndex()].getEPOCROOT());
			if(versionCmb.indexOf(sdkItems[sdkCmb.getSelectionIndex()].getSDKVersion().toString().substring(0, 3))!=-1)
				versionCmb.select(versionCmb.indexOf(sdkItems[sdkCmb.getSelectionIndex()].getSDKVersion().toString().substring(0, 3)));
			else
				versionCmb.setText(""); //$NON-NLS-1$

			String [] platformsList = CompatibilityAnalyserUtils.getPlatformList(sdkItems[sdkCmb.getSelectionIndex()]);

			if(platformsList != null)
				list_build_Config.setItems(platformsList);

			if(list_build_Config.getItemCount() > 0)
			{
				int i = list_build_Config.indexOf(Messages.getString("BaselineEditor.armv5")); //$NON-NLS-1$
				if(i != -1)
					list_build_Config.select(i);
				else
					list_build_Config.select(0);

				String selected=list_build_Config.getItem(list_build_Config.getSelectionIndex());
				radio_build_target.setText(Messages.getString("BaselineEditor.UseLibrariesUnder")+ selected + Messages.getString("BaselineEditor.BuildConfig")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if(infCheck.getSelection() && bldInfPath.getText().length() != 0 )
			{
				String bldPath = bldInfPath.getText();

				File bldFile = new File(bldPath);
				File Project = bldFile.getParentFile().getParentFile();

				userFiles = new ArrayList<File>();
				systemFiles = new ArrayList<File>();

				projName = Project.getName();
				projLocation = Project.getAbsolutePath();

				projExists = false;

				selectedSdk = sdkItems[sdkCmb.getSelectionIndex()];

				IRunnableWithProgress runnable = new IRunnableWithProgress()
				{
					public void run(IProgressMonitor monitor)
					{
						try{

							isCancelled = false;
							selectedProj = ResourcesPlugin.getWorkspace().getRoot().getProject("CompatibilityAnalyser_" + projName);
							if(selectedProj.exists()){
								projExists = true;
							}
							else
							{
								for(IProject wsProj:ResourcesPlugin.getWorkspace().getRoot().getProjects())
								{
									projLocation = FileMethods.convertForwardToBackwardSlashes(projLocation);
									if(FileMethods.convertForwardToBackwardSlashes(wsProj.getLocation().toString()).equals(projLocation))
									{
										projExists = true;
										selectedProj = wsProj;
										break;
									}
								}
							}

							java.util.List<ISymbianBuildContext> buildList = selectedSdk.getFilteredBuildConfigurations();

							if(projExists)
							{
								monitor.beginTask("Reading system includes using selected SDK Configuration ...", 10);
								selectedProj.open(null);
								ICarbideBuildManager buildMgr = CarbideBuilderPlugin.getBuildManager();
								ICarbideProjectModifier modifier = buildMgr.getProjectModifier(selectedProj);

								monitor.worked(2);

								if(monitor.isCanceled())
								{
									isCancelled = true;
									monitor.done();
									return;
								}
								ICarbideProjectInfo projectInfo = buildMgr.getProjectInfo(selectedProj);
								ICarbideBuildConfiguration origConf = projectInfo.getDefaultConfiguration();

								ICarbideBuildConfiguration configTobeSet=null;
								configTobeSet = modifier.createNewConfiguration(buildList.get(0), true);

								configTobeSet.saveConfiguration(false);
								modifier.setDefaultConfiguration(configTobeSet);
								modifier.saveChanges();

								monitor.worked(5);
								if(monitor.isCanceled()){
									modifier.setDefaultConfiguration(origConf);
									modifier.saveChanges();
									monitor.done();

									return;
								}

								if(projectInfo != null)
								{
									EpocEngineHelper.getProjectIncludePaths(projectInfo, projectInfo.getDefaultConfiguration(), userFiles, systemFiles);
									getExportPathsAndFiles(projectInfo, userFiles);
									srcRoots = CompatibilityAnalyserEngine.getIncludesFromSrcFiles(selectedProj).toArray(new String[0]);
								}

								monitor.worked(8);
								modifier.setDefaultConfiguration(origConf);
								modifier.saveChanges();

								monitor.worked(10);
								if(monitor.isCanceled())
									isCancelled = true;
								monitor.done();
							}
							else
							{
								monitor.beginTask("Getting system includes using selected SDK Configuration...", 10);
								selectedProj = ProjectCorePlugin.createProject(projName, projLocation);
								projDeletionReq = true;

								monitor.worked(2);

								if(monitor.isCanceled())
								{
									isCancelled = true;
									if(selectedProj != null && selectedProj.exists()){
										try{
											selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
										}catch(Exception e1){
											e1.printStackTrace();
										}
									}	
									monitor.done();
									projDeletionReq = false;		
									return;
								}
								java.util.List<String> infsList = new ArrayList<String>();

								ProjectCorePlugin.postProjectCreatedActions(selectedProj, "group/bld.inf", buildList, infsList,null, null, new NullProgressMonitor());
								monitor.worked(5);
								if(monitor.isCanceled())
								{
									isCancelled = true;
									if(selectedProj != null && selectedProj.exists()){
										try{
											selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
										}catch(Exception e1){
											e1.printStackTrace();
										}
									}	
									monitor.done();
									projDeletionReq = false;		
									return;
								}
								ICarbideProjectInfo projInfo = CarbideBuilderPlugin.getBuildManager().getProjectInfo(selectedProj);

								if(projInfo != null)
								{
									EpocEngineHelper.getProjectIncludePaths(projInfo, projInfo.getDefaultConfiguration(),
											userFiles, systemFiles);
									getExportPathsAndFiles(projInfo, userFiles);
									monitor.worked(7);
									srcRoots = CompatibilityAnalyserEngine.getIncludesFromSrcFiles(selectedProj).toArray(new String[0]);

									monitor.worked(9);
									if(monitor.isCanceled())
										isCancelled = true;
								}
								monitor.done();
							}

						}catch(CoreException ex){
							ex.printStackTrace();
							try{
								if(selectedProj != null && selectedProj.exists() && projDeletionReq){
									selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
									projDeletionReq = false;
								}
								return;
							}catch(Exception e1){
								e1.printStackTrace();
							}
						}
					}

				};

				try
				{
					new ProgressMonitorDialog(this.shell.getShell()).run(true, true, runnable);
				}catch(Exception ex){
					ex.printStackTrace();
				}

				String [] sysIncludes = new String [systemFiles.size()];
				int s = 0;

				if(isCancelled)
					return;

				for(File f:systemFiles){
					sysIncludes[s] = f.toString();
					s++;
				}
				if(srcRoots != null && srcRoots.length > 0)
				{
					forced_hdrs_list.setItems(srcRoots);
				}

				if(userFiles.size() > 0)
				{
					ArrayList<String> userIncludes = new ArrayList<String>();
					
					for(int i=0; i<userFiles.size(); i++)
					{
						if(userFiles.get(i).exists())
							userIncludes.add(FileMethods.convertForwardToBackwardSlashes(userFiles.get(i).toString()));
					}
					Event modEvent = new Event();
					modEvent.type = SWT.Selection;
					radio_Hdr_dir.setSelection(true);
					hdr_dir_list.setItems(userIncludes.toArray(new String[0]));
					hdr_dir_list.select(0);
					radio_Hdr_dir.notifyListeners(SWT.Selection, modEvent);

				}
				if(sysIncludes.length >0)
				{
					list_systemInc.setItems(sysIncludes);
					removeBtn_sysIncGrp.setEnabled(true);
					removeAllBtn_sysIncGrp.setEnabled(true);
					list_systemInc.select(0);
				}
				updateButtons();
				return;
			}
		}
		else if(e.widget==addBtn_hdrGrp)
		{
			DirectoryDialog dirDlg=new DirectoryDialog(Display.getDefault().getActiveShell());
			String dirName=null;
			if((dirName=dirDlg.open())!=null)
			{
				hdr_dir_list.add(dirName);
				hdr_dir_list.select(0);
				updateButtons();
			}
			else
				return;

		}
		else if(e.widget==removeBtn_hdrGrp)
		{
			hdr_dir_list.remove(hdr_dir_list.getSelectionIndices());
			hdr_dir_list.select(0);
			updateButtons();
		}
		else if(e.widget == removeAllBtn_hdrGrp)
		{
			hdr_dir_list.removeAll();
			updateButtons();			
		}
		else if(e.widget==addDsoDir_btn)
		{
			DirectoryDialog dirDlg=new DirectoryDialog(Display.getDefault().getActiveShell());
			String dirName=null;
			if((dirName=dirDlg.open())!=null)
			{
				dso_dir_list.add(dirName);
				dso_dir_list.select(0);
				updateButtons();
			}
			else
				return;
		}
		else if(e.widget == addDllDir_btn)
		{
			DirectoryDialog dirDlg=new DirectoryDialog(Display.getDefault().getActiveShell());
			String dirName=null;
			if((dirName=dirDlg.open())!=null)
			{
				dll_dir_list.add(dirName);
				dll_dir_list.select(0);
				updateButtons();
			}
			else
				return;
		}
		else if(e.widget==removeDsoDir_Btn)
		{
			dso_dir_list.remove(dso_dir_list.getSelectionIndices());
			dso_dir_list.select(0);
			updateButtons();
		}
		else if(e.widget == removeDllDir_btn)
		{
			dll_dir_list.remove(dll_dir_list.getSelectionIndices());
			dll_dir_list.select(0);
			updateButtons();
		}
		else if(e.widget == removeAllDsoDirs_Btn)
		{
			dso_dir_list.removeAll();
			updateButtons();			
		}
		else if(e.widget == removeAllDllDir_Btn)
		{
			dll_dir_list.removeAll();
			updateButtons();
		}
		else if(e.widget==addBtn_sysIncGrp)
		{
			DirectoryDialog dirDlg=new DirectoryDialog(Display.getDefault().getActiveShell());
			String dirName=null;
			if((dirName=dirDlg.open())!=null)
			{
				list_systemInc.add(dirName);
				list_systemInc.select(0);
				updateButtons();
			}
			else
				return;

		}
		else if(e.widget==removeBtn_sysIncGrp)
		{
			list_systemInc.remove(list_systemInc.getSelectionIndices());
			list_systemInc.select(0);
			updateButtons();
		}
		else if(e.widget== radio_Hdr_dir)
		{
			GridData d = (GridData)hdr_dirs_comp.getLayoutData();
			d.exclude = !radio_Hdr_dir.getSelection();
			hdr_dirs_comp.setVisible(radio_Hdr_dir.getSelection());
			
			hdr_dir_list.setEnabled(radio_Hdr_dir.getSelection());
			addBtn_hdrGrp.setEnabled(radio_Hdr_dir.getSelection());	
			updateButtons();
		}
		else if(e.widget==radio_dir_Libs || e.widget == radio_build_target)
		{
			//dso_dir_list.setEnabled(radio_dir_Libs.getSelection());
			
			addDsoDir_btn.setEnabled(radio_dir_Libs.getSelection());
			addDllDir_btn.setEnabled(radio_dir_Libs.getSelection());
			
			//removeDsoDir_Btn.setEnabled(radio_dir_Libs.getSelection());
			//removeAllDsoDirs_Btn.setEnabled(radio_dir_Libs.getSelection());
			//list_build_Config.setEnabled(!radio_dir_Libs.getSelection());
			
			GridData data = (GridData)list_build_Config.getLayoutData();
			data.exclude = !radio_build_target.getSelection();
			list_build_Config.setVisible(radio_build_target.getSelection());
			
			GridData data2 = (GridData)dllPaths_Folder.getLayoutData();
			data2.exclude = !radio_dir_Libs.getSelection();
			dllPaths_Folder.setVisible(radio_dir_Libs.getSelection());
			
			dllPaths_Folder.getParent().layout(false);
		
			updateButtons();
		}
		else if(e.widget==list_build_Config)
		{
			if(list_build_Config.getSelectionCount() > 1)
				radio_build_target.setText("Use libraries under selected build configurations");
			else
			{
				String selected=list_build_Config.getItem(list_build_Config.getSelectionIndex());
				radio_build_target.setText(Messages.getString("BaselineEditor.UseLibrariesUnder")+ selected+ Messages.getString("BaselineEditor.BuildConfig")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else if(e.widget==removeAllBtn_sysIncGrp)
		{
			list_systemInc.removeAll();
			updateButtons();
		}
		else if(e.widget == browseBld)
		{
			FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell());
			dialog.setFilterExtensions(new String[]{"*.inf"});
			String fileName = null;

			if((fileName = dialog.open())!=null)
			{
				bldInfPath.add(fileName, 0);
				bldInfPath.select(0);

				Event event = new Event();
				event.type = SWT.Selection;
				bldInfPath.notifyListeners(SWT.Selection, event);
			}
		}
		else if(e.widget == infCheck)
		{
			projExists = false;
			if(infCheck.getSelection())
			{
				bldInfPath.setEnabled(true);
				browseBld.setEnabled(true);

				if(bldInfPath.getText().length() > 0)
				{
					userFiles = new ArrayList<File>();
					systemFiles = new ArrayList<File>();

					bldPath = bldInfPath.getText();
					selectedSdk = sdkItems[sdkCmb.getSelectionIndex()];

					File bldFile = new File(bldPath);
					File Project = bldFile.getParentFile().getParentFile();

					if(!bldFile.exists())
					{
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
								Messages.getString("HeaderFilesPage.CompatibilityAnalyser"), "Given Bld.inf does not exist. Please provide a valid path");
						return;
					}

					projName = Project.getName();
					projLocation = Project.getAbsolutePath();

					IRunnableWithProgress runnable = new IRunnableWithProgress()
					{
						public void run(IProgressMonitor monitor)
						{
							try
							{
								isCancelled = false;
								selectedProj = ResourcesPlugin.getWorkspace().getRoot().getProject("CompatibilityAnalyser_"+ projName);
								if(selectedProj.exists()){
									projExists = true;
								}
								else {
									for(IProject wsProj:ResourcesPlugin.getWorkspace().getRoot().getProjects())
									{
										projLocation = FileMethods.convertForwardToBackwardSlashes(projLocation);
										if(FileMethods.convertForwardToBackwardSlashes(wsProj.getLocation().toString()).equals(projLocation))
										{
											projExists = true;
											selectedProj = wsProj;
											break;
										}
									}
								}

								java.util.List<ISymbianBuildContext> buildList = selectedSdk.getFilteredBuildConfigurations();
								java.util.List<String> infsList = new ArrayList<String>();

								if(!projExists)
								{
									monitor.beginTask("Getting Project Info", 10);
									selectedProj = ProjectCorePlugin.createProject("CompatibilityAnalyser_" + projName, projLocation);
									projDeletionReq = true;
									monitor.worked(2);

									if(monitor.isCanceled())
									{
										isCancelled = true;
										if(selectedProj != null && selectedProj.exists()){
											try{
												selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
											}catch(Exception e1){
												e1.printStackTrace();
											}
										}
										monitor.done();			    		    			
										projDeletionReq = false;		
										return;
									}

									ProjectCorePlugin.postProjectCreatedActions(selectedProj, "group/bld.inf", buildList, infsList,null, null, monitor);
									monitor.worked(5);

									if(monitor.isCanceled())
									{
										isCancelled = true;
										if(selectedProj != null && selectedProj.exists()){
											try{
												selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
											}catch(Exception e1){
												e1.printStackTrace();
											}
										}	
										monitor.done();
										projDeletionReq = false;
										return;
									}
									ICarbideProjectInfo projInfo = CarbideBuilderPlugin.getBuildManager().getProjectInfo(selectedProj);

									if(projInfo != null)
									{

										EpocEngineHelper.getProjectIncludePaths(projInfo, projInfo.getDefaultConfiguration(), userFiles, systemFiles);
										getExportPathsAndFiles(projInfo, userFiles);
										monitor.worked(7);
										srcRoots = CompatibilityAnalyserEngine.getIncludesFromSrcFiles(selectedProj).toArray(new String[0]);

										monitor.worked(9);
										if(monitor.isCanceled())
											isCancelled = true;
									}
									monitor.done();
								}
								else
								{
									monitor.beginTask("Reading Project Info..", 10);
									selectedProj.open(null);
									monitor.worked(2);

									if(monitor.isCanceled())
									{
										isCancelled = true;
										monitor.done();
										return;
									}
									ICarbideProjectInfo projInfo = CarbideBuilderPlugin.getBuildManager().getProjectInfo(selectedProj);

									ICarbideBuildManager manager=CarbideBuilderPlugin.getBuildManager();
									ICarbideProjectModifier modifier = manager.getProjectModifier(selectedProj);

									ICarbideBuildConfiguration origConf = projInfo.getDefaultConfiguration();

									ICarbideBuildConfiguration configTobeSet=null;
									java.util.List<ISymbianBuildContext> all= selectedSdk.getFilteredBuildConfigurations();
									configTobeSet = modifier.createNewConfiguration(all.get(0), true);
									configTobeSet.saveConfiguration(false);
									modifier.setDefaultConfiguration(configTobeSet);
									modifier.saveChanges();

									if(monitor.isCanceled())
									{
										isCancelled = true;
										modifier.setDefaultConfiguration(origConf);
										modifier.saveChanges();

										monitor.done();
										return;
									}
									monitor.worked(5);

									if(projInfo != null)
									{

										EpocEngineHelper.getProjectIncludePaths(projInfo, projInfo.getDefaultConfiguration(), userFiles, systemFiles);
										getExportPathsAndFiles(projInfo, userFiles);
										srcRoots = CompatibilityAnalyserEngine.getIncludesFromSrcFiles(selectedProj).toArray(new String[0]);
									}

									monitor.worked(8);
									modifier.setDefaultConfiguration(origConf);
									modifier.saveChanges();

									if(monitor.isCanceled())
										isCancelled = true;

									monitor.worked(10);

									monitor.done();
								}

							}catch(CoreException ex){
								ex.printStackTrace();
								try{
									if(selectedProj != null && selectedProj.exists() && projDeletionReq){
										selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
										projDeletionReq = false;
									}
									return;
								}catch(Exception e1){
									e1.printStackTrace();
								}
							}
						}

					};

					try
					{
						new ProgressMonitorDialog(this.shell.getShell()).run(true, true, runnable);
					}catch(Exception ex){
						ex.printStackTrace();
					}

					if(isCancelled)
						return;

					if(srcRoots != null && srcRoots.length > 0)
					{
						forced_hdrs_list.setItems(srcRoots);
					}

					if(systemFiles != null && systemFiles.size() > 0)
					{
						String [] sysIncludes = new String[systemFiles.size()];

						for(int i =0;i<systemFiles.size(); i++)
							sysIncludes[i] = systemFiles.get(i).toString();
							
						list_systemInc.setItems(sysIncludes);
						list_systemInc.select(0);
						removeBtn_sysIncGrp.setEnabled(true);
						removeAllBtn_sysIncGrp.setEnabled(true);
					}
					if(userFiles != null && userFiles.size() > 0){
						ArrayList<String> userIncludes = new ArrayList<String>();

						for(int i =0;i<userFiles.size(); i++)
						{
							if(userFiles.get(i).exists())
								userIncludes.add(FileMethods.convertForwardToBackwardSlashes(userFiles.get(i).toString()));
						}
						
						radio_default_Hdr.setSelection(false);
						radio_Hdr_dir.setSelection(true);
						hdr_dir_list.setItems(userIncludes.toArray(new String[0]));
						hdr_dir_list.select(0);
						Event selEvent = new Event();
						selEvent.type = SWT.Selection;
						radio_Hdr_dir.notifyListeners(SWT.Selection, selEvent);
					}
				}
			}

			else
			{
				bldInfPath.setEnabled(false);
				browseBld.setEnabled(false);

				if(bldInfPath.getText().length() > 0)
				{
					Event modEvent = new Event();
					modEvent.type = SWT.Selection;
					radio_default_Hdr.setSelection(true);
					radio_Hdr_dir.setSelection(false);
					list_systemInc.removeAll();
					hdr_dir_list.removeAll();
					forced_hdrs_list.removeAll();

					radio_default_Hdr.notifyListeners(SWT.Selection, modEvent);
				}
			}
		}
		else if(e.widget == bldInfPath)
		{
			radio_default_Hdr.setSelection(true);
			radio_Hdr_dir.setSelection(false);
			list_systemInc.removeAll();
			forced_hdrs_list.removeAll();
		
			Event modEvent = new Event();
			modEvent.type = SWT.Selection;
			radio_default_Hdr.notifyListeners(SWT.Selection, modEvent);
		
			bldPath = bldInfPath.getText();
			selectedSdk = sdkItems[sdkCmb.getSelectionIndex()];
			projExists = false;
			if(bldPath.length() >0 && infCheck.getSelection())
			{
				userFiles = new ArrayList<File>();
				systemFiles = new ArrayList<File>();

				File bldFile = new File(bldPath);
				File Project = bldFile.getParentFile().getParentFile();

				if(!bldFile.exists())
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
							Messages.getString("HeaderFilesPage.CompatibilityAnalyser"), "Given Bld.inf does not exist. Please provide a valid path");
					return;
				}
				if(!bldFile.getName().equalsIgnoreCase("bld.inf"))
				{
					return;
				}
				projName = Project.getName();
				projLocation = Project.getAbsolutePath();

				if(selectedProj != null && selectedProj.exists() && projDeletionReq)
				{
					try{
						selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
					}catch(Exception e1){
						e1.printStackTrace();
					}	  		

					projDeletionReq = false;
				}

				IRunnableWithProgress runP = new IRunnableWithProgress()
				{
					public void run(IProgressMonitor monitor)
					{
						try
						{

							isCancelled = false;

							selectedProj = ResourcesPlugin.getWorkspace().getRoot().getProject("CompatibilityAnalyser_"+ projName);


							if(selectedProj.exists()){
								projExists = true;
							}
							else {

								for(IProject wsProj:ResourcesPlugin.getWorkspace().getRoot().getProjects())
								{
									if(FileMethods.convertForwardToBackwardSlashes(wsProj.getLocation().toString()).equals(FileMethods.convertForwardToBackwardSlashes(projLocation)))
									{
										projExists = true;
										selectedProj = wsProj;
										break;
									}
								}
							}

							java.util.List<ISymbianBuildContext> buildList = selectedSdk.getFilteredBuildConfigurations();
							java.util.List<String> infsList = new ArrayList<String>();

							if(!projExists)
							{
								monitor.beginTask("Getting Project Info...", 10);
								selectedProj = ProjectCorePlugin.createProject("CompatibilityAnalyser_" + projName, projLocation);
								projDeletionReq = true;
								monitor.worked(2);

								if(monitor.isCanceled())
								{
									isCancelled = true;
									if(selectedProj != null && selectedProj.exists()){
										try{
											selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
										}catch(Exception e1){
											e1.printStackTrace();
										}
									}
									monitor.done();
									projDeletionReq = false;
									return;
								}

								ProjectCorePlugin.postProjectCreatedActions(selectedProj, "group/bld.inf", buildList, infsList,null, null, monitor);

								if(monitor.isCanceled())
								{
									isCancelled = true;
									if(selectedProj != null && selectedProj.exists()){
										try{
											selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
										}catch(Exception e1){
											e1.printStackTrace();
										}
									}	
									monitor.done();		    		    			
									projDeletionReq = false;

									return;
								}
								monitor.worked(5);
								ICarbideProjectInfo projInfo = CarbideBuilderPlugin.getBuildManager().getProjectInfo(selectedProj);

								if(projInfo != null)
								{

									EpocEngineHelper.getProjectIncludePaths(projInfo, projInfo.getDefaultConfiguration(), userFiles, systemFiles);
									getExportPathsAndFiles(projInfo, userFiles);
									monitor.worked(7);

									srcRoots = CompatibilityAnalyserEngine.getIncludesFromSrcFiles(selectedProj).toArray(new String[0]);

									monitor.worked(9);
									if(monitor.isCanceled())
										isCancelled = true;
								}
								monitor.done();
							}
							else
							{
								monitor.beginTask("Reading Project Info..", 10);
								selectedProj.open(null);
								monitor.worked(2);

								if(monitor.isCanceled())
								{
									isCancelled = true;
									monitor.done();
									return;
								}
								ICarbideProjectInfo projInfo = CarbideBuilderPlugin.getBuildManager().getProjectInfo(selectedProj);

								ICarbideBuildManager manager=CarbideBuilderPlugin.getBuildManager();
								ICarbideProjectModifier modifier = manager.getProjectModifier(selectedProj);

								ICarbideBuildConfiguration origConf = projInfo.getDefaultConfiguration();

								ICarbideBuildConfiguration configTobeSet=null;

								configTobeSet = modifier.createNewConfiguration(buildList.get(0), true);
								configTobeSet.saveConfiguration(false);
								modifier.setDefaultConfiguration(configTobeSet);
								modifier.saveChanges();

								if(monitor.isCanceled())
								{
									isCancelled = true;
									monitor.done();
									modifier.setDefaultConfiguration(origConf);
									modifier.saveChanges();

									return;
								}
								monitor.worked(5);
								if(projInfo != null)
								{
									EpocEngineHelper.getProjectIncludePaths(projInfo, projInfo.getDefaultConfiguration(), userFiles, systemFiles);
									getExportPathsAndFiles(projInfo, userFiles);
									srcRoots = CompatibilityAnalyserEngine.getIncludesFromSrcFiles(selectedProj).toArray(new String[0]);
								}
								if(monitor.isCanceled()){
									isCancelled = true;
									monitor.done();
								}
								monitor.worked(8);
								modifier.setDefaultConfiguration(origConf);
								modifier.saveChanges();
								monitor.worked(10);
								monitor.done();
							}

						}catch(CoreException ex){
							ex.printStackTrace();
							try{
								if(selectedProj != null && selectedProj.exists() && projDeletionReq){
									selectedProj.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
									projDeletionReq = false;
								}
								return;
							}catch(Exception e1){
								e1.printStackTrace();
							}
						}
					}	

				};
				try
				{
					new ProgressMonitorDialog(this.shell.getShell()).run(true, true, runP);
				}catch(Exception ex){
					ex.printStackTrace();
				}

				if(isCancelled)
					return;

				if(srcRoots != null && srcRoots.length > 0)
				{
					Event eve = new Event();
					eve.type = SWT.Selection;
					forced_hdrs_list.setItems(srcRoots);
				}
				if(userFiles != null && userFiles.size() > 0)
				{
					ArrayList<String> userIncludes = new ArrayList<String>();

					for(int i =0;i<userFiles.size(); i++)
					{
						if(userFiles.get(i).exists())
							userIncludes.add(FileMethods.convertForwardToBackwardSlashes(userFiles.get(i).toString()));
					}

					radio_default_Hdr.setSelection(false);
					radio_Hdr_dir.setSelection(true);
					hdr_dir_list.setItems(userIncludes.toArray(new String[0]));
					hdr_dir_list.select(0);
					Event selEvent = new Event();
					selEvent.type = SWT.Selection;
					radio_Hdr_dir.notifyListeners(SWT.Selection, selEvent);
				}

				if(systemFiles != null && systemFiles.size() > 0)
				{
					String [] sysIncludes = new String [systemFiles.size()];

					for(int i =0;i<systemFiles.size(); i++)
						sysIncludes[i] = systemFiles.get(i).toString();

					list_systemInc.setItems(sysIncludes);
					list_systemInc.select(0);
					removeBtn_sysIncGrp.setEnabled(true);
					removeAllBtn_sysIncGrp.setEnabled(true);
				}	
			}
		}
		else if(e.widget==forced_addBtn)
		{
			displayFiles = new ArrayList<String>();
			numOfFiles = new ArrayList<String>();
			children = new ArrayList<String>();
			subChildren = new ArrayList<String>();
			isMonitorCancelled = false;

			if(list_systemInc.getItemCount() > 0)
			{
				ArrayList<String> paths = new ArrayList<String>(Arrays.asList(list_systemInc.getItems()));
				paths.add(FileMethods.appendPathSeparator(sdkrootLbl.getText()) + "epoc32" + File.separator + "include" + File.separator);
				addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), forced_hdrs_list, this, "", true);
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
				addDialog = new ShowFilesListDialog(Display.getCurrent().getActiveShell(), forced_hdrs_list, this, FileMethods.appendPathSeparator(sdkrootLbl.getText()) + "epoc32" + File.separator + "include" + File.separator, true);
				absolutePath=FileMethods.appendPathSeparator(sdkrootLbl.getText()) + "epoc32" + File.separator + "include" + File.separator;
				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						try {
							addFiles_monitor = monitor;
							monitor.beginTask("Getting files from "+ absolutePath, 10);
							getFiles(absolutePath,monitor); 
							addFiles_monitor.worked(5);
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
				if(numOfFiles.size()!=forced_hdrs_list.getItemCount())
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
		else if(e.widget==forced_removeBtn)
		{
			forced_hdrs_list.remove(forced_hdrs_list.getSelectionIndices());
			forced_hdrs_list.select(0);
			
			forced_removeBtn.setEnabled(forced_hdrs_list.getItemCount() > 0);
			forced_removeAllBtn.setEnabled(forced_hdrs_list.getItemCount() > 0);
		}
		else if(e.widget==forced_removeAllBtn)
		{
			forced_hdrs_list.removeAll();
			forced_removeBtn.setEnabled(false);
			forced_removeAllBtn.setEnabled(false);
		}
		else if(e.widget == show_btn)
		{
			GridData data = (GridData)adv_options_comp.getLayoutData();
			data.exclude = false;
			adv_options_comp.setVisible(true);
			
			((StackLayout)show_hide_button_comp.getLayout()).topControl = hide_btn;
			show_hide_button_comp.layout();
		}
		else if(e.widget == hide_btn)
		{
			GridData data = (GridData)adv_options_comp.getLayoutData();
			data.exclude = true;
			adv_options_comp.setVisible(false);
			
			((StackLayout)show_hide_button_comp.getLayout()).topControl = show_btn;
			show_hide_button_comp.layout();
		}
		
		if(!shell.isDisposed()) {
			hdrGrp.layout();
			shell.layout();
		}
	}	
	private void updateButtons()
	{
		forced_removeBtn.setEnabled(forced_hdrs_list.getItemCount() > 0);
		forced_removeAllBtn.setEnabled(forced_hdrs_list.getItemCount() > 0);
		
		if(radio_Hdr_dir.getSelection())
		{
			removeBtn_hdrGrp.setEnabled(hdr_dir_list.getItemCount() > 0);
			removeAllBtn_hdrGrp.setEnabled(hdr_dir_list.getItemCount() > 0);
		}
		else
		{
			removeBtn_hdrGrp.setEnabled(radio_Hdr_dir.getSelection());
			removeAllBtn_hdrGrp.setEnabled(radio_Hdr_dir.getSelection());
		}
		
		removeBtn_sysIncGrp.setEnabled(list_systemInc.getItemCount() > 0);
		removeAllBtn_sysIncGrp.setEnabled(list_systemInc.getItemCount() > 0);
		
		removeDsoDir_Btn.setEnabled(radio_dir_Libs.getSelection() && dso_dir_list.getItemCount() > 0);
		removeAllDsoDirs_Btn.setEnabled(radio_dir_Libs.getSelection() && dso_dir_list.getItemCount() > 0);
		
		removeDllDir_btn.setEnabled(radio_dir_Libs.getSelection() && dll_dir_list.getItemCount() > 0);
		removeAllDllDir_Btn.setEnabled(radio_dir_Libs.getSelection() && dll_dir_list.getItemCount() > 0);
		
	}

	private boolean isPreviouslySelected(String fileName)
	{
		for(String item:forced_hdrs_list.getItems())
		{
			if(item.equalsIgnoreCase(fileName))
				return true;
		}
		return false;
	}
	
	public void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				HelpContextIDs.BASELINE_EDITOR_HELP);			
	}
	public void UpdateFilesList() {
	}
	public void UpdatePage() {
		updateButtons();
	}
	
	public void modifyText(ModifyEvent e) {
		
		if(e.widget == profileCmb)
		{
			Object obj = (BaselineProfile)BaselineProfileUtils.getBaselineProfileData(profileCmb.getText());
			if(obj instanceof BaselineProfile && ((BaselineProfile)obj).isPredefined())
			{
				shell.getShell().setText(Messages.getString("BaselineEditor.BaselineEditor-")+profileCmb.getText()+" (Read-only)");
				setEditorEnabled(false);
			}
			else
			{
				shell.getShell().setText(Messages.getString("BaselineEditor.BaselineEditor-")+profileCmb.getText());
				setEditorEnabled(true);
			}
		}		
	}
	public void setEditorEnabled(boolean flag)
	{
		sdkCmb.setEnabled(flag);
		versionCmb.setEnabled(flag);
		infCheck.setEnabled(flag);
		bldInfPath.setEnabled(infCheck.getEnabled() && infCheck.getSelection());
		browseBld.setEnabled(bldInfPath.getEnabled());
		hdrGrp.setEnabled(flag);
		libGrp.setEnabled(flag);
		forcedGrp.setEnabled(flag);
		deleteBtn.setEnabled(flag);			
	}
	public boolean isMetadataFileUpdated()
	{
		IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		String currentURL = prefStore.getString(CompatibilityAnalyserPreferencesConstants.BASELINES_URL);
		String prevURL = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_BASELINE_URL);
		Long lastModified_metadata=prefStore.getLong(CompatibilityAnalyserPreferencesConstants.LASTMODIFIED_DATE_METADATA);
		
		if(!prevURL.equals("") && prevURL.equals(currentURL))
		{
			try 
			{
				URL url = new URL(currentURL);
				URLConnection connection = url.openConnection();
			
				Long webServerModTime = connection.getLastModified();
				if(webServerModTime <= lastModified_metadata)
					return false;
			
			}catch (MalformedURLException e) {
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}	
		}
		return true;
	}
	
	/**
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
					displayFiles.clear();
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
	 * 
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
	
	/**
	 * This will add exported paths of the given project to the given list.
	 * @param prjInfo
	 * @param usrIncList
	 */
	private static void getExportPathsAndFiles(ICarbideProjectInfo prjInfo, ArrayList<File> usrIncList)
	{
		DefaultViewConfiguration viewConfig = new DefaultViewConfiguration(prjInfo);
	   	  
	    EpocEnginePathHelper pathHelper = new EpocEnginePathHelper(prjInfo.getProject());
	    IDocument doc = viewConfig.getViewParserConfiguration().getModelDocumentProvider().getDocument(prjInfo.getAbsoluteBldInfPath().toFile());
	   	    
	 	BldInfModelFactory factory = new BldInfModelFactory();
	    
	   	IPath wsPath = prjInfo.getAbsoluteBldInfPath();
	   	IBldInfOwnedModel bldInfModel = factory.createModel(wsPath, doc);
	    
	   	bldInfModel.parse();
	    
	    IBldInfView view = bldInfModel.createView(viewConfig);
	 	   
	    IBldInfData data = view.getData();
	  
	    java.util.List<IExport> exports = data.getExports();
	   	  	    	    
	    for(IExport xport:exports)
	    {
	    	IPath exportPath = pathHelper.convertToFilesystem(xport.getSourcePath());
	    	
	    	File parentFolder = exportPath.toFile().getParentFile();
	    	
	    	if(!usrIncList.contains(parentFolder))
	    		usrIncList.add(parentFolder);
	     } 
	}

}
