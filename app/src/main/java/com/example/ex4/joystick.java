package com.example.ex4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.MotionEventCompat;

public class joystick extends AppCompatActivity {

    // 'onCreate' implementation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new JoyStickView(this));
    }

    // 'onDestroy' implementation
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        TcpClient.getInstance().sendMessage("yalla_BYE!!!!!!!!!!!!");
        // cancel connection
        TcpClient.getInstance().stopClient();
    }

    // the actual view of the joystick class
    public class JoyStickView extends View  {
        private float x = 0;
        private float y = 0;
        private final float radius = 100;
        private float beginWid;
        private float endWid;
        private float beginHei;
        private float endHei;
        private RectF oval;
        private Boolean playMoving = false;

        //constructor
        public JoyStickView(Context v){
            super(v);
        }

        // the drawing method
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint myPaint = new Paint();
            // black color for the inner circle of the joystick
            myPaint.setColor(Color.rgb(0, 0, 0));
            myPaint.setStrokeWidth(10);

            Paint myPaint2 = new Paint();
            // blue-ish color for the outer oval
            myPaint2.setColor(Color.rgb(0, 50, 100));
            myPaint2.setStrokeWidth(10);

            // green backgroud
            canvas.drawRGB(0,100,100);
            // draw the shapes
            canvas.drawOval(this.oval, myPaint2);
            canvas.drawCircle(this.x, this.y, this.radius, myPaint);

        }
        // define what happens when the screen size changes
        public void onSizeChanged(int w, int h, int oldW, int oldH) {
            super.onSizeChanged(w, h, oldW, oldH);
            this.beginWid = (float)getWidth()/8;
            this.endWid = (float)getWidth()-((float)getWidth()/8);
            this.beginHei = (float)getHeight()/8;
            this.endHei = getHeight()-((float)getHeight()/8);
            this.oval = new RectF(this.beginWid,this.beginHei, this.endWid, this.endHei);
            returnDefault();
        }
        // re-initiate x and y values
        public void returnDefault() {
            this.x = (float)getWidth()/2;
            this.y = (float)getHeight()/2;
        }

        // define what happens when touching the joystick
        public boolean onTouchEvent(MotionEvent event) {
            int action = MotionEventCompat.getActionMasked(event);
            switch (action) {

                case MotionEvent.ACTION_DOWN: {

                    if(CheckIfInside(event.getX(), event.getY())) {
                        this.playMoving = true;
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (!this.playMoving)
                        return true;

                    if (CheckForLimit(event.getX(), event.getY())) {
                        this.x = event.getX();
                        this.y = event.getY();
                        invalidate();
                        ///////////////////////////////////////////////////
                        double normalizedX = normalizeAileron(x);
                        double normalizedY = normalizeElevator(y);
                        TcpClient tcpClient = TcpClient.getInstance();
                        tcpClient.sendMessage("set controls/flight/aileron " + normalizedX + "\r\n");
                        tcpClient.sendMessage("set controls/flight/elevator " + normalizedY + "\r\n");
                    }
                    break;
                }
                //user input's is finished
                case MotionEvent.ACTION_UP :
                    this.playMoving = false;
                    returnDefault();
                    //call on draw
                    invalidate();
            }
            return true;
        }

        Boolean CheckIfInside(float xVal, float yVal) {
            double distance = Math.sqrt((this.x-xVal)*(this.x-xVal) + (this.y-yVal)*(this.y-yVal));
            return (distance <= this.radius);
        }

        // make sure the inner circle of the joystick doesn't exit the limits of the oval
        Boolean CheckForLimit(float xVal, float yVal) {
            double xCalc = Math.pow(xVal - this.oval.centerX(), 2) / Math.pow((this.oval.width() / 2), 2);
            double yCalc = Math.pow(yVal - this.oval.centerY(), 2) / Math.pow((this.oval.height() / 2), 2);
            xCalc += yCalc;
            if (xCalc <= 1) {
                return true;
            } else {
                return false;
            }
        }

        // normalize aileron between o and 1
        public float normalizeAileron(float x) {
            return (x-((this.beginWid +this.endWid)/2))/((this.endWid-this.beginWid)/2);
        }

        // normalize elevator between o and 1
        public float normalizeElevator(float y) {
            return (y-((this.beginHei +this.endHei)/2))/((this.beginHei -this.endHei)/2);
        }
    }

}
