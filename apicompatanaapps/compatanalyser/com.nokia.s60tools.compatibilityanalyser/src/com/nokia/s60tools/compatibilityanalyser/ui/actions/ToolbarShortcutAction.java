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
* Description: Action class used to launch the tool from Carbide Menu
*
*/

package com.nokia.s60tools.compatibilityanalyser.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.s60tools.compatibilityanalyser.ui.views.MainView;

public class ToolbarShortcutAction implements IWorkbenchWindowActionDelegate {

	public void dispose() {
	
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		MainView view=MainView.showAndReturnYourself();
        view.showWizard();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
