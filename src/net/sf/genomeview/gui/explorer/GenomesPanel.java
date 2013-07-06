/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.genomeview.gui.dialog.OpenDialog;
import be.abeel.gui.GridBagPanel;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class GenomesPanel extends GridBagPanel implements ScrollToTop {

	private static Logger log = LoggerFactory.getLogger(GenomesPanel.class.getCanonicalName());

	private static final long serialVersionUID = 2022250811407852659L;
	private JScrollPane jp;

	public GenomesPanel(final Model model, String msg, ArrayList<Genome> list) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		setPreferredSize(new Dimension((int) (dim.width * 0.8), 480));
		setBackground(Color.WHITE);
		gc.gridwidth = 2;
		add(new JLabel(Icons.LOGO), gc);
		gc.gridy++;
		gc.weightx = 0;

		JButton myData = new JButton(MessageManager.getString("genomespanel.work_with_my_data"), Icons.LARGEOPEN);
		myData.setBackground(Color.WHITE);
		gc.gridwidth = 1;
		add(myData, gc);
		myData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.getGUIManager().getGenomeExplorer().bg.setVisible(false);
				new OpenDialog(model.getGUIManager().getGenomeExplorer().bg, model);

			}
		});

		JButton restore = new JButton(MessageManager.getString("genomespanel.restore_session"), Icons.LARGEOPEN);
		final File prevSession = new File(Configuration.getDirectory(), "previous.gvs");
		restore.setEnabled(prevSession.exists() && prevSession.length() > 100);
		restore.setBackground(Color.WHITE);
		gc.gridx++;
		add(restore, gc);
		restore.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				log.info("Loading previous session: " + prevSession);
				try {
					Session.loadSession(model, prevSession);
				} catch (FileNotFoundException e1) {
					CrashHandler.showErrorMessage("Failed to restore previous session", e1);
				}

			}
		});

		gc.gridwidth = 2;
		gc.gridx--;
		gc.gridy++;
		gc.weighty = 1;
		JEditorPaneLabel msgLabel = new JEditorPaneLabel();
		msgLabel.setText(msg);

		msgLabel.setPreferredSize(new Dimension(Icons.LOGO.getIconWidth(), 50));
		add(msgLabel, gc);

		gc.weighty = 0;
		gc.gridy++;
		JButton box = new JButton(MessageManager.getString("button.dismiss_dialog"));
		box.setBackground(Color.WHITE);
		box.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.getGUIManager().getGenomeExplorer().setVisible(false);

			}
		});
		add(box, gc);

		/*------------------*/
		gc.gridy = 0;
		gc.gridx+=2;
		gc.gridheight = 10;
		gc.weightx = 1;
		gc.weighty = 1;
		jp = new JScrollPane();
		jp.getVerticalScrollBar().setUnitIncrement(25);
		GridBagPanel buttons = new GridBagPanel() {

			private static final long serialVersionUID = 7455856822406470518L;

			@Override
			public Dimension getPreferredSize() {
				Dimension dim = super.getPreferredSize();
				JScrollBar jb = jp.getVerticalScrollBar();
				int sx = 0;
				if (jb.isVisible()) {
					sx = jb.getWidth();
				}
				return new Dimension(jp.getWidth() - sx - gc.insets.right, dim.height);
			}
		};
		buttons.setBackground(Color.WHITE);
		buttons.setSize(10, 100);
		buttons.gc.fill = GridBagConstraints.HORIZONTAL;
		buttons.gc.weightx = 1;
		buttons.gc.weighty = 0;
		buttons.gc.anchor = GridBagConstraints.NORTHWEST;
		if (list != null) {
			for (Genome g : list) {
				buttons.add(g, buttons.gc);
				buttons.gc.gridy++;
			}
		}
		buttons.gc.weighty = 1;
		JLabel space = new JLabel();
		space.setBackground(Color.WHITE);
		buttons.add(space, buttons.gc);

		jp.setViewportView(buttons);
		jp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		add(jp, gc);

		gc.gridx = 0;
		gc.gridy = 20;
		gc.gridwidth = 4;
		gc.weightx = 1;
		gc.weighty = 0;
		JEditorPaneLabel requestNewGenome = new JEditorPaneLabel();
		requestNewGenome.getStyleSheet().addRule("body{ text-align: right;}");
		requestNewGenome.setText(MessageManager.getString("genomespanel.request_text"));
		requestNewGenome.setBackground(Color.WHITE);
		add(requestNewGenome, gc);

	}

	public void scrollToTop() {
		jp.getVerticalScrollBar().setValue(0);

	}
}
