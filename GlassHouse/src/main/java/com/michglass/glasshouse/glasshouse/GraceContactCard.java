package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by vbganesh on 3/25/14.
 */
public class GraceContactCard extends GraceCard {
    private static final String TAG = "Main Activity";
    public String Name;
    public String phoneNumber;
    public static ArrayList<GraceContactCard> contactList = new ArrayList <GraceContactCard>();
    GraceContactCard(Context context, GraceCardScrollerAdapter adapter, String name, String number,
                     GraceCardType cardType)
    {
        super(context, adapter, name, cardType);
        Log.v(TAG, "Entered Contact Card Constructor");
        phoneNumber = number;
        Name = name;
        Log.v(TAG, "Finished Contact Card Constructor");
    }
    public static void addCard(Context context, GraceCardScrollerAdapter adapter, String Name,
                               String phoneNumber, GraceCardType cardType)
    {
        Log.v(TAG, "Right before card added");
        GraceContactCard.contactList.add(new GraceContactCard(context, adapter, Name, phoneNumber, cardType));
        Log.v(TAG, "Right after card added");
    }

}
