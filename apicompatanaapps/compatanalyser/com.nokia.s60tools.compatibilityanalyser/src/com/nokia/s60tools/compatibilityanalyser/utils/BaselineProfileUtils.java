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
package com.nokia.s60tools.compatibilityanalyser.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.SavingUserData;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;

/**
 * All the utility methods related to BaselineProfile
 *
 */
public class BaselineProfileUtils {

	public static final String EPOC_PATH = "epoc32";
	public static final String EPOC_INC_PATH = "epoc32\\include";
	public static final String ARMV5_PATH = "epoc32\\release\\armv5\\lib";
	public static final String BASELINE_PROFILES_FOLDER = Messages.getString("CompatibilityAnalyserEngine.BaselineProfiles"); //$NON-NLS-1$
	private static final String METADATAFILE = "baselines_metadata_2.0";
	
	/**
	 * @param profile will be serialized and stored in file system.
	 */
	public static void saveProfileOnFileSystem(BaselineProfile profile)
	{
		FileOutputStream fileOutStr=null;
		ObjectOutputStream objOutStr=null;
		try {

			String profilePath = getProfilePath(profile.getProfileName());

			File profileData = new File(profilePath);
			
			if(!profileData.exists())
				profileData.getParentFile().mkdirs();
				
			fileOutStr=new FileOutputStream(profilePath);
			try {
				objOutStr=new ObjectOutputStream(fileOutStr);
				objOutStr.writeObject(profile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			objOutStr.close();
			fileOutStr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * It reads the predefined web server profile available in web server.
	 * And returns the list of valid baseline profile names for each version of SDK.
	 * @param serverUrl
	 * @param metadataFilename file name of metadata file in webserver.
	 * @param baselineNames profile names will be filled in this list.
	 * @return error string if not null
	 */
	private static String readPredefinedBaselineProfilesFromWebserver(String serverUrl, String metadataFilename, ArrayList<String> baselineNames)
	{
		try {
			if(!serverUrl.endsWith("/"))
				serverUrl = serverUrl + "/";
			
			URL url = new URL(serverUrl + metadataFilename); 
			URI uri = new URI(serverUrl + metadataFilename);
			
			Proxy proxy = CompatibilityAnalyserUtils.getProxyForUri(uri);
			URLConnection conn = null; 
			
			if(proxy == null)
				conn = url.openConnection();
			else
				conn = url.openConnection(proxy);
			
			conn.connect();
			InputStream inStr = conn.getInputStream();
			    	
			InputStreamReader inReader = new InputStreamReader(inStr);
			BufferedReader br = new BufferedReader(inReader);
				    	
			String line = null;
			String[] attributes = null;
			Map<String, BaselineProfile> uniqueVersions = new HashMap<String, BaselineProfile>();
			
			while((line = br.readLine())!= null)
			{
				line = line.trim();
				if(line.length()==0)
					continue;
				
				attributes = line.trim().split(",");
				if(attributes.length != 3)
					continue;
				
				BaselineProfile pre_base = new BaselineProfile();
				pre_base.setPredefined(true);
				pre_base.setS60version(attributes[0].trim());
				pre_base.setProfileName(attributes[1].trim());
				pre_base.setSdkUrl(attributes[2]);
				pre_base.setUpdated(false);
				//For each version only one entry will be taken. If there two 3.1 entries, the last
				//one will be considered.
				uniqueVersions.put(attributes[0], pre_base);
			}

			for(BaselineProfile p: uniqueVersions.values())
			{

				baselineNames.add(p.getProfileName());
				
				if(predefinedNeedsDownload(p))
					saveProfileOnFileSystem(p);
			}
			
			if(baselineNames.isEmpty())
	    		return Messages.getString("CompatibilityAnalyserEngine.NoIssuesFileExistInWebServer") + metadataFilename + " does not contain any entries."; 
	    	
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch(FileNotFoundException e){
			return "Metadata file cannot be found in given URL Path.";
		}catch (IOException e) {
			e.printStackTrace();
			return "I/O error while opening the connection.\nPlease check the \n- Internet connection && \n- Provided URL";
		} catch (URISyntaxException e) {
			return e.getMessage();
		}
		
		return null;
	}

	/**
	 * @param profileName specifies name of the Baseline Profile
	 * @return an Object Containing Baseline Data stored in the Profile
	 * This method fetches the object with given profile name from workspace
	 * and de-serializes that Object.  
	 */
	public static Object getBaselineProfileData(String profileName)
	{
		try
		{
			String profilePath = getProfilePath(profileName); 
			File fl=new File(profilePath);
					
			FileInputStream fin=null;
			ObjectInputStream oin=null;
			
			Object obj = null;
			
			if(fl.exists() && fl.length()!=0)
			{
				fin = new FileInputStream(fl.getAbsolutePath());	
				oin = new ObjectInputStream(fin);
				obj = oin.readObject();
			}

			if(fin != null)
				fin.close();
			if(oin != null)
				oin.close();
			
			return obj;
			
		}catch(Exception e){
				e.printStackTrace();
		}
		return null;
	}

	/**
	 * Check whether the predefined baseline data exists.
	 * @param name predefined profile name
	 * @return true if previously downloaded baseline data exists.
	 */
	public static boolean isBaselineDataExist(BaselineProfile profile)
	{
		String epocRoot=profile.getSdkEpocRoot();
			
		if(!"".equals(epocRoot))
		{
			File epocRootFolder = new File(epocRoot);		
			if(epocRootFolder.exists())
			{
				//checking include and lib folders are exist or not
				File epocFolder = new File(FileMethods.appendPathSeparator(epocRoot) + EPOC_PATH);
				File incFolder = new File(FileMethods.appendPathSeparator(epocRoot) + EPOC_INC_PATH);
				File libFolder = new File(FileMethods.appendPathSeparator(epocRoot) + ARMV5_PATH);
				if(epocFolder.exists() && incFolder.exists() && libFolder.exists())
					return true;
			}
		}
		return false;
	}

	/**
	 * Used to know whether to download the predefined profiles or not.
	 * 
	 * @param baselineProfileURL
	 * @return true if URL is updated in server.
	 */
	private static boolean isUrlUpdatedForBaselineProfile(BaselineProfile serverProfile, BaselineProfile localProfile)
	{
		String localURL = localProfile.getSdkUrl();
		String serverURL = serverProfile.getSdkUrl();
		
		if(localURL!=null && serverURL!=null && localURL.equalsIgnoreCase(serverURL))
			return false;
		
		return true;
	}

	/**
	 * Checks if the given profile exists or not.
	 * If given profile is predefined, it checks for the paths "epoc32\\include" and
	 * "epoc32\\release\\armv5\\lib" and returns true/false based on the existence/non-existence of these paths.
	 */
	public static boolean isProfileExist(String name,boolean isPredefined)
	{
		Object obj = BaselineProfileUtils.getBaselineProfileData(name);
		if(obj instanceof BaselineProfile)
		{
			if(isPredefined)
			{
				BaselineProfile profile=(BaselineProfile)obj;
				File epocRoot = new File(profile.getSdkEpocRoot());
				File includeFolder=new File(FileMethods.appendPathSeparator(profile.getSdkEpocRoot())+ EPOC_INC_PATH);
				File libFolder=new File(FileMethods.appendPathSeparator(profile.getSdkEpocRoot())+ ARMV5_PATH);
				return epocRoot.exists() && includeFolder.isDirectory() && includeFolder.exists() && libFolder.isDirectory() && libFolder.exists();
			}
			return true;
		}
		return false;
	}

	/**
	 * It is used to get the list of profiles. That includes, last created user profiles and predefined
	 * web server profiles.
	 * @return profile names array
	 */
	public static String[] getAllBaselinesProfiles() {
		SavingUserData prevData=new SavingUserData();
		String[] lastUsedProfiles = prevData.getPreviousValues(SavingUserData.ValueTypes.PROFILENAME);
		ArrayList<String> final_list = new ArrayList<String>();
		if(lastUsedProfiles!=null)
			for(String name:lastUsedProfiles)
			{
				Object obj = BaselineProfileUtils.getBaselineProfileData(name);
				if(obj instanceof BaselineProfile)
				{
					BaselineProfile profile = (BaselineProfile)obj;
					if(profile.isPredefined())
					{
						if(BaselineProfileUtils.isBaselineDataExist(profile))
							final_list.add(name);
						else
							deleteProfile(name);
					}
					else
						final_list.add(name);
				}
			}
		IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		String serverUrl=prefStore.getString(CompatibilityAnalyserPreferencesConstants.BASELINES_URL);
		if(!serverUrl.endsWith("/"))
			serverUrl = serverUrl + "/";
		ArrayList<String> webServerProfiles = new ArrayList<String>();
		
		String status = BaselineProfileUtils.readPredefinedBaselineProfilesFromWebserver(serverUrl, METADATAFILE, webServerProfiles);
		if(status==null)
		{
			//Now combine two lists
			for(String profile:webServerProfiles)
				if(final_list.indexOf(profile)==-1)
					final_list.add(profile);
		}
		
		return final_list.toArray(new String[0]);
	}
	
	/**
	 * This function deletes the profile on the file system using the given name.
	 * Also deletes the name from the previous stored data
	 * @param profileName
	 */
	public static void deleteProfile(String name)
	{
		SavingUserData prevData=new SavingUserData();;
		File fl = new File(getProfilePath(name));
		if(fl.exists())
		{
			fl.delete();			
			prevData.deleteProfile(name);
		}
		else
			prevData.deleteProfile(name);

	}
	
	/**
	 * This method checks if the given profile already exists in the local filesystem.
	 * If it exists, it checks if the version and urls of the given profile match with the
	 * version and url of existing profile.
	 * @param profile
	 * @return true if downloading is needed, else false.
	 */
	private static boolean predefinedNeedsDownload(BaselineProfile profile)
	{
		Object obj = getBaselineProfileData(profile.getProfileName());
		
		if(obj instanceof BaselineProfile)
		{
			//There is already a profile with this name in local file system
			//Now, validate the one in server with the existing one.
			
			BaselineProfile existing = (BaselineProfile)obj;
			
			//First validate the paths in old one
			boolean isPathsValid = isBaselineDataExist(existing);
			//Validate the URL modification date.
			boolean isUrlUpdated = isUrlUpdatedForBaselineProfile(profile, existing);
			
			if(existing.isPredefined() && 
					profile.getS60version().equals(existing.getS60version()) &&
					isPathsValid &&	!isUrlUpdated)
					return false;
			
		}
		return true;

	}
	
	private static String getProfilePath(String profileName)
	{
		return CompatibilityAnalyserPlugin.stateLocation + File.separator + BASELINE_PROFILES_FOLDER + File.separator + profileName+Messages.getString("CompatibilityAnalyserEngine.data");
	}

	/**
	 * Checks whether the given name is predefined profile or not.
	 * @param name
	 * @return true if there is predefined baseline profile with this name
	 */
	public static boolean isPredefinedProfile(String name) {
		Object obj = BaselineProfileUtils.getBaselineProfileData(name);
		return obj instanceof BaselineProfile && ((BaselineProfile)obj).isPredefined();
	}

}
