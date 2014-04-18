package com.michglass.glasshouse.glasshouse;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Oliver
 * Date: 4/14/2014.
 */
public class BluetoothActivity extends Activity {

    // Debug
    private static final String TAG = "Bluetooth Activity";

    // BT Variables
    private Messenger mBluetoothServiceMessenger;
    private boolean mBound;
    private final Messenger clientMessenger = new Messenger(new ServiceHandler());

    @Override
    protected void onStart() {
        Log.v(TAG, "On Start");
        super.onStart();

        // Bind this Activity to the BT Service
        if(!mBound) {

            bindService(new Intent(this, BluetoothService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }
    @Override
    protected void onStop() {
        Log.v(TAG, "On Stop");
        super.onStop();

        // Unbind from BT Service
        if(mBound) {

            sendMessageToService(BluetoothService.UNREGISTER_CLIENT);
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * Bluetooth Utility Functions
     * Service Connection for getting the Interface between Activity and Service
     * Send Message To Service
     * Set Up Message (First contact with Service)
     * Message Handler (Handling incoming messages from the Service)
     */

    /**
     * ServiceConnection
     * Callback Methods that get called when Client binds to Service
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.v(TAG, "On Service Connect");

            // set up messenger
            mBluetoothServiceMessenger = new Messenger(iBinder);
            mBound = true;

            // Send a first message to the service
            setUpMessage();
        }
        /**
         * Only called when Service unexpectedly disconnected!!
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.v(TAG, "On Service Disconnect");
            mBound = false;
        }
    };


    /**
     * Send to Android (1)
     * Send a byte array to android
     * @param androidmsg message for android
     */
    private void sendToAndroid(byte[] androidmsg) {
        sendMessageToService(BluetoothService.ANDROID_DATA, androidmsg);
    }
    /**
     * Send to Android (2)
     * Send a JSON Object to Android
     * @param json Json object for android
     */
    private void sendToAndroid(JSONObject json) {
        byte[] bytemsg = json.toString().getBytes();
        sendToAndroid(bytemsg);
    }
    /**
     * Send Message To Service (1)
     * Sends a non-Android related message to Android

     * @param message Message for service
     */
    public void sendMessageToService(int message) {
        Message msg = new Message();
        msg.what = message;

        try {
            Log.v(TAG, "Try contacting Service: type 1");
            mBluetoothServiceMessenger.send(msg);
        } catch (RemoteException remE) {
            Log.e(TAG, "Couldn't contact Service", remE);
        }
    }
    /**
     * Send Message to Service (2)
     * Send a Android related message to Service
     * @param w what parameter for msg
     * @param androidmsg msg for android
     */
    public void sendMessageToService(int w, byte[] androidmsg) {
        Message msg = new Message();
        msg.what = w;
        msg.obj = androidmsg;

        try{
            Log.v(TAG, "Try contacting Service: type 2");
            mBluetoothServiceMessenger.send(msg);
        } catch (RemoteException remE) {
            Log.e(TAG, "Couldn't contact service");
        }
    }
    /**
     * Set Up Message
     * First contact with Service
     * Has to be send!
     * (with clientMessenger in replyTo Param so Service can respond to client)
     */
    public void setUpMessage() {
        Message startMsg = new Message();
        startMsg.what = BluetoothService.REGISTER_CLIENT;
        startMsg.replyTo = clientMessenger;

        try {
            Log.v(TAG, "First time contact to service");
            mBluetoothServiceMessenger.send(startMsg);
        } catch (RemoteException remE) {
            Log.e(TAG, "Couldn't contact Service", remE);
        }
    }

    /**
     * Message Handler
     * Handles incoming messages from Service
     * Messages wrt Android Input or Connection State
     */
    public class ServiceHandler extends Handler {

        // when message gets send this method
        // gives info to activity
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    Log.v(TAG, "connection state changed");
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.v(TAG, "state connected");
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.v(TAG, "state connecting");
                            break;
                        case BluetoothService.STATE_NONE:
                            Log.v(TAG, "state none");
                            break;
                        case BluetoothService.STATE_LISTENING:
                            Log.v(TAG, "State Listening");
                            break;
                    }
                    break;
                // in case this activity received a string message from phone
                case BluetoothService.MESSAGE_INCOMING:
                    Log.v(TAG, "message income");
                    break;
                case BluetoothService.COMMAND_OK:
                    Log.v(TAG, "Command ok");
                    // Inject a Tap event
                    Gestures g = new Gestures();
                    g.createGesture(Gestures.TYPE_TAP);
                    break;
                case BluetoothService.ANDROID_STOPPED:
                    Log.v(TAG, "Android App closed");
                    sendMessageToService(BluetoothService.MESSAGE_RESTART);
                    break;
            }
        }
    }
}
