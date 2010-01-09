---------------------------------------------------------------
INSTRUCTIONS TO RUN THE COMPATIBILITY ANALYSER UNIT TEST CASES:
---------------------------------------------------------------

Note: Some of the testcases need some data to be executed successfully.
      That data can be found in this test project. (Folder named 'data')

A) The test data can be accessed any of the following method
   The 'data' folder is accessed by adding one VM Argument to 'data' folder path.
   As follows, -DTESTDATALOCATION=<path to data folder>.
	
B) Providing SDK details
	 For this there are some variables related to SDK info.
	 Provide values for the below variables mentioned in Activator.java file
	 - SDK_NAME : 
	 		Provide any SDK name installed/available in your local PC.
	 		Ex: SDK_NAME = "S60_3rd_FP1"
	 - SDK_VERSION :
	 		SDK version of above provided SDK. 
	 		Ex: SDK_VERSION = "3.0"
	 - TARTGET_TYPE :
	 		Ex: TARGET_TYPE = armv5 or gcce etc..
	 - PROFILE_NAME :
			A baseline profile must be present in workspace data location.
			$(WorkspaceLocation)/.metadata/.plugins/com.nokia.s60tools.compatibilityanalyser/BaselineProfiles/
			Ex: Say if test.data profile is present at the workspace location.Then mention profile name as
			    PROFILE_NAME = "test"
	 - EPOCROOT	:
	 		Epocroot of the above profided SDK.
	 		Ex: EPOCROOT = "C:\Symbian\9.2\S60_3rd_FP1"


-------------------
END OF INSTRUCTIONS
-------------------

