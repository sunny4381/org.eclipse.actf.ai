/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Masatomo KOBAYASHI - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.navigator.userinfo;

import org.w3c.dom.Node;


public interface IUserInfoGenerator {
    public static enum Result {
        NOTHING,
        CREATED,
        REMOVED,
        CHANGED,
        ERROR,
    }

    String toString(Result result);
//    Status addUserInfo(Node container, TargetNodeQuery paths, String altText);
    Result addUserInfo(Node node, String altText);
}

