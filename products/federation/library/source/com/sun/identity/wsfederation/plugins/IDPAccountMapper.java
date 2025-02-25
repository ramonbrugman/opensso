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
 * $Id: IDPAccountMapper.java,v 1.3 2008-06-25 05:48:07 qcheng Exp $
 *
 */


package com.sun.identity.wsfederation.plugins;

import com.sun.identity.saml.assertion.NameIdentifier;
import com.sun.identity.wsfederation.common.WSFederationException;

/**
 * The interface <code>IDPAccountMapper</code> is used to map the
 * local identities to the <code>SAML</code> protocol objects and
 * also the vice versa for some of the protocols for e.g.
 * <code>ManageNameIDRequest</code>.
 * This mapper interface is used to map the identities only at the
 * <code>SAMLAssertionProducer</code>, in otherwords, <code>SAML</code>
 *  Provider as an <code>IdentityProvider</code>. The implementation of this
 * interface will be used by the <code>SAML</code> framework to retrieve
 * the user's account federation information for the constructing
 * SAML protocol objects such as <code>Assertion</code> and also to
 * find out the corresponding user account for the given SAML requests.
 * The implementation of this interface may need to consider the
 * deployment of the WS-Federation implementation for example the 
 * <code>AccessManger</code>
 * platform or the <code>FederationManager</code> platform.
 * @see com.sun.identity.wsfederation.plugins.SPAccountMapper
 *
 * @supported.all.api
 */
public interface IDPAccountMapper {

    /**
     * Returns the user's <code>NameID</code>information that contains
     * account federation with the corresponding remote and local entities.
     * @param session Single Sign On session of the user.
     * @param hostEntityID <code>EntityID</code> of the hosted provider.
     * @param remoteEntityID <code>EntityID</code> of the remote provider.
     * @return the <code>NameID</code> corresponding to the authenticated user.
     * @exception WSFederationException if any failure.
     */
    public NameIdentifier getNameID(
        Object session,
        String realm,
        String hostEntityID,
        String remoteEntityID
    ) throws WSFederationException;


    /**
     * Returns the user's disntinguished name or the universal ID for the
     * corresponding  <code>SAML</code> <code>ManageNameIDRequest</code>.
     * This method returns the universal ID or the DN based on the
     * deployment of the SAMLv2 plugin base platform.
     *
     * @param manageNameIDRequest <code>SAML</code>
     *   <code>ManageNameIDRequest</code> that needs to be mapped to the user.
     * @param hostEntityID <code>EntityID</code> of the hosted provider.
     * @param realm realm or the organization name that may be used to find
     *        the user information.
     * @return user's disntinguished name or the universal ID.
     * @exception WSFederationException if any failure.
     */
    /*
    public java.lang.String getIdentity(
        com.sun.identity.saml2.protocol.ManageNameIDRequest manageNameIDRequest,
        java.lang.String hostEntityID,
        java.lang.String realm
    ) throws WSFederationException;
     */

}
