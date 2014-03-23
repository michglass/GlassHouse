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
public class CardScroller extends CardScrollAdapter {

    // Debug
    private static final String TAG = "Card Scroll";

    // list of Cards
    private ArrayList<Card> mCardList = new ArrayList<Card>();

    // Constructor
    public CardScroller() {
        super();
    }

    /**
     * Creates a list of Cards from a list of persons
     * @param personNames names of persons
     * @param context Activity where Cards will be displayed
     * @param b dummy boolean so createCards can be overloaded
     */
    public void createCards(ArrayList<String> personNames, Context context, boolean b) {
        Log.v(TAG, "Create Cards");
        // placeholder card
        Card currCard;
        for(String name: personNames) {
            currCard = new Card(context);
            currCard.setText(name);
            // currCard.addImage(R.drawable.sample_img);
            mCardList.add(currCard);
        }
    }


    /**
     * Creates a list of Cards from a list of persons
     * @param messages List of messages that are going to be displayed in a ScrollView
     * @param context Activity where Cards will be displayed
     */
    public void createCards(ArrayList<String> messages, Context context) {
        Log.v(TAG, "Create Cards");
        // placeholder card
        Card currCard;
        for(String msg: messages) {
            currCard = new Card(context);
            currCard.setText(msg);
            // currCard.addImage(R.drawable.sample_img);
            mCardList.add(currCard);
        }
    }

    /**
     * Set and get the card list
     */
    public void setCardList(ArrayList<Card> cards) {
        this.mCardList = cards;
    }
    public ArrayList<Card> getCardList() {
        return this.mCardList;
    }

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
