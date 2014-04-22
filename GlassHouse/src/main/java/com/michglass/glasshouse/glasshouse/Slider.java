package com.michglass.glasshouse.glasshouse;

import android.util.Log;

/**
 * Created by rodly on 3/24/14.
public class Slider implements Runnable {

    final Gestures mGestures;
    final Handler mHandler;
    int mCurrPosition;
    int mFinalPosition;
    boolean swipeRight;
    boolean swipeLeft;
    boolean stop;

    public Slider(final int numCards, final Handler handler) {
        mCurrPosition = 0;
        mFinalPosition = numCards - 1;
        swipeRight = true;
        swipeLeft = false;
        stop = false;
        mGestures = new Gestures();
        mHandler = handler;
    }

    @Override
    public void run() {

        if (!stop) {

            if(swipeRight) {
                mGestures.createGesture(Gestures.TYPE_SWIPE_RIGHT);
                mCurrPosition++;
            } else if(swipeLeft) {
                mGestures.createGesture(Gestures.TYPE_SWIPE_LEFT);
                mCurrPosition--;
            }

            if(mCurrPosition == 0) {
                swipeRight = true;
                swipeLeft = false;
            } else if(mCurrPosition == mFinalPosition) {
                swipeLeft = true;
                swipeRight = false;
            }

            mHandler.postDelayed(this, 3000);
        }
    }

    public void stop() {
        stop = true;
    }
}*/

/**
 * Slider Thread
 * Loops through a Card ScrollView
 * Sending Motion Events to Glass
 */
public class Slider extends Thread {

    // Debug
    private final String TAG = "Slider Thread";

    // Member vars
    private int mCurrPosition;
    private int numCards = 0;
    private boolean swipeRight;
    private boolean stop;
    private Gestures gestures;

    // Timer Variable
    static private int timer = 3000;

    /**
     * Slider
     * Set up Slider members
     * @param gestures Gesture object that will simulate the Motion Events
     */
    public Slider(Gestures gestures) {
        mCurrPosition = 0;
        swipeRight = true;
        stop = false;
        this.setGestures(gestures);
    }

    public void setScrollSpeed(final int scrollSpeed) {
        if (scrollSpeed >= 1 && scrollSpeed <= 10)
            timer = scrollSpeed * 1000;
        else
            Log.e(TAG, "setScrollSpeed was passed illegal value, " + scrollSpeed);
    }

    public Gestures getGestures() {
        return gestures;
    }

    public void setGestures(Gestures gestures) {
        this.gestures = gestures;
    }

    public int getNumCards() {
        return numCards;
    }

    public void setNumCards(int numCards) {
        this.numCards = numCards;
    }

    /**
     * Run
     */
    @Override
    public void run() {
        Log.v(TAG, "Run");

        while(!stop) {

            try {
                Log.v(TAG, "Sleep");
                sleep(timer);
            } catch (InterruptedException intE) {
                Log.e(TAG, "Slider Interrupted", intE);
                return;
            }

            if(!stop) {
                Log.v(TAG, "While Loop");

                if (swipeRight) {
                    if (stop)
                        return;
                   gestures.createGesture(Gestures.TYPE_SWIPE_RIGHT);
                    mCurrPosition++;
                } else if (!swipeRight) {
                    if (stop)
                        return;
                    gestures.createGesture(Gestures.TYPE_SWIPE_LEFT);
                    mCurrPosition--;
                }

                if (mCurrPosition == 0) {
                    swipeRight = true;
                } else if (mCurrPosition == numCards - 1 ) {
                    swipeRight = false;
                }
            }
        }
        Log.v(TAG, "Run Return");
    }

    public void stopSlider() {
        Log.v(TAG, "Stop Slider");
        stop = true;
    }
}

