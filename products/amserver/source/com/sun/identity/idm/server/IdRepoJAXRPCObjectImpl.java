/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
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
* $Id: IdRepoJAXRPCObjectImpl.java,v 1.13 2010-01-06 01:58:27 veiming Exp $
*/

package com.sun.identity.idm.server;

import com.iplanet.am.sdk.remote.*;
import com.iplanet.am.util.SystemProperties;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.iplanet.services.comm.server.PLLServer;
import com.iplanet.services.comm.server.SendNotificationException;
import com.iplanet.services.comm.share.Notification;
import com.iplanet.services.comm.share.NotificationSet;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.common.CaseInsensitiveHashMap;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdOperation;
import com.sun.identity.idm.IdRepo;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdRepoListener;
import com.sun.identity.idm.IdSearchControl;
import com.sun.identity.idm.IdSearchOpModifier;
import com.sun.identity.idm.IdSearchResults;
import com.sun.identity.idm.IdServices;
import com.sun.identity.idm.IdServicesFactory;
import com.sun.identity.idm.IdType;
import com.sun.identity.idm.IdUtils;
import com.sun.identity.idm.remote.IdRemoteServicesImpl;
import com.sun.identity.session.util.RestrictedTokenAction;
import com.sun.identity.session.util.RestrictedTokenContext;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.xml.XMLUtils;
import com.sun.identity.sm.SMSUtils;
import com.sun.identity.sm.SchemaType;

/**
 * Provides service side implementation of IdRepo for JAX-RPC interface
 * <class>DirectoryManagerIF</class>. Does not provide implementation for
 * deprecated AMSDK interfaces.
 * Remote clients using openssoclientsdk.jar would be calling these
 * methods using SOAP.
 * Implements identity changes notifications via PLL and also stores
 * notifications changes for polling clients.
 */
public abstract class IdRepoJAXRPCObjectImpl implements DirectoryManagerIF {
    
    protected static Debug idRepoDebug = Debug.getInstance("amIdmJAXRPCServer");
    
    protected static SSOTokenManager tokenManager;
        
    protected static IdServices idServices;
    
    protected static final String IDREPO_SERVICE = "IdRepoServiceIF";
    
    // Cache of modifications for last 30 minutes & notification URLs
    protected static int cacheSize = -1;
        
    static LinkedList idrepoCacheIndices = new LinkedList();
        
    static HashMap idrepoCache = null;
    
    protected static HashMap idRepoNotificationURLs = new HashMap();
    
    protected static String serverURL ;
    protected static URL urlServer;
    
    protected static String serverPort;
    
    protected static void initialize_cacheSize() {
        if (cacheSize > -1) {
            return;
        }
        
        // Obtain the cache size, if configured
        String cacheSizeStr = SystemProperties.get(
            "com.sun.am.event.notification.expire.time");
        try {
            cacheSize = Integer.parseInt(cacheSizeStr);
            if (cacheSize < 0) {
                cacheSize = 30;
            }
        } catch(NumberFormatException e) {
            cacheSize = 30;
        }
        if (idRepoDebug.messageEnabled()) {
            idRepoDebug.message("IdRepoJAXRPCObjectImpl.static " +
                "EventNotification cache size is set to " + cacheSize);
        }
    }

    private static void initialize_cache() {
        initialize_cacheSize();
        if (idrepoCache == null && cacheSize > 0) {
            idrepoCache = new HashMap(cacheSize);
        }
    }

    /**
     * Initializes this class with system properties.
     * Called only by getSSOToken() method. Hence all other methods
     * must call either getSSOToken() or initialize() directly.
     */
    protected static void initialize_idrepo() {
        initialize_cache();

        // Construct serverURL
        serverPort = SystemProperties.get(Constants.AM_SERVER_PORT);
        serverURL = SystemProperties.get(Constants.AM_SERVER_PROTOCOL) +
            "://" + SystemProperties.get(Constants.AM_SERVER_HOST) +
             ":" + serverPort;
        if (idRepoDebug.messageEnabled()) {
            idRepoDebug.message("IdRepoJAXRPCObjectImpl.static server URL " +
                serverURL);
        }
        
        // Initialize IdRepo Service Factory
        if (idServices == null) {
            idServices = IdServicesFactory.getDataStoreServices();
        }
    }

    protected boolean isClientOnSameServer(String clientURL) {
        // Check URL is not the local server

        boolean success = true;
        
        URL urlClient = null;
        try {
            urlClient = new URL(clientURL);
            if (urlServer == null) {
                urlServer = new URL(serverURL);
            }
        } catch (MalformedURLException e) {
            if (idRepoDebug.warningEnabled()) {
                idRepoDebug.warning("IdRepoJAXRPCObjectImpl." +
                    "isCientOnSameServer() - clientURL is malformed." +
                    clientURL);
            }
            success = false;
        }
        
        if (success) { // check if it is the same server
            int port = urlClient.getPort();
            if (port == -1) { 
                // If it is Port 80, and is not explicilty in the URL
                port = urlClient.getDefaultPort();              
            }
            String clientPort = Integer.toString(port);

            // Protocol is same - http, so no need to check that
            boolean sameServer = ((urlServer.getHost().equalsIgnoreCase(
                    urlClient.getHost())) && serverPort.equals(clientPort));

            idRepoDebug.message("IdRepoJAXRPCObjectImpl:" +
                    "checkIfClientOnSameServer() "                     
                    + "Received registerNotification request from client: " 
                    + clientURL + " Server URL " + serverURL 
                    + " Port determined as: " + clientPort + " Check is: " 
                    + sameServer);
            
            return sameServer;
        } else { 
            return false;
        }
    }
    
    public void assignService_idrepo(
        String token,
        String type,
        String name,
        String serviceName,
        String stype,
        Map attrMap,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        SchemaType schemaType = new SchemaType(stype);
        idServices.assignService(ssoToken, idtype, name, serviceName,
            schemaType, attrMap, amOrgName, amsdkDN);
        
    }
    
    public String create_idrepo(
        String token,
        String type,
        String name,
        Map attrMap,
        String amOrgName
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        return IdUtils.getUniversalId(idServices.create(ssoToken, idtype, name,
            attrMap, amOrgName));
    }
    
    public void delete_idrepo(
        String token,
        String type,
        String name,
        String orgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        idServices.delete(ssoToken, idtype, name, orgName, amsdkDN);
        
    }
    
    public Set getAssignedServices_idrepo(
        String token,
        String type,
        String name,
        Map mapOfServiceNamesAndOCs,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException,
        SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        return idServices.getAssignedServices(ssoToken, idtype, name,
            mapOfServiceNamesAndOCs, amOrgName, amsdkDN);
    }
    
    public Map getAttributes1_idrepo(
        String token,
        String type,
        String name,
        Set attrNames,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        Map res = idServices.getAttributes(ssoToken, idtype, name, attrNames,
            amOrgName, amsdkDN, true);
        if (res != null && res instanceof CaseInsensitiveHashMap) {
            Map res2 = new HashMap();
            Iterator it = res.keySet().iterator();
            while (it.hasNext()) {
                Object attr = it.next();
                Set set = (Set)res.get(attr);
                set = XMLUtils.encodeAttributeSet(set, idRepoDebug);
                res2.put(attr, set);
            }
            res = res2;
        }
        return res;
    }
    
    public Map getAttributes2_idrepo(
        String token,
        String type,
        String name,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        Map res = idServices.getAttributes(ssoToken, idtype, name, amOrgName,
            amsdkDN);
        
        if (res != null && res instanceof CaseInsensitiveHashMap) {
            Map res2 = new HashMap();
            Iterator it = res.keySet().iterator();
            while (it.hasNext()) {
                Object attr = it.next();
                Set set = (Set)res.get(attr);
                set = XMLUtils.encodeAttributeSet(set, idRepoDebug);
                res2.put(attr, set);
            }
            res = res2;
        }
        return res;
    }
    
    public Set getMembers_idrepo(
        String token,
        String type,
        String name,
        String amOrgName,
        String membersType,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        Set results = new HashSet();
        IdType idtype = IdUtils.getType(type);
        IdType mtype = IdUtils.getType(membersType);
        Set idSet = idServices.getMembers(ssoToken, idtype, name, amOrgName,
            mtype, amsdkDN);
        if (idSet != null) {
            Iterator it = idSet.iterator();
            while (it.hasNext()) {
                AMIdentity id = (AMIdentity) it.next();
                results.add(IdUtils.getUniversalId(id));
            }
        }
        return results;
    }
    
    public Set getMemberships_idrepo(
        String token,
        String type,
        String name,
        String membershipType,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        Set results = new HashSet();
        IdType idtype = IdUtils.getType(type);
        IdType mtype = IdUtils.getType(membershipType);
        Set idSet = idServices.getMemberships(ssoToken, idtype, name, mtype,
            amOrgName, amsdkDN);
        if (idSet != null) {
            Iterator it = idSet.iterator();
            while (it.hasNext()) {
                AMIdentity id = (AMIdentity) it.next();
                results.add(IdUtils.getUniversalId(id));
            }
        }
        return results;
    }
    
    public Map getServiceAttributes_idrepo(
        String token,
        String type,
        String name,
        String serviceName,
        Set attrNames,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        return idServices.getServiceAttributes(ssoToken, idtype, name,
            serviceName, attrNames, amOrgName, amsdkDN);
    }

    public Map getBinaryServiceAttributes_idrepo(
        String token, String type,
        String name, 
        String serviceName, 
        Set attrNames, 
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken stoken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        return idServices.getBinaryServiceAttributes(stoken, idtype, name,
                serviceName, attrNames, amOrgName, amsdkDN);
    }

    public Map getServiceAttributesAscending_idrepo(
        String token,
        String type,
        String name,
        String serviceName,
        Set attrNames,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        return idServices.getServiceAttributesAscending(ssoToken, idtype,
            name, serviceName, attrNames, amOrgName, amsdkDN);
    }
    
    public Set getSupportedOperations_idrepo(
        String token,
        String type,
        String amOrgName
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        Set opSet = idServices
            .getSupportedOperations(ssoToken, idtype, amOrgName);
        Set resSet = new HashSet();
        if (opSet != null) {
            Iterator it = opSet.iterator();
            while (it.hasNext()) {
                IdOperation thisop = (IdOperation) it.next();
                String opStr = thisop.getName();
                resSet.add(opStr);
            }
        }
        return resSet;
    }
    
    public Set getSupportedTypes_idrepo(String token, String amOrgName)
        throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        Set typeSet = idServices.getSupportedTypes(ssoToken, amOrgName);
        Set resTypes = new HashSet();
        if (typeSet != null) {
            Iterator it = typeSet.iterator();
            while (it.hasNext()) {
                IdType thistype = (IdType) it.next();
                String typeStr = thistype.getName();
                resTypes.add(typeStr);
            }
        }
        return resTypes;
    }

    public Set getFullyQualifiedNames_idrepo(String token, String type,
        String name, String amOrgName)
        throws RemoteException, IdRepoException, SSOException {
        SSOToken stoken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        Set opSet = idServices.getFullyQualifiedNames(stoken, idtype,
                name, amOrgName);
        Set resSet = null;
        if (opSet != null) {
            // Convert CaseInsensitiveHashSet to HashSet
            resSet = new HashSet();
            Iterator it = opSet.iterator();
            while (it.hasNext()) {
                String opStr = (String) it.next();
                resSet.add(opStr);
            }
        }
        return resSet;
    }

    public boolean isExists_idrepo(
        String token,
        String type,
        String name,
        String amOrgName
    ) throws RemoteException, SSOException, IdRepoException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        return idServices.isExists(ssoToken, idtype, name, amOrgName);
        
    }
    
    public boolean isActive_idrepo(
        String token,
        String type,
        String name,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        return idServices.isActive(ssoToken, idtype, name, amOrgName, amsdkDN);
    }

    public void setActiveStatus_idrepo(
        String token,
        String type,
        String name,
        String amOrgName,
        String amsdkDN,
        boolean active
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        idServices.setActiveStatus(
            ssoToken, idtype, name, amOrgName, amsdkDN, active);

    }

    public void modifyMemberShip_idrepo(
        String token,
        String type,
        String name,
        Set members,
        String membersType,
        int operation,
        String amOrgName
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        IdType mtype = IdUtils.getType(membersType);
        idServices.modifyMemberShip(ssoToken, idtype, name, members, mtype,
            operation, amOrgName);
    }
    
    public void modifyService_idrepo(
        String token,
        String type,
        String name,
        String serviceName,
        String stype,
        Map attrMap,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        SchemaType schematype = new SchemaType(stype);
        idServices.modifyService(ssoToken, idtype, name, serviceName,
            schematype, attrMap, amOrgName, amsdkDN);
    }
    
    public void removeAttributes_idrepo(
        String token,
        String type,
        String name,
        Set attrNames,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        idServices.removeAttributes(ssoToken, idtype, name, attrNames,
            amOrgName, amsdkDN);
    }
    
    public Map search1_idrepo(
        String token,
        String type,
        String pattern,
        Map avPairs,
        boolean recursive,
        int maxResults,
        int maxTime,
        Set returnAttrs,
        String amOrgName
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        return search2_idrepo(token, type, pattern, maxTime, maxResults,
            returnAttrs, (returnAttrs == null), 0, avPairs, recursive,
            amOrgName);
    }
    
    public Map search2_idrepo(
        String token,
        String type,
        String pattern,
        int maxTime,
        int maxResults,
        Set returnAttrs,
        boolean returnAllAttrs,
        int filterOp,
        Map avPairs,
        boolean recursive,
        String amOrgName
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        IdSearchControl ctrl = new IdSearchControl();
        ctrl.setAllReturnAttributes(returnAllAttrs);
        ctrl.setMaxResults(maxResults);
        ctrl.setReturnAttributes(returnAttrs);
        ctrl.setTimeOut(maxTime);
        IdSearchOpModifier modifier = (filterOp == IdRepo.OR_MOD) ?
            IdSearchOpModifier.OR : IdSearchOpModifier.AND;
        ctrl.setSearchModifiers(modifier, avPairs);
        IdSearchResults idres = idServices.search(ssoToken, idtype, pattern,
            ctrl, amOrgName);
        return IdSearchResultsToMap(idres);
    }
    
    public void setAttributes_idrepo(
        String token,
        String type,
        String name,
        Map attributes,
        boolean isAdd,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        idServices.setAttributes(ssoToken, idtype, name, attributes, isAdd,
            amOrgName, amsdkDN, true);
    }
    
    public void setAttributes2_idrepo(
        String token,
        String type,
        String name,
        Map attributes,
        boolean isAdd,
        String amOrgName,
        String amsdkDN,
        boolean isString
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        idServices.setAttributes(ssoToken, idtype, name, attributes, isAdd,
            amOrgName, amsdkDN, isString);
    }
    
    public void changePassword_idrepo(
        String token,
        String type,
        String name,
        String oldPassword,
        String newPassword,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        idServices.changePassword(ssoToken, idtype, name, oldPassword,
            newPassword, amOrgName, amsdkDN);
    }

    public void unassignService_idrepo(
        String token,
        String type,
        String name,
        String serviceName,
        Map attrMap,
        String amOrgName,
        String amsdkDN
    ) throws RemoteException, IdRepoException, SSOException {
        SSOToken ssoToken = getSSOToken(token);
        IdType idtype = IdUtils.getType(type);
        idServices.unassignService(ssoToken, idtype, name, serviceName,
            attrMap, amOrgName, amsdkDN);
        
    }
    
    public void deRegisterNotificationURL_idrepo(
        String notificationID)
    throws RemoteException {
        synchronized (idRepoNotificationURLs) {
            idRepoNotificationURLs.remove(notificationID);
        }
    }
    
    public Set objectsChanged_idrepo(int time) throws RemoteException {
        Set answer = new HashSet();
        // Get the cache index for times upto time+2
        initialize_cache();
        long cacheIndex = System.currentTimeMillis() / 60000;
        for (int i = 0; i < time + 3; i++) {
            Set modDNs = (Set)idrepoCache.get(Long.toString(cacheIndex));
            synchronized(this){
           	 if (modDNs != null)
                	answer.addAll(modDNs);
            }
            cacheIndex--;
        }
        if (idRepoDebug.messageEnabled()) {
            idRepoDebug.message("IdRepoJAXRPCObjectImpl.objectsChanged " +
                "in time: " + time + " minutes:\n" + answer);
        }
        return (answer);
    }
    
    public String registerNotificationURL_idrepo(String url)
    throws RemoteException {
        // TODO Auto-generated method stub
        String id = SMSUtils.getUniqueID();
        try {
            // Check URL is not the local server
            if (!isClientOnSameServer(url)) {
                synchronized (idRepoNotificationURLs) {
                    idRepoNotificationURLs.put(id, new URL(url));
                }
                if (idRepoDebug.messageEnabled()) {
                    idRepoDebug.message("IdRepoJAXRPCObjectImpl." 
                            + "registerNotificationURL_idrepo() - register " 
                            + "for notification URL: " + url);
                }
            } else {
                // Cannot add this server for notifications
                if (idRepoDebug.warningEnabled()) {
                    idRepoDebug.warning("IdRepoJAXRPCObjectImpl.registerURL "
                        + "cannot add local server: " + url);
                }
                // Set the id to be "0'
                id = "0";
            }
        } catch (MalformedURLException e) {
            if (idRepoDebug.warningEnabled()) {
                idRepoDebug.warning("IdRepoJAXRPCObjectImpl." +
                    "registerNotificationURL invalid URL: " + url, e);
            }
        }
        return (id);
    }

     public Map getSpecialIdentities_idrepo(
         String token,
         String type,
         String amOrgName
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         IdSearchResults result = idServices.getSpecialIdentities(
             ssoToken, idtype, amOrgName);
         return IdSearchResultsToMap(result);
     }

    
    // Implementation to process entry changed events
    protected static void processEntryChanged_idrepo(
        String method, String name, int type, Set attrNames) {
        if (idRepoDebug.messageEnabled()) {
            idRepoDebug.message("IdRepoJAXRPCObjectImpl.processEntryChaged "
                + "method processing method: " + method + " name: " + name +
                " type: " + type + " attrName: " + attrNames);
        }
        initialize_cache();
        // Return if cache size is 0 or there are no remote clients
        if ((cacheSize == 0) && idRepoNotificationURLs.isEmpty()) {
            if (idRepoDebug.messageEnabled()) {
                idRepoDebug.message("IdRepoJAXRPCObjectImpl." +
                    "processEntryChaged No registered notification URLs: " +
                    idRepoNotificationURLs + " and cache size is: " +
                    cacheSize);
            }
            return;
        }
        
         // Construct the XML document for the event change
        StringBuffer sb = new StringBuffer(100);
        sb.append("<EventNotification><AttributeValuePair>")
            .append("<Attribute name=\"method\" /><Value>").append(method)
            .append("</Value></AttributeValuePair>")
            .append("<AttributeValuePair><Attribute name=\"entityName\" />")
            .append("<Value>").append(XMLUtils.escapeSpecialCharacters(name))
            .append("</Value></AttributeValuePair>");
            
        if (method.equalsIgnoreCase("objectChanged") ||
            method.equalsIgnoreCase("objectsChanged")) {
            sb.append("<AttributeValuePair><Attribute name=\"eventType\" />")
                .append("<Value>").append(type).append(
                "</Value></AttributeValuePair>");
            if (method.equalsIgnoreCase("objectsChanged")) {
                sb.append("<AttributeValuePair><Attribute ").append(
                    "name=\"attrNames\"/>");
                for (Iterator items = attrNames.iterator(); items.hasNext();) {
                    String attr = (String) items.next();
                    sb.append("<Value>").append(attr).append("</Value>");
                }
                sb.append("</AttributeValuePair>");
            }
        }
        sb.append("</EventNotification>");
        
        // Update cache for polling by remote clients
        if (cacheSize > 0) {
            // Obtain the cache index
            long currentTime = System.currentTimeMillis() / 60000;
            String cacheIndex = Long.toString(currentTime);
            Set modDNs = (Set)idrepoCache.get(cacheIndex);
            if (modDNs == null) {
                modDNs = new HashSet();
                idrepoCache.put(cacheIndex, modDNs);
                // Maintain cacheIndex
                idrepoCacheIndices.addFirst(cacheIndex);
                cleanupCache(idrepoCacheIndices, idrepoCache, currentTime);
            }

            
            // Add to cache
            modDNs.add(sb.toString());
            if (idRepoDebug.messageEnabled()) {
                idRepoDebug.message("IdRepoJAXRPCObjectImpl.processing " +
                    "entry change:" + sb.toString());
            }
        }
        
        // If notification URLs are present, send notifications
        if (idRepoDebug.messageEnabled()) {
            idRepoDebug.message("IdRepoJAXRPCObjectImpl.processEntryChaged =" +
                " notificationURLS " + idRepoNotificationURLs.values());
        }
        NotificationSet ns = null;
        synchronized (idRepoNotificationURLs) {
            for (Iterator entries = idRepoNotificationURLs.entrySet().iterator(); 
                entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();
                String id = (String) entry.getKey();
                URL url = (URL) entry.getValue();
            
                // Construct NotificationSet
                if (ns == null) {
                    Notification notification = 
                        new Notification(sb.toString());
                    ns = new NotificationSet(IDREPO_SERVICE);
                    ns.addNotification(notification);
                }
                try {
                    PLLServer.send(url, ns);
                    if (idRepoDebug.messageEnabled()) {
                        idRepoDebug.message("IdRepoJAXRPCObjectImpl:" +
                            "sentNotification URL: " + url + " Data: " + ns);
                    }
                } catch (SendNotificationException ne) {
                    if (idRepoDebug.warningEnabled()) {
                        idRepoDebug.warning("IdRepoJAXRPCObjectImpl: failed "
                            + "sending notification to: " + url + "\nRemoving "
                            + "URL from notification list.", ne);
                    }
                    // Remove the URL from Notification List
                    entries.remove();
                }
            }
        }
    }
    
    protected static void cleanupCache(LinkedList cIndices, HashMap thisCache,
            long currentTime) {

        // remove the last cache entries
        if (cIndices.size() > cacheSize) {
            String removedIndex = (String)cIndices.removeLast();
            thisCache.remove(removedIndex);
            if (idRepoDebug.messageEnabled()) {
                idRepoDebug.message("IdRepoJAXRPCObjectImpl:cleanupCache last "
                        + removedIndex);
            }
        }

        // remove expired cache entries
        long lastIndex = Long.parseLong((String)cIndices.getLast());
        while ((currentTime - cacheSize) > lastIndex) {
            String removedIndex = (String)cIndices.removeLast();
            thisCache.remove(removedIndex);
            if (idRepoDebug.messageEnabled()) {
                idRepoDebug.message("IdRepoJAXRPCObjectImpl:cleanupCache expired "
                        + removedIndex);
            }
            lastIndex = Long.parseLong((String)cIndices.getLast());
        }
    }
 
    private Map IdSearchResultsToMap(IdSearchResults res) {
        // TODO ..check if the Map gets properly populated and sent.
        Map answer = new HashMap();
        Map attrMaps = new HashMap();
        Set idStrings = new HashSet();

        Map answer1 = res.getResultAttributes();
        Set ids = res.getSearchResults();
        if (ids != null) {
            Iterator it = ids.iterator();
            while (it.hasNext()) {
                AMIdentity id = (AMIdentity) it.next();
                String idStr = IdUtils.getUniversalId(id);
                idStrings.add(idStr);
                Map attrMap = (Map) answer1.get(id);
                if (attrMap != null) {
                    Map cattrMap = new HashMap();
                    for (Iterator items = attrMap.keySet().iterator();
                        items.hasNext();) {
                        Object item = items.next();
                        Set values = (Set)attrMap.get(item);
                        values = XMLUtils.encodeAttributeSet(values,
                            idRepoDebug);
                        cattrMap.put(item.toString(), values);
                    }
                    attrMaps.put(idStr, cattrMap);
                }
            }
        }
        answer.put(IdRemoteServicesImpl.AMSR_RESULTS, idStrings);
        answer.put(IdRemoteServicesImpl.AMSR_CODE,
            new Integer(res.getErrorCode()));
        answer.put(IdRemoteServicesImpl.AMSR_ATTRS, attrMaps);
        return (answer);
    }

    /**
     * Check if agent token ID is appended to the token string.
     * if yes, we use it as a restriction context. This is meant
     * for cookie hijacking feature where agent appends the agent token ID
     * to the user sso token before sending it over to the server for
     * validation.
     */
    protected SSOToken getSSOToken(String token) throws SSOException {
        // Initalize the class variables
        initialize_idrepo();
        
        int index = token.indexOf(" ");
        if (tokenManager == null) {
            tokenManager = SSOTokenManager.getInstance();
        }
        if (index == -1) {
            return tokenManager.createSSOToken(token);
        }

        SSOToken stoken = null;
        String agentTokenStr = token.substring(index +1);
        String tokenStr = token.substring(0,index);
        final String ftoken = tokenStr;
        
        try {
            /*
             * for 7.0 patch-4 agent, IP address maybe send back to server.
             * this is a very simple check for IP Address
             */
            Object context = null;
            if (agentTokenStr.indexOf('.') != -1) {
                try {
                    context = InetAddress.getByName(agentTokenStr);
                } catch (Exception e) {
                    context = tokenManager.createSSOToken(agentTokenStr);
                }
            } else {
                context = tokenManager.createSSOToken(agentTokenStr);
            } 
            stoken = (SSOToken)RestrictedTokenContext.doUsing(context,
                new RestrictedTokenAction() {
                    public Object run() throws Exception {
                        return tokenManager.createSSOToken(ftoken);
                    }
            });
       } catch (SSOException e) {
           idRepoDebug.error("IdRepoJAXRPCObjectImpl:getSSOToken", e);
           return tokenManager.createSSOToken(tokenStr);
       } catch (Exception e) {
           idRepoDebug.error("IdRepoJAXRPCObjectImpl:getSSOToken", e);
       }
       return stoken;
   }
}
