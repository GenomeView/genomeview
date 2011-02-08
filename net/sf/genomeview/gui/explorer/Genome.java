/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.html.StyleSheet;

import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import be.abeel.gui.GridBagPanel;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class Genome extends GridBagPanel {

	private static final long serialVersionUID = -5738284884971849836L;
	private EmptyBorder empty;
	private Border bevel;

	Genome(final Model model, String name, Icon image, String description, final URL url) {
		
		Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		bevel = BorderFactory.createCompoundBorder(
				raisedetched, loweredetched);
		Border redline = BorderFactory.createLineBorder(Configuration.green);

		bevel = BorderFactory.createCompoundBorder(
				  redline, bevel);


		empty = new EmptyBorder(5,5,5,5);
		setBackground(Color.WHITE);
		gc.weightx = 0;
		gc.weighty = 0;
		final JLabel picture = new JLabel(image);
		picture.setBorder(empty);
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Session.loadSession(model, url);
				} catch (IOException e1) {
					CrashHandler.showErrorMessage("Could not load this session.", e1);
				}
			}

		});
		picture.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				((JLabel) e.getComponent()).setBorder(bevel);
				picture.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				repaint();
			}

			public void mouseExited(MouseEvent e) {
				((JLabel) e.getComponent()).setBorder(empty);
				picture.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				repaint();
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Session.loadSession(model, url);
				} catch (IOException e1) {
					CrashHandler.showErrorMessage("Could not load this session.", e1);
				}
			}
		});

		add(picture, gc);
		gc.gridx++;
		gc.weightx = 1;
		JEditorPaneLabel text = new JEditorPaneLabel();
		StyleSheet css = text.getStyleSheet();
		css.addRule("body {color:#000; margin-left: 4px; margin-right: 4px; }");
		css.addRule("p {margin:0px;padding:0px;}");
		css.addRule("h3 {font-size:115%;color: " + Colors.encode(Configuration.green) + ";margin:0px;padding:0px;}");
		text.setText("<html><h3>" + name + "</h3><p>" + description + "</p></html>");

		add(text, gc);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border colorBorder = BorderFactory.createLineBorder(Configuration.green);
		setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));

	}
}