package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;

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
    private GraceCardScrollerAdapter mPreGameScrollAdapter;
    private GraceCardScrollerAdapter mPostGameScrollAdapter;
    private GraceCard placeholder;

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

        GraceCard startCard = new GraceCard(this, mPreGameScrollAdapter, "", GraceCardType.START_GAME);
        startCard.addImage(R.drawable.games_spell).setImageLayout(Card.ImageLayout.FULL);

        GraceCard goBackCard = new GraceCard(this, mPreGameScrollAdapter, "", GraceCardType.EXIT);
        goBackCard.addImage(R.drawable.games_back).setImageLayout(Card.ImageLayout.FULL);


        // set up view and adapter
        mCardScrollView = new GraceCardScrollView(this, cardScrollViewListener);
        mPreGameScrollAdapter = new GraceCardScrollerAdapter();

        // add cards to adapter
        mPreGameScrollAdapter.pushCardBack(startCard);
        mPreGameScrollAdapter.pushCardBack(goBackCard);

        menuHierarchy = new MenuHierarchy(mCardScrollView, mPreGameScrollAdapter, getResources().getInteger(
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

            mPostGameScrollAdapter = new GraceCardScrollerAdapter();
            placeholder = new GraceCard(this, null, "", GraceCardType.PLAY_AGAIN);
            placeholder.addImage(R.drawable.spelling_playagain).setImageLayout(Card.ImageLayout.FULL);
            mPostGameScrollAdapter.pushCardBack(placeholder);

         //   mPreGameScrollAdapter.pushCardBack(
         //           new GraceCard(this, mPreGameScrollAdapter, "", GraceCardType.PLAY_AGAIN)
         //   .addImage(R.drawable.spelling_playagain).setImageLayout(Card.ImageLayout.FULL));

            placeholder = new GraceCard(this, null, "", GraceCardType.EXIT);
            placeholder.addImage(R.drawable.games_back).setImageLayout(Card.ImageLayout.FULL);
            mPostGameScrollAdapter.pushCardBack(placeholder);

         /*
            mPreGameScrollAdapter.pushCardBack(
                    new GraceCard(this, mPreGameScrollAdapter, DIFFERENT_WORD, GraceCardType.NONE));
            mPreGameScrollAdapter.pushCardBack(
                    new GraceCard(this, mPreGameScrollAdapter, GO_GAME_MENU, GraceCardType.NONE));

         */

            // modify slider and view and show new view
            menuHierarchy.getSlider().setNumCards(mPostGameScrollAdapter.getCount());
            mCardScrollView.setAdapter(mPostGameScrollAdapter);
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
                GraceCard card = (GraceCard) mPreGameScrollAdapter.getItem(i);

                if( (card.getGraceCardType() == GraceCardType.PLAY_AGAIN ||
                        card.getGraceCardType() == GraceCardType.START_GAME)) {
                    menuHierarchy.getSlider().stopSlider();
                    Intent gameIntent = new Intent(thisContext, SpellingGameActivity.class);
                    gameIntent.putExtra("wordFlag", false);
                    startActivity(gameIntent);
                }
                else if( ((String) card.getText()).compareTo(SAME_WORD) == 0 ) {
                    menuHierarchy.getSlider().stopSlider();
                    Intent gameIntent = new Intent(thisContext, SpellingGameActivity.class);
                    gameIntent.putExtra("wordFlag", true);
                    startActivity(gameIntent);
                }
                else if(card.getGraceCardType() == GraceCardType.EXIT) {
                    finish();
                }
            }
        };
    }
}
