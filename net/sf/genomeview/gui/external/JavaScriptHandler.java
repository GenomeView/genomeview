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
import java.net.UnknownHostException;
import java.util.ArrayList;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;

/**
 * Javascript Handler for GenomeView
 * 
 * @author Thomas Abeel
 * 
 */
public class JavaScriptHandler {

	/* Where worker threads stand idle */
	static ArrayList<InstructionWorker> threads = new ArrayList<InstructionWorker>();

	public JavaScriptHandler(final Model model, final String id) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int port = 2223;

				/* start worker threads */
				InstructionWorker w = new InstructionWorker(model, id);
				(new Thread(w, "worker")).start();
				threads.add(w);
				ServerSocket ss = null;
				while (ss == null) {
					try {
						ss = new ServerSocket(port);
					} catch (IOException e) {
						System.out.println("failed on port " + port);
					}
					port++;
				}
				int localPort = ss.getLocalPort();
				if(localPort!=2223)
					notifyMainHandler(localPort);

				System.out.println("listening on port: " + ss.getLocalPort());
				while (true) {
					try {
						Socket s = ss.accept();
						w = null;
						synchronized (threads) {
							if (threads.isEmpty()) {
								InstructionWorker ws = new InstructionWorker(model, id);
								ws.setSocket(s);
								(new Thread(ws, "additional worker")).start();
							} else {
								w = (InstructionWorker) threads.get(0);
								threads.remove(0);
								w.setSocket(s);
							}
						}
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

		}).start();

	}
}
