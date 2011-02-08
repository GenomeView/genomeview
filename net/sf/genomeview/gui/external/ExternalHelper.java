/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.jannot.Location;

/**
 * Utility methods for external input
 * 
 * Either CLI, JNLP arguments, javascript input
 * 
 * @author Thomas Abeel
 * 
 */
public class ExternalHelper {

	public static void setPosition(String position, Model model) {
		try {
			String[] arr = position.split(":");
			assert arr.length == 2 || arr.length == 3;
			if (arr.length == 3) {
				model.setSelectedEntry(model.entry(arr[0]));
				model.setAnnotationLocationVisible(new Location(Integer.parseInt(arr[1]), Integer.parseInt(arr[2])));
			} else if (arr.length == 2) {
				model.setAnnotationLocationVisible(new Location(Integer.parseInt(arr[0]), Integer.parseInt(arr[1])));
			}
		} catch (NumberFormatException ne) {
			CrashHandler.showErrorMessage("Could not parse location: " + position, ne);
		}

	}

}
