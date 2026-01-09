/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.HashMap;
import java.util.Set;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class Description  {

	private HashMap<String, String> keyValues = new HashMap<String, String>();

	public Set<String>keys(){
		return keyValues.keySet();
	}

	public void put(String key, String value) {
		keyValues.put(key, value);
//		setChanged();
//		notifyObservers();
	}

	public String get(String key) {
		return keyValues.get(key);
	}

	// private String msg;
	//
	// public final String getMsg() {
	// return msg;
	// }

	// public final void setMsg(String msg) {
	// this.msg = msg;
	// setChanged();
	// notifyObservers();
	// }

	// private List<String> accessionNumbers = new Vector<String>();

	// public List<String> getAccessionNumbers() {
	// return Collections.unmodifiableList(accessionNumbers);
	// }

	// public void addAccessionNumber(String s) {
	// if (!accessionNumbers.contains(s))
	// accessionNumbers.add(s);
	// if (primaryAccessionNumber == null)
	// primaryAccessionNumber = s;
	// setChanged();
	// notifyObservers();
	//
	// }

	// /* Project identifier */
	// private String projectIdentifier = null;
	//
	// public void setProjectIdentifier(String s) {
	// this.projectIdentifier = s;
	// setChanged();
	// notifyObservers();
	//
	// }

	// private StringBuffer freeDescription = new StringBuffer();
	//
	// public void addDescriptionLine(String s) {
	// freeDescription.append(s + "\n");
	// setChanged();
	// notifyObservers();
	// }

	// private Map<String, String> freeDescriptionPairs = new HashMap<String,
	// String>();
	//
	// public void addDescriptionValue(String key, String value) {
	// freeDescriptionPairs.put(key, value);
	// setChanged();
	// notifyObservers();
	// }

	// public String getDescriptionValue(String key) {
	// return freeDescriptionPairs.get(key);
	// }
	//
	// private String primaryAccessionNumber = null;

	// public void setPrimaryAccessionNumber(String s) {
	// this.primaryAccessionNumber = s;
	// if (!accessionNumbers.contains(s))
	// accessionNumbers.add(s);
	// setChanged();
	// notifyObservers();
	//
	// }

	// private String sequenceVersion = null;
	//
	// private String taxonomicDivision;
	//
	// private String dataClass;
	//
	// private String moleculeType;
	//
	// public void setSequenceVersion(String trim) {
	// this.sequenceVersion = trim;
	// setChanged();
	// notifyObservers();
	//
	// }
	//
	// public void setMoleculeType(String trim) {
	// this.moleculeType = trim;
	// setChanged();
	// notifyObservers();
	//
	// }

	// /**
	// * Class Definition -----------
	// * ----------------------------------------------------------- CON Entry
	// * constructed from segment entry sequences, drawing annotation from
	// segment
	// * entries ANN Entry constructed from segment entry sequences with its own
	// * annotation PAT Patent EST Expressed Sequence Tag GSS Genome Survey
	// * Sequence HTC High Thoughput CDNA sequencing HTG High Thoughput Genome
	// * sequencing MGA Mass Genome Annotation WGS Whole Genome Shotgun TPA
	// Third
	// * Party Annotation STS Sequence Tagged Site STD Standard (all entries not
	// * classified as above)
	// *
	// * @param trim
	// */
	// public void setDataClass(String trim) {
	// this.dataClass = trim;
	// setChanged();
	// notifyObservers();
	//
	// }

	// /**
	// * Division Code ----------------- ---- Bacteriophage PHG Environmental
	// * Sample ENV Fungal FUN Human HUM Invertebrate INV Other Mammal MAM Other
	// * Vertebrate VRT Mus musculus MUS Plant PLN Prokaryote PRO Other Rodent
	// ROD
	// * Synthetic SYN Transgenic TGN Unclassified UNC Viral VRL </code>
	// *
	// * @param trim
	// */
	// public void setTaxonomicDivision(String trim) {
	// this.taxonomicDivision = trim;
	// setChanged();
	// notifyObservers();
	//
	// }
	//
	// private String firstDate = null;
	//
	// private String secondData = null;
	//
	// public void setFirstDate(String substring) {
	// this.firstDate = substring;
	// setChanged();
	// notifyObservers();
	//
	// }
	//
	// public void setSecondDate(String substring) {
	// this.secondData = substring;
	// setChanged();
	// notifyObservers();
	//
	// }
	//
	// public final String getProjectIdentifier() {
	// return projectIdentifier;
	// }

	// public final StringBuffer getFreeDescription() {
	// StringBuffer desc = new StringBuffer(freeDescription);
	// desc.append(System.getProperty("line.separator"));
	// for (String key : freeDescriptionPairs.keySet()){
	// desc.append(key+"="+freeDescriptionPairs.get(key));
	// desc.append(System.getProperty("line.separator"));
	// }
	// return freeDescription;
	// }

	// public final String getPrimaryAccessionNumber() {
	// return primaryAccessionNumber;
	// }

	// public final String getSequenceVersion() {
	// return sequenceVersion;
	// }
	//
	// public final String getTaxonomicDivision() {
	// return taxonomicDivision;
	// }

	// public final String getDataClass() {
	// return dataClass;
	// }
	//
	// public final String getMoleculeType() {
	// return moleculeType;
	// }
	//
	// public final String getFirstDate() {
	// return firstDate;
	// }
	//
	// public final String getSecondData() {
	// return secondData;
	// }

	@Override
	public String toString() {
//		if (id != null) {
//			return id;
//		} else
			return keyValues.toString();
	}

	/**
	 * Add a value to a key. If the keys doesn't exist yet, it is added.
	 * Different values for a key are separated with a line break.
	 * 
	 * @param key
	 *            key to use
	 * @param value
	 *            value to add to the key
	 */
	public void add(String key, String value) {
		if (!keyValues.containsKey(key)) {
			put(key, value);
		} else {
			put(key, get(key) + "\n" + value);
		}

	}
}
