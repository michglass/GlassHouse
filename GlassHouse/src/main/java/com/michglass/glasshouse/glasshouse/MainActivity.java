package com.michglass.glasshouse.glasshouse;

/**
 * Created by vbganesh on 2/26/14.
 */
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.glass.app.Card;



import com.google.android.glass.app.Card;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private Card mCard = new Card(this);
    private Handler handler = new Handler();

    // Debug
    private static final String TAG = "Main Activity";

    // Bluetooth Vars
    private BluetoothAdapter mbtAdapter;
    private BluetoothService mbtService;

    // Request Variables
    private static final int REQUEST_ENABLE_BT = 1;

    // messages from BT service
    public static final int MESSAGE_STATE_CHANGE = 3; // indicates connection state change (debug)
    public static final int MESSAGE_INCOMING = 4; // message with string content (only for debug)

    public static final int COMMAND_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "On Create");

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.mbtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mbtAdapter == null) {
            Log.v(TAG, "BT not supported");
            finish();
        }

        runnable.run();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "On Destroy");

        if(mbtService != null) {
            // *** FIX mbtService.stopThreads();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Debugging
            Log.v(TAG, "inside void run()");

            setContentView(mCard.toView());

            /* and here comes the "trick" */
            handler.postDelayed(this, 5000);
        }
    };

    //From GlassBluetooth

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "On Start");

        // Request enabling Bluetooth, if it's not on
        if(!mbtAdapter.isEnabled()) {
            // Should always be enabled on Glass!
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Log.v(TAG, "Bluetooth already enabled"); // usually on Glass
            // find device Glass should be paired to
            mbtService = new BluetoothService(mHandler); // set up bluetooth service
            mbtService.queryDevices();
        }
    }

    /**
     * On Resume (Activity visible, not in foreground)
     * Start connection
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "On Resume");

        // Starting connection with mbtService
        // if successful ConnectedThread will start
        // (called from within ConnectThread.run) that manages the connection
        mbtService.connect();
    }

    /**
     * On Pause (Activity is not in foreground)
     */
    @Override
    protected void onPause() {
        Log.v(TAG, "On Pause");
        super.onPause();
    }

    /**
     * On Stop (Activity not longer visible)
     */
    @Override
    protected void onStop() {
        Log.v(TAG, "On Stop");
        super.onStop();

        // activity not longer visible
        // stop all threads which also close the sockets
        if(mbtService != null) {
            // *** FIX mbtService.stopThreads();
        }
    }

    /**
     * Util Functions
     */

    /**
     * Update Cards
     * Sets new Text on Card
     * @param msg Message from Android Phone
     */
    void updateCard(int msg, int index) {
        //
    }

    /**
     * Message Handler
     * Receive Messages from BluetoothService about connection state
     * Receive Messages from Connected Thread (android input)
     */
    private final Handler mHandler = new Handler() {

        // when message gets send this method
        // gives info to activity
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
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
                    }
                    break;

                // in case this activity received a string message from phone
                case MESSAGE_INCOMING:
                    Log.v(TAG, "message income");
                    // display message on the card, null check is performed
                    // before message gets send --> shouldn't be null
                    // String msgFromPhone = msg.getData().getString(BluetoothService.EXTRA_MESSAGE);
                    // display message on card
                    // updateCard(msgFromPhone);
                    break;

                // user commands that manipulate glass timeline
                //TODO on those commands invoke some kind of simulated Inputs

                case COMMAND_OK:
                    Log.v(TAG, "Command ok");

                    // need to change ScrollView to next hierarchy, use map for cardID to ScrollView to achieve this

                    break;
                default:
                    // something went wrong...

                    break;
            }
        }
    };

    /**
     * On Activity Result
     * System callback method for startActivityForResult()
     * @param requestCode Code that we put in startActivityForResult
     * @param resultCode Code that indicates if Bluetooth connection was established
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // shouldn't be called, because Bluetooth should be enabled on Glass
        switch (resultCode) {

            case RESULT_OK:
                Log.v(TAG, "Bluetooth Success");
                return;
            case RESULT_CANCELED:
                Log.v(TAG, "Bluetooth Failed");
        }
    }
}
