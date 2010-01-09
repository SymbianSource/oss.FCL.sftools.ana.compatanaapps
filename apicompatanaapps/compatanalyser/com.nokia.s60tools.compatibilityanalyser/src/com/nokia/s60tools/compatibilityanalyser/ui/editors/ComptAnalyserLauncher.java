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
package com.nokia.s60tools.compatibilityanalyser.ui.editors;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorLauncher;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.ToolChain;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.model.ParserEngine;
import com.nokia.s60tools.compatibilityanalyser.ui.views.MainView;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.AnalysisWizard;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.compatibilityanalyser.utils.BaselineProfileUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;

/**
 * This launches CompatibilityAnalysis process directly, using the configuration
 * from given file. It does not launch the wizard.
 *
 */
public class ComptAnalyserLauncher implements IEditorLauncher {

	private CompatibilityAnalyserEngine engine;
	
	public void open(IPath file) {
		
		try{
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(file.toString()));
			engine = ParserEngine.parseTheConfiguration(document);
			
			String status = validateGivenData(engine);
			
			if(status != null)
			{
				boolean query = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Compatibility Analyser", "Invalid Configuration: " + status + "\n Do you want to edit the Configuration file?");
				
				if(query)
				{
					engine.getCurrentSdkData().isOpenedFromConfigFile = true;
					engine.getCurrentSdkData().configFileSysPath = file.toString();
					
					Runnable showWizardRunnable = new Runnable(){
						public void run(){
				  		  WizardDialog wizDialog;
				  		  AnalysisWizard wiz = new AnalysisWizard(engine);
				       	  wiz.setNeedsProgressMonitor(true);
						  wizDialog = new WizardDialog(Display.getCurrent().getActiveShell(), wiz);
						  wizDialog.create();		
						  //wizDialog.getShell().setSize(550, 680);
						  wizDialog.addPageChangedListener(wiz);
						  wizDialog.open();
						 }
					   };
					   
					  Display.getDefault().asyncExec(showWizardRunnable); 
				}
				//MessageDialog.openError(Display.getCurrent().getActiveShell(), "Compatibility Analyser", "Invalid Configuration: " + status);
				return;
			}
			runStaticAnalysis(engine);
		}catch (SAXException e) {
			Status status = new Status(IStatus.ERROR, "Compatibiliy Analyser", 0, e.getMessage(), null);
			ErrorDialog.openError(Display.getCurrent().getActiveShell(),
					"Compatibiliy Analyser", "Unable to read the configuration", status);
		} catch (IOException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Compatibility Analyser", "Unable to read the configuration from " + file.toString());
		} catch (ParserConfigurationException e) {
			Status status = new Status(IStatus.ERROR, "Compatibiliy Analyser", 0, e.getMessage(), null);
			ErrorDialog.openError(Display.getCurrent().getActiveShell(),
					"Compatibiliy Analyser", "Unable to read the configuration", status);
		}
	}

	/*
	 * This method starts the analysis with given configuration
	 */
	private void runStaticAnalysis(CompatibilityAnalyserEngine engine)
	{
		ProductSdkData currentSdk = engine.getCurrentSdkData();
		
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
		
		MainView mainView = MainView.showAndReturnYourself();
		
		String currentDateTime = FileMethods.getDateTime();
		FileMethods.createFolder(FileMethods.convertForwardToBackwardSlashes(CompatibilityAnalyserPlugin.stateLocation.toString())+ File.separator  +currentDateTime);
		currentSdk.setconfigfolder(FileMethods.convertForwardToBackwardSlashes(CompatibilityAnalyserPlugin.stateLocation.toString())+ File.separator  +currentDateTime);
		
		if(mainView !=null)
		  mainView.startAnalysis(engine);
		
	}
	
	/*
	 * This method validated given static analysis data.
	 * The method returns an error if mandatory parameters are missing/invalid.
	 */
	private String validateGivenData(CompatibilityAnalyserEngine engine)
	{
		if(engine == null)
			return "Invalid Data.";
		
		ProductSdkData inputData = engine.getCurrentSdkData();
		
		String profile = engine.getBaselineProfile();
		
		if(profile == null || profile.equals("")) 
		{
			return "Invalid Baseline Profile";
		}
		else{
			Object obj = BaselineProfileUtils.getBaselineProfileData(profile);
			
			if(obj == null || !(obj instanceof BaselineProfile))
				return "Given Baseline Profile does not exist.";
		}
		if(inputData.productSdkName == null || inputData.productSdkVersion == null || !isValidPath(inputData.epocRoot))
		{
			return "Invalid Current SDK Data.";
		}
		
		if(!engine.isHeaderAnalysisChecked() && !engine.isLibraryAnalysisChecked())
		{
			return "Analysis type is not provided.";
		}
		else
		{
			ISymbianSDK [] loadedSdks = CompatibilityAnalyserPlugin.fetchLoadedSdkList().toArray(new ISymbianSDK[0]);
			boolean sdkIsLoaded = false;
	
			for(ISymbianSDK sdk:loadedSdks)
			{
				if(inputData.productSdkName.equalsIgnoreCase(sdk.getUniqueId()))
				{
					sdkIsLoaded = true;
					break;
				}
			}
		
			if(!sdkIsLoaded)
				return "Given SDK is currently uninstalled.";
		}
		
		if(engine.isHeaderAnalysisChecked()){
		
			if(!inputData.defaultHeaderDir && (inputData.currentHeaderDir == null || inputData.currentHeaderDir.length <=0))
			{
				return "Current header directory paths not given.";
			}
			else if(inputData.currentHeaderDir != null)
			{
				String invalidPath = null;
				for(String s:inputData.currentHeaderDir)
				{
					if(!isValidPath(s))
					{
						invalidPath = s;
						break;
					}
				}
				
				if(invalidPath != null)
					return "Provided header directory path " + invalidPath + " is invalid.";
			}
		
			if(!inputData.analyseAll && (inputData.HeaderFilesList == null || inputData.HeaderFilesList.length <=0))
			{
				return "Header files not provided for analysis.";
			}
			
		}
		
		if(engine.isLibraryAnalysisChecked()){
			
			if(inputData.platfromSelection && inputData.libsTargetPlat == null)
				return "Target Platform is not provided.";
			
			if(inputData.selected_library_dirs)
			{
				if(inputData.currentLibsDir == null || inputData.currentLibsDir.length <= 0)
					return "Current Library directory paths are not provided.";
		
				else if(inputData.currentLibsDir != null)
				{
					String invalidPath = null;
					for(String s:inputData.currentLibsDir)
					{
						if(!isValidPath(s))
						{
							invalidPath = s;
							break;
						}
					}
				
					if(invalidPath != null)
						return "Provided Library Path " + invalidPath + " is invalid.";
				}
				
				if(inputData.currentDllDir == null || inputData.currentDllDir.length <= 0)
					return "Current DLL directory paths are not provided.";
		
				else if(inputData.currentDllDir != null)
				{
					String invalidPath = null;
					for(String s:inputData.currentDllDir)
					{
						if(!isValidPath(s))
						{
							invalidPath = s;
							break;
						}
					}
				
					if(invalidPath != null)
						return "Provided DLL Path " + invalidPath + " is invalid.";
				}
			}
			
			if(!inputData.analyzeAllLibs && inputData.libraryFilesList == null)
				return "Libraries not provided for analysis.";
			
			if(inputData.toolChain == null || inputData.toolChainPath == null)
				return "Invalid ToolChain.";
			
			Vector<ToolChain> toolChains = CompatibilityAnalyserEngine.getToolChains();
			
			int index = -1;
			for(int i=0; i<toolChains.size(); i++)
			{
				if(toolChains.elementAt(i).getName().equals(inputData.toolChain))
				{
					index = i;
					break;
				}
			}
			if(index == -1)
				return "Provided ToolChain is not installed.";
			
		}
		return null;
	}
	
	private boolean isValidPath(String path)
	{
		File file = new File(path);
		
		if(!file.exists())
			return false;
		else if(!file.isDirectory())
			return false;
		
		return true;
	}
}
