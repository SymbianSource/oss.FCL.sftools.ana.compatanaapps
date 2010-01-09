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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;

/**
 * Initialize the default values for the settings in preferences page.
 */
public class CompatibilityAnalyserPreferencesInitializer extends
		AbstractPreferenceInitializer {

	public CompatibilityAnalyserPreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();

		store.setDefault(CompatibilityAnalyserPreferencesConstants.DEFAULT_TOOLS, true);
		store.setDefault(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS, false);
		store.setDefault(CompatibilityAnalyserPreferencesConstants.WEB_TOOLS, false);
		store.setDefault(CompatibilityAnalyserPreferencesConstants.SDK_TOOLS, false);
		
		store.setDefault(CompatibilityAnalyserPreferencesConstants.CORETOOLS_URL, Messages.getString("Coretools.URL"));
		store.setDefault(CompatibilityAnalyserPreferencesConstants.KNOWNISSUES_URL, Messages.getString("Knownissues.URL"));
		store.setDefault(CompatibilityAnalyserPreferencesConstants.BASELINES_URL, Messages.getString("BaselineSDK.URL"));
		
		store.setDefault(CompatibilityAnalyserPreferencesConstants.DEFAULT_ISSUES, true);
		store.setDefault(CompatibilityAnalyserPreferencesConstants.LATEST_ISSUES, false);
		store.setDefault(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES, false);
		store.setDefault(CompatibilityAnalyserPreferencesConstants.LOCAL_ISSUES, false);
		
		store.setDefault(CompatibilityAnalyserPreferencesConstants.KNOWNISSUES_PATH_LOCAL, "");	
		store.setDefault(CompatibilityAnalyserPreferencesConstants.LOCAL_TOOLS_PATH, "");
		
		store.setDefault(CompatibilityAnalyserPreferencesConstants.USERECURSION_LAST_SELECTION, false);
		store.setDefault(CompatibilityAnalyserPreferencesConstants.USEPLATFORMDATA_LAST_SELECTION, true);
		store.setDefault(CompatibilityAnalyserPreferencesConstants.FILTER_REPORTS_LAST_SELECTION, false);
	}

}
