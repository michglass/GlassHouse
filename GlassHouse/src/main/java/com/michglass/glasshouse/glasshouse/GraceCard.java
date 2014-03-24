package com.michglass.glasshouse.glasshouse;

import android.content.Context;

import com.google.android.glass.app.Card;

/**
 * Created by rodly on 3/23/14.
 */
public class GraceCard extends Card {

    final private GraceCardScrollerAdapter adapter;

    public GraceCard(Context context, GraceCardScrollerAdapter adapter, String name) {
        super(context);
        super.setText(name);
        this.adapter = adapter;
    }

    public GraceCardScrollerAdapter getAdapter() {
        return adapter;
    }
}
