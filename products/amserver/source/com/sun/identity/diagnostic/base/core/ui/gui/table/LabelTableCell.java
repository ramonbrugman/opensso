/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: LabelTableCell.java,v 1.1 2008-11-22 02:19:57 ak138937 Exp $
 *
 */

package com.sun.identity.diagnostic.base.core.ui.gui.table;

import java.awt.Color;
import javax.swing.ImageIcon;

public abstract class LabelTableCell {
    
    protected String text;  
    protected ImageIcon icon;
    protected Color color;
    
    /** Creates a new instance of LabelTableCell */
    public LabelTableCell() {
        this.color = Color.BLACK;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    
    public void setColor(Color c) {
        this.color = c;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
    
    public ImageIcon getIcon() {
        return icon;
    }
    
}
