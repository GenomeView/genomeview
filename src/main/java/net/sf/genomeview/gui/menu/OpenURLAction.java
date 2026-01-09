/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;

import net.sf.genomeview.gui.StaticUtils;
import be.abeel.net.URIFactory;

/**
 * Opens a URL in a browser window.
 * 
 * @author Thomas Abeel
 * 
 */
public class OpenURLAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -5857826271738146666L;
    private String url;

    public OpenURLAction(String title, String url) {
        super(title);
        this.url=url;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
    	try {
			StaticUtils.browse(URIFactory.uri(url));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

    }

}
