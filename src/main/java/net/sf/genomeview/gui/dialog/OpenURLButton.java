package net.sf.genomeview.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;

public class OpenURLButton extends JButton {

	private static final long serialVersionUID = -1666800247496691936L;

	public OpenURLButton(final Model gvModel) {
		super(gvModel.getMessageMgr().getString("opendialog.url"),
				Icons.get("Globe_48x48.png"));
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				String input = JOptionPane.showInputDialog(
						gvModel.getGUIManager().getMainWindow(),
						"Give the URL of the data");
				if (input != null && input.trim().length() > 0) {
					try {
						DataSourceHelper.load(gvModel,
								new Locator(input.trim(), gvModel.getLog()));

					} catch (IOException | URISyntaxException
							| ReadFailedException e2) {
						// pretty nasty if this would fail to open.
						gvModel.getLog().log(Level.WARNING,
								"Failed to open " + input, e2);
					}
				}

			}
		});
	}
}
