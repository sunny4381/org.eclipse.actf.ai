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

package org.eclipse.actf.ai.fennec.treemanager;

/**
 * ITableCell interface defines the methods to be implemented by the table cell
 * representation on the Fennec tree.
 */
public interface ITableCell {
	/**
	 * @return the column position in the table. 
	 */
	int getColumn();

	/**
	 * @return the row position in the table.
	 */
	int getRow();

	/**
	 * @return the ITreeItem object corresponding to the cell.
	 */
	ITreeItem getItem();

	/**
	 * @return whether the cell is connected with the upper cell or not.
	 */
	boolean isConnectedWithUpCell();

	/**
	 * @return whether the cell is connected with the left next cell or not.
	 */
	boolean isConnectedWithLeftCell();

	/**
	 * @return the ITreeItem object for the row header of the cell.
	 */
	ITreeItem getRowHeader();

	/**
	 * @return the ITreeItem object for the column header of the cell.
	 */
	ITreeItem getColumnHeader();

	/**
	 * @return whether the cell is a cell in the table header.
	 */
	boolean isHeader();
}
