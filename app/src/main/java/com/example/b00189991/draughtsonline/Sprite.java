package com.example.b00189991.draughtsonline;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by B00189991 on 21/04/2016.
 */
public class Sprite {

    private Bitmap bmp;
    private Bitmap scaledBMP;
    private int x = 0;
    private int y = 0;
    private int width;
    private int height;
    private boolean isActive = true;
    private boolean isTouched = false;
    private boolean isKing = false;
    private boolean isRed = false;

    public Sprite(Bitmap bmp, int x, int y, Integer width, Integer height){

        if(width != null){

            //A value was passed to set the width and height.
            this.width = width;
        }else {
            //No values were passed so use default width of image.
            this.width = bmp.getWidth();
        }

        if(height != null){

            this.height = height;
        }else{

            this.height = bmp.getHeight();
        }

        this.bmp = bmp;
        this.scaledBMP = Bitmap.createScaledBitmap(this.bmp, this.width, this.height, true);

        this.x = x;
        this.y = y;

    }

    private void update(){

    }


    public void onDraw(Canvas canvas){

        update();
        canvas.drawBitmap(this.scaledBMP, x, y, null);
    }


    public int getX(){

        return this.x;
    }

    public int getY(){

        return this.y;
    }

    public int getWidth(){

        return this.scaledBMP.getWidth();
    }

    public int getHeight(){

        return this.scaledBMP.getHeight();
    }

    public boolean isHit(float touchX, float touchY){

        if(touchX > this.x && touchX < this.x + this.width){

            if(touchY > this.y && touchY < this.y + this.height){

                return true;
            }
        }
        return false;
    }

    public boolean getTouched(){

        return this.isTouched;
    }

    public void makeTouched(){

        this.isTouched = true;
    }

    public void makeNotTouched(){

        this.isTouched = false;
    }

    public void makeInactive(){

        this.isActive = false;
    }

    public void moveX(float newX){

        this.x = (int)newX;
    }

    public void moveY(float newY){

        this.y = (int)newY;
    }

    public void setKing(){

        this.isKing = true;
    }

    public boolean isKing(){

        return isKing;
    }

    public void setRed(boolean value){

        this.isRed = value;
    };

    public boolean isRed(){

        return this.isRed;
    }

    public boolean isActive(){

        return this.isActive;
    }

    public void makeActive(){

        this.isActive = true;
    }
}
