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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hardcopy.vcontroller.R;
import com.hardcopy.vcontroller.mqtt.Connection;
import com.hardcopy.vcontroller.mqtt.Connections;

/**
 * This fragment shows messages to be sent to watch.
 */
@SuppressLint("ValidFragment")
public class BrokerListFragment extends Fragment implements IAdapterListener {

    private static final String TAG = "BrokerListFragment";
    
    private Context mContext = null;
    private IFragmentListener mFragmentListener;
    
    private ListView mBrokerList = null;
    private BrokerListAdapter mBrokerListAdapter = null;



    public BrokerListFragment(Context c, IFragmentListener l) {
        mContext = c;
        mFragmentListener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_broker_list, container, false);

        mBrokerList = (ListView) rootView.findViewById(R.id.list_broker);
        if(mBrokerListAdapter == null)
            mBrokerListAdapter = new BrokerListAdapter(mContext, R.layout.list_broker_item, null);
        mBrokerListAdapter.setAdapterListener(this);
        mBrokerList.setAdapter(mBrokerListAdapter);

        Collection<Connection> collection = Connections.getInstance(mContext).getConnections().values();
        ArrayList<Connection> clist = new ArrayList<Connection>(collection);
        mBrokerListAdapter.deleteItemAll();
        mBrokerListAdapter.addItemAll(clist);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mBrokerListAdapter != null)
            mBrokerListAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnAdapterCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
        switch(msgType) {
        case IAdapterListener.CALLBACK_CONNECTION_CONNECT:
            if(arg4 != null && mFragmentListener != null)
                mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_CONNECTION_CONNECT, 0, 0, null, null, arg4);
            break;
        case IAdapterListener.CALLBACK_CONNECTION_DISCONNECT:
            if(arg4 != null && mFragmentListener != null)
                mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_CONNECTION_DISCONNECT, 0, 0, null, null, arg4);
            break;
        case IAdapterListener.CALLBACK_CONNECTION_DELETE:
            if(arg4 != null && mFragmentListener != null)
                mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_CONNECTION_DELETE, 0, 0, null, null, arg4);
            break;
        case IAdapterListener.CALLBACK_ITEM_SELECTED:
            if(arg4 != null && mFragmentListener != null)
                mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_CONNECTION_SELECTED, 0, 0, null, null, arg4);
            break;
        }
    }
    
    public void addItem(Connection object) {
        if(mBrokerListAdapter == null) return;
        if(object != null) {
            mBrokerListAdapter.addItem(object);
        }
    }
    
    public void addItemAll(ArrayList<Connection> objList) {
        if(mBrokerListAdapter == null) return;
        if(objList != null) {
            mBrokerListAdapter.addItemAll(objList);
            mBrokerListAdapter.notifyDataSetChanged();
        }
    }
    
    public void deleteItem(String handle) {
        if(mBrokerListAdapter == null) return;
        mBrokerListAdapter.deleteItem(handle);
    }
    
    public void deleteItemAll() {
        if(mBrokerListAdapter == null) return;
        mBrokerListAdapter.deleteItemAll();
        mBrokerListAdapter.notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        if(mBrokerListAdapter == null) return;
        mBrokerListAdapter.notifyDataSetChanged();
    }
    
}
