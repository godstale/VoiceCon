package com.hardcopy.vcontroller.contents;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hardcopyworld.com on 2016-02-28.
 */
/**
 * Usage
 *
 * PacketBuilder pb = PacketBuilder.newInstance();
 * pb.setControlType(ControlType.TYPE_CONTROL_SLIDE);   // Set control type (it can be TYPE_NONE)
 * pb.setValue(string_array);       // Use String[]
 * pb.setValue(index, int_value);   // or use single value. (index >= 0)
 * pb.build(BUILD_TYPE_JSON);       // build in JSON style
 * String result = pb.toString();   // Get string result
 */
/**
 * Result string variations according to build type
 *
 * BUILD_TYPE_VALUE_ONLY ==>
 *     single value: 1
 *     multiple value: 11111 (if boolean)
 *                     121,242,4,256,11 (else)
 *
 * BUILD_TYPE_SIMPLE ==>
 *     single value: switch[3:1]
 *     multiple value: switch[1,0,1,1,0]
 *
 * BUILD_TYPE_JSON ==>
 *     {"control":{
 *         "type", "switch"
 *         "1": 1
 *         "2": 0
 *         "3": 1
 *         ...
 *     }}
 */

public class PacketBuilder {

    public static final int BUILD_TYPE_VALUE_ONLY = 0;  // just value only
    public static final int BUILD_TYPE_SIMPLE = 1;  // simple text type
    public static final int BUILD_TYPE_JSON = 2;

    private static final String TYPE = "type";
    private static final String DIVIDER = ",";
    private static final String EQUAL = ":";

    private static final int PARAM_NONE = 0;
    private static final int PARAM_BOOLEAN = 1;
    private static final int PARAM_INT = 2;
    private static final int PARAM_FLOAT = 3;
    private static final int PARAM_STRING = 4;

    private int mControlType = ControlType.TYPE_NONE;
    private int mParamType = PARAM_NONE;
    private int mBuildType = BUILD_TYPE_SIMPLE;

    private int mIndex = -1;    // -1 means all item
    private boolean mBoolean;
    private int mInteger;
    private float mFloat;
    private String mText;
    private boolean[] mBooleanArray;
    private int[] mIntegerArray;
    private float[] mFloatArray;
    private String[] mTextArray;
    private String mResult;
    private JSONObject mJsonObject;


    public PacketBuilder() {
    }

    public static PacketBuilder newInstance() {
        return new PacketBuilder();
    }


    private void parseBoolean(int btype) {
        if(mIndex < 0 && mBooleanArray == null)
            return;

        String json_string = null;
        StringBuilder sb = new StringBuilder();
        switch(btype) {
            case BUILD_TYPE_VALUE_ONLY:
                // use all item
                if(mIndex < 0) {
                    for(int i=0; i<mBooleanArray.length; i++) {
                        sb.append((mBooleanArray[i] ? 1 : 0));   // Set 1 if true, or 0.
                    }
                }
                // use selected item only
                else {
                    sb.append(mBoolean ? 1 : 0);   // Set 1 if true, or 0.
                }
                break;
            case BUILD_TYPE_SIMPLE:
                sb.append(ControlType.getName(mControlType)).append("[");
                // use all item
                if(mIndex < 0) {
                    for(int i=0; i<mBooleanArray.length; i++) {
                        if(i != 0)
                            sb.append(DIVIDER);
                        sb.append((mBooleanArray[i] ? 1 : 0));   // Set 1 if true, or 0.
                    }
                }
                // use selected item only
                else {
                    sb.append(mIndex).append(EQUAL);
                    sb.append(mBoolean ? 1 : 0);   // Set 1 if true, or 0.
                }
                sb.append("]");
                break;
            case BUILD_TYPE_JSON:
                JSONObject root_obj = new JSONObject();
                JSONObject item_obj = new JSONObject();
                try {
                    item_obj.put(TYPE, ControlType.getName(mControlType));
                    // use all item
                    if(mIndex < 0) {
                        for(int i=0; i<mBooleanArray.length; i++) {
                            item_obj.put(Integer.toString(i), mBooleanArray[i]);
                        }
                    }
                    // use selected item only
                    else {
                        item_obj.put(Integer.toString(mIndex), mBoolean);
                    }
                    root_obj.put(ControlType.getName(0), item_obj);
                    json_string = root_obj.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if(sb.length() < 1) {
            mResult = null;
        } else {
            mResult = sb.toString();
        }
        if(btype == BUILD_TYPE_JSON) {
            mResult = json_string;
        }
    }

    private void parseInteger(int btype) {
        if(mIndex < 0 && mIntegerArray == null)
            return;

        String json_string = null;
        StringBuilder sb = new StringBuilder();
        switch(btype) {
            case BUILD_TYPE_VALUE_ONLY:
                // use all item
                if(mIndex < 0) {
                    for(int i=0; i<mIntegerArray.length; i++) {
                        if(i != 0)
                            sb.append(DIVIDER);
                        sb.append(mIntegerArray[i]);
                    }
                }
                // use selected item only
                else {
                    sb.append(mInteger);
                }
                break;
            case BUILD_TYPE_SIMPLE:
                sb.append(ControlType.getName(mControlType)).append("[");
                // use all item
                if(mIndex < 0) {
                    for(int i=0; i<mIntegerArray.length; i++) {
                        if(i != 0)
                            sb.append(DIVIDER);
                        sb.append(mIntegerArray[i]);
                    }
                }
                // use selected item only
                else {
                    sb.append(mIndex).append(EQUAL);
                    sb.append(mInteger);
                }
                sb.append("]");
                break;
            case BUILD_TYPE_JSON:
                JSONObject root_obj = new JSONObject();
                JSONObject item_obj = new JSONObject();
                try {
                    item_obj.put(TYPE, ControlType.getName(mControlType));
                    // use all item
                    if(mIndex < 0) {
                        for(int i=0; i<mIntegerArray.length; i++) {
                            item_obj.put(Integer.toString(i), mIntegerArray[i]);
                        }
                    }
                    // use selected item only
                    else {
                        item_obj.put(Integer.toString(mIndex), mInteger);
                    }
                    root_obj.put(ControlType.getName(0), item_obj);
                    json_string = root_obj.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if(sb.length() < 1) {
            mResult = null;
        } else {
            mResult = sb.toString();
        }
        if(btype == BUILD_TYPE_JSON) {
            mResult = json_string;
        }
    }

    private void parseFloat(int btype) {
        if(mIndex < 0 && mFloatArray == null)
            return;

        String json_string = null;
        StringBuilder sb = new StringBuilder();
        switch(btype) {
            case BUILD_TYPE_VALUE_ONLY:
                // use all item
                if(mIndex < 0) {
                    for(int i=0; i<mFloatArray.length; i++) {
                        if(i != 0)
                            sb.append(DIVIDER);
                        sb.append(mFloatArray[i]);
                    }
                }
                // use selected item only
                else {
                    sb.append(mFloat);
                }
                break;
            case BUILD_TYPE_SIMPLE:
                sb.append(ControlType.getName(mControlType)).append("[");
                // use all item
                if(mIndex < 0) {
                    for(int i=0; i<mFloatArray.length; i++) {
                        if(i != 0)
                            sb.append(DIVIDER);
                        sb.append(mFloatArray[i]);
                    }
                }
                // use selected item only
                else {
                    sb.append(mIndex).append(EQUAL);
                    sb.append(mFloat);
                }
                sb.append("]");
                break;
            case BUILD_TYPE_JSON:
                JSONObject root_obj = new JSONObject();
                JSONObject item_obj = new JSONObject();
                try {
                    item_obj.put(TYPE, ControlType.getName(mControlType));
                    // use all item
                    if(mIndex < 0) {
                        for(int i=0; i<mFloatArray.length; i++) {
                            item_obj.put(Integer.toString(i), mFloatArray[i]);
                        }
                    }
                    // use selected item only
                    else {
                        item_obj.put(Integer.toString(mIndex), mFloat);
                    }
                    root_obj.put(ControlType.getName(0), item_obj);
                    json_string = root_obj.toString();
                    mJsonObject = root_obj;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if(sb.length() < 1) {
            mResult = null;
        } else {
            mResult = sb.toString();
        }
        if(btype == BUILD_TYPE_JSON) {
            mResult = json_string;
        }
    }

    private void parseText(int btype) {
        if(mIndex < 0 && mTextArray == null)
            return;

        String json_string = null;
        StringBuilder sb = new StringBuilder();
        switch(btype) {
            case BUILD_TYPE_VALUE_ONLY:
                // use all item
                if(mIndex < 0) {
                    for(int i=0; i<mTextArray.length; i++) {
                        if(i != 0)
                            sb.append(DIVIDER);
                        sb.append(mTextArray[i]);
                    }
                }
                // use selected item only
                else {
                    sb.append(mText);
                }
                break;
            case BUILD_TYPE_SIMPLE:
                sb.append(ControlType.getName(mControlType)).append("[");
                // use all item
                if(mIndex < 0) {
                    for(int i=0; i<mTextArray.length; i++) {
                        if(i != 0)
                            sb.append(DIVIDER);
                        sb.append(mTextArray[i]);
                    }
                }
                // use selected item only
                else {
                    sb.append(mIndex).append(EQUAL);
                    sb.append(mText);
                }
                sb.append("]");
                break;
            case BUILD_TYPE_JSON:
                JSONObject root_obj = new JSONObject();
                JSONObject item_obj = new JSONObject();
                try {
                    item_obj.put(TYPE, ControlType.getName(mControlType));
                    // use all item
                    if(mIndex < 0) {
                        for(int i=0; i<mTextArray.length; i++) {
                            item_obj.put(Integer.toString(i), mTextArray[i]);
                        }
                    }
                    // use selected item only
                    else {
                        item_obj.put(Integer.toString(mIndex), mText);
                    }
                    root_obj.put(ControlType.getName(0), item_obj);
                    json_string = root_obj.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        if(sb.length() < 1) {
            mResult = null;
        } else {
            mResult = sb.toString();
        }
        if(btype == BUILD_TYPE_JSON) {
            mResult = json_string;
        }
    }




    public PacketBuilder setControlType(int ctype) {
        mControlType = ctype;
        return this;
    }

    public PacketBuilder setValue(int index, boolean value) {
        mBoolean = value;
        mIndex = index;
        mParamType = PARAM_BOOLEAN;
        return this;
    }
    public PacketBuilder setValue(int index, int value) {
        mInteger = value;
        mIndex = index;
        mParamType = PARAM_INT;
        return this;
    }
    public PacketBuilder setValue(int index, float value) {
        mFloat = value;
        mIndex = index;
        mParamType = PARAM_FLOAT;
        return this;
    }
    public PacketBuilder setValue(int index, String value) {
        mText = value;
        mIndex = index;
        mParamType = PARAM_STRING;
        return this;
    }
    public PacketBuilder setValue(boolean[] value) {
        mBooleanArray = value;
        mParamType = PARAM_BOOLEAN;
        mIndex = -1;
        return this;
    }
    public PacketBuilder setValue(int[] value) {
        mIntegerArray = value;
        mParamType = PARAM_INT;
        mIndex = -1;
        return this;
    }
    public PacketBuilder setValue(float[] value) {
        mFloatArray = value;
        mParamType = PARAM_FLOAT;
        mIndex = -1;
        return this;
    }
    public PacketBuilder setValue(String[] value) {
        mTextArray = value;
        mParamType = PARAM_STRING;
        mIndex = -1;
        return this;
    }

    public PacketBuilder build(int btype) {
        if(mControlType < ControlType.TYPE_NONE || mControlType > ControlType.TYPE_CONTROL_MAX)
            return this;
        if(mParamType == PARAM_NONE)
            return this;

        mBuildType = BUILD_TYPE_JSON;

        switch(mParamType) {
            case PARAM_BOOLEAN:
                parseBoolean(btype);
                break;
            case PARAM_INT:
                parseInteger(btype);
                break;
            case PARAM_FLOAT:
                parseFloat(btype);
                break;
            case PARAM_STRING:
                parseText(btype);
                break;
        }
        return this;
    }

    public String toString() {
        return mResult;
    }

    public JSONObject getJsonObject() {
        return mJsonObject;
    }
}
