package com.nokia.s60tools.compatibilityanalyser.test;

import java.io.File;
import java.util.ArrayList;


import com.nokia.s60tools.compatibilityanalyser.data.BaselineProfile;
import com.nokia.s60tools.compatibilityanalyser.data.BaselineSdkData;
import com.nokia.s60tools.compatibilityanalyser.data.ProductSdkData;
import com.nokia.s60tools.compatibilityanalyser.shared.CompatibilityAnalyserMethods;


import junit.framework.TestCase;

public class CompatibilityAnalysisTest extends TestCase {
	
	public String TESTDATA = System.getProperty("TESTDATALOCATION");
	CompatibilityAnalyserMethods methods = new CompatibilityAnalyserMethods();
	public CompatibilityAnalysisTest(){
		
	}
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetPlatformListFromSdk()
	{
		String [] plats = methods.getPlatformsList(Activator.SDK_NAME);
		assertNotNull(plats); 
	}
	 
	public void testGetLibsPathFromPlatform()
	{
		String libsPath = methods.getLibsPathFromPlatform(Activator.SDK_NAME, Activator.TARTGET_TYPE);
		assertNotNull(libsPath);
	}
	
	public void testGetWorkspacePath()
	{
		String workSpace = methods.getWorkSpacePath();
		assertNotNull(workSpace);
	}
	
	public void testBaselineProfileReadingWithDefaults()
	{
		BaselineProfile profile = new BaselineProfile();
		profile.setProfileName(Activator.PROFILE_NAME);
		profile.setSdkName(Activator.SDK_NAME);
		profile.setS60version(Activator.SDK_VERSION);
		profile.setSdkEpocRoot(Activator.EPOCROOT);
		profile.setRadio_default_hdr(true);
		profile.setBuild_config(new String[]{Activator.TARTGET_TYPE});
		profile.setRadio_build_target(true);
		profile.setRadio_default_build_target(false);
		profile.setLib_dir(new String[]{methods.getLibsPathFromPlatform(Activator.SDK_NAME, profile.getBuild_config()[0])});
	
		methods.saveBaselineProfile(profile);
		BaselineSdkData baselineData = methods.getBaselineProfileData(Activator.PROFILE_NAME);
				
		assertNotNull(baselineData);
	}
	
	public void testBaselineProfileReading()
	{
		BaselineProfile profile = new BaselineProfile();
		
		profile.setProfileName(Activator.PROFILE_NAME);
		profile.setSdkName(Activator.SDK_NAME);
		profile.setS60version(Activator.SDK_VERSION);
		profile.setSdkEpocRoot(Activator.EPOCROOT);
		profile.setRadio_default_hdr(false);
		profile.setRadio_dir_hdr(true);
		profile.setHdr_dir_path(new String [] {Activator.EPOCROOT+ "\\Epoc32\\include\\mmf"});			
		profile.setSystemInc_dirs(new String [] {Activator.EPOCROOT+"\\Epoc32\\include\\libc", "C:\\Symbian\\9.2\\S60_3rd_FP1_3\\Epoc32\\include\\oem"});
		profile.setBuild_config(new String [] {Activator.TARTGET_TYPE});
		profile.setRadio_default_build_target(false);
		profile.setRadio_build_target(true);
		profile.setLib_dir(new String[]{Activator.EPOCROOT + "\\Epoc32\\release\\armv5\\lib"});
		methods.saveBaselineProfile(profile);
		BaselineSdkData baselineData = methods.getBaselineProfileData(Activator.PROFILE_NAME);
		assertNotNull(baselineData);
	}


	public void testHADataValidity()
	{
		ProductSdkData currentData = new ProductSdkData();
		currentData.productSdkName = Activator.SDK_VERSION;
		currentData.productSdkVersion = Activator.SDK_VERSION;
		currentData.epocRoot = Activator.EPOCROOT;
		currentData.useLocalCoreTools = true;
		currentData.coreToolsPath = TESTDATA + File.separator + "BCTools";
		currentData.currentHeaderDir = new String [] {Activator.EPOCROOT+"\\epoc32\\include"};
		currentData.HeaderFilesList = new String [1];
		currentData.HeaderFilesList[0] = "agentdialog.h";
		currentData.allTypes = true;
		currentData.useRecursive = false;
		currentData.usePlatformData = true;
		currentData.currentIncludes = new String[3];
		currentData.currentIncludes[0] = Activator.EPOCROOT + "\\epoc32\\include\\libc";
		currentData.currentIncludes[1] = Activator.EPOCROOT + "\\epoc32\\include\\ecom";
		currentData.currentIncludes[2] = Activator.EPOCROOT + "\\epoc32\\include\\oem";
		currentData.reportName = "Test_Junit.xml";
		currentData.reportPath = currentData.coreToolsPath + "\\reports";
		currentData.config_folder = TESTDATA;
		BaselineSdkData baselineData = methods.getBaselineProfileData(Activator.PROFILE_NAME);
		String status = methods.checkHAInput(currentData, baselineData);
		System.out.println(status);
		assertNull(status);
		File f = new File(currentData.config_folder);
		
		
	}

	public void testHADataValidityWithCoreToolsFromWebServer()
	{
		ProductSdkData currentData = new ProductSdkData();
		currentData.productSdkName = Activator.SDK_NAME;
		currentData.productSdkVersion = Activator.SDK_VERSION;
		currentData.epocRoot = Activator.EPOCROOT;
		currentData.useWebServerCoreTools = true;
		currentData.urlPathofCoreTools = "http://s60tools.ntc.nokia.com/ca/coretools/";
		currentData.currentHeaderDir = new String [] {Activator.EPOCROOT + "\\epoc32\\include\\mmf"};
		currentData.useRecursive = true;
		currentData.usePlatformData = true;
		currentData.replaceSet.add("agentdialog222.h:test.h");
		currentData.replaceSet.add("mmcaf.h:new.h");
		currentData.reportName = "Test_Junit.xml";
		currentData.config_folder = TESTDATA;
		currentData.reportPath = TESTDATA + "BCTools\\reports";
		
		BaselineSdkData baselineData = methods.getBaselineProfileData(Activator.PROFILE_NAME);
				
		String status = methods.checkHAInput(currentData, baselineData);
		System.out.println(status);
		assertNull("Error in HA Input", status);
	}
	
	public void testHAArgsPreparation()
	{
		ProductSdkData currentData = new ProductSdkData();
		currentData.allTypes = true;
		currentData.coreToolsPath = TESTDATA + File.separator + "BCTools";
		currentData.HeaderFilesList = new String[1];
		currentData.HeaderFilesList[0] = "agentdialog.h";
		currentData.reportName = "Test_Junit.xml";
		currentData.config_folder = TESTDATA;
		currentData.reportPath = TESTDATA + File.separator + "BCTools\\reports";
		String [] args = methods.prepareHAArguments(currentData);
		
		assertNotNull(args);
	}
	
	public void testHAAnalyseAll()
	{
		ProductSdkData currentData = new ProductSdkData();
		currentData.analyseAll = true;
		currentData.allTypes = true;
		currentData.coreToolsPath = TESTDATA + File.separator + "BCTools";
		currentData.reportName = "Test_Junit.xml";
		currentData.config_folder = TESTDATA;
		currentData.reportPath = TESTDATA + File.separator +"BCTools\\reports";
		String [] args = methods.prepareHAArguments(currentData);
		
		assertNotNull(args);
	}
	
	public void testHAAnalyseSelectedTypes()
	{
		ProductSdkData currentData = new ProductSdkData();
		currentData.analyseAll = true;
		currentData.allTypes = false;
		currentData.hTypes = true;
		currentData.rsgTypes = true;
		currentData.coreToolsPath = TESTDATA + File.separator + "BCTools";
		currentData.reportName = "Test_Junit.xml";
		currentData.config_folder = TESTDATA;
		currentData.reportPath = TESTDATA + File.separator + "BCTools\\reports";
		String [] args = methods.prepareHAArguments(currentData);
		
		assertNotNull(args);
	}
	
	public void testHAAnalyseSelectedFiles()
	{
		ProductSdkData currentData = new ProductSdkData();
		currentData.analyseAll = false;
		currentData.allTypes = false;
		currentData.hTypes = true;
		currentData.rsgTypes = true;
		currentData.coreToolsPath = TESTDATA + File.separator + "BCTools";
		currentData.HeaderFilesList = new String [10];
		currentData.HeaderFilesList[0] = "test1.h";
		currentData.HeaderFilesList[1] = "test1.hrh";
		currentData.HeaderFilesList[2] = "test1.rsg";
		currentData.HeaderFilesList[3] = "test1.mbg";
		currentData.HeaderFilesList[4] = "abcd.hrh";
		currentData.HeaderFilesList[5] = "pqrs.rsg";
		currentData.HeaderFilesList[6] = "agentdialog.h";
		currentData.HeaderFilesList[7] = "aknui.mbg";
		currentData.HeaderFilesList[8] = "mmapf.h";
		currentData.HeaderFilesList[9] = "xyz.rsg";
		currentData.reportName = "Test_Junit.xml";
		currentData.config_folder = TESTDATA;
		currentData.reportPath = TESTDATA + File.separator + "BCTools\\reports";
	
		String [] args = methods.prepareHAArguments(currentData);
		
		assertNotNull(args);
	}
	
	public void testOCDataValidity()
	{
		ProductSdkData currentData = new ProductSdkData();
		currentData.productSdkName = Activator.SDK_NAME;
		currentData.productSdkVersion = Activator.SDK_VERSION;
		currentData.epocRoot = Activator.EPOCROOT;
		//currentData.useDefaultCoreTools = true;
		currentData.useLocalCoreTools = true;
		currentData.coreToolsPath = TESTDATA + File.separator + "BCTools";
		currentData.toolChain = "GCCE";
		currentData.toolChainPath = "C:\\temp\\gcce\\bin";
		currentData.currentLibsDir = new String[]{Activator.EPOCROOT + "\\epoc32\\release\\armv5\\lib"};
		currentData.reportName = "Test_Junit.xml";
		currentData.config_folder = TESTDATA;
		currentData.reportPath = "C:\\temp\\reports";
		
		BaselineSdkData baselineData = methods.getBaselineProfileData(Activator.PROFILE_NAME);
				
		String status = methods.checkOCInput(currentData, baselineData);
		System.out.println(status);
		assertNull(status);
	}
	
	public void testOCArgsPreparation()
	{
		ProductSdkData currentData = new ProductSdkData();
		currentData.coreToolsPath = TESTDATA + File.separator + "BCTools"; 
		currentData.libraryFilesList = new String[1];
		currentData.libraryFilesList[0] = "apengine.dso";
		currentData.reportName = "Test_Junit.xml";
		currentData.config_folder = TESTDATA;
		currentData.reportPath = TESTDATA + File.separator + "BCTools\\reports";
		String [] args = methods.prepareOCArguments(currentData);
		
		assertNotNull(args);
	}
	
	public void testKnownIssuesReadingFromWebServer()
	{
		String url = "http://s60tools.ntc.nokia.com/ca/knownissues/";
		ArrayList<String> issueFiles = new ArrayList<String>();
		String status =  methods.getAllKnownIssuesFromWebServer(url, issueFiles);
		
		assertNull(status, status);
	}
	
	public void testGetIncludedFiles()
	{
		String sourceFile = TESTDATA + File.separator + File.separator + "Test.h";
		String [] incFiles = methods.getHeadersFromFile(sourceFile);
		assertSame(2, incFiles.length);
	}
	
	public void testGetSupportedHeadersFromDir()
	{
		String sourceDir = TESTDATA + File.separator + File.separator + "TestDir";
		ArrayList<String> supportedFiles = methods.getSupportedFilesFromDir(sourceDir);
		assert(supportedFiles.size() > 0);
	}
	
	public void testCopyFile() {
		File source=new File(TESTDATA + File.separator + "Copy_Checkbc.py");
		File dest=new File(TESTDATA + File.separator + "New_Checkbc.py");
		methods.copyFile(source, dest, true);
		assertTrue(dest.exists());
	}
	public void testDeleteFiles(){
		File carbideHA=new File(TESTDATA + File.separator + "carbide_ha.cf");
		assertTrue(carbideHA.exists());
		assertTrue(carbideHA.delete());
		
		File carbideHeaders=new File(TESTDATA + File.separator + "carbide_headers.txt");
		assertTrue(carbideHeaders.exists());
		assertTrue(carbideHeaders.delete());
		
		File carbideOC=new File(TESTDATA + File.separator + "carbide_oc.cf");
		assertTrue(carbideOC.exists());
		assertTrue(carbideOC.delete());
		
		File new_checkbc=new File(TESTDATA + File.separator + "New_Checkbc.py");
		assertTrue(new_checkbc.exists());
		assertTrue(new_checkbc.delete());
		
	}
}
