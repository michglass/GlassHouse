package com.michglass.glasshouse.glasshouse;

/**
 * Created by Vijay Ganesh
 * Date: 2/26/14
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.media.CameraManager;
import com.google.android.glass.widget.CardScrollView;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends Activity {

    // Debug
    private static final String TAG = "Main Activity";
    public Context context = this;


    // Messages from BT
    private static final int CAMERA_REQ = 5;
    private static final int VIDEO_REQ = 6;

    // BT Variables
    private Messenger mBluetoothServiceMessenger;
    private boolean mBound;
    private final Messenger clientMessenger = new Messenger(new ServiceHandler());


    // UI Variables
    private GraceCardScrollView mCardScrollView;
    private GraceCard lastSelectedCard = new GraceCard(this, null, "blah", GraceCardType.NONE);

    private Gestures mCurrGestures = new Gestures();
    private Map<String, String> mContacts;

    private GraceCardScrollerAdapter mBaseCardsAdapter;
    private GraceCardScrollerAdapter mMediaCardsAdapter;
    private GraceCardScrollerAdapter mPostMediaCardsAdapter;
    private GraceCardScrollerAdapter mCommContactsAdapter;
    private GraceCardScrollerAdapter mCommMessagesAdapter;
    private GraceCardScrollerAdapter mGameCardsAdapter;
    private GraceCardScrollerAdapter mCurrentAdapter;

    /**for bluetooth messaging*/
    private static BluetoothSMS bluetoothMessage = new BluetoothSMS();

    /**
     * Activity Lifecycle Methods
     * On Create: Start BT(only in one Activity)
     * On Start: Bind to BT
     * On Stop: Unbind from BT
     * On Destroy: Stop BT(only in one Activity)
     */

    AdapterView.OnItemClickListener ScrollerListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.v(TAG, "On Item Click");

            GraceCard graceCard = (GraceCard) mCurrentAdapter.getItem(position);
            lastSelectedCard = graceCard;
            Log.v(TAG, graceCard.getText());

            // null adapter means this card has no tap event so do nothing
            if (graceCard.getNextAdapter() == null) {
                Log.v(TAG, "Card Scroll Adapter NULL");
                return;
            }

            // set the next adapter and stop the current slider
            //mNextAdapter = graceCard.getNextAdapter();
            mCurrentAdapter.getSlider().stopSlider();

            // long ass switch for cards that have functions
            final String cardText = graceCard.getText();
            if (graceCard.getGraceCardType() == GraceCardType.CAMERA) {
                takePicture();
                return;
            } else if (graceCard.getGraceCardType() == GraceCardType.VIDEO) {
                recordVideo();
                return;
            } else if (graceCard.getGraceCardType() == GraceCardType.REDO) {
                // remove screenshot from post media menu cards
                mPostMediaCardsAdapter.popCardFront();
            } else if (graceCard.getGraceCardType() == GraceCardType.SAVE) {
                // save to disk, or whatever
            } else if (graceCard.getGraceCardType() == GraceCardType.SEND) {
                // launch contacts picker, send media to phone or whatever
            } else if(graceCard.getGraceCardType() == GraceCardType.CONTACT) {
                GraceContactCard contact = (GraceContactCard) graceCard;
                bluetoothMessage.setNum(contact.phoneNumber);
            }
            else if(graceCard.getGraceCardType() == GraceCardType.MESSAGE) {
                bluetoothMessage.setMessage(graceCard.getText());
                sendMessageToService(BluetoothService.TEXT_MESSAGE, bluetoothMessage.buildBluetoothSMS());
                Log.v(TAG, bluetoothMessage.buildBluetoothSMS());
            }
            else if(graceCard.getGraceCardType() == GraceCardType.TICTACTOE){
                // Launch Tic-Tac-Toe Activity
                Intent intent = new Intent(context, TicTacToeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                return;
            }
            else if(graceCard.getGraceCardType() == GraceCardType.EXIT){
                finish();
            }
            switchHierarchy(graceCard.getNextAdapter());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "On Create");

        // Start the BT Service
        startService(new Intent(this, BluetoothService.class));

        // initialize all hierarchy adapters and add cards to them
        buildScrollers();



        mCurrentAdapter = mBaseCardsAdapter;
        mCardScrollView = new GraceCardScrollView(this, ScrollerListener);
        mCardScrollView.setAdapter(mCurrentAdapter);
        mCardScrollView.activate(); // makes it work


        // mContacts = new TreeMap<String, String>();
      //  mCurrGestures = new Gestures();
      //  mCurrentSlider = new Slider(mBaseCardsAdapter.getCount());
        //Log.v(TAG, "Scroll View Size: " + mCardScrollView.getCount());
        Log.v(TAG, "Adapter Size: " + mBaseCardsAdapter.getCount());

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // start up the base menu cards and its slider
        setContentView(mCardScrollView);
        mBaseCardsAdapter.getSlider().start();

        // grab contacts
        new AsyncTask<Void, Void, Void> () {

            @Override
            protected Void doInBackground(Void... params) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
                while (phones.moveToNext())
                {
                    final String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    final String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Log.e(TAG, "contact retrieved: " + name + " with number " + number);
                    mContacts.put(name, number);
                }
                return null;
            }
        }.execute();
    }

    // sets current adapter to next menus adapter and sets current view to it and starts its respective slider
    private void switchHierarchy(GraceCardScrollerAdapter nextAdapter) {
        if(nextAdapter.equals(null)){
            return;
        }
        mCurrentAdapter = nextAdapter;

        // replace slider with a new one for our new hierarchy
        //mCurrGestures = new Gestures();
        //mCurrentSlider = new Slider(mCurrentAdapter.getCount());

        // switch out cards being displayed
        mCardScrollView.setAdapter(mCurrentAdapter);

        mCardScrollView.setSelection(0);
        mCurrentAdapter.notifyDataSetChanged();
        mCardScrollView.updateViews(false);
        mCardScrollView.activate();
        mCurrentAdapter.setSlider(new Slider(new Gestures()));
        mCurrentAdapter.getSlider().setNumCards(mCurrentAdapter.getCount());
        mCurrentAdapter.getSlider().start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "On Start");

        // Bind this Activity to the BT Service
        if(!mBound) {
            //TODO for Tic
            bindService(new Intent(this, BluetoothService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        Log.v(TAG, "On Pause");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "On Resume");
        if(!lastSelectedCard.equals(null) &&
           lastSelectedCard.getGraceCardType().equals(GraceCardType.TICTACTOE)){
            switchHierarchy(lastSelectedCard.getNextAdapter());
        }
    }


    @Override
    public void onStop() {
        Log.v(TAG, "On Stop");

        // Stop Injecting
        mCurrentAdapter.getSlider().getGestures().stopInjecting();
        mCurrentAdapter.getSlider().stopSlider();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "On Destroy");

        // Unbind from BT Service
        if(mBound) {
            //TODO For tic tac
            sendMessageToService(BluetoothService.INT_MESSAGE, BluetoothService.UNREGISTER_CLIENT);
            unbindService(mConnection);
            mBound = false;
        }
        // Stop the BT Service only in the component that calls startservice() !!
        stopService(new Intent(this, BluetoothService.class));
    }

    // create cards for each hierarchy, add to that's hierarchies adapter
            private void buildScrollers() {
                Log.v(TAG, "private void buildScrollers() called");
                mBaseCardsAdapter = new GraceCardScrollerAdapter(new GraceCardScrollView(this, ScrollerListener), new Slider(new Gestures()));
                mMediaCardsAdapter = new GraceCardScrollerAdapter(new GraceCardScrollView(this, ScrollerListener), new Slider(new Gestures()));
                mPostMediaCardsAdapter = new GraceCardScrollerAdapter(new GraceCardScrollView(this, ScrollerListener), new Slider(new Gestures()));
                mCommContactsAdapter = new GraceCardScrollerAdapter(new GraceCardScrollView(this, ScrollerListener), new Slider(new Gestures()));
                mCommMessagesAdapter = new GraceCardScrollerAdapter(new GraceCardScrollView(this, ScrollerListener), new Slider(new Gestures()));
                mGameCardsAdapter = new GraceCardScrollerAdapter(new GraceCardScrollView(this, ScrollerListener), new Slider(new Gestures()));

                // mBaseCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, "Take a Picture or Record a Video", GraceCardType.MEDIA)); leaving this out of beta, can't inject taps into media capture application
                mBaseCardsAdapter.pushCardBack(new GraceCard(this, mCommContactsAdapter, "Send a Message", GraceCardType.COMM));
                mBaseCardsAdapter.pushCardBack(new GraceCard(this, mGameCardsAdapter, "Play a Game", GraceCardType.GAMES));
                mBaseCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Exit Application", GraceCardType.EXIT));
                mBaseCardsAdapter.getSlider().setNumCards(mBaseCardsAdapter.getCount());

                mMediaCardsAdapter.pushCardBack(new GraceCard(this, mPostMediaCardsAdapter, "Take a Picture", GraceCardType.CAMERA));
                mMediaCardsAdapter.pushCardBack(new GraceCard(this, mPostMediaCardsAdapter, "Record a Video", GraceCardType.VIDEO));
                mMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Back", GraceCardType.BACK));
                mMediaCardsAdapter.getSlider().setNumCards(mMediaCardsAdapter.getCount());

                mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, "Redo", GraceCardType.REDO));
                mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Save Media", GraceCardType.SAVE)); // loop back to main menu for now
                mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Send Media",GraceCardType.SEND)); // loop back to main menu for now
                mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Back", GraceCardType.BACK));
                mPostMediaCardsAdapter.getSlider().setNumCards(mPostMediaCardsAdapter.getCount());
                Log.v(TAG, "Post Media Adapter Built");

                /* **** below needs to be implemented still */

                // communication hierarchy
                GraceContactCard.addCard(this, mCommMessagesAdapter, "Mom", "7346459032", GraceCardType.CONTACT);
                Log.v(TAG, "Tim Wood contact added to adapter");
                GraceContactCard.addCard(this, mCommMessagesAdapter, "Dad", "7346459032", GraceCardType.CONTACT);
                GraceContactCard.addCard(this, mCommMessagesAdapter, "Tim Wood", "7346459032", GraceCardType.CONTACT);
                GraceContactCard.addCard(this, mCommMessagesAdapter, "Danny Francken", "7346459032", GraceCardType.CONTACT);
                Log.v(TAG, "Right before loop to add contacts to adapter" + GraceContactCard.contactList.size());
                for(GraceContactCard C: GraceContactCard.contactList){
                    mCommContactsAdapter.pushCardBack(C);
                    Log.v(TAG, "Contact added to Adapter. Name: " + C.Name);
                }
                mCommContactsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Return to Main Menu", GraceCardType.BACK));
                mCommContactsAdapter.getSlider().setNumCards(mCommContactsAdapter.getCount());

                GraceMessageCard.addCard(this, mBaseCardsAdapter, "I love you.", GraceCardType.MESSAGE);
                GraceMessageCard.addCard(this, mBaseCardsAdapter, "Can we go to Disney World sometime?", GraceCardType.MESSAGE);
                GraceMessageCard.addCard(this, mBaseCardsAdapter, "Could you help me with something?", GraceCardType.MESSAGE);
                GraceMessageCard.addCard(this, mBaseCardsAdapter, "What's for dinner?", GraceCardType.MESSAGE);
                for(GraceMessageCard M: GraceMessageCard.messageList){
                    mCommMessagesAdapter.pushCardBack(M);
                    Log.v(TAG, "Message added to Adapter: " + M.Message);
                }
                mCommMessagesAdapter.pushCardBack(new GraceCard(this, mCommContactsAdapter, "Back to Contact List", GraceCardType.BACK));
                mCommMessagesAdapter.getSlider().setNumCards(mCommMessagesAdapter.getCount());

                mGameCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Tic-Tac-Toe", GraceCardType.TICTACTOE));
                mGameCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Return to Main Menu", GraceCardType.BACK));


                Log.v(TAG, "Exiting buildScrollers()");
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

        switch (requestCode) {

            case CAMERA_REQ:
            {
                final String picturePath = intent.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
                // for now this is not gonna work
                // processPicture.execute(picturePath);
                switchHierarchy(null);
                break;
            }
            case VIDEO_REQ:
            {
                // Video video = (Video) intent.getExtras().get(CameraManager.EXTRA_VIDEO_FILE_PATH);
                switchHierarchy(null);
                break;
            }
            default:
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Utility functions

    private AsyncTask<String, Void, Uri> processPicture = new AsyncTask<String, Void, Uri>() {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Processing...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected Uri doInBackground(String... params) {
            Uri result = null;
            while ((result = processPictureWhenReady(params[0])) == null)
                ;

            return result;
        }

        @Override
        protected void onPostExecute (Uri picture) {
            pd.dismiss();
            Log.e(TAG, "picture path on onPost = " + picture.toString());
            insertScreenshotIntoPostMediaMenu(picture);
        }
    };

    private Uri processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            Log.e(TAG, "picture file done, path = " + picturePath.toString());
            return buildFileUri(picturePath.toString());
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath()) {
                // Protect against additional pending events after CLOSE_WRITE is
                // handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = (event == FileObserver.CLOSE_WRITE
                                && affectedFile.equals(pictureFile));

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
        return null;
    }

    private void insertScreenshotIntoPostMediaMenu(Uri mediaLocation) {
        GraceCard screenshotCard = new GraceCard(this, null, "", GraceCardType.SCREENSHOT);
        screenshotCard.addImage(mediaLocation);
        mPostMediaCardsAdapter.pushCardFront(screenshotCard);
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQ);
    }

    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_REQ);
    }

    private Uri buildFileUri(String pathToFile) {
        return Uri.parse("file://" + pathToFile);
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
    //TODO Copy and Paste this into TCT activity
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
                            //TODO Start sliding only here! User shouldn't be able to interact with
                            //TODO UI before
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
                    mCurrentAdapter.getSlider().getGestures().createGesture(Gestures.TYPE_TAP);
                    break;
                case BluetoothService.COMMAND_BACK:
                    Log.v(TAG, "Command back");
                    break;
                case BluetoothService.ANDROID_STOPPED:
                    Log.v(TAG, "Android App closed");
                    sendMessageToService(BluetoothService.INT_MESSAGE, BluetoothService.MESSAGE_RESTART);
                    //TODO Glass starts listening to incoming requests again, do sth to UI
                    //TODO Splash Screen or similar, to stop client from interaction with UI
                    break;
            }
        }
    }
}
