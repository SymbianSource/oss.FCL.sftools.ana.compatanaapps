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

/**
 * 
 * This class represents a compatibility issue.
 * It contains all the information about an issue, like currentfile path, 
 * baselinefile path, line number of the issue etc.
 *
 */
public class CompatibilityIssueData {
	public enum ISSUE_CATEGORY{BC_BREAK, SC_BREAK, BOTH, NONE};
	
	private String currentIssueFile;
	private String baselineIssueFile;
	private int lineNumber;
	private String issueDescription;
	private String bcSeverityText;
	private String scSeverityText;
	private String currentFileName;
	private ISSUE_CATEGORY issueCategory = ISSUE_CATEGORY.NONE;
	private ISSUE_CATEGORY root = ISSUE_CATEGORY.NONE;
	
	public CompatibilityIssueData(CompatibilityIssueData target)
	{
		this.lineNumber = target.lineNumber;
		this.issueDescription = target.issueDescription;
		this.issueCategory = target.issueCategory;
		this.currentFileName = target.currentFileName;
		this.baselineIssueFile = target.baselineIssueFile;
		this.currentIssueFile = target.currentIssueFile;
		this.bcSeverityText = target.bcSeverityText;
		this.scSeverityText = target.scSeverityText;
	}

	public CompatibilityIssueData() { }
	
	public String getBaselineIssueFile() {
		return baselineIssueFile;
	}
	public void setBaselineIssueFile(String baselineIssueFile) {
		this.baselineIssueFile = baselineIssueFile;
	}
	public String getCurrentIssueFile() {
		return currentIssueFile;
	}
	public void setCurrentIssueFile(String currentIssueFile) {
		this.currentIssueFile = currentIssueFile;
	}
	
	public String getIssueDescription() {
		return issueDescription;
	}
	public void setIssueDescription(String issueDescription) {
		this.issueDescription = issueDescription;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getBCSeverityText() {
		return bcSeverityText;
	}
	public void setBCSeverityText(String bcSeverityText) {
		this.bcSeverityText = bcSeverityText;
	}
	public String getSCSeverityText() {
		return scSeverityText;
	}
	public void setSCSeverityText(String scSeverityText) {
		this.scSeverityText = scSeverityText;
	}
	public ISSUE_CATEGORY getIssueCategory() {
		return issueCategory;
	}
	public void setIssueCategory(ISSUE_CATEGORY issueCategory) {
		this.issueCategory = issueCategory;
	}

	public void setRoot(ISSUE_CATEGORY c) {
		root = c;
	}
	public ISSUE_CATEGORY getRoot() {
		return root;
	}

	public String getCurrentFileName() {
		return currentFileName;
	}

	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}
}
