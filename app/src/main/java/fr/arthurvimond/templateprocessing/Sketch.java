package fr.arthurvimond.templateprocessing;

/**
 * Created by Arthur Vimond on 31/08/2016.
 * Copyright (c) 2016 Arthur Vimond. All rights reserved.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import processing.core.PApplet;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;

public class Sketch extends PApplet implements View.OnTouchListener {

    private static final String TAG = "Sketch";

    Integer screenWidth;
    Integer screenHeight;

    Context context;

    SensorManager sensorManager;
    // Accelerometer
    Sensor accelerometerSensor;
    AccelerometerListener accelerometerListener;
    // Accelerometer data
    float accelerationX, accelerationY, accelerationZ;

    // Gyroscope
    Sensor gyroscopeSensor;
    GyroscopeListener gyroscopeListener;
    // Gyroscope data
    float rotationX, rotationY, rotationZ;

    // Multitouch data
    // Coordinates of all the fingers in one variable (list)
    SparseArray<PointF> touchesList;
    // Coordinates of all the fingers in one variable (array)
    PointF[] touches;
    // Coordinates of all the fingers in independent variables
    Float touchX0, touchX1, touchX2, touchX3, touchX4, touchX5, touchX6, touchX7, touchX8, touchX9;
    Float touchY0, touchY1, touchY2, touchY3, touchY4, touchY5, touchY6, touchY7, touchY8, touchY9;
    // Coordinate when a touch is off
    static final Float TOUCH_OFF = -100f;

    ///////////////////////////////////

    // Processing native function
    // 2- Called before setup()
    @Override
    public void settings() {
        if (screenWidth != null && screenHeight != null) {
            Log.d(TAG, "settings - screenWidth: " + screenWidth + " - screenHeight: " + screenHeight);
            // Fullscreen
            size(screenWidth, screenHeight);
        } else {
            // Default size
            size(1000, 1000);
        }
    }

    // Processing native function
    // 5- Called once at app's creation
    @Override
    public void setup() {
        Log.i(TAG, "setup");

        // Draw black background
        background(0, 0, 0);

    }

    // Processing native function
    // 6- Called every frame (default: 60fps)
    @Override
    public void draw() {

        // Do not remove this function (multitouch purpose)
        setMutlitouchVariables();

        // Draw black background
        background(0, 0, 0);

        // Set shape and color properties for circles
        stroke(255);
        strokeWeight(3);
        noFill();

        // Draw a circle for each finger on screen (using touches list)
        /*for (int i = 0; i < touchesList.size(); i++) {
            if (touchesList.valueAt(i) != null) {
                ellipse(touchesList.valueAt(i).x, touchesList.valueAt(i).y, 100, 100);
            }
        }*/

        // Draw a circle for each finger on screen (using touches array)
        for (int i = 0; i < touches.length; i++) {
            if (touches[i].x != TOUCH_OFF) {
                ellipse(touches[i].x, touches[i].y, 100, 100);
            }
        }

        // Set shape property for texts
        strokeWeight(1);
        // Display accelerometer values
        /*text("accelerationX: " + accelerationX, 10, 20);
        text("accelerationY: " + accelerationY, 10, 50);
        text("accelerationZ: " + accelerationZ, 10, 80);*/

        // Display gyroscope values
        /*text("rotationX: " + rotationX, 300, 20);
        text("rotationY: " + rotationY, 300, 50);
        text("rotationZ: " + rotationZ, 300, 80);*/

    }

    ////////////////////////////////////
    // Fragment lifecycle and listeners

    // 1-
    @Override
    public void onAttach(Activity activity) {
        Log.i(TAG, "onAttach");
        super.onAttach(activity);
        // Get screen size
        screenWidth = ((MainActivity) activity).getScreenWidth();
        screenHeight = ((MainActivity) activity).getScreenHeight();
    }

    // 3-
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        context = getActivity();

        // Initialize sensor manager
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // Initialize Accelerometer
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerListener = new AccelerometerListener();
        // Initialize Accelerometer
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroscopeListener = new GyroscopeListener();

        // Set OnTouch listener
        getSurfaceView().setOnTouchListener(this);

        // Initialize touches list
        touchesList = new SparseArray<>();

        // Initialize touches array
        touches = new PointF[10];
        for (int i = 0; i < touches.length; i++) {
            touches[i] = new PointF(TOUCH_OFF, TOUCH_OFF);
        }
    }

    // 4-
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (sensorManager != null) {
            // Register accelerometer listener
            sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
            // Register gyroscope listener
            sensorManager.registerListener(gyroscopeListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    // 7-
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (sensorManager != null) {
            // Unregister accelerometer listener
            sensorManager.unregisterListener(accelerometerListener);
            // Unregister gyroscope listener
            sensorManager.unregisterListener(gyroscopeListener);
        }
    }

    // 8-
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    // 9-
    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach");
    }

    // Multitouch callback
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        // NB: cf http://www.vogella.com/tutorials/AndroidTouch/article.html

        // Get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // Get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
            case ACTION_DOWN:
            case ACTION_POINTER_DOWN:

                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);
                touchesList.put(pointerId, f);

                break;

            case ACTION_MOVE:

                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    PointF point = touchesList.get(event.getPointerId(i));
                    if (point != null) {
                        point.x = event.getX(i);
                        point.y = event.getY(i);
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                touchesList.remove(pointerId);
                break;
        }

        return true;
    }

    private void setMutlitouchVariables() {

        if (touchesList.size() > 0) {
            touchX0 = touchesList.valueAt(0).x;
            touchY0 = touchesList.valueAt(0).y;
            touches[0] = touchesList.valueAt(0);
        } else {
            touchX0 = touchY0 = TOUCH_OFF;
            touches[0].x = touches[0].y = TOUCH_OFF;
        }

        if (touchesList.size() > 1) {
            touchX1 = touchesList.valueAt(1).x;
            touchY1 = touchesList.valueAt(1).y;
            touches[1] = touchesList.valueAt(1);
        } else {
            touchX1 = touchY1 = TOUCH_OFF;
            touches[1].x = touches[1].y = TOUCH_OFF;
        }

        if (touchesList.size() > 2) {
            touchX2 = touchesList.valueAt(2).x;
            touchY2 = touchesList.valueAt(2).y;
            touches[2] = touchesList.valueAt(2);
        } else {
            touchX2 = touchY2 = TOUCH_OFF;
            touches[2].x = touches[2].y = TOUCH_OFF;
        }

        if (touchesList.size() > 3) {
            touchX3 = touchesList.valueAt(3).x;
            touchY3 = touchesList.valueAt(3).y;
            touches[3] = touchesList.valueAt(3);
        } else {
            touchX3 = touchY3 = TOUCH_OFF;
            touches[3].x = touches[3].y = TOUCH_OFF;
        }

        if (touchesList.size() > 4) {
            touchX4 = touchesList.valueAt(4).x;
            touchY4 = touchesList.valueAt(4).y;
            touches[4] = touchesList.valueAt(4);
        } else {
            touchX4 = touchY4 = TOUCH_OFF;
            touches[4].x = touches[4].y = TOUCH_OFF;
        }

        if (touchesList.size() > 5) {
            touchX5 = touchesList.valueAt(5).x;
            touchY5 = touchesList.valueAt(5).y;
            touches[5] = touchesList.valueAt(5);
        } else {
            touchX5 = touchY5 = TOUCH_OFF;
            touches[5].x = touches[5].y = TOUCH_OFF;
        }

        if (touchesList.size() > 6) {
            touchX6 = touchesList.valueAt(6).x;
            touchY6 = touchesList.valueAt(6).y;
            touches[6] = touchesList.valueAt(6);
        } else {
            touchX6 = touchY6 = TOUCH_OFF;
            touches[6].x = touches[6].y = TOUCH_OFF;
        }

        if (touchesList.size() > 7) {
            touchX7 = touchesList.valueAt(7).x;
            touchY7 = touchesList.valueAt(7).y;
            touches[7] = touchesList.valueAt(7);
        } else {
            touchX7 = touchY7 = TOUCH_OFF;
            touches[7].x = touches[7].y = TOUCH_OFF;
        }

        if (touchesList.size() > 8) {
            touchX8 = touchesList.valueAt(8).x;
            touchY8 = touchesList.valueAt(8).y;
            touches[8] = touchesList.valueAt(8);
        } else {
            touchX8 = touchY8 = TOUCH_OFF;
            touches[8].x = touches[8].y = TOUCH_OFF;
        }

        if (touchesList.size() > 9) {
            touchX9 = touchesList.valueAt(9).x;
            touchY9 = touchesList.valueAt(9).y;
            touches[9] = touchesList.valueAt(9);
        } else {
            touchX9 = touchY9 = TOUCH_OFF;
            touches[9].x = touches[9].y = TOUCH_OFF;
        }

    }

    // Accelerometer listener
    class AccelerometerListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            accelerationX = event.values[0];
            accelerationY = event.values[1];
            accelerationZ = event.values[2];
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    // Gyroscope listener
    class GyroscopeListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            rotationX = event.values[0];
            rotationY = event.values[1];
            rotationZ = event.values[2];
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}