package com.mglass.alphagraceapp.app;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

/*
    Created by Oliver
    Date: 02/19/2014
 */
public class BluetoothService extends Service {

    // Debug
    private static final String TAG = "Bluetooth Service";

    // Bluetooth Vars
    private final BluetoothAdapter mbtAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mbtDevice;
    private static final String DEVICE_NAME = "Galaxy NexusCDMA 2";
    // "Cone" = Danny
    // "SCH-I545" = Tim
    // "Galaxy NexusCDMA 2" = Oliver
    private int mCurrState;

    // Thread that initiates the connection and Thread that manages connection
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    // unique ID for the app (same as on the android phone)
    private static final UUID btUUID = UUID.fromString("bfdd94e0-9a5e-11e3-a5e2-0800200c9a66");


    // Constants that indicate the devices state
    public static final int STATE_NONE = 0; // doing nothing
    public static final int STATE_CONNECTING = 1; // connecting
    public static final int STATE_CONNECTED = 2; // connected
    // Messages for Main Activity
    public static final int MESSAGE_STATE_CHANGE = 3; // indicates connection state change (debug)
    public static final int MESSAGE_INCOMING = 4; // message with string content (only for debug)
    public static final int MESSAGE_CONNECTION_FAILED = 5;
    // Indicates that Android has stopped
    public static final int ANDROID_STOPPED = 8; //( == THIS_STOPPED on Android) indicates if the android app is still running
    public static final int THIS_STOPPED = 9; // (== GLASS_STOPPED on Android) indicates if this app has stopped
    // Commands for Glass
    public static final int COMMAND_OK = 10;
    public static final int COMMAND_BACK = 11;

    // Extra is the key for the string message that gets put into the bundle
    // the bundle is send to the activity which can get the string message with this key
    public static final String EXTRA_MESSAGE = "com.mglass.alphagraceapp.app.EXTRA_MESSAGE";

    // Service variables
    public static final int REGISTER_CLIENT = 12;
    public static final int UNREGISTER_CLIENT = 13;
    public static int BOUND_COUNT = 0;
    private Messenger mClientMessenger;
    private final Messenger mBluetoothServiceMessenger = new Messenger(new ClientHandler());

    public static final int PICTURE_MESSAGE = 14;
    public static final int TEXT_MESSAGE = 15;
    public static final int INT_MESSAGE = 16;

    /**
     * Service Methods
     * Lifecycle methods
     * Handling messages from Client
     * Sending Messages to Client
     */

    /**
     * Life cycle methods of the Service
     */
    @Override
    public void onCreate() {
        Log.v(TAG, "Create Service");
        super.onCreate();

        if(!AdapterEnabled()) {
            // Should always be enabled!
            Log.v(TAG, "Bluetooth not enabled");
        } else {
            Log.v(TAG, "Bluetooth already enabled"); // usually on Glass
            // find paired devices and connect to desired device
            queryDevices();
        }
    }
    /**
     * On Start Command
     * Start up Bluetooth Connection
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "On Start Command: " + startId);

        // start connection with android device
        connect();

        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * On Bind
     * Client can use Bluetooth Connection
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "On Bind");
        return mBluetoothServiceMessenger.getBinder();
    }
    /**
     * On Unbind
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "On Unbind");

        return true;
    }
    /**
     * On Rebind
     */
    @Override
    public void onRebind(Intent intent) {
        Log.v(TAG, "On Rebind");
        super.onRebind(intent);
    }
    /**
     * On Destroy
     * Let Glass know that the Android app has stopped
     * End Bluetooth connection
     */
    @Override
    public void onDestroy() {
        Log.v(TAG, "Destroy Service");

        sendToAndroid(THIS_STOPPED);
        // Disconnect from Bluetooth and stop threads
        disconnect();

        super.onDestroy();
    }

    /**
     * Client Handler
     * Handles incoming Messages from Client
     * Important messages: GLASS_X messages that get send via Bluetooth to Glass
     */
    private class ClientHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case PICTURE_MESSAGE:
                    Log.v(TAG, "Picture for Android");
                    Bitmap bitMap = (Bitmap) msg.obj;
                    // sendPictureToAndroid(bitMap);
                    break;
                case TEXT_MESSAGE:
                    String text = (String) msg.obj;
                    sendStringToAndroid(text);
                    Log.v(TAG, "Text msg for Android");
                    break;
                case REGISTER_CLIENT:
                    Log.v(TAG, "Register Client");
                    BluetoothService.BOUND_COUNT++;
                    Log.v(TAG, "Bound Clients: " + BluetoothService.BOUND_COUNT);

                    // register Client to be able to send Messages back
                    mClientMessenger = msg.replyTo;

                    //sendMessageToClient(TestService.REGISTER_CLIENT);
                    break;
                case UNREGISTER_CLIENT:
                    Log.v(TAG, "Unregister Client");
                    BluetoothService.BOUND_COUNT--;
                    Log.v(TAG, "Bound Clients: " + BluetoothService.BOUND_COUNT);
                    //sendMessageToClient(TestService.UNREGISTER_CLIENT);
                    break;
            }
        }
    }
    /**
     * Send Message To Client
     * Sends a message to a bound Client
     * @param clientMsg Message for the Client
     */
    private void sendMessageToClient(int clientMsg) {
        Message msg = new Message();
        msg.what = clientMsg;

        try {
            if(mClientMessenger == null) {
                Log.v(TAG, "Client Messenger NULL");
            } else {
                mClientMessenger.send(msg);
            }
        } catch (RemoteException remE) {
            Log.e(TAG, "Couldn't contact Client");
        }
    }

    /**
     * Methods to handle the Threads
     * Start Threads
     * Manage Connection
     * Shut Down Threads
     */

    /**
     * Connect
     * Start Up Connection by starting ConnectThread
     * Called by Activity onResume
     */
    public void connect() {
        Log.v(TAG, "Connect");

        // Cancel all Threads currently trying to set up a connection
        if(mCurrState == STATE_CONNECTING) {
            if(mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel Thread that currently runs a connection
        if(mConnectedThread != null) {
            mConnectedThread.cancel(); // close socket
            mConnectedThread = null;
        }

        // Start thread to connect to device
        // Device is passed to obtain socket and Handler for sending messages to Activity
        mConnectThread = new ConnectThread(mbtDevice);
        mConnectThread.start();

        // set state to connecting and send message to activity
        setState(STATE_CONNECTING);
    }
    /**
     * Manage Connection
     * Start ConnectedThread
     * @param btSocket Socket that helps to get I/O streams
     */
    public void manageConnection(BluetoothSocket btSocket) {
        Log.v(TAG, "Manage Connection");
/*
        // Cancel thread currently setting up a connection
        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
*/
        // Cancel thread currently running a connection
        if(mConnectedThread != null) {
            mConnectedThread.cancel(); // closes Bluetooth socket
            mConnectedThread = null;
        }

        // Start thread to manage the connection
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        // connection successful, set state to connected
        setState(STATE_CONNECTED);
    }
    /**
     * Stop Threads
     * Stop all threads
     * Called by Activity onDestroy
     */
    public void disconnect() {
        Log.v(TAG, "Stop all Threads");

        // cancel connecting thread if it exists
        if(mConnectThread != null) {
            mConnectThread.cancel(); // cancels the socket
            mConnectThread = null;
        }

        // cancel thread running the connection
        if(mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // all threads stopped, set state to none and send message to UI
        if(mCurrState != BluetoothService.STATE_NONE)
            setState(STATE_NONE);
    }
    /**
     * Write
     * Sends a message to Android device
     * @param msg Message for Android device
     */
    public void sendToAndroid(int msg) {

        if(mCurrState != BluetoothService.STATE_NONE) {
            if (mConnectedThread != null)
                mConnectedThread.write(msg);
            else {
                Log.v(TAG, "Connection not yet established");
            }
        }
    }
    public void sendStringToAndroid(String text) {
        if(mCurrState != BluetoothService.STATE_NONE) {
            if(mConnectedThread != null)
                mConnectedThread.writeString(text);
            else
                Log.v(TAG, "Connection not yet established");
        }
    }


    /**
     * Util Methods
     */

    /**
     * Adapter Enabled
     * Checks if the BT Adapter is enabled
     * @return True if Adapter is enabled, false otherwise
     */
    public boolean AdapterEnabled() {
        return this.mbtAdapter.isEnabled();
    }
    /**
     * Set state
     * Set the new connection state
     * Send message to Main Activity
     * @param toState new State of connection
     */
    public void setState(int toState) {
        Log.v(TAG, "State changed from " + mCurrState + "-->"+ toState);
        mCurrState = toState;

        // send message to Main Activity
        Message msg = new Message();
        msg.what = BluetoothService.MESSAGE_STATE_CHANGE;
        msg.arg1 = toState;
    }
    /**
     * Query Devices
     * Query all available devices
     * Assign Danny's (later Grace's) phone to the member device
     */
    public void queryDevices() {
        Log.v(TAG, "query devices");
        // get all paired devices
        Set<BluetoothDevice> pairedDevices = mbtAdapter.getBondedDevices();
        Log.v(TAG, mbtAdapter.getName());
        try {
            // start looking only if there's at least one device
            if(pairedDevices.size() > 0) {
                // find specific Device (Grace's phone)
                for(BluetoothDevice btDevice : pairedDevices) {
                    // if device is found save it in member var
                    if(btDevice.getName().equals(DEVICE_NAME)) {
                        mbtDevice = btDevice;
                    }
                    Log.v(TAG, "Device Name: "+ mbtDevice.getName());
                }
            } else {
                Log.v(TAG, "No devices found");
            }
        } catch (Exception e) {
            Log.v(TAG, "No devices found");
        }
    }

    /**
     * Connect Thread
     * Send out connection request
     * Start ConnectedThread
     */
    private class ConnectThread extends Thread {
        // Debug
        private static final String TAG = "Connect Thread";

        // Bluetooth Vars
        private final BluetoothSocket mmBtSocket;

        /**
         * Constructor
         * Set up the device
         * Set up the Handler (sending messages back to Main Activity)
         * Set up Socket
         * @param device Bluetooth device variable (e.g. Grace's phone)
         */
        public ConnectThread(BluetoothDevice device) {
            Log.v(TAG, "Constructor");

            BluetoothSocket tempSocket = null;

            // set up bluetooth socket with UUID
            try {
                Log.v(TAG, "Try setting up Socket");
                // returns a BT Socket ready for outgoing connection
                tempSocket = device.createRfcommSocketToServiceRecord(btUUID);
            } catch (Exception e) {
                Log.e(TAG, "Socket Setup Failed");
            }
            mmBtSocket = tempSocket;
        }
        /**
         * Run
         * Send out request for connection
         * When connection established, start Manage Connection
         */
        @Override
        public void run() {
            int i = 0;
            Log.v(TAG, "Run");
            // Connect device through Socket
            // Blocking call!
            while(!mmBtSocket.isConnected()) {
                try {
                    Log.v(TAG, "Try connecting through socket");
                    mmBtSocket.connect();
                } catch (IOException connectException) {
                    // unable to connect, try closing socket
                    Log.v(TAG, "Unable to Connect");
                    if(i == 5) {
                        Log.v(TAG, "Connection request timed out, Shut Down");
                        try {
                            // send message to activity
                            sendMessageToClient(BluetoothService.MESSAGE_CONNECTION_FAILED);

                            // close Socket
                            mmBtSocket.close();
                        } catch (IOException closeException) {
                            Log.e(TAG, "Closing Socket Failed", closeException);
                        }
                        Log.v(TAG, "Run Return after Fail");
                        return;
                    } else {
                        Log.v(TAG, "Try connecting again in 3sec");
                        i++;
                        SystemClock.sleep(3000);
                    }
                }
            }
            // connection established, manage connection
            manageConnection(mmBtSocket);
            Log.v(TAG, "Run Return after Success");
        }
        /**
         * Cancel
         * Close Socket
         */
        public void cancel() {
            try {
                Log.v(TAG, "Try Closing Socket");
                mmBtSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Closing Socket Failed", e);
            }
        }
    }
    /**
     * Connected Thread
     * Manages Connections
     * Receives and sends messages to Android Phone
     */
    private class ConnectedThread extends Thread {

        // Debug
        private static final String TAG = "Connected Thread";

        // Socket and Input Stream
        private final BluetoothSocket mmBTSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        /**
         * Constructor
         * Set up Socket
         * Set up Handler
         * Get I/O streams from socket
         */
        public ConnectedThread(BluetoothSocket btSocket) {
            Log.v(TAG, "Constructor");

            // set up socket and Instream
            mmBTSocket = btSocket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                Log.v(TAG, "Try getting I/O Streams");
                tempIn = btSocket.getInputStream();
                tempOut = btSocket.getOutputStream();
            } catch (IOException ioE) {
                Log.e(TAG, "Failed getting I/O Streams", ioE);
            }

            // success getting I/O streams
            mmInStream = tempIn;
            mmOutStream = tempOut;
        }
        /**
         * Run
         * Listen to Input from Android Phone
         * Send Input to Main Activity
         */
        public void run() {
            Log.v(TAG, "Run");

            byte[] inBuffer = new byte[1024]; // input buffer that will store the msg form Android
            int bytes; // bytes returned from mmInStream.read

            // listen to incoming data until exception occurs
            //TODO break while loop when Android app stops or this app gets shut down
            while(true) {
                Log.v(TAG, "Loop ConnectedThread");
                try {
                    bytes = mmInStream.read(inBuffer);

                    // convert byte array to int
                    ByteBuffer wrapper = ByteBuffer.wrap(inBuffer);
                    int inMessage = wrapper.getInt();
                    Log.v(TAG, "InputStream: " + inMessage);

                    // Handler sends message to Activity
                    sendMessageToClient(inMessage);
                } catch (IOException ioE) {
                    Log.e(TAG, "Failed reading inStream", ioE);
                    break;
                }
            }
            Log.v(TAG, "Run Return");
        }
        /**
         * Write
         * Write a message to Output Stream and send it to Android Device
         * @param msg Message that gets send to Android
         */
        public void write(int msg) {
            Log.v(TAG, "Write Out");
            byte[] buffer = ByteBuffer.allocate(4).putInt(msg).array();

            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Failed writing to Android", e);
            }
        }
        public void writeString(String msg) {
            Log.v(TAG, "Write Out");
            byte[] buffer = msg.getBytes();

            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Failed writing to Android", e);
            }
        }
        /**
         * Cancel
         * Close the socket
         */
        public void cancel() {
            try {
                Log.v(TAG, "Try closing Socket");
                mmBTSocket.close();
            } catch (IOException ioE) {
                Log.e(TAG, "Failed closing connection");
            }
        }
    }
}
