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
 * $Id: MessageAuthenticator.java,v 1.2 2008-06-25 05:50:11 qcheng Exp $
 *
 */


package com.sun.identity.wss.security.handler;

import javax.security.auth.Subject;

import com.sun.identity.wss.security.SecurityMechanism;
import com.sun.identity.wss.security.SecurityToken;
import com.sun.identity.wss.security.SecurityException;
import com.sun.identity.wss.provider.ProviderConfig;


/**
 * This interface provides a pluggable authenticator for the webservices
 * to authenticate their clients using various security mechanisms. 
 * @supported.all.api
 */ 
public interface MessageAuthenticator {

    /**
     * Authenticates the web services client.
     * @param subject the JAAS subject that may be used during authentication.
     * @param securityMechanism the security mechanism that will be used to
     *        authenticate the web services client.
     * @param securityToken the security token that is used.
     * @param config the provider configuration.
     * @param secureMessage the secure SOAPMessage.
     *      If the message security is provided by the WS-I profies, the
     *      secureMessage object is of type
     *     <code>com.sun.identity.wss.security.handler.SecureSOAPMessage</code>.     *     If the message security is provided by the Liberty ID-WSF
     *     profiles, the secure message is of type
     *     <code>com.sun.identity.liberty.ws.soapbinding.Message</code>.
     * @param isLiberty boolean variable to indicate that the message
     *        security is provided by the liberty security profiles.
     * @exception SecurityException if there is a failure in authentication.
     */
    public Object authenticate(
             Subject subject,
             SecurityMechanism securityMechanism,
             SecurityToken securityToken,
             ProviderConfig config,
             Object secureMessage,
             boolean isLiberty) throws SecurityException; 
}
