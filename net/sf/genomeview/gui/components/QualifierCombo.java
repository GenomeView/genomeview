/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import javax.swing.JComboBox;

import net.sf.genomeview.core.Configuration;


public class QualifierCombo extends JComboBox {



    private static final long serialVersionUID = 3311298470708351886L;

    public QualifierCombo() {
        for (String ct : Configuration.getStringSet("emblQualifiers")) {
            this.addItem(ct);
        }
    }

    public String getQualifierKey() {
        return (String) this.getSelectedItem();
    }

}
