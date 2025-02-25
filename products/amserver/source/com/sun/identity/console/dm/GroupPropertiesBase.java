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
 * $Id: GroupPropertiesBase.java,v 1.2 2008-06-25 05:42:54 qcheng Exp $
 *
 */

package com.sun.identity.console.dm;

import com.iplanet.jato.view.event.RequestInvocationEvent;
import com.sun.identity.console.base.AMViewBeanBase;
import com.sun.identity.console.base.AMViewConfig;
import com.sun.identity.console.base.model.AMFormatUtils;
import com.sun.identity.console.base.model.AMAdminConstants;
import com.sun.identity.console.base.model.AMConsoleException;
import com.sun.identity.console.dm.model.GroupModel;
import com.sun.web.ui.model.CCPageTitleModel;
import com.sun.web.ui.view.tabs.CCTabs;
import java.text.MessageFormat;

public  abstract class GroupPropertiesBase 
    extends GeneralPropertiesBase
{
    private static final String TAB = "groups";

    public CCPageTitleModel ptModel = null;
    public GroupModel model = null;

    public GroupPropertiesBase(String name) {
	super(name);
    }

    protected void registerChildren() {
	registerChild(TAB_COMMON, CCTabs.class);
	super.registerChildren();
    }
 
    /**
     * Handles tab selected event. 
     *
     * @param event Request Invocation Event.
     * @param nodeID Selected Node ID.
     */
    public void nodeClicked(RequestInvocationEvent event, int nodeID) {
        try {
	    AMViewBeanBase vb = getTabNodeAssociatedViewBean(TAB, nodeID);

	    String tmp = (String)getPageSessionAttribute(
		AMAdminConstants.CURRENT_ORG);
	    vb.setPageSessionAttribute(AMAdminConstants.CURRENT_ORG, tmp);
	    
	    tmp = (String)getPageSessionAttribute(
		"AMAdminConstants.CURRENT_GROUP");
	    vb.setPageSessionAttribute("AMAdminConstants.CURRENT_GROUP", tmp);
            
            tmp = (String)getPageSessionAttribute(
                AMAdminConstants.CURRENT_PROFILE);
	    vb.setPageSessionAttribute(
                AMAdminConstants.CURRENT_PROFILE, tmp);

	    tmp = (String)getPageSessionAttribute(
		AMAdminConstants.PREVIOUS_TAB_ID);
	    vb.setPageSessionAttribute(AMAdminConstants.PREVIOUS_TAB_ID, tmp);
	    unlockPageTrailForSwapping();
	    passPgSessionMap(vb);
            vb.forwardTo(getRequestContext());
        } catch (AMConsoleException e) {            
            forwardTo();
        }
    }

    protected void createTabModel() {
        AMViewConfig amconfig = AMViewConfig.getInstance();
	String realmName = (String)getPageSessionAttribute(
	    AMAdminConstants.CURRENT_PROFILE);
        tabModel = amconfig.getTabsModel(TAB, realmName,
	    getRequestContext().getRequest());
    }

    protected void forwardToGroupView(RequestInvocationEvent event) {   
        // reset the current org to be the org that was being viewed before
        // the group profile page was opened.
	String tmp = 
	    (String)getPageSessionAttribute(AMAdminConstants.CURRENT_ORG);
	setPageSessionAttribute(AMAdminConstants.CURRENT_ORG, tmp);

        // reset the tab selected to the realm view
	tmp = (String)getPageSessionAttribute(
	    AMAdminConstants.PREVIOUS_TAB_ID);
	setPageSessionAttribute(getTrackingTabIDName(), tmp);

        // and now forward on to the group page...
        GroupViewBean vb = (GroupViewBean)getViewBean(
            GroupViewBean.class);
        backTrail();
	passPgSessionMap(vb);
        vb.forwardTo(getRequestContext());
    }

    public void setPageTitle(String title) {
        model = (GroupModel)getModel();
        String[] tmp = {getDisplayName()};
        ptModel.setPageTitleText(MessageFormat.format(
	    model.getLocalizedString(title), tmp));
    }

    private String getDisplayName() {
        String name =  (String)getPageSessionAttribute(
            AMAdminConstants.CURRENT_PROFILE);
        model = (GroupModel)getModel();
        if (name == null || name.length() == 0) {
            name = model.getStartDSDN();
        }
        return AMFormatUtils.DNToName(model, name);
    }

    protected String getBreadCrumbDisplayName() {
        model = (GroupModel)getModel();
	String[] arg = {getDisplayName()};
	return MessageFormat.format(model.getLocalizedString(
	    "breadcrumbs.directorymanager.group.edit"), arg);
    }

    /**
     * Creates the label used to take the user back to the previous page
     * they were viewing. The label is defined in the property file as
     * "Back to {0}" which lets us control the format of the label in
     * different locales.  The name of the current object is substituted.
     */
    protected String getBackButtonLabel() {
        return getBackButtonLabel("breadcrumbs.directorymanager.group");
    }

    protected boolean startPageTrail() {
      return false;
    }
}
