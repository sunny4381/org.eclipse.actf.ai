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

package org.eclipse.actf.ai.xmlstore.nvdl.model;

/**
 * The <code>NVDLElement</code> represents an XML element.
 * This class also holds an element section index that identifies the
 @ belonging element section.
 */
public class NVDLElement {
    public final String ns;
    public final String localName;
    public final NVDLElement parent;
    public final int sectionIdx;

    private final boolean isSectionHead;

    public boolean isSectionHead() {
        return isSectionHead;
    }

    private boolean initSectionHeadFlag(NVDLTriggerManager triggerManager) {
        if (parent == null) return true;
        if (!(ns.equals(parent.ns))) return true;
        if (triggerManager == null) return false;
        return triggerManager.match(this, parent);
    }


    public NVDLElement(String ns, String localName, NVDLElement parent,
                       NVDLTriggerManager triggerManager, int nextSectionIdx) {
        this.ns = ns;
        this.localName = localName;
        this.parent = parent;

        this.isSectionHead = initSectionHeadFlag(triggerManager);
        if (isSectionHead()) {
            this.sectionIdx = nextSectionIdx;
        } else {
            this.sectionIdx = parent.sectionIdx;
        }
    }
}

