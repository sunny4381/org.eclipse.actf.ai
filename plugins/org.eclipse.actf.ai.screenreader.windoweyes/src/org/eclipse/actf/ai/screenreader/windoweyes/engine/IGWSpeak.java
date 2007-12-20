/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.screenreader.windoweyes.engine;

import org.eclipse.actf.util.win32.COMUtil;
import org.eclipse.swt.internal.ole.win32.COM;
import org.eclipse.swt.internal.ole.win32.GUID;
import org.eclipse.swt.internal.ole.win32.IDispatch;




public class IGWSpeak extends IDispatch {

    public static final GUID IID = COMUtil.IIDFromString("{A42771AD-03C7-11D3-9F6E-00C095EE683F}"); //$NON-NLS-1$
    
    private int address;

    /**
     * Constructor
     * @param address 
     *      Native address of IDispatch
     */
    public IGWSpeak(int address) {
        super(address);
        this.address = address;
    }
    
    /**
     * Speak a text
     * @param pStringAddress 
     *      Native BSTR address of text
     * @return S_OK if success
     */
    public int SpeakString(int pStringAddress) {
        return COMUtil.VtblCall(7,address,pStringAddress);
    }
    
    /**
     * @return S_OK if success
     */
    public int Silence() {
        return COM.VtblCall(8,address);
    }
    
}
