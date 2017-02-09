package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Sensor linearAccelero;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        enabling Firebase's offline mode
        database.setPersistenceEnabled(true);
        DatabaseReference myRef = database.getReference("testMessage");
//        DatabaseReference myRef = database.getReferenceFromUrl("https://testapp-102e7.firebaseio.com/message");

        myRef.push().setValue("Hello, World!");

        FirebaseStorage storage = FirebaseStorage.getInstance();

        mSensorManager = ((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        linearAccelero = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
}
