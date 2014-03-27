package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.net.Uri;

import com.google.android.glass.app.Card;

/**
 * Created by rodly on 3/23/14.
 */
public class GraceCard extends Card {

    final private GraceCardScrollerAdapter adapter;
    private GraceCardType cardType;

    public GraceCard(Context context, GraceCardScrollerAdapter adapter, String name, GraceCardType cardType) {
        super(context);
        super.setText(name);
        this.adapter = adapter;
        this.cardType = cardType;
    }

    public GraceCardScrollerAdapter getNextAdapter() {
        return adapter;
    }

    public GraceCardType getGraceCardType(){
        return cardType;
    }
    public Card addImage (Uri uri) {
        super.addImage(uri);
        return this;
    }
}