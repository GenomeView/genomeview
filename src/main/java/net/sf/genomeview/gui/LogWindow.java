package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.sf.genomeview.core.DistributingReporter;
import tudelft.utilities.logging.Reporter;

@SuppressWarnings({ "serial" })
public class LogWindow extends JFrame implements Reporter {

	private final LogTableModel logs = new LogTableModel(40);
	// special log message that makes the LogWindow pop to front
	// this can be used to handle user requests to see LogWindow
	public static final String MAKE_LOG_VISIBLE_REQUEST = "show log window";

	/**
	 * 
	 * @param log the {@link DistributingReporter} to subscribe with
	 */
	public LogWindow(DistributingReporter log) {
		setLayout(new BorderLayout());
		log.add(this);
		log.log(Level.INFO, "log window opened");
		pack();

		// set panel content
		JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JTable table = new JTable(logs);
		JTextArea stacktrace = new JTextArea(10, 80);
		// scrollpane around table needed to get headers
		splitpane.add(new JScrollPane(table), JSplitPane.TOP);
		splitpane.add(new JScrollPane(stacktrace), JSplitPane.BOTTOM);
		add(splitpane, BorderLayout.CENTER);
		splitpane.setDividerLocation(200);

		// connect table selection to stacktrace content
		ListSelectionListener selListener = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = table.getSelectedRow();
				stacktrace.setText(row >= 0
						? ExceptionUtils.getStackTrace(logs.getStacktrace(row))
						: "No stacktrace");
			}
		};
		table.getSelectionModel().addListSelectionListener(selListener);
	}

	@Override
	public void log(Level level, String msg, Throwable thrown) {
		LogRecord record = new LogRecord(level, msg);
		record.setThrown(thrown);
		logs.add(record);
		if (level.intValue() >= Level.WARNING.intValue()
				|| msg.equals(MAKE_LOG_VISIBLE_REQUEST))
			showOnTop();
	}

	/**
	 * show the window, force it to front.
	 */
	public void showOnTop() {
		setAlwaysOnTop(true);
		setVisible(true);
		setAlwaysOnTop(false);

	}

	@Override
	public void log(Level level, String msg) {
		log(level, msg, null);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(400, 400);
	}
}

/**
 * A tablemodel of the log records, ready for use by swing. Left column contains
 * the {@link Level}. Right column contains the message. Clicking on a record
 * should trigger viewing of the stacktrace, if any
 */
class LogTableModel implements TableModel {

	private final List<LogRecord> logs = new ArrayList<>();
	private final int maxsize;
	private final List<TableModelListener> listeners = new ArrayList<>();

	public LogTableModel(int maxsize) {
		this.maxsize = maxsize;
	}

	public Throwable getStacktrace(int row) {
		return logs.get(row).getThrown();
	}

	/**
	 * This needs to handle multi-threading
	 * 
	 * @param record a new logrecord to add as first item
	 */
	public synchronized void add(LogRecord record) {
		while (logs.size() > maxsize) {
			logs.remove(logs.size() - 1); // remove last
		}
		logs.add(0, record);
		notifyListeners();
	}

	private void notifyListeners() {
		for (TableModelListener l : listeners) {
			l.tableChanged(null); // null just refreshes the panel. Maybe
									// smarter way?
		}
	}

	@Override
	public int getRowCount() {
		return logs.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "level";
		case 1:
			return "message";
		default:
			return "??";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		LogRecord record = logs.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return record.getLevel().toString();
		case 1:
			return record.getMessage();
		}
		return "??";
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// you can't set values
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

}
