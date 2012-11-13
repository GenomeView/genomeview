/*****************************************************************************
 * Java Plug-in Framework (JPF)
 * Copyright (C) 2004-2007 Dmitry Olshansky
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *****************************************************************************/
package org.java.plugin.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Input/Output, File and URL/URI related utilities.
 *
 * @version $Id: IoUtil.java,v 1.9 2007/04/17 17:39:52 ddimon Exp $
 */
public final class IoUtil {
    private static final String PACKAGE_NAME = "org.java.plugin.util"; //$NON-NLS-1$
    
    /**
     * Copies one file, existing file will be overridden.
     * @param src source file to copy FROM
     * @param dest destination file to copy TO
     * @throws IOException if any I/O error has occurred
     */
    public static void copyFile(final File src, final File dest)
            throws IOException {
        if (!src.isFile()) {
            throw new IOException(
                    ResourceManager.getMessage(PACKAGE_NAME, "notAFile", src)); //$NON-NLS-1$
        }
        if (dest.isDirectory()) {
            throw new IOException(
                    ResourceManager.getMessage(PACKAGE_NAME, "isFolder", dest)); //$NON-NLS-1$
        }
        BufferedInputStream in = new BufferedInputStream(
                new FileInputStream(src));
        try {
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(dest, false));
            try {
                copyStream(in, out, 1024);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        dest.setLastModified(src.lastModified());
    }

    /**
     * Copies folder recursively, existing files will be overridden
     * @param src source folder
     * @param dest target folder
     * @throws IOException if any I/O error has occurred
     */
    public static void copyFolder(final File src, final File dest)
            throws IOException {
        copyFolder(src, dest, true, false, null);
    }

    /**
     * Copies folder, existing files will be overridden
     * @param src source folder
     * @param dest target folder
     * @param recursive if <code>true</code>, processes folder recursively
     * @throws IOException if any I/O error has occurred
     */
    public static void copyFolder(final File src, final File dest,
            final boolean recursive) throws IOException {
        copyFolder(src, dest, recursive, false, null);
    }

    /**
     * Copies folder.
     * @param src source folder
     * @param dest target folder
     * @param recursive if <code>true</code>, processes folder recursively
     * @param onlyNew if <code>true</code>, target file will be overridden if it
     *                is older than source file only
     * @throws IOException if any I/O error has occurred
     */
    public static void copyFolder(final File src, final File dest,
            final boolean recursive, final boolean onlyNew) throws IOException {
        copyFolder(src, dest, recursive, onlyNew, null);
    }
    
    /**
     * Copies folder.
     * @param src source folder
     * @param dest target folder
     * @param recursive if <code>true</code>, processes folder recursively
     * @param onlyNew if <code>true</code>, target file will be overridden if it
     *                is older than source file only
     * @param filter file filter, optional, if <code>null</code> all files will
     *               be copied
     * @throws IOException if any I/O error has occurred
     */
    public static void copyFolder(final File src, final File dest,
            final boolean recursive, final boolean onlyNew,
            final FileFilter filter) throws IOException {
        if (!src.isDirectory()) {
            throw new IOException(
                    ResourceManager.getMessage(PACKAGE_NAME,
                            "notAFolder", src)); //$NON-NLS-1$
        }
        if (dest.isFile()) {
            throw new IOException(
                    ResourceManager.getMessage(PACKAGE_NAME, "isFile", dest)); //$NON-NLS-1$
        }
        if (!dest.exists() && !dest.mkdirs()) {
            throw new IOException(
                    ResourceManager.getMessage(PACKAGE_NAME,
                            "cantMakeFolder", dest)); //$NON-NLS-1$
        }
        File[] srcFiles =
            (filter != null) ? src.listFiles(filter) : src.listFiles();
        for (int i = 0; i < srcFiles.length; i++) {
            File file = srcFiles[i];
            if (file.isDirectory()) {
                if (recursive) {
                    copyFolder(file, new File(dest, file.getName()), recursive,
                            onlyNew, filter);
                }
                continue;
            }
            File destFile = new File(dest, file.getName());
            if (onlyNew && destFile.isFile()
                    && (destFile.lastModified() > file.lastModified())) {
                continue;
            }
            copyFile(file, destFile);
        }
        dest.setLastModified(src.lastModified());
    }
    
    /**
     * Copies streams.
     * @param in source stream
     * @param out destination stream
     * @param bufferSize buffer size to use
     * @throws IOException if any I/O error has occurred
     */
    public static void copyStream(final InputStream in, final OutputStream out,
            final int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }
    
    /**
     * Recursively deletes whole content of the given folder.
     * @param folder folder to be emptied
     * @return <code>true</code> if given folder becomes empty or not exists
     */
    public static boolean emptyFolder(final File folder) {
        if (!folder.isDirectory()) {
            return true;
        }
        File[] files = folder.listFiles();
        boolean result = true;
        for (File file : files)
        {
            if (file.isDirectory()) {
                if (emptyFolder(file)) {
                    result &= file.delete();
                } else {
                    result = false;
                }
            } else {
                result &= file.delete();
            }
        }
        return result;
    }
    
    /**
     * Compares two files for directories/files synchronization purposes.
     * @param file1 one file to compare
     * @param file2 another file to compare
     * @return <code>true</code> if file names are equal (case sensitive), files
     *         have equal lengths and modification dates (milliseconds ignored)
     * 
     * @see #synchronizeFolders(File, File)
     * @see #compareFileDates(Date, Date)
     */
    public static boolean compareFiles(final File file1, final File file2) {
        if (!file1.isFile() || !file2.isFile()) {
            return false;
        }
        if (!file1.getName().equals(file2.getName())) {
            return false;
        }
        if (file1.length() != file2.length()) {
            return false;
        }
        return compareFileDates(new Date(file1.lastModified()),
                new Date(file2.lastModified()));
    }
    
    /**
     * For some reason modification milliseconds for some files are unstable,
     * use this function to compare file dates ignoring milliseconds.
     * @param date1 first file modification date
     * @param date2 second file modification date
     * @return <code>true</code> if files modification dates are equal ignoring
     *         milliseconds
     */
    public static boolean compareFileDates(final Date date1, final Date date2) {
        if ((date1 == null) || (date2 == null)) {
            return false;
        }
        Calendar cldr = Calendar.getInstance(Locale.ENGLISH);
        cldr.setTime(date1);
        cldr.set(Calendar.MILLISECOND, 0);
        long dt1 = cldr.getTimeInMillis();
        cldr.setTime(date2);
        cldr.set(Calendar.MILLISECOND, 0);
        long dt2 = cldr.getTimeInMillis();
        return dt1 == dt2;
    }
    
    /**
     * Performs one-way directories synchronization comparing files only,
     * not folders.
     * @param src source folder
     * @param dest target folder
     * @throws IOException if any I/O error has occurred
     * 
     * @see #synchronizeFolders(File, File, FileFilter)
     * @see #compareFiles(File, File)
     */
    public static void synchronizeFolders(final File src, final File dest)
            throws IOException {
        synchronizeFolders(src, dest, null);
    }
    
    /**
     * Performs one-way directories synchronization comparing files only,
     * not folders.
     * @param src source folder
     * @param dest target folder
     * @param filter file filter, optional, if <code>null</code> all files will
     *               be included into synchronization process
     * @throws IOException if any I/O error has occurred
     * 
     * @see #compareFiles(File, File)
     */
    public static void synchronizeFolders(final File src, final File dest,
            final FileFilter filter) throws IOException {
        if (!src.isDirectory()) {
            throw new IOException(
                    ResourceManager.getMessage(PACKAGE_NAME,
                            "notAFolder", src)); //$NON-NLS-1$
        }
        if (dest.isFile()) {
            throw new IOException(
                    ResourceManager.getMessage(PACKAGE_NAME, "isFile", dest)); //$NON-NLS-1$
        }
        if (!dest.exists() && !dest.mkdirs()) {
            throw new IOException(
                    ResourceManager.getMessage(PACKAGE_NAME,
                            "cantMakeFolder", dest)); //$NON-NLS-1$
        }
        File[] srcFiles =
            (filter != null) ? src.listFiles(filter) : src.listFiles();
        for (File srcFile : srcFiles) {
            File destFile = new File(dest, srcFile.getName());
            if (srcFile.isDirectory()) {
                if (destFile.isFile() && !destFile.delete()) {
                    throw new IOException(
                            ResourceManager.getMessage(PACKAGE_NAME,
                                    "cantDeleteFile", destFile)); //$NON-NLS-1$
                }
                synchronizeFolders(srcFile, destFile, filter);
                continue;
            }
            if (compareFiles(srcFile, destFile)) {
                continue;
            }
            copyFile(srcFile, destFile);
        }
        File[] destFiles = dest.listFiles();
        for (int i = 0; i < destFiles.length; i++) {
            File destFile = destFiles[i];
            File srcFile = new File(src, destFile.getName());
            if (((filter != null)
                    && filter.accept(destFile) && srcFile.exists())
                    || ((filter == null) && srcFile.exists())) {
                continue;
            }
            if (destFile.isDirectory() && !emptyFolder(destFile)) {
                throw new IOException(
                        ResourceManager.getMessage(PACKAGE_NAME,
                                "cantEmptyFolder", destFile)); //$NON-NLS-1$
            }
            if (!destFile.delete()) {
                throw new IOException(
                        ResourceManager.getMessage(PACKAGE_NAME,
                                "cantDeleteFile", destFile)); //$NON-NLS-1$
            }
        }
        dest.setLastModified(src.lastModified());
    }

    /**
     * Checks if resource exist and can be opened.
     * @param url absolute URL which points to a resource to be checked
     * @return <code>true</code> if given URL points to an existing resource
     */
    public static boolean isResourceExists(final URL url) {
        File file = url2file(url);
        if (file != null) {
            return file.canRead();
        }
        if ("jar".equalsIgnoreCase(url.getProtocol())) { //$NON-NLS-1$
            return isJarResourceExists(url);
        }
        return isUrlResourceExists(url);
    }
    
    private static boolean isUrlResourceExists(final URL url) {
        try {
            //url.openConnection().connect();
            // Patch from Sebastian Kopsan
            InputStream is = url.openStream();
            try {
                is.close();
            } catch (IOException ioe) {
                // ignore
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }
    
    private static boolean isJarResourceExists(final URL url) {
        try {
            String urlStr = url.toExternalForm();
            int p = urlStr.indexOf("!/"); //$NON-NLS-1$
            if (p == -1) {// this is invalid JAR file URL
                return false;
            }
            URL fileUrl = new URL(urlStr.substring(4, p));
            File file = url2file(fileUrl);
            if (file == null) {// this is non-local JAR file URL
                return isUrlResourceExists(url);
            }
            if (!file.canRead()) {
                return false;
            }
            if (p == urlStr.length() - 2) {// URL points to the root entry of JAR file
                return true;
            }
            JarFile jarFile = new JarFile(file);
            try {
                return jarFile.getEntry(urlStr.substring(p + 2)) != null;
            } finally {
                jarFile.close();
            }
        } catch (IOException ioe) {
            return false;
        }
    }
    
    /**
     * Opens input stream for given resource. This method behaves differently
     * for different URL types:
     * <ul>
     *   <li>for <b>local files</b> it returns buffered file input stream;</li>
     *   <li>for <b>local JAR files</b> it reads resource content into memory
     *     buffer and returns byte array input stream that wraps those
     *     buffer (this prevents locking JAR file);</li>
     *   <li>for <b>common URL's</b> this method simply opens stream to that URL
     *     using standard URL API.</li>
     * </ul>
     * It is not recommended to use this method for big resources within JAR
     * files.
     * @param url resource URL
     * @return input stream for given resource
     * @throws IOException if any I/O error has occurred
     */
    public static InputStream getResourceInputStream(final URL url)
            throws IOException {
        File file = url2file(url);
        if (file != null) {
            return new BufferedInputStream(new FileInputStream(file));
        }
        if (!"jar".equalsIgnoreCase(url.getProtocol())) { //$NON-NLS-1$
            return url.openStream();
        }
        String urlStr = url.toExternalForm();
        if (urlStr.endsWith("!/")) { //$NON-NLS-1$
            //JAR URL points to a root entry
            throw new FileNotFoundException(url.toExternalForm());
        }
        int p = urlStr.indexOf("!/"); //$NON-NLS-1$
        if (p == -1) {
            throw new MalformedURLException(url.toExternalForm());
        }
        String path = urlStr.substring(p + 2);
        file = url2file(new URL(urlStr.substring(4, p)));
        if (file == null) {// non-local JAR file URL
            return url.openStream();
        }
        JarFile jarFile = new JarFile(file);
        try {
            ZipEntry entry = jarFile.getEntry(path);
            if (entry == null) {
                throw new FileNotFoundException(url.toExternalForm());
            }
            InputStream in = jarFile.getInputStream(entry);
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                copyStream(in, out, 1024);
                return new ByteArrayInputStream(out.toByteArray());
            } finally {
                in.close();
            }
        } finally {
            jarFile.close();
        }
    }

    /**
     * Utility method to convert local URL to a {@link File} object.
     * @param url an URL
     * @return file object for given URL or <code>null</code> if URL is not
     *         local
     */
    @SuppressWarnings("deprecation")
    public static File url2file(final URL url) {
        String prot = url.getProtocol();
        if ("jar".equalsIgnoreCase(prot)) { //$NON-NLS-1$
            if (url.getFile().endsWith("!/")) { //$NON-NLS-1$
                String urlStr = url.toExternalForm();
                try {
                    return url2file(
                            new URL(urlStr.substring(4, urlStr.length() - 2)));
                } catch (MalformedURLException mue) {
                    // ignore
                }
            }
            return null;
        }
        if (!"file".equalsIgnoreCase(prot)) { //$NON-NLS-1$
            return null;
        }
        try {
            // Method URL.toURI() may produce URISyntaxException for some
            // "valid" URL's that contain spaces or other "illegal" characters.
            //return new File(url.toURI());
            return new File(URLDecoder.decode(url.getFile(), "UTF-8")); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return new File(URLDecoder.decode(url.getFile()));
        }
    }
    
    /**
     * Utility method to convert a {@link File} object to a local URL.
     * @param file a file object
     * @return absolute URL that points to the given file
     * @throws MalformedURLException if file can't be represented as URL for
     *         some reason
     */
    public static URL file2url(final File file) throws MalformedURLException {
        try {
            return file.getCanonicalFile().toURI().toURL();
        } catch (MalformedURLException mue) {
            throw mue;
        } catch (IOException ioe) {
            throw new MalformedURLException(
                    ResourceManager.getMessage(PACKAGE_NAME, "file2urlFailed", //$NON-NLS-1$
                            new Object[] {file, ioe}));
        }
    }

    private IoUtil() {
        // no-op
    }
}
