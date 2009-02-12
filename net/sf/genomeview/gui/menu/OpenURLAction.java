/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;

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
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
