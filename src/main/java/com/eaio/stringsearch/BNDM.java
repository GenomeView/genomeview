/*
 * BNDM.java
 * 
 * Created on 21.10.2003
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
 * An implementation of the Backwards Non-deterministic Dawg (Directed
 * acyclic word graph) Matching algorithm by Gonzalo Navarro and
 * Mathieu Raffinot. See "A Bit-Parallel Approach to Suffix Automata: Fast
 * Extended String Matching" (appeared in <em>Proceedings of the 9th Annual
 * Symposium on Combinatorial Pattern Matching, 1998</em>).
 * <br><br>
 * This is one of the fastest algorithms, but it does not beat the 
 * {@link com.eaio.stringsearch.BoyerMooreHorspoolRaita} and the
 * {@link com.eaio.stringsearch.BoyerMooreHorspool} algorithms.
 * <br><br>
 * <pre>
 * Preprocessing: O(m) time
 * 
 * Searching    : O(n/m) (best case)
 *                O(n log|&sum;| m / m) (average)
 *                O(mn) (worst case)
 * </pre> 
 * 
 * @see <a href="http://www.dcc.uchile.cl/~gnavarro/ps/cpm98.ps.gz">
 * http://www.dcc.uchile.cl/~gnavarro/ps/cpm98.ps.gz
 * </a>
 * @see <a href="http://www-igm.univ-mlv.fr/~raffinot/ftp/cpm98.ps.gz">
 * http://www-igm.univ-mlv.fr/~raffinot/ftp/cpm98.ps.gz
 * </a>
 * @see <a href="http://citeseer.nj.nec.com/navarro98bitparallel.html">
 * http://citeseer.nj.nec.com/navarro98bitparallel.html
 * </a> 
 * @author <a href="mailto:jb@eaio.de">Johann Burkard</a>
 * @version 1.2
 */
public class BNDM extends StringSearch {

 /**
  * Constructor for BNDM. Note that it is not required to create multiple
  * instances.
  */
 public BNDM() {}

 /**
  * Pre-processing of the pattern. The pattern may not exceed 32 bytes in
  * length. If it does, <b>only it's first 32 bytes</b> are processed which
  * might lead to unexpected results. Returns an <code>int</code> array.
  * 
  * @see com.eaio.stringsearch.StringSearch#processBytes(byte[])
  */
 public Object processBytes(byte[] pattern) {
  int end = pattern.length < 32 ? pattern.length : 32;

  int[] b = new int[256];

  int j = 1;
  for (int i = end - 1; i >= 0; --i, j <<= 1) {
   b[index(pattern[i])] |= j;
  }

  return b;
 }

 /**
  * Pre-processing of the pattern. The pattern may not exceed 32 bytes in
  * length. If it does, <b>only it's first 32 bytes</b> are processed which
  * might lead to unexpected results. Returns a {@link CharIntMap}.
  * 
  * @see com.eaio.stringsearch.StringSearch#processChars(char[])
  */
 public Object processChars(char[] pattern) {
  int end = pattern.length < 32 ? pattern.length : 32;

  CharIntMap b = createCharIntMap(pattern);

  int j = 1;
  for (int i = end - 1; i >= 0; --i, j <<= 1) {
   b.set(pattern[i], b.get(pattern[i]) | j);
  }

  return b;
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

  /* comment:start */

  return useNative
   ? nativeSearchBytes(text, textStart, textEnd, pattern, processed)
   : javaSearchBytes(text, textStart, textEnd, pattern, processed);

 }

 private int javaSearchBytes(
  byte[] text,
  int textStart,
  int textEnd,
  byte[] pattern,
  Object processed) {

  /* comment:end */

  int[] b = (int[]) processed;

  int d, j, pos, last;
  pos = textStart;
  while (pos <= textEnd - pattern.length) {
   j = pattern.length - 1;
   last = pattern.length;
   d = -1;
   while (d != 0) {
    d &= b[index(text[pos + j])];
    if (d != 0) {
     if (j == 0) {
      return pos;
     }
     last = j;
    }
    --j;
    d <<= 1;
   }
   pos += last;
  }

  return -1;

 }

 /* comment:start */

 private native int nativeSearchBytes(
  byte[] text,
  int textStart,
  int textEnd,
  byte[] pattern,
  Object processed);

 /* comment:end */

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

  CharIntMap b = (CharIntMap) processed;

  int d, j, pos, last;
  pos = textStart;
  while (pos <= textEnd - pattern.length) {
   j = pattern.length - 1;
   last = pattern.length;
   d = -1;
   while (d != 0) {
    d &= b.get(text[pos + j]);
    if (d != 0) {
     if (j == 0) {
      return pos;
     }
     last = j;
    }
    --j;
    d <<= 1;
   }
   pos += last;
  }

  return -1;

 }

}
