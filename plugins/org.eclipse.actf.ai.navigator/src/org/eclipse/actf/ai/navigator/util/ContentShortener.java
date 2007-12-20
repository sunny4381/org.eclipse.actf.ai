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

package org.eclipse.actf.ai.navigator.util;

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;



public class ContentShortener {
    private int limit;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int max) {
        this.limit = max;
    }

    private final MessageFormatter messageFormatter;
    
    public ContentShortener(int max,
                            MessageFormatter messageFormatter) {
        this.limit = max;
        this.messageFormatter = messageFormatter;
    }
    
    public String getSummary(ITreeItem item, boolean verbal) {
        String s = item.getUIString();
        if (s.length() <= limit) {
            return s;
        } else {
            s = s.substring(0, limit);
            if (verbal) {
                return messageFormatter.mes("ContentShortener.VerbalForm", s);
            } else {
                return messageFormatter.mes("ContentShortener.NonVerbalForm", s);
            }
        }
    }
}
