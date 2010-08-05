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

public class MainContent {

	private static Logger logger = Logger.getLogger(MainContent.class.getCanonicalName());

	private static final long serialVersionUID = -2304899922750491897L;

	private static JToolBar createToolBar(Model model) {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);

		bar.add(new UndoAction(model));
		bar.add(new RedoAction(model));
		bar.addSeparator();
		bar.add(new AnnotationZoomInAction(model));
		bar.add(new AnnotationZoomOutAction(model));
		bar.add(new AnnotationMoveLeftAction(model));
		bar.add(new AnnotationMoveRightAction(model));
		bar.addSeparator();
		bar.add(new JLabel("Entry:"));
		bar.add(new JComboBox(new EntryListModel(model)));
		if (Configuration.getBoolean("geneticCodeSelection")) {
			bar.add(new JLabel("Code:"));
			bar.add(new AAMappingChooser(model));
		}
		// String sponsor = Configuration.get("sponsor");
		// if (sponsor.length() != 0) {
		// URL url = Icons.class.getResource(sponsor);
		// logger.info("Sponser logo url: " + url);
		// if (url != null) {
		// ImageIcon sponsorLogo = new ImageIcon(url);
		// bar.add(new JLabel(sponsorLogo));
		// }
		// }

		bar.add(new JLabel(new ImageIcon(Icons.class.getResource("/images/vib.png"))));
		return bar;
	}

	public static JPanel[] createContent(Model model, int screens) {
		if (screens == 1) {
			return createOne(model);
		}

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screen.setSize(screen.getWidth() * 0.7, screen.getHeight() * 0.5);

		JPanel[] out = new JPanel[screens];
		StatusBar sb = new StatusBar(model);
		for (int i = 0; i < out.length - 1; i++) {
			out[i] = new JPanel();
			// registerKeyboard(out[i], model);
			out[i].setLayout(new BorderLayout());
			out[i].add(createToolBar(model), BorderLayout.PAGE_START);

			AnnotationFrame af = new AnnotationFrame(i, model);
			af.setPreferredSize(screen);

			out[i].add(af, BorderLayout.CENTER);
			out[i].add(sb, BorderLayout.SOUTH);
		}
		int last = out.length - 1;
		out[last] = new JPanel();
		out[last].setLayout(new BorderLayout());
		out[last].add(new InformationFrame(model));

		return out;
	}

	private static JPanel[] createOne(Model model) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screen.setSize(screen.getWidth() * 0.7, screen.getHeight() * 0.5);
		JPanel[] out = new JPanel[1];
		int last = out.length - 1;
		out[last] = new JPanel();
		// registerKeyboard(out[last], model);
		out[last].setLayout(new BorderLayout());
		out[last].add(createToolBar(model), BorderLayout.PAGE_START);

		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		Container leftContainer = new Container();

		leftContainer.setLayout(new BorderLayout());

		AnnotationFrame af = new AnnotationFrame(last, model);
		af.setPreferredSize(screen);

		leftContainer.add(af, BorderLayout.CENTER);

		StatusBar sb = new StatusBar(model);
		leftContainer.add(sb, BorderLayout.SOUTH);

		leftRight.setLeftComponent(leftContainer);
		leftRight.setRightComponent(new InformationFrame(model));
		leftRight.setDividerLocation(0.75);
		// out[last].add(new InformationFrame(model));
		out[last].add(leftRight);
		return out;
	}

}
