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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.compatibilityanalyser.data.BCAndSCIssues;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueFileData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueData.ISSUE_CATEGORY;

public class IssuesViewerLabelProvider implements ITableLabelProvider, ITableColorProvider
{
	Color RED_COLOR = new Color(Display.getCurrent(),255,141,28);
	Color YELLOW_COLOR = new Color(Display.getCurrent(),255,255,128);
	
	public void addListener(ILabelProviderListener arg0) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {
	}

	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	public String getColumnText(Object obj, int arg1) {
		if(arg1 == 0)
		{
			if(obj.toString().startsWith(BCAndSCIssues.BC_ROOT) || obj.toString().startsWith(BCAndSCIssues.SC_ROOT))
				return obj.toString();
			if(obj instanceof CompatibilityIssueFileData)
				return ((CompatibilityIssueFileData)obj).getCurrentFileShortName();
			else if(obj instanceof CompatibilityIssueData)
			{
				return ((CompatibilityIssueData)obj).getIssueDescription();
			}
			return null;
		}
		else if(arg1 == 1)
		{
			if(obj instanceof CompatibilityIssueData)
			{
				if(((CompatibilityIssueData)obj).getRoot() == ISSUE_CATEGORY.BC_BREAK)
					return ((CompatibilityIssueData)obj).getBCSeverityText();
				else if(((CompatibilityIssueData)obj).getRoot() == ISSUE_CATEGORY.SC_BREAK)
					return ((CompatibilityIssueData)obj).getSCSeverityText();
				else
					return null;
			}
		}
		return null;
	}

	public Color getBackground(Object obj, int column) {
		if(column == 1)
		{
			if(obj instanceof CompatibilityIssueData)
			{
				if(((CompatibilityIssueData)obj).getRoot() == ISSUE_CATEGORY.BC_BREAK)
				{
					if(((CompatibilityIssueData)obj).getBCSeverityText().equals("BBC Break"))
						return RED_COLOR;
					else if(((CompatibilityIssueData)obj).getBCSeverityText().startsWith("Possible"))
						return YELLOW_COLOR;
					else 
						return null;
				}
				else if(((CompatibilityIssueData)obj).getRoot() == ISSUE_CATEGORY.SC_BREAK)
				{
					if(((CompatibilityIssueData)obj).getSCSeverityText().equals("SC Break"))
						return RED_COLOR;
					else if(((CompatibilityIssueData)obj).getSCSeverityText().startsWith("Possible"))
						return YELLOW_COLOR;
					else //for Informative, No color
						return null;
				}
				else
					return null;
			}
		}
		return null;
	}

	public Color getForeground(Object arg0, int arg1) {
		return null;
	}
}