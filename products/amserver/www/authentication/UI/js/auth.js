/*
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
 * $Id: auth.js,v 1.6 2008-06-25 05:44:32 qcheng Exp $
 *
 */


/** makes current page occupies entire browser window */
function occupyFullBrowser() {
    if (top.location != window.location) {
        top.location = window.location;
    }
}

/** places cursor on the first form element */
function placeCursorOnFirstElm() {
    var frms = document.forms;
    var frmCount = frms.length;

    for (var i = 0; i < frmCount; i++) {
        var frm = frms[i];
        var sz = frm.elements.length;

        for (var j = 0; j < sz; j++) {
            var elm = frm.elements[j];

            if (elm.type != "hidden") {
                elm.focus();
                return;
            }
        }
    }
}

/** 
 * writes the corresponding css based on browser type 
 *
 * @param serviceUri
 *
 */
function writeCSS(serviceUri) {
    document.write("<link href='" + serviceUri);

    if (is_ie6up) {
        // IE 6.x or above.
        document.write("/css/css_ie6up.css");
    } else if (is_ie5up) {
        // IE 5.x or above.
        document.write("/css/css_ie5win.css");
    } else if (is_gecko) {
        // Netscape 6/7, Mozilla
        document.write("/css/css_ns6up.css");
    } else if (is_nav4 && is_win) {
        // Netscape 4 Windows.
        document.write("/css/css_ns4win.css");
    } else if (is_nav4) {
        // Netscape 4 Solaris & Linux.
        document.write("/css/css_ns4sol.css");
    } else {
        // All others
        document.write("/css/css_ns6up.css");
    }

    document.write("' type='text/css' rel='stylesheet' />");
}

/**
 * marks button
 *
 * @param label of button
 * @param href of button
 */
function markupButton(label, href) {
    label = "&nbsp;" + strTrim(label) + "&nbsp;";
    document.write("<td>");
    document.write("<div class=\"logBtn\">");
    document.write("<input name=\"Login.Submit\" type=\"button\"");
    document.write(" class=\"Btn1Def\" value=\"");
    document.write(label);
    document.write("\" onclick=\"");
    document.write(href);
    document.write("\" onmouseover=\"javascript: if (this.disabled==0) this.className='Btn1DefHov'\"");
    document.write(" onmouseout=\"javascript: if (this.disabled==0) this.className='Btn1Def'\"");
    document.write(" onblur=\"javascript: if (this.disabled==0) this.className='Btn1Def'\"");
    document.write(" onfocus=\"javascript: if (this.disabled==0) this.className='Btn1DefHov'\"");
    document.write("/></div></td>");
}

/**
 * aggregrates all the form elements in different forms into
 * a hidden form
 */
function aggSubmit() {
    var frms = document.forms;
    var hiddenFrm = frms['Login'];

    if (hiddenFrm != null) {
        for (var i = 0; i < elmCount; i++) {
            var frm = frms['frm' + i];

            if (frm != null) {
                var elm = frm.elements[0];

                if (elm != null) {
                    if (elm.type == 'radio') {
                        hiddenFrm.elements[i].value =
                            getSelectedRadioValue(frm);
                    } else if (elm.type == 'checkbox') {
                        hiddenFrm.elements[i].value = 
                            getSelectedCheckBoxValues(frm);
                    } else {
                        hiddenFrm.elements[i].value = elm.value;
                    }
                }
            }
        }
    }
}

/**
 * gets selected radio value
 *
 * @param frmObj - form object
 */
function getSelectedRadioValue(frmObj) {
    for (var i = 0; i < frmObj.elements.length; i++) {
        var elm = frmObj.elements[i];

        if (elm.checked) {
            return elm.value;
        }
    }
    return "";
}

/**
 * gets selected check box values separated by "|"
 *
 * @param frmObj - form object
 */
function getSelectedCheckBoxValues(frmObj) {
    var checked = "";
    for (var i = 0; i < frmObj.elements.length; i++) {
        var elm = frmObj.elements[i];

        if ((elm.checked) && (elm.type == 'checkbox')) {
            checked = checked + elm.value + "|";
        }
    }
    return checked;
}

/**
 * trims leading and trailing spaces of a string
 *
 * @param str - string to trim
 * @return trimmed string
 */
function strTrim(str){
    return str.replace(/^\s+/,'').replace(/\s+$/,'')
}

/**
 * clears all form elements
 *
 * @param frm - form obj
 */
function clearFormElms(frm) {
    if (frm != null) {
        var elms = frm.elements;

        if ((elms != null) && (elms.length > 0)) {
            for (var i = 0; i < elms.length; i++) {
                var elm = elms[i];
                elm.value = "";
            }
        }
    }
}
