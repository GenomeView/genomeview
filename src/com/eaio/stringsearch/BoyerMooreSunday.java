/*
 * BoyerMooreSunday.java
 * 
 * Created on 20.10.2003
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
 * An implementation of Sunday's simplified "Quick Finder" version of the
 * Boyer-Moore algorithm. See "A very fast substring search algorithm" (appeared
 * in <em>Communications of the ACM . 33 (8):132-142</em>).
 * 
 * <pre>
 * Preprocessing: O(m + &sum;) time
 * 
 * Processing   : O(mn) worst case
 * </pre>
 * 
 * @author <a href="mailto:jb@eaio.com">Johann Burkard</a>
 * @version 1.2
 */
public class BoyerMooreSunday extends StringSearch {

    /**
     * Constructor for BoyerMooreSunday. Note that it is not required to create
     * multiple instances.
     */
    public BoyerMooreSunday() {
    }

    /**
     * Returns a <code>int</code> array.
     * 
     * @see com.eaio.stringsearch.StringSearch#processBytes(byte[])
     */
    public Object processBytes(byte[] pattern) {
        int[] td1 = new int[256];

        for (int i = 0; i < td1.length; ++i) {
            td1[i] = pattern.length + 1;
        }

        for (int i = 0; i < pattern.length; ++i) {
            td1[index(pattern[i])] = pattern.length - i;
        }

        return td1;
    }

    /**
     * Returns a {@link CharIntMap}.
     * 
     * @see com.eaio.stringsearch.StringSearch#processChars(char[])
     */
    public Object processChars(char[] pattern) {
        CharIntMap td1 = createCharIntMap(pattern, pattern.length + 1);

        for (int i = 0; i < pattern.length; ++i) {
            td1.set(pattern[i], pattern.length - i);
        }

        return td1;
    }

    /**
     * @see com.eaio.stringsearch.StringSearch#searchBytes(byte[], int, int,
     *      byte[], java.lang.Object)
     */
    public int searchBytes(byte[] text, int textStart, int textEnd, byte[] pattern, Object processed) {

        return useNative ? nativeSearchBytes(text, textStart, textEnd, pattern, processed) : javaSearchBytes(text,
                textStart, textEnd, pattern, processed);

    }

    private int javaSearchBytes(byte[] text, int textStart, int textEnd, byte[] pattern, Object processed) {

        int[] td1 = (int[]) processed;

        int p;

        while (textStart + pattern.length <= textEnd) {

            p = 0;

            while (p < pattern.length && pattern[p] == text[textStart + p]) {
                ++p;
            }

            if (p == pattern.length) {
                return textStart;
            }

            if (textStart + pattern.length >= textEnd) {
                return -1;
            }

            textStart += td1[index(text[textStart + pattern.length])];
        }

        return -1;

    }

    private native int nativeSearchBytes(byte[] text, int textStart, int textEnd, byte[] pattern, Object processed);

    /**
     * @see com.eaio.stringsearch.StringSearch#searchChars(char[], int, int,
     *      char[], Object)
     */
    public int searchChars(char[] text, int textStart, int textEnd, char[] pattern, Object processed) {

        CharIntMap td1 = (CharIntMap) processed;

        int p;

        while (textStart + pattern.length <= textEnd) {

            p = 0;

            while (p < pattern.length && pattern[p] == text[textStart + p]) {
                ++p;
            }

            if (p == pattern.length) {
                return textStart;
            }

            if (textStart + pattern.length >= textEnd) {
                return -1;
            }

            textStart += td1.get(text[textStart + pattern.length]);

        }

        return -1;

    }

}
