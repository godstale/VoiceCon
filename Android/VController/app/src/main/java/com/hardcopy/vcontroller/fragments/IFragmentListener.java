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

public interface IFragmentListener {
    public static final int CALLBACK_REQUEST_NULL = 0;
    public static final int CALLBACK_REQUEST_START_VOICE = 1;
    public static final int CALLBACK_REQUEST_SEND_CONTROL = 2;

    public static final int CALLBACK_REQUEST_CONNECTION_SELECTED = 100;
    public static final int CALLBACK_REQUEST_CONNECTION_CONNECT = 101;
    public static final int CALLBACK_REQUEST_CONNECTION_DISCONNECT = 102;
    public static final int CALLBACK_REQUEST_CONNECTION_DELETE = 103;

    public static final int CALLBACK_REQUEST_ADD_CONNECTION = 201;
    public static final int CALLBACK_REQUEST_PUBLISH = 211;
    public static final int CALLBACK_REQUEST_SUBSCRIBE = 212;

    public static final int CALLBACK_REQUEST_RUN_IN_BACKGROUND = 1000;


    public void OnFragmentCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4);
}
