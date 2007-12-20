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

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;


public class Benchmark extends ValidationDriver {
    public long maxMemory;
    private int counter;
    public ContentHandler createContentHandler(NVDLAction action) {
        //return super.createContentHandler(action);
        return null;
    }
    public void nextActionHandler(NVDLAction action, Locator l) {
        if (counter > 10) {
            Runtime r = Runtime.getRuntime();
            r.gc();
            long inused = r.totalMemory() - r.freeMemory();
            if (inused > maxMemory) maxMemory = inused;
            super.nextActionHandler(action, l);
            counter = 0;
        } else {
            counter++;
        }
    }
    Benchmark(ErrorHandler eh, boolean debug, boolean quiet) throws Exception {
        super(eh, debug, quiet);
    }
}
