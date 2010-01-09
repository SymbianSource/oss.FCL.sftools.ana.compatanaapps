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

import java.util.List;

/**
 * 
 * This class contains details specific to a library issue,
 * like error id, function name where the issue exists etc.
 *
 */
public class LibraryIssueData extends CompatibilityIssueData{

	private List<String> dllDirPaths;
	private String functionName;
	private int error_typeid;
	private String ordinalPosition;
	
	public LibraryIssueData() { }
	
	public LibraryIssueData(LibraryIssueData target) {
		super(target);
		this.dllDirPaths = target.dllDirPaths;
		this.error_typeid = target.error_typeid;
		this.ordinalPosition = target.ordinalPosition;
		this.functionName = target.functionName;
	}
	
	public List<String> getDllDirPaths() {
		return dllDirPaths;
	}
	public void setDllDirPaths(List<String> dllDirPaths) {
		this.dllDirPaths = dllDirPaths;
	}
	public int getError_typeid() {
		return error_typeid;
	}
	public void setError_typeid(int error_typeid) {
		this.error_typeid = error_typeid;
	}
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getOrdinalPosition() {
		return ordinalPosition;
	}

	public void setOrdinalPosition(String ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}
		
}
