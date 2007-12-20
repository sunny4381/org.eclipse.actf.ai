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

package org.eclipse.actf.ai.xmlstore.nvdl.reader;

import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLValidateAction;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;


/**
 * The <code>DTDSchemaLoader</code> is a special schema loader class for DTD.
 */
class DTDSchemaLoader implements NVDLValidateAction.SchemaLoader {
    // private final String baseIRI;
    // private Location loc;

    static class DTDValidatorImpl extends Validator {
        public void reset() {
        }

        public void validate(Source source,
                             Result result)
            throws SAXException, IOException {
        }

        public void setErrorHandler(ErrorHandler errorHandler) {
        }

        public ErrorHandler getErrorHandler() {
            return null;
        }

        public void setResourceResolver(LSResourceResolver lsResourceResolver) {
        }

        public LSResourceResolver getResourceResolver() {
            return null;
        }
    }

    static class DTDSchemaImpl extends Schema {

        public Validator newValidator() {
            // TODO Auto-generated method stub
            return null;
        }

        public ValidatorHandler newValidatorHandler() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    public Schema load(NVDLValidateAction validateAction, boolean forAttribute) {
        return null;
        // XMLReader reader = XMLReaderFactory.createXMLReader();
        // reader.setFeature("http://xml.org/sax/features/validation", true);

        // It's not allowed to put SAX into XMLReader.
        // We can't help transforming it into stream.
        // StringWriter writer = new 
    }

    DTDSchemaLoader() {
    	// this.baseIRI = null;
        // this.baseIRI = baseIRI;
        // this.loc = loc;
    }
}
