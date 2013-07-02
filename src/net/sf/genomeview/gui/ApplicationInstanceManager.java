/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.abeel.concurrency.DaemonThread;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class ApplicationInstanceManager {

	private static final Logger log = LoggerFactory.getLogger(ApplicationInstanceManager.class.getCanonicalName());

	/** Randomly chosen, but static, high socket number */
	public static final int SINGLE_INSTANCE_NETWORK_SOCKET = 15987;

	/** Must end with newline */
	public static final String SINGLE_INSTANCE_SHARED_KEY = "$$GenomeViewInstance$$";

	/**
	 * Registers this instance of the application.
	 * 
	 * @return true if first instance, false if not.
	 */
	public static boolean registerInstance(final String[]args) {
		// returnValueOnError should be true if lenient (allows app to run on
		// network error) or false if strict.
		boolean returnValueOnError = true;
		// try to open network socket
		// if success, listen to socket for new instance message, return true
		// if unable to open, connect to existing and send new instance message,
		// return false
		try {
			final ServerSocket socket = new ServerSocket(SINGLE_INSTANCE_NETWORK_SOCKET, 10, InetAddress.getLocalHost());
			log.info("Listening for application instances on socket " + SINGLE_INSTANCE_NETWORK_SOCKET);
			DaemonThread instanceListenerThread = new DaemonThread(new Runnable() {
				public void run() {
					boolean socketClosed = false;
					while (!socketClosed) {
						if (socket.isClosed()) {
							socketClosed = true;
						} else {
							try {
								Socket client = socket.accept();
								BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
								String message = in.readLine();
								if (SINGLE_INSTANCE_SHARED_KEY.trim().equals(message.trim())) {
									log.info("Shared key matched - new application instance found");
									String param=in.readLine();
									log.info("Param line: "+param);
									String s=param.split("=",2)[1];
									log.info("Re-initializing with params: "+s);
									wm.init(s.substring(1, s.length()-1).split(", "),null);
								}
								in.close();
								client.close();
							} catch (IOException e) {
								log.error("Exception in ApplicationInstanceManager",e);
								socketClosed = true;
							} catch (Exception e) {
								log.error("Exception in ApplicationInstanceManager",e);
								
							} 
						}
					}
				}
			});
			instanceListenerThread.start();
//			initialize(args);
			// listen
		} catch (UnknownHostException e) {
			log.error( e.getMessage(), e);
			return returnValueOnError;
		} catch (IOException e) {
			log.info("Port is already taken.  Notifying first instance.");
			try {
				Socket clientSocket = new Socket(InetAddress.getLocalHost(), SINGLE_INSTANCE_NETWORK_SOCKET);
				PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
				pw.println(SINGLE_INSTANCE_SHARED_KEY);
				log.info("Writing parameters: "+Arrays.toString(args));
				pw.println("PARAM="+Arrays.toString(args));
				pw.println();
				pw.flush();
				log.info("Writer error-check: "+pw.checkError());
				pw.close();
				clientSocket.close();
				log.info("Successfully notified first instance.");
				return false;
			} catch (UnknownHostException e1) {
				log.error( e.getMessage(), e);
				return returnValueOnError;
			} catch (IOException e1) {
				log.error("Error connecting to local port for single instance notification");
				log.error( e1.getMessage(), e1);
				return returnValueOnError;
			}

		}
		return true;
	}

	private static WindowManager wm;

	public static void setCallback(WindowManager mw) {
		wm=mw;
		
	}

}
