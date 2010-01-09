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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nokia.s60tools.compatibilityanalyser.data.BCAndSCIssues;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueFileData;

public class IssuesViewerContentProvider implements ITreeContentProvider
{
	BCAndSCIssues allIssues = new BCAndSCIssues();

	public Object[] getChildren(Object element) {
		if(element.toString().equals(BCAndSCIssues.BC_ROOT))
		{
			return allIssues.getBCData().toArray();
		}
		else if(element.toString().equals(BCAndSCIssues.SC_ROOT))
		{
			return allIssues.getSCData().toArray();
		}
		else if(element instanceof CompatibilityIssueFileData)
		{
			return ((CompatibilityIssueFileData)element).getIssues();
		}
		return null;
	}

	public Object getParent(Object element) {	

		if(element instanceof CompatibilityIssueFileData)
			return ((CompatibilityIssueFileData)element).getRoot();
		else if(element instanceof CompatibilityIssueData)
			return ((CompatibilityIssueData)element).getRoot();

		return null;
	}

	public boolean hasChildren(Object element) {
		if(element.toString().equals(BCAndSCIssues.BC_ROOT) || element.toString().equals(BCAndSCIssues.SC_ROOT))
			return true;
		if(element instanceof CompatibilityIssueFileData)
			return ((CompatibilityIssueFileData)element).getIssues().length > 0;

			return false;
	}

	public Object[] getElements(Object parent) {
		if(parent instanceof BCAndSCIssues)
			allIssues = (BCAndSCIssues)parent;

		return new Object[]{BCAndSCIssues.BC_ROOT , BCAndSCIssues.SC_ROOT};
	}

	public void dispose() {
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
	}		
}