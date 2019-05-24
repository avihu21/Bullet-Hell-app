package com.tegi.bullethell;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

class Bob {

    RectF mRect;
    float mBobHight;
    float mBobWidth;
    boolean mTeleporting = false;

    Bitmap mBitmap;

    public Bob(Context context,float screenX,float screenY){
        mBobHight = screenY/10;
        mBobWidth = mBobHight/2;

        mRect = new RectF(screenX/2,screenY/2,(screenX/2)+mBobWidth,(screenY/2)+mBobHight);

        //prepare the bitmap
        //load from his .png file
        //bob practices responsible encapsulation
        //looking after his own resource
        mBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawableנob); //need to add bob.png to get rid of this error

    }

    boolean teleport(float newX,float newY){
        //did bob manage to teleport
        boolean success = false;

        //move bob to the new position
        //if not already teleporting
        if(!mTeleporting){
            //make him roughly central to the touch
            mRect.left = newX - mBobWidth/2;
            mRect.top = newY - mBobHight/2;
            mRect.bottom = mRect.top + mBobHight;
            mRect.right = mRect.left + mBobWidth;

            mTeleporting = true;

            //notify BulletHellGame that teleport
            //attempt was successful
            success = true;
        }
        return success;
    }

    void setTeleportAvailable(){
        mTeleporting = false;
    }

    //return a reference to mRect
    RectF getmRect(){
        return mRect;
    }

    //return a reference to bitmap
    Bitmap getmBitmap(){
        return mBitmap;
    }


}
