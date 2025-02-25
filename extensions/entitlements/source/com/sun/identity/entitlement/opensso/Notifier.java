/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: Notifier.java,v 1.3 2009-08-05 19:05:27 veiming Exp $
 */

package com.sun.identity.entitlement.opensso;

import com.iplanet.am.util.SystemProperties;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.common.configuration.ServerConfiguration;
import com.sun.identity.entitlement.EntitlementThreadPool;
import com.sun.identity.entitlement.PrivilegeManager;
import com.sun.identity.entitlement.interfaces.IThreadPool;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.sm.SMSException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.AccessController;
import java.util.Map;
import java.util.Set;

public class Notifier implements Runnable {
    private static final String currentServerInstance =
        SystemProperties.getServerInstanceName();
    private String action;
    private Map<String, String> params;
    private static IThreadPool threadPool = new EntitlementThreadPool(4);
    private static boolean ssoadm = Boolean.valueOf(
        System.getProperty("ssoadm", "false")).booleanValue();

    public static void submit(String action, Map<String, String> params) {
        threadPool.submit(new Notifier(action, params));
    }

    private Notifier(String action, Map<String, String> params) {
        this.action = action;
        this.params = params;
    }

    public void run() {
        try {
            SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
                AdminTokenAction.getInstance());
            Set<String> serverURLs =
                ServerConfiguration.getServerInfo(adminToken);

            for (String url : serverURLs) {
                int idx = url.indexOf("|");
                if (idx != -1) {
                    url = url.substring(0, idx);
                }

                if (ssoadm || !url.equals(currentServerInstance)) {
                    String strURL = url + NotificationServlet.CONTEXT_PATH +
                        "/" + action;

                    StringBuffer buff = new StringBuffer();
                    boolean bFirst = true;
                    for (String k : params.keySet()) {
                        if (bFirst) {
                            bFirst = false;
                        } else {
                            buff.append("&");
                        }
                        buff.append(URLEncoder.encode(k, "UTF-8")).append("=")
                            .append(URLEncoder.encode(params.get(k),
                            "UTF-8"));
                    }
                    postRequest(strURL, buff.toString());
                }
            }
        } catch (UnsupportedEncodingException ex) {
            PrivilegeManager.debug.error("Notifier.notifyChanges", ex);
        } catch (IOException ex) {
            PrivilegeManager.debug.error("Notifier.notifyChanges", ex);
        } catch (SMSException ex) {
            PrivilegeManager.debug.error("Notifier.notifyChanges", ex);
        } catch (SSOException ex) {
            PrivilegeManager.debug.error("DataStore.notifyChanges", ex);
        }
    }

    private String postRequest(String strURL, String data)
        throws IOException {

        OutputStreamWriter wr = null;
        BufferedReader rd = null;

        try {
            URL url = new URL(strURL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            wr = new OutputStreamWriter(
                conn.getOutputStream());
            wr.write(data);
            wr.flush();

            rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString();
        } finally {
            if (wr != null) {
                wr.close();
            }
            if (rd != null) {
                rd.close();
            }
        }
    }
}