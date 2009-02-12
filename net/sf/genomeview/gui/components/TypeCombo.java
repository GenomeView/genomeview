/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import javax.swing.JComboBox;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Type;

/**
 * Combobox with all possible type terms.
 * 
 * @author Thomas Abeel
 * 
 */
public class TypeCombo extends JComboBox {

    private static final long serialVersionUID = 3311298470708351886L;

    public TypeCombo(Model model) {
        for (Type ct : Type.values()) {
            this.addItem(ct);
        }
    }

    public Type getTerm() {
        return (Type) this.getSelectedItem();
    }

}
