/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.source.MobySource;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;
import net.sf.jannot.source.URLSource;
import net.sf.jannot.source.DataSource.Sources;

public class DataSourceFactory {
    public static DataSource[] create(Sources source, Model model) {
        switch (source) {
        case URL:
            try {
                URL url = new URI(JOptionPane.showInputDialog(model.getParent(), "Give the URL of the data").trim())
                        .toURL();
                return new DataSource[] { new URLSource(url) };
            } catch (Exception e) {
                e.printStackTrace();
            }
        case LOCALFILE:
            try {
                JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
                chooser.setMultiSelectionEnabled(true);
                int returnVal = chooser.showOpenDialog(model.getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles();
                    DataSource[] out = new DataSource[files.length];
                    for (int i = 0; i < files.length; i++)
                        out[i] = new FileSource(files[i]);
                    Configuration.set("lastDirectory", files[0].getParentFile());
                    return out;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // return new LocalFileSource(model);
        case BIOMOBY:
            try {
                URL mobyurl = new URI(JOptionPane.showInputDialog(model.getParent(), "Give the URL of the data").trim())
                        .toURL();
                return new DataSource[] { new MobySource(mobyurl) };
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
