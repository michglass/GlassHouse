package com.michglass.glasshouse.glasshouse;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by vbganesh on 3/25/14.
 */
public class GraceMessageCard extends GraceCard{
    private String Message;
    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
    public static ArrayList<GraceMessageCard> messageList = new ArrayList<GraceMessageCard>();
    GraceMessageCard(Context context, GraceCardScrollerAdapter adapter, String mess, GraceCardType cardType){
        super(context, adapter, mess, cardType);
        Message = mess;
    }
    public static void addCard(Context context, GraceCardScrollerAdapter adapter, String mess,
                               GraceCardType cardType){
        GraceMessageCard.messageList.add(new GraceMessageCard(context, adapter, mess, cardType));
    }
}
