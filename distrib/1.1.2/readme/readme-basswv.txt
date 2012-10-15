BASSWV 2.4
Copyright (c) 2007-2010 Un4seen Developments Ltd. All rights reserved.

Files that you should have found in the BASSWV package
======================================================
Win32 version
-------------
BASSWV.TXT      This file
BASSWV.DLL      The BASSWV module
BASSWV.CHM      BASSWV documentation
C\              C/C++ API...
  BASSWV.H        BASSWV C/C++ header file
  BASSWV.LIB      BASSWV import library
VB\             Visual Basic API...
  BASSWV.BAS      BASSWV Visual Basic module
DELPHI\         Delphi API...
  BASSWV.PAS      BASSWV Delphi unit

MacOSX version
--------------
BASSWV.TXT      This file
LIBBASSWV.DYLIB The BASSWV module
BASSWV.CHM      BASSWV documentation
BASSWV.H        BASSWV C/C++ header file
MAKEFILE

NOTE: To view the documentation, you will need a CHM viewer, such as CHMOX
      which is included in the BASS package.


What's the point?
=================
BASSWV is an extension to the BASS audio library, enabling the playing of
WavPack encoded files.


Requirements
============
BASS 2.4 is required.


Using BASSWV
============
The plugin system (see BASS_PluginLoad) can be used to add WavPack support to
the standard BASS stream (and sample) creation functions. Dedicated WavPack
stream creation functions are also provided by BASSWV.

Win32 version
-------------
To use BASSWV with Borland C++ Builder, you'll first have to create a
Borland C++ Builder import library for it. This is done by using the
IMPLIB tool that comes with Borland C++ Builder. Simply execute this:

	IMPLIB BASSWVBCB.LIB BASSWV.DLL

... and then use BASSWVBCB.LIB in your projects to import BASSWV.

To use BASSWV with LCC-Win32, you'll first have to create a compatible
import library for it. This is done by using the PEDUMP and BUILDLIB
tools that come with LCC-Win32. Run these 2 commands:

	PEDUMP /EXP BASSWV.LIB > BASSWVLCC.EXP
	BUILDLIB BASSWVLCC.EXP BASSWVLCC.LIB

... and then use BASSWVLCC.LIB in your projects to import BASSWV.

For the BASS functions that return strings (char*), VB users should use
the VBStrFromAnsiPtr function to convert the returned pointer into a VB
string.

TIP: The BASSWV.CHM file should be put in the same directory as the BASS.CHM
     file, so that the BASSWV documentation can be accessed from within the
     BASS documentation.

MacOSX version
--------------
A separate "LIB" file is not required for OSX. Using XCode, you can simply
add the DYLIB file to the project. Or using a makefile, you can build your
programs like this, for example:

	gcc yoursource -L. -lbass -lbasswv -o yourprog

As with LIBBASS.DYLIB, the LIBBASSWV.DYLIB file must be put in the same
directory as the executable (it can't just be somewhere in the path).

LIBBASSWV.DYLIB is a universal binary, with support for both PowerPC and
Intel Macs. If you want PowerPC-only or Intel-only versions, the included
makefile can create them for you, by typing "make ppc" or "make i386".


Latest Version
==============
The latest versions of BASSWV & BASS can be found at the BASS website:

	www.un4seen.com


Licence
=======
BASSWV is free to use with BASS.

TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, BASSWV IS PROVIDED
"AS IS", WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND/OR FITNESS FOR A PARTICULAR PURPOSE. THE AUTHORS SHALL NOT BE HELD
LIABLE FOR ANY DAMAGE THAT MAY RESULT FROM THE USE OF BASSWV. YOU USE
BASSWV ENTIRELY AT YOUR OWN RISK.

Usage of BASSWV indicates that you agree to the above conditions.

All trademarks and other registered names contained in the BASSWV
package are the property of their respective owners.


History
=======
These are the major (and not so major) changes at each version stage.
There are ofcourse bug fixes and other little improvements made along
the way too! To make upgrading simpler, all functions affected by a
change to the BASSWV interface are listed.

2.4.2 - 16/3/2010
-----------------
* Updated for WavPack 4.60.1
* Fix for potential stack overflow with large APE tags

2.4.1 - 22/6/2008
-----------------
* Updated for WavPack 4.50

2.4 - 2/4/2008
--------------
* First release


Credits
=======
WavPack decoding is based on the WavPack library,
Copyright (c) 1998-2007 Conifer Software

Thanks also to Sebastian Andersson, who created the original WavPack
add-on for BASS.


Bug reports, Suggestions, Comments, Enquiries, etc...
=====================================================
If you have any of the aforementioned please visit the BASS forum at
the website.

