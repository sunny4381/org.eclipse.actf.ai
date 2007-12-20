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

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;

/**
 * The <code>NVDLModelException</code> is an exception class
 * for NVDL model processings.
 */
public class NVDLModelException extends NVDLException {
    private static final long serialVersionUID = -3285734522668289094L;
	
    public NVDLModelException(String message) {
        super(message);
    }
    public NVDLModelException(Exception e) {
        super(e);
    }
}
