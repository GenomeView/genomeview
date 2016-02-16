/**
 * %HEADER%
 */
package net.sf.jannot.alignment.maf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.jannot.Strand;
import net.sf.jannot.picard.LineBlockCompressedInputStream;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.utils.SequenceTools;

/**
 * 
 * @author Thomas Abeel
 * @author thpar
 * 
 */
public class LazyAlignmentBlock extends AbstractAlignmentBlock {

	private long offsetStart;

	private LineBlockCompressedInputStream zr;

	private ArrayList<AbstractAlignmentSequence> list = new ArrayList<AbstractAlignmentSequence>();

	public LazyAlignmentBlock(long offsetStart, LineBlockCompressedInputStream zr,int start,int end) {
		super(start,end);
		this.offsetStart = offsetStart;
		this.zr = zr;
		
	}

	public void add(AbstractAlignmentSequence as) {
		list.add(as);
	}

//	@Override
//	public int translate(int pos) {
//		if (position == null){
////			System.out.println("Translate lazy load");
//			lazyLoad();
////			System.out.println("Pos after: "+position);
//			
//		}
//		if (pos < position.length)
//			return position[pos];
//		else {
//			System.err.println("Wrong translate: " + position.length + "\t" + pos);
//			return position[position.length - 1];
//		}
//	}

	private boolean lazyLoading = false;

	
	/**
	 * Load the actual alignment block from the zipped maf file and fill in the
	 * gaps in the alignment sequences
	 */
	public synchronized void lazyLoad() {
		if (list.size()==0||lazyLoading)
			return;
		lazyLoading = true;

//		System.out.println("  maf.bgz access ... ");
		// make a mapping id -> alignment sequence
		Map<String, AbstractAlignmentSequence> idMap = new HashMap<String, AbstractAlignmentSequence>();
		for (AbstractAlignmentSequence s : list) {
			idMap.put(s.id, s);
		}

		try {
			zr.seek(offsetStart);
			// long lineNumber = zr.getFilePointer();
			// while (lineNumber >= 0 && lineNumber <= offsetEnd) {
			String line = zr.readLine();
			// System.err.println("Processing block");
			while (!line.startsWith("#") && !line.isEmpty()) {
				// System.err.println("\t" + line);
				String[] cols = line.split("\\s+");
				if (cols.length == 7) {
					String type = cols[0];
					if (type.equals("s")) {
						String id = cols[1];
						LazyAlignmentSequence alSeq = (LazyAlignmentSequence) idMap.get(id);
						
						if (alSeq != null) {
//							System.err.println("Loading sequence for: " + id);
							MemorySequence seq = new MemorySequence(cols[6]);
							alSeq.noNucleotides = Integer.parseInt(cols[3]);
							int startNuc = Integer.parseInt(cols[2]);
							int totalLength = Integer.parseInt(cols[5]);
							if (alSeq.strand == Strand.FORWARD) {
								alSeq.start = startNuc;
								alSeq.setSeq(seq);// = seq;
							} else {
								alSeq.start = totalLength - startNuc - alSeq.noNucleotides;
								alSeq.setSeq(SequenceTools.reverseComplement(seq));//

								// ;
							}
						
//							list.add(alSeq);
						} else {
							System.err.println("LAS is not in map! " + id);
						}
					}
				}
				line = zr.readLine();
				// }
				// lineNumber = zr.getFilePointer();
			}

//			AbstractAlignmentSequence as = list.get(0);
//			this.loc = new Location(as.start(), as.end());
//			 System.err.println("Initing with: " + as);
//			 System.err.println("\tseq=" + as.seq());
//			super.initPosition(as);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<AbstractAlignmentSequence> iterator() {
		return list.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jannot.alignment.maf.AbstractAlignmentBlock#getAlignmentSequence
	 * (int)
	 */
	@Override
	public AbstractAlignmentSequence getAlignmentSequence(int i) {
		return list.get(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.alignment.maf.AbstractAlignmentBlock#size()
	 */
	@Override
	public int size() {
		return list.size();
	}

}
