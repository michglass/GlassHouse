package com.michglass.glasshouse.glasshouse;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By Oliver
 * Date: 3/21/2014
 */
public class TicTacToeActivity extends Activity {

    // Debug
    private static final String TAG = "TicTacToe";
    private DrawingLogic mDrawingLogic;
    private GameSurface gameSurface;


    private boolean gameOver;
    private Handler delayHandler = new Handler();
    private List<Card> mCards;
    private CardScrollView mCardScrollView;
    private Handler gameHandler;
    private final Context mContext = this;
    private ExampleCardScrollAdapter adapter;

    private final String START_GAME = "Start Game";
    private final String GO_BACK = "Back";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "On Create");
        super.onCreate(savedInstanceState);

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set up cardscrollview
        mCards = new ArrayList<Card>();
        mCards.add(new Card(this).setText(START_GAME));
        mCards.add(new Card(this).setText(GO_BACK));
        mCardScrollView = new CardScrollView(this);
        adapter = new ExampleCardScrollAdapter();
        mCardScrollView.setAdapter(adapter);
        mCardScrollView.activate();
        setContentView(mCardScrollView);
        setCardScrollViewListener();

        // set up handler
        gameHandler = setUpGameHandler();

        gameOver = true;
    }
    @Override
    public void onResume() {
        Log.v(TAG, "On Resume");
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        Log.v(TAG, "On Destroy");
        super.onDestroy();

        if(mDrawingLogic != null)
            mDrawingLogic.pauseGame();
    }

    private void setCardScrollViewListener() {

        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v(TAG, "On Item Click Listener");
                Card c = (Card) adapter.getItem(i);
                if(c.getText().equals(GO_BACK)) {
                    finish();
                }
                if(c.getText().equals(START_GAME)) {
                    // set up game
                    gameOver = false;
                    gameSurface = null;
                    gameSurface = new GameSurface(mContext);
                    mDrawingLogic = new DrawingLogic(gameSurface, gameHandler);
                    mDrawingLogic.updateGame();
                    setContentView(gameSurface);
                }
            }
        });
    }
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        Log.v(TAG, "On Key Down");

        if(keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if(!gameOver) {
                Log.v(TAG, "Make Move");
                mDrawingLogic.makeMove(GameSurface.PLAYER_ID);
                return true;
            }
        }
        return super.onKeyDown(keycode, event);
    }
    // set up the game handler
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
    private Runnable delay = new Runnable() {
        private boolean keepRunning = true;
        @Override
        public void run() {
            Log.v(TAG, "Run");

            if(keepRunning) {
                delayHandler.postDelayed(this, 3000);

                finish();
                keepRunning = false;
            }
            Log.v(TAG, "Run Return");
        }
    };

    private class ExampleCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int findIdPosition(Object id) {
            return -1;
        }

        @Override
        public int findItemPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).toView();
        }
    }
}
