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
 * The <code>NVDLConst</code> class holds constants defined in NVDL.
 */
public class NVDLConst {
    static public final String NVDL_NS = "http://purl.oclc.org/dsdl/nvdl/ns/structure/1.0";

    static public final String INSTANCE_NS = "http://purl.oclc.org/dsdl/nvdl/ns/instance/1.0";
    static public final String INSTANCE_PREFIX_BASE = "nvdlinstance";
    static public final String VIRTUALELEMENT_NAME = "virtualElement";
    static public final String PLACEHOLDER_NAME = "placeholder";

    static public final String INSTANCE_REC_NS = "http://purl.oclc.org/dsdl/nvdl/ns/instance/2.0";
    static public final String INSTANCE_REC_PREFIX_BASE = "nir";
    static public final String SLOT_NODE_START_NAME = "slot-node";
    static public final String SLOT_NODE_END_NAME = "slot-node-end";
    static public final String SLOT_NODE_ID_ATTR = "slot-node-id";
    static public final String SECTION_ID_ATTR = "sect-id";
}
