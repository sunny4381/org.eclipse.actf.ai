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

import java.util.ArrayList;
import java.util.List;

import javax.xml.validation.Schema;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;


/**
 * The <code>NVDLValidateAction</code> class represents
 * NVDL `validate' and the schema defined in it.
 */
public class NVDLValidateAction extends NVDLNoResultAction {
    private static class Option {
        final String name;
        final String arg;
        final boolean mustSupport;
        Option(String name, String arg, boolean mustSupport) {
            this.name = name;
            this.arg = arg;
            this.mustSupport = mustSupport;
        }
    }
    private List<Option> options = new ArrayList<Option>(0);
    public void addOption(String name, String arg, boolean mustSupport) {
        options.add(new Option(name, arg, mustSupport));
    }

    private String schemaType = null;
    public String getSchemaType() {
        return schemaType;
    }
    public void setSchemaType(String schemaType) {
        this.schemaType = schemaType;
    }
    private String schemaIRI = null;
    public String getSchemaIRI() {
        return schemaIRI;
    }
    public void setSchemaIRI(String schemaIRI) {
        this.schemaIRI = schemaIRI;
    }

    private Schema schema;
    private Schema schemaForAttribute;

    private SchemaLoader loader;
    public Schema getSchema(boolean forAttribute) throws NVDLException {
        if (forAttribute) {
            if (schemaForAttribute != null) return schemaForAttribute;
            schemaForAttribute = loader.load(this, true);
            return schemaForAttribute;
        } else {
            if (schema != null) return schema;
            schema = loader.load(this, false);
            return schema;
        }
    }

    public interface SchemaLoader {
        Schema load(NVDLValidateAction action,
                    boolean forAttribute) throws NVDLException;
    }
    public void setSchemaLoader(SchemaLoader loader) {
        this.loader = loader;
    }

    public void setSchema(Schema schema, boolean forAttribute) {
        if (forAttribute) {
            this.schemaForAttribute = schema;
        } else {
            this.schema = schema;
        }
    }

    public boolean isSchamaSpecified() {
        return ((schema != null)
                || (schemaForAttribute != null)
                || (getSchemaIRI() != null));
    }

    public String toString() {
        StringBuffer r = new StringBuffer();
            r.append("Validate");
            if (schemaIRI != null) {
                r.append("(");
                r.append(schemaIRI);
                r.append(")");
            }
        return r.toString();
    }

    public NVDLValidateAction(String name, String useModeName,
                              NVDLRule belongingRule) {
        super(name, useModeName, belongingRule);
    }
}
