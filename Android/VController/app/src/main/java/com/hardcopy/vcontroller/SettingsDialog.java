package com.hardcopy.vcontroller;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.hardcopy.vcontroller.fragments.IDialogListener;
import com.hardcopy.vcontroller.utils.Constants;
import com.hardcopy.vcontroller.utils.Settings;

public class SettingsDialog extends Dialog {

    // Global
    private static final String tag = "SettingsDialog";
    private static final String[] LANGUAGE_CODE = {"en-US", "ko-KR"};

    private String mDialogTitle;

    // Context, system
    private Context mContext;
    private IDialogListener mDialogListener;
    private OnClickListener mClickListener;

    // Layout
    private CheckBox mCheckFloat;
    private EditText mEditInterval;
    private RadioGroup mRadioResult;
    private Spinner mSelectSpinner;
    private Button mBtnReset;
    private Button mBtnClose;

    private TextView mTextEnabled;

    // Params
    private Object mContentObject;
    private String mLanguage;

    // Constructor
    public SettingsDialog(Context context) {
        super(context);
        mContext = context;
    }
    public SettingsDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }
    
    /*****************************************************
     *        Overrided methods
     ******************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        //----- Set title
        if(mDialogTitle != null) {
            setTitle(mDialogTitle);
        } else {
            //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();    
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_settings);
        mClickListener = new OnClickListener(this);

        mCheckFloat = (CheckBox) findViewById(R.id.showFloat);
        mCheckFloat.setOnClickListener(mClickListener);
        mEditInterval = (EditText) findViewById(R.id.interval);
        mEditInterval.addTextChangedListener(mIntervalListener);
        mRadioResult = (RadioGroup) findViewById(R.id.resultRadio);
        mRadioResult.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("VController", "Check changed to "+checkedId);
                int view_id = mRadioResult.getCheckedRadioButtonId();
                RadioButton button = (RadioButton) mRadioResult.findViewById(view_id);
                int type = Integer.parseInt((String) button.getTag());
                Settings.getInstance(mContext).setResultType(type);
            }
        });
        mSelectSpinner = (Spinner) findViewById(R.id.localeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.locale_array,
                R.layout.spinner_simple_item2);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_simple_item);
        //mSelectSpinner.setPrompt(mContext.getString(R.string.ctrl_spinner_title));
        mSelectSpinner.setAdapter(adapter);
        mSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mLanguage.equalsIgnoreCase(LANGUAGE_CODE[position])) // nothing changed
                    return;
                mLanguage = LANGUAGE_CODE[position];
                Settings.getInstance(mContext).setLanguage(LANGUAGE_CODE[position]);
                if(mDialogListener != null)
                    mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_SETTINGS_SET_LANGUAGE,
                            0, 0, null, null, null);
            }
            @Override public void onNothingSelected(AdapterView<?> arg0) {}
        });
        mBtnClose = (Button) findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(mClickListener);

        setContent();
    }
    
    @Override
    protected  void onStop() {
        super.onStop();
    }

    
    /*****************************************************
     *        Public methods
     ******************************************************/
    public void setDialogParams(IDialogListener listener, String title, Object co) {
        mDialogListener = listener;
        mDialogTitle = title;
        mContentObject = co;
    }
    
    /*****************************************************
     *        Private methods
     ******************************************************/
    private void setContent() {
        int result_type = Settings.getInstance(mContext).getResultType();
        RadioButton wRadio = (RadioButton)mRadioResult.getChildAt(result_type);
        wRadio.setChecked(true);
        int time = Settings.getInstance(mContext).getSendingInterval();
        mEditInterval.setText(Integer.toString(time));
        boolean isShow = Settings.getInstance(mContext).getShowFloating();
        mCheckFloat.setChecked(isShow);

        int position = 0;
        mLanguage = Settings.getInstance(mContext).getLanguage();
        for(int i=0; i<LANGUAGE_CODE.length; i++) {
            if(mLanguage.equalsIgnoreCase(LANGUAGE_CODE[i])) {
                position = i;
                break;
            }
        }
        mSelectSpinner.setSelection(position);
    }    // End of setContent()
    
    /*****************************************************
     *        Callback, Listener, Sub classes
     ******************************************************/

    private TextWatcher mIntervalListener = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String input = mEditInterval.getText().toString();
            if(input != null && input.length() > 0) {
                int time = Integer.parseInt(input);
                Settings.getInstance(mContext).setSendingInterval(time);
            }
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    };

    private class OnClickListener implements View.OnClickListener {
        SettingsDialog mDialogContext;
        
        public OnClickListener(SettingsDialog context) {
            mDialogContext = context;
        }
        
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.showFloat:
                    boolean isShow = mCheckFloat.isChecked();
                    Settings.getInstance(mContext).setShowFloating(isShow);
                    if(mDialogListener != null)
                        mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_SETTINGS_FLOATING_UI,
                                (isShow ? 1 : 0), 0, null, null, null);
                    break;

                case R.id.btn_close:
                    mDialogContext.dismiss();
                    if(mDialogListener != null)
                        mDialogListener.OnDialogCallback(IDialogListener.CALLBACK_CLOSE, 0, 0, null, null, null);
                    break;
            }
        }
    }    // End of class OnClickListener
}
