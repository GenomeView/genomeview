/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import javax.swing.JComboBox;

import net.sf.jannot.Strand;

/**
 * Extension of a combobox to select a strand
 * 
 * @author Thomas Abeel
 * 
 */
public class StrandCombo extends JComboBox {

    private static final long serialVersionUID = 629278192407282224L;

    public StrandCombo() {
        for (Strand s : Strand.values())
            addItem(s);
    }

    public Strand getStrand() {
        return (Strand)this.getSelectedItem();
    }

}
