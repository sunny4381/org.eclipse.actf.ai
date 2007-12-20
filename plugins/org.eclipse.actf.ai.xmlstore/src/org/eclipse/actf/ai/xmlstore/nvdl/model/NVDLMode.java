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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The <code>NVDLMode</code> class is for NVDL mode.
 */
public class NVDLMode extends NVDLModel {
    public final String name;

    private NVDLRule anyNamespaceRuleForAttribute = null;
    private NVDLRule anyNamespaceRuleForElement = null;
    private List<NVDLRule> rules = new ArrayList<NVDLRule>();

    public NVDLRule getAnyNamespaceRuleForAttribute() {
        return anyNamespaceRuleForAttribute;
    }

    public NVDLRule getAnyNamespaceRuleForElement() {
        return anyNamespaceRuleForElement;
    }

    private void addRuleInternal(NVDLRule rule) {
        if (rule.isAnyNamespace()) {
            if (rule.isTargetElement()) {
                assert anyNamespaceRuleForElement == null;
                anyNamespaceRuleForElement = rule;
            }
            if (rule.isTargetAttribute()) {
                assert anyNamespaceRuleForAttribute == null;
                anyNamespaceRuleForAttribute = rule;
            }
        } else {
            rules.add(rule);
        }
    }

    private NVDLRule getConflictRule(NVDLRule rule) {
        if (rule.isAnyNamespace()) {
            if (rule.isTargetElement()) {
                return anyNamespaceRuleForElement;
            }
            if (rule.isTargetAttribute()) {
                return anyNamespaceRuleForAttribute;
            }
        }
        for (int i = 0; i < rules.size(); i++) {
            NVDLRule r = rules.get(i);
            if (r.isConflicted(rule)) return r;
        }
        return null;
    }

    // Returns a conflicted rule if it exists.
    public NVDLRule addRule(NVDLRule rule) {
        // Simplification 6.4.11
        NVDLRule r = getConflictRule(rule);
        if (r != null) return r;
        addRuleInternal(rule);
        return null;
    }

    public Iterator notAnyNamespaceRuleIterator() {
        return rules.iterator();
    }

    public NVDLRule chooseRule(String ns, boolean element) {
        Iterator it = notAnyNamespaceRuleIterator();
        while (it.hasNext()) {
            NVDLRule rule = (NVDLRule) it.next();
            if (rule.match(ns, element)) {
                return rule;
            }
        }
        if (element) {
            return anyNamespaceRuleForElement;
        } else {
            return anyNamespaceRuleForAttribute;
        }
    }

    public NVDLModel visitModel(NVDLModelVisitor v)
        throws NVDLModelException {
        return v.visitNVDLMode(this);
    }

    public String toString() {
        StringBuffer r = new StringBuffer();
        if (name != null) {
            r.append(name);
            r.append(";");
        }
        Iterator it = notAnyNamespaceRuleIterator();
        while (it.hasNext()) {
            NVDLRule rule = (NVDLRule) it.next();
            r.append(rule.toString());
            r.append(";");
        }
        if (anyNamespaceRuleForElement != null) {
            r.append(anyNamespaceRuleForElement.toString());
            r.append(";");
        }
        if (anyNamespaceRuleForAttribute != null) {
            r.append(anyNamespaceRuleForAttribute.toString());
            r.append(";");
        }
        return r.toString();
    }
    
    public NVDLMode(String name) {
        this.name = name;
    }

    /**********************************************************************
                             Mode Inclusion
     **********************************************************************/

    private List<NVDLMode> includedModes = null;
    private void includeMode(NVDLMode childMode) {
        // Simplification 6.4.10
        if ((childMode.anyNamespaceRuleForAttribute != null)
            && (anyNamespaceRuleForAttribute == null)) {
            addRule(childMode.anyNamespaceRuleForAttribute);
        }
        if ((childMode.anyNamespaceRuleForElement != null)
            && (anyNamespaceRuleForElement == null)) {
            addRule(childMode.anyNamespaceRuleForElement);
        }
        child:
        for (int i = 0; i < childMode.rules.size(); i++) {
            NVDLRule cr = (NVDLRule) childMode.rules.get(i);
            for (int j = 0; j < rules.size(); j++) {
                NVDLRule r = (NVDLRule) rules.get(j);
                if (cr.isOverridden(r)) continue child;
            }
            addRule(cr);
        }
    }
    
    public void addIncludedMode(NVDLMode mode) {
        if (includedModes == null) {
            includedModes = new ArrayList<NVDLMode>(1);
        }
        includedModes.add(mode);
    }
    
    private void removeCancelActionRules() {
        // Simplification 6.4.10
        if (anyNamespaceRuleForAttribute != null) {
            if (anyNamespaceRuleForAttribute.getActionManager()
                .isCancelAction()) {
                anyNamespaceRuleForAttribute = null;
            }
        }
        if (anyNamespaceRuleForElement != null) {
            if (anyNamespaceRuleForElement.getActionManager()
                .isCancelAction()) {
                anyNamespaceRuleForElement = null;
            }
        }
        int i = 0;
        while (i < rules.size()) {
            NVDLRule r = (NVDLRule) rules.get(i);
            if (r.getActionManager().isCancelAction()) {
                rules.remove(i);
            } else {
                i++;
            }
        }
    }

    public void simplifyInclusion() {
        if (includedModes == null) return;
        Iterator it = includedModes.iterator();
        while (it.hasNext()) {
            NVDLMode child = (NVDLMode) it.next();
            includeMode(child);
        }
        removeCancelActionRules();
        includedModes = null;
    }
}
