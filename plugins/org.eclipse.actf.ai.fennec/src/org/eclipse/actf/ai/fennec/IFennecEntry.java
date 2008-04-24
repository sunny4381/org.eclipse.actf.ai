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

package org.eclipse.actf.ai.fennec;

import java.io.File;

/**
 * IFennecEntry represents an entry of Fennec metadata. An entry is
 * corresponding to an Fennec file.
 */
public interface IFennecEntry {
	/**
	 * @return the title of the entry specified in the documentation element.
	 */
	String getDocumentation();

	/**
	 * @return whether the entry is for a metadata of user annotations or not.
	 */
	boolean isUserEntry();

	/**
	 * Export the entry to the specified destination file.
	 * 
	 * @param dest
	 *            the destination of the export.
	 * @return if the export is succeeded then it returns true and failed then
	 *         false.
	 */
	boolean export(File dest);
}
