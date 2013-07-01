package net.sf.genomeview.gui.viztracks.annotation;

import java.util.List;

import net.sf.genomeview.core.Configuration;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class FeatureUtils {
	public static String displayName(Feature f) {

		List<String> identifiers = Configuration.getStringList("track:feature:labelIdentifiers");
		String dpName = null;
		int idx = 0;
		while (idx < identifiers.size() && dpName == null) {
			dpName = f.qualifier(identifiers.get(idx++));

		}

		if (dpName == null) {
			if (f.type() != null)
				dpName = f.type().toString() + " [" + new Location(f.start(), f.end()) + "]";
			else
				dpName = "[" + new Location(f.start(), f.end()).toString() + "]";
		}

		return dpName;
	}
}