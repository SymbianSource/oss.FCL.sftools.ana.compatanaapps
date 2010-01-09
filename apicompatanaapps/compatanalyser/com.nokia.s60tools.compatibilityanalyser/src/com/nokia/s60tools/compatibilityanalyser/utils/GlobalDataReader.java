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
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class which stores the global data between core and plug-in.
 * Data is read from the file present in core tools package.
 *
 */
public class GlobalDataReader {
	
	// XML DOM-document
	private Document document = null;

	public GlobalDataReader(File file) {
		
		Document document = null;
		
		// Try to parse the document from an existing file
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		} catch(IOException ioe) {
			// Handled in finally-block
			ioe.printStackTrace();
		} catch(SAXException saxe) {
			// Handled in finally-block
			saxe.printStackTrace();
		} catch (ParserConfigurationException e) {
			// Handled in finally-block
			e.printStackTrace();
		}
	
		setDocument(document);
	}
	
	public String[] readSupportedSDKVersions()
	{
		if(getDocument()==null)
			return null;
		
		Node globaldataNode = getChildNodeByName(getDocument(), "globaldata");
		
		if(globaldataNode == null)
			return null;

		//Reading supported versions
		ArrayList<String> vList = new ArrayList<String>();
		Node supportedVersionsNode = getChildNodeByName(globaldataNode, "supportedversions");
		if(supportedVersionsNode!=null)
		{
			NodeList versions = supportedVersionsNode.getChildNodes();
			for(int i = 0; i < versions.getLength(); i++) {
				Node versionNode = versions.item(i);
				if(versionNode!=null && versionNode.getNodeName().equals("version"))
				{
					if(versionNode.getFirstChild()!=null)
						vList.add(versionNode.getFirstChild().getNodeValue());
				}
			}
		}
		if(vList.size() > 0)
			return vList.toArray(new String[vList.size()]);
		
		return null;
	}
	
	public String[] readSupportedBuildTypes()
	{
		if(getDocument()==null)
			return null;
		
		Node globaldataNode = getChildNodeByName(getDocument(), "globaldata");
		
		if(globaldataNode == null)
			return null;
		
		//Reading supported build targets 
		ArrayList<String> targetList = new ArrayList<String>();
		Node supportedTargetsNode = getChildNodeByName(globaldataNode, "buildtargets");
		if(supportedTargetsNode!=null)
		{
			NodeList targets = supportedTargetsNode.getChildNodes();
			for(int i = 0; i < targets.getLength(); i++) {
				Node targetNode = targets.item(i);
				if(targetNode!=null && targetNode.getNodeName().equals("target"))
				{
					if(targetNode.getFirstChild()!=null)
						targetList.add(targetNode.getFirstChild().getNodeValue());
				}
			}
		}
		if(targetList.size() > 0)
			return targetList.toArray(new String[targetList.size()]);
	
		return null;
	}
	
	public String[] readSupportedFileTypes()
	{
		if(getDocument()==null)
			return null;
		
		Node globaldataNode = getChildNodeByName(getDocument(), "globaldata");
		
		if(globaldataNode == null)
			return null;
		
		//Reading supported file types
		ArrayList<String> filetypesList = new ArrayList<String>();
		Node supportedFileTypesNode = getChildNodeByName(globaldataNode, "filetypes");
		if(supportedFileTypesNode!=null)
		{
			NodeList filetypes = supportedFileTypesNode.getChildNodes();
			for(int i = 0; i < filetypes.getLength(); i++) {
				Node typeNode = filetypes.item(i);
				if(typeNode!=null && typeNode.getNodeName().equals("type"))
				{
					if(typeNode.getFirstChild()!=null)
						filetypesList.add(typeNode.getFirstChild().getNodeValue());
				}
			}
		}
		
		if(filetypesList.size() > 0)
			return filetypesList.toArray(new String[filetypesList.size()]);
	
		return null;
	}
	
	private Document getDocument()
	{
		return document;
	}
	
	private void setDocument(Document document)
	{
		this.document = document;
	}
	
	/**
	 * Gets the child node of the current node by the name.
	 *
	 * @param currentNode the current node.
	 * @param name the name of the wanted child node.
	 * @return the child node.
	 */
	private Node getChildNodeByName(Node currentNode, String name) {
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
	
	//Reading system includes for each version
	//IMPLEMENT LOGIC HERE TO READ THE SUPPORTED VERSIONS
	//Not implemented as it is not needed for this release
}
