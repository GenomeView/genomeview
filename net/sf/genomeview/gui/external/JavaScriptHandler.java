/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;

/**
 * Javascript Handler for GenomeView
 * 
 * @author Thomas Abeel
 * 
 */
public class JavaScriptHandler {
	
	class DaemonThreadFactory implements ThreadFactory {
	    public Thread newThread(Runnable r) {
	        Thread thread = new Thread(r);
	        thread.setDaemon(true);
	        return thread;
	    }
	}

	private Logger log = Logger.getLogger(JavaScriptHandler.class.getCanonicalName());
	
	private ExecutorService es=Executors.newSingleThreadExecutor(new DaemonThreadFactory());
	
	public JavaScriptHandler(final Model model, final String id) {
		
		ServerSocket tmp = null;

		int port = 2223;

		while (tmp == null) {
			try {
				tmp = new ServerSocket(port);
			} catch (IOException e) {
				System.out.println("failed on port " + port);
			}
			port++;
		}

		final ServerSocket ss = tmp;
		
		Thread handler=new Thread(new Runnable() {

			@Override
			public void run() {
				int localPort = ss.getLocalPort();
				if (localPort != 2223)
					notifyMainHandler(localPort);

				System.out.println("listening on port: " + ss.getLocalPort());
				while (true && !ss.isClosed()) {
					try {
						Socket s = ss.accept();
						InstructionWorker ws = new InstructionWorker(model, id,s);
						es.execute(ws);
						
						
					} catch (SocketException e) {
						log.log(Level.WARNING, "This is normal when closing the socket", e);

					} catch (IOException e) {
						CrashHandler.showErrorMessage("Failed to accept socket", e);
					}
				}

			}

			private void notifyMainHandler(int localPort) {
				try {
					Socket clientSocket = new Socket(InetAddress.getLocalHost(), 2223);
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
					out.println("GenomeViewJavaScriptHandler-" + localPort);
					out.close();
					clientSocket.close();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});
		handler.setDaemon(true);
		handler.start();

	}
}
