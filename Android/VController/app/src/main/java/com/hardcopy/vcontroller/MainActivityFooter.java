package com.hardcopy.vcontroller;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.hardcopy.vcontroller.mqtt.Connection;
import com.hardcopy.vcontroller.mqtt.Connections;

import java.util.Map;

/**
 * Created by P16434 on 2016-02-26.
 */
public class MainActivityFooter {

    public static final int STATUS_NONE = 0;
    public static final int STATUS_DISCONNECTED = 0;
    public static final int STATUS_CONNECTED = 0;

    private Context mContext;
    private ImageView mImageView;
    private TextView mTextView;


    public MainActivityFooter(Context c, ImageView iv, TextView tv) {
        mContext = c;
        mImageView = iv;
        mTextView = tv;
    }

    public void setStatus() {
        int count = 0;
        boolean isConnected = false;
        StringBuilder servers = new StringBuilder();
        Map<String, Connection> connections = Connections.getInstance(mContext).getConnections();
        for(Connection c : connections.values()) {
            if(c.isConnected()) {
                isConnected = true;
                if(count > 0) {
                    servers.append(", ");
                } else {
                    servers.append(" ");
                }
                servers.append(c.getId());
                count++;
            }
        }
        if(count < 1) {
            servers.append(mContext.getString(R.string.disconnected));
        } else {
            servers.insert(0, mContext.getString(R.string.connectedto));
        }
        if(mImageView != null) {
            if(isConnected) {
                mImageView.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.presence_online));
            } else {
                mImageView.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.presence_busy));
            }
        }
        if(mTextView != null) {
            mTextView.setText(servers);
        }
    }

    public void setStatusConnected() {
        setStatus();
    }

    public void setStatusDisconnected() {
        if(mImageView != null) {
            mImageView.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.presence_busy));
        }
    }
}
