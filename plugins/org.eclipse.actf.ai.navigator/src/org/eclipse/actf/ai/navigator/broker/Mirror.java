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

package org.eclipse.actf.ai.navigator.broker;



import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Mirror {
    final private Class class1;
    final private Object object;

    public Object getObject() {
        return object;
    }

    private Class[] parseMethodSignature(String params) {
        String[] paramArray;

        paramArray = params.split(", *");
        Class[] cs = new Class[paramArray.length];
        try {
            for (int i = 0; i < paramArray.length; i++) {
                if ("int".equals(paramArray[i])) {
                    cs[i] = java.lang.Integer.TYPE;
                } else if ("short".equals(paramArray[i])) {
                    cs[i] = java.lang.Short.TYPE;
                } else if ("long".equals(paramArray[i])) {
                    cs[i] = java.lang.Long.TYPE;
                } else if ("char".equals(paramArray[i])) {
                    cs[i] = java.lang.Character.TYPE;
                } else if ("boolean".equals(paramArray[i])) {
                    cs[i] = java.lang.Boolean.TYPE;
                } else if ("byte".equals(paramArray[i])) {
                    cs[i] = java.lang.Byte.TYPE;
                } else if ("float".equals(paramArray[i])) {
                    cs[i] = java.lang.Float.TYPE;
                } else if ("double".equals(paramArray[i])) {
                    cs[i] = java.lang.Double.TYPE;
                } else {
                    cs[i] = Class.forName(paramArray[i], true, class1.getClassLoader());
                }
            }
        } catch (ClassNotFoundException e) {
            return null;
        }

        return cs;
    }

    public Method getMethod(String signature) {
        String name;
        Class[] params = null;
        int posParam = signature.indexOf('(');
        if (posParam == -1) {
            name = signature;
        } else {
            name = signature.substring(0, posParam);
            int posParamEnd = signature.lastIndexOf(')');
            if (posParamEnd == -1) return null;
            params = parseMethodSignature(signature.substring(posParam + 1, posParamEnd));
            if (params == null) return null;
        }

        Method m = null;
        for (Class c = class1; c != null; c = c.getSuperclass()) {
            try {
                if (params == null) {
                    Method[] ms = c.getDeclaredMethods();
                    for (int i = 0; i < ms.length; i++) {
                        if (name.equals(ms[i].getName())) {
                            m = ms[i];
                        }
                    }
                } else {
                    m = c.getDeclaredMethod(name, params);
                }
            } catch (SecurityException e) {
                continue;
            } catch (NoSuchMethodException e) {
                continue;
            }
            if (m != null) {
                m.setAccessible(true);
                return m;
            }
        }

        return null;
    }

    public Object invoke(String signature, Object[] params) throws Exception {
        Method m = getMethod(signature);
        return m.invoke(object, params);
    }

    public Field getField(String name) {
        Field f;

        for (Class c = class1; c != null; c = c.getSuperclass()) {
            try {
                f = c.getDeclaredField(name);
            } catch (SecurityException e) {
                continue;
            } catch (NoSuchFieldException e) {
                continue;
            }
            if (f != null) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    public Object getFieldObject(String name) throws IllegalAccessException {
        return getField(name).get(object);
    }

    static private boolean isParamAccept(Class cp, Class c) {
        if (cp.isAssignableFrom(c)) return true;

        if ((java.lang.Integer.TYPE == cp)
            && java.lang.Integer.class.equals(c))
            return true;
        if ((java.lang.Short.TYPE == cp)
            && java.lang.Short.class.equals(c))
            return true;
        if ((java.lang.Long.TYPE == cp)
            && java.lang.Long.class.equals(c))
            return true;
        if ((java.lang.Character.TYPE == cp)
            && java.lang.Character.class.equals(c))
            return true;
        if ((java.lang.Boolean.TYPE == cp)
            && java.lang.Boolean.class.equals(c))
            return true;
        if ((java.lang.Byte.TYPE == cp)
            && java.lang.Byte.class.equals(c))
            return true;
        if ((java.lang.Float.TYPE == cp)
            && java.lang.Float.class.equals(c))
        if ((java.lang.Double.TYPE == cp)
            && java.lang.Double.class.equals(c))
            return true;

        return false;
    }

    public Constructor findConstructor(Object[] params) {
        Constructor[] cs = class1.getDeclaredConstructors();

        nextConstructor:
        for (int i = 0; i < cs.length; i++) {
            Class[] cParams = cs[i].getParameterTypes();
            if (params == null) {
                if (cParams.length == 0) return cs[i];
                continue nextConstructor;
            }
            if (cParams.length != params.length) continue nextConstructor;;
            for (int j = 0; j < params.length; j++) {
                if (!isParamAccept(cParams[j], params[j].getClass())) continue nextConstructor;
            }
            return cs[i];
        }

        return null;
    }

    public Object newInstance(Object[] params)
    	throws InvocationTargetException, IllegalAccessException, InstantiationException {
        return findConstructor(params).newInstance(params);
    }

    public Mirror(String className) throws ClassNotFoundException {
        this.class1 = Class.forName(className);
        this.object = null;
    }

    public Mirror(Class cls) {
        this.class1 = cls;
        this.object = null;
    }

    public Mirror(Object object) {
        this.class1 = object.getClass();
        this.object = object;
    }

    public Mirror(String className, Object[] params)
    	throws ClassNotFoundException,
               InvocationTargetException, IllegalAccessException, InstantiationException {
        this.class1 = Class.forName(className);
        this.object = newInstance(params);
    }

    public static void main(String[] args) throws Exception {
        Mirror miS = new Mirror("java.lang.String");

        System.out.println("java.lang.String.charAt:"
                           + miS.getMethod("charAt"));
        System.out.println("java.lang.String.indexOf(int,int):"
                           + miS.getMethod("indexOf(int, int)"));

        Mirror miS2 = new Mirror("java.lang.String", new Object[]{"test string"});
        System.out.println("String instanciation test:" + miS2.getObject());

        Mirror miI = new Mirror("java.lang.Integer", new Object[]{new Integer(100)});
        System.out.println("Integer instanciation test:" + miI.getObject());
    }
}
