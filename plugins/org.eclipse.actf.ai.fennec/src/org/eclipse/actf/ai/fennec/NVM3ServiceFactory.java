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

import org.eclipse.actf.ai.fennec.impl.NVM3ServiceImpl;
import org.eclipse.actf.model.dom.dombycom.IDocumentEx;


public class NVM3ServiceFactory {
    public static INVM3Service newNVM3Service(INVM3Entry entry, IDocumentEx doc) throws NVM3Exception {
        return new NVM3ServiceImpl(entry, doc);
    }

    public static INVM3Service newNVM3ServiceWithDefaultMetadata(IDocumentEx doc) {
        return new NVM3ServiceImpl(doc);
    }
}
