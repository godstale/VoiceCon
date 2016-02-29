package com.hardcopy.vcontroller.fragments;

public interface IDialogListener {
	// MQTT action
	public static final int CALLBACK_CONNECTION_CONNECT = 1;
	public static final int CALLBACK_CONNECTION_DISCONNECT = 2;
	public static final int CALLBACK_CONNECTION_DELETE = 3;

	public static final int CALLBACK_SETTINGS_FLOATING_UI = 101;

	public static final int CALLBACK_CLOSE = 1000;
	
	public void OnDialogCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4);
}
