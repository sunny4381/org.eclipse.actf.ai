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

package org.eclipse.actf.ai.xmlstore.nvdl.dispatcher;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModel;

/**
 * The <code>NVDLDispatcherException</code> is an exception class
 * for the dispatcher.
 */
public class NVDLDispatcherException extends NVDLException {
    private static final long serialVersionUID = -6745352761744925844L;

    private final NVDLModel currentModel;

    public NVDLModel getCurrentModel() {
        return currentModel;
    }

    NVDLDispatcherException(Exception e, NVDLModel model) {
        super(e);
        this.currentModel = model;
    }
}
