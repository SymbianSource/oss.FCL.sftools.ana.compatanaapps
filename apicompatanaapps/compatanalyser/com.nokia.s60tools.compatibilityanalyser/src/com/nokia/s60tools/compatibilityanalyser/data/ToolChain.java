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
* Description: This represents a tool chain like GCCE, RVCT etc.
*
*/
package com.nokia.s60tools.compatibilityanalyser.data;

/**
 * This class represents a tool chain like GCCE, RVCT etc.
 */
public class ToolChain {

	private final String name;
	private final String description;
	private String version;
	private String path;
	private boolean installed;
	
	public ToolChain(String name, String desc)
	{
		this.name = name;
		this.description = desc;
	}
	public void setInstalled(boolean isInstalled)
	{
		this.installed = isInstalled;
	}
	public boolean isInstalled()
	{
		return this.installed;
	}
	public String getToolChainPath()
	{
		return path;
	}
	
	/**
	 * This method return the desciption of the tool chain.
	 * However, if version info exists, it wil be appended to the description.
	 */
	public String getDescription()
	{
		if(version == null)
			return description;
		else
			return description + " " + version;
	}
	public void setToolChainPath(String path)
	{
		this.path = path;
	}
	public String getName()
	{
		return name;
	}
	public void setVersion(String version)
	{
		this.version = version;
	}
}
