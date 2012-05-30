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
public class RiffChunk {
	public final static int SIZEOF_RIFF_CHUNK = 4 * SizeOfPrimitive.SIZEOF_BYTE + SizeOfPrimitive.SIZEOF_INT;

	private final byte[] id; //byte[4]
	private final int size;

	public RiffChunk(byte[] id, int size) {
		this.id = id;
		this.size = size;
	}

	public byte[] getId() {
		return id;
	}

	public int getSize() {
		return size;
	}

	/**
	 * Write an <Code>RiffChunk</code> object into a file.
	 * @param file a file to write in.
	 * @param riffChunk a <code>RiffChunk</code> object.
	 */
	protected void write(RandomAccessFile file, FileIOUtils io) throws IOException {
		io.writeByteArray(file, id);
		io.writeInt(file, size);
	}

	protected void put(ByteBuffer buffer) {
		buffer.put(id);
		buffer.putInt(size);
	}
}
