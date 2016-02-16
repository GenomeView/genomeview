package net.sf.nameservice;

import org.junit.Assert;
import org.junit.Test;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class TestNameService {

	
	@Test
	public void testDefaultSynonyms(){
		String primary="Acaryochloris marina MBIC11017 chromosome, complete genome. (NC_009925)";
		String alt1="NC_009925";
		String alt2="NC_009925.1";
		
		Assert.assertEquals(primary, NameService.getPrimaryName(alt1));
		Assert.assertEquals(primary, NameService.getPrimaryName(alt2));
		Assert.assertEquals(primary, NameService.getPrimaryName(primary.replace(' ', '_')));
	}
	
	
	@Test
	public void testAddSynonym(){
		String primary="Acaryochloris marina MBIC11017 chromosome, complete genome. (NC_009925)";
	
		
		NameService.addSynonym(primary, "test alternative");
		
		Assert.assertEquals(primary,  NameService.getPrimaryName("test alternative"));
		Assert.assertEquals(primary,  NameService.getPrimaryName("test ALTERNAtive"));
		
	}
}
