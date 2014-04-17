package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.google.android.glass.app.Card;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by rodly on 3/23/14.
 */
public class GraceCard extends Card {

    final private GraceCardScrollerAdapter adapter;
    private GraceCardType cardType;

    public GraceCard(Context context, GraceCardScrollerAdapter adapter, String name, GraceCardType cardType) {
        super(context);
        super.setText((CharSequence) name);
        this.adapter = adapter;
        this.cardType = cardType;
    }

    public GraceCardScrollerAdapter getNextAdapter() {
        return adapter;
    }

    public GraceCardType getGraceCardType(){
        return cardType;
    }
    public Card addImage (Drawable drawable) {
        super.addImage(drawable);
        return this;
    }
}