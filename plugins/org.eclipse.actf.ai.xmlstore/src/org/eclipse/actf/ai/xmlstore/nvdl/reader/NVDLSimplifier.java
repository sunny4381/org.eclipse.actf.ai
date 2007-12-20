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

package org.eclipse.actf.ai.xmlstore.nvdl.reader;

import java.util.Iterator;
import java.util.List;

import org.eclipse.actf.ai.xmlstore.nvdl.model.Location;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLActionManager;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLMode;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModel;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModelException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModelTraverse;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLNoResultAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRejectAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLResultAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRule;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * The <code>NVDLSimplifier</code> deals with the simplification of NVDL.
 */
public class NVDLSimplifier extends NVDLModelTraverse {
    private NVDLRules rules;
    private NVDLMode currentMode;
    private ErrorHandler eh;
    private int errorCounter;

    private void error(NVDLModel m, String mes) throws NVDLModelException {
        error(m, mes, new Object[0]);
    }

    private void error(NVDLModel m, String mes, Object[] args) throws NVDLModelException {
        error(m, new NVDLReaderException(mes, args));
    }

    private void error(NVDLModel m, NVDLReaderException e) throws NVDLModelException {
        errorCounter++;
        Location l = m.getLocation();
        try {
            eh.error(new SAXParseException(e.getMessage(),
                                           "", l.iri, l.line, l.pos, e));
        } catch (SAXException se) {
            throw new NVDLModelException(se);
        }
    }

    private String generateUniqueActionID() {
        String id;
        for (int i = 1; ; i++) {
            id = "N" + i;
            if (rules.getAction(id) == null) break;
        }
        return id;
    }


    // Simplification 6.4.12
    private void addDefaultAnyNamespace(NVDLMode mode) {
        if (mode.getAnyNamespaceRuleForElement() == null) {
            NVDLRule r = new NVDLRule(true, "", ' ', true, false);
            NVDLActionManager am = r.getActionManager();
            String id = generateUniqueActionID();
            NVDLNoResultAction a = new NVDLRejectAction(id, mode.name, r);
            r.copyLocation(mode);
            a.copyLocation(mode);
            a.setUseMode(mode);
            am.addNoResultAction(a);
            mode.addRule(r);
        }
        if (mode.getAnyNamespaceRuleForAttribute() == null) {
            NVDLRule r = new NVDLRule(true, "", ' ', false, true);
            NVDLActionManager am = r.getActionManager();
            String id = generateUniqueActionID();
            NVDLResultAction a = new NVDLResultAction(id, mode.name,
                                                      NVDLResultAction.TYPE_ATTACH, r);
            r.copyLocation(mode);
            a.copyLocation(mode);
            a.setUseMode(mode);
            am.setResultAction(a);
            mode.addRule(r);
        }
    }

    public NVDLModel visitNVDLMode(NVDLMode mode)
        throws NVDLModelException {
        if (checkTraversed(mode)) return null;
        currentMode = mode;
        mode.simplifyInclusion();
        addDefaultAnyNamespace(mode);
        return super.visitNVDLMode(mode);
    }

    private void setUseMode(NVDLAction action) throws NVDLModelException {
        if (action.getUseMode() == null) {
            String useModeName = action.getUseModeName();
            if (useModeName == null) {
                // Simplification 6.4.14
                action.setUseMode(currentMode);
            } else {
                NVDLMode mode = rules.getMode(useModeName);
                if (mode == null) {
                    error(action, "NVDLSimplifier.ModeIsNotDefinedError",
                          new Object[] {useModeName});
                }
                action.setUseMode(mode);
            }
        }
        List contexts = action.getContextsList();
        Iterator it = contexts.iterator();
        while (it.hasNext()) {
            NVDLAction.Context c = (NVDLAction.Context) it.next();
            if (c.useMode == null) {
                assert c.useModeName != null;
                NVDLMode mode = rules.getMode(c.useModeName);
                if (mode == null) {
                    error(action, "NVDLSimplifier.ModeIsNotDefinedError",
                          new Object[] {c.useModeName});
                }
                c.useMode = mode;
            }
        }
        
    }

    public NVDLModel visitNVDLNoResultAction(NVDLNoResultAction action)
        throws NVDLModelException {
        if (checkTraversed(action)) return null;
        setUseMode(action);
        return super.visitNVDLNoResultAction(action);
    }

    public NVDLModel visitNVDLResultAction(NVDLResultAction action)
        throws NVDLModelException {
        if (checkTraversed(action)) return null;
        setUseMode(action);
        return super.visitNVDLResultAction(action);
    }

    public NVDLModel visitNVDLRules(NVDLRules rules)
        throws NVDLModelException {
        if (rules.getStartMode() == null) {
            String name = rules.getStartModeName();
            if (name == null) {
                error(rules, "NVDLSimplifier.RulesHasNoStartModeError");
            } else {
                NVDLMode mode = rules.getMode(name);
                if (mode == null) {
                    error(rules, "NVDLSimplifier.StartModeIsNotDefinedError",
                          new Object[] {name});
                }
                rules.setStartMode(mode);
            }
        }
        return super.visitNVDLRules(rules);
    }

    public int getErrorCount() {
        return errorCounter;
    }

    public NVDLSimplifier(NVDLRules rules, ErrorHandler eh) {
        this.rules = rules;
        this.eh = eh;
        this.errorCounter = 0;
    }
}
