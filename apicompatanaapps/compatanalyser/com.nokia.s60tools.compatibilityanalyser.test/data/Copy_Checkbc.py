
import sys
import os

#Currently hardcoded values, these will be moved to a metadata file later
DATA_VERSION = "1"
TOOL_VERSION = "0.0.0"

#create the \data path which contains the necessary additional headers
DATA_PATH = os.getcwd() + os.altsep + "data" + os.altsep

#dictionary elements which hold the platform data and forced header information
if os.path.exists( DATA_PATH ):
	platformdata = {
	"3.0": DATA_PATH + "s60_platform_data_30.xml",
	"3.1": DATA_PATH + "s60_platform_data_31.xml",
	"3.2": DATA_PATH + "s60_platform_data_32.xml",
	"5.0": DATA_PATH + "s60_platform_data_50.xml"
	}
	
	forcedheaders = {
	"3.0": DATA_PATH + "forced_9.1.h",
	"3.1": DATA_PATH + "forced_9.2.h",
	"3.2": DATA_PATH + "forced_9.3.h",
	"5.0": DATA_PATH + "forced_9.4.h",
	"5.0v2": DATA_PATH + "forced_9.4v2.h"
	}

#this is the set of possible error values, stored as a dictionary, with the "value" represnting error message
InvalidParam = {
	"excess": [ "Invalid input: unknown parameters input" , 1 ],
	"insufficient": [ "Invalid input: insufficient parameters" , 2 ],
	"indecisive": [ "Invalid input: indeterminate parameter set" , 3 ],
	"syntax": [ "Invalid input: syntax error in input" , 4 ],
	"config": [ "Parameter Error: config file not found", 5 ],
	"toolchain": [ "Parameter Error: Invalid TOOLCHAIN or PATH defined in config file", 6 ],
	"baseline": [ "Parameter Error: Invalid BASELINE_xx parameters defined in config file", 7 ],
	"current": [ "Parameter Error: Invalid CURRENT_xx parameters defined in config file", 8 ],
	"platformdata": [ "Parameter Error: Platform data not available in defined path", 9 ],
	"knownissues": [ "Parameter Error: KnownIssues.xml not found in defined path", 10 ],
	"other": [ "General Error: Please recheck the tool inputs", 11 ]
	}

#this is the set of parameters supported by the tool
paramset = [ "-h", "-l", "-s", "-m", "-a", "-f", "-v", "-dv" ]

#global dictionary and argument list which holds the parameters
plist = {}
args = []

#an exception class, need to update this for better error representation
#value --> holds the error string
#use --> holds whether to print the usage info **can refactor this for more func**
class InputError(Exception):
	def __init__(self, value):
		self.value, self.use = ["other", False]
		if value in InvalidParam:
			self.value = value
		if value in ["excess", "insufficient", "indecisive", "syntax", "other"]:
			self.use = True

#the exception handler class which prints out the error message and usage info when required
class ExHandler():
	def __init__(self, e):
		print InvalidParam[e.value][0]
		if e.use:
			usage()
		sys.exit(InvalidParam[e.value][1])	

#displays the usage characteristics for the interface when command is invoked without proper arguments
def usage():
	print "\nCheckBC <config-file> -l/-h -s/-m/-a [file] [-f] [-v] [-d] <report-file>\n"
	print "config-file: filename of configuration file. Mandatory"
	print "-l: check libraries"
	print "-h: check headers"
	print "-s file: check a single file (file = header/library)"
	print "-m file: check multiple files (file = file with list of headers/libraries)"
	print "-a: check all files (*.h or *.lib or *.dso)"
	print "-f: Filter results after analysis. Optional"
	print "-v: version information of Core Tools and CheckBC interface. Optional"
	print "-d: A more descriptive output that offers further log info to console. Optional"
	print "report-file: report filename ends in .xml(folder name can be specified here or in config-file). Mandatory\n"
		
def getdataversion():
	return DATA_VERSION

def gettoolversion():
	return TOOL_VERSION

#check if input is proper else raise exception
def checkfidelity(alist):
	status = [ False, False]								#used to trace path of execution, and availability of all req params
	findex = -1
	if len(alist) == 0 or len(alist) > 10:					#check for the 'number' of inputs
		raise InputError("excess")
	if not os.path.exists( os.path.abspath(alist[0]) ):		#check if config file exists
		raise InputError("config")
	if "-h" in alist:										#is "-h" specified
		status[0] = True
	if "-l" in alist:										#is "-l" specified
		if status[0]:
			raise InputError("indecisive")					#error if both "-l" and "-h" are present
		status[0] = True
	if "-s" in alist:										#is "-s" used
		status[1] = True
		findex = alist.index("-s")
		if len(alist) >= findex+2:						
			if alist[ findex+1 ] in paramset:				#is "file" option specified correctly
				raise InputError("syntax")
	if "-m" in alist:										#is "-m" specified
		if status[1]:
			raise InputError("syntax")
		status[1] = True
		findex = alist.index("-m")
		if len(alist) >= findex+2:
			if alist[ findex+1 ] in paramset:
				raise InputError("syntax")
			if not os.path.exists( os.path.abspath( alist[findex+1] ) ):
				raise InputError("syntax")
	if "-a" in alist:										#is "-a" specified
		if status[1]:
			raise InputError("syntax")
		status[1] = True
	if not status[0] and not status[1]:
		raise InputError("insufficient")
	for entry in alist[1:len(alist)-1]:
		if entry not in paramset and entry != alist[findex+1]:
			raise InputError("insufficient")
		
#cleansthe whitespace and trailing '\n'
def clean(str):
	return (str.lstrip()).rstrip('\n ')

#read the config file and add te entries to a dictionary
def getinputfromconfigfile(configfile, report):
	file = open(configfile)
	for input in file:
		if input[:2] != "//" and input not in ('\n', '\r\n', '\r'):	# process non-comment lines
			pair = input.split('=')
			plist[clean(pair[0])] = clean(pair[1]).lower()
	file.close()											#update necessary values
	path = plist["REPORT_PATH"]
	if not path.endswith(os.altsep):
		path += os.altsep
	plist["REPORT_PATH"] = os.path.normpath( path + report )
	verifyhrh("BASELINE")									#check for the existence of Symbian hrh variant
	verifyhrh("CURRENT")

def verifyhrh(str):
	if plist[str+"_VER"] == "5.0":
		dirs = []
		dirs.extend( plist[str+"_DIR"].split(os.pathsep) )
		dirs.extend( plist[str+"_INCLUDE"].split(os.pathsep) )
		for path in dirs:
			checkpath = os.path.normpath( path + os.altsep + "variant" + os.altsep + "symbian_os_v9.4.hrh" )
			if not os.path.exists( checkpath ):
				plist[str+"_VER"] = "5.0v2"
	
#resolve which of -s, -m, -a exist
def resolve(alist):
	str = ''
	if "-s" in alist:										#a single component under test
		i = alist.index("-s")
		str = alist.pop(i+1)
		alist.remove("-s")
		if '-l' in alist:
			f = open( plist["TEMP"] + os.altsep + "oc.txt", "w" )
			f.write(str)
			f.close()
			str = plist["TEMP"] + os.altsep + "oc.txt"
	if "-m" in alist:										#a subset of components under test
		i = alist.index("-m")
		path = os.path.abspath(alist.pop(i+1))
		try:
			f = open(path)
		except(IOError):
			print "Cannot open input file"
			sys.exit(1)
		if "-l" in alist:
			str = path
		if "-h" in alist:	
			for file in f:
				str += os.pathsep + file
		alist.remove("-m")
	if "-a" in alist:										#all components in the directory under test
		alist.remove("-a")
		if "-l" in alist:
			f = open( plist["TEMP"] + os.altsep + "oc.txt", "w" )
			f.write("*.dso")
			f.close()
			str = plist["TEMP"] + os.altsep + "oc.txt"
		str = '*.h;*.hrh;*.mbg;*.rsg'
	global args
	args.extend(["-set", str])								#append the "-set" parameter
		
def assembleHAargs():										#create the set of arguments for HA
	args.extend(["-baselinedir", plist["BASELINE_DIR"]])
	args.extend(["-currentdir", plist["CURRENT_DIR"]])
	args.extend(["-baselineversion", plist["BASELINE_NAME"]])
	args.extend(["-currentversion", plist["CURRENT_NAME"]])
	args.extend(["-reportfile", plist["REPORT_PATH"]])
	args.extend(["-baseplatformheaders", plist["BASELINE_DIR"] + os.pathsep + plist["BASELINE_INCLUDE"]])
	args.extend(["-currentplatformheaders", plist["CURRENT_DIR"] + os.pathsep + plist["CURRENT_INCLUDE"]])
	args.extend(["-forcebaseinclude", forcedheaders[plist["BASELINE_VER"]]])
	args.extend(["-forcecurrentinclude", forcedheaders[plist["CURRENT_VER"]]])
	args.extend(["-bundlesize", str(50) ])
	args.extend(["-temp", plist["TEMP"]])
	if plist["USE_PLATFORM_DATA"] is "true":
		args.extend(["-baseplatformdata", platformdata[plist["BASELINE_VER"]]])
		args.extend(["-currentplatformdata", platformdata[plist["CURRENT_VER"]]])
	if plist["RECURSIVE"] == "true":
		args.append("-recursive")
	if plist["EXCLUDE_DIR"]:
		args.extend(["-excludedirs", plist["EXCLUDE_DIR"]])
	if plist["REPLACE"]:
		tlist = plist["REPLACE"].split(os.pathsep)
		args.append("-replace")
		for entry in tlist:
			args.extend(entry.split(':'))
		
def assembleOCargs():									#create the set of arguments for OC
	args.extend(["-baselinedir", plist["BASELINE_DIR"]])
	args.extend(["-currentdir", plist["CURRENT_DIR"]])
	args.extend(["-baselineversion", plist["BASELINE_NAME"]])
	args.extend(["-currentversion", plist["CURRENT_NAME"]])
	args.extend(["-reportfile", plist["REPORT_PATH"]])
	args.extend(["-tools", plist["TOOLCHAIN_PATH"]])
	args.extend(["-temp", plist["TEMP"]])
	args.insert(1, plist["TOOLCHAIN"])					# GCC/GCCE/RVCT
	if plist["TOOLCHAIN"] is "rvct":
		args.extend(["-cfilt", os.path.curdir + os.altsep + "bin" + os.altsep + "cfilt.exe" ])
			
#all arguments have been assembled, now call the executable
def invokeTool():
	global args
	if not os.path.exists(args[0]):
		raise OSError
	print args
	args.insert(1,' ')									#some debugging required here
	os.execv(args[0], args[1:])

#main function which performs the dispatch logic
def main(argv):
	global args
	try:
		checkfidelity(argv)								#check if input is proper else raise exception
	except InputError, e:
		ExHandler(e)
	if "-v" in argv:									#output the Core Tools version number
		print "Core Tool Package version: ",gettoolversion()
		argv.remove("-v")
	if "-dv" in argv:									#dataversion output
		print "Package Data version: ",getdataversion()
		argv.remove("-dv")
	if "-u" in argv:									#update coretools; currently not implemented
		pass
	if not len(argv):									#exit if no more arguments
		sys.exit(0)
	config = os.path.abspath(argv.pop(0))				#argv[0] is config-file
	if os.path.exists(config):
		getinputfromconfigfile(config, argv.pop(len(argv)-1).lower())		
	for arg in argv:
		if arg == "-l":									#verify libraries
			args.insert( 0, os.path.curdir + os.altsep + "bin" + os.altsep + "oc.exe")
			assembleOCargs()
		elif arg == "-h":								#verify headers
			args.insert( 0, os.path.curdir + os.altsep + "bin" + os.altsep + "ha.exe")
			assembleHAargs()
	if "-l" in argv or "-h" in argv:
		resolve(argv)
		invokeTool()
	if "-f" in argv:									#filter with all knownissues interatively
		args = []
		args.insert( 0, os.path.curdir + os.altsep + "bin" + os.altsep + "bcfilter.exe")
		args[1] = plist["REPORT_PATH"]
		temp = plist["ISSUES_FILE"].split(os.pathsep)
		for argv[2] in temp:
			invokeTool()
				
if __name__=="__main__":
	main(sys.argv[1:])
	