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

import com.hardcopy.vcontroller.R;
import com.hardcopy.vcontroller.mqtt.Connection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BrokerListAdapter extends ArrayAdapter<Connection> implements IDialogListener {

    public static final String TAG = "BrokerListAdapter";

    private Context mContext = null;
    private ArrayList<Connection> mObjectList = null;
    private IAdapterListener mAdapterListener = null;

    public BrokerListAdapter(Context c, int resId, ArrayList<Connection> itemList) {
        super(c, resId, itemList);
        mContext = c;
        if(itemList == null)
            mObjectList = new ArrayList<Connection>();
        else
            mObjectList = itemList;
    }

    public void setAdapterListener(IAdapterListener l) {
        mAdapterListener = l;
    }

    public void addItem(Connection co) {
        mObjectList.add(co);
    }

    public void addItemAll(ArrayList<Connection> itemList) {
        if(itemList == null)
            return;
        for(int i=0; i<itemList.size(); i++)
            addItem(itemList.get(i));
    }

    public void deleteItem(String handle) {
        for(int i = mObjectList.size() - 1; -1 < i; i--) {
            Connection co = mObjectList.get(i);
            if(co.handle().equals(handle)) {
                mObjectList.remove(i);
            }
        }
    }

    public void deleteItemAll() {
        mObjectList.clear();
    }

    @Override
    public int getCount() {
        return mObjectList.size();
    }
    @Override
    public Connection getItem(int position) {
        return mObjectList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View v = convertView;
        Connection co = getItem(position);

        if(v == null) {
            LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.list_broker_item, null);
            holder = new ViewHolder();

            holder.mItemContainer = (LinearLayout) v.findViewById(R.id.msg_item_container);
            holder.mItemContainer.setOnTouchListener(mListItemTouchListener);
            holder.mTextInfo = (TextView) v.findViewById(R.id.msg_info);
            holder.mTextOrigin = (TextView) v.findViewById(R.id.msg_origin);
            holder.mTextOrigin.setVisibility(View.GONE);
            holder.mTextConverted = (TextView) v.findViewById(R.id.msg_converted);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.mObject = co;

        if (co != null && holder != null) {
            if(co.isConnected())
                holder.mItemContainer.setBackgroundColor(mContext.getResources().getColor(R.color.lightblue1));
            else
                holder.mItemContainer.setBackgroundColor(mContext.getResources().getColor(R.color.graye));
            holder.mTextInfo.setText(co.toString());
            //holder.mTextOrigin.setText(co.getId());
            holder.mTextConverted.setText(co.getHostName() + " : " + co.getPort());
        }
        return v;
    }    // End of getView()

    @Override
    public void OnDialogCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
        switch(msgType) {
        case IDialogListener.CALLBACK_CONNECTION_CONNECT:
            if(arg4 != null) {
                if(mAdapterListener != null) {
                    mAdapterListener.OnAdapterCallback(IAdapterListener.CALLBACK_CONNECTION_CONNECT, 0, 0, null, null, arg4);
                }
            }
            break;
        case IDialogListener.CALLBACK_CONNECTION_DISCONNECT:
            if(arg4 != null) {
                if(mAdapterListener != null) {
                    mAdapterListener.OnAdapterCallback(IAdapterListener.CALLBACK_CONNECTION_DISCONNECT, 0, 0, null, null, arg4);
                }
            }
            break;
        case IDialogListener.CALLBACK_CONNECTION_DELETE:
            if(arg4 != null) {
                if(mAdapterListener != null) {
                    mAdapterListener.OnAdapterCallback(IAdapterListener.CALLBACK_CONNECTION_DELETE, 0, 0, null, null, arg4);
                }
            }
            break;
        case IDialogListener.CALLBACK_CLOSE:
            break;
        }
    }

    /**
     * Sometimes onClick listener misses event.
     * Uses touch listener instead.
     */
    private OnTouchListener mListItemTouchListener = new OnTouchListener() {
        private float startx = 0;
        private float starty = 0;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                startx = event.getX();
                starty = event.getY();
            }
            if(event.getAction()==MotionEvent.ACTION_UP){
                // if action-up occurred within 30px from start, process as click event.
                if( (startx - event.getX())*(startx - event.getX()) + (starty - event.getY())*(starty - event.getY()) < 900 ) {
                    processOnClickEvent(v);
                }
            }
            return true;
        }
    };    // End of new OnTouchListener

    private void processOnClickEvent(View v) {
        switch(v.getId())
        {
            case R.id.msg_item_container:
                if(v.getTag() == null)
                    break;
                Object co = ((ViewHolder)v.getTag()).mObject;
                if(co != null) {
                    BrokerListDialog dialog = new BrokerListDialog(mContext);
                    dialog.setDialogParams(this, null, co);
                    dialog.show();

                    if(mAdapterListener != null) {
                        mAdapterListener.OnAdapterCallback(IAdapterListener.CALLBACK_CONNECTION_SELECTED, 0, 0, null, null, co);
                    }
                }
                break;
        }    // End of switch()
    }

    public class ViewHolder {
        public LinearLayout mItemContainer = null;
        public TextView mTextInfo = null;
        public TextView mTextOrigin = null;
        public TextView mTextConverted = null;

        public Object mObject = null;
    }

}
