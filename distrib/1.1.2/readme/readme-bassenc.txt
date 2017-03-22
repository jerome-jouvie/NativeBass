BASSenc 2.4
Copyright (c) 2003-2015 Un4seen Developments Ltd. All rights reserved.

Files that you should have found in the BASSenc package
=======================================================
Win32 version
-------------
BASSENC.TXT     This file
BASSENC.DLL     BASSenc module
BASSENC.CHM     BASSenc documentation
X64\
  BASSENC.DLL     64-bit BASSenc module
C\              C/C++ API and examples...
  BASSENC.H       BASSenc C/C++ header file
  BASSENC.LIB     BASSenc import library
  BASSENC.DSW     Visual C++ 6 workspace for examples
  BASSENC.SLN     Visual C++ 2005 (and above) solution for examples
  MAKEFILE        Makefile for all examples
  MAKEFILE.IN     Makefile helper macros
  X64\
    BASSENC.LIB     64-bit BASSenc import library
  CAST\           Shoutcast/Icecast sourcing example
    CAST.C
    CAST.RC
    CAST.DSP
    CAST.VCPROJ
    MAKEFILE
  CONTEST\        Console transcoding example
    CONTEST.C
    CONTEST.DSP
    CONTEST.VCPROJ
    MAKEFILE
  RECTEST\        Recording example
    RECTEST.C
    RECTEST.RC
    RECTEST.DSP
    RECTEST.VCPROJ
    MAKEFILE
  SERVER\         Server example
    SERVER.C
    SERVER.RC
    SERVER.DSP
    SERVER.VCPROJ
    MAKEFILE
  BIN\            Precompiled examples
    CAST.EXE
    CONTEST.EXE
    RECTEST.EXE
    SERVER.EXE
VB\             Visual Basic API and examples...
  BASSENC.BAS     BASSenc Visual Basic module
  CAST\           Shoutcast/Icecast sourcing example
    PRJCAST.VBP
    FRMCAST.FRM
    MODCAST.BAS
  RECTEST\        Recording example
    PRJRECTEST.VBP
    FRMRECTEST.FRM
    MODRECTEST.BAS
DELPHI\         Delphi API and examples...
  BASSENC.PAS     BASSenc Delphi unit
  CAST\           Shoutcast/Icecast sourcing example
    CAST.DPR
    UNIT1.PAS
    UNIT1.DFM
  RECORDTEST\     Recording example
    RECTEST.DPR
    UNIT1.PAS
    UNIT1.DFM

NOTE: To run the example EXEs, first you will have to copy BASSENC.DLL and
      BASS.DLL into the same directory as them.

NOTE: To build the examples, you will need to copy the BASS API into the
      same directory as the BASSenc API.

MacOSX version
--------------
BASSENC.TXT     This file
LIBBASSENC.DYLIB  BASSenc module
BASSENC.CHM     BASSenc documentation
BASSENC.H       BASSenc C/C++ header file
MAKEFILE        Makefile for all examples
MAKEFILE.IN     Makefile helper macros
BASSENC.XCODEPROJ  Xcode project for examples
CAST\           Shoutcast/Icecast sourcing example
  CAST.C
  CAST.NIB
  MAKEFILE
CONTEST\        Console transcoding example
  CONTEST.C
  MAKEFILE
RECTEST\        Recording example
  RECTEST.C
  RECTEST.NIB
  MAKEFILE
SERVER\         Server example
  SERVER.C
  SERVER.NIB
  MAKEFILE

NOTE: To build the examples, you will need to copy the BASS API into the
      same directory as the BASSenc API.

Linux version
-------------
BASSENC.TXT     This file
LIBBASSENC.SO   BASSenc module
BASSENC.CHM     BASSenc documentation
BASSENC.H       BASSenc C/C++ header file
MAKEFILE        Makefile for all examples
MAKEFILE.IN     Makefile helper macros
X64\
  LIBBASSENC.SO   64-bit BASSenc module
CAST\           Shoutcast/Icecast sourcing example
  CAST.C
  CAST.GLADE
  MAKEFILE
CONTEST\        Console transcoding example
  CONTEST.C
  MAKEFILE
RECTEST\        Recording example
  RECTEST.C
  RECTEST.GLADE
  MAKEFILE
SERVER\         Server example
  SERVER.C
  SERVER.GLADE
  MAKEFILE

NOTE: To build the examples, you will need to copy the BASS API into the
      same directory as the BASSenc API.


What's the point?
=================
BASSenc is an extension that allows BASS channels to be encoded using 
command-line encoders with STDIN support (LAME/OGGENC/etc), or ACM codecs
(on Windows) or CoreAudio codecs (on OSX), optionally sending the data to
clients via local servers or Shoutcast/Icecast servers.


Requirements
============
BASS 2.4 is required.

The examples require OGGENC and/or LAME. Both are available from the MO3
webpage at the www.un4seen.com website.


Using BASSenc
=============
The usage information in the BASS.TXT file (from the BASS package) is also
applicable to BASSenc and other add-ons.

TIP: The BASSENC.CHM file should be put in the same directory as the BASS.CHM
     file, so that the BASSenc documentation can be accessed from within the
     BASS documentation.


Latest Version
==============
The latest versions of BASSenc & BASS can be found at the BASS website:

	www.un4seen.com


Licence
=======
BASSenc is free to use with BASS.

TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, BASSENC IS PROVIDED
"AS IS", WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND/OR FITNESS FOR A PARTICULAR PURPOSE. THE AUTHORS SHALL NOT BE HELD
LIABLE FOR ANY DAMAGE THAT MAY RESULT FROM THE USE OF BASSENC. YOU USE
BASSENC ENTIRELY AT YOUR OWN RISK.

Usage of BASSenc indicates that you agree to the above conditions.

All trademarks and other registered names contained in the BASSenc
package are the property of their respective owners.


History
=======
These are the major (and not so major) changes at each version stage.
There are of course bug fixes and other little improvements made along
the way too! To make upgrading simpler, all functions affected by a
change to the BASSenc interface are listed.

2.4.12.7 - 7/5/2015
-------------------
* Admin username and password support for casting stats retrieval
	BASS_Encode_CastGetStats
* Checking in executable's directory for encoders on Linux/OSX (already so on Windows)
	BASS_Encode_Start/Limit

2.4.12 - 3/10/2014
------------------
* User-provided encoders
	BASS_Encode_StartUser
	ENCODERPROC

2.4.11 - 31/10/2013
-------------------
* AIFF encoding/writing
	BASS_ENCODE_AIFF (BASS_Encode_Start/Limit flag)
	CONTEST example updated
* Configurable Icecast source username
	BASS_Encode_CastInit
* Use of the BASS_CONFIG_NET_TIMEOUT setting when connecting to cast servers
	BASS_Encode_CastInit
	BASS_Encode_CastGetStats
	BASS_Encode_CastSetTitle
* Fix for port release issue when using multiple servers
	BASS_Encode_ServerInit

2.4.10 - 20/11/2012
-------------------
* WAVEFORMATEXTENSIBLE format chunk support
	BASS_ENCODE_WFEXT (BASS_Encode_Start/Limit flag)
* Unlimited casting stats length (was limited to 100KB)
	BASS_Encode_CastGetStats
* Fix for final output not being flushed from ACM encoder when freed
	BASS_Encode_Stop/Ex
* Shoutcast 2 metadata thread-safety fix
	BASS_Encode_CastSendMeta
	BASS_Encode_CastSetTitle

2.4.9 - 3/2/2012
----------------
* Support for Shoutcast 2 servers
	BASS_Encode_CastInit
	BASS_Encode_CastSendMeta
* Casting and server support for PCM encoding (allows sending of pre-encoded data)
	BASS_Encode_CastInit
	BASS_Encode_ServerInit
* PCM encoding without writing to file
	BASS_Encode_Start
* Notification of an encoder being freed
	BASS_ENCODE_NOTIFY_FREE (ENCODENOTIFYPROC status)
* RECTEST example updated (C version) for better Windows Vista/7 support

2.4.8 - 11/3/2011
-----------------
* Built-in streaming servers
	BASS_Encode_ServerInit
	BASS_Encode_ServerKick
	SERVER example added
* CoreAudio encoding (OSX/iOS only)
	BASS_Encode_StartCA/File
	CAST/CONTEST/RECTEST examples updated
* Queued/asynchronous encoding
	BASS_ENCODE_QUEUE (BASS_Encode_Start/ACM/CA flag)
	BASS_ENCODE_COUNT_QUEUE/_LIMIT/_FAIL (BASS_Encode_GetCount option)
	BASS_ENCODE_CONFIG_QUEUE (BASS_SetConfig option)
	BASS_Encode_StopEx
* Encoding rate limiting to real-time speed
	BASS_ENCODE_LIMIT (BASS_Encode_Start/ACM/CA flag)
* NULL can be used to set an empty cast stream title
	BASS_Encode_CastSetTitle
* Shoutcast title setting fix
	BASS_Encode_CastSetTitle
* Xcode examples project added for OSX

2.4.7 - 9/7/2010
----------------
* Casting rate limiting is now bypassed on playback & recording, and optional otherwise
	BASS_ENCODE_CAST_NOLIMIT (BASS_Encode_Start/Limit flag)
* Support for Live365 servers
	BASS_Encode_CastInit
* Fixes for BASS_UNICODE flag issues
	BASS_Encode_Start/Limit
	BASS_Encode_GetACMFormat

2.4.6 - 1/12/2009
-----------------
* Use of the BASS_CONFIG_NET_AGENT setting in cast server requests
	BASS_Encode_CastGetStats
	BASS_Encode_CastSetTitle
* Thread-safety fix
	BASS_Encode_Start/Limit
* UTF-16 support on OSX
	BASS_UNICODE (BASS_Encode_Start/Limit flag)

2.4.5 - 26/10/2009
------------------
* Casting via proxy servers
	BASS_CONFIG_ENCODE_CAST_PROXY (BASS_SetConfigPtr option)
* Fix for a potential casting timing issue on Windows
	BASS_Encode_CastInit

2.4.4 - 16/6/2009
-----------------
* Encoding length limiting
	BASS_Encode_StartLimit

2.4.2 - 18/9/2008
-----------------
* RF64 support
	BASS_ENCODE_RF64 (BASS_Encode_Start/ACMFile flag)
* Custom RIFF chunk writing
	BASS_Encode_AddChunk
* "fact" chunk writing
	BASS_Encode_StartACMFile

2.4 - 2/4/2008
--------------
* Encoded data count retrieval
	BASS_Encode_GetCount
* 32-bit integer data conversion
	BASS_ENCODE_FP_32BIT
* Shoutcast title URL setting
	BASS_Encode_CastSetTitle
* Version retrieval
	BASS_Encode_GetVersion
* Callback "user" parameters changed to pointers
	BASS_Encode_Start / ENCODEPROC
	BASS_Encode_SetNotify / ENCODENOTIFYPROC

2.3.0.4 - 30/10/2007
--------------------
* Cast sending timeout
	BASS_CONFIG_ENCODE_CAST_TIMEOUT (BASS_SetConfig option)
	BASS_ENCODE_NOTIFY_CAST_TIMEOUT (ENCODENOTIFYPROC status)
* ACM format choice restriction
	BASS_Encode_GetACMFormat

2.3.0.3 - 8/1/2007
------------------
* PCM/WAV file writing
	BASS_ENCODE_PCM (BASS_Encode_Start flag)

2.3.0.2 - 18/11/2006
--------------------
* Shoutcast/Icecast sourcing
	BASS_Encode_CastInit
	BASS_Encode_CastSetTitle
	BASS_Encode_CastGetStats
* Move encoders between channels
	BASS_Encode_SetChannel
	BASS_Encode_GetChannel
* Notification of encoder status
	BASS_Encode_SetNotify
	ENCODENOTIFYPROC

2.3 - 21/5/2006
---------------
* ACM encoding (Win32 only)
	BASS_Encode_StartACM/File
	RECTEST example updated
* Multiple encoders per channel
	BASS_Encode_Start/ACM/File
	ENCODEPROC (handle parameter)
* Auto-freeing of the encoder when the channel is freed
	BASS_ENCODE_AUTOFREE (BASS_Encode_Start/ACM/File flag)
* Configurable encoder DSP priority
	BASS_CONFIG_ENCODE_PRIORITY (BASS_SetConfig option)

2.2 - 2/10/2005
---------------
* Big-endian sample data option
	BASS_ENCODE_BIGEND (BASS_Encode_Start flag)
* Manual encoder feeding
	BASS_Encode_Write
	BASS_ENCODE_PAUSE (BASS_Encode_Start flag)
* Access to encoder process handle/ID
	BASS_Encode_Start
* Support for Unicode command-lines
	BASS_UNICODE (BASS_Encode_Start flag)
* Error code set on failure (not just return FALSE)
* MacOSX port introduced

2.0 - 31/10/2003
---------------
* Support for receiving encoded data via STDOUT
	BASS_Encode_Start
	ENCODEPROC
* Encoder pausing
	BASS_Encode_SetPaused

1.0 - 9/8/2003
--------------
* First release


Bug reports, Suggestions, Comments, Enquiries, etc...
=====================================================
If you have any of the aforementioned please visit the BASS forum at
the website.

