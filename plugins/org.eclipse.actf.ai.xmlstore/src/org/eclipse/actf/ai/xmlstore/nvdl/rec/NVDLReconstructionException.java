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

package org.eclipse.actf.ai.xmlstore.nvdl.rec;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;

/**
 * The <code>NVDLException</code> is the base exception class for NVDL.
 */
public class NVDLReconstructionException extends NVDLException {
    private static final long serialVersionUID = -3293151852894650069L;

    public NVDLReconstructionException(String message) {
        super(message);
    }
}
