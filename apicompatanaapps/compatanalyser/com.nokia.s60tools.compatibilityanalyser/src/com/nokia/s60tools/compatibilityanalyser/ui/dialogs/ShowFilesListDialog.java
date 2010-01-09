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
* It also allows selection of files and filteration of files. It does not display
* previously selected files.
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.FeedbackHandler;


public class ShowFilesListDialog extends Dialog implements SelectionListener, ModifyListener {

	public String[] fileNamesList;
	public List filesList;
	public String [] selectedItems = null;
	public ArrayList<String> children = new ArrayList<String>();
	public ArrayList<String> subChildren = new ArrayList<String>();
	
	private Shell shell;
	private Button ok;
	private Button cancel;
	
	private boolean isOpened = false;
	private FeedbackHandler feedbackHandler;
	private Text filterText;
	private Button hType;
	private Button hrhType;
	private Button rsgType;
	private Button mbgType;
	private Button hppType;
	private Button panType;
	
	private Button useRecursion;
	private String dirName;
	private Control listControl;
	private boolean useFilterFlags = false;
	private String [] backUpList = null;

	public ShowFilesListDialog(Shell parent, Control list, FeedbackHandler fh, String dirName, boolean useFilterFlags) {
		super(parent);
		listControl=list;
		feedbackHandler = fh;
		this.dirName = dirName;
		this.useFilterFlags = useFilterFlags;

	}
	public void open()
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE |SWT.APPLICATION_MODAL|SWT.BORDER);
		shell.setText(Messages.getString("ShowFilesListDialog.AddFiles")); //$NON-NLS-1$
		shell.setSize(450, 530);
		shell.setLocation(400, 100);

		shell.setLayout(new GridLayout(2,false));
		shell.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label titleLbl=new Label(shell,SWT.NONE);
		if(this.dirName.equalsIgnoreCase(""))
			titleLbl.setText("List of HeaderFiles from multiple directories");
		else
			titleLbl.setText(Messages.getString("ShowFilesListDialog.ListOfFilesIn") + this.dirName + Messages.getString("ShowFilesListDialog.Directory")); //$NON-NLS-1$ //$NON-NLS-2$
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
		filterText.addSelectionListener(this);

		GridData griddata2=new GridData(GridData.FILL_BOTH);
		griddata2.horizontalSpan=2;
		griddata2.heightHint=300;
		griddata2.horizontalAlignment=GridData.FILL;

		filesList = new List(shell,SWT.MULTI|SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		filesList.setLayoutData(griddata2);
		filesList.addSelectionListener(this);

		if(useFilterFlags)
			createFilterGroup(shell);

		GridData data3=new GridData(GridData.FILL_BOTH);
		data3.horizontalSpan=2;
		Composite comp3=new Composite(shell,SWT.NONE); 
		comp3.setLayout(new GridLayout(2,false));
		comp3.setLayoutData(data3);

		ok = new Button(comp3,SWT.PUSH);
		ok.setText(Messages.getString("ShowFilesListDialog.Add"));
		GridData buttondata= new GridData(GridData.FILL_HORIZONTAL);
		buttondata.widthHint=100;
		buttondata.verticalIndent=20;
		buttondata.horizontalAlignment=GridData.END;
		ok.setLayoutData(buttondata);
		ok.addSelectionListener(this);

		cancel = new Button(comp3,SWT.PUSH);
		cancel.setText(Messages.getString("ShowFilesListDialog.Cancel"));
		GridData buttondata2= new GridData(GridData.FILL_HORIZONTAL);
		buttondata2.widthHint=100;
		buttondata2.verticalIndent=20;
		buttondata2.horizontalAlignment=GridData.BEGINNING;

		cancel.setLayoutData(buttondata2);
		cancel.addSelectionListener(this);

		shell.open();		
	}

	private void createFilterGroup(Composite shell)
	{

		Label selectTypeLbl=new Label(shell, SWT.NONE);
		selectTypeLbl.setText("Select file types :");

		Group typesGrp = new Group(shell, SWT.NONE);
		GridLayout typesGL = new GridLayout();
		typesGL.numColumns = 6;
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);

		typesGrp.setLayout(typesGL);
		typesGrp.setLayoutData(gd2);
		typesGrp.setVisible(true);

		hType = new Button(typesGrp, SWT.CHECK);
		hType.setText(Messages.getString("HeaderFilesPage.30"));
		hType.setSelection(true);
		hType.addSelectionListener(this);

		hrhType = new Button(typesGrp, SWT.CHECK);
		hrhType.setText(Messages.getString("HeaderFilesPage.32"));
		hrhType.setSelection(true);
		hrhType.addSelectionListener(this);

		rsgType = new Button(typesGrp, SWT.CHECK);
		rsgType.setText(Messages.getString("HeaderFilesPage.33"));
		rsgType.setSelection(true);
		rsgType.addSelectionListener(this);

		mbgType = new Button(typesGrp, SWT.CHECK);
		mbgType.setText(Messages.getString("HeaderFilesPage.34"));
		mbgType.setSelection(true);
		mbgType.addSelectionListener(this);

		hppType = new Button(typesGrp, SWT.CHECK);
		hppType.setText(Messages.getString("HeaderFilesPage.35"));
		hppType.setSelection(true);
		hppType.addSelectionListener(this);
		
		panType = new Button(typesGrp, SWT.CHECK);
		panType.setText(Messages.getString("HeaderFilesPage.36"));
		panType.setSelection(true);
		panType.addSelectionListener(this);
		
		GridData griddata2=new GridData(GridData.FILL_HORIZONTAL);
		griddata2.horizontalSpan=2;

		useRecursion = new Button(shell,SWT.CHECK);
		useRecursion.setText("Show files under sub directories");
		useRecursion.setLayoutData(griddata2);
		useRecursion.setSelection(true);
		useRecursion.addSelectionListener(this);

	}

	public void widgetDefaultSelected(SelectionEvent e) {
	
	}
	public void widgetSelected(SelectionEvent e) {
		
		if(e.widget == ok)
		{		
			selectedItems = filesList.getSelection();
			if(selectedItems != null)
			{
				String s [] = new String[selectedItems.length];
				int i =0;
				while(i<selectedItems.length)
				{
					s[i] = selectedItems[i].substring(selectedItems[i].lastIndexOf("\\") +1); //$NON-NLS-1$
					if(listControl instanceof Table)
					{
						TableItem item = new TableItem(((Table)listControl), SWT.NONE);
						item.setText(new String[]{s[i], ""}); //$NON-NLS-1$
					}
					else if(listControl instanceof List)
					{
						((List)listControl).add(s[i]);
					}

					i++;

				}
				if(listControl instanceof Table)
				{
					((Table)listControl).select(0);
				}
				else if(listControl instanceof List)
				{
					((List)listControl).select(0);
				}

			}
			if(shell != null)
				shell.close();

			feedbackHandler.UpdatePage();
		}
		else if(e.widget == cancel)
		{
			if(shell!=null)
				shell.close();
		}
		else if(e.widget == useRecursion)
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
		else if(e.widget == hType || e.widget == hrhType || e.widget == rsgType
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

	public void modifyText(ModifyEvent e)
	{
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
	private void updateButtons()
	{
		if(filesList.getItemCount() == 0)
			ok.setEnabled(false);
		else
			ok.setEnabled(true);
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
}
