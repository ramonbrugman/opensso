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
 * $Id: ClientDetectionException.java,v 1.3 2008-06-25 05:41:32 qcheng Exp $
 *
 */

package com.iplanet.services.cdm;

/**
 * The <code>ClientDetectionException</code> class is used to throw exceptions
 * whenever an error is encountered in detecting client type. This includes
 * errors while reading the client data.
 * @supported.all.api
 */
public class ClientDetectionException extends Exception {
    /**
     * Constructs an instance of the <code>ClientDetectionException</code>
     * class
     * 
     * @param msg
     *            The message provided by the object which is throwing the
     *            exception
     */
    public ClientDetectionException(String msg) {
        super(msg);
        fillInStackTrace();
    }

    /**
     * Constructs an instance of the <code>ClientDetectionException</code>
     * class
     * 
     * @param t
     *            The <code>Throwable</code> object provided by the object
     *            which is throwing the exception.
     */
    public ClientDetectionException(Throwable t) {
        super(t.getMessage());
        fillInStackTrace();
    }

}
