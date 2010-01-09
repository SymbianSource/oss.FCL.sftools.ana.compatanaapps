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
* Description: Parses the output from OrdinalAnalysis process.
*
*/
package com.nokia.s60tools.compatibilityanalyser.model;

import java.io.BufferedReader;
import java.io.IOException;

import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.util.cmdline.ICmdLineCommandExecutorObserver;
import com.nokia.s60tools.util.cmdline.ICustomLineReader;

/**
 * This class is used to parse the output from OrdinalAnalysis process.
 *
 */
public class LibraryAnalysisProgressLineReader implements ICustomLineReader {

	int pid;
	int exitCode = -1;
	String currentLine;
	boolean filterationStarted = false;
	
	public String readLine(BufferedReader br,
			ICmdLineCommandExecutorObserver observer) throws IOException {
		
		String s = br.readLine();	
		if(s!= null && s.contains(Messages.getString("LibraryAnalysisProgressLineReader.PID"))) //$NON-NLS-1$
		{
			String processId = s.substring(s.indexOf(":")+1); //$NON-NLS-1$
			this.pid = Integer.parseInt(processId);
		}
		if(s != null && s.contains(Messages.getString("LibraryAnalysisProgressLineReader.TotalFilesToBeProcessed"))) //$NON-NLS-1$
		{
			int tmp = Messages.getString("LibraryAnalysisProgressLineReader.TotalFilesToBeProcessed").length(); //$NON-NLS-1$
			String noOfFiles = s.substring(tmp + 1);
						
			((LibraryAnalyserEngine)observer).beginTask(Integer.parseInt(noOfFiles));
		}
		if(s != null && s.contains("Analysing files")) //$NON-NLS-1$
		{
			String tmp = null;
			if(s.contains("=>"))
				tmp = s.substring(s.indexOf(":")+1, s.indexOf("=>")); //$NON-NLS-1$ //$NON-NLS-2$
			else
				tmp = s.substring(s.indexOf(":")+1);
			
			currentLine = s.substring(0, s.indexOf(":")) + " " + tmp.substring(tmp.lastIndexOf("\\") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			observer.progress(Integer.parseInt(s.substring(s.indexOf("(") +1, s.indexOf("/"))));
		}
		
		if(s != null && s.contains(Messages.getString("LibraryAnalysisProgressLineReader.laExitCode"))) //$NON-NLS-1$
		{
			String exit = s.substring(s.indexOf(":")+1); //$NON-NLS-1$
			int exitCode = Integer.parseInt(exit);
			((LibraryAnalyserEngine)observer).endTask(exitCode);
		}
		if(s != null && s.contains(Messages.getString("LibraryAnalysisProgressLineReader.TotalIssues"))) //$NON-NLS-1$
		{
			filterationStarted = true;
			((LibraryAnalyserEngine)observer).beginFilteration();
		}
		if(s != null && s.contains("%")) //$NON-NLS-1$
		{
			String percent = s.substring(0, s.indexOf("%")); //$NON-NLS-1$
			int p = Integer.parseInt(percent);
			((LibraryAnalyserEngine)observer).progressFilteration(p);
		}
		if(s != null && s.contains(Messages.getString("LibraryAnalysisProgressLineReader.bcfilterExitCode"))) //$NON-NLS-1$
		{
			((LibraryAnalyserEngine)observer).endFilteration();
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
	public int getExitCode()
	{
		return exitCode;
	}
}
