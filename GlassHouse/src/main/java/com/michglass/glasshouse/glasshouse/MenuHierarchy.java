package com.michglass.glasshouse.glasshouse;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Tim Wood on 4/16/14.
 */
public class MenuHierarchy {
    private GraceCardScrollerAdapter mCurrentAdapter;
    private GraceCardScrollView mCardScrollView;
    private int mCrossFadeAnimationTime;
    private Slider slider = new Slider(new Gestures());
    private ArrayList<GraceCardScrollerAdapter> mAdapterList = new ArrayList<GraceCardScrollerAdapter>();


    public MenuHierarchy(GraceCardScrollView mCardScrollView, GraceCardScrollerAdapter first_adapter, int mCrossFadeAnimationTime) {
        this.mCardScrollView = mCardScrollView;
        this.mCurrentAdapter = first_adapter;
        this.mCrossFadeAnimationTime = mCrossFadeAnimationTime;
        this.mCardScrollView.setAdapter(first_adapter);
        this.mAdapterList.add(first_adapter);
        slider.setNumCards(first_adapter.getCount());
    }

    public GraceCardScrollerAdapter getCurrentAdapter() {
        return mCurrentAdapter;
    }

    public void setCurrentAdapter(GraceCardScrollerAdapter mCurrentAdapter) {
        this.mCurrentAdapter = mCurrentAdapter;
    }

    public Slider getSlider() {
        return slider;
    }

    public void setSlider(Slider slider) {
        this.slider = slider;
    }


    public void crossfade(final int delay_seconds, final GraceCardScrollerAdapter next_adapter) {
        // run on a delay
        this.getSlider().stopSlider();


        class DelayCrossFade extends Thread {
            @Override
            public void run() {
                super.run();
                if(next_adapter == null) {
                    return;
                }
                long sleep_miliseconds = delay_seconds * 1000;
                try {
                    Log.v("crossfade delay", "Sleep");
                    sleep(delay_seconds * 1000);
                } catch (InterruptedException intE) {
                    Log.e("crossfade delay", "Slider Interrupted", intE);
                    return;
                }

                crossfade(next_adapter);
            }
        }
        if(delay_seconds == 0) {
            crossfade(next_adapter);
            return;
        }
        else {
            DelayCrossFade runnable = new DelayCrossFade();
            runnable.run();
            return;
        }
    }

    private void crossfade(GraceCardScrollerAdapter next_adapter){

        if(next_adapter == null){
            return;
        }
        mCardScrollView.setAlpha(0f);
        mCardScrollView.setVisibility(View.VISIBLE);

        mCardScrollView.animate()
                .alpha(0f)
                .setDuration(mCrossFadeAnimationTime)
                .setListener(animationListener);

        swapAdapters(next_adapter);

        mCardScrollView.animate()
                .alpha(1f)
                .setDuration(mCrossFadeAnimationTime)
                .setListener(null);

    }


    private void swapAdapters(GraceCardScrollerAdapter next_adapter){
        if(next_adapter.equals(null)) {
            return;
        }

        mCurrentAdapter = next_adapter;

        // replace slider with a new one for our new hierarchy
        //mCurrGestures = new Gestures();
        //mCurrentSlider = new Slider(mCurrentAdapter.getCount());

        // switch out cards being displayed
        mCardScrollView.setAdapter(mCurrentAdapter);

        mCardScrollView.setSelection(0);
        mCurrentAdapter.notifyDataSetChanged();
        mCardScrollView.updateViews(false);
        mCardScrollView.activate();
        this.setSlider(new Slider(new Gestures()));
        this.getSlider().setNumCards(mCurrentAdapter.getCount());
        this.getSlider().start();

    }

    public void addAdapter(GraceCardScrollerAdapter adapter){
        if(adapter == null){
            return;
        }
        this.mAdapterList.add(adapter);
    }

AnimatorListenerAdapter animationListener = new AnimatorListenerAdapter() {
    @Override
    public void onAnimationCancel(Animator animation) {
        super.onAnimationCancel(animation);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mCardScrollView.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        super.onAnimationRepeat(animation);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);
    }
};

}

