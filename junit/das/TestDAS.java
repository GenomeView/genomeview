package junit.das;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.das.DAS;
import net.sf.jannot.source.das.DAS.EntryPoint;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TestDAS {

	

	
	
	

	@Test
	public void testEntry() {
		System.out.println("Test Entry:");
		System.out.println("-----------");
		try {
			DAS ensembl = new DAS("http://www.ensembl.org/");
			for (String ref : ensembl.getReferences()) {
//				System.out.println("Ref: " + ref);
				if (ref.contains("Homo")) {
//					System.out.println("Ref: " + ref);
					List<EntryPoint> list = ensembl.getEntryPoints(ref);
					ensembl.setEntryPoint(list.get(31));
					ensembl.setReference(ref);
					EntrySet set=ensembl.read();
					System.out.println(set);

				}

			}
			

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		} catch (ReadFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
