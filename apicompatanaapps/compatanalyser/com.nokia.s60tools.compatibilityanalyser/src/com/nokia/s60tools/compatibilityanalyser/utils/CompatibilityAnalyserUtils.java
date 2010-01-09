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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.sdk.SdkInformation;
import com.nokia.s60tools.sdk.SdkManager;

public class CompatibilityAnalyserUtils {

	private static final String STR_WILDCHARD_WIN = Messages.getString("CompatibilityAnalyserEngine.WIN");  //$NON-NLS-1$
	private static final String STR_WILDCHARD_TOOLS = Messages.getString("CompatibilityAnalyserEngine.TOOLS");  //$NON-NLS-1$
	private static final String GLOBAL_DATA_FILENAME = "global_data.xml";
	private static String temp;
	
	/**
	 * This method checks all avialable proxies for given uri and returns one of them
	 * @param sourceUri uri which has to be accessed.
	 * @return the proxy for the given URI
	 */
	public static Proxy getProxyForUri(URI sourceUri)
	{
		IProxyService proxyService = CompatibilityAnalyserPlugin.getDefault().getProxyService();

		Proxy proxy = null;
		IProxyData proxyData = null;

		if(proxyService != null)
		{
			IProxyData[] proxies = proxyService.select(sourceUri);

			if(proxies.length > 0)
				proxyData = proxies[0];
		}

		if(proxyData != null)
		{
			String host = proxyData.getHost();
			String pType = proxyData.getType();
			int port = proxyData.getPort();

			Proxy.Type proxyType = Proxy.Type.valueOf(pType);
			InetSocketAddress address = new InetSocketAddress(host, port);
			proxy = new Proxy(proxyType, address);
		}

		return proxy;
	}

	/**
	 * @param sdk Input SDK.
	 * @return Supported Platforms of given SDK
	 * This method ignores platforms which start with WIN and TOOLS.
	 * Also, platforms which does not contain any BuildTypes will be ignored.
	 */
	public static String [] getPlatformList(ISymbianSDK sdk)
	{
		if(sdk == null)
			return null;
		else
		{
			ArrayList<String> supportedPlatforms = new ArrayList<String>();
			try
			{

				SdkInformation [] sdkInfo = SdkManager.getSdkInformation(); 

				for(SdkInformation info: sdkInfo)
				{
					if(info.getSdkId().equals(sdk.getUniqueId()))
					{
						String [] platforms = info.getPlatforms();

						for(String s:platforms)
						{
							if(s.toUpperCase(Locale.ENGLISH).startsWith(STR_WILDCHARD_WIN) ||
									s.toUpperCase(Locale.ENGLISH).startsWith(STR_WILDCHARD_TOOLS))
								continue;

							String [] builds = info.getBuildTypesForPlatform(s);
							if(builds != null && builds.length > 0)
							{
								String dir = info.getReleaseRootDir()+File.separator+s;
								if(new File(dir+"\\lib").exists() && new File(dir+"\\urel").exists())
									supportedPlatforms.add(s.toLowerCase());
							}
						}
					}
				}
			}catch(Exception e){
				return null;
			}

			return (String [])supportedPlatforms.toArray(new String[supportedPlatforms.size()]);
		}
	}

	/**
	 * Gets the path of core tools selected in preferences.
	 * @return path of core tools
	 */
	private static String getSelectedCoretoolsPath()
	{
		String path = null;
		IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_TOOLS))
		{
			path = FileMethods.appendPathSeparator(CompatibilityAnalyserPlugin.getInstallPathOfToolsPlugin()) + Messages.getString("HeaderAnalyserEngine.BCTools");
		}
		else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS))
		{
			path = prefStore.getString(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH);
		}
		else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS))
		{
			path = prefStore.getString(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS_PATH);
		}
		else if(prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_TOOLS))
		{
			path = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH);
		}
		
		if((new File(path).exists()))
			return path;
		
		return null;
	}
	
	/**
	 * Returns the {@link GlobalDataReader} object
	 * @return
	 */
	public static GlobalDataReader getGlobalDataReader()
	{
		String path = getSelectedCoretoolsPath(); 
		File xmlFile = new File(FileMethods.appendPathSeparator(path)+GLOBAL_DATA_FILENAME);
		GlobalDataReader reader = null;
		if(xmlFile.exists())
			reader = new GlobalDataReader(xmlFile);
		return reader;
	}
	
	/**
	 * Starts the downloading core tools from web server with progress monitor.
	 */
	public static String initiateDownloadingCoreTools()
	{
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
				String urlInPref = prefStore.getString(CompatibilityAnalyserPreferencesConstants.CORETOOLS_URL);
				//If core tools were already downloaded from web server earlier, they will be deleted. 
				String previousContents = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR);
				if(previousContents != null && !previousContents.equals("")) //$NON-NLS-1$
				{
					File oldContents = new File(previousContents);
					if(oldContents.exists())
						FileMethods.deleteFolder(previousContents);
				}
				String targetPath = FileMethods.appendPathSeparator(CompatibilityAnalyserEngine.getWorkspacePath()) + Messages.getString("HeaderAnalyserEngine.WebServerContentsNew"); //$NON-NLS-1$
				String coreToolsExtraction = CompatibilityAnalyserEngine.readAndDownloadSupportedCoretools(urlInPref, targetPath, monitor);
				if(coreToolsExtraction == null)
				{
					temp = null;
					prefStore.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_URL, urlInPref);
					prefStore.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH, CompatibilityAnalyserEngine.getWebServerToolsPath());
					prefStore.setValue(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_ROOTDIR, targetPath);
				}
				else
					temp = coreToolsExtraction;
			}
		};
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		Shell shell = win != null ? win.getShell() : null;
		try {
			new ProgressMonitorDialog(shell).run(true, true, op);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return "Error!";
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "Error!";
		}
		
		//If download not successful 
		if(temp != null)
			return temp;
		
		return null;
	}
}
