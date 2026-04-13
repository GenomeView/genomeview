package net.sf.genomeview.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import tudelft.utilities.logging.ReportToFile;
import tudelft.utilities.logging.Reporter;

/**
 * Logs to multiple destinations. Always logs to {@value #GENOMEVIEW_LOGFILE} in
 * case the GUI crashes
 */
public class DistributingReporter implements Reporter {

	private static final File GENOMEVIEW_LOGFILE = new File("genomeview.log");
	private final List<Reporter> children = new ArrayList<>();

	/**
	 * @throws IOException if we can not create a basic log file
	 */
	public DistributingReporter() throws IOException {
		children.add(new ReportToFile(GENOMEVIEW_LOGFILE));
	}

	@Override
	public void log(Level level, String msg, Throwable thrown) {
		for (Reporter child : children) {
			child.log(level, msg, thrown);
		}
	}

	@Override
	public void log(Level level, String msg) {
		log(level, msg, null);
	}

}
