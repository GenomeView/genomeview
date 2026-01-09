/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;
import net.sf.jannot.refseq.MemorySequence;
import be.abeel.io.LineIterator;

/*
 * http://www.ncbi.nlm.nih.gov/Sitemap/samplerecord.html
 * 
 * http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nucleotide&rettype=gb&id=NM_023037
 */
public class GenbankParser extends Parser {

	/**
	 * @param dataKey
	 */
	public GenbankParser() {
		this(null);
		
	}
	/**
	 * @param dataKey
	 */
	public GenbankParser(DataKey key) {
		super(key);
		
	}

	private Feature lastFeature = null;

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		Entry e = null;
		LineIterator it = new LineIterator(is);
		String locus=null;
		String definition=null;
		String version=null;
		boolean featureMode = false;
		boolean seqMode = false;
		for (String line : it) {
			if (line.startsWith("//")) {
				seqMode = false;
			}
			if (seqMode) {
				processSequenceLine(line, e);
			}
			if (line.startsWith("ORIGIN")) {
				addQualifiers(e);
				seqMode = true;
				featureMode = false;
			}

			if (featureMode) {
				if (line.startsWith("BASE COUNT")) {
					//Ignore line
				}else{
					processFeatureLine(line, e,it);
				}
				
			}
			if (line.startsWith("FEATURES")) {
				featureMode = true;
				seqMode = false;
			}

			
			if (line.startsWith("LOCUS")) {
				String[] arr = line.trim().split("[ ]+");
//				e = set.getOrCreateEntry(arr[1]);
				locus=arr[1];
				
			}
			if (line.startsWith("DEFINITION")) {
				definition=line.substring(10).trim();
				
			}
			if (line.startsWith("VERSION")) {
				String[] arr = line.trim().split("\\s+",2);
//				e = set.getOrCreateEntry(arr[1]);
				version=arr[1];
//				if(version!=null)
				e.description.put("VERSION", version);
				
			}
			if (line.startsWith("ACCESSION")) {
				
				String[] arr = line.trim().split("[ ]+");
				// e.description.setID(arr[1]);
				if(arr.length==1){
					arr=new String[2];
					arr[1]=locus;
				}
				e = set.getOrCreateEntry(arr[1]);
//				
				if(locus!=null)
					e.description.put("LOCUS", locus);
				if(definition!=null)
					e.description.put("DEFINITION", definition);
				
			}

		}
		return set;
	}

	private StringBuffer qualifierBuffer = new StringBuffer();

	private void processFeatureLine(String line, Entry e, LineIterator it) {

		if (line.startsWith("                     ")) {
			if(line.trim().startsWith("/"))
				qualifierBuffer.append("\n");
			qualifierBuffer.append(line.trim());
		} else {
			if (lastFeature != null) {
				addQualifiers(e);
			}
			lastFeature = new Feature();
			String nl=it.peek();
			while(nl.startsWith("                     ")&&!nl.trim().startsWith("/")){
				line+=it.next().trim();
				nl=it.peek();
			}
			
			String[] arr = line.trim().split(" [ ]+");
			try{
			lastFeature.setType(Type.get(arr[0]));
			lastFeature.setStrand(ParserTools.getStrand(arr[1]));
			// System.out.println(arr[1]+"\t"+e.annotation.noFeatures());
			lastFeature.setLocation(ParserTools.parseLocation(arr[1]));
			}catch(ArrayIndexOutOfBoundsException aei){
				aei.printStackTrace();
				System.err.println("Offending line: "+line +" for entry "+e);
			}catch(NumberFormatException nfe){
				nfe.printStackTrace();
				System.err.println("Offending line: "+line +" for entry "+e);
			}

		}

	}

	private void addQualifiers(Entry e) {
		String[] arr = qualifierBuffer.toString().split("\n");
		for (int i = 1; i < arr.length; i++) {
			if (arr[i].contains("=")) {

				String[] qarr = arr[i].split("=");
				lastFeature.addQualifier(qarr[0].substring(1), qarr[1]);
			}

			else
				lastFeature.addQualifier(arr[i].substring(1),null);

		}
		qualifierBuffer = new StringBuffer();
		MemoryFeatureAnnotation fa = e.getMemoryAnnotation(lastFeature.type());
		fa.add(lastFeature);
		// e.annotation.add(lastFeature);

	}

	private void processSequenceLine(String line, Entry e) {
		String seq = cleanSeq(line);
		((MemorySequence) e.sequence()).addSequence(seq);

	}

	private String cleanSeq(String line) {
		String out = line.replaceAll("[0-9 ]", "");
		return out;
	}

}
