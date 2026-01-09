/*
 * ShiftOrWildcards.java
 * 
 * Created on 13.08.2003
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
 * An implementation of the Shift-Or algorithm with wildcards ("don't care"
 * symbols). The wildcard character is initially '?', but any character can
 * be used through the {@link #processChars(char[], char)} and the
 * {@link #processBytes(byte[], byte)} methods.
 * <br><br>
 * <pre>
 * Preprocessing: O(2n + &sum;) time
 * 
 * Searching    : O(mn / log n) (worst case and average)
 * </pre> 
 * 
 * @see #processBytes(byte[], byte)
 * @see #processChars(char[], char)
 * @see com.eaio.stringsearch.ShiftOr
 * @author <a href="mailto:jb@eaio.de">Johann Burkard</a>
 * @version 1.2
 */
public class ShiftOrWildcards extends ShiftOr {

 /**
  * The wildcard character (initially '?').
  */
 public static char wildcard = '?';

 /**
  * Constructor for ShiftOrWildcards. Note that it is not required to create
  * multiple instances.
  */
 public ShiftOrWildcards() {}

 /**
  * Pre-processing of the pattern. The pattern may not exceed 31 bytes in
  * length. If it does, <b>only it's first 31 bytes</b> are processed which
  * might lead to unexpected results. Returns an <code>int</code> array. The
  * wildcard character is obtained from the static {@link #wildcard} field.
  * 
  * @see com.eaio.stringsearch.StringSearch#processBytes(byte[])
  * @see #processBytes(byte[], byte)
  */
 public Object processBytes(byte[] pattern) {
  return processBytes(pattern, (byte) wildcard);
 }

 /**
  * Pre-processing of the pattern. The pattern may not exceed 31 bytes in
  * length. If it does, <b>only it's first 31 bytes</b> are processed which
  * might lead to unexpected results. Returns an <code>int</code> array.
  * 
  * @param pattern the <code>byte</code> array containing the pattern, may not
  * be <code>null</code>
  * @param w the wildcard <code>byte</code> character
  * @return an <code>int</code> array
  */
 public Object processBytes(byte[] pattern, byte w) {
  int j = ~0;
  int end = Math.min(pattern.length, 31);

  for (int i = 0; i < end; ++i) {
   if (pattern[i] == w) {
    j -= 1 << i;
   }
  }

  int[] t = new int[256];

  for (int i = 0; i < t.length; ++i) {
   t[i] = j;
  }

  for (int i = 0; i < end; ++i) {
   if (pattern[i] != w) {
    t[index(pattern[i])] &= ~(1 << i);
   }
  }
  return t;
 }

 /**
  * Pre-processing of the pattern. The pattern may not exceed 31 bytes in
  * length. If it does, <b>only it's first 31 bytes</b> are processed which
  * might lead to unexpected results. Returns a {@link CharIntMap}. The wildcard
  * character is obtained from the static {@link #wildcard} field.
  * 
  * @param pattern the <code>char</code> array containing the pattern, may not
  * be <code>null</code>
  * @return a {@link CharIntMap}
  * @see StringSearch#processChars(char[])
  * @see #processChars(char[], char)
  */
 public Object processChars(char[] pattern) {
  return processChars(pattern, wildcard);
 }

 /**
  * Pre-processing of the pattern. The pattern may not exceed 31 bytes in
  * length. If it does, <b>only it's first 31 bytes</b> are processed which
  * might lead to unexpected results. Returns a {@link CharIntMap}.
  * 
  * @param pattern the <code>char</code> array containing the pattern, may not
  * be <code>null</code>
  * @param w the wildcard character
  * @return a {@link CharIntMap}.
  */
 public Object processChars(char[] pattern, char w) {
  int j = ~0;
  int end = Math.min(pattern.length, 31);

  for (int i = 0; i < end; ++i) {
   if (pattern[i] == w) {
    j -= 1 << i;
   }
  }

  CharIntMap m = createCharIntMap(pattern, j);

  for (int i = 0; i < end; ++i) {
   if (pattern[i] != w) {
    m.set(pattern[i], m.get(pattern[i]) & ~(1 << i));
   }
  }

  return m;
 }

 /**
  * Pre-processing of the pattern. The pattern may not exceed 31 bytes in
  * length. If it does, <b>only it's first 31 bytes</b> are processed which
  * might lead to unexpected results. Returns a {@link CharIntMap}.
  * 
  * @param pattern the String containing the pattern, may not be
  * <code>null</code>
  * @param w the wildcard character
  * @return a {@link CharIntMap}.
  */
 public Object processString(String pattern, char w) {
  return processChars(StringSearch.activeDispatch.charsOf(pattern), w);
 }

}
