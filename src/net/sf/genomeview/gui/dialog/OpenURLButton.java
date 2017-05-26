package net.sf.genomeview.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;

public class OpenURLButton extends JButton {

	
	private static final long serialVersionUID = -1666800247496691936L;

	public OpenURLButton(final Model gvModel) {
		super(MessageManager.getString("opendialog.url"), Icons.get("Globe_48x48.png"));
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					String input = JOptionPane.showInputDialog(gvModel.getGUIManager().getParent(), "Give the URL of the data");
					if (input != null && input.trim().length() > 0) {

						DataSourceHelper.load(gvModel, new Locator(input.trim()));
					}

				} catch (MalformedURLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (URISyntaxException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				} catch (ReadFailedException e4) {
					// TODO Auto-generated catch block
					e4.printStackTrace();
				}

			}
		});
	}
}
