/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.genomeview.gui.menu.file.LoadFeaturesAction;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeViewWindow extends JFrame {

	public GenomeViewWindow(Model model, String string, GraphicsConfiguration defaultConfiguration) {
		super(string, defaultConfiguration);

	}

	private static final long serialVersionUID = 7684262555440299887L;

}
