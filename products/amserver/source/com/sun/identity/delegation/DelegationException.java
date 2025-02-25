/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: DelegationException.java,v 1.3 2008-06-25 05:43:24 qcheng Exp $
 *
 */

package com.sun.identity.delegation;

import com.sun.identity.common.ChainedException;

/**
 * The class <code>DelegationException</code> is the basic exception for the
 * delegation component.
 */
public class DelegationException extends ChainedException {

    /**
     * Constructs an instance of the <code>DelegationException</code> class.
     * 
     * @param message The message provided by the object that is throwing 
     *        the exception.

     */
    public DelegationException(String message) {
        super(message);
    }

    /**
     * Constructs an instance of the <code>DelegationException</code> class.
     * 
     * @param nestedException the exception caught by the code block creating 
     *        this exception
     * 
     */
    public DelegationException(Throwable nestedException) {
        super(nestedException);
    }

    /**
     * Constructs an instance of the <code>DelegationException</code> class.
     * 
     * @param message message of this exception
     * @param nestedException the exception caught by the code block creating 
     *        this exception
     * 
     */
    public DelegationException(String message, Throwable nestedException) {
        super(message, nestedException);
    }

    /**
     * Constructs an instance of <code>DelegationException</code> to pass the
     * localized error message. At this level, the locale of the caller is not
     * known and it is not possible to throw localized error message at this
     * level. Instead this constructor provides Resource Bundle name and error
     * code for correctly locating the error message. The default
     * <code>getMessage()</code> will always return English messages only. 
     * This is consistent with current JRE
     * 
     * @param rbName Resource Bundle Name to be used for getting localized 
     *               error message.
     * @param errorCode Key to resource bundle. You can use:
     * 
     * <pre>
     * ResourceBundle rb = ResourceBunde.getBundle(rbName, locale);
     * 
     * String localizedStr = rb.getString(errorCode);
     * </pre>
     * 
     * @param args arguments to message. If it is not present pass the as null
     * @param nestedException The root cause of this error
     */
    public DelegationException(String rbName, String errorCode, Object[] args,
            Throwable nestedException) {
        super(rbName, errorCode, args, nestedException);
    }
}
