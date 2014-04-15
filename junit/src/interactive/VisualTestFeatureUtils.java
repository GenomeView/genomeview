package interactive;

import junit.framework.Assert;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.viztracks.annotation.FeatureUtils;
import net.sf.jannot.Feature;

import org.junit.Test;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class VisualTestFeatureUtils {
	
	@Test
	public void testDisplayName(){
		Configuration.set("track:feature:labelIdentifiers","protein_id,Name,ID,gene,label,note");
		Feature f=new Feature();
		f.addQualifier("protein_id", "protein");
		f.addQualifier("ID", "ID-display");
		f.addQualifier("note", "note-display");
		
		Assert.assertEquals("protein",FeatureUtils.displayName(f));
		f.removeQualifier("protein_id");
		Assert.assertEquals("ID-display",FeatureUtils.displayName(f));
		f.removeQualifier("ID");
		Assert.assertEquals("note-display",FeatureUtils.displayName(f));
		
		
	}

}
