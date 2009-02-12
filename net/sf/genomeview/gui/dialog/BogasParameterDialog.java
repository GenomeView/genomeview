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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

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
/**
 * 
 * @deprecated Bogas parameters are passed on command line in all use cases
 * @author thpar
 *
 */
@Deprecated
public class BogasParameterDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	public final static int BUTTON_OK = 0;
	public final static int BUTTON_CANCEL = 1;
	
	private JButton okButton = new JButton("Ok");
	private JButton cancelButton = new JButton("Cancel");
	
	private int exitCode;
	
	
	private String locusId;
	private String genome;
	private int release;
	private GregorianCalendar date;
	private String context;
	
	private String login;
	private String pass;
	
	private JLabel locusLabel = new JLabel("locus id: ");
	private JLabel genomeLabel = new JLabel("genome: ");
	private JLabel releaseLabel = new JLabel("release: ");
	private JLabel dateLabel = new JLabel("date: ");
	private JLabel contextLabel = new JLabel("context: ");
	
	private JLabel loginLabel = new JLabel("login: ");	
	private JLabel passLabel = new JLabel("pass: ");
	
	private JTextField locusText = new JTextField();
	private JTextField genomeText = new JTextField();
	private JTextField releaseText = new JTextField();
	private JTextField dateText = new JTextField();
	private JTextField contextText = new JTextField();
	
	private JTextField loginText = new JTextField();
	private JTextField passText = new JPasswordField();
	
	public BogasParameterDialog() {
		setModal(true);
		setLayout(new BorderLayout());
		setTitle("BOGAS parameters");
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
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
		
		
		inputPanel.add(loginLabel, gbc);
		gbc.gridy++;
		inputPanel.add(passLabel, gbc);
		gbc.gridy++;
		inputPanel.add(locusLabel, gbc);
		gbc.gridy++;
		inputPanel.add(genomeLabel, gbc);
		gbc.gridy++;
		inputPanel.add(releaseLabel, gbc);
		gbc.gridy++;
		inputPanel.add(dateLabel, gbc);
		gbc.gridy++;
		inputPanel.add(contextLabel, gbc);
		
		
		gbc.weightx+=3;
		gbc.gridy=0;
		gbc.gridx++;
		inputPanel.add(loginText, gbc);
		gbc.gridy++;
		inputPanel.add(passText, gbc);
		gbc.gridy++;
		inputPanel.add(locusText, gbc);
		gbc.gridy++;
		inputPanel.add(genomeText, gbc);
		gbc.gridy++;
		inputPanel.add(releaseText, gbc);
		gbc.gridy++;
		inputPanel.add(dateText, gbc);
		gbc.gridy++;
		inputPanel.add(contextText, gbc);
		
		inputPanel.setPreferredSize(new Dimension(300,180));
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
		getRootPane().setDefaultButton(okButton);
		pack();
	}




	public int showBogasParameterDialog() {
		setVisible(true);
		return exitCode;
	}




	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("ok")){
			exitCode = DatabaseConnectionDialog.BUTTON_OK;
			locusId = locusText.getText();
			genome = genomeText.getText();
			release = Integer.parseInt(releaseText.getText());
			
			String dateString = this.dateText.getText();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		
			date = new GregorianCalendar();
			try {
				date.setTime(df.parse(dateString));
			} catch (ParseException pe) {		
				pe.printStackTrace();
				System.err.println("Could not parse date. Using current time.");
				date.setTime(new Date());
			}
			
			
			
			context = this.contextText.getText();
			
			login = this.loginText.getText();
			pass = this.passText.getText();
			
			this.dispose();
		} else if (action.equals("cancel")){
			exitCode = DatabaseConnectionDialog.BUTTON_CANCEL;
			this.dispose();
		}
	}

	public String getLocusId() {
		return locusId;
	}

	public String getGenome() {
		return genome;
	}

	public int getRelease() {
		return release;
	}

	public GregorianCalendar getDate() {
		return date;
	}

	public String getLogin() {
		return login;
	}

	public String getPass() {
		return pass;
	}

	public String getContext() {
		return context;
	}
	

	
	
	
}
