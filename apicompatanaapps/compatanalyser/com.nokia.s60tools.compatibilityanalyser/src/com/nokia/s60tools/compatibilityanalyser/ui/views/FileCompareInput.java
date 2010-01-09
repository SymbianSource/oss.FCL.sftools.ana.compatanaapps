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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Creates a subclass of CompareEditorInput which is initialized with the given compare configuration.
 * @author rgubba
 *
 */
public class FileCompareInput extends CompareEditorInput {
	File left;
	File right;

	public FileCompareInput(CompareConfiguration cc, File left, File right) {
		super(cc);
		this.left = left;
		this.right = right;
		
		if(left!=null && right !=null)
			setTitle("Compare ("+left.getName()+"-"+right.getName()+")");
		else
			setTitle("Compare");
	}

	@Override
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (right == null || left == null) {
            return null;
        }
		
		CompareItem left = new CompareItem("Left", this.left);
		CompareItem right = new CompareItem("Right", this.right);
		
		try {
            Differencer differencer = new Differencer();
            monitor.beginTask("Bytecode Outline: comparing...", 30);
            IProgressMonitor sub = new SubProgressMonitor(monitor, 10);
            try {
                sub.beginTask("Bytecode Outline: comparing...", 100);
                return differencer.findDifferences(false, sub, null, null, left, right);
            } finally {
                sub.done();
            }
        } catch (OperationCanceledException e) {
            throw new InterruptedException(e.getMessage());
        } finally {
            monitor.done();
        }
		//return (new DiffNode(null, Differencer.CHANGE, null, left, right));
	}
	
	@Override
	public void saveChanges(IProgressMonitor arg0) throws CoreException {
		// TODO Auto-generated method stub
		super.saveChanges(arg0);
	}
}
