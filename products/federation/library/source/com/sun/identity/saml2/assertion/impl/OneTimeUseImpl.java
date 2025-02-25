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
 * $Id: OneTimeUseImpl.java,v 1.2 2008-06-25 05:47:44 qcheng Exp $
 *
 */


package com.sun.identity.saml2.assertion.impl;

import org.w3c.dom.Element;
import com.sun.identity.saml2.assertion.OneTimeUse;
import com.sun.identity.saml2.common.SAML2Constants;
import com.sun.identity.saml2.common.SAML2Exception;

/**
 * The <code>OneTimeUse</code> indicates that the assertion should be
 * used immediately by the relying party and must not be retained for
 * future use. 
*/

public class OneTimeUseImpl 
    extends ConditionAbstractImpl implements OneTimeUse {

    public static final String ONETIMEUSE_ELEMENT = "OneTimeUse";

   /** 
    * Default constructor
    */
    public OneTimeUseImpl() {
    }

    /**
     * This constructor is used to build <code>OneTimeUse</code> object from a
     * XML string.
     *
     * @param xml A <code>java.lang.String</code> representing
     *        a <code>OneTimeUse</code> object
     * @exception SAML2Exception if it could not process the XML string
     */
    public OneTimeUseImpl(String xml) throws SAML2Exception {
    }

    /**
     * This constructor is used to build <code>OneTimeUse</code> object from a
     * block of existing XML that has already been built into a DOM.
     *
     * @param element A <code>org.w3c.dom.Element</code> representing
     *        DOM tree for <code>OneTimeUse</code> object
     * @exception SAML2Exception if it could not process the Element
     */
    public OneTimeUseImpl(Element element) throws SAML2Exception {
    }

   /**
    * Returns a String representation
    * @param includeNSPrefix Determines whether or not the namespace 
    *        qualifier is prepended to the Element when converted
    * @param declareNS Determines whether or not the namespace is 
    *        declared within the Element.
    * @return A String representation
    * @exception SAML2Exception if something is wrong during conversion
    */
    public String toXMLString(boolean includeNSPrefix, boolean declareNS)
        throws SAML2Exception {
        StringBuffer sb = new StringBuffer(2000);
        String NS = "";
        String appendNS = "";
        if (declareNS) {
            NS = SAML2Constants.ASSERTION_DECLARE_STR;
        }
        if (includeNSPrefix) {
            appendNS = SAML2Constants.ASSERTION_PREFIX;
        }
        sb.append("<").append(appendNS).append(ONETIMEUSE_ELEMENT).
            append(NS).append(" />\n");
        return sb.toString();
    }

   /**
    * Returns a String representation
    *
    * @return A String representation
    * @exception SAML2Exception if something is wrong during conversion
    */
    public String toXMLString() throws SAML2Exception {
        return this.toXMLString(true, false);
    }
}
