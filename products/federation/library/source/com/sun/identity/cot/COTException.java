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
 * $Id: COTException.java,v 1.2 2008-06-25 05:46:38 qcheng Exp $
 *
 */


package com.sun.identity.cot;

import com.sun.identity.shared.locale.L10NMessageImpl;

/**
 * This class handles all Circle of Trust related exceptions.
 */
public class COTException extends L10NMessageImpl {
    
    /**
     * Constructs a new <code>COTException</code> without a nested
     * <code>Throwable</code> using default resource bundle.
     *
     * @param errorCode Key to resource bundle. You can use
     * <pre>
     * ResourceBundle rb = ResourceBunde.getBundle (rbName,locale);
     * String localizedStr = rb.getString(errorCode);
     * </pre>
     * @param args arguments to message. If it is not present pass them
     * as null
     * 
     */
    public COTException(String errorCode, Object[] args) {
       super(COTUtils.RESOURCE_BUNDLE_NAME,errorCode, args);
    }

    /**
     * Constructs a new <code>COTException</code> without a nested
     * <code>Throwable</code>.
     *
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
    public COTException(String rbName, String errorCode, Object[] args) {
        super(rbName, errorCode, args);
    }
    
    /**
     * Constructs a new <code>COTException</code> with the given
     * message.
     *
     * @param message message for this exception. This message can be later
     * retrieved by <code>getMessage()</code> method.
     * 
     */
    public COTException(String message) {
        super(message);
    }
    
    /**
     * Constructs an <code>COTException</code> with given
     * <code>Throwable</code>.
     *
     * @param t Exception nested in the new exception.
     * 
     */
    public COTException(Throwable t) {
        super(t);
    }
}
