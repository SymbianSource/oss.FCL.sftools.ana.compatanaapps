package com.nokia.s60tools.compatibilityanalyser.test;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.nokia.s60tools.compatibilityanalyser.test";

	// The shared instance
	private static Activator plugin;

	public String pluginInstallPath = "";

	
	public static String SDK_NAME = "S60_5th_Edition_SDK_v0.9_2";
	public static String TARTGET_TYPE = "armv5";
	public static String PROFILE_NAME = "abcd";
	public static String SDK_VERSION = "5.0";
	public static String EPOCROOT = "C:\\S60\\devices\\S60_5th_Edition_SDK_v0.9_2";
	
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static String getPluginInstallPath() {
		try {
			  if (plugin.pluginInstallPath == "") { 
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

}
