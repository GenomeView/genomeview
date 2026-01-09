/**
 * %HEADER%
 */
package net.sf.jannot.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.jannot.parser.Parser;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class URLSource extends AbstractStreamDataSource {

	protected URL url;

	/*
	 * Only for use by subclasses. The extra object is only to distinguish
	 * constructors and is ignored
	 */
	protected URLSource(URL url, Object x) throws IOException {
		super(new Locator(url.toString()));
		this.url = url;
	}

	private void init() throws MalformedURLException, IOException {
		PushbackInputStream pis=new PushbackInputStream(url.openStream(), 16*1024);
		byte[]buffer=new byte[16*1024];
		int i=pis.read(buffer);
		super.setParser(Parser.detectParser(new ByteArrayInputStream(buffer,0,i),url));
		pis.unread(buffer,0,i);
		super.setIos(pis);

	}

	public URLSource(URL url) throws IOException {
		this(url, null);
		SSL.certify(url);
		init();

	}

//	@Override
//	public void saveOwn(EntrySet entries) throws SaveFailedException {
//		try {
//			System.out.println(url.getProtocol() + "://" + url.getHost() + url.getPath());
//			url = new URL(url.getProtocol() + "://" + url.getHost() + url.getPath());
//			File tmp = File.createTempFile("GenomeView", "save");
//			tmp.deleteOnExit();
//			OutputStream os = new FileOutputStream(tmp);
//			for (Entry e : entries) {
//				super.getParser().write(os, e, this);
//			}
//			os.close();
//			LineIterator it = new LineIterator(tmp);
//			System.out.println("-------");
//			System.out.println("Uploaded file:");
//			for (String line : it)
//				System.out.println(line);
//			System.out.println("---EOF---");
//			it.close();
//
//			String reply = ClientHttpUpload.upload(tmp, url);
//			System.out.println("SERVER REPLY: " + reply);
//			// TODO add more checks on the reply.
//			if (reply.equals("")) {
//
//				throw new SaveFailedException("Empty reply from server");
//
//			}
//			if (reply.toLowerCase().contains("error")) {
//				JOptionPane.showMessageDialog(null, reply);
//				throw new SaveFailedException("Error reply from server");
//
//			}
//			JOptionPane.showMessageDialog(null, reply);
//
//		} catch (IOException e) {
//
//			e.printStackTrace();
//			throw new SaveFailedException("IOException");
//		}
//
//	}

	public URL getURL() {
		return url;
	}

	@Override
	public String toString() {
		return url.toString();
	}

	/* (non-Javadoc)
	 * @see net.sf.jannot.source.DataSource#isIndexed()
	 */
	@Override
	public boolean isIndexed() {
		return false;
	}
	
	
	private long cachedSize=-2;

	/* (non-Javadoc)
	 * @see net.sf.jannot.source.DataSource#size()
	 */
	@Override
	public long size() {
		if(cachedSize==-2)
			try {
				cachedSize=url.openConnection().getContentLength();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return cachedSize;
	}

//	@Override
//	public boolean isDestructiveSave() {
//
//		return false;
//	}

}
