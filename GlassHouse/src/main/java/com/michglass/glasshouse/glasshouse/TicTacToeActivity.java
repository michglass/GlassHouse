package com.michglass.glasshouse.glasshouse;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;

/**
 * Created By Oliver
 * Date: 3/21/2014
 */
public class TicTacToeActivity extends Activity {

    // Debug
    private static final String TAG = "TicTacToe";
    private DrawingLogic mDrawingLogic;
    private GameSurface gameSurface;

    // UI Variables
    private GraceCardScrollerAdapter mGraceCardScrollAdapter;

    // Game Variables
    private boolean gameOver;
    private Handler delayHandler = new Handler();
    private Handler gameHandler;
    private final Context mContext = this;
    private final String START_GAME = "Start Game";
    private final String GO_BACK = "Back";

    // BT Variables
   /* private Messenger mBluetoothServiceMessenger;
    private boolean mBound;
    private final Messenger clientMessenger = new Messenger(new ServiceHandler());*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "On Create");
        super.onCreate(savedInstanceState);

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set the listener for the view
        AdapterView.OnItemClickListener cardScrollViewListener = setCardScrollViewListener();

        // set up Slider
        Slider slider = new Slider(new Gestures());

        // set up cards
        GraceCard startCard = new GraceCard(this, mGraceCardScrollAdapter, START_GAME, GraceCardType.NONE);
        GraceCard goBackCard = new GraceCard(this, mGraceCardScrollAdapter, GO_BACK, GraceCardType.NONE);

        // set up view and adapter
        GraceCardScrollView cardScrollView = new GraceCardScrollView(this, cardScrollViewListener);
        mGraceCardScrollAdapter = new GraceCardScrollerAdapter(cardScrollView, slider);
        mGraceCardScrollAdapter.pushCardBack(startCard);
        mGraceCardScrollAdapter.pushCardBack(goBackCard);
        mGraceCardScrollAdapter.getSlider().setNumCards(mGraceCardScrollAdapter.getCount());

        // Set content view
        cardScrollView.setAdapter(mGraceCardScrollAdapter);
        cardScrollView.activate();
        setContentView(cardScrollView);
        mGraceCardScrollAdapter.getSlider().start();

        // set up handler
        gameHandler = setUpGameHandler();

        gameOver = true;
    }
    @Override
    protected void onStart() {
        Log.v(TAG, "On Start");
        super.onStart();

        // Bind this Activity to the BT Service
       /* if(!mBound) {

            bindService(new Intent(this, BluetoothService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }*/
    }
    @Override
    protected void onResume() {
        Log.v(TAG, "On Resume");
        super.onResume();
    }
    @Override
    protected void onStop() {
        Log.v(TAG, "On Stop");
        super.onStop();

        // Unbind from BT Service
       /* if(mBound) {

            sendMessageToService(BluetoothService.INT_MESSAGE, BluetoothService.UNREGISTER_CLIENT);
            unbindService(mConnection);
            mBound = false;
        }*/
    }
    @Override
    protected void onDestroy() {
        Log.v(TAG, "On Destroy");
        super.onDestroy();

        // Stop Simulating Gestures
        mGraceCardScrollAdapter.getSlider().stopSlider();

        // Stop the Game
        if(mDrawingLogic != null)
            mDrawingLogic.pauseGame();
    }

    /**
     * Game Utility Functions
     * Card Scroll View Listener: Handles the events before the game and starts the game
     * On Key Down: For handling tap events when the user makes a move
     * Game Handler: Gets notified when the game is over
     * Delay Runnable: Delay game for a bit after game is over
     */

    /**
     * On Item Click Listener
     * Handles the Card Scroll View before the game starts
     * @return Item click listener
     */
    private AdapterView.OnItemClickListener setCardScrollViewListener() {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v(TAG, "On Item Click Listener");
                Card c = (Card) mGraceCardScrollAdapter.getItem(i);

                if(c.getText().equals(GO_BACK)) {
                    finish();
                }
                if(c.getText().equals(START_GAME)) {

                    mGraceCardScrollAdapter.getSlider().stopSlider();

                    // set up game
                    gameOver = false;
                    gameSurface = null;
                    gameSurface = new GameSurface(mContext);
                    mDrawingLogic = new DrawingLogic(gameSurface, gameHandler);
                    mDrawingLogic.updateGame();
                    setContentView(gameSurface);
                }
            }
        };
    }
    /**
     * On Key Down
     * Functions for making a move during the game and selecting a card before the game
     * @param keycode Event code
     * @param event Event that occured (only interesting when tap event)
     * @return true if we handled the event
     */
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        Log.v(TAG, "On Key Down");

        if(keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // if the game isn't over then we make a move
            if(!gameOver) {
                Log.v(TAG, "Make Move");
                mDrawingLogic.makeMove(GameSurface.PLAYER_ID);
                return true;
            }
        }
        return super.onKeyDown(keycode, event);
    }
    /**
     * Set up Game Handler
     * The Game Handler receives a message when the game is over
     * @return Handler game handler
     */
    private Handler setUpGameHandler() {

        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {

                if(message.what == GameSurface.GAME_OVER) {
                    gameOver = true;
                    Log.v(TAG, "Game Over");
                    delay.run();
                }
                return false;
            }
        });
    }
    /**
     * Delay Runnable
     * After the Game is over, delay for a little bit
     */
    private Runnable delay = new Runnable() {
        private boolean keepRunning = true;
        @Override
        public void run() {
            Log.v(TAG, "Run");

            if(keepRunning) {
                delayHandler.postDelayed(this, 3000);
            }
            if(!keepRunning)
                finish();
            keepRunning = false;
            Log.v(TAG, "Run Return");
        }
    };

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
 /*   private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.v(TAG, "On Service Connect");

            // set up messenger
            mBluetoothServiceMessenger = new Messenger(iBinder);
            mBound = true;

            // Send a first message to the service
            setUpMessage();
        } */
        /**
         * Only called when Service unexpectedly disconnected!!
         */
   /*     @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.v(TAG, "On Service Disconnect");
            mBound = false;
        }
    }; */

    /**
     * Send Message To Service
     * Sends a message over the Service to Android
     * @param messageType Type of message (int, String, Bitmap)
     * @param message Message body
     */
  /*  public void sendMessageToService(int messageType, Object message) {
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
    } */
    /**
     * Set Up Message
     * First contact with Service
     * Has to be send!
     * (with clientMessenger in replyTo Param so Service can respond to client)
     */
  /*  public void setUpMessage() {
        Message startMsg = new Message();
        startMsg.what = BluetoothService.REGISTER_CLIENT;
        startMsg.replyTo = clientMessenger;

        try {
            Log.v(TAG, "First time contact to service");
            mBluetoothServiceMessenger.send(startMsg);
        } catch (RemoteException remE) {
            Log.e(TAG, "Couldn't contact Service", remE);
        }
    } */

    /**
     * Message Handler
     * Handles incoming messages from Service
     * Messages wrt Android Input or Connection State
     */
   /* public class ServiceHandler extends Handler {

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
    } */
}
