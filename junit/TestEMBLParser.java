/**
 * %HEADER%
 */
package junit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Type;
import net.sf.jannot.parser.EMBLParser;

import org.junit.Test;

public class TestEMBLParser {

    @Test
    public void testSample() {
        EMBLParser parser = new EMBLParser();
        try {
            EntrySet entries = parser.parse(new FileInputStream("sample2.embl"),null, null);

//            System.out.println("ACC:" + entries[0].description.getAccessionNumbers());
            List<Feature> fs = entries.getEntry().annotation.getByType(Type.get("CDS"), new Location(0, 12000));
            for (Feature f : fs) {
                // System.out.println(f.type());
                // System.out.println(f.location());
                SortedSet<Location> loc = f.location();
                // for (Location l : loc)
                // System.out.println("\t" + l.getParent());
            }
            FileOutputStream fos = new FileOutputStream("test.embl");
            parser.write(fos, entries.getEntry());
            fos.close();
            // Assert.assertTrue(entries.length == 1);
            // Assert
            // .assertTrue(entries[0].sequence.getSequence().length() == 1859);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
