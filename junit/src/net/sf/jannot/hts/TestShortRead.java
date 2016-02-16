/**
 * %HEADER%
 */
package net.sf.jannot.hts;

import junit.framework.Assert;
import net.sf.jannot.shortread.BasicShortRead;
import net.sf.jannot.shortread.ShortRead;

import org.junit.Test;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class TestShortRead {

	@Test
	public void testShortRead(){
		String seq="TGGGTTTGGCTGGTGCTAGCTCGGCGCCGTATGCGG";
		ShortRead sr=new BasicShortRead(seq.toCharArray(),16,true);
		for(int i=1;i<=seq.length();i++){
			char c=sr.getNucleotide(i);
			System.out.println(i+"\t"+seq.charAt(i-1)+"\t" +c);
			Assert.assertEquals(seq.charAt(i-1), c);
			
		}
	}
	
}
