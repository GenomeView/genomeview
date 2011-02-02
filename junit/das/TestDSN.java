package junit.das;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import net.sf.jannot.source.das.DSN;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TestDSN {

	@Test
	public void testEnsemblDSN(){
		try {
			new DSN("http://www.ensembl.org/das/dsn");
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
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
