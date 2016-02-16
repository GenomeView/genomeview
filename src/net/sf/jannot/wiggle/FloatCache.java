/**
 * %HEADER%
 */
package net.sf.jannot.wiggle;

import java.util.BitSet;
/**
 * 
 * @author Thomas Abeel
 *
 */
class FloatCache implements Query {

	private static final int reductionfactor = 32;
	private float[] buffer;
	private BitSet valid = new BitSet();
	private Query source;

	public FloatCache(Query source) {
		buffer = new float[1+(int) (source.size() / reductionfactor)];
//		System.out.println("Original: " + source.size() + "\t" + buffer.length);
		this.source = source;
	}

	public float[] getRawRange(int start, int end) {
		if(start/reductionfactor>=buffer.length)
			return new float[0];
//		 System.out.println("Buffer5: "+start+"\t"+end);
		// System.out.println("Buffer5: "+start/reductionfactor+"\t"+end/reductionfactor);
		for (int i = start / reductionfactor; i < end / reductionfactor; i++) {
			if (i>=0&&i<buffer.length&&!valid.get(i)) {
				// System.out.println(i+"\t"+(i*reductionfactor));
				float[] tmp = source.getRawRange(i * reductionfactor, i * reductionfactor + reductionfactor);
				double sum = 0;

				for (float f : tmp)
					sum += f;

				buffer[i] = (float) (sum / reductionfactor);
				// System.out.println("\t"+sum+"\t"+tmp.length+"\t"+buffer[i]);
				valid.set(i);
			}
		}

		float[] out = new float[(end - start) / reductionfactor];
		int len=out.length;
		if(start/reductionfactor+len>buffer.length)
			len=buffer.length-start/reductionfactor;
		if(start<0)
			start=0;
		System.arraycopy(buffer, start / reductionfactor, out, 0,len);
		// System.out.println(out[0]+"\t"+out[1]+"\t"+out[2]+"\t"+out[3]+"\t"+out[4]+"\t"+out[5]);
		return out;
	}

	@Override
	public long size() {
		return buffer.length;
	}

}