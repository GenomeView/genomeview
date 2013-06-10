/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.html.StyleSheet;

import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.jannot.Feature;
import be.abeel.gui.GridBagPanel;

/**
 * Panel with detailed information about a single Feature. (bottom -right in
 * GUI)
 * 
 * @author Thomas Abeel
 * 
 */
public class FeatureDetailPanel extends GridBagPanel implements Observer {

	private static final long serialVersionUID = 1214531303733670258L;

	private JEditorPaneLabel name = new JEditorPaneLabel();

	private Model model;

	public FeatureDetailPanel(Model model) {
		this.model = model;

		StyleSheet css = name.getStyleSheet();
		css.addRule("body {color:#000; margin-left: 4px; margin-right: 4px; }");
		css.addRule("p {margin:0px;padding:0px;}");
		css.addRule("h3 {font-size:115%;color: " + Colors.encode(Configuration.green) + ";margin:0px;padding:0px;}");

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

			private JPopupMenu popupMenu = null;

			private void showPopUp(MouseEvent e) {
				if (popupMenu == null) {
					popupMenu = new JPopupMenu();

					popupMenu.add(wrapMenu(Query.google));
					popupMenu.add(wrapMenu(Query.ncbiQuery));
					popupMenu.add(wrapMenu(Query.ensemblQuery));
					// popupMenu.add(wrapMenu(Query.ebi));

					popupMenu.add(wrapMenu(Query.plaza));

					String extra = Configuration.get("extraqueries");
					if (extra != null) {
						popupMenu.addSeparator();
						String[] arr = extra.split(";");
						for (String s : arr) {
							String[] tmp = s.split(",");
							JMenuItem extramenu = wrapMenu(new Query(tmp[0], tmp[1], null));
							popupMenu.add(extramenu);
						}
					}

				}
				popupMenu.show(e.getComponent(), e.getX(), e.getY());

			}

			private JMenuItem wrapMenu(final Query q) {
				return new JMenuItem(new AbstractAction(q.getLabel(), q.getIcon()) {

					private static final long serialVersionUID = -3208849232821620577L;

					@Override
					public void actionPerformed(ActionEvent e) {

						q.query(name.getSelectedText());

					}
				});
			}

		});

		model.addObserver(this);
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 0;
		add(name, gc);
		gc.gridy++;
		gc.weighty = 1;
		add(new JLabel(), gc);
	}

	private Set<Feature> lastSelection = null;

	@Override
	public void update(Observable o, Object arg) {
		// TODO implement specific model for selections
		// FIXME implement using StringBuffer for speed
		Set<Feature> set = model.selectionModel().getFeatureSelection();
		if (set != null && set.equals(lastSelection))
			return;
		StringBuffer text = new StringBuffer();

		if (set != null && set.size() > 0) {
			for (Feature rf : set) {
				// text += "Data origin: " + rf.getSource() + "\n";
				if (rf.location() != null)
					text.append("Location: " + StaticUtils.escapeHTML(Arrays.toString(rf.location())) + "<br/>");
				text.append("Strand: " + rf.strand() + "<br/>");
				text.append("Score: " + rf.getScore() + "<br/>");
				Set<String> qks = rf.getQualifiersKeys();
				for (String key : qks) {
					if (key.equals("url")) {
						String[] urls = rf.qualifier(key).split(",");
						for (String url : urls)
							text.append("<a href='" + url + "'>" + url + "</a><br/>");
					} else
						text.append(key + "=" + rf.qualifier(key) + "<br/>");

				}

			}

			// text += "---------------------------------------\n";
			// int hash = text.hashCode();
			// if (hash != lastHash) {
			name.setText("<html><body>" + text + "</body></html>");
			// name.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
			// lastHash = hash;
			// }

		} else {
			name.setText("");
		}
		lastSelection = set;
	}
}
