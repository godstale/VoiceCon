package com.hardcopy.vcontroller.fragments;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.hardcopy.vcontroller.R;
import com.hardcopy.vcontroller.contents.ControlType;
import com.hardcopy.vcontroller.utils.Settings;
import com.larswerkman.holocolorpicker.ColorPicker;

/**
 * Created by hardcopyworld on 2016-02-28.
 */
public class ControllerContainer {

    /**
     * This contants must be same with the order of spinner
     * and layout array
     */
    private static final int LAYOUT_SWITCH = 0;
    private static final int LAYOUT_SLIDE = 1;
    private static final int LAYOUT_KEYPAD = 2;
    private static final int LAYOUT_TEXT = 3;
    private static final int LAYOUT_COLOR = 4;
    private static final int CONTROL_COUNT = 5;
    private static final int SWITCH_COUNT = 5;
    private static final int SLIDE_COUNT = 5;
    private static final int KEYPAD_COUNT = 16;
    private static final int TEXT_COUNT = 1;

    private Context mContext;
    private IFragmentListener mFragmentListener = null;
    private View mRootView;

    private Spinner mSelectSpinner;
    private int mLayoutSelected = LAYOUT_SWITCH;
    private LinearLayout[] mControlLayout = new LinearLayout[CONTROL_COUNT];
    private Switch[] mSwitchArray = new Switch[SWITCH_COUNT];
    private SeekBar[] mSeekBarArray = new SeekBar[SLIDE_COUNT];
    private Button[] mButtonArray = new Button[KEYPAD_COUNT];
    private EditText[] mEditArray = new EditText[TEXT_COUNT];
    private Button mButtonSubmit;
    private ColorPicker mColorPicker;

    private boolean[] mSwitchStatus = new boolean[SWITCH_COUNT];
    private int[] mSlideStatus = new int[SWITCH_COUNT];
    private char mKeyCode = 0;
    private String mInputText;
    private int mColor;
    private int mColorAlpha, mColorRed, mColorGreen, mColorBlue;


    public ControllerContainer(Context c, IFragmentListener l) {
        mContext = c;
        mFragmentListener = l;
    }

    public void initControlViews(View v) {
        if(v == null)
            return;
        mRootView = v;

        mSelectSpinner = (Spinner) mRootView.findViewById(R.id.selectSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.control_type_array,
                R.layout.spinner_simple_item2);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_simple_item);
        mSelectSpinner.setPrompt(mContext.getString(R.string.ctrl_spinner_title));
        mSelectSpinner.setAdapter(adapter);
        mSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showAndHideLayout(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mControlLayout[0] = (LinearLayout) mRootView.findViewById(R.id.switchGroup);
        mControlLayout[1] = (LinearLayout) mRootView.findViewById(R.id.slideGroup);
        mControlLayout[2] = (LinearLayout) mRootView.findViewById(R.id.keypadGroup);
        mControlLayout[3] = (LinearLayout) mRootView.findViewById(R.id.textInputGroup);
        mControlLayout[4] = (LinearLayout) mRootView.findViewById(R.id.colorGroup);

        mSwitchArray[0] = (Switch) mRootView.findViewById(R.id.switch1);
        mSwitchArray[1] = (Switch) mRootView.findViewById(R.id.switch2);
        mSwitchArray[2] = (Switch) mRootView.findViewById(R.id.switch3);
        mSwitchArray[3] = (Switch) mRootView.findViewById(R.id.switch4);
        mSwitchArray[4] = (Switch) mRootView.findViewById(R.id.switch5);
        for(int i=0; i<SWITCH_COUNT; i++) {
            mSwitchArray[i].setOnCheckedChangeListener(mSwitchListener);
            mSwitchArray[i].setTag(new ViewTag(LAYOUT_SWITCH, i, null, 0, 0));
            mSwitchStatus[i] = false;
            mSwitchArray[i].setChecked(false);
        }

        mSeekBarArray[0] = (SeekBar) mRootView.findViewById(R.id.slide1);
        mSeekBarArray[1] = (SeekBar) mRootView.findViewById(R.id.slide2);
        mSeekBarArray[2] = (SeekBar) mRootView.findViewById(R.id.slide3);
        mSeekBarArray[3] = (SeekBar) mRootView.findViewById(R.id.slide4);
        mSeekBarArray[4] = (SeekBar) mRootView.findViewById(R.id.slide5);
        for(int i=0; i<SLIDE_COUNT; i++) {
            mSeekBarArray[i].setOnSeekBarChangeListener(mSeekBarListener);
            mSeekBarArray[i].setTag(new ViewTag(LAYOUT_SLIDE, i, null, 0, 0));
            mSlideStatus[i] = 0;
        }

        mButtonArray[0] = (Button) mRootView.findViewById(R.id.keypad_0);
        mButtonArray[1] = (Button) mRootView.findViewById(R.id.keypad_1);
        mButtonArray[2] = (Button) mRootView.findViewById(R.id.keypad_2);
        mButtonArray[3] = (Button) mRootView.findViewById(R.id.keypad_3);
        mButtonArray[4] = (Button) mRootView.findViewById(R.id.keypad_4);
        mButtonArray[5] = (Button) mRootView.findViewById(R.id.keypad_5);
        mButtonArray[6] = (Button) mRootView.findViewById(R.id.keypad_6);
        mButtonArray[7] = (Button) mRootView.findViewById(R.id.keypad_7);
        mButtonArray[8] = (Button) mRootView.findViewById(R.id.keypad_8);
        mButtonArray[9] = (Button) mRootView.findViewById(R.id.keypad_9);
        mButtonArray[10] = (Button) mRootView.findViewById(R.id.keypad_as);
        mButtonArray[11] = (Button) mRootView.findViewById(R.id.keypad_sh);
        mButtonArray[12] = (Button) mRootView.findViewById(R.id.keypad_pl);
        mButtonArray[13] = (Button) mRootView.findViewById(R.id.keypad_mi);
        mButtonArray[14] = (Button) mRootView.findViewById(R.id.keypad_mu);
        mButtonArray[15] = (Button) mRootView.findViewById(R.id.keypad_di);
        for(int i=0; i<KEYPAD_COUNT; i++) {
            mButtonArray[i].setOnClickListener(mKeypadListener);
            mKeyCode = 0;
        }

        mEditArray[0] = (EditText) mRootView.findViewById(R.id.textInput1);
        mButtonSubmit = (Button) mRootView.findViewById(R.id.buttonSubmit);
        mButtonSubmit.setOnClickListener(mButtonListener);

        mColorPicker = (ColorPicker) mRootView.findViewById(R.id.colorPicker);
        mColorPicker.setOnColorSelectedListener(mColorListener);
        mColorPicker.setShowOldCenterColor(true);

        showAndHideLayout(LAYOUT_SWITCH);
    }


    public void showAndHideLayout(int index) {
        for(int i=0; i<CONTROL_COUNT; i++) {
            if(mControlLayout[i] == null)
                continue;
            if(i == index)
                mControlLayout[i].setVisibility(View.VISIBLE);
            else
                mControlLayout[i].setVisibility(View.GONE);
            mLayoutSelected = index;
        }
    }



    private ColorPicker.OnColorSelectedListener mColorListener = new ColorPicker.OnColorSelectedListener() {
        @Override
        public void onColorSelected(int color) {
            mColor = color;
            mColorPicker.setOldCenterColor(color);
            mColorAlpha = Color.alpha(color);
            mColorRed = Color.red(color);
            mColorGreen = Color.green(color);
            mColorBlue = Color.blue(color);

            mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_SEND_CONTROL,
                    ControlType.TYPE_CONTROL_COLOR,     // Control type
                    color,      // value
                    Integer.toString(0),   // control index
                    null, null);
            Log.d("VController", "Red = "+mColorRed+", Green = "+mColorGreen+", Blue = "+mColorBlue);
        }
    };

    private Switch.OnCheckedChangeListener mSwitchListener = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton cb, boolean isChecking) {
            ViewTag tag = (ViewTag)cb.getTag();
            if(tag != null) {
                int i = tag.index;
                if(i > -1 && i < SWITCH_COUNT) {
                    mSwitchStatus[i] = isChecking;
                    mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_SEND_CONTROL,
                            ControlType.TYPE_CONTROL_SWITCH,     // Control type
                            (isChecking ? 1 : 0),      // value
                            Integer.toString(i),   // control index
                            null, null);
                }
            }
        }
    };

    private static long SENDING_INTERVAL = 500;
    private static long mLastProgressSent = 0;
    private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            SENDING_INTERVAL = Settings.getInstance(mContext).getSendingInterval();
            long current = System.currentTimeMillis();
            if(current - mLastProgressSent < SENDING_INTERVAL)
                return;
            ViewTag tag = (ViewTag)seekBar.getTag();
            if(tag != null) {
                int i = tag.index;
                if(i > -1 && i < SLIDE_COUNT) {
                    mSlideStatus[i] = progress;
                    mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_SEND_CONTROL,
                            ControlType.TYPE_CONTROL_SLIDE,     // Control type
                            progress,      // value
                            Integer.toString(i),   // control index
                            null, null);
                    mLastProgressSent = current;
                    Log.d("VController", "Slide progress = "+progress);
                }
            }
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    View.OnClickListener mKeypadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button button = (Button)v;
            Log.d("VController", "Key = " + button.getText().toString());
            mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_SEND_CONTROL,
                    ControlType.TYPE_CONTROL_KEYPAD,     // Control type
                    0,      // value(not available in this case)
                    Integer.toString(0),   // control index
                    button.getText().toString(),    // string value
                    null);
        }
    };

    View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.buttonSubmit:
                    mInputText = mEditArray[0].getText().toString();
                    mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_SEND_CONTROL,
                            ControlType.TYPE_CONTROL_TEXT,     // Control type
                            0,      // value(not available in this case)
                            Integer.toString(0),   // control index
                            mEditArray[0].getText().toString(),    // string value
                            null);
                    break;
            }
        }
    };

    private class ViewTag {
        public int type;    // View type
        public int index;
        public int code;
        public String message;
        public int color;

        public ViewTag(int t, int i, String m, int code, int color) {
            this.type = t;
            this.index = i;
            this.code = code;
            this.message = m;
            this.color = color;
        }
    }
}
