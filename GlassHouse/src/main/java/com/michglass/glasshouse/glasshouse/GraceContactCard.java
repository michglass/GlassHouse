package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by vbganesh on 3/25/14.
 */
public class GraceContactCard extends GraceCard {
    private static final String TAG = "GraceContactCard";
    public String name;
    public String phoneNumber;
    public static ArrayList<GraceContactCard> contactList = new ArrayList <GraceContactCard>();

    GraceContactCard(Context context, GraceCardScrollerAdapter adapter, String name, String number,
                     GraceCardType cardType)
    {
        super(context, adapter, name, cardType);
        Log.v(TAG, "Entered Contact Card Constructor");
        phoneNumber = number;
        this.name = name;
        Log.v(TAG, "Finished Contact Card Constructor");
    }

    public static void addCard(Context context, GraceCardScrollerAdapter adapter, String name,
                               String phoneNumber, GraceCardType cardType)
    {
        Log.v(TAG, "Right before card added");
        for (GraceContactCard contactCard : contactList) {
            if (contactCard.name.equals(name)) {
                Log.v(TAG, "Not adding " + name + ", " + phoneNumber + " because it's a duplicate!");
                return;
            }
        }
        GraceContactCard.contactList.add(new GraceContactCard(context, adapter, name, phoneNumber, cardType));
        Log.v(TAG, "Right after card added");
    }

    public static void clearContacts() {
        contactList.clear();
    }
}
