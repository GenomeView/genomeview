/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.genomeview.gui.StaticUtils;
import tudelft.utilities.logging.Reporter;

/**
 * Opens a URL in a browser window.
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class OpenURLAction extends AbstractAction {

	private final String url;
	private final Reporter log;

	public OpenURLAction(String title, String url, Reporter log) {
		super(title);
		this.url = url;
		this.log = log;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		StaticUtils.browse(url, log);

	}

}
