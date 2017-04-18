package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Dhara on 4/18/2017.
 */

public class ConnectToFirebaseDatabase {
    private static final String TAG = ConnectToFirebaseDatabase.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase;

    private ConnectToFirebaseDatabase() {

    }

    public static FirebaseDatabase instance(Context context) {
        Log.d(TAG, "instance:  " + context);
        if (firebaseDatabase == null) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDCx1090HtVx5ViVxnQ9O0UAyeGxX9nV2E")
                    .setApplicationId("1:695909784169:android:534567159550215c")
                    .setDatabaseUrl("https://testapp-102e7.firebaseio.com")
                    .build();
            FirebaseApp secondApp = FirebaseApp.initializeApp(context.getApplicationContext(), options, "database app");
            firebaseDatabase = FirebaseDatabase.getInstance(secondApp);
        }
        return firebaseDatabase;
    }
}
