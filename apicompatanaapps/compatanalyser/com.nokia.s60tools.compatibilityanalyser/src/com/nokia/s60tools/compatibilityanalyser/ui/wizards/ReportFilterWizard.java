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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.ReportWizardData;
import com.nokia.s60tools.compatibilityanalyser.data.SavingUserData;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageKeys;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.views.MainView;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.ui.UiUtils;
import com.nokia.s60tools.ui.wizards.S60ToolsWizard;

public class ReportFilterWizard extends S60ToolsWizard implements IImportWizard{

	private AddReportFilesPage reportPage;

	ReportWizardData data=new ReportWizardData();  
	String[] files=null;
	private static final ImageDescriptor bannerImgDescriptor = UiUtils.getBannerImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.ANALYSER_WIZARD_BANNER));
	
	public ReportFilterWizard() {
		super(bannerImgDescriptor);
		setWindowTitle(Messages.getString("ReportFilterWizard.Title"));		 //$NON-NLS-1$
	}
	
	public ReportFilterWizard(String[] files) {
		super(bannerImgDescriptor);
		setWindowTitle(Messages.getString("ReportFilterWizard.WindowTitle")); //$NON-NLS-1$
		this.files=files;
	}

	public ReportFilterWizard(ImageDescriptor arg0) {
		super(arg0);
		
	}

	@Override
	public void addPages() {
		reportPage = new AddReportFilesPage(this.files);
		addPage(reportPage);
	}

	@Override
	public boolean performFinish() {
		
		String[] fileNames = reportPage.list.getItems();
		data.reportFiles.clear();
		for (int i = 0; i < fileNames.length; i++) {			
			data.reportFiles.add(fileNames[i]);
		}
		
		data.outputDir = reportPage.dirCmb.getText();
		SavingUserData saveData = new SavingUserData();
		saveData.saveValue(SavingUserData.ValueTypes.REPORT_PATH,data.outputDir);
		//getting the tools path
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_TOOLS))
		{
			data.useDefaultCoreTools = true;
			data.useLocalCoreTools = false;
			data.useSdkCoreTools = false;
			data.useWebServerCoreTools = false;
		}
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS))
		{
			data.useDefaultCoreTools = false;
			data.useLocalCoreTools = true;
			data.useSdkCoreTools = false;
			data.useWebServerCoreTools = false;
			data.bcFilterPath=store.getString(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH);
		}
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS))
		{
			data.useDefaultCoreTools = false;
			data.useLocalCoreTools = false;
			data.useSdkCoreTools = true;
			data.useWebServerCoreTools = false;			
			data.bcFilterPath = store.getString(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS_PATH);
		}	
		else if(store.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_TOOLS))
		{
			data.useDefaultCoreTools = false;
			data.useLocalCoreTools = false;
			data.useSdkCoreTools = false;
			data.useWebServerCoreTools = true;			
		}
		
		String currentDateTime = FileMethods.getDateTime();
		FileMethods.createFolder(FileMethods.convertForwardToBackwardSlashes(CompatibilityAnalyserPlugin.stateLocation.toString())+ File.separator  +currentDateTime);
		data.setconfigfolder(FileMethods.convertForwardToBackwardSlashes(CompatibilityAnalyserPlugin.stateLocation.toString())+ File.separator  +currentDateTime);
		
		MainView mainView = MainView.showAndReturnYourself();
		if(mainView!=null)
		{
			mainView.startFilteration(data);
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
	}

	@Override
	public boolean canFinish() {
		return reportPage.canFinish();
	}
}
