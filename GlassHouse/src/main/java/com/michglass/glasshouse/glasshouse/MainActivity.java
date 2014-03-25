package com.michglass.glasshouse.glasshouse;

/**
 * Created by Vijay Ganesh
 * Date: 2/26/14
 */
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;


import com.google.android.glass.widget.CardScrollView;

import java.io.File;

public class MainActivity extends Activity {

    // Debug
    private static final String TAG = "Main Activity";

    // Messages from BT
    private static final int CAMERA_REQ = 5;
    private static final int VIDEO_REQ = 6;

    // BT Variables
    private Messenger mBluetoothServiceMessenger;
    private boolean mBound;
    private final Messenger clientMessenger = new Messenger(new ServiceHandler());

    // UI Variables
    private CardScrollView mCardScrollView;
    private Gestures mCurrGestures;
    private Slider mCurrentSlider;
    private Media mMedia;

    private GraceCardScrollerAdapter mBaseCardsAdapter;
    private GraceCardScrollerAdapter mMediaCardsAdapter;
    private GraceCardScrollerAdapter mPostMediaCardsAdapter;
    private GraceCardScrollerAdapter mCommContactsAdapter;
    private GraceCardScrollerAdapter mCommMessagesAdapter;
    private GraceCardScrollerAdapter mGameCardsAdapter;
    private GraceCardScrollerAdapter mCurrentAdapter;

    private static final String MEDIA = "Media";
    private static final String COMM = "Comm";
    private static final String GAMES = "Games";
    private static final String CAMERA = "Camera";
    private static final String VIDEO = "Video";
    private static final String REDO = "Redo";
    private static final String SAVE = "Save";
    private static final String SEND = "Send";
    private static final String BACK = "Back";


    /**
     * Activity Lifecycle Methods
     * On Create: Start BT(only in one Activity)
     * On Start: Bind to BT
     * On Stop: Unbind from BT
     * On Destroy: Stop BT(only in one Activity)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "On Create");

        // Start the BT Service
        startService(new Intent(this, BluetoothService.class));

        // initialize all hierarchy adapters and add cards to them
        buildScrollers();
        mCurrentAdapter = mBaseCardsAdapter;

        mCardScrollView = new CardScrollView(this);
        mCardScrollView.setAdapter(mBaseCardsAdapter);
        mCardScrollView.activate(); // makes it work

        mMedia = new Media();
        mCurrGestures = new Gestures();
        mCurrentSlider = new Slider(mBaseCardsAdapter.getCount());
        Log.v(TAG, "Scroll View Size: " + mCardScrollView.getCount());
        Log.v(TAG, "Adapter Size: " + mBaseCardsAdapter.getCount());


        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // implement any specific card behavior here, wrap it in a class though so this function isn't huge
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "On Item Click");

                GraceCard graceCard = (GraceCard) mCurrentAdapter.getItem(position);
                Log.v(TAG, graceCard.getText());

                if(graceCard.getText().equals(COMM))
                    sendMessageToService(BluetoothService.TEXT_MESSAGE, "Hey Tim");
                // null adapter means this card has no tap event so do nothing
                if (graceCard.getAdapter() == null) {
                    Log.v(TAG, "Card Scroll Adapter NULL");
                    return;
                }

                // long ass switch for cards that have functions
                final String cardText = graceCard.getText();
                if (cardText.equals(CAMERA)) {
                    takePicture();
                } else if (cardText.equals(VIDEO)) {
                    recordVideo();
                } else if (cardText.equals(REDO)) {
                    // remove screenshot from post media menu cards
                    mPostMediaCardsAdapter.popCardFront();
                } else if (cardText.equals(SAVE)) {
                    // save to disk, or whatever
                } else if (cardText.equals(SEND)) {
                    // launch contacts picker, send media to phone or whatever
                } else if(cardText.equals(COMM)) {
                    //TODO just a test, When COMM is clicked you shouldn't actually send a msg to Android
                    sendMessageToService(BluetoothService.TEXT_MESSAGE, "Hey Tim");
                }

                // kill "old" slider and replace with a new one for our new hierarchy
                mCurrentSlider.stopSlider();
                mCurrGestures = new Gestures();
                mCurrentSlider = new Slider(mCurrentAdapter.getCount());

                // switch out cards being displayed
                mCurrentAdapter = graceCard.getAdapter();
                mCardScrollView.setAdapter(mCurrentAdapter);
                mCardScrollView.activate();

                // visual switch of hierarchy and start our slider
                mCurrentAdapter.notifyDataSetChanged();

                // Start Running the new swipe loop
                mCurrentSlider.start();
            }
        });

        setContentView(mCardScrollView);
        mCurrentSlider.start();
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "On Start");

        // Bind this Activity to the BT Service
        if(!mBound) {
            bindService(new Intent(this, BluetoothService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }

        // mGestures.startSwipeLoop(mBaseCardsAdapter.getCount());
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "On Stop");

        // Unbind from BT Service
        if(mBound) {
            sendMessageToService(BluetoothService.INT_MESSAGE, BluetoothService.UNREGISTER_CLIENT);
            unbindService(mConnection);
            mBound = false;
        }
        // mGestures.stopSwipeLoop();
        mCurrentSlider.stopSlider();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "On Destroy");

        // Stop the BT Service only in the component that calls startservice() !!
        stopService(new Intent(this, BluetoothService.class));
    }

    // create cards for each hierarchy, add to that's hierarchies adapter
    private void buildScrollers() {
        mBaseCardsAdapter = new GraceCardScrollerAdapter();
        mMediaCardsAdapter = new GraceCardScrollerAdapter();
        mPostMediaCardsAdapter = new GraceCardScrollerAdapter();

        mBaseCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, MEDIA));
        mBaseCardsAdapter.pushCardBack(new GraceCard(this, null, COMM));
        mBaseCardsAdapter.pushCardBack(new GraceCard(this, null, GAMES));

        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mPostMediaCardsAdapter, CAMERA));
        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mPostMediaCardsAdapter, VIDEO));
        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, BACK));


        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, REDO));
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, SAVE)); // loop back to main menu for now
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, SEND)); // loop back to main menu for now
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, BACK));

        /* **** below needs to be implemented still */

        // communication hierarchy
        mCommContactsAdapter = new GraceCardScrollerAdapter();
        mCommMessagesAdapter = new GraceCardScrollerAdapter();

        // game hierarchy
        mGameCardsAdapter = new GraceCardScrollerAdapter();
    }

    // this class is responsible for the right-to-left & left-to-right "sliding" of the cards
    private class Slider extends Thread {

        private int mCurrPosition;
        private int mFinalPosition;
        private boolean swipeRight;
        private boolean swipeLeft;
        private boolean stop;

        public Slider(int numCards) {
            mCurrPosition = 0;
            mFinalPosition = numCards - 1;
            swipeRight = true;
            swipeLeft = false;
            stop = false;
            Log.v(TAG, "Final Pos Slider: " + mFinalPosition);
        }

        @Override
        public void run() {

            // stop = false;

            while(!stop) {

                try {
                    sleep(3000);
                } catch (InterruptedException intE) {
                    Log.e(TAG, "Slider Interrupted", intE);
                    return;
                }

                if(!stop) {

                    if (swipeRight) {
                        mCurrGestures.createGesture(Gestures.TYPE_SWIPE_RIGHT);
                        mCurrPosition++;
                    } else if (swipeLeft) {
                        mCurrGestures.createGesture(Gestures.TYPE_SWIPE_LEFT);
                        mCurrPosition--;
                    }

                    if (mCurrPosition == 0) {
                        swipeRight = true;
                        swipeLeft = false;
                    } else if (mCurrPosition == mFinalPosition) {
                        swipeLeft = true;
                        swipeRight = false;
                    }
                }
            }
        }

        public void stopSlider() {
            stop = true;
        }
    }

    /**
     * On Activity Result
     * System callback method for startActivityForResult()
     * @param requestCode Code that we put in startActivityForResult
     * @param resultCode Code that indicates if Bluetooth connection was established
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode != RESULT_OK)
            return;

        switch (resultCode) {

            case RESULT_OK:
                Log.v(TAG, "Bluetooth Success");
                break;
            case RESULT_CANCELED:
                Log.v(TAG, "Bluetooth Failed");
                break;
            case CAMERA_REQ:
            {
                // fetch media URI, get screenshot from video, create new card and add to beginning of PostMedia adapter
                Uri imageLocation = intent.getData();
                Bundle extras = intent.getExtras();
                Bitmap screenshot = (Bitmap) extras.get("image");
                insertScreenshotIntoPostMediaMenu(screenshot, imageLocation);
                break;
            }
            case VIDEO_REQ:
            {
                // fetch media URI, get screenshot from video, create new card and add to beginning of PostMedia adapter
                Uri videoLocation = intent.getData();
                Bitmap screenshot = ThumbnailUtils.createVideoThumbnail(videoLocation.toString(), MediaStore.Images.Thumbnails.MINI_KIND);
                insertScreenshotIntoPostMediaMenu(screenshot, videoLocation);
                break;
            }
            default:
        }
    }

    // Utility functions

    private void insertScreenshotIntoPostMediaMenu(Bitmap screenshot, Uri mediaLocation) {
        mMedia.addMedia(screenshot, mediaLocation);
        GraceCard screenshotCard = new GraceCard(this, null, "");
        screenshotCard.addImage(mediaLocation);
        mPostMediaCardsAdapter.pushCardFront(screenshotCard);
        mPostMediaCardsAdapter.notifyDataSetChanged();
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQ);
    }

    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_REQ);
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
                    }
                    break;
                // in case this activity received a string message from phone
                case BluetoothService.MESSAGE_INCOMING:
                    Log.v(TAG, "message income");
                    break;
                case BluetoothService.COMMAND_OK:
                    Log.v(TAG, "Command ok");
                    //TODO Do something on OK
                    break;
                case BluetoothService.COMMAND_BACK:
                    Log.v(TAG, "Command back");
                    //TODO Do something on BACK
                    break;
                case BluetoothService.ANDROID_STOPPED:
                    Log.v(TAG, "Android App closed");
                    // mbtService.setState(BluetoothService.STATE_NONE);
                    finish(); // close this application if Android application is down
                    break;
                case BluetoothService.MESSAGE_CONNECTION_FAILED:
                    Log.v(TAG, "Failed Conn App Closing");
                    // mbtService.setState(BluetoothService.STATE_NONE);
                    finish();
                    break;
            }
        }
    }
}
