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


/**
 * IMetadataCacheCleaner interface defines a method to clear the memory cache of the metadata.
 * The instances are managed by extension manager. 
 */
public interface IMetadataCacheCleaner {
    /**
     * Clear the memory cache of the metadata.
     * The application will read metadata again from the disk.
     */
    void clearCache();
}
