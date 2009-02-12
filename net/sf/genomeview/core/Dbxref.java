/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.util.HashMap;
import java.util.Map;

import be.abeel.util.LineIterator;

/**
 * Class to get the database information for a dbxref.
 * 
 * @author Thomas Abeel
 * 
 */
public class Dbxref {

    static {
        load();
    }

    /* Maps abbrev to dbURL */
    private static Map<String, String> dbxrefmap;

    private static void load() {
        dbxrefmap = new HashMap<String, String>();
        LineIterator it = new LineIterator(Dbxref.class.getResourceAsStream("/conf/dbxref.txt"));
        it.setSkipComments(true);
        it.setSkipBlanks(true);
        String abb = null;
        String url = null;
        for (String line : it) {
            if (line.startsWith("Abbrev:")) {
                abb = line.split(": ")[1];
            }
            if (line.startsWith("Db_URL:")) {
                url = line.split(": ")[1];
                dbxrefmap.put(abb, "http://" + url);
            }
        }

    }
    public static String getURL(String abbreviation){
        System.out.println("dbxref query: "+abbreviation);
        if(dbxrefmap.containsKey(abbreviation))
            return dbxrefmap.get(abbreviation);
        return null;
    }

}
