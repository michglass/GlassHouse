package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;

/**
 * Created By Oliver
 * Date: 3/21/2014
 */
public class TicTacToeActivity extends BluetoothActivity {

    // Debug
    private static final String TAG = "TicTacToe";
    private DrawingLogic mDrawingLogic;
    private TTTGameSurface gameSurface;

    // UI Variables
    private GraceCardScrollerAdapter mGraceCardScrollAdapter;
    private GraceCardScrollView mGraceCardScrollView;
    private MenuHierarchy menuHierarchy;

    // Game Variables
    private boolean gameOver;
    private Handler delayHandler = new Handler();
    private Handler gameHandler;
    private final Context mContext = this;
    private final String START_GAME = "Start Game";
    private final String GO_BACK = "Back";

    // boolean that indicates if the user input is allowed
    boolean isInputEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "On Create");
        super.onCreate(savedInstanceState);

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set the listener for the view
        AdapterView.OnItemClickListener cardScrollViewListener = setCardScrollViewListener();

        // set up cards
        GraceCard startCard = new GraceCard(this, null, "", GraceCardType.START_GAME);
        startCard.addImage(R.drawable.games_tictactoe).setImageLayout(Card.ImageLayout.FULL);

        GraceCard goBackCard = new GraceCard(this, null, "", GraceCardType.EXIT);
        goBackCard.addImage(R.drawable.games_back).setImageLayout(Card.ImageLayout.FULL);

        // set up view and adapter
        mGraceCardScrollView = new GraceCardScrollView(this, cardScrollViewListener);
        mGraceCardScrollAdapter = new GraceCardScrollerAdapter();
        mGraceCardScrollAdapter.pushCardBack(startCard);
        mGraceCardScrollAdapter.pushCardBack(goBackCard);

        menuHierarchy = new MenuHierarchy(mGraceCardScrollView, mGraceCardScrollAdapter,getResources().getInteger(
                android.R.integer.config_longAnimTime));

        mGraceCardScrollView.activate();
        setContentView(mGraceCardScrollView);
        menuHierarchy.getSlider().start();

        // disable user input
        isInputEnabled = false;

        // set up handler
        gameHandler = setUpGameHandler();

        gameOver = true;
    }
    @Override
    protected void onDestroy() {
        Log.v(TAG, "On Destroy");
        super.onDestroy();

        // Stop Simulating Gestures
        menuHierarchy.getSlider().stopSlider();

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
                GraceCard c = (GraceCard) mGraceCardScrollAdapter.getItem(i);

                if(c.getGraceCardType() == GraceCardType.EXIT) {
                    finish();
                }
                if(c.getGraceCardType() == GraceCardType.START_GAME) {

                    menuHierarchy.getSlider().stopSlider();

                    // set up game
                    gameOver = false;
                    gameSurface = null;
                    gameSurface = new TTTGameSurface(mContext);
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
                if(isInputEnabled) {
                    mDrawingLogic.makeMove(TTTGameSurface.PLAYER_ID);
                    Log.v(TAG, "Input Enabled");
                } else {
                    Log.v(TAG, "Input Disabled");
                }
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

                if(message.what == TTTGameSurface.GAME_OVER) {
                    gameOver = true;
                    Log.v(TAG, "Game Over");
                    delay.run();
                    return true;
                } else if(message.what == TTTGameSurface.DISABLE_INPUT) {
                    isInputEnabled = false;
                    return true;
                } else if(message.what == TTTGameSurface.ENABLE_INPUT) {
                    isInputEnabled = true;
                    return true;
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
}
