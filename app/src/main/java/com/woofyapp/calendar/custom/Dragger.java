package com.woofyapp.calendar.custom;

import android.content.Context;
import android.graphics.*;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.woofyapp.calendar.R;

/**
 * Created by rujul on 3/14/2016.
 */
public class Dragger extends View {

    float centerX = -1,centerY = -1,width=0,height=0,startsAt;
    RectF rectF;
    Paint pRect,pImage;
    int colorOrange;
    Bitmap bUp,bDown;
    float[] fDown;
    float[] fUp; // 0=left,1=top
    float[] fRect; // 0=left,1=top,2=right,3=bottom
    int pointerId;
    final int INVALID_ID = -1;
    boolean isUp = false,isDown = false;
    private final String TAG  = "DRAGGER_LOG";
    private final int MIN_HEIGHT = 90;
    public int id;
    Paint pText;
    private DraggerInterface view;
    private final int startTime = 7;


    public Dragger(Context context) {
        super(context);
        colorOrange = ContextCompat.getColor(context, R.color.colorOrange);

        pImage = new Paint();
        pImage.setColor(Color.RED);

        pRect = new Paint();
        pRect.setColor(colorOrange);
        setLayerType(LAYER_TYPE_SOFTWARE, pRect);

        pText = new Paint();
        pText.setColor(Color.WHITE);
        pText.setTextSize(20);
        pText.setFlags(Paint.ANTI_ALIAS_FLAG);
        fRect = new float[4];
        fUp = new float[2];
        fDown = new float[2];
    }

//    public float getRectHeight(){
//        float rectHeight = Math.abs(fRect[1] - fRect[3]);
//        if (rectHeight < MIN_HEIGHT) {
//
//                if(isUp){
//                    fRect[1] -= MIN_HEIGHT - rectHeight + 1;
//                    setUpDown();
//                    invalidate();
//                }else if(isDown){
//                    fRect[3] += MIN_HEIGHT - rectHeight + 1;
//                    setUpDown();
//                    invalidate();
//                }
//            return MIN_HEIGHT-1;
//        }
//        return rectHeight;
//    }

    public void setParams(float width,float height){
        this.width = width;
        this.height = height;
    }


    public void setCenter(float cx,float cy){
        this.centerX = cx;
        this.centerY = cy;


        fRect[0] = centerX - (width/2);
        fRect[1] = centerY - (height/2);
        fRect[2] = centerX + (width/2);
        fRect[3] = centerY + (height/2);

        setUpDown();
    }


    private void setUpDown(){

        fUp[0] = ((fRect[0] + fRect[2])/2) - 8;
        fDown[0] = fUp[0];

        fUp[1] = fRect[1] - 8;
        fDown[1] = fRect[3] -8;
    }
    private void isUpTouched(float x,float y){
        isUp = (x>=fUp[0]-32 && x<=fUp[0]+48 && y>=fUp[1]-32 && y<=fUp[1]+48);
    }

    private void isDownTouched(float x,float y){
        isDown = (x>=fDown[0]-32 && x<=fDown[0]+48 && y>=fDown[1]-32&& y<=fDown[1]+48);
    }

    @Override
    protected void onDraw(Canvas canvas) throws NullPointerException {
        super.onDraw(canvas);

        if(centerX == -1 || centerY == -1)
            throw new NullPointerException();


        rectF = new RectF(fRect[0],fRect[1],fRect[2],fRect[3]);
        canvas.drawRect(rectF,pRect);


        bUp= BitmapFactory.decodeResource(getResources(), R.drawable.ic_up);
        canvas.drawBitmap(bUp, fUp[0], fUp[1], pImage);

        bDown = BitmapFactory.decodeResource(getResources(),R.drawable.ic_down);
        canvas.drawBitmap(bDown, fDown[0], fDown[1], pImage);

        String start = getTime(fRect[1]);
        String end = getTime(fRect[3]);


        canvas.drawText(start+" - "+end,((fRect[0]+fRect[2])/2)-20,(fRect[1]+fRect[3])/2,pText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        switch (action){
            case MotionEvent.ACTION_DOWN : {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);

                final float x = MotionEventCompat.getX(event, pointerIndex);
                final float y = MotionEventCompat.getY(event,pointerIndex);

                isUpTouched(x, y);
                isDownTouched(x,y);



                if(!isUp && !isDown)
                    return false;



                pointerId = MotionEventCompat.getPointerId(event,pointerIndex);
                break;
            }

            case MotionEvent.ACTION_CANCEL : {
                pointerId = INVALID_ID;
                break;
            }

            case MotionEvent.ACTION_UP:{
                pointerId = INVALID_ID;
                break;
            }

            case MotionEvent.ACTION_MOVE : {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);

                final float y = MotionEventCompat.getY(event, pointerIndex);




                float rectHeight = Math.abs(fRect[1] - fRect[3]);

                    if (rectHeight > MIN_HEIGHT) {
                        if (isUp) {
                            fRect[1] = y + 8;
                            change();
                        } else if (isDown) {
                            fRect[3] = y + 8;
                            change();
                        }
                    } else {
                        if (isUp) {
                            fRect[1] = fRect[3] - MIN_HEIGHT - 2;
                            change();
                        } else if (isDown) {
                            fRect[3] = fRect[1] + MIN_HEIGHT + 2;
                            change();
                        }

                    }


                break;

            }

            case MotionEvent.ACTION_POINTER_UP : {
                final int pointerINdex = MotionEventCompat.getActionIndex(event);
                final int newpointerId  = MotionEventCompat.getPointerId(event, pointerINdex);
                if(newpointerId== pointerId){
                    final int newPointerIndex = pointerINdex==0?1:0;
                    pointerId = MotionEventCompat.getPointerId(event,newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private void change(){
        setUpDown();
        view.checkBounds(this);
        invalidate();
    }

    public void setTop(float y){
        fRect[1] = y;
        setUpDown();
        invalidate();
    }

    public String getTime(float top){
        int minutes = (int)((top - startsAt)/(height/60))%60;
        int hours = ((int)((top-startsAt)/(height/60))/60)+startTime;
        String min = (minutes < 10)?"0"+minutes:""+minutes;
        String hr = (hours<10)?"0"+hours:""+hours;

        return hr+":"+min;
    }
    public void setBottom(float y){
        fRect[3] = y;
        setUpDown();
        invalidate();
    }

    public float getRectTop(){
        return this.fRect[1];
    }

    public float getRectBootom(){
        return this.fRect[3];
    }


    public void removeView(){
        ViewGroup vg = (ViewGroup)this.getParent();
        vg.removeView(this);
    }

    public boolean getUpState(){
        return isUp;
    }

    private void log(String message){
        Log.d(TAG,message);
    }
    
    public void addView(DraggerInterface view){
        this.view = view;
    }

    public void viewStartsAt(int startsAt) {
        this.startsAt = startsAt;
    }

    public interface DraggerInterface{
        public void checkBounds(Dragger dragger);
    }

}
