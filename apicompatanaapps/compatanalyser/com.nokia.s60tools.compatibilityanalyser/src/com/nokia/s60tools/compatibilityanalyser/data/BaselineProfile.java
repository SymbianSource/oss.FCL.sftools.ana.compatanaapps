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
* Description: Serializable class, which can store data of a baseline profile
*
*/
package com.nokia.s60tools.compatibilityanalyser.data;
import java.io.Serializable;

/**
 * This is a serializable class, which can store data of a baseline profile. 
 */
public class BaselineProfile implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String profileName="";
	private String sdkName="";
	private String s60version="";
	private String sdkEpocRoot="";
	private boolean radio_default_hdr=true;
	private boolean radio_dir_hdr=false;
	private String [] hdr_dir_path;
	private String[] systemInc_dirs;
	private String[] build_config = null;
	
	private boolean radio_default_build_target = true;
	private boolean radio_build_target=false;
	private boolean radio_dir_libs=false;
	private String [] lib_dir;
	private String [] dll_dir;
	private String [] forced_headers;
	
	//Variables useful for predefined Profiles only.
	private boolean isPredefined = false;
	private String sdkUrl = null;
	private boolean isUpdated = false;
	
	public BaselineProfile() {
	}
	
	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String[] getBuild_config() {
		return build_config;
	}

	public void setBuild_config(String[] build_config) {
		this.build_config = build_config;
	}

	public String[] getForced_headers() {
		return forced_headers;
	}

	public void setForced_headers(String[] forced_headers) {
		this.forced_headers = forced_headers;
	}

	public String[] getHdr_dir_path() {
		return hdr_dir_path;
	}

	public void setHdr_dir_path(String[] hdr_dir_path) {
		this.hdr_dir_path = hdr_dir_path;
	}

	public String [] getLib_dir() {
		return lib_dir;
	}

	public void setLib_dir(String[] lib_dir) {
		this.lib_dir = lib_dir;
	}

	public boolean isRadio_default_hdr() {
		return radio_default_hdr;
	}

	public void setRadio_default_hdr(boolean radio_default_hdr) {
		this.radio_default_hdr = radio_default_hdr;
	}

	public boolean isRadio_build_target() {
		return radio_build_target;
	}

	public void setRadio_build_target(boolean radio_default_libs) {
		this.radio_build_target = radio_default_libs;
	}

	public boolean isRadio_dir_hdr() {
		return radio_dir_hdr;
	}

	public void setRadio_dir_hdr(boolean radio_dir_hdr) {
		this.radio_dir_hdr = radio_dir_hdr;
	}

	public boolean isRadio_dir_libs() {
		return radio_dir_libs;
	}

	public void setRadio_dir_libs(boolean radio_dir_libs) {
		this.radio_dir_libs = radio_dir_libs;
	}

	public String getS60version() {
		return s60version;
	}

	public void setS60version(String s60version) {
		this.s60version = s60version;
	}

	public String getSdkEpocRoot() {
		return sdkEpocRoot;
	}

	public void setSdkEpocRoot(String sdkEpocRoot) {
		this.sdkEpocRoot = sdkEpocRoot;
	}

	public String getSdkName() {
		return sdkName;
	}

	public void setSdkName(String sdkName) {
		this.sdkName = sdkName;
	}

	public String[] getSystemInc_dirs() {
		return systemInc_dirs;
	}

	public void setSystemInc_dirs(String[] systemInc_dirs) {
		this.systemInc_dirs = systemInc_dirs;
	}

	public String[] getDll_dir() {
		return dll_dir;
	}

	public void setDll_dir(String[] dll_dir) {
		this.dll_dir = dll_dir;
	}

	public boolean isRadio_default_build_target() {
		return radio_default_build_target;
	}

	public void setRadio_default_build_target(boolean radio_default_build_target) {
		this.radio_default_build_target = radio_default_build_target;
	}
	
	public String getSdkUrl() {
		if(isPredefined())
			return sdkUrl;
		return null;
	}

	public void setSdkUrl(String sdkUrl) {
		if(isPredefined())
			this.sdkUrl = sdkUrl;
	}
	
	public boolean isPredefined() {
		return isPredefined;
	}

	public void setPredefined(boolean isPredefined) {
		this.isPredefined = isPredefined;
	}
	
	public boolean isUpdated()
	{
		if(isPredefined())
			return isUpdated;
		return true;
	}
	
	public void setUpdated(boolean flag)
	{
		if(isPredefined())
			isUpdated = flag;
	}
}
