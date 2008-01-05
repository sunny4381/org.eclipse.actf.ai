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

import java.util.Locale;

import javax.xml.parsers.SAXParser;

import org.eclipse.actf.ai.xmlstore.nvdl.dispatcher.NVDLSAXDispatcher;
import org.eclipse.actf.ai.xmlstore.nvdl.model.Location;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLMessage;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.eclipse.actf.ai.xmlstore.nvdl.reader.NVDLSAXReader;
import org.eclipse.actf.ai.xmlstore.nvdl.util.ContentPrintHandler;
import org.eclipse.actf.ai.xmlstore.nvdl.util.IRIUtil;
import org.eclipse.actf.ai.xmlstore.nvdl.util.Log;
import org.eclipse.actf.ai.xmlstore.nvdl.util.MessageFormatter;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;


/**
 * The <code>ValidationDriver</code> is a driver for validation.
 */
public class ValidationDriver implements NVDLSAXDispatcher.DebugHandlerFactory {
    private SAXParser parser;
    private NVDLSAXReader nvdlReader;
    private ErrorHandler eh;
    private boolean debug;
    private boolean quiet;

    public ContentHandler createContentHandler(String id, NVDLAction action) {
        if (debug) {
            return new ContentPrintHandler(MessageFormatter.model(action, ""));
        }
        return null;
    }
	
    public void nextActionHandler(NVDLAction action, Locator l) {
        if (!quiet) {
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
    }

    private void setupReader() throws Exception {
        parser = NVDLSAXReader.newSAXParser();
        parser.getXMLReader().setErrorHandler(eh);
        nvdlReader = new NVDLSAXReader(parser, eh);
    }

    void validate(String nvdlFile, String instanceFile)
        throws Exception {
        Log.info("ValidationDriver.OpenNVDLFile", new Object[] {nvdlFile});
        NVDLRules rules = nvdlReader.parse(IRIUtil.newInputSourceFromFilename(nvdlFile));
        if (rules == null) return;
        // NVDLModelPrint.printRules(rules);
        NVDLSAXDispatcher dispatcher = new NVDLSAXDispatcher(rules, false);

        dispatcher.setDebugHandlerFactory(this);
        Log.info("ValidationDriver.ValidateInstanceFile", new Object[] {instanceFile});
        if (dispatcher.validate(IRIUtil.newInputSourceFromFilename(instanceFile), eh)) {
            Log.info("ValidationDriver.Success", new Object[] {instanceFile});
        } else {
            Log.error("ValidationDriver.Error",
                      new Object[] {
                          new Integer(dispatcher.getErrorCount()),
                          instanceFile
                      });
        }
    }

    ValidationDriver(ErrorHandler eh, boolean debug,
                     boolean quiet) throws Exception {
        this.eh = eh;
        this.debug = debug;
        this.quiet = quiet;
        setupReader();
    }
}
