/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.html.StyleSheet;

import be.abeel.gui.GridBagPanel;
import be.abeel.util.NaturalOrderComparator;
import net.sf.genomeview.core.Colors;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.jannot.Feature;

/**
 * Panel with detailed information about a single Feature. (bottom -right in
 * GUI)
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class FeatureDetailPanel extends GridBagPanel implements Observer {
	// html table keywords
	private static final String ETH = "</th>";
	private static final String ETR = "</tr>";
	private static final String TR = "<tr>";
	private static final String ETD = "</td>";
	private static final String TD = "<td>";
	private static final String TH = "<th style=\"text-align: left;\">";

	private final JEditorPaneLabel panel;

	private final Model model;

	public FeatureDetailPanel(Model model) {
		this.model = model;
		this.panel = new JEditorPaneLabel(model.getGlobals());

		StyleSheet css = panel.getStyleSheet();
		css.addRule("body {color:#000; margin-left: 4px; margin-right: 4px; }");
		css.addRule("p {margin:0px;padding:0px;}");
		css.addRule("h3 {font-size:115%;color: " + Colors.encode(Color.green)
				+ ";margin:0px;padding:0px;}");

		panel.setEditable(false);
		panel.addMouseListener(new MouseAdapter() {

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

					String extra = model.getConfiguration().get("extraqueries");
					if (extra != null) {
						popupMenu.addSeparator();
						String[] arr = extra.split(";");
						for (String s : arr) {
							String[] tmp = s.split(",");
							JMenuItem extramenu = wrapMenu(
									new Query(tmp[0], tmp[1], null));
							popupMenu.add(extramenu);
						}
					}

				}
				popupMenu.show(e.getComponent(), e.getX(), e.getY());

			}

			private JMenuItem wrapMenu(final Query q) {
				return new JMenuItem(
						new AbstractAction(q.getLabel(), q.getIcon()) {
							@Override
							public void actionPerformed(ActionEvent e) {
								q.query(panel.getSelectedText(),
										model.getGlobals());

							}
						});
			}

		});

		model.addObserver(this);
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 0;
		add(panel, gc);
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
		if (set != null && set.equals(lastSelection)) {
			return;
		}

		Map<String, String> keyvalues = getMap(set);
		panel.setText("<html><body>" + html(keyvalues) + "</body></html>");
		lastSelection = set;
	}

	/**
	 * @param set of {@link Feature}s. null means empty map.
	 * @return map with key-value pairs for the set , sorted by key
	 */
	private Map<String, String> getMap(Set<Feature> set) {
		final Map<String, String> keyvalues = new LinkedHashMap<>();
		if (set == null) {
			set = Collections.emptySet();
		}
		for (final Feature rf : set) {
			if (rf.location() != null) {
				keyvalues.put("Location",
						StaticUtils.escapeHTML(Arrays.toString(rf.location())));
			}
			keyvalues.put("Strand", "" + rf.strand());
			keyvalues.put("Score", "" + rf.getScore());
			List<String> list = new ArrayList<String>(rf.getQualifiersKeys());
			Collections.sort(list,
					NaturalOrderComparator.NUMERICAL_ORDER_IGNORE_CASE);
			int nurls = 1;
			for (final String key : list) {
				if (key.equals("url")) {
					String[] urls = rf.qualifier(key).split(",");
					for (final String url : urls) {
						keyvalues.put("url" + nurls++,
								"<a href='" + url + "'>" + url + "</a>");
					}
				} else {
					keyvalues.put(key, rf.qualifier(key));
				}
			}
		}
		return keyvalues;
	}

	/**
	 * 
	 * @param keyvalues a Map<String,String>
	 * @return html text with table with this map, in the default order of the
	 *         map. Use {@link LinkedHashMap} to fix the order of items.
	 */
	private String html(Map<String, String> keyvalues) {
		final StringBuilder txt = new StringBuilder();
		txt.append("<table>");
		txt.append(TR + TH + "key" + ETH + TH + "value" + ETH + ETR);
		for (Entry<String, String> entry : keyvalues.entrySet()) {
			txt.append(TR);
			txt.append(TD + entry.getKey() + ETD);
			txt.append(TD + entry.getValue() + ETD);
			txt.append(ETR);
		}
		txt.append("</table>");
		return txt.toString();
	}
}
