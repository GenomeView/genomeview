/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;


public class GenomeView {
    private static Logger logger;

    public static void main(String[] args) {
        try {
            LogManager.getLogManager().readConfiguration(GenomeView.class.getResourceAsStream("/conf/logging.conf"));
            logger = Logger.getLogger(GenomeView.class.getCanonicalName());
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        createNewInstance(args);

    }

    private static Set<MainWindow> running = new HashSet<MainWindow>();

    /*
     * This will create AND SHOW the Splash screen.
     */
    private static Splash splash = new Splash();

    /**
     * Create a new instance with no command line parameters.
     */
    public static void createNewInstance() {
        createNewInstance(new String[0]);
    }

    public static void createNewInstance(String args[]) {

        logger.info("Creating new instance");
        try {
            MainWindow mw = new MainWindow(args);
            mw.addInstanceObserver(new Monitor(mw));
            if (!running.add(mw)) {
                JOptionPane
                        .showMessageDialog(
                                null,
                                "Duplicate program instances detected, save your work and quit all instances. If this problem persists, contact us.",
                                "Duplicate instances!!!", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("Couldn't create new instance.");
            logger.warning("MainWindow message: " + e.getMessage());
            // check for other instances or close
            if (running.size() == 0) {
                logger.info("Closed all models, exiting");
                System.exit(0);
            }
        }
        splash.setVisible(false);
    }

    public static void kill(MainWindow ID) {
        logger.info("Removing " + ID + " from instance manager.");
        running.remove(ID);
        if (running.size() == 0) {
            logger.info("Closed all models, exiting");
            try {
                Configuration.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

    }

    public static boolean isInstancesRunning() {
        return running != null && running.size() > 0;
    }

}

class Monitor implements Observer {
    private MainWindow id;

    Monitor(MainWindow id) {
        this.id = id;
    }

    @Override
    public void update(Observable o, Object arg) {
        Model model = (Model) o;
        if (model.isExitRequested()) {
            GenomeView.kill(id);
        }
    }
}