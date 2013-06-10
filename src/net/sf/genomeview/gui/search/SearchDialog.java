/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SearchDialog extends JDialog {

	private static final long serialVersionUID = 6844861145552724990L;

	private static SearchDialog dialog = null;

	final private JTabbedPane pane;

	private SearchDialog(Model model) {
		super(model.getGUIManager().getParent(), MessageManager.getString("searchdialog.search"));
		setModalityType(ModalityType.MODELESS);
		pane = new JTabbedPane();
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
		pane.add(MessageManager.getString("searchdialog.keyword_search"), new KeywordSearchPane(model));
		pane.add(MessageManager.getString("searchdialog.entry_search"),new EntrySearchPane(model));
		pane.add(MessageManager.getString("searchdialog.sequence_search"), new SequenceSearchPane(model));
		pane.add(MessageManager.getString("searchdialog.motif_search"), new MotifSearchPane(model));
		
		pane.add(MessageManager.getString("searchdialog.overlap_search"), new OverlapSearchPane(model));

		pack();
		StaticUtils.right(this, model.getGUIManager().getParent());

	}

	public static void showDialog(Model model) {
		if (dialog == null)
			dialog = new SearchDialog(model);
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				dialog.setVisible(true);

			}
		});
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				((SearchPanel) dialog.pane.getComponentAt(dialog.pane.getSelectedIndex())).getFocusComponent().requestFocusInWindow();

			}
		});

	}

	enum SequenceType {
		Nucleotide, AminoAcid;
		@Override
		public String toString() {
			switch (this) {
			case Nucleotide:
				return "Nucleotide";
			case AminoAcid:
				return "Amino acid";
			}
			return null;
		}
	}

}
