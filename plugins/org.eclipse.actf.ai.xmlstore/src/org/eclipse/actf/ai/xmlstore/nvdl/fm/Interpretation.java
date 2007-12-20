/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.xmlstore.nvdl.fm;

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLElement;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLMode;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLNoResultAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLResultAction;

public abstract class Interpretation {
    private NVDLElement element;

    // private slot --------------------------------------------------------------------------------
    private Object slot;

    public void setSlot(Object slot) {
        this.slot = slot;
    }
    public Object getSlot() {
        return slot;
    }
    // defined prefix ------------------------------------------------------------------------------
    private String prefix;
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getPrefix() {
        return prefix;
    }


    private final NVDLAction action;
    private final NVDLMode mode;
    
    private final boolean attach, unwrap;
    private final boolean attachPlaceHolder;

    public boolean isSectionHead() {
        return (action != null);
    }

    public boolean isDispatch() {
        return (action instanceof NVDLNoResultAction);
    }

    public boolean isAttach() {
        return attach;
    }

    public boolean isUnwrap() {
        return unwrap;
    }

    public boolean isAttachPlaceHolder() {
        return attachPlaceHolder;
    }
    
    public NVDLMode getMode() {
        return mode;
    }

    public NVDLAction getAction() {
        return action;
    }

    public NVDLElement getElement() {
        return element;
    }

    public abstract Interpretation getParent();

    private String id;

    public String getID() {
        if (id == null) {
            this.id = generateID();
        }
        return id;
    }

    private String generateID() {
        StringBuffer buf = new StringBuffer();

        for (Interpretation ip = this; ip != null; ip = ip.getParent()) {
            if (!ip.isSectionHead()) continue;
            if (buf.length() > 0) buf.insert(0, " ");
            String name = ip.action.getName();
            buf.insert(0, name);
        }
        return buf.toString();
    }

    public Interpretation getSectionHeadInterpretation() {
        Interpretation ip;
        for (ip = this; ; ip = ip.getParent()) {
            if (ip == null) return null;
            if (ip.isSectionHead()) break;
        }
        return ip;
    }

    public Interpretation getEffectiveInterpretation() {
        Interpretation ip = getSectionHeadInterpretation();
        if (ip == null) return null;
        if (ip.isUnwrap()) return null;
        if (ip.isDispatch()) return ip;
        for (ip = ip.getParent(); ip != null; ip = ip.getParent()) {
            if (ip.isDispatch()) return ip;
            if (ip.isAttachPlaceHolder()) return null;
        }
        return null;
    }

    Interpretation(NVDLMode mode, NVDLAction action,
                   NVDLElement element) {
        this.mode = mode;
        this.action = action;
        this.element = element;
        if (action instanceof NVDLResultAction) {
            NVDLResultAction a = (NVDLResultAction) action;
            switch (a.type) {
            case NVDLResultAction.TYPE_UNWRAP:
                this.attach = false;
                this.unwrap = true;
                this.attachPlaceHolder = false;
                break;
            case NVDLResultAction.TYPE_ATTACHPLACEHOLDER:
                this.attach = false;
                this.attachPlaceHolder = true;
                this.unwrap = false;
                break;
            case NVDLResultAction.TYPE_ATTACH:
                this.attach = true;
                this.unwrap = false;
                this.attachPlaceHolder = false;
                break;
            default:
                this.attach = false;
                this.unwrap = false;
                this.attachPlaceHolder = false;
            }
        } else {
            this.attach = false;
            this.unwrap = false;
            this.attachPlaceHolder = false;
        }
    }
}

