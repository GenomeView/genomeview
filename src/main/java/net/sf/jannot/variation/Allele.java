package net.sf.jannot.variation;

public class Allele {

	private String alt;
	private String ref;
//	private float af;

	public String alternative() {
		return alt;
	}

	public String reference(){
		return ref;
	}
	public Allele(String ref,String alt) {
		this.alt = alt;
		this.ref=ref;
		
	
	}

//	public float alternativeFrequency() {
//		return af;
//	}

}