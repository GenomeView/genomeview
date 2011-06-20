/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.genomeview.core.LRUSet;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.ReadWorker;
import net.sf.genomeview.data.Session;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.Locator;
import be.abeel.io.LineIterator;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class InstructionWorker implements Runnable {

	class Port{
		private int port;

		
		public String toString(){
			return ""+port;
		}
		public Port(int port) {
			this.port=port;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + port;
			return result;
		}

		/* (non-Javadoc)
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

	private static ArrayList<Port> otherPorts = new ArrayList<Port>();

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
		LineIterator it = new LineIterator(s.getInputStream());
		String line = it.next();
		if (line.startsWith("GenomeViewJavaScriptHandler-")) {
			otherPorts.add(new Port(Integer.parseInt(line.split("-")[1])));
		} else {
			System.out.println(line);

			while (!line.startsWith("GET")) {
				// System.out.println("Handler: GET: " + line);
				line = it.next();
				// System.out.println(line);

			}
			writeOther(line);

			if (line.startsWith("GET /genomeview-" + id + "/") || line.startsWith("GET /genomeview-ALL/")) {
				String[] id = line.split("\\$\\$");
				if (id.length == 1 || !lastID.contains(id[1])) {
					if(id.length>1)
						lastID.add(id[1]);

					line = id[0];
					String[] arr = line.split(" ")[1].split("/", 4);
					if (arr[1].startsWith("genomeview")) {
						if (arr[2].toLowerCase().equals("position")) {
							doPosition(arr[3]);
						}
						if (arr[2].toLowerCase().equals("load")) {
							if (!arr[3].equals(lastLoad)) {
								lastLoad = arr[3];
								doLoad(arr[3]);
							}

						}
						if (arr[2].toLowerCase().equals("session")) {
							doSession(arr[3]);

						}
						if (arr[2].toLowerCase().equals("unload")) {
							model.clearEntries();
							lastLoad = null;
						}
					} else {
						log.log(Level.WARNING, "This instruction doesn't belong to GenomeView, I'll ignore it.");
					}
				}

			}

		}
		s.close();

	}

	private void writeOther(String line) {
		for (Port port : otherPorts) {
			try {
				Socket clientSocket = new Socket(InetAddress.getLocalHost(), port.getPort());
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
				out.println(line);
				out.close();
				clientSocket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Removing port: "+port);
				otherPorts.remove(port);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Removing port: "+port);
				otherPorts.remove(port);
			}
		}

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

	private void doPosition(String string) {
		ExternalHelper.setPosition(string, model);

	}

	private void doLoad(String s) {
		try {
			DataSourceHelper.load(model,new Locator(s));
			
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