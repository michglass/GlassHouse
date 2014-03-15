package com.michglass.glasshouse.glasshouse;

import android.app.Application;

import java.util.ArrayList;

/**
 * Created by dfranckn on 3/12/14.
 */
public class Globals extends Application {
    private ArrayList<MenuOption> currentList;

    public ArrayList<MenuOption> getList(){
        return this.currentList;
    }

    public void setList(ArrayList<MenuOption> clist){
        this.currentList = clist;
    }
}
