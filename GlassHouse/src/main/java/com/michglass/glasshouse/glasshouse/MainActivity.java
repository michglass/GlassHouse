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

import com.google.android.glass.app.Card;
import com.google.android.glass.media.CameraManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

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
    private int mBluetoothState = BluetoothService.STATE_NONE;


    // UI Variables
    private Map<String, String> mContacts;
    private GraceCardScrollView mCardScrollView;
    private GraceCard lastSelectedCard;
    private MenuHierarchy menuHierarchy;
    GraceCardScrollerAdapter mBaseCardsAdapter;
    GraceCardScrollerAdapter mMediaCardsAdapter;
    GraceCardScrollerAdapter mPostMediaCardsAdapter;
    GraceCardScrollerAdapter mCommContactsAdapter;
    GraceCardScrollerAdapter mCommMessagesAdapter;
    GraceCardScrollerAdapter mGameCardsAdapter;
    GraceCardScrollerAdapter mTutorialAdapter;
    GraceCardScrollerAdapter mCommContactInterstitialAdapter;
    GraceCardScrollerAdapter mWelcomeSplashScreenAdapter;
    GraceCardScrollerAdapter mCommMessagesInterstitialAdapter;
    GraceCardScrollerAdapter mMessageSentAdapter;
    GraceCardScrollerAdapter mMessageNotSentAdapter;

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

            GraceCard graceCard = (GraceCard) menuHierarchy.getCurrentAdapter().getItem(position);
            lastSelectedCard = graceCard;
            Log.v(TAG, (String) graceCard.getText());

            // null adapter means this card has no tap event so do nothing
            if (graceCard.getNextAdapter() == null && graceCard.getGraceCardType() != GraceCardType.EXIT){
                Log.v(TAG, "Card Scroll Adapter NULL");
                return;
            }

            // long ass switch for cards that have functions
            final String cardText = (String) graceCard.getText();
            if (graceCard.getGraceCardType() == GraceCardType.CAMERA) {
                takePicture();
                return;

            } else if (graceCard.getGraceCardType() == GraceCardType.WELCOME){
                menuHierarchy.crossfade(graceCard.getNextAdapter());

            } else if (graceCard.getGraceCardType() == GraceCardType.COMM){

                // crossfade to interstitial contact adapter
                menuHierarchy.crossfade(graceCard.getNextAdapter());

                menuHierarchy.crossfade(2, ((GraceCard) menuHierarchy.getCurrentAdapter().getItem(0)).getNextAdapter());

            } else if (graceCard.getGraceCardType() == GraceCardType.GAMES){
                menuHierarchy.crossfade(graceCard.getNextAdapter());

            } else if (graceCard.getGraceCardType() == GraceCardType.VIDEO) {
                recordVideo();
                return;

            } else if (graceCard.getGraceCardType() == GraceCardType.REDO) {
                // remove screenshot from post media menu cards
            } else if (graceCard.getGraceCardType() == GraceCardType.SAVE) {
                // save to disk, or whatever
            } else if (graceCard.getGraceCardType() == GraceCardType.SEND) {
                // launch contacts picker, send media to phone or whatever
            } else if(graceCard.getGraceCardType() == GraceCardType.CONTACT) {
                GraceContactCard contact = (GraceContactCard) graceCard;
                bluetoothMessage.setNum(contact.phoneNumber);

                menuHierarchy.crossfade(graceCard.getNextAdapter());
                menuHierarchy.crossfade(2, ((GraceCard) menuHierarchy.getCurrentAdapter().getItem(0)).getNextAdapter());

            }
            else if(graceCard.getGraceCardType() == GraceCardType.MESSAGE) {
                //TODO Send JSON OBJ to Android
                //bluetoothMessage.setMessage((String) graceCard.getText());
                //sendMessageToService(BluetoothService.TEXT_MESSAGE, bluetoothMessage.buildBluetoothSMS());
                Log.v(TAG, bluetoothMessage.buildBluetoothSMS());

                if(mBluetoothState == BluetoothService.STATE_CONNECTED){
                    menuHierarchy.crossfade(mMessageSentAdapter);
                }
                else{
                    menuHierarchy.crossfade(mMessageNotSentAdapter);
                }
                menuHierarchy.crossfade(2, mBaseCardsAdapter);
            }
            else if (graceCard.getGraceCardType() == GraceCardType.TUTORIAL){
                menuHierarchy.crossfade(graceCard.getNextAdapter());
            }
            else if(graceCard.getGraceCardType() == GraceCardType.TICTACTOE){
                // Launch Tic-Tac-Toe Activity
                Intent intent = new Intent(context, TicTacToeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                return;
            } else if (graceCard.getGraceCardType() == GraceCardType.SPELLING){
                Intent intent = new Intent(context, SpellingMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                return;
            } else if (graceCard.getGraceCardType() == GraceCardType.BACK){
                menuHierarchy.crossfade(graceCard.getNextAdapter());
            } else if (graceCard.getGraceCardType() == GraceCardType.MEDIA) {
                Intent camIntent = new Intent(context, CameraMenuActivity.class);
                camIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(camIntent);
            }
            else if(graceCard.getGraceCardType() == GraceCardType.EXIT){
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "On Create");

        // initialize all hierarchy adapters and add cards to them
        buildMenuHierarchy();
        lastSelectedCard = null;

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


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

    public void onStart() {
        super.onStart();
        Log.v(TAG, "On Start");

        // Bind this Activity to the BT Service
        if(!mBound) {
            bindService(new Intent(this, BluetoothService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }

        // activate and go!
        mCardScrollView.activate();
        setContentView(mCardScrollView);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                @Override
            public void run() {
                Log.v(TAG, "handler running");
                if(!menuHierarchy.getCurrentAdapter().equals(mWelcomeSplashScreenAdapter)){
                    Log.v(TAG, "handler -- not on welcome screen");
                    return;
                }
                if(mBluetoothState != BluetoothService.STATE_CONNECTED){
                    Log.v(TAG, "bluetooth not connected");
                    // TODO animate this
                    ((GraceCard) menuHierarchy.getCurrentAdapter().getItem(0))
                            .setText(R.string.welcome_connecting);
                    ((GraceCard) menuHierarchy.getCurrentAdapter().getItem(0))
                            .setFootnote(R.string.welcome_connecting_footnote);

                    mWelcomeSplashScreenAdapter.notifyDataSetChanged();


                }

            }
        }, 2000);
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
        if(!(lastSelectedCard == null) &&
           (lastSelectedCard.getGraceCardType().equals(GraceCardType.TICTACTOE) ||
            lastSelectedCard.getGraceCardType().equals(GraceCardType.SPELLING))){
           menuHierarchy.crossfade(lastSelectedCard.getNextAdapter());
        }
    }


    @Override
    public void onStop() {
        Log.v(TAG, "On Stop");

        // Unbind from BT Service
        if(mBound) {
            sendMessageToService(BluetoothService.UNREGISTER_CLIENT);
            unbindService(mConnection);
            mBound = false;
        }

        // Stop Injecting
        menuHierarchy.getSlider().getGestures().stopInjecting();
        menuHierarchy.getSlider().stopSlider();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "On Destroy");

        // Stop the BT Service only in the component that calls startservice() !!
        stopService(new Intent(this, BluetoothService.class));
    }

    // create cards for each hierarchy, add to that's hierarchies adapter
    private void buildMenuHierarchy() {
        Log.v(TAG, "private void buildMenuHierarchy() called");
        GraceCard placeHolder;


        mBaseCardsAdapter = new GraceCardScrollerAdapter();
        mMediaCardsAdapter = new GraceCardScrollerAdapter();
        mPostMediaCardsAdapter = new GraceCardScrollerAdapter();
        mCommContactsAdapter = new GraceCardScrollerAdapter();
        mCommMessagesAdapter = new GraceCardScrollerAdapter();
        mGameCardsAdapter = new GraceCardScrollerAdapter();
        mTutorialAdapter = new GraceCardScrollerAdapter();
        mCommContactInterstitialAdapter = new GraceCardScrollerAdapter();
        mWelcomeSplashScreenAdapter = new GraceCardScrollerAdapter();
        mCommMessagesInterstitialAdapter = new GraceCardScrollerAdapter();
        mMessageSentAdapter = new GraceCardScrollerAdapter();
        mMessageNotSentAdapter = new GraceCardScrollerAdapter();

        mCardScrollView = new GraceCardScrollView(this, ScrollerListener);
        menuHierarchy = new MenuHierarchy(mCardScrollView, mWelcomeSplashScreenAdapter, getResources().getInteger(
                android.R.integer.config_longAnimTime));

        // mBaseCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, "Take a Picture or Record a Video", GraceCardType.MEDIA)); leaving this out of beta, can't inject taps into media capture application
        placeHolder = new GraceCard(this, mCommContactInterstitialAdapter, "", GraceCardType.COMM);
        placeHolder.addImage(R.drawable.main_message).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mGameCardsAdapter, "", GraceCardType.GAMES);
        placeHolder.addImage(R.drawable.main_games).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        //TODO check if correct
        placeHolder = new GraceCard(this, new GraceCardScrollerAdapter(), "Media", GraceCardType.MEDIA);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mTutorialAdapter, "", GraceCardType.TUTORIAL);
        placeHolder.addImage(R.drawable.main_tutorial).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, null, "", GraceCardType.EXIT);
        placeHolder.addImage(R.drawable.main_exit).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mPostMediaCardsAdapter, "Take a Picture", GraceCardType.CAMERA));
        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mPostMediaCardsAdapter, "Record a Video", GraceCardType.VIDEO));
        mMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Back", GraceCardType.BACK));

        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, "Redo", GraceCardType.REDO));
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Save Media", GraceCardType.SAVE)); // loop back to main menu for now
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Send Media",GraceCardType.SEND)); // loop back to main menu for now
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Back", GraceCardType.BACK));
        Log.v(TAG, "Post Media Adapter Built");


        // communication adapters
        placeHolder = new GraceCard(this, mCommContactsAdapter, "", GraceCardType.NONE);
        placeHolder.addImage(R.drawable.messages_contact_interstitial).setImageLayout(Card.ImageLayout.FULL);
        mCommContactInterstitialAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mCommMessagesAdapter, "", GraceCardType.NONE);
        placeHolder.addImage(R.drawable.messages_message_interstitial).setImageLayout(Card.ImageLayout.FULL);
        mCommMessagesInterstitialAdapter.pushCardBack(placeHolder);

        GraceContactCard.addCard(this, mCommMessagesInterstitialAdapter, "Mom", "7346459032", GraceCardType.CONTACT);
        Log.v(TAG, "Tim Wood contact added to adapter");
        GraceContactCard.addCard(this, mCommMessagesInterstitialAdapter, "Dad", "7346459032", GraceCardType.CONTACT);
        GraceContactCard.addCard(this, mCommMessagesInterstitialAdapter, "Tim Wood", "7346459032", GraceCardType.CONTACT);
        GraceContactCard.addCard(this, mCommMessagesInterstitialAdapter, "Danny Francken", "7346459032", GraceCardType.CONTACT);
        Log.v(TAG, "Right before loop to add contacts to adapter" + GraceContactCard.contactList.size());
        for(GraceContactCard C: GraceContactCard.contactList){
            mCommContactsAdapter.pushCardBack(C);
            Log.v(TAG, "Contact added to Adapter. Name: " + C.Name);
        }
        mCommContactsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Return to Main Menu", GraceCardType.BACK));

        // messages adapter
        GraceMessageCard.addCard(this, mMessageSentAdapter, "I love you.", GraceCardType.MESSAGE);
        GraceMessageCard.addCard(this, mMessageSentAdapter, "Can we go to Disney World sometime?", GraceCardType.MESSAGE);
        GraceMessageCard.addCard(this, mMessageSentAdapter, "Could you help me with something?", GraceCardType.MESSAGE);
        GraceMessageCard.addCard(this, mMessageSentAdapter, "What's for dinner?", GraceCardType.MESSAGE);
        for(GraceMessageCard M: GraceMessageCard.messageList){
            mCommMessagesAdapter.pushCardBack(M);
            Log.v(TAG, "Message added to Adapter: " + M.Message);
        }
        mCommMessagesAdapter.pushCardBack(new GraceCard(this, mCommContactsAdapter, "Return to Contact List", GraceCardType.BACK));

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "Message Sent!", GraceCardType.BACK);
        mMessageSentAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "Message failed to send.", GraceCardType.BACK);
        mMessageNotSentAdapter.pushCardBack(placeHolder);



        // game cards adapter
        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.TICTACTOE);
        placeHolder.addImage(R.drawable.games_tictactoe).setImageLayout(Card.ImageLayout.FULL);
        mGameCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.SPELLING);
        placeHolder.addImage(R.drawable.games_spell).setImageLayout(Card.ImageLayout.FULL);
        mGameCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.BACK);
        placeHolder.addImage(R.drawable.games_back).setImageLayout(Card.ImageLayout.FULL);
        mGameCardsAdapter.pushCardBack(placeHolder);

        // Tutorial Adapter

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "Menu items automatically change every few seconds.", GraceCardType.NONE);
        placeHolder.addImage(R.drawable.tutorial_side_1).setImageLayout(Card.ImageLayout.LEFT);
        mTutorialAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "Press 'OK' on your phone to choose an item.", GraceCardType.BACK);
        placeHolder.addImage(R.drawable.tutorial_side_2).setImageLayout(Card.ImageLayout.LEFT);
        placeHolder.setFootnote("Tap 'OK' to go back.");
        mTutorialAdapter.pushCardBack(placeHolder);

        // welcome Adapter
        placeHolder = new GraceCard(this, mBaseCardsAdapter, getString(R.string.welcome), GraceCardType.WELCOME);
        placeHolder.addImage(R.drawable.welcome_side).setImageLayout(Card.ImageLayout.LEFT);
        mWelcomeSplashScreenAdapter.pushCardBack(placeHolder);

        // add adapters to menuHierarchy
        menuHierarchy.addAdapter(mBaseCardsAdapter);
        menuHierarchy.addAdapter(mTutorialAdapter);
        menuHierarchy.addAdapter(mCommMessagesAdapter);
        menuHierarchy.addAdapter(mCommContactsAdapter);
        menuHierarchy.addAdapter(mGameCardsAdapter);
        menuHierarchy.addAdapter(mCommContactInterstitialAdapter);
        menuHierarchy.addAdapter(mCommMessagesInterstitialAdapter);
        menuHierarchy.addAdapter(mMessageSentAdapter);
        menuHierarchy.addAdapter(mMessageNotSentAdapter);

        Log.v(TAG, "Exiting buildMenuHierarchy()");
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
                break;
            }
            case VIDEO_REQ:
            {
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
        // mPostMediaCardsAdapter.pushCardFront(screenshotCard);
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
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.v(TAG, "On Service Connect");

            // set up messenger
            mBluetoothServiceMessenger = new Messenger(iBinder);
            mBound = true;

            // Send a first message to the service
            setUpMessage();

            // start service, only if not already connected
            if(mBluetoothState != BluetoothService.STATE_CONNECTED) {
                startBTService();
            }
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
     * Start the Bluetooth Service
     * Called in Service Connected Callback after Activity binds to Service
     * Start Service only when not already connected
     */
    private void startBTService() {
        // Start the BT Service
        startService(new Intent(this, BluetoothService.class));
    }
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
                    mBluetoothState = msg.arg1;
                    Log.v(TAG, "connection state changed");
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.v(TAG, "state connected");
                            GraceCard placeHolder;
                            if(menuHierarchy.getCurrentAdapter().equals(mWelcomeSplashScreenAdapter)){
                                placeHolder = (GraceCard) menuHierarchy.getCurrentAdapter().getItem(0);
                                if(placeHolder.getText().toString().equals(getString(R.string.welcome_connecting))){
                                    placeHolder.setText(R.string.welcome_connected);
                                    placeHolder.setFootnote(R.string.welcome_connected_footnote);
                                }
                                mWelcomeSplashScreenAdapter.notifyDataSetChanged();
                            }
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
                case BluetoothService.ANDROID_MESSAGE:
                    Log.v(TAG, "Android Message: " + (String)msg.obj);
                    //TODO Rodney: Do sth with string (json string)
                    break;
                case BluetoothService.COMMAND_OK:
                    Log.v(TAG, "Command ok");
                    // Inject a Tap event
                    menuHierarchy.getSlider().getGestures().createGesture(Gestures.TYPE_TAP);
                    break;
                case BluetoothService.ANDROID_STOPPED:
                    Log.v(TAG, "Android App closed");
                    sendMessageToService(BluetoothService.MESSAGE_RESTART);
                    //sendMessageToService(BluetoothService.INT_MESSAGE, BluetoothService.MESSAGE_RESTART);
                    //TODO Glass starts listening to incoming requests again, do sth to UI
                    //TODO Splash Screen or similar, to stop client from interaction with UI
                    break;
            }
        }
    }
}
