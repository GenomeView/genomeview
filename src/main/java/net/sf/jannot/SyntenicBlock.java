/**
 * %HEADER%
 */
package net.sf.jannot;

/**
 * Synthenic block provides a syntenic mapping between two entries. It maps one
 * location in one entry to another location in the other Entry.
 * 
 * @author Thomas Abeel
 * 
 */
public class SyntenicBlock {

	private Location refLoc;
	private Location targetLoc;
	/* ID of reference */
	private String refEntry;
	/* ID of target */
	private String targetEntry;
	private Strand refStrand;
	private Strand targetStrand;
	

	public SyntenicBlock(String refEntry, String targetEntry, Location refLoc,
			Location targetLoc,Strand refStrand,Strand targetStrand) {
		super();
		this.refLoc = refLoc;
		this.targetLoc = targetLoc;
		this.refEntry = refEntry;
		this.targetEntry = targetEntry;
		this.refStrand=refStrand;
		this.targetStrand=targetStrand;
		
	}


	/**
	 * Flip reference and target 
	 * @return
	 */
	public SyntenicBlock flip() {
		return new SyntenicBlock(targetEntry,refEntry,targetLoc,refLoc,targetStrand,refStrand);
	}


	public String target() {
		return targetEntry;
	}


	public Location refLocation() {
		return refLoc;
	}


	public Location targetLocation() {
		return targetLoc;
	}

}
