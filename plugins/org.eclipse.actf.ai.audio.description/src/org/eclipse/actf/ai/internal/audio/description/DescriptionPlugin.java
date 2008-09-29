/*******************************************************************************
 * Copyright (c) 2007 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.internal.audio.description;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.actf.ai.audio.description.IMetadataProvider;
import org.eclipse.actf.ai.audio.description.impl.MetadataManager;
import org.eclipse.actf.ai.audio.description.impl.MetadataProviderImpl;
import org.eclipse.actf.ai.audio.description.views.DescriptionView;
import org.eclipse.actf.ai.navigator.IMediaControl;
import org.eclipse.actf.ai.tts.AbstractUIPluginForTTS;
import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.tts.TTSRegistry;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.IXMLStoreService;
import org.eclipse.actf.ai.xmlstore.XMLStoreServiceUtil;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle. And this provides
 * information of audio description and TTS function to speak the description.
 */
public class DescriptionPlugin extends AbstractUIPluginForTTS implements
		IPropertyChangeListener {

	/**
	 * The plug-in ID.
	 */
	public static final String PLUGIN_ID = "org.eclipse.actf.ai.audio.description";

	/**
	 * The preferences ID of TTS engine for audio description.
	 */
	public static final String PREF_ENGINE = "AudioDescriptionTTSEngine";

	/*
	 * The shared instance.
	 */
	private static DescriptionPlugin plugin;

	/*
	 * The switch weather enable or disable audio description speaking.
	 */
	private boolean enable = false;

	/*
	 * The current active audio description manager. If the target page has no
	 * audio description then this should be null.
	 */
	private IMetadataProvider activeProvider;

	/*
	 * The instance of TTS engine for audio description.
	 */
	private ITTSEngine engine;

	/**
	 * The constructor
	 */
	public DescriptionPlugin() {
		plugin = this;
	}

	/**
	 * @param url
	 *            The URL string which determines audio description files in the
	 *            XMLStore
	 * @see org.eclipse.actf.ai.xmlstore.XMLStoreServiceUtil#getXMLStoreService
	 * @return metadata information
	 */
	public IMetadataProvider getMetadata(String url) {
		IXMLStoreService service = XMLStoreServiceUtil
				.getXMLStoreService();
		IXMLStore store = service.getRootStore();
		IXMLSelector selector1 = service.getSelectorWithDocElem("puits",
				"urn:puits");
		IXMLSelector selector2 = service.getSelectorWithURI(url);
		store = store.specify(selector1);
		store = store.specify(selector2);

		ArrayList<IXMLInfo> list = new ArrayList<IXMLInfo>();
		for (Iterator<IXMLInfo> i = store.getInfoIterator(); i.hasNext();) {
			list.add(i.next());
		}
		if (list.size() == 0)
			return null;
		MetadataProviderImpl provider = new MetadataProviderImpl(list);
		if (provider != null) {
			provider.reload();
			provider.setLocale(Locale.getDefault());
			provider.prepareMetadata();
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

	/**
	 * The initializer of the plug-in.
	 */
	private void initialize() {
		engine = newTTSEngine();
		DescriptionPlugin.getDefault().addPropertyChangeListener(this);
	}

	/**
	 * @return TTS engine using the preference value
	 * @see #engine
	 */
	private ITTSEngine newTTSEngine() {
		ITTSEngine engine;
		String e = DescriptionPlugin.getDefault().getPreferenceStore()
				.getString(DescriptionPlugin.PREF_ENGINE);

		// TODO
		if (e.equals("org.eclipse.actf.ai.screenreader.jaws"))
			return null;
		engine = TTSRegistry.createTTSEngine(e);
		if (engine != null)
			engine.setSpeed(50);
		return engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.voice.internal.AbstractPreferenceUIPlugin#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
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

	/**
	 * @return instance of TTS engine.
	 */
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

	/**
	 * The instance of the view.
	 */
	private DescriptionView view;

	/**
	 * @param view
	 *            Keep the instance of the view to share the instance.
	 */
	public void setDescriptionView(DescriptionView view) {
		this.view = view;
	}

	/**
	 * @return shared instance of the view.
	 */
	public DescriptionView getDescriptionView() {
		return view;
	}

	/**
	 * @param flag
	 *            The switch to enable/disable
	 * @return The status of the audio description plug-in
	 * @see IMediaControl.STATUS_NOT_AVAILABLE
	 * @see IMediaControl.STATUS_ON
	 * @see IMediaControl.STATUS_OFF
	 */
	public int setEnable(boolean flag) {
		if (!isAvailable()) {
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

	/**
	 * @return The availability of the plug-in. If there is no active metadata
	 *         manager then this returns false.
	 * @see #setActiveMetadataProvider(MetadataManager)
	 */
	public boolean isAvailable() {
		if (activeProvider == null)
			return false;
		return activeProvider.hasMetadata();
	}

	/**
	 * @return The availability of TTS engine.
	 */
	public boolean canSpeak() {
		return engine != null;
	}

	/**
	 * @param str
	 *            The string to be spoken.
	 */
	public void speak(String str) {
		if (engine != null)
			engine.speak(str, ITTSEngine.TTSFLAG_FLUSH, -1);
	}

	/**
	 * Toggle the enable and disable of the plug-in.
	 * 
	 * @return The status of the plug-in.
	 * @see #setEnable(boolean)
	 */
	public int toggleEnable() {
		return setEnable(!getEnable());
	}

	/**
	 * @return The value of enable/disable
	 */
	public boolean getEnable() {
		return enable;
	}

	/**
	 * @param manager
	 *            The active metadata provider to know the metadata avilability
	 */
	public void setActiveMetadataProvider(IMetadataProvider provider) {
		this.activeProvider = provider;
	}

	/**
	 * @param key
	 *            The key for the resource string.
	 * @return The resource string.
	 */
	public static String getString(String key) {
		return Platform.getResourceString(getDefault().getBundle(), key);
	}
}
