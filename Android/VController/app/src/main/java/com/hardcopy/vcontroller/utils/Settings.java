/*
 * Copyright (C) 2014 The Retro Watch - Open source smart watch project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hardcopy.vcontroller.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Settings {
	
	private static Settings mSettings = null;
	
	private Context mContext;
	private int mResultType = -1;
	
	
	
	public synchronized static Settings getInstance(Context c) {
		if(mSettings == null) {
			mSettings = new Settings(c);
		}
		return mSettings;
	}
	
	public Settings(Context c) {
		if(mContext == null) {
			mContext = c;
			initialize();
		}
	}
	
	
	private void initialize() {
	}
	
	
	public synchronized void finalize() {
		mContext = null;
		mSettings = null;
	}

	public synchronized void setShowFloating(boolean isShow) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.PREFERENCE_KEY_SET_SHOW_FLOATING, isShow);
		editor.commit();
	}

	public synchronized boolean getShowFloating() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(Constants.PREFERENCE_KEY_SET_SHOW_FLOATING, Constants.defaultShowFloat);
	}

	public synchronized void setSendingInterval(int interval) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(Constants.PREFERENCE_KEY_SET_INTERVAL, interval);
		editor.commit();
	}

	public synchronized int getSendingInterval() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(Constants.PREFERENCE_KEY_SET_INTERVAL, Constants.defaultInterval);
	}

	public synchronized void setResultType(int type) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(Constants.PREFERENCE_KEY_SET_RES_TYPE, type);
		editor.commit();
		mResultType = type;
	}

	public synchronized int getResultType() {
		if(mResultType > -1)
			return mResultType;
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(Constants.PREFERENCE_KEY_SET_RES_TYPE, Constants.defaultResultType);
	}

	public synchronized void setCommandSettings(Bundle command) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.PREFERENCE_KEY_CMD_TOPIC,
				command.getString(Constants.topic, ""));
		editor.putInt(Constants.PREFERENCE_KEY_CMD_QOS,
				command.getInt(Constants.qos, Constants.defaultQos));
		editor.putBoolean(Constants.PREFERENCE_KEY_CMD_RETAINED,
				command.getBoolean(Constants.retained, Constants.defaultRetained));
		editor.commit();
	}

	public synchronized Bundle getCommandSettings() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		Bundle data = new Bundle();
		data.putString(Constants.topic,
				prefs.getString(Constants.PREFERENCE_KEY_CMD_TOPIC, ""));
		data.putInt(Constants.qos,
				prefs.getInt(Constants.PREFERENCE_KEY_CMD_QOS, Constants.defaultQos));
		data.putBoolean(Constants.retained,
				prefs.getBoolean(Constants.PREFERENCE_KEY_CMD_RETAINED, Constants.defaultRetained));
		return data;
	}

	
}
