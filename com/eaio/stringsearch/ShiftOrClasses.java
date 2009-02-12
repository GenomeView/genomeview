/*
 * ShiftOrClassesTest.java
 * 
 * Created on 28.10.2003
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
 * An implementation of the Shift-Or algorithm that supports character classes.
 * The following character classes are supported:
 * <br><br>
 * <table style="border: 1px solid #ccc" cellpadding="4">
 * <tr>
 * <td><em>x</em></td>
 * <td>a character from the Alphabet &Sigma;</td>
 * </tr>
 * <tr>
 * <td><em>?</em></td>
 * <td>a &quot;don't care&quot; symbol which matches all symbols</td>
 * </tr>
 * <tr>
 * <td>[<em>characters</em>]</td>
 * <td>a class of characters where ranges (a-z, 0-9) are allowed</td>
 * </tr>
 * <tr>
 * <td><em>^</em></td>
 * <td>the negation of a class(^a, ^[abc], ^[c-h])</td>
 * </tr>
 * <tr>
 * <td>\</td>
 * <td>escapes the next character (\ must be written as \\ in Java).</td>
 * </table>
 * <br>
 * Examples:
 * <br><br>
 * <ul>
 * <li><code>mpeg</code> will obviously match mpeg, and not mpag</li>
 * <li><code>mp?g</code> will match mpeg, mp7g, mpog, but not mpg (see
 * {@link com.eaio.stringsearch.ShiftOrWildcards})</li>
 * <li><code>mp^ag</code> will match mpeg, mpog, mpcg, but not mpag</li>
 * <li><code>mpe^</code> will only match mpe^</li>
 * <li><code>mp\^ag ("mp\\^ag" in Java)</code> will match mp^ag, but not
 * mpeg</li>
 * <li><code>mp[aeiou]g</code> will match mpeg, mpug, but not mptg</li>
 * <li><code>mp^[a-k]g</code> will match mpog, mpzg, but not mpeg</li>
 * <li><code>mp[u-a]g</code> will match mpeg, too</li>
 * <li><code>mp^?g</code> will match mpeg</li>
 * <li><code>mp^[]g</code> will match mpeg, too (negation of the empty
 * class == all characters)</li>
 * </ul>
 * <pre>
 * Preprocessing: O(2n + &sum;) time
 * 
 * Searching    : O(mn / log n) (worst case and average)
 * </pre>
 * 
 * @see com.eaio.stringsearch.ShiftOr
 * @author <a href="mailto:jb@eaio.de">Johann Burkard</a>
 * @version 1.2
 */
public class ShiftOrClasses extends ShiftOr {

 /**
  * Constructor for ShiftOrClasses. Note that it is not required to create
  * multiple instances.
  */
 public ShiftOrClasses() {}

 /**
  * @see ShiftOr#processBytes(byte[])
  */
 public Object processBytes(byte[] pattern) {
  int j = ~0;

  int offset = 0;

  for (int i = 0;(i < pattern.length) && (offset < 31); i++) {
   if (pattern[i] == '\\') {
    ++i;
    ++offset;
   }
   else if (pattern[i] == '?') {
    j -= 1 << offset++;
   }
   else if (pattern[i] == '^' && i < pattern.length - 1 && offset < 31) {

    /* Negate a bit only if there are more characters coming up */

    if (pattern[i + 1] == '?') {
     ++i;
     j -= 1 << offset++;
    }
    j -= 1 << offset;

   }
   else if (pattern[i] == '[' && i < pattern.length - 1 && offset < 31) {

    /* Process patterns only if there are more characters */

    while (i < pattern.length /* && offset < 31 */
     && pattern[i] != ']') {
     if (pattern[i] == '\\') {
      ++i;
     }
     ++i;
    }
    ++offset;

   }
   else {
    ++offset;
   }
  }

  int[] t = new int[256];

  for (int i = 0; i < t.length; ++i) {
   t[i] = j;
  }

  offset = 0;

  boolean negate = false;

  for (int i = 0;(i < pattern.length) && (offset < 31); i++) {
   if (pattern[i] == '\\') {
    ++i;
    if (i < pattern.length - 1 && offset < 31) {
     t[index(pattern[i])] &= ~(1 << offset++);
    }
   }
   else if (pattern[i] == '?') {
    for (int l = 0; l < t.length; ++l) {
     t[l] &= ~(1 << offset);
    }
    ++offset;
   }
   else if (pattern[i] == '^') {
    if (i < pattern.length - 1 && offset < 31) {
     byte next = pattern[i + 1];
     if (next == '[') {
      negate = true;
     }
     else {
      for (int l = 0; l < t.length; ++l) {
       if (l != next) {
        t[l] &= ~(1 << offset);
       }
       else {
        t[l] |= (1 << offset);
       }
      }
      ++i;
      ++offset;
     }
    }
    else {
     t[index(pattern[i])] &= ~(1 << offset++);
    }
   }
   else if (pattern[i] == '[') {

    boolean end = false;

    if (i < pattern.length - 1 && offset < 31) {

     byte low, high;

     do {
      low = pattern[++i];

      if (low == '\\') {
       ++i;
       if (i < pattern.length && offset < 31) {
        low = pattern[i];
       }
      }

      if (negate) {
       t[low] |= 1 << offset;
      }
      else {
       t[low] &= ~(1 << offset);
      }

      if (i < pattern.length - 2
       && offset < 31
       && pattern[i + 1] == '-'
       && pattern[i + 2] != ']') {

       i += 2;
       high = pattern[i];

       /* handle [a-\\]] */

       if (high == '\\' && i < pattern.length - 1) {
        high = pattern[++i];
       }

       char highest = (char) Math.max(low, high);
       char lowest = (char) Math.min(low, high);

       for (; lowest <= highest; ++lowest) {
        if (negate) {
         t[lowest] |= 1 << offset;
        }
        else {
         t[lowest] &= ~(1 << offset);
        }
       }

      }

      if (i < pattern.length - 1 && offset < 31) {
       if (pattern[i + 1] == ']') {
        end = true;
       }
      }

     }
     while (i < pattern.length - 1 && offset < 31 && low != ']' && !end);
     ++i;
     ++offset;
    }

    else {
     if (negate) {
      t[index(pattern[i])] |= 1 << offset++;
     }
     else {
      t[index(pattern[i])] &= ~(1 << offset++);
     }
    }

    negate = false;

   }
   else {
    t[index(pattern[i])] &= ~(1 << offset++);
   }
  }

  return new Object[] { t, new Integer(offset)};
 }

 /**
  * @see ShiftOr#processChars(char[])
  */
 public Object processChars(char[] pattern) {
  int j = ~0;

  int offset = 0;

  for (int i = 0;(i < pattern.length) && (offset < 31); i++) {
   if (pattern[i] == '\\') {
    ++i;
    ++offset;
   }
   else if (pattern[i] == '?') {
    j -= 1 << offset++;
   }
   else if (pattern[i] == '^' && i < pattern.length - 1 && offset < 31) {

    /* Negate a bit only if there are more characters coming up */

    if (pattern[i + 1] == '?') {
     ++i;
     j -= 1 << offset++;
    }
    j -= 1 << offset;

   }
   else if (pattern[i] == '[' && i < pattern.length - 1 && offset < 31) {

    /* Process patterns only if there are more characters */

    while (i < pattern.length /* && offset < 31 */
     && pattern[i] != ']') {
     if (pattern[i] == '\\') {
      ++i;
     }
     ++i;
    }
    ++offset;

   }
   else {
    ++offset;
   }
  }

  CharIntMap m = createCharIntMap(pattern, j);

  offset = 0;

  boolean negate = false;

  for (int i = 0;(i < pattern.length) && (offset < 31); i++) {
   if (pattern[i] == '\\') {
    ++i;
    if (i < pattern.length - 1 && offset < 31) {
     m.set(pattern[i], m.get(pattern[i]) & ~(1 << offset++));
    }
   }
   else if (pattern[i] == '?') {
    char h = m.getHighest();
    for (char c = m.getLowest(); c < h; c++) {
     m.set(c, m.get(c) & ~(1 << offset));
    }
    ++offset;
   }
   else if (pattern[i] == '^') {
    if (i < pattern.length - 1 && offset < 31) {
     char next = pattern[i + 1];
     if (next == '[') {
      negate = true;
     }
     else {
      char h = m.getHighest();
      for (char c = m.getLowest(); c < h; c++) {
       if (c != next) {
        m.set(c, m.get(c) & ~(1 << offset));
       }
       else {
        m.set(c, m.get(c) | (1 << offset));
       }
      }
      ++i;
      ++offset;
     }
    }
    else {
     m.set(pattern[i], m.get(pattern[i]) & ~(1 << offset++));
    }
   }
   else if (pattern[i] == '[') {

    boolean end = false;

    if (i < pattern.length - 1 && offset < 31) {

     char low, high;

     do {
      low = pattern[++i];

      if (low == '\\') {
       ++i;
       if (i < pattern.length && offset < 31) {
        low = pattern[i];
       }
      }

      if (negate) {
       m.set(low, m.get(low) | 1 << offset);
      }
      else {
       m.set(low, m.get(low) & ~(1 << offset));
      }

      if (i < pattern.length - 2
       && offset < 31
       && pattern[i + 1] == '-'
       && pattern[i + 2] != ']') {

       i += 2;
       high = pattern[i];

       /* handle [a-\\]] */

       if (high == '\\' && i < pattern.length - 1) {
        high = pattern[++i];
       }

       char highest = (char) Math.max(low, high);
       char lowest = (char) Math.min(low, high);

       for (; lowest <= highest; ++lowest) {
        if (negate) {
         m.set(lowest, m.get(lowest) | 1 << offset);
        }
        else {
         m.set(lowest, m.get(lowest) & ~(1 << offset));
        }
       }

      }

      if (i < pattern.length - 1 && offset < 31) {
       if (pattern[i + 1] == ']') {
        end = true;
       }
      }

     }
     while (i < pattern.length - 1 && offset < 31 && low != ']' && !end);
     ++i;
     ++offset;
    }

    else {
     if (negate) {
      m.set(pattern[i], m.get(pattern[i]) | 1 << offset++);
     }
     else {
      m.set(pattern[i], m.get(pattern[i]) & ~(1 << offset++));
     }
    }

    negate = false;

   }
   else {
    m.set(pattern[i], m.get(pattern[i]) & ~(1 << offset++));
   }
  }

  return new Object[] { m, new Integer(offset)};
 }

}
