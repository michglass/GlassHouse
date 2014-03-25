package com.michglass.glasshouse.glasshouse;

import android.content.Context;

import java.util.HashSet;

/**
 * Created by vbganesh on 3/25/14.
 */
public class GraceContactCard extends GraceCard {
    public String Name;
    private String phoneNumber;
    public static HashSet<GraceContactCard> contactList;
    GraceContactCard(Context context, GraceCardScrollerAdapter adapter, String name, String number){
        super(context, adapter, "CONTACT");
        phoneNumber = number;
        Name = name;

    }
    public static void addCard(Context context, GraceCardScrollerAdapter adapter, String Name, String phoneNumber){
        GraceContactCard.contactList.add(new GraceContactCard(context, adapter, Name, phoneNumber));
    }

}
