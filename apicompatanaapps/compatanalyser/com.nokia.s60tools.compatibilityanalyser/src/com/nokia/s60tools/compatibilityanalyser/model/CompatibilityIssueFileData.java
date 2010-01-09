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
package com.nokia.s60tools.compatibilityanalyser.model;

import java.util.ArrayList;

import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueData.ISSUE_CATEGORY;

/**
 * 
 * Each instance of this class represents a file which has compatiblity issues.
 * The class also contains list of all issues, in the corresponding file. 
 *
 */
public class CompatibilityIssueFileData {
	
	private ISSUE_CATEGORY root = ISSUE_CATEGORY.NONE;
	private String currentIssueFile;
	private String baselineIssueFile;
	private String fileShortName;
	private ArrayList<CompatibilityIssueData> issuesList;

	public CompatibilityIssueFileData()
	{
		issuesList = new ArrayList<CompatibilityIssueData>();
	}
	
	public CompatibilityIssueFileData(CompatibilityIssueFileData target)
	{
		this.currentIssueFile = target.currentIssueFile;
		this.baselineIssueFile = target.baselineIssueFile;
		this.fileShortName = target.fileShortName;
		this.issuesList = target.issuesList;
	}
	
	public String getCurrentIssueFile() {
		return currentIssueFile;
	}

	public void setCurrentIssueFile(String fileName) {
		this.currentIssueFile = fileName;
	}
	
	public String getBaselinIssueFile() {
		return baselineIssueFile;
	}

	public void setBaselineIssueFile(String fileName) {
		this.baselineIssueFile = fileName;
	}
	
	public String getCurrentFileShortName() {
		return fileShortName;
	}

	public void setCurrentFileShortName(String fileName) {
		this.fileShortName = fileName;
	} 
	
	public void addIssueData(CompatibilityIssueData issueData)
	{
		issuesList.add(issueData);
	}
	
	public CompatibilityIssueData[] getIssues() {
		return issuesList.toArray(new CompatibilityIssueData[0]);
	}
	
	public void setIssues(ArrayList<CompatibilityIssueData> issues)
	{
		issuesList = issues;
	}
	
	public void setRoot(ISSUE_CATEGORY c) {
		root = c;
	}
	
	public ISSUE_CATEGORY getRoot() {
		return root;
	}
}