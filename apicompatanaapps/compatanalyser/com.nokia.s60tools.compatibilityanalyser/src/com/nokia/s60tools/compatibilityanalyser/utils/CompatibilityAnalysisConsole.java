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

import org.eclipse.jface.resource.ImageDescriptor;

import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.util.console.AbstractProductSpecificConsole;

/**
 * Console for the Compatibility Analyser tool. This is singleton class.
 */
public class CompatibilityAnalysisConsole extends AbstractProductSpecificConsole
{

	/**
	 * Singleton instance of the class.
	 */
	private static CompatibilityAnalysisConsole instance = null;
	
	/**
	 * Public accessor method.
	 * @return Singleton instance of the class.
	 */
	public static CompatibilityAnalysisConsole getInstance(){
		if(instance == null ){
			instance = new CompatibilityAnalysisConsole();
		}
		return instance;
	}
	
	/**
	 * Private constructor to avoid instantiation from other classes.
	 */
	private CompatibilityAnalysisConsole(){		
	}
	
	protected ImageDescriptor getProductConsoleImageDescriptor() {
		return null;
	}

	@Override
	protected String getProductConsoleName() {
		return Messages.getString("CompatibilityAnalysisConsole.CA_console"); //$NON-NLS-1$
	}

	
}
