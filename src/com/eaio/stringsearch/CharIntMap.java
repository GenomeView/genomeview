/* 
 * CharIntMap.java
 * 
 * Created on 13.11.2003.
 *
 * eaio: StringSearch - high-performance pattern matching algorithms in Java
 * Copyright (c) 2003, 2004 Johann Burkard (jb@eaio.com) http://eaio.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
package com.eaio.stringsearch;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The CharIntMap is a collection to save <code>char</code> to <code>int</code>
 * mappings in. The CharIntMap is destined to provide fast access to skip tables
 * while being both Unicode-safe and more RAM-effective than a naive
 * <code>int</code> array.
 * <br><br>
 * The CharIntMap is initialized by specifying the extent between the lowest and
 * the highest occuring character and the lowest occuring character. Only an
 * array of size <code>highest - lowest + 1</code> is constructed.
 * <br><br>
 * There's usually no need to construct a CharIntMap yourself, it is done
 * automatically for you in the pre-processing methods.
 * 
 * @author <a href="mailto:jb@eaio.com">Johann Burkard</a>
 * @version 1.2
 */
public class CharIntMap implements Externalizable, Cloneable {
 
 static final long serialVersionUID = 1351686633123489568L;

 private int[] array;
 private char lowest;
 private int defaultValue;

 /**
  * Constructor for CharIntMap. Required for Serialization.
  */
 public CharIntMap() {}

 /**
  * Constructor for CharIntMap.
  * 
  * @param extent the extent of the text
  * @param lowest the lowest occuring character
  */
 public CharIntMap(int extent, char lowest) {
  this(extent, lowest, 0);
 }

 /**
  * Constructor for CharIntMap.
  * 
  * @param extent the extent of the text
  * @param lowest the lowest occuring character
  * @param defaultValue a default value to initialize the underlying
  * <code>int</code> array with
  */

 public CharIntMap(int extent, char lowest, int defaultValue) {
  array = new int[extent];
  this.lowest = lowest;
  if (defaultValue != 0) {
   for (int i = 0; i < array.length; i++) {
    array[i] = defaultValue;
   }
   this.defaultValue = defaultValue;
  }
 }

 /**
  * Returns a deep clone of this CharIntMap.
  * 
  * @return an CharIntMap containing the same mappings
  */
 public Object clone() throws CloneNotSupportedException{
	 super.clone();
	 
  CharIntMap out = new CharIntMap();
  out.lowest = lowest;
  out.defaultValue = defaultValue;
  if (array != null) {
   out.array = new int[array.length];
   System.arraycopy(array, 0, out.array, 0, array.length);
  }
  return out;
 }

 /**
  * Returns the stored value for the given <code>char</code>.
  * 
  * @param c the <code>char</code>
  * @return the stored value
  */
 public int get(char c) {

  /*
   * For the Sun VM, the char version accelerates some algorithms by 5 to 10 %,
   * in the IBM VM, there is no difference.
   */

  //  int x = c - lowest;
  //  if (x < 0 || x >= array.length) {
  //   return defaultValue;
  //  }

  char x = (char) (c - lowest);
  if (x >= array.length) {
   return defaultValue;
  }
  return array[x];
 }

 /**
  * Sets the stored value for the given <code>char</code>.
  *  
  * @param c the <code>char</code>
  * @param val the new value
  */
 public void set(char c, int val) {

  /*
   * For the Sun VM, the char version accelerates some algorithms by 5 to 10 %,
   * in the IBM VM, there is no difference.
   */

  //  int x = c - lowest;
  //  if (x < 0 || x >= array.length) {
  //   return;
  //  }

  char x = (char) (c - lowest);
  if (x >= array.length) {
   return;
  }
  array[x] = val;
 }

 /**
  * Returns the extent of the actual <code>char</code> array.
  * 
  * @return the extent
  */
 public int getExtent() {
  return array.length;
 }

 /**
  * Returns the lowest char that mappings can be saved for.
  * 
  * @return a <code>char</code>
  */
 public char getLowest() {
  return lowest;
 }

 /**
  * Returns the highest char that mappings can be saved for.
  * @return char
  */
 public char getHighest() {
  return (char) (lowest + array.length);
 }

 /**
  * Returns if this Object is equal to another Object.
  * 
  * @param obj the other Object
  * @return if this Object is equal
  * @see java.lang.Object#equals(Object)
  */
 public boolean equals(Object obj) {
  if (this == obj) {
   return true;
  }
  if (!(obj instanceof CharIntMap)) {
   return false;
  }
  CharIntMap m = (CharIntMap) obj;
  if (lowest != m.lowest) {
   return false;
  }
  if (defaultValue != m.defaultValue) {
   return false;
  }
  if (array == null && m.array == null) {
   return true;
  }
  else if (array != null && m.array == null) {
   return false;
  }
  else if (array == null && m.array != null) {
   return false;
  }
  for (int i = 0; i < array.length; i++) {
   if (array[i] != m.array[i]) {
    return false;
   }
  }
  return true;
 }

 /**
  * Returns the hashCode of this Object.
  * 
  * @return the hashCode
  * @see java.lang.Object#hashCode()
  */
 public int hashCode() {
  int out = getClass().getName().hashCode();
  out ^= lowest;
  out ^= defaultValue;
  if (array != null) {
   for (int i = 0; i < array.length; i++) {
    out ^= array[i];
   }
  }
  return out;
 }

 /**
  * Returns a String representation of this Object.
  * 
  * @return a String
  * @see java.lang.Object#toString()
  */
 public String toString() {
  StringBuffer out = new StringBuffer(128);
  out.append("{ CharIntMap: lowest = ");
  out.append(lowest);
  out.append(", defaultValue = ");
  out.append(defaultValue);
  if (array != null) {
   out.append(", array = ");
   for (int i = 0; i < array.length; i++) {
    if (array[i] != 0) {
     out.append(i);
     out.append(": ");
     out.append(array[i]);
    }
   }
  }
  out.append(" }");
  return out.toString();
 }

 /**
  * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
  */
 public void writeExternal(ObjectOutput out) throws IOException {
  if (array == null) {
   out.writeInt(0);
  }
  else {
   out.writeInt(array.length);
   for (int i = 0; i < array.length; i++) {
    out.writeInt(array[i]);
   }
  }
  out.writeChar(lowest);
  out.writeInt(defaultValue);
 }

 /**
  * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
  */
 public void readExternal(ObjectInput in)
  throws IOException, ClassNotFoundException {
  int l = in.readInt();
  if (l > 0) {
   array = new int[l];
   for (int i = 0; i < array.length; i++) {
    array[i] = in.readInt();
   }
  }
  lowest = in.readChar();
  defaultValue = in.readInt();
 }

}
