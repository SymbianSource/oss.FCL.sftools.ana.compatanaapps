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
* Description: Used to store filteration settings.
*
*/

package com.nokia.s60tools.compatibilityanalyser.data;

import java.util.Vector;

/**
 * This class stores filteration settings 
 *
 */
public class ReportWizardData {
	
	public Vector<String> reportFiles=new Vector<String>();
	public String outputDir = null;
	public String bcFilterPath = null;
	public String knownissuesPath = null;
	
	public boolean useDefaultCoreTools;
	public boolean useLocalCoreTools;
	public boolean useSdkCoreTools;
	public boolean useWebServerCoreTools;
	public String config_folder = null;
	public ReportWizardData() {
	}
	public void setconfigfolder(String config_folder){
    	this.config_folder = config_folder;
    }
    public String getconfigfolder(){
    	return config_folder;
    }

}
