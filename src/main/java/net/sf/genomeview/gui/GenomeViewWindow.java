/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;

import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class GenomeViewWindow extends JFrame {

	public GenomeViewWindow(Model model, String string,
			GraphicsConfiguration defaultConfiguration) {
		super(string, defaultConfiguration);

	}

}
