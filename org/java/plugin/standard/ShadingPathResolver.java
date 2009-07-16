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
package org.java.plugin.standard;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.UniqueIdentity;
import org.java.plugin.util.ExtendedProperties;
import org.java.plugin.util.IoUtil;

/**
 * This implementation of path resolver makes "shadow copy" of plug-in resources
 * before resolving paths to them, this helps avoid locking of local resources
 * and run native code from remote locations.
 * <p>
 * <b>Configuration parameters</b>
 * </p>
 * <p>
 * This path resolver implementation supports following configuration
 * parameters:
 * <dl>
 *   <dt>shadowFolder</dt>
 *   <dd>Path to the folder where to copy resources to prevent their locking. By
 *     default this will be
 *     <code>System.getProperty("java.io.tmpdir") + "/.jpf-shadow"</code>.
 *     Please note that this folder will be maintained automatically by the
 *     Framework and might be cleared without any confirmation or notification.
 *     So it is strongly not recommended to use plug-ins folder (or other
 *     sensitive application directory) as shadow folder, this may lead to
 *     losing your data.</dd>
 *   <dt>unpackMode</dt>
 *   <dd>If <code>always</code>, "JAR'ed" or "ZIP'ed" plug-ins will be
 *     un-compressed to the shadow folder, if <code>never</code>, they will be
 *     just copied, if <code>smart</code>, the processing depends on plug-in
 *     content - if plug-in contains JAR libraries, it will be un-packed,
 *     otherwise just copied to shadow folder. It is also possible to add
 *     boolean "unpack" attribute to plug-in manifest, in this case, it's value
 *     will be taken into account. The default parameter value is
 *     <code>smart</code>.</dd>
 * </dl>
 * </p>
 *
 * @version $Id: ShadingPathResolver.java,v 1.5 2007/05/13 16:31:48 ddimon Exp $
 */
public class ShadingPathResolver extends StandardPathResolver {
    private static final String UNPACK_MODE_ALWAIS = "always"; //$NON-NLS-1$
    private static final String UNPACK_MODE_NEVER = "never"; //$NON-NLS-1$
    private static final String UNPACK_MODE_SMART = "smart"; //$NON-NLS-1$
    
    private File shadowFolder;
    private String unpackMode;
    private Map<String, URL> shadowUrlMap = new HashMap<String, URL>(); // <pluginId or fragmentId, shadow URL>
    private Map<String, Boolean> unpackModeMap = new HashMap<String, Boolean>(); // <pluginId or fragmentId, Boolean>
    private ShadowDataController controller;

    /**
     * @see org.java.plugin.PathResolver#configure(ExtendedProperties)
     */
    @Override
    public synchronized void configure(final ExtendedProperties config)
            throws Exception {
        super.configure(config);
        String folder = config.getProperty("shadowFolder"); //$NON-NLS-1$
        if ((folder != null) && (folder.length() > 0)) {
            try {
                shadowFolder = new File(folder).getCanonicalFile();
            } catch (IOException ioe) {
                log.warn("failed initializing shadow folder " + folder //$NON-NLS-1$
                        + ", falling back to the default folder", ioe); //$NON-NLS-1$
            }
        }
        if (shadowFolder == null) {
            shadowFolder = new File(System.getProperty("java.io.tmpdir"), //$NON-NLS-1$
                    ".jpf-shadow"); //$NON-NLS-1$
        }
        log.debug("shadow folder is " + shadowFolder); //$NON-NLS-1$
        if (!shadowFolder.exists()) {
            shadowFolder.mkdirs();
        }
        unpackMode = config.getProperty("unpackMode", UNPACK_MODE_SMART); //$NON-NLS-1$
        log.debug("unpack mode parameter value is " + unpackMode); //$NON-NLS-1$
        controller = ShadowDataController.init(shadowFolder,
                buildFileFilter(config));
        log.info("configured, shadow folder is " + shadowFolder); //$NON-NLS-1$
    }
    
    private FileFilter buildFileFilter(final ExtendedProperties config) {
        final FileFilter includesFilter;
        String patterns = config.getProperty("includes"); //$NON-NLS-1$
        if ((patterns != null) && (patterns.trim().length() > 0)) {
            includesFilter = new RegexpFileFilter(patterns);
        } else {
            includesFilter = null;
        }
        final FileFilter excludesFilter;
        patterns = config.getProperty("excludes"); //$NON-NLS-1$
        if ((patterns != null) && (patterns.trim().length() > 0)) {
            excludesFilter = new RegexpFileFilter(patterns);
        } else {
            excludesFilter = null;
        }
        if ((excludesFilter == null) && (includesFilter == null)) {
            return null;
        }
        return new CombinedFileFilter(includesFilter, excludesFilter);
    }

    /**
     * @see org.java.plugin.standard.StandardPathResolver#registerContext(
     *      org.java.plugin.registry.Identity, java.net.URL)
     */
    @Override
    public void registerContext(Identity idt, URL url) {
        super.registerContext(idt, url);
        Boolean mode;
        if (UNPACK_MODE_ALWAIS.equalsIgnoreCase(unpackMode)) {
            mode = Boolean.TRUE;
        } else if (UNPACK_MODE_NEVER.equalsIgnoreCase(unpackMode)) {
            mode = Boolean.FALSE;
        } else {
            PluginDescriptor descr = null;
            PluginFragment fragment = null;
            if (idt instanceof PluginDescriptor) {
                descr = (PluginDescriptor) idt;
            } else if (idt instanceof PluginFragment) {
                fragment = (PluginFragment) idt;
                descr = fragment.getRegistry().getPluginDescriptor(
                        fragment.getPluginId());
            } else if (idt instanceof PluginElement) {
                PluginElement<?> element = (PluginElement) idt;
                descr = element.getDeclaringPluginDescriptor();
                fragment = element.getDeclaringPluginFragment();
            } else {
                throw new IllegalArgumentException("unknown identity class " //$NON-NLS-1$
                        + idt.getClass().getName());
            }
            mode = getUnpackMode(descr, fragment);
        }
        log.debug("unpack mode for " + idt + " is " + mode); //$NON-NLS-1$ //$NON-NLS-2$
        unpackModeMap.put(idt.getId(), mode);
    }
    
    private Boolean getUnpackMode(final PluginDescriptor descr,
            final PluginFragment fragment) {
        for (PluginAttribute attr :
            filterCollection(descr.getAttributes("unpack"), fragment)) { //$NON-NLS-1$
            return Boolean.valueOf("false".equalsIgnoreCase( //$NON-NLS-1$
                    attr.getValue()));
        }
        for (Library lib : filterCollection(descr.getLibraries(), fragment)) {
            if (lib.isCodeLibrary() && (lib.getPath().toLowerCase(
                            Locale.getDefault()).endsWith(".jar") //$NON-NLS-1$
                    || lib.getPath().toLowerCase(
                            Locale.getDefault()).endsWith(".zip"))) { //$NON-NLS-1$
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
    
    private <T extends PluginElement<?>> Collection<T> filterCollection(
            final Collection<T> coll, final PluginFragment fragment) {
        if (fragment == null) {
            return coll;
        }
        LinkedList<T> result = new LinkedList<T>();
        for (T element : coll) {
            if (fragment.equals(element.getDeclaringPluginFragment())) {
                result.add(element);
            }
        }
        return result;
    }
    
    /**
     * @see org.java.plugin.standard.StandardPathResolver#unregisterContext(
     *      java.lang.String)
     */
    @Override
    public void unregisterContext(String id) {
        shadowUrlMap.remove(id);
        unpackModeMap.remove(id);
        super.unregisterContext(id);
    }

    /**
     * @see org.java.plugin.PathResolver#resolvePath(
     *      org.java.plugin.registry.Identity, java.lang.String)
     */
    @Override
    public URL resolvePath(final Identity idt, final String path) {
        URL baseUrl;
        if (idt instanceof PluginDescriptor) {
            baseUrl = getBaseUrl((PluginDescriptor) idt);
        } else if (idt instanceof PluginFragment) {
            baseUrl = getBaseUrl((PluginFragment) idt);
        } else if (idt instanceof PluginElement) {
            PluginElement<?> element = (PluginElement) idt;
            if (element.getDeclaringPluginFragment() != null) {
                baseUrl = getBaseUrl(
                        element.getDeclaringPluginFragment());
            } else {
                baseUrl = getBaseUrl(
                        element.getDeclaringPluginDescriptor());
            }
        } else {
            throw new IllegalArgumentException("unknown identity class " //$NON-NLS-1$
                    + idt.getClass().getName());
        }
        return resolvePath(baseUrl, path);
    }
    
    protected synchronized URL getBaseUrl(final UniqueIdentity uid) {
        URL result = shadowUrlMap.get(uid.getId());
        if (result != null) {
            return result;
        }
        result = controller.shadowResource(getRegisteredContext(uid.getId()),
                uid.getUniqueId(),
                (unpackModeMap.get(uid.getId())).booleanValue());
        shadowUrlMap.put(uid.getId(), result);
        return result;
    }
}

final class ShadingUtil {
    static String getExtension(final String name) {
        if ((name == null) || (name.length() == 0)) {
            return null;
        }
        int p = name.lastIndexOf('.');
        if ((p != -1) && (p > 0) && (p < name.length() - 1)) {
            return name.substring(p + 1);
        }
        return null;
    }
    
    static void unpack(final ZipFile zipFile,
            final File destFolder) throws IOException {
        for (Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements();) {
            ZipEntry entry = en.nextElement();
            String name = entry.getName();
            File entryFile =
                new File(destFolder.getCanonicalPath() + "/" + name); //$NON-NLS-1$
            if (name.endsWith("/")) { //$NON-NLS-1$
                if (!entryFile.exists() && !entryFile.mkdirs()) {
                    throw new IOException("can't create folder " + entryFile); //$NON-NLS-1$
                }
            } else {
                File folder = entryFile.getParentFile();
                if (!folder.exists() && !folder.mkdirs()) {
                    throw new IOException("can't create folder " + folder); //$NON-NLS-1$
                }
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(entryFile, false));
                try {
                    InputStream in = zipFile.getInputStream(entry);
                    try {
                        IoUtil.copyStream(in, out, 1024);
                    } finally {
                        in.close();
                    }
                } finally {
                    out.close();
                }
            }
            entryFile.setLastModified(entry.getTime());
        }
    }
    
    static void unpack(final InputStream strm,
            final File destFolder) throws IOException {
        ZipInputStream zipStrm = new ZipInputStream(strm);
        ZipEntry entry = zipStrm.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            File entryFile =
                new File(destFolder.getCanonicalPath() + "/" + name); //$NON-NLS-1$
            if (name.endsWith("/")) { //$NON-NLS-1$
                if (!entryFile.exists() && !entryFile.mkdirs()) {
                    throw new IOException("can't create folder " + entryFile); //$NON-NLS-1$
                }
            } else {
                File folder = entryFile.getParentFile();
                if (!folder.exists() && !folder.mkdirs()) {
                    throw new IOException("can't create folder " + folder); //$NON-NLS-1$
                }
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(entryFile, false));
                try {
                    IoUtil.copyStream(zipStrm, out, 1024);
                } finally {
                    out.close();
                }
            }
            entryFile.setLastModified(entry.getTime());
            entry = zipStrm.getNextEntry();
        }
    }
    
    static boolean deleteFile(final File file) {
        if (file.isDirectory()) {
            IoUtil.emptyFolder(file);
        }
        return file.delete();
    }
    
    static Date getLastModified(final URL url) throws IOException {
        long result = 0;
        if ("jar".equalsIgnoreCase(url.getProtocol())) { //$NON-NLS-1$
            String urlStr = url.toExternalForm();
            int p = urlStr.indexOf("!/"); //$NON-NLS-1$
            if (p != -1) {
                //sourceFile = IoUtil.url2file(new URL(urlStr.substring(4, p)));
                return getLastModified(new URL(urlStr.substring(4, p)));
            }
        }
        File sourceFile = IoUtil.url2file(url);
        if (sourceFile != null) {
            result = sourceFile.lastModified();
        } else {
            URLConnection cnn = url.openConnection();
            try {
                cnn.setUseCaches(false);
                cnn.setDoInput(false); // this should force using HTTP HEAD method
                result = cnn.getLastModified();
            } finally {
                try {
                    cnn.getInputStream().close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
        if (result == 0) {
            throw new IOException(
                    "can't retrieve modification date for resource " //$NON-NLS-1$
                    + url);
        }
        // for some reason modification milliseconds for some files are unstable
        Calendar cldr = Calendar.getInstance(Locale.ENGLISH);
        cldr.setTime(new Date(result));
        cldr.set(Calendar.MILLISECOND, 0);
        return cldr.getTime();
    }
    
    private static String getRelativePath(final File base, final File file)
            throws IOException {
        String basePath;
        String filePath = file.getCanonicalPath();
        if (base.isFile()) {
            File baseParent = base.getParentFile();
            if (baseParent == null) {
                return null;
            }
            basePath = baseParent.getCanonicalPath();
        } else {
            basePath = base.getCanonicalPath();
        }
        if (!basePath.endsWith(File.separator)) {
            basePath += File.separator;
        }
        int p = basePath.indexOf(File.separatorChar);
        String prefix = null;
        while (p != -1) {
            String newPrefix = basePath.substring(0, p + 1);
            if (!filePath.startsWith(newPrefix)) {
                break;
            }
            prefix = newPrefix;
            p = basePath.indexOf(File.separatorChar, p + 1);
        }
        if (prefix == null) {
            return null;
        }
        filePath = filePath.substring(prefix.length());
        if (prefix.length() == basePath.length()) {
            return filePath;
        }
        int c = 0;
        p = basePath.indexOf(File.separatorChar, prefix.length());
        while (p != -1) {
            c++;
            p = basePath.indexOf(File.separatorChar, p + 1);
        }
        for (int i = 0; i < c; i++) {
            filePath = ".." + File.separator + filePath; //$NON-NLS-1$
        }
        return filePath;
    }
    
    private static String getRelativeUrl(final File base, final File file)
            throws IOException {
        String result = ShadingUtil.getRelativePath(base, file);
        if (result == null) {
            return null;
        }
        result = result.replace('\\', '/');
        if (file.isDirectory() && !result.endsWith("/")) { //$NON-NLS-1$
            result += "/"; //$NON-NLS-1$
        }
        return result;
    }

    static String getRelativeUrl(final File base, final URL url)
            throws IOException {
        File file = IoUtil.url2file(url);
        if (file != null) {
            String result = getRelativeUrl(base, file);
            if (result != null) {
                return result;
            }
        }
        if ("jar".equalsIgnoreCase(url.getProtocol())) { //$NON-NLS-1$
            String urlStr = url.toExternalForm();
            int p = urlStr.indexOf("!/"); //$NON-NLS-1$
            if (p != -1) {
                return "jar:" //$NON-NLS-1$
                    + getRelativeUrl(base, new URL(urlStr.substring(4, p)))
                    + urlStr.substring(p);
            }
        }
        return url.toExternalForm();
    }
    
    static URL buildURL(final URL base, final String url)
            throws MalformedURLException {
        if (!url.toLowerCase(Locale.ENGLISH).startsWith("jar:")) { //$NON-NLS-1$
            return new URL(base, url);
        }
        int p = url.indexOf("!/"); //$NON-NLS-1$
        if (p == -1) {
            return new URL(base, url);
        }
        return new URL("jar:" //$NON-NLS-1$
                + new URL(base, url.substring(4, p)).toExternalForm()
                + url.substring(p));
    }
    
    private ShadingUtil() {
        // no-op
    }
}

final class ShadowDataController {
    private static final String META_FILE_NAME = ".meta"; //$NON-NLS-1$
    
    private final Log log = LogFactory.getLog(ShadowDataController.class);
    private final File shadowFolder;
    private final URL shadowFolderUrl;
    private final Properties metaData;
    private final DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
    private final FileFilter fileFilter;
    
    static ShadowDataController init(final File shadowFolder,
            final FileFilter filter) throws IOException {
        ShadowDataController result =
            new ShadowDataController(shadowFolder, filter);
        result.quickCheck();
        result.save();
        return result;
    }

    private ShadowDataController(final File folder, final FileFilter filter)
            throws IOException {
        shadowFolder = folder;
        fileFilter = filter;
        shadowFolderUrl = IoUtil.file2url(folder);
        File metaFile = new File(shadowFolder, META_FILE_NAME);
        metaData = new Properties();
        if (metaFile.isFile()) {
            try {
                InputStream in = new FileInputStream(metaFile);
                try {
                    metaData.load(in);
                } finally {
                    in.close();
                }
                if (log.isDebugEnabled()) {
                    log.debug("meta-data loaded from file " + metaFile); //$NON-NLS-1$
                }
            } catch (IOException ioe) {
                log.warn("failed loading meta-data from file " + metaFile, ioe); //$NON-NLS-1$
            }
        }
    }
    
    private void save() {
        File metaFile = new File(shadowFolder, META_FILE_NAME);
        try {
            OutputStream out = new FileOutputStream(metaFile, false);
            try {
                metaData.store(out, "This is automatically generated file."); //$NON-NLS-1$
            } finally {
                out.close();
            }
            if (log.isDebugEnabled()) {
                log.debug("meta-data saved to file " + metaFile); //$NON-NLS-1$
            }
        } catch (IOException ioe) {
            log.warn("failed saving meta-data to file " + metaFile, ioe); //$NON-NLS-1$
        }
    }
    
    private void quickCheck() {
        File[] files = shadowFolder.listFiles(new ShadowFileFilter());
        for (File file : files)
        {
            if (metaData.containsValue(file.getName())) {
                continue;
            }
            if (ShadingUtil.deleteFile(file)) {
                if (log.isDebugEnabled()) {
                    log.debug("deleted shadow file " + file); //$NON-NLS-1$
                }
            } else {
                log.warn("can't delete shadow file " + file); //$NON-NLS-1$
            }
        }
        Set<Object> uids = new HashSet<Object>();
        for (Map.Entry<Object, Object> entry : metaData.entrySet()) {
            String key = (String) entry.getKey();
            if (!key.startsWith("uid:")) { //$NON-NLS-1$
                continue;
            }
            uids.add(entry.getValue());
        }
        for (Object object : uids) {
            quickCheck((String) object);
        }
    }
    
    private void quickCheck(final String uid) {
        if (log.isDebugEnabled()) {
            log.debug("quick check of UID " + uid); //$NON-NLS-1$
        }
        String url = metaData.getProperty("source:" + uid, null); //$NON-NLS-1$
        String file = metaData.getProperty("file:" + uid, null); //$NON-NLS-1$
        String modified = metaData.getProperty("modified:" + uid, null); //$NON-NLS-1$
        if ((url == null) || (file == null) || (modified == null)) {
            if (log.isDebugEnabled()) {
                log.debug("meta-data incomplete, UID=" + uid); //$NON-NLS-1$
            }
            remove(uid);
            return;
        }
        try {
            if (!dtf.parse(modified).equals(ShadingUtil.getLastModified(
                    ShadingUtil.buildURL(shadowFolderUrl, url)))) {
                if (log.isDebugEnabled()) {
                    log.debug("source modification detected, UID=" + uid //$NON-NLS-1$
                            + ", source=" + url); //$NON-NLS-1$
                }
                remove(uid);
            }
        } catch (IOException ioe) {
            log.warn("quick check failed", ioe); //$NON-NLS-1$
            remove(uid);
        } catch (ParseException pe) {
            log.warn("quick check failed", pe); //$NON-NLS-1$
            remove(uid);
        }
    }
    
    private void remove(final String uid) {
        String file = metaData.getProperty("file:" + uid, null); //$NON-NLS-1$
        if (file != null) {
            File lostFile = new File(shadowFolder, file);
            if (ShadingUtil.deleteFile(lostFile)) {
                if (log.isDebugEnabled()) {
                    log.debug("deleted lost file " + file); //$NON-NLS-1$
                }
            } else {
                log.warn("can't delete lost file " + file); //$NON-NLS-1$
            }
        }
        boolean removed = metaData.remove("uid:" + uid) != null; //$NON-NLS-1$
        removed |= metaData.remove("source:" + uid) != null; //$NON-NLS-1$
        removed |= metaData.remove("file:" + uid) != null; //$NON-NLS-1$
        removed |= metaData.remove("modified:" + uid) != null; //$NON-NLS-1$
        if (removed && log.isDebugEnabled()) {
            log.debug("removed meta-data, UID=" + uid); //$NON-NLS-1$
        }
    }
    
    private URL add(final String uid, final URL sourceUrl, final File file,
            final Date modified) throws IOException {
        URL result = IoUtil.file2url(file);
        metaData.setProperty("uid:" + uid, uid); //$NON-NLS-1$
        String source = ShadingUtil.getRelativeUrl(shadowFolder, sourceUrl);
        metaData.setProperty("source:" + uid, source); //$NON-NLS-1$
        metaData.setProperty("file:" + uid, file.getName()); //$NON-NLS-1$
        metaData.setProperty("modified:" + uid, dtf.format(modified)); //$NON-NLS-1$
        save();
        if (log.isDebugEnabled()) {
            log.debug("shading done, UID=" + uid + ", source=" //$NON-NLS-1$ //$NON-NLS-2$
                    + source + ", file=" + result //$NON-NLS-1$
                    + ", modified=" + dtf.format(modified)); //$NON-NLS-1$
        }
        return result;
    }
    
    URL shadowResource(final URL source, final String uid,
            final boolean unpack) {
        try {
            URL result = deepCheck(source, uid);
            if (result != null) {
                if (log.isDebugEnabled()) {
                    log.debug("got actual shaded resource, UID=" + uid //$NON-NLS-1$
                            + ", source=" + source //$NON-NLS-1$
                            + ", file=" + result); //$NON-NLS-1$
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("deep check failed, UID=" + uid //$NON-NLS-1$
                    + ", URL=" + source, e); //$NON-NLS-1$
            remove(uid);
        }
        Date lastModified;
        try {
            lastModified = ShadingUtil.getLastModified(source);
        } catch (IOException ioe) {
            log.error("shading failed, can't get modification date for " //$NON-NLS-1$
                    + source, ioe);
            return source;
        }
        File file = IoUtil.url2file(source);
        if ((file != null) && file.isDirectory()) {
            // copy local folder to the shadow directory
            try {
                File rootFolder = new File(shadowFolder, uid);
                IoUtil.copyFolder(file, rootFolder, true, true, fileFilter);
                return add(uid, source, rootFolder, lastModified);
            } catch (IOException ioe) {
                log.error("failed shading local folder " + file, ioe); //$NON-NLS-1$
                return source;
            }
        }
        try {
            if ("jar".equalsIgnoreCase(source.getProtocol())) { //$NON-NLS-1$
                String urlStr = source.toExternalForm();
                int p = urlStr.indexOf("!/"); //$NON-NLS-1$
                if (p == -1) {
                    p = urlStr.length();
                }
                URL jarFileURL = new URL(urlStr.substring(4, p));
                if (!unpack) {
                    String ext = ShadingUtil.getExtension(jarFileURL.getFile());
                    if (ext == null) {
                        ext = "jar"; //$NON-NLS-1$
                    }
                    File shadowFile = new File(shadowFolder, uid + '.' + ext);
                    File sourceFile = IoUtil.url2file(jarFileURL);
                    InputStream in;
                    if (sourceFile != null) {
                        in = new BufferedInputStream(
                                new FileInputStream(sourceFile));
                    } else {
                        in = jarFileURL.openStream();
                    }
                    try {
                        OutputStream out =
                            new FileOutputStream(shadowFile, false);
                        try {
                            IoUtil.copyStream(in, out, 1024);
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                    return add(uid, source, shadowFile, lastModified);
                }
                URLConnection cnn = null;
                try {
                    File sourceFile = IoUtil.url2file(jarFileURL);
                    ZipFile zipFile;
                    if (sourceFile != null) {
                        zipFile = new ZipFile(sourceFile);
                    } else {
                        cnn = source.openConnection();
                        cnn.setUseCaches(false);
                        zipFile = ((JarURLConnection) cnn).getJarFile();
                    }
                    File rootFolder = new File(shadowFolder, uid);
                    try {
                        ShadingUtil.unpack(zipFile, rootFolder);
                    } finally {
                        zipFile.close();
                    }
                    return add(uid, source, rootFolder, lastModified);
                } finally {
                    if (cnn != null) {
                        cnn.getInputStream().close();
                    }
                }
            }
        } catch (IOException ioe) {
            log.error("failed shading URL connection " + source, ioe); //$NON-NLS-1$
            return source;
        }
        String fileName = source.getFile();
        if (fileName == null) {
            log.warn("can't get file name from resource " + source //$NON-NLS-1$
                    + ", shading failed"); //$NON-NLS-1$
            return source;
        }
        String ext = ShadingUtil.getExtension(fileName);
        if (ext == null) {
            log.warn("can't get file name extension for resource " + source //$NON-NLS-1$
                    + ", shading failed"); //$NON-NLS-1$
            return source;
        }
        if (unpack && ("jar".equalsIgnoreCase(ext) //$NON-NLS-1$
                || "zip".equalsIgnoreCase(ext))) { //$NON-NLS-1$
            try {
                InputStream strm = source.openStream();
                File rootFolder = new File(shadowFolder, uid);
                try {
                    ShadingUtil.unpack(strm, rootFolder);
                } finally {
                    strm.close();
                }
                return add(uid, source, rootFolder, lastModified);
            } catch (IOException ioe) {
                log.error("failed shading packed resource " + source, ioe); //$NON-NLS-1$
                return source;
            }
        }
        try {
            File shadowFile = new File(shadowFolder, uid + '.' + ext);
            InputStream in = source.openStream();
            try {
                OutputStream out = new FileOutputStream(shadowFile, false);
                try {
                    IoUtil.copyStream(in, out, 1024);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
            return add(uid, source, shadowFile, lastModified);
        } catch (IOException ioe) {
            log.error("failed shading resource file " + source, ioe); //$NON-NLS-1$
            return source;
        }
    }
    
    private URL deepCheck(final URL source, final String uid) throws Exception {
        String url = metaData.getProperty("source:" + uid, null); //$NON-NLS-1$
        if (url == null) {
            if (log.isDebugEnabled()) {
                log.debug("URL not found in meta-data, UID=" + uid); //$NON-NLS-1$
            }
            remove(uid);
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("URL found in meta-data, UID=" //$NON-NLS-1$
                    + uid + ", source=" + source //$NON-NLS-1$
                    + ", storedURL=" + url); //$NON-NLS-1$
        }
        URL storedSource = ShadingUtil.buildURL(shadowFolderUrl, url);
        if (!storedSource.equals(source)) {
            if (log.isDebugEnabled()) {
                log.debug("inconsistent URL found in meta-data, UID=" //$NON-NLS-1$
                        + uid + ", source=" + source //$NON-NLS-1$
                        + ", storedSource=" + storedSource); //$NON-NLS-1$
            }
            remove(uid);
            return null;
        }
        String modified = metaData.getProperty("modified:" + uid, null); //$NON-NLS-1$
        if (modified == null) {
            if (log.isDebugEnabled()) {
                log.debug("modification info not found in meta-data, UID=" //$NON-NLS-1$
                        + uid);
            }
            remove(uid);
            return null;
        }
        if (!ShadingUtil.getLastModified(source).equals(dtf.parse(modified))) {
            if (log.isDebugEnabled()) {
                log.debug("source modification detected, UID=" + uid //$NON-NLS-1$
                        + ", source=" + source); //$NON-NLS-1$
            }
            remove(uid);
            return null;
        }
        String fileStr = metaData.getProperty("file:" + uid, null); //$NON-NLS-1$
        if (fileStr == null) {
            if (log.isDebugEnabled()) {
                log.debug("file info not found in meta-data, UID=" + uid); //$NON-NLS-1$
            }
            remove(uid);
            return null;
        }
        File file = new File(shadowFolder, fileStr);
        if (!file.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("shadow file not found, UID=" + uid //$NON-NLS-1$
                        + ", source=" + source //$NON-NLS-1$
                        + ", file=" + file); //$NON-NLS-1$
            }
            remove(uid);
            return null;
        }
        File sourceFile = IoUtil.url2file(source);
        if ((sourceFile != null) && sourceFile.isDirectory()) {
            IoUtil.synchronizeFolders(sourceFile, file, fileFilter);
            if (log.isDebugEnabled()) {
                log.debug("folders synchronized, UID=" + uid //$NON-NLS-1$
                        + ", srcFile=" + sourceFile //$NON-NLS-1$
                        + ", destFile=" + file); //$NON-NLS-1$
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("source " + source + " (file is " + sourceFile //$NON-NLS-1$ //$NON-NLS-2$
                        + ") is not local folder, " //$NON-NLS-1$
                        + "skipping synchronization, UID=" + uid); //$NON-NLS-1$
            }
        }
        return IoUtil.file2url(file);
    }
    
    static class ShadowFileFilter implements FileFilter {
        /**
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(final File file) {
            return !META_FILE_NAME.equals(file.getName());
        }
    }
}

final class RegexpFileFilter implements FileFilter {
    private final Pattern[] patterns;
    
    RegexpFileFilter(final String str) {
        StringTokenizer st = new StringTokenizer(str, "|", false); //$NON-NLS-1$
        patterns = new Pattern[st.countTokens()];
        for (int i = 0; i < patterns.length; i++) {
            String pattern = st.nextToken();
            if ((pattern == null) || (pattern.trim().length() == 0)) {
                continue;
            }
            patterns[i] = Pattern.compile(pattern.trim());
        }
    }

    /**
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(final File file) {
        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i] == null) {
                continue;
            }
            if (patterns[i].matcher(file.getName()).matches()) {
                return true;
            }
        }
        return false;
    }
}

final class CombinedFileFilter implements FileFilter {
    private final FileFilter includesFilter;
    private final FileFilter excludesFilter;

    CombinedFileFilter(final FileFilter includes, final FileFilter excludes) {
        includesFilter = includes;
        excludesFilter = excludes;
    }

    /**
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(final File file) {
        if (includesFilter != null) {
            if (includesFilter.accept(file)) {
                return true;
            }
        }
        if ((excludesFilter != null) && excludesFilter.accept(file)) {
            return false;
        }
        return true;
    }
}
