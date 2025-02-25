/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: IDPSessionListener.java,v 1.10 2009-09-23 22:28:31 bigfatrat Exp $
 *
 */


package com.sun.identity.saml2.profile;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

import com.sun.identity.plugin.monitoring.FedMonAgent;
import com.sun.identity.plugin.monitoring.FedMonSAML2Svc;
import com.sun.identity.plugin.monitoring.MonitorManager;
import com.sun.identity.plugin.session.SessionListener;
import com.sun.identity.plugin.session.SessionManager;
import com.sun.identity.plugin.session.SessionProvider;
import com.sun.identity.plugin.session.SessionException;
import com.sun.identity.saml2.assertion.NameID;
import com.sun.identity.saml2.common.SAML2Constants;
import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.common.SAML2Repository;
import com.sun.identity.saml2.common.SAML2Utils;
import com.sun.identity.saml2.jaxb.entityconfig.BaseConfigType;
import com.sun.identity.saml2.jaxb.entityconfig.SPSSOConfigElement;
import com.sun.identity.saml2.jaxb.metadata.SPSSODescriptorElement;
import com.sun.identity.saml2.logging.LogUtil;
import com.sun.identity.saml2.meta.SAML2MetaException;
import com.sun.identity.saml2.meta.SAML2MetaManager;
import com.sun.identity.saml2.meta.SAML2MetaUtils;
import com.sun.identity.shared.debug.Debug;


/**
 * The class <code>IDPSessionListener</code> implements
 * SessionListener interface and is used for maintaining the 
 * IDP session cache.
 */

public class IDPSessionListener 
             implements SessionListener {

    private static SAML2MetaManager sm = null;
    private static Debug debug = SAML2Utils.debug;
    private static FedMonAgent agent;
    private static FedMonSAML2Svc saml2Svc;

    static {
        try {
            sm = new SAML2MetaManager();
        } catch (SAML2MetaException sme) {
            debug.error("Error retreiving metadata",sme);
        }
        agent = MonitorManager.getAgent();
        saml2Svc = MonitorManager.getSAML2Svc();
    }
    /**
     *  Constructor of <code>IDPSessionListener</code>.
     */

    public IDPSessionListener() {
    }

    /**
     *  Callback for SessionListener.
     *  It is used for cleaning up the IDP session cache.
     *  
     *  @param session The session object
     */
    public void sessionInvalidated(Object session)
    {
        String classMethod = "IDPSessionListener.sessionInvalidated: ";
        HashMap paramsMap = new HashMap();
        
        if (SAML2Utils.debug.messageEnabled()) {
            SAML2Utils.debug.message(
                classMethod + "Entering ...");
        }
        if (session == null) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message(
                    classMethod + "Session is null.");
            }
            return;
        }
        try {
            SessionProvider sessionProvider = SessionManager.getProvider();
            
            String[] values = sessionProvider.getProperty(
               session, SAML2Constants.IDP_SESSION_INDEX);
            if (values == null || values.length == 0) {
                if (SAML2Utils.debug.messageEnabled()) {
                    SAML2Utils.debug.message(
                        classMethod +
                        "No sessionIndex stored in session.");
                }
                return;
            }
            String sessionIndex = values[0];
            if (sessionIndex == null || sessionIndex.length() == 0) {
                if (SAML2Utils.debug.messageEnabled()) {
                    SAML2Utils.debug.message(
                        classMethod +
                        "No sessionIndex stored in session.");
               }
               return;
            }

            IDPSession idpSession = (IDPSession)IDPCache.
                                    idpSessionsByIndices.get(sessionIndex);
            if (idpSession != null) {
                
                paramsMap.put(SAML2Constants.ROLE, SAML2Constants.IDP_ROLE);

                String metaAlias = idpSession.getMetaAlias();

                String realm = SAML2Utils.
                    getRealm(SAML2MetaUtils.getRealmByMetaAlias(metaAlias));

                String idpEntityID = sm.getEntityByMetaAlias(metaAlias);
               
                try {
                    List list = (List)idpSession.getNameIDandSPpairs();
                    for (Iterator iter = list.iterator(); iter.hasNext();) {
                        NameIDandSPpair pair = (NameIDandSPpair)iter.next();
                        String spEntityID = pair.getSPEntityID();
                        NameID nameID = pair.getNameID();

                        BaseConfigType idpConfig =
                               sm.getIDPSSOConfig(realm, idpEntityID);

                        if (idpConfig != null) {
                            List idpSessionSyncList =
                               (List) SAML2MetaUtils.getAttributes(idpConfig).
                                get(SAML2Constants.IDP_SESSION_SYNC_ENABLED);

                            if ((idpEntityID != null &&
                                spEntityID != null &&
                                idpSessionSyncList != null &&
                                idpSessionSyncList.size() != 0)) {

                                boolean idpSessionSyncEnabled =
                                     ((String)idpSessionSyncList.get(0)).
                                      equals(SAML2Constants.TRUE)? true : false;
                                 // Initiate IDP SLO on IDP Idle/Max
                                 // session timeout only when the Session
                                 // Sync flag is enabled
                                if (idpSessionSyncEnabled) {
                                    if (SAML2Utils.debug.messageEnabled()) {
                                         SAML2Utils.debug.message(
                                          classMethod +
                                          "IDP Session Synchronization flag " +
                                          "is enabled, initiating SLO to SP");
                                    }
                                    initiateIDPSingleLogout(sessionIndex,
                                                            metaAlias,
                                                            realm,
                                                            SAML2Constants.SOAP,
                                                            nameID,
                                                            idpEntityID,
                                                            spEntityID,
                                                            paramsMap);
                                }
                            }
                        } else {
                            if (SAML2Utils.debug.messageEnabled()) {
                                SAML2Utils.debug.message(
                                             classMethod +
                                             "Unable to retrieve the IDP " +
                                             "config data, idpConfig is null");
                            }
                        }
                    }
                } catch (SAML2MetaException sme) {
                       SAML2Utils.debug.error(
                           "IDPSessionListener.sessionInvalidated:", sme);
                } catch (SAML2Exception se) {
                           SAML2Utils.debug.error(
                           "IDPSessionListener.sessionInvalidated:", se);
                } catch (SessionException s) {
                           SAML2Utils.debug.error(
                           "IDPSessionListener.sessionInvalidated:", s);
                }
               
                synchronized(IDPCache.idpSessionsByIndices) {
                    List list = (List)idpSession.getNameIDandSPpairs();
                    for(Iterator iter = list.iterator(); iter.hasNext();) {
                        NameIDandSPpair pair = (NameIDandSPpair)iter.next();
                        NameID nameID = pair.getNameID();
                        if (SAML2Constants.NAMEID_TRANSIENT_FORMAT.equals(
                            nameID.getFormat())) {
                            IDPCache.userIDByTransientNameIDValue.remove(
                                   nameID.getValue());
                        }
                    }
                }
            } else {
                if (SAML2Utils.debug.messageEnabled()) {
                    SAML2Utils.debug.message(
                        classMethod +
                        "IDP Session with session index " +
                        sessionIndex + " already removed.");
                }
            }

            IDPCache.idpSessionsByIndices.remove(sessionIndex);
            IDPCache.authnContextCache.remove(sessionIndex);
            String  sessID = sessionProvider.getSessionID(session);
            if (IDPCache.idpSessionsBySessionID.get(sessID) != null) {
                IDPCache.idpSessionsBySessionID.remove(sessID);
                if ((agent != null) && agent.isRunning() && (saml2Svc != null)){
                    saml2Svc.setIdpSessionCount(
		        (long)IDPCache.idpSessionsBySessionID.size());
                }
            }
           
            if (IDPCache.spSessionPartnerBySessionID.get(sessID) != null) {
                IDPCache.spSessionPartnerBySessionID.remove(sessID);
            }

            if (SAML2Utils.isSAML2FailOverEnabled()) {
                SAML2Repository.getInstance().delete(sessionIndex);   
            }       
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message(
                   classMethod +
                   "cleaned up the IDP session cache for a session" +
                   " expiring or being destroyed: sessionIndex=" +
                   sessionIndex);
           }
        } catch (SessionException e) {
            if (SAML2Utils.debug.warningEnabled()) {
                SAML2Utils.debug.warning(
                        classMethod + "invalid or expired session.", e);
            }
        } catch (SAML2MetaException samlme) {
            if (SAML2Utils.debug.warningEnabled()) {
                SAML2Utils.debug.warning(
                        classMethod + "unable to retrieve idp entity id.",
                        samlme);
            }
        } catch (SAML2Exception samle) {
            if (SAML2Utils.debug.warningEnabled()) {
                SAML2Utils.debug.warning(
                        classMethod + "SAML2 Repository error.", samle);
            }
        }
    }

    /**
     * Initiates IDP Single logout <code>initiateIDPSingleLogout</code> to
     * all the SPs
     *
     * @param sessionIndex Session Index
     * @param metaAlias IDP meta alias
     * @param realm Realm
     * @param binding Binding used   
     * @param nameID the NameID
     * @param idpEntityID  IDP Entity ID
     * @param spEntityID  SP Entity ID
     * @param paramsMap  parameters map
     * 
     * <code>initiateIDPSingleLogout</code>.
     * @return
     * @throws SAML2MetaException if error processing
     *          <code>initiateIDPSingleLogout</code>.
     * @throws SAML2Exception if error processing
     *          <code>initiateIDPSingleLogout</code>.
     * @throws SessionException if error processing
     *          <code>initiateIDPSingleLogout</code>.
     */
    private void initiateIDPSingleLogout(String sessionIndex,
                                         String metaAlias,
                                         String realm,
                                         String binding,
                                         NameID nameID,
                                         String idpEntityID,
                                         String spEntityID,                                         
                                         Map paramsMap)
                                         throws SAML2MetaException,
                                         SAML2Exception, SessionException {
                 
        // get SPSSODescriptor
        SPSSODescriptorElement spsso = sm.getSPSSODescriptor(realm, spEntityID);

        if (spsso == null) {
            String[] data = {spEntityID};
            LogUtil.error(Level.INFO,LogUtil.SP_METADATA_ERROR,data, null);
                    throw new SAML2Exception(
                        SAML2Utils.bundle.getString("metaDataError"));
        }

        List slosList = spsso.getSingleLogoutService();
        if (slosList == null) {
            String[] data = {idpEntityID};
            LogUtil.error(Level.INFO,LogUtil.SLO_NOT_FOUND,data, null);
                    throw new SAML2Exception(
                        SAML2Utils.bundle.getString("sloServiceListNotfound"));
        }

        // get SP SLO list
        SPSSOConfigElement spConfig = null;
        spConfig = sm.getSPSSOConfig(realm, spEntityID);

        String location = LogoutUtil.getSLOServiceLocation(slosList,
                                                           SAML2Constants.SOAP);

        // get SP entity config in case of SOAP, for basic auth info
        location = SAML2Utils.fillInBasicAuthInfo(spConfig, location);

        LogoutUtil.doLogout(metaAlias,
                            spEntityID,
                            slosList, null, binding,
                            null, sessionIndex,
                            nameID, null, null, paramsMap, spConfig);
    }
}
