package com.michglass.glasshouse.glasshouse;



import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * Created by dfranckn on 3/3/14.
 */
public class MenuOption {

    public String displayText;
    public ArrayList<MenuOption> parent;
    public int image;
    public ArrayList<MenuOption> nextMenu;
    public boolean finalMenu;
    public String command;

}


