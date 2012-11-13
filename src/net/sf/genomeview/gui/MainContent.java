/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.information.InformationFrame;
import net.sf.genomeview.gui.viztracks.AnnotationFrame;

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

		JPanel[] out = new JPanel[screens];
		StatusBar sb = new StatusBar(model);
		for (int i = 0; i < out.length - 1; i++) {
			out[i] = new JPanel();
			// registerKeyboard(out[i], model);
			out[i].setLayout(new BorderLayout());
			out[i].add(new Toolbar(model), BorderLayout.PAGE_START);

			AnnotationFrame af = new AnnotationFrame(i, model);
			out[i].add(af, BorderLayout.CENTER);
			out[i].add(sb, BorderLayout.SOUTH);

		}
		int last = out.length - 1;
		out[last] = new JPanel();
		out[last].setLayout(new BorderLayout());
		out[last].add(new InformationFrame(model), BorderLayout.CENTER);

		return out;
	}

	private static JPanel[] createOne(Model model) {
		// Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// logger.info("Detected screen width: "+screen.getWidth());
		// logger.info("Detected screen height: "+screen.getHeight());
		// screen.setSize(screen.getWidth() * 0.7, screen.getHeight() * 0.5);
		JPanel[] out = new JPanel[1];

		out[0] = new JPanel();
		// registerKeyboard(out[0], model);
		out[0].setLayout(new BorderLayout());
		out[0].add(new Toolbar(model), BorderLayout.PAGE_START);

		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		Container leftContainer = new Container();

		leftContainer.setLayout(new BorderLayout());

		AnnotationFrame af = new AnnotationFrame(0, model);
		// af.setPreferredSize(screen);

		leftContainer.add(af, BorderLayout.CENTER);

		StatusBar sb = new StatusBar(model);
		leftContainer.add(sb, BorderLayout.SOUTH);

		leftRight.setLeftComponent(leftContainer);
		leftRight.setRightComponent(new InformationFrame(model));
		Rectangle rec = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		leftRight.setPreferredSize(new Dimension(rec.width, rec.height));
		leftRight.setResizeWeight(0.99);

		leftRight.setOneTouchExpandable(true);

		// out[0].add(new InformationFrame(model));
		out[0].add(leftRight, BorderLayout.CENTER);
		return out;
	}

	public static JPanel createMiniContent(Model model) {
		JPanel out = new JPanel();
		// registerKeyboard(out[0], model);
		out.setLayout(new BorderLayout());
		out.add(new Toolbar(model), BorderLayout.PAGE_START);

		// JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		// Container leftContainer = new Container();

		// leftContainer.setLayout(new BorderLayout());

		AnnotationFrame af = new AnnotationFrame(0, model);
		// af.setPreferredSize(screen);

		// leftContainer.add(af, BorderLayout.CENTER);
		out.add(af, BorderLayout.CENTER);
		StatusBar sb = new StatusBar(model);
		out.add(sb, BorderLayout.SOUTH);

		return out;
	}

}
