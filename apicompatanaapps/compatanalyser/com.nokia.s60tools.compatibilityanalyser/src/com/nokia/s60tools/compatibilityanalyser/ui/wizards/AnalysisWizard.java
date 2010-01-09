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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.ICarbideBuildManager;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectModifier;
import com.nokia.carbide.cpp.sdk.core.ISymbianBuildContext;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.LastUsedKnownissues;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.SavingUserData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.model.ParserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageKeys;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.actions.ProjectViewPopupAction;
import com.nokia.s60tools.compatibilityanalyser.ui.views.MainView;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalyserUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.ui.UiUtils;
import com.nokia.s60tools.ui.wizards.S60ToolsWizard;

/**
 * Compatibility Analyser Wizard 
 */
public class AnalysisWizard extends S60ToolsWizard implements IImportWizard {

	private ProductSDKSelectionPage sdkPage;
	private HeaderFilesPage headersPage;
	//private PlatformHeadersPage platformHeadersPage;
	private LibraryFilesPage libsPage;
	private ReportFileInfoPage reportPage;
	private ConfigurationSaveDetails savePage;

	private CompatibilityAnalyserEngine engine;
	private ProductSdkData currentSdk;

	private boolean isOpenedFromConfigFile = false;
	private boolean isOpenedFromProject = false;
	private String configFilePath = null;
	private static final ImageDescriptor bannerImgDescriptor = UiUtils.getBannerImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.ANALYSER_WIZARD_BANNER));
	Font invalidFile_Font=new Font(Display.getCurrent(),"Tahoma",8,SWT.ITALIC);	
	
	public AnalysisWizard(CompatibilityAnalyserEngine engine) {
		super(bannerImgDescriptor);
		setWindowTitle(Messages.getString("AnalysisWizard.Title")); //$NON-NLS-1$
		this.engine = engine;
		currentSdk = engine.getCurrentSdkData();
		this.isOpenedFromConfigFile  = currentSdk.isOpenedFromConfigFile;
		this.isOpenedFromProject = currentSdk.isOpenedFromProject;

		if(isOpenedFromConfigFile)
			configFilePath = currentSdk.configFileSysPath;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		sdkPage = new ProductSDKSelectionPage(engine);
		addPage(sdkPage);

		headersPage = new HeaderFilesPage(engine);
		addPage(headersPage);

		//platformHeadersPage = new PlatformHeadersPage(engine);
		//addPage(platformHeadersPage);

		libsPage = new LibraryFilesPage(engine);
		addPage(libsPage);

		reportPage = new ReportFileInfoPage(engine);
		addPage(reportPage);

		savePage = new ConfigurationSaveDetails(engine);

		if(!engine.getCurrentSdkData().isOpenedFromConfigFile)
			addPage(savePage);
		
		this.getShell().setSize(600, 710);
		this.getShell().setLocation(430, 0);
	}
	public boolean canFinish()
	{
		boolean status = sdkPage.canFinish();
		if(sdkPage.headersButton.getSelection())
			status = status && headersPage.canFinish();
		if(sdkPage.libsButton.getSelection())
			status = status && libsPage.canFinish();

		status = status && reportPage.canFinish();

		if(!currentSdk.isOpenedFromConfigFile)
			status = status && savePage.canFinish();

		return status;
	}
	private List<ISymbianBuildContext> getSDKConfigurations(ISymbianSDK selectedSDK) {
		return selectedSDK.getFilteredBuildConfigurations();
	}

	/**
	 * This method fetches the next page of the wizard based on the current page.
	 * If any of the fields from next page depend on the fields of current page,
	 * such fields will be handled here.
	 */
	public IWizardPage getNextPage(IWizardPage page)
	{		
		if(!engine.getCurrentSdkData().isOpenedFromConfigFile)
			savePage.fileText.setText(sdkPage.selectedSDK.getUniqueId() + "_" + sdkPage.profileCombo.getText() + "." + ConfigurationSaveDetails.CONFIGEXTN);
		
		if(this.getContainer().getCurrentPage() == sdkPage)
		{
			if(currentSdk.isOpenedFromProject && sdkPage.headersButton.getSelection())
			{
				IProject prj= CarbideBuilderPlugin.getProjectInContext();
				ICarbideBuildManager mngr=CarbideBuilderPlugin.getBuildManager();
				ICarbideProjectModifier modifier=mngr.getProjectModifier(prj);

				ICarbideBuildConfiguration origConf = mngr.getProjectInfo(prj).getDefaultConfiguration();

				if(!modifier.getDefaultConfiguration().getSDK().getUniqueId().equals(sdkPage.selectedSDK.getUniqueId()))
				{
					ICarbideBuildConfiguration configTobeSet=null;

					List<ISymbianBuildContext> all=getSDKConfigurations(sdkPage.selectedSDK);
					if(all != null && all.size() >0)
					{	
						configTobeSet=modifier.createNewConfiguration(all.get(0), true);
						configTobeSet.saveConfiguration(false);

						modifier.setDefaultConfiguration(configTobeSet);
						modifier.saveChanges();
						
						ArrayList<String> usrIncList = new ArrayList<String>();
						ArrayList<String> sysIncList = new ArrayList<String>();

						ProjectViewPopupAction.getIncludesFromThisProject(mngr.getProjectInfo(prj), usrIncList, sysIncList);

						modifier.setDefaultConfiguration(origConf);
						modifier.saveChanges();

						currentSdk.currentIncludes = sysIncList.toArray(new String[0]);
					}

				}
			}
			engine.getCurrentSdkData().productSdkVersion = sdkPage.versionCombo.getText();
			
			if(sdkPage.headersButton.getSelection())
			{
				return headersPage;
			}
			if(sdkPage.libsButton.getSelection())
			{
				ISymbianSDK sdk = sdkPage.selectedSDK;
				String [] items = CompatibilityAnalyserUtils.getPlatformList(sdk);
				if(items != null)
				{
					libsPage.buildTarget_list.setItems(items);
					
					//When wizard is opened using configuration file, select the previous used targets.
					String [] current_platforms = engine.getCurrentSdkData().libsTargetPlat;
					ArrayList<Integer> platform_indices = new ArrayList<Integer>();
					if(current_platforms != null)
					{
						for(String s: current_platforms)
						{
							int index = libsPage.buildTarget_list.indexOf(s.toLowerCase());
							if(index != -1)
								platform_indices.add(index);
						}
						
						int[] indices_to_be_selected = new int[platform_indices.size()];    
						for (int i=0; i < indices_to_be_selected.length; i++) {
							indices_to_be_selected[i] = platform_indices.get(i).intValue();    
						}
						libsPage.buildTarget_list.select(indices_to_be_selected);
					}
					else
					{
						int i = libsPage.buildTarget_list.indexOf(ProductSdkData.DEFAULT_TARGET_PLATFORM); //$NON-NLS-1$
						
						if(i != -1)
							libsPage.buildTarget_list.setSelection(i);
						else
							libsPage.buildTarget_list.setSelection(0);
					}
					libsPage.selectedPlatform = libsPage.buildTarget_list.getSelection();
				}
				libsPage.releaseRoot = sdk.getReleaseRoot().toString();
				return libsPage;	
			}
		}
		else if(this.getContainer().getCurrentPage() == headersPage)
		{
			if(sdkPage.libsButton.getSelection())
			{		
				String [] items = CompatibilityAnalyserUtils.getPlatformList(sdkPage.selectedSDK);
				
				if( items != null)
				{
					libsPage.buildTarget_list.setItems(items);
					
					String [] current_platforms = engine.getCurrentSdkData().libsTargetPlat;
					
					ArrayList<Integer> platform_indices = new ArrayList<Integer>();
					
					if(current_platforms != null)
					{
						for(String s: current_platforms)
						{
							int index = libsPage.buildTarget_list.indexOf(s);
							if(index != -1)
								platform_indices.add(index);
						}
						
						for(int i =0; i < platform_indices.size(); i++)
						{
							libsPage.buildTarget_list.select(platform_indices.get(i));
						}
					}
					else
					{
						int i = libsPage.buildTarget_list.indexOf(ProductSdkData.DEFAULT_TARGET_PLATFORM);
						
						if(i != -1)
							libsPage.buildTarget_list.setSelection(i);
						else
							libsPage.buildTarget_list.setSelection(0);
					}

					libsPage.selectedPlatform = libsPage.buildTarget_list.getSelection();
				}
				
				libsPage.releaseRoot = sdkPage.selectedSDK.getReleaseRoot().toString();
				
				return libsPage;
			}
			else
				return reportPage;
		}
		else if(this.getContainer().getCurrentPage() == libsPage)
		{
			return reportPage;
		}

		else if(this.getContainer().getCurrentPage() == reportPage && !engine.getCurrentSdkData().isOpenedFromConfigFile)
		{
			return savePage;
		}

		return null;
	}

	public boolean performFinish()
	{
		engine = new CompatibilityAnalyserEngine();
		currentSdk = new ProductSdkData();

		currentSdk.productSdkName = sdkPage.sdkNameCombo.getText();
		currentSdk.productSdkVersion = sdkPage.versionCombo.getText();
		currentSdk.epocRoot = sdkPage.epocDirectory.getText();

		if(!currentSdk.epocRoot.endsWith("\\")) 
			currentSdk.epocRoot=currentSdk.epocRoot+"\\"; 

		SavingUserData userData = new SavingUserData();

		IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();

		if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_TOOLS))
		{
			currentSdk.useDefaultCoreTools = true;
			currentSdk.useLocalCoreTools = false;
			currentSdk.useSdkCoreTools = false;
			currentSdk.useWebServerCoreTools = false;
		}
		else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS))
		{
			currentSdk.useDefaultCoreTools = false;
			currentSdk.useLocalCoreTools = true;
			currentSdk.useSdkCoreTools = false;
			currentSdk.useWebServerCoreTools = false;
			currentSdk.coreToolsPath = prefStore.getString(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH);
		}
		else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS))
		{
			currentSdk.useDefaultCoreTools = false;
			currentSdk.useLocalCoreTools = false;
			currentSdk.useSdkCoreTools = true;
			currentSdk.useWebServerCoreTools = false;
			currentSdk.coreToolsPath = prefStore.getString(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS_PATH);
		}
		else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_TOOLS))
		{
			currentSdk.useDefaultCoreTools = false;
			currentSdk.useLocalCoreTools = false;
			currentSdk.useSdkCoreTools = false;
			currentSdk.useWebServerCoreTools = true;
			currentSdk.urlPathofCoreTools = prefStore.getString(CompatibilityAnalyserPreferencesConstants.CORETOOLS_URL);
		}

		ISymbianSDK selectedSdk = sdkPage.selectedSDK;

		if(selectedSdk.getSDKVersion().toString().equalsIgnoreCase(Messages.getString("AnalysisWizard.13"))){ //$NON-NLS-1$
			userData.saveSDKNameAndVersion(selectedSdk.getUniqueId(), sdkPage.versionCombo.getText());
		}
		if(sdkPage.headersButton.getSelection())
		{
			engine.setHeaderAnalysis(true);

			if(headersPage.defaultRootDir.getSelection()){
				String s = FileMethods.appendPathSeparator(sdkPage.epocDirectory.getText());
				String[] defIncPath={s + Messages.getString("AnalysisWizard.epoc32Include")};
				currentSdk.currentHeaderDir = defIncPath;
			}
			else{
				
				currentSdk.currentHeaderDir = headersPage.getSelectedHdrPaths();
				currentSdk.defaultHeaderDir = false;
				LastUsedKnownissues saveData=new LastUsedKnownissues();
				saveData.saveValues(LastUsedKnownissues.ValueTypes.CURRENT_HEADERS, currentSdk.currentHeaderDir);
			}
			if(!headersPage.allFiles.getSelection())
			{
				currentSdk.analyseAll = false;
				int size = headersPage.filesList.getItemCount();
				ArrayList<String> validFiles = new ArrayList<String>();
				ArrayList<String> currentFiles = new ArrayList<String>();

				for(int i=0; i<size; i++)
				{
					TableItem item = headersPage.filesList.getItem(i);
					if(!item.getForeground().equals(headersPage.invalidColor))
					{
						currentFiles.add(item.getText(0));
						if(item.getText(1).equalsIgnoreCase("")) //$NON-NLS-1$
							validFiles.add(item.getText(0));
						else
							validFiles.add(item.getText(1));
					}
				}
				currentSdk.currentFiles = currentFiles.toArray(new String [0]);
				currentSdk.HeaderFilesList = validFiles.toArray(new String[0]);
			}
			if(!headersPage.allTypes.getSelection())
			{
				currentSdk.allTypes = false;
				if(headersPage.hType.getSelection())
					currentSdk.hTypes = true;
				if(headersPage.hrhType.getSelection())
					currentSdk.hrhTypes = true;
				if(headersPage.rsgType.getSelection())
					currentSdk.rsgTypes = true;
				if(headersPage.mbgType.getSelection())
					currentSdk.mbgTypes = true;
				if(headersPage.hppType.getSelection())
					currentSdk.hppTypes = true;
				if(headersPage.panType.getSelection())
					currentSdk.panTypes = true;
			}
			if(headersPage.allFiles.getSelection() && !headersPage.useRecursive.getSelection())
			{				
				currentSdk.useRecursive = false;
			}
			prefStore.setValue(CompatibilityAnalyserPreferencesConstants.USERECURSION_LAST_SELECTION, headersPage.useRecursive.getSelection());

			if(headersPage.currentSdkData.usePlatformData)
			{
				currentSdk.usePlatformData = true;
			}

			if(!isOpenedFromProject)
				prefStore.setValue(CompatibilityAnalyserPreferencesConstants.USEPLATFORMDATA_LAST_SELECTION, headersPage.currentSdkData.usePlatformData);

			if(headersPage.currentSdkData.currentIncludes!=null && headersPage.currentSdkData.currentIncludes.length > 0){
				currentSdk.currentIncludes = headersPage.currentSdkData.currentIncludes;
			}
			
			currentSdk.forcedHeaders = headersPage.currentSdkData.forcedHeaders;
			
			currentSdk.replaceSet = headersPage.replaceSet;
		}
		if(sdkPage.libsButton.getSelection())
		{
			engine.setLibraryAnalysis(true);

			if(libsPage.buildtarget_radioBtn.getSelection())
			{
				currentSdk.platfromSelection = true;
				currentSdk.default_platfrom_selection = false;
				currentSdk.selected_library_dirs = false;
				currentSdk.libsTargetPlat = libsPage.selectedPlatform;
				currentSdk.currentLibsDir = new String[currentSdk.libsTargetPlat.length];
				currentSdk.currentDllDir = new String[currentSdk.libsTargetPlat.length];
				
				for(int i=0; i < currentSdk.libsTargetPlat.length; i++)
				{
					currentSdk.currentLibsDir[i] = CompatibilityAnalyserEngine.getLibsPathFromPlatform(sdkPage.selectedSDK,libsPage.selectedPlatform[i]);
					currentSdk.currentDllDir [i] = CompatibilityAnalyserEngine.getDllPathFromPlatform(sdkPage.selectedSDK,libsPage.selectedPlatform[i]);
				}
			}
			else if(libsPage.userDirs_radioBtn.getSelection())
			{
				currentSdk.platfromSelection = false;
				currentSdk.selected_library_dirs = true;
				currentSdk.default_platfrom_selection = false;
				
				currentSdk.currentLibsDir = libsPage.getSelectedDSOPaths();
				currentSdk.currentDllDir = libsPage.getSelectedDLLPaths();
				LastUsedKnownissues saveData=new LastUsedKnownissues();
				saveData.saveValues(LastUsedKnownissues.ValueTypes.LIBRARY_DIRECTORIES, currentSdk.currentLibsDir);
				saveData.saveValues(LastUsedKnownissues.ValueTypes.DLL_DIRECTORIES, currentSdk.currentDllDir);
			}

			if(!libsPage.analyseAllFiles_radioBtn.getSelection())
			{
				currentSdk.analyzeAllLibs = false;
				int size = libsPage.dsoFiles_list.getItemCount();

				currentSdk.libraryFilesList = new String [size];
				for(int i=0; i<size; i++)
					currentSdk.libraryFilesList[i] = libsPage.dsoFiles_list.getItem(i).getText();
			}
			currentSdk.toolChain = libsPage.selectedToolChain.getName();
			currentSdk.toolChainPath = libsPage.selectedToolChain.getToolChainPath();

		}
		currentSdk.reportName = reportPage.fileName.getText();
		currentSdk.reportPath = reportPage.pathCombo.getText();
		userData.saveValue(SavingUserData.ValueTypes.REPORT_PATH, currentSdk.reportPath);

		currentSdk.filterNeeded = reportPage.yes.getSelection();
		prefStore.setValue(CompatibilityAnalyserPreferencesConstants.FILTER_REPORTS_LAST_SELECTION, reportPage.yes.getSelection());
		engine.setCurrentSdkData(currentSdk);
		engine.setBaselineProfile(sdkPage.profileCombo.getText());
		userData.saveValue(SavingUserData.ValueTypes.PROFILENAME, sdkPage.profileCombo.getText());
		prefStore.setValue(CompatibilityAnalyserPreferencesConstants.LAST_USED_BASELINE_PROFILE, sdkPage.profileCombo.getText());

		if(isOpenedFromConfigFile)
		{
			try{
				ParserEngine.saveSettingsToFile(engine, configFilePath);
			}catch(ParserConfigurationException e){
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Compatibility Analyser", "Error in saving the configuration " + e.getMessage());
			}
			return true;
		}
		if(!isOpenedFromConfigFile && savePage.isSavingNeeded()){
			currentSdk.container = savePage.getContainerName();
			currentSdk.configName = savePage.getFileName();

			if(currentSdk.configName != null && !currentSdk.configName.endsWith(".comptanalyser"))
				currentSdk.configName += ".comptanalyser";

			try{
				if(currentSdk.container.length()> 0 && currentSdk.configName.length() >0){
					IPath path = new Path(FileMethods.appendPathSeparator(currentSdk.container) + currentSdk.configName);
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

					ParserEngine.saveSettingsToFile(engine, file.getLocation().toString());
				}
			}catch(ParserConfigurationException e){
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Compatibility Analyser", "Error in saving the configuration " + e.getMessage());
			}
		}
		
		String currentDateTime = FileMethods.getDateTime();
		FileMethods.createFolder(FileMethods.convertForwardToBackwardSlashes(CompatibilityAnalyserPlugin.stateLocation.toString())+ File.separator  +currentDateTime);
		currentSdk.setconfigfolder(FileMethods.convertForwardToBackwardSlashes(CompatibilityAnalyserPlugin.stateLocation.toString())+ File.separator  +currentDateTime);
		
		MainView mainView = MainView.showAndReturnYourself();
		if(mainView !=null)
			mainView.startAnalysis(engine);


		return true;
	}

}
