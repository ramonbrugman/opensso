/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: CommonUtils.java,v 1.8 2009-01-28 05:34:48 ww203982 Exp $
 *
 */

package com.iplanet.am.sdk.ldap;

import com.iplanet.am.sdk.AMHashMap;
import com.iplanet.am.util.SystemProperties;
import com.iplanet.services.ldap.Attr;
import com.iplanet.services.ldap.AttrSet;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.ums.Guid;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.sm.SMSException;
import com.sun.identity.sm.ServiceConfig;
import com.sun.identity.sm.ServiceConfigManager;
import java.security.AccessController;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.sun.identity.shared.ldap.util.DN;

/**
 * This class contains all the miscellaneous utility methods used in the
 * <code>com.iplanet.am.sdk</code> package.
 */
class CommonUtils {

    // Constants
    private static final String LOCALE_PROPERTY = "Locale";

    // this is a hidden property and is straightly for sdk developer
    // to use for debugging purpose only. It allow us to change
    // the name of the local sdk debug file so we can set it to amProfile.
    // thus making it easier to follow the logic flow.
    private static final String AM_SDK_DEBUG_FILE_PROPS = 
        "com.iplanet.am.sdk.ldap.debugFileName";

    private static final String AM_SDK_DEBUG_FILE = "amProfile_ldap";

    private static SSOToken internalToken = null;

    /**
     * Debug variable to be used by AM SDK package
     */
    protected static Debug debug;

    protected static Map supportedTypes = new HashMap();

    protected static Map supportedNames = new HashMap();

    protected static Map creationtemplateMap = new HashMap();

    protected static Map statusAttributeMap = new HashMap();

    protected static Map searchtemplateMap = new HashMap();

    private static final String LOCALE_INTEGRATION_PROPERTY = 
        "locale.integration";

    protected static boolean integrateLocale = true;

    static {

        debug = Debug.getInstance(SystemProperties.get(AM_SDK_DEBUG_FILE_PROPS,
                AM_SDK_DEBUG_FILE));
        String cCaller = SystemProperties.get(LOCALE_INTEGRATION_PROPERTY);
        if ((cCaller == null) || (cCaller.equalsIgnoreCase("true"))) {
            integrateLocale = true;
            if (debug.messageEnabled()) {
                debug.message("AM SDK: Locale integration enabled");
            }
        } else {
            integrateLocale = false;
        }
        populateManagedObjects();
    }

    /**
     * Gets the internal Access Manager user token (dsameuser)
     * 
     * @return the internal user token (dsameuser)
     */
    protected static SSOToken getInternalToken() {
        if (internalToken == null) {
            internalToken = (SSOToken) AccessController
                    .doPrivileged(AdminTokenAction.getInstance());
        }
        return internalToken;
    }

    protected static Debug getDebugInstance() {
        return debug;
    }

    /**
     * Method to get the user locale.
     * 
     * @param token
     *            SSOToken of the authenticated user
     * @return a String value representing the user locale
     */
    protected static String getUserLocale(SSOToken token) {
        try {
            String locale = token.getProperty(LOCALE_PROPERTY);

            if (debug.messageEnabled()) {
                debug.message("CommonUtils.getUserLocale(): locale = "
                        + locale);
            }
            return locale;
        } catch (SSOException ssoe) {
            debug.error("CommonUtils.getUserLocale(): missing locale, "
                    + "setting to null");
            return null;
        }
    }

    /**
     * Combines 2 AttrSets and returns the result set. The original sets are not
     * modified.
     * 
     * @param attrSet1
     *            the first AttrSet
     * @param attrSet2
     *            the second AttrSet
     * @return an AttrSet which has combined values of attrSet1 & attrSet2
     */
    protected static AttrSet combineAttrSets(AttrSet attrSet1, AttrSet attrSet2)
    {
        AttrSet retAttrSet = new AttrSet();
        if (attrSet1 != null) {
            int count = attrSet1.size();
            for (int i = 0; i < count; i++) {
                Attr attr = attrSet1.elementAt(i);
                retAttrSet.add(attr);
            }
        }

        if (attrSet2 != null) {
            int count = attrSet2.size();
            for (int i = 0; i < count; i++) {
                Attr attr = attrSet2.elementAt(i);
                retAttrSet.add(attr);
            }
        }
        return retAttrSet;
    }

    /**
     * Merge the values in two maps and return the result map. The values in the
     * smaller map are merged with the larger map and the larger map is
     * returned.
     * 
     * @param mapA
     *            the first map
     * @param mapB
     *            the second map
     * @return a result map which the biggest of the two maps with values merged
     *         from the smaller one.
     */
    protected static Map mergeMaps(Map mapA, Map mapB) {
        if (mapA == null && mapB == null) {
            return null;
        }
        if (mapA == null || mapA.isEmpty()) {
            return mapB;
        } else if (mapB == null || mapB.isEmpty()) {
            return mapA;
        }
        // Find the map with smaller size and iterate through it.
        Map bigMap = ((mapA.size() > mapB.size()) ? mapA : mapB);
        Map smallMap = ((mapA.size() <= mapB.size()) ? mapA : mapB);
        Iterator itr = smallMap.keySet().iterator();
        while (itr.hasNext()) {
            String attrName = (String) itr.next();
            Set values = (Set) bigMap.get(attrName);
            if (values != null) {
                values.addAll((Set) smallMap.get(attrName));
            } else {
                bigMap.put(attrName, (Set) smallMap.get(attrName));
            }
        }
        return bigMap;
    }

    protected static String mapSetToString(Map map) {
        StringBuffer sb = new StringBuffer();
        if (map != null && !map.isEmpty()) {
            Iterator itr = map.keySet().iterator();
            while (itr.hasNext()) {
                String name = (String) itr.next();
                Set values = (Set) map.get(name);
                sb.append("\n\tName: ").append(name);
                sb.append(" Values: ").append(values.toString());
            }
        } else {
            sb.append("<empty>");
        }
        return sb.toString();
    }

    protected static String mapByteToString(Map map) {
        StringBuffer sb = new StringBuffer();
        if (map != null && !map.isEmpty()) {
            Iterator itr = map.keySet().iterator();
            while (itr.hasNext()) {
                String name = (String) itr.next();
                byte[][] values = (byte[][]) map.get(name);
                sb.append("\n\tName: ").append(name);
                sb.append(" Values: ").append(values);
            }
        } else {
            sb.append("<empty>");
        }
        return sb.toString();
    }

    protected static Set getSetCopy(Set values) {
        Set copyValues = Collections.EMPTY_SET;
        if (!values.isEmpty()) {
            copyValues = new HashSet(values.size());
            Iterator itr = values.iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                copyValues.add(value);
            }
        }
        return copyValues;
    }

    /**
     * Method to convert a String array to a set
     * 
     * @param strs
     *            the String array
     * @return a Set representing the String array
     */
    protected static Set stringArrayToSet(String[] strs) {
        int count = strs.length;
        Set set = ((count > 0) ? new HashSet(count) : new HashSet());

        for (int i = 0; i < count; i++) {
            set.add(strs[i]);
        }
        return set;
    }

    protected static Map attrSetToMap(AttrSet attrSet) {
        return attrSetToMap(attrSet, false);
    }

    /**
     * Method to convert a AttrSet object to Map.
     * 
     * @param attrSet
     *            the AttrSet to be converted to a Map
     * @param fetchByteValues
     *            if false stringValues are added, if true byteValues are added.
     * @return a Map containing attribute names as key's and a Set of attribute
     *         values or byte Values
     */
    protected static Map attrSetToMap(AttrSet attrSet, boolean fetchByteValues) 
    {
        Map attributesMap = new AMHashMap(fetchByteValues);
        if (attrSet == null) {
            return attributesMap;
        }
        int attrSetSize = attrSet.size();
        if (!fetchByteValues) {
            for (int i = 0; i < attrSetSize; i++) {
                Attr attr = attrSet.elementAt(i);
                String values[] = attr.getStringValues();
                attributesMap.put(attr.getName(), stringArrayToSet(values));
            }
        } else {
            for (int i = 0; i < attrSetSize; i++) {
                Attr attr = attrSet.elementAt(i);
                attributesMap.put(attr.getName(), attr.getByteValues());
            }
        }
        return attributesMap;
    }

    /**
     * Method to convert a Map to AttrSet.
     * 
     * @param map
     *            a map contaning attribute names as keys and a Set of attribute
     *            values corresponding to each map key.
     * @return an AttrSet having the contents of the supplied map
     */
    protected static AttrSet mapToAttrSet(Map map) {
        return mapToAttrSet(map, false);
    }

    /**
     * Method to convert a Map to AttrSet.
     * 
     * @param map
     *            a map contaning attribute names as keys and a Set of attribute
     *            values corresponding to each map key.
     * @param byteValues
     *            if true then values are bytes otherwise strings
     * @return an AttrSet having the contents of the supplied map
     */
    protected static AttrSet mapToAttrSet(Map map, boolean byteValues) {
        AttrSet attrSet = new AttrSet();
        if (map == null) {
            return attrSet;
        }

        if (!byteValues) {
            Iterator itr = map.keySet().iterator();
            while (itr.hasNext()) {
                String attrName = (itr.next()).toString();
                Set set = (Set) (map.get(attrName));
                String attrValues[] = (set == null) ? null : (String[]) set
                        .toArray(new String[set.size()]);
                if (attrValues != null) {
                    attrSet.replace(new Attr(attrName, attrValues));
                }
            }
        } else {
            Iterator itr = map.keySet().iterator();
            while (itr.hasNext()) {
                String attrName = (itr.next()).toString();
                byte[][] attrValues = (byte[][]) (map.get(attrName));
                if (attrValues != null) {
                    attrSet.replace(new Attr(attrName, attrValues));
                }
            }
        }
        return attrSet;
    }

    /**
     * Convert a Set to Guid object array
     * 
     * @param set
     *            the Set to be converted
     * @return an array of Guid[] objects
     */
    protected static Guid[] toGuidArray(Set set) {
        Object[] obj = set.toArray();
        Guid[] ss = new Guid[obj.length];
        for (int i = 0; i < obj.length; i++) {
            ss[i] = new Guid((String) obj[i]);
        }
        return ss;
    }

    /**
     * Convert a Set to Guid object array
     * 
     * @param set
     *            the Set to be converted
     * @return an array of Guid[] objects
     */
    protected static Guid[] getGuidArray(Set set) {
        return (Guid[]) set.toArray(new Guid[set.size()]);
    }

    /**
     * Converts a DN String to a RFC format and lowers case.
     * 
     * @param dn
     *            the DN String to be formated
     * @return a lowercase RFC fromat DN String
     */
    protected static String formatToRFC(String dn) {
        return (new DN(dn)).toRFCString().toLowerCase();
    }

    /**
     * Gets the principal DN String in RFC lowercase format from the SSOToken
     * 
     * @param token
     *            a valid SSOToken
     * @return a principal DN corresponding to token
     * @throws SSOException
     *             if the token is not valid
     */
    protected static String getPrincipalDN(SSOToken token) throws SSOException {
        String principalName = token.getPrincipal().getName();
        return formatToRFC(principalName);
    }

    /**
     * Combines two sets which contains objectclass values of an entry in a case
     * insensitive manner. OC values are sometimes returned by LDAP in mixed
     * case and when duplicate values for Objectclass exists in a mixed case,
     * there is a Object class violation Note: there is a possibility that this
     * method will return null
     */
    protected static Set combineOCs(Set one, Set two) {
        if (one == null || one.isEmpty()) {
            return two;
        }
        if (two == null || two.isEmpty()) {
            return one;
        }
        Set resultSet = new HashSet();
        Iterator itr1 = one.iterator();
        while (itr1.hasNext()) {
            String value1 = (String) itr1.next();
            resultSet.add(value1.toLowerCase());
        }

        Iterator itr2 = two.iterator();
        while (itr2.hasNext()) {
            String value2 = (String) itr2.next();
            resultSet.add(value2.toLowerCase());
        }
        return resultSet;
    }

    /**
     * Method to compare all the object classes in the Set with the specfied
     * object class. Will do a case insensitive comparision.
     * 
     * @param objectClasses
     *            Set of object classes
     * @param objectClass
     *            the specified object class
     * @return true if the specified object class is present. False otherwise.
     */
    protected static boolean isObjectClassPresent(Set objectClasses,
            String objectClass) {
        if (objectClasses != null && !objectClasses.isEmpty()) {
            Iterator itr = objectClasses.iterator();
            while (itr.hasNext()) {
                String serviceOC = (String) itr.next();
                if (serviceOC.equalsIgnoreCase(objectClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method which removes the specified object class from original set and
     * returns the set of object classes that were removed.
     * 
     * @param origSet
     *            the original Set
     * @param removeOCs
     *            the Set of object classes to be removed
     * @return the Set of object classes that were removed.
     */
    protected static Set updateAndGetRemovableOCs(Set origSet, Set removeOCs) {
        // The remove OCs will be removed from the origSet and only the ones
        // that were removed will be returned
        Set returnOCs = removeOCs;
        if (removeOCs != null && !removeOCs.isEmpty()) {
            returnOCs = new HashSet();
            Iterator itr1 = origSet.iterator();
            while (itr1.hasNext()) {
                String origOC = (String) itr1.next();
                Iterator itr2 = removeOCs.iterator();
                while (itr2.hasNext()) {
                    String removeOC = (String) itr2.next();
                    if (origOC.equalsIgnoreCase(removeOC)) {
                        // Can't remove from orig set directly. Will throw
                        // ConcurrentModificatiomException
                        returnOCs.add(origOC);
                        break;
                    }
                }
            }
            origSet.removeAll(returnOCs);
        }
        return returnOCs;
    }

    /**
     * Removes Empty sets from attribute-value maps. So that SDK does not try to
     * remove these attributes from newly created entries. Such empty sets
     * should be ignored, when entry is being created.
     * 
     * @param attrMap
     * @return map without empty set.
     */
    protected static Map removeEmptyValues(Map attrMap) {
        Map finalMap = new HashMap();
        Iterator iter = attrMap.keySet().iterator();

        while (iter.hasNext()) {
            String tStr = (String) iter.next();
            Set s = (Set) attrMap.get(tStr);

            if (!s.isEmpty()) {
                finalMap.put(tStr, attrMap.get(tStr));
            }
        }

        return finalMap;
    }

    protected static boolean populateManagedObjects() {
        try {
            ServiceConfigManager scm = new ServiceConfigManager("DAI",
                    getInternalToken());
            ServiceConfig gc = scm.getGlobalConfig(null);
            Set managedObjects = gc.getSubConfigNames("*", "ManagedObjects");
            if (managedObjects == null || managedObjects.isEmpty()) {
                debug.message("CommonUtils.populateManagedObject " +
                        "managedObjects=null");
                return false;
                // populateManagedObjectsWithDefaults();
            }
            Iterator mIter = managedObjects.iterator();
            while (mIter.hasNext()) {
                String mo = (String) mIter.next();
                mo = mo.toLowerCase();
                ServiceConfig sc = gc.getSubConfig(mo);
                if (sc != null) {
                    Map attrs = sc.getAttributes();
                    Set ocSet = (Set) attrs.get("objectclass");
                    Set ctSet = (Set) attrs.get("creationtemplatename");
                    Set stSet = (Set) attrs.get("searchtemplatename");
                    Set typeSet = (Set) attrs.get("type");
                    Set stAttr = (Set) attrs.get("statusattribute");
                    String oc = getObjectClass(ocSet, mo);
                    String ct = getCreationTemplateName(ctSet, mo);
                    String st = getSearchTemplateName(stSet, mo);
                    String stAttrName = getStatusAttributeName(stAttr);
                    // Assumes a type is always defined in the config.
                    // TODO be careful with NPE here.
                    String typeS = (String) typeSet.iterator().next();
                    // int type = Integer.parseInt(typeS);
                    supportedTypes.put(mo, typeS);
                    supportedNames.put(typeS, mo);
                    if (oc != null) {
                        ObjectClassManager.objectClassMap.put(typeS, oc);
                        ObjectClassManager.objectTypeMap.put(oc, typeS);
                    }
                    if (st != null)
                        searchtemplateMap.put(typeS, st);
                    if (ct != null)
                        creationtemplateMap.put(typeS, ct);
                    if (stAttrName != null)
                        statusAttributeMap.put(typeS, stAttrName);
                }
            }
            if (debug.messageEnabled()) {
                debug.message("CreationTemplate MAP = "
                        + creationtemplateMap.toString());
                debug.message("SearchTemplate Map = "
                        + searchtemplateMap.toString());
                debug.message("ObjectClass-Type Map = "
                        + ObjectClassManager.objectClassMap.toString());
                debug.message("Type-ObjectClass MAP = "
                        + ObjectClassManager.objectTypeMap.toString());
                debug.message("Supported names-type = "
                        + supportedTypes.toString());
                debug.message("Status Attributes= "
                        + statusAttributeMap.toString());
            }
        } catch (SMSException se) {
            return false;
            // populateManagedObjectsWithDefaults();
        } catch (SSOException ssoe) {
            return false;
            // populateManagedObjectsWithDefaults();
        }
        return true;
    }

    protected static void populateManagedObjectsWithDefaults() {
    }

    private static String getObjectClass(Set ocSet, String objectName) {
        if (ocSet == null || ocSet.isEmpty()) {
            return null;
        }
        return ((String) ocSet.iterator().next()).toLowerCase();
    }

    private static String getCreationTemplateName(Set ocSet, String objectName) 
    {
        if (ocSet == null || ocSet.isEmpty()) {
            return null;
        }
        return ((String) ocSet.iterator().next());
    }

    private static String getSearchTemplateName(Set ocSet, String objectName) {
        if (ocSet == null || ocSet.isEmpty()) {
            return null;
        }
        return ((String) ocSet.iterator().next());
    }

    private static String getStatusAttributeName(Set stSet) {
        if (stSet == null || stSet.isEmpty()) {
            return null;
        }
        return ((String) stSet.iterator().next());
    }

}
