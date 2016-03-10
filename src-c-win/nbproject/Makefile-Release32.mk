#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
GREP=grep
NM=nm
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=gfortran
AS=as

# Macros
CND_PLATFORM=MinGW-32-Windows
CND_DLIB_EXT=dll
CND_CONF=Release32
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/BASS_3DVECTOR.o \
	${OBJECTDIR}/BASS_BFX_APF.o \
	${OBJECTDIR}/BASS_BFX_AUTOWAH.o \
	${OBJECTDIR}/BASS_BFX_BQF.o \
	${OBJECTDIR}/BASS_BFX_CHORUS.o \
	${OBJECTDIR}/BASS_BFX_COMPRESSOR.o \
	${OBJECTDIR}/BASS_BFX_COMPRESSOR2.o \
	${OBJECTDIR}/BASS_BFX_DAMP.o \
	${OBJECTDIR}/BASS_BFX_DISTORTION.o \
	${OBJECTDIR}/BASS_BFX_ECHO.o \
	${OBJECTDIR}/BASS_BFX_ECHO2.o \
	${OBJECTDIR}/BASS_BFX_ECHO3.o \
	${OBJECTDIR}/BASS_BFX_ENV_NODE.o \
	${OBJECTDIR}/BASS_BFX_FLANGER.o \
	${OBJECTDIR}/BASS_BFX_LPF.o \
	${OBJECTDIR}/BASS_BFX_MIX.o \
	${OBJECTDIR}/BASS_BFX_PEAKEQ.o \
	${OBJECTDIR}/BASS_BFX_PHASER.o \
	${OBJECTDIR}/BASS_BFX_REVERB.o \
	${OBJECTDIR}/BASS_BFX_VOLUME.o \
	${OBJECTDIR}/BASS_BFX_VOLUME_ENV.o \
	${OBJECTDIR}/BASS_CHANNELINFO.o \
	${OBJECTDIR}/BASS_DEVICEINFO.o \
	${OBJECTDIR}/BASS_FILEPROCS.o \
	${OBJECTDIR}/BASS_INFO.o \
	${OBJECTDIR}/BASS_MIDI_DEVICEINFO.o \
	${OBJECTDIR}/BASS_MIDI_EVENT.o \
	${OBJECTDIR}/BASS_MIDI_FONT.o \
	${OBJECTDIR}/BASS_MIDI_FONTINFO.o \
	${OBJECTDIR}/BASS_MIDI_MARK.o \
	${OBJECTDIR}/BASS_MIXER_NODE.o \
	${OBJECTDIR}/BASS_PLUGINFORM.o \
	${OBJECTDIR}/BASS_PLUGININFO.o \
	${OBJECTDIR}/BASS_RECORDINFO.o \
	${OBJECTDIR}/BASS_SAMPLE.o \
	${OBJECTDIR}/Bass.o \
	${OBJECTDIR}/BufferPointerUtils.o \
	${OBJECTDIR}/CallbackManager.o \
	${OBJECTDIR}/Enumeration.o \
	${OBJECTDIR}/HCHANNEL.o \
	${OBJECTDIR}/JavaObject.o \
	${OBJECTDIR}/NativeBass.o \
	${OBJECTDIR}/Pointer.o \
	${OBJECTDIR}/TAG_APE_BINARY.o \
	${OBJECTDIR}/TAG_BEXT.o \
	${OBJECTDIR}/TAG_CART.o \
	${OBJECTDIR}/TAG_CART_TIMER.o \
	${OBJECTDIR}/TAG_CA_CODEC.o \
	${OBJECTDIR}/TAG_FLAC_CUE.o \
	${OBJECTDIR}/TAG_FLAC_CUE_TRACK.o \
	${OBJECTDIR}/TAG_FLAC_CUE_TRACK_INDEX.o \
	${OBJECTDIR}/TAG_FLAC_PICTURE.o \
	${OBJECTDIR}/TAG_ID3.o \
	${OBJECTDIR}/Utils.o


# C Compiler Flags
CFLAGS=-m32

# CC Compiler Flags
CCFLAGS=-m32
CXXFLAGS=-m32

# Fortran Compiler Flags
FFLAGS=-m32

# Assembler Flags
ASFLAGS=--32

# Link Libraries and Options
LDLIBSOPTIONS=lib32/bass.lib lib32/bass_aac.lib lib32/bass_ac3.lib lib32/bass_fx.lib lib32/bass_mpc.lib lib32/bass_spx.lib lib32/bassalac.lib lib32/bassenc.lib lib32/bassflac.lib lib32/bassmidi.lib lib32/bassmix.lib lib32/basswv.lib

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bass.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bass_aac.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bass_ac3.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bass_fx.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bass_mpc.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bass_spx.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bassalac.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bassenc.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bassflac.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bassmidi.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/bassmix.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: lib32/basswv.lib

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}: ${OBJECTFILES}
	${MKDIR} -p ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}
	${LINK.cc} -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT} ${OBJECTFILES} ${LDLIBSOPTIONS} -shared

${OBJECTDIR}/BASS_3DVECTOR.o: BASS_3DVECTOR.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_3DVECTOR.o BASS_3DVECTOR.cpp

${OBJECTDIR}/BASS_BFX_APF.o: BASS_BFX_APF.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_APF.o BASS_BFX_APF.cpp

${OBJECTDIR}/BASS_BFX_AUTOWAH.o: BASS_BFX_AUTOWAH.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_AUTOWAH.o BASS_BFX_AUTOWAH.cpp

${OBJECTDIR}/BASS_BFX_BQF.o: BASS_BFX_BQF.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_BQF.o BASS_BFX_BQF.cpp

${OBJECTDIR}/BASS_BFX_CHORUS.o: BASS_BFX_CHORUS.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_CHORUS.o BASS_BFX_CHORUS.cpp

${OBJECTDIR}/BASS_BFX_COMPRESSOR.o: BASS_BFX_COMPRESSOR.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_COMPRESSOR.o BASS_BFX_COMPRESSOR.cpp

${OBJECTDIR}/BASS_BFX_COMPRESSOR2.o: BASS_BFX_COMPRESSOR2.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_COMPRESSOR2.o BASS_BFX_COMPRESSOR2.cpp

${OBJECTDIR}/BASS_BFX_DAMP.o: BASS_BFX_DAMP.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_DAMP.o BASS_BFX_DAMP.cpp

${OBJECTDIR}/BASS_BFX_DISTORTION.o: BASS_BFX_DISTORTION.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_DISTORTION.o BASS_BFX_DISTORTION.cpp

${OBJECTDIR}/BASS_BFX_ECHO.o: BASS_BFX_ECHO.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_ECHO.o BASS_BFX_ECHO.cpp

${OBJECTDIR}/BASS_BFX_ECHO2.o: BASS_BFX_ECHO2.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_ECHO2.o BASS_BFX_ECHO2.cpp

${OBJECTDIR}/BASS_BFX_ECHO3.o: BASS_BFX_ECHO3.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_ECHO3.o BASS_BFX_ECHO3.cpp

${OBJECTDIR}/BASS_BFX_ENV_NODE.o: BASS_BFX_ENV_NODE.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_ENV_NODE.o BASS_BFX_ENV_NODE.cpp

${OBJECTDIR}/BASS_BFX_FLANGER.o: BASS_BFX_FLANGER.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_FLANGER.o BASS_BFX_FLANGER.cpp

${OBJECTDIR}/BASS_BFX_LPF.o: BASS_BFX_LPF.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_LPF.o BASS_BFX_LPF.cpp

${OBJECTDIR}/BASS_BFX_MIX.o: BASS_BFX_MIX.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_MIX.o BASS_BFX_MIX.cpp

${OBJECTDIR}/BASS_BFX_PEAKEQ.o: BASS_BFX_PEAKEQ.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_PEAKEQ.o BASS_BFX_PEAKEQ.cpp

${OBJECTDIR}/BASS_BFX_PHASER.o: BASS_BFX_PHASER.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_PHASER.o BASS_BFX_PHASER.cpp

${OBJECTDIR}/BASS_BFX_REVERB.o: BASS_BFX_REVERB.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_REVERB.o BASS_BFX_REVERB.cpp

${OBJECTDIR}/BASS_BFX_VOLUME.o: BASS_BFX_VOLUME.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_VOLUME.o BASS_BFX_VOLUME.cpp

${OBJECTDIR}/BASS_BFX_VOLUME_ENV.o: BASS_BFX_VOLUME_ENV.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_BFX_VOLUME_ENV.o BASS_BFX_VOLUME_ENV.cpp

${OBJECTDIR}/BASS_CHANNELINFO.o: BASS_CHANNELINFO.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_CHANNELINFO.o BASS_CHANNELINFO.cpp

${OBJECTDIR}/BASS_DEVICEINFO.o: BASS_DEVICEINFO.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_DEVICEINFO.o BASS_DEVICEINFO.cpp

${OBJECTDIR}/BASS_FILEPROCS.o: BASS_FILEPROCS.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_FILEPROCS.o BASS_FILEPROCS.cpp

${OBJECTDIR}/BASS_INFO.o: BASS_INFO.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_INFO.o BASS_INFO.cpp

${OBJECTDIR}/BASS_MIDI_DEVICEINFO.o: BASS_MIDI_DEVICEINFO.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_MIDI_DEVICEINFO.o BASS_MIDI_DEVICEINFO.cpp

${OBJECTDIR}/BASS_MIDI_EVENT.o: BASS_MIDI_EVENT.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_MIDI_EVENT.o BASS_MIDI_EVENT.cpp

${OBJECTDIR}/BASS_MIDI_FONT.o: BASS_MIDI_FONT.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_MIDI_FONT.o BASS_MIDI_FONT.cpp

${OBJECTDIR}/BASS_MIDI_FONTINFO.o: BASS_MIDI_FONTINFO.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_MIDI_FONTINFO.o BASS_MIDI_FONTINFO.cpp

${OBJECTDIR}/BASS_MIDI_MARK.o: BASS_MIDI_MARK.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_MIDI_MARK.o BASS_MIDI_MARK.cpp

${OBJECTDIR}/BASS_MIXER_NODE.o: BASS_MIXER_NODE.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_MIXER_NODE.o BASS_MIXER_NODE.cpp

${OBJECTDIR}/BASS_PLUGINFORM.o: BASS_PLUGINFORM.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_PLUGINFORM.o BASS_PLUGINFORM.cpp

${OBJECTDIR}/BASS_PLUGININFO.o: BASS_PLUGININFO.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_PLUGININFO.o BASS_PLUGININFO.cpp

${OBJECTDIR}/BASS_RECORDINFO.o: BASS_RECORDINFO.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_RECORDINFO.o BASS_RECORDINFO.cpp

${OBJECTDIR}/BASS_SAMPLE.o: BASS_SAMPLE.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BASS_SAMPLE.o BASS_SAMPLE.cpp

${OBJECTDIR}/Bass.o: Bass.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/Bass.o Bass.cpp

${OBJECTDIR}/BufferPointerUtils.o: BufferPointerUtils.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/BufferPointerUtils.o BufferPointerUtils.cpp

${OBJECTDIR}/CallbackManager.o: CallbackManager.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/CallbackManager.o CallbackManager.cpp

${OBJECTDIR}/Enumeration.o: Enumeration.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/Enumeration.o Enumeration.cpp

${OBJECTDIR}/HCHANNEL.o: HCHANNEL.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/HCHANNEL.o HCHANNEL.cpp

${OBJECTDIR}/JavaObject.o: JavaObject.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/JavaObject.o JavaObject.cpp

${OBJECTDIR}/NativeBass.o: NativeBass.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/NativeBass.o NativeBass.cpp

${OBJECTDIR}/Pointer.o: Pointer.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/Pointer.o Pointer.cpp

${OBJECTDIR}/TAG_APE_BINARY.o: TAG_APE_BINARY.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_APE_BINARY.o TAG_APE_BINARY.cpp

${OBJECTDIR}/TAG_BEXT.o: TAG_BEXT.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_BEXT.o TAG_BEXT.cpp

${OBJECTDIR}/TAG_CART.o: TAG_CART.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_CART.o TAG_CART.cpp

${OBJECTDIR}/TAG_CART_TIMER.o: TAG_CART_TIMER.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_CART_TIMER.o TAG_CART_TIMER.cpp

${OBJECTDIR}/TAG_CA_CODEC.o: TAG_CA_CODEC.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_CA_CODEC.o TAG_CA_CODEC.cpp

${OBJECTDIR}/TAG_FLAC_CUE.o: TAG_FLAC_CUE.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_FLAC_CUE.o TAG_FLAC_CUE.cpp

${OBJECTDIR}/TAG_FLAC_CUE_TRACK.o: TAG_FLAC_CUE_TRACK.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_FLAC_CUE_TRACK.o TAG_FLAC_CUE_TRACK.cpp

${OBJECTDIR}/TAG_FLAC_CUE_TRACK_INDEX.o: TAG_FLAC_CUE_TRACK_INDEX.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_FLAC_CUE_TRACK_INDEX.o TAG_FLAC_CUE_TRACK_INDEX.cpp

${OBJECTDIR}/TAG_FLAC_PICTURE.o: TAG_FLAC_PICTURE.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_FLAC_PICTURE.o TAG_FLAC_PICTURE.cpp

${OBJECTDIR}/TAG_ID3.o: TAG_ID3.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/TAG_ID3.o TAG_ID3.cpp

${OBJECTDIR}/Utils.o: Utils.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.cc) -O2  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/Utils.o Utils.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libsrc-c-win.${CND_DLIB_EXT}

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
