/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class DatabaseConnectionDialog extends JDialog implements ActionListener, WindowListener{
	private static final long serialVersionUID = 5882425878045915464L;
	
	public final static int BUTTON_OK = 0;
	public final static int BUTTON_CANCEL = 1;
	
	private String host;
	private String user;
	private String password;
	
	private JTextField hostText = new JTextField();
	private JTextField userText = new JTextField();
	private JPasswordField passwordText = new JPasswordField();
	
	private JLabel hostLabel = new JLabel("Hostname: ");
	private JLabel userLabel = new JLabel("User: ");
	private JLabel passwordLabel = new JLabel("Password: ");
	
	private JButton okButton = new JButton("Ok");
	private JButton cancelButton = new JButton("Cancel");


	private int exitCode;
	
	
	public DatabaseConnectionDialog(String host, String user, String password){
		this.setModal(true);
		this.setLayout(new BorderLayout());
		this.setTitle("BioSQL Database Connection");
		this.setResizable(false);
		//this.setLocation(300,300);
		this.setLocationRelativeTo(null);
		this.addWindowListener(this);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//set default values
		hostText.setText(host);
		userText.setText(user);
		passwordText.setText(password);
		
		
		Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		
		//form
		JPanel inputPanel =  new JPanel();
		inputPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.WEST;
		
		inputPanel.add(hostLabel, gbc);
		gbc.gridy++;
		inputPanel.add(userLabel, gbc);
		gbc.gridy++;
		inputPanel.add(passwordLabel, gbc);
		
		
		gbc.weightx+=3;
		gbc.gridy=0;
		gbc.gridx++;
		inputPanel.add(hostText, gbc);
		gbc.gridy++;
		inputPanel.add(userText, gbc);
		gbc.gridy++;
		inputPanel.add(passwordText, gbc);
		inputPanel.setPreferredSize(new Dimension(300,100));
		inputPanel.setBorder(border);
		
		//button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createGlue());
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		buttonPanel.add(cancelButton);
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		buttonPanel.setBorder(border);
		
		
		add(inputPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		this.getRootPane().setDefaultButton(okButton);
		this.pack();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("ok")){
			exitCode = DatabaseConnectionDialog.BUTTON_OK;
			this.host = this.hostText.getText();
			this.user = this.userText.getText();
			this.password = new String(this.passwordText.getPassword());
			this.dispose();
		} else if (action.equals("cancel")){
			exitCode = DatabaseConnectionDialog.BUTTON_CANCEL;
			this.dispose();
		}
	}
	
	
	public int showDatabaseConnectionDialog(){
		this.setVisible(true);
		return exitCode;
	}


	public String getHost() {
		return host;
	}


	public String getUser() {
		return user;
	}


	public String getPassword() {
		return password;
	}


	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowClosed(WindowEvent e) {
	}


	@Override
	public void windowClosing(WindowEvent e) {
		this.exitCode = DatabaseConnectionDialog.BUTTON_CANCEL;		
	}


	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
