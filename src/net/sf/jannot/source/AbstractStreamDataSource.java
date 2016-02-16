/**
 * %HEADER%
 */
package net.sf.jannot.source;

import java.io.InputStream;

import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.parser.Parser;

/**
 * Extends DataSource. 
 * 
 * Contains methods to read  
 * 
 * @author Thomas Abeel
 *
 */
public abstract class AbstractStreamDataSource extends DataSource {
    private Parser parser;

    private InputStream ios;

    public final void setParser(Parser parser) {
        this.parser = parser;
    }

    public final void setIos(InputStream ios) {
        this.ios = ios;
    }

    protected AbstractStreamDataSource(Locator l) {
super(l);
    }

    /**
     * 
     */
    @Override
    public EntrySet read(EntrySet set) throws ReadFailedException {
       return parser.parse(ios,set);
     

    }

    public InputStream getIos(){
    	return ios;
    }
    
    public Parser getParser() {
        return parser;
    }
}
