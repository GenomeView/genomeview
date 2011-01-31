/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.awt.Image;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class Icons {
	//FIXME fix with get method
	public static final Icon DELETE = new ImageIcon(Icons.class.getResource("/images/delete.png"));

	public static final Icon YES = new ImageIcon(Icons.class.getResource("/images/yes.png"));

	public static final Icon NO = new ImageIcon(Icons.class.getResource("/images/no.png"));
	public static final Icon BDASH = new ImageIcon(Icons.class.getResource("/images/bdash.png"));

	public static final Icon VISIBLE=new ImageIcon(Icons.class.getResource("/images/visible.png"));
	public static final Icon INVISIBLE=new ImageIcon(Icons.class.getResource("/images/invisible.png"));
	
	public static final Icon EDIT = new ImageIcon(Icons.class.getResource("/images/edit.png"));

	public static final Icon DOWN_ARROW = new ImageIcon(Icons.class.getResource("/images/downarrow.png"));

	public static final Icon UP_ARROW = new ImageIcon(Icons.class.getResource("/images/uparrow.png"));

	public static final Icon HELP = new ImageIcon(Icons.class.getResource("/images/help.png"));

	public static final Icon PLAZA = new ImageIcon(Icons.class.getResource("/images/search/plaza.png"));

	public static Icon GOOGLE = new ImageIcon(Icons.class.getResource("/images/search/google.png"));
	public static Icon NCBI = new ImageIcon(Icons.class.getResource("/images/search/ncbi.png"));
	public static Icon Ensembl = new ImageIcon(Icons.class.getResource("/images/search/ensembl.png"));

	public static Icon LOGO = new ImageIcon(Icons.class.getResource("/images/logo.png"));

	public static Icon LARGEOPEN=get("boxopen.png");
	
	public static Image MINILOGO=get("gv2.png").getImage();

	private Icons() throws IOException {

	}

	public static ImageIcon get(String string) {
		return new ImageIcon(Icons.class.getResource("/images/"+string));
	}

}

