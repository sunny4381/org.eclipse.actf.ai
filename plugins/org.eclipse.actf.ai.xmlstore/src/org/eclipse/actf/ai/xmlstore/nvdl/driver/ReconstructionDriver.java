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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.SAXParser;

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.eclipse.actf.ai.xmlstore.nvdl.reader.NVDLSAXReader;
import org.eclipse.actf.ai.xmlstore.nvdl.rec.SAXReconstructor;
import org.eclipse.actf.ai.xmlstore.nvdl.util.IRIUtil;
import org.eclipse.actf.ai.xmlstore.nvdl.util.Log;
import org.eclipse.actf.ai.xmlstore.nvdl.util.WritingContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * The <code>ReconstructionDriver</code> is a driver for reconstruction.
 */
public class ReconstructionDriver {
    private SAXParser parser;
    private NVDLSAXReader nvdlReader;
    private ErrorHandler eh;
    // private boolean debug;
    // private boolean quiet;

    private static class InputThread extends Thread {
        private XMLReader reader;
        private InputSource inputSource;

        public void run() { 
            try {
                reader.parse(inputSource);
            } catch (IOException e) {
                // TODO
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        
        InputThread(XMLReader reader, File file) throws IOException {
            super(file.getName());
            this.reader = reader;
            String path = file.getCanonicalPath();
            this.inputSource = IRIUtil.newInputSourceFromFilename(path);
        }
    }

    private void setInput(SAXReconstructor rec, File file) throws Exception {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setContentHandler(rec.requestInput());
        InputThread ithread = new InputThread(reader, file);
        ithread.start();
    }

    private boolean setupInput(SAXReconstructor rec, File dir) throws Exception {
        if (!dir.isDirectory()) {
            throw new FileNotFoundException(dir + " is not a directory.");
        }
        File[] fileList = dir.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            File file = fileList[i];
            if (file.isFile() && file.canRead()) {
                setInput(rec, file);
            }
        }
        return true;
    }

    void reconstruct(String nvdlFile, String instanceFilename, String targetDirectory)
        throws Exception {
        Log.info("ValidationDriver.OpenNVDLFile", new Object[] {nvdlFile});
        NVDLRules rules = nvdlReader.parse(IRIUtil.newInputSourceFromFilename(nvdlFile));
        if (rules == null) return;

        SAXReconstructor rec = new SAXReconstructor(rules);

        File instanceFile = new File(instanceFilename);
        FileOutputStream os = new FileOutputStream(instanceFile);
        OutputStreamWriter w = new OutputStreamWriter(os, "utf-8");
        rec.setOutput(new WritingContentHandler(w));

        setupInput(rec, new File (targetDirectory));

        rec.start();
    }

    private void setupReader() throws Exception {
        parser = NVDLSAXReader.newSAXParser();
        parser.getXMLReader().setErrorHandler(eh);
        nvdlReader = new NVDLSAXReader(parser, eh);
    }

    ReconstructionDriver(ErrorHandler eh,
                         boolean debug,
                         boolean quiet) throws Exception {
        this.eh = eh;
        // this.debug = debug;
        // this.quiet = quiet;
        setupReader();
    }
}
