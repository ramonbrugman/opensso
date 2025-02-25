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
 * $Id: DSAMEHrefTag.java,v 1.3 2008-06-25 05:41:50 qcheng Exp $
 *
 */

package com.sun.identity.authentication.UI.taglib;

import com.iplanet.jato.CompleteRequestException;
import com.iplanet.jato.model.Model;
import com.iplanet.jato.taglib.DisplayFieldTagBase;
import com.iplanet.jato.util.NonSyncStringBuffer;
import com.iplanet.jato.view.CommandField;
import com.iplanet.jato.view.ContainerView;
import com.iplanet.jato.view.DisplayField;
import com.iplanet.jato.view.View;
import com.iplanet.jato.view.ViewBean;
import com.iplanet.jato.view.ViewBeanBase;
import com.iplanet.jato.view.html.HREF;
import com.iplanet.jato.view.html.HtmlDisplayField;
import com.sun.identity.authentication.UI.AuthViewBeanBase;
import com.sun.identity.shared.locale.Locale;
import com.sun.identity.shared.debug.Debug;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.Enumeration;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * Href tag reimplements JATO Href tag.  It adds a content encoding
 * query parameter and do not do extra encoding.  This element helps
 * web server to figure out the encoding used.
 */
public class DSAMEHrefTag
extends DisplayFieldTagBase
implements BodyTag {
    private BodyContent bodyContent=null;
    private NonSyncStringBuffer buffer=null;
    private boolean displayed=false;
    private CompleteRequestException abortedException;
    static Debug debug ;
    static {
        debug = Debug.getInstance("amTagLib");
    }
    
    /** constructs a href tag */
    public DSAMEHrefTag() {
        super();
    }
    
    /**
     * reset tag
     */
    public void reset() {
        super.reset();
        bodyContent = null;
        buffer = null;
        displayed = false;
        abortedException = null;
    }
    
    /**
     * performs start tag
     *
     * @return EVAL_BODY_BUFFERED always
     * @throws JspException if request context is null
     */
    public int doStartTag()
    throws JspException {
        reset();
        
        try {
            if (fireBeginDisplayEvent()) {
                
                ViewBean viewBean = getParentViewBean();
                AuthViewBeanBase dsameVB = (AuthViewBeanBase) viewBean;
                String value = (String)viewBean.getDisplayFieldValue(getName());
                /*if (debug.messageEnabled()) {
                 *  debug.message("URL value is : " + value);
                 *}
                 */ 
                buffer = new NonSyncStringBuffer("<a href=\"");
                String pgEncoding =
                (String) dsameVB.getDisplayFieldValue(
                AuthViewBeanBase.PAGE_ENCODING);
                
                int paramIdx = value.indexOf('?');
                if (paramIdx != -1) {
                    buffer.append(value.substring(0,paramIdx+1));
                    String qStr = value.substring(paramIdx+1);
                    // parameter values followed by URL must be encoded
                    if ((qStr != null) && qStr.length() != 0) {
                        encodeValues(buffer,pgEncoding, qStr);
                        String tmpStr = buffer.toString();
                        if (!tmpStr.endsWith("&amp;") && !tmpStr.endsWith("?")){
                            buffer.append("&amp;");
                        }
                    }
                } else {
                    buffer.append(value);
                    buffer.append('?');
                }
                
                if (getAnchor()!=null) {
                    buffer.append("#").append(getAnchor());
                }
                buffer.append(AuthViewBeanBase.PAGE_ENCODING)
                .append('=')
                .append(dsameVB.getDisplayFieldValue(
                AuthViewBeanBase.PAGE_ENCODING));
                // buffer.append ("&val="+getName()+"&val2="+value);
                // Append the Query String NVP's that might have been added
                // as JSP tag attributes
                appendQueryParams(buffer);
                
                if (getTarget() != null) {
                    buffer.append(" target=\"")
                    .append(getTarget())
                    .append("\"");
                }
                
                if (getTitle() != null) {
                    buffer.append(" title=\"")
                    .append(getTitle())
                    .append("\"");
                }
                
                // Append the additional "standard" attributes
                appendCommonHtmlAttributes(buffer);
                appendJavaScriptAttributes(buffer);
                appendStyleAttributes(buffer);
                
                buffer.append("\"");
                
                buffer.append(">");
                displayed = true;
            } else {
                displayed = false;
            }
        } catch (CompleteRequestException e) {
            // CompleteRequestException tunneling workaround:
            // Workaround to allow developers to stop the request
            // from a display event by throwing a CompleteRequestException.
            // The problem is that some containers catch this exception in
            // their JSP rendering subsystem, and so we need to tunnel it
            // through for the developer.
            // Save the exception here to rethrow later (in doEndTag)
            abortedException = e;
            return SKIP_BODY;
        }
        
        if (displayed) {
            return EVAL_BODY_BUFFERED;
        } else {
            return SKIP_BODY;
        }
    }
    
    private void encodeValues( NonSyncStringBuffer buffer, String encoding,
    String qStr) {
        int idx = 0;
        int lastProcessedIdx = 0;
        char[] carr = null ;
        try {
            if (debug.messageEnabled()) {
                debug.message("in encodeValue");
                debug.message("buffer is : " + buffer);
                debug.message("encoding.. : " + encoding);
                //debug.message("query string : " + qStr);
            }
            // If the encoded value has gx_charset , we need to remove it Since 
            // it may not have the correct value.
            if (qStr != null) {
                StringTokenizer st = new StringTokenizer(qStr,"&");
                while (st.hasMoreTokens()) {
                    String keyValuePair = (String)st.nextToken();
                    int valueIdx = keyValuePair.indexOf('=');
                    if (valueIdx != -1) {
                        String key = keyValuePair.substring(0,valueIdx);
                        if (!key.equals("gx_charset")) {
                            String keyValue =keyValuePair.substring(valueIdx+1);
                            String hrefName = key;
                            buffer.append(hrefName).append("=");
                            buffer.append(keyValue);
                            buffer.append("&amp;");
                        }  // end if
                    } // end if (valueIdx != -1)
                }  // end while
            }
            /*if (debug.messageEnabled()) {
             *  debug.message("Final buffer is : " + buffer);
             *}
             */
        } catch (ArrayIndexOutOfBoundsException ex) {
            return;
        } catch (Exception e) {
            debug.error("Error : " +e.getMessage());
            return;
        }
    }
    
    
    /**
     * does nothing here
     */
    public void doInitBody()
    throws JspException {
    }
    
    /**
     * does nothing here
     */
    public int doAfterBody()
    throws JspException {
        return SKIP_BODY;
    }
    
    /**
     * does end tag
     *
     * @return SKIP_PAGE if tag is not going to be displayed
     */
    public int doEndTag()
    throws JspException {
        try {
            // If the display was aborted during the beginning of the process by
            // a CompleteRequestException, we retrhow that exception here,
            // because this is the only location we can safely return SKIP_PAGE
            
            if (abortedException != null) {
                throw abortedException;
            }
            
            if (displayed) {
                BodyContent bodyContent = getBodyContent();
                
                if (bodyContent != null) {
                    // Assume that "true" is default for trim
                    if (getTrim() == null || isTrue(getTrim())) {
                        buffer.append(bodyContent.getString().trim());
                    } else {
                        buffer.append(bodyContent.getString());
                    }
                }
                
                buffer.append("</a>");
                writeOutput(fireEndDisplayEvent(buffer.toString()));
            }
        } catch (CompleteRequestException e) {
            // CompleteRequestException tunneling workaround:
            // Workaround to allow developers to stop the request
            // from a display event by throwing a CompleteRequestException.
            // The problem is that some containers catch this exception in
            // their JSP rendering subsystem, and so we need to tunnel it
            // through for the developer.
            
            // Mark the JSP rendering as cancelled.  The calling
            // ViewBean.foward() or ViewBean.include() methods
            // should pick this up and then throw a complete request
            // exception that was properly thrown here.
            getRequestContext().getRequest().setAttribute(
            ViewBeanBase.DISPLAY_EVENT_COMPLETED_REQUEST_ATTRIBUTE_NAME, e);
            return SKIP_PAGE;
        }
        
        return EVAL_PAGE;
    }
    
    public BodyContent getBodyContent() {
        return bodyContent;
    }
    
    public void setBodyContent(BodyContent value) {
        bodyContent = value;
    }
    
    /**
     * Appends the Query String name/value pairs (NVP) that have been supplied
     * via the JSP tag attribute "queryParams".
     * NOTE - this tag assumes that the JSP author has URL encoded the value
     * portions of the name value pairs where needed.
     * This tag also prepends an ampersand '&' before the first NVP, and assumes
     * that the JSP author has provided the '&' delimiters between the remaining
     * NVPs.
     *
     */
    protected void appendQueryParams(NonSyncStringBuffer buffer)
    throws JspException {
        String nvPairs = getQueryParams();
        
        if ((nvPairs != null) && (nvPairs.length() > 0)) {
            buffer.append("&amp;");
            buffer.append(nvPairs);
        }
    }
    
    
    
    
    /**
     * takes an arbitrarily deep namePath (e.g. "Page1.Repeated2.Foo") which
     * describes the containment relationship of a given display field to
     * its container views, and returns a reference to the display field itself.
     *
     * @param namePath - An arbitrarily deep namePath (e.g.
     * "Page1.Repeated2.Foo") which describes the containment relationship of
     * a display field to its container views
     * @return requested DisplayField
     */
    private DisplayField getDisplayField(String[] namePath)
    throws JspException {
        // We can assume that the source class must be the parent view bean !!!
        // The same is not true for the target though.
        ContainerView parentView=getParentViewBean();
        
        /*
         * Descend through view hierarchy until you get to the direct parent
         * field the direct parent may be a tiled view, arbitrarily nested
         * start count at one,
         */
        for (int j = 1; j < namePath.length-1; j++) {
            parentView = (ContainerView)parentView.getChild(namePath[j]);
        }
        
        return (DisplayField) parentView.getChild(namePath[namePath.length-1]);
    }
    
    public String getTarget() {
        return (String)getValue("target");
    }
    
    public void setTarget(String value) {
        setValue("target",value);
    }
    
    public String getTitle() {
        return (String)getValue("title");
    }
    
    public void setTitle(String value) {
        setValue("title",value);
    }
    
    public String getAnchor() {
        return (String)getValue("anchor");
    }
    
    public void setAnchor(String value) {
        setValue("anchor",value);
    }
    
    public String getQueryParams() {
        return (String)getValue("queryParams");
    }
    
    public void setQueryParams(String value) {
        setValue("queryParams",value);
    }
    
    public String getTrim() {
        return (String)getValue("trim");
    }
    
    public void setTrim(String value) {
        setValue("trim",value);
    }
}
