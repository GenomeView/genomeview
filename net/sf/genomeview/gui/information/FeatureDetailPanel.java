/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

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

            private void showPopUp(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem ncbiQuery = new JMenuItem("Query at NCBI Entrez");
                ncbiQuery.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String query = "http://www.ncbi.nlm.nih.gov/sites/gquery?term="
                                    + URLEncoder.encode(name.getSelectedText(), "UTF-8");

                            Desktop.getDesktop().browse(new URI(query));
                        } catch (MalformedURLException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (IOException f) {
                            // TODO Auto-generated catch block
                            f.printStackTrace();
                        } catch (URISyntaxException g) {
                            // TODO Auto-generated catch block
                            g.printStackTrace();
                        }

                    }

                });
                JMenuItem ensemblQuery = new JMenuItem("Query at Ensembl");
                ensemblQuery.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            URL query = new URL(
                                    "http://www.ensembl.org/Homo_sapiens/Search/Summary?species=all;idx=;q="
                                            + URLEncoder.encode(name.getSelectedText(), "UTF-8"));
                            Desktop.getDesktop().browse(query.toURI());
                        } catch (MalformedURLException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (IOException f) {
                            // TODO Auto-generated catch block
                            f.printStackTrace();
                        } catch (URISyntaxException g) {
                            // TODO Auto-generated catch block
                            g.printStackTrace();
                        }

                    }

                });
                JMenuItem ebi = new JMenuItem("Query at EMBL-EBI");
                ebi.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            URL query = new URL(
                                    "http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query="
                                            + URLEncoder.encode(name.getSelectedText(), "UTF-8"));
                            Desktop.getDesktop().browse(query.toURI());
                        } catch (MalformedURLException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (IOException f) {
                            // TODO Auto-generated catch block
                            f.printStackTrace();
                        } catch (URISyntaxException g) {
                            // TODO Auto-generated catch block
                            g.printStackTrace();
                        }

                    }

                });
                
                menu.add(ncbiQuery);
                menu.add(ensemblQuery);
                menu.add(ebi);
                menu.show(e.getComponent(), e.getX(), e.getY());

            }

        });
        name.setEditorKit(new HTMLEditorKit());
        name.setDocument(new HTMLDocument());
        name.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

                    System.out.println(e.getDescription());
                    URL url = e.getURL();
                    System.out.println("Hyperlink: " + url);
                    // try {
                    // Desktop.getDesktop().browse(url.toURI());
                    // } catch (IOException e1) {
                    // // TODO Auto-generated catch block
                    // e1.printStackTrace();
                    // } catch (URISyntaxException e1) {
                    // // TODO Auto-generated catch block
                    // e1.printStackTrace();
                    // }
                    //                
                }
            }

        });
        ;

        model.addObserver(this);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 0;
        add(name, gc);
        gc.gridy++;
        gc.weighty = 1;
        add(new JLabel(), gc);
    }

    private int lastHash = 0;

    @Override
    public void update(Observable o, Object arg) {
        Set<Feature> set = model.getFeatureSelection();
        String text = "";

        if (set != null && set.size() > 0) {
            for (Feature rf : set) {
                text += "Data origin: " + rf.getSource() + "<br/>";
                text += "Location: " + StaticUtils.escapeHTML(rf.location().toString()) + "<br/>";
                text += "Strand: " + rf.strand() + "<br/>";
                text += "Score: " + rf.getScore() + "<br/>";
                Set<String> qks = rf.getQualifiersKeys();
                for (String key : qks) {
                    for (Qualifier q : rf.qualifier(key)) {
                        text += q + "<br/>";
                    }
                }

            }

            text += "---------------------------------------<br/>";
            int hash=text.hashCode();
            if (hash != lastHash) {
                System.out.println("FDP: " + text + "\t" + name);
                name.setText("<html>" + text + "</html>");
                name.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
                lastHash=hash;
            }

        } else {
            name.setText("");
        }

    }
}
