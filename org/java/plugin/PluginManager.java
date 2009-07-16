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
package org.java.plugin;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.java.plugin.registry.Identity;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;

/**
 * JPF "runtime" class - the entry point to the framework API. It is expected
 * that only one instance of this class will be created per application (other
 * scenarios may be possible but not tested).
 * <p>
 * Usage example. Somewhere in the beginning of your application:
 * 
 * <pre>
 * manager = factory.createManager();
 * manager.publishPlugins(getLocations(dir));
 * </pre>
 * 
 * Later on, before accessing plug-in:
 * 
 * <pre>
 * manager.activatePlugin(pluginId);
 * </pre>
 * 
 * @see org.java.plugin.ObjectFactory#createManager()
 * 
 * @version $Id: PluginManager.java,v 1.5 2007/04/07 12:42:14 ddimon Exp $
 */
public abstract class PluginManager {
    /**
     * JPF version number.
     */
    //NB: don't forget to update version number with new release
    public static final String VERSION = "1.5.1"; //$NON-NLS-1$
    
    /**
     * JPF version system property name.
     */
    public static final String VERSION_PROPERTY =
        "org.java.plugin.jpf-version"; //$NON-NLS-1$
    
    static {
        try {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    System.setProperty(VERSION_PROPERTY, VERSION);
                    return null;
                }
            });
        } catch (SecurityException se) {
            // ignore
        }
    }
    
    /**
     * Looks for plug-in manager instance for given object.
     * 
     * @param obj
     *            any object that may be managed by some plug-in manager
     * @return plug-in manager instance or <code>null</code> if given object
     *         doesn't belong to any plug-in (possibly it is part of "host"
     *         application) and thus doesn't managed by the Framework directly
     *         or indirectly.
     */
    public static PluginManager lookup(final Object obj) {
        if (obj == null) {
            return null;
        }
        ClassLoader clsLoader;
        if (obj instanceof Plugin) {
            return ((Plugin) obj).getManager();
        } else if (obj instanceof Class) {
            clsLoader = ((Class) obj).getClassLoader();
        } else if (obj instanceof ClassLoader) {
            clsLoader = (ClassLoader) obj;
        } else {
            clsLoader = obj.getClass().getClassLoader();
        }
        if (!(clsLoader instanceof PluginClassLoader)) {
            return lookup(clsLoader.getParent());
        }
        return ((PluginClassLoader) clsLoader).getPluginManager();
    }

    /**
     * @return registry, used by this manager
     */
    public abstract PluginRegistry getRegistry();

    /**
     * @return path resolver
     */
    public abstract PathResolver getPathResolver();

    /**
     * Registers plug-ins and their locations with this plug-in manager. You
     * should use this method to register new plug-ins to make them available
     * for activation with this manager instance (compare this to
     * {@link PluginRegistry#register(URL[])} method that just makes plug-in's
     * meta-data available for reading and doesn't "know" where are things
     * actually located).
     * <p>
     * Note that this method only load plug-ins to this manager but not activate
     * them. Call {@link #activatePlugin(String)} method to make plug-in
     * activated. It is recommended to do this immediately before first plug-in
     * use.
     * 
     * @param locations
     *            plug-in locations data
     * @return map where keys are manifest URL's and values are registered
     *         plug-ins or plug-in fragments, URL's for unprocessed manifests
     *         are not included
     * @throws JpfException
     *             if given plug-ins can't be registered or published (optional
     *             behavior)
     * 
     * @see org.java.plugin.registry.PluginDescriptor
     * @see org.java.plugin.registry.PluginFragment
     */
    public abstract Map<String, Identity> publishPlugins(
            final PluginLocation[] locations) throws JpfException;

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
    public abstract Plugin getPlugin(final String id)
            throws PluginLifecycleException;

    /**
     * Activates plug-in with given ID if it is not activated yet. Actually this
     * makes plug-in "running" and calls {@link Plugin#doStart()} method. This
     * method will effectively activate all depending plug-ins. It is safe to
     * call this method more than once.
     * 
     * @param id
     *            plug-in ID
     * @throws PluginLifecycleException
     *             if plug-in can't be found or activated
     */
    public abstract void activatePlugin(final String id)
            throws PluginLifecycleException;

    /**
     * Looks for plug-in, given object belongs to.
     * 
     * @param obj
     *            any object that maybe belongs to some plug-in
     * @return plug-in or <code>null</code> if given object doesn't belong to
     *         any plug-in (possibly it is part of "host" application) and thus
     *         doesn't managed by the Framework directly or indirectly
     */
    public abstract Plugin getPluginFor(final Object obj);

    /**
     * @param descr
     *            plug-in descriptor
     * @return <code>true</code> if plug-in with given descriptor is activated
     */
    public abstract boolean isPluginActivated(final PluginDescriptor descr);

    /**
     * @param descr
     *            plug-in descriptor
     * @return <code>true</code> if plug-in disabled as it's activation fails
     */
    public abstract boolean isBadPlugin(final PluginDescriptor descr);

    /**
     * @param descr
     *            plug-in descriptor
     * @return <code>true</code> if plug-in is currently activating
     */
    public abstract boolean isPluginActivating(final PluginDescriptor descr);

    /**
     * Returns instance of plug-in's class loader and not tries to activate
     * plug-in. Use this method if you need to get access to plug-in resources
     * and don't want to cause plug-in activation.
     * 
     * @param descr
     *            plug-in descriptor
     * @return class loader instance for plug-in with given descriptor
     */
    public abstract PluginClassLoader getPluginClassLoader(
            final PluginDescriptor descr);

    /**
     * Shuts down the framework. <br>
     * Calling this method will deactivate all active plug-ins in order, reverse
     * to the order they was activated. It also releases all resources allocated
     * by this manager (class loaders, plug-in descriptors etc.). All disabled
     * plug-ins will be marked as "enabled", all registered event listeners will
     * be unregistered.
     */
    public abstract void shutdown();

    /**
     * Deactivates plug-in with given ID if it has been successfully activated
     * before. This method makes plug-in "not running" and calls
     * {@link Plugin#doStop()} method. Note that this method will effectively
     * deactivate all depending plug-ins.
     * 
     * @param id
     *            plug-in ID
     */
    public abstract void deactivatePlugin(final String id);

    /**
     * Disables plug-in (with dependencies) in this manager instance. Disabled
     * plug-in can't be activated although it may be valid and successfully
     * registered with plug-in registry. Before disabling, plug-in will be
     * deactivated if it was successfully activated.
     * <p>
     * Be careful with this method as it can effectively disable large set of
     * inter-depending plug-ins and your application may become unstable or even
     * disabled as whole.
     * 
     * @param descr
     *            descriptor of plug-in to be disabled
     * @return descriptors of plug-ins that was actually disabled
     */
    public abstract PluginDescriptor[] disablePlugin(
            final PluginDescriptor descr);

    /**
     * Enables plug-in (or plug-ins) in this manager instance. Don't miss this
     * with plug-in activation semantic. Enabled plug-in is simply ready to be
     * activated. By default all loaded plug-ins are enabled.
     * 
     * @param descr
     *            descriptor of plug-in to be enabled
     * @param includeDependings
     *            if <code>true</code>, depending plug-ins will be also
     *            enabled
     * @return descriptors of plug-ins that was actually enabled
     * @see #disablePlugin(PluginDescriptor)
     */
    public abstract PluginDescriptor[] enablePlugin(
            final PluginDescriptor descr, final boolean includeDependings);

    /**
     * @param descr
     *            plug-in descriptor
     * @return <code>true</code> if given plug-in is disabled in this manager
     */
    public abstract boolean isPluginEnabled(final PluginDescriptor descr);

    /**
     * Registers plug-in manager event listener. If given listener has been
     * registered before, this method will throw an
     * {@link IllegalArgumentException}.
     * 
     * @param listener
     *            new manager event listener
     */
    public abstract void registerListener(final EventListener listener);

    /**
     * Unregisters manager event listener. If given listener hasn't been
     * registered before, this method will throw an
     * {@link IllegalArgumentException}.
     * 
     * @param listener
     *            registered listener
     */
    public abstract void unregisterListener(final EventListener listener);

    // Delegating methods

    /**
     * Initializes given plug-in with this manager instance and given
     * descriptor.
     * 
     * @param plugin
     *            plug-in instance to be initialized
     * @param descr
     *            plug-in descriptor
     */
    protected final void initPlugin(final Plugin plugin,
            final PluginDescriptor descr) {
        plugin.setManager(this);
        plugin.setDescriptor(descr);
    }

    /**
     * Starts given plug-in. Simply forward call to {@link Plugin#doStart()}
     * method.
     * 
     * @param plugin
     *            plug-in to be started
     * @throws Exception
     *             if any error has occurred during plug-in start
     */
    protected final void startPlugin(final Plugin plugin) throws Exception {
        plugin.start();
    }

    /**
     * Stops given plug-in. Simply forward call to {@link Plugin#doStop()}
     * method.
     * 
     * @param plugin
     *            plug-in to be stopped
     * @throws Exception
     *             if any error has occurred during plug-in stop
     */
    protected final void stopPlugin(final Plugin plugin) throws Exception {
        plugin.stop();
    }

    /**
     * Forwards call to {@link PluginClassLoader#dispose()} method.
     * 
     * @param cl
     *            plug-in class loader to be disposed
     */
    protected final void disposeClassLoader(final PluginClassLoader cl) {
        cl.dispose();
    }

    /**
     * Forwards call to {@link PluginClassLoader#pluginsSetChanged()} method.
     * 
     * @param cl
     *            plug-in class loader to be notified
     */
    protected final void notifyClassLoader(final PluginClassLoader cl) {
        cl.pluginsSetChanged();
    }

    /**
     * Plug-ins life-cycle events callback interface.
     * 
     * @version $Id: PluginManager.java,v 1.5 2007/04/07 12:42:14 ddimon Exp $
     */
    public interface EventListener {
        /**
         * This method will be called by the manager just after plug-in has been
         * successfully activated.
         * 
         * @param plugin
         *            just activated plug-in
         */
        void pluginActivated(Plugin plugin);

        /**
         * This method will be called by the manager just before plug-in
         * deactivation.
         * 
         * @param plugin
         *            plug-in to be deactivated
         */
        void pluginDeactivated(Plugin plugin);

        /**
         * This method will be called by the manager just before plug-in
         * disabling.
         * 
         * @param descriptor
         *            descriptor of plug-in to be disabled
         */
        void pluginDisabled(PluginDescriptor descriptor);

        /**
         * This method will be called by the manager just after plug-in
         * enabling.
         * 
         * @param descriptor
         *            descriptor of enabled plug-in
         */
        void pluginEnabled(PluginDescriptor descriptor);
    }

    /**
     * An abstract adapter class for receiving plug-ins life-cycle events. The
     * methods in this class are empty. This class exists as convenience for
     * creating listener objects.
     * 
     * @version $Id: PluginManager.java,v 1.5 2007/04/07 12:42:14 ddimon Exp $
     */
    public abstract static class EventListenerAdapter implements EventListener {
        /**
         * @see org.java.plugin.PluginManager.EventListener#pluginActivated(
         *      org.java.plugin.Plugin)
         */
        public void pluginActivated(final Plugin plugin) {
            // no-op
        }

        /**
         * @see org.java.plugin.PluginManager.EventListener#pluginDeactivated(
         *      org.java.plugin.Plugin)
         */
        public void pluginDeactivated(final Plugin plugin) {
            // no-op
        }

        /**
         * @see org.java.plugin.PluginManager.EventListener#pluginDisabled(
         *      org.java.plugin.registry.PluginDescriptor)
         */
        public void pluginDisabled(final PluginDescriptor descriptor) {
            // no-op
        }

        /**
         * @see org.java.plugin.PluginManager.EventListener#pluginEnabled(
         *      org.java.plugin.registry.PluginDescriptor)
         */
        public void pluginEnabled(final PluginDescriptor descriptor) {
            // no-op
        }
    }

    /**
     * Simple callback interface to hold info about plug-in manifest and plug-in
     * data locations.
     * 
     * @version $Id: PluginManager.java,v 1.5 2007/04/07 12:42:14 ddimon Exp $
     */
    public static interface PluginLocation {
        /**
         * @return location of plug-in manifest
         */
        URL getManifestLocation();

        /**
         * @return location of plug-in context ("home")
         */
        URL getContextLocation();
    }
}
