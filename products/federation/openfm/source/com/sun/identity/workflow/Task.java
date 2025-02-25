/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: Task.java,v 1.10 2008-10-29 00:02:39 veiming Exp $
 *
 */

package com.sun.identity.workflow;

import com.iplanet.am.util.SystemProperties;
import com.sun.identity.saml2.meta.SAML2MetaException;
import com.sun.identity.saml2.meta.SAML2MetaManager;
import com.sun.identity.shared.Constants;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;

/**
 * Base class for all Tasks.
 */
public abstract class Task
    implements ITask 
{
    private static Map resMap = new HashMap();
    static String REQ_OBJ = "_request_";

    protected String getString(Map params, String key) {
        Object values = params.get(key);
        if (values == null) {
            return null;
        }
        if (values instanceof Set) {
            Set set = (Set)values;
            return (!set.isEmpty()) ? (String)set.iterator().next() : null;
        } else {
            return (String)values;
        }
    }

    protected static ResourceBundle getResourceBundle(Locale locale) {
        ResourceBundle rb = (ResourceBundle)resMap.get(locale);
        if (rb == null){
            rb = ResourceBundle.getBundle("workflowMessages" ,locale);
            resMap.put(locale, rb);
        }
        return rb;
    }

    protected static String getMessage(String key, Locale locale) {
        ResourceBundle resBundle = getResourceBundle(locale);
        return resBundle.getString(key);
    }

    public static String getContent(String resName, Locale locale)
        throws WorkflowException {
        if (resName.startsWith("http://") ||
            resName.startsWith("https://")
        ) {
            return getWebContent(resName, locale);
        } else {
            return resName;
        }
    }
    
    protected String getFileContent(String filename)
        throws WorkflowException {
        StringBuffer buff = new StringBuffer();
        try {
            FileReader input = new FileReader(filename);
            BufferedReader bufRead = new BufferedReader(input);
            String line = bufRead.readLine();
            while (line != null) {
                buff.append(line).append("\n");
                line = bufRead.readLine();
            }
            return buff.toString();
        } catch (IOException e){
            throw new WorkflowException(e.getMessage());
        }
    }
    
    private static String getWebContent(String url, Locale locale)
        throws WorkflowException {
        try {
            StringBuffer content = new StringBuffer();
            URL urlObj = new URL(url);
            URLConnection conn = urlObj.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection)conn;
                httpConnection.setRequestMethod("GET");
                httpConnection.setDoOutput(true);

                httpConnection.connect();
                int response = httpConnection.getResponseCode();
                InputStream is = httpConnection.getInputStream();
                BufferedReader dataInput = new BufferedReader(
                    new InputStreamReader(is));
                String line = dataInput.readLine();

                while (line != null) {
                    content.append(line).append('\n');
                    line = dataInput.readLine();
                }
            }
            return content.toString();
        } catch (ProtocolException e) {
            Object[] param = {url};
            throw new WorkflowException(MessageFormat.format(
                getMessage("unable.to.reach.url", locale), param));
        } catch (MalformedURLException e) {
            Object[] param = {url};
            throw new WorkflowException(MessageFormat.format(
                getMessage("malformedurl", locale), param));
        } catch (IOException e) {
            Object[] param = {url};
            throw new WorkflowException(MessageFormat.format(
                getMessage("unable.to.reach.url", locale), param));
        }
    }
    
    protected List getAttributeMapping(Map params) {
        List list = new ArrayList();
        String strAttrMapping = getString(params, ParameterKeys.P_ATTR_MAPPING);
        if ((strAttrMapping != null) && (strAttrMapping.length() > 0)) {
            StringTokenizer st = new StringTokenizer(strAttrMapping, "|");
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (s.length() > 0) {
                    list.add(s);
                }
            }
        }
        return list;
    }
    
    static String generateMetaAliasForIDP(String realm)
        throws WorkflowException {
        try {
            Set metaAliases = new HashSet();
            SAML2MetaManager mgr = new SAML2MetaManager();
            metaAliases.addAll(
                mgr.getAllHostedIdentityProviderMetaAliases(realm));
            metaAliases.addAll(
                mgr.getAllHostedServiceProviderMetaAliases(realm));
            String metaAliasBase = (realm.equals("/")) ? "/idp" : realm + "/idp";
            String metaAlias = metaAliasBase;
            int counter = 1;

            while (metaAliases.contains(metaAlias)) {
                metaAlias = metaAliasBase + Integer.toString(counter);
                counter++;
            }
            return metaAlias;
        } catch (SAML2MetaException e) {
            throw new WorkflowException(e.getMessage());
        }
    }
    
    static String generateMetaAliasForSP(String realm)
        throws WorkflowException {
        try {
            Set metaAliases = new HashSet();
            SAML2MetaManager mgr = new SAML2MetaManager();
            metaAliases.addAll(
                mgr.getAllHostedIdentityProviderMetaAliases(realm));
            metaAliases.addAll(
                mgr.getAllHostedServiceProviderMetaAliases(realm));
            String metaAliasBase = (realm.equals("/")) ? "/sp" : realm + "/sp";
            String metaAlias = metaAliasBase;
            int counter = 1;

            while (metaAliases.contains(metaAlias)) {
                metaAlias = metaAliasBase + Integer.toString(counter);
                counter++;
            }
            return metaAlias;
        } catch (SAML2MetaException e) {
            throw new WorkflowException(e.getMessage());
        }
    }

    protected String getRequestURL(Map map) {
        boolean isConsoleRemote = Boolean.valueOf(
            SystemProperties.get(Constants.AM_CONSOLE_REMOTE)).booleanValue();

        if (isConsoleRemote) {
            return SystemProperties.getServerInstanceName();
        } else {
            HttpServletRequest req = (HttpServletRequest) map.get(REQ_OBJ);
            String uri = req.getRequestURI().toString();
            int idx = uri.indexOf('/', 1);
            uri = uri.substring(0, idx);
            return req.getScheme() + "://" + req.getServerName() +
                ":" + req.getServerPort() + uri;
        }

    }
}
