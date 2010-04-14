/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
import net.sf.jannot.parser.EMBLParser;
import net.sf.jannot.parser.Parser;
import be.abeel.io.ExtensionManager;

/**
 * Action to save the contents of a model to an url or a file.
 * 
 * @author thpar
 * @author Thomas Abeel
 * 
 */
public class SaveAsAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -1318894389028565654L;

    private Model model;

    public SaveAsAction(Model model) {
        super("Save as...");
        this.model = model;
    }

    public void actionPerformed(ActionEvent arg0) {
        // TODO currently saves everything in the data model to an EMBL file.

//        JFileChooser f = new JFileChooser(Configuration.getFile("lastDirectory"));
//        f.showSaveDialog(model.getParent());
//        File outputFile = f.getSelectedFile();
//        if (outputFile != null) {
//            Configuration.set("lastDirectory", outputFile.getParentFile());
//            Parser p = new EMBLParser();
//            try {
//                FileOutputStream fos = new FileOutputStream(ExtensionManager.extension(outputFile, "embl"));
//                for (Entry e : model.entries()) {
//                    p.write(fos, e, null);
//                }
//                fos.close();
//            } catch (IOException e) {
//
//                e.printStackTrace();
//
//            }
//        }

    }
}