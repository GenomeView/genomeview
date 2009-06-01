/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.annotation.AnnotationFrame;
import net.sf.genomeview.gui.chromosome.ChromosomeFrame;
import net.sf.genomeview.gui.components.AAMappingChooser;
import net.sf.genomeview.gui.information.InformationFrame;
import net.sf.genomeview.gui.menu.edit.RedoAction;
import net.sf.genomeview.gui.menu.edit.UndoAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveLeftAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveRightAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationZoomInAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationZoomOutAction;
import net.sf.genomeview.gui.menu.selection.SelectFromSelectedBack;
import net.sf.genomeview.gui.menu.selection.SelectFromSelectedFirst;
import net.sf.genomeview.gui.menu.selection.SelectFromSelectedForward;
import net.sf.genomeview.gui.menu.selection.SelectFromSelectedLast;
import net.sf.jannot.Entry;

public class MainContent {

	private static Logger logger = Logger.getLogger(MainContent.class
			.getCanonicalName());

	private static final long serialVersionUID = -2304899922750491897L;

	// private AnnotationFrame af;
	//
	private static JSplitPane annotChrom;

	//
	// public MainContent(Model model) {
	//       
	// }

	/**
	 * Register all custom keyboard actions.
	 * 
	 * @param model2
	 * 
	 * 
	 */
	private static void registerKeyboard(JComponent comp, Model model) {

		InputMap inputs = comp
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		System.out.println(inputs);
		ActionMap actions = comp.getActionMap();
		/* move left */
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0),
				"customMoveLeft");
		actions.put("customMoveLeft", new AnnotationMoveLeftAction(model));
		/* move right */
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0),
				"customMoveRight");
		actions.put("customMoveRight", new AnnotationMoveRightAction(model));
		/* zoom in */
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "customZoomIn");
		actions.put("customZoomIn", new AnnotationZoomInAction(model));
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0),
				"customZoomOut");
		actions.put("customZoomOut", new AnnotationZoomOutAction(model));

		/* select first from selection */
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0),
				"customSelectFirst");
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
				"customSelectFirst");
		actions.put("customSelectFirst", new SelectFromSelectedFirst(model));

		/* select last from selection */
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0),
				"customSelectLast");
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
				"customSelectLast");
		actions.put("customSelectLast", new SelectFromSelectedLast(model));

		/* move selection one location forward */
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0),
				"customSelectForward");
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
				"customSelectForward");
		actions
				.put("customSelectForward",
						new SelectFromSelectedForward(model));

		/* move selection one location back */
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0),
				"customSelectBack");
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
				"customSelectBack");
		actions.put("customSelectBack", new SelectFromSelectedBack(model));

	}

	private static JToolBar createToolBar(Model model) {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);

		bar.add(new UndoAction(model));
		bar.add(new RedoAction(model));
		bar.addSeparator();
		bar.add(new JLabel("Entry:"));
		bar.add(new JComboBox(new ChromosomeListModel(model)));
		if (Configuration.getBoolean("geneticCodeSelection")) {
			bar.add(new JLabel("Code:"));
			bar.add(new AAMappingChooser(model));
		}
		String sponsor = Configuration.get("sponsor");
		if (sponsor.length() != 0) {
			URL url = Icons.class.getResource(sponsor);
			logger.info("Sponser logo url: " + url);
			if (url != null) {
				ImageIcon sponsorLogo = new ImageIcon(url);
				bar.add(new JLabel(sponsorLogo));
			}
		}

		bar.add(new JLabel(new ImageIcon(Icons.class
				.getResource("/images/vib.png"))));
		return bar;
	}

	public static JPanel[] createContent(Model model, int screens) {
		if(screens==1){
			return createOne(model);
		}
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screen.setSize(screen.getWidth() * 0.7, screen.getHeight() * 0.5);

		JPanel[] out = new JPanel[screens];
		for (int i = 0; i < out.length - 1; i++) {
			out[i] = new JPanel();
			registerKeyboard(out[i], model);
			out[i].setLayout(new BorderLayout());
			out[i].add(createToolBar(model), BorderLayout.PAGE_START);

			annotChrom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

			annotChrom.setTopComponent(new ChromosomeFrame(i,model));
			AnnotationFrame af = new AnnotationFrame(i,model);
			af.setPreferredSize(screen);
			annotChrom.setBottomComponent(af);

			out[i].add(annotChrom, BorderLayout.CENTER);
		}
		int last = out.length - 1;
		out[last] = new JPanel();
		out[last].setLayout(new BorderLayout());
		out[last].add(new InformationFrame(model));

		model.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				Model model = (Model) o;
				boolean tmp = model.isAnnotationVisible();
				if (tmp && (annotVisible ^ tmp)) {
					annotChrom.resetToPreferredSizes();
				}
				annotVisible = tmp;
			}
		});
		return out;
	}

	private static JPanel[] createOne(Model model) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screen.setSize(screen.getWidth() * 0.7, screen.getHeight() * 0.5);
		JPanel[]out=new JPanel[1];
		int last = out.length - 1;
		out[last] = new JPanel();
		registerKeyboard(out[last], model);
		out[last].setLayout(new BorderLayout());
		out[last].add(createToolBar(model), BorderLayout.PAGE_START);

		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		Container leftContainer = new Container();

		leftContainer.setLayout(new BorderLayout());

		annotChrom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		annotChrom.setTopComponent(new ChromosomeFrame(last,model));
		AnnotationFrame af = new AnnotationFrame(last,model);
		af.setPreferredSize(screen);
		annotChrom.setBottomComponent(af);

		leftContainer.add(annotChrom, BorderLayout.CENTER);

		leftRight.setLeftComponent(leftContainer);
		leftRight.setRightComponent(new InformationFrame(model));
//		out[last].add(new InformationFrame(model));
		out[last].add(leftRight);
		return out;
	}

	private static boolean annotVisible = false;

}

class ChromosomeListModel extends DefaultComboBoxModel implements Observer {

	private Model model;

	public ChromosomeListModel(Model model) {
		model.addObserver(this);
		this.model = model;
	}

	private static final long serialVersionUID = -3028394066023453566L;

	@Override
	public Object getElementAt(int arg0) {
		return model.entry(arg0);
	}

	@Override
	public int getSize() {
		return model.noEntries();
	}

	@Override
	public Object getSelectedItem() {
		return model.getSelectedEntry();
	}

	@Override
	public void setSelectedItem(Object anItem) {
		model.setSelectedEntry((Entry) anItem);

	}

	@Override
	public void update(Observable arg0, Object arg1) {
		fireContentsChanged(arg0, 0, model.noEntries());

	}

}
