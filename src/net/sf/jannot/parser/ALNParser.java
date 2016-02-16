/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.alignment.ReferenceSequence;
import net.sf.jannot.alignment.mfa.Alignment;
import net.sf.jannot.alignment.mfa.AlignmentAnnotation;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;
import be.abeel.io.LineIterator;

public class ALNParser extends Parser {

	/**
	 * @param dataKey
	 */
	public ALNParser(DataKey dataKey) {
		super(dataKey);
		
	}

	@Override
	public EntrySet parse(InputStream is,  EntrySet set) {
		if(set==null)
			set=new EntrySet();
		List<Entry> list = new ArrayList<Entry>();
		boolean header=true;
		LineIterator it=new LineIterator(is);
		int index=0;
		
		while(it.hasNext()){
			String line=it.next();
			if(line.length()==0&&header){
				header=false;
				
				
			}
			if(line.length()==0){
				index=0;
			}else{
				String[]arr=line.split("  +");
				if(header){
					list.add(new Entry(arr[0].split(" ")[0].trim()));	
				}
				((MemorySequence)list.get(index).sequence()).addSequence(arr[1]);
				index++;
			}
			
			
			
			
		}
		Entry ref = set.getEntry(list.get(0).getID());
		if (ref != null) {
			List<Alignment> alist = new ArrayList<Alignment>();
			ReferenceSequence rs = new ReferenceSequence((MemorySequence)list.get(0).sequence());
			for (int i = 0; i < list.size(); i++) {
				System.out.println(list.get(i).getID());
				Alignment align = new Alignment(list.get(i).getID(),(MemorySequence)list.get(i).sequence(), rs);
				alist.add(align);
				System.out.println("adding alignment: " + align);
			}
			//ref.alignment.addAll(alist);
			AlignmentAnnotation alignAnnot=new AlignmentAnnotation();
			ref.add(dataKey, alignAnnot);
			alignAnnot.addAll((Iterable<Alignment>)alist);
			
		}
		
		return set;
	}

	

}
