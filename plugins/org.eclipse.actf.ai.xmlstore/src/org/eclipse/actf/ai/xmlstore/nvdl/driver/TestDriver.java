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

package org.eclipse.actf.ai.xmlstore.nvdl.driver;

import java.io.File;
import java.io.FilenameFilter;

import javax.xml.validation.ValidatorHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;
import org.eclipse.actf.ai.xmlstore.nvdl.dispatcher.NVDLSAXDispatcher;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.eclipse.actf.ai.xmlstore.nvdl.reader.NVDLSAXReader;
import org.eclipse.actf.ai.xmlstore.nvdl.util.DefaultErrorHandler;
import org.eclipse.actf.ai.xmlstore.nvdl.util.IRIUtil;
import org.eclipse.actf.ai.xmlstore.nvdl.util.Log;

public class TestDriver {
    XMLReader xmlReader;
    NVDLSAXReader r;

    TestDriver() throws SAXException {
        this.xmlReader = NVDLSAXReader.newXMLReader();
        xmlReader.setErrorHandler(DefaultErrorHandler.getErrorHandler());
        this.r = new NVDLSAXReader(xmlReader);
    }

    static public class TestSet {
        boolean result;
        String baseName;
        String nvdlFilename;
        Case[] cases;
        public class Case {
            boolean result;
            String xmlFilename;
        }
        Case newCase() {
            return new Case();
        }
    }

    NVDLRules readTest(String path, boolean result) throws Exception {
        InputSource is = IRIUtil.newInputSourceFromFilename(path);
        if (!result) {
            System.out.println("Interpret " + path
                               + ", assuming it is invalid.");
            try {
                r.parse(is);
            } catch (Exception e) {
                return null;
            }
            throw new NVDLException("The test failed... ");
        } else {
            System.out.println("Interpret " + path
                               + ", assuming it is valid.");
            return r.parse(is);
        }
    }

    void dispatchTest(XMLReader vr, File d, TestSet.Case c) throws Exception {
        String path = d.getPath() + File.separator + c.xmlFilename;
        InputSource is = IRIUtil.newInputSourceFromFilename(path);
        if (!c.result) {
            System.out.println("Validate " + path
                               + ", assuming it is invalid.");
            try {
                vr.parse(is);
            } catch (Exception e) {
                return;
            }
            throw new NVDLException("The test failed... ");
        } else {
            System.out.println("Validate " + path
                               + ", assuming it is valid.");
            vr.parse(is);
        }
    }

    void doTest(File d, TestSet ts) throws Exception {
        NVDLRules rules = readTest(d.getPath() + File.separator
                                   + ts.nvdlFilename, ts.result);
        if (!ts.result) return;
        NVDLSAXDispatcher dispatcher = new NVDLSAXDispatcher(rules, false);
        ValidatorHandler h = dispatcher.getValidatorHandler();
        XMLReader vr = XMLReaderFactory.createXMLReader();
        vr.setFeature("http://xml.org/sax/features/namespaces", true);
        vr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        h.setErrorHandler(DefaultErrorHandler.getErrorHandler());
        vr.setContentHandler(h);
        vr.setErrorHandler(DefaultErrorHandler.getErrorHandler());
        for (int i = 0; i < ts.cases.length; i++) {
            dispatchTest(vr, d, ts.cases[i]);
            dispatcher.reset();
        }
    }

    void invalidFilename(String filename) {
        throw new IllegalArgumentException("Filename must be of the form of '<name>-<true/false>-<number>.<suffix>':" + filename);
    }

    static class BaseNameFilter implements FilenameFilter {
        final String baseName;
        public boolean accept(File dir, String name) {
            if (!name.endsWith(".xml")) return false;
            String[] parts = name.split("-");
            if (parts.length != 3) return false;
            if (!parts[0].equals(baseName)) return false;
            return true;
        }
        BaseNameFilter(String baseName) {
            this.baseName = baseName;
        }
    }

    void testCaseSet(File base, TestSet ts) {
        String[] fileList;
        fileList = base.list(new BaseNameFilter(ts.baseName));
        ts.cases = new TestSet.Case[fileList.length];
        for (int i = 0; i < fileList.length; i++) {
            String[] parts = fileList[i].split("-");
            TestSet.Case cs = ts.newCase();
            cs.xmlFilename = fileList[i];
            if ("true".equals(parts[1])) {
                cs.result = true;
            } else if ("false".equals(parts[1])) {
                cs.result = false;
            } else {
                invalidFilename(cs.xmlFilename);
            }
            ts.cases[i] = cs;
        }
    }

    TestSet parseFilename(File base, String filename) {
        String[] parts = filename.split("-");
        if (parts.length != 3) {
            invalidFilename(filename);
        }
        TestSet ts = new TestSet();
        ts.nvdlFilename = filename;
        ts.baseName = parts[0];
        if ("true".equals(parts[1])) {
            ts.result = true;
            testCaseSet(base, ts);
        } else if ("false".equals(parts[1])) {
            ts.result = false;
        } else {
            invalidFilename(filename);
        }

        return ts;
    }

    void testDir(String dir) throws Exception {
        File f = new File(dir);
        if (!f.exists()) {
            Log.error("Directory:" + dir + " does not exist.  Skip it.");
            return;
        }
        if (!f.isDirectory()) {
            Log.error(dir + " is not a directory.  Skip it.");
            return;
        }
        
        String[] fileList;
        fileList = f.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (!name.endsWith(".nvdl")) return false;
                String[] parts = name.split("-");
                if (parts.length != 3) return false;
                return true;
            }
        });

        for (int i = 0; i < fileList.length; i++) {
            doTest(f, parseFilename(f, fileList[i]));
        }
    }

    public static void main(String[] args) {
        try {
            TestDriver td = new TestDriver();
            for (int i = 0; i < args.length; i++) {
                td.testDir(args[i]);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            System.out.flush();
            System.exit(255);
        }
        System.out.println("All tests are done.");
        System.out.flush();
        System.exit(0);
    }
}
