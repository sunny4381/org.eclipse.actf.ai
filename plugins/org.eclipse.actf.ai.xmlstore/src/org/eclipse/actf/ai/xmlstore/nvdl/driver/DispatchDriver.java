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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.eclipse.actf.ai.xmlstore.nvdl.dispatcher.NVDLSAXDispatcher;
import org.eclipse.actf.ai.xmlstore.nvdl.model.Location;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLMessage;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.eclipse.actf.ai.xmlstore.nvdl.reader.NVDLSAXReader;
import org.eclipse.actf.ai.xmlstore.nvdl.util.IRIUtil;
import org.eclipse.actf.ai.xmlstore.nvdl.util.Log;
import org.eclipse.actf.ai.xmlstore.nvdl.util.MessageFormatter;
import org.eclipse.actf.ai.xmlstore.nvdl.util.WritingContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * The <code>DispatchDriver</code> is a driver for validation.
 */
public class DispatchDriver implements NVDLSAXDispatcher.DebugHandlerFactory {
    private XMLReader reader;
    private NVDLSAXReader nvdlReader;
    private ErrorHandler eh;
    private String targetDirectory;
    private File instanceFile;
    // private boolean debug;
    private boolean quiet;

    private String quoteID(String id) {
        StringBuffer buf = new StringBuffer();
        int len = id.length();
        for (int i = 0; i < len; i++) {
            char c = id.charAt(i);
            switch (c) {
            case '/':
            case '\\':
            case ' ':
            case '*':
            case '?':
            case '>':
            case '<':
            case '|':
                buf.append('_');
                break;
            default:
                buf.append(c);
                break;
            }
        }
        return buf.toString();
    }

    private File generateFile(String id,
                              NVDLAction action) {
        String name = instanceFile.getName() + "-" + quoteID(id);
        return new File(targetDirectory, name);
    }

    public ContentHandler createContentHandler(String id,
                                               NVDLAction action) throws SAXException {
        try {
            FileOutputStream os = new FileOutputStream(generateFile(id, action));
            return new WritingContentHandler(new OutputStreamWriter(os, "utf-8"));
        } catch (FileNotFoundException e) {
            throw new SAXException(e);
        } catch (UnsupportedEncodingException e) {
            throw new SAXException(e);
        }
    }
	
    public void nextActionHandler(NVDLAction action, Locator l) {
        if (quiet) return;

        NVDLMessage mes = action.getMessage();
        String localeCode = Locale.getDefault().getLanguage();
        Location location = new Location(l.getLineNumber(),
                                         l.getColumnNumber(),
                                         l.getSystemId());
        String m = mes.getMessage(localeCode);
        if (m != null) {
            String r = MessageFormatter.locAndModel(location, action, m);
            Log.info(r);
        }
    }

    private void setupReader() throws Exception {
        reader = NVDLSAXReader.newXMLReader();
        reader.setErrorHandler(eh);
        nvdlReader = new NVDLSAXReader(reader, eh);
    }

    void dispatch(String nvdlFile, String instanceFilename, String targetDirectory)
        throws Exception {
        Log.info("ValidationDriver.OpenNVDLFile", new Object[] {nvdlFile});
        this.instanceFile = new File(instanceFilename);
        NVDLRules rules = nvdlReader.parse(IRIUtil.newInputSourceFromFilename(nvdlFile));
        if (rules == null) return;

        NVDLSAXDispatcher dispatcher = new NVDLSAXDispatcher(rules, true);

        this.targetDirectory = targetDirectory;

        dispatcher.setDebugHandlerFactory(this);
        Log.info("ValidationDriver.ValidateInstanceFile", new Object[] {instanceFilename});
        if (dispatcher.validate(IRIUtil.newInputSourceFromFilename(instanceFilename), eh)) {
            Log.info("ValidationDriver.Success", new Object[] {instanceFilename});
        } else {
            Log.error("ValidationDriver.Error",
                      new Object[] {
                          new Integer(dispatcher.getErrorCount()),
                          instanceFilename
                      });
        }
    }

    DispatchDriver(ErrorHandler eh,
                   boolean debug,
                   boolean quiet) throws Exception {
        this.eh = eh;
        // this.debug = debug;
        this.quiet = quiet;
        setupReader();
    }
}
