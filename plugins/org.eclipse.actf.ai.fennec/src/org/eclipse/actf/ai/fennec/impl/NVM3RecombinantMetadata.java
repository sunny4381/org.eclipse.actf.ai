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

import org.eclipse.actf.ai.fennec.NVM3Exception;
import org.eclipse.actf.ai.query.IQuery;
import org.w3c.dom.Node;


class NVM3RecombinantMetadata extends NVM3BundleMetadata {
    static public final int TYPE_SIMPLE = 0;
    static public final int TYPE_ATTACH = 1;
    static public final int TYPE_UNWRAP = 2;

    private final int type;
    private NVM3Mode nextMode;

    public NVM3Mode getNextMode() {
        return nextMode;
    }

    List expand(TreeItemNVM3 pItem, int trigger) throws NVM3Exception {
        return nextMode.expand(pItem, null, trigger);
    }

    List buildItems(TreeItemNVM3 baseItem, Node baseNode, int trigger) throws NVM3Exception {
        return nextMode.expand(baseItem, baseNode, trigger);
    }

    TreeItemNVM3 buildRootItem() throws NVM3Exception {
        TreeItemNVM3 root = super.buildRootItem();
        if (root == null) {
            throw new NVM3Exception("NVM3.EMPTY_PAGE", null);            
        }
        return root;
    }


    static NVM3RecombinantMetadata newAttach(NVM3ServiceImpl nvm3Service,
                                             IQuery q, NVM3Mode mode,
                                             NVM3Mode nextMode,
                                             NVM3Metadata[] mds) {
        NVM3RecombinantMetadata md = new NVM3RecombinantMetadata(nvm3Service,
                                                                 q, mode,
                                                                 nextMode, mds,
                                                                 TYPE_ATTACH);
        return md;
    }

    static NVM3RecombinantMetadata newUnwrap(NVM3ServiceImpl nvm3Service,
                                             IQuery q, NVM3Mode mode,
                                             NVM3Mode nextMode,
                                             NVM3Metadata[] mds) {
        NVM3RecombinantMetadata md = new NVM3RecombinantMetadata(nvm3Service,
                                                                 q, mode,
                                                                 nextMode, mds,
                                                                 TYPE_UNWRAP);
        return md;
    }

    private NVM3RecombinantMetadata(NVM3ServiceImpl nvm3Service,
                                    IQuery q,
                                    NVM3Mode mode,
                                    NVM3Mode nextMode,
                                    NVM3Metadata[] mds,
                                    int type) {
        super(nvm3Service, q, mode, mds);
        this.type = type;
        this.nextMode = nextMode;
        nextMode.setBaseMetadata(this);
    }
}

