/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.file.LoadFeaturesAction;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeViewWindow extends JFrame implements Observer {

	class ReferenceMissingWarning extends JWindow {

		private static final long serialVersionUID = 566883531326807914L;
		private JLabel floater = new JLabel();

		public ReferenceMissingWarning(Model model, String text) {
			this(model, text, Color.RED);
		}

		public ReferenceMissingWarning(Model model, String text, Color color) {
			final JWindow _self = this;
			Rectangle bounds = model.getGUIManager().getParent().getBounds();
			// this.setBounds(bounds);
			this.setAlwaysOnTop(true);
			floater.setPreferredSize(new Dimension(bounds.width / 5, bounds.height / 5));
			floater.setHorizontalAlignment(SwingConstants.CENTER);
			floater.setVerticalAlignment(SwingConstants.CENTER);
			floater.setAlignmentY(CENTER_ALIGNMENT);
			this.setLocation(bounds.x + bounds.width / 5 * 2, bounds.y + bounds.height / 5);

			floater.setOpaque(true);
			floater.setText(text);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
			Border colorBorder = BorderFactory.createLineBorder(color);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			floater.setBackground(new Color(255, 0, 0, 100));
			setLayout(new BorderLayout());
			add(floater, BorderLayout.CENTER);

			Container buttons = new Container();
			buttons.setLayout(new BorderLayout());
			add(buttons, BorderLayout.SOUTH);

			JButton dismiss = new JButton("Dismiss");
			dismiss.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					_self.setVisible(false);

				}
			});

			buttons.add(dismiss, BorderLayout.WEST);

			JButton data = new JButton("Load data");
			data.addActionListener(new LoadFeaturesAction(model));
			buttons.add(data, BorderLayout.EAST);
			pack();
		}

		public void setMsg(String text) {
			floater.setText(text);

		}

		public String getMsg() {
			return floater.getText();
		}
	}

	private Model model;

	public GenomeViewWindow(Model model, String string, GraphicsConfiguration defaultConfiguration) {
		super(string, defaultConfiguration);
		// this.setGlassPane(new WarningPane());
		this.model = model;
		model.addObserver(this);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7684262555440299887L;
	private ReferenceMissingWarning hider = null;

	@Override
	public void update(Observable o, Object arg) {
		/**
		 * Detect missing reference sequences.
		 */
		EntrySet es = model.entries();
		int missingReference = 0;
		ArrayList<String> missing = new ArrayList<String>();
		for (Entry e : es) {
			int dataCount = 0;

			for (DataKey dk : e)
				dataCount++;
			if (e.sequence().size() == 0 && dataCount > 0) {
				missingReference++;
				missing.add(e.getID());
			}
		}
		// System.out.println("Missing refs: " + missingReference);
		if (missingReference > 0) {
			StringBuffer msg = new StringBuffer(
					"<html><h1>Warning!!</h1>Not every entry has a reference sequence loaded! Some visualizations will not work as expected without reference.<br><br>Entries without reference: ");
			for (String s : missing) {
				msg.append("&nbsp;&nbsp;" + s + "");
			}
			msg.append("</html>");
			if (hider == null) {
				hider = new ReferenceMissingWarning(model, msg.toString());
				hider.setVisible(true);
			} else {
				if (hider.getMsg().length() != msg.length())
					hider.setMsg(msg.toString());
			}
		} else {
			if (hider != null) {
				hider.dispose();
				hider = null;
			}

		}

	}

}
