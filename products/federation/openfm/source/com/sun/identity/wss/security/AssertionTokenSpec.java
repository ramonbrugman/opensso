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
 * $Id: AssertionTokenSpec.java,v 1.6 2009-08-29 03:05:57 mallas Exp $
 *
 */

package com.sun.identity.wss.security;

import org.w3c.dom.Element;
import java.util.Map;
import java.util.List;
import javax.xml.namespace.QName;
import com.sun.identity.saml.assertion.NameIdentifier;


/**
 * This class implements the interface <code>SecurityTokenSpec</code> to
 * create <code>SAML1.0</code> and <code>SAML1.1</code> Assertions / Security 
 * Tokens.
 * 
 * @supported.all.api 
 */
public class AssertionTokenSpec implements SecurityTokenSpec {

    private SecurityMechanism securityMechanism = null;
    private String certAlias = null;
    private NameIdentifier nameIdentifier = null;
    private String issuer = null;
    private String confirmationMethod = null;
    private Map<QName, List<String>> claims = null;
    private String appliesTo = null;
    private long assertionInterval = 300000;
    private String authMethod = null;
    private String assertionID = null;
    private String signingAlias = null;
    private Element keyInfo = null;
       
    public AssertionTokenSpec() {
           
    }

      /**
       * Construtor
       * 
       * @param nameIdentifier the name identifier of the authenticated subject.
       *
       * @param securityMechanism the security mechanism that should be used
       *        to generate the assertion token.
       *
       * @param certAlias the public key certificate alias of the authenticated
       *        subject. 
       */
      public AssertionTokenSpec(NameIdentifier nameIdentifier, 
                SecurityMechanism securityMechanism, 
                String certAlias) {

           this.nameIdentifier = nameIdentifier;
           this.securityMechanism = securityMechanism;
           this.certAlias = certAlias;
      }

      /**
       * Returns the authenticated subject name identifier.
       *
       * @return the name identifier of the authenticated subject.
       */
      public NameIdentifier getSenderIdentity() {
           return nameIdentifier;
      }
      
      /**
       * Sets the sender identity
       * @param nameID the sender's name identifier.
       */
       public void setSenderIdentity(NameIdentifier nameID) {
           this.nameIdentifier = nameID;
       }

      /**
       * Returns the security mechanism
       * @return the security mechanism
       */
      public SecurityMechanism getSecurityMechanism() {
          return securityMechanism;
      }

      /**
       * Returns the certficate alias of the subject.
       *
       * @return the certificate alias of the subject.
       */
      public String getSubjectCertAlias() {
          return certAlias;
      }
                  
   /**
     * Sets the certificate alias of the subject.
     * @param certAlias the certificate alias of the subject.
     */
    public void setSubjectCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }
    
    /**
     * Returns the issuer name.
     * @return the issuer name.
     */
    public String getIssuer() {
        return issuer;
    }
    
    /**
     * Sets the issuer name.
     * @param issuer the issuer name.
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    /**
     * Returns the cliamed attributes
     * @return the cliamed attributes
     */
    public Map<QName, List<String>> getClaimedAttributes() {
        return claims;
    }
    
    /**
     * Sets the claimed attributes
     * @param attrs the claimed attributes
     */
    public void setClaimedAttributes(Map attrs) {
        this.claims = attrs;
    }
    
    /**
     * Returns the confirmation method.
     * @return the confirmation method.
     */
    public String getConfirmationMethod() {
        return confirmationMethod;
    }
    
    /**
     * Sets the confirmation method
     * @param confirmationMethod the confirmation method
     */
    public void setConfirmationMethod(String confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
    }
    
    /**
     * Returns the name of the service for which assertion needs to be issued
     * @return the name of the service for which assertion needs to be issued
     */
    public String getAppliesTo() {
        return appliesTo;
    }
    
    /**
     * Sets the name of the service for which the assertion needs to be issued.
     * @param appliesTo the name of the service for which the assertion needs
     *        to be issued.
     */
    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }
    
    /**
     * Returns the assertion interval
     * @return the assertion interval
     */
    public long getAssertionInterval() {
        return assertionInterval;
    }
    
    /**
     * Sets the assertion interval
     * @param interval the assertion interval.
     */
    public void setAssertionInterval(long interval) {
        this.assertionInterval = interval;
    }
    
    /**
     * Returns the authentication method
     * @return the authentication method
     */
    public String getAuthenticationMethod() {
        return authMethod;
    }
    
    /**
     * Sets the authentication method
     * @param authMethod the authentication method.
     */
    public void setAuthenticationMethod(String authMethod) {
        this.authMethod = authMethod;
    }
    
    /**
     * Returns the assertion identifier.
     * @return the assertion identifier.
     */
    public String getAssertionID() {
        return assertionID;
    }
    
    /**
     * Sets the assertion identifier.
     * @param assertionID the assertion identifier.
     */
    public void setAssertionID(String assertionID) {
        this.assertionID = assertionID;
    }
    
    /**
     * Returns the signing alias
     * @return the signing alias
     */
    public String getSigningAlias() {
        return signingAlias;
    }
    
    /**
     * Sets the signing cert alias.
     * @param alias the sigining cert alias.
     */
    public void setSigningAlias(String alias) {
        this.signingAlias = alias;
    }
    
    /**
     * Returns the keyinfo element.
     * @return the keyinfo element.
     */
    public Element getKeyInfo() {
        return keyInfo;
    }
    
    /**
     * Sets the keyinfo element.
     * @param keyInfo the keyinfo element.
     */
    public void setKeyInfo(Element keyInfo) {
        this.keyInfo = keyInfo;
    }

}
