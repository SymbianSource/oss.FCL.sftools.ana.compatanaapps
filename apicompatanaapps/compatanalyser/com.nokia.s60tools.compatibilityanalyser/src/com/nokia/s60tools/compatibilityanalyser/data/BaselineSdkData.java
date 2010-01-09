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
* Description: Class used to store properties of Baseline SDK.
*
*/

package com.nokia.s60tools.compatibilityanalyser.data;

public class BaselineSdkData {

	public String baselineSdkName;
	public String baselineSdkVersion;
	public String epocRoot;
	
    public String[] baselineHeaderDir;
    public String [] forcedHeaders;
       
    public String baselineIncludes;
    
    public String [] baselineLibsDir;
    public String [] baselineDllDir;
    public BaselineSdkData() {
	}
}
