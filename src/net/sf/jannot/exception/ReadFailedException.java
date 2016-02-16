/**
 * %HEADER%
 */
package net.sf.jannot.exception;

import java.io.IOException;

public class ReadFailedException extends Exception {

	private static final long serialVersionUID = 1L;


	public ReadFailedException(IOException e) {
        super(e);
    }

    
    public ReadFailedException(Throwable arg0) {
        super(arg0);
    }
    public ReadFailedException(String arg0) {
        super(arg0);
    }
}
