/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: AMCommonNameGenerator.java,v 1.4 2008-11-08 08:20:41 veiming Exp $
 *
 */

package com.sun.identity.console.base.model;

import com.sun.identity.shared.debug.Debug;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdUtils;
import com.sun.identity.sm.AttributeSchema;
import com.sun.identity.sm.OrganizationConfigManager;
import com.sun.identity.sm.SchemaType;
import com.sun.identity.sm.ServiceConfigManager;
import com.sun.identity.sm.ServiceSchema;
import com.sun.identity.sm.ServiceSchemaManager;
import com.sun.identity.sm.ServiceListener;
import com.sun.identity.sm.SMSException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/* - NEED NOT LOG - */

/**
 * <code>AMCommonNameGenerator</code> is a singleton class that generates
 * common name for an user.
 */
public class AMCommonNameGenerator
    implements AMAdminConstants, ServiceListener
{
    private static SSOToken adminSSOToken =
        AMAdminUtils.getSuperAdminSSOToken();

    private static final String DEFAULT_FORMAT = "{cn}";

    private static Debug debug = Debug.getInstance(CONSOLE_DEBUG_FILENAME);
    private static AMCommonNameGenerator instance
        = new AMCommonNameGenerator();
    private static ServiceSchemaManager serviceSchemaManager;
    private static ServiceConfigManager serviceConfigManager;
    private Map mapRealmToFormat = new HashMap();
    private static final String ROOT = "/";

    /**
     * Creates a Console Listener Manager.
     */
    private AMCommonNameGenerator() {
        initialize();
    }

    private void initialize() {
        try {
            serviceSchemaManager = new ServiceSchemaManager(
                G11N_SERVICE_NAME, adminSSOToken);
            serviceConfigManager = new ServiceConfigManager(
                G11N_SERVICE_NAME, adminSSOToken);
            serviceConfigManager.addListener(this); //listener for org
            serviceSchemaManager.addListener(this); //listener for global
        } catch (SMSException smse) {
            debug.error("AMCommonNameGenerator.initManager", smse);
        } catch (SSOException ssoe) {
            debug.error("AMCommonNameGenerator.initManager", ssoe);
        }
    }

    /**
     * Gets an instance of Common Name generator.
     *
     * @return an instance of Common Name generator.
     */
    public static AMCommonNameGenerator getInstance() {
        return instance;
    }

    /**
     * Generates common name for an user. This name is generated by reading
     * the common name format attribute from Globalization service.  This
     * format contains tokenized User Profile's attributes. E.g.
     * <code>{givenname} {initials} {sn}</code>.  This method replaces these
     * tokens with attributes values from <code>userInfoMap</code>.
     *
     * @param universalId Universal Id of user.
     * @param model of the view bean.
     * @return common name for user.
     */
    public String generateCommonName(String univId, AMModel model)
    {
        String commonName = "";
        Map userInfoMap = getUserAttributeValues(univId);

        if ((userInfoMap != null) && !userInfoMap.isEmpty()) {
            String localeStr = AMAdminUtils.getFirstElement(
                (Set)userInfoMap.get(USER_SERVICE_PREFERRED_LOCALE));

            if ((localeStr == null) || (localeStr.trim().length() == 0)) {
                localeStr = model.getUserLocale().toString();
            }

            String format = getCommonNameFormat(localeStr, model);
            Set tokens = getTokens(format);
            Map mapAttrValue = getAttributeValues(userInfoMap, tokens);
            commonName = format;

            for (Iterator iter = mapAttrValue.keySet().iterator();
                iter.hasNext();
            ) {
                String token = (String)iter.next();
                String value = (String)mapAttrValue.get(token);
                if (!format.equals(DEFAULT_FORMAT)) {
                    value += " ";
                }
                commonName = AMFormatUtils.replaceString(
                    commonName, token, value);
            }

            // remove unused token
            tokens.removeAll(mapAttrValue.keySet());

            for (Iterator iter = tokens.iterator(); iter.hasNext(); ) {
                String token = (String)iter.next();
                int idx = commonName.indexOf(token + " ");

                if (idx != -1) {
                    commonName = commonName.substring(0, idx) +
                        commonName.substring(idx + token.length() +1);
                } else {
                    idx = commonName.indexOf(token);

                    if (idx != -1) {
                        commonName = commonName.substring(0, idx) +
                            commonName.substring(idx + token.length());
                    }
                }
            }
        }

        return commonName;
    }

    private Map getUserAttributeValues(String univId) {
        Map values = null;
        try {
            AMIdentity amid = IdUtils.getIdentity(adminSSOToken, univId);
            if (amid != null) {
                Map map = amid.getAttributes();
                Map attributeSchemasNames = getAttributeSchemaExactNames(
                    amid.getType().getName());
                values = new HashMap(map.size() *2);

                /*
                * Need to get the attribute name to the proper case.
                * SDK makes them all lowercased
                */
                for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
                    String name = (String)i.next();
                    String exactName = (String)attributeSchemasNames.get(name);
                    if (exactName == null) {
                        exactName = name;
                    }
                    values.put(exactName, map.get(name));
                }
            }
        } catch (SSOException e) {
            debug.warning("AMCommonNameGenerator.getUserAttributeValues", e);
        } catch (SMSException e) {
            debug.warning("AMCommonNameGenerator.getUserAttributeValues", e);
        } catch (IdRepoException e) {
            debug.warning("AMCommonNameGenerator.getUserAttributeValues", e);
        }
        return (values != null) ? values : Collections.EMPTY_MAP;
    }

    private Map getAttributeSchemaExactNames(String idType)
        throws SMSException, SSOException, IdRepoException
    {
        Map mapping = new HashMap();
        String serviceName = IdUtils.getServiceName(IdUtils.getType(idType));
        if (serviceName != null) {
            ServiceSchemaManager svcSchemaMgr = new ServiceSchemaManager(
                serviceName, adminSSOToken);
            ServiceSchema svcSchema = svcSchemaMgr.getSchema(idType);
            Set attributeSchemas = (svcSchema != null) ?
                svcSchema.getAttributeSchemas() : Collections.EMPTY_SET;
            for (Iterator i = attributeSchemas.iterator(); i.hasNext(); ) {
                AttributeSchema as = (AttributeSchema)i.next();
                String name = as.getName();
                mapping.put(name.toLowerCase(), name);
            }
        }
                                                                                
        return mapping;
    }

    private Map getAttributeValues(Map userInfoMap, Set tokens) {
        Map map = new HashMap(tokens.size() *2);

        for (Iterator iter = tokens.iterator(); iter.hasNext(); ) {
            String token = (String)iter.next();
            String attrName = token.substring(1, token.length() -1);
            String strValue = AMAdminUtils.getFirstElement(
                (Set)userInfoMap.get(attrName));

            if ((strValue != null) && (strValue.length() > 0)) {
                map.put(token, strValue);
            }
        }

        return map;
    }

    private Set getTokens(String format) {
        Set tokens = new HashSet();
        int start = format.indexOf('{');

        while (start != -1) {
            int end = format.indexOf('}', start+1);

            if (end != 1) {
                tokens.add(format.substring(start, end+1));
                format = format.substring(end+1);

                start = format.indexOf('{');
            } else {
                break;
            }
        }

        return tokens;
    }

    private String getCommonNameFormat(String locale, AMModel model) {
        String format = null;
        Map map = getFormats(model);

        if ((map != null) && !map.isEmpty()) {
            format = (String)map.get(locale.toLowerCase());
            if (format == null) {
                int idx = locale.indexOf('_');

                if (idx != -1) {
                    locale = locale.substring(0, idx);
                    format = (String)map.get(locale.toLowerCase());
                }
            }
        }
        return (format != null) ? format : DEFAULT_FORMAT;
    }

    /**
     * Adds format by retrieving the globalization service
     * attributes to get the list of formats and add them
     * accordingly.
     *
     * @param realm Realm Name
     * @return map of locale to formats
     */
    private Map addFormats(String realm) {
        Set values = null;
        Map map = null;

        try {
            AMIdentityRepository repo = new AMIdentityRepository(
                adminSSOToken, realm);
            AMIdentity realmIdentity = repo.getRealmIdentity();
            Set servicesFromIdRepo = realmIdentity.getAssignedServices();

            if (servicesFromIdRepo.contains(G11N_SERVICE_NAME)) {
                map = realmIdentity.getServiceAttributes(G11N_SERVICE_NAME);
            } else {
                OrganizationConfigManager orgCfgMgr =
                    new OrganizationConfigManager(
                        adminSSOToken, realm);
                map = orgCfgMgr.getServiceAttributes(G11N_SERVICE_NAME);
            }
        } catch (SSOException e) {
            debug.warning("AMCommonNameGenerator.addFormats", e);
        } catch (SMSException e) {
            debug.warning("AMCommonNameGenerator.addFormats", e);
        } catch (IdRepoException e) {
            debug.warning("AMCommonNameGenerator.addFormats", e);
        }

        if ((map != null) && !map.isEmpty()) {
            values = (Set)map.get(G11N_SERIVCE_COMMON_NAME_FORMAT);
        }

        if ((values == null) || values.isEmpty()) {
            if (serviceSchemaManager != null) {
                try {
                values = AMAdminUtils.getAttribute(serviceSchemaManager,
                    SchemaType.ORGANIZATION, G11N_SERIVCE_COMMON_NAME_FORMAT);
                } catch (SMSException e) {
                    debug.error("AMCommonNameGenerator.addFormats", e);
                }
            } else {
                debug.error(
                    "AMCommonNameGenerator.addFormats: " +
                    "formats are not added because Console cannot get " +
                    "an instance of service schema manager.");
            }
        }

        Map mapFormats = getFormatMap(values);

        synchronized (mapRealmToFormat) {
            mapRealmToFormat.put(realm, mapFormats);
        }

        return mapFormats;
    }

    /**
     * Returns a map of locale string to common name format.
     *
     * @param formats set of formatting information.
     * @return a map of locale string to common name format.
     */
    private Map getFormatMap(Set formats) {
        Map map = new HashMap(formats.size());

        for (Iterator i = formats.iterator(); i.hasNext(); ) {
            String format = (String)i.next();
            StringTokenizer st = new StringTokenizer(format, "=");
            if (st.countTokens() == 2) {
                String localeName = st.nextToken().trim();
                String formatString = st.nextToken().trim();
                map.put(localeName.toLowerCase(), formatString);
            } else {
                debug.error("Incorrect common name format, " +
                    format + " in Globalization Service");
            }
        }

        return map;
    }

    /**
     * Returns a map of common name formats.
     */
    public Map getFormats(AMModel model) {
        String startDN = model.getStartDN();
        Map map = (Map)mapRealmToFormat.get(startDN);
        if (map == null) {
            map = addFormats(startDN);
        }
        return map;
    }

    /**
     * This method will be invoked when a service's schema has been changed.
     *
     * @param seviceName name of the service
     * @param version version of the service
     */
    public void schemaChanged(String serviceName, String version) {
        synchronized (mapRealmToFormat) {
            mapRealmToFormat.remove(ROOT);
        }
    }

    /**
     * This method will be invoked when a service's global configuration
     * data has been changed. The parameter <code>groupName</code> denotes the
     * name of the configuration grouping (e.g. default) and
     * <code>serviceComponent</code> denotes the service's sub-component that
     * changed (e.g. <code>/NamedPolicy</code>, <code>/Templates</code>).
     *
     * @param seviceName name of the service
     * @param version version of the service
     * @param groupName name of the configuration grouping
     * @param serviceComponent name of the service components that
     *        changed
     * @param type change type, i.e., ADDED, REMOVED or MODIFIED
     */
    public void globalConfigChanged(
        String serviceName,
        String version,
        String groupName,
        String serviceComponent,
        int type)
    {
        //NO-OP
    }

    /**
     * This method will be invoked when a service's organization
     * configuration data has been changed. The parameters
     * <code>orgName</code>, <code>groupName</code> and 
     * <code>serviceComponent</code> denotes the organization name,
     * configuration grouping name and service's sub-component that are
     * changed respectively.
     *
     * @param seviceName name of the service
     * @param version version of the service
     * @param orgName organization name as DN
     * @param groupName name of the configuration grouping
     * @param serviceComponent the name of the service components that
     *        changed
     * @param type change type, i.e., ADDED, REMOVED or MODIFIED
     */
    public void organizationConfigChanged(
        String serviceName,
        String version,
        String orgName,
        String groupName,
        String serviceComponent,
        int type)
    {
        synchronized (mapRealmToFormat) {
            mapRealmToFormat.remove(orgName);
        }
    }
}
