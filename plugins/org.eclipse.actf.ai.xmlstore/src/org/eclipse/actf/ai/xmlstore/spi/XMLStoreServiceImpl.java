/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.xmlstore.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.actf.ai.xmlstore.IXMLEditableInfo;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.IXMLStoreService;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.eclipse.actf.ai.xmlstore.XMLStorePlugin;
import org.eclipse.actf.ai.xmlstore.local.UserXMLStore;
import org.eclipse.actf.ai.xmlstore.local.XMLStoreLocal;

public class XMLStoreServiceImpl implements IXMLStoreService {
    public IXMLStore getRootStore() {
        return rootStore;
    }

    private static class XMLSelectorWithDocElem extends XMLSelectorDefault {
        private String name;

        private String iri;

        @Override
        public String getDocumentElementName() {
            return name;
        }

        @Override
        public String getDocumentElementNS() {
            return iri;
        }

        XMLSelectorWithDocElem(String name, String iri) {
            this.name = name;
            this.iri = iri;
        }
    }

    public IXMLSelector getSelectorWithDocElem(String name, String iri) {
        return new XMLSelectorWithDocElem(name, iri);
    }

    private static class XMLSelectorWithIRI extends XMLSelectorDefault {
        private String iri;

        @Override
        public String getURI() {
            return iri;
        }

        XMLSelectorWithIRI(String iri) {
            this.iri = iri;
        }
    }

    public IXMLSelector getSelectorWithURI(String iri) {
        return new XMLSelectorWithIRI(iri);
    }

    // -----------------------------------------------------------

    private XMLStoreAggregator rootStore;

    private XMLStoreServiceImpl() {
        XMLStoreAggregator store = new XMLStoreAggregator();
        rootStore = store;
    }
    
    public void addStore(IXMLStore store) {
        rootStore.addStore(store);
    }
    
    public void setUserStore(UserXMLStore store) {
        this.userStore = store;
        rootStore.addStore(store);
    }

    private static XMLStoreServiceImpl instance = new XMLStoreServiceImpl();

    public static XMLStoreServiceImpl getInstance() {
        return instance;
    }

    private UserXMLStore userStore;

    public IXMLEditableInfo newUserXML(String namespaceURI, String qualifiedName, String targetUriPattern) throws XMLStoreException {
        return userStore.newXML(namespaceURI, qualifiedName, targetUriPattern);
    }
    
    /*********************************
     * Import and Export
     *********************************/

    public boolean exportMetadata(IXMLInfo info, File dest) {
        try {
            if (info instanceof IXMLEditableInfo) {
                ((IXMLEditableInfo) info).save(dest);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean importMetadata(File src) {
        try {
            if (importZippedMetadata(src))
                return true;
            return importFile(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean importFile(File src) {
        XMLSelectorInfo info;
        try {
            info = XMLSelectorInfo.parse(XMLStoreLocal.newXMLFile(src));
        } catch (XMLStoreException e1) {
            return false;
        }
        
        if (info.getDocumentElementName() == null)
            return false;

        String docElementName = info.getDocumentElementName();
        String childDirName;
        if (info.isUserEntry()) {
            childDirName = UserXMLStore.TEMP_DIR_NAME;
        } else {
            childDirName = XMLStoreLocal.SYSTEM_DIR_NAME;
        }
        File destBaseDir = XMLStorePlugin.getDefault().getLocalDir(childDirName);
        destBaseDir = new File(destBaseDir, docElementName);
        File dest = new File(destBaseDir, src.getName());
        
        if (src.equals(dest)) return true;
        
        destBaseDir.mkdirs();
        
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            byte[] buff = new byte[4096];
            while (true) {
                int read = fis.read(buff);
                if (read >= 0) {
                    fos.write(buff, 0, read);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private boolean importZippedMetadata(File src) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(src));
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            String destDir = XMLStorePlugin.getDefault().getStateLocation().toOSString();
            boolean flag = true;
            while ((entry = zis.getNextEntry()) != null) {
                File f = new File(destDir + "\\" + entry.getName());
                FileOutputStream fos = new FileOutputStream(f);
                int len = 0;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                zis.closeEntry();
                fos.close();
                importFile(f);
                f.delete();
                flag = false;
            }
            zis.close();
            if (flag)
                return false;
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static final String Fennec_NAMESPACE_URI = "http://www.ibm.com/xmlns/prod/aiBrowser/fennec";

    public static final String Fennec_DOCUMENT_ELEMENT_NAME = "fennec";

    public boolean exportAllAnnotations(File dest) {
        try {
            IXMLSelector selector = getSelectorWithDocElem(Fennec_DOCUMENT_ELEMENT_NAME, Fennec_NAMESPACE_URI);
            IXMLStore store = getRootStore().specify(selector);

            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest));
            for (Iterator<IXMLInfo> i = store.getInfoIterator(); i.hasNext();) {
                IXMLInfo info = i.next();
                if (info instanceof IXMLEditableInfo) {
                    IXMLEditableInfo eInfo = (IXMLEditableInfo) info;
                    eInfo.save(zos);
                }
            }
            zos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
