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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.Plugin;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginPrerequisite;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.util.IoUtil;


/**
 * Standard implementation of plug-in class loader.
 * 
 * @version $Id: StandardPluginClassLoader.java,v 1.8 2007/04/07 12:39:50 ddimon
 *          Exp $
 */
public class StandardPluginClassLoader extends PluginClassLoader {

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		try {
			byte[] bytes = toByte(super.getResourceAsStream(name.replace('.', '/') + ".class"));
			Class loaded = super.defineClass(name, bytes, 0, bytes.length, this.getClass().getProtectionDomain());

			return loaded;
		} catch (Exception e) {
			throw new ClassNotFoundException("IOException", e.getCause());
		}
	}

	private byte[] toByte(InputStream is) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (true) {
			int len = is.read(buffer);
			if (len < 0) {
				break;
			}
			bout.write(buffer, 0, len);
		}
		return bout.toByteArray();

	}

	static Log log = LogFactory.getLog(StandardPluginClassLoader.class);

	private static File libCacheFolder;
	private static boolean libCacheFolderInitialized = false;

	private static URL getClassBaseUrl(final Class<?> cls) {
		ProtectionDomain pd = cls.getProtectionDomain();
		if (pd != null) {
			CodeSource cs = pd.getCodeSource();
			if (cs != null) {
				return cs.getLocation();
			}
		}
		return null;
	}

	private static URL[] getUrls(final PluginManager manager, final PluginDescriptor descr) {
		List<URL> result = new LinkedList<URL>();
		for (Library lib : descr.getLibraries()) {
			if (!lib.isCodeLibrary()) {
				continue;
			}
			result.add(manager.getPathResolver().resolvePath(lib, lib.getPath()));
		}
		if (log.isDebugEnabled()) {
			final StringBuilder buf = new StringBuilder();
			buf.append("Code URL's populated for plug-in " //$NON-NLS-1$
					+ descr + ":\r\n"); //$NON-NLS-1$
			for (Object element : result) {
				buf.append("\t"); //$NON-NLS-1$
				buf.append(element);
				buf.append("\r\n"); //$NON-NLS-1$
			}
			log.debug(buf.toString());
		}
		return result.toArray(new URL[result.size()]);
	}

	private static URL[] getUrls(final PluginManager manager, final PluginDescriptor descr, final URL[] existingUrls) {
		final List<URL> urls = Arrays.asList(existingUrls);
		final List<URL> result = new LinkedList<URL>();
		for (Library lib : descr.getLibraries()) {
			if (!lib.isCodeLibrary()) {
				continue;
			}
			URL url = manager.getPathResolver().resolvePath(lib, lib.getPath());
			if (!urls.contains(url)) {
				result.add(url);
			}
		}
		return result.toArray(new URL[result.size()]);
	}

	private static File getLibCacheFolder() {
		if (libCacheFolder != null) {
			return libCacheFolderInitialized ? libCacheFolder : null;
		}
		synchronized (StandardPluginClassLoader.class) {
			libCacheFolder = new File(System.getProperty("java.io.tmpdir"), //$NON-NLS-1$
					System.currentTimeMillis() + ".jpf-lib-cache"); //$NON-NLS-1$
			log.debug("libraries cache folder is " + libCacheFolder); //$NON-NLS-1$
			File lockFile = new File(libCacheFolder, "lock"); //$NON-NLS-1$
			if (lockFile.exists()) {
				log.error("can't initialize libraries cache folder " //$NON-NLS-1$
						+ libCacheFolder + " as lock file indicates that it" //$NON-NLS-1$
						+ " is owned by another JPF instance"); //$NON-NLS-1$
				return null;
			}
			if (libCacheFolder.exists()) {
				// clean up folder
				IoUtil.emptyFolder(libCacheFolder);
			} else {
				libCacheFolder.mkdirs();
			}
			try {
				if (!lockFile.createNewFile()) {
					log.error("can\'t create lock file in JPF libraries cache" //$NON-NLS-1$
							+ " folder " + libCacheFolder); //$NON-NLS-1$
					return null;
				}
			} catch (IOException ioe) {
				log.error("can\'t create lock file in JPF libraries cache" //$NON-NLS-1$
						+ " folder " + libCacheFolder, ioe); //$NON-NLS-1$
				return null;
			}
			lockFile.deleteOnExit();
			libCacheFolder.deleteOnExit();
			libCacheFolderInitialized = true;
		}
		return libCacheFolder;
	}

	private PluginDescriptor[] publicImports;
	private PluginDescriptor[] privateImports;
	private PluginDescriptor[] reverseLookups;
	private PluginResourceLoader resourceLoader;
	private Map<String, ResourceFilter> resourceFilters;
	private Map<String, File> libraryCache;
	private boolean probeParentLoaderLast;
	private boolean stickySynchronizing;
	private boolean localClassLoadingOptimization = true;
	private boolean foreignClassLoadingOptimization = true;
	private final Set<String> localPackages = new HashSet<String>();
	private final Map<String, PluginDescriptor> pluginPackages = new HashMap<String, PluginDescriptor>();

	/**
	 * Creates class instance configured to load classes and resources for given
	 * plug-in.
	 * 
	 * @param aManager
	 *            plug-in manager instance
	 * @param descr
	 *            plug-in descriptor
	 * @param parent
	 *            parent class loader, usually this is JPF "host" application
	 *            class loader
	 */
	public StandardPluginClassLoader(final PluginManager aManager, final PluginDescriptor descr, final ClassLoader parent) {
		super(aManager, descr, getUrls(aManager, descr), parent);
		collectImports();
		resourceLoader = PluginResourceLoader.get(aManager, descr);
		collectFilters();
		libraryCache = new HashMap<String, File>();
	}

	protected void collectImports() {
		// collect imported plug-ins (exclude duplicates)
		final Map<String, PluginDescriptor> publicImportsMap = new HashMap<String, PluginDescriptor>();
		final Map<String, PluginDescriptor> privateImportsMap = new HashMap<String, PluginDescriptor>();
		PluginRegistry registry = getPluginDescriptor().getRegistry();
		for (PluginPrerequisite pre : getPluginDescriptor().getPrerequisites()) {
			if (!pre.matches()) {
				continue;
			}
			PluginDescriptor preDescr = registry.getPluginDescriptor(pre.getPluginId());
			if (pre.isExported()) {
				publicImportsMap.put(preDescr.getId(), preDescr);
			} else {
				privateImportsMap.put(preDescr.getId(), preDescr);
			}
		}
		publicImports = publicImportsMap.values().toArray(new PluginDescriptor[publicImportsMap.size()]);
		privateImports = privateImportsMap.values().toArray(new PluginDescriptor[privateImportsMap.size()]);
		// collect reverse look up plug-ins (exclude duplicates)
		final Map<String, PluginDescriptor> reverseLookupsMap = new HashMap<String, PluginDescriptor>();
		for (PluginDescriptor descr : registry.getPluginDescriptors()) {
			if (descr.equals(getPluginDescriptor()) || publicImportsMap.containsKey(descr.getId()) || privateImportsMap.containsKey(descr.getId())) {
				continue;
			}
			for (PluginPrerequisite pre : descr.getPrerequisites()) {
				if (!pre.getPluginId().equals(getPluginDescriptor().getId()) || !pre.isReverseLookup()) {
					continue;
				}
				if (!pre.matches()) {
					continue;
				}
				reverseLookupsMap.put(descr.getId(), descr);
				break;
			}
		}
		reverseLookups = reverseLookupsMap.values().toArray(new PluginDescriptor[reverseLookupsMap.size()]);
	}

	protected void collectFilters() {
		if (resourceFilters == null) {
			resourceFilters = new HashMap<String, ResourceFilter>();
		} else {
			resourceFilters.clear();
		}
		for (Library lib : getPluginDescriptor().getLibraries()) {
			resourceFilters.put(getPluginManager().getPathResolver().resolvePath(lib, lib.getPath()).toExternalForm(), new ResourceFilter(lib));
		}
	}

	/**
	 * @see org.java.plugin.PluginClassLoader#pluginsSetChanged()
	 */
	@Override
	protected void pluginsSetChanged() {
		URL[] newUrls = getUrls(getPluginManager(), getPluginDescriptor(), getURLs());
		for (URL element : newUrls) {
			addURL(element);
		}
		if (log.isDebugEnabled()) {
			StringBuilder buf = new StringBuilder();
			buf.append("New code URL's populated for plug-in " //$NON-NLS-1$
					+ getPluginDescriptor() + ":\r\n"); //$NON-NLS-1$
			for (URL element : newUrls) {
				buf.append("\t"); //$NON-NLS-1$
				buf.append(element);
				buf.append("\r\n"); //$NON-NLS-1$
			}
			log.debug(buf.toString());
		}
		collectImports();
		// repopulate resource URLs
		resourceLoader = PluginResourceLoader.get(getPluginManager(), getPluginDescriptor());
		collectFilters();
		Set<Entry<String, File>> entrySet = libraryCache.entrySet();
		for (Iterator<Entry<String, File>> it = entrySet.iterator(); it.hasNext();) {
			if (it.next().getValue() == null) {
				it.remove();
			}
		}
		synchronized (localPackages) {
			localPackages.clear();
		}
		synchronized (pluginPackages) {
			pluginPackages.clear();
		}
	}

	/**
	 * @see org.java.plugin.PluginClassLoader#dispose()
	 */
	@Override
	protected void dispose() {
		for (File file : libraryCache.values()) {
			file.delete();
		}
		libraryCache.clear();
		resourceFilters.clear();
		privateImports = null;
		publicImports = null;
		resourceLoader = null;
		synchronized (localPackages) {
			localPackages.clear();
		}
		synchronized (pluginPackages) {
			pluginPackages.clear();
		}
	}

	protected void setProbeParentLoaderLast(final boolean value) {
		probeParentLoaderLast = value;
	}

	protected void setStickySynchronizing(final boolean value) {
		stickySynchronizing = value;
	}

	protected void setLocalClassLoadingOptimization(final boolean value) {
		localClassLoadingOptimization = value;
	}

	protected void setForeignClassLoadingOptimization(final boolean value) {
		foreignClassLoadingOptimization = value;
	}

	/**
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		Class<?> result;
		boolean tryLocal = true;
		if (isLocalClass(name)) {
			if (log.isDebugEnabled()) {
				log.debug("loadClass: trying local class guess, name=" //$NON-NLS-1$
						+ name + ", this=" + this); //$NON-NLS-1$
			}
			result = loadLocalClass(name, resolve, this);
			if (result != null) {
				if (log.isDebugEnabled()) {
					log.debug("loadClass: local class guess succeeds, name=" //$NON-NLS-1$
							+ name + ", this=" + this); //$NON-NLS-1$
				}
				checkClassVisibility(result, this);
				return result;
			}
			tryLocal = false;
		}
		if (probeParentLoaderLast) {
			try {
				result = loadPluginClass(name, resolve, tryLocal, this, null);
			} catch (ClassNotFoundException cnfe) {
				result = getParent().loadClass(name);
			}
			if (result == null) {
				result = getParent().loadClass(name);
			}
		} else {
			try {
				result = getParent().loadClass(name);
			} catch (ClassNotFoundException cnfe) {
				result = loadPluginClass(name, resolve, tryLocal, this, null);
			}
		}
		if (result != null) {
			return result;
		}
		throw new ClassNotFoundException(name);
	}

	private Class<?> loadLocalClass(final String name, final boolean resolve, final StandardPluginClassLoader requestor) {
		boolean debugEnabled = log.isDebugEnabled();
		synchronized (stickySynchronizing ? requestor : this) {
			Class<?> result = findLoadedClass(name);
			if (result != null) {
				if (debugEnabled) {
					log.debug("loadLocalClass: found loaded class, class=" //$NON-NLS-1$
							+ result + ", this=" //$NON-NLS-1$
							+ this + ", requestor=" + requestor); //$NON-NLS-1$
				}
				return result; // found already loaded class in this plug-in
			}
			try {
				result = findClass(name);
			} catch (LinkageError le) {
				if (debugEnabled) {
					log.debug("loadLocalClass: class loading failed," //$NON-NLS-1$
							+ " name=" + name + ", this=" //$NON-NLS-1$ //$NON-NLS-2$
							+ this + ", requestor=" + requestor, le); //$NON-NLS-1$
				}
				throw le;
			} catch (ClassNotFoundException cnfe) {
				// ignore
			}
			if (result != null) {
				if (debugEnabled) {
					log.debug("loadLocalClass: found class, class=" //$NON-NLS-1$
							+ result + ", this=" //$NON-NLS-1$
							+ this + ", requestor=" + requestor); //$NON-NLS-1$
				}
				if (resolve) {
					resolveClass(result);
				}
				registerLocalPackage(result);
				return result; // found class in this plug-in
			}
		}
		return null;
	}

	private Class<?> loadPluginClass(final String name, final boolean resolve, final boolean tryLocal, final StandardPluginClassLoader requestor, final Set<String> seenPlugins) throws ClassNotFoundException {
		Set<String> seen = seenPlugins;
		if ((seen != null) && seen.contains(getPluginDescriptor().getId())) {
			return null;
		}
		if (seen == null) {
			seen = new HashSet<String>();
		}
		seen.add(getPluginDescriptor().getId());
		if ((this != requestor) && !getPluginManager().isPluginActivated(getPluginDescriptor()) && !getPluginManager().isPluginActivating(getPluginDescriptor())) {
			String msg = "can't load class " + name + ", plug-in " //$NON-NLS-1$ //$NON-NLS-2$
					+ getPluginDescriptor() + " is not activated yet"; //$NON-NLS-1$
			log.warn(msg);
			throw new ClassNotFoundException(msg);
		}
		Class<?> result = null;
		boolean debugEnabled = log.isDebugEnabled();
		PluginDescriptor descr = guessPlugin(name);
		if ((descr != null) && !seen.contains(descr.getId())) {
			if (debugEnabled) {
				log.debug("loadPluginClass: trying plug-in guess, name=" //$NON-NLS-1$
						+ name + ", this=" //$NON-NLS-1$
						+ this + ", requestor=" + requestor); //$NON-NLS-1$
			}
			result = ((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(descr)).loadPluginClass(name, resolve, true, requestor, seen);
			if (result != null) {
				if (debugEnabled) {
					log.debug("loadPluginClass: plug-in guess succeeds, name=" //$NON-NLS-1$
							+ name + ", this=" //$NON-NLS-1$
							+ this + ", requestor=" + requestor); //$NON-NLS-1$
				}
				return result;
			}
		}
		if (tryLocal) {
			result = loadLocalClass(name, resolve, requestor);
			if (result != null) {
				checkClassVisibility(result, requestor);
				return result;
			}
		}
		if (debugEnabled) {
			log.debug("loadPluginClass: local class not found, name=" //$NON-NLS-1$
					+ name + ", this=" //$NON-NLS-1$
					+ this + ", requestor=" + requestor); //$NON-NLS-1$
		}
		for (PluginDescriptor element : publicImports) {
			if (seen.contains(element.getId())) {
				continue;
			}
			result = ((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).loadPluginClass(name, resolve, true, requestor, seen);
			if (result != null) {
				break; // found class in publicly imported plug-in
			}
		}
		if ((this == requestor) && (result == null)) {
			for (PluginDescriptor element : privateImports) {
				if (seen.contains(element.getId())) {
					continue;
				}
				result = ((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).loadPluginClass(name, resolve, true, requestor, seen);
				if (result != null) {
					break; // found class in privately imported plug-in
				}
			}
		}
		if (result == null) {
			for (PluginDescriptor element : reverseLookups) {
				if (seen.contains(element.getId())) {
					continue;
				}
				if (!getPluginManager().isPluginActivated(element) && !getPluginManager().isPluginActivating(element)) {
					continue;
				}
				result = ((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).loadPluginClass(name, resolve, true, requestor, seen);
				if (result != null) {
					break; // found class in plug-in that marks itself as
					// allowed reverse look up
				}
			}
		}
		registerPluginPackage(result);
		return result;
	}

	private boolean isLocalClass(final String className) {
		if (!localClassLoadingOptimization) {
			return false;
		}
		String pkgName = getPackageName(className);
		if (pkgName == null) {
			return false;
		}
		return localPackages.contains(pkgName);
	}

	private void registerLocalPackage(final Class<?> cls) {
		if (!localClassLoadingOptimization) {
			return;
		}
		String pkgName = getPackageName(cls.getName());
		if ((pkgName == null) || localPackages.contains(pkgName)) {
			return;
		}
		synchronized (localPackages) {
			localPackages.add(pkgName);
		}
		if (log.isDebugEnabled()) {
			log.debug("registered local package: name=" + pkgName); //$NON-NLS-1$
		}
	}

	private PluginDescriptor guessPlugin(final String className) {
		if (!foreignClassLoadingOptimization) {
			return null;
		}
		String pkgName = getPackageName(className);
		if (pkgName == null) {
			return null;
		}
		return pluginPackages.get(pkgName);
	}

	private void registerPluginPackage(final Class<?> cls) {
		if (!foreignClassLoadingOptimization) {
			return;
		}
		Plugin plugin = getPluginManager().getPluginFor(cls);
		if (plugin == null) {
			return;
		}
		String pkgName = getPackageName(cls.getName());
		if ((pkgName == null) || pluginPackages.containsKey(pkgName)) {
			return;
		}
		synchronized (pluginPackages) {
			pluginPackages.put(pkgName, plugin.getDescriptor());
		}
		if (log.isDebugEnabled()) {
			log.debug("registered plug-in package: name=" + pkgName //$NON-NLS-1$
					+ ", plugin=" + plugin.getDescriptor()); //$NON-NLS-1$
		}
	}

	private String getPackageName(final String className) {
		int p = className.lastIndexOf('.');
		if (p == -1) {
			return null;
		}
		return className.substring(0, p);
	}

	protected void checkClassVisibility(final Class<?> cls, final StandardPluginClassLoader requestor) throws ClassNotFoundException {
		if (this == requestor) {
			return;
		}
		URL lib = getClassBaseUrl(cls);
		if (lib == null) {
			return; // cls is a system class
		}
		ClassLoader loader = cls.getClassLoader();
		if (!(loader instanceof StandardPluginClassLoader)) {
			return;
		}
		if (loader != this) {
			((StandardPluginClassLoader) loader).checkClassVisibility(cls, requestor);
		} else {
			ResourceFilter filter = resourceFilters.get(lib.toExternalForm());
			if (filter == null) {
				log.warn("class not visible, no class filter found, lib=" + lib //$NON-NLS-1$
						+ ", class=" + cls + ", this=" + this //$NON-NLS-1$ //$NON-NLS-2$
						+ ", requestor=" + requestor); //$NON-NLS-1$
				throw new ClassNotFoundException("class " //$NON-NLS-1$
						+ cls.getName() + " is not visible for plug-in " //$NON-NLS-1$
						+ requestor.getPluginDescriptor().getId() + ", no filter found for library " + lib); //$NON-NLS-1$
			}
			if (!filter.isClassVisible(cls.getName())) {
				log.warn("class not visible, lib=" + lib //$NON-NLS-1$
						+ ", class=" + cls + ", this=" + this //$NON-NLS-1$ //$NON-NLS-2$
						+ ", requestor=" + requestor); //$NON-NLS-1$
				throw new ClassNotFoundException("class " //$NON-NLS-1$
						+ cls.getName() + " is not visible for plug-in " //$NON-NLS-1$
						+ requestor.getPluginDescriptor().getId());
			}
		}
	}

	/**
	 * @see java.lang.ClassLoader#findLibrary(java.lang.String)
	 */
	@Override
	protected String findLibrary(final String name) {
		if ((name == null) || "".equals(name.trim())) { //$NON-NLS-1$
			return null;
		}
		if (log.isDebugEnabled()) {
			log.debug("findLibrary(String): name=" + name //$NON-NLS-1$
					+ ", this=" + this); //$NON-NLS-1$
		}
		String libname = System.mapLibraryName(name);
		String result = null;
		PathResolver pathResolver = getPluginManager().getPathResolver();
		for (Library lib : getPluginDescriptor().getLibraries()) {
			if (lib.isCodeLibrary())
				continue;

			URL libUrl = pathResolver.resolvePath(lib, lib.getPath() + libname);
			if (log.isDebugEnabled()) {
				log.debug("findLibrary(String): trying URL " + libUrl); //$NON-NLS-1$
			}
			File libFile = IoUtil.url2file(libUrl);
			if (libFile != null) {
				if (log.isDebugEnabled()) {
					log.debug("findLibrary(String): URL " + libUrl //$NON-NLS-1$
							+ " resolved as local file " + libFile); //$NON-NLS-1$
				}
				if (libFile.isFile()) {
					result = libFile.getAbsolutePath();
					break;
				}
				continue;
			}
			// we have some kind of non-local URL
			// try to copy it to local temporary file
			String libraryCacheKey = libUrl.toExternalForm();
			libFile = libraryCache.get(libraryCacheKey);
			if (libFile != null) {
				if (libFile.isFile()) {
					result = libFile.getAbsolutePath();
					break;
				}
				libraryCache.remove(libraryCacheKey);
			}
			if (libraryCache.containsKey(libraryCacheKey)) {
				// already tried to cache this library
				break;
			}
			libFile = cacheLibrary(libUrl, libname);
			if (libFile != null) {
				result = libFile.getAbsolutePath();
				break;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("findLibrary(String): name=" + name //$NON-NLS-1$
					+ ", libname=" + libname //$NON-NLS-1$
					+ ", result=" + result //$NON-NLS-1$
					+ ", this=" + this); //$NON-NLS-1$
		}
		return result;
	}

	protected synchronized File cacheLibrary(final URL libUrl, final String libname) {
		String libraryCacheKey = libUrl.toExternalForm();
		File result = libraryCache.get(libraryCacheKey);
		if (result != null) {
			return result;
		}
		try {
			File cacheFolder = getLibCacheFolder();
			if (cacheFolder == null) {
				throw new IOException("can't initialize libraries cache folder"); //$NON-NLS-1$
			}
			File libCachePluginFolder = new File(cacheFolder, getPluginDescriptor().getUniqueId());
			if (!libCachePluginFolder.exists() && !libCachePluginFolder.mkdirs()) {
				throw new IOException("can't create cache folder " //$NON-NLS-1$
						+ libCachePluginFolder);
			}
			result = new File(libCachePluginFolder, libname);
			InputStream in = IoUtil.getResourceInputStream(libUrl);
			try {
				OutputStream out = new BufferedOutputStream(new FileOutputStream(result));
				try {
					IoUtil.copyStream(in, out, 512);
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
			if (log.isDebugEnabled()) {
				log.debug("library " + libname //$NON-NLS-1$
						+ " successfully cached from URL " + libUrl //$NON-NLS-1$
						+ " and saved to local file " + result); //$NON-NLS-1$
			}
		} catch (IOException ioe) {
			log.error("can't cache library " + libname //$NON-NLS-1$
					+ " from URL " + libUrl, ioe); //$NON-NLS-1$
			result = null;
		}
		libraryCache.put(libraryCacheKey, result);
		return result;
	}

	/**
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	@Override
	public URL findResource(final String name) {
		return findResource(name, this, null);
	}

	/**
	 * @see java.lang.ClassLoader#findResources(java.lang.String)
	 */
	@Override
	public Enumeration<URL> findResources(final String name) throws IOException {
		final List<URL> result = new LinkedList<URL>();
		findResources(result, name, this, null);
		return Collections.enumeration(result);
	}

	protected URL findResource(final String name, final StandardPluginClassLoader requestor, final Set<String> seenPlugins) {
		Set<String> seen = seenPlugins;
		if ((seen != null) && seen.contains(getPluginDescriptor().getId())) {
			return null;
		}
		URL result = super.findResource(name);
		if (result != null) { // found resource in this plug-in class path
			if (log.isDebugEnabled()) {
				log.debug("findResource(...): resource found in classpath, name=" //$NON-NLS-1$
						+ name + " URL=" + result + ", this=" //$NON-NLS-1$ //$NON-NLS-2$
						+ this + ", requestor=" + requestor); //$NON-NLS-1$
			}
			if (isResourceVisible(name, result, requestor)) {
				return result;
			}
			return null;
		}
		if (resourceLoader != null) {
			result = resourceLoader.findResource(name);
			if (result != null) { // found resource in this plug-in resource
				// libraries
				if (log.isDebugEnabled()) {
					log.debug("findResource(...): resource found in libraries, name=" //$NON-NLS-1$
							+ name + ", URL=" + result + ", this=" //$NON-NLS-1$ //$NON-NLS-2$
							+ this + ", requestor=" + requestor); //$NON-NLS-1$
				}
				if (isResourceVisible(name, result, requestor)) {
					return result;
				}
				return null;
			}
		}
		if (seen == null) {
			seen = new HashSet<String>();
		}
		if (log.isDebugEnabled()) {
			log.debug("findResource(...): resource not found, name=" //$NON-NLS-1$
					+ name + ", this=" //$NON-NLS-1$
					+ this + ", requestor=" + requestor); //$NON-NLS-1$
		}
		seen.add(getPluginDescriptor().getId());
		for (PluginDescriptor element : publicImports) {
			if (seen.contains(element.getId())) {
				continue;
			}
			result = ((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).findResource(name, requestor, seen);
			if (result != null) {
				break; // found resource in publicly imported plug-in
			}
		}
		if ((this == requestor) && (result == null)) {
			for (PluginDescriptor element : privateImports) {
				if (seen.contains(element.getId())) {
					continue;
				}
				result = ((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).findResource(name, requestor, seen);
				if (result != null) {
					break; // found resource in privately imported plug-in
				}
			}
		}
		if (result == null) {
			for (PluginDescriptor element : reverseLookups) {
				if (seen.contains(element.getId())) {
					continue;
				}
				result = ((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).findResource(name, requestor, seen);
				if (result != null) {
					break; // found resource in plug-in that marks itself as
					// allowed reverse look up
				}
			}
		}
		return result;
	}

	protected void findResources(final List<URL> result, final String name, final StandardPluginClassLoader requestor, final Set<String> seenPlugins) throws IOException {
		Set<String> seen = seenPlugins;
		if ((seen != null) && seen.contains(getPluginDescriptor().getId())) {
			return;
		}
		URL url;
		for (Enumeration<URL> enm = super.findResources(name); enm.hasMoreElements();) {
			url = enm.nextElement();
			if (isResourceVisible(name, url, requestor)) {
				result.add(url);
			}
		}
		if (resourceLoader != null) {
			for (Enumeration<URL> enm = resourceLoader.findResources(name); enm.hasMoreElements();) {
				url = enm.nextElement();
				if (isResourceVisible(name, url, requestor)) {
					result.add(url);
				}
			}
		}
		if (seen == null) {
			seen = new HashSet<String>();
		}
		seen.add(getPluginDescriptor().getId());
		for (PluginDescriptor element : publicImports) {
			if (seen.contains(element.getId())) {
				continue;
			}
			((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).findResources(result, name, requestor, seen);
		}
		if (this == requestor) {
			for (PluginDescriptor element : privateImports) {
				if (seen.contains(element.getId())) {
					continue;
				}
				((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).findResources(result, name, requestor, seen);
			}
		}
		for (PluginDescriptor element : reverseLookups) {
			if (seen.contains(element.getId())) {
				continue;
			}
			((StandardPluginClassLoader) getPluginManager().getPluginClassLoader(element)).findResources(result, name, requestor, seen);
		}
	}

	protected boolean isResourceVisible(final String name, final URL url, final StandardPluginClassLoader requestor) {
		if (this == requestor) {
			return true;
		}
		URL lib;
		try {
			String file = url.getFile();
			lib = new URL(url.getProtocol(), url.getHost(), file.substring(0, file.length() - name.length()));
		} catch (MalformedURLException mue) {
			log.error("can't get resource library URL", mue); //$NON-NLS-1$
			return false;
		}
		ResourceFilter filter = resourceFilters.get(lib.toExternalForm());
		if (filter == null) {
			log.warn("no resource filter found for library " //$NON-NLS-1$
					+ lib + ", name=" + name //$NON-NLS-1$
					+ ", URL=" + url + ", this=" + this //$NON-NLS-1$ //$NON-NLS-2$
					+ ", requestor=" + requestor); //$NON-NLS-1$
			return false;
		}
		if (!filter.isResourceVisible(name)) {
			log.warn("resource not visible, name=" + name //$NON-NLS-1$
					+ ", URL=" + url + ", this=" + this //$NON-NLS-1$ //$NON-NLS-2$
					+ ", requestor=" + requestor); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	protected static final class ResourceFilter {
		private boolean isPublic;

		private final Set<String> entries;

		protected ResourceFilter(final Library lib) {
			entries = new HashSet<String>();
			for (String exportPrefix : lib.getExports()) {
				if ("*".equals(exportPrefix)) { //$NON-NLS-1$
					isPublic = true;
					entries.clear();
					break;
				}
				if (!lib.isCodeLibrary()) {
					exportPrefix = exportPrefix.replace('\\', '.').replace('/', '.');
					if (exportPrefix.startsWith(".")) { //$NON-NLS-1$
						exportPrefix = exportPrefix.substring(1);
					}
				}
				entries.add(exportPrefix);
			}
		}

		protected boolean isClassVisible(final String className) {
			if (isPublic) {
				return true;
			}
			if (entries.isEmpty()) {
				return false;
			}
			if (entries.contains(className)) {
				return true;
			}
			int p = className.lastIndexOf('.');
			if (p == -1) {
				return false;
			}
			return entries.contains(className.substring(0, p) + ".*"); //$NON-NLS-1$
		}

		protected boolean isResourceVisible(final String resPath) {
			// quick check
			if (isPublic) {
				return true;
			}
			if (entries.isEmpty()) {
				return false;
			}
			// translate "path spec" -> "full class name"
			String str = resPath.replace('\\', '.').replace('/', '.');
			if (str.startsWith(".")) { //$NON-NLS-1$
				str = str.substring(1);
			}
			if (str.endsWith(".")) { //$NON-NLS-1$
				str = str.substring(0, str.length() - 1);
			}
			return isClassVisible(str);
		}
	}

	static class PluginResourceLoader extends URLClassLoader {
		private static Log logger = LogFactory.getLog(PluginResourceLoader.class);

		static PluginResourceLoader get(final PluginManager manager, final PluginDescriptor descr) {
			final List<URL> urls = new LinkedList<URL>();
			for (Library lib : descr.getLibraries()) {
				if (lib.isCodeLibrary())
					continue;

				urls.add(manager.getPathResolver().resolvePath(lib, lib.getPath()));
			}
			if (logger.isDebugEnabled()) {
				StringBuilder buf = new StringBuilder();
				buf.append("Resource URL's populated for plug-in " + descr //$NON-NLS-1$
						+ ":\r\n"); //$NON-NLS-1$
				for (URL url : urls) {
					buf.append("\t"); //$NON-NLS-1$
					buf.append(url);
					buf.append("\r\n"); //$NON-NLS-1$
				}
				logger.trace(buf.toString());
			}
			if (urls.isEmpty()) {
				return null;
			}
			return AccessController.<PluginResourceLoader> doPrivileged(new PrivilegedAction<PluginResourceLoader>() {
				public PluginResourceLoader run() {
					return new PluginResourceLoader(urls.toArray(new URL[urls.size()]));
				}
			});
		}

		/**
		 * Creates loader instance configured to load resources only from given
		 * URLs.
		 * 
		 * @param urls
		 *            array of resource URLs
		 */
		PluginResourceLoader(final URL[] urls) {
			super(urls);
		}

		/**
		 * @see java.lang.ClassLoader#findClass(java.lang.String)
		 */
		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			throw new ClassNotFoundException(name);
		}

		/**
		 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
		 */
		@Override
		protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
			throw new ClassNotFoundException(name);
		}
	}
}
