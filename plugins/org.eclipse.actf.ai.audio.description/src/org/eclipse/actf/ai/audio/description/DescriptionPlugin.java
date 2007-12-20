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
package org.eclipse.actf.ai.audio.description;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.actf.ai.audio.description.impl.MetadataManager;
import org.eclipse.actf.ai.audio.description.impl.MetadataProviderImpl;
import org.eclipse.actf.ai.audio.description.views.DescriptionView;
import org.eclipse.actf.ai.navigator.IMediaControl;
import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.voice.internal.AbstractPreferenceUIPlugin;
import org.eclipse.actf.ai.voice.internal.TTSRegistry;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.IXMLStoreService;
import org.eclipse.actf.ai.xmlstore.XMLStorePlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class DescriptionPlugin extends AbstractPreferenceUIPlugin implements IPropertyChangeListener {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.actf.ai.audio.description";

    public static final String PREF_ENGINE = "AudioDescriptionTTSEngine";

    // The shared instance
    private static DescriptionPlugin plugin;

    private boolean enable = false;

    private MetadataManager activeManager;

    ITTSEngine engine;

    /**
     * The constructor
     */
    public DescriptionPlugin() {
        plugin = this;
    }

    public IMetadataProvider getMetadata(String url) {
        IXMLStoreService service = XMLStorePlugin.getDefault().getXMLStoreService();
        IXMLStore store = service.getRootStore();
        IXMLSelector selector1 = service.getSelectorWithDocElem("puits", "urn:puits");
        IXMLSelector selector2 = service.getSelectorWithIRI(url);
        store = store.specify(selector1);
        store = store.specify(selector2);
        
        ArrayList<IXMLInfo> list = new ArrayList<IXMLInfo>();
        for(Iterator<IXMLInfo> i = store.getInfoIterator(); i.hasNext();){
            list.add(i.next());
        }
        if(list.size() == 0)
            return null;
        MetadataProviderImpl provider = new MetadataProviderImpl(list);
        if (provider != null){
            provider.reload();
            provider.setLocale(Locale.getDefault());
            provider.getAlternatives();
        }
        return provider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        initialize();
    }

    private void initialize() {
        engine = newTTSEngine();
        DescriptionPlugin.getDefault().addPropertyChangeListener(this);
    }

    private ITTSEngine newTTSEngine() {
        ITTSEngine engine;
        String e = DescriptionPlugin.getDefault().getPreferenceStore().getString(DescriptionPlugin.PREF_ENGINE);
        
        // TODO  
        if (e.equals("org.eclipse.actf.ai.screenreader.jaws"))
            return null;
        engine = TTSRegistry.createTTSEngine(e);
        if(engine != null)
            engine.setSpeed(50);
        return engine;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (DescriptionPlugin.PREF_ENGINE.equals(event.getProperty())) {
            if (null != engine) {
                engine.stop();
                engine.dispose();
                engine = newTTSEngine();
            }
        }
    }

    public ITTSEngine getTTSEngine() {
        return engine;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static DescriptionPlugin getDefault() {
        return plugin;
    }

    DescriptionView view;

    public void setDescriptionView(DescriptionView view) {
        this.view = view;
    }

    public DescriptionView getDescriptionView() {
        return view;
    }

    public int setEnable(boolean flag) {
        if(!isAvailable()){
            enable = false;
            return IMediaControl.STATUS_NOT_AVAILABLE;
        }

        enable = flag;
        getDescriptionView().setEnable(enable);
        if (enable) {
            return IMediaControl.STATUS_ON;
        } else {
            return IMediaControl.STATUS_OFF;
        }
    }

    public boolean isAvailable() {
        if (activeManager == null)
            return false;
        return activeManager.hasMetadata();
    }
    
    public boolean canSpeak() {
        return engine != null;
    }

    public void speak(String str) {
        if(engine != null)
            engine.speak(str, ITTSEngine.TTSFLAG_FLUSH, -1);
    }

    public int toggleEnable() {
        return setEnable(!getEnable());
    }

    public boolean getEnable() {
        return enable;
    }

    public void setActiveMetadata(MetadataManager manager) {
        this.activeManager = manager;
    }
    
    public static String getString(String key) {
        return Platform.getResourceString(getDefault().getBundle(), key);
    }
}
