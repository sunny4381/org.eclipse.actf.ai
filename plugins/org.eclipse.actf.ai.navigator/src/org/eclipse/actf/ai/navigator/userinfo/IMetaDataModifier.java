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

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.navigator.userinfo.IUserInfoGenerator.Result;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;




public interface IMetaDataModifier {

    String toString(Result result);

    Result commit(boolean save) throws XMLStoreException;

    String getSite();

    void setSite(String targetSite);

    ITreeItem getItem();

    void setItem(ITreeItem item);

    String getText();

    void setText(String text);

    IUserInfoGenerator getGenerator();

    void setGenerator(IUserInfoGenerator generator);

    boolean remove() throws XMLStoreException;

    void setPageTitle(String title);
}
