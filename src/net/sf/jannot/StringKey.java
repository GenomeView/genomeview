package net.sf.jannot;

public class StringKey implements DataKey {

	@Override
	public String toString() {
		return key;
	}

	private String key;

	public StringKey(String key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object dkey) {
		if (!(dkey instanceof StringKey))
			return false;
		return ((StringKey) dkey).key.equals(this.key);

	}

	@Override
	public int compareTo(DataKey o) {
		return o.toString().compareTo(this.toString());
	}
}