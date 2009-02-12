/* 
 * StringSearch.java
 * 
 * Created on 14.06.2003.
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

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * The base class for String searching implementations. String searching
 * implementations do not maintain state and are thread safe - one instance can
 * be used by as many threads as required.
 * <p>
 * Most pattern-matching algorithms pre-process the pattern to search for in
 * some way. Subclasses of StringSearch allow retrieving the pre-processed
 * pattern to save the time required to build up character tables.
 * <p>
 * Some of the Objects returned from {@link #processBytes(byte[])},
 * {@link #processChars(char[])}, {@link #processString(String)} might implement
 * the {@link java.io.Serializable} interface and enable you to serialize
 * pre-processed Objects to disk, see concrete implementations for details.
 * 
 * @author <a href="mailto:jb@eaio.com">Johann Burkard</a>
 * @version 1.2
 */
public abstract class StringSearch {

    private static final int CROSSOVER_IBM_1_3 = 68;

    private static final int CROSSOVER_SUN_PRE_1_4 = 90;

    private static final int CROSSOVER_SUN_1_4 = 12;

    /**
     * Stores if the native library should be loaded. This package comes with a
     * native library called "NativeSearch". If the system property
     * "com.eaio.stringsearch.native" is <code>null</code> (not defined) or
     * "true" (ignoring case), an attempt is always made to load the native
     * library. Any other values will prevent the native library from being
     * loaded.
     */

    /* comment:start */

    protected static boolean useNative = false;

    /* comment:end */

    /**
     * The crossover point at which the Reflection based char accessor should be
     * used - if Reflection access is allowed, of course. The crossover point is
     * set in the static initializer. If a String is longer than this value and
     * Reflection is allowed, it's <code>char</code> array will be extracted
     * through Reflection.
     */
    private static int crossover = 0;

    /**
     * The Dispatch instance.
     */
    protected static Dispatch activeDispatch;

    /**
     * The Dispatch class implements the strategy to convert Strings to
     * <code>char</code> arrays and calls the appropriate
     * <code>searchChars</code> method in the given StringSearch instance.
     */
    protected static class Dispatch {

        /**
         * Instances are created in StringSearch only.
         */
        private Dispatch() {
        }

        /**
         * Searches a pattern inside a text, using the pre-processed Object and
         * using the given StringSearch instance.
         */
        protected int searchString(String text, int textStart, int textEnd, String pattern, Object processed,
                StringSearch instance) {

            return instance.searchChars(text.toCharArray(), textStart, textEnd, pattern.toCharArray(), processed);

        }

        /**
         * Searches a pattern inside a text, using the given StringSearch
         * instance.
         */
        protected int searchString(String text, int textStart, int textEnd, String pattern, StringSearch instance) {

            return instance.searchChars(text.toCharArray(), textStart, textEnd, pattern.toCharArray());

        }

        /**
         * Searches a pattern inside a text with at most k mismatches, using the
         * given MismatchSearch instance.
         */

        /* comment:start */

        protected int[] searchString(String text, int textStart, int textEnd, String pattern, int k,
                MismatchSearch instance) {

            return instance.searchChars(text.toCharArray(), textStart, textEnd, pattern.toCharArray(), k);

        }

        /* comment:end */

        /**
         * Searches a pattern inside a text, using the pre-processed Object and
         * at most k mismatches, using the given MismatchSearch instance.
         */

        /* comment:start */

        protected int[] searchString(String text, int textStart, int textEnd, String pattern, Object processed, int k,
                MismatchSearch instance) {

            return instance.searchChars(text.toCharArray(), textStart, textEnd, pattern.toCharArray(), processed, k);

        }

        /* comment:end */

        /**
         * Returns the underlying <code>char</code> array.
         * 
         * @param s
         *            the String, may not be <code>null</code>
         * @return char[]
         */
        protected char[] charsOf(String s) {
            return s.toCharArray();
        }

    }

    /**
     * The ReflectionDispatch class is used if Reflection can be used to access
     * the underlying <code>char</code> array in Strings to avoid the cloning
     * overhead.
     */
    protected static class ReflectionDispatch extends Dispatch {

        private Field value, offset;

        /**
         * Instances are created in StringSearch only.
         * 
         * @param value
         *            the "value" field in String
         * @param offset
         *            the "offset" field in String
         */
        private ReflectionDispatch(Field value, Field offset) {
            this.value = value;
            this.offset = offset;
        }

        /**
         * @see com.eaio.stringsearch.StringSearch.Dispatch#searchString(String,
         *      int, int, String, Object, StringSearch)
         */
        protected int searchString(String text, int textStart, int textEnd, String pattern, Object processed,
                StringSearch instance) {

            int l = text.length();
            if (l > crossover) {
                try {
                    int o = offset.getInt(text);
                    char[] t = (char[]) value.get(text);
                    return instance.searchChars(t, textStart + o, textEnd + o, charsOf(pattern), processed) - o;

                } catch (IllegalAccessException ex) {
                    synchronized (activeDispatch) {
                        activeDispatch = new Dispatch();
                    }
                }
            }

            return super.searchString(text, textStart, textEnd, pattern, processed, instance);

        }

        /**
         * @see com.eaio.stringsearch.StringSearch.Dispatch#searchString(String,
         *      int, int, String, StringSearch)
         */
        protected int searchString(String text, int textStart, int textEnd, String pattern, StringSearch instance) {

            int l = text.length();
            if (l > crossover) {
                try {
                    int o = offset.getInt(text);
                    char[] t = (char[]) value.get(text);
                    return instance.searchChars(t, textStart + o, textEnd + o, charsOf(pattern)) - o;
                } catch (IllegalAccessException ex) {
                    synchronized (activeDispatch) {
                        activeDispatch = new Dispatch();
                    }
                }
            }

            return super.searchString(text, textStart, textEnd, pattern, instance);

        }

        /**
         * @see com.eaio.stringsearch.StringSearch.Dispatch#searchString(String,
         *      int, int, String, int, MismatchSearch)
         */

        /* comment:start */

        protected int[] searchString(String text, int textStart, int textEnd, String pattern, int k,
                MismatchSearch instance) {

            int l = text.length();
            if (l > crossover) {
                try {
                    int o = offset.getInt(text);
                    char[] t = (char[]) value.get(text);
                    int[] r = instance.searchChars(t, textStart + o, textEnd + o, charsOf(pattern), k);
                    if (r[0] != -1) {
                        r[0] -= o;
                    }
                    return r;
                } catch (IllegalAccessException ex) {
                    synchronized (activeDispatch) {
                        activeDispatch = new Dispatch();
                    }
                }
            }

            return super.searchString(text, textStart, textEnd, pattern, k, instance);

        }

        /* comment:end */

        /**
         * @see com.eaio.stringsearch.StringSearch.Dispatch#searchString(String,
         *      int, int, String, Object, int, MismatchSearch)
         */

        /* comment:start */

        protected int[] searchString(String text, int textStart, int textEnd, String pattern, Object processed, int k,
                MismatchSearch instance) {

            int l = text.length();
            if (l > crossover) {
                try {
                    int o = offset.getInt(text);
                    char[] t = (char[]) value.get(text);
                    int[] r = instance.searchChars(t, textStart + o, textEnd + o, charsOf(pattern), processed, k);
                    if (r[0] != -1) {
                        r[0] -= o;
                    }
                    return r;
                } catch (IllegalAccessException ex) {
                    synchronized (activeDispatch) {
                        activeDispatch = new Dispatch();
                    }
                }
            }

            return super.searchString(text, textStart, textEnd, pattern, processed, k, instance);

        }

        /* comment:end */

        /**
         * Tries to return the underlying <code>char</code> array directly. Only
         * works if the "offset" field is 0 and the "count" field is equal to
         * the String's length.
         * 
         * @see com.eaio.stringsearch.StringSearch.Dispatch#chars(java.lang.String)
         */
        protected char[] charsOf(String s) {
            int l = s.length();
            if (l > crossover) {
                try {
                    if (offset.getInt(s) != 0) {
                        return super.charsOf(s);
                    }
                    char[] c = (char[]) value.get(s);
                    if (c.length != l) {
                        return super.charsOf(s);
                    }
                    return c;
                } catch (IllegalAccessException ex) {
                    synchronized (activeDispatch) {
                        activeDispatch = new Dispatch();
                    }
                }
            }
            return super.charsOf(s);
        }

    }

    static {

        final String shortString = " ";
        shortString.hashCode(); // make sure the cached hashCode is not 0

        /*
         * Try loading the native library.
         */

        /* comment:start */

        String s = null;
        try {
            s = System.getProperty("com.eaio.stringsearch.native");
        } catch (SecurityException ex) {
        }

        if (s == null || "true".equalsIgnoreCase(s)) {
            try {
                Runtime.getRuntime().loadLibrary("NativeSearch");
                useNative = true;
            } catch (SecurityException ex) {
            } catch (UnsatisfiedLinkError ex) {
            }
        }

        /* comment:end */

        /*
         * Find out if we can use Reflection. If there's a SecurityManager in
         * place, we probably can't.
         */

        Field value = null;
        Field offset = null;

        Field[] stringFields = null;

        try {
            stringFields = shortString.getClass().getDeclaredFields();
        } catch (SecurityException ex) {
            try {
                stringFields = (Field[]) AccessController.doPrivileged(new PrivilegedAction() {

                    public Object run() {
                        return shortString.getClass().getDeclaredFields();
                    }

                });

            } catch (SecurityException ex2) {
            }
        }

        if (stringFields != null) {

            Class charArray = new char[0].getClass();

            for (int i = 0; i < stringFields.length; i++) {
                if (stringFields[i].getType() == charArray) {
                    final Field v = stringFields[i];

                    try {

                        AccessController.doPrivileged(new PrivilegedAction() {

                            public Object run() {
                                v.setAccessible(true);
                                return null;
                            }

                        });
                        value = v;
                    } catch (SecurityException ex) {
                    }
                } else if (stringFields[i].getType() == Integer.TYPE) {
                    final Field o = stringFields[i];

                    try {

                        AccessController.doPrivileged(new PrivilegedAction() {

                            public Object run() {
                                o.setAccessible(true);
                                return null;
                            }

                        });

                        if (o.getInt(shortString) == 0) {
                            offset = o;
                            break;
                        }
                    } catch (SecurityException ex) {
                    } catch (IllegalAccessException ex) {
                    }
                }
            }
        }

        if (value != null && offset != null) {

            StringSearch.activeDispatch = new ReflectionDispatch(value, offset);

            /*
             * We can use Reflection. Set the crossover point at which
             * Reflection becomes faster than cloning the char array.
             */

            /*
             * Find out the version of the virtual machine.
             */

            String vendor = System.getProperty("java.vendor");
            String version = System.getProperty("java.version");

            char v = vendor.charAt(0);
            int ver = ((int) version.charAt(2)) - 48;

            if (v == 'I') {

                /*
                 * IBM VMs
                 */

                /*
                 * Reflection in IBM 1.4.1 Linux is extremely fast, so we simply
                 * use Reflection all the time.
                 */

                if (ver < 4) {

                    /*
                     * For the 1.3 IBM SDK, the crossover point is around 68
                     * chars. At this point, String#toCharArray becomes slower
                     * than Reflection based access. Their System#arraycopy()
                     * implementation is about two times slower than the one
                     * from Sun btw.
                     */

                    crossover = CROSSOVER_IBM_1_3;

                }

            } else if (v == 'S') {

                /*
                 * Sun VMs
                 */

                if (ver > 3) {

                    /*
                     * For the 1.4 Sun SDKs, the crossover point is 12 chars. At
                     * this point, String#toCharArray becomes slower than
                     * Reflection based access. It's not String#toCharArray that
                     * is slow, it's Reflection they made so fast.
                     */

                    crossover = CROSSOVER_SUN_1_4;

                } else {

                    /*
                     * For the 1.3 Sun SDKs, the crossover point is around 90
                     * chars.
                     */

                    crossover = CROSSOVER_SUN_PRE_1_4;

                }

            }

            /* Intentionally commented out */

            // else if (v == 'B') {
            //
            // /*
            // * BEA JRockIt VMs
            // */
            //
            // /*
            // * In all BEA JRockIt VMs, Reflection access is so incredibly fast
            // that we
            // * simply use Reflection all the time.
            // */
            //
            // }
            // else if (v == 'T') {
            //
            // /*
            // * Kaffe VM (reports "Transvirtual Technologies, Inc." in
            // java.vendor)
            // */
            //
            // /*
            // * In the Kaffe VM, Reflection again is much faster than cloning,
            // so we use
            // * it all the time.
            // */
            //
            // }
            /* */

        } else {

            /*
             * No Reflection support.
             */

            StringSearch.activeDispatch = new Dispatch();

        }

    }

    /**
     * Returns if Reflection is used to access the underlying <code>char</code>
     * array in Strings.
     * 
     * @return <code>true</code> or <code>false</code>
     */
    public static boolean usesReflection() {
        return activeDispatch instanceof ReflectionDispatch;
    }

    /**
     * Constructor for StringSearch. Note that it is not required to create
     * multiple instances.
     */
    protected StringSearch() {
    }

    /**
     * Returns if this algorithm currently uses the native library - if it could
     * be loaded. If the algorithm has a different strategy concerning native
     * libraries or if it does not use the native library at all, it will return
     * <code>false</code>.
     * 
     * @return <code>true</code> or <code>false</code>
     */

    /* comment:start */

    public boolean usesNative() {
        return useNative;
    }

    /* comment:end */

    /*
     * Pre-processing methods
     */

    /**
     * Pre-processes a <code>byte</code> array.
     * 
     * @param pattern
     *            the <code>byte</code> array containing the pattern, may not be
     *            <code>null</code>
     * @return an Object
     */
    public abstract Object processBytes(byte[] pattern);

    /**
     * Pre-processes a <code>char</code> array
     * 
     * @param pattern
     *            a <code>char</code> array containing the pattern, may not be
     *            <code>null</code>
     * @return an Object
     */
    public abstract Object processChars(char[] pattern);

    /**
     * Pre-processes a String. This method should not be used directly because
     * it is implicitly called in the {@link #searchString(String, String)}
     * methods.
     * 
     * @param pattern
     *            the String containing the pattern, may not be
     *            <code>null</code>
     * @return an Object
     * @see #processChars(char[])
     */
    public final Object processString(String pattern) {
        return processChars(activeDispatch.charsOf(pattern));
    }

    /* Byte searching methods */

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            the <code>byte</code> array containing the text, may not be
     *            <code>null</code>
     * @param pattern
     *            the <code>byte</code> array containing the pattern, may not be
     *            <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchBytes(byte[], int, int, byte[], Object)
     */
    public final int searchBytes(byte[] text, byte[] pattern) {
        return searchBytes(text, 0, text.length, pattern, processBytes(pattern));
    }

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            the <code>byte</code> array containing the text, may not be
     *            <code>null</code>
     * @param pattern
     *            the pattern to search for, may not be <code>null</code>
     * @param processed
     *            an Object as returned from {@link #processBytes(byte[])}, may
     *            not be <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchBytes(byte[], int, int, byte[], Object)
     */
    public final int searchBytes(byte[] text, byte[] pattern, Object processed) {
        return searchBytes(text, 0, text.length, pattern, processed);
    }

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            the <code>byte</code> array containing the text, may not be
     *            <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param pattern
     *            the <code>byte</code> array containing the pattern, may not be
     *            <code>null</code>
     * @return int the position in the text or -1 if the pattern was not found
     * @see #searchBytes(byte[], int, int, byte[], Object)
     */
    public final int searchBytes(byte[] text, int textStart, byte[] pattern) {
        return searchBytes(text, textStart, text.length, pattern, processBytes(pattern));
    }

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            the <code>byte</code> array containing the text, may not be
     *            <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param pattern
     *            the pattern to search for, may not be <code>null</code>
     * @param processed
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchBytes(byte[], int, int, byte[], Object)
     */
    public final int searchBytes(byte[] text, int textStart, byte[] pattern, Object processed) {

        return searchBytes(text, textStart, text.length, pattern, processed);

    }

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            text the <code>byte</code> array containing the text, may not
     *            be <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param textEnd
     *            at which position in the text comparing should stop
     * @param pattern
     *            the <code>byte</code> array containing the pattern, may not be
     *            <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchBytes(byte[], int, int, byte[], Object)
     */
    public final int searchBytes(byte[] text, int textStart, int textEnd, byte[] pattern) {

        return searchBytes(text, textStart, textEnd, pattern, processBytes(pattern));

    }

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            text the <code>byte</code> array containing the text, may not
     *            be <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param textEnd
     *            at which position in the text comparing should stop
     * @param pattern
     *            the pattern to search for, may not be <code>null</code>
     * @param processed
     *            an Object as returned from {@link #processBytes(byte[])}, may
     *            not be <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #processBytes(byte[])
     */
    public abstract int searchBytes(byte[] text, int textStart, int textEnd, byte[] pattern, Object processed);

    /* Char searching methods */

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            the character array containing the text, may not be
     *            <code>null</code>
     * @param pattern
     *            the <code>char</code> array containing the pattern, may not be
     *            <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchChars(char[] text, char[] pattern) {
        return searchChars(text, 0, text.length, pattern, processChars(pattern));
    }

    /**
     * Returns the index of the pattern in the text using the pre-processed
     * Object. Returns -1 if the pattern was not found.
     * 
     * @param text
     *            the character array containing the text, may not be
     *            <code>null</code>
     * @param pattern
     *            the <code>char</code> array containing the pattern, may not be
     *            <code>null</code>
     * @param processed
     *            an Object as returned from {@link #processChars(char[])} or
     *            {@link #processString(String)}, may not be <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchChars(char[] text, char[] pattern, Object processed) {
        return searchChars(text, 0, text.length, pattern, processed);
    }

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            the character array containing the text, may not be
     *            <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param pattern
     *            the <code>char</code> array containing the pattern, may not be
     *            <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchChars(char[] text, int textStart, char[] pattern) {
        return searchChars(text, textStart, text.length, pattern, processChars(pattern));
    }

    /**
     * Returns the index of the pattern in the text using the pre-processed
     * Object. Returns -1 if the pattern was not found.
     * 
     * @param text
     *            the String containing the text, may not be <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param pattern
     *            the <code>char</code> array containing the pattern, may not be
     *            <code>null</code>
     * @param processed
     *            an Object as returned from {@link #processChars(char[])} or
     *            {@link #processString(String)}, may not be <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchChars(char[] text, int textStart, char[] pattern, Object processed) {

        return searchChars(text, textStart, text.length, pattern, processed);

    }

    /**
     * Returns the position in the text at which the pattern was found. Returns
     * -1 if the pattern was not found.
     * 
     * @param text
     *            the character array containing the text, may not be
     *            <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param textEnd
     *            at which position in the text comparing should stop
     * @param pattern
     *            the <code>char</code> array containing the pattern, may not be
     *            <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchChars(char[] text, int textStart, int textEnd, char[] pattern) {

        return searchChars(text, textStart, textEnd, pattern, processChars(pattern));

    }

    /**
     * Returns the index of the pattern in the text using the pre-processed
     * Object. Returns -1 if the pattern was not found.
     * 
     * @param text
     *            the String containing the text, may not be <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param textEnd
     *            at which position in the text comparing should stop
     * @param pattern
     *            the pattern to search for, may not be <code>null</code>
     * @param processed
     *            an Object as returned from {@link #processChars(char[])} or
     *            {@link #processString(String)}, may not be <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     */
    public abstract int searchChars(char[] text, int textStart, int textEnd, char[] pattern, Object processed);

    /* String searching methods */

    /**
     * Convenience method to search for patterns in Strings. Returns the
     * position in the text at which the pattern was found. Returns -1 if the
     * pattern was not found.
     * 
     * @param text
     *            the String containing the text, may not be <code>null</code>
     * @param pattern
     *            the String containing the pattern, may not be
     *            <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchString(String text, String pattern) {
        return searchString(text, 0, text.length(), pattern);
    }

    /**
     * Convenience method to search for patterns in Strings. Returns the
     * position in the text at which the pattern was found. Returns -1 if the
     * pattern was not found.
     * 
     * @param text
     *            the String containing the text, may not be <code>null</code>
     * @param pattern
     *            the String containing the pattern, may not be
     *            <code>null</code>
     * @param processed
     *            an Object as returned from {@link #processChars(char[])} or
     *            {@link #processString(String)}, may not be <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchString(String text, String pattern, Object processed) {
        return searchString(text, 0, text.length(), pattern, processed);
    }

    /**
     * Convenience method to search for patterns in Strings. Returns the
     * position in the text at which the pattern was found. Returns -1 if the
     * pattern was not found.
     * 
     * @param text
     *            the String containing the text, may not be <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param pattern
     *            the String containing the pattern, may not be
     *            <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchString(String text, int textStart, String pattern) {
        return searchString(text, textStart, text.length(), pattern);
    }

    /**
     * Convenience method to search for patterns in Strings. Returns the
     * position in the text at which the pattern was found. Returns -1 if the
     * pattern was not found.
     * 
     * @param text
     *            the String containing the text, may not be <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param pattern
     *            the String containing the pattern, may not be
     *            <code>null</code>
     * @param processed
     *            an Object as returned from {@link #processChars(char[])} or
     *            {@link #processString(String)}, may not be <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[], Object)
     */
    public final int searchString(String text, int textStart, String pattern, Object processed) {

        return searchString(text, textStart, text.length(), pattern, processed);

    }

    /**
     * Convenience method to search for patterns in Strings. Returns the
     * position in the text at which the pattern was found. Returns -1 if the
     * pattern was not found.
     * 
     * @param text
     *            the String containing the text, may not be <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param textEnd
     *            at which position in the text comparing should stop
     * @param pattern
     *            the String containing the pattern, may not be
     *            <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[])
     */
    public final int searchString(String text, int textStart, int textEnd, String pattern) {

        return StringSearch.activeDispatch.searchString(text, textStart, textEnd, pattern, this);

    }

    /**
     * Convenience method to search for patterns in Strings. Returns the
     * position in the text at which the pattern was found. Returns -1 if the
     * pattern was not found.
     * 
     * @param text
     *            the String containing the text, may not be <code>null</code>
     * @param textStart
     *            at which position in the text the comparing should start
     * @param textEnd
     *            at which position in the text comparing should stop
     * @param pattern
     *            the String containing the pattern, may not be
     *            <code>null</code>
     * @param processed
     *            an Object as returned from {@link #processChars(char[])} or
     *            {@link #processString(String)}, may not be <code>null</code>
     * @return the position in the text or -1 if the pattern was not found
     * @see #searchChars(char[], int, int, char[])
     */
    public final int searchString(String text, int textStart, int textEnd, String pattern, Object processed) {

        return StringSearch.activeDispatch.searchString(text, textStart, textEnd, pattern, processed, this);

    }

    /**
     * Returns if the Object's class name matches this Object's class name.
     * 
     * @param obj
     *            the other Object
     * @return if the Object is equal to this Object
     * @see java.lang.Object#equals(Object)
     */
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass().getName().equals(obj.getClass().getName());
    }

    /**
     * Returns the hashCode of the Object's Class because all instances of this
     * Class are equal.
     * 
     * @return an int
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode() {
        return getClass().getName().hashCode();
    }

    /**
     * Returns a String representation of this. Simply returns the name of the
     * Class.
     * 
     * @return a String
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return toStringBuffer(null).toString();
    }

    /**
     * Appends a String representation of this to the given {@link StringBuffer}
     * or creates a new one if none is given. This method is not
     * <code>final</code> because subclasses might want a different String
     * format.
     * 
     * @param in
     *            the StringBuffer to append to, may be <code>null</code>
     * @return a StringBuffer
     */
    public StringBuffer toStringBuffer(StringBuffer in) {
        if (in == null) {
            in = new StringBuffer();
        }
        in.append("{ ");
        int idx = getClass().getName().lastIndexOf(".");
        if (idx > -1) {
            in.append(getClass().getName().substring(++idx));
        } else {
            in.append(getClass().getName());
        }
        in.append(" }");
        return in;
    }

    /* Utility methods */

    /**
     * Returns a {@link CharIntMap} of the extent of the given pattern, using no
     * default value.
     * 
     * @param pattern
     *            the pattern
     * @return a CharIntMap
     * @see CharIntMap#CharIntMap(int, char)
     */
    protected CharIntMap createCharIntMap(char[] pattern) {
        return createCharIntMap(pattern, 0);
    }

    /**
     * Returns a {@link CharIntMap} of the extent of the given pattern, using
     * the specified default value.
     * 
     * @param pattern
     *            the pattern
     * @param defaultValue
     *            the default value
     * @return a CharIntMap
     * @see CharIntMap#CharIntMap(int, char, int)
     */
    protected CharIntMap createCharIntMap(char[] pattern, int defaultValue) {
        char min = Character.MAX_VALUE;
        char max = Character.MIN_VALUE;
        for (int i = 0; i < pattern.length; i++) {
            max = max > pattern[i] ? max : pattern[i];
            min = min < pattern[i] ? min : pattern[i];
        }
        return new CharIntMap(max - min + 1, min, defaultValue);
    }

    /**
     * Converts the given <code>byte</code> to an <code>int</code>.
     * 
     * @param idx
     *            the byte
     * @return an int
     */
    protected final int index(byte idx) {
        return (idx < 0) ? 256 + idx : idx;
    }

    /* Utility methods */

}
