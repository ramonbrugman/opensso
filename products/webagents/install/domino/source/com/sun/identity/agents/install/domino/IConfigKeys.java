/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: IConfigKeys.java,v 1.1 2009-11-04 22:09:38 leiming Exp $
 *
 */

package com.sun.identity.agents.install.domino;

/**
 * Interface to isolate the IBM Lotus Domino server specific config keys
 * These keys hold installation information which gets reused
 * throughout installation interactions.
 */
public interface IConfigKeys {
    
    /**
     * Key for the interaction to lookup config dir
     */
    public static String STR_KEY_DOMINO_INST_CONF_DIR = "CONFIG_DIR";
    
    /**
     * Key to store IBM Lotus Domino home dir
     */
    public static String STR_KEY_DOMINO_HOME_DIR = "DOMINO_HOME_DIR";
    
}
