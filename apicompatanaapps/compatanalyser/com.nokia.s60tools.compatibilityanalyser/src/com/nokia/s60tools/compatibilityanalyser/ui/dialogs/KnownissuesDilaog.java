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
* Description: Class to display KnownIssues settings
*
*/
package com.nokia.s60tools.compatibilityanalyser.ui.dialogs;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.LastUsedKnownissues;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageKeys;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;

/**
 * Class for KnownIssues dialog
 *
 */
public class KnownissuesDilaog extends TrayDialog implements SelectionListener{

	private Composite shell;
	private Button ok;
	private Button cancel;
	private Button browse;
	private List cmb;
	private Button radio1;
	private Button radio2;
	private Button radio3;
	private Button radio4;
	private List list;
	private Button remove;
	private LastUsedKnownissues issuesStore=new LastUsedKnownissues();
	private String issuesUrl;
	
	public KnownissuesDilaog(Shell parent,String issuesUrl) {
		super(parent);
		setShellStyle(getShellStyle()|SWT.RESIZE);
		this.issuesUrl=issuesUrl;
	}
	public KnownissuesDilaog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle()|SWT.RESIZE);
	}
	protected Control createContents(Composite parent) {
		shell = new Composite(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		shell.getShell().setText(Messages.getString("KnownissuesDilaog.KnownIssuesDialog")); //$NON-NLS-1$
		shell.setLocation(350, 100);
		
		shell.setLayout(new GridLayout(2,false));
		shell.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label titleLbl=new Label(shell,SWT.NONE);
		titleLbl.setText(Messages.getString("KnownissuesDilaog.TitleLabel")); //$NON-NLS-1$
		GridData griddata=new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		griddata.horizontalSpan=2;
		griddata.horizontalAlignment=GridData.FILL;
		titleLbl.setLayoutData(griddata);
		
		Group grp=new Group(shell,SWT.NONE);
		grp.setText(Messages.getString("KnownissuesDilaog.GroupName")); //$NON-NLS-1$
		grp.setLayout(new GridLayout(2,false));
		GridData griddata2=new GridData(GridData.FILL_HORIZONTAL);
		griddata2.horizontalSpan=2;
		griddata2.horizontalAlignment=GridData.FILL;
		
		grp.setLayoutData(griddata2);
		
		radio1 = new Button(grp,SWT.RADIO);
		radio1.setText(Messages.getString("KnownissuesDilaog.UseDefaultIssuesFile")); //$NON-NLS-1$
		radio1.setToolTipText(Messages.getString("KnownissuesDilaog.UseIssuesProvidedBytheTool")); //$NON-NLS-1$
		GridData radio1data= new GridData(GridData.FILL_HORIZONTAL);
		radio1data.horizontalSpan=2;
		radio1.setLayoutData(radio1data);
		radio1.addSelectionListener(this);
		
		radio2 = new Button(grp,SWT.RADIO);
		radio2.setText(Messages.getString("KnownissuesDilaog.UseLatestIssesFromWeb")); //$NON-NLS-1$
		radio2.setToolTipText(Messages.getString("KnownissuesDilaog.UseSelectedIssuesFromWeb")); //$NON-NLS-1$
		GridData radio2data= new GridData(GridData.FILL_HORIZONTAL);
		radio2data.horizontalSpan=2;
		radio2.setLayoutData(radio2data);
		radio2.addSelectionListener(this);
		
		radio3 = new Button(grp,SWT.RADIO);
		radio3.setText(Messages.getString("KnownissuesDilaog.UseIssuesFromLocalFilesystem")); //$NON-NLS-1$
		radio3.setToolTipText(Messages.getString("KnownissuesDilaog.LabelUseBelowFile")); //$NON-NLS-1$
		GridData radio3data= new GridData(GridData.FILL_HORIZONTAL);
		radio3data.horizontalSpan=2;
		radio3.setLayoutData(radio3data);
		radio3.addSelectionListener(this);
		
		list = new List(grp,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
		GridData listdata= new GridData(GridData.FILL_HORIZONTAL);
		listdata.heightHint=100;
		listdata.horizontalSpan=2;
		list.setLayoutData(listdata);
		list.select(0);
		
		radio4 = new Button(grp,SWT.RADIO);
		radio4.setText(Messages.getString("KnownissuesDilaog.UseIssuesFileFromLocalFileSystem")); //$NON-NLS-1$
		GridData radio4data= new GridData(GridData.FILL_HORIZONTAL);
		radio4data.horizontalSpan=2;
		radio4.setLayoutData(radio4data);
		radio4.addSelectionListener(this);
		
		cmb = new List(grp,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
		GridData listdata2= new GridData(GridData.FILL_HORIZONTAL);
		listdata2.heightHint=80;
		listdata2.verticalSpan=2;
		cmb.setLayoutData(listdata2);
		String[] paths=issuesStore.getPreviousValues(LastUsedKnownissues.ValueTypes.LOCAL_ISSUES_PATH);
		
		if(paths!=null)
		{
			addOnlyExistingFiles(paths);
		}
		browse = new Button(grp,SWT.PUSH);
		browse.setText(Messages.getString("KnownissuesDilaog.Add")); //$NON-NLS-1$
		browse.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_BEGINNING));
		browse.addSelectionListener(this);
		
		remove = new Button(grp,SWT.PUSH);
		remove.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_BEGINNING));
		remove.setText(Messages.getString("KnownissuesDilaog.Remove")); //$NON-NLS-1$
		remove.addSelectionListener(this);
		
		// restores all the previous data from the preferenceStore 
		
		shell.getShell().setImage(ImageResourceManager.getImage(ImageKeys.KNOWNISSUES_DIALOG_ICON));
		
		setDialogHelpAvailable(false);
		setHelpAvailable(true);
		setHelp();
		return super.createContents(shell);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ok=this.createButton(parent, IDialogConstants.OK_ID, Messages.getString("KnownissuesDilaog.Ok"), false); //$NON-NLS-1$
		ok.addSelectionListener(this);
		cancel=this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("KnownissuesDilaog.Cancel"), false); //$NON-NLS-1$
		cancel.addSelectionListener(this);
		getPrefsStoreValues();
		
	}
	
	@Override
	protected void okPressed() {
	}
	@Override
	protected void cancelPressed() {
	}
	
	private void getPrefsStoreValues() {
		IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		
		radio1.setSelection(store.getBoolean(CompatibilityAnalyserPreferencesConstants.DEFAULT_ISSUES));
		radio2.setSelection(store.getBoolean(CompatibilityAnalyserPreferencesConstants.LATEST_ISSUES));
		radio3.setSelection(store.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES));
		radio4.setSelection(store.getBoolean(CompatibilityAnalyserPreferencesConstants.LOCAL_ISSUES));
		if(radio1.getSelection()==true)
		{
			list.setEnabled(false);
			cmb.setEnabled(false);
			browse.setEnabled(false);
			remove.setEnabled(false);
		}
		if(radio2.getSelection()==true)
		{
			list.setEnabled(false);
			cmb.setEnabled(false);
			browse.setEnabled(false);
			remove.setEnabled(false);
		}
		if(radio3.getSelection()==true)
		{
			ArrayList<String> issuesList = new ArrayList<String>();
			String status = CompatibilityAnalyserEngine.readMetadataFileFromWebServer(issuesUrl, Messages.getString("CompatibilityAnalyserEngine.knownissues_metadata"), issuesList);
			
			list.setEnabled(true);
			
			if(status == null)
			{
				radio3.setText(Messages.getString("KnownissuesDilaog.UseSelectedFromLocalFileSystem")); //$NON-NLS-1$
				String [] items = issuesList.toArray(new String [0]);
				if(items != null)
				{
					list.setItems(items);
					list.setSelection(0);
				}
				ok.setEnabled(true);
			}
			else
			{
				radio3.setText(Messages.getString("KnownissuesDilaog.UseIssuesFromWebCouldNotFound")); //$NON-NLS-1$
				radio3.getShell().layout();
				ok.setEnabled(false);
			}
			cmb.setEnabled(false);
			browse.setEnabled(false);
			remove.setEnabled(false);
						
		}
		if(radio4.getSelection()==true)
		{
			list.setEnabled(false);
			cmb.setEnabled(true);
			browse.setEnabled(true);
			remove.setEnabled(true);
			
			if(cmb.getItemCount()!=0)
				ok.setEnabled(true);
			else
				ok.setEnabled(false);
		}
	}
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}
	public void widgetSelected(SelectionEvent e) {
		
		if(e.widget==cancel)
		{
			if(shell!=null)
			shell.getShell().dispose();
		}
		else if(e.widget==browse)
		{
			FileDialog fileSelector = new FileDialog(shell.getShell(), SWT.MULTI);
			fileSelector.setText(Messages.getString("KnownissuesDilaog.AddFiles")); //$NON-NLS-1$
			String[] filterExt = { "*.xml","*.*"};   //$NON-NLS-1$ //$NON-NLS-2$
			fileSelector.setFilterExtensions(filterExt);
			
			if((fileSelector.open())!=null)
			{
				String [] files = fileSelector.getFileNames();
				String filterPath = fileSelector.getFilterPath();
				
				ArrayList<String> invalidfiles = new ArrayList<String>();
				for(String s: files)
				{
					if(!filterPath.endsWith(File.separator))
					   filterPath+=File.separator;
					
					try {
						DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filterPath+s));
					} catch (Exception e1) {
						invalidfiles.add(filterPath+s);
						continue;
					}
					cmb.add(filterPath+s);
					cmb.select(0);
				}
				if(invalidfiles.size() > 0)
				{
					String set = "";
					for(String f:invalidfiles)
						set = set + f + "\n";
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Knownissues...", "Selected file(s) are invalid xml files,\n"+set);
				}
				ok.setEnabled(cmb.getItemCount()>0);
				remove.setEnabled(cmb.getItemCount()>0);
			}
			else
				return;
		}
		else if(e.widget==ok)
		{
			IPreferenceStore store=CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
			store.setValue(CompatibilityAnalyserPreferencesConstants.DEFAULT_ISSUES, radio1.getSelection());
			store.setValue(CompatibilityAnalyserPreferencesConstants.LATEST_ISSUES, radio2.getSelection());
			store.setValue(CompatibilityAnalyserPreferencesConstants.WEB_ISSUES, radio3.getSelection());
			store.setValue(CompatibilityAnalyserPreferencesConstants.LOCAL_ISSUES, radio4.getSelection());
			
			if(radio3.getSelection()==true)
			{
				if(!issuesUrl.endsWith("/")) //$NON-NLS-1$
					issuesUrl=issuesUrl+"/"; //$NON-NLS-1$
				String[] fullURLs=list.getSelection();
				for (int i = 0; i < fullURLs.length; i++) {
					fullURLs[i]=issuesUrl+fullURLs[i];
				}
				issuesStore.saveValues(LastUsedKnownissues.ValueTypes.ISSUES_URL, fullURLs);
				String[] temp={issuesUrl,""}; //$NON-NLS-1$
				issuesStore.saveValues(LastUsedKnownissues.ValueTypes.WEBSERVER_MAIN_URL, temp);
			}
			else if(radio4.getSelection()==true)
			{
				issuesStore.saveValues(LastUsedKnownissues.ValueTypes.LOCAL_ISSUES_PATH, cmb.getItems());
			}						
			
			if(shell!=null)
				shell.getShell().dispose();
			
		}
		else if(e.widget==radio1)
		{
			list.setEnabled(false);
			cmb.setEnabled(false);
			browse.setEnabled(false);
			remove.setEnabled(false);
			ok.setEnabled(true);
		}
		else if(e.widget==radio2)
		{
			list.setEnabled(false);
			cmb.setEnabled(false);
			browse.setEnabled(false);
			remove.setEnabled(false);
			ok.setEnabled(true);
		}
		else if(e.widget==radio3 && radio3.getSelection())
		{
			ArrayList<String> issuesList = new ArrayList<String>();
			String status = CompatibilityAnalyserEngine.readMetadataFileFromWebServer(issuesUrl.trim(), Messages.getString("CompatibilityAnalyserEngine.knownissues_metadata"), issuesList);
			list.setEnabled(true);

			if(status == null)
			{
				radio3.setText(Messages.getString("KnownissuesDilaog.UseSelectedFilesFromWeb")); //$NON-NLS-1$
				String [] items = issuesList.toArray(new String [0]);
				if(items != null)
				{
					list.setItems(items);
					list.setSelection(0);
				}
				ok.setEnabled(true);	
			}
			else
			{
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Knownissues...", status);
				radio3.setText(Messages.getString("KnownissuesDilaog.UseIssuesFromWebCouldNotFound")); //$NON-NLS-1$
				ok.setEnabled(false);
				list.setEnabled(false);
			}
			
			cmb.setEnabled(false);
			browse.setEnabled(false);
			remove.setEnabled(false);
		}
		else if(e.widget==radio4)
		{
			list.setEnabled(false);
			cmb.setEnabled(true);
			browse.setEnabled(true);
			remove.setEnabled(cmb.getItemCount()>0);
			ok.setEnabled(cmb.getItemCount()>0);
		}	
		else if(e.widget == remove)
		{
			cmb.remove(cmb.getSelectionIndices());
			remove.setEnabled(cmb.getItemCount()>0);
			if(cmb.getItemCount()!=0)
			cmb.select(0);
			else
			ok.setEnabled(false);	
			
		}
	}
	public void addOnlyExistingFiles(String[] paths)
	{
		for (int i = 0; i < paths.length; i++) {
		File issues=new File(paths[i]);
			if(issues.exists() )
			{
				cmb.add(paths[i]);			
			}
		}
	}
	
	public void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell.getShell(),
				HelpContextIDs.CONFIGURE_KNOWNISSUES);
	}	
}
