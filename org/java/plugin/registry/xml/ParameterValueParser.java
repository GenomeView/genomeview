/*****************************************************************************
 * Java Plug-in Framework (JPF)
 * Copyright (C) 2006-2007 Dmitry Olshansky
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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.StringTokenizer;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.ParameterType;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.registry.ExtensionPoint.ParameterDefinition;

/**
 * @version $Id$
 */
class ParameterValueParser {
    private static ExtensionPoint getExtensionPoint(
            final PluginRegistry registry, final String uniqueId) {
        String pluginId = registry.extractPluginId(uniqueId);
        if (!registry.isPluginDescriptorAvailable(pluginId)) {
            return null;
        }
        String pointId = registry.extractId(uniqueId);
        for (ExtensionPoint point : registry.getPluginDescriptor(pluginId)
                .getExtensionPoints()) {
            if (point.getId().equals(pointId)) {
                return point;
            }
        }
        return null;
    }

    private Object value;
    private final boolean isParsingSucceeds;
    private String parsingMessage;
    
    ParameterValueParser(final PluginRegistry registry,
            final ParameterDefinition definition, final String rawValue) {
        if (definition == null) {
            parsingMessage = "parameter definition is NULL"; //$NON-NLS-1$
            isParsingSucceeds = false;
            return;
        }
        if (rawValue == null) {
            isParsingSucceeds = true;
            return;
        }
        if ((ParameterType.ANY == definition.getType())
                || (ParameterType.NULL == definition.getType())) {
            isParsingSucceeds = true;
            return;
        } else if (ParameterType.STRING == definition.getType()) {
            value = rawValue;
            isParsingSucceeds = true;
            return;
        }
        String val = rawValue.trim();
        if (val.length() == 0) {
            isParsingSucceeds = true;
            return;
        }
        switch (definition.getType()) {
        case BOOLEAN:
            if ("true".equals(val)) { //$NON-NLS-1$
                value = Boolean.TRUE;
            } else if ("false".equals(val)) { //$NON-NLS-1$
                value = Boolean.FALSE;
            } else {
                isParsingSucceeds = false;
                return;
            }
            break;
        case NUMBER:
            try {
                value =
                    NumberFormat.getInstance(Locale.ENGLISH).parse(val);
            } catch (ParseException nfe) {
                isParsingSucceeds = false;
                return;
            }
            break;
        case DATE: {
            DateFormat fmt =
                new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH); //$NON-NLS-1$
            try {
                value = fmt.parse(val);
            } catch (ParseException pe) {
                isParsingSucceeds = false;
                return;
            }
            break;
        }
        case TIME: {
            DateFormat fmt =
                new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH); //$NON-NLS-1$
            try {
                value = fmt.parse(val);
            } catch (ParseException pe) {
                isParsingSucceeds = false;
                return;
            }
            break;
        }
        case DATE_TIME:{
            DateFormat fmt =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH); //$NON-NLS-1$
            try {
                value = fmt.parse(val);
            } catch (ParseException pe) {
                isParsingSucceeds = false;
                return;
            }
            break;
        }
        case PLUGIN_ID:
            try {
                value = registry.getPluginDescriptor(val);
            } catch (IllegalArgumentException iae) {
                parsingMessage = "unknown plug-in ID " + val; //$NON-NLS-1$
                isParsingSucceeds = false;
                return;
            }
            break;
        case EXTENSION_POINT_ID:
            value = getExtensionPoint(registry, val);
            if (value == null) {
                parsingMessage = "unknown extension point UID " + val; //$NON-NLS-1$
                isParsingSucceeds = false;
                return;
            }
            if (definition.getCustomData() != null) {
                ExtensionPoint customExtPoint =
                    getExtensionPoint(registry, definition.getCustomData());
                if (customExtPoint == null) {
                    parsingMessage = "unknown extension point UID " //$NON-NLS-1$
                            + definition.getCustomData()
                            + " provided as custom data"; //$NON-NLS-1$
                    isParsingSucceeds = false;
                    return;
                }
                if (!((ExtensionPoint) value).isSuccessorOf(
                        customExtPoint)) {
                    parsingMessage = "extension point with UID " + val //$NON-NLS-1$
                            + " doesn't \"inherit\" point that is defined" //$NON-NLS-1$
                            + " according to custom data in parameter" //$NON-NLS-1$
                            + " definition - " //$NON-NLS-1$
                            + definition.getCustomData();
                    isParsingSucceeds = false;
                    return;
                }
            }
            break;
        case EXTENSION_ID:
            String extId = registry.extractId(val);
            for (Extension ext : registry.getPluginDescriptor(
                    registry.extractPluginId(val)).getExtensions()) {
                if (ext.getId().equals(extId)) {
                    value = ext;
                    break;
                }
            }
            if (value == null) {
                parsingMessage = "unknown extension UID " + val; //$NON-NLS-1$
                isParsingSucceeds = false;
                return;
            }
            if (definition.getCustomData() != null) {
                ExtensionPoint customExtPoint =
                    getExtensionPoint(registry, definition.getCustomData());
                if (customExtPoint == null) {
                    parsingMessage = "unknown extension point UID " //$NON-NLS-1$
                            + definition.getCustomData()
                            + " provided as custom data in parameter definition " //$NON-NLS-1$
                            + definition;
                    isParsingSucceeds = false;
                    return;
                }
                String extPointUid = registry.makeUniqueId(
                        ((Extension) value).getExtendedPluginId(),
                        ((Extension) value).getExtendedPointId());
                ExtensionPoint extPoint =
                    getExtensionPoint(registry, extPointUid);
                if (extPoint == null) {
                    parsingMessage = "extension point " + extPointUid //$NON-NLS-1$
                            + " is unknown for extension " //$NON-NLS-1$
                            + ((Extension) value).getUniqueId();
                    isParsingSucceeds = false;
                    return;
                }
                if (!extPoint.equals(customExtPoint)
                        && !extPoint.isSuccessorOf(customExtPoint)) {
                    parsingMessage = "extension with UID " + val //$NON-NLS-1$
                            + " extends point that not allowed according" //$NON-NLS-1$
                            + " to custom data defined in parameter" //$NON-NLS-1$
                            + " definition - " //$NON-NLS-1$
                            + definition.getCustomData();
                    isParsingSucceeds = false;
                    return;
                }
            }
            break;
        case FIXED:
            for (StringTokenizer st = new StringTokenizer(
                    definition.getCustomData(), "|", false); //$NON-NLS-1$
                    st.hasMoreTokens();) {
                if (val.equals(st.nextToken().trim())) {
                    value = val;
                    isParsingSucceeds = true;
                    return;
                }
            }
            parsingMessage = "not allowed value " + val; //$NON-NLS-1$
            isParsingSucceeds = false;
            return;
        case RESOURCE:
            try {
                value = new URL(val);
            } catch (MalformedURLException mue) {
                parsingMessage = "can't parse value " + val //$NON-NLS-1$
                        + " as an absolute URL, will treat it as relative URL"; //$NON-NLS-1$
                //return Boolean.FALSE;
                value = null;
            }
            isParsingSucceeds = true;
            return;
        case ANY:
            // no-op
            break;
        case NULL:
            // no-op
            break;
        case STRING:
            // no-op
            break;
        }
        isParsingSucceeds = true;
    }
    
    Object getValue() {
        return value;
    }
    
    String getParsingMessage() {
        return parsingMessage;
    }
    
    boolean isParsingSucceeds() {
        return isParsingSucceeds;
    }
}
