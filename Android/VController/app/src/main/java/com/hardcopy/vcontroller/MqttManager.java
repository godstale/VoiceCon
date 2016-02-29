package com.hardcopy.vcontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.hardcopy.vcontroller.mqtt.Connection;
import com.hardcopy.vcontroller.mqtt.Connection.ConnectionStatus;
import com.hardcopy.vcontroller.mqtt.Connections;
import com.hardcopy.vcontroller.mqtt.MqttTraceCallback;
import com.hardcopy.vcontroller.mqtt.Notify;
import com.hardcopy.vcontroller.utils.Constants;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

/**
 * Created by hardcopyworld.com
 */
public class MqttManager {

    private Context mContext;
    private static MqttManager mInstance;
    private static IMqttManagerListener mManagerListener;
    private ChangeListener mChangeListener = new ChangeListener();
    private MqttActionCallback mActionCallback;

    private Connection mSelectedConnection; // for internal use only
    private static boolean mPahoLogging = false;    // Paho logging flag


    public synchronized static MqttManager getInstance(Context c, IMqttManagerListener l) {
        if(mInstance == null) {
            mInstance = new MqttManager(c, l);
        }
        return mInstance;
    }


    /************************************************************************
     *
     * Private methods
     *
     ***********************************************************************/

    private MqttManager(Context c, IMqttManagerListener l) {
        mContext = c;
        mManagerListener = l;
        initialize();
    }

    private void initialize() {
        mActionCallback = new MqttActionCallback();
        // get all the available connections
        Map<String, Connection> connections = Connections.getInstance(mContext).getConnections();
        // register change listener
        for (Connection conn : connections.values()) {
            conn.registerChangeListener(mChangeListener);
        }
    }

    private void continueDeletion() {
        //user pressed continue disconnect client and delete
        if(mSelectedConnection != null) {
            // notify to activity
            if(mManagerListener != null)
                mManagerListener.OnMqttManagerCallback(
                        IMqttManagerListener.CALLBACK_MQTT_CONNECTION_DELETED,
                        0, 0,
                        null, null, mSelectedConnection);
            // remove instance
            Connections.getInstance(mContext).removeConnection(mSelectedConnection);
        }
    }



    /************************************************************************
     *
     * Public methods
     *
     ***********************************************************************/

    public int isConnected() {
        int count = 0;
        Map<String, Connection> connections = Connections.getInstance(mContext).getConnections();
        for(Connection c : connections.values()) {
            if(c.isConnected()) {
                count++;
            }
        }
        return count;
    }

    public void disconnectAll() {
        Map<String, Connection> connections = Connections.getInstance(mContext).getConnections();
        for(Connection c : connections.values()) {
            if(c.isConnected()) {
                disconnect(c.handle());
            }
        }
    }

    public boolean addConnection(Bundle data) {
        boolean result = false;
        if(data == null)
            return result;

        MqttConnectOptions conOpt = new MqttConnectOptions();
        /*
         * Mutal Auth connections could do something like this
         *
         * SSLContext context = SSLContext.getDefault();
         * context.init({new CustomX509KeyManager()},null,null); //where CustomX509KeyManager proxies calls to keychain api
         * SSLSocketFactory factory = context.getSSLSocketFactory();
         *
         * MqttConnectOptions options = new MqttConnectOptions();
         * options.setSocketFactory(factory);
         * client.connect(options);
         */

        // The basic client information
        String server = (String) data.get(Constants.BUNDLE_MQTT_SERVER);
        String clientId = (String) data.get(Constants.BUNDLE_MQTT_CLIENT_ID);
        int port = Integer.parseInt((String) data.get(Constants.BUNDLE_MQTT_PORT));
        boolean cleanSession = (Boolean) data.get(Constants.BUNDLE_MQTT_CLEAN_SESSION);

        boolean ssl = (Boolean) data.get(Constants.BUNDLE_MQTT_SSL);
        String ssl_key = (String) data.get(Constants.BUNDLE_MQTT_SSL_KEY);
        String uri = null;
        if (ssl) {
            Log.e("SSLConnection", "Doing an SSL Connect");
            uri = "ssl://";
        }
        else {
            uri = "tcp://";
        }
        uri = uri + server + ":" + port;

        MqttAndroidClient client;
        client = Connections.getInstance(mContext).createClient(mContext, uri, clientId);

        if (ssl){
            try {
                if(ssl_key != null && !ssl_key.equalsIgnoreCase("")) {
                    FileInputStream key = new FileInputStream(ssl_key);
                    conOpt.setSocketFactory(client.getSSLSocketFactory(key, "mqtttest"));
                } else {
                    ssl = false;
                }
            } catch (MqttSecurityException e) {
                Log.e(this.getClass().getCanonicalName(), "MqttException Occured: ", e);
                ssl = false;
            } catch (FileNotFoundException e) {
                Log.e(this.getClass().getCanonicalName(), "MqttException Occured: SSL Key file not found", e);
                ssl = false;
            }
        }

        // create a client handle
        String clientHandle = uri + clientId;

        // last will message
        String message = (String) data.get(Constants.BUNDLE_MQTT_MESSAGE);
        String topic = (String) data.get(Constants.BUNDLE_MQTT_TOPIC);
        Integer qos = (Integer) data.get(Constants.BUNDLE_MQTT_QOS);
        Boolean retained = (Boolean) data.get(Constants.BUNDLE_MQTT_RETAINED);

        // connection options
        String username = (String) data.get(Constants.BUNDLE_MQTT_USER_NAME);
        String password = (String) data.get(Constants.BUNDLE_MQTT_PASSWORD);
        int timeout = (Integer) data.get(Constants.BUNDLE_MQTT_TIMEOUT);
        int keepalive = (Integer) data.get(Constants.BUNDLE_MQTT_KEEPALIVE);

        Connection connection = new Connection(clientHandle, clientId, server, port, mContext, client, ssl);
        connection.registerChangeListener(mChangeListener);

        // connect client
        String[] actionArgs = new String[1];
        actionArgs[0] = clientId;
        connection.changeConnectionStatus(ConnectionStatus.CONNECTING);

        conOpt.setCleanSession(cleanSession);
        conOpt.setConnectionTimeout(timeout);
        conOpt.setKeepAliveInterval(keepalive);
        if (!username.equals(Constants.empty)) {
            conOpt.setUserName(username);
        }
        if (!password.equals(Constants.empty)) {
            conOpt.setPassword(password.toCharArray());
        }

        // ActionListener notify user with toast and change connection status
        final MqttActionListener callback = new MqttActionListener(mContext,
                MqttActionListener.Action.CONNECT, clientHandle, mActionCallback, actionArgs);

        boolean doConnect = true;

        if ((!message.equals(Constants.empty))
                || (!topic.equals(Constants.empty))) {
            // need to make a message since last will is set
            try {
                conOpt.setWill(topic, message.getBytes(), qos.intValue(),
                        retained.booleanValue());
            }
            catch (Exception e) {
                Log.e(this.getClass().getCanonicalName(), "Exception Occured", e);
                doConnect = false;
                callback.onFailure(null, e);
            }
        }
        // set callback for connection status, publish and message receive event
        client.setCallback(new MqttManager.MqttCallbackHandler(mContext, clientHandle));

        //set traceCallback
        client.setTraceCallback(new MqttTraceCallback());

        connection.addConnectionOptions(conOpt);
        Connections.getInstance(mContext).addConnection(connection);
        if (doConnect) {
            try {
                client.connect(conOpt, mContext, callback);
                result = true;
                if(mManagerListener != null)
                    mManagerListener.OnMqttManagerCallback(IMqttManagerListener.CALLBACK_MQTT_ADD_CONNECTION,
                            0, 0, null, null, (Object)connection);
            }
            catch (MqttException e) {
                Log.e(this.getClass().getCanonicalName(), "MqttException Occured", e);
                result = false;
            }
        }
        return result;
    }

    public void deleteConnection(String handle) {
        mSelectedConnection = Connections.getInstance(mContext).getConnection(handle);
        if (mSelectedConnection.isConnectedOrConnecting()) {
            //display a dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.disconnectClient)
                    .setMessage(mContext.getString(R.string.deleteDialog))
                    .setNegativeButton(R.string.cancelBtn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // do nothing. user cancelled action
                        }
                    })
                    .setPositiveButton(R.string.continueBtn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            try {
                                // disconnect
                                mSelectedConnection.getClient().disconnect();
                                // user pressed continue disconnect client and delete
                                continueDeletion();
                            }
                            catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .show();
        }
        else {
            continueDeletion();
        }
    }

    /**
     * Reconnect the selected client
     */
    public void reconnect(String clientHandle) {
        Connection c = Connections.getInstance(mContext).getConnection(clientHandle);
        c.changeConnectionStatus(ConnectionStatus.CONNECTING);
        try {
            MqttAndroidClient client = c.getClient();
            client.setCallback(new MqttManager.MqttCallbackHandler(mContext, clientHandle));
            client.setTraceCallback(new MqttTraceCallback());
            client.connect(c.getConnectionOptions(), mContext,
                    new MqttActionListener(mContext, MqttActionListener.Action.CONNECT,
                            clientHandle, mActionCallback, null));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to reconnect the client with the handle " + clientHandle, e);
            c.addAction("Client failed to connect");
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to reconnect the client with the handle " + clientHandle, e);
            c.addAction("Client failed to connect");
        }
    }

    /**
     * Disconnect the client
     */
    public void disconnect(String clientHandle) {
        Connection c = Connections.getInstance(mContext).getConnection(clientHandle);
        //if the client is not connected, process the disconnect
        if (c == null || !c.isConnected()) {
            return;
        }
        try {
            c.getClient().disconnect(null, new MqttActionListener(mContext, MqttActionListener.Action.DISCONNECT,
                    clientHandle, mActionCallback, null));
            c.changeConnectionStatus(ConnectionStatus.DISCONNECTING);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to disconnect the client with the handle " + clientHandle, e);
            c.addAction("Client failed to disconnect");
        }
    }

    /**
     * Subscribe to a topic from all connected server
     */
    public int subscribeFromAll(String topic, int qos) {
        int count = 0;
        Map<String, Connection> connections = Connections.getInstance(mContext).getConnections();
        for(Connection c : connections.values()) {
            if(c.isConnected()) {
                subscribe(c.handle(), topic, qos);
                count++;
            }
        }
        return count;
    }

    /**
     * Subscribe to a topic that the user has specified
     */
    public void subscribe(String clientHandle, String topic, int qos) {
        Connection c = Connections.getInstance(mContext).getConnection(clientHandle);
        if (c == null || !c.isConnected()) {
            return;
        }
        try {
            String[] topics = new String[1];
            topics[0] = topic;
            c.getClient().subscribe(topic, qos, null,
                    new MqttActionListener(mContext, MqttActionListener.Action.SUBSCRIBE,
                            clientHandle, mActionCallback, topics));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
        }
    }

    /**
     * Publish the message to all connected server
     */
    public int publishToAll(String topic, String message, int qos, boolean retained) {
        int count = 0;
        Map<String, Connection> connections = Connections.getInstance(mContext).getConnections();
        for(Connection c : connections.values()) {
            if(c.isConnected()) {
                publish(c.handle(), topic, message, qos, retained);
                count++;
            }
        }
        return count;
    }

    /**
     * Publish the message the user has specified
     */
    public void publish(String clientHandle, String topic, String message, int qos, boolean retained) {
        String[] args = new String[2];
        args[0] = message;
        args[1] = topic+";qos:"+qos+";retained:"+retained;

        Connection c = Connections.getInstance(mContext).getConnection(clientHandle);
        if (c == null || !c.isConnected()) {
            return;
        }
        try {
            c.getClient().publish(topic, message.getBytes(), qos, retained, null,
                    new MqttActionListener(mContext, MqttActionListener.Action.PUBLISH,
                            clientHandle, mActionCallback, args));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
    }

    /**
     * Enables logging in the Paho MQTT client
     */
    public void enablePahoLogging() {

        try {
            InputStream logPropStream = mContext.getResources().openRawResource(R.raw.jsr47android);
            LogManager.getLogManager().readConfiguration(logPropStream);
            mPahoLogging = true;

            HashMap<String, Connection> connections = (HashMap<String,Connection>)Connections.getInstance(mContext).getConnections();
            if(!connections.isEmpty()){
                Map.Entry<String, Connection> entry = connections.entrySet().iterator().next();
                Connection connection = (Connection)entry.getValue();
                connection.getClient().setTraceEnabled(true);
                //Connections.getInstance(context).getConnection(clientHandle).getClient().setTraceEnabled(true);
            }else{
                Log.i("SampleListener","No connection to enable log in service");
            }
        }
        catch (IOException e) {
            Log.e("MqttAndroidClient",
                    "Error reading logging parameters", e);
        }
    }

    /**
     * Disables logging in the Paho MQTT client
     */
    public void disablePahoLogging() {
        LogManager.getLogManager().reset();
        mPahoLogging = false;

        HashMap<String, Connection> connections = (HashMap<String,Connection>)Connections.getInstance(mContext).getConnections();
        if(!connections.isEmpty()){
            Map.Entry<String, Connection> entry = connections.entrySet().iterator().next();
            Connection connection = (Connection)entry.getValue();
            connection.getClient().setTraceEnabled(false);
        }else{
            Log.i("SampleListener", "No connection to disable log in service");
        }
    }



    /************************************************************************
     *
     * Listener, Callback
     *
     ***********************************************************************/

    /**
     * This class ensures that the user interface is updated as the Connection objects change their states
     */
    private class ChangeListener implements PropertyChangeListener {
        /**
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        private String propertyDetails = null;
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            // Use connection status message only
            if (!event.getPropertyName().equals(Constants.PROPERTY_CONNECTION_STATUS)) {
                return;
            }
            propertyDetails = "[!] " + event.getSource();
            if(mContext instanceof Activity) {
                ((MainActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mManagerListener != null)
                            // Activity have to update connection's history
                            mManagerListener.OnMqttManagerCallback(
                                    IMqttManagerListener.CALLBACK_MQTT_PROPERTY_CHANGED,
                                    0, 0,
                                    propertyDetails, null, null);
                    }
                });
            }
        }
    }

    public class MqttActionCallback implements IMqttActionCallback {
        @Override
        public void onActionResult(int msg_type, int arg0, int arg1, String arg2, String arg3, Object arg4) {
            switch(msg_type) {
                case IMqttActionCallback.ACTION_RESULT_CONNECTED:
                    // notify to activity
                    if(mManagerListener != null)
                        mManagerListener.OnMqttManagerCallback(
                                IMqttManagerListener.CALLBACK_MQTT_CONNECTED,
                                0, 0,
                                arg2/*handle*/, arg3, null);
                    break;
                case IMqttActionCallback.ACTION_RESULT_DISCONNECTED:
                    // notify to activity
                    if(mManagerListener != null)
                        mManagerListener.OnMqttManagerCallback(
                                IMqttManagerListener.CALLBACK_MQTT_DISCONNECTED,
                                0, 0,
                                arg2/*handle*/, arg3, null);
                    break;
                case IMqttActionCallback.ACTION_RESULT_PUBLISHED:
                    // notify to activity
                    if(mManagerListener != null)
                        mManagerListener.OnMqttManagerCallback(
                                IMqttManagerListener.CALLBACK_MQTT_PUBLISHED,
                                0, 0,
                                arg2/*handle*/, arg3/*message*/, null);
                    break;
                case IMqttActionCallback.ACTION_RESULT_SUBSCRIBED:
                    // notify to activity
                    if(mManagerListener != null)
                        mManagerListener.OnMqttManagerCallback(
                                IMqttManagerListener.CALLBACK_MQTT_SUBSCRIBED,
                                0, 0,
                                arg2/*handle*/, arg3/*message*/, null);
                    break;
            }
        }
    }

    public class MqttCallbackHandler implements MqttCallback {

        /** {@link Context} for the application used to format and import external strings**/
        private Context context;
        /** Client handle to reference the connection that this handler is attached to**/
        private String clientHandle;

        /**
         * Creates an <code>MqttCallbackHandler</code> object
         * @param context The application's context
         * @param clientHandle The handle to a {@link Connection} object
         */
        public MqttCallbackHandler(Context context, String clientHandle) {
            this.context = context;
            this.clientHandle = clientHandle;
        }

        /**
         * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
         */
        @Override
        public void connectionLost(Throwable cause) {
            // cause.printStackTrace();
            if (cause != null) {
                Connection c = Connections.getInstance(context).getConnection(clientHandle);
                c.addAction("Connection Lost");
                c.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);

                // for notification
                // format string to use a notification text
                Object[] args = new Object[2];
                args[0] = c.getId();
                args[1] = c.getHostName();
                String message = context.getString(R.string.connection_lost, args);

                // notify to activity
                if(mManagerListener != null)
                    mManagerListener.OnMqttManagerCallback(
                            IMqttManagerListener.CALLBACK_MQTT_CONNECTION_LOST,
                            0, 0,
                            c.handle(), message, c);

                //build intent
                Intent intent = new Intent();
                intent.setClassName(context, Constants.NOTI_TARGET_ACTIVITY);
                intent.putExtra("handle", clientHandle);

                //notify the user
                Notify.notifcation(context, message, intent, R.string.notifyTitle_connectionLost);
            }
        }

        /**
         * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
         */
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            //Get connection object associated with this object
            Connection c = Connections.getInstance(context).getConnection(clientHandle);

            //create arguments to format message arrived notifcation string
            String[] args = new String[2];
            args[0] = new String(message.getPayload());
            args[1] = topic+";qos:"+message.getQos()+";retained:"+message.isRetained();

            //update client history
            c.addAction(args[0]);

            // notify to activity
            if(mManagerListener != null)
                mManagerListener.OnMqttManagerCallback(
                        IMqttManagerListener.CALLBACK_MQTT_MESSAGE_ARRIVED,
                        0, 0,
                        c.handle(), args[0], c);

            // notify to activity
            // create intent to start activity
            //Intent intent = new Intent();
            //intent.setClassName(context, Constants.NOTI_TARGET_ACTIVITY);
            //intent.putExtra("handle", clientHandle);

            //format string args
            //Object[] notifyArgs = new String[3];
            //notifyArgs[0] = c.getId();
            //notifyArgs[1] = new String(message.getPayload());
            //notifyArgs[2] = topic;

            //notify the user
            //Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);
        }

        /**
         * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
         */
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // notify to activity
            // I think this is same with [MqttActionCallback - IMqttActionCallback.ACTION_RESULT_PUBLISHED]
            if(mManagerListener != null)
                mManagerListener.OnMqttManagerCallback(
                        IMqttManagerListener.CALLBACK_MQTT_MESSAGE_DELIVERED,
                        0, 0,
                        null, null, token);
        }

    } // End of class MqttCallbackHandler

}
