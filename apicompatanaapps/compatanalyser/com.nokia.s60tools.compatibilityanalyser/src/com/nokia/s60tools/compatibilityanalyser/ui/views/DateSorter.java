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

import java.text.ParseException;
import java.util.Date;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

import com.nokia.s60tools.compatibilityanalyser.data.ReportData;

/**
 * This sorter class is associated with 'Last Modification Time' column of CA view.
 * This class contains the logic for sorting values in the 'Last Modification Time' column.
 */
public class DateSorter extends ViewerSorter
{
	int sortDirection;

	public DateSorter(int sortDirection) {
		this.sortDirection = sortDirection;
	}
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		int returnValue = 0;

		String s1 = ((ReportData)e1).gettLastModifiedDate();
		String s2 = ((ReportData)e2).gettLastModifiedDate();

		Date d1 = null;
		Date d2 = null;

		try {
			d1 = ReportData.dateFormat.parse(s1);
			d2 = ReportData.dateFormat.parse(s2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(d1 != null && d2 != null)
		{
			returnValue = d1.compareTo(d2);
		}

		if(sortDirection == SWT.UP)
			return returnValue * 1;
		else
			return returnValue * -1;
	}
}