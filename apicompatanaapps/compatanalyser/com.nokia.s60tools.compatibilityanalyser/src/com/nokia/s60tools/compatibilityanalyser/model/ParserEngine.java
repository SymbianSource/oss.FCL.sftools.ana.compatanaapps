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
* Description: Contains utility methods to save given analysis configuration
* to a file and to scan given xml file 
*
*/
package com.nokia.s60tools.compatibilityanalyser.model;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;

/**
 * This class contains methods used to save analysis configuration to a 
 * file and to parse given xml file.
 *
 */
public class ParserEngine {

	
	/**
	 * @param doc specifies the document to be parsed.
	 * The method parses the document and sets various fields of CompatibilityAnalyserEngine object.
	 * @return a CompatibilityAnalyserEngine object which stores the configuration
	 * given in the document.
	 */
	public static CompatibilityAnalyserEngine parseTheConfiguration(Document doc)
	{
		CompatibilityAnalyserEngine engine = new CompatibilityAnalyserEngine();
		ProductSdkData currentSdk = new ProductSdkData();
		
		Node configurationNode = getChildNodeByName(doc, "configuration");
		
		if(configurationNode == null)
			return null;
		
		currentSdk.isOpenedFromConfigFile = true;
		Node baselineProfile = getChildNodeByName(configurationNode, "baselineprofile");
		
		if(baselineProfile != null && baselineProfile.getFirstChild() != null)
			engine.setBaselineProfile(baselineProfile.getFirstChild().getNodeValue());
		
		Node currentSdkName = getChildNodeByName(configurationNode, "currentsdkname");
		
		if(currentSdkName != null && currentSdkName.getFirstChild() != null)
			currentSdk.productSdkName = currentSdkName.getFirstChild().getNodeValue();
		
		Node currentSdkVersion = getChildNodeByName(configurationNode, "currentsdkversion");
		
		if(currentSdkVersion != null && currentSdkVersion.getFirstChild() != null)
			currentSdk.productSdkVersion = currentSdkVersion.getFirstChild().getNodeValue();
		
		Node currentepocRoot = getChildNodeByName(configurationNode, "currentepocroot");
		
		if(currentepocRoot != null && currentepocRoot.getFirstChild() != null)
			currentSdk.epocRoot = currentepocRoot.getFirstChild().getNodeValue();
		
		Node analysisType = getChildNodeByName(configurationNode, "analysistype");
		
		if(analysisType != null)
		{
			Node headerAnalysis = getChildNodeByName(analysisType, "headeranalysis");
			Node ordinalAnalysis = getChildNodeByName(analysisType, "ordinalanalysis");
			
			if(headerAnalysis != null && headerAnalysis.getFirstChild() != null)
				engine.setHeaderAnalysis(new Boolean(headerAnalysis.getFirstChild().getNodeValue()));
			
			if(ordinalAnalysis != null && ordinalAnalysis.getFirstChild() != null)
				engine.setLibraryAnalysis(new Boolean(ordinalAnalysis.getFirstChild().getNodeValue()));
		}
		
		if(engine.isHeaderAnalysisChecked()){
			
			Node headersRoot = getChildNodeByName(configurationNode, "headers-configuration");
			
			readHeadersConfiguration(headersRoot, currentSdk);
		}
		
		if(engine.isLibraryAnalysisChecked())
		{
			Node libsRoot = getChildNodeByName(configurationNode, "libraries-configuration");
			
			readLibrariesConfiguration(libsRoot, currentSdk);
		}
		
		readReportAndFilterationInfo(configurationNode, currentSdk);
		
		engine.setCurrentSdkData(currentSdk);
		return engine;
	}
	
	
	/**
	 * This method scans the given node and returns the child node 
	 * of given name.
	 */
	private static Node getChildNodeByName(Node currentNode, String name) {
		if(currentNode == null)
			return null;
		
		// Get the child nodes
		NodeList nodes = currentNode.getChildNodes();
		
		// Go through the children, and if names match return the node
		for(int i = 0; i < nodes.getLength(); i++)
			if(nodes.item(i).getNodeName().equals(name))
				return nodes.item(i);

		// Not found, return null
		return null;
	}
	
	/**
	 * This method scans all fields related to HeaderAnalysis.
	 */
	private static void readHeadersConfiguration(Node parent, ProductSdkData currentSdk)
	{
		if(parent != null)
		{
			Node defHeadersDir = getChildNodeByName(parent, "defaultheaderspath");
			if(defHeadersDir != null && defHeadersDir.getFirstChild() != null)
					currentSdk.defaultHeaderDir = new Boolean(defHeadersDir.getFirstChild().getNodeValue());
			
			Node headerDirs = getChildNodeByName(parent, "currentdirectories");
			
			NodeList dirsNodeList = headerDirs.getChildNodes();
			
			int count = dirsNodeList.getLength();
			
			ArrayList<String> dirsList = new ArrayList<String>();
						
			for(int i=0;i<count; i++)
			{
				if(dirsNodeList.item(i).getNodeName().equals("dir"))
				{
					Node dirNode = dirsNodeList.item(i);
					
					if(dirNode.getFirstChild() != null)
						dirsList.add(dirNode.getFirstChild().getNodeValue());
				}
			}
			
			currentSdk.currentHeaderDir = dirsList.toArray(new String[0]);
			
			//Fetching Replace Set
			Node replaceSetNode = getChildNodeByName(parent, "replace-set");
			
			if(replaceSetNode != null)
			{
				NodeList replaceList = replaceSetNode.getChildNodes();
				int size = replaceList.getLength();
			
				for(int i =0; i<size; i++)
				{
					Node replaceNode = replaceList.item(i);
					if(replaceNode != null && replaceNode.getNodeName().equals("replace"))
					{
						String oldFile = replaceNode.getAttributes().getNamedItem("old-file").getNodeValue();
						String newFile = replaceNode.getAttributes().getNamedItem("new-file").getNodeValue();
					
						String set = oldFile + ":" + newFile;
					
						currentSdk.replaceSet.add(set);
					}
				}
			}
			
			Node analyseAll = getChildNodeByName(parent, "analyseallfiles");
			
			if(analyseAll !=null && analyseAll.getFirstChild() != null)
				currentSdk.analyseAll = new Boolean(analyseAll.getFirstChild().getNodeValue());
			else
				currentSdk.analyseAll = false;
			
			//Fetching list of Header files
			if(!currentSdk.analyseAll)
			{
				Node headersNode = getChildNodeByName(parent, "headerslist");
				ArrayList<String> filesList = new ArrayList<String>();
				
				if(headersNode != null)
				{	
				NodeList headersList = headersNode.getChildNodes();
				int size = headersList.getLength();
												
				for(int i =0; i<size; i++)
				{
					Node header = headersList.item(i);
					
					if(header.getNodeName().equals("file"))
					{
						if(header != null && header.getFirstChild() != null)
							filesList.add(header.getFirstChild().getNodeValue());
					}
				}
				currentSdk.currentFiles = filesList.toArray(new String[0]);
				}
				
				if(currentSdk.replaceSet != null)
				{
					for(String s:currentSdk.replaceSet)
					{
						String baseName = s.substring(0, s.indexOf(":"));
						if(!filesList.contains(baseName))
							filesList.add(baseName);
						
						String currentName = s.substring(s.indexOf(":") +1);
						if(filesList.contains(currentName))
							filesList.remove(currentName);
					}
				}
				currentSdk.HeaderFilesList = filesList.toArray(new String[0]);	
			}
			
			if(currentSdk.analyseAll)
			{
				Node fileTypesNode = getChildNodeByName(parent, "filetypes");
				
				Node defaultTypeNode = getChildNodeByName(fileTypesNode, "defaultfiletypes");
				
				if(defaultTypeNode == null)
				{
				
					NodeList typesList = fileTypesNode.getChildNodes();
					int types = typesList.getLength();
				
					currentSdk.allTypes = false;
					
					for(int i=0; i<types; i++)
					{
						if(typesList.item(i).getNodeName().equals("htype"))
						{
							Node hNode = typesList.item(i);
						
							if(hNode.getFirstChild() != null)
								currentSdk.hTypes = new Boolean(hNode.getFirstChild().getNodeValue());
						}
						else if(typesList.item(i).getNodeName().equals("hrhtype"))
						{
							Node hrhNode = typesList.item(i);
						
							if(hrhNode.getFirstChild() != null)
								currentSdk.hrhTypes = new Boolean(hrhNode.getFirstChild().getNodeValue());
						}
						else if(typesList.item(i).getNodeName().equals("mbgtype"))
						{
							Node mbgNode = typesList.item(i);
						
							if(mbgNode.getFirstChild() != null)
								currentSdk.mbgTypes = new Boolean(mbgNode.getFirstChild().getNodeValue());
						}
						else if(typesList.item(i).getNodeName().equals("rsgtype"))
						{
							Node rsgNode = typesList.item(i);
						
							if(rsgNode.getFirstChild() != null)
								currentSdk.rsgTypes = new Boolean(rsgNode.getFirstChild().getNodeValue());
						}
						else if(typesList.item(i).getNodeName().equals("hpptype"))
						{
							Node hppNode = typesList.item(i);
						
							if(hppNode.getFirstChild() != null)
								currentSdk.hppTypes = new Boolean(hppNode.getFirstChild().getNodeValue());
						}
						else if(typesList.item(i).getNodeName().equals("pantype"))
						{
							Node panNode = typesList.item(i);
						
							if(panNode.getFirstChild() != null)
								currentSdk.panTypes = new Boolean(panNode.getFirstChild().getNodeValue());
						}
					}
				}
				else
				{
					if(defaultTypeNode.getFirstChild() != null)
						currentSdk.allTypes = new Boolean(defaultTypeNode.getFirstChild().getNodeValue());
				}
			}
			
			Node recursionNode = getChildNodeByName(parent, "userecursion");
			
			if(recursionNode != null && recursionNode.getFirstChild() != null)
				currentSdk.useRecursive = new Boolean(recursionNode.getFirstChild().getNodeValue());
			
			Node platformDataNode = getChildNodeByName(parent, "useplatformdata");
			
			if(platformDataNode != null && platformDataNode.getFirstChild() != null)
				currentSdk.usePlatformData = new Boolean(platformDataNode.getFirstChild().getNodeValue());
			
			
			Node sysincNode = getChildNodeByName(parent, "systemincludes");
			
			if(sysincNode != null)
			{
				if(sysincNode.getFirstChild() != null && !sysincNode.getFirstChild().getNodeValue().equals("default"))
				{
					NodeList incList = sysincNode.getChildNodes();
					
					ArrayList<String> incPaths = new ArrayList<String>();
					for(int i=0;i<incList.getLength(); i++)
					{
						Node incNode = incList.item(i);
							
						if(incNode != null && incNode.getNodeName().equals("inc"))	
							incPaths.add(incNode.getFirstChild().getNodeValue());
					}
					
					currentSdk.currentIncludes = incPaths.toArray(new String[0]);
				}
			}
			
			Node forcedHdrsProvided = getChildNodeByName(parent, "forcedheadersprovided");
			
			if(forcedHdrsProvided != null && forcedHdrsProvided.getFirstChild() != null)
				currentSdk.isForcedProvided = new Boolean(forcedHdrsProvided.getFirstChild().getNodeValue());
			
			Node forcedHdrsNode = getChildNodeByName(parent, "forced-headers");
			
			if(forcedHdrsNode != null)
			{
				NodeList forcedHdrsList = forcedHdrsNode.getChildNodes();
				
				currentSdk.forcedHeaders = new ArrayList<String>();
				for(int i=0; i<forcedHdrsList.getLength(); i++)
				{
					Node hdrNode = forcedHdrsList.item(i);
					if(hdrNode != null && hdrNode.getNodeName().equals("hdr"))
						currentSdk.forcedHeaders.add(hdrNode.getFirstChild().getNodeValue());
				}
			}
		}
	}
	
	/**
	 * This method scans all fields related to OrdinalAnalysis
	 */
	private static void readLibrariesConfiguration(Node parent, ProductSdkData currentSdk)
	{
		if(parent != null)
		{
			Node defaultPlatformNode = getChildNodeByName(parent, "default-build-target");
			
			if(defaultPlatformNode != null && defaultPlatformNode.getFirstChild() != null)
			{
				currentSdk.default_platfrom_selection = true;
				currentSdk.platfromSelection = false;
				currentSdk.selected_library_dirs = false;
			}
			else
			{
				Node targetPlatNode = getChildNodeByName(parent, "target-platforms");
							
				if(targetPlatNode != null)
				{
					NodeList buildsNodeList = targetPlatNode.getChildNodes();
					
					int count = buildsNodeList.getLength();
				
					ArrayList<String> platformsList = new ArrayList<String>();
							
					for(int i=0;i<count; i++)
					{
						if(buildsNodeList.item(i).getNodeName().equals("target"))
						{
							Node dirNode = buildsNodeList.item(i);
						
							if(dirNode.getFirstChild() != null)
								platformsList.add(dirNode.getFirstChild().getNodeValue());
						}
					}
				
					if(platformsList.size() > 0)
						currentSdk.libsTargetPlat = platformsList.toArray(new String[0]);
				
					currentSdk.platfromSelection = true;
					currentSdk.default_platfrom_selection = false;
					currentSdk.selected_library_dirs = false;
				}			
				else
				{
					currentSdk.platfromSelection = false;
					currentSdk.default_platfrom_selection = false;
					currentSdk.selected_library_dirs = true;
				
					readGivenLibraryPaths(parent, currentSdk);
				}
			}
			
			Node allLibsNode = getChildNodeByName(parent, "all-libraries");
			
			if(allLibsNode != null && allLibsNode.getFirstChild()!= null)
				currentSdk.analyzeAllLibs = new Boolean(allLibsNode.getFirstChild().getNodeValue());
			else
				currentSdk.analyzeAllLibs = false;
			
			if(!currentSdk.analyzeAllLibs)
			{
				Node libsRootNode = getChildNodeByName(parent, "libraries-list");
				
				if(libsRootNode != null)
				{
					NodeList libNodesList = libsRootNode.getChildNodes();
					
					ArrayList<String> libsList = new ArrayList<String>();
					for(int i=0; i<libNodesList.getLength(); i++)
					{
						Node libNode = libNodesList.item(i);
						
						if(libNode != null && libNode.getNodeName().equals("lib"))
							libsList.add(libNode.getFirstChild().getNodeValue());
					}
					currentSdk.libraryFilesList = libsList.toArray(new String[0]);
				}
			}
			
			Node toolChainNode = getChildNodeByName(parent, "toolchain");
			
			if(toolChainNode != null)
			{
				Node nameNode = toolChainNode.getAttributes().getNamedItem("name");
				
				if(nameNode != null && nameNode.getFirstChild() != null)
					currentSdk.toolChain = nameNode.getFirstChild().getNodeValue();
				
				Node pathNode = toolChainNode.getAttributes().getNamedItem("path");
				
				if(pathNode != null && pathNode.getFirstChild()!= null)
					currentSdk.toolChainPath = pathNode.getFirstChild().getNodeValue();
			}
		}
	}
	
	private static void readGivenLibraryPaths(Node parent, ProductSdkData currentSdk)
	{
		Node userPath = getChildNodeByName(parent, "libsdirpath");
		
		if(userPath != null)
		{
			NodeList dirsNodeList = userPath.getChildNodes();
	
			int count = dirsNodeList.getLength();
	
			ArrayList<String> dirsList = new ArrayList<String>();
				
			for(int i=0;i<count; i++)
			{
				if(dirsNodeList.item(i).getNodeName().equals("dir"))
				{
					Node dirNode = dirsNodeList.item(i);
			
					if(dirNode.getFirstChild() != null)
						dirsList.add(dirNode.getFirstChild().getNodeValue());
				}
			}
			if(dirsList.size() > 0)
				currentSdk.currentLibsDir = dirsList.toArray(new String[0]);
		}
	
		Node userdllPath = getChildNodeByName(parent, "dllsdirpath");
		
		if(userdllPath != null)
		{
			NodeList dlldirsNodeList = userdllPath.getChildNodes();
	
			int count = dlldirsNodeList.getLength();
	
			ArrayList<String> dlldirsList = new ArrayList<String>();
				
			for(int i=0;i<count; i++)
			{
				if(dlldirsNodeList.item(i).getNodeName().equals("dir"))
				{
					Node dirNode = dlldirsNodeList.item(i);
			
					if(dirNode.getFirstChild() != null)
						dlldirsList.add(dirNode.getFirstChild().getNodeValue());
				}
			}
			
			if(count > 0)
				currentSdk.currentDllDir = dlldirsList.toArray(new String[0]);
		}
	
		
	}
	
	/**
	 * This method scans fields related to filteration and report storage.
	 */
	private static void readReportAndFilterationInfo(Node parent, ProductSdkData currentSdk)
	{
		Node filterNode = getChildNodeByName(parent, "filteration-needed");
		
		if(filterNode != null && filterNode.getFirstChild() != null)
			currentSdk.filterNeeded = new Boolean(filterNode.getFirstChild().getNodeValue());
		
		Node reportNode = getChildNodeByName(parent, "report");
		
		if(reportNode != null){
			
			Node nameNode = reportNode.getAttributes().getNamedItem("name");
			Node pathNode = reportNode.getAttributes().getNamedItem("path");
			
			if(nameNode != null && nameNode.getFirstChild() != null)
				currentSdk.reportName = nameNode.getFirstChild().getNodeValue();
			
			if(pathNode != null && pathNode.getFirstChild() != null)
				currentSdk.reportPath = pathNode.getFirstChild().getNodeValue();
		}
	}
	
		
	/**
	 * @param engine will be read and its properties will be written to a file, in xml format.
	 * @param path path of the xml file
	 * @throws ParserConfigurationException
	 */
	public static void saveSettingsToFile(CompatibilityAnalyserEngine engine, String path) throws ParserConfigurationException
	{
		if(engine == null)
			return;
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		Document newDocument = builder.newDocument();
		
		// Root element
		Element configuration = (Element) newDocument.createElement("configuration");
		
		newDocument.appendChild(configuration);
		
		Element baseProfile = (Element) newDocument.createElement("baselineprofile");
		baseProfile.appendChild(newDocument.createTextNode(engine.getBaselineProfile()));
		configuration.appendChild(baseProfile);
		
		ProductSdkData currentSdk = engine.getCurrentSdkData();
		
		Element sdkName = (Element) newDocument.createElement("currentsdkname");
		sdkName.appendChild(newDocument.createTextNode(currentSdk.productSdkName));
		configuration.appendChild(sdkName);
		
		Element sdkVersion = (Element) newDocument.createElement("currentsdkversion");
		sdkVersion.appendChild(newDocument.createTextNode(currentSdk.productSdkVersion));
		configuration.appendChild(sdkVersion);
				
		Element sdkEpocRoot = (Element) newDocument.createElement("currentepocroot");
		sdkEpocRoot.appendChild(newDocument.createTextNode(currentSdk.epocRoot));
		configuration.appendChild(sdkEpocRoot);
		
		Element analysis = (Element) newDocument.createElement("analysistype");
	
		Element headerAnalysis = (Element) newDocument.createElement("headeranalysis");
		headerAnalysis.appendChild(newDocument.createTextNode(new Boolean(engine.isHeaderAnalysisChecked()).toString()));
		analysis.appendChild(headerAnalysis);
		
		Element libraryAnalysis = (Element) newDocument.createElement("ordinalanalysis");
		libraryAnalysis.appendChild(newDocument.createTextNode(new Boolean(engine.isLibraryAnalysisChecked()).toString()));
		analysis.appendChild(libraryAnalysis);
		
		configuration.appendChild(analysis);
		
		if(engine.isHeaderAnalysisChecked())
		{
			Element headersRoot = (Element) newDocument.createElement("headers-configuration");
			
			Element defaultDir = (Element) newDocument.createElement("defaultheaderspath");
			defaultDir.appendChild(newDocument.createTextNode(new Boolean(currentSdk.defaultHeaderDir).toString()));
			headersRoot.appendChild(defaultDir);
			
			Element headerDirs = (Element) newDocument.createElement("currentdirectories");
			
			for(String s:currentSdk.currentHeaderDir)
			{
				Element headerDir = (Element) newDocument.createElement("dir");
				headerDir.appendChild(newDocument.createTextNode(s));
				headerDirs.appendChild(headerDir);
			}
			headersRoot.appendChild(headerDirs);
			
			if(!currentSdk.analyseAll)
			{
				Element headersList = (Element) newDocument.createElement("headerslist");
				
				for(String s:currentSdk.currentFiles)
				{
					Element headerFile = (Element) newDocument.createElement("file");
					headerFile.appendChild(newDocument.createTextNode(s));
					headersList.appendChild(headerFile);
				}
				headersRoot.appendChild(headersList);
			}
			else
			{
				Element analyseAll = (Element) newDocument.createElement("analyseallfiles");
				analyseAll.appendChild(newDocument.createTextNode("true"));
				headersRoot.appendChild(analyseAll);
			}
			
			if(currentSdk.analyseAll)
			{
				Element filetypes = (Element) newDocument.createElement("filetypes");
				
				if(currentSdk.allTypes)
				{
					Element allFileTypes = (Element) newDocument.createElement("defaultfiletypes");
					allFileTypes.appendChild(newDocument.createTextNode(new Boolean(currentSdk.allTypes).toString()));
					filetypes.appendChild(allFileTypes);
				}
				else
				{
					Element hType = (Element) newDocument.createElement("htype");
					hType.appendChild(newDocument.createTextNode(new Boolean(currentSdk.allTypes | currentSdk.hTypes).toString()));
					filetypes.appendChild(hType);
				
					Element hrhType = (Element) newDocument.createElement("hrhtype");
					hrhType.appendChild(newDocument.createTextNode(new Boolean(currentSdk.allTypes | currentSdk.hrhTypes).toString()));
					filetypes.appendChild(hrhType);
				
					Element mbgType = (Element) newDocument.createElement("mbgtype");
					mbgType.appendChild(newDocument.createTextNode(new Boolean(currentSdk.allTypes | currentSdk.mbgTypes).toString()));
					filetypes.appendChild(mbgType);
				
					Element rsgType = (Element) newDocument.createElement("rsgtype");
					rsgType.appendChild(newDocument.createTextNode(new Boolean(currentSdk.allTypes | currentSdk.rsgTypes).toString()));
					filetypes.appendChild(rsgType);
					
					Element hppType = (Element) newDocument.createElement("hpptype");
					hppType.appendChild(newDocument.createTextNode(new Boolean(currentSdk.allTypes | currentSdk.hppTypes).toString()));
					filetypes.appendChild(hppType);
					
					Element panType = (Element) newDocument.createElement("pantype");
					panType.appendChild(newDocument.createTextNode(new Boolean(currentSdk.allTypes | currentSdk.panTypes).toString()));
					filetypes.appendChild(panType);
				}
				headersRoot.appendChild(filetypes);
			}
			
			Element recursion = (Element) newDocument.createElement("userecursion");
			recursion.appendChild(newDocument.createTextNode(new Boolean(currentSdk.useRecursive).toString()));
			headersRoot.appendChild(recursion);
			
			Element platformData = (Element) newDocument.createElement("useplatformdata");
			platformData.appendChild(newDocument.createTextNode(new Boolean(currentSdk.usePlatformData).toString()));
			headersRoot.appendChild(platformData);
			
			if(currentSdk.currentIncludes != null)
			{
				Element sysIncludes = (Element) newDocument.createElement("systemincludes");
				
				for(String s:currentSdk.currentIncludes)
				{
					Element includeDir = (Element) newDocument.createElement("inc");
					includeDir.appendChild(newDocument.createTextNode(s));
					sysIncludes.appendChild(includeDir);
				}
				
				headersRoot.appendChild(sysIncludes);
			}
			
			if(currentSdk.replaceSet.size() >0)
			{
				Element replaceSet = (Element) newDocument.createElement("replace-set");
				
				for(String s:currentSdk.replaceSet)
				{
					Element replace = (Element) newDocument.createElement("replace");
					
					Attr old = newDocument.createAttribute("old-file");
					old.setNodeValue(s.substring(0, s.indexOf(":")));
					replace.setAttributeNode(old);
					
					Attr newFile = newDocument.createAttribute("new-file");
					newFile.setNodeValue(s.substring(s.indexOf(":")+1));
					replace.setAttributeNode(newFile);
					
					replaceSet.appendChild(replace);
				}
				
				headersRoot.appendChild(replaceSet);
			}
			
			Element forcedHdrsProvided = (Element) newDocument.createElement("forcedheadersprovided");
			forcedHdrsProvided.appendChild(newDocument.createTextNode(new Boolean(currentSdk.isForcedProvided).toString()));
			headersRoot.appendChild(forcedHdrsProvided);
			
			if(currentSdk.forcedHeaders != null)
			{
				Element forcedHdrs = (Element) newDocument.createElement("forced-headers");
				
				for(String s:currentSdk.forcedHeaders)
				{
					 Element forcedHdr = (Element) newDocument.createElement("hdr");
					 forcedHdr.appendChild(newDocument.createTextNode(s));
					 forcedHdrs.appendChild(forcedHdr);
					
				}
				
				headersRoot.appendChild(forcedHdrs);
			}
			
			configuration.appendChild(headersRoot);
		}
		
		if(engine.isLibraryAnalysisChecked())
		{
			Element libsRoot = (Element) newDocument.createElement("libraries-configuration");
			
			if(currentSdk.default_platfrom_selection)
			{
				Element default_platform = (Element) newDocument.createElement("default-build-target");
				default_platform.appendChild(newDocument.createTextNode("true"));
				libsRoot.appendChild(default_platform);
			}
			else if(currentSdk.platfromSelection)
			{
				Element libsPlat = (Element) newDocument.createElement("target-platforms");
				
				for(String s:currentSdk.libsTargetPlat)
				{
					Element childDir = (Element) newDocument.createElement("target");
					childDir.appendChild(newDocument.createTextNode(s));
					libsPlat.appendChild(childDir);
				}
				libsRoot.appendChild(libsPlat);
			}
			else
			{
				Element libsDir = (Element) newDocument.createElement("libsdirpath");
				
				for(String s:currentSdk.currentLibsDir)
				{
					Element childDir = (Element) newDocument.createElement("dir");
					childDir.appendChild(newDocument.createTextNode(s));
					libsDir.appendChild(childDir);
				}
				libsRoot.appendChild(libsDir);
				
				Element dllsDir = (Element) newDocument.createElement("dllsdirpath");
				
				for(String s:currentSdk.currentDllDir)
				{
					Element childDir = (Element) newDocument.createElement("dir");
					childDir.appendChild(newDocument.createTextNode(s));
					dllsDir.appendChild(childDir);
				}
				libsRoot.appendChild(dllsDir);
			}
			
			if(!currentSdk.analyzeAllLibs)
			{
				Element libsList = (Element) newDocument.createElement("libraries-list");
				
				for(String s:currentSdk.libraryFilesList)
				{
					Element libFile = (Element) newDocument.createElement("lib");
					libFile.appendChild(newDocument.createTextNode(s));
					libsList.appendChild(libFile);
				}
				libsRoot.appendChild(libsList);
			}
			else
			{
				Element analyseAll = (Element) newDocument.createElement("all-libraries");
				analyseAll.appendChild(newDocument.createTextNode("true"));
				libsRoot.appendChild(analyseAll);
			}
			
			Element toolChain = (Element) newDocument.createElement("toolchain");
			
			Attr name = newDocument.createAttribute("name");
			name.setNodeValue(currentSdk.toolChain);
			toolChain.setAttributeNode(name);
			
			Attr toolPath = newDocument.createAttribute("path");
			toolPath.setNodeValue(currentSdk.toolChainPath);
			toolChain.setAttributeNode(toolPath);
			
			libsRoot.appendChild(toolChain);
			
			configuration.appendChild(libsRoot);
		}
		
		Element filterRoot = (Element) newDocument.createElement("filteration-needed");
		filterRoot.appendChild(newDocument.createTextNode(new Boolean(currentSdk.filterNeeded).toString()));
		configuration.appendChild(filterRoot);
		
		Element report = (Element) newDocument.createElement("report");
		
		Attr repName = newDocument.createAttribute("name");
		repName.setNodeValue(currentSdk.reportName);
		report.setAttributeNode(repName);
		
		Attr repPath = newDocument.createAttribute("path");
		repPath.setNodeValue(currentSdk.reportPath);
		report.setAttributeNode(repPath);
				
		configuration.appendChild(report);
		
		
		try
		{
			CompatibilityAnalyserEngine.saveConfiguration(newDocument, path);
		}catch(CoreException e){
			e.printStackTrace();
		}catch(TransformerException e){
			e.printStackTrace();
		}
		
	}
}
