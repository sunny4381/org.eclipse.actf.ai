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

import org.eclipse.actf.ai.xmlstore.nvdl.util.IRIUtil;

/**
 * The <code>Location</code> encapsulates the origin of each model.
 */
public class Location {
    public final int line;
    public final int pos;
    public final String iri;

    public String toString() {
        StringBuffer r = new StringBuffer(IRIUtil.IRIToFilename(iri));
        r.append("(");
        if (line > 0) {
            r.append(line);
        } else {
            r.append("NoLine");
        }
        if (pos > 0) {
            r.append(":" + pos);
        }
        r.append(")");

        return r.toString();
    }

    public Location() {
        line = -1;
        pos = -1;
        iri = "";
    }
    public Location(int line) {
        this.line = line;
        this.pos = -1;
        this.iri = "";
    }
    public Location(int line, int pos) {
        this.line = line;
        this.pos = pos;
        this.iri = "";
    }
    public Location(int line, int pos, String iri) {
        this.line = line;
        this.pos = pos;
        if (iri == null)
            this.iri = "";
        else
            this.iri = iri;
    }
}
