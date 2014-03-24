package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.os.Handler;

/**
 * Created by rodly on 3/24/14.
 */
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
}
