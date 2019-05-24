package com.tegi.bullethell;

import android.graphics.RectF;

class Bullet {

    //a RectF to represent the size and location of the bullet
    private RectF mRect;

    //how fast is the bullet traveling
    private float mXVelocity;
    private float mYVelocity;

    //how big is a bullet
    private float mWidth;
    private float mHight;

    //the constructor
    Bullet(int screenX){

        //configure the bullet bassed on
        //the screen width in pixels
        mWidth = screenX/100;
        mHight = screenX/100;
        mRect = new RectF();
        mYVelocity = (screenX/5);
        mXVelocity = (screenX/5);
    }

    //return a reference to the RectF
    RectF getmRect(){
        return  mRect;
    }

    //move the bullet based on the speed and the frame rate
    void update(long fps){
        //update the left and top coordinates
        //based on the velocity and currentframe rate
        mRect.left = mRect.left + (mXVelocity/fps);
        mRect.top = mRect.top + (mYVelocity/fps);

        mRect.right = mRect.left + mWidth;
        mRect.bottom = mRect.top + mHight;
    }

    //reverse the bullet vertical direction
    void reverseYVelocity (){
        mYVelocity = -mYVelocity;
    }

    //reverse the bullet horizontla direction
    void reverseXVelocity (){
        mXVelocity = -mXVelocity;
    }

    //spawn a new bullet
    void spawn(int pX,int pY,int vX,int vY){

        //spawn the bullet at the location
        //passed in as parameters
        mRect.left = pX;
        mRect.top = pY;
        mRect.right = pX + mWidth;
        mRect.bottom = pY + mHight;

        //head away from the player
        //its only fair
        mXVelocity = mXVelocity * vX;
        mYVelocity = mYVelocity * vY;
    }
}
