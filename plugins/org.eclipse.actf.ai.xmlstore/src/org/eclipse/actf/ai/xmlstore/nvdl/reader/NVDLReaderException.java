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

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;

/**
 * The <code>NVDLReaderException</code> is an exception class
 * for NVDL reader.
 */
public class NVDLReaderException extends NVDLException {
    private static final long serialVersionUID = 3266774582350581335L;

    public NVDLReaderException(String message) {
        super(message);
    }

    public NVDLReaderException(String message, Object[] args) {
        super(message, args);
    }

    public NVDLReaderException(Exception e) {
        super(e);
    }
}
