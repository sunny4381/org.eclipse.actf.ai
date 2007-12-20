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

package org.eclipse.actf.ai.audio.description.views;

import org.eclipse.actf.ai.audio.description.DescriptionPlugin;
import org.eclipse.actf.ai.audio.description.Messages;
import org.eclipse.actf.ai.audio.description.impl.MetadataContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;




public class DescriptionView extends ViewPart {
    public static final String ID = "org.eclipse.actf.ai.audio.description.views.DescriptionView";

    private VideoStatusViewer status;

    private TableViewer tableViewer;
    
    @Override
    public void createPartControl(Composite parent) {
        initView(parent);
        DescriptionPlugin.getDefault().setDescriptionView(this);
    }

    private void initView(Composite parent) {
        GridLayout gLayout = new GridLayout(1, true);
        gLayout.marginWidth = 0;
        gLayout.marginHeight = 0;
        parent.setLayout(gLayout);

        status = new VideoStatusViewer(parent);

        tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        MetadataContentProvider provider = new MetadataContentProvider();
        tableViewer.setContentProvider(provider);
        tableViewer.setLabelProvider(provider);

        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn tableColumnProperty = new TableColumn(table, SWT.LEFT);
        tableColumnProperty.setText(Messages.getString("AudioDescription.view.time"));
        tableColumnProperty.setWidth(70);
        TableColumn tableColumnValue = new TableColumn(table, SWT.LEFT);
        tableColumnValue.setText(Messages.getString("AudioDescription.view.desc"));
        tableColumnValue.setWidth(130);
        
        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);
    }

    @Override
    public void setFocus() {

    }

    public void setTime(double time) {
        status.setTime(time);
    }
    public void setInput(Object input){
        if(tableViewer.getTable().isDisposed())
            return;
        tableViewer.setInput(input);
    }
    public void setIndex(int index){
        if(tableViewer.getTable().isDisposed())
            return;
        tableViewer.getTable().select(index);
    }

    public void setEnable(boolean b) {
        status.setEnable(b);
    }
    
    public boolean toggleViewShowing() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart part = page.findView(ID);

        if (part != null) {
            page.hideView(part);
            return false;
        } else {
            try {
                page.showView(ID);
                return true;
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
