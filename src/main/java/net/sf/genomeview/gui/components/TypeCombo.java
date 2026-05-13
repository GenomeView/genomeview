/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Type;

/**
 * Combobox with which use can select a {@link Type} from all known
 * {@link Type}s.
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class TypeCombo extends JComboBox {

	public TypeCombo(Model model) {
		this(model, true);

	}

	public TypeCombo(Model model, boolean editable) {
		super(new TypeModel(model));
		this.setEditable(editable);
		setMinimumSize(new Dimension(25, this.getPreferredSize().height));
	}

	public Type getSelectedType() {
		return (Type) this.getSelectedItem();
	}

}

/**
 * A model for a combobox containing all known {@link Type}s
 */
@SuppressWarnings("serial")
class TypeModel extends DefaultComboBoxModel implements Observer {

	private final Model model;

	public TypeModel(Model model) {
		model.addObserver(this);
		this.model = model;
	}

	@Override
	public Object getElementAt(int index) {

		return Type.values()[index];
	}

	@Override
	public int getSize() {
		return Type.values().length;
	}

	@Override
	public void update(Observable o, Object arg) {
		fireContentsChanged(o, 0, Type.values().length);
	}

}
