/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import java.util.logging.Logger;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.samtools.util.StringUtil;

/**
 * Utility methods for external input
 * 
 * Either CLI, JNLP arguments, javascript input
 * 
 * @author Thomas Abeel
 * 
 */
public class ExternalHelper {

	
	private static Logger log=Logger.getLogger(ExternalHelper.class.getCanonicalName());
	public static void setPosition(final String position, final Model model) {

		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					boolean success = false;
					while (!success) {
						String[] tmp = StringUtil.reverseString(position).split("[:-]",3);
						String[] arr=new String[Math.min(tmp.length, 3)];
						for(int i=0;i<arr.length;i++)
							arr[i]=StringUtil.reverseString(tmp[(arr.length-1)-i]);
						/* If the location is not 2 or 3 tokens long, just stop */
						if (arr.length > 3 || arr.length < 2) {
							CrashHandler.showErrorMessage("Could not parse location: " + position,
									new NumberFormatException("Unknown format"));
							return;

						}
						if (hasEntry(arr)) {
							if (inRange(arr)) {
								if (arr.length == 3) {
									model.setSelectedEntry(model.entries().getEntry(arr[0]));
									model.vlm.setAnnotationLocationVisible(new Location(Integer.parseInt(arr[1]), Integer
											.parseInt(arr[2])));

								} else if (arr.length == 2) {
									model.vlm.setAnnotationLocationVisible(new Location(Integer.parseInt(arr[0]), Integer
											.parseInt(arr[1])));
								}
								success = true;

							}
						}
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							//Nothing to do in this case
						}
						if(!success){
							log.info("Failed to move to location: "+position+". This instruction has been requeued and will be retried.");
						}
					}
				} catch (NumberFormatException ne) {
					CrashHandler.showErrorMessage("Could not parse location: " + position, ne);
				}
			}

			private boolean hasEntry(String[] arr) {
				if (model.entries().size() == 0)
					return false;

				if (arr.length == 2)
					return true;

				return (arr.length == 3 && model.entries().getEntry(arr[0]) != null);

			}

			private boolean inRange(String[] arr) {
				int max = Integer.parseInt(arr[arr.length - 1]);
				Entry e = null;
				if (arr.length == 2)
					e = model.vlm.getSelectedEntry();//model.entries().getEntry();
				else
					e = model.entries().getEntry(arr[0]);
				return max <= e.getMaximumLength();

			}
		});
		t.start();
	}
}
