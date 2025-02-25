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
 * $Id: CDMBundle.java,v 1.4 2008-06-25 05:41:32 qcheng Exp $
 *
 */

package com.iplanet.services.cdm;

import java.util.ResourceBundle;

/**
 * Provides methods to get the Locale values from resource bundle for
 * <code>ClientDetection</code> class.
 * @supported.all.api
 */
public class CDMBundle {

    private static ResourceBundle cdmBundle = null;

    /**
     * @param str
     *            The key for the resource bundle property to be returned.
     * @return The Locale value for the given key
     */
    public static String getString(String str) {
        if (cdmBundle == null) {
            cdmBundle = com.sun.identity.shared.locale.Locale
                    .getInstallResourceBundle("amClientDetection");
        }
        return cdmBundle.getString(str);
    }
}
