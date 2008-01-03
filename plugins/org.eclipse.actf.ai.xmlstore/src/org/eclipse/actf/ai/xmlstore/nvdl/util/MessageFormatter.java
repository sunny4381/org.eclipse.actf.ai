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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;
import org.eclipse.actf.ai.xmlstore.nvdl.dispatcher.NVDLDispatcherException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.Location;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModel;
import org.xml.sax.SAXParseException;


/**
 * The <code>MessageFormatter</code> class formats output messages.
 */
public class MessageFormatter {
    static ResourceBundle resourceBundle = null;

    static public void setResourceBundle(String pkgName) {
        try {
            resourceBundle = ResourceBundle.getBundle(pkgName);
        } catch (MissingResourceException e) {
            resourceBundle = null;
        }
    }

    static private String getFormat(String propName) {
        if (resourceBundle == null) {
            return propName;
        }
        String format;
        try {
            format = resourceBundle.getString(propName);
        } catch (MissingResourceException e) {
            return propName;
        }
        if (format != null) return format;
        return propName;
    }

    static public String model(NVDLModel model, String message) {
        Location modelLocation = model.getLocation();
        if (modelLocation == null) {
            return MessageFormat.format("{0} : {1}",
                                        new Object[] {model.toString(),
                                                      message});
        } else {
            return MessageFormat.format("{0}#{1}, Line:{2,number,integer}, Col:{3,number,integer} : {4}",
                                        new Object[] {model.toString(),
                                                      modelLocation.iri,
                                                      new Integer(modelLocation.line),
                                                      new Integer(modelLocation.pos),
                                                      message});
        }
    }

    static public String locAndModel(Location location, NVDLModel model,
                                     String message) {
        Location modelLocation = model.getLocation();
        if (modelLocation == null) {
            return MessageFormat.format("{0} Line:{1,number,integer}, Col:{2,number,integer}, ({3} ): {4}",
                                        new Object[] {location.iri,
                                                      new Integer(location.line),
                                                      new Integer(location.pos),
                                                      model.toString(),
                                                      message});
        } else {
            return MessageFormat.format("{0} Line:{1,number,integer}, Col:{2,number,integer}, ({3} {4}, Line:{5,number,integer}, Col:{6,number,integer}): {7}",
                                        new Object[] {location.iri,
                                                      new Integer(location.line),
                                                      new Integer(location.pos),
                                                      model.toString(),
                                                      modelLocation.iri,
                                                      new Integer(modelLocation.line),
                                                      new Integer(modelLocation.pos),
                                                      message});
        }
    }

    static public String loc(Location location, String message) {
        return MessageFormat.format("{0} Line:{1,number,integer}, Col:{2,number,integer}: {3}",
                                    new Object[] {location.iri,
                                                  new Integer(location.line),
                                                  new Integer(location.pos),
                                                  message});
    }

    static public String mes(String propName) {
        return getFormat(propName);
    }

    static public String mes(String propName, Object[] args) {
        String format = getFormat(propName);
        String message = MessageFormat.format(format, args);
        return message;
    }

    static public String exception(Exception e) {
        Location loc = null;
        NVDLModel model = null;
        Object[] args = null;
        String message;
        Exception messageException = e;
        if (e instanceof SAXParseException) {
            SAXParseException se = (SAXParseException) e;
            int line = se.getLineNumber();
            int col = se.getColumnNumber();
            String systemId = se.getSystemId();
            if (line > 0) {
                loc = new Location(line, col, systemId);
            }
            e = se.getException();
            if (e == null) e = se;
        }
        if (e instanceof NVDLException) {
            NVDLException ne = (NVDLException) e;
            args = ne.getMessageArguments();
            if (e instanceof NVDLDispatcherException) {
                NVDLDispatcherException nvdlDispatcherException = (NVDLDispatcherException) e;
                model = nvdlDispatcherException.getCurrentModel();
            }
        }
        if (args != null) {
            message = mes(messageException.getMessage(), args);
        } else {
            message = messageException.getLocalizedMessage();
            if (message == null) {
                message = messageException.getMessage();
            }
            if (message == null) {
            	message = messageException.toString();
            }
        }
        if (loc != null) {
            if (model != null) {
                message = locAndModel(loc, model, message);
            } else {
                message = loc(loc, message);
            }
        } else if (model != null) {
            message = model(model, message);
        }
        return message;
    }
}
