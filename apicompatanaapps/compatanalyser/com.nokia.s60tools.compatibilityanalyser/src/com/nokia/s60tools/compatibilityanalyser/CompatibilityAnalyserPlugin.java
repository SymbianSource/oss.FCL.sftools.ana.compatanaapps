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
* Description: This activator class controls the plug-in life cycle
*
*/
package com.nokia.s60tools.compatibilityanalyser;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.nokia.carbide.cpp.sdk.core.ISDKManager;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;
import com.nokia.s60tools.compatibilityanalyser.data.ToolChain;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalysisConsole;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.sdk.RVCTToolChainInfo;
import com.nokia.s60tools.sdk.SdkManager;
import com.nokia.s60tools.sdk.SdkEnvInfomationResolveFailureException;

/**
 * The activator class controls the plug-in life cycle
 */
public class CompatibilityAnalyserPlugin extends AbstractUIPlugin { 

	// The plug-in ID
	public static final String PLUGIN_ID = "com.nokia.s60tools.compatibilityanalyser"; //$NON-NLS-1$
	public static final String CORE_COMPONENTS_PLUGIN_ID = "com.nokia.s60tools.compatibilityanalyser.corecomponents";
	
	// The shared instance 
	private static CompatibilityAnalyserPlugin plugin;
	public static int DATA_VERSION=6;
	
	private String pluginInstallPath = ""; //$NON-NLS-1$
		
	private final String  RVCT_BIN_ENV_VARIABLE = Messages.getString("CompatibilityAnalyserPlugin.RVCT22BIN"); //$NON-NLS-1$
	private final String RVCT_FROM_ELF_EXECUTABLE = Messages.getString("CompatibilityAnalyserPlugin.fromelf"); //$NON-NLS-1$
	
	public static IPath stateLocation;

	private static ISDKManager sdkMgr = SDKCorePlugin.getSDKManager();
	private static long lastModTime;
	
	/**
	 * The logic to scan sdks installed in the system, takes some time.
	 * So to save the time during wizard launching, this is executed in static block
	 * and the last modification time of devices.xml file is stored.
	 */
	static{
		try{
			sdkMgr.scanSDKs();
			lastModTime = sdkMgr.getDevicesXMLFile().lastModified();
		}catch(Exception e){}
	}
	/**
	 * The constructor
	 */
	public CompatibilityAnalyserPlugin() {
		plugin = this;
	}
		

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		initializeToolChains();
		String pluginInstallLocation = getPluginInstallPath();
		String imagesPath = getImagesPath(pluginInstallLocation);
		
		// Loading images required by this plug-in
		ImageResourceManager.loadImages(imagesPath);
		
		//gets plugin state location i.e: &(workspace)\.metadata\.plugins\$(pluginFolder)
		stateLocation = Platform.getStateLocation(this.getBundle());//(or) getStateLocation();
		
	}

	
	/**
	 * Gets images path relative to given plugin install path.
	 * @param pluginInstallPath Plugin installation path.
	 * @return Path were image resources are located.
	 * @throws IOException
	 */
	private String getImagesPath(String pluginInstallPath) {
		if(!"".equals(pluginInstallPath))
			return pluginInstallPath + File.separator + "icons";
		
		return "";
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * @return the shared instance
	 */
	public static CompatibilityAnalyserPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the preferences store object
	 * 
	 */

	public static IPreferenceStore getCompatabilityAnalyserPrefStore()
	{
		return getDefault().getPreferenceStore();
	}
	
	/**
	 * The method checks if devices.xml has been modified. 
	 * If so, it re-scans for loaded sdks. 
	 * @return all the SDK s installed in your local file system.
	 */
		
	public static List<ISymbianSDK> fetchLoadedSdkList()
	{
		if(sdkMgr.getDevicesXMLFile().lastModified() > lastModTime)
		{
			sdkMgr.scanSDKs();
		}
		return sdkMgr.getSDKList();
	
	}
	/**
	 * Returns the plugin installation path.
	 */
	public static String getPluginInstallPath() {
		try {
			  if ("".equals(plugin.pluginInstallPath)) {  //$NON-NLS-1$
					 // URL to the plugin's root ("/")
					URL relativeURL = plugin.getBundle().getEntry("/"); //$NON-NLS-1$
					//	Converting into local path
					URL localURL = FileLocator.toFileURL(relativeURL);
					//	Getting install location in correct form
					File f = new File(localURL.getPath());
					plugin.pluginInstallPath = f.getAbsolutePath();
				}
				return plugin.pluginInstallPath;
			} catch (Exception e) {
				return ""; //$NON-NLS-1$
			}
		} 
	
	/**
	 * Returns the installation path of default coretools package.
	 */
	public static String getInstallPathOfToolsPlugin()
	{
		try
		{
			Bundle bd = Platform.getBundle(CORE_COMPONENTS_PLUGIN_ID); //$NON-NLS-1$
			URL relativeURL = bd.getEntry("/"); //$NON-NLS-1$
			//	Converting into local path
			URL localURL = FileLocator.toFileURL(relativeURL);
			//	Getting install location in correct form
			File f = new File(localURL.getPath());
					
			return f.getAbsolutePath();
			
		}catch(Exception e){
			return ""; //$NON-NLS-1$
		}
		
	}
	/**
	 * Returns the path of BCTools folder in default core tools package.
	 * If the path does not exist, it returns null.
	 *
	 */
	public static String getDefaltCoretoolsPath()
	{
		String corePath = CompatibilityAnalyserPlugin.getInstallPathOfToolsPlugin();
		
		if(corePath == null || corePath.equalsIgnoreCase("")) //$NON-NLS-1$
		{
			return null;
		}
		else
		{
			File toolsFolder = new File(FileMethods.appendPathSeparator(corePath) + Messages.getString("HeaderAnalyserEngine.BCTools")); //$NON-NLS-1$
			if(toolsFolder.exists())
				return FileMethods.appendPathSeparator(corePath) + Messages.getString("HeaderAnalyserEngine.BCTools") + File.separator;
				
			return null;
		}
	}
	
	/**
	 * 
	 * @return the proxy service 
	 */
	public IProxyService getProxyService()
	{
		BundleContext context = this.getBundle().getBundleContext();
		ServiceReference serviceRef = context.getServiceReference(IProxyService.class.getName());
		
		if(serviceRef != null)
			return (IProxyService)context.getService(serviceRef);
		
		return null;
	}
	/**
	 * This method intializes the Tool chain paths of GCCE and RVCT Tools.
	 */
	private void initializeToolChains()
	{
		ToolChain gcceToolChain = new ToolChain(Messages.getString("CompatibilityAnalyserPlugin.GCCE"), Messages.getString("CompatibilityAnalyserPlugin.CSLARMToolChain")); //$NON-NLS-1$ //$NON-NLS-2$
		ToolChain rvctToolChain = new ToolChain(Messages.getString("CompatibilityAnalyserPlugin.RVCT"), Messages.getString("CompatibilityAnalyserPlugin.RealViewCompilerToolChain")); //$NON-NLS-1$ //$NON-NLS-2$
		
		try
		{
			String gccePath = SdkManager.getCSLArmToolchainInstallPathAndCheckReqTools();
			
			if(gccePath != null)
			{
				gcceToolChain.setToolChainPath(FileMethods.convertForwardToBackwardSlashes(gccePath));
				gcceToolChain.setInstalled(true);
			}
		}catch(SdkEnvInfomationResolveFailureException  e){
			gcceToolChain.setInstalled(false);
			e.printStackTrace();
		}
			//String rvctTolPath = resolveRvctToolChainPath();
			
		try
		{
			RVCTToolChainInfo rvctChain = resolveRvctToolChain();
						
			if(rvctChain != null)
			{
				rvctToolChain.setToolChainPath(FileMethods.convertForwardToBackwardSlashes(rvctChain.getRvctToolBinariesDirectory()));
				rvctToolChain.setVersion(rvctChain.getRvctToolsVersion());
				rvctToolChain.setInstalled(true);
			}
		}catch(Exception e){
			rvctToolChain.setInstalled(false);
			e.printStackTrace();
		}

		if(!gcceToolChain.isInstalled() && !rvctToolChain.isInstalled())
		{
			CompatibilityAnalysisConsole.getInstance().println(Messages.getString("CompatibilityAnalyserPlugin.NeitherGCCEnorRVCTInstalled") + //$NON-NLS-1$
					Messages.getString("CompatibilityAnalyserPlugin.ToRunOCneedMinOneTool")); //$NON-NLS-1$
		}
		if(gcceToolChain.isInstalled())
			CompatibilityAnalyserEngine.addToolChain(gcceToolChain);
		if(rvctToolChain.isInstalled())
			CompatibilityAnalyserEngine.addToolChain(rvctToolChain);
		
	 }
	/**
	 * This method Returns the RVCT toolchain, installed in the system.
	 * If more than one RVCT Tools are found:
	 * a)If the variable RVCT22BIN is set, this method returns the toolchain,
	 * whose Bin directory is same as the path to which this variable is set to.
	 * b Else the method returns the toolchain that is installed recently.  
	 * @return RVCT ToolChain 
	 */
	private RVCTToolChainInfo resolveRvctToolChain() {
		RVCTToolChainInfo[] rvctTools = SdkManager.getInstalledRVCTTools();
						
		String defaultRvctBinDir = System.getenv(RVCT_BIN_ENV_VARIABLE);
		
		if(rvctTools.length > 1){
			// Has user defined the default chain in environment variables?
			if(defaultRvctBinDir != null){
				// Yes using the default if can be found among the registered toolchains
				for (int i = 0; i < rvctTools.length; i++) {
					String binDir = rvctTools[i].getRvctToolBinariesDirectory();
					if(binDir.equalsIgnoreCase(defaultRvctBinDir)){
						//rvctVersion = rvctTools[i].getRvctToolsVersion();
						return rvctTools[i];
					}
				}
			}
			// Environment variable is not defined or
			// there was no match => Using the newest binaries.
			return useTheNewestAvailableRvctToolbinaries(rvctTools);							
		}
		else if(rvctTools.length == 1){
			// There is only one possibility
			return useTheFirstAvailableToolchain(rvctTools);
		}
		else{
			return null;

		}
	}
	private RVCTToolChainInfo useTheFirstAvailableToolchain(RVCTToolChainInfo[] rvctTools) {
		return rvctTools[0];
	}
	
	/**
	 * This method returns the RVCT Tool chains that is installed recently.
	 * The method uses the modified time of fromelf.exe program to find the 
	 * Installation time of RVCT Tool chain.
	 * @param rvctTools specifies an array of RVCTToolChainInfo
	 * @return the path of newly Installed RVCT Toolchain
	 */
	
	private RVCTToolChainInfo useTheNewestAvailableRvctToolbinaries(RVCTToolChainInfo[] rvctTools) {
		
		RVCTToolChainInfo rvctTemp = null;
		long lastModifiedStored = 0;
		for (int i = 0; i < rvctTools.length; i++) {
			String binDirTmp = rvctTools[i].getRvctToolBinariesDirectory();
			String elfProg = binDirTmp
							 + File.separatorChar 
							 + RVCT_FROM_ELF_EXECUTABLE;
			File elfFile = new File(elfProg);
			if(elfFile.exists()){
				long modified = elfFile.lastModified();
				if(modified > lastModifiedStored){
					lastModifiedStored = modified;
					//binDirStored = binDirTmp;
					rvctTemp = rvctTools[i];
				}
			}
		}
		return rvctTemp;
	}
	
}
