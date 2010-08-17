/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.annotation.AnnotationFrame;
import net.sf.genomeview.gui.components.AAMappingChooser;
import net.sf.genomeview.gui.information.InformationFrame;
import net.sf.genomeview.gui.menu.edit.RedoAction;
import net.sf.genomeview.gui.menu.edit.UndoAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveLeftAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveRightAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationZoomInAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationZoomOutAction;
import net.sf.jannot.Entry;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class MainContent {

	private static Logger logger = Logger.getLogger(MainContent.class.getCanonicalName());

	private static final long serialVersionUID = -2304899922750491897L;

	public static JPanel[] createContent(Model model, int screens) {
		if (screens == 1) {
			return createOne(model);
		}

//		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
//		logger.info("Detected screen width: "+screen.getWidth());
//		logger.info("Detected screen height: "+screen.getHeight());
//		screen.setSize(screen.getWidth() * 0.7, screen.getHeight() * 0.5);

		JPanel[] out = new JPanel[screens];
		StatusBar sb = new StatusBar(model);
		for (int i = 0; i < out.length - 1; i++) {
			out[i] = new JPanel();
			// registerKeyboard(out[i], model);
			out[i].setLayout(new BorderLayout());
			out[i].add(new Toolbar(model), BorderLayout.PAGE_START);

			AnnotationFrame af = new AnnotationFrame(i, model);
//			af.setPreferredSize(screen);

			out[i].add(af, BorderLayout.CENTER);
			out[i].add(sb, BorderLayout.SOUTH);
//			out[i].setPreferredSize(screen);
		}
		int last = out.length - 1;
		out[last] = new JPanel();
		out[last].setLayout(new BorderLayout());
		out[last].add(new InformationFrame(model));

		return out;
	}

	private static JPanel[] createOne(Model model) {
//		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
//		logger.info("Detected screen width: "+screen.getWidth());
//		logger.info("Detected screen height: "+screen.getHeight());
//		screen.setSize(screen.getWidth() * 0.7, screen.getHeight() * 0.5);
		JPanel[] out = new JPanel[1];
		int last = out.length - 1;
		out[last] = new JPanel();
		// registerKeyboard(out[last], model);
		out[last].setLayout(new BorderLayout());
		out[last].add(new Toolbar(model), BorderLayout.PAGE_START);

		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		Container leftContainer = new Container();

		leftContainer.setLayout(new BorderLayout());

		AnnotationFrame af = new AnnotationFrame(last, model);
//		af.setPreferredSize(screen);

		leftContainer.add(af, BorderLayout.CENTER);

		StatusBar sb = new StatusBar(model);
		leftContainer.add(sb, BorderLayout.SOUTH);

		leftRight.setLeftComponent(leftContainer);
		leftRight.setRightComponent(new InformationFrame(model));
		leftRight.setResizeWeight(1);
		leftRight.setOneTouchExpandable(true);
		
		// out[last].add(new InformationFrame(model));
		out[last].add(leftRight);
		return out;
	}

}
