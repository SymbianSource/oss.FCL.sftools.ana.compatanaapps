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

import java.io.BufferedReader;
import java.io.IOException;

import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.util.cmdline.ICmdLineCommandExecutorObserver;
import com.nokia.s60tools.util.cmdline.ICustomLineReader;

public class ReportFilterLineReader implements ICustomLineReader {


	int pid;
	int exitCode = -1;
	String currentLine;
	boolean filterationStarted = false;
	String currentFile=null;
	public String outputFile;
	
	public String readLine(BufferedReader br,
		ICmdLineCommandExecutorObserver observer) throws IOException {
		
		String s = br.readLine();	
		if(s!= null && s.contains(Messages.getString("ReportFilterLineReader.PID"))) //$NON-NLS-1$
		{
			String processId = s.substring(s.indexOf(":") + 1); //$NON-NLS-1$
			this.pid = Integer.parseInt(processId);
			
		}
		if(s != null && s.contains(Messages.getString("ReportFilterLineReader.Processing"))) //$NON-NLS-1$
		{
			currentLine = s;
			observer.progress(Integer.parseInt(s.substring(s.indexOf("(") +1, s.indexOf("/")))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(s != null && s.contains(Messages.getString("ReportFilterLineReader.TotalIssues"))) //$NON-NLS-1$
		{
			((ReportFilterEngine)observer).beginFilteration();
		}
		if(s != null && s.contains("%")) //$NON-NLS-1$
		{
			String percent = s.substring(0, s.indexOf("%")); //$NON-NLS-1$
			int p = Integer.parseInt(percent);
			((ReportFilterEngine)observer).progressFilteration(p);
		}
		if(s != null && s.contains(Messages.getString("ReportFilterLineReader.exitCode"))) //$NON-NLS-1$
		{
			((ReportFilterEngine)observer).endFilteration(s.substring(s.indexOf(":")+1)); //$NON-NLS-1$
		}
		if(s != null && s.contains(Messages.getString("ReportFilterLineReader.ReportFile"))) //$NON-NLS-1$
		{
			currentFile=s.substring(s.indexOf(":") + 2); //$NON-NLS-1$
		}
		if(s!=null && s.contains(Messages.getString("ReportFilterLineReader.OutputFile"))) //$NON-NLS-1$
		{
			outputFile = s.substring(s.indexOf(":") + 2); //$NON-NLS-1$
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
