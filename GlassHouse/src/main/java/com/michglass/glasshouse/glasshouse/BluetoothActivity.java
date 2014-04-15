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

            sendMessageToService(BluetoothService.INT_MESSAGE, BluetoothService.UNREGISTER_CLIENT);
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
     * Send Message To Service
     * Sends a message over the Service to Android
     * @param messageType Type of message (int, String, Bitmap)
     * @param message Message body
     */
    public void sendMessageToService(int messageType, Object message) {
        Message msg = new Message();
        switch (messageType) {
            case BluetoothService.INT_MESSAGE:
                int intMsg = (Integer) message;
                msg.what = intMsg;
                break;
            case BluetoothService.TEXT_MESSAGE:
                msg.what = BluetoothService.TEXT_MESSAGE;
                msg.obj = message;
                break;
            case BluetoothService.PICTURE_MESSAGE:
                msg.what = BluetoothService.PICTURE_MESSAGE;
                msg.obj = message;
                break;
        }

        try {
            Log.v(TAG, "Try contacting Service");
            mBluetoothServiceMessenger.send(msg);
        } catch (RemoteException remE) {
            Log.e(TAG, "Couldn't contact Service", remE);
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
                case BluetoothService.COMMAND_BACK:
                    Log.v(TAG, "Command back");
                    break;
                case BluetoothService.ANDROID_STOPPED:
                    Log.v(TAG, "Android App closed");
                    sendMessageToService(BluetoothService.INT_MESSAGE, BluetoothService.MESSAGE_RESTART);
                    break;
            }
        }
    }
}
