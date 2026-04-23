/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import be.abeel.concurrency.DaemonThreadFactory;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;

/**
 * Javascript Handler for GenomeView
 * 
 * @author Thomas Abeel
 * 
 */
public class JavaScriptHandler {

	private final ExecutorService es = Executors
			.newSingleThreadExecutor(new DaemonThreadFactory());

	public JavaScriptHandler(final Model model, final String id) {

		ServerSocket tmp = null;

		int port = 2223;

		while (tmp == null) {
			try {
				tmp = new ServerSocket(port);
			} catch (IOException e) {
				// not serious, we just try the next port
				model.getLog().log(Level.INFO, "failed on port " + port);
			}
			port++;
		}

		final ServerSocket ss = tmp;
		model.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				if (model.isExitRequested())
					try {
						ss.close();
					} catch (IOException e) {
						model.getLog().log(Level.WARNING,
								"javascripthandler close failed", e);
					}

			}
		});
		Thread handler = new Thread(new Runnable() {

			@Override
			public void run() {
				int localPort = ss.getLocalPort();
				if (localPort != 2223)
					notifyMainHandler(localPort);

				model.getLog().log(Level.INFO,
						"listening on port: " + ss.getLocalPort());
				while (true && !ss.isClosed()) {
					try {
						Socket s = ss.accept();
						InstructionWorker ws = new InstructionWorker(model, id,
								s);
						es.execute(ws);

					} catch (SocketException e) {
						model.getLog().log(Level.INFO,
								"Normal close-down exception received, closing sockets");

					} catch (IOException e) {
						model.getLog().log(Level.SEVERE,
								MessageManager.getString(
										"jshandler.failed_to_accept_socket"),
								e);
					}
				}

			}

			private void notifyMainHandler(int localPort) {
				try {
					// what is this doing?
					Socket clientSocket = new Socket(InetAddress.getLocalHost(),
							2223);
					PrintWriter out = new PrintWriter(
							clientSocket.getOutputStream());
					out.println("GenomeViewJavaScriptHandler-" + localPort);
					out.close();
					clientSocket.close();
				} catch (IOException e) {
					model.getLog().log(Level.WARNING,
							"Failure sending javascripthandler info to port "
									+ localPort,
							e);
				}

			}

		});
		handler.setDaemon(true);
		handler.start();

	}

}
