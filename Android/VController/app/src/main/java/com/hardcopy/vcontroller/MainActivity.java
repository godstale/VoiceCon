package com.hardcopy.vcontroller;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hardcopy.vcontroller.SpeechRecognition.SpeechManager;
import com.hardcopy.vcontroller.contents.ControlType;
import com.hardcopy.vcontroller.contents.PacketBuilder;
import com.hardcopy.vcontroller.fragments.BrokerListFragment;
import com.hardcopy.vcontroller.fragments.ControllerFragment;
import com.hardcopy.vcontroller.fragments.HistoryFragment;
import com.hardcopy.vcontroller.fragments.IDialogListener;
import com.hardcopy.vcontroller.fragments.IFragmentListener;
import com.hardcopy.vcontroller.mqtt.Connection;
import com.hardcopy.vcontroller.mqtt.Connections;
import com.hardcopy.vcontroller.utils.Constants;
import com.hardcopy.vcontroller.utils.Logs;
import com.hardcopy.vcontroller.utils.RecycleUtils;
import com.hardcopy.vcontroller.utils.Settings;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IFragmentListener {

    private static final String TAG = "MainActivity";
    private Context mContext;

    // UI stuff
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private FloatingActionButton mFab;
    private VCFragmentAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ImageView mImageStatus;
    private TextView mTextStatus;
    private MainActivityFooter mFooter;

    // Tools
    private SpeechManager mSpeechHandler;
    private MqttManager mMqttManager;
    private int mBuildType = PacketBuilder.BUILD_TYPE_SIMPLE;



    /************************************************************************
     *
     * Overrided methods
     *
     ***********************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mSpeechHandler = SpeechManager.getInstance(this, mSpeechListener,
                Settings.getInstance(mContext).getLanguage());

        setContentView(R.layout.activity_main);

        // Set toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new VCFragmentAdapter(getSupportFragmentManager(), mContext, this);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // initialize & start speech recognition
                if(mSpeechHandler == null) return;
                startVoiceRecognition();
                // Show speech recognition status
                Snackbar.make(view, R.string.speech_listening, Snackbar.LENGTH_LONG)
                        .setAction(R.string.speech_stop, mSnackbarOnClickListener).show();
            }
        });

        // Set drawer fragment
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mImageStatus = (ImageView) findViewById(R.id.status_image);
        mTextStatus = (TextView) findViewById(R.id.status_text);
        mFooter = new MainActivityFooter(mContext, mImageStatus, mTextStatus);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        initialize();
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
        //mSpeechHandler.init();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(mMqttManager == null) {
            mMqttManager = MqttManager.getInstance(mContext, mMqttManagerListener);
        }
        mFooter.setStatus();
        if(Settings.getInstance(mContext).getShowFloating())
            mFab.show();
        else
            mFab.hide();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopVoiceRecognition();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finalizeActivity();
    }

    @Override
    public void onLowMemory (){
        super.onLowMemory();
        // onDestroy is not always called when applications are finished by Android system.
        finalizeActivity();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //mMenuClearHistory = menu.findItem(R.id.action_clearhistory);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_publish) {
            mViewPager.setCurrentItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY, true);
            HistoryFragment history = (HistoryFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY);
            if(history != null) {
                history.setInputMode(HistoryFragment.INPUT_PUBLISH);
                return true;
            }
        } else if(id == R.id.action_subscribe) {
            mViewPager.setCurrentItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY, true);
            HistoryFragment history = (HistoryFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY);
            if(history != null) {
                history.setInputMode(HistoryFragment.INPUT_SUBSCRIBE);
                return true;
            }
        } else if(id == R.id.action_clearhistory) {
            HistoryFragment history = (HistoryFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY);
            if(history != null) {
                history.initHistory();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            mViewPager.setCurrentItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY, true);
        } else if (id == R.id.nav_broker_list) {
            mViewPager.setCurrentItem(VCFragmentAdapter.FRAGMENT_POS_BROKER_LIST, true);
        } else if (id == R.id.nav_add_broker) {
            mViewPager.setCurrentItem(VCFragmentAdapter.FRAGMENT_POS_ADD_BROKER, true);
        } else if (id == R.id.nav_controller) {
            mViewPager.setCurrentItem(VCFragmentAdapter.FRAGMENT_POS_CONTROLLER, true);
        } else if (id == R.id.nav_settings) {
            showSettingsDialog();
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnFragmentCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
        switch(msgType) {
            case IFragmentListener.CALLBACK_REQUEST_CONNECTION_SELECTED:
                break;
            case IFragmentListener.CALLBACK_REQUEST_START_VOICE:
                startVoiceRecognition();
                break;
            case IFragmentListener.CALLBACK_REQUEST_SEND_CONTROL:
                Bundle bundle = getMqttPublishInfo();
                if(bundle != null) {
                    // prepare MQTT params
                    String topic = bundle.getString(Constants.topic);
                    if(topic == null || topic.length() < 1)
                        return;
                    int qos = bundle.getInt(Constants.qos, Constants.defaultQos);
                    boolean retained = bundle.getBoolean(Constants.retained, Constants.defaultRetained);

                    // build packet to send
                    int controlType = arg0;
                    PacketBuilder pb = PacketBuilder.newInstance();
                    pb.setControlType(controlType);   // Set control type (it can be TYPE_NONE)

                    if(controlType == ControlType.TYPE_CONTROL_SWITCH) {
                        pb.setValue(Integer.parseInt(arg2), (arg1==0 ? false : true));   // use single value.
                    } else if(controlType == ControlType.TYPE_CONTROL_SLIDE) {
                        pb.setValue(Integer.parseInt(arg2), arg1);   // use single value.
                    } else if(controlType == ControlType.TYPE_CONTROL_KEYPAD) {
                        pb.setValue(Integer.parseInt(arg2), arg3);   // use single value.
                    } else if(controlType == ControlType.TYPE_CONTROL_TEXT) {
                        pb.setValue(Integer.parseInt(arg2), arg3);   // use single value.
                    } else if(controlType == ControlType.TYPE_CONTROL_COLOR) {
                        int[] colors = new int[4];
                        colors[0] = Color.red(arg1); colors[1] = Color.green(arg1);
                        colors[2] = Color.blue(arg1); colors[3] = Color.alpha(arg1);
                        pb.setValue(colors);   // use multiple values.
                    }
                    mBuildType = Settings.getInstance(mContext).getResultType();
                    pb.build(mBuildType);           // build
                    String result = pb.toString();   // Get string result

                    int count = mMqttManager.publishToAll(topic, result, qos, retained);
                    String resultString = mContext.getString(R.string.voice_pub_result, count);
                    setPublishResult(resultString);
                }
                break;

            case IFragmentListener.CALLBACK_REQUEST_CONNECTION_CONNECT:
                if(arg4 == null || !(arg4 instanceof Connection)) {
                    return;
                } else {
                    Connection connection = (Connection)arg4;
                    if(mMqttManager != null) {
                        //mMqttManager.disconnect(connection.handle());
                        mMqttManager.reconnect(connection.handle());
                    }
                }
                break;
            case IFragmentListener.CALLBACK_REQUEST_CONNECTION_DISCONNECT:
                if(arg4 == null || !(arg4 instanceof Connection)) {
                    return;
                } else {
                    Connection connection = (Connection)arg4;
                    if(mMqttManager != null) {
                        mMqttManager.disconnect(connection.handle());
                    }
                }
                break;
            case IFragmentListener.CALLBACK_REQUEST_CONNECTION_DELETE:
                if(arg4 == null || !(arg4 instanceof Connection)) {
                    return;
                } else {
                    Connection connection = (Connection)arg4;
                    if(mMqttManager != null)
                        mMqttManager.deleteConnection(connection.handle());
                }
                break;
            case IFragmentListener.CALLBACK_REQUEST_ADD_CONNECTION:
                if(arg4 == null || !(arg4 instanceof Bundle)) {
                    return;
                } else {
                    Bundle data = (Bundle)arg4;
                    boolean isStarted = mMqttManager.addConnection(data);
                    if(isStarted) {
                        // mMqttManagerListener will receive result event instead
                        // No need to do the UI job here.
                    }
                }
                break;
            case IFragmentListener.CALLBACK_REQUEST_PUBLISH:
                if(arg4 == null || !(arg4 instanceof Bundle)) {
                    return;
                } else {
                    Bundle data = (Bundle)arg4;
                    String topic = data.getString(Constants.topic);
                    if(topic == null || topic.length() < 1)
                        return;
                    String message = data.getString(Constants.message);
                    int qos = data.getInt(Constants.qos, Constants.defaultQos);
                    boolean retained = data.getBoolean(Constants.retained, Constants.defaultRetained);
                    int count = mMqttManager.publishToAll(topic, message, qos, retained);
                    if(count > 0) {
                    }
                }
                break;
            case IFragmentListener.CALLBACK_REQUEST_SUBSCRIBE:
                if(arg4 == null || !(arg4 instanceof Bundle)) {
                    return;
                } else {
                    Bundle data = (Bundle)arg4;
                    String topic = data.getString(Constants.topic);
                    if(topic == null || topic.length() < 1)
                        return;
                    int qos = data.getInt(Constants.qos, Constants.defaultQos);
                    boolean retained = data.getBoolean(Constants.retained, Constants.defaultRetained);
                    int count = mMqttManager.subscribeFromAll(topic, qos);
                    if(count > 0) {
                    }
                }
                break;
            default:
                break;
        }
    }

    /************************************************************************
     *
     * Private methods
     *
     ***********************************************************************/

    private void initialize() {
        Logs.d(TAG, "# Activity - initialize()");
        if(mMqttManager == null) {
            mMqttManager = MqttManager.getInstance(mContext, mMqttManagerListener);
        }
    }

    private void finalizeActivity() {
        Logs.d(TAG, "# Activity - finalizeActivity()");

        stopVoiceRecognition();
        // TODO: background connection
        if(mMqttManager != null) {
            mMqttManager.disconnectAll();
            mMqttManager = null;
        }
        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();
    }

    private void addMessageToHistory(String msg) {
        HistoryFragment.appendHistoryToCache(msg);
        HistoryFragment bl3 = (HistoryFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY);
        if(bl3 != null)
            bl3.appendHistory(msg);
    }

    private void addCommandToController(String[] cmd) {
        ControllerFragment cf = (ControllerFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_CONTROLLER);
        if(cf != null)
            cf.setVoiceCommand(cmd);
    }

    private Bundle getMqttPublishInfo() {
        Bundle bundle = null;
        ControllerFragment cf = (ControllerFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_CONTROLLER);
        if(cf != null)
            bundle = cf.getMqttPublishInfo();
        return bundle;
    }

    private void setVoiceStatus(boolean isWorking) {
        ControllerFragment cf = (ControllerFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_CONTROLLER);
        if(cf != null)
            cf.setVoiceStatus(isWorking);
        if(isWorking) {
            mFab.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.icon_microphone3));
        } else {
            mFab.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.icon_microphone1));
        }
    }

    private void startVoiceRecognition() {
        if(mSpeechHandler.isWorking())
            mSpeechHandler.stop();
        mSpeechHandler.init();
        mSpeechHandler.start();
        setVoiceStatus(true);
    }

    private void stopVoiceRecognition() {
        mSpeechHandler.stop();
        setVoiceStatus(false);
    }

    private void setPublishResult(String result) {
        ControllerFragment cf = (ControllerFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_CONTROLLER);
        if(cf != null)
            cf.setPublishResult(result);
    }

    private Dialog showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(mContext);
        dialog.setDialogParams(mDialogListener, mContext.getString(R.string.menu_settings), null);
        dialog.show();

        return dialog;
    }
    
    

    /************************************************************************
     *
     * Listener, Callback
     *
     ***********************************************************************/

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case VCFragmentAdapter.FRAGMENT_POS_HISTORY:
                    break;
                case VCFragmentAdapter.FRAGMENT_POS_BROKER_LIST:
                case VCFragmentAdapter.FRAGMENT_POS_ADD_BROKER:
                case VCFragmentAdapter.FRAGMENT_POS_CONTROLLER:
                    break;
            }
            invalidateOptionsMenu();
        }
        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    private View.OnClickListener mSnackbarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopVoiceRecognition();
        }
    };

    private IDialogListener mDialogListener = new IDialogListener() {
        @Override
        public void OnDialogCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
            if(msgType == IDialogListener.CALLBACK_SETTINGS_FLOATING_UI) {
                if(arg0 == 0)
                    mFab.hide();
                else
                    mFab.show();
            } else if(msgType == IDialogListener.CALLBACK_SETTINGS_SET_LANGUAGE) {
                if(mSpeechHandler.isWorking())
                    mSpeechHandler.stop();
                mSpeechHandler = SpeechManager.resetInstance(mContext, mSpeechListener,
                        Settings.getInstance(mContext).getLanguage());
            }
        }
    };

    private RecognitionListener mSpeechListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            setVoiceStatus(true);
        }
        @Override public void onBeginningOfSpeech() {}
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override
        public void onEndOfSpeech() {
            stopVoiceRecognition();
        }
        @Override
        public void onError(int error) {
            stopVoiceRecognition();
        }
        @Override
        public void onResults(Bundle results) {
            // Convert result to string
            String key = android.speech.SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);

            if(mResult == null || mResult.size() < 1)
                return;
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);

            addCommandToController(rs);
            Bundle bundle = getMqttPublishInfo();
            if(bundle != null) {
                String topic = bundle.getString(Constants.topic);
                if(topic == null || topic.length() < 1)
                    return;

                // build packet to send
                PacketBuilder pb = PacketBuilder.newInstance();
                pb.setControlType(ControlType.TYPE_CONTROL_VOICE);   // Set control type (it can be TYPE_NONE)
                pb.setValue(rs);
                mBuildType = Settings.getInstance(mContext).getResultType();
                pb.build(mBuildType);           // build
                String result = pb.toString();   // Get string result

                int qos = bundle.getInt(Constants.qos, Constants.defaultQos);
                boolean retained = bundle.getBoolean(Constants.retained, Constants.defaultRetained);
                int count = mMqttManager.publishToAll(topic, result, qos, retained);
                String resultString = mContext.getString(R.string.voice_pub_result, count);
                setPublishResult(resultString);
            }
        }
        @Override public void onPartialResults(Bundle partialResults) {}
        @Override public void onEvent(int eventType, Bundle params) {}
    };

    private IMqttManagerListener mMqttManagerListener = new IMqttManagerListener() {
        @Override
        public void OnMqttManagerCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
            switch(msgType) {
            case IMqttManagerListener.CALLBACK_MQTT_ADD_CONNECTION:
                if(arg4 == null) {
                    return;
                } else {
                    Connection c = (Connection) arg4;
                    // Update history
                    String msg = "[+] " + getResources().getString(R.string.newConnectionAdded)
                            + " : " + c.getId() + "\n";
                    addMessageToHistory(msg);
                    // Add to broker list
                    BrokerListFragment bl = (BrokerListFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_BROKER_LIST);
                    if(bl != null) {
                        bl.addItem(c);
                        bl.notifyDataSetChanged();
                    }
                    // Move to history fragment
                    mViewPager.setCurrentItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY, true);
                    // Set handle on history fragment
                    HistoryFragment hf = (HistoryFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_HISTORY);
                    if(hf != null)
                        hf.setHandle(c.handle());
                }
                mFooter.setStatus();
                break;

            case IMqttManagerListener.CALLBACK_MQTT_CONNECTION_DELETED:
                if(arg4 == null) {
                    return;
                } else {
                    Connection c = (Connection) arg4;
                    // Update history
                    String msg = "[-] " + getResources().getString(R.string.connDeleted)
                            + " : " + c.getId() + "\n";
                    addMessageToHistory(msg);
                    // Add to broker list
                    BrokerListFragment bl = (BrokerListFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_BROKER_LIST);
                    if(bl != null) {
                        bl.deleteItem(c.handle());
                        bl.notifyDataSetChanged();
                    }
                }
                mFooter.setStatus();
                break;

            case IMqttManagerListener.CALLBACK_MQTT_DISCONNECTED: {
                if(arg4 == null) {
                    return;
                } else {
                    Connection c = Connections.getInstance(mContext).getConnection((String)arg2);
                    // Update history
                    String msgs = "[!] " + getResources().getString(R.string.disconnectedFrom)
                            + " " + c.getId() + "\n";
                    addMessageToHistory(msgs);
                    // Notify to broker list
                    BrokerListFragment bl = (BrokerListFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_BROKER_LIST);
                    if (bl != null) {
                        bl.notifyDataSetChanged();
                    }
                }
                mFooter.setStatus();
                break;
            }
            case IMqttManagerListener.CALLBACK_MQTT_CONNECTED: {
                if(arg2 != null) {
                    return;
                } else {
                    Connection c = Connections.getInstance(mContext).getConnection((String)arg2);
                    // Update history
                    String msgs = "[+] " + getResources().getString(R.string.connectedto)
                            + c.getId() + "\n";
                    addMessageToHistory(msgs);
                    // Notify to broker list
                    BrokerListFragment bl = (BrokerListFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_BROKER_LIST);
                    if (bl != null) {
                        bl.notifyDataSetChanged();
                    }
                }
                mFooter.setStatus();
                break;
            }
            case IMqttManagerListener.CALLBACK_MQTT_SUBSCRIBED:
                if(arg2 != null && arg3 != null) {
                    String msg = (String) arg3;
                    // Update history
                    String msgs = "[*] " + msg + "\n";
                    addMessageToHistory(msgs);
                    mFooter.setStatus();
                }
                mFooter.setStatus();
                break;
            case IMqttManagerListener.CALLBACK_MQTT_PUBLISHED:
                if(arg2 != null && arg3 != null) {
                    String msg = (String) arg3;
                    // Update history
                    String msgs = "--> " + msg + "\n";
                    addMessageToHistory(msgs);
                    mFooter.setStatus();
                }
                mFooter.setStatus();
                break;
            case IMqttManagerListener.CALLBACK_MQTT_CONNECTION_LOST:
                if(arg4 != null && arg3 != null) {
                    String message = (String) arg3;
                    //Connection c = (Connection) arg4;
                    // Update history
                    String msg = "[!] " + message + "\n";
                    addMessageToHistory(msg);
                    // Notify to broker list
                    BrokerListFragment bl = (BrokerListFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_BROKER_LIST);
                    if(bl != null) {
                        bl.notifyDataSetChanged();
                    }
                }
                mFooter.setStatus();
                break;

            case IMqttManagerListener.CALLBACK_MQTT_MESSAGE_ARRIVED:
                if(arg3 != null && arg4 == null)
                    return;
                else {
                    Connection c = (Connection) arg4;
                    String message = (String) arg3;
                    // Update history
                    String msg = "<-- " + c.getId() + " "
                            + getResources().getString(R.string.messageRecieved2) + message + "\n";
                    addMessageToHistory(msg);
                }
                mFooter.setStatus();
                break;

            case IMqttManagerListener.CALLBACK_MQTT_MESSAGE_DELIVERED:
                /* I think this is same with [CALLBACK_MQTT_PUBLISHED]*/
                /*if(arg4 == null) {
                    return;
                } else {
                    IMqttDeliveryToken c = (IMqttDeliveryToken) arg4;
                    MqttMessage tmp = null;
                    try {
                        tmp = c.getMessage();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    if(tmp == null)
                        return;
                    // Update history
                    String msg = "--> " + getResources().getString(R.string.messageDelivered2)
                            + " : " + tmp.toString() + "\n";
                    addMessageToHistory(msg);
                }*/
                mFooter.setStatus();
                break;

            case IMqttManagerListener.CALLBACK_MQTT_PROPERTY_CHANGED:
                // Use connection status message only
                if(arg2 == null)
                    return;
                String msg = (String)arg2;
                // Update history & broker list
                addMessageToHistory(msg + "\n");
                BrokerListFragment bl = (BrokerListFragment) mSectionsPagerAdapter.getItem(VCFragmentAdapter.FRAGMENT_POS_BROKER_LIST);
                if (bl != null) {
                    bl.notifyDataSetChanged();
                }
                // Refresh footer
                mFooter.setStatus();
                break;
            }
        }
    };

}


