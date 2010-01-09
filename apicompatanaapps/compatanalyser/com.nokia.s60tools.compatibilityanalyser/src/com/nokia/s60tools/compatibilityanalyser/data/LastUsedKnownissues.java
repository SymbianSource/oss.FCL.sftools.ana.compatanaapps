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
* Description: This stores set of values in dialog_settings.xml and retreives them when needed
*
*/
package com.nokia.s60tools.compatibilityanalyser.data;

import org.eclipse.jface.dialogs.IDialogSettings;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;

/**
 * This class uses IDialogSettings mechanism to store
 * set of values in dialog_settings.xml file and to retreive them when needed.
 *
 */
public class LastUsedKnownissues {

	public static final String WEBSERVER_ISSUES_URL = Messages.getString("LastUsedKnownissues.WebIssuesURL"); //$NON-NLS-1$
	public static final String WEBSERVER_URL = Messages.getString("LastUsedKnownissues.WebServerURL"); //$NON-NLS-1$
	public static final String LOCAL_ISSUES_PATHS = Messages.getString("LastUsedKnownissues.LocalIssuesPath"); //$NON-NLS-1$
	public static final String CURRENT_HEADER_DIRECTORIES = "Last used current header dirs";
	public static final String CURRENT_LIBRARY_DIRECTORIES = "Last used current libs dirs";
	public static final String CURRENT_DLL_DIRECTORIES = "Last used current dll dirs";
	
	public static final String WEBSERVER_ISSUES_SECTION = Messages.getString("LastUsedKnownissues.WebServerSection"); //$NON-NLS-1$
	public static final String WEBSERVER_URL_SECTION = Messages.getString("LastUsedKnownissues.WebServerURLSection"); //$NON-NLS-1$
	public static final String LOCAL_ISSUES_SECTION = Messages.getString("LastUsedKnownissues.LocalIssuesSection"); //$NON-NLS-1$
	public static final String CURRENT_HEADER_SECTION = "current headers section";
	public static final String CURRENT_LIBRARY_SECTION = "current libraries section";
	public static final String CURRENT_DLL_SECTION = "current dll section";
	
	public static enum ValueTypes {ISSUES_URL, LOCAL_ISSUES_PATH,WEBSERVER_MAIN_URL,CURRENT_HEADERS, LIBRARY_DIRECTORIES, DLL_DIRECTORIES}
	
	/**
	 * @param pathType specifies the type of the value being stored. This type
	 * maps to a labelled set of values under a section, in dialog_settings.xml
	 * @param value set of values to be stored.
	 */
	public void saveValues(ValueTypes pathType, String[] value) {
		try {
			switch (pathType) {
				case ISSUES_URL: {
					savePaths(value, WEBSERVER_ISSUES_URL, getSection(WEBSERVER_ISSUES_SECTION));
					break;
				}	
				case LOCAL_ISSUES_PATH:{
					savePaths(value, LOCAL_ISSUES_PATHS, getSection(LOCAL_ISSUES_SECTION));
					break;
				}
				case WEBSERVER_MAIN_URL:{
					savePaths(value, WEBSERVER_URL, getSection(WEBSERVER_URL_SECTION));
					break;
				}
				case CURRENT_HEADERS:{
					savePaths(value, CURRENT_HEADER_DIRECTORIES, getSection(CURRENT_HEADER_SECTION));
					break;				
				}
				case LIBRARY_DIRECTORIES:{
					savePaths(value, CURRENT_LIBRARY_DIRECTORIES, getSection(CURRENT_LIBRARY_SECTION));
					break;				
				}
				case DLL_DIRECTORIES:{
					savePaths(value, CURRENT_DLL_DIRECTORIES, getSection(CURRENT_DLL_SECTION));
					break;				
				}
				default: 
					break;
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	
	/**
	 * @param valueType type of the value to be retreived 
	 * @return user's previous stored values for this type.
	 */
	public String[] getPreviousValues(ValueTypes valueType) {
		try {
			String[] retVal = null;
			
			switch (valueType) {
				case ISSUES_URL: {
					retVal = getPreviousPaths(WEBSERVER_ISSUES_SECTION,WEBSERVER_ISSUES_URL);
					break;
				}
				case LOCAL_ISSUES_PATH:{
					retVal = getPreviousPaths(LOCAL_ISSUES_SECTION, LOCAL_ISSUES_PATHS);
					break;
				}
				case WEBSERVER_MAIN_URL:{
					retVal = getPreviousPaths(WEBSERVER_URL_SECTION, WEBSERVER_URL);
					break;
				}
				case CURRENT_HEADERS:{
					retVal = getPreviousPaths(CURRENT_HEADER_SECTION, CURRENT_HEADER_DIRECTORIES);
					break;
				}
				case LIBRARY_DIRECTORIES:{
					retVal = getPreviousPaths(CURRENT_LIBRARY_SECTION, CURRENT_LIBRARY_DIRECTORIES);
					break;
				}
				case DLL_DIRECTORIES:{
					retVal = getPreviousPaths(CURRENT_DLL_SECTION, CURRENT_DLL_DIRECTORIES);
					break;
				}
				default: 
					break;
			}			
			return retVal;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Saves given path to given section in dialog_settings.xml
	 * @param path paths to be saved
	 * @param listLabel name of the array which contains the values
	 * @param section section which contains the actual array 
	 */
	protected void savePaths(String[] path, String listLabel, IDialogSettings section) {
		if (section != null) {
			String[] previousValues = section.getArray(listLabel);
			
			// No previous values exist
			if (previousValues == null) {
				previousValues = path;
			// Previous values exists
			} else {
				previousValues = path;
			}
			section.put(listLabel, previousValues);
		}
	}
	
	/**
	 * Returns previously entered values of wanted context (i.e. wizard page).
	 * @param section section which contains array
	 * @param listLabel name of the array whose values are needed
	 * @return previously entered paths for given section
	 */
	protected String[] getPreviousPaths(String section, String listLabel) {
		String[] retVal = null;
		IDialogSettings sect = getSection(section);
		if (sect != null)
			retVal = sect.getArray(listLabel);
		
		return retVal;
	}
	
	/**
	 * @param sectionName name of the section to be retreived
	 * @return section which maps to given name, in dialog_setting.xml file.
	 */
	protected IDialogSettings getSection(String sectionName) {
		IDialogSettings retVal = null;
		if (CompatibilityAnalyserPlugin.getDefault().getDialogSettings() != null) {
			 retVal = CompatibilityAnalyserPlugin.getDefault().getDialogSettings().getSection(sectionName);
			if (retVal == null) {
			 retVal = CompatibilityAnalyserPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
			}
		}
		return retVal;
	}	
	
}
