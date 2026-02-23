/*
 * ShiftOrMismatches.java
 * 
 * Created on 14.11.2003.
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

/**
 * An implementation of the Shift-Or algorithm with mismatches. Note that the
 * pattern length may not be larger than 31 / &lceil; log<sub>2</sub> (k + 1)
 * &rceil;.
 * <br><br>
 * <table style="border: 1px solid #ccc" cellpadding="4">
 * <tr>
 * <td>Editing distance (k)</td>
 * <td>Maximum pattern length</td>
 * </tr>
 * <tr>
 * <td>0</td>
 * <td>31</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>15</td>
 * </tr>
 * <tr>
 * <td>2-3</td>
 * <td>10</td>
 * </tr>
 * <tr>
 * <td>4-5</td>
 * <td>7</td>
 * </tr>
 * </table>
 * <br><br>
 * This algorithm is slower than {@link com.eaio.stringsearch.ShiftOr}. In
 * future versions of this library, faster alternatives are likely to be added.
 * <pre>
 * Preprocessing: O(3n + &sum;) time
 * 
 * Searching    : O(mn / log n) (worst case and average)
 * </pre> 
 * 
 * @author <a href="mailto:burkard@ergosign.de">Johann Burkard</a>
 * @version 1.2
 * @see #processBytes(byte[], int)
 * @see #processChars(char[], int)
 * @see com.eaio.stringsearch.ShiftOr
 */
public class ShiftOrMismatches extends MismatchSearch {

 private static final Object MISMATCH = new Object();
 private static final Object MATCH = new Object();

 /**
  * Constructor for ShiftOrMismatches. Note that it is not required to create
  * multiple instances.
  */
 public ShiftOrMismatches() {}

 /**
  * @throws IllegalArgumentException if the pattern length is larger than 31 /
  * &lceil; log<sub>2</sub> (k + 1) &rceil;
  * @see com.eaio.stringsearch.MismatchSearch#processBytes(byte[], int)
  */
 public Object processBytes(byte[] pattern, int k) {

  Object type = MISMATCH;

  if ((k << 1) > pattern.length) {
   type = MATCH;
   k = pattern.length - k;
  }

  int b = clog2(k + 1) + 1;

  if (pattern.length > (31 / b)) {
   throw new IllegalArgumentException();
  }

  /* Preprocessing */

  int i;
  int lim = k << ((pattern.length - 1) * b);
  int ovmask = 0;

  for (i = 0; i < pattern.length; i++) {
   ovmask = (ovmask << b) | (1 << (b - 1));
  }

  int[] t = new int[256];

  /* Loop that nulls the array if type == MATCH removed */

  if (type == MISMATCH) {
   lim += 1 << ((pattern.length - 1) * b);
   for (i = 0; i < t.length; i++) {
    t[i] = ovmask >> (b - 1);
   }
  }

  i = 1;
  for (int p = 0; p < pattern.length; p++, i <<= b) {
   if (type == MATCH) {
    t[index(pattern[p])] += i;
   }
   else {
    t[index(pattern[p])] &= ~i;
   }
  }

  return new Object[] {
   t,
   type,
   Integer.valueOf(i - 1),
   Integer.valueOf(ovmask),
   Integer.valueOf(b),
   Integer.valueOf(lim)};
 }

 /**
  * @throws IllegalArgumentException if the pattern length is larger than 31 /
  * &lceil; log<sub>2</sub> (k + 1) &rceil;
  * @see com.eaio.stringsearch.MismatchSearch#processChars(char[], int)
  */
 public Object processChars(char[] pattern, int k) {

  Object type = MISMATCH;

  if ((k << 1) > pattern.length) {
   type = MATCH;
   k = pattern.length - k;
  }

  int b = clog2(k + 1) + 1;

  if (pattern.length > (31 / b)) {
   throw new IllegalArgumentException();
  }

  /* Preprocessing */

  int i;
  int lim = k << ((pattern.length - 1) * b);
  int ovmask = 0;

  for (i = 0; i < pattern.length; i++) {
   ovmask = (ovmask << b) | (1 << (b - 1));
  }

  CharIntMap t;

  if (type == MATCH) {
   t = createCharIntMap(pattern);
  }
  else {
   lim += 1 << ((pattern.length - 1) * b);
   t = createCharIntMap(pattern, ovmask >> (b - 1));
  }

  i = 1;
  for (int p = 0; p < pattern.length; p++, i <<= b) {
   if (type == MATCH) {
    t.set(pattern[p], t.get(pattern[p]) + i);
   }
   else {
    t.set(pattern[p], t.get(pattern[p]) & ~i);
   }
  }

  return new Object[] {
   t,
   type,
  Integer.valueOf(i - 1),
  Integer.valueOf(ovmask),
  Integer.valueOf(b),
  Integer.valueOf(lim)};

 }

 /**
  * @see com.eaio.stringsearch.MismatchSearch#searchBytes(byte[], int, int,
  * byte[], Object, int)
  */
 public int[] searchBytes(
  byte[] text,
  int textStart,
  int textEnd,
  byte[] pattern,
  Object processed,
  int k) {

  Object[] o = (Object[]) processed;
  int[] t = (int[]) o[0];
  Object type = o[1];
  int mask = ((Integer) o[2]).intValue();
  int ovmask = ((Integer) o[3]).intValue();
  int b = ((Integer) o[4]).intValue();
  int lim = ((Integer) o[5]).intValue();

  int state, overflow;

  if (type == MATCH) {
   state = 0;
   overflow = 0;
  }
  else {
   state = mask & ~ovmask;
   overflow = ovmask;
  }

  for (int p = textStart; p < textEnd; p++) {
   state = ((state << b) + t[index(text[p])]) & mask;
   overflow = ((overflow << b) | (state & ovmask)) & mask;
   state &= ~ovmask;
   if (type == MATCH) {
    if ((state | overflow) >= lim) {
     return new int[] { p - pattern.length + 1, pattern.length - k };
    }
   }
   else if ((state | overflow) < lim) {
    return new int[] {
     p - pattern.length + 1,
     (state >> (pattern.length - 1) * b)};
   }
  }

  return new int[] { -1, 0 };

 }

 /**
  * @see com.eaio.stringsearch.MismatchSearch#searchChars(char[], int, int,
  * char[], Object, int)
  */
 public int[] searchChars(
  char[] text,
  int textStart,
  int textEnd,
  char[] pattern,
  Object processed,
  int k) {

  Object[] o = (Object[]) processed;
  CharIntMap t = (CharIntMap) o[0];
  Object type = o[1];
  int mask = ((Integer) o[2]).intValue();
  int ovmask = ((Integer) o[3]).intValue();
  int b = ((Integer) o[4]).intValue();
  int lim = ((Integer) o[5]).intValue();

  int state, overflow;

  if (type == MATCH) {
   state = 0;
   overflow = 0;
  }
  else {
   state = mask & ~ovmask;
   overflow = ovmask;
  }

  for (int p = textStart; p < textEnd; p++) {
   state = ((state << b) + t.get(text[p])) & mask;
   overflow = ((overflow << b) | (state & ovmask)) & mask;
   state &= ~ovmask;
   if (type == MATCH) {
    if ((state | overflow) >= lim) {
     return new int[] { p - pattern.length + 1, pattern.length - k };
    }
   }
   else if ((state | overflow) < lim) {
    return new int[] {
     p - pattern.length + 1,
     (state >> (pattern.length - 1) * b)};
   }
  }

  return new int[] { -1, 0 };

 }

 /**
  * Ceiling of log2(x).
  * 
  * @param x x
  * @return &lceil;log2(x)&rceil;
  */
 private int clog2(int x) {
  int i = 0;
  while (x > (1 << i)) {
   ++i;
  }
  return i;
 }

 /**
  * This algorithm is currently not using the native library. This method
  * therefore always returns <code>false</code>.
  * 
  * @see com.eaio.stringsearch.StringSearch#usesNative()
  */
 public boolean usesNative() {
  return false;
 }

}
