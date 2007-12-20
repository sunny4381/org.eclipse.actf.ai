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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;


/**
 * The <code>NVDLAction</code> is the base abstract class for NVDL action.
 */
public abstract class NVDLAction extends NVDLModel {
    private String useModeName;
    private NVDLMode useMode;
    private List<Context> contexts = new ArrayList<Context>();
    private final String name;

    public String getName() {
        return name;
    }

    public String getUseModeName() {
        return useModeName;
    }

    public List<Context> getContextsList() {
        return contexts;
    }

    public NVDLMode getUseMode() {
        return useMode;
    }

    public void setUseMode(NVDLMode useMode) {
        this.useMode = useMode;
        useModeName = useMode.name;
    }
 
    private NVDLMessage message;
    public NVDLMessage getMessage() {
        if (message == null) {
            message = new NVDLMessage();
        }
        return message;
    }

    private NVDLRule belongingRule;
    public NVDLRule getBelongingRule() {
        return belongingRule;
    }

    public static class Context {
        public final String path;
        static class Path {
            final String[] pathElems;
            final boolean isAbsolute;
            Path(String[] pathElems, boolean isAbsolute) {
                this.pathElems = pathElems;
                this.isAbsolute = isAbsolute;
            }
        }
        public final Path[] pathExps;

        public final String useModeName;
        public NVDLMode useMode;

        public boolean match(LinkedList elemStack) {
            for (int i = 0; i < pathExps.length; i++) {
                int sectionIdx = -1;
                ListIterator it = elemStack.listIterator(elemStack.size());
                Path p = pathExps[i];
                for (int j = p.pathElems.length - 1; ; j--) {
                    if (!it.hasPrevious()) {
                        if (j >= 0) break;
                        return true;
                    }
                    NVDLElement e = (NVDLElement) it.previous();
                    if (sectionIdx < 0) {
                        sectionIdx = e.sectionIdx;
                    } else if (e.sectionIdx != sectionIdx) {
                        if (j >= 0) break;
                        return true;
                    }
                    if (j < 0) {
                        if (p.isAbsolute) break;
                        return true;
                    }
                    if (!p.pathElems[j].equals(e.localName)) break;
                }
            }
            return false;
        }

        public boolean match(NVDLElement e) {
            for (int i = 0; i < pathExps.length; i++) {
                Path p = pathExps[i];
                for (int j = p.pathElems.length - 1; ; j--) {
                    if (j < 0) {
                        if (p.isAbsolute) break;
                        return true;
                    }
                    if (!p.pathElems[j].equals(e.localName)) break;
                    if (e.isSectionHead()) {
                        if (j > 0) break;
                        return true;
                    }
                    e = e.parent;
                }
            }
            return false;
        }

        private int extractPathElement(int i, String path, StringBuffer ret) {
            boolean startPath = false;
            boolean slashAppeared = false;
            boolean endPath = false;
            ret.delete(0, ret.length());
            while (i < path.length()) {
                switch (path.charAt(i)) {
                case '|':
                    return i;
                case '/':
                    if (startPath) return i;
                    if (slashAppeared) return -1;
                    slashAppeared = true;
                    ret.append(path.charAt(i++));
                    break;
                case ' ': case '\r': case '\n': case '\t':
                    if (startPath) endPath = true;
                    i++;
                    break;
                default:
                    if (endPath) return -1;
                    startPath = true;
                    ret.append(path.charAt(i++));
                    continue;
                }
            }
            return i;
        }

        private void invalidPath(String path) throws NVDLException {
            throw new NVDLException("Invalid Path:" + path);
        }

        private Context.Path[] parsePath(String path) throws NVDLException {
            StringBuffer buf = new StringBuffer();
            ArrayList<String> paths = new ArrayList<String>();
            ArrayList<Context.Path> pathExps = new ArrayList<Context.Path>();
            boolean isAbsolute = false;
            int len = path.length();
            int i = 0;
            while (i < len) {
                paths.clear();
                i = extractPathElement(i, path, buf);
                if (i < 0) invalidPath(path);
                if (i < len && path.charAt(i) == '|') invalidPath(path);
                if (buf.charAt(0) == '/') {
                    isAbsolute = true;
                    paths.add(buf.substring(1, buf.length()));
                } else {
                    isAbsolute = false;
                    if (buf.length() == 0) invalidPath(path);
                    paths.add(buf.toString());
                }
                if (i < len) {
                    for (;;) {
                        i = extractPathElement(i, path, buf);
                        if (i < 0) invalidPath(path);
                        if (buf.charAt(0) == '/') {
                            if (buf.length() == 1) invalidPath(path);
                            paths.add(buf.substring(1, buf.length()));
                        } else {
                            paths.add(buf.toString());
                        }
                        if (i < len) {
                            if (path.charAt(i) == '|') {
                                i++;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                int pathElems = paths.size();
                if (pathElems == 0) invalidPath(path);
                Context.Path cpath = new Context.Path((String[]) paths.toArray(new String[pathElems]),
                                                      isAbsolute);
                pathExps.add(cpath);
            }
            return pathExps.toArray(new Context.Path[pathExps.size()]);
        }

        Context(String path, String useModeName, NVDLMode useMode) throws NVDLException {
            this.path = path;
            this.pathExps = parsePath(path);
            this.useModeName = useModeName;
            this.useMode = useMode;
        }
    }

    public void addContext(String path, String useModeName) throws NVDLException {
        contexts.add(new Context(path, useModeName, null));
    }

    public void addContext(String path, NVDLMode useMode) throws NVDLException {
        contexts.add(new Context(path, useMode.name, useMode));
    }

    public NVDLMode nextMode(LinkedList elemStack) {
        int size = contexts.size();
        for (int i = 0; i < size; i++) {
            Context c = contexts.get(i);
            if (c.match(elemStack)) return c.useMode;
        }
        return useMode;
    }

    NVDLAction(String name, String useModeName, NVDLRule belongingRule) {
        this.name = name;
        this.useModeName = useModeName;
        this.belongingRule = belongingRule;
    }
}
