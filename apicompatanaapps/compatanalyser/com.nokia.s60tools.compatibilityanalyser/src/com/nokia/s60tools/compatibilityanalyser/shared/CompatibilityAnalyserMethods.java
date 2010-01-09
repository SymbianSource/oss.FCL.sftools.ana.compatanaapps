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
package com.nokia.s60tools.compatibilityanalyser.shared;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.model.HeaderAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.model.LibraryAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.utils.BaselineProfileUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalyserUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;

public class CompatibilityAnalyserMethods {
	
	public String [] getPlatformsList(String sdkName)
	{
	 	String [] platformsList = null;
	 	
	 	ISymbianSDK [] loadedSdks = SDKCorePlugin.getSDKManager().getSDKList().toArray(new ISymbianSDK [0]);
	 	
	 	for(ISymbianSDK sdk: loadedSdks)
	 	{
	 		if(sdk.getUniqueId().equalsIgnoreCase(sdkName))
	 			platformsList = CompatibilityAnalyserUtils.getPlatformList(sdk);
	 	}
		return platformsList;
	}
	
	public String getLibsPathFromPlatform(String sdkName, String platformName){
		String s = null;
		ISymbianSDK [] loadedSdks = SDKCorePlugin.getSDKManager().getSDKList().toArray(new ISymbianSDK [0]);
		for(ISymbianSDK sdk: loadedSdks)
	 	{
	 		if(sdk.getUniqueId().equalsIgnoreCase(sdkName))
	 			s = CompatibilityAnalyserEngine.getLibsPathFromPlatform(sdk, platformName);
	 	}
		return s;
	}
	
	public BaselineSdkData getBaselineProfileData(String profileName)
	{
		CompatibilityAnalyserEngine engine = new CompatibilityAnalyserEngine();
		Object obj = BaselineProfileUtils.getBaselineProfileData(profileName);
		
		if((obj != null) && (obj instanceof BaselineProfile))
		{
			BaselineProfile profile = (BaselineProfile)obj;
			return engine.readBaselineSdkData(profile);
		}
		else
			return null;
				
	}
	
	public void copyFile(File source, File dest,boolean overwrite)
	{
		FileMethods.copyFile(source, dest, overwrite);
	}
	public boolean deleteFolder(String folder)
	{
		return FileMethods.deleteFolder(folder);
	}
	public void saveBaselineProfile(BaselineProfile profile)
	{
		FileOutputStream fout=null;
		ObjectOutputStream oout=null;
		
		try {
			
			//String profilePath = getWorkSpacePath() + File.separator + CompatibilityAnalyserEngine.BASELINE_PROFILES_FOLDER + File.separator + profile.profileName+".data";
			String profilePath = CompatibilityAnalyserPlugin.stateLocation + File.separator + BaselineProfileUtils.BASELINE_PROFILES_FOLDER + File.separator + profile.getProfileName()+".data";
			File profileData = new File(profilePath);
			
			if(!profileData.exists())
				profileData.getParentFile().mkdirs();
			
			fout=new FileOutputStream(profilePath);
			oout=new ObjectOutputStream(fout);
			oout.writeObject(profile);
			
			if(oout != null)
				oout.close();
			if(fout != null)
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	public String getWorkSpacePath()
	{
		return CompatibilityAnalyserEngine.getWorkspacePath();
	}
	public String extractZipContent(String source, String target)
	{
		return CompatibilityAnalyserEngine.extractZipContent(source, target, CompatibilityAnalyserEngine.ElementTypes.FileType,"CheckBC.py",null);
	}
	
	public String getAllKnownIssuesFromWebServer(String url, ArrayList<String> issuesList)
	{
		return CompatibilityAnalyserEngine.readMetadataFileFromWebServer(url, "knownissues_metadata", issuesList);
	}
	public String [] prepareHAArguments(ProductSdkData currentSdk)
	{
		CompatibilityAnalyserEngine  engine = new CompatibilityAnalyserEngine();
		
		engine.setCurrentSdkData(currentSdk);
		HeaderAnalyserEngine headersEngine = new HeaderAnalyserEngine(engine, null);
		return headersEngine.prepareHAArguments(currentSdk);
	}
	public String [] prepareOCArguments(ProductSdkData currentSdk)
	{
		CompatibilityAnalyserEngine  engine = new CompatibilityAnalyserEngine();
		
		engine.setCurrentSdkData(currentSdk);
		LibraryAnalyserEngine libsEngine = new LibraryAnalyserEngine(engine, null);
		return libsEngine.prepareOCArguments(currentSdk);
	}
	
	public String checkOCInput(ProductSdkData currentData, BaselineSdkData baselineData)
	{
		CompatibilityAnalyserEngine  engine = new CompatibilityAnalyserEngine();
			
		engine.setCurrentSdkData(currentData);
		LibraryAnalyserEngine libsEngine = new LibraryAnalyserEngine(engine, null);
		return libsEngine.readAndCheckInput(currentData, baselineData);
		
		//return null;
	}
	public String checkHAInput(ProductSdkData currentData, BaselineSdkData baselineData)
	{
		CompatibilityAnalyserEngine  engine = new CompatibilityAnalyserEngine();
			
		engine.setCurrentSdkData(currentData);
		HeaderAnalyserEngine headersEngine = new HeaderAnalyserEngine(engine, null);
		return headersEngine.readAndCheckInput(currentData, baselineData);
		
		//return null;
	}
	public String [] getHeadersFromFile(String name)
	{
		ArrayList<String> list = new ArrayList<String>();
		CompatibilityAnalyserEngine.getIncFilesFromFile(name, list);
		
		return list.toArray(new String[0]);
	}
	public ArrayList<String> getSupportedFilesFromDir(String dirPath)
	{
		return CompatibilityAnalyserEngine.getHeaderFilesFromDir(dirPath);
	}
}
