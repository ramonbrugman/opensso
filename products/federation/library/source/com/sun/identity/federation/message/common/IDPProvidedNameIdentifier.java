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
 * $Id: IDPProvidedNameIdentifier.java,v 1.2 2008-06-25 05:46:47 qcheng Exp $
 *
 */

package com.sun.identity.federation.message.common;

import com.sun.identity.federation.common.FSUtils;
import com.sun.identity.federation.common.IFSConstants;
import com.sun.identity.saml.assertion.NameIdentifier;
import com.sun.identity.saml.common.SAMLException;
import com.sun.identity.shared.xml.XMLUtils;
import org.w3c.dom.Element;

/**
 * This class has methods to create the <code>NameIdentifier</code>
 * object provided by the Identity Provider.
 *
 * @supported.all.api
 */
public class IDPProvidedNameIdentifier extends NameIdentifier {

    protected int minorVersion = IFSConstants.FF_11_PROTOCOL_MINOR_VERSION;

    /**
     * Constructor creates <code>IDPProvidedNameIdentifier</code> object.
     *
     * @param name the Identity Provider name.
     * @param nameQualifier 
     * @param format
     * @throws SAMLException on error.
     */
    public IDPProvidedNameIdentifier(String name,String nameQualifier,
            String format) throws SAMLException {                
                super(name, nameQualifier, format);                
        }
    
    /**
     * Constructor creates <code>IDPProvidedNameIdentifier</code> object
     * from Document Element.
     *
     * @param idpProvidedNameIdentifierElement the Document Element.
     * @throws FSMsgException on error.
     */
    public IDPProvidedNameIdentifier(Element idpProvidedNameIdentifierElement)
                                     throws FSMsgException {
        FSUtils.debug.message("IDPProvidedNameIdentifier(Element):  Called");
        Element elt = (Element) idpProvidedNameIdentifierElement;
        String eltName = elt.getLocalName();
        if (eltName == null)  {
            if (FSUtils.debug.messageEnabled()) {
                FSUtils.debug.message("IDPProvidedNameIdentifier(Element): "
                        + "local name missing");
            }
            throw new FSMsgException("nullInput",null) ;
        }
        if (!(eltName.equals("IDPProvidedNameIdentifier")))  {
            if (FSUtils.debug.messageEnabled()) {
                FSUtils.debug.message("IDPProvidedNameIdentifier(Element: "
                        + "invalid root element");
            }
            throw new FSMsgException("invalidElement",null) ;
        }
        String read = elt.getAttribute("NameQualifier");
        if (read != null) {
            setNameQualifier(read);
        }
        read = elt.getAttribute("Format");
        if (read != null) {
            setFormat(read);
        }
        read = XMLUtils.getElementValue(elt);
        if ((read == null) || (read.length() == 0)) {
            if (FSUtils.debug.messageEnabled()) {
                FSUtils.debug.message("IDPProvidedNameIdentifier(Element: "
                        + "null input specified");
            }
            throw new FSMsgException("nullInput",null) ;
        } else {
            setName(read);
        }
    }
    
    /**
     * Constructor creates <code> IDPProvidedNameIdentifier</code> object.
     *
     * @param securityDomain
     * @param name
     * @throws FSMsgException on error.
     */
    public IDPProvidedNameIdentifier(
            String securityDomain, String name
            ) throws FSMsgException {
        if (name== null || name.length() == 0 )  {
            if (FSUtils.debug.messageEnabled()) {
                FSUtils.debug.message("IDPProvidedNameIdentifier: "
                        + "null input specified");
            }
            throw new FSMsgException("nullInput",null) ;
        }
        setName(name);
        if(securityDomain==null)
            setNameQualifier("");
        else
            setNameQualifier(securityDomain);
    }
       
    /**
     * Sets the <code>MinorVersion</code> attribute.
     *
     * @param version the <code>MinorVersion</code> attribute.
     * @see #getMinorVersion()
     */
    public void setMinorVersion(int version) {
        minorVersion = version;
    }
        
    /**
     * Returns the <code>MinorVersion</code> attribute.
     *
     * @return the <code>MinorVersion</code> attribute.
     * @see #setMinorVersion(int)
     */
    public int getMinorVersion() {
        return minorVersion;
    }
    
    /**
     * Returns the string representation of this object.
     *
     * @return a string containing the valid <code>XML</code> for this element
     * @throws FSMsgException if there is an error creating
     *         <code>XML</code> string from this object.
     */
    public String toXMLString() throws FSMsgException {
        String xml = this.toXMLString(true, false);
        return xml;
    }
    /**
     * Returns the string representation of this object.
     *
     * @param includeNS determines whether or not the namespace qualifier
     *        is prepended to the Element when converted
     * @param declareNS : Determines whether or not the namespace is declared
     *        within the Element.
     * @return a string containing the valid <code>XML</code> for this element
     * @throws FSMsgException if there is an error creating
     *         <code>XML</code> string from this object.
     */
    public String  toXMLString(
            boolean includeNS, boolean declareNS
            ) throws FSMsgException {
        StringBuffer xml = new StringBuffer(3000);
        String NS="";
        String appendNS="";
        if (declareNS) {
            if(minorVersion == IFSConstants.FF_12_PROTOCOL_MINOR_VERSION) {
                NS = IFSConstants.LIB_12_NAMESPACE_STRING;
            } else {
                NS=IFSConstants.LIB_NAMESPACE_STRING;
            }
        }
        if (includeNS) appendNS=IFSConstants.LIB_PREFIX;
        xml.append("<").append(appendNS).append("IDPProvidedNameIdentifier").
                append(" ").append(NS).append(" ");
        if ((getNameQualifier() != null) && 
                     (!(getNameQualifier().length() == 0))) {
            xml.append("NameQualifier").append("=\"")
               .append(getNameQualifier())
               .append("\"").append(" ");
        }
        if ((getFormat() != null) && (!(getFormat().length() == 0))) {
            xml.append("Format").append("=\"").append(getFormat())
               .append("\"").append(" ");
        }
        if ((getName() != null) && (!(getName().length() == 0))) {
            xml.append(">").append(getName());
            xml.append("</").append(appendNS)
               .append("IDPProvidedNameIdentifier").append(">\n");
        }
           return xml.toString();
    }    
}
