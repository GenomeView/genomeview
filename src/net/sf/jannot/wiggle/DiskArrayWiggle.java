/**
 * %HEADER%
 */
package net.sf.jannot.wiggle;

import java.io.IOException;
import java.util.Iterator;

import net.sf.jannot.picard.BinaryBlob;

/**
 * 
 * Make wiggle
 * 
 * Fill it with setMethod
 * 
 * Initialize with init();
 * 
 * @author Thomas Abeel
 * 
 */
public class DiskArrayWiggle extends AbstractWiggle implements Iterable<Float> {

	// private float[] buffer;
	// private String name;
	private float min = Float.POSITIVE_INFINITY;
	private float max = Float.NEGATIVE_INFINITY;
	// private FloatBuffer fb = null;
	// private int size;

	private BinaryBlob blob = null;
	private int size;

	public DiskArrayWiggle(int size) throws IOException {
		this.size=size;
		System.out.println("Mapping: " + size * 4);
		blob = new BinaryBlob(size * 4);
		System.out.println("Mapping successfull!");

	}

	/**
	 * Zero based coordinate
	 * 
	 * @param position
	 * @param value
	 */
	public void set(int position, float value) {

		if (value > max)
			max = value;
		if (value < min)
			min = value;
		try {
			blob.putFloat(position * 4, value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init() {
		super.init(this);
	}

	//
	// @Override
	// public String getName() {
	// return name;
	// }

	@Override
	public float[] getRawRange(int start, int end) {
		if (start >= size)
			return new float[0];
		float[] out = new float[end - start];
		int len = out.length;
		if (start + len > size)
			len = size - start;
		if (start < 0)
			start = 0;
		for(int i=start;i<end;i++)
			try {
				out[i-start]=blob.getFloat(i*4);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		blob.getFloat(out, start, len);
		// System.arraycopy(fb.capacity(), start, out, 0, len);
		return out;
	}

	@Override
	public float max() {
		return max;
	}

	@Override
	public float min() {
		return min;
	}

	@Override
	public long size() {
		return size;// / 4;
	}

	/**
	 * Get a single value, one based coordinate
	 * 
	 * @see net.sf.jannot.wiggle.Graph#value(int)
	 */
	@Override
	public float value(int pos) {
		try {
			return blob.getFloat(4 * (pos - 1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// buffer[pos - 1];
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get()
	 */
	@Override
	public Iterable<Float> get() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#canSave()
	 */
	@Override
	public boolean canSave() {
		return false;
	}

	class DiskIterator implements Iterator<Float> {

		private int currentIdx = 1;
		private DiskArrayWiggle daw;

		/**
		 * @param blob
		 */
		public DiskIterator(DiskArrayWiggle blob) {
			this.daw = blob;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return currentIdx <= daw.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Float next() {
			return daw.value(currentIdx++);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new RuntimeException("Does not work");

		}

	}

	@Override
	public Iterator<Float> iterator() {
		return new DiskIterator(this);
	}

}
