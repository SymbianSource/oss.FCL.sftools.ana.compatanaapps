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
import java.util.ArrayList;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.nokia.s60tools.compatibilityanalyser.CompatibilityAnalyserPlugin;
import com.nokia.s60tools.compatibilityanalyser.data.BCAndSCIssues;
import com.nokia.s60tools.compatibilityanalyser.data.ReportData;
import com.nokia.s60tools.compatibilityanalyser.data.ReportWizardData;
import com.nokia.s60tools.compatibilityanalyser.data.ReportData.FILE_TYPE;
import com.nokia.s60tools.compatibilityanalyser.model.AnalysisFeedbackHandler;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueData;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueFileData;
import com.nokia.s60tools.compatibilityanalyser.model.HeaderAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.model.LibraryAnalyserEngine;
import com.nokia.s60tools.compatibilityanalyser.model.LibraryIssueData;
import com.nokia.s60tools.compatibilityanalyser.model.ReportFilterEngine;
import com.nokia.s60tools.compatibilityanalyser.model.CompatibilityIssueData.ISSUE_CATEGORY;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageKeys;
import com.nokia.s60tools.compatibilityanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.compatibilityanalyser.resources.Messages;
import com.nokia.s60tools.compatibilityanalyser.ui.actions.OpenSourceFileIssuesViewAction;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.AnalysisWizard;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.CompatibilityAnalyserPreferencesConstants;
import com.nokia.s60tools.compatibilityanalyser.ui.wizards.ReportFilterWizard;
import com.nokia.s60tools.compatibilityanalyser.utils.CompatibilityAnalyserUtils;
import com.nokia.s60tools.compatibilityanalyser.utils.HelpContextIDs;

/**
 * CompatibilityAnalyser view class.
 *
 */
public class MainView extends ViewPart implements AnalysisFeedbackHandler {

	public static final String ID = Messages.getString("MainView.0"); //$NON-NLS-1$
	
	private SashForm listViewSashForm;
	private TableViewer tableviewer;
	private TreeViewer treeViewer;
	
	private Action openAnalysisWizardAction;
	private Action doubleClickReportAction;
	private Action openReportWizardAction;
	private Action clearAction;
	private Action filterReportAction;
	private Action selectAllAction;
	private Action openInXmlEditorAction;
	private Action collapseAction;
	private Action openIssueAction;
	private Action openInwebBrowserAction;
	private Action showDiffAction;

	private ViewerSelectionListener selectionListener = new ViewerSelectionListener(this);
	
	private CompatibilityAnalyserEngine engine = null;
	private ReportWizardData data=null;
	
	private ReportFilterEngine reportEngine;
	public String[] selectedReportfilesInViews;

	private String feedbackMessage;
	private String feedbackMessageTitle;
	private OpenSourceFileIssuesViewAction doubleClickIssuesAction;
	
	Color RED_COLOR = new Color(Display.getCurrent(),255,141,28);
	Color YELLOW_COLOR = new Color(Display.getCurrent(),255,255,128);
	String BC_ROOT = "BC Breaks";
	String SC_ROOT = "SC Breaks";
	
	/**
	 * The content provider class is responsible for
	 * providing objects to the view.
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			Object[] reportFiles = null;
			
			if(engine != null ||reportEngine!=null){
			    reportFiles = CompatibilityAnalyserEngine.getReportFiles();
			}
			if(reportFiles == null || reportFiles.length==0)
			{
				reportFiles = new String[1];
				reportFiles[0] = new String(Messages.getString("MainView.DoubleClickHereToStartAnalysis")); //$NON-NLS-1$
			}
			return reportFiles;
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		private Image img;
		public String getColumnText(Object obj, int index) {
			if(obj instanceof ReportData)
			{
				if (index == 0)
					return ((ReportData)obj).getName();
				else if(index ==1)
					return ((ReportData)obj).gettLastModifiedDate();
				else if(index == 2){
					ReportData report = (ReportData)obj;
					return report.getFilterationStatus();
				}
			}
			else{
				if(index == 0)
					return obj.toString();
			}
			return "";
		}
		public Image getColumnImage(Object obj, int index) {
			if(index == 0)
				return getImage(obj);
			
			return null;
		}
		public Image getImage(Object obj) {
			if(obj.toString().startsWith(Messages.getString("MainView.DoubleClickHereToStartAnalysis"))) //$NON-NLS-1$
			{
				return new Image(Display.getCurrent(),CompatibilityAnalyserPlugin.getPluginInstallPath()+Messages.getString("MainView.CompatibilityAnalyserIconPath")); //$NON-NLS-1$
			}
			else
			{
				Program p=Program.findProgram(".xml"); //$NON-NLS-1$
				img = null;
				if(p!=null)
					img = new Image(Display.getCurrent(),p.getImageData());
			
				return img;
			}
		}
	}

	public void createPartControl(Composite parent) {
		listViewSashForm = new SashForm(parent, SWT.HORIZONTAL);
		listViewSashForm.SASH_WIDTH = 5;
		listViewSashForm.setLayout(new GridLayout(2, false));
		
		tableviewer = createListViewTableViewer(listViewSashForm);
		tableviewer.setContentProvider(new ViewContentProvider());
		tableviewer.setLabelProvider(new ViewLabelProvider());
		tableviewer.setInput(getViewSite());
		
		treeViewer = createTreeViewer(listViewSashForm);
		treeViewer.setLabelProvider(new IssuesViewerLabelProvider());
		treeViewer.setContentProvider(new IssuesViewerContentProvider());
		
		listViewSashForm.setWeights(new int[] {2,1});
		
		tableviewer.addSelectionChangedListener(selectionListener);
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		setHelp();
	}

	/**
     * Makes main view visible and returns an instanse of itself.
     * @return instance of main view
     */
	public static MainView showAndReturnYourself() {
    	try {

    		IWorkbenchWindow ww = CompatibilityAnalyserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
    		if (ww == null)
    			return null;
    		
    		IWorkbenchPage page = ww.getActivePage();
    		
    		// Checking if view is already open
    		IViewReference[] viewRefs = page.getViewReferences();
    		for (int i = 0; i < viewRefs.length; i++) {
				IViewReference reference = viewRefs[i];
				String id = reference.getId();
				if(id.equalsIgnoreCase(MainView.ID)){
					// Found, restoring the view
					IViewPart viewPart = reference.getView(true);
					page.activate(viewPart);
					return (MainView)viewPart;
				}
			}
    		
    		// View was not found, opening it up as a new view.
    		return (MainView)page.showView(MainView.ID);
    		
    	} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(openAnalysisWizardAction);
		manager.add(openReportWizardAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(openAnalysisWizardAction);
		manager.add(openReportWizardAction);
		manager.add(collapseAction);
	}

	private void hookDoubleClickAction() {
		tableviewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickReportAction.run();
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickIssuesAction.setCurrentSdkData(engine.getCurrentSdkData());
				doubleClickIssuesAction.run();
			}
		});
	}

	private void makeActions() {
		openAnalysisWizardAction = new Action() {
			public void run() {
				showWizard();
			}
		};
		openAnalysisWizardAction.setText("Analysis Wizard"); //$NON-NLS-1$
		openAnalysisWizardAction.setToolTipText(Messages.getString("MainView.ToolTipForAnalysis"));  //$NON-NLS-1$
		openAnalysisWizardAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.ANALYSIS_SMALL_ICON));
		
		openReportWizardAction = new Action() {
			public void run() {
				showReportFilterWizard(null);
			}
		};
		openReportWizardAction.setText(Messages.getString("MainView.ReportFilterWizard")); //$NON-NLS-1$
		openReportWizardAction.setToolTipText(Messages.getString("MainView.ToolTipForReportWizard")); //$NON-NLS-1$
		openReportWizardAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.FILTERATION_SMALL_ICON));
		
		openIssueAction = new Action(){
			public void run(){
				doubleClickIssuesAction.setCurrentSdkData(engine.getCurrentSdkData());
				doubleClickIssuesAction.run();
			}
		};
		openIssueAction.setText("Open");
		openIssueAction.setToolTipText("Opens the issue file from current SDK");

		showDiffAction = new Action(){
			public void run() {
				IStructuredSelection selectedElem = (IStructuredSelection)treeViewer.getSelection();
				String current_file = null;
				String baseline_file = null;
				if(selectedElem.getFirstElement() instanceof CompatibilityIssueFileData) {
					current_file = ((CompatibilityIssueFileData)selectedElem.getFirstElement()).getCurrentIssueFile();
					baseline_file = ((CompatibilityIssueFileData)selectedElem.getFirstElement()).getBaselinIssueFile();
				} 
				else if(selectedElem.getFirstElement() instanceof CompatibilityIssueData){
					CompatibilityIssueData issue = (CompatibilityIssueData)(selectedElem.getFirstElement());
					current_file = issue.getCurrentIssueFile();
					baseline_file = issue.getBaselineIssueFile();
				}
				if(current_file == null)
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Compatibility Analyser", "Current file does not exist.");
				else if(baseline_file == null)
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Compatibility Analyser", "Baseline file does not exist.");
				else if(current_file!=null && baseline_file!=null)
				{
					File left =new File(current_file);
					File right = new File(baseline_file);
					
					//Open the compare editor
					final CompareConfiguration cc = new CompareConfiguration();
					//left side title
					cc.setLeftLabel(left.getAbsolutePath());
					//right side title
					cc.setRightLabel(right.getAbsolutePath());
					CompareUI.openCompareEditor((new FileCompareInput(cc, left,right)));
				}
			}
		};
		showDiffAction.setText("Compare with baseline");
		showDiffAction.setToolTipText("Open the diff view of current and baseline files");

		doubleClickReportAction = new Action(){
			public void run(){
				IStructuredSelection selection = (IStructuredSelection) tableviewer.getSelection();
				
				if(selection.getFirstElement() instanceof String)
				{
					String selectedItem = selection.getFirstElement().toString();
					if(selectedItem.equals(Messages.getString("MainView.DoubleClickHereToStartAnalysis"))) //$NON-NLS-1$
					{
						showWizard();
						return;
					}
				}
				if(openInwebBrowserAction.isChecked())
					openInwebBrowserAction.run();
				else if(openInXmlEditorAction.isChecked())
					openInXmlEditorAction.run();
			}
		};
		
		doubleClickIssuesAction = new OpenSourceFileIssuesViewAction(treeViewer);
		collapseAction = new Action() {
					public void run() {
						treeViewer.collapseAll();
					}	
				};
		collapseAction.setText("Collapse all");
		collapseAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));		

		openInwebBrowserAction = new Action("Web Browser", IAction.AS_RADIO_BUTTON) {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) tableviewer.getSelection();
				if(isChecked() && selection.getFirstElement() instanceof ReportData)
				{
					IPath path = new Path(((ReportData)selection.getFirstElement()).getName());
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(path); 
					String editorId = null;

					if (fileStore.fetchInfo().exists())
					{ 
						IWorkbenchPage page=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
						try 
						{ 
							IEditorDescriptor[] editors = PlatformUI.getWorkbench().getEditorRegistry().getEditors("test.html");			

							for(IEditorDescriptor desc:editors)
							{
								if(desc.getLabel().equalsIgnoreCase("Web Browser")){
									editorId = desc.getId();
									break;
								}

							}

							if(editorId != null){
								//String id = editor.getId();
								page.openEditor(new FileStoreEditorInput(fileStore), editorId, true, IWorkbenchPage.MATCH_ID);
							}
							else
								MessageDialog.openError(Display.getDefault().getActiveShell(),"Compatibility Analyser", "Web Browser Editor does not exist.");
						} catch (PartInitException e) { 
							e.printStackTrace(); 
						} 
					}
					else
					{
						MessageDialog.openError(Display.getDefault().getActiveShell(),"Compatibility Analyser", "File does not exist.");
						return;
					}	
				}
			}	
		};
		try {
			openInwebBrowserAction.setImageDescriptor(IDE.getEditorDescriptor(".html").getImageDescriptor());
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}		
		openInwebBrowserAction.setChecked(true);

		clearAction=new Action(){
			  public void run() {
				  CompatibilityAnalyserEngine.clearReportFiles();
				  updateView();
			  }
		};
		clearAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.CLEAR_ALL));
		
		filterReportAction =new Action(){
			  public void run() {
				  IStructuredSelection selection = (IStructuredSelection) tableviewer.getSelection();
				  Object[] selectedObjs = selection.toArray();
				  String[] files=new String[selectedObjs.length];
				  for (int i = 0; i < selectedObjs.length; i++) {
					if(selectedObjs[i] instanceof ReportData)
					{
						ReportData report = (ReportData) selectedObjs[i];
						files[i]= report.getName();
					}
					else
						files[i] = selectedObjs[i].toString();
				}
				  showReportFilterWizard(files);
				  updateView();
			  }
		};
		filterReportAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.FILTERATION_SMALL_ICON));
		
		selectAllAction=new Action(){
			  public void run() {
				 tableviewer.getTable().selectAll();
			  }
		};
		
		openInXmlEditorAction=new Action("XML Editor", IAction.AS_RADIO_BUTTON){
			public void run(){
				IStructuredSelection selection = (IStructuredSelection) tableviewer.getSelection();
				if(isChecked()&& selection.getFirstElement() instanceof ReportData)
				{
					ReportData report = (ReportData)selection.getFirstElement();
					String selectedItem = report.getName();
					IPath path = new Path(selectedItem); 
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(path); 
				
					if (fileStore.fetchInfo().exists()) { 
						IWorkbenchPage page=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
						
						try { 
							String id=IDE.getEditorDescriptor(".txt").getId();
							page.openEditor(new FileStoreEditorInput(fileStore), id);
									
						} catch (PartInitException e) { 
							e.printStackTrace(); 
						} 
					}
					else
					{
						MessageDialog.openError(tableviewer.getControl().getShell(),"Compatibility Analyser",selectedItem + " file does not exist.");
						return;
					}
				}
			}
		};
		try {
			openInXmlEditorAction.setImageDescriptor(IDE.getEditorDescriptor(".xml").getImageDescriptor());
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private TableViewer createListViewTableViewer(SashForm parent) {
		TableViewer tblViewer = new TableViewer(parent, SWT.BORDER|SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd = new GridData(GridData.FILL_BOTH);
		tblViewer.getTable().setLayoutData(gd);
		tblViewer.getTable().setLinesVisible(true);
		tblViewer.getTable().setHeaderVisible(true);
		
		TableColumn report_column = new TableColumn(tblViewer.getTable(), SWT.NONE);
		report_column.setText("Report Files of Analysis");
		report_column.setWidth(550);
		
		TableColumn time_column = new TableColumn(tblViewer.getTable(), SWT.NONE);
		time_column.setText("Last Modified Time");
		time_column.setWidth(150);
		
		TableColumn status_column = new TableColumn(tblViewer.getTable(), SWT.NONE);
		status_column.setText("Report Filtered");
		status_column.setWidth(100);
		
		//Added logic to bind a sorter with column labeled as 'Last Modified Time'
		report_column.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event) {
				TableColumn sortedColumn = tableviewer.getTable().getSortColumn();
				TableColumn currentSelected = (TableColumn)event.widget;
				
				int dir = tableviewer.getTable().getSortDirection();
				
				if(sortedColumn == currentSelected){
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				}else
				{
					tableviewer.getTable().setSortColumn(currentSelected);
					dir = SWT.UP;
				}
				if(currentSelected == tableviewer.getTable().getColumn(1))
				{
					tableviewer.setSorter(new DateSorter(dir));
				}
				tableviewer.getTable().setSortDirection(dir);
			}
		}
		);
		
		//Added logic to bind column labeled with 'Report Filtered ?' 
		time_column.addListener(SWT.Selection, new Listener(){
			
			public void handleEvent(Event event) {
			
				TableColumn sortedColumn = tableviewer.getTable().getSortColumn();
				TableColumn currentSelected = (TableColumn)event.widget;
				
				int dir = tableviewer.getTable().getSortDirection();
			
				if(sortedColumn == currentSelected){
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				}else
				{
					tableviewer.getTable().setSortColumn(currentSelected);
					dir = SWT.UP;
				}
				if(currentSelected == tableviewer.getTable().getColumn(2))
				{
					tableviewer.setSorter(new Sorter(dir));
				}
				tableviewer.getTable().setSortDirection(dir);
			}
		
		}
		);
		
		//TableViewer tblViewer = new TableViewer(tbl.getTableInstance());
		//tbl.setHostingViewer(tblViewer);
		//tblViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		return tblViewer;
	}

	private TreeViewer createTreeViewer(SashForm sash)
	{
		TreeViewer tree_viewer = new TreeViewer(sash, SWT.BORDER|SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 450;
		tree_viewer.getTree().setLayoutData(gd);

		TreeColumn issue_column = new TreeColumn(tree_viewer.getTree(),SWT.NONE);
		issue_column.setText("Compatibility issues");
		issue_column.setWidth(300);
		issue_column.setResizable(true);
		
		TreeColumn severity_column = new TreeColumn(tree_viewer.getTree(),SWT.NONE);
		severity_column.setText("Severity");
		severity_column.setWidth(100);
		severity_column.setResizable(false);
		
		tree_viewer.getTree().setLinesVisible(true);
		tree_viewer.getTree().setHeaderVisible(true);
		
		return tree_viewer;
	}
	
	@Override
	public void setFocus() {
		listViewSashForm.setFocus();

	}
	/**
	 * Opens the analysis wizard.
	 *
	 */
	public void showWizard()
	{
		//First Check if web server core tools are selected in preferences
		//If yes, check whether core tools are updated or not.
		IPreferenceStore prefStore = CompatibilityAnalyserPlugin.getCompatabilityAnalyserPrefStore();
		String urlInPref = prefStore.getString(CompatibilityAnalyserPreferencesConstants.CORETOOLS_URL);
		boolean webToolsSelected = prefStore.getBoolean(CompatibilityAnalyserPreferencesConstants.WEB_TOOLS);
		if(webToolsSelected && CompatibilityAnalyserEngine.isDownloadAndExtractionNeeded(urlInPref))
		{
			//If not, ask for the confirmation
			boolean okPressed = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Compatibility Analyser", "Core components from Web server are in use.\nPress Ok to download them, or Cancel and change the settings in preferences page.");
			if(okPressed)
			{
				String status = CompatibilityAnalyserUtils.initiateDownloadingCoreTools();
				if(status!=null)
				{
					status = status + "\n\nNote: Using the different core tools may solve your problem.";
					MessageDialog.openError(tableviewer.getControl().getShell(),"Compatibility Analyser",status);
					return;
				}
			}
			else
			{
				return;
			}
		}
		
		//If the opened from the view, set false 
		Runnable showWizardRunnable = new Runnable(){
			public void run(){
				WizardDialog wizDialog;
				CompatibilityAnalyserEngine engine = new CompatibilityAnalyserEngine();
				AnalysisWizard wiz = new AnalysisWizard(engine);
				wiz.setNeedsProgressMonitor(true);
				wiz.setNeedsProgressMonitor(true);
				wizDialog = new WizardDialog(getViewSite().getShell(), wiz);
				wizDialog.create();		
				wizDialog.addPageChangedListener(wiz);
				wizDialog.getShell().setSize(wizDialog.getShell().getSize().x, wizDialog.getShell().getSize().y+70);
				wizDialog.open();
			}
		};

		Display.getDefault().asyncExec(showWizardRunnable); 
	}
	/**
	 * Opens the report filter wizard.
	 *
	 */
	public void showReportFilterWizard(String[] files)
	{
	    selectedReportfilesInViews = files;
		Runnable showWizardRunnable = new Runnable(){
		public void run(){
  		  WizardDialog wizDialog;
		  ReportFilterWizard wiz = new ReportFilterWizard(selectedReportfilesInViews);
       	  wizDialog = new WizardDialog(getViewSite().getShell(), wiz);
		  wizDialog.create();		
		  wizDialog.getShell().setSize(550, 620);
		  wizDialog.addPageChangedListener(wiz);
		  wizDialog.open();
		 }
	   };
	   
	  Display.getDefault().asyncExec(showWizardRunnable); 
	}
	
	/**
	 * Start the header analyser and ordinal analyser.
	 * @param engine
	 */
	public void startAnalysis(CompatibilityAnalyserEngine engine)
	{
		this.engine = engine;
		
		showConsoleView();
		
		if(engine.isLibraryAnalysisChecked())
		{
			LibraryAnalyserEngine libsEngine = new LibraryAnalyserEngine(engine, this);
			libsEngine.startOC();
		}
		else
		{
			startHA(engine);
		}
	}
	/**
	 * Start the filteration.
	 * @param data
	 */
	public void startFilteration(ReportWizardData data)
	{
		this.data=data;
		showConsoleView();
		reportEngine = new ReportFilterEngine(data,this);
		reportEngine.startAnalysis();
		
	}
	/**
	 * Makes visible the console view.
	 *
	 */
	private void showConsoleView(){
		
		 try {
		    	
		    	Runnable showConsoleRunnable = new Runnable(){
					public void run(){
						try{
						IWorkbench page = CompatibilityAnalyserPlugin.getDefault().getWorkbench();
				    	IWorkbenchWindow wbwindow = page.getActiveWorkbenchWindow();
				    	IWorkbenchPage activePage = wbwindow.getActivePage();
						activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW);
						}catch(Exception e){
							System.out.println(e.getMessage());
						}
					}
				};
				
				// Showing console view in separate thread
				Display.getDefault().asyncExec(showConsoleRunnable);      
		    	
			} catch (Exception e) {
				System.out.println(e.getMessage()); 
			} 
	}
	
	public void startHA(CompatibilityAnalyserEngine engine)
	{
		showConsoleView();
		HeaderAnalyserEngine headersEngine = new HeaderAnalyserEngine(engine, this);
		headersEngine.startHA();
	}
	
	public void HandleFeedback(String title,String message)
	{
		if(message != "") //$NON-NLS-1$
		{
			feedbackMessage = message;
			feedbackMessageTitle = title;
			showErrorMessage();
		}
		setMainViewVisible();
	}
	
	/**
	 * Shows an error message dialog.
	 *
	 */
	private void showErrorMessage() {
		Runnable showErrorMessageRunnable = new Runnable(){
			public void run(){
				MessageDialog.openError(tableviewer.getControl().getShell(),feedbackMessageTitle,feedbackMessage);
				feedbackMessage = "";
			}
		};
		
		//Showing error message in separate thread
		Display.getDefault().asyncExec(showErrorMessageRunnable);        
		
	}
	
	/**
	 * Shows Compatibility Analyser view visible or brings front
	 */
	private void setMainViewVisible() {
		Runnable decodingFinishedRunnable = new Runnable(){
			public void run(){
				updateView();
			}
		};
		
		//Showing main view in separate thread
		Display.getDefault().asyncExec(decodingFinishedRunnable);        		
	}
	/**
	 * The main will be updated and do refresh 
	 *
	 */
	private void updateView() {
		try {
			getViewSite().getPage().showView(MainView.ID);
			tableviewer.refresh();
			tableviewer.getTable().deselectAll();
			tableviewer.getTable().notifyListeners(SWT.Selection, new Event());
		} catch (Exception e) {		
			System.out.println(e.getMessage());
		}
	}
	private void hookContextMenu() {
		//Menu for the report view
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MainView.this.fillContextMenuForFirstViewer(manager);		
			}
        });
	    Menu menu = menuMgr.createContextMenu(tableviewer.getControl());
	    tableviewer.getControl().setMenu(menu);	 
	    getSite().registerContextMenu(menuMgr, tableviewer);
	    
	    //Menu for the tree view
	    MenuManager menuMgr2 = new MenuManager("#PopupMenu");
	    menuMgr2.setRemoveAllWhenShown(true);
	    menuMgr2.addMenuListener(new IMenuListener() {
	    	public void menuAboutToShow(IMenuManager manager) {
	    		MainView.this.fillContextMenuForTreeViewer(manager);
	    	}
	    });
	    Menu menu2 = menuMgr2.createContextMenu(treeViewer.getControl());
	    treeViewer.getControl().setMenu(menu2);
	    getSite().registerContextMenu(menuMgr2, treeViewer);
	}
	
	protected void fillContextMenuForFirstViewer(IMenuManager manager) {
		try{
		IStructuredSelection selection = (IStructuredSelection) tableviewer.getSelection();
		if(selection != null && selection.size() > 0){
				String selectedItem = selection.getFirstElement().toString();
				if(!selectedItem.startsWith(Messages.getString("MainView.DoubleClickHereToStartAnalysis"))) //$NON-NLS-1$
				{
					//Creating sub menu "Open with"
					MenuManager subMenuMgr = new MenuManager("Open With", "Open with");
				    subMenuMgr.add(openInwebBrowserAction);
					subMenuMgr.add(openInXmlEditorAction);
					manager.add(subMenuMgr);
				    
					doubleClickReportAction.setText("Open");
					doubleClickReportAction.setImageDescriptor(null);
					manager.add(doubleClickReportAction);
					manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
					manager.add(new Separator());
					filterReportAction.setText(Messages.getString("MainView.Filter..")); 
					manager.add(filterReportAction);
					manager.add(new Separator());
					selectAllAction.setText(Messages.getString("MainView.SelectAll")); 
					manager.add(selectAllAction);
					clearAction.setText(Messages.getString("MainView.ClearAll")); 
					manager.add(clearAction);	
					
				}
				else
				{
					doubleClickReportAction.setText(Messages.getString("MainView.OpenWizard")); //$NON-NLS-1$
					manager.add(doubleClickReportAction);
					doubleClickReportAction.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.ANALYSIS_SMALL_ICON));
				}
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				
		}
		}catch(Exception e){
			System.out.println("===>"+e.getMessage());
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	protected void fillContextMenuForTreeViewer(IMenuManager manager)
	{
		if(treeViewer.getInput() instanceof BCAndSCIssues)
		{
			BCAndSCIssues input = (BCAndSCIssues)treeViewer.getInput();
			
			Object selectedElem = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
			
			boolean enableState = false;
			if((input.getType() == FILE_TYPE.LIBRARY  && selectedElem instanceof CompatibilityIssueData) ||
				(input.getType() == FILE_TYPE.HEADER && (selectedElem instanceof CompatibilityIssueFileData
						|| selectedElem instanceof CompatibilityIssueData)))	
				enableState = true;
			
			if(enableState)
			{	
				manager.add(openIssueAction);
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(showDiffAction);
			}
		}
	}
	
	public void setHelp()
	{
		PlatformUI.getWorkbench().getHelpSystem().setHelp(doubleClickReportAction,
				HelpContextIDs.MAIN_VIEW);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(openReportWizardAction,
				HelpContextIDs.MAIN_VIEW);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(openAnalysisWizardAction,
				HelpContextIDs.MAIN_VIEW);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(listViewSashForm,
				HelpContextIDs.MAIN_VIEW);
	}

	/**
	 * The method displays a message asking whether to restart the analysis with default core
	 * tools set. If yes, it modifies the corresponding fields to invoke analysis with
	 * default core tools set.
	 */
	public void reStartAnalysisUsingSameData(String title, String message) 
	{
		feedbackMessageTitle=title;
		feedbackMessage=message;
		Runnable showErrorMessageRunnable = new Runnable(){
			public void run(){
				if(MessageDialog.openQuestion(tableviewer.getControl().getShell(), "Confirmation", feedbackMessage))
				{	
					String defaultToolsPath=CompatibilityAnalyserPlugin.getDefaltCoretoolsPath();
					if(defaultToolsPath==null)
						MessageDialog.openError(tableviewer.getControl().getShell(),feedbackMessageTitle,Messages.getString("HeaderAnalyserEngine.ToolsPluginDoesNotContainCoreComponents"));
					else
					{
						engine.getCurrentSdkData().useDefaultCoreTools = true;
						engine.getCurrentSdkData().useLocalCoreTools = false;
						engine.getCurrentSdkData().useSdkCoreTools = false;
						engine.getCurrentSdkData().useWebServerCoreTools = false;							
						engine.getCurrentSdkData().coreToolsPath = defaultToolsPath;
						startAnalysis(engine);				
					}
				}
				feedbackMessage="";
			}
		};
		Display.getDefault().asyncExec(showErrorMessageRunnable);
	}

	/**
	 * The method displays a message asking whether to restart the filteration with default core
	 * tools set. If yes, it modifies the corresponding fields to invoke filteration with
	 * default core tools set.
	 */
	public void reStartFilterationUsingSameData(String title, String message) 
	{
		feedbackMessageTitle=title;
		feedbackMessage=message;
		Runnable showErrorMessageRunnable = new Runnable(){
			public void run(){
				if(MessageDialog.openQuestion(tableviewer.getControl().getShell(), "Confirmation", feedbackMessage))
				{	
					String defaultToolsPath=CompatibilityAnalyserPlugin.getDefaltCoretoolsPath();
					if(defaultToolsPath==null)
						MessageDialog.openError(tableviewer.getControl().getShell(),feedbackMessageTitle,Messages.getString("HeaderAnalyserEngine.ToolsPluginDoesNotContainCoreComponents"));
					else
					{
						data.useDefaultCoreTools=true;
						data.useLocalCoreTools=false;
						data.useSdkCoreTools=false;
						data.useWebServerCoreTools=false;
						data.bcFilterPath = defaultToolsPath;
						startFilteration(data);				
					}
				}
				feedbackMessage="";
			}
		};
		Display.getDefault().asyncExec(showErrorMessageRunnable);
	}

	public void setCompatibilityIssues(ArrayList<CompatibilityIssueFileData> issues, FILE_TYPE type) {
		
		if(type == FILE_TYPE.NONE){
			treeViewer.getTree().removeAll();
			treeViewer.getTree().getColumn(0).setText("Compatibility issues");
			return;
		}
		
		if(type == FILE_TYPE.LIBRARY){
			treeViewer.getTree().getColumn(0).setText("Compatibility issues in Libraries");
		}
		else if(type == FILE_TYPE.HEADER){
			treeViewer.getTree().getColumn(0).setText("Compatibility issues in Headers");
		}
		
		if(issues != null)
		{
			BCAndSCIssues separatedIssues = separateBCAndSCIssues(issues, type);
			treeViewer.setInput(separatedIssues);
		}
		else
		{
			treeViewer.getTree().removeAll();
			TreeItem item = new TreeItem(treeViewer.getTree(), SWT.NONE);
			item.setText("No issues");
		}
	}

	/**
	 * This will separate the BC and SC issues.
	 * @param issues all
	 * @return
	 */
	private BCAndSCIssues separateBCAndSCIssues(ArrayList<CompatibilityIssueFileData> issueFiles, FILE_TYPE type) {
		BCAndSCIssues bcAndSCIssues = new BCAndSCIssues();
		bcAndSCIssues.setType(type);
		
		ArrayList<CompatibilityIssueFileData> bc_data = new ArrayList<CompatibilityIssueFileData>();
		ArrayList<CompatibilityIssueFileData> sc_data = new ArrayList<CompatibilityIssueFileData>();
		
		for(CompatibilityIssueFileData data:issueFiles)
		{
			ArrayList<CompatibilityIssueData> bc_issues = new ArrayList<CompatibilityIssueData>();
			ArrayList<CompatibilityIssueData> sc_issues = new ArrayList<CompatibilityIssueData>();
			
			for(CompatibilityIssueData issue : data.getIssues())
			{
				if(issue.getIssueCategory() == ISSUE_CATEGORY.BC_BREAK) {
					issue.setRoot(ISSUE_CATEGORY.BC_BREAK);
					bc_issues.add(issue);
				}
				else if(issue.getIssueCategory() == ISSUE_CATEGORY.SC_BREAK) {
					issue.setRoot(ISSUE_CATEGORY.SC_BREAK);
					sc_issues.add(issue);
				}
				else if(issue.getIssueCategory() == ISSUE_CATEGORY.BOTH) {
					CompatibilityIssueData bcIssue;
					CompatibilityIssueData scIssue;
					
					if(issue instanceof LibraryIssueData){
						bcIssue = new LibraryIssueData((LibraryIssueData)issue);
						scIssue = new LibraryIssueData((LibraryIssueData)issue);
					}
					else{
						bcIssue = new CompatibilityIssueData(issue);
						scIssue = new CompatibilityIssueData(issue);
					}
					bcIssue.setRoot(ISSUE_CATEGORY.BC_BREAK);
					bc_issues.add(bcIssue);
					
					scIssue.setRoot(ISSUE_CATEGORY.SC_BREAK);
					sc_issues.add(scIssue);
				}
			}
			
			if(bc_issues.size()>0)
			{
				CompatibilityIssueFileData bc = new CompatibilityIssueFileData(data);
				bc.setIssues(bc_issues);
				bc.setRoot(ISSUE_CATEGORY.BC_BREAK);
				bc_data.add(bc);
			}
			if(sc_issues.size()>0)
			{
				CompatibilityIssueFileData sc = new CompatibilityIssueFileData(data);
				sc.setIssues(sc_issues);
				sc.setRoot(ISSUE_CATEGORY.SC_BREAK);
				sc_data.add(sc);
			}
		}
		bcAndSCIssues.setBCData(bc_data);
		bcAndSCIssues.setSCData(sc_data);
		
		return bcAndSCIssues;
	}	
		
}
