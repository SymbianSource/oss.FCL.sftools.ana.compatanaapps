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
* Description: This class represents compatibility Analyser report.
*
*/
package com.nokia.s60tools.compatibilityanalyser.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueFileData;


/**
 * This class represents compatibility Analyser report.
 */
public class ReportData {
	public enum FILE_TYPE {LIBRARY,HEADER, NONE};
	public static SimpleDateFormat dateFormat;
	
	private String name;
	private boolean isFiltered;
	private ArrayList<CompatibilityIssueFileData> headerIssues;
	private ArrayList<CompatibilityIssueFileData> libraryIssues;
	
	private FILE_TYPE TYPE = FILE_TYPE.NONE;
	
	static{
		dateFormat =  new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss a");
	}
	public ReportData(String name)
	{
		this.name = name;
	}
	
	/**
	 * This method returns the Last Modified Date of the current report file
	 */
	public String gettLastModifiedDate()
	{
		if(name == null)
			return "";
		
		File report = new File(name);
		
		long tmp = report.lastModified();
		
		Date date = new Date(tmp);
		
		GregorianCalendar calendar = new GregorianCalendar();
		
		calendar.setTime(date);
				
		String formattedDate = dateFormat.format(date);
				
		return new String(formattedDate);
	}
	public String getName()
	{
		return name;
	}
	public void setFilterationStatus(boolean status)
	{
		isFiltered = status;
	}
	
	/*
	 * This method returns the filteration status of the report.
	 * This status will be shown in the view. 
	 */
	public String getFilterationStatus()
	{
		if(isFiltered)
			return "Yes";
		else
			return "No";
	}

	public ArrayList<CompatibilityIssueFileData> getHeaderIssues() {
		return headerIssues;
	}

	public void setHeaderIssues(ArrayList<CompatibilityIssueFileData> headerIssues) {
		this.headerIssues = headerIssues;
	}

	public ArrayList<CompatibilityIssueFileData> getLibraryIssues() {
		return libraryIssues;
	}

	public void setLibraryIssues(ArrayList<CompatibilityIssueFileData> libraryIssues) {
		this.libraryIssues = libraryIssues;
	}

	public FILE_TYPE getTYPE() {
		return TYPE;
	}

	public void setTYPE(FILE_TYPE type) {
		TYPE = type;
	}
}
