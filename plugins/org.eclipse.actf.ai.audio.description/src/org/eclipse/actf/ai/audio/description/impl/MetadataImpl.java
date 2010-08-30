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

import java.util.Locale;

import org.eclipse.actf.ai.audio.description.IMetadata;
import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.voice.IVoice;

public class MetadataImpl implements IMetadata {
	private double scale = 1;

	private String sTime;

	private int type = MASK_SPEAK;

	private int start;

	private int originalTime;

	private int duration;

	private String description;

	private int importance;

	private String lang;

	private int speed = IVoice.SPEED_NORMAL;

	private String gender = ITTSEngine.GENDER_MALE;

	/*
	 * private String addition = "";
	 * 
	 * private boolean hasAddition = false;
	 * 
	 * private String additionGender = ITTSEngine.GENDER_MALE;
	 * 
	 * private int additionSpeed = IVoice.SPEED_NORMAL;
	 */

	private boolean hasValidWav = false;

	private int wavSpeed = 100;

	private String wavUri = "";

	private String wavLocal = "";

	private boolean wavEnabled = true;

	public MetadataImpl(String start, String duration, String desc,
			String lang, String importance, String speed, String gender,
			String extended) {
		setStart(start);
		setDuration(duration);
		setLang(lang);
		setDescription(desc);
		setImportance(importance);
		setSpeed(speed);
		setGender(gender);
		setExtended(extended);
	}

	private void setExtended(String extended) {
		if ("true".equalsIgnoreCase(extended)) {
			setType(MASK_PAUSE_DURING_SPEAK | MASK_SPEAK);
		}
	}

	private void setSpeed(String speed) {
		try {
			int speedVal = Integer.parseInt(speed);
			if (speedVal >= IVoice.SPEED_MIN && speedVal <= IVoice.SPEED_MAX) {
				this.speed = speedVal;
			}
		} catch (Exception e) {
		}
	}

	private void setGender(String gender) {
		if (ITTSEngine.GENDER_MALE.equalsIgnoreCase(gender)) {
			this.gender = ITTSEngine.GENDER_MALE;
		} else if (ITTSEngine.GENDER_FEMALE.equalsIgnoreCase(gender)) {
			this.gender = ITTSEngine.GENDER_FEMALE;
		}
	}

	private void setType(int type) {
		this.type = type;
	}

	private void setStart(String start) {
		this.start = string2time(start);
		setStringTime();
	}

	private void setDuration(String duration) {
		this.duration = string2time(duration);
	}

	private void setLang(String lang) {
		this.lang = (new Locale(lang)).getLanguage();
	}

	private void setDescription(String desc) {
		this.description = desc;
	}

	private void setImportance(String importance) {
		importance = importance.toLowerCase();
		if (importance.equals("low")) {
			this.importance = IMPORTANCE_LOW;
		} else if (importance.equals("middle")) {
			this.importance = IMPORTANCE_MIDDLE;
		} else if (importance.equals("high")) {
			this.importance = IMPORTANCE_HIGH;
		}
	}

	private int string2time(String str) {
		int time = 0;
		try {
			String[] part = str.split(":");
			int[] r = new int[] { 1, 1000, 60000, 3600000 };

			time = 0;
			for (int i = part.length - 1, j = 0; i >= 0; i--, j++) {
				int temp = Integer.parseInt(part[i]);
				time += temp * r[j];
			}
			time /= 10;
		} catch (Exception e) {
			e.printStackTrace();
			time = 0;
		}
		return time;
	}

	private void setStringTime() {
		int temp = getStartTime();
		int mm = 0;
		int s = 0;
		int m = 0;
		int h = 0;
		mm = temp % 100;
		temp -= mm;
		temp /= 100;
		s = temp % 60;
		temp -= s;
		temp /= 60;
		m = temp % 60;
		temp -= m;
		temp /= 60;
		h = temp;

		StringBuffer sb = new StringBuffer();
		if (h != 0) {
			sb.append(h + "h");
		}
		sb.append((m < 10 ? "0" : "") + m + "m");
		sb.append((s < 10 ? "0" : "") + s + "s");
		sb.append((mm < 10 ? "0" : "") + mm);

		sTime = sb.toString();
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public String getDescription() {
		return description;
	}

	public int getStartTime() {
		int temp = start;
		if (temp < 0) {
			temp = 0;
		}
		return (int) (temp * scale);
	}

	public int getType() {
		return type;
	}

	@Override
	public String toString() {
		return start + " (" + duration + ") " + description + ", " + importance;
	}

	public String getStringTime() {
		return sTime;
	}

	public void refresh() {
		start = originalTime;
		setStringTime();
	}

	public int getDuration() {
		return duration;
	}

	public String getLang() {
		return lang;
	}

	public int getImportance() {
		return importance;
	}

	public int getSpeed() {
		return speed;
	}

	public String getGender() {
		return gender;
	}

	/*
	 * public String getAddition() { return addition; }
	 * 
	 * public void setAddition(String addition) { if (addition != null) {
	 * this.addition = addition; hasAddition = true; } }
	 * 
	 * public boolean hasAddition() { return hasAddition; }
	 * 
	 * public String getAdditionGender() { return additionGender; }
	 * 
	 * public int getAdditionSpeed() { return additionSpeed; }
	 * 
	 * public void setAdditionGender(String gender) { if
	 * (ITTSEngine.GENDER_MALE.equalsIgnoreCase(gender)) { this.additionGender =
	 * ITTSEngine.GENDER_MALE; } else if
	 * (ITTSEngine.GENDER_FEMALE.equalsIgnoreCase(gender)) { this.additionGender
	 * = ITTSEngine.GENDER_FEMALE; } }
	 * 
	 * public void setAdditionSpeed(String speed) { try { int speedVal =
	 * Integer.parseInt(speed); if (speedVal >= IVoice.SPEED_MIN && speedVal <=
	 * IVoice.SPEED_MAX) { this.additionSpeed = speedVal; } } catch (Exception
	 * e) { } }
	 */

	public boolean getWavEnabled() {
		return wavEnabled;
	}

	public String getWavLocal() {
		return wavLocal;
	}

	public int getWavSpeed() {
		return wavSpeed;
	}

	public String getWavUri() {
		return wavUri;
	}

	public void setWavEnabled(String wavEnabled) {
		// TODO Auto-generated method stub

	}

	public void setWavLocal(String wavLocal) {
		this.wavLocal = wavLocal;
		// TODO validation
		hasValidWav = true;
	}

	public void setWavSpeed(String wavSpeed) {
		// TODO Auto-generated method stub

	}

	public void setWavUri(String wavUri) {
		// TODO Auto-generated method stub

	}

	public boolean hasValidWav() {
		return hasValidWav;
	}

}
