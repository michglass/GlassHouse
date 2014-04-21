package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by vbganesh on 3/25/14.
 */
public class GraceContactCard extends GraceCard {
    private static final String TAG = "GraceContactCard";
    private String name;

    private String phoneNumber;

    private String emailAddress;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public static ArrayList<GraceContactCard> contactList = new ArrayList <GraceContactCard>();

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    GraceContactCard(Context context, GraceCardScrollerAdapter adapter, String name, String number,
                     String emailAddress, GraceCardType cardType)
    {
        super(context, adapter, name, cardType);
        Log.v(TAG, "Entered Contact Card Constructor");
        phoneNumber = number;
        this.name = name;
        this.emailAddress = emailAddress;
        Log.v(TAG, "Finished Contact Card Constructor");
    }

    public static GraceContactCard addCard(Context context, GraceCardScrollerAdapter adapter, String name,
                               String phoneNumber, String emailAddress, GraceCardType cardType)
    {
        Log.v(TAG, "Right before card added");
        for (GraceContactCard contactCard : contactList) {
            if (contactCard.name.equals(name)) {
                Log.v(TAG, "Not adding " + name + ", " + phoneNumber + " because it's a duplicate!");
                return null;
            }
        }
        GraceContactCard c;
        GraceContactCard.contactList.add(c = new GraceContactCard(context, adapter, name, phoneNumber, emailAddress, cardType));
        Log.v(TAG, "Right after card added");
        return c;
    }

    public static void clearContacts() {
        contactList.clear();
    }
}
