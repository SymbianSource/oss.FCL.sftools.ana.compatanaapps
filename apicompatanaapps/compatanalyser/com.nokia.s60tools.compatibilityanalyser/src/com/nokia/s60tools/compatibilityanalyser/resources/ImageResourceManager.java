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

package com.nokia.s60tools.compatibilityanalyser.resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Class for handling CompatibilityAnalyser icons.
 *
 */
public class ImageResourceManager {

	public static void loadImages(String imagesPath){
		
    	Display disp = Display.getCurrent();
    	
    	if(disp!= null)
    	{
    		ImageRegistry imgReg = JFaceResources.getImageRegistry();
    	
    	//
    	// Storing images to image registry
    	//
    	// There are both images copyrighted by Nokia and
    	
    		if(imagesPath != null && !imagesPath.equals(""))
    		{
    			Image img = new Image( disp, imagesPath + "\\compatibility_analyser_main_55x45.png" );        	 //$NON-NLS-1$
    			imgReg.put( ImageKeys.ANALYSER_WIZARD_BANNER, img );
        
    			img = new Image( disp, imagesPath + "\\compatibility_analyser_main_16.png"); //$NON-NLS-1$
    			imgReg.put( ImageKeys.ANALYSIS_SMALL_ICON, img);
        
    			img = new Image( disp, imagesPath + "\\compatibility_analyser_main_55x45.png"); //$NON-NLS-1$
    			imgReg.put( ImageKeys.FILTER_WIZARD_BANNER, img);
        
    			img = new Image( disp, imagesPath + "\\filter.png"); //$NON-NLS-1$
    			imgReg.put( ImageKeys.FILTERATION_SMALL_ICON, img);
        
    			img = new Image( disp, imagesPath + "\\baseline_configuration.png"); //$NON-NLS-1$
    			imgReg.put( ImageKeys.BASELINE_EDITOR_ICON, img);
        
    			img = new Image( disp, imagesPath + "\\known_issues_configuration.png"); //$NON-NLS-1$
    			imgReg.put( ImageKeys.KNOWNISSUES_DIALOG_ICON, img);
        
    			img = new Image( disp, imagesPath + "\\delete_edit.gif"); //$NON-NLS-1$
    			imgReg.put( ImageKeys.CLEAR_ALL, img);
    		}
        
    	}
       
        
	}
	
	public static ImageDescriptor getImageDescriptor( String key ){
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();
    	return  imgReg.getDescriptor( key );		
	}	

	public static Image getImage( String key ){
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();    	
    	return  imgReg.get(key);		
	}	
}
