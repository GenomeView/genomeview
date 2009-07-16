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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.JpfException;
import org.java.plugin.PathResolver;
import org.java.plugin.Plugin;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.PluginPrerequisite;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.registry.PluginRegistry.RegistryChangeData;
import org.java.plugin.registry.PluginRegistry.RegistryChangeListener;

/**
 * Standard implementation of plug-in manager.
 * 
 * @version $Id: StandardPluginManager.java,v 1.8 2007/04/07 12:41:01 ddimon Exp $
 */
public final class StandardPluginManager extends PluginManager {
    Log log = LogFactory.getLog(getClass());
    private final PathResolver pathResolver;
    private final PluginRegistry registry;
    private final PluginLifecycleHandler lifecycleHandler;
    private final Map<String, Plugin> activePlugins =
        new HashMap<String, Plugin>();
    private final Set<String> activatingPlugins = new HashSet<String>();
    private final Set<String> badPlugins = new HashSet<String>();
    private final List<String> activationLog = new LinkedList<String>();
    private final Map<String, PluginClassLoader> classLoaders =
        new HashMap<String, PluginClassLoader>();
    private final Set<String> disabledPlugins = new HashSet<String>();
    private final List<EventListener> listeners =
        Collections.synchronizedList(new LinkedList<EventListener>());
    private RegistryChangeListener registryChangeListener;
    private Map<String, URL> notRegisteredPluginLocations =
        new HashMap<String, URL>();

    /**
     * Creates instance of plug-in manager for given registry, path resolver and
     * life cycle handler.
     * 
     * @param aRegistry
     *            some implementation of plug-in registry interface
     * @param aPathResolver
     *            some implementation of path resolver interface
     * @param aLifecycleHandler
     *            an implementation of plug-in life cycle handler
     * 
     * @see StandardObjectFactory
     */
    protected StandardPluginManager(final PluginRegistry aRegistry,
            final PathResolver aPathResolver,
            final PluginLifecycleHandler aLifecycleHandler) {
        registry = aRegistry;
        pathResolver = aPathResolver;
        lifecycleHandler = aLifecycleHandler;
        lifecycleHandler.init(this);
        registryChangeListener = new RegistryChangeListener() {
            public void registryChanged(final RegistryChangeData data) {
                registryChangeHandler(data);
            }
        };
        registry.registerListener(registryChangeListener);
    }

    /**
     * @see org.java.plugin.PluginManager#getRegistry()
     */
    @Override
    public PluginRegistry getRegistry() {
        return registry;
    }

    /**
     * @see org.java.plugin.PluginManager#getPathResolver()
     */
    @Override
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    /**
     * Method to handle plug-in registry change events.
     * 
     * @param data
     *            registry change data holder
     */
    synchronized void registryChangeHandler(final RegistryChangeData data) {
        badPlugins.clear();
        for (String id : data.removedPlugins()) {
            deactivatePlugin(id);
            pathResolver.unregisterContext(id);
        }
        URL location;
        for (PluginDescriptor idt : registry.getPluginDescriptors()) {
            location = notRegisteredPluginLocations.remove(
                    idt.getLocation().toExternalForm());
            if (location != null) {
                pathResolver.registerContext(idt, location);
            }
        }
        for (PluginFragment idt : registry.getPluginFragments()) {
            location = notRegisteredPluginLocations.remove(
                    idt.getLocation().toExternalForm());
            if (location != null) {
                pathResolver.registerContext(idt, location);
            }
        }
        for (String id : data.modifiedPlugins()) {
            if (activePlugins.containsKey(id)) {
                deactivatePlugin(id);
                try {
                    activatePlugin(id);
                } catch (Exception e) {
                    log.error("failed activating modified plug-in " + id, e); //$NON-NLS-1$
                }
            } else {
                PluginClassLoader clsLoader = classLoaders.get(id);
                if (clsLoader != null) {
                    notifyClassLoader(clsLoader);
                }
            }
        }
    }

    /**
     * Registers plug-ins and their locations with this plug-in manager. You
     * should use this method to register new plug-ins to make them available
     * for activation with this manager instance (compare this to
     * {@link PluginRegistry#register(URL[])} method that just makes plug-in's
     * meta-data available for reading and doesn't "know" where are things
     * actually located).
     * 
     * @param locations
     *            plug-in locations data
     * @return map where keys are manifest URL's and values are registered
     *         plug-ins or plug-in fragments, URL's for unprocessed manifests
     *         are not included
     * @throws JpfException
     *             if given plug-ins can't be registered or published (optional
     *             behavior)
     */
    @Override
    public Map<String, Identity> publishPlugins(final PluginLocation[] locations)
            throws JpfException {
        LinkedList<URL> manifests = new LinkedList<URL>();
        for (PluginLocation location : locations) {
            URL manifest = location.getManifestLocation();
            manifests.add(manifest);
            notRegisteredPluginLocations.put(
                    manifest.toExternalForm(), location.getContextLocation());
        }
        return registry.register(manifests.toArray(new URL[manifests.size()]));
    }

    /**
     * Looks for plug-in with given ID and activates it if it is not activated
     * yet. Note that this method will never return <code>null</code>.
     * 
     * @param id
     *            plug-in ID
     * @return found plug-in
     * @throws PluginLifecycleException
     *             if plug-in can't be found or activated
     */
    @Override
    public Plugin getPlugin(final String id) throws PluginLifecycleException {
        Plugin result = activePlugins.get(id);
        if (result != null) {
            return result;
        }
        if (badPlugins.contains(id)) {
            throw new IllegalArgumentException("plug-in " + id //$NON-NLS-1$
                    + " disabled internally as it wasn't properly initialized"); //$NON-NLS-1$
        }
        if (disabledPlugins.contains(id)) {
            throw new IllegalArgumentException("plug-in " + id //$NON-NLS-1$
                    + " disabled externally"); //$NON-NLS-1$
        }
        PluginDescriptor descr = registry.getPluginDescriptor(id);
        if (descr == null) {
            throw new IllegalArgumentException("unknown plug-in ID - " + id); //$NON-NLS-1$
        }
        return activatePlugin(descr);
    }

    /**
     * Activates plug-in with given ID if it is not activated yet.
     * 
     * @param id
     *            plug-in ID
     * @throws PluginLifecycleException
     *             if plug-in can't be found or activated
     */
    @Override
    public void activatePlugin(final String id) throws PluginLifecycleException {
        if (activePlugins.containsKey(id)) {
            return;
        }
        if (badPlugins.contains(id)) {
            throw new IllegalArgumentException("plug-in " + id //$NON-NLS-1$
                    + " disabled internally as it wasn't properly initialized"); //$NON-NLS-1$
        }
        if (disabledPlugins.contains(id)) {
            throw new IllegalArgumentException("plug-in " + id //$NON-NLS-1$
                    + " disabled externally"); //$NON-NLS-1$
        }
        PluginDescriptor descr = registry.getPluginDescriptor(id);
        if (descr == null) {
            throw new IllegalArgumentException("unknown plug-in ID - " + id); //$NON-NLS-1$
        }
        activatePlugin(descr);
    }

    /**
     * Looks for plug-in, given object belongs to.
     * 
     * @param obj
     *            any object that maybe belongs to some plug-in
     * @return plug-in or <code>null</code> if given object doesn't belong to
     *         any plug-in (possibly it is part of "host" application) and thus
     *         doesn't managed by the Framework directly or indirectly
     */
    @Override
    public Plugin getPluginFor(final Object obj) {
        if (obj == null) {
            return null;
        }
        ClassLoader clsLoader;
        if (obj instanceof Class) {
            clsLoader = ((Class) obj).getClassLoader();
        } else if (obj instanceof ClassLoader) {
            clsLoader = (ClassLoader) obj;
        } else {
            clsLoader = obj.getClass().getClassLoader();
        }
        if (!(clsLoader instanceof PluginClassLoader)) {
            return null;
        }
        PluginDescriptor descr = ((PluginClassLoader) clsLoader)
                .getPluginDescriptor();
        Plugin result = activePlugins.get(descr.getId());
        if (result != null) {
            return result;
        }
        throw new IllegalStateException("can't get plug-in " + descr); //$NON-NLS-1$
    }

    /**
     * @param descr
     *            plug-in descriptor
     * @return <code>true</code> if plug-in with given descriptor is activated
     */
    @Override
    public boolean isPluginActivated(final PluginDescriptor descr) {
        return activePlugins.containsKey(descr.getId());
    }

    /**
     * @param descr
     *            plug-in descriptor
     * @return <code>true</code> if plug-in disabled as it's activation fails
     */
    @Override
    public boolean isBadPlugin(final PluginDescriptor descr) {
        return badPlugins.contains(descr.getId());
    }

    /**
     * @param descr
     *            plug-in descriptor
     * @return <code>true</code> if plug-in is currently activating
     */
    @Override
    public boolean isPluginActivating(final PluginDescriptor descr) {
        return activatingPlugins.contains(descr.getId());
    }

    /**
     * Returns instance of plug-in's class loader and not tries to activate
     * plug-in. Use this method if you need to get access to plug-in resources
     * and don't want to cause plug-in activation.
     * 
     * @param descr
     *            plug-in descriptor
     * @return class loader instance for plug-in with given descriptor
     */
    @Override
    public PluginClassLoader getPluginClassLoader(final PluginDescriptor descr) {
        if (badPlugins.contains(descr.getId())) {
            throw new IllegalArgumentException("plug-in " + descr.getId() //$NON-NLS-1$
                    + " disabled internally as it wasn't properly initialized"); //$NON-NLS-1$
        }
        if (disabledPlugins.contains(descr.getId())) {
            throw new IllegalArgumentException("plug-in " + descr.getId() //$NON-NLS-1$
                    + " disabled externally"); //$NON-NLS-1$
        }
        PluginClassLoader result = classLoaders.get(descr.getId());
        if (result != null) {
            return result;
        }
        synchronized (this) {
            result = classLoaders.get(descr.getId());
            if (result != null) {
                return result;
            }
            result = lifecycleHandler.createPluginClassLoader(descr);
            classLoaders.put(descr.getId(), result);
        }
        return result;
    }

    /**
     * Shuts down the framework. <br>
     * Calling this method will deactivate all active plug-ins in order, reverse
     * to the order they was activated. It also releases all resources allocated
     * by this manager (class loaders, plug-in descriptors etc.). All disabled
     * plug-ins will be marked as "enabled", all registered event listeners will
     * be unregistered.
     */
    @Override
    public synchronized void shutdown() {
        log.debug("shutting down..."); //$NON-NLS-1$
        dump();
        registry.unregisterListener(registryChangeListener);
        final List<String> reversedLog = new ArrayList<String>(activationLog);
        Collections.reverse(reversedLog);
        for (String id : reversedLog) {
            PluginDescriptor descr = registry.getPluginDescriptor(id);
            if (descr == null) {
                log.warn("can't find descriptor for plug-in " + id //$NON-NLS-1$
                        + " to deactivate plug-in", new Exception( //$NON-NLS-1$
                        "fake exception to view stack trace")); //$NON-NLS-1$
                continue;
            }
            deactivatePlugin(descr);
        }
        dump();
        classLoaders.clear();
        disabledPlugins.clear();
        listeners.clear();
        lifecycleHandler.dispose();
        log.info("shutdown done"); //$NON-NLS-1$
    }

    private synchronized Plugin activatePlugin(final PluginDescriptor descr)
            throws PluginLifecycleException {
        Plugin result = activePlugins.get(descr.getId());
        if (result != null) {
            return result;
        }
        if (badPlugins.contains(descr.getId())) {
            throw new IllegalArgumentException("plug-in " + descr.getId() //$NON-NLS-1$
                    + " disabled as it wasn't properly initialized"); //$NON-NLS-1$
        }
        if (activatingPlugins.contains(descr.getId())) {
            throw new PluginLifecycleException(
                    StandardObjectFactory.PACKAGE_NAME,
                    "pluginActivating", descr.getId()); //$NON-NLS-1$
        }
        activatingPlugins.add(descr.getId());
        try {
            try {
                checkPrerequisites(descr);
                String pluginClassName = descr.getPluginClassName();
                if ((pluginClassName == null)
                        || (pluginClassName.trim().length() == 0)) {
                    result = new EmptyPlugin();
                } else {
                    result = lifecycleHandler.createPluginInstance(descr);
                }
                initPlugin(result, descr);
                lifecycleHandler.beforePluginStart(result);
                startPlugin(result);
            } catch (PluginLifecycleException ple) {
                badPlugins.add(descr.getId());
                classLoaders.remove(descr.getId());
                throw ple;
            } catch (Exception e) {
                badPlugins.add(descr.getId());
                classLoaders.remove(descr.getId());
                throw new PluginLifecycleException(
                        StandardObjectFactory.PACKAGE_NAME,
                        "pluginStartFailed", descr.getUniqueId(), e); //$NON-NLS-1$
            }
            activePlugins.put(descr.getId(), result);
            activationLog.add(descr.getId());
            log.info("plug-in started - " + descr.getUniqueId() //$NON-NLS-1$
                    + " (active/total: " + activePlugins.size() //$NON-NLS-1$
                    + " of "  //$NON-NLS-1$
                    + registry.getPluginDescriptors().size() + ")"); //$NON-NLS-1$
            fireEvent(result, true);
            return result;
        } finally {
            activatingPlugins.remove(descr.getId());
        }
    }

    private void checkPrerequisites(final PluginDescriptor descr)
            throws PluginLifecycleException {
        for (PluginPrerequisite pre : descr.getPrerequisites()) {
            if (activatingPlugins.contains(pre.getPluginId())) {
                log.warn("dependencies loop detected during " //$NON-NLS-1$
                        + "activation of plug-in " + descr, new Exception( //$NON-NLS-1$
                        "fake exception to view stack trace")); //$NON-NLS-1$
                continue;
            }
            if (badPlugins.contains(pre.getPluginId())) {
                if (pre.isOptional()) {
                    continue;
                }
                throw new PluginLifecycleException(
                        StandardObjectFactory.PACKAGE_NAME,
                        "pluginPrerequisiteBad", //$NON-NLS-1$
                        new Object[] { descr.getId(), pre.getPluginId() });
            }
            if (disabledPlugins.contains(pre.getPluginId())) {
                if (pre.isOptional()) {
                    continue;
                }
                throw new PluginLifecycleException(
                        StandardObjectFactory.PACKAGE_NAME,
                        "pluginPrerequisiteDisabled", //$NON-NLS-1$
                        new Object[] { descr.getId(), pre.getPluginId() });
            }
            if (!pre.matches()) {
                if (pre.isOptional()) {
                    continue;
                }
                throw new PluginLifecycleException(
                        StandardObjectFactory.PACKAGE_NAME,
                        "pluginPrerequisiteNotMatches", //$NON-NLS-1$
                        new Object[] { descr.getId(), pre.getPluginId() });
            }
            try {
                activatePlugin(registry.getPluginDescriptor(pre.getPluginId()));
            } catch (PluginLifecycleException ple) {
                if (pre.isOptional()) {
                    log.warn("failed activating optional plug-in from" //$NON-NLS-1$
                            + " prerequisite " + pre, ple); //$NON-NLS-1$
                    continue;
                }
                throw ple;
            }
        }
    }

    /**
     * Deactivates plug-in with given ID if it has been successfully activated
     * before. Note that this method will effectively deactivate all plug-ins
     * that depend on the given plug-in.
     * 
     * @param id
     *            plug-in ID
     */
    @Override
    public void deactivatePlugin(final String id) {
        if (!activePlugins.containsKey(id)) {
            return;
        }
        PluginDescriptor descr = registry.getPluginDescriptor(id);
        if (descr == null) {
            throw new IllegalArgumentException("unknown plug-in ID - " + id); //$NON-NLS-1$
        }
        // Collect depending plug-ins
        final Map<String, PluginDescriptor> dependingPluginsMap = new HashMap<String, PluginDescriptor>();
        for (PluginDescriptor dependingPlugin : registry
                .getDependingPlugins(descr)) {
            dependingPluginsMap.put(dependingPlugin.getId(), dependingPlugin);
        }
        // Prepare list of plug-ins to be deactivated in correct order
        final List<PluginDescriptor> tobeDeactivated = new LinkedList<PluginDescriptor>();
        final List<String> reversedLog = new ArrayList<String>(activationLog);
        Collections.reverse(reversedLog);
        for (String pluginId : reversedLog) {
            if (pluginId.equals(descr.getId())) {
                tobeDeactivated.add(descr);
            } else if (dependingPluginsMap.containsKey(pluginId)) {
                tobeDeactivated.add(dependingPluginsMap.get(pluginId));
            }
        }
        // Deactivate plug-ins
        for (PluginDescriptor descriptor : tobeDeactivated) {
            deactivatePlugin(descriptor);
        }
        dump();
    }

    private synchronized void deactivatePlugin(final PluginDescriptor descr) {
        Plugin plugin = activePlugins.remove(descr.getId());
        if (plugin != null) {
            try {
                if (plugin.isActive()) {
                    fireEvent(plugin, false);
                    stopPlugin(plugin);
                    lifecycleHandler.afterPluginStop(plugin);
                    log.info("plug-in stopped - " + descr.getUniqueId() //$NON-NLS-1$
                            + " (active/total: " + activePlugins.size() //$NON-NLS-1$
                            + " of " //$NON-NLS-1$
                            + registry.getPluginDescriptors().size() + ")"); //$NON-NLS-1$
                } else {
                    log.warn("plug-in " + descr.getUniqueId() //$NON-NLS-1$
                            + " is not active although present in active " //$NON-NLS-1$
                            + "plug-ins list", new Exception( //$NON-NLS-1$
                            "fake exception to view stack trace")); //$NON-NLS-1$
                }
            } catch (Exception e) {
                log.error("error while stopping plug-in " //$NON-NLS-1$
                        + descr.getUniqueId(), e);
            }
        }
        PluginClassLoader clsLoader = classLoaders.remove(descr.getId());
        if (clsLoader != null) {
            disposeClassLoader(clsLoader);
        }
        badPlugins.remove(descr.getId());
        activationLog.remove(descr.getId());
    }

    private void dump() {
        if (!log.isDebugEnabled()) {
            return;
        }
        StringBuilder buf = new StringBuilder("PLUGIN MANAGER DUMP:\r\n"); //$NON-NLS-1$
        buf.append("-------------- DUMP BEGIN -----------------\r\n"); //$NON-NLS-1$
        buf.append("\tActive plug-ins: " + activePlugins.size()) //$NON-NLS-1$
                .append("\r\n"); //$NON-NLS-1$
        for (Plugin plugin : activePlugins.values()) {
            buf.append("\t\t") //$NON-NLS-1$
                    .append(plugin).append("\r\n"); //$NON-NLS-1$
        }
        buf.append("\tActivating plug-ins: " //$NON-NLS-1$
                + activatingPlugins.size()).append("\r\n"); //$NON-NLS-1$
        for (String s : activatingPlugins) {
            buf.append("\t\t") //$NON-NLS-1$
                    .append(s).append("\r\n"); //$NON-NLS-1$
        }
        buf.append("\tPlug-ins with instantiated class loaders: " //$NON-NLS-1$
                + classLoaders.size()).append("\r\n"); //$NON-NLS-1$
        for (String s : classLoaders.keySet()) {
            buf.append("\t\t") //$NON-NLS-1$
                    .append(s).append("\r\n"); //$NON-NLS-1$
        }
        buf.append("\tDisabled plug-ins: " + disabledPlugins.size()) //$NON-NLS-1$
                .append("\r\n"); //$NON-NLS-1$
        for (String s : disabledPlugins) {
            buf.append("\t\t") //$NON-NLS-1$
                    .append(s).append("\r\n"); //$NON-NLS-1$
        }
        buf.append("\tBad plug-ins: " + badPlugins.size()) //$NON-NLS-1$
                .append("\r\n"); //$NON-NLS-1$
        for (String s : badPlugins) {
            buf.append("\t\t") //$NON-NLS-1$
                    .append(s).append("\r\n"); //$NON-NLS-1$
        }
        buf.append("\tActivation log: " + activationLog.size()) //$NON-NLS-1$
                .append("\r\n"); //$NON-NLS-1$
        for (String s : activationLog) {
            buf.append("\t\t") //$NON-NLS-1$
                    .append(s).append("\r\n"); //$NON-NLS-1$
        }
        buf.append("Memory TOTAL/FREE/MAX: ") //$NON-NLS-1$
                .append(Runtime.getRuntime().totalMemory()).append("/") //$NON-NLS-1$
                .append(Runtime.getRuntime().freeMemory()).append("/") //$NON-NLS-1$
                .append(Runtime.getRuntime().maxMemory()).append("\r\n"); //$NON-NLS-1$
        buf.append("-------------- DUMP END -----------------"); //$NON-NLS-1$
        log.debug(buf.toString());
    }

    /**
     * Disables plug-in (with dependencies) in this manager instance. Disabled
     * plug-in can't be activated although it may be valid and successfully
     * registered with plug-in registry. Before disabling, plug-in will be
     * deactivated if it was successfully activated. <br>
     * Be careful with this method as it can effectively disable large set of
     * inter-depending plug-ins and your application may become unstable or even
     * disabled as whole.
     * 
     * @param descr
     *            descriptor of plug-in to be disabled
     * @return descriptors of plug-ins that was actually disabled
     */
    @Override
    public PluginDescriptor[] disablePlugin(final PluginDescriptor descr) {
        final List<PluginDescriptor> result = new LinkedList<PluginDescriptor>();
        if (!disabledPlugins.contains(descr.getId())) {
            deactivatePlugin(descr);
            fireEvent(descr, false);
            disabledPlugins.add(descr.getId());
            result.add(descr);
        }

        for (PluginDescriptor dependedPlugin : registry
                .getDependingPlugins(descr)) {
            if (!disabledPlugins.contains(dependedPlugin.getId())) {
                deactivatePlugin(dependedPlugin);
                fireEvent(dependedPlugin, false);
                disabledPlugins.add(dependedPlugin.getId());
                result.add(dependedPlugin);
            }
        }
        return result.toArray(new PluginDescriptor[result.size()]);
    }

    /**
     * Enables plug-in (or plug-ins) in this manager instance.
     * 
     * @param descr
     *            descriptor of plug-in to be enabled
     * @param includeDependings
     *            if <code>true</code>, depending plug-ins will be also
     *            enabled
     * @return descriptors of plug-ins that was actually enabled
     * @see #disablePlugin(PluginDescriptor)
     */
    @Override
    public PluginDescriptor[] enablePlugin(final PluginDescriptor descr,
            final boolean includeDependings) {
        final List<PluginDescriptor> result = new LinkedList<PluginDescriptor>();
        if (disabledPlugins.contains(descr.getId())) {
            disabledPlugins.remove(descr.getId());
            fireEvent(descr, true);
            result.add(descr);
        }
        if (includeDependings) {
            for (PluginDescriptor dependedPlugin : registry
                    .getDependingPlugins(descr)) {
                if (disabledPlugins.contains(dependedPlugin.getId())) {
                    disabledPlugins.remove(dependedPlugin.getId());
                    fireEvent(dependedPlugin, true);
                    result.add(dependedPlugin);
                }
            }
        }
        return result.toArray(new PluginDescriptor[result.size()]);
    }

    /**
     * @param descr
     *            plug-in descriptor
     * @return <code>true</code> if given plug-in is disabled in this manager
     */
    @Override
    public boolean isPluginEnabled(final PluginDescriptor descr) {
        return !disabledPlugins.contains(descr.getId());
    }

    /**
     * Registers plug-in manager event listener. If given listener has been
     * registered before, this method will throw an
     * {@link IllegalArgumentException}.
     * 
     * @param listener
     *            new manager event listener
     */
    @Override
    public void registerListener(final EventListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("listener " + listener //$NON-NLS-1$
                    + " already registered"); //$NON-NLS-1$
        }
        listeners.add(listener);
    }

    /**
     * Unregisters manager event listener. If given listener hasn't been
     * registered before, this method will throw an
     * {@link IllegalArgumentException}.
     * 
     * @param listener
     *            registered listener
     */
    @Override
    public void unregisterListener(final EventListener listener) {
        if (!listeners.remove(listener)) {
            log.warn("unknown listener " + listener); //$NON-NLS-1$
        }
    }

    private void fireEvent(final Object data, final boolean on) {
        if (listeners.isEmpty()) {
            return;
        }
        // make local copy
        EventListener[] arr = listeners.toArray(new EventListener[listeners
                .size()]);
        // propagate event basing on given data type and on/off flag
        // NB: revise this logic if EventListener members are changed
        if (data instanceof PluginDescriptor) {
            PluginDescriptor descr = (PluginDescriptor) data;
            if (on) {
                if (log.isDebugEnabled()) {
                    log.debug("propagating \"pluginEnabled\" event for " //$NON-NLS-1$
                            + descr);
                }
                for (EventListener element : arr) {
                    element.pluginEnabled(descr);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("propagating \"pluginDisabled\" event for " //$NON-NLS-1$
                            + descr);
                }
                for (EventListener element : arr) {
                    element.pluginDisabled(descr);
                }
            }
        } else {
            Plugin plugin = (Plugin) data;
            if (on) {
                if (log.isDebugEnabled()) {
                    log.debug("propagating \"pluginActivated\" event for " //$NON-NLS-1$
                            + plugin);
                }
                for (EventListener element : arr) {
                    element.pluginActivated(plugin);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("propagating \"pluginDeactivated\" event for " //$NON-NLS-1$
                            + plugin);
                }
                for (EventListener element : arr) {
                    element.pluginDeactivated(plugin);
                }
            }
        }
    }

    static final class EmptyPlugin extends Plugin {
        /**
         * @see org.java.plugin.Plugin#doStart()
         */
        @Override
        protected void doStart() throws Exception {
            // no-op
        }

        /**
         * @see org.java.plugin.Plugin#doStop()
         */
        @Override
        protected void doStop() throws Exception {
            // no-op
        }
    }
}
