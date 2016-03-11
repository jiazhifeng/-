package com.jzf.record;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jzf.record.R;

public class MainActivity extends Activity {
	// 调试信息
	private static final String TAG = "info信息";
	// 设置间隔时间
	private long UPTATE_INTERVAL_TIME = 1000;
	// 设置速率
	private double SPEED_SHRESHOLD = 7;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SharedPreferences mSP;
	// 上一次产生事件的时间
	private long lastUpdateTime;

	// 传感器监听器
	private SensorEventListener mSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// 判断事件是否是加速度传感器
			long currentUpdateTime = System.currentTimeMillis();

			long timeInterval = currentUpdateTime - lastUpdateTime;
			Log.i(TAG, "timeInterval= " + timeInterval);
			setText(R.id.tv_record_X, "间隔时间(ms): " + timeInterval);
			if (timeInterval < UPTATE_INTERVAL_TIME)
				return;
			lastUpdateTime = currentUpdateTime;
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			mSP = getSharedPreferences("record", MODE_PRIVATE);
			float lastX = mSP.getFloat("x", 0.0f);
			float lastY = mSP.getFloat("y", 0.0f);
			float lastZ = mSP.getFloat("z", 0.0f);
			saveInfo(x, y, z);

			float deltaX = x - lastX;
			float deltaY = y - lastY;
			float deltaZ = z - lastZ;

			/*
			 * lastX = x; lastY = y; lastZ = z;
			 */
			double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
					* deltaZ)
					/ timeInterval * 10000;
			Log.v("thelog", "===========log===================");
			Log.i(TAG, "speed= " + speed);
			setText(R.id.tv_record_Y, "速率为: " + speed);
			if (speed >= SPEED_SHRESHOLD) {
				// onShakeListener.onShake();
				mSP = getSharedPreferences("record", MODE_PRIVATE);
				int path = mSP.getInt("path", 1);
				setText(R.id.tv_record_current, "正在走路...第" + path + "步");
				Editor editor = mSP.edit();
				editor.putInt("path", path + 1);
				editor.commit();
			}
		}

		private void saveInfo(float x, float y, float z) {
			// 储存数据
			mSP = getSharedPreferences("record", MODE_PRIVATE);
			Editor editor = mSP.edit();
			editor.putFloat("x", x);
			editor.putFloat("y", y);
			editor.putFloat("z", z);
			editor.commit();
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 获取传感器管理器
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 获取加速度传感器
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(mSensorEventListener);
	}

	// 根据id设置文本的内容
	public void setText(int id, String text) {
		TextView tv = (TextView) findViewById(id);
		tv.setVisibility(TextView.VISIBLE);
		tv.setText(text);
	}

	/**
	 * 开始计数
	 * 
	 * @param v
	 */
	public void begin(View v) {
		// 注册传感器管理器监听事件
		mSensorManager.registerListener(mSensorEventListener, mSensor,
				mSensorManager.SENSOR_DELAY_NORMAL);
		lastUpdateTime = System.currentTimeMillis();
	}

	/**
	 * 重置计数
	 * @param v
	 */
	public void reset(View v) {
		// 储存数据
		mSP = getSharedPreferences("record", MODE_PRIVATE);
		Editor editor = mSP.edit();
		editor.putInt("path", 0);
		editor.commit();

	}
	/**
	 * 修改间隔时间
	 * @param v
	 */
	public void data1(View v) {
		EditText et_interval = (EditText) findViewById(R.id.et_interval);
		UPTATE_INTERVAL_TIME = Long.parseLong(et_interval.getText().toString());
	}
	/**
	 * 修改速率
	 * @param v
	 */
	public void data2(View v) {
		EditText et_speed = (EditText) findViewById(R.id.et_speed);
		SPEED_SHRESHOLD = Double.parseDouble(et_speed.getText().toString());
	}

}
