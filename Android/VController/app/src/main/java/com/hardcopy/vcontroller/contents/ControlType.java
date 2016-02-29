package com.hardcopy.vcontroller.contents;

/**
 * Created by Administrator on 2016-02-28.
 */
public class ControlType {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_CONTROL_VOICE = 1;
    public static final int TYPE_CONTROL_SWITCH = 2;
    public static final int TYPE_CONTROL_SLIDE = 3;
    public static final int TYPE_CONTROL_KEYPAD = 4;
    public static final int TYPE_CONTROL_TEXT = 5;
    public static final int TYPE_CONTROL_COLOR = 6;
    public static final int TYPE_CONTROL_MAX = 6;

    public static final String[] CONTROL_PREFIX = {"control", "voice", "switch", "slide", "keypad", "text", "color"};

    public static String getName(int index) {
        if(index < 0 || index > TYPE_CONTROL_MAX)
            return CONTROL_PREFIX[0];
        return CONTROL_PREFIX[index];
    }
}
