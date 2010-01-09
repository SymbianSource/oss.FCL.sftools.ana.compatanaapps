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
* Description: This stores properties of Current SDK and also analysis settings
*
*/
package com.nokia.s60tools.compatibilityanalyser.data;

import java.util.ArrayList;

import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;

/**
 * This stores properties of Current SDK and analysis settings
 *
 */
public class ProductSdkData {

	public static final String DEFAULT_TARGET_PLATFORM = "armv5";
	public String productSdkName;
	public String productSdkVersion;
	public String epocRoot;
	
	public boolean defaultHeaderDir = true;
    public String[] currentHeaderDir;
    public String[] currentIncludes;
    
    public String toolChain;
    public String toolChainPath;
    
    public boolean allTypes = true;
    public boolean analyseAll = true;
    
    public String [] HeaderFilesList;
    public String [] currentFiles;
    
    public boolean hTypes;
    public boolean hrhTypes;
    public boolean rsgTypes;
    public boolean mbgTypes;
    public boolean hppTypes;
    public boolean panTypes;
    
    public boolean useRecursive;
    public boolean usePlatformData;
    
    public String [] currentLibsDir;
    public String [] currentDllDir;
    public String [] libsTargetPlat;
    
    public boolean default_platfrom_selection = true;
    public boolean platfromSelection = false;
    public boolean selected_library_dirs = false;
    
    public boolean analyzeAllLibs = true; 
    public String [] libraryFilesList;
 
    public String tempPath;
    
    public boolean useDefaultCoreTools;
    public boolean useLocalCoreTools;
    public boolean useSdkCoreTools;
    public boolean useWebServerCoreTools;
    
    public String urlPathofCoreTools;
    public String coreToolsPath;
    
    public String knownIssuesPath;
    
    public String selectedKnownIssues;
    
    public String reportName;
    public String reportPath;
    public boolean filterNeeded;
    public boolean isForcedProvided;
    
    public ArrayList<String> replaceSet;
    public ArrayList<String> forcedHeaders;
    
    public String projectName;
    public boolean isOpenedFromProject;
    public boolean isOpenedFromConfigFile;
    
    public boolean saveConfig;
    public String container;
    public String configName;
    public String configFileSysPath; 
    
    public String config_folder = null;
	
    public ProductSdkData()
    {
    	toolChain = new String(Messages.getString("ProductSdkData.GCCE")); //$NON-NLS-1$
    	useRecursive  = true;
    	libsTargetPlat = new String[]{DEFAULT_TARGET_PLATFORM};
    	replaceSet = new ArrayList<String>();
    }
    
    public String readIncludePaths()
    {
		if(currentIncludes == null)
    		return ""; //$NON-NLS-1$
		
		StringBuffer sb = new StringBuffer(""); 
		for(String s: currentIncludes)
		{
			sb.append(s);
			sb.append(';');
		}
		return FileMethods.convertForwardToBackwardSlashes(sb.toString());
    }
    public void setconfigfolder(String config_folder){
    	this.config_folder = config_folder;
    }
    public String getconfigfolder(){
    	return config_folder;
    }
}
