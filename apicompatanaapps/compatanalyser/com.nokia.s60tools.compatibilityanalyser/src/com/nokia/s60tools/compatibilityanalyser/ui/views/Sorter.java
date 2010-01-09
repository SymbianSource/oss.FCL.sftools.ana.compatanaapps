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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

import com.nokia.s60tools.compatibilityanalyser.data.ReportData;

/**
 * This sorter class is associated with 'Report Filtered?' column of CA view.
 * This class contains the logic for sorting values in the 'Report Filtered?' column.
 */
public class Sorter extends ViewerSorter
{
	int sortDirection;

	Sorter(int sortDirection)
	{
		this.sortDirection = sortDirection;
	}
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		int returnValue = 0;

		ReportData r1 = (ReportData)e1;
		ReportData r2 = (ReportData)e2;

		returnValue = (r1.getFilterationStatus()).compareTo(r2.getFilterationStatus());

		if(sortDirection == SWT.UP)
			return returnValue;
		else
			return returnValue * -1;
	}
}