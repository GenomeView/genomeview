/*
 * BoyerMooreHorspoolRaita.java
 * 
 * Created on 15.09.2003
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
 * An implementation of Raita's enhancement to the Boyer-Moore-Horspool String
 * searching algorithm. See "Tuning the Boyer-Moore-Horspool string searching
 * algorithm" (appeared in <em>Software - Practice & Experience,
 * 22(10):879-884</em>). <br>
 * <br>
 * This algorithm is slightly faster than the
 * {@link com.eaio.stringsearch.BoyerMooreHorspool} algorithm for the
 * <code>searchChars</code> and <code>searchString</code> methods. It's
 * <code>searchBytes</code> methods are slightly slower.
 * 
 * <pre>
 * Preprocessing: O(m + &sum;) time
 * 
 * Processing   : O(mn) worst case
 * </pre>
 * 
 * @see <a * href="http://www.cs.ubc.ca/local/reading/proceedings/spe91-95/spe/vol22/issue10/spe787tr.pdf"
 *      > * http://www.cs.ubc.ca/local/reading/proceedings/spe91-95/spe/vol22/
 *      issue10/spe787tr.pdf * < /a>
 * @author <a href="mailto:jb@eaio.com">Johann Burkard</a>
 * @version 1.2
 */
public class BoyerMooreHorspoolRaita extends /* comment:start */
BoyerMooreHorspool /* comment:end *//*
                                     * comment:remove StringSearch
                                     * comment:remove
                                     */{

    /**
     * Constructor for BoyerMooreHorspoolRaita. Note that it is not required to
     * create multiple instances.
     */
    public BoyerMooreHorspoolRaita() {
    }

    /**
     * @see com.eaio.stringsearch.StringSearch#searchBytes(byte[], int, int,
     *      byte[], java.lang.Object)
     */
    public int searchBytes(byte[] text, int textStart, int textEnd, byte[] pattern, Object processed) {

        /* comment:start */

        return useNative ? nativeSearchBytes(text, textStart, textEnd, pattern, processed) : javaSearchBytes(text,
                textStart, textEnd, pattern, processed);

    }

    private int javaSearchBytes(byte[] text, int textStart, int textEnd, byte[] pattern, Object processed) {

        /* comment:end */

        int[] b = (int[]) processed;

        int i, j, k, mMinusOne;
        byte last, first;

        i = pattern.length - 1;
        mMinusOne = pattern.length - 2;

        last = pattern[pattern.length - 1];
        first = pattern[0];

        i += textStart;

        while (i < textEnd) {

            if (text[i] == last) {

                if (text[i - (pattern.length - 1)] == first) {

                    k = i - 1;
                    j = mMinusOne;

                    while (k > -1 && j > -1 && text[k] == pattern[j]) {
                        --k;
                        --j;
                    }
                    if (j == -1) {
                        return k + 1;
                    }
                }
            }

            i += b[index(text[i])];
        }

        return -1;

    }

    /* comment:start */

    private native int nativeSearchBytes(byte[] text, int textStart, int textEnd, byte[] pattern, Object processed);

    /* comment:end */

    /**
     * @see com.eaio.stringsearch.StringSearch#searchChars(char[], int, int,
     *      char[], Object)
     */
    public int searchChars(char[] text, int textStart, int textEnd, char[] pattern, Object processed) {

        CharIntMap m = (CharIntMap) processed;

        int i, j, k, mMinusOne;
        char last, first;

        i = pattern.length - 1;
        mMinusOne = i - 1;

        last = pattern[i];
        first = pattern[0];

        i += textStart;

        while (i < textEnd) {

            if (text[i] == last) {

                if (text[i - (pattern.length - 1)] == first) {

                    k = i - 1;
                    j = mMinusOne;

                    while (k > -1 && j > -1 && text[k] == pattern[j]) {
                        --k;
                        --j;
                    }
                    if (j == -1) {
                        return k + 1;
                    }
                }
            }
            i += m.get(text[i]);
        }

        return -1;

    }

    /*
     * comment:remove
     * 
     * public Object processBytes(byte[] pattern) { int[] skip = new int[256];
     * 
     * for (int i = 0; i < skip.length; ++i) { skip[i] = pattern.length; }
     * 
     * for (int i = 0; i < pattern.length - 1; ++i) { skip[index(pattern[i])] =
     * pattern.length - i - 1; }
     * 
     * return skip; }
     * 
     * public Object processChars(char[] pattern) { CharIntMap skip =
     * createCharIntMap(pattern, pattern.length);
     * 
     * for (int i = 0; i < pattern.length - 1; ++i) { skip.set(pattern[i],
     * pattern.length - i - 1); }
     * 
     * return skip; }
     * 
     * comment:remove
     */

}
