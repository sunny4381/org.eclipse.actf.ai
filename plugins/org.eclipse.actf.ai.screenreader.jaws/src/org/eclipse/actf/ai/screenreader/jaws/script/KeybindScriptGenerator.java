/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.screenreader.jaws.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class KeybindScriptGenerator {
    private String defaultPath = ".";

    private String userPath = ".";

    //private String userName = "";

    private String appName;

    private String flagName;

    private String keySettingFile = "keysettings.xml";

    private Vector<Key> keys = new Vector<Key>();

    //private static final int TYPE_ALL = 7;

    private static final int TYPE_COMMON = 1;

    private static final int TYPE_QUICK = 2;

    private static final int TYPE_VIRTUAL = 4;

    private Vector<Keymap> keymaps_common = new Vector<Keymap>();

    private Vector<Keymap> keymaps_quick = new Vector<Keymap>();

    private Vector<Keymap> keymaps_virtual = new Vector<Keymap>();

    public static void main(String args[]) {
        KeybindScriptGenerator ksg = new KeybindScriptGenerator();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-dp")) {
                ksg.defaultPath = args[++i];
            } else if (args[i].equals("-up")) {
                ksg.userPath = args[++i];
            } else if (args[i].equals("-name")) {
                ksg.appName = args[++i];
                ksg.flagName = "g_" + ksg.appName.replaceAll(" ", "") + "Flag";
            } else if (args[i].equals("-key")) {
                ksg.keySettingFile = args[++i];
            }
        }

        ksg.generate();
    }

    public void generate() {
        readKeySettings();
        readDefaultKeymap();
        writeScript();
    }

    private void readKeySettings() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(new FileInputStream(keySettingFile));
            Document doc = builder.parse(input);

            String[] tags = new String[] { "Commands", "Functions" };
            for (int i = 0; i < tags.length; i++) {
                NodeList commandsList = doc.getElementsByTagName(tags[i]);
                for (int j = 0; j < commandsList.getLength(); j++) {
                    Node commands = commandsList.item(j);
                    NodeList commandList = commands.getChildNodes();
                    for (int k = 0; k < commandList.getLength(); k++) {
                        Node command = commandList.item(k);
                        if (command.getNodeType() == Node.ELEMENT_NODE) {
                            Element commandElement = (Element) command;
                            NodeList keyList = command.getChildNodes();
                            for (int l = 0; l < keyList.getLength(); l++) {
                                Node keyNode = keyList.item(l);
                                if ("key".equals(keyNode.getNodeName())) {
                                    Key key = new Key(keyNode, commandElement);
                                    keys.add(key);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readDefaultKeymap() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(defaultPath
                    + "\\Default.JKM")));
            String line;
            boolean flag = false;
            int type = TYPE_COMMON;

            while ((line = br.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) == '[') {
                    if (line.toLowerCase().equals("[common keys]")) {
                        flag = true;
                        type = TYPE_COMMON;
                    } else if (line.toLowerCase().equals("[quick navigation keys]")) {
                        flag = true;
                        type = TYPE_QUICK;
                    } else if (line.toLowerCase().equals("[virtual keys]")) {
                        flag = true;
                        type = TYPE_VIRTUAL;
                    } else
                        flag = false;
                }
                if (line.length() > 0 && line.charAt(0) != ';' && line.charAt(0) != '[' && flag
                        && !line.toLowerCase().startsWith("braille")) {

                    Keymap keymap = new Keymap(line);
                    switch (type) {
                    case TYPE_COMMON:
                        keymaps_common.add(keymap);
                        break;
                    case TYPE_QUICK:
                        keymaps_quick.add(keymap);
                        break;
                    case TYPE_VIRTUAL:
                        keymaps_virtual.add(keymap);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeScript() {
        try {
            File jss = new File(userPath + "\\" + appName + ".jss");
            PrintWriter jssw = new PrintWriter(new FileOutputStream(jss));
            writeFunctions(jssw);
            jssw.close();

            File jkm = new File(userPath + "\\" + appName + ".jkm");
            PrintWriter jkmw = new PrintWriter(new FileOutputStream(jkm));
            writeKeyMap(jkmw);
            jkmw.close();

            File jsh = new File(userPath + "\\" + appName + ".jsh");
            PrintWriter jshw = new PrintWriter(new FileOutputStream(jsh));
            writeGlobals(jshw);
            jshw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeGlobals(PrintWriter pw) {
        pw.println("Globals");
        pw.println("  int " + flagName + ",");
        pw.println("  int g_aiBrowserSayAllFlag,");
        pw.println("  handle g_aiBrowserSayAllWindow,");
        pw.println("  int g_aiBrowserSayAllMessage");
    }

    private void writeFixedCode(PrintWriter pw) {
        pw.print(";--------------------------------------------------------------------------------\r\n"
                 + "Script AiBrowserSayAllOff ()\r\n"
                 + "  If g_aiBrowserSayAllFlag Then\r\n"
                 + "    let g_aiBrowserSayAllFlag = 0\r\n"
                 + "    StopSpeech()\r\n"
                 + "    Delay(5, 1)\r\n"
                 + "  EndIf\r\n"
                 + "EndScript\r\n"
                 + "\r\n"
                 + "Void Function FocusChangedEvent(Handle hCurWin, Handle hPrevWin)\r\n"
                 + "If g_aiBrowserSayAllFlag Then\r\n"
                 + "    return\r\n"
                 + "  Else\r\n"
                 + "    FocusChangedEvent(hCurWin, hPrevWin)\r\n"
                 + "  EndIf\r\n"
                 + "EndFunction\r\n"
                 + "\r\n"
                 + "void function FocusChangedEventEx(Handle hwndFocus, int nObject, int nChild,\r\n"
                 + "                                  Handle hwndPrevFocus, int nPrevObject, int nPrevChild,\r\n"
                 + "	                          int nChangeDepth)\r\n"
                 + "  If g_aiBrowserSayAllFlag Then\r\n"
                 + "    return\r\n"
                 + "  Else\r\n"
                 + "    FocusChangedEventEx(hwndFocus, nObject, nChild, hwndPrevFocus, nPrevObject, nPrevChild, nChangeDepth)\r\n"
                 + "  EndIf\r\n"
                 + "EndFunction\r\n"
                 + "\r\n"
                 + "int function FocusRedirected(Handle focusWindow, Handle prevWindow)\r\n"
                 + "  If g_aiBrowserSayAllFlag Then\r\n"
                 + "    return 1\r\n"
                 + "  Else\r\n"
                 + "    FocusRedirected(focusWindow, prevWindow)\r\n"
                 + "  EndIf\r\n"
                 + "EndFunction\r\n"
                 + "\r\n"
                 + "Void Function SayTutorialHelp (int iObjType, int IsScriptKey)\r\n"
                 + "  If g_aiBrowserSayAllFlag Then\r\n"
                 + "    return\r\n"
                 + "  Else\r\n"
                 + "    SayTutorialHelp(iObjType, IsScriptKey)\r\n"
                 + "  EndIf\r\n"
                 + "EndFunction\r\n"
                 + "\r\n"
                 + "Void Function SendaiBrowserMessage (Int param)\r\n"
                 + "  SendMessage(g_aiBrowserSayAllWindow, g_aiBrowserSayAllMessage, param, 0);\r\n"
                 + "EndFunction\r\n"
                 + "\r\n"
                 + "Void Function SayAllStoppedEvent ()\r\n"
                 + "  PCCursor()\r\n"
                 + "  If (!SayAllInProgress()) Then\r\n"
                 + "    SendaiBrowserMessage(0)\r\n"
                 + "    PCCursor()\r\n"
                 + "  EndIf\r\n"
                 + "EndFunction\r\n"
                 + "\r\n"
                 + "Script JAWSCursor ()\r\n"
                 + "EndScript\r\n"
                 + "\r\n"
                 + "Script InvisibleCursor ()\r\n"
                 + "EndScript\r\n"
                 + "\r\n"
                 + "Void Function ObserveSpeechFunction ()\r\n"
                 + "    If (SayAllInProgress()) Then\r\n"
                 + "    Else\r\n"
                 + "      If g_aiBrowserSayAllFlag != 1 Then\r\n"
                 + "        let g_aiBrowserSayAllWindow = FindWindow(GetAppMainWindow(GetFocus()), \"Jaws-aiBrowser-Communication\", \"\")\r\n"
                 + "        ;SayInteger(g_aiBrowserSayAllWindow)\r\n"
                 + "        ;SetActiveCursor(1)\r\n"
                 + "        ;MoveToWindow(g_aiBrowserSayAllWindow)\r\n"
                 + "        let g_aiBrowserSayAllMessage = RegisterWindowMessage(\"WM_JAWS_AIBROWSER_MESSAGE\")\r\n"
                 + "        let g_aiBrowserSayAllFlag = 1\r\n"
                 + "        PCCursor()\r\n"
                 + "        SetFocus(g_aiBrowserSayAllWindow)\r\n"
                 + "        PCCursor()\r\n"
                 + "        Delay(5, 0)\r\n"
                 + "      Else\r\n"
                 + "        If (GetFocus() != g_aiBrowserSayAllWindow) Then\r\n"
                 + "          SetFocus(g_aiBrowserSayAllWindow)\r\n"
                 + "        EndIf\r\n"
                 + "        PCCursor()\r\n"
                 + "      EndIf\r\n"
                 + "\r\n"
                 + "      PCCursor()\r\n"
                 + "      SayAll(0)\r\n"
                 + "      ;SkimRead()\r\n"
                 + "      PCCursor()\r\n"
                 + "    EndIf\r\n"
                 + "EndFunction\r\n"
                 + "\r\n"
                 + "Script ObserveSpeech ()\r\n"
                 + "  ObserveSpeechFunction()\r\n"
                 + "EndScript\r\n"
                 + ";--------------------------------------------------------------------------------\r\n"
                 );
    }

    private void outputPerformScript(PrintWriter pw, String func, String funcExec) {
        if (func.endsWith(")"))
            pw.println("    PerformScript " + funcExec + "");
        else
            pw.println("    PerformScript " + func + "()");
    }

    private void outputJawsSayAllStop(PrintWriter pw, Key key) {
        if (key.jawsSayAllStop) {
            pw.println("    If g_aiBrowserSayAllFlag Then");
            if (key.jawsSayAllStopIgnore) {
                pw.println("      return");
            } else {
                pw.println("      PerformScript AiBrowserSayAllOff()");
                pw.println("      SendaiBrowserMessage(1)");
                pw.println("      Delay(5, 1)");
            }
            pw.println("    EndIf");
        }
    }

    private void writeFunctions(PrintWriter pw) {
        pw.println("include \"" + appName + ".jsh\"");
        pw.println();
        pw.println("Script JawsOn ()");
        pw.println("  let " + flagName + " = 1");
        pw.println("EndScript");
        pw.println();
        pw.println("Script JawsOff ()");
        pw.println("  let " + flagName + " = 0");
        pw.println("EndScript");
        pw.println();
        writeFixedCode(pw);
        pw.println();
        pw.println();

        HashSet<String> generated = new HashSet<String>();
        for (int i = 0; i < keys.size(); i++) {
            Key key = keys.get(i);
            
            if (!key.jawsScript)
                continue;
            
            String func = getFuncName(key, TYPE_COMMON);

            if (func != null) {
                pw.println("Script " + key.toFuncString() + "()");
                if (key.jawsHandle) {
                    outputPerformScript(pw, func, func);
                } else {
                    pw.println("  If " + flagName + " Then");
                    outputPerformScript(pw, func, func);
                    pw.println("  Else");
                    outputJawsSayAllStop(pw, key);
                    pw.println("    TypeKey(\"" + key.toTypeString() + "\")");
                    pw.println("  EndIf");
                }
                pw.println("EndScript");
                pw.println();
                pw.println();
                continue;
            }

            func = getFuncName(key, TYPE_COMMON | TYPE_QUICK | TYPE_VIRTUAL);
            String funcName = "";
            String funcExec = "";
            if (func != null) {
                funcName = func.replaceAll("\\([0-9]\\)", "(int n)");
                funcExec = func.replaceAll("\\([0-9]\\)", "(n)");
            }

            if (!generated.contains(funcName)) {
                generated.add(funcName);

                if (func != null) {
                    if (func.endsWith(")"))
                        pw.println("Script " + funcName);
                    else
                        pw.println("Script " + func + "()");
                    pw.println("  If " + flagName + " Then");
                    outputPerformScript(pw, func, funcExec);
                    pw.println("  Else");
                    outputJawsSayAllStop(pw, key);
                    pw.println("    TypeCurrentScriptKey()");
                    pw.println("  EndIf");
                    pw.println("EndScript");
                    pw.println();
                    pw.println();
                }
            }

            pw.println("Script " + key.toFuncString() + "()");
            pw.println("  If " + flagName + " Then");
            pw.println("    SayString(\"" + key.toString() + "\")");
            pw.println("  EndIf");
            outputJawsSayAllStop(pw, key);
            if (key.jawsKey != null) {
                if (key.jawsKey.currentScript) {
                    pw.println("  TypeCurrentScriptKey()");
                } else {
                    pw.println("  TypeKey(\"" + key.jawsKey.toTypeString() + "\")");
                }
            } else {
                pw.println("  TypeKey(\"" + key.toTypeString() + "\")");
            }
            pw.println("EndScript");
            pw.println();
            pw.println();
        }
    }

    private String getFuncName(Key key, int type) {
        String ret;
        if (isIt(type, TYPE_COMMON)) {
            ret = getFucnName(keymaps_common, key);
            if (ret != null)
                return ret;
        }
        if (isIt(type, TYPE_QUICK)) {

            ret = getFucnName(keymaps_quick, key);
            if (ret != null)
                return ret;
        }
        if (isIt(type, TYPE_VIRTUAL)) {
            ret = getFucnName(keymaps_virtual, key);
            if (ret != null)
                return ret;
        }
        return null;
    }

    private boolean isIt(int type, int mask) {
        return (type & mask) == mask;
    }

    private String getFucnName(Vector<Keymap> keymaps, Key key) {
        for (int i = 0; i < keymaps.size(); i++) {
            if (keymaps.get(i).equals(key)) {
                return keymaps.get(i).scriptName;
            }
        }
        return null;
    }

    private void writeKeyMap(PrintWriter pw) {
        pw.println("[Common keys]");
        for (int i = 0; i < keys.size(); i++) {
            Key key = keys.get(i);
            if (!key.jawsScript) continue;
            pw.println(key.toString() + "=" + key.toFuncString());
        }
    }
}
