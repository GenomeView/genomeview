/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import be.abeel.net.URIFactory;
import net.sf.genomeview.core.LRUSet;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;

/**
 * This is a service listening on a socket for incoming instructions. These
 * instructions are then executed on the model, effectively controlling
 * GenomeView remotely through a socket.
 * 
 * @author Thomas Abeel
 * 
 */
class InstructionWorker implements Runnable {
	// special message header indicating another genomeview port
	private static final String OTHER_GENOMEVIEW = "GenomeViewJavaScriptHandler-";
	private static final HashSet<Port> otherPorts = new HashSet<Port>();
	private static String lastLoad = null;

	/* Socket to client we're handling */
	private final Socket s;

	private final Model model;

	private final String id;

	private static LRUSet<String> lastID = new LRUSet<String>(20);

	/**
	 * this handles one requests for remotely controlling GenomeView. The
	 * request comes in as a HTTP GET on {@link #s}s
	 * 
	 * @param model the model to control
	 * @param id    our ID. Only incoming requests containing this ID are
	 *              handled by us.
	 * @param s     a socket on which an instruction came in
	 */
	InstructionWorker(Model model, String id, Socket s) {
		if (model == null) {
			throw new NullPointerException("model must be non-null");
		}
		this.model = model;
		this.id = id;
		this.s = s;
	}

	@Override
	public void run() {

		model.getLog().log(Level.INFO, "Running worker");
		try {
			handleClient();
		} catch (Exception e) {
			model.getLog().log(Level.SEVERE,
					"failed to handle instruction " + id, e);
		}
	}

	/**
	 * Reads the input on the socket and handles it. if the line starts with
	 * {@link #OTHER_GENOMEVIEW} we register the other genomeview port.
	 * 
	 * Otherwise, we read lines till a line is found starting with "GET". This
	 * line is first sent to all {@link #otherPorts} and if someone replies, the
	 * result is taken and considered done.
	 * 
	 * Otherwise, we handle the task ourself.
	 * 
	 * Requests are only responded to if someone handles it.
	 * 
	 * @throws IOException
	 */
	void handleClient() throws IOException {
		if (s == null) {
			return; // This happens when exiting
		}
		s.setSoTimeout(5000);
		s.setTcpNoDelay(true);
		BufferedReader it = new BufferedReader(
				new InputStreamReader(s.getInputStream()));
		String line = it.readLine();
		model.getLog().log(Level.INFO,
				"Handling socket service request: " + line);

		if (line.startsWith(OTHER_GENOMEVIEW)) {
			otherPorts.add(new Port(Integer.parseInt(line.split("-")[1])));
		} else {
			while (!line.startsWith("GET") && line != null) {
				// System.out.println("Handler: GET: " + line);
				line = it.readLine();
				// System.out.println(line);
			}
			StringBuffer others = writeOther(line);
			model.getLog().log(Level.INFO, "Reply from others: " + others);
			if (others.length() > 0) {
				PrintWriter pw = new PrintWriter(s.getOutputStream());
				pw.print(others.toString());
				pw.close();
			}
			weHandleRequest(line);

		}
		s.close();

	}

	/**
	 * We handle the request ourselves
	 * 
	 * @param line the request staring with "GET".
	 * @throws IOException
	 */
	private void weHandleRequest(String line) throws IOException {
		if (line.startsWith("GET /genomeview-" + id + "/")
				|| line.startsWith("GET /genomeview-ALL/")) {
			String[] id = line.split("\\$\\$");
			if (id.length == 1 || !lastID.contains(id[1])) {
				if (id.length > 1) {
					lastID.add(id[1]);
				}

				line = id[0];
				String[] arr = line.split(" ")[1].split("/", 4);
				if (arr[1].startsWith("genomeview")) {
					if (arr[2].toLowerCase().equals("position")) {
						doPosition(arr[3]);
					} else if (arr[2].toLowerCase().equals("load")) {
						if (!arr[3].equals(lastLoad)) {
							lastLoad = arr[3];
							doLoad(arr[3]);
						}

					} else if (arr[2].toLowerCase().equals("track")) {
						doTrack(arr[3]);
					} else if (arr[2].toLowerCase().equals("config")) {
						doConfig(arr[3]);
					} else if (arr[2].toLowerCase().equals("session")) {
						doSession(arr[3]);

					} else if (arr[2].toLowerCase().equals("unload")) {
						model.clearEntries();
						lastLoad = null;
					} else if (arr[2].toLowerCase().equals("heartbeat")) {
						PrintWriter pw = new PrintWriter(s.getOutputStream());
						pw.println("HTTP/1.1 200 OK");
						pw.println("Content-Type: text/plain");
						pw.println();
						pw.println("isGenomeViewAlive=true;");
						pw.flush();
						pw.close();

					} else {
						model.getLog().log(Level.WARNING, "Instruction " + line
								+ " was not understood by GenomeView");

					}
				} else {
					model.getLog().log(Level.WARNING,
							"This instruction doesn't belong to GenomeView, I'll ignore it.");
				}
			}

		}
	}

	/**
	 * Scroll to track
	 * 
	 * @param trackName
	 */
	private void doTrack(String trackName) {
		String input = trackName.toLowerCase();
		ArrayList<Track> hits = new ArrayList<Track>();
		for (Track t : model.getTrackList()) {
			if (t.getDataKey().toString().toLowerCase().contains(input)
					|| t.config().displayName().toLowerCase().contains(input)) {
				hits.add(t);
			}

		}
		if (hits.size() > 0) {
			model.getGUIManager().getEvidenceLabel().scroll2track(hits.get(0));
		}
	}

	/**
	 * There is a connection to {@link #otherPorts} that also run GenomeView. It
	 * is assumed we can communicate quickly with them. This method writes the
	 * line to each of them and joins all the replies
	 * 
	 * @param line the line to write to all others
	 * @return the collected responses,
	 */
	private StringBuffer writeOther(String line) {
		StringBuffer buffer = new StringBuffer();
		for (Port port : otherPorts) {
			try {
				Socket clientSocket = new Socket(InetAddress.getLocalHost(),
						port.getPort());
				clientSocket.setTcpNoDelay(true);

				BufferedReader bis = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(
						clientSocket.getOutputStream());
				out.println(line);
				out.flush();
				// out.close();

				String l = bis.readLine();
				while (l != null) {
					buffer.append(l + "\n");
					l = bis.readLine();
				}
				out.close();
				bis.close();
				clientSocket.close();
			} catch (IOException e) {
				model.getLog().log(Level.SEVERE, "Write failed to " + port, e);
				otherPorts.remove(port);
			}
		}
		return buffer;

	}

	private void doSession(String string) {
		try {
			Session.loadSession(model, URIFactory.url(string));
		} catch (IOException | URISyntaxException e) {
			// FIXME unreachable? load should not crash
			model.getLog().log(Level.SEVERE, "Failed to load session " + string,
					e);
		}

	}

	private void doConfig(String string) {
		String[] arr = string.trim().split("=", 2);
		model.getConfiguration().set(arr[0], arr[1]);
	}

	private void doPosition(String string) {
		model.setPosition(string);

	}

	private void doLoad(String s) {
		try {
			new DataSourceHelper(model).load(new Locator(s, model.getLog()));
		} catch (URISyntaxException | IOException | ReadFailedException e) {
			// FIXME unreachable? load should not crash
			model.getLog().log(Level.SEVERE, "Failed to load " + s, e);
		}

	}

}

class Port {
	private int port;

	@Override
	public String toString() {
		return "" + port;
	}

	public Port(int port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + port;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Port other = (Port) obj;
		if (port != other.port) {
			return false;
		}
		return true;
	}

	public int getPort() {
		return port;
	}

}
