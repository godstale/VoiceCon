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
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hardcopy.vcontroller.R;
import com.hardcopy.vcontroller.mqtt.CallbackBundle;
import com.hardcopy.vcontroller.mqtt.OpenFileDialog;
import com.hardcopy.vcontroller.utils.Constants;
import com.hardcopy.vcontroller.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * This fragment shows user defined message filters.
 */
@SuppressLint("ValidFragment")
public class AddBrokerFragment extends Fragment {

    private Context mContext = null;
    private IFragmentListener mFragmentListener = null;
    private static Dialog mDialog;
    private static final int mDialogId = 0;

    private EditText mEditClientId;
    private AutoCompleteTextView mAutoTextServer;
    private EditText mEditServerUri;
    private EditText mEditPort;

    private LinearLayout mLayoutAdvanced;
    private CheckBox mCheckCleanSession;
    private EditText mEditUserName;
    private EditText mEditPassword;
    private CheckBox mCheckSsl;
    private EditText mEditSslKeyPath;
    private Button mButtonSslLocation;
    private EditText mEditTimeOut;
    private EditText mEditKeepAlive;

    private LinearLayout mLayoutLastWill;
    private EditText mEditTopic;
    private EditText mEditMessage;
    private RadioGroup mRadioQos;
    private CheckBox mCheckRetained;

    private Button mButtonAdd;
    private Button mButtonReset;
    private ImageView mButtonShowAdvanced;
    private ImageView mButtonShowLastWill;

    public static Bundle mUserInput;
    private boolean mShowAdvanced = false;
    private boolean mShowLastWill = false;



    public AddBrokerFragment(Context c, IFragmentListener l) {
        mContext = c;
        mFragmentListener = l;

        if(mUserInput == null) {
            mUserInput = new Bundle();
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_broker, container, false);

        mAutoTextServer = (AutoCompleteTextView) rootView.findViewById(R.id.serverURI);
        addAutoCompleteAdapter();

        mEditClientId = (EditText) rootView.findViewById(R.id.clientId);
        mEditServerUri = (EditText) rootView.findViewById(R.id.serverURI);
        mEditPort = (EditText) rootView.findViewById(R.id.port);

        mLayoutAdvanced = (LinearLayout) rootView.findViewById(R.id.advancedBodyGroup);
        mCheckCleanSession = (CheckBox) rootView.findViewById(R.id.cleanSessionCheckBox);
        mEditUserName = (EditText) rootView.findViewById(R.id.uname);
        mEditPassword = (EditText) rootView.findViewById(R.id.password);
        mEditSslKeyPath = (EditText) rootView.findViewById(R.id.sslKeyLocaltion);
        mButtonSslLocation = (Button) rootView.findViewById(R.id.sslKeyBut);
        mButtonSslLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showFileChooser();
                mDialog = showFileDialog();
            }
        });
        mCheckSsl = (CheckBox) rootView.findViewById(R.id.sslCheckBox);
        mCheckSsl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mButtonSslLocation.setClickable(true);
                } else {
                    mButtonSslLocation.setClickable(false);
                }
            }
        });
        mButtonSslLocation.setClickable(false);
        mEditTimeOut = (EditText) rootView.findViewById(R.id.timeout);
        mEditKeepAlive = (EditText) rootView.findViewById(R.id.keepalive);

        mLayoutLastWill = (LinearLayout) rootView.findViewById(R.id.lastWillBodyGroup);
        mEditTopic = (EditText) rootView.findViewById(R.id.lastWillTopic);
        mEditMessage = (EditText) rootView.findViewById(R.id.lastWill);
        mRadioQos = (RadioGroup) rootView.findViewById(R.id.qosRadio);
        mCheckRetained = (CheckBox) rootView.findViewById(R.id.retained);

        mButtonAdd = (Button) rootView.findViewById(R.id.buttonAdd);
        mButtonAdd.setOnClickListener(mOnClickListener);
        mButtonReset = (Button) rootView.findViewById(R.id.buttonReset);
        mButtonReset.setOnClickListener(mOnClickListener);
        mButtonShowAdvanced = (ImageView) rootView.findViewById(R.id.advancedShowButton);
        mButtonShowAdvanced.setOnClickListener(mOnClickListener);
        mButtonShowLastWill = (ImageView) rootView.findViewById(R.id.lastWillShowButton);
        mButtonShowLastWill.setOnClickListener(mOnClickListener);

        showOrHideAdvanced();
        showOrHideLastWill();

        return rootView;
    }



    private Dialog showFileDialog() {
        Map<String, Integer> images = new HashMap<String, Integer>();
        images.put(OpenFileDialog.sRoot, R.drawable.ic_launcher);
        images.put(OpenFileDialog.sParent, R.drawable.icon_sign_up);
        images.put(OpenFileDialog.sFolder, R.drawable.icon_folder);
        images.put("bks", R.drawable.icon_file_code);
        images.put(OpenFileDialog.sEmpty, R.drawable.icon_file_empty);
        Dialog dialog = OpenFileDialog.createDialog(mDialogId, mContext, mContext.getString(R.string.sslKeySelect),
                new CallbackBundle() {
                    @Override
                    public void callback(Bundle bundle) {
                        String filepath = bundle.getString("path");
                        // setTitle(filepath);
                        mEditSslKeyPath.setText(filepath);
                    }
                }, ".bks;", images);
        dialog.show();
        return dialog;
    }

    private void initUserInput() {
        mUserInput = new Bundle();

        mUserInput.putString(Constants.clientId, Constants.empty);
        mUserInput.putString(Constants.server, Constants.empty);
        mUserInput.putString(Constants.port, Constants.empty);

        mUserInput.putBoolean(Constants.cleanSession, Constants.defaultCleanSession);
        mUserInput.putString(Constants.username, Constants.empty);
        mUserInput.putString(Constants.password, Constants.empty);

        mUserInput.putBoolean(Constants.ssl, Constants.defaultSsl);
        mUserInput.putString(Constants.ssl_key, Constants.empty);
        mUserInput.putInt(Constants.timeout, Constants.defaultTimeOut);
        mUserInput.putInt(Constants.keepalive, Constants.defaultKeepAlive);

        mUserInput.putString(Constants.message, Constants.empty);
        mUserInput.putString(Constants.topic, Constants.empty);
        mUserInput.putInt(Constants.qos, Constants.defaultQos);
        mUserInput.putBoolean(Constants.retained, Constants.defaultRetained);
        if(this.isVisible())
            restoreUserInput();
    }

    private void restoreUserInput() {
        if(mEditClientId == null)
            return;
        String clientId = mUserInput.getString(Constants.clientId, Constants.empty);
        mEditClientId.setText(clientId);
        String server = mUserInput.getString(Constants.server, Constants.empty);
        mEditServerUri.setText(server);
        String port = mUserInput.getString(Constants.port, Integer.toString(Constants.defaultPort));
        mEditPort.setText(port);

        mCheckCleanSession.setChecked(mUserInput.getBoolean(Constants.cleanSession, Constants.defaultCleanSession));
        String username = mUserInput.getString(Constants.username, Constants.empty);
        mEditUserName.setText(username);
        String password = mUserInput.getString(Constants.password, Constants.empty);
        mEditPassword.setText(password);

        mCheckSsl.setChecked(mUserInput.getBoolean(Constants.ssl, Constants.defaultSsl));
        String sslkey = mUserInput.getString(Constants.ssl_key, Constants.empty);
        mEditSslKeyPath.setText(sslkey);
        mEditTimeOut.setText(Integer.toString(mUserInput.getInt(Constants.timeout, Constants.defaultTimeOut)));
        mEditKeepAlive.setText(Integer.toString(mUserInput.getInt(Constants.keepalive, Constants.defaultKeepAlive)));

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

        String clientId = mEditClientId.getText().toString();
        String serverUri = mEditServerUri.getText().toString();
        String port = mEditPort.getText().toString();

        boolean cleanSession = mCheckCleanSession.isChecked();
        String username = mEditUserName.getText().toString();
        String password = mEditPassword.getText().toString();
        String sslkey = null;
        boolean ssl = mCheckSsl.isChecked();
        if(ssl) {
            sslkey = mEditSslKeyPath.getText().toString();
        }
        int keepalive;
        int timeout;
        try {
            timeout = Integer.parseInt(mEditTimeOut.getText().toString());
        }
        catch (NumberFormatException nfe) {
            timeout = Constants.defaultTimeOut;
        }
        try {
            keepalive = Integer.parseInt(mEditKeepAlive.getText().toString());
        }
        catch (NumberFormatException nfe) {
            keepalive = Constants.defaultKeepAlive;
        }
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
        if(clientId == null || clientId.length() < 1) {
            if(addAndConnect) {
                Utils.showToast(mContext.getString(R.string.errorClientId), Toast.LENGTH_SHORT);
                return false;
            }
        }
        if(serverUri == null || serverUri.length() < 1) {
            if(addAndConnect) {
                Utils.showToast(mContext.getString(R.string.errorServerUri), Toast.LENGTH_SHORT);
                return false;
            }
        } else {
            Utils.writeSuggestion(Utils.AUTO_COMPLETE_SERVER, serverUri);  // remember server URI for auto-complete
        }
        if(port == null || port.length() < 1 || !TextUtils.isDigitsOnly(port)) {
            if(addAndConnect) {
                Utils.showToast(mContext.getString(R.string.errorServerUri), Toast.LENGTH_SHORT);
                return false;
            }
            port = Integer.toString(Constants.defaultPort);
        }
        if(username == null)
            username = new String(Constants.empty);
        if(password == null)
            password = new String(Constants.empty);
        if(sslkey == null)
            sslkey = new String(Constants.empty);
        if(message == null)
            message = new String(Constants.empty);
        if(topic == null)
            topic = new String(Constants.empty);

        // put the daya collected into the bundle
        mUserInput.putString(Constants.clientId, clientId);
        mUserInput.putString(Constants.server, serverUri);
        mUserInput.putString(Constants.port, port);
        mUserInput.putBoolean(Constants.cleanSession, cleanSession);
        mUserInput.putString(Constants.username, username);
        mUserInput.putString(Constants.password, password);
        mUserInput.putBoolean(Constants.ssl, ssl);
        mUserInput.putString(Constants.ssl_key, sslkey);
        mUserInput.putInt(Constants.timeout, timeout);
        mUserInput.putInt(Constants.keepalive, keepalive);
        mUserInput.putString(Constants.message, message);
        mUserInput.putString(Constants.topic, topic);
        mUserInput.putInt(Constants.qos, qos);
        mUserInput.putBoolean(Constants.retained, retained);

        return true;
    }

    private void addAutoCompleteAdapter() {
        if(mAutoTextServer == null)
            return;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1);
        adapter.addAll(Utils.getInstance(mContext).readSuggestion(Utils.AUTO_COMPLETE_SERVER));
        mAutoTextServer.setAdapter(adapter);
    }

    private void showOrHideAdvanced() {
        if(mShowAdvanced) {
            mLayoutAdvanced.setVisibility(View.VISIBLE);
            mButtonShowAdvanced.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_sign_up));
        } else {
            mLayoutAdvanced.setVisibility(View.GONE);
            mButtonShowAdvanced.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_sign_down));
        }
    }

    private void showOrHideLastWill() {
        if(mShowLastWill) {
            mLayoutLastWill.setVisibility(View.VISIBLE);
            mButtonShowLastWill.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_sign_up));
        } else {
            mLayoutLastWill.setVisibility(View.GONE);
            mButtonShowLastWill.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_sign_down));
        }
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.buttonAdd:
                    boolean isSuccess = saveUserInput(true);
                    if(isSuccess) {
                        mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_ADD_CONNECTION,
                                0, 0, null, null, mUserInput);
                        initUserInput();
                        addAutoCompleteAdapter();
                    }
                    break;
                case R.id.buttonReset:
                    initUserInput();
                    break;
                case R.id.advancedShowButton:
                    mShowAdvanced = !mShowAdvanced;
                    showOrHideAdvanced();
                    break;
                case R.id.lastWillShowButton:
                    mShowLastWill = !mShowLastWill;
                    showOrHideLastWill();
                    break;
            }
        }
    };

}
