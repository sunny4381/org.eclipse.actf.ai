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

import java.io.PrintStream;

/**
 * The <code>Log</code> is a singleton class for logging.
 */
public class Log {
    public static final int FATAL = 0;
    public static final int ERROR = 1;
    public static final int WARN = 2;
    public static final int INFO = 3;
    public static final int DEBUG = 4;

    private static final Object[] defArg = new Object[0];

    private static int logLevel = INFO;


    private static void print(int lv, String mes, Object[] args) {
        if (lv <= logLevel) {
            PrintStream w;
            String outstr = MessageFormatter.mes(mes, args);
            if (lv <= ERROR) {
                w = System.err;
            } else {
                w = System.out;
            }
            w.println(outstr);
        }
    }

    private static void print(int lv, String mes) {
        print(lv, mes, defArg);
    }

    public static void setLevel(int lv) {
        logLevel = lv;
    }

    public static void debug(String mes) {
        print(DEBUG, mes);
    }
    public static void debug(String mes, Object[] args) {
        print(DEBUG, mes, args);
    }
    public static void info(String mes) {
        print(INFO, mes);
    }
    public static void info(String mes, Object[] args) {
        print(INFO, mes, args);
    }
    public static void warn(String mes) {
        print(WARN, mes);
    }
    public static void warn(String mes, Object[] args) {
        print(WARN, mes, args);
    }
    public static void error(String mes) {
        print(ERROR, mes);
    }
    public static void error(String mes, Object[] args) {
        print(ERROR, mes, args);
    }
    public static void fatal(String mes) {
        print(FATAL, mes);
    }
    public static void fatal(String mes, Object[] args) {
        print(FATAL, mes, args);
    }
}

