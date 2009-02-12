/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Splash extends JFrame {

    private static final long serialVersionUID = 2469220030126994849L;

    public Splash() {
        super();
        getContentPane().add(new JLabel(new ImageIcon(this.getClass().getResource("/images/splash.png"))));
        // setAlwaysOnTop(true);
        setUndecorated(true);
        setVisible(true);
        pack();
        Dimension ss = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(ss.width / 2 - getPreferredSize().width / 2, ss.height / 2 - getPreferredSize().height / 2);

    }
}
