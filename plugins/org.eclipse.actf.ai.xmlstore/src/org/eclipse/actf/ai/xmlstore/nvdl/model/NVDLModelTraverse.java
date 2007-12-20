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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The <code>NVDLModelTraverse</code> is a utility for traversing
 * all reachable models in a depth-first manner.
 */
public class NVDLModelTraverse implements NVDLModelVisitor {
    private Set<NVDLModel> traversed = new HashSet<NVDLModel>();

    private boolean checkTraversedInternal(NVDLModel m) {
        if (traversed.contains(m)) return true;
        traversed.add(m);
        return false;
    }

    protected boolean checkTraversed(NVDLModel m) {
        if (traversed.contains(m)) return true;
        return false;
    }
    
    public void traverse(NVDLModel m) throws NVDLModelException {
    	traversed.clear();
        m.visitModel(this);
    }

    private NVDLModel visit(NVDLModel m) throws NVDLModelException {
    	if (m == null) return null;
        return m.visitModel(this);
    }

    public NVDLModel visitNVDLMode(NVDLMode mode) throws NVDLModelException {
        if (checkTraversedInternal(mode)) return null;
        NVDLRule rule;
        rule = mode.getAnyNamespaceRuleForAttribute();
        if (rule != null) visit(rule);
        rule = mode.getAnyNamespaceRuleForElement();
        if (rule != null) visit(rule);
        Iterator it = mode.notAnyNamespaceRuleIterator();
        while (it.hasNext()) {
            rule = (NVDLRule) it.next();
            visit(rule);
        }
        return null;
    }

    private void traverseAction(NVDLAction a) throws NVDLModelException {
        visit(a.getUseMode());
        List contexts = a.getContextsList();
        Iterator it = contexts.iterator();
        while (it.hasNext()) {
            NVDLAction.Context c = (NVDLAction.Context) it.next();
            visit(c.useMode);
        }
    }

    public NVDLModel visitNVDLNoResultAction(NVDLNoResultAction action) throws NVDLModelException {
        if (checkTraversedInternal(action)) return null;
        traverseAction(action);
        return null;
    }

    public NVDLModel visitNVDLResultAction(NVDLResultAction action) throws NVDLModelException {
        if (checkTraversedInternal(action)) return null;
        traverseAction(action);
        return null;
    }

    public NVDLModel visitNVDLRule(NVDLRule rule) throws NVDLModelException {
        if (checkTraversedInternal(rule)) return null;
        NVDLActionManager am = rule.getActionManager();
        NVDLResultAction ra = am.getResultAction();
        if (ra != null) visit(ra);
        List nras = am.getNoResultActions();
        Iterator it = nras.iterator();
        while (it.hasNext()) {
            NVDLNoResultAction nra = (NVDLNoResultAction) it.next();
            visit(nra);
        }
        return null;
    }

    public NVDLModel visitNVDLRules(NVDLRules rules) throws NVDLModelException {
        if (checkTraversedInternal(rules)) return null;
        visit(rules.getStartMode());
        return null;
    }
}
