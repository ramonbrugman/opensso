/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: NewOrganizationViewBean.java,v 1.2 2008-06-25 05:42:54 qcheng Exp $
 *
 */

package com.sun.identity.console.dm;

import com.iplanet.jato.RequestContext;
import com.iplanet.jato.RequestManager;
import com.iplanet.jato.model.ModelControlException;
import com.iplanet.jato.view.View;
import com.iplanet.jato.view.event.DisplayEvent;
import com.iplanet.jato.view.event.RequestInvocationEvent;
import com.sun.identity.console.base.AMPropertySheet;
import com.sun.identity.console.base.model.AMPropertySheetModel;
import com.sun.identity.console.base.model.AMAdminConstants;
import com.sun.identity.console.base.model.AMModel;
import com.sun.identity.console.base.model.AMConsoleException;
import com.sun.identity.console.dm.model.DMConstants;
import com.sun.identity.console.dm.model.UMOrganizationModel;
import com.sun.identity.console.dm.model.UMOrganizationModelImpl;
import com.sun.identity.console.realm.RMRealmOpViewBeanBase;
import com.sun.web.ui.model.CCPageTitleModel;
import com.sun.web.ui.view.alert.CCAlert;
import com.sun.web.ui.view.pagetitle.CCPageTitle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NewOrganizationViewBean
    extends RMRealmOpViewBeanBase
{
    public static final String DEFAULT_DISPLAY_URL =
	"/console/dm/NewOrganization.jsp";

    private static final String PGTITLE_TWO_BTNS = "pgtitleTwoBtns";
    private static final String PROPERTY_ATTRIBUTE = "propertyAttributes";
    private static final String NAME = "tfName";

    private AMPropertySheetModel propertySheetModel;
    private UMOrganizationModel model = null;

    /**
     * Creates a realm creation view bean.
     */
    public NewOrganizationViewBean() {
	super("NewOrganization");
	setDefaultDisplayURL(DEFAULT_DISPLAY_URL);
	createPageTitleModel();
	createPropertyModel();
	registerChildren();
    }

    protected void registerChildren() {
	super.registerChildren();
	registerChild(PGTITLE_TWO_BTNS, CCPageTitle.class);
	registerChild(PROPERTY_ATTRIBUTE, AMPropertySheet.class);
	propertySheetModel.registerChildren(this);
    }

    protected View createChild(String name) {
	View view = null;

	if (name.equals(PGTITLE_TWO_BTNS)) {
	    view = new CCPageTitle(this, ptModel, name);
	} else if (name.equals(PROPERTY_ATTRIBUTE)) {
	    view = new AMPropertySheet(this, propertySheetModel, name);
	} else if (propertySheetModel.isChildSupported(name)) {
	    view = propertySheetModel.createChild(this, name);
	} else {
	    view = super.createChild(name);
	}

	return view;
    }

    public void beginDisplay(DisplayEvent event)
	throws ModelControlException
    {
        AMPropertySheet ps = (AMPropertySheet)getChild(PROPERTY_ATTRIBUTE);
        ps.init();

        // set the default organization status to active
        Set values = new HashSet(2);
        values.add(DMConstants.ACTIVE);
        Map attributes = new HashMap(2);
        attributes.put(DMConstants.ORGANIZATION_STATUS, values);
        ps.setAttributeValues(attributes, getModel());

	super.beginDisplay(event);
    }

    private void createPageTitleModel() {
	ptModel = new CCPageTitleModel(
	    getClass().getClassLoader().getResourceAsStream(
		"com/sun/identity/console/twoBtnsPageTitle.xml"));
	ptModel.setValue("button1", "button.ok");
	ptModel.setValue("button2", "button.cancel");
    }

    private void createPropertyModel() {
	try {
	    UMOrganizationModel model = (UMOrganizationModel)getModel();
	    propertySheetModel = new AMPropertySheetModel(
		model.getCreateOrganizationPropertyXML());
	    propertySheetModel.clear();
	} catch (AMConsoleException e) {
	    setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
		e.getMessage());
	}
    }

    protected AMModel getModelInternal() {
        if (model == null) {
            RequestContext rc = RequestManager.getRequestContext();
            model = new UMOrganizationModelImpl(
                rc.getRequest(), getPageSessionAttributes());
        }
        return model;
    }

    /**
     * Handles cancel request.
     *
     * @param event Request invocation event
     */
    public void handleButton2Request(RequestInvocationEvent event) {
        forwardToOrgView(event);
    }

    /**
     * Handles create realm request.
     *
     * @param event Request invocation event
     */
    public void handleButton1Request(RequestInvocationEvent event)
	throws ModelControlException
    {
	UMOrganizationModel model = (UMOrganizationModel)getModel();
	AMPropertySheet ps = (AMPropertySheet)getChild(PROPERTY_ATTRIBUTE);
	String location = (String)getPageSessionAttribute(
	    AMAdminConstants.CURRENT_ORG);

	String name = (String)getDisplayFieldValue(NAME);
	name = name.trim();

	if (name.length() > 0) {
	    try {
		Map values = ps.getAttributeValues(
		    model.getDataMap(), false, model);
		model.createOrganization(location, name, values);
	        forwardToOrgView(event);
             } catch (AMConsoleException e) {
		setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
		    e.getMessage());
		forwardTo();
	    }
	} else {
	    setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
		"message.missing.name");
	    forwardTo();
	}
    }

    private void forwardToOrgView(RequestInvocationEvent event){
        OrganizationViewBean vb = (OrganizationViewBean)getViewBean(
            OrganizationViewBean.class);
        backTrail();
	passPgSessionMap(vb);
        vb.forwardTo(getRequestContext());
    }

    protected String getBreadCrumbDisplayName() {
	return "breadcrumbs.directorymanager.organization.add";
    }

    protected boolean startPageTrail() {
	return false;
    }

}
