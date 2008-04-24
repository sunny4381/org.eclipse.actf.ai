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

/**
 * IXMLEditableInfo interface defines the method to be implemented by the
 * editable XML file implementation, which is used for user annotation.
 */
public interface IXMLEditableInfo extends IXMLInfo {
	/**
	 * The XML file will be saved as the default name. The default name is
	 * determined from the target URI pattern.
	 */
	void save();

	/**
	 * The XML file will be saved as the specified file.
	 * 
	 * @param file
	 *            the destination.
	 */
	void save(File file);

	/**
	 * @param priority
	 *            the priority value to be set in the meta information.
	 */
	void setPriority(int priority);

	/**
	 * @param documentation
	 *            the documentation text to be set in the meta information.
	 */
	void setDocumentation(String documentation);

	/**
	 * @param pageTitle
	 *            the page title to be set in the meta information.
	 */
	void setPageTitle(String pageTitle);

	/**
	 * @param authorName
	 *            the author name to be set in the meta information.
	 */
	void setAuthorName(String authorName);

	/**
	 * The XML file will be deleted.
	 */
	void remove();

	/**
	 * The XML file will be saved in the zip output stream.
	 * @param zos the destination to be used for save.
	 */
	void save(ZipOutputStream zos);
}
