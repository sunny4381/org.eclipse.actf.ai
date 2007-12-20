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

package org.eclipse.actf.ai.xmlstore;

import java.io.File;
import java.util.zip.ZipOutputStream;

public interface IXMLEditableInfo extends IXMLInfo {
    void save();
    void save(File file);
    void setPriority(int priority);
    void setDocumentation(String documentation);
    void setPageTitle(String pageTitle);
    void setAuthorName(String authorName);
    void remove();
    void save(ZipOutputStream zos);
}
