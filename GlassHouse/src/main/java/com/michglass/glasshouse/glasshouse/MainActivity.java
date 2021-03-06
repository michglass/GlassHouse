package com.michglass.glasshouse.glasshouse;

/**
 * Created by Vijay Ganesh
 * Date: 2/26/14
 */
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    // Debug
    private static final String TAG = "MainActivity";
    public Context context = this;

    public static final String SCROLL_SPEED_KEY = "SCROLL_SPEED";
    public static final String NUM_CONTACTS_KEY = "NUM_CONTACTS";
    public static final String NUM_MESSAGES_KEY = "NUM_MESSAGES";

    private boolean sendAsEmail = false;
    private boolean contactsLoaded = false;
    // BT Variables
    private Messenger mBluetoothServiceMessenger;
    private boolean mBound;
    private final Messenger clientMessenger = new Messenger(new ServiceHandler());
    private int mBluetoothState = BluetoothService.STATE_NONE;

    // UI Variables
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
    GraceCardScrollerAdapter mMessageTypeAdapter;

    /**for bluetooth messaging*/
    private static BluetoothSMS bluetoothMessage = new BluetoothSMS();
    private org.json.JSONObject current_message;

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
                menuHierarchy.getSlider().stopSlider();
                Intent picInt = new Intent(context, PictureActivity.class);
                startActivity(picInt);

            } else if (graceCard.getGraceCardType() == GraceCardType.WELCOME){
                menuHierarchy.crossfade(graceCard.getNextAdapter());

            } else if (graceCard.getGraceCardType() == GraceCardType.COMM){

                // crossfade to interstitial contact adapter
                current_message = new JSONObject();
                menuHierarchy.crossfade(graceCard.getNextAdapter());

                //menuHierarchy.crossfade(2, ((GraceCard) menuHierarchy.getCurrentAdapter().getItem(0)).getNextAdapter());

            } else if (graceCard.getGraceCardType() == GraceCardType.GAMES){
                menuHierarchy.crossfade(graceCard.getNextAdapter());

            } else if (graceCard.getGraceCardType() == GraceCardType.VIDEO) {
                return;

            } else if (graceCard.getGraceCardType() == GraceCardType.REDO) {
                // remove screenshot from post media menu cards
            } else if (graceCard.getGraceCardType() == GraceCardType.SAVE) {
                // save to disk, or whatever
            } else if (graceCard.getGraceCardType() == GraceCardType.SEND) {
                // launch contacts picker, send media to phone or whatever
            } else if(graceCard.getGraceCardType() == GraceCardType.CONTACT) {
                GraceContactCard contact = (GraceContactCard) graceCard;
                //bluetoothMessage.setNum(contact.phoneNumber);
                try {
                    current_message.put("name", contact.getName());
                    if(current_message.getString("type").equals("email")){
                        current_message.put("emailAddress", contact.getEmailAddress());
                    }
                    else if(current_message.getString("type").equals("text")){
                        current_message.put("number", contact.getPhoneNumber());
                    }
                }
                catch(JSONException j){
                    Log.e(TAG, j.toString());
                }
                menuHierarchy.crossfade(graceCard.getNextAdapter());
                menuHierarchy.crossfade(2, mCommMessagesAdapter);

            }
            else if(graceCard.getGraceCardType() == GraceCardType.SEND_AS_EMAIL){
                Log.v(TAG, "Sending As Email");
                try {
                    current_message.put("type", "email");
                }
                catch(JSONException j){
                    Log.e(TAG, j.toString());
                }
                menuHierarchy.crossfade(graceCard.getNextAdapter());
                menuHierarchy.crossfade(2, mCommContactsAdapter);
            }
            else if(graceCard.getGraceCardType() == GraceCardType.SEND_AS_TEXT){
                try {
                    current_message.put("type", "text");
                }
                catch(JSONException j){
                    Log.e(TAG, j.toString());
                }
                menuHierarchy.crossfade(graceCard.getNextAdapter());
                menuHierarchy.crossfade(2, mCommContactsAdapter);
            }
            else if(graceCard.getGraceCardType() == GraceCardType.MESSAGE) {
                //TODO Send JSON OBJ to Android
                //bluetoothMessage.setMessage((String) graceCard.getText());
                //sendMessageToService(BluetoothService.TEXT_MESSAGE, bluetoothMessage.buildBluetoothSMS());
                GraceMessageCard Message = (GraceMessageCard) graceCard;
                Log.v(TAG, bluetoothMessage.buildBluetoothSMS());
                try {
                    current_message.put("message", Message.getMessage());
                    sendToAndroid(current_message);
                }
                catch(JSONException j){
                    Log.e(TAG, j.toString());
                }

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
                menuHierarchy.getSlider().stopSlider();
                Intent intent = new Intent(context, TicTacToeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                return;
            } else if (graceCard.getGraceCardType() == GraceCardType.SPELLING){
                menuHierarchy.getSlider().stopSlider();
                Intent intent = new Intent(context, SpellingMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                return;
            } else if (graceCard.getGraceCardType() == GraceCardType.BACK){
                menuHierarchy.crossfade(graceCard.getNextAdapter());
            } else if (graceCard.getGraceCardType() == GraceCardType.MEDIA) {
                menuHierarchy.crossfade(graceCard.getNextAdapter());
                //Intent camIntent = new Intent(context, CameraMenuActivity.class);
                //camIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //context.startActivity(camIntent);
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
                if(mBluetoothState == BluetoothService.STATE_CONNECTED){
                    ((GraceCard) mWelcomeSplashScreenAdapter.getItem(0)).setFootnote(R.string.welcome_connected_footnote);
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
            lastSelectedCard.getGraceCardType().equals(GraceCardType.SPELLING) ||
            lastSelectedCard.getGraceCardType().equals(GraceCardType.CAMERA))){
           menuHierarchy.crossfade(lastSelectedCard.getNextAdapter());
        }
    }


    @Override
    public void onStop() {
        Log.v(TAG, "On Stop");

        // Unbind from BT Service
        unbindBTService();

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
        stopBTService();
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
        mMessageTypeAdapter = new GraceCardScrollerAdapter();

        mCardScrollView = new GraceCardScrollView(this, ScrollerListener);
        menuHierarchy = new MenuHierarchy(mCardScrollView, mWelcomeSplashScreenAdapter, getResources().getInteger(
                android.R.integer.config_longAnimTime));

        // mBaseCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, "Take a Picture or Record a Video", GraceCardType.MEDIA)); leaving this out of beta, can't inject taps into media capture application
        placeHolder = new GraceCard(this, mMessageTypeAdapter, "", GraceCardType.COMM);
        placeHolder.addImage(R.drawable.main_message).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mGameCardsAdapter, "", GraceCardType.GAMES);
        placeHolder.addImage(R.drawable.main_games).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        //TODO check if correct
        placeHolder = new GraceCard(this, mMediaCardsAdapter, "", GraceCardType.MEDIA);
        placeHolder.addImage(R.drawable.main_camera).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mTutorialAdapter, "", GraceCardType.TUTORIAL);
        placeHolder.addImage(R.drawable.main_tutorial).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, null, "", GraceCardType.EXIT);
        placeHolder.addImage(R.drawable.main_exit).setImageLayout(Card.ImageLayout.FULL);
        mBaseCardsAdapter.pushCardBack(placeHolder);

        // media adapter
        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.CAMERA);
        placeHolder.addImage(R.drawable.media_take_pic).setImageLayout(Card.ImageLayout.FULL);
        mMediaCardsAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.BACK);
        placeHolder.addImage(R.drawable.media_back).setImageLayout(Card.ImageLayout.FULL);
        mMediaCardsAdapter.pushCardBack(placeHolder);

        /* functionality not currently supported

        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mMediaCardsAdapter, "Redo", GraceCardType.REDO));
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Save Media", GraceCardType.SAVE)); // loop back to main menu for now
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Send Media",GraceCardType.SEND)); // loop back to main menu for now
        mPostMediaCardsAdapter.pushCardBack(new GraceCard(this, mBaseCardsAdapter, "Back", GraceCardType.BACK));
        Log.v(TAG, "Post Media Adapter Built");

        */


        // communication adapters
        placeHolder = new GraceCard(this, mCommContactInterstitialAdapter, "", GraceCardType.SEND_AS_EMAIL);
        placeHolder.addImage(R.drawable.send_email).setImageLayout(Card.ImageLayout.FULL);
        mMessageTypeAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mCommContactInterstitialAdapter, "", GraceCardType.SEND_AS_TEXT);
        placeHolder.addImage(R.drawable.send_text).setImageLayout(Card.ImageLayout.FULL);
        mMessageTypeAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.BACK);
        placeHolder.addImage(R.drawable.message_back).setImageLayout(Card.ImageLayout.FULL);
        mMessageTypeAdapter.pushCardBack(placeHolder);

        // contact interstitial
        placeHolder = new GraceCard(this, mCommContactsAdapter, "", GraceCardType.NONE);
        placeHolder.addImage(R.drawable.messages_contact_interstitial).setImageLayout(Card.ImageLayout.FULL);
        mCommContactInterstitialAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mCommMessagesAdapter, "", GraceCardType.NONE);
        placeHolder.addImage(R.drawable.messages_message_interstitial).setImageLayout(Card.ImageLayout.FULL);
        mCommMessagesInterstitialAdapter.pushCardBack(placeHolder);

        GraceContactCard placeHolderContact;
        placeHolderContact = new GraceContactCard(this, mCommMessagesInterstitialAdapter, "Mom", "7346459032", "vijayganesh999@gmail.com",GraceCardType.CONTACT);
        placeHolderContact.addImage(R.drawable.contact_left).setImageLayout(Card.ImageLayout.LEFT);
        mCommContactsAdapter.pushCardBack(placeHolderContact);


        placeHolderContact = new GraceContactCard(this, mCommMessagesInterstitialAdapter, "Dad", "7346459032", "vijayganesh999@gmail.com",GraceCardType.CONTACT);
        placeHolderContact.addImage(R.drawable.contact_left).setImageLayout(Card.ImageLayout.LEFT);
        mCommContactsAdapter.pushCardBack(placeHolderContact);
    /*
        placeHolderContact = GraceContactCard.addCard(this, mCommMessagesInterstitialAdapter, "Dad", "7346459032", "",GraceCardType.CONTACT);
        placeHolderContact.addImage(R.drawable.contact_left).setImageLayout(Card.ImageLayout.LEFT);

        placeHolderContact = GraceContactCard.addCard(this, mCommMessagesInterstitialAdapter, "Tim Wood", "7346459032", "", GraceCardType.CONTACT);
        placeHolderContact.addImage(R.drawable.contact_left).setImageLayout(Card.ImageLayout.LEFT);

        placeHolderContact = GraceContactCard.addCard(this, mCommMessagesInterstitialAdapter, "Danny Francken", "7346459032", "",GraceCardType.CONTACT);
        placeHolderContact.addImage(R.drawable.contact_left).setImageLayout(Card.ImageLayout.LEFT);

        Log.v(TAG, "Right before loop to add contacts to adapter" + GraceContactCard.contactList.size());
        for(int i = 0; i < GraceContactCard.contactList.size(); i++){
            mCommContactsAdapter.pushCardBack(GraceContactCard.contactList.get(i));
            Log.v(TAG, "Contact added to Adapter");
        }
    */
        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.BACK);
        placeHolder.addImage(R.drawable.message_back).setImageLayout(Card.ImageLayout.FULL);
        mCommContactsAdapter.pushCardBack(placeHolder);

         // messages adapter

        GraceMessageCard placeHolderMessage;

        placeHolderMessage = new GraceMessageCard(this, mMessageSentAdapter, "Hey there!", GraceCardType.MESSAGE);
        placeHolderMessage.addImage(R.drawable.message_left).setImageLayout(Card.ImageLayout.LEFT);
        mCommMessagesAdapter.pushCardBack(placeHolderMessage);

        placeHolderMessage = new GraceMessageCard(this, mMessageSentAdapter, "What's for dinner?!", GraceCardType.MESSAGE);
        placeHolderMessage.addImage(R.drawable.message_left).setImageLayout(Card.ImageLayout.LEFT);
        mCommMessagesAdapter.pushCardBack(placeHolderMessage);

    /*
        placeHolderMessage = GraceMessageCard.addCard(this, mMessageSentAdapter, "Hey there!", GraceCardType.MESSAGE);
        placeHolderMessage.addImage(R.drawable.message_left).setImageLayout(Card.ImageLayout.LEFT);
        placeHolderMessage = GraceMessageCard.addCard(this, mMessageSentAdapter, "Can we go to Disney World?", GraceCardType.MESSAGE);
        placeHolderMessage.addImage(R.drawable.message_left).setImageLayout(Card.ImageLayout.LEFT);
        placeHolderMessage = GraceMessageCard.addCard(this, mMessageSentAdapter, "What's for dinner?", GraceCardType.MESSAGE);
        placeHolderMessage.addImage(R.drawable.message_left).setImageLayout(Card.ImageLayout.LEFT);
        placeHolderMessage = GraceMessageCard.addCard(this, mMessageSentAdapter, "Could I get help with something?", GraceCardType.MESSAGE);
        placeHolderMessage.addImage(R.drawable.message_left).setImageLayout(Card.ImageLayout.LEFT);

        for(int i = 0; i < GraceMessageCard.messageList.size(); i++){
            mCommMessagesAdapter.pushCardBack(GraceMessageCard.messageList.get(i));
            Log.v(TAG, "Message added to Adapter");
        }
    */

        placeHolder = new GraceCard(this, mCommContactsAdapter, "", GraceCardType.BACK);
        placeHolder.addImage(R.drawable.message_back).setImageLayout(Card.ImageLayout.FULL);
        mCommMessagesAdapter.pushCardBack(placeHolder);

        // post comm confirmation screens
        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.BACK);
        placeHolder.addImage(R.drawable.message_sent).setImageLayout(Card.ImageLayout.FULL);
        mMessageSentAdapter.pushCardBack(placeHolder);

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.BACK);
        placeHolder.addImage(R.drawable.message_failed).setImageLayout(Card.ImageLayout.FULL);
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

        placeHolder = new GraceCard(this, mBaseCardsAdapter, "Menu items automatically change every few seconds.", GraceCardType.BACK);
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

    // Utility functions

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
     * Stop BT Service
     */
    private void stopBTService() {
        stopService(new Intent(this, BluetoothService.class));
    }
    /**
     * Unbind from BT Service
     */
    private void unbindBTService() {
        if(mBound) {
            sendMessageToService(BluetoothService.UNREGISTER_CLIENT);
            unbindService(mConnection);
            mBound = false;
        }
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
                        case BluetoothService.BT_DISABLED:
                            Log.e(TAG, "BT is Disabled");
                            // unbing from service
                            unbindBTService();
                            // stop service
                            stopBTService();
                            break;
                        case BluetoothService.NOT_PAIRED:
                            Log.e(TAG, "Glass not Paired");
                            // unbind from service
                            unbindBTService();
                            // stop service
                            stopBTService();
                    }
                    break;
                // in case this activity received a string message from phone
                case BluetoothService.MESSAGE_INCOMING:
                    Log.v(TAG, "message income");
                    break;
                case BluetoothService.ANDROID_MESSAGE:
                    Log.v(TAG, "Android Message: " + msg.obj.toString());
                    updateSettings(msg.obj.toString());
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

    private void updateSettings(String strSettings) {
        try {
            final JSONObject settings = new JSONObject(strSettings);

            // slider speed update
            menuHierarchy.getSlider().setScrollSpeed(settings.getInt(SCROLL_SPEED_KEY));
            final int numContacts = settings.getInt(NUM_CONTACTS_KEY);
            final int numMessages = settings.getInt(NUM_MESSAGES_KEY);

            // back card needed for contacts/messages
            final GraceCard back = new GraceCard(this, mBaseCardsAdapter, "", GraceCardType.BACK);
            back.addImage(R.drawable.message_back).setImageLayout(Card.ImageLayout.FULL);

            // contact updates
            if (numContacts > 0) {
                GraceContactCard.clearContacts();
                mCommContactsAdapter.clearCards();

                for (int i = 1; i <= numContacts; i++) {
                    final String name_key = "contact_" + i + "_name";
                    final String number_key = "contact_" + i + "_number";
                    final String email_key = "contact_" + i + "_email";
                    final String name = settings.getString(name_key);
                    final String number = settings.getString(number_key);
                    final String email = settings.getString(email_key);

                    GraceContactCard.addCard(MainActivity.this, mCommMessagesInterstitialAdapter, name, number, email, GraceCardType.CONTACT);
                }

                // now add these contacts to the contacts adapter
                for (GraceContactCard card : GraceContactCard.contactList) {
                    mCommContactsAdapter.pushCardBack(card);
                    Log.v(TAG, "Contact added to Adapter. Name: " + card.getName());
                }

                // add back card, then notify adapter
                mCommContactsAdapter.pushCardBack(back);
                mCommContactsAdapter.notifyDataSetChanged();
            }

            // message updates
            if (numMessages > 0) {
                mCommMessagesAdapter.clearCards();

                for (int i = 1; i <= numMessages; i++) {
                    final String message_key = "message_" + i;
                    GraceMessageCard messageCard = new GraceMessageCard(this, mMessageSentAdapter, settings.getString(message_key), GraceCardType.MESSAGE);
                    Log.e(TAG, "msg being added: " + settings.getString(message_key));
                    messageCard.addImage(R.drawable.message_left).setImageLayout(Card.ImageLayout.LEFT);
                    mCommMessagesAdapter.pushCardBack(messageCard);
                }

                // add back card, then notify adapter
                mCommMessagesAdapter.pushCardBack(back);
                mCommMessagesAdapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

