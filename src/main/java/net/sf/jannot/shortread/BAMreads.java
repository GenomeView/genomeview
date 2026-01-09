/**
 * %HEADER%
 */
package net.sf.jannot.shortread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.source.SAMDataSource;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.util.CloseableIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class BAMreads extends ReadGroup implements Iterable<SAMRecord> {

	private String key;

	private synchronized Iterable<SAMRecord> get(Location r) {
		return qFast( r);

	}

	/* qFast */
	private List<SAMRecord> qFastBuffer = new ArrayList<SAMRecord>();
	private Location qFastBufferLocation = new Location(-5, -5);
	private HashMap<String, SAMRecord> qFastFirst = new HashMap<String, SAMRecord>();
	private HashMap<String, SAMRecord> qFastSecond = new HashMap<String, SAMRecord>();
	private int qFastMaxPairedLenght;

	private Logger log=Logger.getLogger(BAMreads.class.getCanonicalName());
	
	private synchronized Iterable<SAMRecord> qFast(Location r) {
		if (r.start() != qFastBufferLocation.start() || r.end() != qFastBufferLocation.end()) {
			qFastBuffer.clear();
			qFastFirst.clear();
			qFastSecond.clear();
			//System.out.println("QFAST");
			int start = r.start() - 500;
			int end = r.end() + 500;
			CloseableIterator<SAMRecord> it = cqr.query(key, start, end, false);
			if(cqr==null||key==null)
			log.warning("NullPointerDetected: key="+key+"\tcqr="+cqr);
			while (it.hasNext()) {
				try {
					SAMRecord tmp = it.next();
					int aStart = tmp.getAlignmentStart();
					int aEnd = tmp.getAlignmentEnd();
					if (aStart == 0 || aEnd == 0)
						continue;
					if ((aEnd - aStart + 1) > maxLenght)
						maxLenght = (aEnd - aStart + 1);
					qFastBuffer.add(tmp);
					// byte[] seq = tmp.getReadBases();
					// if (complete(seq)) {
					// ExtendedShortRead esr = new ExtendedShortRead(tmp);
					// qFastBuffer.add(esr);
					String name = tmp.getReadName();
					if (ShortReadTools.isPaired(tmp) && tmp.getFirstOfPairFlag())
						qFastFirst.put(name, tmp);
					if (ShortReadTools.isPaired(tmp) && tmp.getSecondOfPairFlag())
						qFastSecond.put(name, tmp);

					if (qFastFirst.containsKey(name) && qFastSecond.containsKey(name)) {
						int len = Math.max(qFastFirst.get(name).getAlignmentEnd()
								- qFastSecond.get(name).getAlignmentStart() + 1, qFastSecond.get(name)
								.getAlignmentEnd() - qFastFirst.get(name).getAlignmentStart() + 1);
						if (len > qFastMaxPairedLenght)
							qFastMaxPairedLenght = len;
					}

				} catch (RuntimeException ex) {
					System.err.println(key);

				}
			}
			qFastBufferLocation = r;
			it.close();
		}

		return qFastBuffer;
	}

	public int getPairLength() {
		return qFastMaxPairedLenght;
	}

	private int maxLenght = 0;

	@Override
	public Iterable<SAMRecord> get(int start, int end) {
		return get(new Location(start, end));
	}

	@Override
	public Iterable<SAMRecord> get() {
		return this;

	}

	static class BAMiterator implements CloseableIterator<SAMRecord> {
		private CloseableIterator<SAMRecord> it;
		private int keyIndex;

		public BAMiterator(CloseableIterator<SAMRecord> in, int key) {
			this.keyIndex = key;
			it = in;
			makeNext();
		}

		@Override
		public boolean hasNext() {
			if (next == null)
				it.close();
			return next != null;
		}

		private void makeNext() {

			if (!it.hasNext()) {
				next = null;
			} else {
				boolean found = false;
				while (!found) {
					try {
						SAMRecord tmp = it.next();
						if (tmp == null || tmp.getReferenceIndex() == keyIndex) {
							next = tmp;
							found = true;
						} else {
							next = null;
						}
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private SAMRecord next = null;

		@Override
		public SAMRecord next() {
			SAMRecord tmp = next;
			makeNext();
			return tmp;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported");

		}

		@Override
		public void close() {
			it.close();

		}
	}

	@Override
	public Iterator<SAMRecord> iterator() {
		return new BAMiterator(cqr.iterator(), keyIndex);
	}

	public String label() {
		return source.getSourceKey().toString();
	}

	private CachingQueryReader cqr;
	private int keyIndex;
	private SAMDataSource source;

	private static final Logger logger = Logger.getLogger(BAMreads.class.getCanonicalName());

	public BAMreads(SAMDataSource source, String key) {
		this.source = source;
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
		this.keyIndex = source.getReader().getFileHeader().getSequenceIndex(key.toString());
		cqr = CachingQueryReader.create(source);
		this.key = key;

	}

	public String getKey() {
		return key;
	}

	@Override
	public int readLength() {
		return maxLenght;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jannot.shortread.ReadGroup#getSecondRead(net.sf.samtools.SAMRecord
	 * )
	 */
	@Override
	public SAMRecord getSecondRead(SAMRecord one) {
		return qFastSecond.get(one.getReadName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jannot.shortread.ReadGroup#getSecondRead(net.sf.samtools.SAMRecord
	 * )
	 */
	@Override
	public SAMRecord getFirstRead(SAMRecord second) {
		return qFastFirst.get(second.getReadName());
	}
}
