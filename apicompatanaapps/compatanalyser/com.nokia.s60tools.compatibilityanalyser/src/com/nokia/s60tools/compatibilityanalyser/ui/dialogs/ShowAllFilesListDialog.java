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
* Description: Dialog displays all supported files based on selected directory path.
* It also allows selection of files and filteration of files. 
*
*/
package com.nokia.s60tools.compatibilityanalyser.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;

public class ShowAllFilesListDialog extends Dialog implements SelectionListener, ModifyListener{

	public List filesList;
	public String[] fileNamesList;
	public ArrayList<String> children = new ArrayList<String>();
	public ArrayList<String> subChildren = new ArrayList<String>();
	
	private Shell shell;
	private Button ok;
	private Button cancel;
	private Text filterText;
	private String label;
	private boolean isOpened = false; 
	private Button hType;
	private Button hrhType;
	private Button rsgType;
	private Button mbgType;
	private Button hppType;
	private Button panType;

	private Button useRecursion;
	private Control resultControl;
	private String [] backUpList = null;

	
	public ShowAllFilesListDialog(Shell parent, String label, Control resultControl) {
		super(parent);
		this.label = label;
		this.resultControl = resultControl;
	}

	public void open()
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE |SWT.APPLICATION_MODAL|SWT.BORDER);
		shell.setText("");
		shell.setSize(450, 520);
		shell.setLocation(400, 100);
		shell.setText("Filename Selection Dialog");
		shell.setLayout(new GridLayout(2,false));
		shell.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label titleLbl=new Label(shell,SWT.NONE);
		titleLbl.setText(label);

		GridData griddata=new GridData(GridData.VERTICAL_ALIGN_END);
		griddata.horizontalSpan=2;
		griddata.horizontalAlignment=GridData.FILL;
		titleLbl.setLayoutData(griddata);

		filterText = new Text(shell, SWT.BORDER);
		filterText.setText("[type filter text here]");
		GridData griddata1 = new GridData(GridData.VERTICAL_ALIGN_END);
		griddata1.horizontalSpan=2;
		griddata1.horizontalAlignment=GridData.FILL;
		filterText.setLayoutData(griddata1);
		filterText.addModifyListener(this);
		filterText.selectAll();

		GridData griddata2=new GridData(GridData.FILL_BOTH);
		griddata2.horizontalSpan=2;
		griddata2.heightHint=300;
		griddata2.horizontalAlignment=GridData.FILL;

		filesList = new List(shell,SWT.SINGLE|SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		filesList.setLayoutData(griddata2);

		filesList.addSelectionListener(this);

		Label selectTypeLbl=new Label(shell, SWT.WRAP);
		selectTypeLbl.setText("Select file types :");

		Composite comp2=new Composite(shell,SWT.NONE); 
		comp2.setLayout(new GridLayout(6,false));
		comp2.setLayoutData(new GridData(GridData.BEGINNING));

		hType = new Button(comp2, SWT.CHECK);
		hType.setText(Messages.getString("HeaderFilesPage.30"));
		hType.setSelection(true);
		hType.addSelectionListener(this);

		hrhType = new Button(comp2, SWT.CHECK);
		hrhType.setText(Messages.getString("HeaderFilesPage.32"));
		hrhType.setSelection(true);
		hrhType.addSelectionListener(this);

		rsgType = new Button(comp2, SWT.CHECK);
		rsgType.setText(Messages.getString("HeaderFilesPage.33"));
		rsgType.setSelection(true);
		rsgType.addSelectionListener(this);

		mbgType = new Button(comp2, SWT.CHECK);
		mbgType.setText(Messages.getString("HeaderFilesPage.34"));
		mbgType.setSelection(true);
		mbgType.addSelectionListener(this);

		hppType = new Button(comp2, SWT.CHECK);
		hppType.setText(Messages.getString("HeaderFilesPage.35"));
		hppType.setSelection(true);
		hppType.addSelectionListener(this);
		
		panType = new Button(comp2, SWT.CHECK);
		panType.setText(Messages.getString("HeaderFilesPage.36"));
		panType.setSelection(true);
		panType.addSelectionListener(this);
		
		useRecursion = new Button(shell,SWT.CHECK);
		useRecursion.setText("Show files under sub directories");
		useRecursion.setLayoutData(griddata);
		useRecursion.setSelection(true);
		useRecursion.addSelectionListener(this);

		GridData data3=new GridData(GridData.FILL_BOTH);
		data3.horizontalSpan=2;
		Composite comp3=new Composite(shell,SWT.NONE); 
		comp3.setLayout(new GridLayout(2,false));
		comp3.setLayoutData(data3);

		ok = new Button(comp3,SWT.PUSH);
		ok.setText("Ok");
		GridData buttondata= new GridData(GridData.FILL_HORIZONTAL);
		buttondata.widthHint=100;
		buttondata.verticalIndent=20;
		buttondata.horizontalAlignment=GridData.END;
		ok.setLayoutData(buttondata);
		ok.addSelectionListener(this);

		cancel = new Button(comp3,SWT.PUSH);
		cancel.setText("Cancel");
		GridData buttondata2= new GridData(GridData.FILL_HORIZONTAL);
		buttondata2.widthHint=100;
		buttondata2.verticalIndent=20;
		buttondata2.horizontalAlignment=GridData.BEGINNING;

		cancel.setLayoutData(buttondata2);
		cancel.addSelectionListener(this);

		shell.open();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if(e.widget == ok)
		{
			if(resultControl instanceof Text)
			{
				Text tmpText = (Text)resultControl;
				tmpText.setText(filesList.getItem(filesList.getSelectionIndex()));
			}
			if(shell != null)
				shell.close();
		}
		else if(e.widget == cancel)
		{
			if(shell != null)
				shell.close();
		}
		else if(e.widget == filterText)
		{
			if(filterText.getText().equals("[type filter text here]"))
				filterText.setText("");
		}
		if(e.widget == useRecursion)
		{
			if(useRecursion.getSelection())
			{
				ArrayList<String> allFiles = new ArrayList<String>(children);

				for(String s:subChildren)
				{
					if(!allFiles.contains(s))
						allFiles.add(s);
				}
				filesList.removeAll();
				Collections.sort(allFiles);
				fileNamesList = allFiles.toArray(new String[0]);
				backUpList = allFiles.toArray(new String[0]);
				filesList.setItems(fileNamesList);
			}
			else
			{
				Collections.sort(children);
				filesList.removeAll();
				fileNamesList = children.toArray(new String[0]);
				backUpList = children.toArray(new String[0]);
				filesList.setItems(fileNamesList);
			}
			processListBasedOnSelectedTypes(backUpList);

			if(filterText.getText().length() > 0 && !filterText.getText().equalsIgnoreCase("[type filter text here]"))
				filterAndShowFiles(filterText.getText());
			updateButtons();
		}
		if(e.widget == hType || e.widget == hrhType || e.widget == rsgType 
				|| e.widget == mbgType || e.widget == hppType || e.widget == panType)
		{
			if(backUpList == null && fileNamesList != null)
			{
				backUpList = new String[fileNamesList.length];
				System.arraycopy(fileNamesList,0,backUpList,0,fileNamesList.length);
			}		
			processListBasedOnSelectedTypes(backUpList);
			if(filterText.getText().length() > 0 && !filterText.getText().equalsIgnoreCase("[type filter text here]"))
				filterAndShowFiles(filterText.getText());

			updateButtons();
		}
	}

	public void modifyText(ModifyEvent e) {
		if(e.widget == filterText)
		{
			String token = filterText.getText();
			filterAndShowFiles(token);
		}	
	}
	public void setOpened ()
	{
		isOpened = true;
	}
	public boolean isOpen()
	{
		return isOpened;
	}
	private void filterAndShowFiles(String token)
	{
		filesList.removeAll();

		if(fileNamesList != null)
		{
			for(int i =0; i< fileNamesList.length;i++)
			{
				String tmp = fileNamesList[i];
				if(tmp.contains("\\"))
				{
					String name = tmp;
					while(name.indexOf("\\") != -1)
					{
						name = name.substring(name.indexOf("\\") +1);
						String lowCaseName = name.toLowerCase();
						if(lowCaseName.startsWith(token.toLowerCase())){
							filesList.add(tmp);
							break;
						}
					}

				}
				else
				{
					String lowerCaseTmp = tmp.toLowerCase();
					if(lowerCaseTmp.startsWith(token.toLowerCase()))
					{
						filesList.add(tmp);
					}
				}
			}
		}

	} 

	private void processListBasedOnSelectedTypes(String [] backupList)
	{
		if(backupList == null)
			return;

		if(hType.getSelection() && hrhType.getSelection() && mbgType.getSelection() 
				&& rsgType.getSelection() && hppType.getSelection() && panType.getSelection())
		{
			filesList.setItems(backUpList);
			fileNamesList = filesList.getItems();
		}
		else
		{
			ArrayList<String> tempArray = new ArrayList<String>();

			for(String s:backUpList)
			{
				if(s.endsWith(CompatibilityAnalyserEngine.H_EXTN) && !hType.getSelection())
					continue;
				else if(s.endsWith(CompatibilityAnalyserEngine.HRH_EXTN) && !hrhType.getSelection())
					continue;
				else if(s.endsWith(CompatibilityAnalyserEngine.RSG_EXTN) && !rsgType.getSelection())
					continue;
				else if(s.endsWith(CompatibilityAnalyserEngine.MBG_EXTN) && !mbgType.getSelection())
					continue;
				else if(s.endsWith(CompatibilityAnalyserEngine.HPP_EXTN) && !hppType.getSelection())
					continue;
				else if(s.endsWith(CompatibilityAnalyserEngine.PAN_EXTN) && !panType.getSelection())
					continue;
				
				tempArray.add(s);
			}
			filesList.removeAll();
			Collections.sort(tempArray);
			fileNamesList = tempArray.toArray(new String[0]);
			filesList.setItems(fileNamesList);
		}

	}

	private void updateButtons()
	{
		if(filesList.getItemCount() == 0)
			ok.setEnabled(false);
		else
			ok.setEnabled(true);
	}
}
