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

package org.eclipse.actf.ai.xmlstore.nvdl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.InputSource;

/**
 * The <code>IRIUtil</code> is a utility class for IRI.
 */
public class IRIUtil {
    static public final String fileSchema = "file:";

    static public InputSource newInputSourceFromFilename(String fileName)
    	throws FileNotFoundException {
        InputSource is = new InputSource(new FileInputStream(fileName));
        is.setSystemId(fileSchema + fileName);
        return is;
    }

    static public String IRIToFilename(String iri) {
        if (iri.startsWith(fileSchema)) {
            iri = iri.substring(fileSchema.length());
            URI uri;
            try {
                uri = new URI(iri);
            } catch (URISyntaxException e) {
                return iri;
            }
            return uri.getPath().replace('/', File.separatorChar);
        }
        return iri;
    }
}
