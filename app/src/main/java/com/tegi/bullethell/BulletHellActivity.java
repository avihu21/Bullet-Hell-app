package com.tegi.bullethell;

import android.graphics.Point;
import android.os.Bundle;
import android.app.Activity;
import android.view.Display;

//this class is almost exactly
//the same as the pong project

public class BulletHellActivity extends Activity {

    //an instance of the main class of this project
    private BulletHellGame mBHGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get the screen resolution
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        //call the constructor (initialize)
        //the  BulletHellGame instance
        mBHGame = new BulletHellGame(this ,size.x, size.y);
        setContentView(mBHGame);
    }

    @Override
    //start the main game thread
    //when the game is launched
    protected void onResume(){
        super.onResume();

        mBHGame.resume();
    }

    @Override
    //stop the thread when the player quits
    protected void onPause(){
        super.onPause();

        mBHGame.pause();
    }
}
