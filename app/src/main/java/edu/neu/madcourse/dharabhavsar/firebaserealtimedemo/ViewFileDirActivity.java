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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.AnnotatedFileDetails;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.utils.ConnectToFirebaseDatabase;

import static edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE;

public class ViewFileDirActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ViewFileDirActivity.class.getSimpleName();

    private DatabaseReference myRef;

    // week long or annotated data
    private Spinner spinnerSelectType;

    // year
    private Spinner spinnerSelectYear;
    private ArrayAdapter<String> adapterSelectYear;
    private List<String> fileListSelectYear = new ArrayList<>();

    // month
    private Spinner spinnerSelectMonth;
    private ArrayAdapter<String> adapterSelectMonth;
    private List<String> fileListSelectMonth = new ArrayList<>();

    // day
    private Spinner spinnerSelectDay;
    private ArrayAdapter<String> adapterSelectDay;
    private List<String> fileListSelectDay = new ArrayList<>();

    // hour
    private Spinner spinnerSelectHour;
    private ArrayAdapter<String> adapterSelectHour;
    private List<String> fileListSelectHour = new ArrayList<>();

    // annotated data -> another spinner to select from data or the annotated file
    private Spinner spinnerSelectFileType;
    private ArrayAdapter<String> adapterSelectFileType;
    private List<String> fileListSelectFileType = new ArrayList<>();

    private Map<String, Object> testMon = null;
    private Map<String, Object> testDay = null;
    private Map<String, Object> testHour = null;
    private Map<String, Object> testDetails = null;

    private Map<String, Object> yearMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file_dir);

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

        FirebaseDatabase database = ConnectToFirebaseDatabase.instance(getApplicationContext());
        myRef = database.getReference();

        findViewById(R.id.button_download_file).setOnClickListener(this);
        findViewById(R.id.button_download_file_2).setOnClickListener(this);

        spinnerSelectType = (Spinner) findViewById(R.id.spinnerSelectType);
        // Create an ArrayAdapter using the string array and a default spinner layout
//        adapterSelectMonth = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectType);
        ArrayAdapter<CharSequence> adapterSelectType = ArrayAdapter.createFromResource(this, R.array.filetype_array,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSelectType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectType.setAdapter(adapterSelectType);
        spinnerSelectType.setSelection(0, false);
        spinnerSelectType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectType: " + spinnerSelectType.getSelectedItem().toString());
                if (spinnerSelectType.getSelectedItem().toString().equals("Week-long Data")) {
                    Log.d(TAG, "onItemSelected: week-long");
                    call2017();
                } else {
                    Log.d(TAG, "onItemSelected: annotated");
                    call2016();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectType: ");
            }
        });

        spinnerSelectYear = (Spinner) findViewById(R.id.spinnerSelectYear);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectYear = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectYear);
        // Specify the layout to use when the list of choices appears
        adapterSelectYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectYear.setAdapter(adapterSelectYear);
        spinnerSelectYear.setSelection(0, false);
        spinnerSelectYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectYear: " + spinnerSelectYear.getSelectedItem().toString());
                setMonthSpinner(spinnerSelectYear.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectMonth: ");
            }
        });

        spinnerSelectMonth = (Spinner) findViewById(R.id.spinnerSelectMonth);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectMonth = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectMonth);
        // Specify the layout to use when the list of choices appears
        adapterSelectMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectMonth.setAdapter(adapterSelectMonth);
        spinnerSelectMonth.setSelection(0, false);
        spinnerSelectMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectMonth: " + spinnerSelectMonth.getSelectedItem().toString());
                setDaySpinner(spinnerSelectMonth.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectMonth: ");
            }
        });

        spinnerSelectDay = (Spinner) findViewById(R.id.spinnerSelectDay);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectDay = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectDay);
        // Specify the layout to use when the list of choices appears
        adapterSelectDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectDay.setAdapter(adapterSelectDay);
        spinnerSelectDay.setSelection(0, false);
        spinnerSelectDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectDay: " + spinnerSelectDay.getSelectedItem().toString());
                setHourSpinner(spinnerSelectDay.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectDay: ");
            }
        });

        spinnerSelectHour = (Spinner) findViewById(R.id.spinnerSelectHour);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectHour = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectHour);
        // Specify the layout to use when the list of choices appears
        adapterSelectHour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectHour.setAdapter(adapterSelectHour);
        spinnerSelectHour.setSelection(0, false);
        spinnerSelectHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectHour: " + spinnerSelectHour.getSelectedItem().toString());
                setFileDetailsSpinner(spinnerSelectHour.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectHour: ");
            }
        });

        spinnerSelectFileType = (Spinner) findViewById(R.id.spinnerSelectFileType);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectFileType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectFileType);
        // Specify the layout to use when the list of choices appears
        adapterSelectFileType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectFileType.setAdapter(adapterSelectFileType);
        spinnerSelectFileType.setSelection(0, false);
        spinnerSelectFileType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectFileType: " + spinnerSelectFileType.getSelectedItem().toString());
                enableButtons();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectFileType: ");
            }
        });
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
        String fileName = spinnerSelectYear.getSelectedItem().toString() + "/"
                + spinnerSelectMonth.getSelectedItem().toString() + "/"
                + spinnerSelectDay.getSelectedItem().toString() + "/"
                + spinnerSelectHour.getSelectedItem().toString() + "/"
                + spinnerSelectFileType.getSelectedItem().toString();
        if (spinnerSelectFileType.getSelectedItem().toString().contains("sensor")) {
            Intent i = new Intent(this, GraphActivity.class);
            Log.d(TAG, "openDownloadFileActivity: " + fileName);
            i.putExtra("FileName", fileName);
            startActivity(i);
        } else {
            Log.d(TAG, "openDownloadFileActivity: reading annotated....");
//            Intent i = new Intent(this,. class);
//            Log.d(TAG, "openDownloadFileActivity: " + fileName);
//            i.putExtra("FileName", fileName);
//            startActivity(i);
        }
    }

    private void openDownloadFileActivity2() {
        String fileName = spinnerSelectYear.getSelectedItem().toString() + "/"
                + spinnerSelectMonth.getSelectedItem().toString() + "/"
                + spinnerSelectDay.getSelectedItem().toString() + "/"
                + spinnerSelectHour.getSelectedItem().toString() + "/"
                + spinnerSelectFileType.getSelectedItem().toString();
        if (spinnerSelectFileType.getSelectedItem().toString().contains("sensor")) {
            Intent i = new Intent(this, Graph2Activity.class);
            Log.d(TAG, "openDownloadFileActivity2: " + fileName);
            i.putExtra("FileName", fileName);
            startActivity(i);
        } else {
            Log.d(TAG, "openDownloadFileActivity: reading annotated....");
//            Intent i = new Intent(this,. class);
//            Log.d(TAG, "openDownloadFileActivity: " + fileName);
//            i.putExtra("FileName", fileName);
//            startActivity(i);
        }
    }

    private void call2017() {
        myRef.child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapterSelectYear.clear();

                yearMap = (Map<String, Object>) dataSnapshot.getValue();
                Log.d(TAG, "onDataChange: " + yearMap.size());
//                Log.d(TAG, "onDataChange: " + yearMap.keySet().toString());
                for (Map.Entry<String, Object> yearEntry : yearMap.entrySet()) {
                    Log.d(TAG, "onDataChange:....... " + yearEntry.getKey());
                    fileListSelectYear.add(yearEntry.getKey());
                }

//                Map<String, Object> testMon = (Map<String, Object>) yearMap.get("2016");ig
                adapterSelectYear.notifyDataSetChanged();
                if (!spinnerSelectYear.isSelected()) {
                    spinnerSelectYear.setSelection(0, false);
                    setMonthSpinner(spinnerSelectYear.getSelectedItem().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void call2016() {
        myRef.child("annotatedData2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapterSelectYear.clear();

                yearMap = (Map<String, Object>) dataSnapshot.getValue();
                Log.d(TAG, "onDataChange: " + yearMap.size());
//                Log.d(TAG, "onDataChange: " + yearMap.keySet().toString());
                for (Map.Entry<String, Object> yearEntry : yearMap.entrySet()) {
                    Log.d(TAG, "onDataChange:....... " + yearEntry.getKey());
                    fileListSelectYear.add(yearEntry.getKey());
                }

//                Map<String, Object> testMon = (Map<String, Object>) yearMap.get("2016");
//                Log.d(TAG, "onDataChange: " + testMon.keySet().toString());
                Collections.sort(fileListSelectYear);
                adapterSelectYear.notifyDataSetChanged();
                if (!spinnerSelectYear.isSelected()) {
                    spinnerSelectYear.setSelection(0, false);
                    setMonthSpinner(spinnerSelectYear.getSelectedItem().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMonthSpinner(String selectedYear) {
        adapterSelectMonth.clear();
        fileListSelectMonth.clear();
        testMon = (Map<String, Object>) yearMap.get(selectedYear);
        for (Map.Entry<String, Object> monthEntry : testMon.entrySet()) {
            fileListSelectMonth.add(monthEntry.getKey());
            Log.d(TAG, "setMonthSpinner: " + monthEntry.getKey());
        }
        Collections.sort(fileListSelectMonth);
        adapterSelectMonth.notifyDataSetChanged();
        if (!spinnerSelectMonth.isSelected()) {
            spinnerSelectMonth.setSelection(0, false);
            setDaySpinner(spinnerSelectMonth.getSelectedItem().toString());
        }
    }

    private void setDaySpinner(String selectedMonth) {
        adapterSelectDay.clear();
        fileListSelectHour.clear();
        for (Map.Entry<String, Object> monthEntry : testMon.entrySet()) {
            if (monthEntry.getKey().equals(selectedMonth)) {
                testDay = (Map<String, Object>) monthEntry.getValue();
            }
        }
        if (testDay != null) {
            for (Map.Entry<String, Object> dayEntry : testDay.entrySet()) {
                fileListSelectDay.add(dayEntry.getKey());
                Log.d(TAG, "setDaySpinner: " + dayEntry.getKey());
            }
        } else {
            Log.e(TAG, "setDaySpinner: Oops something went wrong.....NPE");
        }
        Collections.sort(fileListSelectDay);
        adapterSelectDay.notifyDataSetChanged();
        if (!spinnerSelectDay.isSelected()) {
            spinnerSelectDay.setSelection(0, false);
            setHourSpinner(spinnerSelectDay.getSelectedItem().toString());
        }
    }

    private void setHourSpinner(String selectedDay) {
        adapterSelectHour.clear();
        fileListSelectHour.clear();
        for (Map.Entry<String, Object> dayEntry : testDay.entrySet()) {
            if (dayEntry.getKey().equals(selectedDay)) {
                testHour = (Map<String, Object>) dayEntry.getValue();
            }
        }
        if (testHour != null) {
            for (Map.Entry<String, Object> hourEntry : testHour.entrySet()) {
                fileListSelectHour.add(hourEntry.getKey());
                Map<String, Object> testFileDetails = (Map<String, Object>) hourEntry.getValue();
                Log.d(TAG, "setHourSpinner: " + hourEntry.getKey());
            }
        } else {
            Log.e(TAG, "setHourSpinner: Oops something went wrong.....NPE");
        }
        Collections.sort(fileListSelectHour);
        adapterSelectHour.notifyDataSetChanged();
        if (!spinnerSelectHour.isSelected()) {
            spinnerSelectHour.setSelection(0, false);
            setFileDetailsSpinner(spinnerSelectHour.getSelectedItem().toString());
        }
    }

    private void setFileDetailsSpinner(String selectedHour) {
        adapterSelectFileType.clear();
        fileListSelectFileType.clear();
        for (Map.Entry<String, Object> hourEntry : testHour.entrySet()) {
            if (hourEntry.getKey().equals(selectedHour)) {
                testDetails = (Map<String, Object>) hourEntry.getValue();
            }
        }
        if (testDetails != null) {
            for (Map.Entry<String, Object> detailEntry : testDetails.entrySet()) {
                Log.d(TAG, "setFileDetailsSpinner: " + detailEntry.getValue().toString());
                AnnotatedFileDetails fileDetails = new AnnotatedFileDetails();
                if (detailEntry.getKey().equals("annotation")) {
                    fileDetails.setAnnotation(detailEntry.getValue().toString());
                    fileListSelectFileType.add(fileDetails.getAnnotation());
                } else {
                    fileDetails.setData(detailEntry.getValue().toString());
                    fileListSelectFileType.add(fileDetails.getData());
                }
            }
        } else {
            Log.e(TAG, "setFileDetailsSpinner: Oops something went wrong.....NPE");
        }
        Collections.sort(fileListSelectFileType);
        adapterSelectFileType.notifyDataSetChanged();
        if (!spinnerSelectFileType.isSelected()) {
            spinnerSelectFileType.setSelection(0, false);
            enableButtons();
        }
    }

    private void enableButtons() {
        findViewById(R.id.button_download_file).setEnabled(true);
        findViewById(R.id.button_download_file_2).setEnabled(true);
        Log.d(TAG, "onItemSelected: " + spinnerSelectFileType.getSelectedItem().toString());
    }
}
