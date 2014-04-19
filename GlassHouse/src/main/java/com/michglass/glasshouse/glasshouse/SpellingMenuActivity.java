package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

/**
 * Created by Oliver
 * Date: 4/14/2014.
 */
public class SpellingMenuActivity extends BluetoothActivity {

    // Debug
    private static final String TAG = "Spelling Menu Activity: ";

    // UI Variables
    private MenuHierarchy menuHierarchy;
    private GraceCardScrollView mCardScrollView;
    private GraceCardScrollerAdapter mGraceCardScrollAdapter;

    // Text for Cards
    private final String START_GAME = "Start Game";
    private final String SAME_WORD = "Play again with same word";
    private final String DIFFERENT_WORD = "Play again with different word";
    private final String GO_GAME_MENU = "Go to Game Menu";

    // context for listener
    private Context thisContext = this;

    // boolean indicating if this activity started back from the Game
    public static boolean FROM_GAME;

    // On Create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "On Create");
        super.onCreate(savedInstanceState);

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // activity started from game menu
        FROM_GAME = false;

        // set the listener for the view
        AdapterView.OnItemClickListener cardScrollViewListener = setCardScrollViewListener();

        GraceCard startCard = new GraceCard(this, mGraceCardScrollAdapter, START_GAME, GraceCardType.NONE);
        GraceCard goBackCard = new GraceCard(this, mGraceCardScrollAdapter, GO_GAME_MENU, GraceCardType.NONE);

        // set up view and adapter
        mCardScrollView = new GraceCardScrollView(this, cardScrollViewListener);
        mGraceCardScrollAdapter = new GraceCardScrollerAdapter();

        // add cards to adapter
        mGraceCardScrollAdapter.pushCardBack(startCard);
        mGraceCardScrollAdapter.pushCardBack(goBackCard);

        menuHierarchy = new MenuHierarchy(mCardScrollView, mGraceCardScrollAdapter, getResources().getInteger(
                android.R.integer.config_longAnimTime));
        // Set content view

        mCardScrollView.activate();
        setContentView(mCardScrollView);
        menuHierarchy.getSlider().start();
    }
    @Override
    protected void onStart() {
        Log.v(TAG, "On Start");
        super.onStart();

        if(FROM_GAME) {
            Log.v(TAG, "From Game");
            // stop current slider and set up new slider
            menuHierarchy.getSlider().stopSlider();
            menuHierarchy.setSlider(new Slider(new Gestures()));

            mGraceCardScrollAdapter = new GraceCardScrollerAdapter();
            mGraceCardScrollAdapter.pushCardBack(
                    new GraceCard(this, mGraceCardScrollAdapter, SAME_WORD, GraceCardType.NONE));
            mGraceCardScrollAdapter.pushCardBack(
                    new GraceCard(this, mGraceCardScrollAdapter, DIFFERENT_WORD, GraceCardType.NONE));
            mGraceCardScrollAdapter.pushCardBack(
                    new GraceCard(this, mGraceCardScrollAdapter, GO_GAME_MENU, GraceCardType.NONE));

            // modify slider and view and show new view
            menuHierarchy.getSlider().setNumCards(mGraceCardScrollAdapter.getCount());
            mCardScrollView.setAdapter(mGraceCardScrollAdapter);
            mCardScrollView.activate();
            setContentView(mCardScrollView);
            menuHierarchy.getSlider().start();
        }
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
    }
    @Override
    protected void onPause() {
        Log.v(TAG, "On Pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "On Destroy");
        super.onDestroy();
        menuHierarchy.getSlider().stopSlider();
    }

    // item click listener for starting game/exiting activity
    private AdapterView.OnItemClickListener setCardScrollViewListener() {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v(TAG, "On Item Click Listener");
                GraceCard card = (GraceCard) mGraceCardScrollAdapter.getItem(i);

                if( (((String) card.getText()).compareTo(START_GAME) == 0) ||
                        (((String) card.getText()).compareTo(START_GAME) == 0)) {
                    menuHierarchy.getSlider().stopSlider();
                    Intent gameIntent = new Intent(thisContext, SpellingGameActivity.class);
                    gameIntent.putExtra("wordFlag", false);
                    startActivity(gameIntent);
                } else if( ((String) card.getText()).compareTo(SAME_WORD) == 0 ) {
                    menuHierarchy.getSlider().stopSlider();
                    Intent gameIntent = new Intent(thisContext, SpellingGameActivity.class);
                    gameIntent.putExtra("wordFlag", true);
                    startActivity(gameIntent);
                }
                else if(((String) card.getText()).compareTo(GO_GAME_MENU) == 0) {
                    finish();
                }
            }
        };
    }
}
