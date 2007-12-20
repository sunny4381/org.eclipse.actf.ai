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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <code>NVDLRule</code> is a model class for NVDL `rule'.
 */
public class NVDLRule extends NVDLModel {
    private final boolean anyNamespace;
    public boolean isAnyNamespace() {
        return anyNamespace;
    }
    private final String ns;
    private final Pattern nsPattern;
    private final char wildCardChar;

    private final boolean targetElement;
    public boolean isTargetElement() {
        return targetElement;
    }
    private final boolean targetAttribute;
    public boolean isTargetAttribute() {
        return targetAttribute;
    }

    private NVDLActionManager actionManager = new NVDLActionManager();

    public boolean isConflicted(NVDLRule rule) {
        // Case 0
        if (anyNamespace || rule.anyNamespace) {
            if (anyNamespace == rule.anyNamespace) {
                //
                if ((isTargetElement() == rule.isTargetElement())
                    || (isTargetAttribute() == rule.isTargetAttribute()))
                    return true;
            }
            return false;
        }
        // TODO! intersection of two automaton...
        return false;
    }

    public boolean isOverridden(NVDLRule rule) {
        if (((targetElement != rule.targetElement)
             || (targetAttribute != rule.targetAttribute)))
            return false;
        if (isAnyNamespace()) {
            return rule.isAnyNamespace();
        }
        return (wildCardChar == rule.wildCardChar) && ns.equals(rule.ns);
    }

    public boolean match(String ns, boolean targetElement) {
        if (targetElement) {
            if (!isTargetElement()) return false;
        } else {
            if (!isTargetAttribute()) return false;
        }
        if (anyNamespace) return true;
        if (nsPattern == null) {
            return this.ns.equals(ns);
        }
        Matcher m = nsPattern.matcher(ns);
        return m.matches();
    }

    public NVDLModel visitModel(NVDLModelVisitor v) throws NVDLModelException {
        return v.visitNVDLRule(this);
    }

    public NVDLActionManager getActionManager() {
        return actionManager;
    }

    public String toString() {
        StringBuffer r = new StringBuffer();
        if (anyNamespace) {
            r.append("AnyNamespace");
        } else {
            r.append("NS={");
            r.append(ns);
            r.append("},W=");
            r.append(wildCardChar);
        }
        return r.toString();
    }

    private String quote(String pat, char wildCardChar) {
        StringBuffer r = new StringBuffer();
        for (int i = 0; i < pat.length(); i++) {
            char c = pat.charAt(i);
            if (c == wildCardChar) {
                r.append(".*");
            } else if (("\\?*+.[]{}()$^".indexOf(c) >= 0)) {
                r.append('\\');
                r.append(c);
            } else {
                r.append(c);
            }
        }
        return r.toString();
    }

    public NVDLRule(boolean anyNamespace, String ns, char wildCardChar,
                    boolean targetElement, boolean targetAttribute) {
        assert (targetElement || targetAttribute);

        this.anyNamespace = anyNamespace;
        if (!anyNamespace) {
        	this.ns = ns;
        	if (ns.indexOf(wildCardChar) > 0) {
        		nsPattern = Pattern.compile(quote(ns, wildCardChar));
        	} else {
        		nsPattern = null;
        	}
        } else {
        	this.ns = null;
        	this.nsPattern = null;
        }
        this.wildCardChar = wildCardChar;
        this.targetElement = targetElement;
        this.targetAttribute = targetAttribute;
    }
}
