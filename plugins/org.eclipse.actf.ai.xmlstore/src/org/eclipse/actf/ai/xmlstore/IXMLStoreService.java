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

package org.eclipse.actf.ai.xmlstore;

import java.io.File;

/**
 * IXMLStoreService has factory methods and utility methods related to XML
 * store. The following is a sample usage. The root {@link IXMLStore} can be
 * obtained from the {@link IXMLStoreService}. {@link IXMLStore} has some XML
 * files and the files can be selected by an {@link IXMLSelector} and also the
 * files can be iterated by {@link IXMLStore#getInfoIterator()}.
 * 
 * 
 * <pre>
 * IXMLStoreService ss = XMLStorePlugin.getDefault().getXMLStoreService();
 * IXMLSelector selector = ss.getSelectorWithDocElem(&quot;fennec&quot;,
 * 		&quot;http://www.ibm.com/xmlns/prod/aiBrowser/fennec&quot;);
 * IXMLStore store = ss.getRootStore();
 * store = store.specify(selector);
 * </pre>
 */
public interface IXMLStoreService {
	/**
	 * It creates an instance of IXMLSelector which selects the elements matched
	 * with the <i>name</i> and the <i>iri</i>.
	 * 
	 * @param name
	 *            the name of the element for selecting.
	 * @param iri
	 *            the namespace IRI of the element for selecting.
	 * @return
	 */
	IXMLSelector getSelectorWithDocElem(String name, String iri);

	/**
	 * It creates an instance of IXMLSelector which selects the elements matched
	 * with the <i>uri</i>.
	 * 
	 * @param uri
	 *            the URI for selecting.
	 * @return
	 */
	IXMLSelector getSelectorWithURI(String uri);

	// IXMLSelector getSelectorWithXPath(String xpath);
	/**
	 * @return the root XML store instance.
	 */
	IXMLStore getRootStore();

	/**
	 * It creates an instance of {@link IXMLEditableInfo} for user annotation.
	 * 
	 * @param namespaceURI
	 *            the default namespace of the document.
	 * @param qualifiedName
	 *            the name of the document element.
	 * @param targetUriPattern
	 *            the URI pattern to be used for the target site declaration.
	 * @return new instance of {@link IXMLEditableInfo}
	 * @throws XMLStoreException
	 */
	IXMLEditableInfo newUserXML(String namespaceURI, String qualifiedName,
			String targetUriPattern) throws XMLStoreException;

	/**
	 * @param info
	 *            the metadata to be exported.
	 * @param file
	 *            the destination of the export.
	 * @return whether the export is succeeded or not.
	 */
	boolean exportMetadata(IXMLInfo info, File file);

	/**
	 * @param file
	 *            the metadata file to be imported.
	 * @return whether the import is succeeded or not.
	 */
	boolean importMetadata(File file);

	/**
	 * It exports all user annotations to the destination file <i>dest</i> in
	 * format of zip file.
	 * 
	 * @param dest
	 *            the destination of the export.
	 * @return whether the export is succeeded or not.
	 */
	boolean exportAllAnnotations(File dest);
}
