#
# Copyright (c) 2008, 2009 Nokia Corporation and/or its subsidiary(-ies).
# All rights reserved.
# This component and the accompanying materials are made available
# under the terms of the License "Symbian Foundation License v1.0"
# which accompanies this distribution, and is available
# at the URL "http://www.symbianfoundation.org/legal/sfl-v10.html".
#
# Initial Contributors:
# Nokia Corporation - initial contribution.
#
# Contributors:
#
# Description:  End-user Interface for Core Tools execution
#

import sys
import os
import re
#while importing check if all modules are available, else raise error
try:
	import subprocess
	import platform
	import urllib
except ImportError:
	python_error()
	
#-------------------------Hardcoded values-----------------------------------------
#Currently hardcoded values, these will be moved to a metadata file later
#data version denotes compatibility between the tool and carbide plugin
DATA_VERSION = "5"
#tool version denotes the version of the core tools package
TOOL_VERSION = "2.8.0"
TOOL_DATE = "19th June 2009"

#server to be used for downloading Core tools package and knownissues
SERVER_PATH = "http://"

#-------------------------Global values--------------------------------------------
CUR_DIR = os.getcwd()
#used to obtain the path in which checkbc and core tools are placed
tempStr = os.path.realpath(sys.argv[0]) 
[head,tail] = os.path.split(tempStr )
TOOL_DIR = head + os.sep
#create the \data and \bin paths which contain the necessary additional headers
DATA_PATH = TOOL_DIR + "data" + os.sep
EXEC_PATH = TOOL_DIR + "bin" + os.sep
REPORT_PATH = TOOL_DIR + "reports" + os.sep
DEFAULT_ISSUES_FILE = TOOL_DIR + "data" + os.sep + "knownissues.xml"
s60_build_targets = [ 'armv5', 'armv5_abiv2', 'armv6', 'armv6t2', 'armv7a' ]

#dictionary elements which hold the platform data(CDS) and forced header(symbian macros) information
#these are available only when the \data and \bin folders respectively are avaliable
if os.path.exists( DATA_PATH ):
	platformdata = {
	"3.0": DATA_PATH + "s60_platform_data_30.xml",
	"3.1": DATA_PATH + "s60_platform_data_31.xml",
	"3.2": DATA_PATH + "s60_platform_data_32.xml",
	"5.0": DATA_PATH + "s60_platform_data_50.xml",
	"5.1": DATA_PATH + "s60_platform_data_51.xml",
	"5.2": DATA_PATH + "s60_platform_data_52.xml",
	"SF1": DATA_PATH + "s60_platform_data_51.xml",
	"SF2": DATA_PATH + "s60_platform_data_52.xml"
	}
else:
	platformdata = {}
	
if os.path.exists( EXEC_PATH ):	
	forcedheadersdata = {
	"3.0": EXEC_PATH + "forced_9.1.h",
	"3.1": EXEC_PATH + "forced_9.2.h",
	"3.2": EXEC_PATH + "forced_9.3.h",
	"5.0": EXEC_PATH + "forced_9.4.h",
	"5.0v2": EXEC_PATH + "forced_9.4v2.h",
	"5.1": EXEC_PATH + "forced_9.4v2.h",
	"5.2": EXEC_PATH + "forced_9.4v2.h",
	"SF1": EXEC_PATH + "forced_9.4v2.h",
	"SF2": EXEC_PATH + "forced_9.4v2.h"
	}
else:
	forcedheadersdata = {}
	
if os.path.exists( DATA_PATH ):
	dllXMLdata = {
	"3.0": DATA_PATH + "s60_dll_data_30.xml",
	"3.1": DATA_PATH + "s60_dll_data_31.xml",
	"3.2": DATA_PATH + "s60_dll_data_32.xml",
	"5.0": DATA_PATH + "s60_dll_data_50.xml",
	"5.1": DATA_PATH + "s60_dll_data_51.xml",
	"5.2": DATA_PATH + "s60_dll_data_52.xml",
	"SF1": DATA_PATH + "s60_dll_data_51.xml",
	"SF2": DATA_PATH + "s60_dll_data_52.xml"
	}
else:
	dllXMLdata = {}

#Lists to hold platform dependant system include paths
sys_hdr_30 = [ '', 'libc', 'oem', 'ecom' ]

sys_hdr_32 = ['middleware', 'domain'+ os.sep +'middleware', 'osextensions', 'domain'+ os.sep +'osextensions', 'applications', 'domain'+ os.sep +'applications']
sys_hdr_32.extend(sys_hdr_30)
	
sys_hdr_50 = ['domain'+ os.sep +'middleware'+ os.sep + 'loc', 'domain'+ os.sep +'osextensions'+ os.sep +'loc', 'domain'+ os.sep +'applications' + os.sep + 'loc',
				  'domain'+ os.sep +'middleware'+ os.sep +'loc'+ os.sep +'sc', 'domain'+ os.sep +'osextensions'+ os.sep +'loc'+ os.sep +'sc',
				  'domain'+ os.sep +'applications'+ os.sep +'loc'+ os.sep +'sc']
sys_hdr_50.extend(sys_hdr_30)
sys_hdr_50.extend(sys_hdr_32)

sys_hdr_51 = ['mw', 'platform'+ os.sep + 'mw', 'platform', 'app','platform'+ os.sep + 'app', 'platform'+ os.sep + 'loc', 'platform'+ os.sep + 'mw' + os.sep + 'loc',
              'platform'+ os.sep + 'app' + os.sep + 'loc', 'platform'+ os.sep + 'loc' + os.sep + 'sc', 'platform'+ os.sep + 'mw' + os.sep + 'loc' + os.sep +'sc',
              'platform'+ os.sep + 'app' + os.sep + 'loc' + os.sep +'sc']
sys_hdr_51.extend(sys_hdr_50)

sys_includes = {
	"3.0": sys_hdr_30,
	"3.1": sys_hdr_30,
	"3.2": sys_hdr_32,
	"5.0": sys_hdr_50,
	"5.1": sys_hdr_51,
	"5.2": sys_hdr_51,
	"SF1": sys_hdr_51,
	"SF2": sys_hdr_51
	}

#set of binaries in the Core tools set, this is windows specific, to be added for linux support
if os.name == 'nt':
    HA_SET = [ EXEC_PATH+"ha.exe", EXEC_PATH+"ha_gccxml_cc1plus.exe", EXEC_PATH+"libxerces-c2_7_0.dll" ]
    LA_SET = [ EXEC_PATH+"la.exe", EXEC_PATH+"cfilt.exe"  ]
    BCFILTER_SET = [ EXEC_PATH+"bcfilter.exe", EXEC_PATH+"libxerces-c2_7_0.dll" ]
else:
    HA_SET = [ EXEC_PATH+"ha", EXEC_PATH+"ha_gccxml_cc1plus", EXEC_PATH+"libxerces-c2_7_0.dll" ]
    LA_SET = [ EXEC_PATH+"la", EXEC_PATH+"cfilt" ]
    BCFILTER_SET = [ EXEC_PATH+"bcfilter", EXEC_PATH+"libxerces-c2_7_0.dll" ]

#Default report paths
HEADER_REPORT = "Headers_CompatibilityReport"
LIBRARY_REPORT = "Libraries_CompatibilityReport"
#-------------------------Global Definitions------------------------------------------
#defines set of file types analysed supported in header analyser
ALL_HEADER_SET = '*.h;*.hrh;*.mbg;*.rsg;*.pan;*.hpp;*.rh'
#true if checkbc is called from carbide plugin,
#this make additional info available to STDOUT and STDEERR
CARBIDE_PLUGIN = False
#-------------------------Error Handling--------------------------------------------
#exhults with a environment error when the installed python version is unsupported
def python_error():
	sys.stdout.write("ERROR: Invalid python version")
	sys.stdout.write("\nPython versions from 2.4 to 3.0 are supported")
	sys.exit(1)

#this is the set of possible error values, stored as a dictionary, with the "value" represnting error message
ErrorCode = {
	"cmdlineIP": [ "\nERROR: Commandline input parameter invalid -- " , 1 ],
	"cmdlineIS": [ "\nERROR: Commandline parameter syntax invalid -- " , 2 ],
	"cmdlineMP": [ "\nERROR: Commandline parameter missing -- " , 3 ],
	"confIS": [ "\nERROR: Invalid syntax in config file -- " , 4 ],
	"confIP": [ "\nERROR: Invalid parameter in config file -- ", 5 ],
	"confMP": [ "\nERROR: Missing parameter in config file -- ", 6 ],
	"confMPath": [ "\nERROR: File/path in config file not found -- ", 7 ],
	"cmdhelp": [ "", 8],
	"other": [ "\nGENERAL ERROR: Please recheck the tool inputs.", 9 ]
	}

#an exception class, need to update this for better error representation
#value --> holds the error string, #text --> the info text to be displayed
class InputError(Exception):
	def __init__(self, list):
		self.error = ErrorCode["other"]
		if list[0] in ErrorCode:
			self.error = ErrorCode[ list[0] ]
		self.text = list[1]
		self.use = list[2]

#the exception handler class which prints out the error message and usage info when required
class ExHandler:
	def __init__(self, e):
		sys.stdout.write( e.error[0] )
		sys.stdout.write( e.text )
		if e.use:
			usage()
		sys.exit(e.error[1])	

#displays the usage characteristics for the interface when command is invoked without proper arguments
def usage():
	print ""
	print "Compatibility Analyser v" + TOOL_VERSION + " - " + TOOL_DATE
	print "Copyright (c) 2001-2009 Nokia Corporation and/or its subsidiary(-ies). All rights reserved."
	print ""
	print "Usage: CheckBC <configfile> [-ha/-hm/-hs] [-la/-lm/-ls] [-f] [reportid]"
	print ""
	print "Where:"
	print "  configfile    Filename of a configuration file"
	print "  -ha           Check all headers"
	print "  -hm FILE      Check multiple headers (FILE = file with list of headers)"
	print "  -hs FILE      Check a single file (FILE = header)"
	print "  -la           Check all libraries"
	print "  -lm FILE      Check multiple libraries (FILE = file with list of libraries)"
	print "  -ls FILE      Check a single file (FILE = library)"
	print "  -f            Filter results after analysis"
	print "  reportid      ID to be used for report files"
	print ""
	print "Examples:"
	print "  To analyse all headers and libraries and filter results:"
	print "    CheckBC myconfig -ha -la -f MYID"
	print "  To analyse a single header file"
	print "    CheckBC myconfig -hs aknlists.h MYID"
	
#-------------------------Header Analysis--------------------------------------------
class HeaderAnalyser:
	# static dict to hold the arguments
	args = {}

	#initialization function for HA component	
	def __init__(self, ip_data, set_list, reportid):
		#'args' defines the parametrs required by HA
		self.args = {"BASELINE_NAME":[], "CURRENT_NAME":[], "BASELINE_DIR":[], "CURRENT_DIR":[], "REPORT_FILE":[],
				"BASELINE_SYSTEMINCLUDEDIR":[], "CURRENT_SYSTEMINCLUDEDIR":[], "FORCEBASEINCLUDE":[], "FORCECURRENTINCLUDE":[],
				"RECURSIVE":[], "REPLACE":[], "TEMP":[], "USE_PLATFORM_DATA":[], "SET":[], "BUNDLESIZE":['-bundlesize', '50'] }

		base_sys_include = []
		curr_sys_include = []
		base_forced = []
		curr_forced = []

		# specify the basline and current names
		if not getdata(ip_data, "BASELINE_NAME"):
			raise InputError(["confMP", "baseline name missing\n", False])
		self.args["BASELINE_NAME"] = ["-baselineversion", quote( ip_data["BASELINE_NAME"] )]			
		if not getdata( ip_data, "CURRENT_NAME"):
			raise InputError(["confMP", "current name missing\n", False])
		self.args["CURRENT_NAME"] = ["-currentversion", quote( ip_data["CURRENT_NAME"] )]

		# get the analysis directories
		tmp = []
		sdkBaseTmp = []
		if getdata( ip_data, "BASELINE_SDK_DIR"):
			if(ip_data["BASELINE_SDK_DIR"] == os.sep):
				sdkBaseTmp.append( validate(os.sep + "epoc32" + os.sep + "include"))
			else:
			  sdkBaseTmp.append( validate(ip_data["BASELINE_SDK_DIR"] + os.sep + "epoc32" + os.sep + "include") )
		else:
			raise InputError(["confMP", "baseline header directory missing\n", False])

		if getdata( ip_data, "BASELINE_HEADERS"):
			if(ip_data["BASELINE_HEADERS"] == os.sep):
			  tmp = sdkBaseTmp
			else:
			  for i in ip_data["BASELINE_HEADERS"].split(';'):
			  	if not os.path.exists(i):
			  		if(ip_data["BASELINE_SDK_DIR"] == os.sep):
			  		  tmp.append(validate(os.sep + "epoc32" + os.sep + "include" + os.sep + i))
			  		else:
			  			tmp.append(validate(ip_data["BASELINE_SDK_DIR"] + os.sep + "epoc32" + os.sep + "include" + os.sep + i))
			  	else:
				    tmp.append(validate(i))
		else:
			tmp = sdkBaseTmp

		self.args["BASELINE_DIR"] = ["-baselinedir", ';'.join(["%s" % quote(i) for i in tmp]) ]
		base_sys_include.extend(tmp)
		
		tmp = []	
		sdkCurrTmp = []	
		if getdata( ip_data, "CURRENT_SDK_DIR"):
		  if(ip_data["CURRENT_SDK_DIR"] == os.sep):
		    sdkCurrTmp.append( validate(os.sep+"epoc32" + os.sep + "include" ))
		  else:
			  sdkCurrTmp.append( validate(ip_data["CURRENT_SDK_DIR"] + os.sep + "epoc32" + os.sep + "include") )
		else:
			raise InputError(["confMP", "current header directory missing\n", False])

		if getdata( ip_data, "CURRENT_HEADERS"):
		  if(ip_data["CURRENT_HEADERS"] == os.sep):
		    tmp = sdkCurrTmp
		  else:
			  for i in ip_data["CURRENT_HEADERS"].split(';'):
			  	if not os.path.exists(i):
			  		if(ip_data["CURRENT_SDK_DIR"] == os.sep):
			  		  tmp.append(validate(os.sep + "epoc32" + os.sep + "include" + os.sep + i))
			  		else:
			  			tmp.append(validate(ip_data["CURRENT_SDK_DIR"] + os.sep + "epoc32" + os.sep + "include" + os.sep + i))
			  	else:
				    tmp.append(validate(i))

		else:
			tmp = sdkCurrTmp
		
		self.args["CURRENT_DIR"] = ["-currentdir", ';'.join(["%s" % quote(i) for i in tmp]) ]
		curr_sys_include.extend(tmp)

		# get the report file name		
		if not getdata( ip_data, "REPORT_FILE_HEADERS"):
			if not reportid:
				ip_data["REPORT_FILE_HEADERS"] = REPORT_PATH + HEADER_REPORT
			else:
				ip_data["REPORT_FILE_HEADERS"] = REPORT_PATH + HEADER_REPORT +'_'+ reportid
		else:
			[head, tail] = os.path.split(ip_data["REPORT_FILE_HEADERS"])
			if tail != '':
				if reportid:
					ip_data["REPORT_FILE_HEADERS"] = ip_data["REPORT_FILE_HEADERS"] +'_'+ reportid
			else:
				if reportid:
					ip_data["REPORT_FILE_HEADERS"] = ip_data["REPORT_FILE_HEADERS"] + HEADER_REPORT +'_'+ reportid
				else:
					ip_data["REPORT_FILE_HEADERS"] = ip_data["REPORT_FILE_HEADERS"] + HEADER_REPORT
			
		ip_data["REPORT_FILE_HEADERS"] = ip_data["REPORT_FILE_HEADERS"] + ".xml"		
		self.args["REPORT_FILE"] = ["-reportfile", quote( ip_data["REPORT_FILE_HEADERS"])]

		tmp = []
		# get the base system include directories		
		if getdata( ip_data, "BASELINE_SYSTEMINCLUDEDIR"):
			for term in ip_data["BASELINE_SYSTEMINCLUDEDIR"].split(';'):
			  if not os.path.exists(term):
			  	if(ip_data["BASELINE_SDK_DIR"] == os.sep):
			  	  tmp.append(os.sep + "epoc32" + os.sep + "include" + os.sep + term)
			  	else:
			  	  tmp.append(ip_data["BASELINE_SDK_DIR"] + os.sep + "epoc32" + os.sep + "include" + os.sep + term)
			  else:
			  	tmp.append(term);			  	
			
		for i in sys_includes[ip_data["BASELINE_SDK_S60_VERSION"]]:
			if(ip_data["BASELINE_SDK_DIR"] == os.sep):
			  tmp.append( os.sep + "epoc32" + os.sep + "include" + os.sep + i )
			else:
			  tmp.append( ip_data["BASELINE_SDK_DIR"] + os.sep + "epoc32" + os.sep + "include" + os.sep + i )
			
		for i in tmp:	
			try:
				term = validate(i)
			except InputError, e:
				sys.stderr.write(os.linesep + "WARNING: Baseline system include path "+ i +" not found")
			else:
				if term not in base_sys_include:
					base_sys_include.append(term)
		self.args["BASELINE_SYSTEMINCLUDEDIR"] = ["-baseplatformheaders", quote( ';'.join(["%s" % quote(i) for i in base_sys_include]) ) ]

		tmp = []
		# get the current system include directories		
		if getdata( ip_data, "CURRENT_SYSTEMINCLUDEDIR"):
			for term in ip_data["CURRENT_SYSTEMINCLUDEDIR"].split(';'):
				if not os.path.exists(term):
					if(ip_data["CURRENT_SDK_DIR"] == os.sep):
						tmp.append(os.sep + "epoc32" + os.sep + "include" + os.sep + term)
					else:
						 tmp.append(ip_data["CURRENT_SDK_DIR"] + os.sep + "epoc32" + os.sep + "include" + os.sep + term)
				else:
					tmp.append(term);
					
		for i in sys_includes[ip_data["CURRENT_SDK_S60_VERSION"]]:
			if(ip_data["CURRENT_SDK_DIR"] == os.sep):
			  tmp.append( os.sep + "epoc32" + os.sep + "include" + os.sep + i )
			else:
			  tmp.append( ip_data["CURRENT_SDK_DIR"] + os.sep + "epoc32" + os.sep + "include" + os.sep + i )
		
		for i in tmp:			
			try:
				term = validate(i)
			except InputError, e:
				sys.stderr.write(os.linesep + "WARNING: Current system include path "+ i +" not found")
			else:
				if term not in curr_sys_include:
					curr_sys_include.append(term)
		self.args["CURRENT_SYSTEMINCLUDEDIR"] = ["-currentplatformheaders", quote( ';'.join(["%s" % quote(i) for i in curr_sys_include]) ) ]

		# get the forced headers 														 
		if not getdata( ip_data, "BASELINE_SDK_S60_VERSION"):
			raise InputError(["confMP", "Baseline SDK version undefined\n", False])
		for i in getdata( ip_data, "BASELINE_FORCED_HEADERS").split(';'):
			if i:
				base_forced.append(i)
		if forcedheadersdata.has_key(ip_data["BASELINE_SDK_S60_VERSION"]):
			base_forced.append( self.forcedheaders(ip_data["BASELINE_SDK_S60_VERSION"], self.args["BASELINE_SYSTEMINCLUDEDIR"][1]) )
		self.args["FORCEBASEINCLUDE"] = ["-forcebaseinclude", ';'.join(["%s" % quotep(i) for i in base_forced ]) ]
		
		if not getdata( ip_data, "CURRENT_SDK_S60_VERSION"):
			raise InputError(["confMP", "Current SDK version undefined\n", False])
		for i in getdata( ip_data, "CURRENT_FORCED_HEADERS").split(';'):
			if i:
				curr_forced.append(i)
		if forcedheadersdata.has_key(ip_data["CURRENT_SDK_S60_VERSION"]):
			curr_forced.append( self.forcedheaders(ip_data["CURRENT_SDK_S60_VERSION"], self.args["CURRENT_SYSTEMINCLUDEDIR"][1]) )
		self.args["FORCECURRENTINCLUDE"] = ["-forcecurrentinclude", ';'.join(["%s" % quotep(i) for i in curr_forced ]) ]

		# setup the replace and exclude directories
		self.args["RECURSIVE"] = [ "-recursive" ]
		if getdata( ip_data, "RECURSIVE_HEADERS"):
			if 'false' == ip_data["RECURSIVE_HEADERS"].lower():
				self.args["RECURSIVE"] = [ "" ]
		tmp = []
		for i in getdata( ip_data, "EXCLUDE_DIR_HEADERS").split(';'):
			if i:
				tmp.append(i)
		if tmp:
			self.args["EXCLUDE_DIR_HEADERS"] = ["-excludedirs", ';'.join(["%s" % quote(i) for i in tmp]) ]
		
		self.args["TEMP"] = ["-temp", quotep( ip_data["TEMP"] )]

		if not getdata( ip_data, "USE_PLATFORM_DATA") or ('true' == ip_data["USE_PLATFORM_DATA"].lower()):
			if getdata(	platformdata, ip_data["BASELINE_SDK_S60_VERSION"] ) and getdata( platformdata, ip_data["CURRENT_SDK_S60_VERSION"] ):
				self.args["USE_PLATFORM_DATA"] = ["-baseplatformdata", quotep( platformdata[ip_data["BASELINE_SDK_S60_VERSION"]] ),
										 "-currentplatformdata", quotep( platformdata[ip_data["CURRENT_SDK_S60_VERSION"]] )]
			else:
				sys.stderr.write( os.linesep + "WARNING: Platform data not available in default paths, continuing without platform data information." )

		if getdata( ip_data, "REPLACE_HEADERS"):
			#separate the sets
			tlist_1 = ip_data["REPLACE_HEADERS"].split(';')
			str = ''
			#seperate the replacement pairs
			for entry in tlist_1:
				if entry != '':
					tlist_2 = (entry.split(':'))
					#create a single str, which is input param	
					str = ' '.join(["%s" % i for i in tlist_2])
			self.args["REPLACE"] = ["-replace", str ]
		else:
			del self.args["REPLACE"]

		# handling the files provide with 'm' or 's' options on commandline
		str = ''
		line = ''
		tmp = []
		if 's' == set_list[0]:
			str = set_list[1]
		elif 'm' == set_list[0]:
			fname = os.path.abspath(set_list[1])
			if not os.path.exists(fname):
				raise InputError(["confIP", "Input list file unavailable" + os.linesep, False])	
			filehandle = open(fname, 'r')
			for line in filehandle:
				tmp.append(clean(line))
			str = ';'.join(["%s" % i for i in tmp])
			filehandle.close()	
		elif 'a' == set_list[0]:
			str = ALL_HEADER_SET
		self.args["SET"] = ["-set", quote(str)]
		
	#'stringize' all params and invoke the tool
	def run(self):
		str = ''
		for i in HA_SET:
			if not os.path.exists(i):
				print os.linesep + i + " does not exist. Please reinstall."
				sys.exit(1)
		it_tmp = self.args.itervalues()
		for i in xrange(0, len(self.args), 1):
			for val in it_tmp.next():
				str = str + ' ' + val
		cmd_file= unquote(self.args["TEMP"][1]) + os.sep + 'ha.txt'
		file = open(cmd_file,'w')
		file.write(str)
		file.close()
		cmd_str = ' -commandfile ' + cmd_file
		invokeTool(quote(HA_SET[0]), cmd_str)

	#verify existence of system hrh files
	def forcedheaders(self, str, dirs):
		if "5.0" == str:
			dirs = unquote(dirs).split(';')
			for path in dirs:
				checkpath = os.path.normpath( unquote(path) + os.sep + "variant" + os.sep + "symbian_os_v9.4.hrh" )
				if os.path.exists( checkpath ):
					if forcedheadersdata.has_key(str):
						return forcedheadersdata[str]
			if forcedheadersdata.has_key("5.0v2"):
				return forcedheadersdata["5.0v2"]
		else:
			if forcedheadersdata.has_key(str):
				return forcedheadersdata[str]
		return ''	

#-------------------------Library Analysis-------------------------------------------
class LibraryAnalyser:
	args = {}
	def __init__(self, ip_data, set_list, reportid):
		#'args' defines the parametrs required by HA, 'optargs' defines optional ones
		self.args = { "TOOLCHAIN":[], "TOOLCHAIN_PATH":[], "BASELINE_NAME":[], "CURRENT_NAME":[], "BASELINE_DIR":[], "CURRENT_DIR":[],
				 "REPORT_FILE":[], "CFILT":[], "TEMP":[], "SET":[] }
		
		if getdata( ip_data, "TOOLCHAIN"):
			self.args["TOOLCHAIN"] = [ ip_data["TOOLCHAIN"] ]			
		else:
			self.args["TOOLCHAIN"] = [ 'GCCE' ]
			
		if getdata( ip_data, "TOOLCHAIN_PATH"):
			self.args["TOOLCHAIN_PATH"] = [ '-tools', quotep( ip_data["TOOLCHAIN_PATH"] )]
		else:
			del self.args["TOOLCHAIN_PATH"]

		if not getdata( ip_data, "BASELINE_NAME"):
			raise InputError(["confMP", "baseline analysis directory missing" + os.linesep, False])
		self.args["BASELINE_NAME"] = [ '-baselineversion', quote( ip_data["BASELINE_NAME"] )]
		if not getdata( ip_data, "CURRENT_NAME"):
			raise InputError(["confMP", "current analysis directory missing" + os.linesep, False])
		self.args["CURRENT_NAME"] = [ '-currentversion', quote( ip_data["CURRENT_NAME"] )]
			

		#Check if baseline import library is present, import dll should also be present and vice versa.
		libLength = 0
		dllLength = 0
	
		if getdata( ip_data, "BASELINE_IMPORTLIBRARIES"):
			libLength = len(ip_data["BASELINE_IMPORTLIBRARIES"])
			
		if getdata( ip_data, "BASELINE_IMPORTDLLS"):
			dllLength = len(ip_data["BASELINE_IMPORTDLLS"])
			
		if( libLength > 0 and dllLength <= 0 ):
		  raise InputError(["confMP", "BASELINE_IMPORTDLLS Path missing" + os.linesep, False])
		if( libLength <= 0 and dllLength > 0 ):
		  raise InputError(["confMP", "BASELINE_IMPORTLIBRARIES Path missing" + os.linesep, False])

		#Check if current import library is present, import dll should also be present and vice versa.
		libLength = 0
		dllLength = 0

		if getdata( ip_data, "CURRENT_IMPORTLIBRARIES"):
			libLength = len(ip_data["CURRENT_IMPORTLIBRARIES"])
			
		if getdata( ip_data, "CURRENT_IMPORTDLLS"):
			dllLength = len(ip_data["CURRENT_IMPORTDLLS"])
			  
		if( libLength > 0 and dllLength <= 0 ):
		  raise InputError(["confMP", "CURRENT_IMPORTDLLS missing" + os.linesep, False])
		if( libLength <= 0 and dllLength > 0 ):
		  raise InputError(["confMP", "CURRENT_IMPORTLIBRARIES missing" + os.linesep, False])			

				
		if not getdata( ip_data, "BASELINE_SDK_DIR"):
			raise InputError(["confMP", "baseline importlib directory missing" + os.linesep, False])
		if not getdata( ip_data, "CURRENT_SDK_DIR"):
			raise InputError(["confMP", "current importlib directory missing" + os.linesep, False])
		

		if getdata(	dllXMLdata, ip_data["BASELINE_SDK_S60_VERSION"] ):
			baseDlldata = dllXMLdata[ip_data["BASELINE_SDK_S60_VERSION"]]
		if getdata(	dllXMLdata, ip_data["CURRENT_SDK_S60_VERSION"] ):
			currDlldata = dllXMLdata[ip_data["CURRENT_SDK_S60_VERSION"]]
		
		dbasebuild = GetBuildTarget(ip_data["BASELINE_SDK_DIR"],validate(baseDlldata),ip_data["TEMP"])
		dcurrentbuild = GetBuildTarget(ip_data["CURRENT_SDK_DIR"],validate(currDlldata),ip_data["TEMP"])
		
		if getdata( ip_data, "BASELINE_BUILDTARGET"):
			basebuild = ip_data["BASELINE_BUILDTARGET"]
		else:
			basebuild = dbasebuild
			
		if getdata( ip_data, "CURRENT_BUILDTARGET"):
			currentbuild = ip_data["CURRENT_BUILDTARGET"]
		else:
			currentbuild = dcurrentbuild

		if 	basebuild == "" and currentbuild == "":
			basebuild = 'armv5'
			currentbuild = 'armv5'
		else:
			if basebuild == "":
				basebuild = currentbuild
			if currentbuild == "":
				currentbuild = basebuild
		
		libBasetmp = []
		libtmp = []		
		for target in basebuild.split(';'):
			if(ip_data["BASELINE_SDK_DIR"] == os.sep):
				libBasetmp.append(os.sep + 'epoc32' + os.sep + 'release' + os.sep + target + os.sep + 'lib')
			else:			
				libBasetmp.append(ip_data["BASELINE_SDK_DIR"] + os.sep + 'epoc32' + os.sep + 'release' + os.sep + target + os.sep + 'lib')	
		if getdata( ip_data, "BASELINE_IMPORTLIBRARIES"):
			if(ip_data["BASELINE_IMPORTLIBRARIES"] == os.sep):
				for path in libBasetmp:
					libtmp.append(validate(path))
			else:
				for i in ip_data["BASELINE_IMPORTLIBRARIES"].split(';'):
					if not os.path.exists(i):
						if(ip_data["BASELINE_SDK_DIR"] == os.sep):
							libtmp.append(validate( os.sep + 'epoc32' + os.sep + 'release' + os.sep + i))
						else:
							libtmp.append(validate( ip_data["BASELINE_SDK_DIR"] + os.sep + 'epoc32' + os.sep + 'release' + os.sep + i ))
					else:
						libtmp.append(validate(i))
		else:
			for path in libBasetmp:
				libtmp.append(validate(path))
			
		dllBasetmp = []
		dlltmp = []			
		if dbasebuild == "":
			dllBasetmp.append(baseDlldata)
		else:
			for target in basebuild.split(';'):
				if(ip_data["BASELINE_SDK_DIR"] == os.sep):
					dllBasetmp.append(os.sep + 'epoc32' + os.sep + 'release' + os.sep + target + os.sep + 'urel')
				else:
					dllBasetmp.append(ip_data["BASELINE_SDK_DIR"] + os.sep + 'epoc32' + os.sep + 'release' + os.sep + target + os.sep + 'urel')	
		if getdata( ip_data, "BASELINE_IMPORTDLLS"):
			if(ip_data["BASELINE_IMPORTDLLS"] == os.sep):
				for path in dllBasetmp:
					dlltmp.append(validate(path))
			else:
				for i in ip_data["BASELINE_IMPORTDLLS"].split(';'):
					if not os.path.exists(i):
						if(ip_data["BASELINE_SDK_DIR"] == os.sep):
							dlltmp.append(validate( os.sep + 'epoc32' + os.sep + 'release' + os.sep + i))
						else:
							dlltmp.append(validate( ip_data["BASELINE_SDK_DIR"] + os.sep + 'epoc32' + os.sep + 'release' + os.sep + i ))
					else:
						dlltmp.append(validate(i))
		else:
			for path in dllBasetmp:
				dlltmp.append(validate(path))
				
		self.args["BASELINE_DIR"] = ["-baselinelibdir", ';'.join(["%s" % quote(i) for i in libtmp])]	
		self.args["BASELINE_DLL_DIR"] = ["-baselinedlldir", ';'.join(["%s" % quote(i) for i in dlltmp])]

		libCurrTmp = []
		libtmp = []
		for target in currentbuild.split(';'):
			if(ip_data["CURRENT_SDK_DIR"] == os.sep):
				libCurrTmp.append(os.sep + 'epoc32' + os.sep + 'release' + os.sep + target + os.sep + 'lib')
			else:
			    libCurrTmp.append(ip_data["CURRENT_SDK_DIR"] + os.sep + 'epoc32' + os.sep + 'release' + os.sep + target + os.sep + 'lib')
		if getdata( ip_data, "CURRENT_IMPORTLIBRARIES"):
			if(ip_data["CURRENT_IMPORTLIBRARIES"] == os.sep):
				for target in libCurrTmp:
					libtmp.append(validate(target))
			else:
				for i in ip_data["CURRENT_IMPORTLIBRARIES"].split(';'):
					if not os.path.exists(i):
						if(ip_data["CURRENT_SDK_DIR"] == os.sep):
							libtmp.append(validate( os.sep + 'epoc32' + os.sep + 'release' + os.sep + i))
						else:
							libtmp.append(validate( ip_data["CURRENT_SDK_DIR"] + os.sep + 'epoc32' + os.sep + 'release' + os.sep + i ))
					else:
						libtmp.append(validate(i))
		else:
			for target in libCurrTmp:
				libtmp.append(validate(target))
			
		#Get baseline and current dll path and provide for analysis	
		dllCurrtmp = []
		dlltmp = []	
		if dcurrentbuild == "":
			dllCurrtmp.append(currDlldata)	
		else:
			for target in currentbuild.split(';'):
				if(ip_data["CURRENT_SDK_DIR"] == os.sep):
					dllCurrtmp.append(os.sep + 'epoc32' + os.sep + 'release' + os.sep + target + os.sep + 'urel')
				else:
					dllCurrtmp.append(ip_data["CURRENT_SDK_DIR"] + os.sep + 'epoc32' + os.sep + 'release' + os.sep + target + os.sep + 'urel')	
		if getdata( ip_data, "CURRENT_IMPORTDLLS"):
			if(ip_data["CURRENT_IMPORTDLLS"] == os.sep):
				for target in dllCurrtmp:
					dlltmp.append(validate(target))
			else:
				for i in ip_data["CURRENT_IMPORTDLLS"].split(';'):
					if not os.path.exists(i):
						if(ip_data["CURRENT_SDK_DIR"] == os.sep):
							dlltmp.append(validate( os.sep + 'epoc32' + os.sep + 'release' + os.sep + i))
						else:
							dlltmp.append(validate( ip_data["CURRENT_SDK_DIR"] + os.sep + 'epoc32' + os.sep + 'release' + os.sep + i ))
					else:
						dlltmp.append(validate(i))
		else:
			for target in dllCurrtmp:
				dlltmp.append(validate(target))

		self.args["CURRENT_DIR"] = ["-currentlibdir", ';'.join(["%s" % quote(i) for i in libtmp])]
		self.args["CURRENT_DLL_DIR"] = ["-currentdlldir", ';'.join(["%s" % quote(i) for i in dlltmp])]

		# get the report file name		
		if not getdata( ip_data, "REPORT_FILE_LIBRARIES"):
			if not reportid:
				ip_data["REPORT_FILE_LIBRARIES"] = REPORT_PATH + LIBRARY_REPORT
			else:
				ip_data["REPORT_FILE_LIBRARIES"] = REPORT_PATH + LIBRARY_REPORT +'_'+ reportid
		else:
			[head, tail] = os.path.split(ip_data["REPORT_FILE_LIBRARIES"])
			if tail != '':
				if reportid:
					ip_data["REPORT_FILE_LIBRARIES"] = ip_data["REPORT_FILE_LIBRARIES"] +'_'+ reportid
			else:
				if reportid:
					ip_data["REPORT_FILE_LIBRARIES"] = ip_data["REPORT_FILE_LIBRARIES"] + LIBRARY_REPORT +'_'+ reportid
				else:
					ip_data["REPORT_FILE_LIBRARIES"] = ip_data["REPORT_FILE_LIBRARIES"] + LIBRARY_REPORT
					
		ip_data["REPORT_FILE_LIBRARIES"] = ip_data["REPORT_FILE_LIBRARIES"] + ".xml"
		self.args["REPORT_FILE"] = ["-reportfile", quote( ip_data["REPORT_FILE_LIBRARIES"] )]
		
		if 'rvct' == self.args["TOOLCHAIN"][0].lower():
			self.args["CFILT"] = [ '-cfilt', quotep( LA_SET[1] )]
		else:
			del self.args["CFILT"]
			
		self.args["TEMP"] = ["-temp", quotep( ip_data["TEMP"] )]

		str = ''
		if 's' == set_list[0]:
			str = unquote(self.args["TEMP"][1]) + os.sep + 'la.txt'
			file = open( str, 'w')
			file.write( set_list[1] )
			file.close()
		elif 'm' == set_list[0]:
			str = os.path.abspath( set_list[1] )
			if not os.path.exists(str):
				raise InputError(["confIP", "Input list file unavailable" + os.linesep, False])	
		self.args["SET"] = ["-set", quote(str)]
		if 'a' == set_list[0]:
			del self.args["SET"]
		
	#'stringize' all params and invoke the tool
	def run(self):
		for i in LA_SET:
			if not os.path.exists(i):
				print os.linesep + i + " does not exist. Please reinstall."
				sys.exit(1)
		str = ' ' + self.args["TOOLCHAIN"][0]
		del self.args["TOOLCHAIN"]
		it_tmp = self.args.itervalues()
		for i in xrange(0, len(self.args), 1):
			for val in it_tmp.next():
				str = str + ' ' + val
		invokeTool(quote(LA_SET[0]), str)
			
#-------------------------Report Filter-------------------------------------------
class ReportFilter:
	args = {}
	issueargs = {}
	filterfiles = False
	def __init__(self, ip_data, cmds, reportid):
		reportlist = []
		outputlist = []
		issuelist = []
		#'args' defines the parametrs required by HA, 'optargs' defines optional ones
		self.args = { "ISSUES_FILE":[], "REPORT_FILE":[], "OUTPUT_FILE":[] }
		
		for val in cmds:
			if 'h' in val:
				if getdata( ip_data, "REPORT_FILE_HEADERS"):
					reportlist.append( ip_data["REPORT_FILE_HEADERS"] )
					if getdata( ip_data, "FILTER_FILE_HEADERS"):
						if reportid:
							ip_data["FILTER_FILE_HEADERS"] = ip_data["FILTER_FILE_HEADERS"] +'_'+ reportid + ".xml"
						else:
							ip_data["FILTER_FILE_HEADERS"] = ip_data["FILTER_FILE_HEADERS"] + ".xml"
					outputlist.append( getdata(ip_data, "FILTER_FILE_HEADERS") )
			if 'l' in val:
				print ""
				if getdata( ip_data, "REPORT_FILE_LIBRARIES"):
					reportlist.append( ip_data["REPORT_FILE_LIBRARIES"] )
					if getdata( ip_data, "FILTER_FILE_LIBRARIES"):
						if reportid:
							ip_data["FILTER_FILE_LIBRARIES"] = ip_data["FILTER_FILE_LIBRARIES"] +'_'+ reportid + ".xml"
						else:
							ip_data["FILTER_FILE_LIBRARIES"] = ip_data["FILTER_FILE_LIBRARIES"] + ".xml"
					outputlist.append( ip_data["FILTER_FILE_LIBRARIES"] )
			if 'f' in val:
				self.filterfiles=True
				rtmp = []
				otmp = []
				if getdata( ip_data, "REPORT_FILE_FILTER"):
					rtmp = ip_data["REPORT_FILE_FILTER"].split(';')
					for i in xrange(0, len(rtmp)):
						if reportid:
							rtmp[i] = rtmp[i] +'_'+ reportid + ".xml"
						else:
							rtmp[i] = rtmp[i] + ".xml"
					reportlist.extend( rtmp )
					if getdata( ip_data, "OUTPUT_FILE_FILTER"):
						otmp = ip_data["OUTPUT_FILE_FILTER"].split(';')
						for i in xrange(0, len(otmp)):
							if otmp[i] != "":
								if reportid:
									otmp[i] = otmp[i] +'_'+ reportid + ".xml"
								else:
									otmp[i] = otmp[i] + ".xml"
					outputlist.extend( otmp )
				
		for term in reportlist:
			if not os.path.exists( term ):
				raise InputError( ["confMP", "report file missing -- " + term, False] )
		self.args["REPORT_FILE"] = reportlist

		for term in outputlist:
			if '' != term:
				if not os.path.exists( os.path.dirname(term) ):
					os.makedirs( os.path.dirname(term) )
		self.args["OUTPUT_FILE"] = outputlist

		if not getdata( ip_data, "ISSUES_FILE"):
			issuelist.append(DEFAULT_ISSUES_FILE)
		else:
			issuelist = ip_data["ISSUES_FILE"].split(';')
		for term in issuelist:
			if term.startswith( SERVER_PATH ):									#server path used in issues set
				i = issuelist.index(term)
				localfile = ip_data["TEMP"] + os.sep + "tmpissues.xml"
				tmp = os.path.splitext(term)
				if '' == tmp[1]:
					term = getlatestfile( term )
				try:
					urllib.urlretrieve( term, localfile )
				except	IOError:
					raise InputError( ["confMPath", "Error in knownissues server path " + term + os.linesep, False] )
				f=open(localfile)
				if ((f.read()).find("404 Not Found") != -1):
					raise InputError( ["confMPath", "Error in knownissues server path " + term + os.linesep, False] )	
				f.close()
				issuelist[i] = localfile
				self.issueargs[localfile] = term
			elif os.path.exists( validate(term) ):
				issuelist[issuelist.index(term)] = validate(term)
			elif not os.path.exists( validate(term) ):
				raise InputError( ["confMP", "issues file " + term + " missing" + os.linesep, False] )
		self.args["ISSUES_FILE"] = issuelist
		
	#'stringize' all params and invoke the tool
	def run(self):
		report = ''
		output = ''
		issue = ''
		num = 1
		for i in BCFILTER_SET:
			if not os.path.exists(i):
				print os.linesep + i + " does not exist. Please reinstall."
				sys.exit(1)
		total = len(self.args["REPORT_FILE"]) * len(self.args["ISSUES_FILE"])
		for i in range(0, len(self.args["REPORT_FILE"]), 1):
			for j in xrange(0, len(self.args["ISSUES_FILE"]), 1):
				report = self.args["REPORT_FILE"][i]
				if i < len(self.args["OUTPUT_FILE"]):
					output = self.args["OUTPUT_FILE"][i]
				else:
					output = ""
				issue = self.args["ISSUES_FILE"][j]
				str = " " + quote(report) + " " + quote(issue)
				if getdata( self.issueargs, issue):
					issue = self.issueargs[issue]		
				if self.filterfiles:
					dispstr =  "\nprocessing (" + `num` + "/" + `total` +") files >>\nReport File: " + report + "\nKnown Issues File: " + issue + "\nOutput File: "				
				else:
					dispstr =  "\nprocessing file >>\nReport File: " + report + "\nKnown Issues File: " + issue + "\nOutput File: "
				if not output:
					dispstr = dispstr + "None(inplace filtration)"
				else:	
					dispstr = dispstr + output
					str = str + " " + quote(output)
				print dispstr
				invokeTool(quote(BCFILTER_SET[0]), str)
				num = num + 1
				
#----------------------CONFIGURATION PARSER-------------------------------------------
class ConfigParser:
	reportstr = ''
	data = {}
	#read the config values into a data structure
	def __init__(self, cmd):
		self.data = {"BASELINE_NAME":'', "BASELINE_SDK_DIR":'', "BASELINE_SDK_S60_VERSION":'', "CURRENT_NAME":'', "CURRENT_SDK_DIR":'', "CURRENT_SDK_S60_VERSION":'', "TEMP":'', 
						"BASELINE_HEADERS":'', "CURRENT_HEADERS":'', "BASELINE_SYSTEMINCLUDEDIR":'', "CURRENT_SYSTEMINCLUDEDIR":'', "BASELINE_FORCED_HEADERS":'', "CURRENT_FORCED_HEADERS":'', "USE_PLATFORM_DATA":'', "RECURSIVE_HEADERS":'',
						"EXCLUDE_DIR_HEADERS":'', "REPLACE_HEADERS":'', "REPORT_FILE_HEADERS":'', "FILTER_FILE_HEADERS":'', "TOOLCHAIN":'', "TOOLCHAIN_PATH":'', "BASELINE_BUILDTARGET":'',
						"CURRENT_BUILDTARGET":'', "BASELINE_IMPORTLIBRARIES":'', "CURRENT_IMPORTLIBRARIES":'', "BASELINE_IMPORTDLLS":'', "CURRENT_IMPORTDLLS":'',
						"REPORT_FILE_LIBRARIES":'', "FILTER_FILE_LIBRARIES":'', "REPORT_FILE_FILTER":'',"OUTPUT_FILE_FILTER":'', "ISSUES_FILE":'', "EXCLUDE_DIR":''}
		file = open(cmd.conffile)
		for input in file:
			if not input.startswith("#") and input != '\n':			# process non-comment lines
				pair = input.split('=')
				if len(pair) == 1:
					pair.append("")
				if not self.data.has_key(clean(pair[0])):
					raise InputError(["confIP", clean(pair[0]) + " is not valid" + os.linesep, False])
				self.data[clean(pair[0])] = clean(pair[1])
		file.close()
		if self.data.has_key("EXCLUDE_DIR"):
			if getdata( self.data,"EXCLUDE_DIR"):
				self.data["EXCLUDE_DIR_HEADERS"] = self.data["EXCLUDE_DIR"]
			del self.data["EXCLUDE_DIR"]
		self.reportstr = cmd.reportstr

	#updates the report path/file names, creates dirs if they do not exist
	def __updatereport(self,key):
		tmp = []
		if getdata( self.data, key ):
		#if field is empty raise an error		
			for term in self.data[key].split(';'):
				if '' != term:
					[head, tail] = os.path.split(term)
					try:
						term = validate(head) + os.sep + os.path.splitext(tail)[0]
					except InputError, e:
						os.makedirs(os.path.abspath(head))
					tmp.append(term)
				else:
					raise InputError(["confIS", key + " syntax incorrect" + os.linesep, False])
		#assign the updated report file str back to dict			
			self.data[key] = ';'.join([ "%s" % i for i in tmp])
		
	#updates the filter output path/file names, creates dirs if they do not exist
	def __updateoutput(self, key):
		tmp = []
		if getdata( self.data, key ):
		#if field is empty raise an error		
			for term in self.data[key].split(';'):
				if '' != term:
					[head, tail] = os.path.split(term)
					try:
						term = validate(head) + os.sep + os.path.splitext(tail)[0]
					except InputError, e:
						os.makedirs(os.path.abspath(head))
				tmp.append(term)
		#assign the updated report file str back to dict			
			self.data[key] = ';'.join(["%s" % i for i in tmp])
		
	#update necessary values
	def parse(self, cmds):
		for val in cmds:
			if 'h' in val:
				self.__updatereport("REPORT_FILE_HEADERS")
				self.__updateoutput("FILTER_FILE_HEADERS")
			if 'l' in val:
				self.__updatereport("REPORT_FILE_LIBRARIES")
				self.__updateoutput("FILTER_FILE_LIBRARIES")
			if 'f' in val:
				self.__updatereport("REPORT_FILE_FILTER")
				self.__updateoutput("OUTPUT_FILE_FILTER")
				
		if not getdata( self.data, "TEMP"):
			self.data["TEMP"] = TOOL_DIR + "temp"
		if not os.path.exists(self.data["TEMP"]):
			os.makedirs(self.data["TEMP"])	
		return self.data			

#-------------------------Utility Analysis-------------------------------------------
class CmdLineParser:
	#these are data interfaces that are exposed
	cmd = []
	conffile = ''
	reportstr = ''
	def __init__(self, argv):
		self.__check_help(argv)
		self.__check_carbide(argv)								
		self.__check_versions(argv)
		self.__parsecmd(argv)

	#create a list of commands to be executed
	def __addcmd(self, str):
		if (not 0 < len(str) < 3) or (len(str) == 1 and str != 'f'):
			raise InputError(["cmdlineIP", "Parameter -" + str + " incorrect" + os.linesep, True])
		if (str[0] in ['h', 'l']) and (str[1] in ['s', 'm', 'a']):
			self.cmd.insert(0, [ str[0], str[1] ])
		elif str[0] in ['f']:
			self.cmd.append([ str[0], "" ])
		else:
			raise InputError(["cmdlineIP", "Parameter - " + str + " incorrect" + os.linesep, True])
			
	#check if input parameter is a 'help' command
	def __check_help(self, argv):
		for term in ["-?", "--help", "-h", "/h", "/?"]:
			if term in argv:					            
				raise InputError( ["cmdhelp", "", True] )
	
	#check if any tool version inputs are required
	def __check_versions(self, argv):
		errstr = "Cannot combine version parameter with others"+ os.linesep +"Please reissue command without '-v'"
		if "-v" in argv:									
			sys.stdout.write( gettoolversion() )
			if len(argv) > 1:
				raise InputError(["cmdlineIP", errstr, False] )
			sys.exit(0)	
		if "-dv" in argv:									
			sys.stdout.write( getdataversion() )
			if len(argv) > 1:
				raise InputError(["cmdlineIP", errstr, False] )
			sys.exit(0)	

	#check if interface has been invoked from carbide	
	def __check_carbide(self, argv):
		global CARBIDE_PLUGIN
		if "-c" in argv:
			CARBIDE_PLUGIN = True
			argv.remove("-c")

	#check if input is proper else raise exception
	def __parsecmd(self, argv):
		#if no parameters present
		if not len(argv):
			raise InputError(["cmdlineMP", "None specified" + os.linesep, True])
		#check if config file exists
		if os.path.exists( os.path.abspath(argv[0]) ):
			self.conffile = os.path.abspath(argv[0])
			argv.pop(0)
		else:	
			raise InputError(["cmdlineMP", "Config file not found" + os.linesep, True])
		#parse the -xx type inputs, add them to command string
		for term in argv:
			if term[0] == "-":
				self.__addcmd(term[1:])

		#append additional args to cmd string
		i = 0
		for val in self.cmd:
			param = "-" + val[0] + val[1]
		#filter and 'a'- all option do not need any additional input	
			if ('f' != val[0]) and ('a' != val[1]):
				nxtterm = argv[ argv.index(param) + 1 ]
		#if next term is not an option string, append it as filename, to the command		
				if nxtterm[0] != "-":
					self.cmd[i].append(nxtterm)
					argv.remove(nxtterm)
				else:
					raise InputError(["cmdlineMP", "Input error -" + self.cmd[i][0] + self.cmd[i][1] + os.linesep, True])
			argv.remove(param)
			i = i + 1

		#if any additional param are defined, it is the report string
		if len(argv) == 1:
			self.reportstr = os.path.splitext(argv[0])[0]
		#if more then 1 additonal param, then raise error condition	
		elif len(argv) > 1:
			raise InputError(["cmdlineIP", "Error in input" + os.linesep, True])
		if not len(self.cmd):
			raise InputError(["cmdlineMP", "No command specified" + os.linesep, True])
										
#-------------------------Utility functions-------------------------------------------
#cleans the whitespace and trailing '\n', '\t'
def clean(str):
	return str.strip('\r\n\t ')

#used to wrap a string argument provided to checkbc in quotes
def quote(str):
	return "\"" + str + "\""

#validate the path provided
def validate(str):
	if not os.path.exists(str):
		tmp = os.path.abspath(str)
		if not os.path.exists(tmp):
			raise InputError(["confMPath", "Field "+ str +" is not valid" + os.linesep,False])
		str = tmp
	return os.path.normpath(os.path.abspath(str))

#used to wrap a string argument provided to checkbc in quotes
def quotep(str):
	return quote(validate(str))
	
#return an unquoted version of input string
def unquote(str):
	if str[0] == '\"':
		str = str[1:]
	if str[len(str)-1] == '\"':
		str = str[:len(str)-1]
	return str	
	
def getdata( mydict, key):
	if mydict.has_key(key):
		if '' != mydict[key]:
			return mydict[key]
	return ''

#return default build target from Rnd SDK and "" from Public SDK
def GetBuildTarget(sdk,dlldata,temp_path):
	bldtarget = ""
	path = ""
	xmlFile = open (dlldata);
	dllcases = xmlFile.readlines()
	xmlFile.close()

	dll_file = temp_path + os.sep + "dll.txt"
	dir_err_file = temp_path + os.sep + "dir_err.txt"	
	for target in s60_build_targets:
		if sdk == os.sep:
			path = quote(os.sep+'epoc32'+os.sep+'release'+os.sep+target+os.sep+'urel'+os.sep)
		else:
			path = quote(sdk+os.sep+'epoc32'+os.sep+'release'+os.sep+target+os.sep+'urel'+os.sep)
		if not os.path.exists(path):
			pass
		
		if os.name =='nt':
			cmd = "dir /b " + path + "*.dll > " + quote(dll_file) + " 2> " + quote (dir_err_file)
		else:
			cmd = "ls --format=single-column " + path + "*.dll > " + quote(dll_file) + " 2> " + quote( dir_err_file)
	
		os.system(cmd)
	
		file = open (dll_file)
		cases = file.readlines()
		file.close()
		os.remove(dll_file)
		os.remove(dir_err_file)
	
		matchFound = 0
		#Get no of dll present in xml file, 2 lines are for </dll_list> tag and each dll has 8 fileds.
		dllCount = 0
		for dll in dllcases:
			temp = dll.find ('<dllname>')
			if temp != -1 :
				dllCount = dllCount + 1
				exe = dll.lower().split('<dllname>')[1]
				exe = exe.split('</dllname>')[0]
				for en in cases:
					if (en.lower().find (exe.lower()) != -1) :
						matchFound = matchFound + 1
		
		if(matchFound > (dllCount /2) ):
			bldtarget = target
			break
			
	return bldtarget
			
#---------------------------Other funcs---------------------------------------------	
def getdataversion():
	return DATA_VERSION

def gettoolversion():
	return TOOL_VERSION + ' - ' + TOOL_DATE

#check the metadata file in server path and retrieve the latest file mentioned there
def getlatestfile( path ):
	if not path.endswith('/'):
		path = path + '/'
	try:
		file = urllib.urlopen( path + "knownissues_metadata" )
	except IOError:
		return path
	else:	
		knfile = (file.readline()).strip(' \t\n\r')
		if (knfile.find("DOCTYPE") != -1):
			return path
		return path + knfile
			
#all arguments have been assembled, now call the executable
#print process-id and exit-code only if invoked form carbide client
def invokeTool(exe, args):
	os.chdir(EXEC_PATH)
	val= True
	print os.linesep
	print exe+args
	if os.name == 'nt':
		val = False
	process = subprocess.Popen(exe+args, shell=val)	
	if CARBIDE_PLUGIN:
		sys.stdout.write("PID:")
		print process.pid
	sys.stdout.flush()
#wait for current child to complete before executing next one	
	exitcode = process.wait()
	if CARBIDE_PLUGIN:
		sys.stdout.write(os.linesep+ os.path.splitext((os.path.split(exe)[1]))[0] +" exitCode:")
		print exitcode
	os.chdir(TOOL_DIR)

#main function which performs the dispatch logic
def main(argv):
	try:
		cmd = CmdLineParser(argv)						#parse command line input
	except InputError, e:
		ExHandler(e)
	os.chdir(TOOL_DIR)									#change to checkbc path, to parse relative paths	
	try:	
		cfg = ConfigParser(cmd)							#takes the commandline parsed input
		args = cfg.parse(cmd.cmd)						#create key-value pair of inputs form the config file
	except InputError, e:
		ExHandler(e)	

	isFilterTrue = False
	if 'f' in cmd.cmd[len(cmd.cmd)-1]:
		isFilterTrue = True
		Fcmd = []
		Fcmd.append(cmd.cmd[len(cmd.cmd)-1])
		try:
			r = ReportFilter(args, Fcmd, cmd.reportstr)
		except InputError, e:
			ExHandler(e)
		r.run()
		
	for val in cmd.cmd:
		if 'h' in val:									#verify headers
			try:
				h = HeaderAnalyser(args, val[1:], cmd.reportstr)
			except InputError, e:
				ExHandler(e)
			h.run()
			if isFilterTrue:
				HFcmd = []
				HFcmd.append(val)
				try:
					r = ReportFilter(args, HFcmd, cmd.reportstr)
				except InputError, e:
					ExHandler(e)
				r.run()
		
		if 'l' in val:									#verify libraries
			try:			
				o = LibraryAnalyser(args, val[1:], cmd.reportstr)
			except InputError, e:
				ExHandler(e)
			o.run()			
			if isFilterTrue:
				LFcmd = []
				LFcmd.append(val)
				try:
					r = ReportFilter(args, LFcmd, cmd.reportstr)
				except InputError, e:
					ExHandler(e)
				r.run()
				
						
					
if __name__=="__main__":
	#Check for proper python version and then continue execution
	if not "2.4" <= platform.python_version() < "3.0":
		python_error()
	main(sys.argv[1:])
	