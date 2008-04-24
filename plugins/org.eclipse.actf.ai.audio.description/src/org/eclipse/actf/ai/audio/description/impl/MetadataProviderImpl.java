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
package org.eclipse.actf.ai.audio.description.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import org.eclipse.actf.ai.audio.description.IMetadata;
import org.eclipse.actf.ai.audio.description.IMetadataProvider;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MetadataProviderImpl implements IMetadataProvider {

	private static class Key {
		public String id;

		public String lang;

		public Key(String id, String lang) {
			this.id = id;
			if (lang == null || lang.length() == 0) {
				this.lang = "";
			} else {
				this.lang = (new Locale(lang)).getLanguage();
			}
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return toString().equals(o.toString());
		}

		@Override
		public String toString() {
			return id + lang;
		}
	}

	private HashMap<Key, ArrayList<IMetadata>> metadataList = new HashMap<Key, ArrayList<IMetadata>>();

	private ArrayList<IMetadata> metadata;

	private ArrayList<IXMLInfo> entries;

	private Locale locale = null;

	public MetadataProviderImpl(ArrayList<IXMLInfo> entries) {
		this.entries = entries;
	}

	public void reload() {
		metadataList.clear();
		readFile();
	}

	private Stack<String> langStack = new Stack<String>();

	private class BaseHandler extends DefaultHandler {
		private BaseHandler back;

		private IXMLInfo entry;

		public BaseHandler(BaseHandler back, IXMLInfo entry) {
			this.back = back;
			this.entry = entry;
		}

		public BaseHandler(BaseHandler back) {
			this.back = back;
			this.entry = back.getEntry();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (attributes.getValue("xml:lang") != null) {
				langStack.push(attributes.getValue("xml:lang"));
			} else {
				langStack.push(langStack.peek() + "");
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			langStack.pop();
			getEntry().setContentHandler(back);
		}

		public IXMLInfo getEntry() {
			return entry;
		}
	}

	private class AllHandler extends BaseHandler {
		private boolean altFlag;

		public AllHandler(IXMLInfo entry) {
			super(null, entry);
			langStack.clear();
			langStack.push("");
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);

			if (altFlag) {
				if ("item".equals(localName)) {
					String importance = attributes.getValue("importance");
					if (importance == null || importance.length() == 0) {
						importance = "middle";
					}
					getEntry().setContentHandler(
							new ItemHandler(this, importance));
				}
			} else {
				if ("alternative".equals(localName)) {
					String type = attributes.getValue("type");
					if (type != null && type.equals("audio-description")) {
						altFlag = true;
					}
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// super.endElement(uri, localName, qName);
		}
	}

	private class ItemHandler extends BaseHandler {
		private boolean descFlag = false;

		private boolean startFlag = false;

		private boolean durationFlag = false;

		private StringBuffer buf = new StringBuffer();

		private String start;

		private String duration;

		private ArrayList<String> desc = new ArrayList<String>();

		private ArrayList<String> lang = new ArrayList<String>();

		private String importance;

		public ItemHandler(BaseHandler back, String importance) {
			super(back);
			this.importance = importance;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);

			if ("start".equals(localName)) {
				if (attributes.getValue("type").equals("relTime")) {
					startFlag = true;
				}
			} else if ("duration".equals(localName)) {
				durationFlag = true;
			} else if ("description".equals(localName)) {
				descFlag = true;
			}

		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (startFlag) {
				buf.append(ch, start, length);
			} else if (durationFlag) {
				buf.append(ch, start, length);
			} else if (descFlag) {
				buf.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if ("start".equals(localName)) {
				startFlag = false;
				start = buf.toString();
				buf.delete(0, buf.length());
			} else if ("duration".equals(localName)) {
				durationFlag = false;
				duration = buf.toString();
				buf.delete(0, buf.length());
			} else if ("description".equals(localName)) {
				descFlag = false;
				desc.add(buf.toString());
				lang.add(langStack.peek());
				buf.delete(0, buf.length());
			} else if ("item".equals(localName)) {

				for (int i = 0; i < desc.size(); i++) {
					MetadataImpl mi = new MetadataImpl(start, duration, desc
							.get(i), lang.get(i), importance);
					// System.out.println(start + ", " + duration + ", " +
					// desc.get(i) + ", " + lang.get(i));

					Key key = new Key(getEntry().getDocumentation(), lang
							.get(i));

					ArrayList<IMetadata> list = metadataList.get(key);

					if (list == null) {
						list = new ArrayList<IMetadata>();
						metadataList.put(key, list);
					}
					list.add(mi);
				}
				super.endElement(uri, localName, qName);
			}
		}
	}

	private void readFile() {
		for (int i = 0; i < entries.size(); i++) {
			IXMLInfo entry = entries.get(i);

			AllHandler ah = new AllHandler(entry);
			entry.setContentHandler(ah);
			try {
				entry.startSAX();
			} catch (XMLStoreException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	ArrayList<ArrayList<IMetadata>> alternatives;

	public void prepareMetadata() {
		prepareMetadata(this.locale);
	}

	public void prepareMetadata(Locale locale) {
		Set<Key> keys = metadataList.keySet();
		alternatives = new ArrayList<ArrayList<IMetadata>>();
		for (Iterator<Key> i = keys.iterator(); i.hasNext();) {
			Key key = i.next();
			if (key.lang.equals(locale.getLanguage())) {
				alternatives.add(metadataList.get(key));
			}
		}
		if (alternatives.size() > 0)
			setMetadata(alternatives.get(0));
	}

	public void setMetadata(ArrayList<IMetadata> metadata) {
		this.metadata = metadata;
	}

	public IMetadata getItem(int index) {
		if (0 <= index && index < metadata.size())
			return metadata.get(index);
		return null;
	}

	public ArrayList<IMetadata> getAllItems() {
		return metadata;
	}

	public int getIndex(int position) {
		if (metadata == null)
			return 0;

		int ret = 0;
		for (int i = 0; i < metadata.size(); i++) {
			if (metadata.get(i).getStartTime() >= position) {
				ret = i - 1;
				return ret;
			}
		}
		return metadata.size() - 1;
	}

	public int getSize() {
		if (metadata == null)
			return 0;
		return metadata.size();
	}

	public boolean hasMetadata() {
		return metadata.size() > 0;
	}
}
