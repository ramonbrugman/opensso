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
 * $Id: SAMLException.java,v 1.2 2008-06-25 05:47:34 qcheng Exp $
 *
 */


package com.sun.identity.saml.common;

import com.sun.identity.shared.locale.L10NMessageImpl;

/**
 * This class is an extension point for all SAML related exceptions.
 * It also handles message localization in SAML exceptions.
 *
 * @supported.all.api
 */
public class SAMLException extends L10NMessageImpl {

    /**
     * Constructs an <code>SAMLException</code> with a message.
     *
     * @param s exception message.
     */
    public SAMLException(String s) {
        super(s);
    }

    /**
     * Constructs an <code>SAMLException</code> with given
     * <code>Throwable</code>.
     *
     * @param t Exception nested in the new exception.
     *
     */
    public SAMLException(Throwable t) {
        super(t);
    }

    /**
     * Constructs a new <code>SAMLException</code> without a nested
     * <code>Throwable</code>.
     * @param rbName Resource Bundle Name to be used for getting
     * localized error message.
     * @param errorCode Key to resource bundle. You can use
     * <pre>
     * ResourceBundle rb = ResourceBunde.getBundle (rbName,locale);
     * String localizedStr = rb.getString(errorCode);
     * </pre>
     * @param args arguments to message. If it is not present pass them
     * as null
     *
     */
    public SAMLException(String rbName, String errorCode, Object[] args) {
        super(rbName, errorCode, args);
    }

}
