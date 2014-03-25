package com.michglass.glasshouse.glasshouse;

import android.content.Context;

import java.util.HashSet;

/**
 * Created by vbganesh on 3/25/14.
 */
public class GraceMessageCard extends GraceCard{
    public String Message;
    public static HashSet<GraceMessageCard> messageList;
    GraceMessageCard(Context context, GraceCardScrollerAdapter adapter, String mess){
        super(context, adapter, "MESSAGE");
        Message = mess;
    }
    public static void addCard(Context context, GraceCardScrollerAdapter adapter, String mess){
        GraceMessageCard.messageList.add(new GraceMessageCard(context, adapter, mess));
    }
}
