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
* Description: Parses the output from HeaderAnalysis process
*
*/
package com.nokia.s60tools.compatibilityanalyser.model;

import java.io.BufferedReader;
import java.io.IOException;

import com.nokia.s60tools.util.cmdline.ICmdLineCommandExecutorObserver;
import com.nokia.s60tools.util.cmdline.ICustomLineReader;

import com.nokia.s60tools.compatibilityanalyser.resources.Messages;


/**
 * This class is used to parse the output from HeaderAnalysis process.
 *
 */
public class HeaderAnalysisProgressLineReader implements ICustomLineReader {

	int pid;
	String currentLine;
	
	public String readLine(BufferedReader br,
			ICmdLineCommandExecutorObserver observer) throws IOException {

			
			String s = br.readLine();
			if(s!= null && s.contains("PID")) //$NON-NLS-1$
			{
				//String processId = br.readLine();
				String processId = s.substring(s.indexOf(":")+1); //$NON-NLS-1$
				this.pid = Integer.parseInt(processId);
			}
			if(s != null && s.contains(Messages.getString("HeaderAnalysisProgressLineReader.FilePairsFound"))) //$NON-NLS-1$
			{
				String tmp = Messages.getString("HeaderAnalysisProgressLineReader.FilePairsFound"); //$NON-NLS-1$
				((HeaderAnalyserEngine)observer).beginTask(Integer.parseInt(s.substring(tmp.length() + 1)));
			}
			if(s != null && s.contains(Messages.getString("HeaderAnalysisProgressLineReader.Analysingfiles"))) //$NON-NLS-1$
			{
				String tmp = s.substring(s.indexOf(":")+1, s.indexOf("=>")); //$NON-NLS-1$ //$NON-NLS-2$
				currentLine = s.substring(0, s.indexOf(":")) + " " + tmp.substring(tmp.lastIndexOf("\\") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				observer.progress(Integer.parseInt(s.substring(s.indexOf("(") +1, s.indexOf("/")))); //$NON-NLS-1$ //$NON-NLS-2$
								
			}
			if(s != null && s.contains(Messages.getString("HeaderAnalysisProgressLineReader.haExitcode"))) //$NON-NLS-1$
			{
				String exit = s.substring(s.indexOf(":")+1); //$NON-NLS-1$
				int exitCode = Integer.parseInt(exit);
				((HeaderAnalyserEngine)observer).endTask(exitCode);				
				
			}
			if(s != null && s.contains(Messages.getString("HeaderAnalysisProgressLineReader.TotalIssues"))) //$NON-NLS-1$
			{
				((HeaderAnalyserEngine)observer).beginFilteration();
			}
			if(s != null && s.contains("%")) //$NON-NLS-1$
			{
				String percent = s.substring(0, s.indexOf("%")); //$NON-NLS-1$
				int p = Integer.parseInt(percent);
				((HeaderAnalyserEngine)observer).progressFilteration(p);
			}
			if(s != null && s.contains(Messages.getString("HeaderAnalysisProgressLineReader.bcfilterExitcode"))) //$NON-NLS-1$
			{
				((HeaderAnalyserEngine)observer).endFilteration();
			}
			if(s != null && (s.contains(Messages.getString("HeaderAnalysisProgressLineReader.ParsingPlatformData")) || s.contains(Messages.getString("HeaderAnalysisProgressLineReader.------------")))) //$NON-NLS-1$ //$NON-NLS-2$
			{
				((HeaderAnalyserEngine)observer).checkForCancellation();
			}
			return s;
	}
	public int getPid()
	{
		return pid;
	}
	public String getCurrentLine()
	{
		return currentLine;
	}

}
