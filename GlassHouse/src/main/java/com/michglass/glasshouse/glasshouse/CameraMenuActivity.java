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
 * Date: 4/17/2014.
 */
public class CameraMenuActivity extends BluetoothActivity {

    // Debug
    private static final String TAG = "Camera Menu Activity";

    // Card Scroll Variables
    private GraceCardScrollerAdapter mScrollAdapter;
    private GraceCardScrollView mScrollView;
    private MenuHierarchy mMenu;

    // Card Names
    private final String START_CAM = "Start Camera";
    private final String GO_BACK = "Go Back";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "On Create");
        super.onCreate(savedInstanceState);

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set up on item click listener
        AdapterView.OnItemClickListener scrollViewListener = setUpListener(this);

        // set up cards
        GraceCard startCam = new GraceCard(this, mScrollAdapter, START_CAM, GraceCardType.NONE);
        GraceCard goBack = new GraceCard(this, mScrollAdapter, GO_BACK, GraceCardType.NONE);

        // set up view and adapter
        mScrollView = new GraceCardScrollView(this, scrollViewListener);
        mScrollAdapter = new GraceCardScrollerAdapter();
        mScrollAdapter.pushCardBack(startCam);
        mScrollAdapter.pushCardBack(goBack);

        // Set up menu hierarchy
        mMenu = new MenuHierarchy(mScrollView, mScrollAdapter, getResources().getInteger(
                android.R.integer.config_longAnimTime));

        // show view
        mScrollView.activate();
        setContentView(mScrollView);
    }
    @Override
    protected void onStart() {
        Log.v(TAG, "On Start");
        super.onStart();
        mMenu.getSlider().stopSlider();
        mMenu.setSlider(new Slider(new Gestures()));
        mMenu.getSlider().setNumCards(mScrollAdapter.getCount());
        mMenu.getSlider().start();
    }
    @Override
    protected void onDestroy() {
        Log.v(TAG, "On Destroy");
        super.onDestroy();
        mMenu.getSlider().stopSlider();
    }

    // set up view listener
    private AdapterView.OnItemClickListener setUpListener(final Context context) {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v(TAG, "On Item Click");
                GraceCard card = (GraceCard) mScrollAdapter.getItem(i);

                if( ((String) card.getText()).compareTo(START_CAM) == 0) {
                    Log.v(TAG, "Start Cam click");
                    mMenu.getSlider().stopSlider();
                    Intent picInt = new Intent(context, PictureActivity.class);
                    startActivity(picInt);
                } else if( ((String) card.getText()).compareTo(GO_BACK) == 0 ) {
                    finish();
                }
            }
        };
    }
}
