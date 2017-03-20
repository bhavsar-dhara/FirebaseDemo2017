/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Activity to demonstrate anonymous login and account linking (with an email/password account).
 */
public class AnonymousAuthActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = AnonymousAuthActivity.class.getSimpleName();

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private EditText mEmailField;
    private EditText mPasswordField;

    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_auth);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]

        // Fields
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Click listeners
        findViewById(R.id.button_anonymous_sign_in).setOnClickListener(this);
        findViewById(R.id.button_anonymous_sign_out).setOnClickListener(this);
        findViewById(R.id.button_link_account).setOnClickListener(this);
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    private void signInAnonymously() {
        showProgressDialog();
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(AnonymousAuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END signin_anonymously]
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void linkAccount() {
        // Make sure form is valid
        if (!validateLinkForm()) {
            return;
        }

        // Get email and password from form
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        // Create EmailAuthCredential with email and password
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        // Link the anonymous user to the email credential
        showProgressDialog();
        // [START link_credential]
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(AnonymousAuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END link_credential]
    }

    private boolean validateLinkForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        TextView idView = (TextView) findViewById(R.id.anonymous_status_id);
        TextView emailView = (TextView) findViewById(R.id.anonymous_status_email);
        boolean isSignedIn = (user != null);

        // Status text
        if (isSignedIn) {
            idView.setText(getString(R.string.id_fmt, user.getUid()));
            emailView.setText(getString(R.string.email_fmt, user.getEmail()));
            downloadFile();
        } else {
            idView.setText(R.string.signed_out);
            emailView.setText(null);
        }

        // Button visibility
        findViewById(R.id.button_anonymous_sign_in).setEnabled(!isSignedIn);
        findViewById(R.id.button_anonymous_sign_out).setEnabled(isSignedIn);
        findViewById(R.id.button_link_account).setEnabled(isSignedIn);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.button_anonymous_sign_in) {
            signInAnonymously();
        } else if (i == R.id.button_anonymous_sign_out) {
            signOut();
        } else if (i == R.id.button_link_account) {
            linkAccount();
        }
    }

    private void downloadFile() {
        storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl("gs://testapp-102e7.appspot.com");

        StorageReference pathReference = storageRef.child("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv.gz");

        File localFile = null;
        try {
            localFile = File.createTempFile(pathReference.getName(), null);
            final File finalLocalFile = localFile;
            pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Data for "gz file" is returned, use this as needed
                    Log.d(TAG, "onSuccess: File obtained..... SPACE: " + finalLocalFile.getTotalSpace());
                    Log.d(TAG, "onSuccess: File obtained..... NAME: " + finalLocalFile.getName());
                    Log.d(TAG, "onSuccess: PATH: " + finalLocalFile.getPath());
                    gunZipIt(finalLocalFile.getParent(), finalLocalFile.getName());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.e(TAG, "onFailure: ", exception);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's a download in progress, save the reference so you can query it later
        if (storageRef != null) {
            outState.putString("reference", storageRef.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was a download in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        List<FileDownloadTask> tasks = storageRef.getActiveDownloadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the download
            FileDownloadTask task = tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot state) {
                    // handleSuccess(state); //call a user defined function to handle the event.
                    Log.d(TAG, "onSuccess: ");
                }
            });
        }
    }

    private void gunZipIt(String path, String zipName) {
        final String INPUT_GZIP_FILE = path + "/" + zipName;
        final String OUTPUT_FILE = INPUT_GZIP_FILE.replaceAll("(.gz)+([^\\s])+(.tmp)", "");
        Log.d(TAG, "gunZipIt: OUTPUT_FILE: " + OUTPUT_FILE);

        byte[] buffer = new byte[1024];

        InputStream fis;
        GZIPInputStream gis;
        try {
            fis = new FileInputStream(INPUT_GZIP_FILE);
            gis = new GZIPInputStream(new BufferedInputStream(fis));

            Log.d(TAG, "gunZipIt: ");
            
            InputStreamReader reader = new InputStreamReader(gis);
            BufferedReader bufferReader = new BufferedReader(reader);
            OutputStream fout = new FileOutputStream(OUTPUT_FILE);
            byte data[] = new byte[2097152];
            long total = 0;
            int count;

            while ((count = bufferReader.read()) != -1) {
                total += count;
                fout.write(data, 0, count);
            }

            fout.flush();
            fout.close();
            reader.close();
            Log.d(TAG, "gunZipIt: SUCCESSFUL.. total = " + total);
        } catch(IOException | BufferOverflowException e) {
            Log.e(TAG, "gunZipIt: ", e);
        }

        Log.d(TAG, "gunZipIt: DONE.. ");

        File file = new File(OUTPUT_FILE);
        if(file.exists()) {
            //Do something
            Log.d(TAG, "gunZipIt: File Exists. Size: " + file.getTotalSpace());
        } else {
            // Do something else.
            Log.e(TAG, "gunZipIt: File doesn't exists. Oops something went wrong.");
        }

    }
}
