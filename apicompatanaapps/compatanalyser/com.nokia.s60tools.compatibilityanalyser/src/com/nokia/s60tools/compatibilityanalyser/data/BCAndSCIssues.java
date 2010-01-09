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
package com.nokia.s60tools.compatibilityanalyser.data;

import java.util.ArrayList;

import com.nokia.s60tools.compatibilityanalyser.data.ReportData.FILE_TYPE;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueFileData;

public class BCAndSCIssues
{
	public static final String BC_ROOT = "BC Breaks";
	public static final String SC_ROOT = "SC Breaks";
	
	private FILE_TYPE report_file_type;
	ArrayList<CompatibilityIssueFileData> bc_data = new ArrayList<CompatibilityIssueFileData>();
	ArrayList<CompatibilityIssueFileData> sc_data = new ArrayList<CompatibilityIssueFileData>();
	
	public ArrayList<CompatibilityIssueFileData> getBCData() {
		return bc_data;
	}
	public ArrayList<CompatibilityIssueFileData> getSCData() {
		return sc_data;
	}
	public void setBCData(ArrayList<CompatibilityIssueFileData> data) {
		bc_data = data;
	}
	public void setSCData(ArrayList<CompatibilityIssueFileData> data) {
		sc_data = data;
	}
	public FILE_TYPE getType() {
		return report_file_type;
	}
	public void setType(FILE_TYPE type) {
		this.report_file_type = type;
	}
}