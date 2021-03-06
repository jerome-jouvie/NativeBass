/* DO NOT EDIT - AUTOGENERATED */
/**
 * 				NativeBass Project
 *
 * Want to use BASS (www.un4seen.com) in the Java language ? NativeBass is made for you.
 * Copyright @ 2007-2011 Jérôme Jouvie
 *
 * Created on 02 jul. 2007
 * @version file v1.1.1
 * @author Jérôme Jouvie (Jouvieje)
 * @site   http://jerome.jouvie.free.fr/
 * @mail   jerome.jouvie@gmail.com
 * 
 * 
 * INTRODUCTION
 * BASS is an audio library for use in Windows and Mac OSX software.
 * Its purpose is to provide developers with the most powerful and
 * efficient (yet easy to use), sample, stream (MP3, MP2, MP1, OGG, WAV, AIFF,
 * custom generated, and more via add-ons), MOD music (XM, IT, S3M, MOD, MTM, UMX),
 * MO3 music (MP3/OGG compressed MODs),
 * and recording functions. All in a tiny DLL, under 100KB* in size.
 * 
 * BASS official web site :
 * 		http://www.un4seen.com/
 * 
 * 
 * GNU LESSER GENERAL PUBLIC LICENSE
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA 
 */

package jouvieje.bass.defines;

/**
 * MIDI events [NAME] MIDI_EVENT
 */
public interface MIDI_EVENT {
	/**  */
	public static final int MIDI_EVENT_NOTE = 1;
	/**  */
	public static final int MIDI_EVENT_PROGRAM = 2;
	/**  */
	public static final int MIDI_EVENT_CHANPRES = 3;
	/**  */
	public static final int MIDI_EVENT_PITCH = 4;
	/**  */
	public static final int MIDI_EVENT_PITCHRANGE = 5;
	/**  */
	public static final int MIDI_EVENT_DRUMS = 6;
	/**  */
	public static final int MIDI_EVENT_FINETUNE = 7;
	/**  */
	public static final int MIDI_EVENT_COARSETUNE = 8;
	/**  */
	public static final int MIDI_EVENT_MASTERVOL = 9;
	/**  */
	public static final int MIDI_EVENT_BANK = 10;
	/**  */
	public static final int MIDI_EVENT_MODULATION = 11;
	/**  */
	public static final int MIDI_EVENT_VOLUME = 12;
	/**  */
	public static final int MIDI_EVENT_PAN = 13;
	/**  */
	public static final int MIDI_EVENT_EXPRESSION = 14;
	/**  */
	public static final int MIDI_EVENT_SUSTAIN = 15;
	/**  */
	public static final int MIDI_EVENT_SOUNDOFF = 16;
	/**  */
	public static final int MIDI_EVENT_RESET = 17;
	/**  */
	public static final int MIDI_EVENT_NOTESOFF = 18;
	/**  */
	public static final int MIDI_EVENT_PORTAMENTO = 19;
	/**  */
	public static final int MIDI_EVENT_PORTATIME = 20;
	/**  */
	public static final int MIDI_EVENT_PORTANOTE = 21;
	/**  */
	public static final int MIDI_EVENT_MODE = 22;
	/**  */
	public static final int MIDI_EVENT_REVERB = 23;
	/**  */
	public static final int MIDI_EVENT_CHORUS = 24;
	/**  */
	public static final int MIDI_EVENT_CUTOFF = 25;
	/**  */
	public static final int MIDI_EVENT_RESONANCE = 26;
	/**  */
	public static final int MIDI_EVENT_RELEASE = 27;
	/**  */
	public static final int MIDI_EVENT_ATTACK = 28;
	/**  */
	public static final int MIDI_EVENT_REVERB_MACRO = 30;
	/**  */
	public static final int MIDI_EVENT_CHORUS_MACRO = 31;
	/**  */
	public static final int MIDI_EVENT_REVERB_TIME = 32;
	/**  */
	public static final int MIDI_EVENT_REVERB_DELAY = 33;
	/**  */
	public static final int MIDI_EVENT_REVERB_LOCUTOFF = 34;
	/**  */
	public static final int MIDI_EVENT_REVERB_HICUTOFF = 35;
	/**  */
	public static final int MIDI_EVENT_REVERB_LEVEL = 36;
	/**  */
	public static final int MIDI_EVENT_CHORUS_DELAY = 37;
	/**  */
	public static final int MIDI_EVENT_CHORUS_DEPTH = 38;
	/**  */
	public static final int MIDI_EVENT_CHORUS_RATE = 39;
	/**  */
	public static final int MIDI_EVENT_CHORUS_FEEDBACK = 40;
	/**  */
	public static final int MIDI_EVENT_CHORUS_LEVEL = 41;
	/**  */
	public static final int MIDI_EVENT_CHORUS_REVERB = 42;
	/**  */
	public static final int MIDI_EVENT_DRUM_FINETUNE = 50;
	/**  */
	public static final int MIDI_EVENT_DRUM_COARSETUNE = 51;
	/**  */
	public static final int MIDI_EVENT_DRUM_PAN = 52;
	/**  */
	public static final int MIDI_EVENT_DRUM_REVERB = 53;
	/**  */
	public static final int MIDI_EVENT_DRUM_CHORUS = 54;
	/**  */
	public static final int MIDI_EVENT_DRUM_CUTOFF = 55;
	/**  */
	public static final int MIDI_EVENT_DRUM_RESONANCE = 56;
	/**  */
	public static final int MIDI_EVENT_DRUM_LEVEL = 57;
	/**  */
	public static final int MIDI_EVENT_SOFT = 60;
	/**  */
	public static final int MIDI_EVENT_SYSTEM = 61;
	/**  */
	public static final int MIDI_EVENT_TEMPO = 62;
	/**  */
	public static final int MIDI_EVENT_SCALETUNING = 63;
	/**  */
	public static final int MIDI_EVENT_MIXLEVEL = 0x10000;
	/**  */
	public static final int MIDI_EVENT_TRANSPOSE = 0x10001;
}