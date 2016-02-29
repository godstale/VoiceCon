package com.hardcopy.vcontroller;

/**
 * Created by hardcopyworld.com on 2016-02-26.
 */
public interface IMqttActionCallback {
    public static final int ACTION_RESULT_CONNECTED = 0;
    public static final int ACTION_RESULT_DISCONNECTED = 1;
    public static final int ACTION_RESULT_SUBSCRIBED = 2;
    public static final int ACTION_RESULT_PUBLISHED = 3;
    public void onActionResult(int msg_type, int arg0, int arg1, String arg2, String arg3, Object arg4);
}
