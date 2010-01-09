<!-- 
- Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
- All rights reserved.
- This component and the accompanying materials are made available
- under the terms of the License "Symbian Foundation License v1.0"
- which accompanies this distribution, and is available
- at the URL "http://www.symbianfoundation.org/legal/sfl-v10.html".
- 
- Initial Contributors:
- Nokia Corporation - initial contribution.
- 
- Contributors:
- 
- Description:
- 
-->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">

<HTML xmlns:msie="urn:default">
<msie:download id="dler" style="behavior:url(#default#download)"/>

<xsl:variable name="reporttype">
  <xsl:choose>
    <xsl:when test="count(bbcresults/header/haversion) &gt; 0">HDR</xsl:when>
    <xsl:when test="count(bbcresults/header/laversion) &gt; 0">LIB</xsl:when>
    <xsl:otherwise>NON</xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<HEAD>
<TITLE>Binary Compatibility Report :: 
		<xsl:choose>
      <xsl:when test="$reporttype='HDR'">Headerfiles</xsl:when>
      <xsl:otherwise>Libraries</xsl:otherwise>
    </xsl:choose>
</TITLE>
  
<script type="text/javascript">

var diffFileName = new Array(2);
var diffFileContent = new Array(2);
var diffFileIndex;
var popup;
var hasFailed = false;
var diffTitle = "";
var diffFileName1;
var diffFileName2;

var hrefLoaction;
hrefLoaction = window.location.href;

function switchMenu(obj) {
	if (document.getElementById)
	{
		var el = document.getElementById(obj);
		if (el)
		{
				if ( el.style.display != "none" ) {
				   el.style.display = 'none';
				   document.getElementById("cl").innerHTML = "View OK Issues";
			   	}
				else {
				   el.style.display = 'inline';
				   document.getElementById("cl").innerHTML = "Hide OK Issues";
				}
		}
	}
}	
function switchBacklink(obj)
{
if (document.getElementById)
	{
		var el = document.getElementById(obj);
		if (el)
		{				
				   el.style.display = 'none';
				   window.location.href = hrefLoaction;
		}
	}
}


function openPath( file )
{	
	var thefile = file;
	thefile = file.replace( /\\/g, "/" );
	if ( thefile != null )
	{
	  pos1=thefile.lastIndexOf('/');	  
	  thefile=thefile.substring(0,pos1);
	}
	if ( thefile != null )
		window.location = "file://" + thefile;
}

function showFile( file1, file2 )
{	
	var thefile = null;
  
	origbasedir = document.getElementById( "origbaselinedir" ).value.replace( /\\/g, "/" ).replace( /\*\.h/g, "" ).replace(/\/s*$/, "");
	newbasedir = document.getElementById( "baselinedir" ).firstChild.data.replace( /\\/g, "/" ).replace(/\/s*$/, "");
	origcurrdir = document.getElementById( "origcurrentdir" ).value.replace( /\\/g, "/" ).replace(/\/s*$/, "");;
	newcurrdir = document.getElementById( "currentdir" ).firstChild.data.replace( /\\/g, "/" ).replace(/\/s*$/, "");;
	
	if ( file1 != null )
		thefile = file1.replace( origbasedir, newbasedir );
	else if ( file2 != null )
		thefile = file2.replace( origcurrdir, newcurrdir );
	if ( thefile != null )
		window.location = "file://" + thefile;
}

function showDiff( title, file1, file2 )
{

	diffTitle = "diff : " + title;
	origbasedir = document.getElementById( "origbaselinedir" ).value.replace( /\\/g, "/" ).replace( /\*\.h/g, "" ).replace(/\/s*$/, "");
	newbasedir = document.getElementById( "baselinedir" ).firstChild.data.replace( /\\/g, "/" ).replace(/\/s*$/, "");
	origcurrdir = document.getElementById( "origcurrentdir" ).value.replace( /\\/g, "/" ).replace(/\/s*$/, "");;
	newcurrdir = document.getElementById( "currentdir" ).firstChild.data.replace( /\\/g, "/" ).replace(/\/s*$/, "");;

	file1 = diffFileName1 = file1.replace( origbasedir, newbasedir );
	file2 = diffFileName2 = file2.replace( origcurrdir, newcurrdir );

	diffFileName[0] = file1; diffFileName[1] = file2;
	diffFileContent[0] = ""; diffFileContent[1] = "";
	diffFileIndex = 0;
	if ( typeof(dler) != "undefined" &amp;&amp; typeof(dler.startDownload) != "undefined" ) ieLoad( "" );
	else ffLoad();
	window.setTimeout( "loadFailed()", 1500 );	
}

function loadFailed()
{
	if ( hasFailed ) return;
	else hasFailed = true;
	if ( diffFileContent[0] == "" ) alert( "Could not load file: " + diffFileName[0] );
	else if ( diffFileContent[1] == "" ) alert( "Could not load file: " + diffFileName[1] );
}

function ffLoad()
{
	var req = new XMLHttpRequest(); 
	req.onreadystatechange = function() { 
		if (req.readyState == 4 ) {
			diffFileContent[diffFileIndex++] = req.responseText;
			if ( diffFileIndex == 1 ) ffLoad();
			else diffFilesLoaded();
		}
	};
	req.open( "GET", "file:///" + diffFileName[diffFileIndex], true ); 
	req.send( null ); 
} 

function ieLoad( text )
{
	if ( diffFileIndex &gt; 0 ) {
		if ( text == "" ) {
			loadFailed();
			return;
		}
		diffFileContent[diffFileIndex-1] = text;
	}
	++diffFileIndex;
	if ( diffFileIndex &lt;= 2 ) dler.startDownload( diffFileName[diffFileIndex-1], ieLoad );
	else diffFilesLoaded();
}

function setProgress( percentage )
{
	var prog1 = Math.max( 1, Math.round( percentage * 1.5 ) );
	var prog2 = 150 - prog1;
}
function SetFocus(element)
{
window.scrollTo(0,0);
var tmp=document.getElementById(element);
if(tmp !=null)
tmp.focus();
return true;
} 

function diffFilesLoaded()
{
	var w = window.screen.availWidth - 50;
	var h = window.screen.availHeight - 150;
	popup = window.open( "", "_self");
	popup.document.title = diffTitle;
	popup.document.body.style.margin = 0;

	var oldcontent = diffFileContent[0];
	var newcontent = diffFileContent[1];

	var oldlines = oldcontent.replace(/\r/g, "").split("\n");
	var newlines = newcontent.replace(/\r/g, "").split("\n");

	res = diff( oldlines, newlines );
	var matching = res.matching;
	var matchtype = res.matchtype;
	var output = "&lt;table width='100%' cellspacing='0' cellpadding='1' border='0' style='font-family: Courier; font-size=1;'&gt;";

	var lastA = -1;
	var lastB = -1;
	for ( i = 0; i &lt;= matching.length; i++ ) {
		if ( i == matching.length || matching[i] != -1 ) {
			// all lines ]lastA..i[ --&gt; removed
			for ( j = lastA + 1; j &lt; i; ++j ) {
				output += "&lt;tr&gt;&lt;td bgColor='#ff9080'&gt;" + repfunc( oldlines[j] ) + "&lt;/td&gt;&lt;td width='1' bgColor='black'&gt;&lt;/td&gt;" +
						  "&lt;td bgColor='#e0e0e0'&gt;" + repfunc( "" ) + "&lt;/td&gt;&lt;/tr&gt;";
			}

			// all lines ]lastB..matching[i][ --&gt; inserted
			if ( i &lt; matching.length )
				untilB = matching[i];
			else
				untilB = newlines.length;
			for ( j = lastB + 1; j &lt; untilB; ++j ) {
				output += "&lt;tr&gt;&lt;td bgColor='#e0e0e0'&gt;" + repfunc( "" ) + "&lt;/td&gt;&lt;td width='1' bgColor='black'&gt;&lt;/td&gt;" +
						  "&lt;td bgColor='#a0e0a0'&gt;" + repfunc( newlines[j] ) + "&lt;/td&gt;&lt;/tr&gt;";
			}

			// matching line
			if ( i &lt; matching.length ) {
				if ( matchtype[i] == 0 ) {
					output += "&lt;tr&gt;&lt;td&gt;" + repfunc( oldlines[i] ) + "&lt;/td&gt;&lt;td width='1' bgColor='black'&gt;&lt;/td&gt;" +
							  "&lt;td&gt;" + repfunc( newlines[matching[i]] ) + "&lt;/td&gt;&lt;/tr&gt;";
				}
				else {
					output += "&lt;tr&gt;&lt;td bgColor='#ffff80'&gt;" + repfunc( oldlines[i] ) + "&lt;/td&gt;&lt;td width='1' bgColor='black'&gt;&lt;/td&gt;" +
							  "&lt;td bgColor='#ffff80'&gt;" + repfunc( newlines[matching[i]] ) + "&lt;/td&gt;&lt;/tr&gt;";
				}
				lastA = i;
				lastB = matching[i];
			}
		}
	}
	output += "&lt;/table&gt;";

	var backlink = document.getElementById("backlink");
	if(backlink != null)
	backlink.innerHTML = "Close diff view";
	    
	var titleconent = document.getElementById("title");
	if(titleconent != null)
	titleconent.innerHTML = "Viewing diff between  " + diffFileName1 + "  and   " + diffFileName2;
	      
  var diffconent = document.getElementById("diff");
  if(diffconent != null)
	diffconent.innerHTML = output;
	     
  var backlink1 = document.getElementById("backlink1");
  if(backlink1 != null)
	backlink1.innerHTML = "Close diff view";  
	
	if(backlink != null)
	SetFocus(backlink);
	  
}

function repfunc( str )
{
	if ( str.length &gt; 70 )
		str = str.substring(0,69) + "..."
	else if ( str.replace(/\s+/g,"").length == 0 )
		str = "&amp;nbsp;";

	return "&lt;nobr&gt;" + str.replace(/&lt;/g, "&amp;lt;").replace(/&gt;/g, "&amp;gt;").replace(/\s/g, "&amp;nbsp;") + "&lt;/nobr&gt;";
}

function diff( o, n )
{
	var olen = o.length;
	var nlen = n.length;

	var crcTable = new Array( 256 );
	for ( i = 0; i &lt; 256; i++ ) {
		crc = i;
		for ( j = 8 ; j &gt; 0; j-- ) {
			if ( crc &amp; 1 ) crc = ( crc &gt;&gt;&gt; 1 ) ^ 0xEDB88320;
			else crc &gt;&gt;&gt;= 1;
		}
		crcTable[i] = crc;
	}

	var aHash = new Array( olen );
	for ( i = 0; i &lt; olen; ++i ) {
		str = o[i].replace(/^\s+/, "").replace(/\s+$/, "");
		crc = 0xFFFFFFFF;
		for ( k = 0; k &lt; str.length; ++k )
			crc = crcTable[ ( crc &amp; 0xff ) ^ str.charCodeAt(k) ] ^ ( crc &gt;&gt;&gt; 8 );
		aHash[i] = crc ^ 0xFFFFFFFF;
	}

	var bHash = new Array( nlen );
	for ( i = 0; i &lt; nlen; ++i ) {
		str = n[i].replace(/^\s+/, "").replace(/\s+$/, "");
		crc = 0xFFFFFFFF;
		for ( k = 0; k &lt; str.length; ++k )
			crc = crcTable[ ( crc &amp; 0xff ) ^ str.charCodeAt(k) ] ^ ( crc &gt;&gt;&gt; 8 );
		bHash[i] = crc ^ 0xFFFFFFFF;
	}

	matches = lcs( aHash, bHash, 0 ).matching;

	for ( i = 0; i &lt; olen; ++i )
		if ( matches[i] != -1 &amp;&amp; o[i].replace(/^\s+/, "").replace(/\s+$/, "") != n[matches[i]].replace(/^\s+/, "").replace(/\s+$/, "") )
			matches[i] = -1;

	matchtype = new Array ( olen );
	for ( i = 0; i &lt; olen; ++i )
		matchtype[i] = 0;

	olast = -1;
	nlast = -1;
	for ( oline = 0; oline &lt;= olen; ++oline ) {
		setProgress( 75 + ( oline + 1 ) * 25 / ( olen + 1 ) );
		if ( oline == olen || matches[oline] != -1 ) {
			if ( oline &lt; olen )
				nline = matches[oline];
			else
				nline = nlen;
			for ( ocurrent = olast + 1; ocurrent &lt; oline; ++ocurrent ) {
				for ( ncurrent = nlast + 1; ncurrent &lt; nline; ++ncurrent ) {
					str1 = o[ocurrent];
					str2 = n[ncurrent];
					if ( ( pos = str1.lastIndexOf( "//" ) ) != -1 ) str1 = str1.substring( 0, pos );
					if ( ( pos = str2.lastIndexOf( "//" ) ) != -1 ) str2 = str2.substring( 0, pos );
					str1 = str1.replace( /\s+/g, " " );
					str2 = str2.replace( /\s+/g, " " );
					if ( str1.length == 0 ) {
						if ( str2.replace(/\s/g, "").length == 0 )
							break;
						else
							continue;
					}
					else if ( str2.length == 0 ) {
						if ( str1.replace( /\s/g, "" ).length == 0 )
							break;
						else
							continue;
					}
					if ( str1.length &lt; str2.length / 3 || str2.length &lt; str1.length / 3 )
						continue;
					matchlen = lcs( str1, str2, 1 ).length;
					if ( matchlen &gt;= str1.length * 4 / 5 || matchlen &gt;= str2.length * 4 / 5 )
						break;
				}
				if ( ncurrent &lt; nline ) {
					matches[ocurrent] = ncurrent;
					matchtype[ocurrent] = 1;
					nlast = ncurrent;
				}
			}
			if ( oline &lt; olen ) {
				olast = oline;
				nlast = nline;
				matchtype[oline] = 0;
			}
		}
	}

	return { matching: matches, matchtype: matchtype };
}

function lcs( A, B, valueType )
{
	m = A.length;
	n = B.length;

	V = new Array( n );
	if ( valueType == 0 ) {
		for ( i = 0; i &lt; n; ++i )
			V[i] = { pos: i, value: B[i] };
	}
	else {
		for ( i = 0; i &lt; n; ++i )
			V[i] = { pos: i, value: B.charCodeAt(i) };
	}
	V.sort( sortByValue );

	E = new Array( n + 1 );
	E[0] = { pos: -1, last: true};
	for ( i = 0; i &lt; n; ++i )
		E[i+1] = { pos: V[i].pos, last: ( i == n-1 || V[i].value != V[i+1].value ) };

	P = new Array( m );
	for ( i = 0; i &lt; m; ++i ) {
		var idx;
		var val = ( valueType == 0 ? A[i] : A.charCodeAt(i) );
		for ( min = 0, max = n-1, idx = ( n - 1 ) &gt;&gt; 1; true; idx = ( min + max ) &gt;&gt; 1 ) {
			if ( val &lt; V[idx].value )
				max = idx - 1;
			else if ( val &gt; V[idx].value )
				min = idx + 1;
			else if ( !E[idx].last )
				max = idx - 1;
			else
				break;
			if ( min &gt; max )
				break;
		}
		if ( val == V[idx].value &amp;&amp; E[idx].last )
			P[i] = idx;
		else
			P[i] = -1;
	}

	K = new Array( Math.min( m, n ) + 2 );
	K[0] = new candidate( -1, -1, null );
	K[1] = new candidate( m, n, null );
	k = 0;
	for ( i = 0; i &lt; m; ++i ) {
		if ( valueType == 0 )
			setProgress( ( i + 1 ) * 75 / ( m + 1 ) );
		if ( P[i] != -1 ) {
			r = 0;
			c = K[0];
			p = P[i] + 1;
			var s;

			while ( true ) {
				j = E[p].pos;
				for ( min = r, max = k, s = ( r + k ) &gt;&gt; 1; true; s = ( min + max ) &gt;&gt; 1 ) {
					if ( j &lt; K[s].b )
						max = s - 1;
					else if ( j &gt; K[s + 1].b )
						min = s + 1;
					else
						break;
					if ( min &gt; max )
						break;
				}

				if ( j &gt; K[s].b &amp;&amp; j &lt; K[s + 1].b ) {
					temp = new candidate( i, j, K[s] );
					K[r] = c;
					c = temp;
					r = s + 1;
					if ( s == k ) {
						K[k + 2] = K[k + 1];
						++k;
						break;
					}
				}

				if ( E[p].last )
					break;
				++p;
			}

			K[r] = c;
		}
	}

	J = new Array( m );
	len = 0;
	for ( i = 0; i &lt; m; ++i )
		J[i] = -1;
	for ( c = K[k]; c.a != -1; c = c.previous, ++len ) {
		J[c.a] = c.b;
	}

	return { matching: J, length: len };
}

function candidate( a, b, previous )
{
  this.a = a;
  this.b = b;
  this.previous = previous;
}

function sortByValue( a, b )
{
  ret = a.value - b.value;
  return ( ret != 0 ? ret : a.pos - b.pos );
}

function pw() {return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth}
function mouseX(evt) {return evt.clientX ? evt.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft) : evt.pageX;}
function mouseY(evt) {return evt.clientY ? evt.clientY + (document.documentElement.scrollTop || document.body.scrollTop) : evt.pageY}
function popUp(evt,oi) 
{
	if (document.getElementById)
	{
		var wp = pw();
		dm = document.getElementById(oi);
		ds = dm.style; st = ds.visibility;
		if (dm.offsetWidth)
			ew = dm.offsetWidth;
		else if (dm.clip.width)
			ew = dm.clip.width;
		if (st == "visible" || st == "show")
		{
			ds.visibility = "hidden";
		}
		else
		{
			tv = mouseY(evt) - 40;
			lv = mouseX(evt) + (ew/8);
			if (2 > lv)
				lv = 2;
			else if (lv + ew > wp) lv -= ew/2;
			lv += 'px';
			tv += 'px'; 
			ds.left = lv;
			ds.top = tv;
			ds.visibility = "visible";
		}
	}
}

function getIssueTableIdx( aRepBlock )
{
	var len = aRepBlock.getElementsByTagName("table").length;
	if( len &gt; 0 ) {
		var aTable = aRepBlock.getElementsByTagName("table");
		for( i = 0; i &lt; len; i++) {
			if( aTable[i].id == "issueTable" ) {
				return i;
			}
		}
	}
	return -1;
}

function getSeverityColIdx(aVal, tdNodes)
{
	if( tdNodes.length &gt; 0 )
	{
		for( i = 0 ; i &lt; tdNodes.length; i++ ) {
			if( tdNodes[i].id == aVal ) {
			    return i;
			}
		}
	}
	return -1;
}

function showAllIssues( aRepBlock, n, tableIdx, x, y)
{
	for( i = 0; i &lt; n; i++ ) {
		// getting row that displays information
		aTr = aRepBlock[i].getElementsByTagName("table")[tableIdx].getElementsByTagName("tr");
		nTr = aTr.length;
		
		for( j = 0; j &lt; nTr ; j++ ) {
			aTr[j].style.display = ""; //unhide row
			aTr[j].childNodes[x].style.display = ""; //unhide BC column
			aTr[j].childNodes[y].style.display = ""; //unhide SC column
		}
		aRepBlock[i].style.display = ""; //unhide Report Block
	}
}

function hideIssues(aRepBlock, n, tableIdx, x, y)
{
	for( i = 0; i &lt; n; i++ ) {
		// getting row that displays information
		aTr = aRepBlock[i].getElementsByTagName("table")[tableIdx].getElementsByTagName("tr");
		nTr = aTr.length;
		
		// hiding header row
		aTr[0].childNodes[x].style.display = "";
		aTr[0].childNodes[y].style.display = "none";
		
		hideBlock = 1; // flag for hiding the report block
		
		for( j = 1; j &lt; nTr; j++ ) {
			// hiding row if "None"
			if( aTr[j].childNodes[x].innerHTML == "None" ) {
				aTr[j].style.display = "none";
			}
			else {
				// else hiding column only
				aTr[j].childNodes[x].style.display = "";
				aTr[j].childNodes[y].style.display = "none";
				aTr[j].style.display = "";		// unhiding row if it was hidden
				hideBlock = 0;
			}
		}
	
		if( hideBlock == 1 ) {
			aRepBlock[i].style.display = "none";		// hiding entire "Report Block"
		}
		else {
			aRepBlock[i].style.display = "";				// unhiding entire "Report Block"
		}
	}
}
		
function getRepBlock(){rb=document.getElementsByTagName("div");if(rb.length &gt; 0){var arr=new Array();i2=0;for(i1=0;i1 &lt;rb.length;i1++){if(rb[i1].id=="repBlock"){arr.push(rb[i1]);i2=1;}}if(i2==1){return arr;}else{return null;}}return null;}
	
function changeView( aVal ) {
	// get "Report Blocks" in array
	aRepBlock = getRepBlock();
	
	if(aRepBlock ) {
		if( aRepBlock.length > 0 ) {
		
			tableIdx = getIssueTableIdx(aRepBlock[0]);
			if( tableIdx == -1 ) {
				alert("Error: Table of issue details not found.\nPossibly change in HTML!");
				return;
			}
			
			// these values are set considering BC Break as default
			var bc = getSeverityColIdx("BCSeverity", aRepBlock[0].getElementsByTagName("table")[tableIdx].getElementsByTagName("tr")[0].childNodes),
			    sc = getSeverityColIdx("SCSeverity", aRepBlock[0].getElementsByTagName("table")[tableIdx].getElementsByTagName("tr")[0].childNodes);
			    
			if( bc == -1 || sc == -1 ) {
				alert("Error: BC/SC Severity column not found.\nPossibly change in HTML!");
				return;
			}
			
			if ( aVal == "All" ) {
			// if user selects All, show everything
				showAllIssues( aRepBlock, aRepBlock.length, tableIdx, bc, sc );
			}
			else {
				// code if user selects BC or SC
				
				if ( aVal == "SC Break" ) {
					hideIssues(aRepBlock, aRepBlock.length, tableIdx, sc, bc); // changing rows id if its a SC Break
				}
				else {
					hideIssues(aRepBlock, aRepBlock.length, tableIdx, bc, sc);
				}
			}
		}
	}
}
</script>

<style type="text/css">
                    div.tip {font:12px/14px
                    Arial,Helvetica,sans-serif; border:solid 1px
                    #666666; width:270px; padding:1px;
                    position:absolute; z-index:100;
                    visibility:hidden; color:#333333; top:20px;
                    left:90px; background-color:#ffffcc;
                    layer-background-color:#ffffcc;}
</style>
</HEAD>

	<BODY bgColor="#f8f8f8">
		<font size="4"><a id ="backlink" onclick="switchBacklink('dl');" href=""></a></font><BR/>
	    <ul id="dl" style="display:none">	
	      <a name="back"></a><font size="0" ></font>
	    </ul>
	    	          
	  <font size="4"><a id ="title"></a></font>
	    <ul id="dl" style="display:none">	
	      <a name="title"></a><font size="0" ></font>
	    </ul>
	          
	  <font size="2"><a id ="diff"></a></font>
	    <ul id="dl" style="display:none">	
	      <a name="diff"></a><font size="0" ></font>
	    </ul>	          
	          
    <font size="4"><a id ="backlink1" onclick="switchBacklink('dl');" href=""></a></font><BR/>
	    <ul id="dl" style="display:none">	
	      <a name="back"></a><font size="0" ></font>
	    </ul>
	
		<font size="5"><a id ="ReportInformation" > Report Information</a></font><BR/><HR/>
		<TABLE >
			<TR>
				<TD><i>type</i></TD>
				<TD>
		       <xsl:choose>
             <xsl:when test="$reporttype='HDR'">Headerfiles</xsl:when>
             <xsl:otherwise>Libraries</xsl:otherwise>
           </xsl:choose>
				</TD>
			</TR>
			<TR>
				<TD><i>creation date</i></TD>
				<TD>
					<xsl:value-of select="bbcresults/header/timestamp/year"/>-<xsl:value-of select="bbcresults/header/timestamp/month"/>-<xsl:value-of select="bbcresults/header/timestamp/day"/>
					   at <xsl:value-of select="bbcresults/header/timestamp/hour"/>:<xsl:value-of select="bbcresults/header/timestamp/minute"/>.<xsl:value-of select="bbcresults/header/timestamp/second"/>
				</TD>
			</TR>
			<TR>
				<TD><i>baseline version</i></TD>
				<TD><xsl:value-of select="bbcresults/header/baselineversion"/></TD>
			</TR>
			<TR>
				<TD><i>current version</i></TD>
				<TD><xsl:value-of select="bbcresults/header/currentversion"/></TD>
			</TR>
			<TR>
				<TD><i>known issues list</i></TD>
				<TD><xsl:value-of select="bbcresults/header/knownissuesversion"/></TD>
			</TR>
			<TR>
				<TD><i>known issue platform versions</i></TD>
				<xsl:for-each select="bbcresults/header/os">
				<TR><TD>-</TD><TD><xsl:value-of select="version"/>_<xsl:value-of select="buildweek"/></TD></TR>
				</xsl:for-each>
			</TR>
			<TR>
				<TD><i>software version</i></TD>
				<TD>
				  <xsl:if test="$reporttype='HDR'"><xsl:value-of select="bbcresults/header/haversion"/></xsl:if>
				  <xsl:if test="$reporttype='LIB'"><xsl:value-of select="bbcresults/header/laversion"/></xsl:if>
				</TD>
			</TR>
			<TR>
				<TD><i>report format version</i></TD>
				<TD><xsl:value-of select="bbcresults/header/formatversion"/></TD>
			</TR>
		</TABLE>

		<BR/><BR/>
		<font size="5">Command-Line Parameters</font><BR/><HR/>
		<TABLE>
			<xsl:for-each select="bbcresults/header/cmdlineparms/parm">
			<TR>
				<xsl:choose>
				  <xsl:when test="pname='baselinedir' and $reporttype = 'HDR'">
				    <TD align="left"><I><xsl:value-of select="pname"/></I></TD>
				    <TD id="baselinedir" align="left">      
				      <xsl:choose>
				        <xsl:when test="contains(pvalue,'*.h')">
				          <xsl:value-of select="substring(pvalue,0,string-length(pvalue)-3)"/>   
				        </xsl:when>
				        <xsl:otherwise>
				          <xsl:value-of select="pvalue"/>
				       </xsl:otherwise>
				     </xsl:choose>
				     </TD>
				    <input type="hidden" name="origbaselinedir" id="origbaselinedir">
				      <xsl:attribute name="value">
				        <xsl:value-of select="pvalue"/>
				      </xsl:attribute>
				    </input>
				  </xsl:when>
				  <xsl:when test="pname='currentdir' and $reporttype = 'HDR'">
				    <TD align="left"><I><xsl:value-of select="pname"/></I></TD>
				    <TD id="currentdir" align="left">  
				      <xsl:value-of select="pvalue"/>
				    </TD>
				    <input type="hidden" name="origcurrentdir" id="origcurrentdir">
				      <xsl:attribute name="value">
				        <xsl:value-of select="pvalue"/>
				      </xsl:attribute>
				    </input>
				  </xsl:when>
				  <xsl:when test="pname='warning'">
				    <TD></TD>
				    <TD align="left"><b><xsl:value-of select="pvalue"/></b></TD>
				  </xsl:when>  
				  <xsl:otherwise>
				    <TD align="left"><I><xsl:value-of select="pname"/></I></TD>
				    <TD align="left"><xsl:value-of select="pvalue"/></TD>
				  </xsl:otherwise>
		    </xsl:choose>
	    </TR>
	    </xsl:for-each>
    </TABLE>

		<BR/><BR/>
		<font size="5">Compatibility Issues</font><HR/>
		<table border="0"><tr><td><b>Issue Type</b></td>
			<td width="100"><select name="mydropdown" onchange="changeView(this.value)" align="right">
					<option value="All">All</option>
					<option value="BC Break">BC Break</option>
					<option value="SC Break">SC Break</option>
				</select>
			</td></tr></table>
			<HR/>
			<br/>

  <xsl:if test="$reporttype='LIB'">
    <xsl:if test="count(bbcresults/issuelist/library[(count(issue[status='NOK'and typeid!=2 and typeid!=7 and typeid!=14]) &gt; 0 or count(issue[count(status) = 0 and typeid!=2 and typeid!=7 and typeid!=14]) &gt; 0)]) = 0">
      <i>There are no issues.</i><BR/>
    </xsl:if>
		<xsl:for-each select="bbcresults/issuelist/library">
			<xsl:if test="(count(issue[status='NOK'and typeid!=2 and typeid!=7 and typeid!=14]) &gt; 0 or count(issue[count(status) = 0 and typeid!=2 and typeid!=7 and typeid!=14]) &gt; 0) ">
			<div id="repBlock">
			 <table border="0" cellpadding="2" cellspacing="0" width = "100%">
			 <TR>
			 <TD align="left">
			  <xsl:if test="count(shortname) &gt; 0">
			    <font size="4"><xsl:value-of select="shortname"/></font>
			  </xsl:if>
			  <xsl:if test="count(shortname) = 0">
			    <font size="4"><xsl:value-of select="name"/></font>
			  </xsl:if>
			 </TD>
			 
			  <TD align="right">
						<A>
							<xsl:attribute name="href">
							  javascript:openPath( '<xsl:value-of select="translate(name, '\\', '/')"/>' );
							</xsl:attribute>
							<xsl:if test="name != ''">baseline</xsl:if>
						</A>		
			<xsl:if test="name = ''"><font color="#808080">baseline</font></xsl:if>
			<font size="3"> . </font>
		
						<A>
							<xsl:attribute name="href">
							  javascript:openPath( '<xsl:value-of select="translate(comparefilename, '\\', '/')"/>' );
							</xsl:attribute>
							<xsl:if test="comparefilename != ''">current</xsl:if>
						</A>
				
			<xsl:if test="comparefilename = ''"><font color="#808080">current</font></xsl:if>
			<font size="3"> . </font> 
			
			</TD></TR>
     </table>
     <table>
			  <xsl:choose>
			    <xsl:when test="count(baseplatform) &gt; 0 and count(currentplatform) &gt; 0 ">
			      <tr>
			      <td>
			      <xsl:choose>
					    <xsl:when test="baseplatform!=''"><font size="2">base platform: <xsl:value-of select="baseplatform"/></font></xsl:when>
					    <xsl:otherwise><font size="2">base platform: Unknown</font></xsl:otherwise>
					  </xsl:choose>
					  </td>
					  <xsl:if test="comparefilename != ''">
					  <td/><td/><td/><td/><td/>
					  <td>
					  <xsl:choose>
					    <xsl:when test="currentplatform!=''"><font size="2">current platform: <xsl:value-of select="currentplatform"/></font></xsl:when>
					    <xsl:otherwise><font size="2">current platform: Unknown</font></xsl:otherwise>
					  </xsl:choose>
					  </td>
					  </xsl:if>
					  </tr>
				 </xsl:when>
			 </xsl:choose>
			</table>
			 
			<table border="0" cellpadding="2" cellspacing="0" bgColor="black" width = "100%">
			<TR><TD><table border="0" cellspacing="0" cellpadding="3" width = "100%">

			<TR>
              <xsl:attribute name="bgColor">
			    <xsl:choose>
			      <xsl:when test="status = 'OK'">#a8d098</xsl:when>
			      <xsl:otherwise>#d0c8c0</xsl:otherwise>
			    </xsl:choose>
			  </xsl:attribute>
			<TD align="left">
			<xsl:choose>
			    <xsl:when test="count(issue[status='NOK']) &gt; 0"><b>This library contains changes that cause incompatibility. It must be fixed.</b></xsl:when>
				  <xsl:otherwise><b>This library contains changes that may cause incompatibility. It must be analysed by an expert.</b></xsl:otherwise>
			</xsl:choose>
			</TD></TR>

			</table>
				<table width="100%" cellpadding="0" cellspacing="0" bgColor="black" height="1"><tr><td/></tr></table>
				<table width = "100%" cellpadding="0" cellspacing="0" bgColor="#b0a8a0">
				<tr><td><table width = "100%" cellpadding="3" cellspacing="1" id="issueTable">
				<tr bgcolor="#f0e8e0">
				<TH align="center" width="70"><B>Change</B></TH>
				<TH align="center" width="70"><B>Pos</B></TH>
				<TH align="left"><B>Details</B></TH>
				<TH align="left" width="130" id="BCSeverity"><B>BC Severity</B></TH>
				<xsl:if test="count(issue/sc_severity) &gt; 0">
				<TH align="left" width="130" id="SCSeverity"><B>SC Severity</B></TH>
				</xsl:if>
				</tr>

				<xsl:for-each select="issue[typeid!=2 and typeid!=7 and typeid!=14 ]">
				
				<xsl:if test="(status='NOK'or count(status) = 0)">
 				<xsl:variable name="bccellbgcolour">
				<xsl:choose>
				<xsl:when test="count(bc_severity) &gt; 0">	
				  <xsl:choose>
					  <xsl:when test="bc_severity = 'BBC Break'"><xsl:text>#ffb880</xsl:text></xsl:when>			   
					  <xsl:when test="bc_severity = 'Possible BBC Break' or bc_severity = 'Informative'"><xsl:text>#ffffa0</xsl:text></xsl:when>			  
				    <xsl:otherwise><xsl:text>White</xsl:text></xsl:otherwise>
				  </xsl:choose>
				</xsl:when>			   
				</xsl:choose> 
				</xsl:variable>
				    
				<xsl:variable name="sccellbgcolour">		
				  <xsl:choose>
				    <xsl:when test="sc_severity = 'SC Break'"><xsl:text>#ffb880</xsl:text></xsl:when>
					  <xsl:when test="sc_severity = 'Possible SC Break' or sc_severity = 'Informative'"><xsl:text>#ffffa0</xsl:text></xsl:when>
					  <xsl:otherwise><xsl:text>White</xsl:text></xsl:otherwise>
				  </xsl:choose>				
				</xsl:variable>	
 				<xsl:variable name="sevdesc">
					<xsl:choose>
					  <xsl:when test="typeid=7">Informative</xsl:when>					  
					  <xsl:when test="status='NOK'">BBC Break</xsl:when>
				  	<xsl:when test="status='OK'">Informative</xsl:when>
				  	<xsl:otherwise>Possible BBC Break</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>

		    <TR bgcolor= "White">
				  <xsl:choose>
				    <xsl:when test="typeid=1">
				      <TD align="center"><i>removed</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD >Library is no longer available</TD>
				    </xsl:when>
				    <xsl:when test="typeid=3">
				      <TD align="center"><i>moved</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><b><xsl:value-of select="funcpos"/> <font size="2"> =&gt; </font> <xsl:value-of select="newfuncpos"/></b></TD>
				      <TD><xsl:value-of select="funcname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=4">
				      <TD align="center"><i>deleted</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><b><xsl:value-of select="funcpos"/></b></TD>
				      <TD><xsl:value-of select="funcname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=5">
				      <TD align="center"><i>inserted</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><b><xsl:value-of select="newfuncpos"/></b></TD>
				      <TD><xsl:value-of select="newfuncname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=6">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><b><xsl:value-of select="funcpos"/></b></TD>
				      <TD><b><i>was </i></b><xsl:value-of select="funcname"/><br/>
				          <b><i>now </i></b><xsl:value-of select="newfuncname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=7">
				      <TD align="center"><i>added</i></TD>
				      <TD align="center" bgcolor="#f8f8f8"><b><xsl:value-of select="newfuncpos"/></b></TD>
				      <TD><xsl:value-of select="newfuncname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=8">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=9">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=10">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=11">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=12">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=13">
				      <TD align="center"><i>removed</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=14">
				      <TD align="center"><i>not available</i></TD>
				      <TD align="center" bgcolor="#f8f8f8"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				   </xsl:choose>
				  
				  <xsl:choose>
				    <xsl:when test="count(bc_severity) = 0">
				      <TD bgcolor="{$bccellbgcolour}"><xsl:value-of select="$sevdesc"/></TD>
				    </xsl:when>
				    <xsl:otherwise>
		          <TD bgcolor="{$bccellbgcolour}"><xsl:value-of select="bc_severity"/></TD>
		          <TD bgcolor="{$sccellbgcolour}"><xsl:value-of select="sc_severity"/></TD>
		        </xsl:otherwise>
		      </xsl:choose>
				</TR>
				</xsl:if>
				
				</xsl:for-each>
				</table></td></tr></table>
			</TD></TR></table>
			<BR/>
			</div>
			</xsl:if>
		</xsl:for-each>
  </xsl:if>

  <xsl:if test="$reporttype='HDR'">
    <xsl:if test="count(bbcresults/issuelist/headerfile[status!='OK']) = 0">
      <i>There are no issues.</i><BR/>
    </xsl:if>
		<xsl:for-each select="bbcresults/issuelist/headerfile[status!='OK']">
		<div id="repBlock">
			<table width="100%" cellspacing="0" cellpadding="0" border="0" id="whole" style="visibility:visible">
			  <tr><td align="left"><font size="4"><xsl:value-of select="shortname"/></font></td>
			  <td align="right">
						<A>
							<xsl:attribute name="href">
							  javascript:showFile( '<xsl:value-of select="translate(filename, '\\', '/')"/>', null );
							</xsl:attribute>
							<xsl:if test="filename != ''">baseline</xsl:if>
						</A>		
			<xsl:if test="filename = ''"><font color="#808080">baseline</font></xsl:if>

			<font size="3"> . </font>
		
						<A>
							<xsl:attribute name="href">
							  javascript:showFile( null, '<xsl:value-of select="translate(comparefilename, '\\', '/')"/>' );
							</xsl:attribute>
							<xsl:if test="comparefilename != ''">current</xsl:if>
						</A>
				
			<xsl:if test="comparefilename = ''"><font color="#808080">current</font></xsl:if>

			<font size="3"> . </font>

			<xsl:choose>
			  <xsl:when test="filename != '' and comparefilename != ''">
			    <A>
						<xsl:attribute name="href">
							javascript:showDiff( '<xsl:value-of select="translate(shortname, '\\', '/')"/>', '<xsl:value-of select="translate(filename, '\\', '/')"/>', '<xsl:value-of select="translate(comparefilename, '\\', '/')"/>' );
						</xsl:attribute>
						diff
			    </A>
			  </xsl:when>
				<xsl:otherwise>
			    <font color="#808080">diff</font>
				</xsl:otherwise>
			</xsl:choose>
			
			</td></tr>
			</table>
			<table border="0" cellpadding="2" cellspacing="0" bgColor="black" width = "100%">
			<TR><TD><table border="0" cellspacing="0" cellpadding="3" width = "100%">

			<TR>
         <xsl:attribute name="bgColor">
			    <xsl:choose>
			      <xsl:when test="status = 'OK'">#a8d098</xsl:when>
			      <xsl:otherwise>#d0c8c0</xsl:otherwise>
			    </xsl:choose>
			  </xsl:attribute>
			<TD align="left">
			<xsl:choose>
			    <xsl:when test="status = 'NOK'"><b>This header contains changes that cause incompatibility. It must be fixed.</b></xsl:when>
			    <xsl:when test="status = 'MOK'"><b>This header contains changes that cannot be verified automatically; they must be investigated manually.</b></xsl:when>
			    <xsl:when test="status = 'OK'"><b>This header is known to be ok.</b></xsl:when>
			    <xsl:when test="status = 'IOK'"><b>This issue is known to be ok. But the header contains other unrelated changes.</b></xsl:when>
				<xsl:otherwise><b>This header contains changes that may cause incompatibility. It must be analysed by an expert.</b></xsl:otherwise>
			</xsl:choose>

			<xsl:choose>
			    <xsl:when test="status = 'NOK' or status = 'MOK' or status = 'OK' or status = 'IOK'">
			      <HR/><xsl:value-of select="comment"/>
				</xsl:when>
			</xsl:choose>
			</TD></TR>

			</table>

				<table width="100%" cellpadding="0" cellspacing="0" bgColor="black" height="1"><tr><td/></tr></table>
				<table width = "100%" cellpadding="0" cellspacing="0" bgColor="#b0a8a0">
				<tr><td><table width = "100%" cellpadding="3" cellspacing="1"  id="issueTable">
				<tr bgcolor="#f0e8e0">
				<TH align="left" width = "350"><B>Problem description</B></TH>
				<TH align="left"><B>Cause</B></TH>
				<TH align="left" width = "150" id="BCSeverity"><B>BC Severity</B></TH>
				<xsl:if test="count(issue/scseverity) &gt; 0">	
				  <TH align="left" width = "150" id="SCSeverity"><B>SC Severity</B></TH>		
				</xsl:if>	
				</tr>

				<xsl:for-each select="issue">

				<xsl:variable name="bccellbgcolour">
				<xsl:choose>
				<xsl:when test="count(scseverity) &gt; 0">	
				  <xsl:choose>
					  <xsl:when test="severity/typeid = 1 or severity/typeid = 4"><xsl:text>#ffb880</xsl:text></xsl:when>			   
					  <xsl:when test="severity/typeid = 2 or severity/typeid = 5"><xsl:text>#ffffa0</xsl:text></xsl:when>			  
				    <xsl:otherwise><xsl:text>White</xsl:text></xsl:otherwise>
				  </xsl:choose>
				</xsl:when>			  
				<xsl:otherwise>
				  <xsl:choose>
				    <xsl:when test="severity/typeid = 0"><xsl:text>#ffb880</xsl:text></xsl:when>			   
				    <xsl:when test="severity/typeid = 1"><xsl:text>#ffffa0</xsl:text></xsl:when>			  
				    <xsl:otherwise><xsl:text>White</xsl:text></xsl:otherwise>
				  </xsl:choose>
				</xsl:otherwise> 
				</xsl:choose> 
				</xsl:variable>
				    
				<xsl:variable name="sccellbgcolour">		
				  <xsl:choose>
				    <xsl:when test="scseverity/typeid = 1"><xsl:text>#ffb880</xsl:text></xsl:when>
					  <xsl:when test="scseverity/typeid = 2"><xsl:text>#ffffa0</xsl:text></xsl:when>
					  <xsl:otherwise><xsl:text>White</xsl:text></xsl:otherwise>
				  </xsl:choose>				
				</xsl:variable>				
         
		        <TR bgcolor= "White">
			    <xsl:variable name = "notSpecified">
					<xsl:text>Not specified</xsl:text>
				</xsl:variable>	
				<xsl:variable name = "valueFromDocument">					
					<xsl:value-of select="documentation" />					
				</xsl:variable>
				<xsl:variable name="global">
					<xsl:value-of select="identitydescription"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="typestring"/>										
				</xsl:variable>				
				<TD><xsl:value-of select="$global"/></TD>
			
				<xsl:choose>
					<xsl:when test = "typeid = 13 and identityid = 13">
					
						<xsl:variable name="compErrID">
							<xsl:text>err</xsl:text><xsl:value-of select="issueid" />							
						</xsl:variable>
					
						<TD>
							<div>
								<xsl:attribute name="id"><xsl:value-of select="$compErrID" /></xsl:attribute>
								<xsl:attribute name="class">tip</xsl:attribute>
								<xsl:value-of select="compilationerror"/>
							</div>
							
							<a>
								<xsl:attribute name="href">#</xsl:attribute>
								<xsl:attribute name="onmouseout">popUp(event,'<xsl:value-of select="$compErrID"/>')</xsl:attribute>
								<xsl:attribute name="onmouseover">popUp(event,'<xsl:value-of select="$compErrID"/>')</xsl:attribute>
								<xsl:attribute name="onclick">return false</xsl:attribute>
								<font color="#FF0000">Compilation Error</font>
							</a>
						</TD>
					</xsl:when>
					<xsl:when test = "contains($valueFromDocument, $notSpecified)">
						<TD><xsl:value-of select="cause"/></TD>
					</xsl:when>
					<xsl:otherwise>
						<TD> <A><xsl:attribute name="href"><xsl:value-of select="documentation" /></xsl:attribute><xsl:value-of select="cause"/></A></TD>
					</xsl:otherwise>
				</xsl:choose>
            
	    		<TD bgcolor= "{$bccellbgcolour}"><xsl:value-of select="severity/typestring"/></TD>
	    		<xsl:if test="count(scseverity) &gt; 0">	
	    		<TD bgcolor= "{$sccellbgcolour}"><xsl:value-of select="scseverity/typestring"/></TD>
	    		</xsl:if>
				</TR>
				</xsl:for-each>			
				</table></td></tr></table>
			</TD></TR></table>
			<BR/>
			</div>
		</xsl:for-each>
  </xsl:if>

<BR/><BR/>
<font size="3"><a id ="cl" onclick="switchMenu('sd');" href="#hd">View OK Issues</a></font><BR/><BR/>

<ul id="sd" style="display:none">	
  <a name="hd"></a><BR/><font size="5" >Compatibility OK Issues</font><HR/>
  <xsl:if test="$reporttype='LIB'">
    <xsl:if test="count(bbcresults/issuelist/library[count(issue[status='OK'])=0 and count(issue[typeid!=2]) = 0]) = 0">
      <TR><i>There are no issues.</i></TR>
    </xsl:if>
		<xsl:for-each select="bbcresults/issuelist/library">
			<xsl:if test="count(issue[status='OK']) &gt; 0 and count(issue[typeid!=2])&gt; 0">
			<div id="repBlock">
			<table border="0" cellpadding="2" cellspacing="0" width = "100%">
			<TR>
			 <TD align="left">
			  <xsl:if test="count(shortname) &gt; 0">
			    <font size="4"><xsl:value-of select="shortname"/></font>
			  </xsl:if>
			  <xsl:if test="count(shortname) = 0">
			    <font size="4"><xsl:value-of select="name"/></font>
			  </xsl:if>
			 </TD>
			 
			  <TD align="right">
						<A>
							<xsl:attribute name="href">
							  javascript:openPath( '<xsl:value-of select="translate(name, '\\', '/')"/>' );
							</xsl:attribute>
							<xsl:if test="name != ''">baseline</xsl:if>
						</A>		
			<xsl:if test="name = ''"><font color="#808080">baseline</font></xsl:if>
			<font size="3"> . </font>
		
						<A>
							<xsl:attribute name="href">
							  javascript:openPath( '<xsl:value-of select="translate(comparefilename, '\\', '/')"/>' );
							</xsl:attribute>
							<xsl:if test="comparefilename != ''">current</xsl:if>
						</A>
				
			<xsl:if test="comparefilename = ''"><font color="#808080">current</font></xsl:if>
			<font size="3"> . </font> 
			
			</TD></TR>
     </table>
     <table>
			  <xsl:choose>
			    <xsl:when test="count(baseplatform) &gt; 0 and count(currentplatform) &gt; 0 ">
			      <tr>
			      <td>
			      <xsl:choose>
					    <xsl:when test="baseplatform!=''"><font size="2">base platform: <xsl:value-of select="baseplatform"/></font></xsl:when>
					    <xsl:otherwise><font size="2">base platform: Unknown</font></xsl:otherwise>
					  </xsl:choose>
					  </td>
					  <xsl:if test="comparefilename != ''">
					  <td/><td/><td/><td/><td/>
					  <td>
					  <xsl:choose>
					    <xsl:when test="currentplatform!=''"><font size="2">current platform: <xsl:value-of select="currentplatform"/></font></xsl:when>
					    <xsl:otherwise><font size="2">current platform: Unknown</font></xsl:otherwise>
					  </xsl:choose>
					  </td>
					  </xsl:if>
					  </tr>
				 </xsl:when>
			 </xsl:choose>
			</table> 
				
			<table border="0" cellpadding="2" cellspacing="0" bgColor="black" width = "100%">
			<TR><TD><table border="0" cellspacing="0" cellpadding="3" width = "100%">

			<TR>
              <xsl:attribute name="bgColor">
			    <xsl:choose>
			      <xsl:when test="status = 'OK'">#a8d098</xsl:when>
			      <xsl:otherwise>#a8d098</xsl:otherwise>
			    </xsl:choose>
			  </xsl:attribute>
			<TD align="left">
			    <b>This library is known to be ok.</b>
			</TD></TR>

			</table>
				<table width = "100%" cellpadding="0" cellspacing="0" bgColor="black" height="1"><tr><td></td></tr></table>
				<table width = "100%" cellpadding="0" cellspacing="0" bgColor="#b0a8a0">
				<tr><td><table width = "100%" cellpadding="3" cellspacing="1" id="issueTable">
				<tr bgcolor="#f0e8e0">
				<TH align="center" width="70"><B>Change</B></TH>
				<TH align="center" width="70"><B>Pos</B></TH>
				<TH align="left"><B>Details</B></TH>
				<TH align="left" width="130" id="BCSeverity"><B>BC Severity</B></TH>
				<xsl:if test="count(issue/sc_severity) &gt; 0">
				<TH align="left" width="130" id="SCSeverity"><B>SC Severity</B></TH>
				</xsl:if>
				</tr>

				<xsl:for-each select="issue[typeid!=2]">
				<xsl:if test="(status='OK')">
 				<xsl:variable name="bgcol">
					<xsl:choose>
					  <xsl:when test="typeid=7">#ffffff</xsl:when>
					  <xsl:when test="typeid=14">#ffffff</xsl:when>
				  	<xsl:when test="status='NOK'">#ffb880</xsl:when>
				  	<xsl:when test="status='OK'">#ffffff</xsl:when>
				  	<xsl:otherwise>#ffffa0</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
 				<xsl:variable name="sevdesc">
					<xsl:choose>
					  <xsl:when test="typeid=7">Informative</xsl:when>
					  <xsl:when test="typeid=14">Informative</xsl:when>
					  <xsl:when test="status='NOK'">BBC Break</xsl:when>
				  	<xsl:when test="status='OK'">Informative</xsl:when>
				  	<xsl:otherwise>Possible BBC Break</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
 				
		    <TR bgcolor= "White">
				  <xsl:choose>
				    	<xsl:when test="typeid=1">
				      <TD align="center"><i>removed</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD >Library is no longer available</TD>
				    </xsl:when>
				    <xsl:when test="typeid=3">
				      <TD align="center"><i>moved</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><b><xsl:value-of select="funcpos"/> <font size="2"> =&gt; </font> <xsl:value-of select="newfuncpos"/></b></TD>
				      <TD><xsl:value-of select="funcname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=4">
				      <TD align="center"><i>deleted</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><b><xsl:value-of select="funcpos"/></b></TD>
				      <TD><xsl:value-of select="funcname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=5">
				      <TD align="center"><i>inserted</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><b><xsl:value-of select="newfuncpos"/></b></TD>
				      <TD><xsl:value-of select="newfuncname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=6">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><b><xsl:value-of select="funcpos"/></b></TD>
				      <TD><b><i>was </i></b><xsl:value-of select="funcname"/><br/>
				          <b><i>now </i></b><xsl:value-of select="newfuncname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=7">
				      <TD align="center"><i>added</i></TD>
				      <TD align="center" bgcolor="#f8f8f8"><b><xsl:value-of select="newfuncpos"/></b></TD>
				      <TD><xsl:value-of select="newfuncname"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=8">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=9">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=10">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=11">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=12">
				      <TD align="center"><i>modified</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=13">
				      <TD align="center"><i>removed</i></TD>
				      <TD align="center" bgcolor="#f4f4f4"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				    <xsl:when test="typeid=14">
				      <TD align="center"><i>not available</i></TD>
				      <TD align="center" bgcolor="#f8f8f8"><i>-</i></TD>
				      <TD><xsl:value-of select="typeinfo"/></TD>
				    </xsl:when>
				  </xsl:choose>
				  <xsl:choose>
				    <xsl:when test="count(bc_severity) = 0">
				      <TD bgcolor="{$bgcol}"><xsl:value-of select="$sevdesc"/></TD>
				    </xsl:when>
				    <xsl:otherwise>
		          <TD bgcolor="{$bgcol}"><xsl:value-of select="bc_severity"/></TD>
		          <TD bgcolor="{$bgcol}"><xsl:value-of select="sc_severity"/></TD>
		        </xsl:otherwise>
		      </xsl:choose>
				</TR>
				</xsl:if>
				</xsl:for-each>
				</table></td></tr></table>
			</TD></TR></table>
			<BR/>
			</div>
			</xsl:if>
		</xsl:for-each>
  </xsl:if>

  <xsl:if test="$reporttype='HDR'">
    <xsl:if test="count(bbcresults/issuelist/headerfile[status='OK']) = 0">
      <TR><i>There are no issues.</i></TR>
    </xsl:if>
		<xsl:for-each select="bbcresults/issuelist/headerfile[status='OK']">
			<div id="repBlock">
			<table width="100%" cellspacing="0" cellpadding="0" border="0">
			  <tr><td align="left"><font size="4"><xsl:value-of select="shortname"/></font></td>
			  <td align="right">
						<A>
							<xsl:attribute name="href">
							  javascript:showFile( '<xsl:value-of select="translate(filename, '\\', '/')"/>', null );
							</xsl:attribute>
							<xsl:if test="filename != ''">baseline</xsl:if>
					  </A>
	
			<xsl:if test="filename = ''"><font color="#808080">baseline</font></xsl:if>

			<font size="3"> . </font>

						<A>
							<xsl:attribute name="href">
							  javascript:showFile( null, '<xsl:value-of select="translate(comparefilename, '\\', '/')"/>' );
							</xsl:attribute>
							<xsl:if test="comparefilename != ''">current</xsl:if>
						</A>
						
			<xsl:if test="comparefilename = ''"><font color="#808080">current</font></xsl:if>

			<font size="3"> . </font>

			<xsl:choose>
			  <xsl:when test="filename != '' and comparefilename != ''">
			    <A>
						<xsl:attribute name="href">
							javascript:showDiff( '<xsl:value-of select="translate(shortname, '\\', '/')"/>', '<xsl:value-of select="translate(filename, '\\', '/')"/>', '<xsl:value-of select="translate(comparefilename, '\\', '/')"/>' );
						</xsl:attribute>
						diff
			    </A>
			  </xsl:when>
				<xsl:otherwise>
			    <font color="#808080">diff</font>
				</xsl:otherwise>
			</xsl:choose>
			
			</td></tr>
			</table>
			<table border="0" cellpadding="2" cellspacing="0" bgColor="black" width = "100%">
			<TR><TD><table border="0" cellspacing="0" cellpadding="3" width = "100%">

			<TR>
              <xsl:attribute name="bgColor">
			    <xsl:choose>
			      <xsl:when test="status = 'OK'">#a8d098</xsl:when>
			      <xsl:otherwise>#d0c8c0</xsl:otherwise>
			    </xsl:choose>
			  </xsl:attribute>
			<TD align="left">
			<xsl:choose>
			    <xsl:when test="status = 'NOK'"><b>This header contains changes that cause incompatibility. It must be fixed.</b></xsl:when>
			    <xsl:when test="status = 'MOK'"><b>This header contains changes that cannot be verified automatically; they must be investigated manually.</b></xsl:when>
			    <xsl:when test="status = 'OK'"><b>This header is known to be ok.</b></xsl:when>
			    <xsl:when test="status = 'IOK'"><b>This issue is known to be ok. But the header contains other unrelated changes.</b></xsl:when>
				<xsl:otherwise><b>This header contains changes that may cause incompatibility. It must be analysed by an expert.</b></xsl:otherwise>
			</xsl:choose>

			<xsl:choose>
			    <xsl:when test="status = 'NOK' or status = 'MOK' or status = 'OK' or status = 'IOK'">
			      <HR/><xsl:value-of select="comment"/>
				</xsl:when>
			</xsl:choose>
			</TD></TR>

			</table>

				<table width = "100%" cellpadding="0" cellspacing="0" bgColor="black" height="1"><tr><td></td></tr></table>
				<table width = "100%" cellpadding="0" cellspacing="0" bgColor="#b0a8a0">
				<tr><td><table width = "100%" cellpadding="3" cellspacing="1" id="issueTable">
				<tr bgcolor="#f0e8e0">
				<TH align="left" width = "350"><B>Problem description</B></TH>
				<TH align="left"><B>Cause</B></TH>
				<TH align="left" width = "150" id="BCSeverity"><B>BC Severity</B></TH>	
				<xsl:if test="count(issue/scseverity) &gt; 0">	
				  <TH align="left" width = "150" id="SCSeverity"><B>SC Severity</B></TH>		
				</xsl:if>		
				</tr>

				<xsl:for-each select="issue">

		        <TR bgcolor= "White">
			    <xsl:variable name = "notSpecified">
					<xsl:text>Not specified</xsl:text>
				</xsl:variable>	
				<xsl:variable name = "valueFromDocument">					
					<xsl:value-of select="documentation" />					
				</xsl:variable>
				<xsl:variable name="global">
					<xsl:value-of select="identitydescription"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="typestring"/>										
				</xsl:variable>				
				<TD><xsl:value-of select="$global"/></TD>
			
				<xsl:choose>
				  <xsl:when test = "typeid = 13 and identityid = 13">
					
						<xsl:variable name="compErrID">
							<xsl:text>err</xsl:text><xsl:value-of select="issueid" />							
						</xsl:variable>
											
						<TD>
							<div>
								<xsl:attribute name="id"><xsl:value-of select="$compErrID" /></xsl:attribute>
								<xsl:attribute name="class">tip</xsl:attribute>
								<xsl:value-of select="compilationerror"/>
							</div>							
							<a>
								<xsl:attribute name="href">#</xsl:attribute>
								<xsl:attribute name="onmouseout">popUp(event,'<xsl:value-of select="$compErrID"/>')</xsl:attribute>
								<xsl:attribute name="onmouseover">popUp(event,'<xsl:value-of select="$compErrID"/>')</xsl:attribute>
								<xsl:attribute name="onclick">return false</xsl:attribute>
								<font color="#FF0000">Compilation Error</font>
							</a>
						</TD>
					</xsl:when>
					<xsl:when test = "contains($valueFromDocument, $notSpecified)">
						<TD><xsl:value-of select="cause"/></TD>
					</xsl:when>
					<xsl:otherwise>
						<TD> <A><xsl:attribute name="href"><xsl:value-of select="documentation" /></xsl:attribute><xsl:value-of select="cause"/></A></TD>
					</xsl:otherwise>
				</xsl:choose>
            
	    		<TD ><xsl:value-of select="severity/typestring"/></TD>
	    		<xsl:if test="count(scseverity) &gt; 0">	
	    		<TD ><xsl:value-of select="scseverity/typestring"/></TD>
	    		</xsl:if>
				</TR>
				</xsl:for-each>			
				</table></td></tr></table>
			</TD></TR></table>
			<BR/>
			</div>
		</xsl:for-each>
  </xsl:if>	
</ul>
		</BODY>
</HTML>
</xsl:template>
</xsl:stylesheet>
