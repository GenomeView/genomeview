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
package org.java.plugin.registry.xml;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.IntegrityCheckReport;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginPrerequisite;
import org.java.plugin.util.IoUtil;
import org.java.plugin.util.ResourceManager;

/**
 * @version $Id$
 */
class IntegrityChecker implements IntegrityCheckReport {
    private static Log log = LogFactory.getLog(IntegrityChecker.class);

    private final PluginRegistryImpl registry;
    private List<ReportItem> items = new LinkedList<ReportItem>();
    private int errorsCount;
    private int warningsCount;

    IntegrityChecker(final PluginRegistryImpl aRegistry,
            final Collection<ReportItem> anItems) {
        this.items = new LinkedList<ReportItem>();
        this.registry = aRegistry;
        for (ReportItem item : anItems) {
            switch (item.getSeverity()) {
            case ERROR:
                break;
            case WARNING:
                warningsCount++;
                break;
            case INFO:
                // no-op
                break;
            }
            this.items.add(item);
        }
    }
    
    void doCheck(final PathResolver pathResolver) {
        int count = 0;
        items.add(new ReportItemImpl(Severity.INFO, null, Error.NO_ERROR,
                "pluginsCheckStart", null)); //$NON-NLS-1$
        try {
            for (PluginDescriptor descriptor : registry.getPluginDescriptors()) {
                PluginDescriptorImpl descr = (PluginDescriptorImpl) descriptor;
                count++;
                items.add(new ReportItemImpl(Severity.INFO, descr,
                        Error.NO_ERROR, "pluginCheckStart", //$NON-NLS-1$
                        descr.getUniqueId()));
                checkPlugin(descr, pathResolver);
                items.add(new ReportItemImpl(Severity.INFO, descr,
                        Error.NO_ERROR, "pluginCheckFinish", //$NON-NLS-1$
                        descr.getUniqueId()));
            }
        } catch (Exception e) {
            log.error("integrity check failed for registry " + registry, e); //$NON-NLS-1$
            errorsCount++;
            items.add(new ReportItemImpl(Severity.ERROR, null,
                    Error.CHECKER_FAULT, "pluginsCheckError", e)); //$NON-NLS-1$
        }
        items.add(new ReportItemImpl(Severity.INFO, null, Error.NO_ERROR,
                "pluginsCheckFinish", Integer.valueOf(count))); //$NON-NLS-1$
    }
    
    private void checkPlugin(final PluginDescriptorImpl descr,
            final PathResolver pathResolver) {
        // checking prerequisites
        int count = 0;
        items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                "prerequisitesCheckStart", descr.getUniqueId())); //$NON-NLS-1$
        for (PluginPrerequisite prerequisite : descr.getPrerequisites()) {
            PluginPrerequisiteImpl pre = (PluginPrerequisiteImpl) prerequisite;
            count++;
            if (!pre.isOptional() && !pre.matches()) {
                errorsCount++;
                items.add(new ReportItemImpl(Severity.ERROR, descr,
                        Error.UNSATISFIED_PREREQUISITE,
                        "unsatisfiedPrerequisite", new Object[] { //$NON-NLS-1$
                        pre.getPluginId(), descr.getUniqueId()}));
            }
        }
        items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                "prerequisitesCheckFinish", //$NON-NLS-1$
                new Object[] {Integer.valueOf(count), descr.getUniqueId()}));
        // checking libraries
        if (pathResolver != null) {
            count = 0;
            items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                    "librariesCheckStart", descr.getUniqueId())); //$NON-NLS-1$
            for (Library library : descr.getLibraries()) {
                LibraryImpl lib = (LibraryImpl) library;
                count++;
                URL url = pathResolver.resolvePath(lib, lib.getPath());
                if (!IoUtil.isResourceExists(url)) {
                    errorsCount++;
                    items.add(new ReportItemImpl(Severity.ERROR, lib,
                            Error.BAD_LIBRARY,
                            "accesToResourceFailed", new Object[] { //$NON-NLS-1$
                            lib.getUniqueId(), descr.getUniqueId(), url}));
                }
            }
            items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                    "librariesCheckFinish", //$NON-NLS-1$
                    new Object[] {Integer.valueOf(count),
                    descr.getUniqueId()}));
        } else {
            items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                    "librariesCheckSkip", descr.getUniqueId())); //$NON-NLS-1$
        }
        // checking extension points
        count = 0;
        items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                "extPointsCheckStart", null)); //$NON-NLS-1$
        for (ExtensionPoint extensionPoint : descr.getExtensionPoints()) {
            count++;
            ExtensionPointImpl extPoint = (ExtensionPointImpl) extensionPoint;
            items.add(new ReportItemImpl(Severity.INFO, extPoint,
                    Error.NO_ERROR, "extPointCheckStart", //$NON-NLS-1$
                    extPoint.getUniqueId()));
            Collection<ReportItem> extPointItems = extPoint.validate();
            for (ReportItem item : extPointItems) {
                switch (item.getSeverity()) {
                case ERROR:
                    errorsCount++;
                    break;
                case WARNING:
                    warningsCount++;
                    break;
                case INFO:
                    // no-op
                    break;
                }
                items.add(item);
            }
            items.add(new ReportItemImpl(Severity.INFO, extPoint,
                    Error.NO_ERROR, "extPointCheckFinish", //$NON-NLS-1$
                    extPoint.getUniqueId()));
        }
        items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                "extPointsCheckFinish", //$NON-NLS-1$
                new Object[] {Integer.valueOf(count), descr.getUniqueId()}));
        // checking extensions
        count = 0;
        items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                "extsCheckStart", null)); //$NON-NLS-1$
        for (Extension extension : descr.getExtensions()) {
            count++;
            ExtensionImpl ext = (ExtensionImpl) extension;
            items.add(new ReportItemImpl(Severity.INFO, ext, Error.NO_ERROR,
                    "extCheckStart", ext.getUniqueId())); //$NON-NLS-1$
            Collection<ReportItem> extItems = ext.validate();
            for (ReportItem item : extItems) {
                switch (item.getSeverity()) {
                case ERROR:
                    errorsCount++;
                    break;
                case WARNING:
                    warningsCount++;
                    break;
                case INFO:
                    // no-op
                    break;
                }
                items.add(item);
            }
            items.add(new ReportItemImpl(Severity.INFO, ext, Error.NO_ERROR,
                    "extCheckFinish", ext.getUniqueId())); //$NON-NLS-1$
        }
        items.add(new ReportItemImpl(Severity.INFO, descr, Error.NO_ERROR,
                "extsCheckFinish", //$NON-NLS-1$
                new Object[] {Integer.valueOf(count), descr.getUniqueId()}));
    }
    
    /**
     * @see org.java.plugin.registry.IntegrityCheckReport#countErrors()
     */
    public int countErrors() {
        return errorsCount;
    }

    /**
     * @see org.java.plugin.registry.IntegrityCheckReport#countWarnings()
     */
    public int countWarnings() {
        return warningsCount;
    }

    /**
     * @see org.java.plugin.registry.IntegrityCheckReport#getItems()
     */
    public Collection<ReportItem> getItems() {
        return items;
    }

    static class ReportItemImpl implements ReportItem {
        private final Severity severity;
        private final Identity source;
        private final Error code;
        private final String msg;
        private final Object data;
        
        ReportItemImpl(final Severity aSeverity, final Identity aSource,
                final Error aCode, final String aMsg, final Object aData) {
            severity = aSeverity;
            source = aSource;
            code = aCode;
            msg = aMsg;
            data = aData;
        }
        
        /**
         * @see org.java.plugin.registry.IntegrityCheckReport.ReportItem#getCode()
         */
        public Error getCode() {
            return code;
        }
        
        /**
         * @see org.java.plugin.registry.IntegrityCheckReport.ReportItem#getMessage()
         */
        public String getMessage() {
            return ResourceManager.getMessage(PluginRegistryImpl.PACKAGE_NAME,
                    msg, data);
        }

        /**
         * @see org.java.plugin.registry.IntegrityCheckReport.ReportItem#getMessage(
         *      java.util.Locale)
         */
        public String getMessage(Locale locale) {
            return ResourceManager.getMessage(PluginRegistryImpl.PACKAGE_NAME,
                    msg, locale, data);
        }
        
        /**
         * @see org.java.plugin.registry.IntegrityCheckReport.ReportItem#getSeverity()
         */
        public Severity getSeverity() {
            return severity;
        }
        
        /**
         * @see org.java.plugin.registry.IntegrityCheckReport.ReportItem#getSource()
         */
        public Identity getSource() {
            return source;
        }
    }
}
