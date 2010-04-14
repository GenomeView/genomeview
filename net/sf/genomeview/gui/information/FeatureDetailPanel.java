/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.Feature;
import net.sf.jannot.Qualifier;
import be.abeel.gui.GridBagPanel;

/**
 * Panel with detailed information about a single Feature. (bottom -right in
 * GUI)
 * 
 * @author Thomas Abeel
 * 
 */
public class FeatureDetailPanel extends GridBagPanel implements Observer {

	/**
     * 
     */
	private static final long serialVersionUID = 1214531303733670258L;

	private JEditorPane name = new JEditorPane();

	private Model model;

	public FeatureDetailPanel(Model model) {
		this.model = model;
		name.setEditable(false);
		name.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopUp(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopUp(e);
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopUp(e);
				}
			}

			/**
			 * Class representing a website query.
			 * 
			 * @author Thomas
			 * 
			 */
			class Query extends AbstractAction {

				/**
				 * 
				 */
				private static final long serialVersionUID = 5252902483799067615L;
				private String queryURL;

				// private String label;

				Query(String label, String queryURL) {
					super(label);
					// this.label = label;
					this.queryURL = queryURL;
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String query = queryURL.replaceAll("%query%",
								URLEncoder.encode(name.getSelectedText().trim(),
										"UTF-8"));

						StaticUtils.browse(new URI(query));
					} catch (IOException f) {
						// TODO Auto-generated catch block
						f.printStackTrace();
					} catch (URISyntaxException g) {
						// TODO Auto-generated catch block
						g.printStackTrace();
					}

				}

			}

			private JPopupMenu popupMenu = null;

			private void showPopUp(MouseEvent e) {
				if (popupMenu == null) {
					popupMenu = new JPopupMenu();
					JMenuItem ncbiQuery = new JMenuItem(
							new Query("Query at NCBI Entrez",
									"http://www.ncbi.nlm.nih.gov/sites/gquery?term=%query%"));
					JMenuItem ensemblQuery = new JMenuItem(
							new Query("Query at Ensembl",
									"http://www.ensembl.org/Homo_sapiens/Search/Summary?species=all;idx=;q=%query%"));
					JMenuItem ebi = new JMenuItem(
							new Query("Query at EMBL-EBI",
									"http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=%query%"));

					JMenuItem google = new JMenuItem(new Query(
							"Query at Google",
							"http://www.google.com/search?q=%query%"));

					popupMenu.add(ncbiQuery);
					popupMenu.add(ensemblQuery);
					popupMenu.add(ebi);
					popupMenu.add(google);
					String extra = Configuration.get("extraqueries");
					if (extra != null) {
						popupMenu.addSeparator();
						String[] arr = extra.split(";");
						for (String s : arr) {
							String[] tmp = s.split(",");
							JMenuItem extramenu = new JMenuItem(new Query(
									tmp[0], tmp[1]));
							popupMenu.add(extramenu);
						}
					}
					

				}
				popupMenu.show(e.getComponent(), e.getX(), e.getY());

			}

		});
//		name.setEditorKit(new HTMLEditorKit());
//		name.setDocument(new HTMLDocument());
//		name.addHyperlinkListener(new HyperlinkListener() {
//
//			@Override
//			public void hyperlinkUpdate(HyperlinkEvent e) {
//				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//
//					System.out.println(e.getDescription());
//					URL url = e.getURL();
//					System.out.println("Hyperlink: " + url);
//					// try {
//					// Desktop.getDesktop().browse(url.toURI());
//					// } catch (IOException e1) {
//					// // TODO Auto-generated catch block
//					// e1.printStackTrace();
//					// } catch (URISyntaxException e1) {
//					// // TODO Auto-generated catch block
//					// e1.printStackTrace();
//					// }
//					//                
//				}
//			}
//
//		});
//		;

		model.addObserver(this);
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 0;
		add(name, gc);
		gc.gridy++;
		gc.weighty = 1;
		add(new JLabel(), gc);
	}

	private Set<Feature>lastSelection=null;
	
	@Override
	public void update(Observable o, Object arg) {
		//TODO implement specific model for selections
		//FIXME implement using StringBuffer for speed
		Set<Feature> set = model.selectionModel().getFeatureSelection();
		if(set!=null&&set.equals(lastSelection))
			return;
		String text = "";

		if (set != null && set.size() > 0) {
			for (Feature rf : set) {
//				text += "Data origin: " + rf.getSource() + "\n";
				if(rf.location()!=null)
				text += "Location: "
						+ StaticUtils.escapeHTML(rf.location().toString())
						+ "\n";
				text += "Strand: " + rf.strand() + "\n";
				text += "Score: " + rf.getScore() + "\n";
				Set<String> qks = rf.getQualifiersKeys();
				for (String key : qks) {
					for (Qualifier q : rf.qualifier(key)) {
						text += q + "\n";
					}
				}

			}

			text += "---------------------------------------\n";
//			int hash = text.hashCode();
//			if (hash != lastHash) {
				name.setText(text);
//				name.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
//				lastHash = hash;
//			}

		} else {
			name.setText("");
		}
		lastSelection=set;
	}
}
