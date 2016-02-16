/**
 * %HEADER%
 */
package net.sf.jannot.refseq;



import static org.junit.Assert.assertEquals;
import net.sf.jannot.Entry;
import net.sf.jannot.refseq.MemorySequence;

import org.junit.Test;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class TestMemorySequence {
	
	/**
	 * When adding a String to an empty sequence, the sequence should
	 * be extended with this String.
	 * 
	 */
	
	@Test
	public void addSequenceTest(){
		Entry entry = new Entry("test");
		MemorySequence seq =(MemorySequence) entry.sequence();
		String seqString = "actgactg";
		seq.addSequence(seqString);
		assertEquals(seqString.toUpperCase(), entry.sequence().toString());
	}
}
