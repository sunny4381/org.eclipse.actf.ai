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

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>NVDLRules</code> is a model class for NVDL `rules'.
 */
public class NVDLRules extends NVDLModel {
    private final String schemaType;
    public String getSchmaType() {
        return schemaType;
    }

    private final String startModeName;
    public String getStartModeName() {
        return startModeName;
    }
    private NVDLMode startMode;
    public NVDLMode getStartMode() {
        return startMode;
    }
    public void setStartMode(NVDLMode mode) {
        this.startMode = mode;
    }

    // Trigger

    private NVDLTriggerManager triggerMan = new NVDLTriggerManager();
    public NVDLTriggerManager getTriggerManager() {
        return triggerMan;
    }

    // Mode
    private Map<String, NVDLMode> modes = new HashMap<String, NVDLMode>();

    public NVDLMode getMode(String name) {
        return modes.get(name);
    }
    public void putMode(NVDLMode mode) {
        modes.put(mode.name, mode);
    }

    // Action
    private HashMap<String, NVDLAction> actionMap = new HashMap<String, NVDLAction>();
    public NVDLAction getAction(String name) {
        return actionMap.get(name);
    }
    public void putAction(NVDLAction action) {
        actionMap.put(action.getName(), action);
    }
    
    // Visitor Handler
    public NVDLModel visitModel(NVDLModelVisitor v) throws NVDLModelException {
        return v.visitNVDLRules(this);
    }

    public NVDLRules(String schemaType, String startModeName) {
        this.schemaType = schemaType;
        this.startModeName = startModeName;
    }

}
