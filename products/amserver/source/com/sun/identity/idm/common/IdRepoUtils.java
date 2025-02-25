/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems, Inc. All Rights Reserved.
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
 * $Id: IdRepoUtils.java,v 1.3 2010-01-06 22:31:55 veiming Exp $
 */

package com.sun.identity.idm.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;

import com.iplanet.am.sdk.AMHashMap;
import com.iplanet.am.util.SSLSocketFactoryManager;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.common.LDAPUtils;
import com.sun.identity.idm.IdConstants;
import com.sun.identity.idm.IdRepoBundle;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.shared.StringUtils;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.ldap.LDAPAttribute;
import com.sun.identity.shared.ldap.LDAPConnection;
import com.sun.identity.shared.ldap.LDAPEntry;
import com.sun.identity.shared.ldap.LDAPException;
import com.sun.identity.shared.ldap.LDAPSearchResults;
import com.sun.identity.shared.ldap.LDAPv2;
import com.sun.identity.setup.ServicesDefaultValues;
import com.sun.identity.sm.OrganizationConfigManager;
import com.sun.identity.sm.SMSException;
import com.sun.identity.sm.ServiceConfig;
import com.sun.identity.sm.ServiceConfigManager;


/**
 * This class provides common utility functions for IdRepo.
 */
public class IdRepoUtils {

    private static final String LDAPv3ForSUNDS = "LDAPv3ForAMDS";
    private static final String LDAPv3ForOpenDS = "LDAPv3ForOpenDS";
    private static final String LDAPv3ForAD = "LDAPv3ForAD";
    private static final String LDAPv3ForADAM = "LDAPv3ForADAM";
    private static final String LDAPv3ForTivoli = "LDAPv3ForTivoli";

    private static final String SUNDS_LDIF = "sundsSchema";
    private static final String OpenDS_LDIF = "opendsUserSchema";
    private static final String AD_LDIF = "adUserSchema";
    private static final String ADAM_LDIF = "adamUserSchema";
    private static final String TIVOLI_LDIF = "tivoliUserSchema";

    private static final String SCHEMA_PROPERTY_FILENAME = "schemaNames";
    private static Set defaultPwdAttrs = null;
    private static Debug debug = Debug.getInstance("amIdm");

    static {
        defaultPwdAttrs = new HashSet();
        defaultPwdAttrs.add("userpassword");
        defaultPwdAttrs.add("unicodepwd");
    }

    /**
     * Returns an attribute map with all the password attributes being masked.
     * 
     * @param attrMap an attribute map
     * @param pwdAttrs a set of password attribute names
     *
     * @return an attribute map with all the password attributes being masked.
     */
    public static Map getAttrMapWithoutPasswordAttrs(Map attrMap,
        Set pwdAttrs) {

        if ((attrMap == null) || (attrMap.isEmpty())) {
            return attrMap;
        }

        Set allPwdAttrs = null;
        if ((pwdAttrs == null) || (pwdAttrs.isEmpty())) {
            allPwdAttrs = defaultPwdAttrs;
        } else {
            allPwdAttrs = new HashSet();
            allPwdAttrs.addAll(defaultPwdAttrs);
            allPwdAttrs.addAll(pwdAttrs);
        }

        AMHashMap returnAttrMap = null;
        for(Iterator iter = allPwdAttrs.iterator(); iter.hasNext(); ) {
            String pwdAttr = (String)iter.next();
            if (attrMap.containsKey(pwdAttr)) {
                if (returnAttrMap == null) {
                    returnAttrMap = new AMHashMap();
                    returnAttrMap.copy(attrMap);
                }
                returnAttrMap.put(pwdAttr, "xxx...");
            }
        }

        return (returnAttrMap == null ? attrMap : returnAttrMap);
    }

    private static String getSchemaFiles(String idRepoType) {
        ResourceBundle rb = ResourceBundle.getBundle(
            SCHEMA_PROPERTY_FILENAME);
        String schemaFiles = null;

        if (idRepoType.equals(LDAPv3ForSUNDS)) {
            schemaFiles = rb.getString(SUNDS_LDIF);
        } else if (idRepoType.equals(LDAPv3ForOpenDS)) {
            schemaFiles = rb.getString(OpenDS_LDIF);
        } else if (idRepoType.equals(LDAPv3ForAD)) {
            schemaFiles = rb.getString(AD_LDIF);
        } else if (idRepoType.equals(LDAPv3ForADAM)) {
            schemaFiles = rb.getString(ADAM_LDIF);
        } else if (idRepoType.equals(LDAPv3ForTivoli)) {
            schemaFiles = rb.getString(TIVOLI_LDIF);
        }

        return schemaFiles;
    }

    /**
     * Return true if specified IdRepo type has schemas.
     * 
     * @param idRepoType IdRepo type
     * @return true if specified IdRepo type has schemas
     */
    public static boolean hasIdRepoSchema(String idRepoType) {
        if (idRepoType == null) {
            return false;
        }

        String schemaFiles = getSchemaFiles(idRepoType);
        return ((schemaFiles != null) && (schemaFiles.trim().length() > 0));
            
    }

    /**
     * Loads schema to specified IdRepo.
     * 
     * @param ssoToken single sign on token of authenticated user identity
     * @param idRepoName IdRepo name
     * @param realm the realm
     * @param servletCtx the servlet context
     *
     * @throws IdRepoException If schema can't be loaded or there are
     *     repository related error conditions.
     */
    public static void loadIdRepoSchema(SSOToken ssoToken, String idRepoName,
        String realm, ServletContext servletCtx) throws IdRepoException {

        if (servletCtx == null) {
            return;
        }

        try {
            ServiceConfigManager svcCfgMgr = new ServiceConfigManager(
                IdConstants.REPO_SERVICE, ssoToken);
            ServiceConfig cfg = svcCfgMgr.getOrganizationConfig(realm, null);
            ServiceConfig ss = cfg.getSubConfig(idRepoName);
            if (ss == null) {
                if (debug.messageEnabled()) {
                    debug.message("IdRepoUtils.loadIdRepoSchema: data store " +
                    idRepoName + " for realm " + realm + " doesn't exist.");
                }
                Object args[] = { idRepoName, realm };
                throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "317",
                    args);
            }

            String idRepoType = ss.getSchemaID();

            Map attrValues = ss.getAttributes();

            String schemaFiles = getSchemaFiles(idRepoType);
            if ((schemaFiles == null) || (schemaFiles.trim().length() == 0)) {
                if (debug.messageEnabled()) {
                    debug.message("IdRepoUtils.loadIdRepoSchema: data store " +
                    idRepoName + " for realm " + realm + " doesn't have " +
                    "schema files.");
                }
                return;
            }

            StringTokenizer st = new StringTokenizer(schemaFiles);
            while (st.hasMoreTokens()) {
                String schemaFile = st.nextToken();
                tagSwapAndImportSchema(schemaFile, attrValues, servletCtx,
                    idRepoType);
            }
        } catch (SMSException smsex) {
            if (debug.messageEnabled()) {
                debug.message("IdRepoUtils.loadIdRepoSchema:", smsex);
            }
            Object args[] = { idRepoName, realm };
            throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "314", args);
        } catch (SSOException ssoex) {
            if (debug.messageEnabled()) {
                debug.message("IdRepoUtils.loadIdRepoSchema:", ssoex);
            }
            Object args[] = { idRepoName, realm };
            throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "315", args);
        } catch (Exception ex) {
            if (debug.messageEnabled()) {
                debug.message("IdRepoUtils.loadIdRepoSchema:", ex);
            }
            Object args[] = { idRepoName, realm, ex.getMessage() };
            throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "316", args);
       }
    }

    private static void tagSwapAndImportSchema(String schemaFile,
        Map attrValues, ServletContext servletCtx, String idRepoType)
        throws Exception {

        LDAPConnection ld = null;
        InputStreamReader fin = null;
        DataInputStream dis = null;
        try {
            ld = getLDAPConnection(attrValues);

            fin = new InputStreamReader(servletCtx.getResourceAsStream(
                schemaFile));

            StringBuffer sbuf = new StringBuffer();
            char[] cbuf = new char[1024];
            int len;
            while ((len = fin.read(cbuf)) > 0) {
                sbuf.append(cbuf, 0, len);
            }
            String schemaStr = sbuf.toString();

            String suffix = CollectionHelper.getMapAttr(attrValues,
                "sun-idrepo-ldapv3-config-organization_name");
            if (suffix != null) {
                schemaStr = StringUtils.strReplaceAll(schemaStr, 
                    "@userStoreRootSuffix@", suffix);
                String dbName =LDAPUtils.getDBName(suffix, ld);
                schemaStr = StringUtils.strReplaceAll(schemaStr, "@DB_NAME@",
                    dbName);
            }

            if (idRepoType.equals(LDAPv3ForADAM)) {
                String adamInstanceGUID = getADAMInstanceGUID(attrValues);
                if (adamInstanceGUID != null) {
                    schemaStr = StringUtils.strReplaceAll(schemaStr, 
                        "@INSTANCE_GUID@", adamInstanceGUID);
                }
            }
	    schemaStr = ServicesDefaultValues.tagSwap(schemaStr);

            dis = new DataInputStream(
                new ByteArrayInputStream(schemaStr.getBytes()));
            LDAPUtils.createSchemaFromLDIF(dis, ld);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (Exception ex) {
                    //No handling requried
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception ex) {
                    //No handling requried
                }
            }
            if ((ld != null) && ld.isConnected()) {
                try {
                    ld.disconnect();
                } catch (LDAPException ex) {
                }
            }
        }
    }

    private static String getADAMInstanceGUID(Map attrValues) throws Exception {
        LDAPConnection ld = null;
        try {
            ld = getLDAPConnection(attrValues);
            String attrName = "schemaNamingContext";
            String[] attrs = { attrName };
            LDAPSearchResults res = ld.search("", LDAPv2.SCOPE_BASE,
                "(objectclass=*)", null, false );
            if (res.hasMoreElements()) {
                LDAPEntry entry = (LDAPEntry)res.nextElement();
                LDAPAttribute ldapAttr = entry.getAttribute(attrName);
                if (ldapAttr != null) {
                    String value = ldapAttr.getStringValueArray()[0];
                    int index = value.lastIndexOf("=");
                    if (index != -1) {
                        return value.substring(index + 1).trim();
                    }
                }
            }
        } finally {
            if ((ld != null) && ld.isConnected()) {
                try {
                    ld.disconnect();
                } catch (LDAPException ex) {
                }
            }
        }

        return null;
    }

    private static LDAPConnection getLDAPConnection(Map attrValues) 
        throws Exception {
        String s = CollectionHelper.getMapAttr(attrValues,
            "sun-idrepo-ldapv3-config-ssl-enabled");
        boolean ssl = ((s != null) && s.equals("true"));
        LDAPConnection ld = (ssl) ? new LDAPConnection(
            SSLSocketFactoryManager.getSSLSocketFactory()) :
            new LDAPConnection();
        ld.setConnectTimeout(300);

        String hostPort = CollectionHelper.getMapAttr(attrValues,
            "sun-idrepo-ldapv3-config-ldap-server");
        if ((hostPort == null) || (hostPort.trim().length() == 0)) {
            throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "405", null);
        }

        String bindDn = CollectionHelper.getMapAttr(attrValues,
            "sun-idrepo-ldapv3-config-authid");
        if ((bindDn == null) || (bindDn.trim().length() == 0)) {
            throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "405", null);
        }
        String bindPwd = CollectionHelper.getMapAttr(attrValues,
            "sun-idrepo-ldapv3-config-authpw");
        if ((bindPwd == null) || (bindPwd.trim().length() == 0)) {
            throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "405", null);
        }

        // hostPort contains port so 389 will be ignored
        ld.connect(3, hostPort, 389, bindDn, bindPwd);
        return ld;
    }
}
