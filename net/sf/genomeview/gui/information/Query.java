/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.utils.URIFactory;

/**
 * Class representing a website query.
 * 
 * @author Thomas
 * 
 */
public class Query {

	public static Query ncbiQuery = new Query("Query at NCBI Entrez",
			"http://www.ncbi.nlm.nih.gov/sites/gquery?term=%query%",Icons.NCBI);
	public static Query ensemblQuery = new Query("Query at Ensembl",
			"http://www.ensembl.org/common/Search/Results?species=all;idx=;q=%query%",Icons.Ensembl);
	public static Query ebi = new Query("Query at EMBL-EBI",
			"http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=%query%",null);

	public static Query google = new Query("Query at Google", "http://www.google.com/search?q=%query%",Icons.GOOGLE);

	public static Query plaza = new Query("Query in Plaza",
			"http://bioinformatics.psb.ugent.be/plaza/genes/view/%query%",Icons.PLAZA);

	private static final long serialVersionUID = 5252902483799067615L;
	private String queryURL;

	private String label;
	private Icon icon;

	
	public Query(String label, String queryURL,Icon icon) {
		// super(label);
		this.label = label;
		this.queryURL = queryURL;
		this.icon=icon;
	}

	public Icon getIcon(){
		return icon;
	}
	public String getLabel(){
		return label;
	}
	
	public void query(String q) {
		try {
			String query = queryURL.replaceAll("%query%", URLEncoder.encode(q.trim(), "UTF-8"));
			StaticUtils.browse(URIFactory.uri(query));
		} catch (IOException f) {
			// TODO Auto-generated catch block
			f.printStackTrace();
		} catch (URISyntaxException g) {
			// TODO Auto-generated catch block
			g.printStackTrace();
		}

	}

	@Override
	public String toString() {
		return label;
	}

}