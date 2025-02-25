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
 * $Id: IDPPAttributeMapper.java,v 1.2 2008-06-25 05:47:17 qcheng Exp $
 *
 */


package com.sun.identity.liberty.ws.idpp.plugin;

import com.sun.identity.liberty.ws.idpp.*;
import com.sun.identity.liberty.ws.idpp.common.*;
import java.util.Map;
import java.util.HashMap;

/**
 * This class <code>IDPPAttributeMapper</code> is an implementation of IDPP
 * and the LDAP Attribute Mapper.
 */
public class IDPPAttributeMapper implements AttributeMapper {

    /**
     * Constructor
     */
    public IDPPAttributeMapper() {
       IDPPUtils.debug.message("IDPPAttributeMapper:Init:");
    }

    /**
     * Gets the DS attribute for the PP attribute.
     * @param ppAttribute PP attribute
     * @return String LDAP Attribute
     */
    public String getDSAttribute(String ppAttribute) {
       Map configMap = IDPPServiceManager.getInstance().getPPDSMap();
       if(configMap == null) {
          IDPPUtils.debug.error("IDPPAttributeMapper:getDSAttribute:Attrib" +
          "Map is not null");
          return null;
       }
       String attribMap = (String)configMap.get(ppAttribute);
       if(IDPPUtils.debug.messageEnabled()) {
          IDPPUtils.debug.message("IDPPAttributeMapper:getDSAttribute:" +
          "attribute map for " + ppAttribute + " is " + attribMap);
       }
       return attribMap;
    }

}
