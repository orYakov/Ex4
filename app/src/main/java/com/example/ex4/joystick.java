package com.example.ex4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.core.view.MotionEventCompat;

public class joystick extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new JoyStickView(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TcpClient.getInstance().sendMessage("yalla_BYE!!!!!!!!!!!!");
        TcpClient.getInstance().stopClient();
    }

    public class JoyStickView extends View  {
        private float x = 0;
        private float y = 0;
        private final float radius = 100;
        private float startWid;
        private float endWid;
        private float startHei;
        private float endHei;
        private RectF oval;
        private Boolean playMoving = false;

        public JoyStickView(Context v){
            super(v);
        }

        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint myPaint = new Paint();
            myPaint.setColor(Color.rgb(0, 0, 0));
            myPaint.setStrokeWidth(10);

            Paint myPaint2 = new Paint();
            myPaint2.setColor(Color.rgb(0, 50, 100));
            myPaint2.setStrokeWidth(10);

            canvas.drawRGB(0,100,100);
            canvas.drawOval(this.oval, myPaint2);
            canvas.drawCircle(this.x, this.y, this.radius, myPaint);

        }
        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            this.startWid = (float)getWidth()/8;
            this.endWid = (float)getWidth()-((float)getWidth()/8);
            this.startHei = (float)getHeight()/8;
            this.endHei = getHeight()-((float)getHeight()/8);
            this.oval = new RectF(this.startWid,this.startHei , this.endWid, this.endHei);
            returnDefualt();
        }
        public void returnDefualt() {
            this.x = (float)getWidth()/2;
            this.y = (float)getHeight()/2;
        }

        public boolean onTouchEvent(MotionEvent event) {
            int action = MotionEventCompat.getActionMasked(event);
            switch (action) {
                //if the user touched the screen
                case MotionEvent.ACTION_DOWN: {
                    //check if the input is inside the circle
                    if(CheckIfInside(event.getX(), event.getY())) {
                        this.playMoving = true;
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (!this.playMoving)
                        return true;
                    //make sure user input is inside limits
                    if (CheckForLimit(event.getX(), event.getY())) {
                        this.x = event.getX();
                        this.y = event.getY();
                        invalidate();
                        ///////////////////////////////////////////////////
                        double xl = normelizeAilron(x);
                        double yl = normelizeElevator(y);
                        TcpClient tcpClient = TcpClient.getInstance();
                        tcpClient.sendMessage("set controls/flight/aileron " + xl + "\r\n");
                        tcpClient.sendMessage("set controls/flight/elevator " + yl + "\r\n");
                    }
                    break;
                }
                //user input's is finished
                case MotionEvent.ACTION_UP :
                    this.playMoving = false;
                    returnDefualt();
                    //call on draw
                    invalidate();
            }
            return true;
        }

        /**
         * check if user touching inside the circle
         * @param xVal
         * @param yVal
         * @return
         */
        Boolean CheckIfInside(float xVal, float yVal) {
            double distance = Math.sqrt((this.x-xVal)*(this.x-xVal) + (this.y-yVal)*(this.y-yVal));
            return (distance <= this.radius);
        }

        /**
         * make sure give x,y inside the oval shape
         * @param xVal
         * @param yVal
         * @return
         */
        Boolean CheckForLimit(float xVal, float yVal) {
            //return (this.oval.contains(xVal, yVal));
            double xCalc = Math.pow(xVal - this.oval.centerX(), 2) / Math.pow((this.oval.width() / 2), 2);
            double yCalc = Math.pow(yVal - this.oval.centerY(), 2) / Math.pow((this.oval.height() / 2), 2);
            xCalc += yCalc;
            if (xCalc <= 1) {
                return true;
            } else {
                return false;
            }
        }

        public float normelizeAilron(float x) {
            return (x-((this.startWid+this.endWid)/2))/((this.endWid-this.startWid)/2);
        }

        public float normelizeElevator(float y) {
            return (y-((this.startHei+this.endHei)/2))/((this.startHei-this.endHei)/2);
        }
    }

}
