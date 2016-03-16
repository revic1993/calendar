package com.woofyapp.calendar;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.woofyapp.calendar.custom.Dragger;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements Dragger.DraggerInterface {

    CoordinatorLayout clLayot;
    GestureDetector detector;
    MaterialCalendarView calendar;
    ArrayList<Dragger> draggers;
    RelativeLayout rlSelectable;
    int width;
    float centerX;
    String TAG = "MAIN_DRAGGER";
    public int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);

        rlSelectable = (RelativeLayout) findViewById(R.id.rlSelectable);
        draggers = new ArrayList<Dragger>();

        detector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int index  = MotionEventCompat.getActionIndex(e);
                float x = MotionEventCompat.getX(e, index);
                float y = MotionEventCompat.getY(e, index);
                addDragger(centerX,y);
                return true;
            }
        });
//
//        clLayot.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return detector.onTouchEvent(event);
//            }
//        });



        rlSelectable.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        width = rlSelectable.getWidth();
        centerX = (rlSelectable.getRight()-rlSelectable.getLeft())/2;
    }


    public void addDragger(float x,float y) {
        Dragger dragger = new Dragger(MainActivity.this);
        dragger.setWidth(width);
        dragger.setCenter(x, y);
        dragger.id = count++;
        draggers.add(dragger);
        dragger.addView(this);
        rlSelectable.addView(dragger);
    }




    private void log(String message){
        Log.d(TAG,message);
    }

    @Override
    public void checkBounds(Dragger current) {
        int currentId = current.id;

        if(draggers.size()>1){
            for(Dragger dragger : draggers){
                if(currentId !=dragger.id){
                    if(current.getUpState() && current.getRectTop()<dragger.getRectBootom() && current.getRectBootom() > dragger.getRectTop()){
                        dragger.setBottom(current.getRectBootom());
                        current.removeView();
                        draggers.remove(current);
                        break;
                    }else if(!current.getUpState() && current.getRectBootom() > dragger.getRectTop() && current.getRectTop() < dragger.getRectBootom()){
                        dragger.setTop(current.getRectTop());
                        current.removeView();
                        draggers.remove(current);
                        break;
                    }
                }
            }
        }
    }
}
