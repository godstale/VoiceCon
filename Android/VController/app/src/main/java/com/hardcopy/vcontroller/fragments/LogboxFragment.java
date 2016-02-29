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

package com.hardcopy.vcontroller.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hardcopy.vcontroller.R;


/**
 * This fragment shows user defined message filters.
 */
@SuppressLint("ValidFragment")
public class LogboxFragment extends Fragment {

    private Context mContext = null;
    private IFragmentListener mFragmentListener = null;

    private ScrollView mScrollMessage;
    private TextView mMessageBox;

    public static StringBuilder mLog;



    public LogboxFragment(Context c, IFragmentListener l) {
        mContext = c;
        mFragmentListener = l;

        if(mLog == null) {
            mLog = new StringBuilder(mContext.getString(R.string.init_msg));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMessageBox != null) {
            mMessageBox.setText(mLog);
        }
        if(mScrollMessage != null) {
            mScrollMessage.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logbox, container, false);

        mScrollMessage = (ScrollView) rootView.findViewById(R.id.scroll_logbox);
        mMessageBox = (TextView) rootView.findViewById(R.id.logbox);

        return rootView;
    }

    public static void appendLogToCache(String msg) {
        if(mLog == null) {
            mLog = new StringBuilder();
        }
        mLog.append(msg);
    }
    
    public void appendLog(String msg) {
        if(mMessageBox != null) {
            mMessageBox.append(msg);
        }
        if(mScrollMessage != null) {
            mScrollMessage.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public void initLog() {
        mLog = new StringBuilder();
        if(mMessageBox != null) {
            mMessageBox.setText("");
        }
        if(mScrollMessage != null) {
            mScrollMessage.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
