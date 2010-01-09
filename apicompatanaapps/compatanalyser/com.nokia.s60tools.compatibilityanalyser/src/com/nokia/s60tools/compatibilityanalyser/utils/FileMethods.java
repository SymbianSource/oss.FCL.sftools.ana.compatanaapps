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
package com.nokia.s60tools.compatibilityanalyser.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;

import com.nokia.s60tools.compatibilityanalyser.resources.Messages;

/**
 * Utility class
 */
public class FileMethods {
	
	/**
	 * Appends the path seperator at the end of the given path.
	 * @param s path of ant file or directory
	 * @return path with seperator at the end
	 */
	static public String appendPathSeparator(String s)
	{
		String path = null;
		
		if(s != null)
		{
			if(s.endsWith(File.separator))
				path = s;
			else
				path = new String(s+File.separator);
		}
		
		return path;
	}
	
	/**
	 * Copies the given source file to target file.
	 * @param file source file
	 * @param destinationFile
	 * @param overwrite if set to true, target file will be deleted before copy.
	 * @return
	 */
	public static boolean copyFile(File file, File destinationFile, boolean overwrite) {
		
		if(file.getAbsolutePath().equalsIgnoreCase(destinationFile.getAbsolutePath()))
			return true;
		
		if (destinationFile.exists()) {
			if (overwrite) {
				destinationFile.delete();
			} else {
				return false;
			}
		}
		else 
		{
			if(!destinationFile.getParentFile().exists())
				createFolder(destinationFile.getParent());
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try {
	        fis = new FileInputStream(file);
	        fos = new FileOutputStream(destinationFile);
	        byte[] buf = new byte[1024];
	        int i = 0;
	        while((i=fis.read(buf))!=-1) {
	        	fos.write(buf, 0, i);
	        }
	        fis.close();
	        fos.close();
	    } catch (Exception e) {
	    	try {
	    		if (fis != null) fis.close();
	    	} catch (Exception E) {E.printStackTrace();}
	    	try {
	    		if (fos != null) fos.close();
	    	} catch (Exception E) {E.printStackTrace();}
	        return false;
	    }		
		return true;
	}
	
	/**
	 * Creates folder of given path
	 * @param path folder name including the path
	 * @return
	 */
	public static boolean createFolder(String path) {
		try {
			File file = new File(path); 
			return file.mkdir();
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Deletes given folder's contents and folder itself.
	 * @param folder Folder to be deleted
	 */
	public static boolean deleteFolder(String folder) {
	  try
	  {
		File f = new File(folder);
		if (f.exists() && f.isDirectory())
		{
			if(deleteAllFiles(f))
				return f.delete();
			
			else
				return false;
		}
		else
			return f.delete();
	  }catch(Exception e){
		  return false;
	  }
	  
	  
	}
	
	/**
	 * Deletes given file
	 * @param path path to file to be deleted
	 */
	public static void deleteFile(String path) {
		File f = new File(path);
		if (f.isFile())
			f.delete();
	}
	
	/**
	 * Deletes all files from given directory and directory itself
	 * @param dir Directory to be deleted
	 * @return true if successful, false if not
	 */
	private static boolean deleteAllFiles(File dir) {
		if(!dir.exists()) {
			return true;
		}
		
		boolean res = true;
		if(dir.isDirectory()) {
			File[] files = dir.listFiles();
			for(int i = 0; i < files.length; i++) {
				res &= deleteAllFiles(files[i]);
			}
			res = dir.delete();
			//Delete dir itself
		} else {
			res = dir.delete();
		}
		return res;
	}
	
	/**
	 * Copy the bbcresult.xsl file to the parent folder of given report(if not exists).
	 * @param reportPath
	 */
	public static void copyBBCResultsFileIfNotExists(String reportPath,String corePath)
	{
		if(corePath != null)
		{
			File xslFile = new File(corePath +File.separator+Messages.getString("FileMethods.reports")+File.separator+Messages.getString("FileMethods.BBCResults.xsl")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			File dest=new File(reportPath+File.separator+Messages.getString("FileMethods.BBCResults.xsl")); //$NON-NLS-1$
			if(xslFile.exists())
				copyFile(xslFile, dest, true);
		}
		
	}
		
	/**
	 * This method checks if the given String contain any forward slashes.
	 * If it contains forward slashes, those will be replaced with backward slashes
	 */
	public static String convertForwardToBackwardSlashes(String path)
	{
		if(path == null)
			return null;
		
		if(path.contains("/")){ //$NON-NLS-1$
			path = path.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(path.contains("\\\\")){ //$NON-NLS-1$
			path = path.replace("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		}
			
		return path;
		
	}
	/**
	 * Converts string arry to string by combining all the paths with path separator ';'.
	 * @param paths an array of strings
	 * @return returns "paths[0];paths[1];paths[2]"
	 */
	public static String convertToOneString(String[] paths)
	{
		String finalPath="";
		
		if(paths==null||paths.length==0)
		{
			return finalPath;
		}
		else if(paths.length==1)
		{
			return paths[0];
		}
		else
		{
			finalPath=paths[0];
			for (int i = 1; i < paths.length; i++) {
				finalPath=finalPath+";"+paths[i];
			}
			return finalPath;
		}
		
	}
	/*
	 * This Method is used to get the current Date & time stamp.
	 */
	public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH_mm_ss");
        Date date = new Date();
        return dateFormat.format(date);
    } 
	
	/** 
	 * The method calculates offset position for the starting character 
	 * from given line number in the given file.
	 * @param filePath 
	 * @param lineNumber
	 * @return
	 */
	public  static long getOffsetPosition(String filePath, int lineNumber)
	{
		long offsetPos = -1;
		try
		{
			File file = new File(filePath);
			RandomAccessFile randomAccFile = new RandomAccessFile(file, "r");
				
			for(int i =1; i<lineNumber; i++)
				randomAccFile.readLine();
 
			offsetPos = randomAccFile.getFilePointer();
			return offsetPos;
		
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return offsetPos;
	}
	
	/**
	 * This method opens the given file, in the default editor, associated for 
	 * that file extension and highlights the given line number. 
	 * @param fileName path of the file to be opened
	 * @param offsetPos position to be highlighted.
	 */
	public static void openFileAndHighLightGivenLine(String fileName, final long offsetPos)
	{
		if(fileName == null)
			return;
		
		try
		{
			final File file = new File(fileName);
			
			final String editorId = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName()).getId();
		
			String issueFile = file.getAbsolutePath();								
			issueFile = issueFile.replace("\\", "/");
						
			final URI uri = new URI("file://" + issueFile);
			
			if(!file.exists())
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Compatibility Analyser", file.getAbsolutePath() + " does not exist.");
			else{
					
				final IWorkspaceRunnable runOpen = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						// do the actual work in here

						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						try {
							IEditorPart editorPart = IDE.openEditor(page, uri, editorId, true);
							
							System.out.println("Opening the File " + file + " in " + editorId);
							System.out.println(" " + editorPart.getClass());
							
							if(editorPart != null && editorPart instanceof TextEditor)
							{
								((TextEditor)editorPart).resetHighlightRange();
							}
							if(offsetPos > 0)
							{
								if(editorPart != null && editorPart instanceof TextEditor)
								{
									TextEditor editor = ((TextEditor)(editorPart));
														
									editor.setHighlightRange((int)offsetPos, 0, true);
								}
							}
						
						} catch (PartInitException e) {
							MessageDialog.openError(Display.getDefault().getActiveShell(), "Compatibility Analyser", "Unable to open the issue file in editor area.");
							e.printStackTrace();
						} 

					}
				};		
				ResourcesPlugin.getWorkspace().run(runOpen, null, IWorkspace.AVOID_UPDATE, null);
			}		
				
		}catch(URISyntaxException e){
			e.printStackTrace();
		} catch(CoreException e){
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Compatibility Analyser", "Unable to open the issue file in editor area.");
			e.printStackTrace();
		}
	}
	
}
