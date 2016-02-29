package com.hardcopy.vcontroller;

/**
 * Created by P16434 on 2016-02-23.
 */
public interface IMqttManagerListener {
    public static final int CALLBACK_MQTT_NULL = 0;
    public static final int CALLBACK_MQTT_ADD_CONNECTION = 1;
    public static final int CALLBACK_MQTT_CONNECTION_DELETED = 2;
    public static final int CALLBACK_MQTT_CONNECTION_LOST = 3;
    public static final int CALLBACK_MQTT_MESSAGE_ARRIVED = 4;
    public static final int CALLBACK_MQTT_MESSAGE_DELIVERED = 5;
    public static final int CALLBACK_MQTT_CONNECTED = 6;
    public static final int CALLBACK_MQTT_DISCONNECTED = 7;
    public static final int CALLBACK_MQTT_PUBLISHED = 8;
    public static final int CALLBACK_MQTT_SUBSCRIBED = 9;
    public static final int CALLBACK_MQTT_PROPERTY_CHANGED = 11;


    public void OnMqttManagerCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4);
}
