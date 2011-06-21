package junit;

import net.sf.genomeview.data.Model;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.Locator;

import org.junit.Assert;
import org.junit.Test;

public class TestComplex {

	@Test
	public void testTooManyFeatures() {
		Model model = new Model(null, null);
		try {
			DataSource ds = DataSourceFactory.create(new Locator(
					"http://www.broadinstitute.org/software/genomeview/demo/b_anthracis/bantracis.gff"), null);
			EntrySet es = ds.read();
			MemoryFeatureAnnotation m = es.getEntry().getMemoryAnnotation(Type.get("CDS"));
			System.out.println("Entry: "+es.getEntry());
			System.out.println("Max len: "+es.getEntry().getMaximumLength());
			System.out.println(m.getEstimateCount(new Location(1, 200)));
			System.out.println("Number of features: "+m.cachedCount());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
