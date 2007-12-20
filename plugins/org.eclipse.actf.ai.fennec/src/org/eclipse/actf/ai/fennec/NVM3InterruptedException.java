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
package org.eclipse.actf.ai.fennec;

public class NVM3InterruptedException extends NVM3Exception {
    private static final long serialVersionUID = 503781748413609768L;

    public NVM3InterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NVM3InterruptedException(String message) {
        super(message, null);
    }
}
