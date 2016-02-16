/**
 * %HEADER%
 */
package net.sf.jannot.parser.software;

import java.io.InputStream;

import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.parser.Parser;
import be.abeel.io.LineIterator;

public class BlastM8Parser extends Parser {

    /**
	 * @param dataKey
	 */
	public BlastM8Parser() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	@Override
    public EntrySet parse(InputStream is, EntrySet set) {
    	if(set==null)
    		set=new EntrySet();
//        Entry[] out = new Entry[1];
//        // try {
//        out[0] = new Entry(source);
        LineIterator it = new LineIterator(is);
        it.setSkipComments(true);
        Type t=Type.get("NCBI Blast hit");
        for (String line : it) {
            String[] arr = line.split("\t");
            Feature f = new Feature();
            f.setType(t);
            f.setScore(Double.parseDouble(arr[10]));
            int start = Integer.parseInt(arr[6]);
            int end = Integer.parseInt(arr[7]);
            Strand s = Strand.FORWARD;
            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
                s = Strand.REVERSE;
            }
            f.setStrand(s);
            f.addLocation(new Location(start, end));
            f.addQualifier("subject id", arr[1]);
            f.addQualifier("% identity", arr[2]);
            // f.addQualifier(new Qualifier("% positives", arr[3]));
            f.addQualifier("alignment length", arr[3]);
            f.addQualifier("bit score", arr[11]);
            MemoryFeatureAnnotation fa=set.iterator().next().getMemoryAnnotation(t);
            fa.add(f);

        }

        return set;
    }


}
