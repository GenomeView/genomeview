/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

import net.sf.genomeview.data.Model;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class ReferenceMissingMonitor implements Observer {

	private static final long serialVersionUID = 566883531326807914L;
//	private JEditorPaneLabel floater = new JEditorPaneLabel();
	private Model model;
//	private boolean dismissed = false;
//	private JScrollPane jp;
	private static ReferenceMissingMonitor rmm = null;

	public static void init(Model model) {
		if (rmm == null) {
			rmm = new ReferenceMissingMonitor(model);
		}

	}

	private ReferenceMissingMonitor(Model model) {
		// super(model.getGUIManager().getMainWindow(), ModalityType.MODELESS);
//		setTitle(MessageManager.getString("referencemissing.title"));
//		setIconImage(Icons.MINILOGO);
//		addWindowListener(new WindowAdapter() {
//
//			@Override
//			public void windowClosing(WindowEvent e) {
//				dismissed = true;
//				super.windowClosing(e);
//			}
//
//		});

//		final JDialog _self = this;

		this.model = model;
		model.addObserver(this);
		model.getWorkerManager().addObserver(this);
		Rectangle bounds = model.getGUIManager().getMainWindow().getBounds();

//		this.setPreferredSize(
//				new Dimension(bounds.width / 3, bounds.height / 5));
//
//		this.setLocation(bounds.x + bounds.width / 3,
//				bounds.y + bounds.height / 5);

//		floater.setOpaque(true);
//		floater.setText("");
//		floater.setForeground(Color.BLACK);
//		Border emptyBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
//		Border colorBorder = BorderFactory.createLineBorder(Color.RED);
//		floater.setBorder(
//				BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
//		floater.setBackground(new Color(255, 0, 0, 100));
//		setLayout(new BorderLayout());
//		jp = new JScrollPane(floater);
//		jp.getVerticalScrollBar().addAdjustmentListener(new ScrollFixer(jp));
//		add(jp, BorderLayout.CENTER);
//
//		Container buttons = new Container();
//		buttons.setLayout(new BorderLayout());
//		add(buttons, BorderLayout.SOUTH);
//
//		JButton dismiss = new JButton(
//				MessageManager.getString("referencemissing.dismiss"));
//		dismiss.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				_self.setVisible(false);
//				dismissed = true;
//
//			}
//		});
//
//		buttons.add(dismiss, BorderLayout.WEST);
//
//		JButton data = new JButton(
//				MessageManager.getString("referencemissing.load_data"));
//		data.addActionListener(new LoadFeaturesAction(model));
//		buttons.add(data, BorderLayout.EAST);
//		pack();
	}

	private int lastMissing = -1;

//	class ScrollFixer implements AdjustmentListener {
//
//		private JScrollPane jp;
//
//		public ScrollFixer(JScrollPane jp) {
//			this.jp = jp;
//		}
//
//		@Override
//		public void adjustmentValueChanged(AdjustmentEvent e) {
//			if (!e.getValueIsAdjusting())
//				jp.repaint();
//
//		}
//
//	}

	@Override
	public void update(Observable o, Object arg) {
//		final JDialog _self = this;
		/* Data was reset */
//		if (dismissed && model.entries().size() == 0)
//			dismissed = false;
//		/* When window was dismissed, we don't want to show it anymore */
//		if (dismissed)
//			return;
//		/* While there is still data loading, we wait */
//		if (model.getWorkerManager().runningJobs() > 0) {
//			setVisible(false);
//			return;
//
//		}
		/**
		 * Detect missing reference sequences.
		 */
		EntrySet es = model.entries();
		int missingReference = 0;
		ArrayList<String> missing = new ArrayList<String>();
		for (Entry e : es) {
			if (e.sequence().size() == 0) {
				// System.out.println(e.sequence().getClass());
				int dataCount = 0;
				for (DataKey dk : e) {
					dataCount++;
				}
				if (dataCount > 0) {
					missingReference++;
					missing.add(e.getID());
				}
			}
		}

		if (missingReference > 0 && lastMissing != missingReference) {
			String msg = model.getMessageMgr().getString(
					"referencemissing.not_every_entry_has_reference") + missing;
			model.getLog().log(Level.WARNING, msg);
//			floater.setText(msg.toString());
			/* Make dialog visible */
//			EventQueue.invokeLater(new Runnable() {
//
//				@Override
//				public void run() {
//					if (!Configuration.instance()
//							.getBoolean("general:ignoreMissingReferences"))
//						_self.setVisible(true);
//
//				}
//			});
			/* Scroll to top */
//			EventQueue.invokeLater(new Runnable() {
//
//				@Override
//				public void run() {
//					jp.getVerticalScrollBar().setValue(0);
//
//				}
//			});
		}
//		} else if (missingReference == 0) {
//			this.setVisible(false);
//
//		}
		lastMissing = missingReference;

	}
}
