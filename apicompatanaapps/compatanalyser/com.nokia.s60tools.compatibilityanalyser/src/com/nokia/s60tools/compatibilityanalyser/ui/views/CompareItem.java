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
package com.nokia.s60tools.compatibilityanalyser.ui.views;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * Input content to compare.
 *
 */
class CompareItem implements IStreamContentAccessor,ITypedElement, IModificationDate {
	
	private String name;
	private File file;
	CompareItem(String name, File file) {
		this.name = name;
		this.file = file;
	}
	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(convertToString(this.file).getBytes());
	}
	public Image getImage() {
		return null;
	}
	public long getModificationDate() {
		return file.lastModified();
	}
	public String getName() {
		return name;
	}
	public String getString() {
		return convertToString(this.file);
	}
	public String getType() {
		return ITypedElement.TEXT_TYPE;
	}
	
	/**
	 * Converts the text file contents to string.
	 * @param file input file
	 * @return string
	 */
	public String convertToString(File file)
	{
		String str = "";
		try {
			String thisLine;
			StringBuffer strb = new StringBuffer("") ;
			FileInputStream fin =  new FileInputStream(file);
			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {  
				strb.append(thisLine+"\r\n");
			}
			str = strb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}	
}
