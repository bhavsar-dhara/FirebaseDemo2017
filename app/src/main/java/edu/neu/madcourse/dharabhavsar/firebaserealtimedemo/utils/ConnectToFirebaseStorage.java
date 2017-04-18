package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Created by Dhara on 4/18/2017.
 */

public class ConnectToFirebaseStorage {
    private static final String TAG = ConnectToFirebaseStorage.class.getSimpleName();

    private static FirebaseStorage firebaseStorage;

    private ConnectToFirebaseStorage() {

    }

    public static FirebaseStorage instance(Context context) {
        Log.d(TAG, "instance:  " + context);
        if (firebaseStorage == null) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDCx1090HtVx5ViVxnQ9O0UAyeGxX9nV2E")
                    .setApplicationId("1:695909784169:android:534567159550215c")
                    .setStorageBucket("testapp-102e7.appspot.com")
                    .build();
            FirebaseApp secondApp = FirebaseApp.initializeApp(context, options, "storage app");
            firebaseStorage = FirebaseStorage.getInstance(secondApp);
        }
        return firebaseStorage;
    }
}
