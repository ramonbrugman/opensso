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
 * $Id: Level.java,v 1.3 2008-06-25 05:43:38 qcheng Exp $
 *
 */



/*This file is added on 4th March, 2002 for DSAME5.2 logging
 *The WebTopParser calls the process() method of this class
 *when its parseoutput is the logType element.
 */
package com.sun.identity.log.service;

import java.util.Hashtable;
import java.util.Vector;
import com.iplanet.dpro.parser.ParseOutput;

/**
 * This class implements <code>ParseOutput</code> interface and parsing
 * log level. This class is registered with the SAX parser.
 */
public class Level implements ParseOutput {
    /**
     * Sting variable to keep parsing target data
     */
    public String str;
    /**
     * The method that implements the ParseOutput interface. This is called
     * by the SAX parser.
     * @param name name of request
     * @param elems vaector has parsing elements
     * @param atts parsing attributes
     * @param pcdata given data to be parsed.
     */
    public void process(String name, Vector elems, Hashtable atts,
    String pcdata) {
        str = pcdata;
    }
} //end of LogType
