/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.util.BitSet;
/**
 * 
 * @author Thomas Abeel
 *
 */
class TilingMatrix {
		/* One place Bitset per pixel column on screen */
		private BitSet[] tc;
		private int visibleLength;

		/* Get a free line for the tranlated genomic coordinate */
		int getFreeLine(int startX) {
			int transX = translate(startX);
			if (transX >= 0 && transX < tc.length)
				return tc[transX].nextClearBit(0);
			else
				return tc[0].nextClearBit(0);

		}

		private int translate(int startX) {
			return (int) (startX * ((double) tc.length / (double) visibleLength));

		}

		public TilingMatrix(double screenWidth, int visibleLength, int maxStack) {
			this.visibleLength = visibleLength;
			tc = new BitSet[(int) screenWidth + 1];
			for (int i = 0; i < tc.length; i++) {
				tc[i] = new BitSet(maxStack);
			}
		}

		public int length() {
			return tc.length;
		}

		/*
		 * set from all x [from,to[
		 */
		public void rangeSet(int fromX, int toX, int y) {
			fromX = translate(fromX);
			toX = translate(toX);
			if (fromX < 0)
				fromX = 0;
			if (toX > tc.length)
				toX = tc.length;
			for (int i = fromX; i < toX; i++) {
				if (i >= 0 && i < tc.length)
					tc[i].set(y);

			}

		}

	}