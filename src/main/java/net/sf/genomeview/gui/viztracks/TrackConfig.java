/**
 * This file is part of GenomeView, a genome browser and annotation curator
 * 
 * Copyright (C) 2012 Thomas Abeel
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Project: http://genomeview.org/
 */
package net.sf.genomeview.gui.viztracks;

import java.util.Observable;

import javax.swing.JLabel;

import be.abeel.gui.GridBagPanel;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.config.BooleanConfig;
import net.sf.genomeview.gui.config.StringConfig;
import net.sf.jannot.DataKey;

/**
 * Contains model with track options like visibility, collapsed, color settings,
 * thresholds. Usually a track will have track specific extensions, this is the
 * default.
 * 
 * @author Thomas Abeel
 * 
 */
public class TrackConfig extends Observable {

	protected final DataKey dataKey;
	protected final Model model;

	private final GridBagPanel guicontainer = new GridBagPanel();
	private boolean configVisible = false;

	/**
	 * 
	 * @param model   the {@link Model}
	 * @param dataKey the datakey of the data containing the data for this
	 *                model.
	 */
	protected TrackConfig(Model model, DataKey dataKey) {
		this.dataKey = dataKey;
		this.model = model;
		guicontainer.add(
				new JLabel(MessageManager.getString("trackconfig.track_key")
						+ " \n" + dataKey),
				guicontainer.gc);
		guicontainer.gc.gridy++;

		guicontainer.add(new StringConfig("track:alias:" + dataKey,
				MessageManager.getString("trackconfig.track_alias"), model),
				guicontainer.gc);
		guicontainer.gc.gridy++;
		Configuration.getVisible(dataKey);
		guicontainer.add(new BooleanConfig("track:visible:" + dataKey,
				MessageManager.getString("trackconfig.track_visible"), model),
				guicontainer.gc);
		guicontainer.gc.gridy++;
		guicontainer.add(new BooleanConfig("track:highlight:" + dataKey,
				MessageManager.getString("trackconfig.track_highlight"), model),
				guicontainer.gc);

		if (isCollapsible()) {
			guicontainer.gc.gridy++;
			guicontainer.add(
					new BooleanConfig("track:collapsed:" + dataKey,
							MessageManager.getString(
									"trackconfig.track_collapsed"),
							model),
					guicontainer.gc);
		}

	}

	final public String shortDisplayName() {
		String alias = Configuration.get("track:alias:" + dataKey);
		if (alias != null && alias.length() > 0)
			return alias;
		else {
			String dn = "" + dataKey;
			int sepIdx = Math.max(dn.lastIndexOf('/'), dn.lastIndexOf('\\'))
					+ 1;
			if (sepIdx < 0)
				sepIdx = 0;
			dn = dn.substring(sepIdx);
			dn = dn.replaceAll("(\\.[a-zA-Z0-9]{3})+$", "");
			return dn;
		}

	}

	final public String displayName() {
		String alias = Configuration.get("track:alias:" + dataKey);
		if (alias != null && alias.length() > 0)
			return alias;
		else
			return "" + dataKey;

	}

	/**
	 * When overriding this method, make sure to add new components to the
	 * parent panel
	 * 
	 * @return
	 */
	protected GridBagPanel getGUIContainer() {
		return guicontainer;

	}

	public boolean isVisible() {
		return Configuration.getVisible(dataKey);
	}

	public void setVisible(boolean visible) {
		Configuration.setVisible(dataKey, visible);
		setChanged();
		notifyObservers("TrackConfig::setVisible");
	}

	public void setCollapsed(boolean collapsed) {
		Configuration.set("track:collapsed:" + dataKey, collapsed);
		setChanged();
		notifyObservers("TrackConfig::setCollapsed");
	}

	public boolean isCollapsible() {
		return false;
	}

	public boolean isCollapsed() {
		return Configuration.getBoolean("track:collapsed:" + dataKey);
	}

	public void setConfigVisible(boolean b) {
		configVisible = b;
		setChanged();
		notifyObservers("TrackConfig::setConfigVisible");

	}

	public boolean isConfigVisible() {
		return configVisible;

	}

}
