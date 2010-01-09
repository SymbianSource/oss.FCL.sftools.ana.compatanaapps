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
package com.nokia.s60tools.compatibilityanalyser.ui.wizards;

import com.nokia.s60tools.compatibilityanalyser.resources.Messages;

/**
 * This defines the constant variables used to store preference settings
 */
public class CompatibilityAnalyserPreferencesConstants {	
	
	public final static String CORETOOLS_URL=Messages.getString("CompatibilityAnalyserPreferencesConstants.URLofCoretools"); //$NON-NLS-1$
	public final static String KNOWNISSUES_URL=Messages.getString("CompatibilityAnalyserPreferencesConstants.URLofKnownissues"); //$NON-NLS-1$
	public final static String BASELINES_URL=Messages.getString("CompatibilityAnalyserPreferencesConstants.URLofBaselines");
	
	public final static String DEFAULT_TOOLS=Messages.getString("CompatibilityAnalyserPreferencesConstants.defaultCoreTools"); //$NON-NLS-1$
	public final static String LOCAL_TOOLS=Messages.getString("CompatibilityAnalyserPreferencesConstants.localFileSystemTools"); //$NON-NLS-1$
	public final static String WEB_TOOLS=Messages.getString("CompatibilityAnalyserPreferencesConstants.webServerTools"); //$NON-NLS-1$
	public final static String SDK_TOOLS=Messages.getString("CompatibilityAnalyserPreferencesConstants.sdkTools"); //$NON-NLS-1$
	
	public final static String LOCAL_TOOLS_PATH=Messages.getString("CompatibilityAnalyserPreferencesConstants.localToolsPath"); //$NON-NLS-1$
	public final static String SDK_NAME = Messages.getString("CompatibilityAnalyserPreferencesConstants.sdkName"); //$NON-NLS-1$
	public final static String SDK_TOOLS_PATH=Messages.getString("CompatibilityAnalyserPreferencesConstants.sdkBctoolsPath"); //$NON-NLS-1$
	
	public final static String KNOWNISSUES_LABEL=Messages.getString("CompatibilityAnalyserPreferencesConstants.knownissuesInfo"); //$NON-NLS-1$
	
	
	// data related to the know issues dialog
	public final static String DEFAULT_ISSUES=Messages.getString("CompatibilityAnalyserPreferencesConstants.defaultIssues"); //$NON-NLS-1$
	public final static String LATEST_ISSUES=Messages.getString("CompatibilityAnalyserPreferencesConstants.latestIssuesFromWebserver"); //$NON-NLS-1$
	public final static String WEB_ISSUES=Messages.getString("CompatibilityAnalyserPreferencesConstants.issuesFromWebserver"); //$NON-NLS-1$
	public final static String LOCAL_ISSUES=Messages.getString("CompatibilityAnalyserPreferencesConstants.loaclFileSystemIssues"); //$NON-NLS-1$
	
	public final static String KNOWNISSUES_PATH_LOCAL=Messages.getString("CompatibilityAnalyserPreferencesConstants.knownissuesPath"); //$NON-NLS-1$
		
	public final static String SELECTED_KNOWN_ISSUES_PATH=Messages.getString("CompatibilityAnalyserPreferencesConstants.finalPath"); //$NON-NLS-1$
	
	public final static String PREVIOUS_WEB_CORE_TOOLS_PATH = Messages.getString("CompatibilityAnalyserPreferencesConstants.lastCoreToolsPath"); //$NON-NLS-1$
	public final static String PREVIOUS_WEB_CORE_TOOLS_ROOTDIR = Messages.getString("CompatibilityAnalyserPreferencesConstants.lastCoreToolsRootDir"); //$NON-NLS-1$
	public final static String PREVIOUS_WEB_CORE_TOOLS_URL = Messages.getString("CompatibilityAnalyserPreferencesConstants.lastCoreToolUrl"); //$NON-NLS-1$
	
	public final static String PREVIOUS_BASELINE_URL = "last used baselines url";
	public final static String LASTMODIFIED_DATE_METADATA = "last modification time of metadata file";
	
	public final static String USERECURSION_LAST_SELECTION = "Use recursion last selection";
	public final static String USEPLATFORMDATA_LAST_SELECTION = "Use platform data last selection";
	public final static String FILTER_REPORTS_LAST_SELECTION = "Filter reports last selection";
	public final static String LAST_USED_BASELINE_PROFILE = "Last used profile";
	
}
