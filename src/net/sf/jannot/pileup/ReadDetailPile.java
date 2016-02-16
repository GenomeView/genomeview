/**
 * %HEADER%
 */
package net.sf.jannot.pileup;


/**
 * @author Thomas Abeel
 * 
 */
public class ReadDetailPile extends DoublePile {

	private byte[] bases = null;


	/**
	 * Gives you the actual bases that are present in this pile. This method may
	 * return <code>null</code> if the summary format does not have base
	 * information.
	 * 
	 * @return
	 */
	public byte[] getBases() {
		return bases;
	}

	

	public ReadDetailPile(int pos, float fcov, float rcov, byte[] bases) {
		super(pos,fcov,rcov);
		this.bases = bases;
	}

	/**
	 * Creates a pile up for read data from a pile up file
	 * 
	 * @param pos
	 *            position of the pile
	 * @param reads
	 *            read data
	 */
	public static ReadDetailPile create(int pos, byte[] reads) {
		float rcoverage = 0;
		float fcoverage = 0;
		for (int i = 0; i < reads.length; i++) {
			byte c = reads[i];
			if (c == '^')
				i++;
			else if (c == '-' || c == '+') {
				int jump = reads[++i];
				try {
					i += Integer.parseInt("" + (char) jump);
				} catch (NumberFormatException ne) {
					System.err.println("NFE: " + pos);
				}
			} else if (c == '.' || c == 'A' || c == 'C' || c == 'G' || c == 'T' || c == 'N') {
				fcoverage++;
			} else if (c == ',' || c == 'a' || c == 'c' || c == 'g' || c == 't' || c == 'n') {
				rcoverage++;
			} else {
				// System.err.println("PILE: unknown char: "+(char)c);
			}

		}
		return new ReadDetailPile(pos, fcoverage, rcoverage, reads);
	}

	
	

	
}
