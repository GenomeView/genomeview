/**
 * %HEADER%
 */
package net.sf.genomeview.gui.external;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

	public JavaScriptHandler(final Model model) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int port = 2223;

				/* start worker threads */
				for (int i = 0; i < 1; ++i) {
					InstructionWorker w = new InstructionWorker(model);
					(new Thread(w, "worker #" + i)).start();
					threads.add(w);
				}
				try {
					ServerSocket ss = new ServerSocket(port);

					while (true) {
						try {
							Socket s = ss.accept();

							InstructionWorker w = null;
							synchronized (threads) {
								if (threads.isEmpty()) {
									InstructionWorker ws = new InstructionWorker(model);
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

				} catch (IOException e) {
					CrashHandler.showErrorMessage("Failed to start server socket", e);
				}
			}

		}).start();

	}
}
