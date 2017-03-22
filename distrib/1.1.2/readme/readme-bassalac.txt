BASSALAC 2.4
Copyright (c) 2016 Un4seen Developments Ltd. All rights reserved.

Files that you should have found in the BASSALAC package
========================================================
Win32 version
-------------
BASSALAC.TXT    This file
BASSALAC.DLL    BASSALAC module
BASSALAC.CHM    BASSALAC documentation
X64\
  BASSALAC.DLL    64-bit BASSALAC module
C\              C/C++ API...
  BASSALAC.H      BASSALAC C/C++ header file
  BASSALAC.LIB    BASSALAC import library
  X64\
    BASSALAC.LIB    64-bit BASSALAC import library
VB\             Visual Basic API...
  BASSALAC.BAS    BASSALAC Visual Basic module
DELPHI\         Delphi API...
  BASSALAC.PAS    BASSALAC Delphi unit

Linux version
-------------
BASSALAC.TXT    This file
LIBBASSALAC.SO  BASSALAC module
BASSALAC.CHM    BASSALAC documentation
BASSALAC.H      BASSALAC C/C++ header file
X64\
  LIBBASSALAC.SO  64-bit BASSALAC module


What's the point?
=================
BASSALAC is an extension to the BASS audio library, enabling the playing of
ALAC (Apple Lossless Audio Codec) encoded files.

NOTE: The ALAC format is supported as standard by BASS via the OS's codecs
      on OSX and iOS, and also on Windows 10.


Requirements
============
BASS 2.4 is required.


Using BASSALAC
==============
The plugin system (see BASS_PluginLoad) can be used to add WavPack support to
the standard BASS stream (and sample) creation functions. Dedicated WavPack
stream creation functions are also provided by BASSALAC.

The usage information in the BASS.TXT file (from the BASS package) is also
applicable to BASSALAC and other add-ons.

TIP: The BASSALAC.CHM file should be put in the same directory as the BASS.CHM
     file, so that the BASSALAC documentation can be accessed from within the
     BASS documentation.


Latest Version
==============
The latest versions of BASSALAC & BASS can be found at the BASS website:

	www.un4seen.com


Licence
=======
BASSALAC is free to use with BASS.

TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, BASSALAC IS PROVIDED
"AS IS", WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND/OR FITNESS FOR A PARTICULAR PURPOSE. THE AUTHORS SHALL NOT BE HELD
LIABLE FOR ANY DAMAGE THAT MAY RESULT FROM THE USE OF BASSALAC. YOU USE
BASSALAC ENTIRELY AT YOUR OWN RISK.

Usage of BASSALAC indicates that you agree to the above conditions.

All trademarks and other registered names contained in the BASSALAC
package are the property of their respective owners.


History
=======
These are the major (and not so major) changes at each version stage.
There are ofcourse bug fixes and other little improvements made along
the way too! To make upgrading simpler, all functions affected by a
change to the BASSALAC interface are listed.

2.4 - 2/2/2016
--------------
* First release


Credits
=======
BASSALAC contains code from Apple's ALAC decoder (http://alac.macosforge.org/),
Copyright (c) 2011 Apple Inc. And from David Hammerton's ALAC decoder,
Copyright (c) 2005 David Hammerton.

Thanks also to Sebastian Andersson, who created the original ALAC add-on
for BASS.


Bug reports, Suggestions, Comments, Enquiries, etc...
=====================================================
If you have any of the aforementioned please visit the BASS forum at
the website.

