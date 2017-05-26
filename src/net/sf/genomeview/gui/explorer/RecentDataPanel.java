/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import be.abeel.gui.GridBagPanel;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.genomeview.gui.dialog.OpenFileButton;
import net.sf.genomeview.gui.dialog.OpenURLButton;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class RecentDataPanel extends GridBagPanel {

	// private Model model;
	// private NCBIRepository repos;

	// class NCBIEntry {
	//
	// private String name;
	// private ArrayList<String> data = new ArrayList<String>();
	//
	// public NCBIEntry(String string) {
	// this.name = string;
	//
	// }
	//
	// public void addData(String data, String label) {
	// this.data.add(data);
	//
	// }
	//
	// public String toString() {
	// return name;
	// }
	//
	// public void load() {
	// String prefix = Configuration.get("instance:ncbiprefix");
	// for (String s : data) {
	// try {
	// DataSourceHelper.load(model, new Locator(prefix + s));
	// } catch (Exception e) {
	// CrashHandler.showErrorMessage(MessageManager.getString("ncbipanel.failed_to_load_data"),
	// e);
	// }
	// }
	//
	// }
	//
	// }

	// class LocationCollectionModel implements ListModel {
	// private List<String> list=null;
	//
	// public LocationCollectionModel(List<String>data) {
	// list=data;
	//
	// }
	//
	// @Override
	// public int getSize() {
	// return list.size();
	// }
	//
	// @Override
	// public String getElementAt(int index) {
	// return list.get(index);
	// }
	//
	// @Override
	// public void addListDataListener(ListDataListener l) {
	// // Do nothing, we don't care about observers
	//
	// }
	//
	// @Override
	// public void removeListDataListener(ListDataListener l) {
	// // Do nothing, we don't care about observers
	//
	// }
	//
	//
	//
	// }

	public RecentDataPanel(final Model model) {
		// this.model = model;
		// this.repos = new NCBIRepository();

		// gc.gridy++;
		gc.weighty = 0;
		gc.gridheight = 3;
		gc.weightx = 0;
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		setPreferredSize(new Dimension((int) (dim.width * 0.9), 480));
		setBackground(Color.WHITE);
		add(new JLabel(Icons.MINILOGO_ICON), gc);

		gc.gridheight = 1;
		gc.gridy += 3;
		JButton myData = new OpenFileButton(model);
		// myData.setBackground(Color.WHITE);
		add(myData, gc);

		gc.gridy++;
		JButton myURL = new OpenURLButton(model);
		// myData.setBackground(Color.WHITE);
		add(myURL, gc);

		gc.gridy++;
		gc.weighty = 1;
		JEditorPaneLabel msgLabel = new JEditorPaneLabel();
		msgLabel.setText("");

		msgLabel.setPreferredSize(new Dimension(Icons.MINILOGO_ICON.getIconWidth(), 50));
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

		gc.gridy = 0;
		gc.gridx++;
		gc.weightx = 1;

		add(new JLabel("Recently used files"), gc);

		final FilteredListModel<String> recent = model.getRecentFiles();

		gc.gridy++;
		add(new FilterField(recent), gc);

		gc.gridy++;
		gc.gridheight = 10;

		final JList<String> recentJList = new JList<String>(recent);
		recentJList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {

					List<String> selectionList = recentJList.getSelectedValuesList();
					for (String selection : selectionList) {
						try {

							DataSourceHelper.load(model, new Locator(selection));
						} catch (Exception e1) {
							CrashHandler.showErrorMessage(MessageManager.getString("genome.couldnt_load_this_session"), e1);
						}
					}

				}
			}
		});

		add(new JScrollPane(recentJList), gc);

		final FilteredListModel<String> extra = model.getExtraSessionFiles();
		final JList<String> extraJList = new JList<String>(extra);

		extraJList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					List<String> selectionList = extraJList.getSelectedValuesList();
					for (String selection : selectionList) {
						try {

							DataSourceHelper.load(model, new Locator(selection));
						} catch (Exception e1) {
							CrashHandler.showErrorMessage(MessageManager.getString("genome.couldnt_load_this_session"), e1);
						}
					}

				}
			}
		});

		gc.gridy = 0;
		gc.gridx++;
		gc.weightx = 1;
		gc.gridheight = 1;
		add(new JLabel("Additional data files from session"), gc);

		gc.gridy++;
		add(new FilterField(extra), gc);

		gc.gridy++;

		gc.gridheight = 10;

		add(new JScrollPane(extraJList), gc);

		// model.addObserver(new Observer(){
		//
		// @Override
		// public void update(Observable o, Object arg) {
		// extra.refresh();
		// recent.refresh();
		// extraJList.repaint();
		// recentJList.repaint();
		//
		//
		//
		// }
		//
		// });

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
		// requestNewGenome.getStyleSheet().addRule("body{ text-align:
		// right;}");
		// requestNewGenome.setText("If your genome of interest is not in the
		// list, <a href=\"mailto:support@genomeview.org\">please e-mail us</a>
		// your request.");
		// requestNewGenome.setBackground(Color.WHITE);
		// add(requestNewGenome,gc);
	}

}
