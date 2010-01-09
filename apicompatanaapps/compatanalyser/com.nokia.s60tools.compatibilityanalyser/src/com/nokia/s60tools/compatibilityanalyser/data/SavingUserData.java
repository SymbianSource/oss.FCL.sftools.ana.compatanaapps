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
* Description:Stores set of values in dialog_settings.xml and retreives them when needed.
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
public class SavingUserData {
	
	
	public static final String PROFILES_SECTION=Messages.getString("SavingUserData.ProfileSelection");	 //$NON-NLS-1$
	public static final String PROFILES_LIST_LABEL=Messages.getString("SavingUserData.ProfileLabel"); //$NON-NLS-1$
	
	public static final String HEADERS_DIR_SECTION = Messages.getString("SavingUserData.HeaderSection"); //$NON-NLS-1$
	public static final String HEADERS_DIR_LIST = Messages.getString("SavingUserData.HeaderList"); //$NON-NLS-1$
	
	public static final String LIBRARIES_DIR_SECTION = Messages.getString("SavingUserData.LibrarySection"); //$NON-NLS-1$
	public static final String LIBRARIES_DIR_LIST = Messages.getString("SavingUserData.LibraryList"); //$NON-NLS-1$
	
	public static final String REPORT_PATH_SECTION = Messages.getString("SavingUserData.ReportSection"); //$NON-NLS-1$
	public static final String REPORT_PATH_LIST = Messages.getString("SavingUserData.ReportPathList"); //$NON-NLS-1$
	
	public static final String CORE_PATH_SECTION = Messages.getString("SavingUserData.CoreSection"); //$NON-NLS-1$
	public static final String CORE_PATH_LIST = Messages.getString("SavingUserData.CorePathList"); //$NON-NLS-1$
	
	public static final String SDK_SECTION = Messages.getString("SavingUserData.SDKSelection"); //$NON-NLS-1$
	
	public static final String BLD_PATH_LIST = "Bld Inf List";
	public static final String BLDINF_SECTION = "BldInf Section";
	
	private static final int MAX_SAVED_VALUES = 5;
	
	public static enum ValueTypes {PROFILENAME, REPORT_PATH, CORE_PATH, BLDINF_PATH}
	
	public SavingUserData() {
	}
	
	/**
	 * @param pathType specifies the type of the value being stored. This type
	 * maps to a labelled set of values under a section, in dialog_settings.xml
	 * @param value set of values to be stored.
	 */
	public void saveValue(ValueTypes pathType, String value) {
		try {
			switch (pathType) {
				case PROFILENAME: {
					savePath(value, PROFILES_LIST_LABEL, getSection(PROFILES_SECTION));
					break;
				}	
				case REPORT_PATH:{
					savePath(value, REPORT_PATH_LIST, getSection(REPORT_PATH_SECTION));
					break;
				}
				case CORE_PATH:{
					savePath(value, CORE_PATH_LIST, getSection(CORE_PATH_SECTION));
					break;
				}
				case BLDINF_PATH:{
					savePath(value, BLD_PATH_LIST, getSection(BLDINF_SECTION));
					break;
				}
				default: 
					break;
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	
	public void saveValues(ValueTypes pathType, String[] values) {
		try {
			switch (pathType) {
				default:
					break;
				}	
			}catch(Exception e){
				
			}
	}
	/**
	 * This method stores given sdkVersion against given sdkId using IDialogSettings mechanism
	 * @param sdkUniqueId name of the sdk to be stored
	 * @param sdkVersion version of the sdk to be stored.
	 */
	public void saveSDKNameAndVersion(String sdkUniqueId, String sdkVersion)
	{
		IDialogSettings section = getSection(SDK_SECTION);
		if(section != null)
		{
			section.put(sdkUniqueId, sdkVersion);
		}
	}
	
	/**
	 * This method returns previously stored version value for a given sdk,
	 * from dialog_settings.xml file.
	 * @param sdkUniqueId name of the sdk 
	 * @return previously stored version for the given sdk.
	 */
	public String getLastSdkVersion (String sdkUniqueId)
	{
		String version = null;
		IDialogSettings section = getSection(SDK_SECTION);
		if(section != null)
		{
			version = section.get(sdkUniqueId);
		}
		return version;
	}
	
	/**
	 * @param valueType type of the values to be retreived 
	 * @return user's previous stored values for this type
	 */
	public String[] getPreviousValues(ValueTypes valueType) {
		try {
			String[] retVal = null;
			
			switch (valueType) {
				case PROFILENAME: {
					retVal = getPreviousPaths(PROFILES_SECTION,PROFILES_LIST_LABEL);
					break;
				}
				case REPORT_PATH:{
					retVal = getPreviousPaths(REPORT_PATH_SECTION, REPORT_PATH_LIST);
					break;
				}
				case CORE_PATH:{
					retVal = getPreviousPaths(CORE_PATH_SECTION, CORE_PATH_LIST);
					break;
				}
				case BLDINF_PATH:{
					retVal = getPreviousPaths(BLDINF_SECTION, BLD_PATH_LIST);
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
	 * Saves given path to correct section in dialog_settings.xml
	 * @param path path to be saved
	 * @param listLabel name of the array which contains the values
	 * @param section section which contain the actual array 
	 */
	protected void savePath(String path, String listLabel, IDialogSettings section) {
		if (section != null) {
			String[] previousValues = section.getArray(listLabel);
			
			// No previous values exist
			if (previousValues == null) {
				previousValues = new String[1];
				previousValues[0] = path;
			// Previous values exists
			} else {
				int valuesCount = previousValues.length;
				
				boolean valueExisted = false;
				// see if passed value already exist.
				for (int i = 0; i < valuesCount; i++) {
					if (previousValues[i].compareToIgnoreCase(path) == 0) {
						valueExisted = true;
						
						// passed value exists, move it to first position
						for (int j = i; j > 0; j--) {
							previousValues[j] = previousValues[j-1];
						}
						previousValues[0] = path;
						
						break;
					}
				}
				
				// passed value did not exist, add it to first position (and move older values "down")
				if (!valueExisted) {
						if (valuesCount >= MAX_SAVED_VALUES) {
							for (int i = valuesCount-1; i > 0; i--) {
								previousValues[i] = previousValues[i-1];
							}
							previousValues[0] = path;
						}else{
							String[] values = new String[valuesCount + 1];
							values[0] = path;
							for (int i = 0; i < valuesCount; i++) {
								values[i+1] = previousValues[i];
							}
							previousValues = values;
						}
					}
				}
			section.put(listLabel, previousValues);
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
	 * @param name of the profile to be deleted from corresponding section.
	 * 
	 */
	public void deleteProfile(String name)
	{
		IDialogSettings section= getSection(PROFILES_SECTION);
		
		if (section != null) {
			String[] previousValues = section.getArray(PROFILES_LIST_LABEL);
			if(previousValues!=null)
			{
				
				int check_exists=-1;
			    for (int i = 0; i < previousValues.length; i++) {
			    			    	
					if(previousValues[i].equalsIgnoreCase(name))
					{					
						check_exists=i;
						break;
					}					
				}
			    
			    if(check_exists!=-1)
			    {
			    	String[] values=new String[previousValues.length - 1]; 
					
			    	for (int i = 0,j=0; i < previousValues.length; i++) {
						
			    		if(i!=check_exists)
			    		{
			    			values[j]=previousValues[i];
			    			j++;
			    		}
			    		else
			    			continue;
					}
			    	section.put(PROFILES_LIST_LABEL, values);
			    }		    
			}
		}
		
	}
	
	/**
	 * @param sectionName name of the section to be retreived.
	 * If the section does not exist, it creates one.
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
