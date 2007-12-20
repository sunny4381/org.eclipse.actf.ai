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

package org.eclipse.actf.ai.navigator;

import org.eclipse.actf.model.IWebBrowserACTF;



public interface IScreenReaderControl {
    boolean isAvailable();

    void screenReaderOn();
    void screenReaderOff();
    void takeBackControl(IWebBrowserACTF browser);
}
