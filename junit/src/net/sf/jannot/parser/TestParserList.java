package net.sf.jannot.parser;

import org.junit.Assert;
import org.junit.Test;

public class TestParserList {

	
	@Test
	public void testGFF() {
		Parser[]arr=Parser.parsers("test");
		boolean contains=false;
		for(Parser p:arr)
			if(p instanceof GFF3Parser)
				contains=true;
		
		Assert.assertTrue(contains);
		
	}
	
	
	@Test
	public void testVCF() {
		Parser[]arr=Parser.parsers("test");
		boolean contains=false;
		for(Parser p:arr)
			if(p instanceof VCFParser)
				contains=true;
		
		Assert.assertTrue(contains);
		
	}
}
