/**
 * %HEADER%
 */
package net.sf.jannot.source.das;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.source.DataSource;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import be.abeel.net.URIFactory;

public class DAS extends DataSource {

	private String serverPrefix;

	private DSN dsn = null;

	public DAS(String serverPrefix) throws MalformedURLException, ParserConfigurationException, SAXException, IOException, URISyntaxException {
		super(null);
		this.serverPrefix = serverPrefix;
		dsn = new DSN(serverPrefix);
	}

	private DSN getDSN() {
		return dsn;
	}

	private EntryPointParser entryPoints = null;

	private class EntryPointParser extends DefaultHandler {
		private Stack<String> parserStack = new Stack<String>();
		private ArrayList<EntryPoint> epList = new ArrayList<EntryPoint>();

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			// TODO Auto-generated method stub
			super.endElement(uri, localName, name);
			String stackName = parserStack.pop();
			if (!name.equals(stackName)) {
				throw new SAXException("Tags do not match: expected=" + stackName + "; actual=" + name);
			}
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			super.startElement(uri, localName, name, attributes);
			parserStack.push(name);
			if (name.equalsIgnoreCase("SEGMENT")) {
				EntryPoint ep = new EntryPoint();
				ep.id = attributes.getValue("id");
				ep.start = Integer.parseInt(attributes.getValue("start"));
				ep.stop = Integer.parseInt(attributes.getValue("stop"));
				epList.add(ep);
			}
		}

		public List<EntryPoint> getAll() {
			return epList;
		}

	}

	static public class EntryPoint {
		String id;
		int start, stop;

		public String toString() {
			return id + " [" + start + "," + stop + "]";
		}
	}

	private static class SequenceParser extends DefaultHandler {
		StringBuffer seq = null;

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			super.startElement(uri, localName, name, attributes);
			if (name.equalsIgnoreCase("DNA")) {
				seq = new StringBuffer();
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// TODO Auto-generated method stub
			super.characters(ch, start, length);
			seq.append(ch, start, length);
		}

	}

	private void getEntry(EntrySet set,String ref, EntryPoint ep) throws MalformedURLException, ParserConfigurationException, SAXException, IOException, URISyntaxException {
		StringBuffer seq = this.getSequence(ref, ep);
		Entry out = set.getOrCreateEntry(ref + ":" + ep);
//		out.setID();
		out.setSequence(new MemorySequence(seq));
		// System.out.println("Ref: " + ref);
		// if (ref.contains("Homo")) {
		System.out.println("Ref: " + ref);

		for (String source : this.getDSN().getSources(ref)) {
			List<Feature> list = this.getFeatures(source, ep);
			for(Feature f:list){
//				out.annotation.addAll(list);
				MemoryFeatureAnnotation fa = out.getMemoryAnnotation(f.type());
				fa.add(f);
			}
		}
		// System.out.println("** " + list);

		// }
		

	}

	private static class FeatureParser extends DefaultHandler {
		private Stack<String> parserStack = new Stack<String>();
		private String featureID = null;
		private String typeID = null;
		private int end;
		private int start;
		private String methodID;
		private char strand;
		private double score;

		@Override
		public void characters(char[] ch, int st, int length) throws SAXException {
			// TODO Auto-generated method stub
			super.characters(ch, st, length);
			if (parserStack.peek().equalsIgnoreCase("START")) {
				start = Integer.parseInt(new String(ch, st, length));

			}
			if (parserStack.peek().equalsIgnoreCase("END")) {
				end = Integer.parseInt(new String(ch, st, length));

			}
			if (parserStack.peek().equalsIgnoreCase("orientation")) {
				strand = ch[st];
			}
			if (parserStack.peek().equalsIgnoreCase("score")) {
				if (length == 1 && ch[st] == '-')
					score = 0;
				else {
					score = Double.parseDouble(new String(ch, st, length));
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			// TODO Auto-generated method stub
			super.endElement(uri, localName, name);
			String stackName = parserStack.pop();
			if (!name.equals(stackName)) {
				throw new SAXException("Tags do not match: expected=" + stackName + "; actual=" + name);
			}
			if (name.equalsIgnoreCase("feature")) {
				Feature f = new Feature();
				f.addLocation(new Location(start, end));
				f.setType(Type.get(typeID));
				f.addQualifier("source", methodID);
				f.addQualifier("name", featureID);
				f.setScore(score);
				f.setStrand(Strand.fromSymbol(strand));
				list.add(f);

			}
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			super.startElement(uri, localName, name, attributes);
			parserStack.push(name);
			if (name.equalsIgnoreCase("feature")) {
				featureID = attributes.getValue("id");

			}
			if (name.equalsIgnoreCase("type")) {
				typeID = attributes.getValue("id");

			}
			if (name.equalsIgnoreCase("method")) {
				methodID = attributes.getValue("id");

			}
		}

		private List<Feature> list = new ArrayList<Feature>();

	}

	private List<Feature> getFeatures(String source, EntryPoint e) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, URISyntaxException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		FeatureParser featp = new FeatureParser();
		// System.out.println("Getting features: " + featureDSN);
		// System.out.println(serverPrefix + "/das/" + featureDSN +
		// "/features?segment=" + e.id + ":" + e.start + "," + e.stop);
		parser.parse(URIFactory.url(serverPrefix + "/das/" + source + "/features?segment=" + e.id + ":" + e.start + "," + e.stop).openStream(), featp);
		return featp.list;
	}

	private EntryPoint ep = null;
	private String reference = null;

	public void setEntryPoint(EntryPoint ep) {
		this.ep = ep;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	private StringBuffer getSequence(String ref, EntryPoint e) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, URISyntaxException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		SequenceParser seqp = new SequenceParser();
		System.out.println(ref + "/dna?segment=" + e.id + ":" + e.start + "," + e.stop);
		parser.parse(URIFactory.url(ref + "/dna?segment=" + e.id + ":" + e.start + "," + e.stop).openStream(), seqp);
		return clean(seqp.seq);
	}

	private StringBuffer clean(StringBuffer seq) {
		return new StringBuffer(new String(seq).replaceAll("[ \t\n\r]", ""));
	}

	public List<EntryPoint> getEntryPoints(String ref) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, URISyntaxException {
		if (entryPoints == null) {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			entryPoints = new EntryPointParser();
			parser.parse(URIFactory.url(ref + "/entry_points").openStream(), entryPoints);
		}
		return entryPoints.getAll();

	}

//	@Override
//	public boolean isDestructiveSave() {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public EntrySet read(EntrySet set) throws ReadFailedException {
		if (set == null)
			set = new EntrySet();
		if(ep==null||reference==null)
			throw new ReadFailedException("Both the EntryPoint and the Reference need to be set!");
		try {
			this.getEntry(set,reference, ep);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new ReadFailedException(e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new ReadFailedException(e);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new ReadFailedException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReadFailedException(e);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return set;
	}


	public List<String> getReferences() {
		return dsn.getReferences();
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jannot.source.DataSource#isIndexed()
	 */
	@Override
	public boolean isIndexed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.jannot.source.DataSource#size()
	 */
	@Override
	public long size() {
		return 0;
	}

}
