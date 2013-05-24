/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.Component;
import java.awt.Container;

import net.sf.genomeview.data.Model;

public abstract class ConfigBox {

	public static Container booleanInstance(Model model,String key, String title) {
		return new BooleanConfig(key, title,model);

	}

	public static Container dropDownInstance(String listKey, String valueKey,String title){
		return new ComboBoxConfig(listKey,valueKey,title);
	}
	
	public static Container stringInstance(String key, String title) {
		return new StringConfig(key, title);

	}
	
	public static Container integerInstance(String key, String title) {
		return new IntegerConfig(key, title);

	}
	
	public static Container colorInstance(Model model,String key, String title) {
		return new ColorConfig(model,key, title);

	}

	public static Component doubleInstance(String key, String title) {
		return new DoubleConfig(key, title);
	}
	
}
