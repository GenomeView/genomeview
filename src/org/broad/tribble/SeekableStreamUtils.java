package org.broad.tribble;

import java.io.EOFException;
import java.io.IOException;

import net.sf.samtools.seekablestream.SeekableStream;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class SeekableStreamUtils {

	public static void readFully(byte[] buffer, SeekableStream fis) throws IOException {
	
		int len = buffer.length;
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = fis.read(buffer, n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}
}
