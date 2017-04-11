package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver;

import static edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE;

public class ViewFilesListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        View.OnClickListener {

    private static final String TAG = ViewFilesListActivity.class.getSimpleName();

    FirebaseDatabase database;
    DatabaseReference myRef;

    Spinner spinner;
    ArrayAdapter<String> adapter;
    List<String> fileList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files_list);

        IntentFilter intentFilter = new IntentFilter(NetworkStateChangeReceiver.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";

                if (networkStatus.equals("connected")) {
                    Snackbar.make(findViewById(R.id.activity_main), "Network Status: " + networkStatus, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.activity_main), "Network Status: " + networkStatus, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        }, intentFilter);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        findViewById(R.id.button_download_file).setOnClickListener(this);
        findViewById(R.id.button_download_file_2).setOnClickListener(this);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileList);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // TODO : add progress spinner till data from database is fetched
        myRef.child("fileDir").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    fileList.add(snapshot.getValue().toString().replaceAll("_", "."));
                }
                Log.d(TAG, "onChildAdded: size = " + fileList.size());

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError);
            }


        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        findViewById(R.id.button_download_file).setEnabled(true);
        findViewById(R.id.button_download_file_2).setEnabled(true);
        Log.d(TAG, "onItemSelected: " + spinner.getSelectedItem().toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d(TAG, "onNothingSelected: ");
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.button_download_file) {
            openDownloadFileActivity();
        } else if (i == R.id.button_download_file_2) {
            openDownloadFileActivity2();
        }
    }

    private void openDownloadFileActivity() {
        Intent i = new Intent(this, GraphActivity.class);
        Log.d(TAG, "openDownloadFileActivity: " + spinner.getSelectedItem().toString());
        i.putExtra("FileName", spinner.getSelectedItem().toString());
        startActivity(i);
    }

    private void openDownloadFileActivity2() {
        Intent i = new Intent(this, Graph2Activity.class);
        Log.d(TAG, "openDownloadFileActivity2: " + spinner.getSelectedItem().toString());
        i.putExtra("FileName", spinner.getSelectedItem().toString());
        startActivity(i);
    }
}