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

package com.hardcopy.vcontroller.utils;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Constants {
    // Preference
    public static final String PREFERENCE_NAME = "VController";
    public static final String PREFERENCE_KEY_BG_SERVICE = "BackgroundService";
    public static final String PREFERENCE_KEY_CMD_TOPIC = "CommandTopic";
    public static final String PREFERENCE_KEY_CMD_QOS = "CommandQoS";
    public static final String PREFERENCE_KEY_CMD_RETAINED = "CommandRetained";
    public static final String PREFERENCE_KEY_SET_SHOW_FLOATING = "SettingShowFloat";
    public static final String PREFERENCE_KEY_SET_INTERVAL = "SettingInterval";
    public static final String PREFERENCE_KEY_SET_RES_TYPE = "SettingResultType";
    public static final String PREFERENCE_KEY_SET_LANGUAGE = "SettingLanguage";

    public static final int MENU_MODE_LOGBOX = 1;
    public static final int MENU_MODE_BROKER_LIST = 2;
    public static final int MENU_MODE_ADD_BROKER = 3;
    public static final int MENU_MODE_SETTINGS = 4;

    /** Notification **/
    public static final String NOTI_TARGET_ACTIVITY = "com.hardcopy.vcontroller.MainActivity";


    /* Default settings **/
    public static final boolean defaultShowFloat = true;
    public static final int defaultInterval = 500;
    public static final int defaultResultType = 1;  // simple text type
    public static final String defaultLanguage = "ko-KR";


    /* MQTT Bundle Keys */

    /** Server Bundle Key **/
    public static final String server = "server";
    /** Port Bundle Key **/
    public static final String port = "port";
    /** ClientID Bundle Key **/
    public static final String clientId = "clientId";
    /** Topic Bundle Key **/
    public static final String topic = "topic";
    /** History Bundle Key **/
    public static final String history = "history";
    /** Message Bundle Key **/
    public static final String message = "message";
    /** Retained Flag Bundle Key **/
    public static final String retained = "retained";
    /** QOS Value Bundle Key **/
    public static final String qos = "qos";
    /** User name Bundle Key **/
    public static final String username = "username";
    /** Password Bundle Key **/
    public static final String password = "password";
    /** Keep Alive value Bundle Key **/
    public static final String keepalive = "keepalive";
    /** Timeout Bundle Key **/
    public static final String timeout = "timeout";
    /** SSL Enabled Flag Bundle Key **/
    public static final String ssl = "ssl";
    /** SSL Key File Bundle Key **/
    public static final String ssl_key = "ssl_key";
    /** Connections Bundle Key **/
    public static final String connections = "connections";
    /** Clean Session Flag Bundle Key **/
    public static final String cleanSession = "cleanSession";
    /** Action Bundle Key **/
    public static final String action = "action";

    /* MQTT Default values **/

    /** Default QOS value*/
    public static final int defaultQos = 0;
    /** Default timeout*/
    public static final int defaultTimeOut = 1000;
    /** Default keep alive value*/
    public static final int defaultKeepAlive = 10;
    /** Default SSL enabled flag*/
    public static final boolean defaultCleanSession = false;
    /** Default SSL enabled flag*/
    public static final boolean defaultSsl = false;
    /** Default message retained flag */
    public static final boolean defaultRetained = false;
    /** Default last will message*/
    public static final MqttMessage defaultLastWill = null;
    /** Default port*/
    public static final int defaultPort = 1883;

    /** Connect Request Code */
    public static final int connect = 0;
    /** Advanced Connect Request Code  **/
    public static final int advancedConnect = 1;
    /** Last will Request Code  **/
    public static final int lastWill = 2;
    /** Show History Request Code  **/
    public static final int showHistory = 3;

    /* Bundle Keys for MQTT Connection */

    /** Server Bundle Key **/
    public static final String BUNDLE_MQTT_SERVER = "server";
    /** Port Bundle Key **/
    public static final String BUNDLE_MQTT_PORT = "port";
    /** ClientID Bundle Key **/
    public static final String BUNDLE_MQTT_CLIENT_ID = "clientId";
    /** Topic Bundle Key **/
    public static final String BUNDLE_MQTT_TOPIC = "topic";
    /** History Bundle Key **/
    public static final String BUNDLE_MQTT_HISTORY = "history";
    /** Message Bundle Key **/
    public static final String BUNDLE_MQTT_MESSAGE = "message";
    /** Retained Flag Bundle Key **/
    public static final String BUNDLE_MQTT_RETAINED = "retained";
    /** QOS Value Bundle Key **/
    public static final String BUNDLE_MQTT_QOS = "qos";
    /** User name Bundle Key **/
    public static final String BUNDLE_MQTT_USER_NAME = "username";
    /** Password Bundle Key **/
    public static final String BUNDLE_MQTT_PASSWORD = "password";
    /** Keep Alive value Bundle Key **/
    public static final String BUNDLE_MQTT_KEEPALIVE = "keepalive";
    /** Timeout Bundle Key **/
    public static final String BUNDLE_MQTT_TIMEOUT = "timeout";
    /** SSL Enabled Flag Bundle Key **/
    public static final String BUNDLE_MQTT_SSL = "ssl";
    /** SSL Key File Bundle Key **/
    public static final String BUNDLE_MQTT_SSL_KEY = "ssl_key";
    /** Connections Bundle Key **/
    public static final String BUNDLE_MQTT_CONNECTIONS = "connections";
    /** Clean Session Flag Bundle Key **/
    public static final String BUNDLE_MQTT_CLEAN_SESSION = "cleanSession";
    /** Action Bundle Key **/
    public static final String BUNDLE_MQTT_ACTION = "action";

    /* Property names */

    /** Property name for the history field in {@link Connection} object for use with {@link java.beans.PropertyChangeEvent} **/
    public static final String PROPERTY_HISTORY = "history";
    /** Property name for the connection status field in {@link Connection} object for use with {@link java.beans.PropertyChangeEvent} **/
    public static final String PROPERTY_CONNECTION_STATUS = "connectionStatus";



    /** Space String Literal **/
    public static final String space = " ";
    /** Empty String for comparisons **/
    public static final String empty = new String();

}
