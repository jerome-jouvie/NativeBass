BASSmix 2.4
Copyright (c) 2005-2015 Un4seen Developments Ltd. All rights reserved.

Files that you should have found in the BASSmix package
=======================================================
Win32 version
-------------
BASSMIX.TXT     This file
BASSMIX.DLL     BASSmix module
BASSMIX.CHM     BASSmix documentation
X64\
  BASSMIX.DLL     64-bit BASSmix module
C\              C/C++ API and examples...
  BASSMIX.H       BASSmix C/C++ header file
  BASSMIX.LIB     BASSmix import library
  BASSMIX.DSW     Visual C++ 6 workspace for examples
  BASSMIX.SLN     Visual C++ 2005 (and above) solution for examples
  MAKEFILE        Makefile for all examples
  MAKEFILE.IN     Makefile helper macros
  X64\
    BASSMIX.LIB     64-bit BASSmix import library
  MULTI\          Multiple device example
    MULTI.C
    MULTI.RC
    MULTI.DSP
    MULTI.VCPROJ
    MAKEFILE
  SPEAKERS\       Multi-speaker example
    SPEAKERS.C
    SPEAKERS.RC
    SPEAKERS.DSP
    SPEAKERS.VCPROJ
    MAKEFILE
  BIN\            Precompiled examples
    MULTI.EXE
    SPEAKERS.EXE
VB\             Visual Basic API and examples...
  BASSMIX.BAS     BASSmix Visual Basic module
  MULTI\          Multiple device example
    PRJMULTI.VBP
    FRMMULTI.FRM
    FRMDEVICE.FRM
  SPEAKERS\       Multi-speaker example
    PRJSPEAKERS.VBP
    FRMSPEAKERS.FRM
DELPHI\         Delphi API and examples...
  BASSMIX.PAS     BASSmix Delphi unit
  SPEAKERS\       Multi-speaker example
    SPEAKERS.DPR
    MAINFRM.PAS
    MAINFRM.DFM

NOTE: To run the example EXEs, first you will have to copy BASSMIX.DLL and
      BASS.DLL into the same directory as them.

NOTE: To build the examples, you will need to copy the BASS API into the
      same directory as the BASSmix API.

OSX version
-----------
BASSMIX.TXT     This file
LIBBASSMIX.DYLIB  BASSmix module
BASSMIX.CHM     BASSmix documentation
BASSMIX.H       BASSmix C/C++ header file
MAKEFILE        Makefile for all examples
MAKEFILE.IN     Makefile helper macros
BASSMIX.XCODEPROJ  Xcode project for examples
MULTI\          Multiple device example
  MULTI.C
  MAKEFILE
  MULTI.NIB
SPEAKERS\       Multi-speaker example
  SPEAKERS.C
  MAKEFILE
  SPEAKERS.NIB

NOTE: To build the examples, you will need to copy the BASS API into the
      same directory as the BASSmix API.

Linux version
-------------
BASSMIX.TXT     This file
LIBBASSMIX.SO   BASSmix module
BASSMIX.CHM     BASSmix documentation
BASSMIX.H       BASSmix C/C++ header file
X64\
  LIBBASSMIX.SO   64-bit BASSmix module


What's the point?
=================
BASSmix is an extension to the BASS audio library, providing the ability
to mix together multiple BASS channels, with resampling and matrix mixing
features. It also provides the ability to go the other way and split a
BASS channel into multiple channels.


Requirements
============
BASS (version 2.4.11 or above) is required.


Using BASSmix
=============
The usage information in the BASS.TXT file (from the BASS package) is also
applicable to BASSmix and other add-ons.

As well as the examples included in this package, several of the examples
included in the BASSWASAPI package also use BASSmix.

TIP: The BASSmix.CHM file should be put in the same directory as the BASS.CHM
     file, so that the BASSmix documentation can be accessed from within the
     BASS documentation.


Latest Version
==============
The latest versions of BASSmix & BASS can be found at the BASS website:

	www.un4seen.com


Licence
=======
BASSmix is free to use with BASS.

TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, BASSMIX IS PROVIDED
"AS IS", WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND/OR FITNESS FOR A PARTICULAR PURPOSE. THE AUTHORS SHALL NOT BE HELD
LIABLE FOR ANY DAMAGE THAT MAY RESULT FROM THE USE OF BASSMIX. YOU USE
BASSMIX ENTIRELY AT YOUR OWN RISK.

Usage of BASSmix indicates that you agree to the above conditions.

All trademarks and other registered names contained in the BASSmix
package are the property of their respective owners.


History
=======
These are the major (and not so major) changes at each version stage.
There are of course bug fixes and other little improvements made along
the way too! To make upgrading simpler, all functions affected by a
change to the BASSmix interface are listed.

2.4.8 - 13/3/2015
-----------------
* Accounting for custom output latency in source position/data/level retrieval
	BASS_ATTRIB_MIXER_LATENCY (BASS_ChannelSetAttribute option)
	BASS_Mixer_ChannelGetPosition
	BASS_Mixer_ChannelGetData
	BASS_Mixer_ChannelGetLevel/Ex
* Ramped matrix changes
	BASS_Mixer_ChannelSetMatrixEx
* Speaker assignment with matrix mixing
	BASS_SPEAKER_xxx (BASS_Mixer_StreamAddChannel/Ex and BASS_Mixer_ChannelFlags flags)
* Extended level retrieval support
	BASS_Mixer_ChannelGetLevelEx
* Absolute source buffer length option
	BASS_CONFIG_MIXER_BUFFER
* Length of resampled output rounded to nearest sample

2.4.7 - 8/5/2012
----------------
* Improved and adjustable sample rate conversion quality
	BASS_ATTRIB_SRC (BASS_ChannelSetAttribute option)
* Additional filtering option removed (superseded by BASS_ATTRIB_SRC)
	BASS_MIXER_FILTER
	BASS_CONFIG_MIXER_FILTER

2.4.6 - 26/9/2011
-----------------
* Retrieval of all splitters set on a channel
	BASS_Split_StreamGetSplits
* "Slave" splitters that only read buffered data (not directly from the source)
	BASS_SPLIT_SLAVE (BASS_Split_StreamCreate flag)
* Splitter buffer length is now unrestricted
	BASS_CONFIG_SPLIT_BUFFER
* Splitter position now available immediately following creation of the splitter
	BASS_ChannelGetPosition
* Envelope node syncs
	BASS_SYNC_MIXER_ENVELOPE_NODE (BASS_Mixer_ChannelSetSync type)
* Limiting of envelope syncs to a particular envelope type
	BASS_SYNC_MIXER_ENVELOPE/NODE (BASS_Mixer_ChannelSetSync type)
* More precise mixer sample rate conversion
* Xcode examples project added for OSX

2.4.5 - 17/12/2010
------------------
* Accounting for latency when getting the position of a mixer source
	BASS_MIXER_POSEX (BASS_Mixer_StreamCreate flag)
	BASS_Mixer_ChannelGetPositionEx
* Splitter buffer positioning
	BASS_Split_StreamResetEx
* Splitter buffer level retrieval
	BASS_Split_StreamGetAvailable
* Splitter buffer length limit increased to 10 seconds
	BASS_CONFIG_SPLIT_BUFFER
* Decode-to-position seeking support for mixers and splitters
	BASS_POS_DECODETO (BASS_ChannelSetPosition flag)
* Fix for potential splitter resetting deadlock
	BASS_Split_StreamReset

2.4.4 - 4/2/2010
----------------
* Fix for a bug that could result in a mixer source ending a fraction early

2.4.3 - 14/12/2009
------------------
* Envelope sync fix
	BASS_SYNC_MIXER_ENVELOPE (BASS_Mixer_ChannelSetSync type)
* Thread-safety fix
	BASS_Mixer_ChannelRemove

2.4.2 - 4/9/2009
----------------
* Matrix mixing example
	SPEAKERS example added
* Splitter stream example
	MULTI example added

2.4.1 - 19/12/2008
------------------
* Splitting into multiple streams
	BASS_Split_StreamCreate
	BASS_Split_StreamGetSource
	BASS_Split_StreamReset
	BASS_CONFIG_SPLIT_BUFFER (BASS_SetConfig option)
* Mixer output limiting to the amount of data available from a source
	BASS_MIXER_LIMIT (BASS_Mixer_StreamAddChannel/Ex flag)

2.4 - 2/4/2008
--------------
* Attribute envelopes
	BASS_Mixer_ChannelSetEnvelope
	BASS_Mixer_ChannelGetEnvelopePos
	BASS_Mixer_ChannelSetEnvelopePos
	BASS_MIXER_NODE
	BASS_SYNC_MIXER_ENVELOPE (BASS_Mixer_ChannelSetSync type)
* Adjustable resampling filtering
	BASS_MIXER_FILTER (BASS_Mixer_StreamAddChannel/Ex flag)
	BASS_CONFIG_MIXER_FILTER (BASS_SetConfig option)
* Source level/data retrieval
	BASS_Mixer_ChannelGetData/Level
	BASS_MIXER_BUFFER (BASS_Mixer_StreamAddChannel/Ex flag)
	BASS_CONFIG_MIXER_BUFFER (BASS_SetConfig option)
* Immediate resuming of a stalled mixer upon a new/unpaused source
	BASS_MIXER_RESUME (BASS_Mixer_StreamCreate flag)
* Support for position "modes"
	BASS_Mixer_ChannelSetPosition
	BASS_Mixer_ChannelGetPosition
* 64-bit channel start & length settings
	BASS_Mixer_StreamAddChannelEx
* Support for all source sync types
	BASS_Mixer_ChannelSetSync
* Mixer channel type added
	BASS_CTYPE_STREAM_MIXER
* Version retrieval
	BASS_Mixer_GetVersion
* Callback "user" parameters changed to pointers
	BASS_Mixer_ChannelSetSync

2.3 - 30/7/2007
---------------
* First release


Bug reports, Suggestions, Comments, Enquiries, etc...
=====================================================
If you have any of the aforementioned please visit the BASS forum at
the website.

