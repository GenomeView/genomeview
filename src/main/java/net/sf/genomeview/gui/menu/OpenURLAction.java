/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.genomeview.core.Globals;
import net.sf.genomeview.gui.StaticUtils;

/**
 * Opens a URL in a browser window.
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class OpenURLAction extends AbstractAction {

	private final String url;
	private final Globals globals;

	public OpenURLAction(String title, String url, Globals globals) {
		super(title);
		this.url = url;
		this.globals = globals;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		StaticUtils.browse(url, globals);

	}

}
