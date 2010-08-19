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
 * Combobox with all possible type terms.
 * 
 * @author Thomas Abeel
 * 
 */
public class TypeCombo extends JComboBox {

	private static final long serialVersionUID = 3311298470708351886L;

	public TypeCombo(Model model) {
		this(model,true);
		

	}
	public TypeCombo(Model model, boolean editable) {
		super(new TypeModel(model));
		this.setEditable(editable);
		setMinimumSize(new Dimension(25,this.getPreferredSize().height));
		//setPreferredSize(new Dimension(25,this.getPreferredSize().height));
	}
	public Type getTerm() {
		return (Type) this.getSelectedItem();
	}

}

class TypeModel extends DefaultComboBoxModel implements Observer {

	private static final long serialVersionUID = -5021594934465844712L;

	private Model model;
	
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
