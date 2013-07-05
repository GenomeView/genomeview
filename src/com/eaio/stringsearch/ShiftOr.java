/* 
 * ShiftOr.java
 * 
 * Created on 12.08.2003.
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
 * An implementation of the Shift-Or algorithm by Ricardo Baeza-Yates and
 * Gaston Gonnet as outlined in "A New Approach to Text Searching" (appeared in
 * <em>Proceedings of the 12th International Conference on Research and
 * Development in Datum Retrieval</em>). The Shift-Or algorithm is a
 * bit-parallel algorithm.
 * <br><br>
 * The Shift-Or algorithm is not the fastest and by itself slower than String's
 * <code>indexOf</code> method. It's usefulness comes from it's ability to
 * support character classes and searching with errors at the same speed.
 * <br><br>
 * It's {@link #searchChars(char[], int, int, char[], Object)} method is
 * extremely slow in the Sun Java Virtual Machines. If possible, the 
 * {@link #searchBytes(byte[], int, int, byte[], Object)} methods should be
 * preferred. Because the main loop is also used by the
 * {@link com.eaio.stringsearch.ShiftOrWildcards} class, the implementation
 * cannot skip forward until the first character matches (as in the original
 * algorithm).
 * <br><br>
 * This implementation currently limited to at most 31 characters because Java
 * has no unsigned <code>int</code> type. An implementation that used
 * <code>long</code> has proved to take twice the amount of time.
 * <pre>
 * Preprocessing: O(n + &sum;) time
 * 
 * Searching    : O(mn / log n) (worst case and average)
 * </pre> 
 * 
 * @see
 * <a href="ftp://sunsite.dcc.uchile.cl/pub/users/rbaeza/papers/CACM92.ps.gz">
 * ftp://sunsite.dcc.uchile.cl/pub/users/rbaeza/papers/CACM92.ps.gz
 * </a>
 * @see <a href="http://citeseer.nj.nec.com/50265.html">
 * http://citeseer.nj.nec.com/50265.html
 * </a>
 * @author <a href="mailto:jb@eaio.com">Johann Burkard</a>
 * @version 1.2
 * @see com.eaio.stringsearch.ShiftOrWildcards
 * @see com.eaio.stringsearch.ShiftOrClasses
 * @see com.eaio.stringsearch.ShiftOrMismatches
 */
public class ShiftOr extends StringSearch {

 /**
  * Constructor for ShiftOr. Note that it is not required to create multiple
  * instances.
  */
 public ShiftOr() {}

 /**
  * Pre-processing of the pattern. The pattern may not exceed 31 bytes in
  * length. If it does, <b>only it's first 31 bytes</b> are processed which
  * might lead to unexpected results. Returns an <code>int</code> array.
  * 
  * @see com.eaio.stringsearch.StringSearch#processBytes(byte[])
  */
 public Object processBytes(byte[] pattern) {
  int j = ~0;
  int end = Math.min(pattern.length, 31);

  int[] t = new int[256];

  for (int i = 0; i < t.length; ++i) {
   t[i] = j;
  }

  for (int i = 0; i < end; ++i) {
   t[index(pattern[i])] &= ~(1 << i);
  }

  return t;
 }

 /**
  * Pre-processing of the pattern. The pattern may not exceed 31 characters in
  * length. If it does, <b>only it's first 31 characters</b> are processed which
  * might lead to unexpected results. Returns a {@link CharIntMap}.
  * 
  * @param char[] the pattern
  * @return an Object
  * @see StringSearch#processChars(char[])
  */
 public Object processChars(char[] pattern) {
  int end = Math.min(pattern.length, 31);

  CharIntMap m = createCharIntMap(pattern, ~0);

  for (int i = 0; i < end; ++i) {
   m.set(pattern[i], m.get(pattern[i]) & ~(1 << i));
  }

  return m;
 }

 /**
  * @see com.eaio.stringsearch.StringSearch#searchBytes(byte[], int, int,
  * byte[], java.lang.Object)
  */
 public int searchBytes(
  byte[] text,
  int textStart,
  int textEnd,
  byte[] pattern,
  Object processed) {

  if (StringSearch.useNative) {
   if (processed instanceof int[]) {
    return nativeSearchBytes(
     text,
     textStart,
     textEnd,
     pattern,
     (int[]) processed,
     pattern.length);

   }
   else {
    Object[] params = (Object[]) processed;
    int[] t = (int[]) params[0];
    int l = ((Integer) params[1]).intValue();
    return nativeSearchBytes(text, textStart, textEnd, pattern, t, l);
   }
  }
  else {
   return javaSearchBytes(text, textStart, textEnd, pattern, processed);
  }

 }

 private int javaSearchBytes(
  byte[] text,
  int textStart,
  int textEnd,
  byte[] pattern,
  Object processed) {

  int[] t;
  int l = pattern.length;

  if (processed instanceof int[]) {
   t = (int[]) processed;
  }
  else {
   Object[] params = (Object[]) processed;
   t = (int[]) params[0];
   l = ((Integer) params[1]).intValue();
  }

  int lim = ~((1 << (l - 1)) - 1);
  int state = ~0;
  for (int i = textStart; i < textEnd; ++i) {
   state = (state << 1) | t[index(text[i])];
   if (state < lim) {
    return i - l + 1;
   }
  }
  return -1;

 }

 private native int nativeSearchBytes(
  byte[] text,
  int textStart,
  int textEnd,
  byte[] pattern,
  int[] t,
  int l);

 /**
  * @see com.eaio.stringsearch.StringSearch#searchChars(char[], int, int,
  * char[], Object)
  */
 public int searchChars(
  char[] text,
  int textStart,
  int textEnd,
  char[] pattern,
  Object processed) {

  CharIntMap m;
  int l = pattern.length;

  if (processed instanceof CharIntMap) {
   m = (CharIntMap) processed;
  }
  else {
   Object[] params = (Object[]) processed;
   m = (CharIntMap) params[0];
   l = ((Integer) params[1]).intValue();
  }

  int lim = ~((1 << (l - 1)) - 1);
  int state = ~0;
  for (int i = textStart; i < textEnd; ++i) {
   state = (state << 1) | m.get(text[i]);
   if (state < lim) {
    return i - l + 1;
   }
  }

  return -1;

 }

}
