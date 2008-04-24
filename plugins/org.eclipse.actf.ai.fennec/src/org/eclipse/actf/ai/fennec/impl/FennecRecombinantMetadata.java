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
package org.eclipse.actf.ai.fennec.impl;

import java.util.List;

import org.eclipse.actf.ai.fennec.FennecException;
import org.eclipse.actf.ai.query.IQuery;
import org.w3c.dom.Node;


class FennecRecombinantMetadata extends FennecBundleMetadata {
    static public final int TYPE_SIMPLE = 0;
    static public final int TYPE_ATTACH = 1;
    static public final int TYPE_UNWRAP = 2;

    private final int type;
    private FennecMode nextMode;

    public FennecMode getNextMode() {
        return nextMode;
    }

    List expand(TreeItemFennec pItem, int trigger) throws FennecException {
        return nextMode.expand(pItem, null, trigger);
    }

    List buildItems(TreeItemFennec baseItem, Node baseNode, int trigger) throws FennecException {
        return nextMode.expand(baseItem, baseNode, trigger);
    }

    TreeItemFennec buildRootItem() throws FennecException {
        TreeItemFennec root = super.buildRootItem();
        if (root == null) {
            throw new FennecException("Fennec.EMPTY_PAGE", null);            
        }
        return root;
    }


    static FennecRecombinantMetadata newAttach(FennecServiceImpl fennecService,
                                             IQuery q, FennecMode mode,
                                             FennecMode nextMode,
                                             FennecMetadata[] mds) {
        FennecRecombinantMetadata md = new FennecRecombinantMetadata(fennecService,
                                                                 q, mode,
                                                                 nextMode, mds,
                                                                 TYPE_ATTACH);
        return md;
    }

    static FennecRecombinantMetadata newUnwrap(FennecServiceImpl fennecService,
                                             IQuery q, FennecMode mode,
                                             FennecMode nextMode,
                                             FennecMetadata[] mds) {
        FennecRecombinantMetadata md = new FennecRecombinantMetadata(fennecService,
                                                                 q, mode,
                                                                 nextMode, mds,
                                                                 TYPE_UNWRAP);
        return md;
    }

    private FennecRecombinantMetadata(FennecServiceImpl fennecService,
                                    IQuery q,
                                    FennecMode mode,
                                    FennecMode nextMode,
                                    FennecMetadata[] mds,
                                    int type) {
        super(fennecService, q, mode, mds);
        this.type = type;
        this.nextMode = nextMode;
        nextMode.setBaseMetadata(this);
    }
}

