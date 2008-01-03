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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLActionManager;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAttributeSection;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLElement;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLMode;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLNoResultAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLResultAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRule;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;


/*
  Moore PDA
 */

public class PDA {
    // Internal state -----------------------------------------------------------------
    private ArrayList<StackElement> currentContext = new ArrayList<StackElement>();
    private ArrayList<StackElement> nextContext = new ArrayList<StackElement>();
    private boolean isAttributeAttached;

    // --------------------------------------------------------------------------------

    // Compiled Result -----------------------------------------------------------------
    private final State initialState;
    // --------------------------------------------------------------------------------

    private static class StackElement extends Interpretation {
        public final State state;
        public final StackElement parent;
        // To avoid duplicatedly merging branches.
        public final boolean firstBranch;

        public Interpretation getParent() {
            return parent;
        }
        
        private StackElement(NVDLMode mode, NVDLAction action,
                             NVDLElement e, StackElement parent, State state,
                             boolean firstBranch) {
            super(mode, action, e);
            this.parent = parent;
            this.state = state;
            this.firstBranch = firstBranch;
        }

        static StackElement newEffectiveStackElement(StackElement parent,
                                                     State state, NVDLElement element,
                                                     boolean firstBranch) {
            return new StackElement(state.mode, state.action, element, parent, state,
                                    firstBranch);
        }

        static StackElement newNoneffectiveStackElement(StackElement parent,
                                                        NVDLElement element) {
            return new StackElement(null, null, element, parent, parent.state, true);
        }
    }

    private static class AttrInterpretation extends Interpretation {
        public final StackElement parent;

        public Interpretation getParent() {
            return parent;
        }
        
        private AttrInterpretation(NVDLMode mode, NVDLAction action,
                                   StackElement parent) {
            super(mode, action, null);
            this.parent = parent;
        }

        static AttrInterpretation newAttrInterpretation(StackElement parent,
                                                  State state) {
            return new AttrInterpretation(state.mode, state.action, parent);
        }
    }

    // E ~= {Q' -> 2^Q'}, e \in E = rule(q')
    private static class Edge {
        final NVDLAction.Context allowableContext;
        final Mode nextMode;

        static class Mode {
            static class Dest {
                final NVDLRule rule;
                final State[] nextStates;
                Dest(NVDLRule rule, State[] nextStates) {
                    this.rule = rule;
                    this.nextStates = nextStates;
                }
                public String toString() {
                    return "Dest:" + rule.toString();
                }
            }
            private Dest[] dests;
            
            public State[] nextStates(String ns, boolean isElement) {
                for (int i = 0; i < dests.length; i++) {
                    if (dests[i].rule.match(ns, isElement)) return dests[i].nextStates;
                }
                // notreachable
                return null;
            }
        }

        boolean match(NVDLElement e) {
            return allowableContext.match(e);
        }

        Edge(NVDLAction.Context allowableContext, Mode nextMode) {
            this.allowableContext = allowableContext;
            this.nextMode = nextMode;
        }

        public String toString() {
            String ret = "Edge:";
            if (allowableContext != null)
                return ret + allowableContext.toString();
            else
                return ret;
        }
    }

    // Q' ~= Mode X Action
    private static class State {
        String id;

        Edge defaultEdge;
        Edge[] contextEdges;

        private Edge.Mode nextMode(NVDLElement e) {
            assert (e != null) || (contextEdges == null);
            if (contextEdges != null) {
                for (int i = 0; i < contextEdges.length; i++) {
                    if (contextEdges[i].match(e)) return contextEdges[i].nextMode;
                }
            }
            return defaultEdge.nextMode;
        }
        State[] next(NVDLElement e, String ns, boolean isElement) {
            Edge.Mode nextMode = nextMode(e);
            return nextMode.nextStates(ns, isElement);
        }

        final NVDLMode mode;
        final NVDLAction action;
        State(NVDLMode mode, NVDLAction action) {
            this.mode = mode;
            this.action = action;
        }

        String getID() {
            return id;
        }

        public String toString() {
            return ("PDA State-ID:" + id
                    + " Mode:" + mode
                    + " Action:" + action);
        }
    }

    private void finishTransition() {
        // ArrayList<StackElement> tmp = currentContext;
        currentContext = nextContext;
        nextContext = new ArrayList<StackElement>(currentContext.size());
        // nextContext.clear();
    }


    private boolean nextStateTransition(StackElement stack, NVDLElement e) {
        boolean firstBranch = true;
        State[] nextStates = stack.state.next(e.parent, e.ns, true);
        for (int i = 0; i < nextStates.length; i++) {
            StackElement newStack = StackElement.newEffectiveStackElement(stack, nextStates[i],
                                                                          e, firstBranch);
            nextContext.add(newStack);
            firstBranch = false;
        }
        return (nextStates.length > 0);
    }

    // --------------------------------------------------------------------------------
    //    APIs
    // --------------------------------------------------------------------------------

    public boolean startElement(NVDLElement e) {
        boolean flag = false;
        int size = currentContext.size();
        for (int i = 0; i < size; i++) {
            StackElement stack = currentContext.get(i);
            if (!e.isSectionHead()) {
                // push current state.
                StackElement se = StackElement.newNoneffectiveStackElement(stack, e);
                nextContext.add(se);
            } else {
                flag = nextStateTransition(stack, e);
            }
        }
        finishTransition();
        return flag;
    }

    public boolean endElement() {
        boolean flag = false;
        int size = currentContext.size();
        for (int i = 0; i < size; i++) {
            StackElement stack = currentContext.get(i);
            if (!stack.firstBranch) continue;
            StackElement parent = stack.parent;
            assert(parent != null);
            if (parent.state != stack.state) {
                flag = true;
            }
            nextContext.add(parent);
        }
        finishTransition();
        return flag;
    }

    public boolean isAttributeAttached() {
        return isAttributeAttached;
    }

    public List<Interpretation> getAttributeInterpretation(Interpretation current,
                                                           NVDLAttributeSection as) {
        StackElement stack = (StackElement) current;
        isAttributeAttached = false;
        List<Interpretation> result = new ArrayList<Interpretation>();

        State[] nextStates = stack.state.next(as.getBaseElement(),
                                              as.getNamespace(),
                                              false);
        for (int i = 0; i < nextStates.length; i++) {
            AttrInterpretation ai = AttrInterpretation.newAttrInterpretation(stack, nextStates[i]);
            if (ai.isDispatch()) {
                result.add(ai);
            }
            if (ai.isAttach()) {
                isAttributeAttached = true;
            }
        }
        assert (isAttributeAttached || (result.size() > 0));
        return result;
    }

    public List getCurrentInterpretations() {
        return currentContext;
    }

    public enum MatchResult {
        NOMATCH,
        POSSIBLE,
        MATCH
    }

    private boolean matchActionListWithStackElement(StackElement s,
                                                    NVDLElement e,
                                                    ActionList al) {
        List<NVDLAction> l = al.getActions();
        int lastIdx = l.size() - 1;
        int idx = lastIdx - 1;
        State state = s.state;
        for (; s != null; s = s.parent) {
            NVDLAction a = s.getAction();
            if (a == null) continue;
            if (idx < 0) return false;
            NVDLAction a2 = l.get(idx);
            if (!a.equals(a2)) return false;
            idx--;
        }
        NVDLAction a2 = l.get(lastIdx);
        State[] states = state.next(e.parent, e.ns, true);
        for (int i = 0; i < states.length; i++) {
            if (a2.equals(states[i].action)) return true;
        }
        return false;
    }

    public MatchResult matchActionList(NVDLElement nextElement,
                                       ActionList al) {
        int size = currentContext.size();
        for (int i = 0; i < size; i++) {
            StackElement s = currentContext.get(i);
            if (matchActionListWithStackElement(s, nextElement, al))
                return MatchResult.MATCH;
        }
        return MatchResult.NOMATCH;
    }

    public void reset() {
        currentContext.clear();
        nextContext.clear();
        isAttributeAttached = false;
        StackElement initialStack = StackElement.newEffectiveStackElement(null, initialState, null, true);
        currentContext.add(initialStack);
    }

    public PDA(NVDLRules rules) {
        NVDLMode startMode = rules.getStartMode();
        Compiler compiler = new Compiler();
        this.initialState = compiler.compile(startMode);
        reset();
    }

    public PDA(PDA base) {
        this.initialState = base.initialState;
        reset();
    }

    // --------------------------------------------------------------------------------
    // compilation part
    // --------------------------------------------------------------------------------

    private static class Compiler {
        static class StateDicKey {
            private NVDLMode mode;
            private NVDLAction action;
            public boolean equals(Object o) {
                if (!(o instanceof StateDicKey)) return false;
                StateDicKey s = (StateDicKey) o;
                return mode.equals(s.mode) && action.equals(s.action);
                
            }
            public int hashCode() {
                if (action == null) {
                    return mode.hashCode();
                } else {
                    return mode.hashCode() ^ action.hashCode();
                }
            }
            StateDicKey(NVDLMode mode, NVDLAction action) {
                this.mode = mode;
                this.action = action;
            }
        }
        // StateDicKey -> State
        private HashMap<StateDicKey, State> stateDic = new HashMap<StateDicKey, State>();

        private State getState(StateDicKey k) {
            return stateDic.get(k);
        }

        private State newState(StateDicKey k) {
            State s = new State(k.mode, k.action);
            stateDic.put(k, s);
            return s;
        }

        private Edge.Mode.Dest compileRule(NVDLRule rule, NVDLMode m) {
            NVDLActionManager am = rule.getActionManager();
            ArrayList<State> states = new ArrayList<State>();

            NVDLResultAction ra = am.getResultAction();
            if (ra != null) states.add(compileToState(ra, m));
            List nras = am.getNoResultActions();
            Iterator it = nras.iterator();
            while (it.hasNext()) {
                NVDLNoResultAction nra = (NVDLNoResultAction) it.next();
                states.add(compileToState(nra, m));
            }

            return new Edge.Mode.Dest(rule,
                                      states.toArray(new State[states.size()]));
        }

        private Edge.Mode compileToMode(NVDLMode m) {
            Edge.Mode em = new Edge.Mode();

            NVDLRule rule;
            ArrayList<Edge.Mode.Dest> dests = new ArrayList<Edge.Mode.Dest>();
            Iterator it = m.notAnyNamespaceRuleIterator();
            while (it.hasNext()) {
                rule = (NVDLRule) it.next();
                Edge.Mode.Dest d = compileRule(rule, m);
                dests.add(d);
            }
            rule = m.getAnyNamespaceRuleForAttribute();
            if (rule != null) {
                Edge.Mode.Dest d = compileRule(rule, m);
                dests.add(d);
            }
            rule = m.getAnyNamespaceRuleForElement();
            if (rule != null) {
                Edge.Mode.Dest d = compileRule(rule, m);
                dests.add(d);
            }
            em.dests = (Edge.Mode.Dest[]) dests.toArray(new Edge.Mode.Dest[dests.size()]);
            return em;
        }

        private Edge compileToDefaultEdge(NVDLMode m) {
            Edge e = new Edge(null, compileToMode(m));
            return e;
        }

        private Edge[] compileToContextEdges(NVDLAction a) {
            List<NVDLAction.Context> contextsList = a.getContextsList();
            Edge[] contextEdges = new Edge[contextsList.size()];
            int i = 0;
            Iterator<NVDLAction.Context> it = contextsList.iterator();
            while (it.hasNext()) {
                NVDLAction.Context c = it.next();
                contextEdges[i++] = new Edge(c, compileToMode(c.useMode));
            }
            return contextEdges;
        }

        private State compileToState(NVDLAction a, NVDLMode m) {
            StateDicKey k = new StateDicKey(m, a);
            State s = getState(k);
            if (s != null) return s;
            s = newState(k);

            if (a == null) {
                s.defaultEdge = compileToDefaultEdge(m);
            } else {
                s.contextEdges = compileToContextEdges(a);
                s.defaultEdge = compileToDefaultEdge(a.getUseMode());
            }
            return s;
        }

        State compile(NVDLMode startMode) {
            return compileToState(null, startMode);
        }
    }

    private void dumpDest(Edge.Mode.Dest d, Set<State> traversed) {
        System.out.println(d.toString());
        for (int i = 0; i < d.nextStates.length; i++) {
            dumpState(d.nextStates[i], traversed);
        }
    }

    private void dumpMode(Edge.Mode em, Set<State> traversed) {
        for (int i = 0; i < em.dests.length; i++) {
            dumpDest(em.dests[i], traversed);
        }
    }

    private void dumpEdge(Edge edge, Set<State> traversed) {
        System.out.println(edge.toString());
        dumpMode(edge.nextMode, traversed);
    }

    private void dumpState(State state, Set<State> traversed) {
        System.out.println(state.toString());
        if (!(traversed.contains(state))) {
            traversed.add(state);
            dumpEdge(state.defaultEdge, traversed);
            
            for (int i = 0; i < state.contextEdges.length; i++) {
                dumpEdge(state.contextEdges[i], traversed);
            }
        }
        System.out.println("--------------------");
    }

    public void dump() {
        dumpState(initialState, new HashSet<State>());
    }
}

