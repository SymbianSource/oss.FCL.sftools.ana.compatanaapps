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
 * Description: This contains various utility methods.
 *
 */
package com.nokia.s60tools.compatibilityanalyser.model;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.Document;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.EpocEngineHelper;
import com.nokia.carbide.cdt.builder.EpocEnginePathHelper;
import com.nokia.carbide.cdt.builder.ICarbideBuildManager;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.ReportData;
import com.nokia.s60tools.compatibilityanalyser.data.ToolChain;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalyserUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.FileMethods;
import com.nokia.s60tools.compatibilityanalyser.utils.GlobalDataReader;
import com.nokia.s60tools.util.python.PythonUtilities;

/**
 * This is an engine class which contains utility methods.
 *
 */

public class CompatibilityAnalyserEngine {

	public static final String ARMV5_PATH = "epoc32\\release\\armv5\\lib";
	public static final String SUPPORTED_PYTHON_VERSION = "2.4";

	public static final String H_EXTN = ".h";
	public static final String HRH_EXTN = ".hrh";
	public static final String MBG_EXTN = ".mbg";
	public static final String RSG_EXTN = ".rsg";
	public static final String HPP_EXTN = ".hpp";
	public static final String PAN_EXTN = ".pan";

	static ArrayList<String> reportNames = new ArrayList<String>();
	static ArrayList<ReportData> reportObjects = new ArrayList<ReportData>();

	private static String epocFolderPath = null;
	private static String coreToolsRoot = null;
	private static Vector<ToolChain> toolChains = new Vector<ToolChain>();

	ProductSdkData currentSdk;
	BaselineSdkData baselineData;

	private boolean runHA;
	private boolean runOC;
	private String baselineProfile;

	public static enum ElementTypes{FolderType, FileType};

	/*
	 * Constructor
	 */
	public CompatibilityAnalyserEngine()
	{
		currentSdk = new ProductSdkData();
	}


	/**
	 * @param CurrentSDK Data
	 * Each Analysis runs with its own Current SDK data.
	 * This method is used to set Current SDK data for the Engine 
	 */
	public void setCurrentSdkData(ProductSdkData data)
	{
		this.currentSdk = data;
	}

	/**
	 * Returns Current SDK data.
	 */
	public ProductSdkData getCurrentSdkData()
	{
		return this.currentSdk;
	}
	/**
	 * @param tools specifies the tool chain to be added to the list.
	 * The tool maintains list of GCCE and RVCT toolchains installed in the machine.
	 * It fetches this list during startup and displays the tools in ordinal analysis page of Analysis wizard. 
	 */
	public static void addToolChain(ToolChain tools)
	{
		toolChains.add(tools);
	}

	/**
	 * @return List of RVCT and GCCE Tools, installed in the machine
	 */
	public static Vector<ToolChain> getToolChains()
	{
		return toolChains;
	}


	/**
	 * This method returns the target path of all Libs/Dsos for a particular Build configuration
	 * @param  selectedSDK specifies the SDK against which selected files are built.
	 * @param platformName specifies the platform against which selected files are built
	 * @return target path of generated Libs/Dsos.
	 */
	public static String getLibsPathFromPlatform(ISymbianSDK selectedSDK, String platformName) {
		try
		{
			String releasePath=FileMethods.appendPathSeparator(selectedSDK.getReleaseRoot().toString());
			return  FileMethods.convertForwardToBackwardSlashes(releasePath + platformName + File.separator + Messages.getString("CompatibilityAnalyserEngine.lib")); //$NON-NLS-1$ //$NON-NLS-2$
		}catch(NullPointerException e)
		{
			return null;
		}
	}

	/**
	 * This method returns the target path of all dll for a particular Build configuration
	 * @param  selectedSDK specifies the SDK against which selected files are built.
	 * @param platformName specifies the platform against which selected files are built
	 * @return target path of generated dll.
	 */
	public static String getDllPathFromPlatform(ISymbianSDK selectedSDK, String platformName) {
		try
		{
			String releasePath=FileMethods.appendPathSeparator(selectedSDK.getReleaseRoot().toString());
			return  FileMethods.convertForwardToBackwardSlashes(releasePath + platformName + File.separator + Messages.getString("CompatibilityAnalyserEngine.dll")); //$NON-NLS-1$ //$NON-NLS-2$
		}catch(NullPointerException e)
		{
			return null;
		}
	}


	public void setBaselineProfile(String profileName)
	{
		this.baselineProfile = profileName;	
	}
	public String getBaselineProfile()
	{
		return baselineProfile;
	}
	/*
	 * This method specifies whether current analysis involves Header Files or not.  
	 */
	public void setHeaderAnalysis(boolean ha)
	{
		this.runHA = ha;
	}

	public boolean isHeaderAnalysisChecked()
	{
		return this.runHA;
	}

	public boolean isLibraryAnalysisChecked()
	{
		return this.runOC;
	}
	/*
	 * This method specifies whether current analysis involves Library Files or not.  
	 */
	public void setLibraryAnalysis(boolean oc){
		this.runOC = oc;
	}

	/**
	 * Read all the contents from the given BaselineProfile object 
	 * @param profile is the Deserialized BaselineProfile Object
	 * @return an object of BaselineSdkData with its fields set to corresponding 
	 * values from Baseline Profile.
	 */
	public BaselineSdkData readBaselineSdkData(BaselineProfile profile)
	{
		if(profile == null)
		{
			return null;
		}
		BaselineSdkData baselineData = new BaselineSdkData();

		baselineData.baselineSdkName = profile.getProfileName();
		baselineData.baselineSdkVersion = profile.getS60version();

		baselineData.epocRoot = profile.getSdkEpocRoot();

		if(profile.isRadio_default_hdr())
		{
			baselineData.baselineHeaderDir = new String[1];
			baselineData.baselineHeaderDir[0] = FileMethods.convertForwardToBackwardSlashes(FileMethods.appendPathSeparator(profile.getSdkEpocRoot()) + Messages.getString("CompatibilityAnalyserEngine.epoc32include")); //$NON-NLS-1$
		}
		else{
			baselineData.baselineHeaderDir = new String [profile.getHdr_dir_path().length];
			baselineData.baselineHeaderDir = profile.getHdr_dir_path();
		}

		if(profile.getSystemInc_dirs() == null || profile.getSystemInc_dirs().length == 0)
		{
			baselineData.baselineIncludes = "";
		}
		else
		{
			StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
			for(String s: profile.getSystemInc_dirs())
			{
				sb.append(s);
				sb.append(";"); //$NON-NLS-1$
			}
			baselineData.baselineIncludes = sb.toString();
		}

		if(profile.isRadio_default_build_target())
		{
			//Do nothing
			//By default the analysis will be done with default build target
		}
		else if(profile.isRadio_build_target())
		{
			if(profile.getBuild_config() != null)
			{
				String [] lib_dirs = new String[profile.getBuild_config().length];
				String [] dll_dirs = new String[profile.getBuild_config().length];

				for(int i =0; i < profile.getBuild_config().length; i++)
				{
					lib_dirs[i] = FileMethods.convertForwardToBackwardSlashes(profile.getSdkEpocRoot() + File.separator + Messages.getString("CompatibilityAnalyserEngine.epocRelease") + File.separator + profile.getBuild_config()[i] + File.separator + Messages.getString("CompatibilityAnalyserEngine.lib"));
					dll_dirs[i] = FileMethods.convertForwardToBackwardSlashes(profile.getSdkEpocRoot() + File.separator + Messages.getString("CompatibilityAnalyserEngine.epocRelease") + File.separator + profile.getBuild_config()[i] + File.separator + Messages.getString("CompatibilityAnalyserEngine.dll"));
				}
				baselineData.baselineLibsDir = lib_dirs;
				baselineData.baselineDllDir = dll_dirs;
			}
		}
		else
		{
			baselineData.baselineLibsDir = profile.getLib_dir();
			baselineData.baselineDllDir = profile.getDll_dir();
		}

		baselineData.forcedHeaders = profile.getForced_headers();

		return baselineData;
	}

	/**
	 * @return the current workspace path
	 */
	public static String getWorkspacePath() {
		try{
			IPath location = Platform.getLocation();
			return location.toOSString();
		}catch(IllegalStateException e){
			return "";
		}
	}
	/*
	 * @return an array of all CompatibilityAnalysis Report files created
	 * during the analysis.
	 */
	public static Object [] getReportFiles()
	{
		if(reportObjects != null)
			return reportObjects.toArray(new Object[0]);
		return null;
	}

	/*
	 * This method clears previous Report files
	 */
	public static void clearReportFiles()
	{
		if(reportNames != null)
			reportNames.clear();
		if(reportObjects != null)
			reportObjects.clear();
	}

	/**
	 * @return the path of CoreTools Extracted from WebServer
	 */
	public static String getWebServerToolsPath()
	{
		return coreToolsRoot;
	}

	/**
	 * @return the path of "include" folder for predefined baseline profiles
	 */
	public static String getEpocFolderPath()
	{
		return epocFolderPath;
	}
	/**
	 * This Method Downloads given zip file from WebServer and
	 * stores this to specified location.
	 * @param sourceUrl specifies URL of WebServer
	 * @params type and key specify the Type and Name of File to be searched during extraction 
	 * @param monitor to display progress of Download
	 * @return error message if any
	 */
	public static String downloadAndExtractFileFromWebServer(String sourceUrl, String targetPath, ElementTypes type, String key, IProgressMonitor monitor)
	{
		try
		{
			URL url = new URL(sourceUrl);
			URI uri = new URI(sourceUrl);

			// Get the zip file name
			String fileName = null;
			if(url.getFile().contains("/"))
			{
				int index = url.getFile().lastIndexOf("/"); 
				fileName = url.getFile().substring(index + 1); 
			}
			else
				fileName = url.getFile();

			if(monitor != null){
				monitor.beginTask(Messages.getString("CompatibilityAnalyserEngine.ExtractingTools") + " "+ fileName + " " + Messages.getString("CompatibilityAnalyserEngine.FromWebServer"), 10); //$NON-NLS-1$
				monitor.subTask(""); //$NON-NLS-1$
			}

			URLConnection conn = null;

			Proxy proxy = CompatibilityAnalyserUtils.getProxyForUri(uri);

			if(proxy != null){
				conn = url.openConnection(proxy);
			}
			else{
				conn = url.openConnection();
			}
			conn.connect();

			if(!conn.getContentType().contains(Messages.getString("CompatibilityAnalyserEngine.zip"))) //$NON-NLS-1$
				return new String(Messages.getString("CompatibilityAnalyserEngine.URLdoesNotMapWithZip")); //$NON-NLS-1$

			String webServerzipFilePath = targetPath + File.separator + fileName;
			File targetZip = new File(webServerzipFilePath);
			targetZip.getParentFile().mkdirs();
			InputStream inStr = conn.getInputStream();
			FileOutputStream fileStr = new FileOutputStream(webServerzipFilePath);

			if(monitor!=null)
				monitor.subTask("Downloading...");

			byte [] arr = new byte[1024];
			int i = 0;
			while((i =inStr.read(arr))!=-1 )
			{
				fileStr.write(arr, 0, i);
				if(monitor != null && monitor.isCanceled())
				{
					monitor.done();
					if(inStr != null)
						inStr.close();
					if(fileStr != null)
						fileStr.close();

					FileMethods.deleteFolder(targetPath);
					return Messages.getString("CompatibilityAnalyserEngine.CancelledDownloading"); //$NON-NLS-1$
				}
			}
			if(inStr != null)
				inStr.close();
			if(fileStr != null)
				fileStr.close();
			if(monitor != null)
			{
				monitor.worked(3);
				monitor.subTask("Completed downloading");
			}
			String extractionStatus = extractZipContent(webServerzipFilePath, targetPath,type,key, monitor);

			targetZip.delete();

			if(extractionStatus == null)
			{
				if(key.equals(Messages.getString("CompatibilityAnalyserEngine.checkbc.py")) && coreToolsRoot == null)
					return Messages.getString("CompatibilityAnalyserEngine.ErrorGivenZipDoesNotHaveSCRIPT"); //$NON-NLS-1$
				else if(key.equalsIgnoreCase("include") && epocFolderPath == null)
					return "Given zip does not contain 'include' folder. Please check the Baselines URL."; 
			}
			else if(extractionStatus.equals(Messages.getString("CompatibilityAnalyserEngine.IfCancelledDownloading"))) //$NON-NLS-1$
			{
				FileMethods.deleteFolder(targetPath);
				return Messages.getString("CompatibilityAnalyserEngine.CancelledDownloadingATreturn"); //$NON-NLS-1$
			}
			else
				FileMethods.deleteFolder(targetPath);

			if(monitor != null)
				monitor.done();

			return extractionStatus;

		}catch(MalformedURLException e){
			FileMethods.deleteFolder(targetPath);
			return Messages.getString("CompatibilityAnalyserEngine.InvalidURL"); //$NON-NLS-1$
		}catch(IOException e){
			FileMethods.deleteFolder(targetPath);
			return Messages.getString("CompatibilityAnalyserEngine.ErrorinConnectingToWeb") + e.getMessage(); //$NON-NLS-1$
		}catch(URISyntaxException e){
			FileMethods.deleteFolder(targetPath);
			return Messages.getString("CompatibilityAnalyserEngine.InvalidURL"); //$NON-NLS-1$
		} 

	}
	/**
	 * @param source specifies the Source Zip file to be extracted
	 * @param target specifies the target diretcory to which zip should be extracted to
	 * @param monitor is used to report back the progress of extraction
	 * @return any error during the extraction
	 * The method extractes give zip file to given target directory.
	 * It scans all folder entries first. If a folder entry already exists in filesystem it deletes and creates it again.
	 * Similarly,if a file extry already exists in filesystem, it deletes and creates that file again.
	 * 
	 */
	public static String extractZipContent(String source, String target, ElementTypes elementType, String key,IProgressMonitor monitor)
	{
		try 
		{
			ZipFile zip = new ZipFile(source);

			ZipEntry [] zipEntries = new ZipEntry[zip.size()];
			Enumeration<? extends ZipEntry> entries = zip.entries();

			FileOutputStream outStr = null;

			int i =0;
			while(entries.hasMoreElements())
			{
				zipEntries[i] = (ZipEntry)entries.nextElement();
				i++; 					
			}
			int tmp = zipEntries.length /2;

			for (int t = 0; t < zipEntries.length; t++) 
			{
				if(zipEntries[t].isDirectory())
				{
					File folder = new File(target + "\\" + zipEntries[t]); 
					if(folder.exists())
						FileMethods.deleteFolder(folder.getAbsolutePath());
					if(elementType.equals(ElementTypes.FolderType) && folder.getName().equalsIgnoreCase(key))
					{
						if(key.equalsIgnoreCase("include"))
							epocFolderPath = folder.getParentFile().getAbsolutePath();
					}	
				}
			}
			if(monitor != null)
				monitor.subTask("Extracting the zip file...");

			for (int j = 0; j < zipEntries.length; j++) 
			{				
				if(j == tmp && monitor !=null)
					monitor.worked(5);

				if(!zipEntries[j].isDirectory())
				{
					File currentFile = new File(target + "\\" +zipEntries[j]); 
					File parentDir=new File(currentFile.getParent());

					if(!parentDir.exists())
						parentDir.mkdirs();

					if(currentFile.exists())
						currentFile.delete();

					if(currentFile.createNewFile())
					{
						outStr = new FileOutputStream(currentFile);
					}
					if(elementType.equals(ElementTypes.FileType) && currentFile.getName().equalsIgnoreCase(key)) 
					{
						if(key.equalsIgnoreCase("checkbc.py"))
							coreToolsRoot = currentFile.getParentFile().getAbsolutePath();
					}
					InputStream inStr = zip.getInputStream(zipEntries[j]);

					byte [] arr = new byte[1024];
					int k = 0;
					while((k =inStr.read(arr))!=-1 )
					{
						outStr.write(arr, 0, k);
						if(monitor != null && monitor.isCanceled())
						{
							monitor.done();
							if(inStr != null)
								inStr.close();
							if(outStr != null)
								outStr.close();

							zip.close();
							return Messages.getString("CompatibilityAnalyserEngine.CancelledDownloading"); 
						}
					}
					if(inStr!=null)
						inStr.close();

					if(outStr != null)
						outStr.close();
				}
			}

			zip.close();	

			if(monitor != null)
			{
				monitor.worked(8);
				monitor.subTask("Extraction completed");
			}
		} catch (ZipException e) {
			e.printStackTrace();
			return "Unable to open Zip File ";
		}catch (Exception e){
			e.printStackTrace();
			return "Error in reading Zip File " + e.getMessage(); 
		}	

		return null;
	}

	/**
	 * The method reads given metadata file, under given webserver path and 
	 * retreives the entries specified in the metadata file.
	 * @param serverUrl specifies Url of webserver.
	 * @param metadataFilename name of the file to be read for entries, under given Url path
	 * @param issuesList will be updated with the list of entries in metadafile.
	 * @return any error while reading the given metadatafile.
	 */
	public static String readMetadataFileFromWebServer(String serverUrl, String metadataFilename, ArrayList<String> issuesList)
	{
		try 
		{
			if(serverUrl.equals(""))
				return Messages.getString("CompatibilityAnalyserPreferences.26");
			
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

			while((line = br.readLine())!= null)
			{
				line = line.trim();
				if(line.length()>0)
					issuesList.add(line);
			}

			if(issuesList.isEmpty())
				return Messages.getString("CompatibilityAnalyserEngine.NoIssuesFileExistInWebServer") + metadataFilename + " does not contain any entries."; 

		} catch (MalformedURLException e) {
			return e.getMessage();
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
	 * This method gets invoked when User tries to use Core Tools from WebServer.
	 * This method checks whether download of CoreTools is required or not.
	 * The method checks if previous donwloaded Core Tools exist in File System.
	 * If they exist, then it checks whether previous WebServer URL is same as Current URL.
	 * If they are same then it checks the last modification time of CoreTools in WebServer.
	 * If modification time of Tools in WebServer is not higher than the previous downloaded Tools,
	 * then the tool assumes that WebServer Content is not modified and returns false.
	 * In all other cases, the method returns true.
	 * @param currentUrl specfies Current Webserver URL.
	 * @return boolean to indicate the need for Download of CoreTools from WebServer.
	 */
	public static boolean isDownloadAndExtractionNeeded(String currentUrl)
	{
		IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();

		String previousUrl = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_URL);
		String previousPath = prefStore.getString(CompatibilityAnalyserPreferencesConstants.PREVIOUS_WEB_CORE_TOOLS_PATH);

		if(!"".equals(previousPath)) 
		{
			File prevFolder = new File(previousPath);

			if(prevFolder.exists() && previousUrl.equals(currentUrl))
			{
				try 
				{
					URL url = new URL(currentUrl);
					URLConnection connection = url.openConnection();

					Long webServerModTime = connection.getLastModified();

					if(webServerModTime <= prevFolder.lastModified())
						return false;

				}catch (MalformedURLException e) {
					e.printStackTrace();
				}catch(IOException e){
					e.printStackTrace();
				}	
			}
		}
		return true;
	}


	/**
	 * This method fetches list of included headers from each src file of
	 * given IProject. This list would be used as forced headers list during
	 * Header Analysis.
	 */
	public static ArrayList<String> getIncludesFromSrcFiles(IProject project)
	{
		ICarbideBuildManager manager = CarbideBuilderPlugin.getBuildManager();

		ICarbideProjectInfo prjInfo = manager.getProjectInfo(project);

		if(prjInfo != null)
		{
			ArrayList<String> srcArray = new ArrayList<String>();
			ArrayList<String> incFiles = new ArrayList<String>();

			List<IPath> mmpList = EpocEngineHelper.getMMPFilesForProject(prjInfo);

			if(mmpList != null)
			{
				for(IPath mmp: mmpList){
					ICarbideBuildConfiguration config = prjInfo.getDefaultConfiguration();
					List<IPath> srcList = EpocEngineHelper.getSourceFilesForConfiguration(config, mmp);

					for(IPath path: srcList)
						srcArray.add(path.toString());
				}

				for(String s:srcArray)
					getIncFilesFromFile(s, incFiles);
			}
			return incFiles;

		}

		return null;

	}

	/**
	 * This method fetches list of included header files from given file.
	 * The method reads the file and fetches the lines which contain the 
	 * strings #include, < and >
	 * @param name name of the file to be read.
	 * @param list will be updated with the included header file names
	 */
	public static void getIncFilesFromFile(String name, ArrayList<String> list)
	{
		try
		{
			File file = new File(name);
			FileReader fileReader = new FileReader(file);

			BufferedReader reader = new BufferedReader(fileReader);

			String tmp = null;
			while((tmp = reader.readLine()) != null)
			{
				if(tmp.contains("#include") && tmp.contains("<") && tmp.contains(">"))
				{					
					//String incName = tmp.substring(tmp.indexOf("<") +1, tmp.indexOf(">"));

					if(tmp.contains("//") && tmp.indexOf("//") < tmp.indexOf("#"))
						continue;

					String incName = tmp.substring(tmp.indexOf("<") +1, tmp.indexOf(">"));

					if(!list.contains(incName))
						list.add(incName);
				}
			}
			if(reader != null)
				reader.close();
			if(fileReader != null)
				fileReader.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}

	}

	/**
	 * This method scans given source file and fetches list of included header files from its own project.
	 * @param name specifies the name of the source file being scanned.
	 * @param list will be updated with the included header file names.
	 * The method reads the file and fetches the lines which contain the 
	 * strings #include, " and "
	 */
	public static void getLocalIncFilesFromFile(String name, ArrayList<String> list)
	{
		try
		{
			File file = new File(name);
			FileReader fileReader = new FileReader(file);

			BufferedReader reader = new BufferedReader(fileReader);

			String tmp = null;
			while((tmp = reader.readLine()) != null)
			{
				if(tmp.contains("#include") && tmp.contains("\""))
				{					
					//String incName = tmp.substring(tmp.indexOf("<") +1, tmp.indexOf(">"));

					if(tmp.contains("//") && tmp.indexOf("//") < tmp.indexOf("#"))
						continue;

					String incName = tmp.substring(tmp.indexOf("\"") +1, tmp.lastIndexOf("\""));

					if(!list.contains(incName))
						list.add(incName);
				}
			}
			if(reader != null)
				reader.close();
			if(fileReader != null)
				fileReader.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}

	}

	/**
	 * This method scans the given project to fetch the target Dsos for given header file.
	 * @param prjInfo, specifies the details of the carbide project being scanned.
	 * @param header, specifies the input header file.
	 * The method fecthes list of mmps under given project. It scans source files of each mmp,
	 * to find out if any of the source files include given header file. If so, then the target of 
	 * that mmp will be added to the list.
	 */
	public static ArrayList<String> getTargetDsoForHeader(ICarbideProjectInfo prjInfo, String header)
	{
		ArrayList< String> dsoList = new ArrayList<String>();

		List<IPath> mmpList = EpocEngineHelper.getMMPFilesForProject(prjInfo);
		EpocEnginePathHelper helper=new EpocEnginePathHelper(prjInfo.getProject());
		System.out.println(header+" - mmplist - "+mmpList.size());

		for(IPath path:mmpList)
		{
			boolean headerIncluded = false;

			IPath relativePath = helper.convertFilesystemToWorkspace(path);
			IPath targetPath = EpocEngineHelper.getTargetPathForExecutable(prjInfo.getDefaultConfiguration(), relativePath);

			if(targetPath == null || (!targetPath.getFileExtension().equalsIgnoreCase("dll")))
				continue;

			List<IPath> srcList = EpocEngineHelper.getSourceFilesForConfiguration(prjInfo.getDefaultConfiguration(), path);

			if(srcList == null)
				continue;

			for(IPath srcPath:srcList)
			{
				if(isHeaderIncluded(srcPath, header))
				{
					headerIncluded = true;
					break;
				}
			}

			if(headerIncluded)
			{
				String name = targetPath.removeFileExtension().addFileExtension(Messages.getString("ProjectViewPopupAction.dso")).toFile().getName();
				dsoList.add(name);
			}
		}

		return dsoList;
	}

	private static boolean isHeaderIncluded(IPath srcPath, String headerName)
	{
		ArrayList<String> incHdrs = new ArrayList<String>();

		getLocalIncFilesFromFile(srcPath.toString(), incHdrs);
		for(String s:incHdrs)
		{
			if(s.equalsIgnoreCase(headerName))
				return true;
		}
		return false;
	}

	/**
	 * This method scans given Directory path.
	 * Returns list of all supported (*.h, *.hrh, *.rsg and *.mbg) files under the given path
	 * @param dirPath filesystem path to be scanned.
	 */
	public static ArrayList<String> getHeaderFilesFromDir(String dirPath)
	{
		ArrayList<String> filesList = new ArrayList<String>();

		File rootDir = new File(dirPath);

		if(rootDir.isDirectory())
		{
			String [] fileNames = rootDir.list(new FilenameFilter()
			{

				public boolean accept(File file, String name) {
					File temp = new File(FileMethods.appendPathSeparator(file.getAbsolutePath())+ name);

					if(temp.isFile() && isSupportedType(name))
						return true;
					return false;
				}

			});

			if(fileNames != null)
			{
				for(String name:fileNames)
				{
					if(!filesList.contains(name))
						filesList.add(name);

				}
			}
		}
		return filesList;
	}

	public static boolean isSupportedType(String fileName)
	{		
		return fileName.endsWith(H_EXTN) || fileName.endsWith(HRH_EXTN) || fileName.endsWith(RSG_EXTN)
		|| fileName.endsWith(MBG_EXTN) || fileName.endsWith(HPP_EXTN) || fileName.endsWith(PAN_EXTN);

	}

	/** The tool tries to read the Metadata file(coretools_metadata) from the given URL path
	 * If any errors encountered during this metadata file reading, then 
	 * it assumes that given path is that of a zip file and tries to download that.
	 * If any errors occur during this download, they will be thrown back to the user.
	 */
	public static String readAndDownloadSupportedCoretools(String coretoolsUrl, String targetPath, IProgressMonitor monitor)
	{
		String givenUrl = coretoolsUrl;
		String coreToolsExtraction = null;

		if(!givenUrl.endsWith("/"))
			givenUrl = givenUrl + "/";

		ArrayList<String> bctoolsList = new ArrayList<String>();	
		String status = readMetadataFileFromWebServer(givenUrl, Messages.getString("CompatibilityAnalyserEngine.coretools_metadata"), bctoolsList);

		int supportedFound = 0;
		if(status == null)
		{
			String bctoolsUrl;
			for(int i=0;i<bctoolsList.size();i++)
			{
				String versionStr = null;
				if(bctoolsList.get(i).contains(","))
					versionStr = bctoolsList.get(i).substring(bctoolsList.get(i).indexOf(",") +1);
				else
					continue;

				int version = Integer.parseInt(versionStr.trim());

				if(version==CompatibilityAnalyserPlugin.DATA_VERSION){
					supportedFound = 1;		
					bctoolsUrl = givenUrl + bctoolsList.get(i).substring(0,bctoolsList.get(i).indexOf(","));

					coreToolsExtraction = downloadAndExtractFileFromWebServer(bctoolsUrl, targetPath, CompatibilityAnalyserEngine.ElementTypes.FileType, Messages.getString("CompatibilityAnalyserEngine.checkbc.py"), monitor);
				}
				if(supportedFound ==1)
					break;
			}
			if(supportedFound ==0)
				coreToolsExtraction = "No Supported coretools are specified in the metadata file."; 
		}
		else if(status.contains(Messages.getString("CompatibilityAnalyserEngine.NoIssuesFileExistInWebServer")))
			coreToolsExtraction = "No coretools are specified in the metadatafile";
		else
			coreToolsExtraction = downloadAndExtractFileFromWebServer(coretoolsUrl, targetPath, CompatibilityAnalyserEngine.ElementTypes.FileType, Messages.getString("CompatibilityAnalyserEngine.checkbc.py"), monitor);

		return coreToolsExtraction;
	}

	/**
	 * This method stores the analysis configuration from the given document
	 * to a file in local file system.
	 */
	public static void saveConfiguration(Document document, String fileName) throws CoreException, TransformerException{

		// Get instances of the transformer factory and the actual transformer,
		// and transform the DOM-document into a XML configuration file
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", new Integer(4));
		Transformer transformer = factory.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// Transform the DOM to XML
		StreamResult result;

		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));

		if(file == null) {
			result = new StreamResult(new File(fileName));
			transformer.transform(new DOMSource(document), result);
		}
		else {
			result = new StreamResult(new ByteArrayOutputStream());
			transformer.transform(new DOMSource(document), result);
			ByteArrayInputStream in = new ByteArrayInputStream(((ByteArrayOutputStream)result.getOutputStream()).toByteArray());

			// If file already exists, delete it.
			if(file.exists())
				file.delete(true, true, new NullProgressMonitor());

			// Create the configuration file
			file.create(in, true, new NullProgressMonitor());
		}
	}

	/**
	 * @param toolsPath location of coretools
	 * @return version of coretools
	 */
	public static String getCoreToolsVersion(String toolsPath)
	{
		try
		{
			String[] args={"cmd","/c",PythonUtilities.getPythonPath(),"\"" + toolsPath  +"\\CheckBC.py" + "\"" ,"-v"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			Process pr = Runtime.getRuntime().exec(args);//cmd.execute(pythonPath, in, null, path);

			BufferedInputStream br = new BufferedInputStream(pr.getInputStream());
			pr.waitFor();
			StringBuffer bf = new StringBuffer(""); //$NON-NLS-1$
			int r = -1;
			while((r = br.read()) != -1)
			{
				char ch = (char)r;

				if(ch!='-')
					bf.append(ch);
				else
					break;
			}
			return bf.toString();

		}catch(Exception e){
			return null;
		}		
	}

	/**
	 * @param toolsPath location of coretools
	 * @return DataVersion of the coretools
	 */
	public static String getDataVersion(String toolsPath) throws Exception
	{
		try
		{
			File checkbc=new File(toolsPath+File.separator+"checkbc.py");
			if(!checkbc.exists())
				throw new Exception("Checkbc.py does not exist in the selected coretools.");
			String[] args={"cmd","/c",PythonUtilities.getPythonPath(), "\"" + toolsPath + "\\CheckBC.py" + "\"" ,"-dv"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			Process pr = Runtime.getRuntime().exec(args);//cmd.execute(pythonPath, in, null, path);

			BufferedInputStream br = new BufferedInputStream(pr.getInputStream());
			pr.waitFor();
			StringBuffer bf = new StringBuffer(""); //$NON-NLS-1$
			int r = -1;
			while((r = br.read()) != -1)
			{
				char ch = (char)r;
				bf.append(ch);
			}
			return bf.toString();

		}catch(Exception e){
			throw e;
		}		
	}

	/**
	 * Download files from given set of url paths to given target path.
	 * @param sourceURLs 
	 * @param targetPath
	 * @param localPaths contains paths on local file system.
	 * @return any error while downloading.
	 */
	public static String downloadSelectedKnownIssues(String [] sourceURLs, String targetPath, ArrayList<String> localPaths) 
	{
		for(String tempUrl:sourceURLs)
		{
			try 
			{
				URL url = new URL(tempUrl);
				URI uri = new URI(tempUrl);

				String fileName = null;
				if(url.getFile().contains("/")) //$NON-NLS-1$
				{
					int index = url.getFile().lastIndexOf("/");  //$NON-NLS-1$
					fileName = url.getFile().substring(index + 1); 
				}
				else
					fileName = url.getFile();

				URLConnection conn = null;
				Proxy proxy = CompatibilityAnalyserUtils.getProxyForUri(uri);

				if(proxy == null)
					conn = url.openConnection();
				else
					conn = url.openConnection(proxy);

				conn.connect();

				String webServerzipFilePath = targetPath + File.separator + fileName;
				File targetFile = new File(webServerzipFilePath);
				targetFile.getParentFile().mkdirs();
				InputStream inStr = conn.getInputStream();
				FileOutputStream fileStr = new FileOutputStream(webServerzipFilePath);

				localPaths.add(targetFile.getAbsolutePath());

				byte [] arr = new byte[1024];
				int i = 0;
				while((i =inStr.read(arr))!=-1 )
				{
					fileStr.write(arr, 0, i);

					if(inStr != null)
						inStr.close();
					if(fileStr != null)
						fileStr.close();

				}
				if(inStr != null)
					inStr.close();
				if(fileStr != null)
					fileStr.close();

			}catch (MalformedURLException e){
				return new String("Invalid URL " + tempUrl);				
			}catch (IOException e) {
				return new String("Error downloading knownissues " + tempUrl);
			} catch (URISyntaxException e) {
				return new String("Invalid URL " + tempUrl);
			}

		}

		return null;
	}

	/**
	 * This method calculates default build target for a SDK by comparing the amount of DLLs
	 * in each platform against the ones defined in the platform xml file.
	 * @param releaseRoot of the SDK
	 * @param sdkVer S60 version of the SDK
	 * @return default build target
	 */
	public static String getDefaultBuildPlatform(String releaseRoot, String sdkVer)
	{
		String [] buildTargets = null;
		GlobalDataReader xmlReader = CompatibilityAnalyserUtils.getGlobalDataReader();
		if(xmlReader != null)
		{
			String[] supportedTargets = xmlReader.readSupportedBuildTypes();
			if(supportedTargets!=null)
				buildTargets = supportedTargets;
			
			for(String s:buildTargets)
				System.out.println("-> "+s);
		}
		else
			buildTargets = new String[]{"armv5", "armv5_abiv2", "armv6", "armv6t2", "armv7a"};
		
		String version = "";
		if(sdkVer.contains("."))
		{
			String prePart = sdkVer.substring(0, sdkVer.indexOf("."));
			String postPart = sdkVer.substring(sdkVer.indexOf(".") +1, sdkVer.length()); 
			version = prePart+postPart;
		}

		try 
		{
			String data_path = FileMethods.appendPathSeparator(CompatibilityAnalyserPlugin.getDefaltCoretoolsPath()) + "data";
			String dllPath = data_path + File.separator + "s60_dll_data_" + version + ".xml";
			ArrayList<String> dlls = new ArrayList<String>();
			File dllFile = new File(dllPath);
			FileReader reader = new FileReader(dllFile);
			BufferedReader inReader = new BufferedReader(reader);
			String line;

			while((line = inReader.readLine()) != null)
			{
				if(line.contains("<dllname>") &&line.contains("</dllname>"))
				{
					String tmp = line.substring(line.indexOf(">") + 1);
					String dllName = tmp.substring(0, tmp.indexOf("</"));

					dlls.add(dllName.trim());
				}
			}

			String buildTarget = null;

			for (String platform:buildTargets)
			{
				String path = FileMethods.appendPathSeparator(releaseRoot);
				path += platform + File.separator + "urel" + File.separator;

				File directory = new File(path);

				String [] dllList = directory.list(new FilenameFilter()
				{
					public boolean accept(File file, String name)
					{
						File temp = new File(FileMethods.appendPathSeparator(file.getAbsolutePath())+ name);

						if(temp.isFile() && name.endsWith(".dll"))
							return true;

						return false;
					}
				});

				if(dllList == null)
					continue;

				List<String> tmpDlls = Arrays.asList(dllList);

				int count = 0;
				boolean REL_FOUND = false;

				for(String dll:dlls)
				{
					if(tmpDlls.contains(dll))
						count++;

					if(count > (dlls.size()/2))
					{
						REL_FOUND = true;
						break;
					}
				}

				if(REL_FOUND)
				{
					buildTarget = platform;
					break;
				}
			}


			if(buildTarget != null)
				return buildTarget;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(NullPointerException e){

		}

		return "armv5";
	}

}
