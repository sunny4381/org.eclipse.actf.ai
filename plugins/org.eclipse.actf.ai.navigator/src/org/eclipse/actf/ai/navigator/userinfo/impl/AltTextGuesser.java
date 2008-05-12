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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.model.ui.editor.browser.IWebBrowserACTF;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;




public class AltTextGuesser {
    private final IWebBrowserACTF browser;
    private final ITreeItem item;
    
    public AltTextGuesser(IWebBrowserACTF browser, ITreeItem item) {
        this.browser = browser;
        this.item = item;
    }
    
    public String getDefaultText() {
        return item.getUIString();
    }
    
    public String guessByOCR() {
        throw new RuntimeException("Not implemented.");
    }
    
    public String guessByContext() {
        return getContext(browser, getContext(item));
    }
    
    private String getContext(ITreeItem item) {
        Object o = item.getBaseNode();
        if (!(o instanceof Node))
            return null;
        for (Node n = (Node) o; n != null; n = n.getParentNode()) {
//            System.err.println(n.getClass());
            if (n instanceof Document)
                break;
            String name = n.getLocalName();
            // I'd like to use getLinkURI!!
            if ("A".equals(name)) {
//                System.out.println(n.getClass());
                NamedNodeMap v = n.getAttributes();
                if (v == null)
                    continue;
                
                Node a = v.getNamedItem("href");
                if (a == null)
                    continue;
                return a.getNodeValue();
            }
        }
        return null;
    }
    
    private String getContext(IWebBrowserACTF browser, String path) {
        try {
            URL url =  new URL(new URL(browser.getURL()), path);
            String s = null;
            String title = "", h1 = "";
            Pattern pattern = Pattern.compile("<(?:(title)|(h1))[^>]*>([^<]*)</(?:title|h1)>" , Pattern.CASE_INSENSITIVE);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((s = br.readLine()) != null) { // TODO ad-hoc
                Matcher matcher = pattern.matcher(s);
                if (!matcher.matches())
                    continue;
                if (matcher.group(1) != null)
                    title = matcher.group(3);
                if (matcher.group(2) != null)
                    h1 = matcher.group(3);
                if (title.length() > 0 && h1.length() > 0)
                    break;
            }
            br.close();
            System.out.println("URL\t" + url);
            System.out.println("title\t" + title);
            System.out.println("h1\t" + h1);
            return title.length() > h1.length() ? title : h1;
        } catch (MalformedURLException e) {
//          e.printStackTrace();
            return null;
        } catch (IOException e) {
//          e.printStackTrace();
            return null;
        }
    }
    
}
