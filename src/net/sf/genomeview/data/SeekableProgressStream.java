/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.awt.Component;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.swing.ProgressMonitor;

import net.sf.samtools.seekablestream.SeekableStream;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class SeekableProgressStream extends SeekableStream {

	private ProgressMonitor monitor;
	private int nread = 0;
	private int size = 0;
	private SeekableStream in;

	public SeekableProgressStream(Component parentComponent, Object message, SeekableStream in) {
		this.in = in;
		try {
			size = in.available();
		} catch (IOException ioe) {
			size = 0;
		}
		monitor = new ProgressMonitor(parentComponent, message, null, 0, size);
	}

	@Override
	public void close() throws IOException {
		monitor.close();
		in.close();

	}

	@Override
	public boolean eof() throws IOException {
		return in.eof();
	}

	@Override
	public String getSource() {
		return in.getSource();
	}

	@Override
	public long length() {
		return in.length();
	}

	@Override
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
		int read=in.read(arg0,arg1,arg2);
		nread+=read;
		monitor.setProgress(nread);
		return read;
	}

	@Override
	public void seek(long arg0) throws IOException {
		if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
		in.seek(arg0);

	}

	@Override
	public int read() throws IOException {
		if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
		nread++;
		monitor.setProgress(nread);
	
		return in.read();
	}

	public ProgressMonitor getProgressMonitor() {
		return monitor;
	}

	@Override
	public long position() throws IOException {
		return in.position();
	}

}
