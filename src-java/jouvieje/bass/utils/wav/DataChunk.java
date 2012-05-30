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

/**
 * Based on FMOD Ex examples
 * Copyright (c), Firelight Technologies Pty, Ltd, 1999-2004.
 */
public class DataChunk {
	public final static int SIZEOF_DATA_CHUNK = RiffChunk.SIZEOF_RIFF_CHUNK;

	private RiffChunk chunk = null;

	public DataChunk(RiffChunk chunk) {
		this.chunk = chunk;
	}

	public RiffChunk getChunk() {
		return chunk;
	}

	public void setChunk(RiffChunk chunk) {
		this.chunk = chunk;
	}

	/**
	 * Write a <Code>DataChunk</code> object into a file.<BR>
	 * Call this methods after <code>FmtChunk.write(...)</code><BR>
	 * After writting <code>DataChunk</code>, you can write datas.
	 * @param raf a file to write in.
	 * @see FmtChunk#write(RandomAccessFile, FileIOUtils)
	 */
	public void write(RandomAccessFile raf, FileIOUtils io) throws IOException {
		chunk.write(raf, io);
	}

	public void put(ByteBuffer buffer) {
		chunk.put(buffer);
	}
}
