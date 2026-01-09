/**
 * %HEADER%
 */
package net.sf.jannot.mafix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import net.sf.jannot.Strand;
import net.sf.jannot.alignment.maf.AbstractAlignmentBlock;
import net.sf.jannot.alignment.maf.AbstractMAFMultipleAlignment;
import net.sf.jannot.alignment.maf.LazyAlignmentBlock;
import net.sf.jannot.alignment.maf.LazyAlignmentSequence;
import net.sf.jannot.picard.LineBlockCompressedInputStream;
import net.sf.samtools.seekablestream.SeekableStream;

import org.broad.LRUCache;

/**
 * @author thpar
 * @author Thomas Abeel
 * 
 */
public class IndexedMAF extends AbstractMAFMultipleAlignment {

	private static final long serialVersionUID = -5395637172502888568L;
	private MAFIndex idx;
	private SeekableStream compressedContent;

	private String selectedChrom = null;

	/**
	 * Looks for a compressed version of the requested MAF file and its index
	 * file. Creates an index from this file.
	 * 
	 * @param maf
	 *            name of the compressed MAF stream.
	 * @param index
	 *            name of the index stream
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public IndexedMAF(SeekableStream maf, InputStream index) throws FileNotFoundException, IOException {
		this.compressedContent = maf;
		this.idx = new MAFIndex(index);
	}

	/**
	 * Creates a new IndexedMaf out of an other one, but setting the selected
	 * chromosome and pointing to the content of the original IndexedMaf.
	 * 
	 * @param chr
	 * @param maf
	 */
	public IndexedMAF(String chr, IndexedMAF maf) {
		this.compressedContent = maf.compressedContent;
		this.idx = maf.getIndex();
		setSelectedChrom(chr);
	}

	@Override
	public Iterable<AbstractAlignmentBlock> get(int start, int end) {
		if (selectedChrom != null)
			return get(selectedChrom, start, end);
		else
			return new ArrayList<AbstractAlignmentBlock>();
	}

	private LRUCache<MAFEntry, LazyAlignmentBlock> blockCache = new LRUCache<MAFEntry, LazyAlignmentBlock>(200);

	/**
	 * 
	 * @param fromNuc
	 * @param toNuc
	 * @return list of all AlignmentBlocks that overlap with the requested range
	 *         [fromNuc, toNuc[
	 */
	Iterable<AbstractAlignmentBlock> get(String chr, int fromNuc, int toNuc) {
		SortedSet<MAFEntry> mafEntries = idx.getMAFEntries(chr, fromNuc, toNuc);
		BlockList blockList = new BlockList(mafEntries);
		return blockList;

	}

	@Override
	public Iterable<AbstractAlignmentBlock> get() {
		return get(0, Integer.MAX_VALUE);
	}

	@Override
	public boolean canSave() {
		return false;
	}

	MAFIndex getIndex() {
		return this.idx;
	}

	String getSelectedChrom() {
		return selectedChrom;
	}

	void setSelectedChrom(String selectedChrom) {
		this.selectedChrom = selectedChrom;
	}

	public Set<String> getNames() {
		return this.idx.getNames();
	}

	// 'species' include ALL SPECIES that are compared.
	// in 'names', we're only talking about the reference species or chromosome
	// @Override
	// Collection<String>species(){
	// return Collections.unmodifiableCollection(getNames());
	// }

	private class BlockList implements Iterable<AbstractAlignmentBlock> {

		private SortedSet<MAFEntry> mafEntries;

		BlockList(SortedSet<MAFEntry> mafEntries) {
			this.mafEntries = mafEntries;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<AbstractAlignmentBlock> iterator() {
			return new BlockIterator();
		}

		private class BlockIterator implements Iterator<AbstractAlignmentBlock> {
			private Iterator<MAFEntry> entryIterator;
			private LineBlockCompressedInputStream zr;

			BlockIterator() {
				this.entryIterator = mafEntries.iterator();
				this.zr = new LineBlockCompressedInputStream(compressedContent);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() {
				return entryIterator.hasNext();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#next()
			 */
			@Override
			public AbstractAlignmentBlock next() {
				MAFEntry mafEntry = entryIterator.next();

				// here we can already get the info in mafEntry,
				// without further parsing! (position, length, strands, species,
				// ...)

				LazyAlignmentBlock alBlock = getOrCreate(mafEntry);

				return alBlock;
			}

			private LazyAlignmentBlock getOrCreate(MAFEntry mafEntry) {
				if (!blockCache.containsKey(mafEntry)) {

					LazyAlignmentBlock alBlock = new LazyAlignmentBlock(mafEntry.getStart(),  zr,
							mafEntry.getNucStart(), mafEntry.getNucStart() + mafEntry.getAlignmentLength());

//					List<Strand> strands = mafEntry.getStrands();
					int[] species = mafEntry.getSpecies();

					LazyAlignmentSequence alSeq = new LazyAlignmentSequence(getSelectedChrom(), mafEntry.getNucStart(),
							mafEntry.getAlignmentLength(), Strand.FORWARD, alBlock);
					alBlock.add(alSeq);
					for (int spec : species) {
						int specIdx=(int)Math.abs(spec);
						Strand s=Strand.FORWARD;
						if(spec<0)
							s=Strand.REVERSE;
						String name = idx.getSpeciesName(getSelectedChrom(), specIdx);
						alSeq = new LazyAlignmentSequence(name, mafEntry.getNucStart(), mafEntry.getAlignmentLength(),
								s, alBlock);
						alBlock.add(alSeq);
					}
					blockCache.put(mafEntry,alBlock);
				}
				return blockCache.get(mafEntry);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() {
				// no removing support
			}

		}
	}

	// @Override
	// String toString(){
	// return this.getClass().getName()+"@"+this.hashCode();
	// }

	@Override
	public Collection<String> species() {
		if (this.selectedChrom == null)
			return new ArrayList<String>();
		return idx.getSpecies(selectedChrom);
	}

	@Override
	public int noAlignmentBlocks() {
		return idx.size(selectedChrom);
	}

}
