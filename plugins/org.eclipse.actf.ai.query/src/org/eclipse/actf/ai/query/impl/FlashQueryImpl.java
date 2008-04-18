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

package org.eclipse.actf.ai.query.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.actf.model.dom.dombycom.IFlashNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



public class FlashQueryImpl {
    private static final String FLASH_QUERY_NS = "http://www.ibm.com/xmlns/prod/aiBrowser/fennec/flash-query";
    private final String base;

    private final Target[] targets;

    private final boolean isFrameRangeSpecified;
    private final int frameRangeMin;
    private final int frameRangeMax;

    private final boolean isDepthSpecified;
    private final int depth;

    private FlashQueryImpl(String base, List<Target> targetList,
                           boolean isDepthSpecified, int depth,
                           boolean isFrameRangeSpecified,
                           int frameRangeMin, int frameRangeMax) {
        this.base = base;
        this.targets = targetList.toArray(new Target[targetList.size()]);
        this.isDepthSpecified = isDepthSpecified;
        this.depth = depth;
        this.isFrameRangeSpecified = isFrameRangeSpecified;
        this.frameRangeMin = frameRangeMin;
        this.frameRangeMax = frameRangeMax;
    }

    static private abstract class Target {
        protected boolean isRelative;
        public abstract List<Node> query(IFlashNode fn, List<Node> r);
    }

    static private class SimpleTarget extends Target {
        private final String path;

        @Override
        public List<Node> query(IFlashNode fn, List<Node> l) {
            IFlashNode r;
            if (isRelative) {
                if (path.length() > 0) {
                    r = fn.getNodeFromPath(fn.getTarget() + "." + path);
                } else {
                    r = fn;
                }
            } else {
                r = fn.getNodeFromPath(path);
            }
            if (r != null) l.add(r);
            return l;
        }

        SimpleTarget(String path) {
            if (path.charAt(0) == '.') {
                isRelative = true;
                this.path = path.substring(1);
            } else {
                isRelative = false;
                this.path = path;
            }
        }
    }

    static private class WildCardTarget extends Target {
        private List<Object> pathSegments;

        static private Pattern regExpQuote(String pat, char wildCardChar) {
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
            return Pattern.compile(r.toString());
        }

        private boolean match(Pattern p, IFlashNode fn) {
            String target = fn.getTarget();
            int idx = target.lastIndexOf(".");
            if (idx > 0) {
                target = target.substring(idx + 1);
            }
            Matcher m = p.matcher(target);
            return m.matches();
        }

        private ArrayList<Node> queryWC(IFlashNode fn, Pattern p, ArrayList<Node> l) {
            IFlashNode[] fns = fn.getInnerNodes();
            for (int i = 0; i < fns.length; i++) {
                if (match(p, fns[i])) {
                    l.add(fns[i]);
                }
            }
            return l;
        }

        @Override
        public List<Node> query(IFlashNode fn, List<Node> l) {
            ArrayList<Node> cnl = new ArrayList<Node>();
            Iterator<Object> it = pathSegments.iterator();
            if (isRelative) {
                cnl.add(fn);
            } else {
                if (!it.hasNext()) return l;
                Object ps = it.next();
                if (!(ps instanceof String)) return l;
                IFlashNode fn2 = fn.getNodeFromPath((String) ps);
                if (fn2 == null) return l;
                cnl.add(fn2);
            }
            while (it.hasNext()) {
                ArrayList<Node> cnl2 = new ArrayList<Node>();
                Object ps = it.next();
                if (ps instanceof String) {
                    for (int i = 0; i < cnl.size(); i++) {
                        IFlashNode fn2 = (IFlashNode) cnl.get(i);
                        fn2 = fn2.getNodeFromPath(fn2.getTarget() + "." + ps);
                        if (fn2 != null) cnl2.add(fn2);
                    }
                } else {
                    for (int i = 0; i < cnl.size(); i++) {
                        IFlashNode fn2 = (IFlashNode) cnl.get(i);
                        cnl2 = queryWC(fn2, (Pattern) ps, cnl2);
                    }
                }
                cnl = cnl2;
                if (cnl.size() == 0) return l;
            }
            l.addAll(cnl);
            return l;
        }

        WildCardTarget(String target, String base) {
            pathSegments = new ArrayList<Object>();
            char ct = target.charAt(0);
            switch (ct) {
            case '/':
                isRelative = false;
                target = target.substring(1);
                break;
            case '.':
                isRelative = true;
                target = target.substring(1);
                break;
            default:
                isRelative = false;
                target = base + "." + target;
            }

            StringBuffer buf = new StringBuffer();
            String[] segs = target.split("\\.");
            for (int i = 0; i < segs.length; i++) {
                if (segs[i].indexOf('*') >= 0) {
                    if (buf.length() > 0) {
                        pathSegments.add(buf.toString());
                        buf.setLength(0);
                    }
                    pathSegments.add(regExpQuote(segs[i], '*'));
                } else {
                    if (buf.length() > 0) {
                        buf.append(".");
                    }
                    buf.append(segs[i]);
                }
            }
            if (buf.length() > 0) {
                pathSegments.add(buf.toString());
            }
        }
    }

    public boolean hasTarget() {
        return targets.length > 0;
    }

    public List<Node> query(Node base) {
        if (!(base instanceof IFlashNode)) return null;
        IFlashNode fn = (IFlashNode) base;

        List<Node> r = new ArrayList<Node>();
        for (int i = 0; i < targets.length; i++) {
            r = targets[i].query(fn, r);
        }

        if (isDepthSpecified) {
            List<Node> r2 = new ArrayList<Node>();
            Iterator<Node> it = r.iterator();
            while (it.hasNext()) {
                IFlashNode fn2 = (IFlashNode) it.next();
                IFlashNode fnd = fn2.getNodeAtDepth(depth);
                if (fnd != null) r2.add(fnd);
            }
            r = r2;
        }
        if (isFrameRangeSpecified) {
            Iterator<Node> it = r.iterator();
            while (it.hasNext()) {
                IFlashNode fn2 = (IFlashNode) it.next();
                int f = fn2.getCurrentFrame();
                if (!((frameRangeMin <= f) && (f < frameRangeMax))) it.remove();
            }
        }

        return r;
    }

    private static String computeBase(String path, String pbase) {
        char ct = path.charAt(0);
        switch (ct) {
        case '/':
            return path.substring(1);
        case '.':
            return path;
        default:
            if ((pbase.length() == 0) || (pbase.equals("."))) return path;
            return pbase + "." + path;
        }
    }

    static FlashQueryImpl parse(Element e, FlashQueryImpl parentQuery) {
        String base = e.getAttributeNS(FLASH_QUERY_NS, "base");

        String pbase = "";
        if (parentQuery != null) {
            pbase = parentQuery.base;
        }
        if (base.length() == 0) {
            base = pbase;
        } else {
            base = computeBase(base, pbase);
        }

        String targetStr = e.getAttributeNS(FLASH_QUERY_NS, "targets");
        ArrayList<Target> targetList = new ArrayList<Target>();
        if (targetStr.length() > 0) {
            String[] targets = targetStr.split("[ \t\r\n]");
            for (int i = 0; i < targets.length; i++) {
                if (targets[i].indexOf('*') >= 0) {
                    targetList.add(new WildCardTarget(targets[i], base));
                } else {
                    targetList.add(new SimpleTarget(computeBase(targets[i], base)));
                }
            }
        }

        
        String depthStr = e.getAttributeNS(FLASH_QUERY_NS, "depth");
        boolean isDepthSpecified = false;
        int depth = 0;
        if (depthStr.length() > 0) {
            try {
                depth = Integer.parseInt(depthStr.trim());
                isDepthSpecified = true;
            } catch (NumberFormatException ex) {
            }
        }

        boolean isFrameRangeSpecified = false;
        int frameRangeMin = 0;
        int frameRangeMax = 0;
        String frameRangeStr = e.getAttributeNS(FLASH_QUERY_NS, "frameRange");
        if (frameRangeStr.length() > 0) {
            int pos = frameRangeStr.indexOf('-');
            try {
                if (pos < 0) {
                    int m = Integer.parseInt(frameRangeStr.trim());
                    frameRangeMin = m;
                    frameRangeMax = m + 1;
                } else {
                    frameRangeMin = Integer.parseInt(frameRangeStr.substring(0, pos).trim());
                    frameRangeMax = Integer.parseInt(frameRangeStr.substring(pos + 1).trim());
                }
                isFrameRangeSpecified = true;
            } catch (NumberFormatException ex) {
            }
        }

        FlashQueryImpl q = new FlashQueryImpl(base, targetList,
                                              isDepthSpecified, depth,
                                              isFrameRangeSpecified,
                                              frameRangeMin,
                                              frameRangeMax);

        return q;
    }

    static Attr serializeQuery(Node domNode, Node usrNode) {
        if (!(domNode instanceof IFlashNode))
            return null;
        Document doc = usrNode.getOwnerDocument();
        String target = ((IFlashNode)domNode).getTarget();
        if (target != null && target.length() > 0) {
            Attr attr = doc.createAttributeNS(FLASH_QUERY_NS, "flq:targets");
            attr.setNodeValue(target);
            return attr;
        }
        return null;
    }
    
}
