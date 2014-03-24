package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.ArrayList;

/**
 *  Created By Oliver
 *  Date: 02/20/2014
 */
public class GraceCardScrollerAdapter extends CardScrollAdapter {

    // Debug
    private static final String TAG = "Card Scroll";

    // list of Cards
    private ArrayList<GraceCard> mCardList = new ArrayList<GraceCard>();

    // Constructor
    public GraceCardScrollerAdapter() {
        super();
    }

    public void popCardFront() { mCardList.remove(0); }
    public void popCardBack() { mCardList.remove(mCardList.size() - 1); }
    public void pushCardBack(GraceCard card) { mCardList.add(card); }
    public void pushCardFront(GraceCard card) { mCardList.add(0, card);}

    /**
     * Set and get the card list
     */
    public void setCardList(ArrayList<GraceCard> cards) {
        this.mCardList = cards;
    }
    public ArrayList<GraceCard> getCardList() { return this.mCardList; }

    /*
        Adapter Methods
     */
    /**
     * @param id Card Id
     * @return int position of Id
     */
    @Override
    public int findIdPosition(Object id) {
        Log.v(TAG, "Find id Position");
        return -1;
    }
    /**
     * Gives the position of the card in the ScrollView
     * @param item Card we want to find position
     * @return position of Card (item)
     */
    @Override
    public int findItemPosition(Object item) {
        return mCardList.indexOf(item);
    }
    /**
     * @return Number of cards
     */
    @Override
    public int getCount() {
        return mCardList.size();
    }
    /**
     * @param position of the Card in the CardList
     * @return Get card at "position"
     */
    @Override
    public Object getItem(int position) {
        return mCardList.get(position);
    }
    /**
     * @param position Position of Card
     * @param convertView
     * @param parent
     * @return Card converted to a View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mCardList.get(position).toView();
    }
}
