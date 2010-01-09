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
* Description: When Edit action is run on a .comptanalyser configuration file,
* this class will be used to open the Analysis wizard with settings from the given file.
*
*/
package com.nokia.s60tools.compatibilityanalyser.ui.editors;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorLauncher;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.model.ParserEngine;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.AnalysisWizard;

/**
 * This class gets invoked when Edit action is run on a .comptanalyser
 * configuration file.This opens the Analysis wizard with settings
 * from the given file.
 */
public class ComptAnalyserEditLauncher implements IEditorLauncher {

	Document document;
	private CompatibilityAnalyserEngine result;
	

	public void open(IPath file) {
		
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(file.toString()));
		} catch (SAXException e) {
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
		
		//Parse the given Configuration file
		result = ParserEngine.parseTheConfiguration(document);
		result.getCurrentSdkData().configFileSysPath = file.toString();
		
		if(result == null)
		{
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Compatibility Analyser", "Selected file is not a valid configuration file ");
			return;
		}
		else
		{
				//Invoke Wizard with data in configuration file
				Runnable showWizardRunnable = new Runnable(){
				public void run(){
		  		  WizardDialog wizDialog;
		  		  AnalysisWizard wiz = new AnalysisWizard(result);
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
	}
	
}
