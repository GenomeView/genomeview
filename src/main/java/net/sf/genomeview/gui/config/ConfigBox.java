/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.Component;
import java.awt.Container;

import net.sf.genomeview.data.Model;

public abstract class ConfigBox {

	public static Container booleanInstance(Model model, String key,
			String title) {
		return new BooleanConfig(key, title, model);

	}

	public static Container dropDownInstance(Model model, String[] objects,
			String valueKey, String title) {

		return new ComboBoxConfig(objects, valueKey, title, model);
	}

	public static Container stringInstance(Model model, String key,
			String title) {
		return new StringConfig(key, title, model);

	}

	public static Container integerInstance(String key, String title,
			Model model) {
		return new IntegerConfig(key, title, model.getConfiguration());

	}

	public static Container colorInstance(Model model, String key,
			String title) {
		return new ColorConfig(model, key, title);

	}

	public static Component doubleInstance(String key, String title,
			Model model) {
		return new DoubleConfig(key, title, model.getConfiguration());
	}

}
