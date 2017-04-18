package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Dhara on 4/18/2017.
 */

public class ConnectToFirebaseAuth {
    private static final String TAG = ConnectToFirebaseStorage.class.getSimpleName();

    private static FirebaseAuth firebaseAuth;

    private ConnectToFirebaseAuth() {

    }

    public static FirebaseAuth instance(Context context) {
        Log.d(TAG, "instance:  " + context);
        if (firebaseAuth == null) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDCx1090HtVx5ViVxnQ9O0UAyeGxX9nV2E")
                    .setApplicationId("1:695909784169:android:534567159550215c")
                    .build();
            FirebaseApp secondApp = FirebaseApp.initializeApp(context, options, "auth app");
            firebaseAuth = FirebaseAuth.getInstance(secondApp);
        }
        return firebaseAuth;
    }
}
