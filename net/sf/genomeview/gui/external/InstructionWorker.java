/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.ReadWorker;
import net.sf.genomeview.data.Session;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.utils.URIFactory;
import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class InstructionWorker implements Runnable {

	/* Socket to client we're handling */
	private Socket s;

	private Model model;

	private String id;

	private static Logger log=Logger.getLogger(InstructionWorker.class.getCanonicalName());
	
	InstructionWorker(Model model, String id) {
		this.model=model;
		this.id=id;
		s = null;
	}

	synchronized void setSocket(Socket s) {
		this.s = s;
		notify();
	}

	public synchronized void run() {
		System.out.println("Running worker");
		while (true) {
			if (s == null) {
				/* nothing to do */
				try {
					wait();
				} catch (InterruptedException e) {
					/* should not happen */
					continue;
				}
			}
			try {
				handleClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*
			 * go back in wait queue if there's fewer than numHandler
			 * connections.
			 */
			s = null;
			ArrayList<InstructionWorker> pool = JavaScriptHandler.threads;
			synchronized (pool) {
				if (pool.size() >= 1) {
					/* too many threads, exit this one */
					return;
				} else {
					pool.add(this);
				}
			}
		}
	}

	void handleClient() throws IOException {
		s.setSoTimeout(5000);
		s.setTcpNoDelay(true);
		System.out.println("Handling client");
		// InputStream is = new BufferedInputStream(s.getInputStream());
		LineIterator it = new LineIterator(s.getInputStream());
		String line = it.next();
		System.out.println(line);
		while (!line.startsWith("GET")){
			System.out.println("Handler: GET: "+line);
			line = it.next();
			System.out.println(line);
			
		}
		if(line.startsWith("GET /genomeview-"+id+"/")){
			System.out.println("Good to go...");
			String[]arr=line.split(" ")[1].split("/",4);
			if(arr[1].startsWith("genomeview")){
				if(arr[2].toLowerCase().equals("position")){
					doPosition(arr[3]);
				}
				if(arr[2].toLowerCase().equals("load")){
					doLoad(arr[3]);
					
				}
				if(arr[2].toLowerCase().equals("session")){
					doSession(arr[3]);
					
				}
				if(arr[2].toLowerCase().equals("unload")){
					model.clearEntries();
				}
			}else{
				log.log(Level.WARNING, "This instruction doesn't belong to GenomeView, I'll ignore it.");
			}
			
		}
		s.close();

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
			DataSource ds=DataSourceFactory.createURL(URIFactory.url(s));
			ReadWorker rw=new ReadWorker(ds, model);
			rw.execute();
		} catch (ReadFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}