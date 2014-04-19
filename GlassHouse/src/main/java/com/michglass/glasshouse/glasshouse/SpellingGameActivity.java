package com.michglass.glasshouse.glasshouse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

public class SpellingGameActivity extends BluetoothActivity {

    // Debug
    private static final String TAG = "Main Activity";

    // spelling logic
    SpellLogic mSpellingLogic;

    // enable user input for the game
    boolean isInputEnabled;

    // handler for incoming messages
    private Handler gameHandler;

    // indicates which word to pic
    public static int GAME_NUMBER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "On Create");
        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set up game handler
        gameHandler = setUpGameHandler();

        // disable user input
        isInputEnabled = false;

        // get extra from menu activity
        Intent intent = getIntent();
        boolean wordFlag = intent.getBooleanExtra("wordFlag", false);

        // Start the Game
        SpellingGameSurface gameSurface = new SpellingGameSurface(this);
        mSpellingLogic = new SpellLogic(gameSurface, gameHandler, wordFlag);
        mSpellingLogic.startGame();
        setContentView(gameSurface);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpellingLogic.stopGame();
        Log.v(TAG, "On Destroy");
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {

        // If user selects a letter
        if(keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
            Log.v(TAG, "User Made Move");
            if(isInputEnabled) {
                mSpellingLogic.userSelectLetter();
                Log.v(TAG, "Input Enabled");
            }
            else
                Log.v(TAG, "Input Disabled");
            return true;
        }
        // otherwise handle as usual
        return super.onKeyDown(keycode, event);
    }

    private Handler setUpGameHandler() {

        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {

                if(message.what == SpellingGameSurface.GAME_OVER) {
                    SpellingMenuActivity.FROM_GAME = true;
                    finish();
                } else if(message.what == SpellingGameSurface.DISABLE_INPUT) {
                    isInputEnabled = false;
                } else if(message.what == SpellingGameSurface.ENABLE_INPUT) {
                    isInputEnabled = true;
                }
                return true;
            }
        });
    }
}
