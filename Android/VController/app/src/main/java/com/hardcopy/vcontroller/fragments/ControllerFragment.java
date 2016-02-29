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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.hardcopy.vcontroller.R;
import com.hardcopy.vcontroller.utils.Constants;
import com.hardcopy.vcontroller.utils.Settings;
import com.hardcopy.vcontroller.utils.Utils;
import com.larswerkman.holocolorpicker.ColorPicker;


@SuppressLint("ValidFragment")
public class ControllerFragment extends Fragment {

    private Context mContext = null;
    private IFragmentListener mFragmentListener = null;

    private View mRootView;

    private EditText mEditTopic;
    private ImageView mImageTopicWarning;
    private EditText mEditMessage;
    private RadioGroup mRadioQos;
    private CheckBox mCheckRetained;

    private ImageView mVoiceImageView;
    private TextView mTextStatus;

    private static Bundle mUserInput;
    private static boolean mVoiceStatus;
    private boolean mTopicWarningEnabled;


    
    public ControllerFragment(Context c, IFragmentListener l) {
        mContext = c;
        mFragmentListener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_controller, container, false);

        mEditTopic = (EditText) mRootView.findViewById(R.id.topic);
        mEditTopic.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mTopicWarningEnabled) {
                    showTopicWarning(false);
                    mEditTopic.setHint(mContext.getString(R.string.setTargetTopic));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mImageTopicWarning = (ImageView) mRootView.findViewById(R.id.topicWarningImage);
        showTopicWarning(false);
        mEditMessage = (EditText) mRootView.findViewById(R.id.message);
        mRadioQos = (RadioGroup) mRootView.findViewById(R.id.qosRadio);
        mCheckRetained = (CheckBox) mRootView.findViewById(R.id.retained);

        mVoiceImageView = (ImageView) mRootView.findViewById(R.id.voiceImageView);
        mVoiceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // request voice recognition to activity
                mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_START_VOICE,
                        0, 0, null, null, null);
            }
        });
        mTextStatus = (TextView) mRootView.findViewById(R.id.statusText);

        // TODO:

        ControllerContainer cc = new ControllerContainer(mContext, mFragmentListener);
        cc.initControlViews(mRootView);

        addAutoCompleteAdapter();
        initUserInput();
        
        return mRootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        saveUserInput(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        restoreUserInput(true);
        setVoiceStatus(mVoiceStatus);
    }



    private void showTopicWarning(boolean isShow) {
        if(isShow) {
            mImageTopicWarning.setVisibility(View.VISIBLE);
        } else {
            mImageTopicWarning.setVisibility(View.GONE);
        }
        mTopicWarningEnabled = isShow;
    }

    private void addAutoCompleteAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1);
        adapter.addAll(Utils.getInstance(mContext).readSuggestion(Utils.AUTO_COMPLETE_TOPIC));
        AutoCompleteTextView textView = (AutoCompleteTextView) mRootView.findViewById(R.id.topic);
        textView.setAdapter(adapter);
    }

    private void initUserInput() {
        mUserInput = new Bundle();

        mUserInput.putString(Constants.topic, Constants.empty);
        mUserInput.putString(Constants.message, Constants.empty);
        mUserInput.putInt(Constants.qos, Constants.defaultQos);
        mUserInput.putBoolean(Constants.retained, Constants.defaultRetained);
        if(this.isVisible())
            restoreUserInput(false);
    }

    private void restoreUserInput(boolean getFromPref) {
        if(getFromPref) {
            mUserInput = Settings.getInstance(mContext).getCommandSettings();
        }
        String topic = mUserInput.getString(Constants.topic, Constants.empty);
        mEditTopic.setText(topic);
        RadioButton wRadio = (RadioButton)mRadioQos.getChildAt(mUserInput.getInt(Constants.qos, Constants.defaultQos));
        wRadio.setChecked(true);
        mCheckRetained.setChecked(mUserInput.getBoolean(Constants.retained, Constants.defaultRetained));
    }

    private boolean saveUserInput(boolean addAndConnect) {
        mUserInput = new Bundle();

        String topic = mEditTopic.getText().toString();
        String message = mEditMessage.getText().toString();
        int checkedId = mRadioQos.getCheckedRadioButtonId();
        int qos = 0;
        //determine which qos value has been selected
        switch (checkedId) {
            case R.id.qos0 :
                qos = 0;
                break;
            case R.id.qos1 :
                qos = 1;
                break;
            case R.id.qos2 :
                qos = 2;
                break;
        }
        boolean retained = mCheckRetained.isChecked();

        // check input value
        if(topic == null || topic.length() < 1) {
            if(addAndConnect) {
                showTopicWarning(true);
                mEditTopic.setHint(mContext.getString(R.string.setTargetTopic));
                return false;
            }
            topic = new String(Constants.empty);
        } else {
            Utils.writeSuggestion(Utils.AUTO_COMPLETE_TOPIC, topic);  // remember server URI for auto-complete
        }

        // put the data collected into the bundle
        mUserInput.putString(Constants.message, message);
        mUserInput.putString(Constants.topic, topic);
        mUserInput.putInt(Constants.qos, qos);
        mUserInput.putBoolean(Constants.retained, retained);

        Settings.getInstance(mContext).setCommandSettings(mUserInput);

        return true;
    }



    public Bundle getMqttPublishInfo() {
        boolean isSuccess = saveUserInput(true);
        if(isSuccess) {
            return mUserInput;
        }
        return null;
    }

    public void setVoiceCommand(String[] command) {
        if(command == null || command.length < 1)
            return;
        StringBuilder cmdString = new StringBuilder();
        for(int i=0; i<command.length; i++) {
            cmdString.append(command[i]).append(", ");
        }
        mEditMessage.setText(cmdString);
    }

    public void setVoiceStatus(boolean isWorking) {
        mVoiceStatus = isWorking;
        if(isWorking) {
            mVoiceImageView.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.icon_microphone2));
            mTextStatus.setText(mContext.getText(R.string.speech_listening));
        } else {
            mVoiceImageView.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.icon_microphone1));
            mTextStatus.setText(mContext.getText(R.string.speech_stopped));
        }
    }

    public void setPublishResult(String result) {
        mTextStatus.setText(result);
    }



    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.buttonPub:
                    break;
            }
        }
    };
    
    
}
