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

package org.eclipse.actf.ai.xmlstore.nvdl.util;

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLMode;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModel;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModelException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModelTraverse;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLNoResultAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLResultAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRule;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;

/**
 * The <code>NVDLModelPrint</code> is a utility class for dumping
 * NVDL models by traversing them.
 */
public class NVDLModelPrint extends NVDLModelTraverse {
    private void println(Object o) {
        System.out.println(o);
    }

    public NVDLModel visitNVDLMode(NVDLMode mode) throws NVDLModelException {
        println("Mode " + mode.name + " " + mode.getLocation() + ":");
        return super.visitNVDLMode(mode);
    }

    private void printNVDLAction(NVDLAction action) {
        StringBuffer r = new StringBuffer(action.toString());
        if (action.getMessage() != null) {
            r.append("Message:");
            r.append(action.getMessage().toString());
        }
        r.append("useMode:");
        if (action.getUseModeName() != null) {
            r.append(action.getUseModeName());
        }
        println(r);
    }

    public NVDLModel visitNVDLNoResultAction(NVDLNoResultAction action)
        throws NVDLModelException {
        printNVDLAction(action);
        return super.visitNVDLNoResultAction(action);
    }
    public NVDLModel visitNVDLResultAction(NVDLResultAction action)
        throws NVDLModelException {
        printNVDLAction(action);
        return super.visitNVDLResultAction(action);
    }

    public NVDLModel visitNVDLRule(NVDLRule rule)
        throws NVDLModelException {
        println(rule);
        return super.visitNVDLRule(rule);
    }

    public NVDLModel visitNVDLRules(NVDLRules rules) throws NVDLModelException {
        println("Rules " + rules.getLocation() + ":");
        println("  SchemaType:" + rules.getSchmaType());
        println("  StartMode:"); println(rules.getStartMode().name);
        println(rules.getTriggerManager());
        println("  Defined modes:");
        return super.visitNVDLRules(rules);
    }

    public static void printRules(NVDLRules r) {
        NVDLModelPrint v = new NVDLModelPrint();
        try {
            r.visitModel(v);
        } catch (NVDLModelException e) {
            System.out.println("Received NVDLModelException:" + e);
            e.printStackTrace();
        }
    }
}
