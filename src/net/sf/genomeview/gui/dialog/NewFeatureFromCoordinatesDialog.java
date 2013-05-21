/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.StrandCombo;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class NewFeatureFromCoordinatesDialog extends JDialog {

	/**
     * 
     */
	private static final long serialVersionUID = -5266511180264863028L;

	private static Logger log = Logger.getLogger(NewFeatureFromCoordinatesDialog.class.getCanonicalName());

	public NewFeatureFromCoordinatesDialog(final Model model) {
		super(model.getGUIManager().getParent(), MessageManager.getString("newfeature.title"));
		final NewFeatureFromCoordinatesDialog _self = this;
		setModal(true);
		Container c = new Container();
		c.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.insets = new Insets(3, 3, 3, 3);
		gc.fill = GridBagConstraints.BOTH;
		final JTextField coordinates = new JTextField("<coordinates>");
		coordinates.setPreferredSize(new Dimension(200, coordinates.getPreferredSize().height));
		coordinates.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				super.focusGained(arg0);
				coordinates.selectAll();
			}

		});

		final TypeCombo typeCombo = new TypeCombo(model);
		typeCombo.setSelectedItem(net.sf.jannot.Type.get("CDS"));
		final StrandCombo strandSelection = new StrandCombo();

		/* Coordinates field + help */
		gc.gridwidth = 2;
		c.add(coordinates, gc);
		gc.gridx += 2;
		c.add(new HelpButton(
				this,
				MessageManager.getString("newfeaturecoord.hlp_text")));

		gc.gridx = 0;
		gc.gridy++;
		c.add(strandSelection, gc);
		gc.gridy++;
		c.add(typeCombo, gc);
		gc.gridwidth = 1;
		gc.gridy++;
		JButton ok = new JButton(MessageManager.getString("button.ok"));
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					SortedSet<Location> loc = parse(coordinates.getText());
					filterLocation(loc, new Location(1, model.vlm.getSelectedEntry().getMaximumLength()));
					Feature f = new Feature();
					f.setLocation(loc);
					f.setType(typeCombo.getTerm());
					f.setStrand(strandSelection.getStrand());
					// model.getSelectedEntry().annotation.add(f);
					// MemoryFeatureAnnotation fa = (MemoryFeatureAnnotation)
					// model.getSelectedEntry().get(f.type());
					MemoryFeatureAnnotation fa = model.vlm.getSelectedEntry().getMemoryAnnotation(f.type());
					fa.add(f);
					// model.setSelectedRegion(null);
					model.updateTracks();
					_self.dispose();
				} catch (Exception ex) {
					JOptionPane
							.showMessageDialog(
									_self,
									MessageManager.getString("newfeaturecoord.couldnt_create_warn"),
									MessageManager.getString("newfeaturecoord.error"), JOptionPane.WARNING_MESSAGE);
				}

			}

			private void filterLocation(SortedSet<Location> loc, Location range) {
				Iterator<Location> it = loc.iterator();
				while (it.hasNext()) {
					Location l = it.next();
					if (l.end < range.start)
						it.remove();
					else if (l.start < range.start)
						l.setStart(range.start);
					else if (l.start > range.end)
						it.remove();
					else if (l.end > range.end)
						l.setEnd(range.end);
				}

			}

			private SortedSet<Location> parse(String text) {
				SortedSet<Location> out = new TreeSet<Location>();
				String[] arr = text.split(",");
				for (String l : arr) {
					String[] a2 = l.trim().split("\\.\\.");
					try {
						out.add(new Location(Integer.parseInt(a2[0].trim()), Integer.parseInt(a2[1].trim())));
					} catch (NumberFormatException ne) {
						log.warning("Error while parsing '" + l + "' in " + Arrays.toString(arr));
						throw ne;
					}

				}
				return out;
			}

		});

		JButton cancel = new JButton(MessageManager.getString("button.cancel"));
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_self.dispose();

			}

		});
		c.add(ok, gc);
		gc.gridx++;
		gc.gridwidth = 3;
		c.add(cancel, gc);
		setContentPane(c);
		pack();
		StaticUtils.center(model.getGUIManager().getParent(), this);

	}
}
