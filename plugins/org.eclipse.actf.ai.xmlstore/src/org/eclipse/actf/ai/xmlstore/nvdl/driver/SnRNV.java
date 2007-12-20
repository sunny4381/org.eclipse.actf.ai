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

package org.eclipse.actf.ai.xmlstore.nvdl.driver;

import java.util.Locale;

import org.eclipse.actf.ai.xmlstore.nvdl.util.Log;
import org.eclipse.actf.ai.xmlstore.nvdl.util.MessageErrorHandler;
import org.eclipse.actf.ai.xmlstore.nvdl.util.MessageFormatter;
import org.xml.sax.ErrorHandler;


/**
 * The <code>SnRNV</code> is a driver for command line interface.
 */
public class SnRNV {
    private static String targetDirectory = "";
    private static boolean debug = false;
    private static boolean quiet = false;
    private static boolean dispatch = false;
    private static boolean reconstruction = false;

    static private void usage() {
        Log.error("SnRNV.Usage");
    }

    static private void printException(Exception e) {
        Log.error(MessageFormatter.exception(e));
    }

    static private void validate(String nvdlFile,
                                 String instanceFile,
                                 ErrorHandler eh) {
        try {
            ValidationDriver d = new ValidationDriver(eh, debug, quiet);
            // ValidationDriver d = new Benchmark(eh, debug, quiet);
            d.validate(nvdlFile, instanceFile);
            // System.out.println("Used memory:" + ((Benchmark) d).maxMemory);
        } catch (Exception e) {
            printException(e);
        }
    }

    static private void dispatch(String nvdlFile,
                                 String instanceFile,
                                 ErrorHandler eh) {
        try {
            DispatchDriver d = new DispatchDriver(eh, debug, quiet);
            d.dispatch(nvdlFile, instanceFile, targetDirectory);
        } catch (Exception e) {
            printException(e);
        }
    }

    static private void reconstruct(String nvdlFile,
                                    String instanceFile,
                                    ErrorHandler eh) {
        try {
            ReconstructionDriver d = new ReconstructionDriver(eh, debug, quiet);
            d.reconstruct(nvdlFile, instanceFile, targetDirectory);
        } catch (Exception e) {
            printException(e);
        }
    }


    static private int setTargetDirectory(String[] args, int i) {
        if (i == (args.length - 1)) {
            Log.error("SnRNV.TargetDirectoryNotSpecifiedError");
            System.exit(255);
        }
        targetDirectory = args[++i];
        return i;
    }

    static private int setLocale(String[] args, int i) {
        if (i == (args.length - 1)) {
            Log.error("SnRNV.LocaleRequireArgumentError");
            System.exit(255);
        }
        Locale locale;
        String iso646Code = args[++i];
        int idx = iso646Code.indexOf('_');
        if (idx > 0) {
            String lang = iso646Code.substring(0, idx);
            String country = iso646Code.substring(idx + 1);
            locale = new Locale(lang, country);
        } else {
            locale = new Locale(iso646Code);
        }
        Locale.setDefault(locale);
        // set the resource again.
        MessageFormatter.setResourceBundle("org.eclipse.actf.ai.xmlstore.nvdl.driver.message.Messages");
        return i;
    }

    static public void main(String[] args) {
        ErrorHandler eh = new MessageErrorHandler();
        int i;

        MessageFormatter.setResourceBundle("org.eclipse.actf.ai.xmlstore.nvdl.driver.message.Messages");

        for (i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                if (args[i].equals("-d")) {
                    Log.setLevel(Log.DEBUG);
                    debug = true;
                } else if (args[i].equals("-v")) {
                    Log.setLevel(Log.INFO);
                } else if (args[i].equals("-q")) {
                    Log.setLevel(Log.WARN);
                    quiet = true;
                } else if (args[i].equals("-p")) {
                    i = setTargetDirectory(args, i);
                    dispatch = true;
                } else if (args[i].equals("-r")) {
                    i = setTargetDirectory(args, i);
                    reconstruction = true;
                } else if (args[i].equals("-locale")) {
                    i = setLocale(args, i);
                } else {
                    Log.error("SnRNV.InvalidOptionError", new Object[] {args[i]});
                    System.exit(255);
                }
            } else {
                break;
            }
        }

        if ((args.length - i) < 2) {
            usage();
            System.exit(255);
        }

        String nvdlFile = args[i];
        String instanceFile = args[i + 1];

        if (dispatch) {
            dispatch(nvdlFile, instanceFile, eh);
        } else if (reconstruction) {
            reconstruct(nvdlFile, instanceFile, eh);
        } else {
            validate(nvdlFile, instanceFile, eh);
        }

        System.exit(0);
    }
}
