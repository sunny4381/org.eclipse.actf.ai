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

package org.eclipse.actf.ai.navigator.userinfo.impl;

import java.util.Iterator;

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier;
import org.eclipse.actf.ai.navigator.userinfo.IUserInfoConstants;
import org.eclipse.actf.ai.navigator.userinfo.IUserInfoGenerator;
import org.eclipse.actf.ai.navigator.userinfo.IUserInfoGenerator.Result;
import org.eclipse.actf.ai.xmlstore.IXMLEditableInfo;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.IXMLStoreService;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.eclipse.actf.ai.xmlstore.XMLStorePlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;




public class MetaDataModifier implements IUserInfoConstants, IMetaDataModifier {
    private IUserInfoGenerator generator;
    private ITreeItem item;
    private String text;
    private String site;
    private String pageTitle;
    
//    @Override
//    public String toString() {
//        StringBuffer ret = new StringBuffer();
//        String s = item.getUIString();
//        ret.append(generator);
//        ret.append(" : ");
//        ret.append(s != null && s.length() > 0 ? s : "X");
//        ret.append(" is ");
//        ret.append(text);
//        return ret.toString();
//    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#toString(org.eclipse.actf.ai.navigator.userinfo.IUserInfoConstants.Result)
     */
    public String toString(Result result) {
        return generator.toString(result);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#commit(boolean)
     */
    public Result commit(boolean save) throws XMLStoreException {
        IXMLEditableInfo info = getUserInfo();
        info.setPageTitle(pageTitle);
        Result result = Result.NOTHING;
        if (generator != null) {
            if (item == null) return Result.ERROR;
            Document infoDocument = info.getRootNode().getOwnerDocument();
            Node parent = infoDocument.getElementsByTagNameNS(DEFAULT_NAMESPACE, "attach").item(0);
//            TargetNodeQuery paths = getQueryPaths(parent.getOwnerDocument());
//            result = generator.addUserInfo(parent, paths, text);
//            Node node = getInfoNode(parent);
            Node node = item.serializeQuery(parent);
            if (node == null) return Result.ERROR;
            result = generator.addUserInfo(node, text);
            if (!node.hasChildNodes())
                parent.removeChild(node);
        }
        if (save)
            info.save();
        return result;
    }
    
    private IXMLEditableInfo getUserInfo() throws XMLStoreException {
        IXMLStoreService service = XMLStorePlugin.getDefault().getXMLStoreService();
        IXMLEditableInfo info = getStoredUserInfo(service);
        if (info != null) {
            return info;
        } else {
            return createNewUserInfo(service);
        }
    }
    
    private IXMLEditableInfo getStoredUserInfo(IXMLStoreService service) {
        IXMLStore store0 = service.getRootStore();
        IXMLSelector sel1 = service.getSelectorWithDocElem("fennec", DEFAULT_NAMESPACE);
        IXMLStore store1 = store0.specify(sel1);
        if (store1 == null)
            return null;
        IXMLSelector sel2 = service.getSelectorWithIRI(site);
        IXMLStore store2 = store1.specify(sel2);
        if (store2 == null)
            return null;
        for (Iterator<IXMLInfo> i = store2.getInfoIterator(); i.hasNext(); ) {
            IXMLInfo info = i.next();
            if (info.isUserEntry() && (info instanceof IXMLEditableInfo)) {
                //  System.out.println("open stored user info.");
                return (IXMLEditableInfo) info;
            }
        }
        return null;
    }
    
    private IXMLEditableInfo createNewUserInfo(IXMLStoreService service) throws XMLStoreException {
        IXMLEditableInfo info = service.newUserXML(DEFAULT_NAMESPACE, "fennec", site);
        info.setPageTitle(pageTitle);
        Node root = info.getRootNode();
        Document doc = root.getOwnerDocument();
        Element node = doc.createElementNS(DEFAULT_NAMESPACE, "attach");
        node.setAttributeNS(LOC_NAMESPACE, "loc:path", ".");
        root.appendChild(node);
        //System.out.println("create new user info.");
        return info;
    }
    
    public boolean remove() throws XMLStoreException {
        IXMLStoreService service = XMLStorePlugin.getDefault().getXMLStoreService();
        IXMLEditableInfo info = getStoredUserInfo(service);

        if (info == null) {
            return false;
        }
        info.remove();
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#getSite()
     */
    public String getSite() {
        return site;
    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#setSite(java.lang.String)
     */
    public void setSite(String targetSite) {
        this.site = targetSite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#getItem()
     */
    public ITreeItem getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#setItem(org.eclipse.actf.ai.fennec.treemanager.ITreeItem)
     */
    public void setItem(ITreeItem item) {
        this.item = item;
    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#getText()
     */
    public String getText() {
        return text;
    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#setText(java.lang.String)
     */
    public void setText(String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#getGenerator()
     */
    public IUserInfoGenerator getGenerator() {
        return generator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier#setGenerator(org.eclipse.actf.ai.navigator.userinfo.IUserInfoGenerator)
     */
    public void setGenerator(IUserInfoGenerator generator) {
        this.generator = generator;
    }
    
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }
}

