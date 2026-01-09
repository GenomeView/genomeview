/**
 * %HEADER%
 */
package net.sf.jannot.pileup;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public interface Pile {
	public float getTotal();
	public int start();
	public int end();
	public float getValue(int i);
	public int getValueCount();
	public byte[]getBases();
	public int getLength();
	public void setLength(int len);
	
	
}
