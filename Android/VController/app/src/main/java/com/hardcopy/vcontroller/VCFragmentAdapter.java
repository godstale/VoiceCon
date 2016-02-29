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

package com.hardcopy.vcontroller;

import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hardcopy.vcontroller.fragments.AddBrokerFragment;
import com.hardcopy.vcontroller.fragments.ControllerFragment;
import com.hardcopy.vcontroller.fragments.HistoryFragment;
import com.hardcopy.vcontroller.fragments.IFragmentListener;
import com.hardcopy.vcontroller.fragments.LogboxFragment;
import com.hardcopy.vcontroller.fragments.BrokerListFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class VCFragmentAdapter extends FragmentPagerAdapter {

    public static final String TAG = "VCFragmentAdapter";
    
    // Total count
    public static final int FRAGMENT_COUNT = 4;
    
    // Fragment position
    //public static final int FRAGMENT_POS_LOG_BOX = 0;
    public static final int FRAGMENT_POS_CONTROLLER = 0;
    public static final int FRAGMENT_POS_HISTORY = 1;
    public static final int FRAGMENT_POS_BROKER_LIST = 2;
    public static final int FRAGMENT_POS_ADD_BROKER = 3;

    public static final String ARG_SECTION_NUMBER = "section_number";
    
    // System
    private Context mContext = null;
    private IFragmentListener mFragmentListener = null;
    
    //private Fragment mLogboxFragment = null;
    private Fragment mHistoryFragment = null;
    private Fragment mBrokerListFragment = null;
    private Fragment mAddBrokerFragment = null;
    private Fragment mControllerFragment = null;
    
    public VCFragmentAdapter(FragmentManager fm, Context c, IFragmentListener l) {
        super(fm);
        mContext = c;
        mFragmentListener = l;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        Fragment fragment = null;
        boolean needToSetArguments = false;
        
//        if(position == FRAGMENT_POS_LOG_BOX) {
//            if(mLogboxFragment == null) {
//                mLogboxFragment = new LogboxFragment(mContext, mFragmentListener);
//                needToSetArguments = true;
//            }
//            fragment = mLogboxFragment;
//        } else
        if(position == FRAGMENT_POS_HISTORY) {
            if(mHistoryFragment == null) {
                mHistoryFragment = new HistoryFragment(mContext, mFragmentListener, null);
                needToSetArguments = true;
            }
            fragment = mHistoryFragment;
        }
        else if(position == FRAGMENT_POS_BROKER_LIST) {
            if(mBrokerListFragment == null) {
                mBrokerListFragment = new BrokerListFragment(mContext, mFragmentListener);
                needToSetArguments = true;
            }
            fragment = mBrokerListFragment;
        }
        else if(position == FRAGMENT_POS_ADD_BROKER) {
            if(mAddBrokerFragment == null) {
                mAddBrokerFragment = new AddBrokerFragment(mContext, mFragmentListener);
                needToSetArguments = true;
            }
            fragment = mAddBrokerFragment;
        }
        else if(position == FRAGMENT_POS_CONTROLLER) {
            if(mControllerFragment == null) {
                mControllerFragment = new ControllerFragment(mContext, mFragmentListener);
                needToSetArguments = true;
            }
            fragment = mControllerFragment;
        }
        else {
            fragment = null;
        }
        
        // TODO: If you have something to notify to the fragment.
        if(needToSetArguments) {
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
        }
        
        return fragment;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
//        case FRAGMENT_POS_LOG_BOX:
//            return mContext.getString(R.string.nav_logbox);
        case FRAGMENT_POS_HISTORY:
            return mContext.getString(R.string.nav_history);
        case FRAGMENT_POS_BROKER_LIST:
            return mContext.getString(R.string.nav_broker_list);
        case FRAGMENT_POS_ADD_BROKER:
            return mContext.getString(R.string.nav_add_broker);
        case FRAGMENT_POS_CONTROLLER:
            return mContext.getString(R.string.nav_controller);
        }
        return null;
    }
    
}

