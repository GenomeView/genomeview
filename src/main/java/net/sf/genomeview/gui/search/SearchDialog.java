/**
 *    This file is part of GenomeView.
 *
 *    GenomeView is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JAnnot is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with GenomeView.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.genomeview.gui.search;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JTabbedPane;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.EscapeDialog;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SearchDialog extends EscapeDialog {

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
