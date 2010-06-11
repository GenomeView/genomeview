/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.image4j.codec.ico.ICODecoder;

public class Icons {
	
	
	public static final Icon DELETE = new ImageIcon(Icons.class.getResource("/images/delete.png"));

	public static final Icon YES = new ImageIcon(Icons.class.getResource("/images/yes.png"));

	public static final Icon NO = new ImageIcon(Icons.class.getResource("/images/no.png"));
	public static final Icon BDASH = new ImageIcon(Icons.class.getResource("/images/bdash.png"));

	public static final Icon EDIT = new ImageIcon(Icons.class.getResource("/images/edit.png"));

	public static final Icon DOWN_ARROW = new ImageIcon(Icons.class.getResource("/images/downarrow.png"));

	public static final Icon UP_ARROW = new ImageIcon(Icons.class.getResource("/images/uparrow.png"));

	public static final Icon HELP = new ImageIcon(Icons.class.getResource("/images/help.png"));

	public static Icon GOOGLE= new ImageIcon(Icons.class.getResource("/images/search/google.png"));;
	
	private Icons() throws IOException {

	}

}
