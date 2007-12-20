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

/**
 * The <code>NVDLModelVisitor</code> interface is for visitor pattern.
 */
public interface NVDLModelVisitor {
    NVDLModel visitNVDLMode(NVDLMode mode) throws NVDLModelException;
    NVDLModel visitNVDLNoResultAction(NVDLNoResultAction action) throws NVDLModelException;
    NVDLModel visitNVDLResultAction(NVDLResultAction action) throws NVDLModelException;
    NVDLModel visitNVDLRule(NVDLRule rule) throws NVDLModelException;
    NVDLModel visitNVDLRules(NVDLRules rules) throws NVDLModelException;
}
