/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
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
public class ReferenceMissingMonitor extends JDialog implements Observer {

	private static final long serialVersionUID = 566883531326807914L;
	private JEditorPaneLabel floater = new JEditorPaneLabel();
	private Model model;
	private boolean dismissed = false;
	private JScrollPane jp;
	private static ReferenceMissingMonitor rmm=null;
	public static void init(Model model){
		if(rmm==null)
			rmm=new ReferenceMissingMonitor(model);
		
	}
	
	private ReferenceMissingMonitor(Model model) {
		super(model.getGUIManager().getParent(), ModalityType.MODELESS);
		setTitle("Reference sequence missing!!");
		setIconImage(Icons.MINILOGO);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				dismissed=true;
				super.windowClosing(e);
			}
			
		});
		
		final JDialog _self = this;

		this.model = model;
		model.addObserver(this);
		model.getWorkerManager().addObserver(this);
		Rectangle bounds = model.getGUIManager().getParent().getBounds();
		
		this.setPreferredSize(new Dimension(bounds.width / 3, bounds.height / 5));
		
		this.setLocation(bounds.x + bounds.width / 3, bounds.y + bounds.height / 5);

		floater.setOpaque(true);
		floater.setText("");
		floater.setForeground(Color.BLACK);
		Border emptyBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		Border colorBorder = BorderFactory.createLineBorder(Color.RED);
		floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		floater.setBackground(new Color(255, 0, 0, 100));
		setLayout(new BorderLayout());
		jp=new JScrollPane(floater);
		jp.getVerticalScrollBar().addAdjustmentListener(new ScrollFixer(jp));
		add(jp, BorderLayout.CENTER);

		Container buttons = new Container();
		buttons.setLayout(new BorderLayout());
		add(buttons, BorderLayout.SOUTH);

		JButton dismiss = new JButton("Dismiss");
		dismiss.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_self.setVisible(false);
				dismissed = true;

			}
		});

		buttons.add(dismiss, BorderLayout.WEST);

		JButton data = new JButton("Load data");
		data.addActionListener(new LoadFeaturesAction(model));
		buttons.add(data, BorderLayout.EAST);
		pack();
	}

	



	private int lastMissing = -1;

	class ScrollFixer implements AdjustmentListener{

		private JScrollPane jp;

		public ScrollFixer(JScrollPane jp) {
			this.jp=jp;
		}

		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			if(!e.getValueIsAdjusting())
				jp.repaint();
			
		}

	
		
	}
	
	@Override
	public void update(Observable o, Object arg) {
		final JDialog _self = this;
		/* Data was reset */
		if (dismissed && model.entries().size() == 0)
			dismissed = false;
		/* When window was dismissed, we don't want to show it anymore */
		if (dismissed)
			return;
		/* While there is still data loading, we wait */
		if (model.getWorkerManager().runningJobs() > 0) {
			setVisible(false);
			return;

		}
		/**
		 * Detect missing reference sequences.
		 */
		EntrySet es = model.entries();
		int missingReference = 0;
		ArrayList<String> missing = new ArrayList<String>();
		for (Entry e : es) {
			if (e.sequence().size() == 0) {
				//System.out.println(e.sequence().getClass());
				int dataCount = 0;
				for (DataKey dk : e)
					dataCount++;
				if (dataCount > 0) {
					missingReference++;
					missing.add(e.getID());
				}
			}
		}
		
		if (missingReference > 0 && lastMissing != missingReference) {
			StringBuffer msg = new StringBuffer(
					"<html><h1>Warning!!</h1>Not every entry has a reference sequence loaded! Some visualizations will not work as expected without reference.<br><br>Entries without reference: ");
			for (String s : missing) {
				msg.append("\n" + s);
			}
			msg.append("</html>");
			floater.setText(msg.toString());
			/* Make dialog visible */
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					if(!Configuration.getBoolean("general:ignoreMissingReferences"))
						_self.setVisible(true);

				}
			});
			/* Scroll to top */
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					jp.getVerticalScrollBar().setValue(0);

				}
			});

		} else if(missingReference == 0){
			this.setVisible(false);

		}
		lastMissing = missingReference;

	}
}
