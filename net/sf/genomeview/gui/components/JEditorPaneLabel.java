/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;



/**
 * 
 * @author Thomas Abeel
 * 
 */
public class JEditorPaneLabel extends JEditorPane {

	
	private static final long serialVersionUID = 7954185710654053247L;

	class Hyperactive implements HyperlinkListener {

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				
				try {
					Desktop.getDesktop().browse(e.getURL().toURI());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public JEditorPaneLabel(String text) {
		super("text/html", text);
		setEditable(false);
		super.addHyperlinkListener(new Hyperactive());
			
	}

	public JEditorPaneLabel() {
		this(null);
	}

	public StyleSheet getStyleSheet() {
		StyleSheet css = ((HTMLEditorKit) super.getEditorKit()).getStyleSheet();
		return css;

	}
}
