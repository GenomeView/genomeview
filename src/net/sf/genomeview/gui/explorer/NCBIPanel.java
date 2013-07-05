/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.List;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.genomeview.gui.dialog.OpenDialog;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;
import be.abeel.gui.GridBagPanel;
import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class NCBIPanel extends GridBagPanel implements ScrollToTop {

	private Model model;
	private NCBIRepository repos;

	class NCBIEntry {

		private String name;
		private ArrayList<String> data = new ArrayList<String>();

		public NCBIEntry(String string) {
			this.name = string;

		}

		public void addData(String data, String label) {
			this.data.add(data);

		}

		public String toString() {
			return name;
		}

		public void load() {
			String prefix = Configuration.get("instance:ncbiprefix");
			for (String s : data) {
				try {
					DataSourceHelper.load(model, new Locator(prefix + s));
				} catch (Exception e) {
					CrashHandler.showErrorMessage(MessageManager.getString("ncbipanel.failed_to_load_data"), e);
				}
			}

		}

	}

	class NCBIRepository implements ListModel {
		private ArrayList<NCBIEntry> list = new ArrayList<NCBIEntry>();

		public NCBIRepository() {
			try {
				LineIterator it = new LineIterator(NCBIPanel.class.getResourceAsStream("/conf/ncbi.tsv"), true, true);
				NCBIEntry entry = null;
				for (String line : it) {
					String[] arr = line.split("\t");
					if (line.startsWith("species")) {
						entry = new NCBIEntry(arr[1]);
						list.add(entry);
					} else {
						entry.addData(arr[1], arr[2]);
					}

				}
				it.close();
			} catch (Exception e) {
				CrashHandler.showErrorMessage(MessageManager.getString("ncbipanel.coudnt_read_ncbi"), e);
			}
		}

		@Override
		public int getSize() {
			return list.size();
		}

		@Override
		public NCBIEntry getElementAt(int index) {
			return list.get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			// Do nothing, we don't care about observers

		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			// Do nothing, we don't care about observers

		}
	}

	public NCBIPanel(final Model model) {
		this.model = model;
		this.repos = new NCBIRepository();

		gc.gridy++;
		gc.weighty = 0;
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		setPreferredSize(new Dimension((int) (dim.width * 0.8), 480));
		setBackground(Color.WHITE);
		add(new JLabel(Icons.LOGO), gc);
		gc.gridy++;
		gc.weightx = 0;

		JButton myData = new JButton(MessageManager.getString("ncbipanel.work_with_my_data"), Icons.LARGEOPEN);
		myData.setBackground(Color.WHITE);
		add(myData, gc);
		myData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.getGUIManager().getGenomeExplorer().bg.setVisible(false);
				new OpenDialog(model.getGUIManager().getGenomeExplorer().bg, model);

			}
		});

		gc.gridy++;
		gc.weighty = 1;
		JEditorPaneLabel msgLabel = new JEditorPaneLabel();
		msgLabel.setText(MessageManager.getString("ncbipanel.all_ncbi_bacterial_data"));

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

		final JList list = new JList(repos);

		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					repos.getElementAt(list.getSelectedIndex()).load();
				}
			}
		});

		gc.gridy = 0;
		gc.gridx++;
		gc.weightx = 1;
		gc.gridheight = 10;

		add(new JScrollPane(list), gc);
		// gc.weighty = 0;
		// gc.gridy++;
		// JButton box=new JButton("Dismiss this dialog");
		// box.setBackground(Color.WHITE);
		// box.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// model.getGUIManager().getGenomeExplorer().setVisible(false);
		//
		// }
		// });
		// add(box, gc);

		/*------------------*/
		// gc.gridy = 0;
		// gc.gridx++;
		// gc.gridheight = 10;
		// gc.weightx = 1;
		// gc.weighty = 1;
		// jp = new JScrollPane();
		// jp.getVerticalScrollBar().setUnitIncrement(25);
		// GridBagPanel buttons = new GridBagPanel(){
		//
		// private static final long serialVersionUID = 7455856822406470518L;
		//
		// @Override
		// public Dimension getPreferredSize() {
		// Dimension dim=super.getPreferredSize();
		// JScrollBar jb=jp.getVerticalScrollBar();
		// int sx=0;
		// if(jb.isVisible()){
		// sx=jb.getWidth();
		// }
		// return new Dimension(jp.getWidth()-sx-gc.insets.right, dim.height);
		// }
		// };
		// buttons.setBackground(Color.WHITE);
		// buttons.setSize(10, 100);
		// buttons.gc.fill = GridBagConstraints.HORIZONTAL;
		// buttons.gc.weightx = 1;
		// buttons.gc.weighty = 0;
		// buttons.gc.anchor = GridBagConstraints.NORTHWEST;
		// if (list != null) {
		// for (Genome g : list) {
		// buttons.add(g, buttons.gc);
		// buttons.gc.gridy++;
		// }
		// }
		// buttons.gc.weighty = 1;
		// JLabel space=new JLabel();
		// space.setBackground(Color.WHITE);
		// buttons.add(space, buttons.gc);
		//
		//
		// jp.setViewportView(buttons);
		// jp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//
		// add(jp, gc);
		//
		//
		//
		//
		// gc.gridx=0;
		// gc.gridy=20;
		// gc.gridwidth=2;
		// gc.weightx=1;
		// gc.weighty=0;
		// JEditorPaneLabel requestNewGenome=new JEditorPaneLabel();
		// requestNewGenome.getStyleSheet().addRule("body{ text-align: right;}");
		// requestNewGenome.setText("If your genome of interest is not in the list, <a href=\"mailto:support@genomeview.org\">please e-mail us</a> your request.");
		// requestNewGenome.setBackground(Color.WHITE);
		// add(requestNewGenome,gc);
	}

	@Override
	public void scrollToTop() {
		// TODO Auto-generated method stub

	}

}
