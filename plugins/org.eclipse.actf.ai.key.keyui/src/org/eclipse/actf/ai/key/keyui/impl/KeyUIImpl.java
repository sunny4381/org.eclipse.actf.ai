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

package org.eclipse.actf.ai.key.keyui.impl;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.actf.ai.key.keyui.Messages;
import org.eclipse.actf.ai.key.keyui.impl.KeyUIImpl.KC.Type;
import org.eclipse.actf.ai.navigator.IBrowserControl;
import org.eclipse.actf.ai.navigator.IManipulator;
import org.eclipse.actf.ai.navigator.INavigatorUI;
import org.eclipse.actf.ai.navigator.ui.NavigatorUIUtil;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.IXMLStoreService;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.eclipse.actf.ai.xmlstore.XMLStoreServiceUtil;
import org.eclipse.actf.util.win32.comclutch.ComPlugin;
import org.eclipse.actf.util.win32.keyhook.IKeyHook;
import org.eclipse.actf.util.win32.keyhook.IKeyHookListener;
import org.eclipse.actf.util.win32.keyhook.ISendEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




public class KeyUIImpl implements IKeyHookListener, IManipulator {
    private static boolean initialized = false;

    public static final String PREFERENCES_NS = "http://www.ibm.com/xmlns/prod/AcTF/aiBrowser/preferences/1.0";

    public static final int CMD_PASSTHROUGH = -1;

    public static final int CMD_NOOP = 9999;

    // for demo
    public static final int CMD_SPEED_UP = 1300;

    public static final int CMD_SPEED_DOWN = 1301;

    public static final int CMD_PLAY_NEXT = 1302;

    public static final int CMD_PLAY_PREV = 1303;

    public static final int CMD_START_RECORDING = 1304;

    private static final int VK_RETURN = 13;

    private IManipulator.Mode mode = null;

    private IBrowserControl browserControl;

    private INavigatorUI navigatorUI;

    private HashMap<KeyEntry, KC> keyConfigMapForTreeNavigation = new HashMap<KeyEntry, KC>();

    private static IKeyHook keyHook;

    private static ISendEvent sendEvent;

    public KeyUIImpl() {
        if (!initialized) {
            initialized = true;
            initialize();
        }
    }

    public void initialize() {
        keyHook = ComPlugin.getDefault().newKeyHook(this);
        sendEvent = ComPlugin.getDefault().newSendEvent();

        IXMLStoreService xmlStoreService = XMLStoreServiceUtil.getXMLStoreService();
        IXMLSelector selector = xmlStoreService.getSelectorWithDocElem("UserPreferences", PREFERENCES_NS);
        IXMLStore rootStore = xmlStoreService.getRootStore();
        IXMLStore specifiedStroe = rootStore.specify(selector);
        for (Iterator<IXMLInfo> i = specifiedStroe.getInfoIterator(); i.hasNext();) {
            IXMLInfo info = i.next();
            try {
                read(info.getRootNode());
            } catch (XMLStoreException e) {
                e.printStackTrace();
            }
        }
        
        initAccessKey();
    }
    
    private void initAccessKey() {
        for (int i = 0; i < 10; i++) {
            KC kc = new KC(KC.Type.ACCESSKEY, KeyEvent.VK_0 + i, KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK, null);
            register(kc);
        }
        for (int i = 0; i < 26; i++) {
            KC kc = new KC(KC.Type.ACCESSKEY, KeyEvent.VK_A + i, KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK, null);
            register(kc);
        }
    }

    private void read(Node rootNode) {
        NodeList children = rootNode.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node commands = children.item(i);
            KC.Type type = KC.Type.COMMAND;

            if (commands.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (commands.getLocalName().equals("Commands")) {
                type = KC.Type.COMMAND;
            } else if (commands.getLocalName().equals("Functions")) {
                type = KC.Type.FUNCTION;
            } else {
                continue;
            }

            NodeList commandList = commands.getChildNodes();

            for (int j = 0; j < commandList.getLength(); j++) {
                Node command = commandList.item(j);

                if (command.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                NodeList keyList = command.getChildNodes();

                ArrayList<KC> array = new ArrayList<KC>();
                for (int k = 0; k < keyList.getLength(); k++) {
                    Node key = keyList.item(k);

                    if (key.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    KC kc = createKC(type, command, key);
                    register(kc);

                    if (!"none".equals(((Element) key).getAttribute("display")))
                        array.add(kc);
                }
                addMenu(commands, command, array);
            }
        }
    }

    private KC createKC(Type type, Node command, Node key) {
        String name = command.getLocalName();
        int vkey = 0;
        int mod = 0;
        if ("key".equals(key.getLocalName()) && PREFERENCES_NS.equals(key.getNamespaceURI())) {
            String stroke = key.getTextContent();
            stroke = stroke.trim();
            String[] keys = stroke.split("[ \n\t\r]+");
            for (int j = 0; j < keys.length; j++) {
                try {
                    if ("VK_RETURN".equals(keys[j])) {
                        vkey = VK_RETURN;
                    } else if (keys[j].startsWith("VK_")) {
                        Field f = KeyEvent.class.getField(keys[j]);
                        vkey = f.getInt(KeyEvent.class);
                    } else if (keys[j].endsWith("_MASK")) {
                        Field f = KeyEvent.class.getField(keys[j]);
                        mod |= f.getInt(KeyEvent.class);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return new KC(type, vkey, mod, name);
    }

    private void addMenu(Node commands, Node command, ArrayList<KC> kc) {
        String menuName = ((Element) commands).getAttribute("menu");
        if (menuName.length() == 0)
            return;
        String groupName = ((Element) commands).getAttribute("menuGroup");
        if (groupName.length() == 0)
            return;
        String name = command.getLocalName();

        IMenuManager menu = NavigatorUIUtil.menuManager;
        IMenuManager menu2 = menu.findMenuUsingPath(menuName);

        if ("separator".equals(name)) {
            menu2.appendToGroup(groupName, new Separator());
        } else {
            menu2.appendToGroup(groupName, new KeyAction(name, kc));
        }
    }

    static class KeyEntry {
        int vkey;

        int mod;

        @Override
        public int hashCode() {
            return (vkey | mod << 16);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof KeyEntry))
                return false;
            KeyEntry e = (KeyEntry) o;
            return ((e.vkey == vkey) && (e.mod == mod));
        }

        KeyEntry(int vkey, int mod) {
            this.vkey = vkey;
            this.mod = mod;
        }
    }

    private void register(KC kc) {
        keyConfigMapForTreeNavigation.put(new KeyEntry(kc.vkey, kc.mod), kc);
        keyHook.registerHookedKey(kc.vkey, kc.mod);
    }

    static class KC {
        enum Type {
            COMMAND, FUNCTION, NOOP, ACCESSKEY
        }

        int vkey;

        int mod;

        Type type;

        String name;
        
        enum Target {
            INAVIGATORUI, IBROWSERCONTROL
        }
        
        Target target;

        Method method;

        int command;
        
        KC(Type type, int vkey, int mod, String name) {
            this.type = type;
            this.vkey = vkey;
            this.mod = mod;
            this.name = name;

            if (type == Type.FUNCTION) {
            	try {
            		method = INavigatorUI.class.getMethod(name);
                    target = Target.INAVIGATORUI;
            	} catch (Exception e) {
            		method = null;
            	}
            	
                if (method == null) {
                	try {
                		method = IBrowserControl.class.getMethod(name);
                        target = Target.IBROWSERCONTROL;
                	} catch (Exception e){
                        System.err.println("No such method:" + name);
                	}
                }
            } else if (type == Type.COMMAND) {
                try {
                    Field field = KeyUIImpl.class.getField("CMD_" + name);
                    command = field.getInt(KeyUIImpl.class);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hookedKey(int vkey, int modifier, boolean isUp) {
        if (mode == IManipulator.TREE_NAVIGATION_MODE) {
            KC kc = (KC) keyConfigMapForTreeNavigation.get(new KeyEntry(vkey, modifier));
            if (kc != null) {
                if (kc.type == KC.Type.COMMAND && kc.command == CMD_NOOP)
                    return true;
                if (kc.type != KC.Type.ACCESSKEY && kc.method == null)
                    return false;
                
                /*
                if (vkey == VK_RETURN) {
                    if (!isUp)
                        call(kc);
                } else {
                    if (isUp)
                        call(kc);
                }*/
                if (!isUp)
                    call(kc);

                return true;
            }
        } else if (mode == IManipulator.FORM_INPUT_MODE) {
            switch (vkey) {
            case KeyEvent.VK_TAB:
                break;
            case VK_RETURN:
                // navigatorUI.submitForm();
                sendEvent.postKey(vkey, isUp);
                break;
            case KeyEvent.VK_ESCAPE:
                navigatorUI.exitFormMode();
                break;
            default:
                return false;
            }
            return true;
        } else if (mode == IManipulator.KEYHOOK_DISABLED_MODE) {
            return false;
        }
        return false;
    }

    private void call(KC kc) {
        if (kc.type == KC.Type.COMMAND) {
            switch (kc.command) {
            // for demo
            case CMD_SPEED_UP:
                sendKey(KeyEvent.VK_UP, true);
                break;
            case CMD_SPEED_DOWN:
                sendKey(KeyEvent.VK_DOWN, true);
                break;
            case CMD_PLAY_NEXT:
                sendKey(KeyEvent.VK_RIGHT, false);
                break;
            case CMD_PLAY_PREV:
                sendKey(KeyEvent.VK_LEFT, false);
                break;
            case CMD_START_RECORDING:
                sendKey(KeyEvent.VK_S, true);
                break;
            case CMD_NOOP:
            case CMD_PASSTHROUGH:
                break;
            }
        } else if (kc.type == KC.Type.FUNCTION) {
            try {
                switch (kc.target) {
                case IBROWSERCONTROL:
                    if (browserControl != null)
                        kc.method.invoke(browserControl, new Object[0]);
                    break;
                case INAVIGATORUI:
                    if (navigatorUI != null)
                        kc.method.invoke(navigatorUI, new Object[0]);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (kc.type == KC.Type.ACCESSKEY) {
            navigatorUI.jumpToAccessKey((char) kc.vkey);
        }
    }

    private void sendKey(int key, boolean ctrlFlag) {
        long h = sendEvent.findWindow("SWT_Window0", "Sound Controller");
        if (ctrlFlag) {
            sendEvent.postKeyToWindow(h, KeyEvent.VK_CONTROL, false);
        }
        sendEvent.postKeyToWindow(h, key, false);
        sendEvent.postKeyToWindow(h, key, true);
        if (ctrlFlag) {
            sendEvent.postKeyToWindow(h, KeyEvent.VK_CONTROL, true);
        }
    }

    public void setNavigator(INavigatorUI navigatorUI) {
        this.navigatorUI = navigatorUI;
    }

    public void setBrowserControl(IBrowserControl browserControl) {
        this.browserControl = browserControl;
    }

    public void dispose() {
        keyHook.dispose();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == IManipulator.FORM_INPUT_MODE) {
            keyHook.hookAll(true);
        } else if (mode == IManipulator.KEYHOOK_DISABLED_MODE) {
            keyHook.hookAll(false);
        } else {
            keyHook.hookAll(false);
        }
    }

    class KeyAction extends Action {
        String name;

        ArrayList<KC> kcs;

        public KeyAction(String name, ArrayList<KC> kcs) {
            super();
            this.name = name;
            this.kcs = kcs;

            StringBuffer sb = new StringBuffer();
            sb.append(Messages.getString(name));
            if (kcs.size() > 0)
                sb.append("    (" + getKey(kcs) + ")");

            setText(sb.toString());
        }

        private String getKey(ArrayList<KC> kcs) {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < kcs.size(); i++) {
                if (i != 0)
                    sb.append(" " + Messages.getString("Key.OR") + " ");

                int vkey = kcs.get(i).vkey;
                int mod = kcs.get(i).mod;

                if (is(mod, KeyEvent.CTRL_MASK)) {
                    sb.append("Ctrl+");
                }
                if (is(mod, KeyEvent.ALT_MASK)) {
                    sb.append("Alt+");
                }
                if (is(mod, KeyEvent.SHIFT_MASK)) {
                    sb.append("Shift+");
                }
                String key = "";
                if (('0' <= vkey && vkey <= '9') || ('A' <= vkey && vkey <= 'Z')) {
                    key = "" + (char) vkey;
                } else if (KeyEvent.VK_F1 <= vkey && vkey <= KeyEvent.VK_F24) {
                    key = "F" + (vkey - KeyEvent.VK_F1 + 1);
                } else {
                    switch (vkey) {
                    case VK_RETURN:
                        key = "Enter";
                        break;
                    case KeyEvent.VK_UP:
                        key = "Up";
                        break;
                    case KeyEvent.VK_DOWN:
                        key = "Down";
                        break;

                    case KeyEvent.VK_LEFT:
                        key = "Left";
                        break;
                    case KeyEvent.VK_RIGHT:
                        key = "Right";
                        break;

                    case KeyEvent.VK_HOME:
                        key = "Home";
                        break;
                    case KeyEvent.VK_END:
                        key = "End";
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        key = "PageUp";
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        key = "PageDown";
                        break;

                    case KeyEvent.VK_TAB:
                        key = "Tab";
                        break;
                    case KeyEvent.VK_PAUSE:
                        key = "Pause";
                        break;
                    case KeyEvent.VK_SPACE:
                        key = "Space";
                        break;
                    }
                }
                sb.append(key);
            }

            return sb.toString();
        }

        private boolean is(int a, int b) {
            return (a & b) == b;
        }

        @Override
        public void run() {
            if (kcs.size() > 0)
                call(kcs.get(0));
        }
    }

}
