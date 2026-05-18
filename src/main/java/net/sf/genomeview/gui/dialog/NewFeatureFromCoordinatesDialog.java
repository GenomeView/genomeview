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
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.StrandCombo;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;

/**
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class NewFeatureFromCoordinatesDialog extends JDialog {

	public NewFeatureFromCoordinatesDialog(final Model model) {
		super(model.getGUIManager().getMainWindow(),
				model.getMessageMgr().getString("newfeature.title"));
		final MessageManager mm = model.getMessageMgr();
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
		coordinates.setPreferredSize(
				new Dimension(200, coordinates.getPreferredSize().height));
		coordinates.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				super.focusGained(arg0);
				coordinates.selectAll();
			}

		});

		final TypeCombo typeCombo = new TypeCombo(model);
		typeCombo.setSelectedItem(model.getGlobal().typeFactory().get("CDS"));
		final StrandCombo strandSelection = new StrandCombo();

		/* Coordinates field + help */
		gc.gridwidth = 2;
		c.add(coordinates, gc);
		gc.gridx += 2;
		c.add(new HelpButton(this, mm.getString("newfeaturecoord.hlp_text")));

		gc.gridx = 0;
		gc.gridy++;
		c.add(strandSelection, gc);
		gc.gridy++;
		c.add(typeCombo, gc);
		gc.gridwidth = 1;
		gc.gridy++;
		JButton ok = new JButton(mm.getString("button.ok"));
		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					SortedSet<Location> loc = parse(coordinates.getText());
					filterLocation(loc, new Location(1,
							model.vlm.getVisibleEntry().getMaximumLength()));
					Feature f = new Feature(loc, typeCombo.getSelectedType(),
							strandSelection.getStrand());
					MemoryFeatureAnnotation fa = model.vlm.getVisibleEntry()
							.getMemoryAnnotation(f.type());
					fa.add(f);
					model.updateTracks();
					_self.dispose();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(_self,
							mm.getString("newfeaturecoord.couldnt_create_warn"),
							mm.getString("newfeaturecoord.error"),
							JOptionPane.WARNING_MESSAGE);
				}

			}

			private void filterLocation(SortedSet<Location> loc,
					Location range) {
				Iterator<Location> it = loc.iterator();
				while (it.hasNext()) {
					Location l = it.next();
					if (l.end() < range.start()) {
						it.remove();
					} else if (l.start() < range.start()) {
						l.setStart(range.start());
					} else if (l.start() > range.end()) {
						it.remove();
					} else if (l.end() > range.end()) {
						l.setEnd(range.end());
					}
				}

			}

			private SortedSet<Location> parse(String text) {
				SortedSet<Location> out = new TreeSet<Location>();
				String[] arr = text.split(",");
				for (String l : arr) {
					String[] a2 = l.trim().split("\\.\\.");
					try {
						out.add(new Location(Integer.parseInt(a2[0].trim()),
								Integer.parseInt(a2[1].trim())));
					} catch (NumberFormatException ne) {
						model.getLog().log(Level.WARNING,
								"Error while parsing '" + l + "' in "
										+ Arrays.toString(arr));
						throw ne;
					}

				}
				return out;
			}

		});

		JButton cancel = new JButton(
				model.getMessageMgr().getString("button.cancel"));
		cancel.addActionListener(new ActionListener() {

			@Override
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
		StaticUtils.center(model.getGUIManager().getMainWindow(), this);

	}
}
