/**
 * 			NativeFmodEx Project
 *
 * Want to use FMOD Ex API (www.fmod.org) in the Java language ? NativeFmodEx is made for you.
 * Copyright @ 2005-2010 Jérôme Jouvie (Jouvieje)
 *
 * Created on 23 feb. 2005
 * @version file v1.5.0
 * @author Jérôme Jouvie (Jouvieje)
 * @site   http://jerome.jouvie.free.fr/
 * @mail   jerome.jouvie@gmail.com
 * 
 * INTRODUCTION
 * FMOD Ex is a music and sound effects system, by Firelight Technologies Pty, Ltd.
 * More informations can be found at:
 * 		http://www.fmod.org/
 * The aim of this project is to provide a java interface for this amazing sound API.
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

package jouvieje.bass.utils.wav;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import jouvieje.bass.utils.FileIOUtils;
import jouvieje.bass.utils.SizeOfPrimitive;

/**
 * Based on FMOD Ex examples
 * Copyright (c), Firelight Technologies Pty, Ltd, 1999-2004.
 */
public class FmtChunk {
	public final static int SIZEOF_FMT_CHUNK = RiffChunk.SIZEOF_RIFF_CHUNK + 4*SizeOfPrimitive.SIZEOF_SHORT + 2*SizeOfPrimitive.SIZEOF_INT;
	
	private final RiffChunk chunk;
	private final short wFormatTag;			/* format type */
	private final short nChannels;			/* number of channels (i.e. mono, stereo...) */
	private final int   nSamplesPerSec;		/* sample rate */
	private final int   nAvgBytesPerSec;		/* for buffer estimation */
	private final short nBlockAlign;			/* block size of data */
	private final short wBitsPerSample;		/* number of bits per sample of mono data */
	
	public FmtChunk(RiffChunk chunk, short wFormatTag, short nChannels, int nSamplesPerSec,
			int nAvgBytesPerSec, short nBlockAlign, short wBitsPerSample) {
		this.chunk = chunk;
		this.wFormatTag = wFormatTag;
		this.nChannels = nChannels;
		this.nSamplesPerSec = nSamplesPerSec;
		this.nAvgBytesPerSec = nAvgBytesPerSec;
		this.nBlockAlign = nBlockAlign;
		this.wBitsPerSample = wBitsPerSample;
	}
	
	public RiffChunk getChunk() {
		return chunk;
	}

	public int getNAvgBytesPerSec() {
		return nAvgBytesPerSec;
	}

	public short getNBlockAlign() {
		return nBlockAlign;
	}

	public short getNChannels() {
		return nChannels;
	}

	public int getNSamplesPerSec() {
		return nSamplesPerSec;
	}

	public short getWBitsPerSample() {
		return wBitsPerSample;
	}

	public short getWFormatTag() {
		return wFormatTag;
	}

	/**
	 * Write an <Code>FmtChunk</code> object into a file.<BR>
	 * Call this methods after <code>WavHearder.write(...)</code>
	 * @param file a file to write in.
	 * @see WavHeader#write(RandomAccessFile, FileIOUtils)
	 */
	public void write(RandomAccessFile file, FileIOUtils io) throws IOException {
		chunk.write(file, io);
		io.writeShort(file, wFormatTag);
		io.writeShort(file, nChannels);
		io.writeInt(file, nSamplesPerSec);
		io.writeInt(file, nAvgBytesPerSec);
		io.writeShort(file, nBlockAlign);
		io.writeShort(file, wBitsPerSample);
	}

	public void put(ByteBuffer buffer) {
		chunk.put(buffer);
		buffer.putShort(wFormatTag);
		buffer.putShort(nChannels);
		buffer.putInt(nSamplesPerSec);
		buffer.putInt(nAvgBytesPerSec);
		buffer.putShort(nBlockAlign);
		buffer.putShort(wBitsPerSample);
	}
}
