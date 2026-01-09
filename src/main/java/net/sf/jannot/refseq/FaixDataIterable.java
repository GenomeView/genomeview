/**
 * %HEADER%
 */
package net.sf.jannot.refseq;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.samtools.seekablestream.SeekableStream;

/**
 * @author mavoo
 * @author Thomas Abeel
 * 
 */
public class FaixDataIterable implements Iterable<Character> {

	private SeekableStream data;

	private int qStart;

	private int qEnd;

	private long start;

	private long len;

	private long lineLen;

	private long byteLen;

	public FaixDataIterable(SeekableStream data, int start, int end, long start2, long len, long lineLen, long byteLen) {
		this.data = data;
		this.qStart = start;
		this.qEnd = end;
		this.start = start2;
		this.len = len;

		this.lineLen = lineLen;
		this.byteLen = byteLen;
	}

	public Iterator<Character> iterator() {
		return new FaixDataIterator(data, qStart, qEnd, start, len, lineLen, byteLen);
	}
	private static Logger log = Logger.getLogger(FaixDataIterable.class.getCanonicalName());

	private class FaixDataIterator implements Iterator<Character> {
		private static final int BUFFERSIZE = 1024;// *1024;
		private SeekableStream data;
		/* Contains nucleotides */
		private byte[] buffer = new byte[BUFFERSIZE];
		// private long[] coordinates;
		private long qPosition = 0L;

		/* Make sure the first time, we read something */
		private int bufferIndex = buffer.length + 1;
		private long currentFilePos;
		private long byteLen;
		private long lineLen;
		private long start;


		/* Incoming qStart and qEnd coordinates are 1-based */
		public FaixDataIterator(SeekableStream data, int qStart, int qEnd, long start, long len, long lineLen,
				long byteLen) {
			this.data = data;
			currentFilePos = translate(qStart<1?1:qStart, start, lineLen, byteLen);
			
			this.start = start;
			this.lineLen = lineLen;
			this.byteLen = byteLen;
			qPosition = qStart;
		}

		private long translate(int position, long start, long lineLen, long byteLen) {
			position--;
			long lines = position / lineLen;
			long lineOffset = position - lines * lineLen;
			return start + lines * byteLen + lineOffset;

		}

		public boolean hasNext() {
			return qPosition < qEnd + 1;
		}

		
		public Character next() {
			if (qPosition < 1) {
				qPosition++;
				return '_';
			}
			if (bufferIndex >= buffer.length)
				try {
					refill();
				} catch (IOException e) {
					log.log(Level.SEVERE,"Exception while refilling buffer",e);
					throw new RuntimeException(e);
				}
			char retVal = (char) buffer[bufferIndex++];
			if(retVal>='a'&&retVal<='z')
				retVal+='A'-'a';
			qPosition++;
			return retVal;

		}

		/* Fill the buffer with data */
		private void refill() throws IOException {
			long endFilePos = translate((int) qPosition + 1024, start, lineLen, byteLen);
			byte[] tmpBuffer = new byte[(int) (endFilePos - currentFilePos)];
			data.seek(currentFilePos);
			data.read(tmpBuffer);
			currentFilePos = endFilePos;
			int bIdx = 0;
			for (int i = 0; i < tmpBuffer.length; i++) {
				if (Character.isLetter((char) tmpBuffer[i]) && bIdx < buffer.length)
					buffer[bIdx++] = tmpBuffer[i];

			}
			bufferIndex = 0;

		}

		public void remove() {
			throw new UnsupportedOperationException("Remove not supported for FaixDataIterator.");
		}

		private void close() {
			//
		}
	}
}
