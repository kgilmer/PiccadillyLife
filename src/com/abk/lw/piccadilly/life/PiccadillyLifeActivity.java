/*
 *   Copyright 2013 Ken Gilmer
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.abk.lw.piccadilly.life;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;

/**
 * @author kgilmer
 *
 */
public class PiccadillyLifeActivity extends Activity implements Handler.Callback, OnTouchListener, SensorEventListener {
	
	private static final String TAG = PiccadillyLifeActivity.class.getSimpleName();
	
	/**
	 * Reference to model root
	 */
	private PiccadillyLifeModelRoot model;
	
	/**
	 * Reference to UI view
	 */
	private PiccadillyLifeView view;
	
	private Handler handler;
	private static final int MESSAGE_ID = Integer.MIN_VALUE;
	
	private long lastUpdateTime;

    private SensorManager sensorManager;

    private Sensor gravitySensor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		model = (PiccadillyLifeModelRoot) getLastNonConfigurationInstance();
		if (model == null) {
			model = new PiccadillyLifeModelRoot();
		}
		view = new PiccadillyLifeView(this);
		view.setOnTouchListener(this);
		view.setModel(model);
		setContentView(view);
		
		handler = new Handler(this);
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return model;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		handler.sendEmptyMessage(MESSAGE_ID);
		lastUpdateTime = SystemClock.uptimeMillis();
		sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		handler.removeMessages(MESSAGE_ID);
		sensorManager.unregisterListener(this);
	}

	public boolean handleMessage(Message msg) {
		if (msg.what == MESSAGE_ID) {
			update();
			handler.sendEmptyMessageDelayed(MESSAGE_ID, 10);
			return true;
		}
		return false;
	}
	
	private void update() {
	    new UpdateWorldTask().execute(null, null, null);
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		float viewportSize = PiccadillyLifeView.VIEWPORT_SIZE;
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		float x = (event.getX(pointerIndex) - v.getWidth() / 2) * viewportSize / v.getWidth();
		float y = (event.getY(pointerIndex) - v.getHeight() / 2) * viewportSize / v.getWidth();
		int pointerId = event.getPointerId(pointerIndex);
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
			Log.i(TAG, "down: " + pointerId + " " + x + " " + y);
			model.userActionStart(pointerId, x, y);
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			for (int i = 0; i < event.getPointerCount(); i++) {
				x = (event.getX(i) - v.getWidth() / 2) * viewportSize / v.getWidth();
				y = (event.getY(i) - v.getHeight() / 2) * viewportSize / v.getWidth();
				pointerId = event.getPointerId(i);
				//Log.i(TAG, "move: " + pointerId + " " + x + " " + y);
				model.userActionUpdate(pointerId, x, y);
			}
		}
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
			Log.i(TAG, "up: " + pointerId + " " + x + " " + y);
			model.userActionEnd(pointerId, x, y);
		}
		return true;
	}
	
	private class UpdateWorldTask extends AsyncTask<Void, Void, Void> {

        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Void doInBackground(Void... params) {
            long currentTime = SystemClock.uptimeMillis();
            long elapsed = currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;
            model.update(elapsed);
            return null;
        }
	    
        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Void result) {
            view.invalidate();
        }
	}

    /* (non-Javadoc)
     * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }

    /* (non-Javadoc)
     * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];

        model.worldForce(y / 20f, x / 20f);
    }
}
