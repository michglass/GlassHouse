package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.widget.AdapterView;

import com.google.android.glass.widget.CardScrollView;

/**
 * Created by vbganesh on 3/26/14.
 */
public class GraceCardScrollView extends CardScrollView {

    private boolean firstTimeLoadingView = true;


    public GraceCardScrollView(Context context, OnItemClickListener listener) {
        super(context);
        this.setOnItemClickListener(listener);
    }

    public boolean isFirstTimeLoadingView() {
        return firstTimeLoadingView;
    }

    public void setFirstTimeLoadingView(boolean firstTimeLoadingView) {
        this.firstTimeLoadingView = firstTimeLoadingView;
    }
}
