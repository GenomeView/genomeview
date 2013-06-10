/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.LRUSet;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class InstructionWorker implements Runnable {

	class Port {
		private int port;

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
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Port other = (Port) obj;
			if (port != other.port)
				return false;
			return true;
		}

		public int getPort() {
			return port;
		}

	}

	/* Socket to client we're handling */
	private Socket s;

	private Model model;

	private String id;

	private static Logger log = Logger.getLogger(InstructionWorker.class.getCanonicalName());

	private static HashSet<Port> otherPorts = new HashSet<Port>();

	InstructionWorker(Model model, String id, Socket s) {
		this.model = model;
		this.id = id;
		this.s = s;
	}

	public void run() {

		System.out.println("Running worker");
		try {
			handleClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String lastLoad = null;

	private static LRUSet<String> lastID = new LRUSet<String>(20);

	void handleClient() throws IOException {
		/* This happens when exiting */
		if (s == null)
			return;
		s.setSoTimeout(5000);
		s.setTcpNoDelay(true);
		System.out.println("Handling client");
		// InputStream is = new BufferedInputStream(s.getInputStream());
		BufferedReader it = new BufferedReader(new InputStreamReader(s.getInputStream()));
		// LineIterator it = new LineIterator(s.getInputStream());
		String line = it.readLine();
		if (line.startsWith("GenomeViewJavaScriptHandler-")) {
			otherPorts.add(new Port(Integer.parseInt(line.split("-")[1])));
		} else {
			while (!line.startsWith("GET") && line != null) {
				// System.out.println("Handler: GET: " + line);
				line = it.readLine();
				// System.out.println(line);

			}
			StringBuffer others = writeOther(line);
			System.out.println("Reply from others: " + others);
			if (others.length() > 0) {
				PrintWriter pw = new PrintWriter(s.getOutputStream());
				pw.print(others.toString());
				pw.close();
			}
			if (line.startsWith("GET /genomeview-" + id + "/") || line.startsWith("GET /genomeview-ALL/")) {
				String[] id = line.split("\\$\\$");
				if (id.length == 1 || !lastID.contains(id[1])) {
					if (id.length > 1)
						lastID.add(id[1]);

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
						}else if (arr[2].toLowerCase().equals("session")) {
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
							log.log(Level.WARNING, "Instruction " + line + " was not understood by GenomeView");

						}
					} else {
						log.log(Level.WARNING, "This instruction doesn't belong to GenomeView, I'll ignore it.");
					}
				}

			}

		}
		s.close();

	}

	/**
	 * Scroll to track
	 * 
	 * 
	 * 
	 * @param trackName
	 */
	private void doTrack(String trackName) {
		String input = trackName.toLowerCase();
		ArrayList<Track> hits = new ArrayList<Track>();
		for (Track t : model.getTrackList()) {
			if (t.getDataKey().toString().toLowerCase().contains(input) || t.config().displayName().toLowerCase().contains(input))
				hits.add(t);

		}
		if (hits.size() > 0)
			model.getGUIManager().getEvidenceLabel().scroll2track(hits.get(0));
	}

	private StringBuffer writeOther(String line) {
		StringBuffer buffer = new StringBuffer();
		for (Port port : otherPorts) {
			try {
				Socket clientSocket = new Socket(InetAddress.getLocalHost(), port.getPort());
				clientSocket.setTcpNoDelay(true);

				BufferedReader bis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
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
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Removing port: " + port);
				otherPorts.remove(port);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Removing port: " + port);
				otherPorts.remove(port);
			}
		}
		return buffer;

	}

	private void doSession(String string) {
		try {
			Session.loadSession(model, URIFactory.url(string));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void doConfig(String string){
		String[] arr=string.trim().split("=",2);
		Configuration.set(arr[0], arr[1]);
	}
	private void doPosition(String string) {
		ExternalHelper.setPosition(string, model);

	}

	private void doLoad(String s) {
		try {
			DataSourceHelper.load(model, new Locator(s));

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}