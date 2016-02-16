/**
 * %HEADER%
 */
package net.sf.jannot.utils;

import junit.framework.Assert;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.utils.SequenceTools;

import org.junit.Test;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class TestSequenceTools {

	

	@Test
	public void testReverseComplementSmall() {
		MemorySequence a = new MemorySequence();
		a.setSequence("AACCGGTTACTGACTG");
		MemorySequence b = (MemorySequence)SequenceTools.reverseComplement(a);
		for (int i = 1; i <= 16; i++) {
			Assert.assertEquals(a.getNucleotide(i), b
					.getReverseNucleotide(17 - i));
		}
		System.out.println(a);
		System.out.println(b);
	}
	@Test
	public void testReverseComplementOdd() {
		MemorySequence a = new MemorySequence();
		a.setSequence("AACCGGTTACTGACT");
		MemorySequence b = (MemorySequence)SequenceTools.reverseComplement(a);
		for (int i = 1; i <= 15; i++) {
			Assert.assertEquals(a.getNucleotide(i), b
					.getReverseNucleotide(16 - i));
		}
		System.out.println(a);
		System.out.println(b);
	}
}
