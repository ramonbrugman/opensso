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
 * $Id: NewStaticGroupViewBean.java,v 1.2 2008-06-25 05:42:54 qcheng Exp $
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
import com.sun.identity.console.dm.model.GroupModel;
import com.sun.identity.console.dm.model.GroupModelImpl;
import com.sun.identity.console.dm.model.DMConstants;
import com.sun.identity.console.realm.RMRealmOpViewBeanBase;
import com.sun.web.ui.model.CCPageTitleModel;
import com.sun.web.ui.view.alert.CCAlert;
import com.sun.web.ui.view.pagetitle.CCPageTitle;
import java.util.Map;
import java.util.HashMap;

public class NewStaticGroupViewBean
    extends RMRealmOpViewBeanBase
{
    public static final String DEFAULT_DISPLAY_URL =
	"/console/dm/NewStaticGroup.jsp";

    private static final String PGTITLE_TWO_BTNS = "pgtitleTwoBtns";
    private static final String PROPERTY_ATTRIBUTE = "propertyAttributes";

    private AMPropertySheetModel propertySheetModel;
    private GroupModel model = null;

    public NewStaticGroupViewBean() {
	super("NewStaticGroup");
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
	GroupModel model = (GroupModel)getModel();
	propertySheetModel = new AMPropertySheetModel(
	    model.getCreateStaticGroupXML());
	propertySheetModel.clear();
    }

    protected AMModel getModelInternal() {
        RequestContext rc = RequestManager.getRequestContext();
        return new GroupModelImpl(rc.getRequest(), getPageSessionAttributes());
    }

    /**
     * Handles cancel request.
     *
     * @param event Request invocation event
     */
    public void handleButton2Request(RequestInvocationEvent event) {
        forwardToGroupView(event);
    }

    /**
     * Handles create realm request.
     *
     * @param event Request invocation event
     */
    public void handleButton1Request(RequestInvocationEvent event)
	throws ModelControlException
    {
	GroupModel model = (GroupModel)getModel();
	String location = (String)getPageSessionAttribute(
	    AMAdminConstants.CURRENT_ORG);
        if (location == null || location.length() == 0) {
            location = model.getStartDSDN();
	}

	try {
	    AMPropertySheet ps = 
	        (AMPropertySheet)getChild(PROPERTY_ATTRIBUTE);
	    Map values = ps.getAttributeValues(model.getDataMap(
                DMConstants.SUB_SCHEMA_GROUP), false, model);
            if (values == null) {
                values = new HashMap();
            }
	    // add the type of group being created. It will either be
	    // group type is either Static or Assignable Dynamic
            values.put(GroupModel.GROUP_TYPE, model.getManagedGroupType());

	    model.createGroup(location, values);
	    forwardToGroupView(event);
        } catch (AMConsoleException e) {
	    setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
	        e.getMessage());
	    forwardTo();
	}
    }

    private void forwardToGroupView(RequestInvocationEvent event){
        GroupViewBean vb = (GroupViewBean)getViewBean(GroupViewBean.class);
	backTrail();
        passPgSessionMap(vb);
        vb.forwardTo(getRequestContext());
    }

    protected String getBreadCrumbDisplayName() {
	return "breadcrumbs.directorymanager.group.add";
    }

    protected boolean startPageTrail() {
	return false;
    }
}
