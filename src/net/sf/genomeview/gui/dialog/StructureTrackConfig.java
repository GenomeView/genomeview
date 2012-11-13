/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import be.abeel.gui.GridBagPanel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrackModel;
import net.sf.jannot.Type;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class StructureTrackConfig extends JDialog {

	private static final long serialVersionUID = -5209291628487502687L;

	private StructureTrackConfig(final Model model, final StructureTrackModel stm) {
		super(model.getGUIManager().getParent(), "Structure track configuration", true);
		final Window _self=this;
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagPanel().gc;
		for (final net.sf.jannot.Type t : net.sf.jannot.Type.values()) {
			final JCheckBox jc = new JCheckBox();
			jc.setSelected(stm.isTypeVisible(t));
			jc.setAction(new AbstractAction(t.toString()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					stm.setTypeVisible(t, jc.isSelected());

				}
			});
			add(jc, gc);
			gc.gridy++;
		}
		add(new JButton(new AbstractAction("OK") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				_self.dispose();
				
			}
		}),gc);
		pack();
		StaticUtils.center(model.getGUIManager().getParent(),this);
		setVisible(true);

	}

	public static void display(Model model, StructureTrackModel stm) {
		new StructureTrackConfig(model, stm);

	}

}
