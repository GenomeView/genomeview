/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.sf.genomeview.core.Globals;
import net.sf.genomeview.gui.StaticUtils;

/**
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class JEditorPaneLabel extends JEditorPane {

	public JEditorPaneLabel(Globals globals) {
		super("text/html", null);
		setEditable(false);
		super.addHyperlinkListener(new Hyperactive(globals));
		// ## Fix for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6993691
		setEditorKit(new HTMLEditorKit() {

			private static final long serialVersionUID = -8823280246213759957L;

			@Override
			protected Parser getParser() {
				try {
					@SuppressWarnings("rawtypes")
					Class c = Class.forName(
							"javax.swing.text.html.parser.ParserDelegator");
					Parser defaultParser = (Parser) c.newInstance();
					return defaultParser;
				} catch (Throwable e) {
				}
				return null;
			}
		});

	}

	public StyleSheet getStyleSheet() {
		StyleSheet css = ((HTMLDocument) this.getDocument()).getStyleSheet();
		return css;

	}
}

class Hyperactive implements HyperlinkListener {

	private final Globals globals;

	public Hyperactive(Globals globals) {
		this.globals = globals;
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			StaticUtils.browse(e.getURL().toString(), globals);
		}
	}
}
