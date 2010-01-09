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
package com.nokia.s60tools.compatibilityanalyser.model;

/**
 * This interface must be implemented by those classes
 * which has to perform some actions based on the status of CompatibilityAnalysis
 * It acts like a listener for CompatibilityAnalysis.
 *
 */
public interface AnalysisFeedbackHandler {

	public void HandleFeedback(String title,String message);
	public void startHA(CompatibilityAnalyserEngine engine);
	public void reStartAnalysisUsingSameData(String title,String message);
	public void reStartFilterationUsingSameData(String title,String message);
}
