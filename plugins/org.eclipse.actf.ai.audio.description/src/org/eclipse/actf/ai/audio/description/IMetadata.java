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

package org.eclipse.actf.ai.audio.description;




public interface IMetadata {
    int IMPORTANCE_LOW = 0;
    int IMPORTANCE_MIDDLE = 1;
    int IMPORTANCE_HIGH = 2;
    
    int MASK_NONE = 0;
    int MASK_PAUSE_BEFORE = 1;
    int MASK_SPEAK = 2;
    int MASK_PAUSE_AFTER = 4;
    
    public String getDescription();
    public int getStartTime();
    public int getDuration();
    public String getLang();
    public int getType();
}
