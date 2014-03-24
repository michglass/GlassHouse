package com.michglass.glasshouse.glasshouse;

/**
 * Created by vbganesh on 2/26/14.
 */
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;


import com.google.android.glass.widget.CardScrollView;

public class MainActivity extends Activity {

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
    private static final int CAMERA_REQ = 5;
    private static final int VIDEO_REQ = 6;

    public static final int COMMAND_OK = 1;

    private CardScrollView mCardScrollView;
    private Gestures mGestures;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "On Create");

        // initialize all hierarchy adapters and add cards to them
        buildScrollers();
        mCurrentAdapter = mBaseCardsAdapter;

        mCardScrollView = new CardScrollView(this);
        mCardScrollView.setAdapter(mBaseCardsAdapter);

        mMedia = new Media();
        mGestures = new Gestures();
        mCurrentSlider = new Slider(mCardScrollView.getCount());

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.mbtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mbtAdapter == null) {
            Log.v(TAG, "BT not supported");
            finish();
        }

        // implement any specific card behavior here, wrap it in a class though so this function isn't huge
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                GraceCard graceCard = (GraceCard) mCurrentAdapter.getItem(position);

                // null adapter means this card has no tap event so do nothing
                if (graceCard.getAdapter() == null)
                    return;

                final String cardText = graceCard.getText();
                if (cardText.equals(CAMERA)) {
                    takePicture();
                } else if (cardText.equals(VIDEO)) {
                    recordVideo();
                }

                // kill "old" slider and replace with a new one for our new hierarchy
                mCurrentSlider.stop();
                mCurrentSlider = new Slider(mCurrentAdapter.getCount());

                // switch out cards being displayed
                mCurrentAdapter = graceCard.getAdapter();
                mCardScrollView.setAdapter(mCurrentAdapter);

                // visual switch of hierarchy and start our slider
                mCurrentAdapter.notifyDataSetChanged();
                mCurrentSlider.run();
            }
        });

        setContentView(mCardScrollView);
        mCurrentSlider.run();
    }

    // create cards for each hierarchy, add to that's hierarchies adapter
    void buildScrollers() {
        mBaseCardsAdapter = new GraceCardScrollerAdapter();
        mBaseCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, MEDIA));
        mBaseCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, COMM));
        mBaseCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, GAMES));

        mMediaCardsAdapter = new GraceCardScrollerAdapter();
        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mPostMediaCardsAdapter, CAMERA));
        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mPostMediaCardsAdapter, VIDEO));
        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, BACK));

        mPostMediaCardsAdapter = new GraceCardScrollerAdapter();
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, REDO));
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, SAVE));
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, SEND));
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, BACK));

        /* **** below needs to be implemented still */

        // communication hierarchy
        mCommContactsAdapter = new GraceCardScrollerAdapter();
        mCommMessagesAdapter = new GraceCardScrollerAdapter();

        // game hierarchy
        mGameCardsAdapter = new GraceCardScrollerAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "On Destroy");

        if(mbtService != null) {
            // *** FIX mbtService.stopThreads();
        }
    }

    // this class is responsible for the right-to-left & left-to-right "sliding" of the cards
    private class Slider implements Runnable {

        int mCurrPosition;
        int mFinalPosition;
        boolean swipeRight;
        boolean swipeLeft;
        boolean stop;

        public Slider(int numCards) {
            mCurrPosition = 0;
            mFinalPosition = numCards - 1;
            swipeRight = true;
            swipeLeft = false;
            stop = false;
        }

        @Override
        public void run() {

            stop = false;

            if(!stop) {

                if(swipeRight) {
                    mGestures.createGesture(Gestures.TYPE_SWIPE_RIGHT);
                    mCurrPosition++;
                } else if(swipeLeft) {
                    mGestures.createGesture(Gestures.TYPE_SWIPE_LEFT);
                    mCurrPosition--;
                }

                if(mCurrPosition == 0) {
                    swipeRight = true;
                    swipeLeft = false;
                } else if(mCurrPosition == mFinalPosition) {
                    swipeLeft = true;
                    swipeRight = false;
                }

                mHandler.postDelayed(this, 3000);
            }
        }

        public void stop() {
            stop = true;
        }
    }

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
            // mbtService = new BluetoothService(mHandler); // set up bluetooth service
            mbtService = new BluetoothService(); // set up bluetooth service
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // shouldn't be called, because Bluetooth should be enabled on Glass

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
                insertScreenshotIntoPostMedia(screenshot, imageLocation);
                break;
            }
            case VIDEO_REQ:
            {
                // fetch media URI, get screenshot from video, create new card and add to beginning of PostMedia adapter
                Uri videoLocation = intent.getData();
                Bitmap screenshot = (Bitmap) ThumbnailUtils.createVideoThumbnail(videoLocation.toString(), MediaStore.Images.Thumbnails.MINI_KIND);
                insertScreenshotIntoPostMedia(screenshot, videoLocation);
                break;
            }
            default:
        }
    }

    // Utility functions

    private void insertScreenshotIntoPostMedia(Bitmap screenshot, Uri mediaLocation) {
        mMedia.addMedia(screenshot, mediaLocation);
        GraceCard screenshotCard = new GraceCard(this, null, "");
        screenshotCard.addImage(mediaLocation);
        mPostMediaCardsAdapter.pushCardFront(screenshotCard);
        mPostMediaCardsAdapter.notifyDataSetChanged();
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(takePictureIntent, CAMERA_REQ);
        }
        else
            Toast.makeText(this, "Could not resolve image capture activity!", Toast.LENGTH_SHORT).show();
    }

    private void recordVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, VIDEO_REQ);
        }
        else
            Toast.makeText(this, "Could not resolve video capture activity!", Toast.LENGTH_SHORT).show();
    }
}
