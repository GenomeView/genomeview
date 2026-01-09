/**
 * %HEADER%
 */
package net.sf.jannot.refseq;

import net.sf.jannot.refseq.FaidxIndex.IndexEntry;
import net.sf.samtools.seekablestream.SeekableStream;


/**
 * 
 * @author Thomas Abeel
 *
 */
public class FaidxData extends Sequence {


	private SeekableStream data = null;
	private IndexEntry idx;

	
	/**
	 * @param index
	 * @param content
	 * @param name
	 */
	public FaidxData(FaidxIndex index, SeekableStream content, String name) {

		this.data = content;
		this.idx = index.get(name);


	}

	@Override
	public Iterable<Character> get(int start, int end) {
		return new FaixDataIterable(data, start, end-1, idx.start, idx.len, idx.lineLen, idx.byteLen);
	}

	@Override
	public Iterable<Character> get() {
		return get(1,(int)(idx.len+1));
		//return new FaixDataIterable(data, 1, (int) this.len, this.start, this.len, this.lineLen, this.byteLen);
	}

	@Override
	public int size() {
		return (int) idx.len;
	}

}
