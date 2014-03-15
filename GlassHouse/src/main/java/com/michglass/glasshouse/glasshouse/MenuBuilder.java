package com.michglass.glasshouse.glasshouse;

import android.util.Log;
import android.view.Menu;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by dfranckn on 3/3/14.
 */
public class MenuBuilder {

    private static final String TAG = "Build Activity";
    public void build(ArrayList<MenuOption> questionList,ArrayList<MenuOption> helpList,
                      ArrayList<MenuOption> messageList,ArrayList<MenuOption> relationshipList,
                      ArrayList<MenuOption> friendList, ArrayList<MenuOption> familyList,
                      ArrayList<MenuOption> commandList, ArrayList<MenuOption> cameraList,
                      ArrayList<MenuOption> pictureList, ArrayList<MenuOption> videoList,
                      MenuOption helpOpt, MenuOption question, MenuOption friend, MenuOption family,
                      MenuOption where, MenuOption day, MenuOption homeTime, MenuOption bathroom,
                      MenuOption coldTemp, MenuOption hotTemp, MenuOption helpMe, MenuOption dad,
                      MenuOption mom, MenuOption friend1, MenuOption friend2, MenuOption friend3,
                      MenuOption message, MenuOption camera,MenuOption picture,MenuOption video,
                      MenuOption send, MenuOption redo, MenuOption delete
                ){

        //Question List

        //Where are you
        where.displayText = "Where are you?";
        where.parent = questionList;
        where.image = -1;
        where.finalMenu = true;
        where.command = "Send Message where";

        //How was your day?
        day.displayText = "How was your day?";
        day.parent = questionList;
        day.image = -1;
        day.finalMenu = true;
        day.command = "Send Message day";

        //When will you be home?
        homeTime.displayText = "When will you be home?";
        homeTime.parent = questionList;
        homeTime.image = -1;
        homeTime.finalMenu = true;
        homeTime.command = "Send Message homeTime";


        //Help List

        //I need to use the bathroom
        bathroom.displayText = "I need to use the bathroom";
        bathroom.parent = helpList;
        bathroom.image = -1;
        bathroom.finalMenu = true;
        bathroom.command = "Send Message bathroom";

        //I'm cold
        coldTemp.displayText = "I'm cold";
        coldTemp.parent = helpList;
        coldTemp.image = -1;
        coldTemp.finalMenu = true;
        coldTemp.command = "Send Message coldTemp";

        //I'm hot
        hotTemp.displayText = "I'm hot";
        hotTemp.parent = helpList;
        hotTemp.image = -1;
        hotTemp.finalMenu = true;
        hotTemp.command = "Send Message hotTemp";

        //Help me
        helpMe.displayText = "Help me";
        helpMe.parent = helpList;
        helpMe.image = -1;
        helpMe.finalMenu = true;
        helpMe.command = "Send Message Help Me";


        //Message Menu

        //Questions
        questionList.add(homeTime);
        questionList.add(where);
        questionList.add(day);

        messageList.add(question);
        messageList.add(helpOpt);

        question.displayText = "Questions";
        question.image = R.drawable.question;
        question.nextMenu = questionList;
        question.finalMenu = false;
        question.command = "Open Question Menu";
        question.parent = messageList;

////////////Help
        helpList.add(helpMe);
        helpList.add(hotTemp);
        helpList.add(coldTemp);
        helpList.add(bathroom);

        helpOpt.displayText = "Help";
        helpOpt.image = -1;
        helpOpt.finalMenu = false;
        helpOpt.nextMenu = helpList;
        helpOpt.parent = messageList;
        helpOpt.command = "Open Help Menu";

///////////Build Relationship List
        relationshipList.add(friend);
        relationshipList.add(family);

//////////Build Family Members

        dad.displayText = "Dad";
        dad.image = R.drawable.dad;
        dad.nextMenu = messageList;
        dad.parent = relationshipList;
        dad.finalMenu = false;
        dad.command = "Open Message Menu For Dad";


        mom.displayText = "Mom";
        mom.image = R.drawable.mom;
        mom.nextMenu = messageList;
        mom.parent = relationshipList;
        mom.finalMenu = false;
        mom.command = "Open Message Menu for Mom";


        //Build Family List
        familyList.add(dad);
        familyList.add(mom);

        //Create her friends

        friend1.displayText = "Danny";
        friend1.image = -1;
        friend1.nextMenu = messageList;
        friend1.parent = relationshipList;
        friend1.finalMenu = false;
        friend1.command = "Open Message Menu for Friend1";

        friend2.displayText = "Oliver";
        friend2.image = -1;
        friend2.nextMenu = messageList;
        friend2.parent = relationshipList;
        friend2.finalMenu = false;
        friend2.command = "Open message menu for Friend2";

        friend3.displayText = "VJ";
        friend3.image = -1;
        friend3.nextMenu = messageList;
        friend3.parent = relationshipList;
        friend3.finalMenu = false;
        friend3.command = "Open message Menu for Friend3";

        //Build Friend List
        friendList.add(friend1);
        friendList.add(friend2);
        friendList.add(friend3);


        //Friend Menu Option
        friend.displayText = "Friends";
        friend.image = -1;
        friend.nextMenu = friendList;
        friend.parent = commandList;
        friend.finalMenu = false;
        friend.command = "Open Friend List";

        //Family Menu Option
        family.displayText = "Family";
        family.image = -1;
        family.nextMenu = familyList;
        family.parent = commandList;
        family.finalMenu = false;
        family.command = "Open Family List";


        //Base Menu Commands

        message.displayText = "Send a Message";
        message.image = -1;
        message.nextMenu = relationshipList;
        message.parent = null;
        message.finalMenu = false;
        message.command = "Open Relationship list";


        camera.displayText = "Camera";
        camera.image = -1;
        camera.nextMenu = cameraList;
        camera.parent = null;
        camera.finalMenu = false;
        camera.command = "Open camera command list";

        commandList.add(message);
        commandList.add(camera);

        //Options for Camera

        picture.displayText = "Take a Picture";
        picture.image = -1;
        picture.nextMenu = pictureList;
        picture.parent = commandList;
        picture.command = "Take a picture";
        picture.finalMenu = false;


        video.displayText = "Record a Video";
        video.image = -1;
        video.nextMenu = videoList;
        video.parent = commandList;
        video.finalMenu = false;
        video.command = "Record a video";

        //Build Camera List
        cameraList.add(video);
        cameraList.add(picture);

        //Options for Picture

        send.displayText = "Send";
        send.image = -1;
        send.nextMenu = null;
        send.parent = cameraList;
        send.finalMenu = true;
        send.command = "Send item";


        redo.displayText = "Redo";
        redo.image = -1;
        redo.nextMenu = null;
        redo.parent = cameraList;
        redo.finalMenu = true;
        redo.command = "Redo picture";


        delete.displayText = "Delete";
        delete.image = -1;
        delete.nextMenu = null;
        delete.parent = cameraList;
        delete.finalMenu = true;
        delete.command = "Delete Picture";

        pictureList.add(send);
        pictureList.add(delete);
        pictureList.add(redo);

        videoList.add(send);
        videoList.add(delete);
        videoList.add(redo);

        Log.v(TAG, "Built it");
    }
}
