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
package org.java.plugin.registry;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.PluginManager;
import org.java.plugin.util.ExtendedProperties;

/**
 * Root interface to get access to all meta-information about discovered
 * plug-ins. All objects accessible from the registry are immutable. You can
 * imagine registry as a read-only storage of full information about discovered
 * plug-ins. There is only one exception from this rule: internal state of
 * registry, plug-in descriptors and plug-in elements can be modified indirectly
 * by {@link #register(URL[]) registering} or
 * {@link #unregister(String[]) un-registering} plug-ins with this registry. If
 * your code is interested to be notified on all modifications of plug-ins set,
 * you can
 * {@link #registerListener(PluginRegistry.RegistryChangeListener) register} an
 * implementation of {@link PluginRegistry.RegistryChangeListener} with this
 * registry.
 * <p>
 * <i>Notes on unique ID's (UID's)</i>
 * </p>
 * <p>
 * There are two types of identifiers in the API: ID's and UID's. ID is an
 * identifier that is unique within set of elements of the same type. UID is an
 * identifier that unique globally within registry space. ID is usually defined
 * by developer in plug-in manifest. UID always combined automatically from
 * several other plug-in "parts". All plug-in elements have method
 * {@link org.java.plugin.registry.Identity#getId() getId()} that come from
 * basic {@link org.java.plugin.registry.Identity} interface, but not all
 * elements have UID - only those that inherits
 * {@link org.java.plugin.registry.UniqueIdentity}interface.
 * </p>
 * <p>
 * There are several utility methods available in this interface that aimed to
 * build UID from different plug-in "parts" and also split UID to it's original
 * elements: {@link #makeUniqueId(String, Version)},
 * {@link #makeUniqueId(String, String)}, {@link #extractPluginId(String)},
 * {@link #extractId(String)} and {@link #extractVersion(String)}.
 * </p>
 * 
 * @see org.java.plugin.ObjectFactory#createRegistry()
 * 
 * @version $Id: PluginRegistry.java,v 1.5 2007/03/03 17:16:26 ddimon Exp $
 */
public interface PluginRegistry {
    /**
     * Registers plug-ins and plug-in fragments in this registry. Note that this
     * method not makes plug-ins available for activation by any
     * {@link PluginManager} instance as it is not aware of any manager. Using
     * this method just makes plug-in meta-data available for reading from this
     * registry.
     * <p>
     * If more than one version of the same plug-in or plug-in fragment given,
     * the only latest version should be registered. If some plug-in or plug-in
     * fragment already registered it should be ignored by this method. Client
     * application have to un-register such plug-ins first before registering
     * their newest versions.
     * 
     * @param manifests
     *            array of manifest locations
     * @return map where keys are URL's and values are registered plug-ins or
     *         plug-in fragments, URL's for unprocessed manifests are not
     *         included
     * @throws ManifestProcessingException
     *             if manifest processing error has occurred (optional behavior)
     * 
     * @see PluginManager#publishPlugins(PluginManager.PluginLocation[])
     */
    Map<String, Identity> register(URL[] manifests)
            throws ManifestProcessingException;

    /**
     * Reads basic information from a plug-in or plug-in fragment manifest.
     * 
     * @param manifest
     *            manifest data URL
     * @return manifest info
     * @throws ManifestProcessingException
     *             if manifest data can't be read
     */
    ManifestInfo readManifestInfo(URL manifest)
            throws ManifestProcessingException;

    /**
     * Unregisters plug-ins and plug-in fragments with given ID's (including
     * depending plug-ins and plug-in fragments).
     * 
     * @param ids
     *            ID's of plug-ins and plug-in fragments to be unregistered
     * @return collection of UID's of actually unregistered plug-ins and plug-in
     *         fragments
     */
    Collection<String> unregister(String[] ids);

    /**
     * Returns descriptor of plug-in with given ID. <br>
     * If plug-in descriptor with given ID can't be found or such plug-in exists
     * but is damaged this method have to throw an
     * {@link IllegalArgumentException}. In other words, this method shouldn't
     * return <code>null</code>.
     * 
     * @param pluginId
     *            plug-id ID
     * @return plug-in descriptor
     */
    PluginDescriptor getPluginDescriptor(String pluginId);

    /**
     * Checks if plug-in exists and is in valid state. If this method returns
     * <code>true</code>, the method {@link #getPluginDescriptor(String)}
     * should always return valid plug-in descriptor.
     * 
     * @param pluginId
     *            plug-in ID
     * @return <code>true</code> if plug-in exists and valid
     */
    boolean isPluginDescriptorAvailable(String pluginId);

    /**
     * Returns collection of descriptors of all plug-ins that was successfully
     * populated by this registry.
     * 
     * @return collection of {@link PluginDescriptor} objects
     */
    Collection<PluginDescriptor> getPluginDescriptors();

    /**
     * Looks for extension point. This method have throw an
     * {@link IllegalArgumentException} if requested extension point can't be
     * found or is in invalid state.
     * 
     * @param pluginId
     *            plug-in ID
     * @param pointId
     *            extension point ID
     * @return plug-in extension point
     * @see ExtensionPoint#isValid()
     */
    ExtensionPoint getExtensionPoint(String pluginId, String pointId);

    /**
     * Looks for extension point.
     * 
     * @param uniqueId
     *            extension point unique ID
     * @return plug-in extension point
     * @see #getExtensionPoint(String, String)
     */
    ExtensionPoint getExtensionPoint(String uniqueId);

    /**
     * Checks if extension point exists and is in valid state. If this method
     * returns <code>true</code>, the method
     * {@link #getExtensionPoint(String, String)} should always return valid
     * extension point.
     * 
     * @param pluginId
     *            plug-in ID
     * @param pointId
     *            extension point ID
     * @return <code>true</code> if extension point exists and valid
     */
    boolean isExtensionPointAvailable(String pluginId, String pointId);

    /**
     * Checks if extension point exists and is in valid state.
     * 
     * @param uniqueId
     *            extension point unique ID
     * @return <code>true</code> if extension point exists and valid
     * @see #isExtensionPointAvailable(String, String)
     */
    boolean isExtensionPointAvailable(String uniqueId);

    /**
     * Returns collection of descriptors of all plug-in fragments that was
     * successfully populated by this registry.
     * 
     * @return collection of {@link PluginFragment} objects
     */
    Collection<PluginFragment> getPluginFragments();

    /**
     * Utility method that recursively collects all plug-ins that depends on the
     * given plug-in.
     * 
     * @param descr
     *            descriptor of plug-in to collect dependencies for
     * @return collection of {@link PluginDescriptor plug-in descriptors} that
     *         depend on given plug-in
     */
    Collection<PluginDescriptor> getDependingPlugins(PluginDescriptor descr);

    /**
     * Performs integrity check of all registered plug-ins and generates result
     * as a collection of standard report items.
     * 
     * @param pathResolver
     *            optional path resolver
     * @return integrity check report
     */
    IntegrityCheckReport checkIntegrity(PathResolver pathResolver);

    /**
     * Performs integrity check of all registered plug-ins and generates result
     * as a collection of standard report items.
     * 
     * @param pathResolver
     *            optional path resolver
     * @param includeRegistrationReport
     *            if <code>true</code>, the plug-ins registration report will
     *            be included into resulting report
     * @return integrity check report
     */
    IntegrityCheckReport checkIntegrity(PathResolver pathResolver,
            boolean includeRegistrationReport);

    /**
     * @return plug-ins registration report for this registry
     */
    IntegrityCheckReport getRegistrationReport();

    /**
     * Constructs unique identifier for some plug-in element from it's ID.
     * 
     * @param pluginId
     *            plug-in ID
     * @param elementId
     *            element ID
     * @return unique ID
     */
    String makeUniqueId(String pluginId, String elementId);

    /**
     * Constructs unique identifier for plug-in with given ID.
     * 
     * @param pluginId
     *            plug-in ID
     * @param version
     *            plug-in version identifier
     * @return unique plug-in ID
     */
    String makeUniqueId(String pluginId, Version version);

    /**
     * Extracts plug-in ID from some unique identifier.
     * 
     * @param uniqueId
     *            unique ID
     * @return plug-in ID
     */
    String extractPluginId(String uniqueId);

    /**
     * Extracts plug-in element ID from some unique identifier.
     * 
     * @param uniqueId
     *            unique ID
     * @return element ID
     */
    String extractId(String uniqueId);

    /**
     * Extracts plug-in version identifier from some unique identifier (plug-in
     * or plug-in fragment).
     * 
     * @param uniqueId
     *            unique ID
     * @return plug-in version identifier
     */
    Version extractVersion(String uniqueId);

    /**
     * Registers plug-in registry change event listener. If given listener has
     * been registered before, this method should throw an
     * {@link IllegalArgumentException}.
     * 
     * @param listener
     *            new registry change event listener
     */
    void registerListener(RegistryChangeListener listener);

    /**
     * Unregisters registry change event listener. If given listener hasn't been
     * registered before, this method should throw an
     * {@link IllegalArgumentException}.
     * 
     * @param listener
     *            registered listener
     */
    void unregisterListener(RegistryChangeListener listener);

    /**
     * Configures this registry instance. Usually this method is called from
     * {@link ObjectFactory object factory} implementation.
     * 
     * @param config
     *            registry configuration data
     */
    void configure(ExtendedProperties config);

    /**
     * Plug-in registry changes callback interface.
     * 
     * @version $Id: PluginRegistry.java,v 1.5 2007/03/03 17:16:26 ddimon Exp $
     */
    interface RegistryChangeListener {
        /**
         * This method will be called by the framework when changes are made on
         * registry (via {@link PluginRegistry#register(URL[])} or
         * {@link PluginRegistry#unregister(String[])} methods).
         * 
         * @param data
         *            registry changes data
         */
        void registryChanged(RegistryChangeData data);
    }

    /**
     * Registry changes data holder interface.
     * 
     * @version $Id: PluginRegistry.java,v 1.5 2007/03/03 17:16:26 ddimon Exp $
     */
    interface RegistryChangeData {
        /**
         * @return collection of ID's of newly added plug-ins
         */
        Set<String> addedPlugins();

        /**
         * @return collection of ID's of removed plug-ins
         */
        Set<String> removedPlugins();

        /**
         * @return collection of ID's of changed plug-ins
         */
        Set<String> modifiedPlugins();

        /**
         * @return collection of unique ID's of newly connected extensions
         */
        Set<String> addedExtensions();

        /**
         * @param extensionPointUid
         *            unique ID of extension point to filter result
         * @return collection of unique ID's of newly connected extensions
         */
        Set<String> addedExtensions(String extensionPointUid);

        /**
         * @return collection of unique ID's of disconnected extensions
         */
        Set<String> removedExtensions();

        /**
         * @param extensionPointUid
         *            unique ID of extension point to filter result
         * @return collection of unique ID's of disconnected extensions
         */
        Set<String> removedExtensions(String extensionPointUid);

        /**
         * @return collection of unique ID's of modified extensions
         */
        Set<String> modifiedExtensions();

        /**
         * @param extensionPointUid
         *            unique ID of extension point to filter result
         * @return collection of unique ID's of modified extensions
         */
        Set<String> modifiedExtensions(String extensionPointUid);
    }
}
