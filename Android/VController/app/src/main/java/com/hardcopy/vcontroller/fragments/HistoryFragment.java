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
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hardcopy.vcontroller.R;
import com.hardcopy.vcontroller.mqtt.Connection;
import com.hardcopy.vcontroller.mqtt.Connections;
import com.hardcopy.vcontroller.utils.Constants;
import com.hardcopy.vcontroller.utils.Utils;


/**
 * This fragment shows user defined message filters.
 */
@SuppressLint("ValidFragment")
public class HistoryFragment extends Fragment {

    private Context mContext = null;
    private IFragmentListener mFragmentListener = null;

    private View mRootView;
    private ScrollView mScrollMessage;
    private TextView mMessageBox;
    private ImageView mButtonToggle;
    private LinearLayout mLayoutInput;
    private EditText mEditTopic;
    private LinearLayout mLayoutMessage;
    private EditText mEditMessage;
    private RadioGroup mRadioQos;
    private LinearLayout mLayoutRetained;
    private CheckBox mCheckRetained;
    private Button mButtonPub;
    private Button mButtonSub;

    public static final int INPUT_CLOSE = 0;
    public static final int INPUT_PUBLISH = 1;
    public static final int INPUT_SUBSCRIBE = 2;

    private static int mInputMode = INPUT_PUBLISH;
    public static StringBuilder mHistory;
    public static String mHandle;

    private Bundle mUserInput = new Bundle();



    public HistoryFragment(Context c, IFragmentListener l, String handle) {
        mContext = c;
        mFragmentListener = l;
        if(handle != null)
            mHandle = handle;

        Connection connection = Connections.getInstance(mContext).getConnection(handle);
        if(mHistory == null) {
            mHistory = new StringBuilder(mContext.getString(R.string.init_msg_history) + "\n");
        }
        if(connection != null) {
            mHistory.append(connection.getHostName() + "\n");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        mRootView = rootView;

        mScrollMessage = (ScrollView) rootView.findViewById(R.id.scroll_history);
        mMessageBox = (TextView) rootView.findViewById(R.id.text_history);
        mButtonToggle = (ImageView) rootView.findViewById(R.id.inputButton);
        mButtonToggle.setOnClickListener(mOnClickListener);
        mLayoutInput = (LinearLayout) rootView.findViewById(R.id.inputGroup);
        addAutoCompleteAdapter();
        mEditTopic = (EditText) rootView.findViewById(R.id.topic);
        mLayoutMessage = (LinearLayout) rootView.findViewById(R.id.msgGroup);
        mEditMessage = (EditText) rootView.findViewById(R.id.message);
        mRadioQos = (RadioGroup) rootView.findViewById(R.id.qosRadio);
        mLayoutRetained = (LinearLayout) rootView.findViewById(R.id.retainedGroup);
        mCheckRetained = (CheckBox) rootView.findViewById(R.id.retained);
        mButtonPub = (Button) rootView.findViewById(R.id.buttonPub);
        mButtonPub.setOnClickListener(mOnClickListener);
        mButtonSub = (Button) rootView.findViewById(R.id.buttonSub);
        mButtonSub.setOnClickListener(mOnClickListener);

        setInputMode(mInputMode);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        saveUserInput(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        restoreUserInput();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMessageBox != null) {
            mMessageBox.setText(mHistory);
        }
        if(mScrollMessage != null) {
            mScrollMessage.fullScroll(ScrollView.FOCUS_DOWN);
        }
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
            restoreUserInput();
    }

    private void restoreUserInput() {
        String topic = mUserInput.getString(Constants.topic, Constants.empty);
        mEditTopic.setText(topic);
        String message = mUserInput.getString(Constants.message, Constants.empty);
        mEditMessage.setText(message);
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
                Utils.showToast(mContext.getString(R.string.errorTopic), Toast.LENGTH_SHORT);
                return false;
            }
            topic = new String(Constants.empty);
        } else {
            Utils.writeSuggestion(Utils.AUTO_COMPLETE_TOPIC, topic);  // remember server URI for auto-complete
        }
        if(message == null || message.length() < 1) {
            if(mInputMode == INPUT_PUBLISH && addAndConnect) {
                Utils.showToast(mContext.getString(R.string.errorMessage), Toast.LENGTH_SHORT);
                return false;
            }
            message = new String(Constants.empty);
        }

        // put the data collected into the bundle
        mUserInput.putString(Constants.message, message);
        mUserInput.putString(Constants.topic, topic);
        mUserInput.putInt(Constants.qos, qos);
        mUserInput.putBoolean(Constants.retained, retained);

        return true;
    }



    public void setInputMode(int mode) {
        switch(mode) {
            case INPUT_CLOSE:
                if(mLayoutInput != null) {
                    mLayoutInput.setVisibility(View.GONE);
                    mButtonToggle.setImageDrawable(
                            mContext.getResources().getDrawable(R.drawable.icon_sign_down));
                    mInputMode = mode;
                }
                break;
            case INPUT_PUBLISH:
                if(mLayoutInput != null) {
                    mLayoutInput.setVisibility(View.VISIBLE);
                    mButtonToggle.setImageDrawable(
                            mContext.getResources().getDrawable(R.drawable.icon_sign_left));
                    mButtonPub.setVisibility(View.VISIBLE);
                    mButtonSub.setVisibility(View.GONE);
                    mLayoutMessage.setVisibility(View.VISIBLE);
                    mLayoutRetained.setVisibility(View.VISIBLE);
                    mInputMode = mode;
                }
                break;
            case INPUT_SUBSCRIBE:
                if(mLayoutInput != null) {
                    mLayoutInput.setVisibility(View.VISIBLE);
                    mButtonToggle.setImageDrawable(
                            mContext.getResources().getDrawable(R.drawable.icon_sign_up));
                    mButtonPub.setVisibility(View.GONE);
                    mButtonSub.setVisibility(View.VISIBLE);
                    mLayoutMessage.setVisibility(View.GONE);
                    mLayoutRetained.setVisibility(View.GONE);
                    mInputMode = mode;
                }
                break;
        }
    }

    public static void appendHistoryToCache(String msg) {
        if(mHistory == null) {
            mHistory = new StringBuilder();
        }
        mHistory.append(msg);
    }

    public void appendHistory(String msg) {
        if(mMessageBox != null) {
            mMessageBox.append(msg);
        }
        if(mScrollMessage != null) {
            mScrollMessage.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public void setHandle(String handle) {
        mHandle = handle;
    }

    public void initHistory() {
        mHistory = new StringBuilder();
        if(mMessageBox != null) {
            mMessageBox.setText("");
        }
        if(mScrollMessage != null) {
            mScrollMessage.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public void reloadHistory() {
        if(mHandle == null)
            return;
        Connection c = Connections.getInstance(mContext).getConnection(mHandle);
        if(c == null)
            return;
        Spanned[] spanneds = c.history();
        mMessageBox.setText(spanneds.toString());
        mMessageBox.append("\n");
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.buttonPub:
                    if(saveUserInput(true)) {
                        mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_PUBLISH,
                                0, 0, null, null, mUserInput);
                        Utils.readSuggestion(Utils.AUTO_COMPLETE_TOPIC);
                        //initUserInput();
                    }
                    break;
                case R.id.buttonSub:
                    if(saveUserInput(true)) {
                        mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_SUBSCRIBE,
                                0, 0, null, null, mUserInput);
                        Utils.readSuggestion(Utils.AUTO_COMPLETE_TOPIC);
                        //initUserInput();
                    }
                    break;
                case R.id.inputButton:
                    switch(mInputMode) {
                        case INPUT_CLOSE:
                            setInputMode(INPUT_PUBLISH);
                            break;
                        case INPUT_PUBLISH:
                            setInputMode(INPUT_SUBSCRIBE);
                            break;
                        case INPUT_SUBSCRIBE:
                            setInputMode(INPUT_CLOSE);
                            break;
                    }
                    break;
            }
        }
    };

}
