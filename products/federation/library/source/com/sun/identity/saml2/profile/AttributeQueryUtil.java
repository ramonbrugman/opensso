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
 * $Id: AttributeQueryUtil.java,v 1.11 2009-07-24 22:51:48 madan_ranganath Exp $
 *
 */


package com.sun.identity.saml2.profile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Element;

import com.sun.identity.plugin.datastore.DataStoreProviderException;
import com.sun.identity.plugin.datastore.DataStoreProvider;
import com.sun.identity.plugin.session.SessionException;
import com.sun.identity.plugin.session.SessionManager;
import com.sun.identity.saml.xmlsig.KeyProvider;
import com.sun.identity.saml2.assertion.Assertion;
import com.sun.identity.saml2.assertion.AssertionFactory;
import com.sun.identity.saml2.assertion.Attribute;
import com.sun.identity.saml2.assertion.AttributeStatement;
import com.sun.identity.saml2.assertion.Conditions;
import com.sun.identity.saml2.assertion.EncryptedAssertion;
import com.sun.identity.saml2.assertion.Issuer;
import com.sun.identity.saml2.assertion.NameID;
import com.sun.identity.saml2.assertion.EncryptedID;
import com.sun.identity.saml2.assertion.Subject;
import com.sun.identity.saml2.common.SAML2Constants;
import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.common.SAML2Utils;
import com.sun.identity.saml2.jaxb.assertion.AttributeElement;
import com.sun.identity.saml2.jaxb.assertion.AttributeValueElement;
import com.sun.identity.saml2.jaxb.entityconfig.AttributeAuthorityConfigElement;
import com.sun.identity.saml2.jaxb.entityconfig.AttributeQueryConfigElement;
import com.sun.identity.saml2.jaxb.metadata.AttributeAuthorityDescriptorElement;
import com.sun.identity.saml2.jaxb.metadata.AttributeServiceElement;
import com.sun.identity.saml2.jaxb.metadataextquery.AttributeQueryDescriptorElement;
import com.sun.identity.saml2.key.EncInfo;
import com.sun.identity.saml2.key.KeyUtil;
import com.sun.identity.saml2.meta.SAML2MetaException;
import com.sun.identity.saml2.meta.SAML2MetaManager;
import com.sun.identity.saml2.meta.SAML2MetaUtils;
import com.sun.identity.saml2.plugins.AttributeAuthorityMapper;
import com.sun.identity.saml2.protocol.AttributeQuery;
import com.sun.identity.saml2.protocol.ProtocolFactory;
import com.sun.identity.saml2.protocol.Response;
import com.sun.identity.saml2.protocol.Status;
import com.sun.identity.saml2.protocol.StatusCode;
import com.sun.identity.saml2.xmlenc.EncManager;


/**
 * This class provides methods to send or processs <code>AttributeQuery</code>.
 *
 * @supported.api
 */

public class AttributeQueryUtil {

    static KeyProvider keyProvider = KeyUtil.getKeyProviderInstance(); 
    static Hashtable attrAuthorityMapperCache = new Hashtable(); 
    static DataStoreProvider dsProvider = null;
    static SAML2MetaManager metaManager = SAML2Utils.getSAML2MetaManager();

    static {
        try {
            dsProvider = SAML2Utils.getDataStoreProvider(); 
        } catch (SAML2Exception se) {
            SAML2Utils.debug.error("AttributeQueryUtil.static:", se);
        }
    }

    private AttributeQueryUtil() {
    }

    /**
     * Sends the <code>AttributeQuery</code> to specifiied
     * attribute authority and returns <code>Response</code> coming
     * from the attribute authority.
     *
     * @param attrQuery the <code>AttributeQuery</code> object
     * @param attrAuthorityEntityID entity ID of attribute authority
     * @param realm the realm of hosted entity
     * @param attrQueryProfile the attribute query profile or null to ignore
     * @param attrProfile the attribute profile
     * @param binding the binding
     *
     * @return the <code>Response</code> object
     * @exception SAML2Exception if the operation is not successful
     *
     * @supported.api
     */
    public static Response sendAttributeQuery(AttributeQuery attrQuery,
        String attrAuthorityEntityID, String realm, String attrQueryProfile,
        String attrProfile, String binding) throws SAML2Exception {

        AttributeAuthorityDescriptorElement aad = null;
        try {
             aad = metaManager.getAttributeAuthorityDescriptor(
                realm, attrAuthorityEntityID);
        } catch (SAML2MetaException sme) {
            SAML2Utils.debug.error("AttributeQueryUtil.sendAttributeQuery:",
                sme);
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("metaDataError"));
        }

        if (aad == null) {
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("attrAuthorityNotFound"));
        }

        if (binding == null) {
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("unsupportedBinding"));
        }

        String location = findLocation(aad, binding, attrQueryProfile,
             attrProfile);

        if (location == null) {
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("attrAuthorityNotFound"));
        }

        if (binding.equalsIgnoreCase(SAML2Constants.SOAP)) {
            signAttributeQuery(attrQuery, realm, false);
            return sendAttributeQuerySOAP(attrQuery, location,
                attrAuthorityEntityID, aad);
        } else {
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("unsupportedBinding"));
        }
    }

    /**
     * Rrocesses the <code>AttributeQuery</code> coming
     * from a requester.
     *
     * @param attrQuery the <code>AttributeQuery</code> object
     * @param request the <code>HttpServletRequest</code> object
     * @param response the <code>HttpServletResponse</code> object
     * @param attrAuthorityEntityID entity ID of attribute authority
     * @param realm the realm of hosted entity
     * @param attrQueryProfileAlias the attribute query profile alias
     *
     * @return the <code>Response</code> object
     * @exception SAML2Exception if the operation is not successful
     */
    public static Response processAttributeQuery(AttributeQuery attrQuery,
        HttpServletRequest request, HttpServletResponse response,
        String attrAuthorityEntityID, String realm,
        String attrQueryProfileAlias) throws SAML2Exception {

        AttributeAuthorityMapper attrAuthorityMapper = 
            getAttributeAuthorityMapper(realm, attrAuthorityEntityID,
            attrQueryProfileAlias);

        String attrQueryProfile = AttributeQueryUtil.getAttributeQueryProfile(
            attrQueryProfileAlias);

        try {
            attrAuthorityMapper.authenticateRequester(request, response,
                attrQuery, attrAuthorityEntityID, realm);
        } catch(SAML2Exception se) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message("AttributeQueryUtil." +
                "processAttributeQuery: ", se);
            }
            return SAML2Utils.getErrorResponse(attrQuery,
                SAML2Constants.REQUESTER, null, se.getMessage(), null);
        }

        try {
            attrAuthorityMapper.validateAttributeQuery(request, response,
                attrQuery, attrAuthorityEntityID, realm);
        } catch(SAML2Exception se) {
            SAML2Utils.debug.error("AttributeQueryUtil.processAttributeQuery:",
                se);
            return SAML2Utils.getErrorResponse(attrQuery,
                SAML2Constants.REQUESTER, null, se.getMessage(), null);
        }

        Issuer issuer = attrQuery.getIssuer();
        String requesterEntityID = issuer.getValue();        
        AttributeAuthorityDescriptorElement aad = null;
        try {
             aad = metaManager.getAttributeAuthorityDescriptor(
                realm, attrAuthorityEntityID);
        } catch (SAML2MetaException sme) {
            SAML2Utils.debug.error("AttributeQueryUtil.processAttributeQuery:",
                sme);
            return SAML2Utils.getErrorResponse(attrQuery,
                SAML2Constants.RESPONDER, null,
                SAML2Utils.bundle.getString("metaDataError"), null);
        } 

        if (aad == null) {
            return SAML2Utils.getErrorResponse(attrQuery,
                SAML2Constants.REQUESTER, null,
                SAML2Utils.bundle.getString("attrAuthorityNotFound"), null);
        }

        Object identity = null;
        try {
            identity = attrAuthorityMapper.getIdentity(request, response,
                attrQuery, attrAuthorityEntityID, realm);
        } catch (SAML2Exception se) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message("AttributeQueryUtil." +
                "processAttributeQuery: ", se);
            }
            return SAML2Utils.getErrorResponse(attrQuery,
                SAML2Constants.REQUESTER, SAML2Constants.UNKNOWN_PRINCIPAL,
                se.getMessage(), null);
        }

        if (identity == null) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message("AttributeQueryUtil." +
                "processAttributeQuery: unable to find identity.");
            }
            return SAML2Utils.getErrorResponse(attrQuery,
                SAML2Constants.REQUESTER, SAML2Constants.UNKNOWN_PRINCIPAL,
                null, null);
        }

        

        List desiredAttrs = attrQuery.getAttributes();
        try {
            desiredAttrs = verifyDesiredAttributes(aad.getAttribute(),
                desiredAttrs);
        } catch (SAML2Exception se) {
            return SAML2Utils.getErrorResponse(attrQuery,
                SAML2Constants.REQUESTER, 
                SAML2Constants.INVALID_ATTR_NAME_OR_VALUE, null, null);
        }

        List attributes = attrAuthorityMapper.getAttributes(identity,
            attrQuery, attrAuthorityEntityID, realm);

        attributes = filterAttributes(attributes, desiredAttrs);

        ProtocolFactory protocolFactory = ProtocolFactory.getInstance();
        Response samlResp = protocolFactory.createResponse();
        List assertionList = new ArrayList();

        Assertion assertion = null;
        try {
            assertion = getAssertion(attrQuery, attrAuthorityEntityID,
                requesterEntityID, realm, attrQueryProfileAlias, attributes);
        } catch (SAML2Exception se) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message(
                    "AttributeQueryUtil.processAttributeQuery:", se);
            }
            return SAML2Utils.getErrorResponse(attrQuery,
                SAML2Constants.RESPONDER, null, se.getMessage(), null);
        }

        EncryptedID encryptedID = attrQuery.getSubject().getEncryptedID();
        if (encryptedID != null) {
            EncryptedAssertion encryptedAssertion = null;
            try {
                signAssertion(assertion, realm, attrAuthorityEntityID, false);
                encryptedAssertion = encryptAssertion(assertion,
                        encryptedID, attrAuthorityEntityID, requesterEntityID,
                        realm, attrQueryProfileAlias);
            } catch (SAML2Exception se) {
                if (SAML2Utils.debug.messageEnabled()) {
                        SAML2Utils.debug.message(
                            "AttributeQueryUtil.processAttributeQuery:", se);
                }
                return SAML2Utils.getErrorResponse(attrQuery,
                        SAML2Constants.RESPONDER, null, se.getMessage(), null);
            }
            assertionList.add(encryptedAssertion);        
            samlResp.setEncryptedAssertion(assertionList);
        } else {
            assertionList.add(assertion);        
            samlResp.setAssertion(assertionList);
        }

        samlResp.setID(SAML2Utils.generateID());
        samlResp.setInResponseTo(attrQuery.getID());

        samlResp.setVersion(SAML2Constants.VERSION_2_0);
        samlResp.setIssueInstant(new Date());
    
        Status status = protocolFactory.createStatus();
        StatusCode statusCode = protocolFactory.createStatusCode();
        statusCode.setValue(SAML2Constants.SUCCESS);
        status.setStatusCode(statusCode);
        samlResp.setStatus(status);

        Issuer respIssuer = AssertionFactory.getInstance().createIssuer();
        respIssuer.setValue(attrAuthorityEntityID);
        samlResp.setIssuer(respIssuer);

        signResponse(samlResp, attrAuthorityEntityID, realm, false);

        return samlResp;
    }

    /**
     * Converts attribute query profile alias to attribute query profile.
     *
     * @param attrQueryProfileAlias attribute query profile alias
     *
     * @return attribute query profile
     */
    public static String getAttributeQueryProfile(
        String attrQueryProfileAlias) {

        if (attrQueryProfileAlias == null) {
            return null;
        } else if (attrQueryProfileAlias.equals(
            SAML2Constants.DEFAULT_ATTR_QUERY_PROFILE_ALIAS)) {
            return  SAML2Constants.DEFAULT_ATTR_QUERY_PROFILE;
        } else if (attrQueryProfileAlias.equals(
            SAML2Constants.X509_SUBJECT_ATTR_QUERY_PROFILE_ALIAS)) {
            return  SAML2Constants.X509_SUBJECT_ATTR_QUERY_PROFILE;
        }

        return null;
    }

    private static void signAttributeQuery(AttributeQuery attrQuery,
        String realm, boolean includeCert) throws SAML2Exception {
        String requesterEntityID = attrQuery.getIssuer().getValue();
        
        String alias = SAML2Utils.getSigningCertAlias(realm, requesterEntityID,
            SAML2Constants.ATTR_QUERY_ROLE);

        PrivateKey signingKey = keyProvider.getPrivateKey(alias);
        if (signingKey == null) {
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("missingSigningCertAlias"));
        }

        X509Certificate signingCert = null;
        if (includeCert) {
            signingCert = keyProvider.getX509Certificate(alias);
        }
        
        if (signingKey != null) {
            attrQuery.sign(signingKey, signingCert);
        }
    }

    public static void validateEntityRequester(AttributeQuery attrQuery,
        String attrAuthorityEntityID, String realm) throws SAML2Exception {

        Issuer issuer = attrQuery.getIssuer();
        String format = issuer.getFormat();
        if ((format == null) || (format.length() == 0) ||
            (format.equals(SAML2Constants.UNSPECIFIED)) ||
            (format.equals(SAML2Constants.ENTITY))) {

            String requestedEntityID = issuer.getValue();

            if (!SAML2Utils.isSourceSiteValid(issuer, realm,
                attrAuthorityEntityID)) {
                throw new SAML2Exception(SAML2Utils.bundle.getString(
                    "attrQueryIssuerInvalid"));
            }
        } else {
            throw new SAML2Exception(SAML2Utils.bundle.getString(
                "attrQueryIssuerInvalid"));
        }
    }

    /**
     * Checks if the attribute query signature is valid.
     *
     * @param attrQuery attribute query
     * @param attrAuthorityEntityID entity ID of attribute authority
     * @param realm the realm of hosted entity
     *
     * @exception SAML2Exception if the attribute query signature is not valid.
     */
    public static void verifyAttrQuerySignature(AttributeQuery attrQuery,
        String attrAuthorityEntityID, String realm)
        throws SAML2Exception {

        if (!attrQuery.isSigned()) {
            throw new SAML2Exception(SAML2Utils.bundle.getString(
                "attrQueryNotSigned"));
        }

        String requestedEntityID = attrQuery.getIssuer().getValue();

        AttributeQueryDescriptorElement attrqDesc =
            metaManager.getAttributeQueryDescriptor(realm, requestedEntityID);
        if (attrqDesc == null) {
            throw new SAML2Exception(SAML2Utils.bundle.getString(
                "attrQueryIssuerNotFound"));
        }
        X509Certificate signingCert =
            KeyUtil.getVerificationCert(attrqDesc, requestedEntityID,
            SAML2Constants.ATTR_QUERY_ROLE);

        if (signingCert != null) {
            boolean valid = attrQuery.isSignatureValid(signingCert);
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message(
                    "AttributeQueryUtil.verifyAttributeQuery: " +
                    "Signature validity is : " + valid);
            }
            if (!valid) {
                throw new SAML2Exception(SAML2Utils.bundle.getString(
                    "invalidSignatureAttrQuery"));
            }
        } else {
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("missingSigningCertAlias"));
        }
    }

    public static String getIdentityFromDataStoreX509Subject(
        AttributeQuery attrQuery, String attrAuthorityEntityID, String realm)
        throws SAML2Exception {

        Subject subject = attrQuery.getSubject();
        NameID nameID = null;
        EncryptedID encryptedID = subject.getEncryptedID();

        if (encryptedID != null) {
            String alias = SAML2Utils.getEncryptionCertAlias(realm, 
                attrAuthorityEntityID, SAML2Constants.ATTR_AUTH_ROLE); 
            PrivateKey privateKey = keyProvider.getPrivateKey(alias);
            
            nameID = encryptedID.decrypt(privateKey);
        } else {
            nameID = subject.getNameID();
        }

        if (!SAML2Constants.X509_SUBJECT_NAME.equals(nameID.getFormat())) {
            throw new SAML2Exception(SAML2Utils.bundle.getString(
                "unsupportedAttrQuerySubjectNameID"));
        }

        String mappingAttrName = getAttributeValueFromAttrAuthorityConfig(
            realm, attrAuthorityEntityID,
            SAML2Constants.X509_SUBJECT_DATA_STORE_ATTR_NAME);

        if ((mappingAttrName == null) || (mappingAttrName.length() == 0)) {
            throw new SAML2Exception(SAML2Utils.bundle.getString(
                "x509SubjectMappingNotConfigured"));
        }

        String x509SubjectDN = nameID.getValue();
        Map attrMap = new HashMap();
        Set values = new HashSet();
        values.add(x509SubjectDN);
        attrMap.put(mappingAttrName, values);

        if (SAML2Utils.debug.messageEnabled()) {
            SAML2Utils.debug.message(
                "AttributeQueryUtil.getIdentityFromDataStoreX509Subject: " +
                "mappingAttrName = " + mappingAttrName +
                ", X509 subject DN = " + x509SubjectDN);
        }

        try {
            return dsProvider.getUserID(realm, attrMap);
        } catch (DataStoreProviderException dse) {
            SAML2Utils.debug.error(
                "AttributeQueryUtil.getIdentityFromDataStoreX509Subject:",dse);
            throw new SAML2Exception(dse.getMessage());
        }
    }

    public static String getIdentity(AttributeQuery attrQuery,
        String attrAuthorityEntityID, String realm) throws SAML2Exception {

        Subject subject = attrQuery.getSubject();
        NameID nameID = null;
        EncryptedID encryptedID = subject.getEncryptedID();

        if (encryptedID != null) {
            String alias = SAML2Utils.getEncryptionCertAlias(realm, 
                attrAuthorityEntityID, SAML2Constants.ATTR_AUTH_ROLE); 
            PrivateKey privateKey = keyProvider.getPrivateKey(alias);
            
            nameID = encryptedID.decrypt(privateKey);
        } else {
            nameID = subject.getNameID();
        }

        String nameIDFormat = nameID.getFormat();
        if (SAML2Constants.NAMEID_TRANSIENT_FORMAT.equals(nameIDFormat)) {
            return (String)IDPCache.userIDByTransientNameIDValue.get(
                nameID.getValue());
        } else {
            String requestedEntityID = attrQuery.getIssuer().getValue();

            try {
                return dsProvider.getUserID(realm, SAML2Utils.getNameIDKeyMap(
                    nameID, attrAuthorityEntityID, requestedEntityID, realm,
                    SAML2Constants.IDP_ROLE));
            } catch (DataStoreProviderException dse) {
                SAML2Utils.debug.error(
                    "AttributeQueryUtil.getIdentityFromDataStore:", dse);
                throw new SAML2Exception(dse.getMessage());
            }
        }
    }

    public static List getUserAttributes(String userId,
        AttributeQuery attrQuery, String attrAuthorityEntityID, String realm)
        throws SAML2Exception {
 
        String requestedEntityID = attrQuery.getIssuer().getValue();

            Map configMap = SAML2Utils.getConfigAttributeMap(realm,
                requestedEntityID, SAML2Constants.SP_ROLE);
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message(
                    "AttributeQueryUtil.getUserAttributes: " +
                    "remote SP attribute map = " + configMap);
            }
            if (configMap == null || configMap.isEmpty()) {
                configMap = SAML2Utils.getConfigAttributeMap(realm,
                    attrAuthorityEntityID, SAML2Constants.IDP_ROLE);
                if (configMap == null || configMap.isEmpty()) {
                    if (SAML2Utils.debug.messageEnabled()) {
                        SAML2Utils.debug.message(
                            "AttributeQueryUtil.getUserAttributes:" +
                            "Configuration map is not defined.");
                    }
                    return null;
                }
                if (SAML2Utils.debug.messageEnabled()) {
                    SAML2Utils.debug.message(
                        "AttributeQueryUtil.getUserAttributes: " +
                        "hosted IDP attribute map=" + configMap);
                }
            }

            List attributes = new ArrayList();
            
            Set localAttributes = new HashSet();
            localAttributes.addAll(configMap.values());
            Map valueMap = null;

            try {
                valueMap = dsProvider.getAttributes(userId, localAttributes); 
            } catch (DataStoreProviderException dse) {
                if (SAML2Utils.debug.warningEnabled()) {
                    SAML2Utils.debug.warning(
                        "AttributeQueryUtil.getUserAttributes:", dse);
                }
            }

            Iterator iter = configMap.keySet().iterator();
            while(iter.hasNext()) {
                String samlAttribute = (String)iter.next();
                String localAttribute = (String)configMap.get(samlAttribute);
                String[] localAttributeValues = null;
                if ((valueMap != null) && (!valueMap.isEmpty())) {
                    Set values = (Set)valueMap.get(localAttribute); 
                    if ((values == null) || values.isEmpty()) {
                        if (SAML2Utils.debug.messageEnabled()) {
                            SAML2Utils.debug.message(
                                "AttributeQueryUtil.getUserAttributes:" +
                                " user profile does not have value for " + 
                                localAttribute);
                        }
                    } else {
                        localAttributeValues = (String[])
                            values.toArray(new String[values.size()]);
                    }
                } 

                if ((localAttributeValues == null) ||
                    (localAttributeValues.length == 0)) {
                    if (SAML2Utils.debug.messageEnabled()) {
                        SAML2Utils.debug.message(
                            "AttributeQueryUtil.getUserAttributes:" +
                            " user does not have " + localAttribute);
                    }
                    continue;
                }

                attributes.add(SAML2Utils.getSAMLAttribute(samlAttribute,
                    localAttributeValues));
            }
            return attributes;      
    }

    private static void signResponse(Response response,
        String attrAuthorityEntityID, String realm, boolean includeCert)
        throws SAML2Exception {
        
        String alias = SAML2Utils.getSigningCertAlias(realm,
            attrAuthorityEntityID, SAML2Constants.ATTR_AUTH_ROLE);

        PrivateKey signingKey = keyProvider.getPrivateKey(alias);
        if (signingKey == null) {
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("missingSigningCertAlias"));
        }

        X509Certificate signingCert = null;
        if (includeCert) {
            signingCert = keyProvider.getX509Certificate(alias);
        }
        
        if (signingKey != null) {
            response.sign(signingKey, signingCert);
        }
    }

    private static Assertion getAssertion(AttributeQuery attrQuery,
        String attrAuthorityEntityID, String requesterEntityID, String realm,
        String attrQueryProfileAlias, List attributes) throws SAML2Exception {

        AssertionFactory assertionFactory = AssertionFactory.getInstance();
        Assertion assertion = assertionFactory.createAssertion();
        assertion.setID(SAML2Utils.generateID());    
        assertion.setVersion(SAML2Constants.VERSION_2_0);
        assertion.setIssueInstant(new Date());
        Issuer issuer = assertionFactory.createIssuer();
        issuer.setValue(attrAuthorityEntityID);
        assertion.setIssuer(issuer);

        Subject subjectQ = attrQuery.getSubject();
        Subject subject = assertionFactory.createSubject();
        subject.setEncryptedID(subjectQ.getEncryptedID());
        subject.setNameID(subjectQ.getNameID());
        subject.setBaseID(subjectQ.getBaseID());
        subject.setSubjectConfirmation(subjectQ.getSubjectConfirmation());
        assertion.setSubject(subject);

        if ((attributes != null) && (!attributes.isEmpty())) {
            AttributeStatement attrStatement =
                assertionFactory.createAttributeStatement();

            attrStatement.setAttribute(attributes);
            List attrStatementList = new ArrayList();
            attrStatementList.add(attrStatement);
            assertion.setAttributeStatements(attrStatementList); 
        }

        int effectiveTime = IDPSSOUtil.getEffectiveTime(realm,
            attrAuthorityEntityID);
        int notBeforeSkewTime = IDPSSOUtil.getNotBeforeSkewTime(realm,
            attrAuthorityEntityID);
        Conditions conditions = IDPSSOUtil.getConditions(requesterEntityID, 
            notBeforeSkewTime, effectiveTime);
        assertion.setConditions(conditions);

        return assertion;
    }

    private static void signAssertion(Assertion assertion, String realm,
        String attrAuthorityEntityID, boolean includeCert)
        throws SAML2Exception {

        String alias = SAML2Utils.getSigningCertAlias(realm,
            attrAuthorityEntityID, SAML2Constants.ATTR_AUTH_ROLE);

        PrivateKey signingKey = keyProvider.getPrivateKey(alias);
        X509Certificate signingCert = null;
        if (includeCert) {
            signingCert = keyProvider.getX509Certificate(alias);
        }
        
        if (signingKey != null) {
            assertion.sign(signingKey, signingCert);
        }
    }

    private static EncryptedAssertion encryptAssertion(Assertion assertion,
        EncryptedID encryptedID, String attrAuthorityEntityID,
        String requesterEntityID, String realm, String attrQueryProfileAlias)
        throws SAML2Exception {

        String alias = SAML2Utils.getEncryptionCertAlias(realm, 
            attrAuthorityEntityID, SAML2Constants.ATTR_AUTH_ROLE); 
        PrivateKey privateKey = keyProvider.getPrivateKey(alias);

        SecretKey secretKey = EncManager.getEncInstance().getSecretKey(
            encryptedID.toXMLString(true, true), privateKey);

        AttributeQueryDescriptorElement aqd =
            metaManager.getAttributeQueryDescriptor(realm, requesterEntityID);
        EncInfo encInfo = KeyUtil.getEncInfo(aqd, requesterEntityID,
            SAML2Constants.ATTR_QUERY_ROLE);

        Element el = EncManager.getEncInstance().encrypt(
            assertion.toXMLString(true, true), encInfo.getWrappingKey(),
            secretKey, encInfo.getDataEncAlgorithm(),
            encInfo.getDataEncStrength(), requesterEntityID,
            "EncryptedAssertion");

        return AssertionFactory.getInstance().createEncryptedAssertion(el);
    }

    private static List verifyDesiredAttributes(List supportedAttrs,
        List desiredAttrs) throws SAML2Exception {

        if ((supportedAttrs == null) || (supportedAttrs.isEmpty())) {
            return desiredAttrs;
        }

        if ((desiredAttrs == null) || (desiredAttrs.isEmpty())) {
            return convertAttributes(supportedAttrs);
        }

        for(Iterator iterD = desiredAttrs.iterator(); iterD.hasNext(); ) {
            Attribute attrD = (Attribute)iterD.next();
            boolean isAttrValid = false;
            Iterator iterS = supportedAttrs.iterator();
            while(iterS.hasNext()) {
                AttributeElement attrS = (AttributeElement)iterS.next();
                if (isSameAttribute(attrD, attrS)) {
                    if (isValueValid(attrD, attrS)) {
                        isAttrValid = true;
                        break;
                    } else {
                        throw new SAML2Exception(
                            "Attribute value not suppoted");
                    }
                }
            }
            if (isAttrValid) {
                iterS.remove();
            } else {
                throw new SAML2Exception("Attribute name not suppoted");
            }
        }
        return desiredAttrs;
    }

    private static List convertAttributes(List jaxbAttrs)
        throws SAML2Exception {

        List resultAttrs = new ArrayList();
        for(Iterator iter = jaxbAttrs.iterator(); iter.hasNext(); ) {
            AttributeElement jaxbAttr = (AttributeElement)iter.next();
            Attribute attr = AssertionFactory.getInstance().createAttribute();
            attr.setName(jaxbAttr.getName());
            attr.setNameFormat(jaxbAttr.getNameFormat());
            attr.setFriendlyName(jaxbAttr.getFriendlyName());

            List jaxbValues = jaxbAttr.getAttributeValue();
            if ((jaxbValues != null) && (!jaxbValues.isEmpty())) {
                List newValues = new ArrayList();
                for(Iterator iterV = jaxbValues.iterator(); iterV.hasNext();) {
                    AttributeValueElement jaxbValeu =
                        (AttributeValueElement)iter.next();
                    List content = jaxbValeu.getContent();
                    if ((content != null) && (!content.isEmpty())) {
                        newValues.add(content.get(0));
                    }
                }
                if (!newValues.isEmpty()) {
                    attr.setAttributeValueString(newValues);
                }
            }
            resultAttrs.add(attr);
        }
        return resultAttrs;
    }

    private static List filterAttributes(List attributes, List desiredAttrs) {

        if ((attributes == null) || (attributes.isEmpty())) {
            return attributes;
        }
        if ((desiredAttrs == null) || (desiredAttrs.isEmpty())) {
            return attributes;
        }

        List returnAttributes = new ArrayList();
        if ((desiredAttrs != null) && (!desiredAttrs.isEmpty())) {
            for(Iterator iterD = desiredAttrs.iterator(); iterD.hasNext();){

                Attribute attrD = (Attribute)iterD.next();
                for(Iterator iter = attributes.iterator(); iter.hasNext();) {

                    Attribute attr = (Attribute)iter.next();
                    if (isSameAttribute(attr, attrD) ) {
                        attr = filterAttributeValues(attr, attrD);
                        if (attr != null) {
                            returnAttributes.add(attr);
                        }
                        iter.remove();
                        break;
                    }
                }
            }

        }
        return returnAttributes;

    }

    private static boolean isSameAttribute(Attribute attr1, Attribute attr2) {

        if (!attr1.getName().equals(attr2.getName())) {
            return false;
        }

        String nameFormat1 = attr1.getNameFormat();

        if (nameFormat1 == null) {
            return (attr2.getNameFormat() == null);
        } else {
            return (nameFormat1.equals(attr2.getNameFormat()));
        }

    }

    private static Attribute filterAttributeValues(Attribute attr,
        Attribute desiredAttr) {

        List valuesD = desiredAttr.getAttributeValueString();
        if ((valuesD == null) || (valuesD.isEmpty())) {
            return attr;
        }

        List values = attr.getAttributeValueString();
        if ((values == null) || (values.isEmpty())) {
            return null;
        }

        List newValuesD = new ArrayList();
        for(Iterator iter = valuesD.iterator(); iter.hasNext(); ) {
            String valueD = (String)iter.next();
            if (values.contains(valueD)) {
                newValuesD.add(valueD);
            }
        }

        if (newValuesD.isEmpty()) {
            return null;
        }

        if (newValuesD.size() == valuesD.size()) {
            return desiredAttr;
        }

        try {
            Attribute newAttr =
                AssertionFactory.getInstance().createAttribute();
            newAttr.setName(desiredAttr.getName());
            newAttr.setNameFormat(desiredAttr.getNameFormat());
            newAttr.setFriendlyName(desiredAttr.getFriendlyName());
            newAttr.setAnyAttribute(desiredAttr.getAnyAttribute());
            newAttr.setAttributeValueString(newValuesD);

            return newAttr;
        } catch(SAML2Exception se) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message(
                    "AttributeQueryUtil.filterAttributeValues:", se);
            }
            return null;
        }
    }

    private static boolean isSameAttribute(Attribute attr1,
        AttributeElement attr2) {

        if (!attr1.getName().equals(attr2.getName())) {
            return false;
        }

        String nameFormat1 = attr1.getNameFormat();

        if (nameFormat1 == null) {
            return (attr2.getNameFormat() == null);
        } else {
            return (nameFormat1.equals(attr2.getNameFormat()));
        }

    }

    private static boolean isValueValid(Attribute desiredAttr,
        AttributeElement supportedAttr) {

        List valuesD = desiredAttr.getAttributeValueString();
        if ((valuesD == null) || (valuesD.isEmpty())) {
            return true;
        }
        List attrValuesS = supportedAttr.getAttributeValue();
        if ((attrValuesS == null) || (attrValuesS.isEmpty())) {
            return true;
        }

        List valuesS = new ArrayList();
        for(Iterator iter = attrValuesS.iterator(); iter.hasNext(); ) {
            AttributeValueElement attrValueElem =
                (AttributeValueElement)iter.next();
            valuesS.addAll(attrValueElem.getContent());
        }

        try {
            return valuesS.containsAll(valuesD);
        } catch (Exception ex) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message(
                    "AttributeQueryUtil.isValueValid:", ex);
            }
            return false;
        }
    }

    private static Response sendAttributeQuerySOAP(AttributeQuery attrQuery,
        String attributeServiceURL, String attrAuthorityEntityID,
        AttributeAuthorityDescriptorElement aad) throws SAML2Exception {

        String attrQueryXMLString = attrQuery.toXMLString(true, true);
        if (SAML2Utils.debug.messageEnabled()) {
            SAML2Utils.debug.message(
                "AttributeQueryUtil.sendAttributeQuerySOAP: " +
                "attrQueryXMLString = " + attrQueryXMLString);
            SAML2Utils.debug.message(
                "AttributeQueryUtil.sendAttributeQuerySOAP: " +
                "attributeServiceURL = " + attributeServiceURL);
        }
        
        SOAPMessage resMsg = null;
        try {
            resMsg = SAML2Utils.sendSOAPMessage(attrQueryXMLString,
                attributeServiceURL, true);
        } catch (SOAPException se) {
            SAML2Utils.debug.error(
                "AttributeQueryUtil.sendAttributeQuerySOAP: ", se);
            throw new SAML2Exception(
                SAML2Utils.bundle.getString("errorSendingAttributeQuery"));
        }
        
        Element respElem = SAML2Utils.getSamlpElement(resMsg, "Response");
        Response response =
            ProtocolFactory.getInstance().createResponse(respElem);
        
        if (SAML2Utils.debug.messageEnabled()) {
            SAML2Utils.debug.message(
                "AttributeQueryUtil.sendAttributeQuerySOAP: " +
                "response = " + response.toXMLString(true, true));
        }

        verifyResponse(response, attrQuery, attrAuthorityEntityID, aad);

        return response;
    }

    private static void verifyResponse(Response response,
        AttributeQuery attrQuery, String attrAuthorityEntityID,
        AttributeAuthorityDescriptorElement aad)
        throws SAML2Exception {

        String attrQueryID = attrQuery.getID();
        if ((attrQueryID != null) &&
            (!attrQueryID.equals(response.getInResponseTo()))) {

            throw new SAML2Exception(
                SAML2Utils.bundle.getString("invalidInResponseToAttrQuery"));
        }

        Issuer respIssuer = response.getIssuer();
        if (respIssuer == null) {
            return;
        }

        if (!attrAuthorityEntityID.equals(respIssuer.getValue())) {
            throw new SAML2Exception(SAML2Utils.bundle.getString(
                "responseIssuerMismatch"));
        }

        if (!response.isSigned()) {
            throw new SAML2Exception(SAML2Utils.bundle.getString(
                "responseNotSigned"));
        }

        X509Certificate signingCert =
            KeyUtil.getVerificationCert(aad, attrAuthorityEntityID,
                SAML2Constants.ATTR_AUTH_ROLE);

        if (signingCert != null) {
            boolean valid = response.isSignatureValid(signingCert);
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message(
                    "AttributeQueryUtil.verifyResponse: " +
                    "Signature validity is : " + valid);
            }
            if (!valid) {
                throw new SAML2Exception(SAML2Utils.bundle.getString(
                    "invalidSignatureOnResponse"));
            }
        } else {
            throw new SAML2Exception(
                    SAML2Utils.bundle.getString("missingSigningCertAlias"));
        }

    }

    private static String findLocation(
        AttributeAuthorityDescriptorElement aad, String binding,
        String attrQueryProfile, String attrProfile) {

        List attrProfiles = aad.getAttributeProfile();
        if ((attrProfiles == null) || (attrProfiles.isEmpty())) {
            if (attrProfile != null) {
                return null;
            }
        } else if (!attrProfiles.contains(attrProfile)) {
            return null;
        }

        List attrServices = aad.getAttributeService();
        for(Iterator iter = attrServices.iterator(); iter.hasNext(); ) {
            AttributeServiceElement attrService =
                (AttributeServiceElement)iter.next();
            if (isValidAttributeService(binding, attrService,
                attrQueryProfile)) {
                return attrService.getLocation();
            }
        }

        return null;
    }

    private static boolean isValidAttributeService(String binding,
        AttributeServiceElement attrService, String attrQueryProfile) {
    
        if (!binding.equalsIgnoreCase(attrService.getBinding())) {
            return false;
        }

        if (attrQueryProfile == null) {
            return false;
        }

        return ((attrQueryProfile.equals(
            SAML2Constants.DEFAULT_ATTR_QUERY_PROFILE)) ||
            (SAML2Constants.X509_SUBJECT_ATTR_QUERY_PROFILE.equals(
            attrQueryProfile) && attrService.isSupportsX509Query()));
    }

    /** 
     * Returns an <code>AttributeAuthorityMapper</code>
     *
     * @param realm the realm name
     * @param attrAuthorityEntityID the entity id of the attribute authority
     * @param attrQueryProfileAlias attribute profile alias
     *
     * @return the <code>AttributeAuthorityMapper</code>
     * @exception SAML2Exception if the operation is not successful
     */
    static AttributeAuthorityMapper getAttributeAuthorityMapper(String realm,
        String attrAuthorityEntityID, String attrQueryProfileAlias)
        throws SAML2Exception {

        String attrAuthorityMapperName = null;
        AttributeAuthorityMapper attrAuthorityMapper = null;
        try {
            attrAuthorityMapperName = getAttributeValueFromAttrAuthorityConfig(
                realm, attrAuthorityEntityID, attrQueryProfileAlias + "_" +
                SAML2Constants.ATTRIBUTE_AUTHORITY_MAPPER);

            if (attrAuthorityMapperName == null) {
                attrAuthorityMapperName = 
                    SAML2Constants.DEFAULT_ATTRIBUTE_AUTHORITY_MAPPER_CLASS;
                if (SAML2Utils.debug.messageEnabled()) {
                    SAML2Utils.debug.message(
                        "AttributeQueryUtil.getAttributeAuthorityMapper: use "+
                        attrAuthorityMapperName);
                }
            }
            attrAuthorityMapper = (AttributeAuthorityMapper)
                attrAuthorityMapperCache.get(attrAuthorityMapperName);
            if (attrAuthorityMapper == null) {
                attrAuthorityMapper = (AttributeAuthorityMapper)
                    Class.forName(attrAuthorityMapperName).newInstance();
                attrAuthorityMapperCache.put(attrAuthorityMapperName,
                       attrAuthorityMapper);
            } else {
                if (SAML2Utils.debug.messageEnabled()) {
                    SAML2Utils.debug.message(
                        "AttributeQueryUtil.getAttributeAuthorityMapper: " +
                        "got the AttributeAuthorityMapper from cache");
                }
            }
        } catch (Exception ex) {
            SAML2Utils.debug.error(
                "AttributeQueryUtil.getAttributeAuthorityMapper: " +
                "Unable to get IDP Attribute Mapper.", ex);
            throw new SAML2Exception(ex);
        }

        return attrAuthorityMapper;
    }

    private static String getAttributeValueFromAttrAuthorityConfig(
        String realm, String attrAuthorityEntityID, String attrName)
    {
        try {
            AttributeAuthorityConfigElement config =
                metaManager.getAttributeAuthorityConfig(realm,
                attrAuthorityEntityID);
            Map attrs = SAML2MetaUtils.getAttributes(config);
            String value = null;
            List values = (List) attrs.get(attrName);
            if ((values != null) && (!values.isEmpty())) {
                value = ((String)values.iterator().next()).trim();
            }
            return value;
        } catch (SAML2MetaException sme) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message("AttributeQueryUtil." +
                   "getAttributeValueFromAttrAuthorityConfig: " +
                   "get AttributeAuthorityConfig failed", sme);
            }
        }
        return null;
    }

    /**
     * Sends the AttributeQuery to specifiied attribute authority, 
     * validates the response and returns the attribute map
     * <code>Map</code> to the Fedlet
     *
     * @param spEntityID SP entity ID
     * @param idpEntityID IDP entity ID
     * @param nameIDValue  NameID value 
     * @param attrsList The list of attributes whose values needs to be 
     *                  fetched from IDP
     * @param encrypt  Boolean value to indicate whether the Fedlet will
     *                 receive the encrypted data
     * @param attrQueryProfileAlias  Attribute Query Profile Alias
     * @param subjectDN  Attribute name which contains X.509 subject DN
     *
     * @return the <code>Map</code> object
     * @exception SAML2Exception if the operation is not successful
     *
     * @supported.api
     */
    public static Map getAttributeMapForFedlet(String spEntityID,
                                               String idpEntityID,
                                               String nameIDValue,
                                               List attrsList,
                                               String attrQueryProfileAlias,
                                               String subjectDN)
                                               throws SAML2Exception {

        AttributeQueryConfigElement attrQueryConfig =
                metaManager.getAttributeQueryConfig("/", spEntityID);
        if (attrQueryConfig == null) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message("AttributeQueryUtil." +
                                          "getAttributeMapForFedlet: " +
                                          "Attribute Query Config is null");
             }
            return null;
        }

        String attrqMetaAlias = attrQueryConfig.getMetaAlias();
        if (attrqMetaAlias == null) {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message("AttributeQueryUtil." +
                                       "getAttributeMapForFedlet: " +
                                       "Attribute Query MetaAlias is null");
             }
            return null;
        }
        
        boolean wantNameIDEncrypted = SAML2Utils.getWantNameIDEncrypted("/",
                                              spEntityID,
                                              SAML2Constants.ATTR_QUERY_ROLE);
        
        AttributeQuery attrQuery = constructAttrQueryForFedlet(spEntityID,
                                                          idpEntityID,
                                                          nameIDValue,
                                                          attrsList,
                                                          attrqMetaAlias,
                                                          attrQueryProfileAlias,
                                                          subjectDN,
                                                          wantNameIDEncrypted);
        
        String attrQueryProfile = null;
        if (attrQueryProfileAlias.equals(
                SAML2Constants.DEFAULT_ATTR_QUERY_PROFILE_ALIAS)) {
            attrQueryProfile = SAML2Constants.DEFAULT_ATTR_QUERY_PROFILE;
        } else if (attrQueryProfileAlias.equals(
                SAML2Constants.X509_SUBJECT_ATTR_QUERY_PROFILE_ALIAS)) {
            attrQueryProfile = SAML2Constants.X509_SUBJECT_ATTR_QUERY_PROFILE;
        }

        Response samlResp = sendAttributeQuery(attrQuery, idpEntityID,
                                  "/",
                                  attrQueryProfile,
                                  SAML2Constants.BASIC_ATTRIBUTE_PROFILE,
                                  SAML2Constants.SOAP);

        // Validate the response
        boolean validResp = validateSAMLResponseForFedlet(samlResp, 
                                                          spEntityID,
                                                          wantNameIDEncrypted);
        Map attrMap = new HashMap();
        if (validResp) {
            // Return back the AttributeMap
            if (samlResp != null) {
                List assertions = null;
                if (wantNameIDEncrypted) {
                    assertions = samlResp.getEncryptedAssertion();
                } else {
                    assertions = samlResp.getAssertion();
                }
                for (Iterator asserIter = assertions.iterator();
                    asserIter.hasNext();) {
                    Assertion assertion = null;
                    if (wantNameIDEncrypted) {
                        assertion = getDecryptedAssertion(
                                           (EncryptedAssertion)asserIter.next(),
                                            spEntityID);
                    } else {
                        assertion = (Assertion)asserIter.next();
                    }
                    if (assertion != null) {
                        List statements = assertion.getAttributeStatements();
                        if (statements != null && statements.size() > 0 ) {
                            for (Iterator stmtIter = statements.iterator();
                                stmtIter.hasNext();) {
                                AttributeStatement statement =
                                    (AttributeStatement)stmtIter.next();
                                List attributes = statement.getAttribute();
                                if (attributes != null) {
                                    for (Iterator attribIter =
                                        attributes.iterator();
                                        attribIter.hasNext(); ) {
                                        Attribute attr =
                                            (Attribute)attribIter.next();
                                        String attrName = attr.getName();
                                        List attrValueList =
                                            attr.getAttributeValueString();
                                        StringBuffer attrValue =
                                                     new StringBuffer();
                                        for (Iterator attrValueIter =
                                             attrValueList.iterator();
                                             attrValueIter.hasNext(); ) {
                                            // Multiple attribute values
                                            // are seperated with "|"
                                            if (attrValue.length() > 0) {
                                                attrValue.append('|');
                                            }
                                            String value =
                                                (String)attrValueIter.next();
                                            attrValue.append(value);
                                        }
                                        attrMap.put(attrName,
                                                    attrValue.toString());
                                    }
                                } else {
                                    if (SAML2Utils.debug.messageEnabled()) {
                                        SAML2Utils.debug.message(
                                            "AttributeQueryUtil." +
                                            "getAttributeMapForFedlet: " +
                                            "No Attributes present in " +
                                            "SAML response");
                                    }
                                }
                            }
                        } else {
                            if (SAML2Utils.debug.messageEnabled()) {
                                SAML2Utils.debug.message("AttributeQueryUtil." +
                                "getAttributeMapForFedlet: " +
                                "Empty Statement present in SAML response");
                            }
                        }
                    } else {
                        if (SAML2Utils.debug.messageEnabled()) {
                            SAML2Utils.debug.message("AttributeQueryUtil." +
                            "getAttributeMapForFedlet: " +
                            "Empty Assertion present in SAML response");
                        }
                    }
                }
            }
        } else {
            if (SAML2Utils.debug.messageEnabled()) {
                SAML2Utils.debug.message("AttributeQueryUtil." +
                   "getAttributeMapForFedlet: " +
                   "Invalid response obtained from Attribute Authority");
            }
        }
        // Return the attribute map and to the fedlet
        return attrMap;
    }

    /**
     * Constructs the Attribute Query used by the Fedlet to retrieve the 
     * values from IDP
     *
     * @param samlResp saml response
     *
     * @exception SAML2Exception if the operation is not successful
     *
     * @supported.api
     */
    private static AttributeQuery constructAttrQueryForFedlet(
                             String spEntityID,
                             String idpEntityID,
                             String nameIDValue,
                             List attrsList,
                             String attrqMetaAlias,
                             String attrProfileNameAlias,
                             String subjectDN,
                             boolean wantNameIDEncrypted) throws SAML2Exception
    {
        String attrqEntityID =
          SAML2Utils.getSAML2MetaManager().getEntityByMetaAlias(attrqMetaAlias);

        ProtocolFactory protocolFactory = ProtocolFactory.getInstance();
        AssertionFactory assertionFactory = AssertionFactory.getInstance();        

        AttributeQuery attrQuery = protocolFactory.createAttributeQuery();

        Issuer issuer = assertionFactory.createIssuer();
        issuer.setValue(attrqEntityID);

        attrQuery.setIssuer(issuer);
        attrQuery.setID(SAML2Utils.generateID());
        attrQuery.setVersion(SAML2Constants.VERSION_2_0);
        attrQuery.setIssueInstant(new Date());

        List attrs = new ArrayList();
        for (Iterator attrIter = attrsList.iterator();
                                attrIter.hasNext();) {
            Attribute attr = assertionFactory.createAttribute();
            String attributeName = (String)attrIter.next();
            attr.setName(attributeName);
            attr.setNameFormat(SAML2Constants.BASIC_NAME_FORMAT);
            attrs.add(attr);
        }        
        attrQuery.setAttributes(attrs);

        Subject subject = assertionFactory.createSubject();
        NameID nameID = assertionFactory.createNameID();
        nameID.setNameQualifier(idpEntityID);
        nameID.setSPNameQualifier(spEntityID);

        if (attrProfileNameAlias.equals(
                    SAML2Constants.DEFAULT_ATTR_QUERY_PROFILE_ALIAS)) {
            nameID.setFormat(SAML2Constants.NAMEID_TRANSIENT_FORMAT);
            nameID.setValue(nameIDValue);
        }

        if (attrProfileNameAlias.equals(
                    SAML2Constants.X509_SUBJECT_ATTR_QUERY_PROFILE_ALIAS)) {
            nameID.setFormat(SAML2Constants.X509_SUBJECT_NAME);
            nameID.setValue(subjectDN);            
        }

        if (!wantNameIDEncrypted) {
            subject.setNameID(nameID);
        } else {
            AttributeAuthorityDescriptorElement aad =
                  metaManager.getAttributeAuthorityDescriptor("/", idpEntityID);

            EncInfo encInfo = KeyUtil.getEncInfo(aad, idpEntityID,
                                                 SAML2Constants.ATTR_AUTH_ROLE);            

            EncryptedID encryptedID = nameID.encrypt(encInfo.getWrappingKey(),
                                                  encInfo.getDataEncAlgorithm(),
                                                  encInfo.getDataEncStrength(),
                                                  idpEntityID);
            subject.setEncryptedID(encryptedID);
        }
    
        attrQuery.setSubject(subject);

        return attrQuery;
    }

     /**
     * Validates the SAML response obtained from Attribute Authortity
     *
     * @param samlResp saml response
     *
     * @exception SAML2Exception if the operation is not successful
     *
     * @supported.api
     */
    private static boolean validateSAMLResponseForFedlet(
                              Response samlResp,
                              String spEntityID, 
                              boolean wantNameIDEncrypted) throws SAML2Exception
    {
        boolean resp = true;
        if (samlResp != null && samlResp.isSigned()) {
            List assertions = null;
            if (wantNameIDEncrypted) {
                assertions = samlResp.getEncryptedAssertion();
            } else {
                assertions = samlResp.getAssertion();
            }
            if (assertions == null) {
                return false;
            }
            for (Iterator asserIter = assertions.iterator();
                 asserIter.hasNext();) {
                Assertion assertion = null;
                if (wantNameIDEncrypted) {
                    assertion = getDecryptedAssertion(
                                         (EncryptedAssertion)asserIter.next(),
                                          spEntityID);
                } else {
                    assertion = (Assertion)asserIter.next();
                }
                if (assertion != null) {
                    Conditions conditions = assertion.getConditions();
                    if (conditions != null) {
                        List audienceRes = conditions.
                                               getAudienceRestrictions();
                        if (audienceRes.size() > 1) {
                                resp = false;
                                break;
                        }
                     }
                     List statements = assertion.getAttributeStatements();
                     if (statements.size() > 1) {
                         resp = false;
                         break;
                     }
                }
            }
        } else {
            resp = false;
        }
        return resp;
    }

     /**
     * Returns the decrypted assertion
     *
     * @param samlResp saml response
     *
     * @exception SAML2Exception if the operation is not successful
     *
     * @supported.api
     */
    private static Assertion getDecryptedAssertion(
                                      EncryptedAssertion eAssertion,
                                      String spEntityID) throws SAML2Exception 
    {
        if (eAssertion != null) {
            String alias = SAML2Utils.getEncryptionCertAlias("/", spEntityID,
                                      SAML2Constants.ATTR_QUERY_ROLE);
            PrivateKey privateKey = 
                       KeyUtil.getKeyProviderInstance().getPrivateKey(alias);
            if (privateKey != null) {
                Assertion assertion = eAssertion.decrypt(privateKey);
                return assertion;
            }
        }        
        return null;
    }
}
