package com.michglass.glasshouse.glasshouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

/**
 * Created by Oliver
 * Date: 4/14/2014.
 */
public class SpellingMenuActivity extends Activity {

    // Debug
    private static final String TAG = "Spelling Menu Activity: ";

    // UI Variables
    private GraceCardScrollerAdapter mGraceCardScrollAdapter;
    private GraceCardScrollView mCardScrollView;

    // Text for Cards
    private final String START_GAME = "Start Game";
    private final String PLAY_AGAIN = "Play again";
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

        // set up Slider
        Slider slider = new Slider(new Gestures());

        // set up cards
        GraceCard startCard = new GraceCard(this, mGraceCardScrollAdapter, START_GAME, GraceCardType.NONE);
        GraceCard goBackCard = new GraceCard(this, mGraceCardScrollAdapter, GO_GAME_MENU, GraceCardType.NONE);

        // set up view and adapter
        mCardScrollView = new GraceCardScrollView(this, cardScrollViewListener);
        mGraceCardScrollAdapter = new GraceCardScrollerAdapter(mCardScrollView, slider);
        mGraceCardScrollAdapter.pushCardBack(startCard);
        mGraceCardScrollAdapter.pushCardBack(goBackCard);
        mGraceCardScrollAdapter.getSlider().setNumCards(mGraceCardScrollAdapter.getCount());

        // Set content view
        mCardScrollView.setAdapter(mGraceCardScrollAdapter);
        mCardScrollView.activate();
        setContentView(mCardScrollView);
        mGraceCardScrollAdapter.getSlider().start();

    }
    @Override
    protected void onStart() {
        Log.v(TAG, "On Start");
        super.onStart();

        if(FROM_GAME) {
            Log.v(TAG, "From Game");
            mGraceCardScrollAdapter.getSlider().stopSlider();
            mGraceCardScrollAdapter.setSlider(new Slider(new Gestures()));
            mGraceCardScrollAdapter.getSlider().setNumCards(mGraceCardScrollAdapter.getCount());
            mGraceCardScrollAdapter.popCardFront();
            mGraceCardScrollAdapter.pushCardFront(
                    new GraceCard(this, mGraceCardScrollAdapter, PLAY_AGAIN, GraceCardType.NONE));
            mCardScrollView.setAdapter(mGraceCardScrollAdapter);
            mCardScrollView.activate();
            setContentView(mCardScrollView);
            mGraceCardScrollAdapter.getSlider().start();
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
        mGraceCardScrollAdapter.getSlider().stopSlider();
    }

    // item click listener for starting game/exiting activity
    private AdapterView.OnItemClickListener setCardScrollViewListener() {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v(TAG, "On Item Click Listener");
                GraceCard card = (GraceCard) mGraceCardScrollAdapter.getItem(i);

                if(card.getText().compareTo(START_GAME) == 0 ||
                        card.getText().compareTo(PLAY_AGAIN) == 0) {
                    mGraceCardScrollAdapter.getSlider().stopSlider();
                    Intent gameIntent = new Intent(thisContext, SpellingGameActivity.class);
                    startActivity(gameIntent);
                } else if(card.getText().compareTo(GO_GAME_MENU) == 0) {
                    finish();
                }
            }
        };
    }
}
