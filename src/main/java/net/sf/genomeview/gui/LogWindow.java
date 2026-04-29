package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.sf.jannot.DistributingReporter;
import tudelft.utilities.logging.Reporter;

@SuppressWarnings({ "serial" })
public class LogWindow extends JFrame implements Reporter {

	private final LogTableModel logs = new LogTableModel(40);
	// special log message that makes the LogWindow pop to front
	// this can be used to handle user requests to see LogWindow
	public static final String MAKE_LOG_VISIBLE_REQUEST = "show log window";

	/**
	 * 
	 * @param parentlogger the {@link DistributingReporter} to subscribe with
	 */
	public LogWindow(DistributingReporter parentlogger) {
		setLayout(new BorderLayout());
		parentlogger.add(this);
		parentlogger.log(Level.INFO, "log window opened");
		pack();

		// set panel content
		JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JTable table = new JTable(logs);
		table.setDefaultRenderer(Level.class, new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row,
					int column) {
				Level level = (Level) value;
				JLabel label = new JLabel(level.toString());
				label.setOpaque(true);
				if (level == Level.WARNING) {
					label.setBackground(Color.yellow);
				} else if (level == Level.SEVERE) {
					label.setBackground(Color.red);
				}
				return label;
			}
		});
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
				// message also to text panel so long messages can be read
				stacktrace
						.setText(row >= 0
								? logs.getMessage(row) + "\n"
										+ ExceptionUtils.getStackTrace(
												logs.getStacktrace(row))
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
				|| msg.equals(MAKE_LOG_VISIBLE_REQUEST)) {
			showOnTop();
		}
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

	/**
	 * 
	 * @param row the row to get message from
	 * @return message currently at row.
	 */
	public synchronized String getMessage(int row) {
		return logs.get(row).getMessage();
	}

	/**
	 * 
	 * @param row the row to get message from
	 * @return stacjtrace currently at row.
	 */
	public synchronized Throwable getStacktrace(int row) {
		return logs.get(row).getThrown();
	}

	/**
	 * This needs to handle multi-threading
	 * 
	 * @param record a new logrecord to add as first item
	 */
	public synchronized void add(LogRecord record) {
		// this particular order ensures list size never decreases
		// and helps thread 'safety'
		logs.add(0, record);
		while (logs.size() > maxsize) {
			logs.remove(logs.size() - 1); // remove last
		}
		notifyListeners();
	}

	private void notifyListeners() {
		// tableChanged is NOT THREAD SAFE.. Workaround
		SwingUtilities.invokeLater(() -> notifyListenersOnSwingThread());
	}

	/**
	 * ONLY CALL THIS FROM INSIDE SWING THREAD.
	 */
	private synchronized void notifyListenersOnSwingThread() {
		try {
			for (TableModelListener l : listeners) {
				// null just refreshes the entire panel.
				l.tableChanged(null);
			}
		} catch (Exception e) {
			// can't log problems inside a logger! What now?
			e.printStackTrace();
		}
	}

	@Override
	public synchronized int getRowCount() {
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
		return columnIndex == 0 ? Level.class : String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public synchronized Object getValueAt(int rowIndex, int columnIndex) {
		LogRecord record = logs.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return record.getLevel();
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
	public synchronized void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public synchronized void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

}
