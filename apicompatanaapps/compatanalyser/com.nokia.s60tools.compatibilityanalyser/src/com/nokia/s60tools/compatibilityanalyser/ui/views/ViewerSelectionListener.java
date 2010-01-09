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
package com.nokia.s60tools.compatibilityanalyser.ui.views;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.nokia.s60tools.compatibilityanalyser.data.ReportData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueFileData;

public class ViewerSelectionListener implements ISelectionChangedListener {

	private MainView view;
	private String prevSelected = "";
	
	public ViewerSelectionListener(MainView view) {
		this.view = view;
	}
	public void selectionChanged(SelectionChangedEvent event) {
		
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object selected_file = selection.getFirstElement();
		if(selected_file instanceof ReportData)
		{
			if(((ReportData)selected_file).getName().equals(prevSelected))
				return;
				
			ReportData report = (ReportData)selected_file;
			ArrayList<CompatibilityIssueFileData> issues = null;
			 
			if(report.getTYPE() == ReportData.FILE_TYPE.HEADER)
				 issues = report.getHeaderIssues();
			else if(report.getTYPE() == ReportData.FILE_TYPE.LIBRARY)
				issues = report.getLibraryIssues();

			view.setCompatibilityIssues(issues,report.getTYPE());	
			
			prevSelected = ((ReportData)selected_file).getName();
		}
		else
			view.setCompatibilityIssues(null, ReportData.FILE_TYPE.NONE);
	}
}
