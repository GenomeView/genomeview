package net.sf.genomeview.gui.components;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.text.html.StyleSheet;

import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import tudelft.utilities.logging.ReportToLogger;
import tudelft.utilities.logging.Reporter;

public class JOptionPaneX {
	public final static Reporter log = new ReportToLogger(
			JOptionPaneX.class.getSimpleName());

	public static void main(String[] args) {
		JOptionPaneX.showOkCancelDialog(null, "test", "title",
				JOptionPane.WARNING_MESSAGE);
	}

	public static boolean showOkCancelDialog(Frame frame, String msg,
			String title, int type) {
		DD x = new DD(frame, msg, title, type);
		return x.getRet();

	}

}

class DD {
	private boolean retOK = false;

	public DD(Frame frame, String msg, String title, int type) {
		final JDialog dialog = new JDialog(frame, title, true);

		Container contentPane = dialog.getContentPane();

		JEditorPaneLabel text = new JEditorPaneLabel(JOptionPaneX.log);
		StyleSheet css = text.getStyleSheet();
		// css.addRule("body {color:#000; margin-left: 4px; margin-right: 4px;
		// }");
		// css.addRule("p {margin:0px;padding:0px;}");
		// css.addRule("h3 {font-size:115%;color: " +
		// Colors.encode(Configuration.green) +
		// ";margin:0px;padding:0px;}");
		text.setText("<html>" + msg + "</html>");

		contentPane.add(text, BorderLayout.CENTER);
		JButton ok = new JButton(MessageManager.getString("button.ok"));

		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				retOK = true;
				dialog.setVisible(false);

			}
		});

		JButton cancel = new JButton(MessageManager.getString("button.cancel"));
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);

			}
		});

		Container buttons = new Container();
		buttons.setLayout(new BorderLayout());
		buttons.add(ok, BorderLayout.WEST);
		buttons.add(cancel, BorderLayout.EAST);

		contentPane.add(buttons, BorderLayout.SOUTH);

		dialog.pack();
		StaticUtils.center(frame, dialog);
		dialog.setVisible(true);
		dialog.dispose();
	}

	public boolean getRet() {
		return retOK;

	}

}