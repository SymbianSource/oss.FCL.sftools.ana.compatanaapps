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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;

/**
 * WizardPage of Analysis wizard, wherein details related to
 * configuration saving can be provided.
 *
 */
public class ConfigurationSaveDetails extends S60ToolsWizardPage implements ModifyListener, SelectionListener{

	private Text containerText;
	Text fileText;

	private Button button;
	private Button saveButton;

	private Label projLabel;
	private Label label;
	
	private CompatibilityAnalyserEngine engine;
	private ProductSdkData currentSdk;
	
	public static final String CONFIGEXTN = "comptanalyser";
	
	public ConfigurationSaveDetails(CompatibilityAnalyserEngine engine)
	{
		super("Configuration Details");
		setTitle("Configuration Information"); 
		
		setDescription("Provide saving details"); 
		setPageComplete(true);
		this.engine = engine;
		currentSdk = engine.getCurrentSdkData();
	}
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		saveButton = new Button(container, SWT.CHECK);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		saveButton.setLayoutData(data);
		saveButton.setSelection(currentSdk.saveConfig);
		saveButton.setText("Save configuration to a file. ");
		saveButton.addSelectionListener(this);
		
		projLabel = new Label(container, SWT.NULL);
		projLabel.setText("&Project:");
		projLabel.setEnabled(saveButton.getSelection());
		
		containerText = new Text(container, SWT.BORDER | SWT.SINGLE );
	
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.setEnabled(saveButton.getSelection());
				
		if(currentSdk.projectName != null)
		{
			containerText.setText(ResourcesPlugin.getWorkspace().getRoot().getProject(currentSdk.projectName).toString().substring(2));
		}
		
		containerText.addModifyListener(this);
		
		button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(this);
		button.setEnabled(saveButton.getSelection());
		
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");
		label.setEnabled(saveButton.getSelection());
		
		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.setEnabled(saveButton.getSelection());
				
		if(currentSdk.isOpenedFromProject && currentSdk.projectName != null)
		{
			fileText.setText(currentSdk.projectName + "." + CONFIGEXTN);
		}
		else if(!currentSdk.isOpenedFromProject)
		{
			String baseProfile = engine.getBaselineProfile();
			String sdkName = currentSdk.productSdkName;
						
			if(baseProfile != null && sdkName != null)
				fileText.setText(sdkName + "_" + baseProfile + "." + ConfigurationSaveDetails.CONFIGEXTN);
		}
		fileText.addModifyListener(this);
		
		setHelp();
		setControl(container);
	
	}
	
	@Override
	public void recalculateButtonStates() {
	}

	@Override
	public void setInitialFocus() {
	}
	
	public String getContainerName() {
		return containerText.getText();
	}
	public String getFileName() {
		return fileText.getText();
	}
	
	public void modifyText(ModifyEvent e) 
	{
		if(e.widget == containerText)
		{
			getWizard().getContainer().updateButtons();
		}
		if(e.widget == fileText)
		{
			getWizard().getContainer().updateButtons();
		}
	}

	/**
	 * Checks the values provided by user and if they are invalid
	 * an error message will be shown and the method return false.
	 * Otherwise, it returns true.
	 */
	public boolean canFlipToNextPage()
	{
		this.setMessage(null);
		this.setErrorMessage(null);
		
		if(saveButton.getSelection()) 
		{
			if(containerText.getText().length() == 0)
				this.setErrorMessage("Please provide a valid project name.");
			else 
			{
				if(ResourcesPlugin.getWorkspace().getRoot().exists(new Path(containerText.getText())) &&
						ResourcesPlugin.getWorkspace().getRoot().findMember(containerText.getText()) instanceof IContainer )
				{
					//System.out.println("Given path is of a container..");
				}
				else
				{
					this.setErrorMessage("Invalid Container.");
					return false;
				}
			}
		    if(fileText.getText().length() == 0)
				this.setErrorMessage("Please provide a valid name for the configuration.");
			
			if(fileText.getText().length() >0)
			{
				if(fileText.getText().lastIndexOf(".") != -1)
				{
					String extn = fileText.getText().substring(fileText.getText().lastIndexOf(".") + 1);
					if(!extn.equals(CONFIGEXTN))
						this.setErrorMessage("File extension must be \"comptanalyser\"");
				}
				if(containerText.getText().length() >0){
					IPath path = new Path(FileMethods.appendPathSeparator(containerText.getText()) + fileText.getText());
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				
					if(file != null && file.exists())
						this.setMessage("File already exists. It will be overwritten", DialogPage.WARNING);
				}
			}
		
		}
		return false;
	}
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	public void widgetSelected(SelectionEvent e) {
		if(e.widget == button)
		{
			IProject [] projList = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			
			int closed = 0;
			for(IProject p:projList)
			{
				if(!p.isOpen())
					closed ++;
			}
				
			if(closed == projList.length){
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Compatibility Analyser", "The workspace does not contain open projects. So, configuration cannot be saved");
			}
			else{
				ContainerSelectionDialog dialog = new ContainerSelectionDialog(
						getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
					"Select new file container");
			
				dialog.showClosedProjects(false);
			
				if (dialog.open() == ContainerSelectionDialog.OK) {
					Object[] result = dialog.getResult();
					if (result.length == 1) {
						String selected = ((Path)result[0]).toString().substring(1);
						containerText.setText(FileMethods.convertForwardToBackwardSlashes(selected));
					}
				}
			}
		}
		if(e.widget == saveButton)
		{
			projLabel.setEnabled(saveButton.getSelection());
			containerText.setEnabled(saveButton.getSelection());
			
			button.setEnabled(saveButton.getSelection());
			label.setEnabled(saveButton.getSelection());
			fileText.setEnabled(saveButton.getSelection());
			
			getWizard().getContainer().updateButtons();
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
	public boolean isSavingNeeded()
	{
		return saveButton.getSelection();
	}
	
	/**
	 * Sets context sensitive help for this page
	 *
	 */
	public void setHelp()
	{
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(saveButton,
				HelpContextIDs.CONFIGURATION_INFO);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(button,
				HelpContextIDs.CONFIGURATION_INFO);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(containerText,
				HelpContextIDs.CONFIGURATION_INFO);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fileText,
				HelpContextIDs.CONFIGURATION_INFO);
		
		
	}
			
}
