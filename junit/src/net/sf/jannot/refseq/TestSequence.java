/**
 * %HEADER%
 */
package net.sf.jannot.refseq;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.source.IndexedFastaDataSource;
import net.sf.jannot.source.Locator;

import org.junit.Test;

public class TestSequence {

	@Test
	public void testFaidx() throws URISyntaxException{
		try {
			Locator l = new Locator(
					"http://bioinformatics.psb.ugent.be/downloads/genomeview/genomes/hg19/genome.fasta");
			Locator i = new Locator(
					"http://bioinformatics.psb.ugent.be/downloads/genomeview/genomes/hg19/genome.fasta.fai");

		
			EntrySet es= new IndexedFastaDataSource(l, i).read();
			Sequence seq=es.firstEntry().sequence();
			String tmp = "";
			for (Character c : seq.get(1, 4)) {
				tmp += c;
			}
			Assert.assertEquals(3, tmp.length());
			
			
		} catch (ReadFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSubSequence() {
		MemorySequence a = new MemorySequence("AGTCG");

		Assert.assertEquals("GT", a.subsequence(2, 4).stringRepresentation());
		Assert.assertEquals("AGTCG", a.subsequence(1, 6).stringRepresentation());
		String tmp = "";
		for (Character c : a.get(2, 4)) {
			tmp += c;
		}
		Assert.assertEquals("GT", tmp);
		tmp = "";
		for (Character c : a.get(1, 6)) {
			tmp += c;
		}
		Assert.assertEquals("AGTCG", tmp);

	}

}
