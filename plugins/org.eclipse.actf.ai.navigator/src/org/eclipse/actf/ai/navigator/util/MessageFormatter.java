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
package org.eclipse.actf.ai.navigator.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

public class MessageFormatter {
    private ResourceBundle resourceBundle = null;

    private void setResourceBundle(String pkgName) {
        try {
            resourceBundle = ResourceBundle.getBundle(pkgName);
        } catch (MissingResourceException e) {
            resourceBundle = null;
        }
    }

    public MessageFormatter(String pkgName) {
        setResourceBundle(pkgName);
    }

    private String getFormat(String propName) {
        if (resourceBundle == null) {
            return propName;
        }
        String format;
        try {
            format = resourceBundle.getString(propName);
        } catch (MissingResourceException e) {
            return propName;
        }
        if (format != null)
            return format;
        return propName;
    }

    public String mes(String propName, Object[] args) {
        String format = getFormat(propName);
        String message = MessageFormat.format(format, args);
        return message;
    }

    public String mes(String propName, Object obj1) {
        return mes(propName, new Object[] { obj1 });
    }

    public String mes(String propName, Object obj1, Object obj2) {
        return mes(propName, new Object[] { obj1, obj2 });
    }

    public String mes(String propName, Object obj1, Object obj2, Object obj3) {
        return mes(propName, new Object[] { obj1, obj2, obj3 });
    }

    public String mes(String propName, Object obj1, Object obj2, Object obj3, Object obj4) {
        return mes(propName, new Object[] { obj1, obj2, obj3, obj4 });
    }

    public String mes(String propName) {
        return getFormat(propName);
    }
    
    public String concat(String... args){
        if (args.length == 0)
            return "";
        if (args.length == 1)
            return args[0];
        
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < args.length-1; i++) {
            buf.append(args[i]);
            buf.append(" ");
        }
        buf.append(args[args.length-1]);
        return buf.toString();
    }
}
