/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import net.sf.genomeview.gui.StaticUtils;

/**
 * Class representing a website query.
 * 
 * @author Thomas
 * 
 */
public class Query {

	public static Query ncbiQuery = new Query("Query at NCBI Entrez",
			"http://www.ncbi.nlm.nih.gov/sites/gquery?term=%query%");
	public static Query ensemblQuery = new Query("Query at Ensembl",
			"http://www.ensembl.org/Homo_sapiens/Search/Summary?species=all;idx=;q=%query%");
	public static Query ebi = new Query("Query at EMBL-EBI",
			"http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=%query%");

	public static Query google = new Query("Query at Google", "http://www.google.com/search?q=%query%");

	public static Query plaza = new Query("Query in Plaza",
			"http://bioinformatics.psb.ugent.be/plaza/genes/view/%query%");

	private static final long serialVersionUID = 5252902483799067615L;
	private String queryURL;

	private String label;

	public Query(String label, String queryURL) {
		// super(label);
		this.label = label;
		this.queryURL = queryURL;
	}

	public void query(String q) {
		try {
			String query = queryURL.replaceAll("%query%", URLEncoder.encode(q.trim(), "UTF-8"));
			StaticUtils.browse(new URI(query));
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