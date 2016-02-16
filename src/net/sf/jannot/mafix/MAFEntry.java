/**
 * %HEADER%
 */
package net.sf.jannot.mafix;


/**
 * The offset pair (start and end of an aligment) in a block zipped file and the
 * length of the represented alignment in nucleotides.
 * 
 * @author thpar
 * @author Thomas Abeel
 * 
 */
class MAFEntry implements Comparable<MAFEntry> {

	/**
	 * @return the start
	 */
	long getStart() {
		return start;
	}

	/**
	 * Start of the alignment (nucleotides are counted one based)
	 */
	private int nucStart;

	/**
	 * Length of the represented alignment
	 */
	private int alignmentLength;


	/**
	 * The list of species involved in this particular alignment (the reference
	 * chromosome NOT included)
	 */
	private int[] species;

	/**
	 * Begin offset in the block zipped file
	 */

	private long start;



	/**
	 * 
	 * @param allSpecies
	 * @param specCode
	 *            comma-separated list of species numbers
	 */
	void setSpeciesEncoding(String specCode) {
		if(specCode.trim().equals(".")){
			species=new int[0];
			return;
		}
		String[] specsArray = specCode.split(",");
		species = new int[specsArray.length];
		for (int i = 0; i < specsArray.length; i++)
			species[i] = Integer.parseInt(specsArray[i]);

	}

	/**
	 * Does the alignment this entry represent overlap with the range defined by
	 * [rangeFrom, rangeTo[ ?
	 * 
	 * @param rangeFrom
	 * @param rangeTo
	 * @return
	 */
	boolean overlaps(int rangeFrom, int rangeTo) {
		int nucEnd = nucStart + alignmentLength - 1;

		return ((nucStart < rangeFrom && nucEnd >= rangeFrom) || nucStart >= rangeFrom && nucStart < rangeTo);

	}

	@Override
	public int compareTo(MAFEntry o) {
		if (nucStart == o.nucStart) {
			if (alignmentLength == o.alignmentLength)
				return 0;
			else if (alignmentLength < o.alignmentLength)
				return -1;
			else
				return 1;
		} else {
			if (nucStart < o.nucStart)
				return -1;
			else
				return 1;
		}
	}

	@Override
	public	boolean equals(Object o) {
		if (!(o instanceof MAFEntry))
			return false;
		MAFEntry oo = (MAFEntry) o;
		int comp = this.compareTo(oo);
		return comp == 0;
	}

	int getNucStart() {
		return nucStart;
	}

	void setNucStart(int nucStart) {
		this.nucStart = nucStart;
	}

	int getAlignmentLength() {
		return alignmentLength;
	}

	void setAlignmentLength(int alignmentLength) {
		this.alignmentLength = alignmentLength;
	}

	void setOffsetPair(long start) {
		this.start = start;
	}


	int[] getSpecies() {
		return species;
	}

	/**
	 * @param speciesLocationMap
	 * @return
	 */
	String getEncodedSpecies() {

		if (species.length == 0)
			return ".";

		StringBuffer list = new StringBuffer();
		for (int spec : species) {
			list.append(spec);
			list.append(",");

		}
		if (list.length() > 0) {
			list.deleteCharAt(list.length() - 1);
		}
		return list.toString();

	}

	/**
	 * @param encodeSpecies
	 */
	void setSpecies(int[] encodeSpecies) {
		this.species = encodeSpecies;

	}


}
