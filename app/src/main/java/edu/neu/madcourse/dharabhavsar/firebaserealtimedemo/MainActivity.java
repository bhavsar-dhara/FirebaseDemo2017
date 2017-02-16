package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Sensor linearAccelero;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    private TextView text1;
    private StringBuffer textStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        enabling Firebase's offline mode
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        text1 = (TextView) findViewById(R.id.text1);



        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("testMessage");
//        DatabaseReference myRef = database.getReferenceFromUrl("https://testapp-102e7.firebaseio.com/message");

        myRef.push().setValue("Hello, World!!!");

        mSensorManager = ((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        linearAccelero = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRecording();
    }

    @Override
    public void onResume(){
        super.onResume();
        recordAccelerometerData();
    }

    private void recordAccelerometerData() {
        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                Log.e(TAG, "onSensorChanged: getType: " + sensorEvent.sensor.getType()
                        + " accuracy: " + sensorEvent.accuracy
                        + " timestamp: " + sensorEvent.timestamp);
                int i = 1;
                for (float f : sensorEvent.values) {
                    Log.e(TAG, "onSensorChanged: value : " + i++ + " : " + f);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                Log.d(TAG, "onAccuracyChanged: ");
            }
        };

        mSensorManager.registerListener(mSensorListener,
                linearAccelero, SensorManager.SENSOR_DELAY_UI);
    }

    private void stopRecording() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorListener);
            mSensorListener = null;
        }
    }
}
