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

import java.util.ArrayList;
import java.util.Locale;

public interface IMetadataProvider {
    public int getSize();

    public IMetadata getItem(int index);

    public ArrayList<IMetadata> getAllItems();

    public int getIndex(int position);

    public void setLocale(Locale locale);
    
    public int getAlternatives();

    public void setAlternative(int index);

    public void reload();
    
    public boolean hasMetadata();
}
