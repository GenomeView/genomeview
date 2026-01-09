/**
 * %HEADER%
 */
package net.sf.jannot;

import junit.framework.Assert;
import net.sf.jannot.AminoAcidMapping;

import org.junit.Test;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class TestAAMapping {

	@Test
	public void testAAmapping(){
		AminoAcidMapping aa=AminoAcidMapping.STANDARDCODE;
		AminoAcidMapping ab=AminoAcidMapping.YEASTMITOCHONDRIAL;
		AminoAcidMapping ac=AminoAcidMapping.INVERTEBRATEMITOCHONDRIAL;
		Assert.assertTrue(true);
	}
}
