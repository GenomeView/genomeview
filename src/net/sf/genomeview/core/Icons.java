/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class Icons {
	// FIXME fix with get method
	public static final Icon DELETE = new ImageIcon(Icons.class.getResource("/images/delete.png"));

	public static final Icon YES = new ImageIcon(Icons.class.getResource("/images/yes.png"));

	public static final Icon NO = new ImageIcon(Icons.class.getResource("/images/no.png"));
	public static final Icon BDASH = new ImageIcon(Icons.class.getResource("/images/bdash.png"));

	public static final Icon VISIBLE = new ImageIcon(Icons.class.getResource("/images/visible.png"));
	public static final Icon INVISIBLE = new ImageIcon(Icons.class.getResource("/images/invisible.png"));

	public static final Icon EDIT = new ImageIcon(Icons.class.getResource("/images/edit.png"));

	public static final Icon DOWN_ARROW = new ImageIcon(Icons.class.getResource("/images/downarrow.png"));

	public static final Icon UP_ARROW = new ImageIcon(Icons.class.getResource("/images/uparrow.png"));

	public static final Icon HELP = new ImageIcon(Icons.class.getResource("/images/help.png"));

	public static final Icon PLAZA = new ImageIcon(Icons.class.getResource("/images/search/plaza.png"));

	public static Icon GOOGLE = new ImageIcon(Icons.class.getResource("/images/search/google.png"));
	public static Icon NCBI = new ImageIcon(Icons.class.getResource("/images/search/ncbi.png"));
	public static Icon Ensembl = new ImageIcon(Icons.class.getResource("/images/search/ensembl.png"));

	public static Icon LOGO = new ImageIcon(Icons.class.getResource("/images/logo.png"));

	public static Icon LARGEOPEN = get("boxopen.png");

	public static Icon MINILOGO_ICON = get("gv2.png");

	public static Image MINILOGO = get("gv2.png").getImage();
	
	public static Image COG = get("cog.png").getImage();

	private Icons() throws IOException {

	}

	private static Logger log=Logger.getLogger(Icons.class.getCanonicalName());
	
	public static ImageIcon get(String string) {
		if (string.startsWith("http"))
			try {
				return new ImageIcon(new URL(string));
			} catch (MalformedURLException e) {
				log.log(Level.SEVERE,"Problem while loading image from URL: "+string,e);
				return null;
			}
		else
			return new ImageIcon(Icons.class.getResource("/images/" + string));
	}

}
