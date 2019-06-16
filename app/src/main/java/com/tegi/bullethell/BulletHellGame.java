package com.tegi.bullethell;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;
import android.graphics.RectF;

class BulletHellGame extends SurfaceView implements Runnable {

    //are we currently debugging?
    boolean mDebugging = true;

    //objects for the game loop/thread
    private Thread mGameThread = null;
    private volatile boolean mPlaying;
    private boolean mPaused = true;

    //objects for drawing
    private SurfaceHolder mOurHolder;
    private Canvas mCanvas;
    private Paint mPaint;

    //keep track of the frame rate
    private long mFPS;
    //the number of milliseconds in a second
    private final int MILLIS_IN_SECOND = 1000;

    //holds the resolution of the screen
    private int mScreenX;
    private int mScreenY;

    //how big will the text be?
    private int mFontSize;
    private int mFontMargin;

    //these are for the sound
    private SoundPool mSP;
    private int mBeepID = -1;
    private int mTeleportID = -1;

    //up to 10000 bullets
    private Bullet[] mBullets = new Bullet[10000];
    private int mNumBullets = 0;
    private int mSpawnRate = 1;

    private Random mRandomX = new Random();
    private Random mRandomY = new Random();

    private Bob mBob;
    private boolean mHit = false;
    private int mNumHits;
    private int mShield = 10;

    //lets time the game
    private long mStartGameTime;
    private long mBestGameTime;
    private long mTotalGameTime;

    //this is constructor method that called
    //from BulletHellActivity
    public BulletHellGame(Context context,int x ,int y){
        super(context);

        mScreenX = x;
        mScreenY = y;
        //font is 5% of the screen width
        mFontSize = mScreenX/20;
        //margin is 2% of screen width
        mFontMargin = mScreenX/50;

        mOurHolder = getHolder();
        mPaint = new Paint();

        //initialize the sound pool
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            mSP = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
        }else{
            mSP = new SoundPool(5,AudioManager.STREAM_MUSIC,0);
        }

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("beep.ogg");
            mBeepID = mSP.load(descriptor,0);

            descriptor = assetManager.openFd("teleport.ogg");
            mTeleportID = mSP.load(descriptor,0);
        }catch (IOException e){
            Log.e("error","failed to load sound files");
        }

        for(int i = 0;i < mBullets.length;i++){
            mBullets[i] = new Bullet(mScreenX);
        }

        mBob = new Bob(context,mScreenX,mScreenY);

        startGame();
    }

    //called to start a new game


    public void startGame(){
        mNumHits = 0;
        mNumBullets = 0;
        mHit = false;

        //did the player survive longer than previously
        if(mTotalGameTime > mBestGameTime){
            mBestGameTime = mTotalGameTime;
        }

    }

    //spawn ANOTHER bullet
    private void spawnBullet(){
        //add one to the chamber of bullets
        mNumBullets++;

        //where to spawn the next bullet
        //and in wich direction should it travel
        int spawnX;
        int spawnY;
        int velocityX;
        int velocityY;

        //this code will change in chapter 13

        /*
        //pick a random point on the screen
        //to spawn a buullet
        spawnX = mRandomX.nextInt(mScreenX);
        spawnY = mRandomY.nextInt(mScreenY);

        //the horizontal direction of travel
        velocityX = 1;
        //randomly make velocityX negetive
        if(mRandomX.nextInt(2)==0){
            velocityX = -1;
        }

        velocityY = 1;
        //randomly make velocityY negetive
        if(mRandomY.nextInt(2)==0){
            velocityY = -1;
        }
        */

        //dont spawn to close to bob
        if(mBob.getmRect().centerX() < mScreenX/2){

            //bob is on the left
            //spawn bullet on the right
            spawnX = mRandomX.nextInt(mScreenX/2)+mScreenX/2;
            //head right
            velocityX = 1;
        }else{
            //bob is on the right
            //spawn bullet left
            spawnX = mRandomX.nextInt(mScreenX/2);
            //head left
            velocityX = -1;
        }

        //dont spawn to close to bob
        if(mBob.getmRect().centerY() < mScreenY/2){

            //bob is on the top
            //spawn bullet on the bottom
            spawnY = mRandomY.nextInt(mScreenY/2)+mScreenY/2;
            //head down
            velocityY = 1;
        }else{
            //bob is on the bottom
            //spawn bullet on the top
            spawnY = mRandomY.nextInt(mScreenY/2);
            //head up
            velocityY = -1;
        }

        //spawn the bullet
        mBullets[mNumBullets - 1].spawn(spawnX,spawnY,velocityX,velocityY);

    }

    //handles the game loop
    @Override
    public void run(){
        while(mPlaying){

            long frameStartTime = System.currentTimeMillis();

            if(!mPaused){
                update();
                //now all tyhe bullets have been moved
                //we can detect any collisions
                detectCollisions();
            }

            draw();

            long timeThisFrame = System.currentTimeMillis() - frameStartTime;
            if(timeThisFrame>=1){
                mFPS = MILLIS_IN_SECOND/timeThisFrame;
            }
        }
    }

    //update all thje game objects

    private void update(){
        for(int i = 0;i < mNumBullets;i++){
            mBullets[i].update(mFPS);
        }

    }

    private void detectCollisions(){
        //has a bullet collided with a wall
        //loop through each active bullet in turn
        for(int i = 0;i < mNumBullets;i++){
            if(mBullets[i].getmRect().bottom > mScreenY){
                mBullets[i].reverseYVelocity();
            }
            else if (mBullets[i].getmRect().top < 0){
                mBullets[i].reverseYVelocity();
            }
            else if(mBullets[i].getmRect().left < 0){
                mBullets[i].reverseXVelocity();
            }
            else if(mBullets[i].getmRect().right > mScreenX){
                mBullets[i].reverseXVelocity();
            }
        }

        //has a bullet hit bob?
        //check each bullet for an intersection with bob's RectF
        for(int i = 0;i < mNumBullets;i++){
            if(RectF.intersects(mBullets[i].getmRect(),mBob.getmRect())){
                //bob has been hit
                mSP.play(mBeepID,1,1,0,0,1);

                //this flages that a hit occurred
                //so that the draw
                //method "knows" as well
                mHit = true;

                //rebound the bullet that collided
                mBullets[i].reverseXVelocity();
                mBullets[i].reverseYVelocity();

                //keep track of the number of hits
                mNumHits++;

                if(mNumHits == mShield){
                    mPaused = true;
                    mTotalGameTime = System.currentTimeMillis() - mStartGameTime;

                    startGame();
                }
            }
        }

    }

    private void draw(){
        if (mOurHolder.getSurface().isValid()){
            mCanvas = mOurHolder.lockCanvas();
            mCanvas.drawColor(Color.argb(255,243,111,36));
            mPaint.setColor(Color.argb(255,255,255,255));

            //all the drawing code will go here
            for(int i = 0;i < mNumBullets;i++){
                mCanvas.drawRect(mBullets[i].getmRect(),mPaint);
            }

            mCanvas.drawBitmap(mBob.getmBitmap(),mBob.getmRect().left,mBob.getmRect().top,mPaint);

            mPaint.setTextSize(mFontSize);
            mCanvas.drawText("Bullets:" + mNumBullets + "Best Time:" + mBestGameTime/MILLIS_IN_SECOND,mFontMargin,mFontSize,mPaint);

            //dont draw the current time when paused
            if(!mPaused){
                mCanvas.drawText("Seconds Survived:" + ((System.currentTimeMillis() - mStartGameTime)/MILLIS_IN_SECOND),mFontMargin,mFontMargin*30,mPaint);
            }

            if(mDebugging){
                printDebuggingText();
            }

            mOurHolder.unlockCanvasAndPost(mCanvas);

        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:

                if (mPaused) {
                    mStartGameTime = System.currentTimeMillis();
                    mPaused = false;
                }
                if(mBob.teleport(motionEvent.getX(),motionEvent.getY())){
                    mSP.play(mTeleportID,1,1,0,0,1);
                }
                break;

            case MotionEvent.ACTION_UP:

                mBob.setTeleportAvailable();
                spawnBullet();
                break;
        }

        return true;

      /*  mPaused = false;
        spawnBullet();

        return true; */
    }

    public void pause(){
        mPlaying = false;
        try{
            mGameThread.join();
        }catch (InterruptedException e){
            Log.e("error:","joining thread");
        }
    }
    public void resume(){
        mPlaying = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    private void printDebuggingText(){
        int debugSize = 35;
        int debugStart = 150;
        mPaint.setTextSize(debugSize);

        mCanvas.drawText("FPS" + mFPS,10,debugStart + debugSize,mPaint);
        mCanvas.drawText("Bob left:" + mBob.getmRect().left,10 ,debugStart +debugSize*2,mPaint);
        mCanvas.drawText("Bob top:" + mBob.getmRect().top,10 ,debugStart +debugSize*3,mPaint);
        mCanvas.drawText("Bob right:" + mBob.getmRect().right,10 ,debugStart +debugSize*4,mPaint);
        mCanvas.drawText("Bob bottom:" + mBob.getmRect().bottom,10 ,debugStart +debugSize*5,mPaint);
        mCanvas.drawText("Bob centerX:" + mBob.getmRect().centerX(),10 ,debugStart +debugSize*6,mPaint);
        mCanvas.drawText("Bob centerY:" + mBob.getmRect().centerY(),10 ,debugStart +debugSize*7,mPaint);
    }

}
