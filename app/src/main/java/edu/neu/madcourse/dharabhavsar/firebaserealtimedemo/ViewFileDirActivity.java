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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.AnnoYear;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.AnnotatedDays;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.AnnotatedFileDetails;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.AnnotatedHours;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.AnnotatedMonths;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.AnnotatedYears;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver;

import static edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE;

public class ViewFileDirActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ViewFileDirActivity.class.getSimpleName();

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // week long or annotated data
    private Spinner spinnerSelectType;
    private ArrayAdapter<CharSequence> adapterSelectType;

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

    AnnotatedYears years = new AnnotatedYears();
    AnnotatedMonths months = new AnnotatedMonths();
    AnnotatedDays days = new AnnotatedDays();
    AnnotatedHours hours = new AnnotatedHours();
    AnnotatedFileDetails fileDetails = new AnnotatedFileDetails();

    AnnoYear yearDir;

    Map<String, Object> yearMap;
    List<Map<String, Object>> monthMapList = new ArrayList<>();
    List<Map<String, Object>> dayMapList = new ArrayList<>();
    List<Map<String, Object>> hoursMapList = new ArrayList<>();
    List<AnnotatedFileDetails> fileDetailsList = new ArrayList<>();

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

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        spinnerSelectType = (Spinner) findViewById(R.id.spinnerSelectType);
        spinnerSelectType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectType: " + spinnerSelectType.getSelectedItem().toString());
                if (spinnerSelectType.getSelectedItem().toString().equals("Week-long Data")) {
//                    TODO call2017();
                    Log.d(TAG, "onItemSelected: week-long");
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
        // Create an ArrayAdapter using the string array and a default spinner layout
//        adapterSelectMonth = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectType);
        adapterSelectType = ArrayAdapter.createFromResource(this, R.array.filetype_array,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSelectType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectType.setAdapter(adapterSelectType);

        spinnerSelectYear = (Spinner) findViewById(R.id.spinnerSelectYear);
        spinnerSelectYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectMonth: ");
                setMonthSpinner(spinnerSelectYear.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectMonth: ");
            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectYear = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectYear);
        // Specify the layout to use when the list of choices appears
        adapterSelectYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectYear.setAdapter(adapterSelectYear);

        spinnerSelectMonth = (Spinner) findViewById(R.id.spinnerSelectMonth);
        spinnerSelectMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectMonth: ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectMonth: ");
            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectMonth = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectMonth);
        // Specify the layout to use when the list of choices appears
        adapterSelectMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectMonth.setAdapter(adapterSelectMonth);

        spinnerSelectDay = (Spinner) findViewById(R.id.spinnerSelectDay);
        spinnerSelectDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectDay: ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectDay: ");
            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectDay = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectDay);
        // Specify the layout to use when the list of choices appears
        adapterSelectDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectDay.setAdapter(adapterSelectDay);

        spinnerSelectHour = (Spinner) findViewById(R.id.spinnerSelectHour);
        spinnerSelectHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectHour: ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectHour: ");
            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectHour = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectHour);
        // Specify the layout to use when the list of choices appears
        adapterSelectHour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectHour.setAdapter(adapterSelectHour);

        spinnerSelectFileType = (Spinner) findViewById(R.id.spinnerSelectFileType);
        spinnerSelectFileType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinnerSelectFileType: ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: spinnerSelectFileType: ");
            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSelectFileType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileListSelectFileType);
        // Specify the layout to use when the list of choices appears
        adapterSelectFileType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSelectFileType.setAdapter(adapterSelectFileType);
    }

    @Override
    public void onClick(View view) {

    }

    private void call2017() {
        // TODO : add progress spinner till data from database is fetched
        myRef.child("2017").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                adapterSelectMonth.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    fileListSelectMonth.add(snapshot.getValue().toString().replaceAll("_", "."));
                }
                Log.d(TAG, "onChildAdded: size = " + fileListSelectMonth.size());

                adapterSelectMonth.notifyDataSetChanged();
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

    private void call2016() {
        myRef.child("annotatedData2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapterSelectYear.clear();
                adapterSelectMonth.clear();
                adapterSelectDay.clear();
                adapterSelectHour.clear();
                adapterSelectFileType.clear();

                yearMap = (Map<String, Object>) dataSnapshot.getValue();
                Log.d(TAG, "onDataChange: " + yearMap.size());
//                Log.d(TAG, "onDataChange: " + yearMap.keySet().toString());
                for (Map.Entry<String, Object> yearEntry : yearMap.entrySet()) {
                    Log.d(TAG, "onDataChange:....... " + yearEntry.getKey());
                    fileListSelectYear.add(yearEntry.getKey());
                    Map<String, Object> testMon = (Map<String, Object>) yearEntry.getValue();

                    monthMapList.add(testMon);
                }

//                Map<String, Object> testMon = (Map<String, Object>) yearMap.get("2016");
//                Log.d(TAG, "onDataChange: " + testMon.keySet().toString());

                adapterSelectYear.notifyDataSetChanged();
                adapterSelectMonth.notifyDataSetChanged();
                adapterSelectDay.notifyDataSetChanged();
                adapterSelectHour.notifyDataSetChanged();
                adapterSelectFileType.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // TODO : add progress spinner till data from database is fetched
//        myRef.child("annotatedData").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                adapterSelectYear.clear();
//                adapterSelectMonth.clear();
//                adapterSelectDay.clear();
//                adapterSelectHour.clear();
//                adapterSelectFileType.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Log.d(TAG, "onChildAdded;;: " + dataSnapshot.getChildrenCount());
//                    yearDir = snapshot.getValue(AnnoYear.class);
//                }
//                Log.d(TAG, "onChildAdded: size = " + fileListSelectMonth.size());
//
////                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
////                    years = snapshot.getValue(AnnotatedYears.class);
////                }
////                Log.d(TAG, "onChildAdded: size = " + fileListSelectMonth.size());
////
////                for (AnnotatedYear year : years.getYears()) {
////                    for (Map.Entry<Long, AnnotatedMonths> yearEntry : year.getYear().entrySet()) {
////                        Log.e(TAG, "onChildAdded:yearEntry " + yearEntry.getKey().toString());
////                        fileListSelectYear.add(yearEntry.getKey().toString());
////                        months = yearEntry.getValue();
////                        for (AnnotatedMonth month : months.getMonths()) {
////                            for (Map.Entry<Long, AnnotatedDays> monthEntry : month.getMonth().entrySet()) {
////                                Log.e(TAG, "onChildAdded:monthEntry " + monthEntry.getKey().toString());
////                                fileListSelectMonth.add(monthEntry.getKey().toString());
////                                days = monthEntry.getValue();
////                                for (AnnotatedDay day : days.getDays()) {
////                                    for (Map.Entry<Long, AnnotatedHours> dayEntry : day.getDay().entrySet()) {
////                                        Log.e(TAG, "onChildAdded:dayEntry " + dayEntry.getKey().toString());
////                                        fileListSelectDay.add(dayEntry.getKey().toString());
////                                        hours = dayEntry.getValue();
////                                        for (AnnotatedHour hour : hours.getHours()) {
////                                            for (Map.Entry<Long, AnnotatedFileDetails> hourEntry : hour.getHour().entrySet()) {
////                                                Log.e(TAG, "onChildAdded:hourEntry " + hourEntry.getKey().toString());
////                                                fileListSelectHour.add(hourEntry.getKey().toString());
////                                                fileDetails = hourEntry.getValue();
////                                                Log.e(TAG, "onChildAdded:hourEntry " + fileDetails.getData());
////                                                fileListSelectFileType.add(fileDetails.getData());
////                                                Log.e(TAG, "onChildAdded:hourEntry " + fileDetails.getAnnotation());
////                                                fileListSelectFileType.add(fileDetails.getAnnotation());
////                                            }
////                                        }
////                                    }
////                                }
////                            }
////                        }
////                    }
////                }
//
//                adapterSelectYear.notifyDataSetChanged();
//                adapterSelectMonth.notifyDataSetChanged();
//                adapterSelectDay.notifyDataSetChanged();
//                adapterSelectHour.notifyDataSetChanged();
//                adapterSelectFileType.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "onCancelled: " + databaseError);
//            }
//
//        });
    }

    private void setMonthSpinner(String selectedYear) {
        Map<String, Object> testMon = (Map<String, Object>) yearMap.get(selectedYear);
        for (Map.Entry<String, Object> monthEntry : testMon.entrySet()) {
            fileListSelectMonth.add(monthEntry.getKey());
            Map<String, Object> testDay = (Map<String, Object>) monthEntry.getValue();

            dayMapList.add(testDay);
        }
    }

//    private void setDaySpinner(String selectedMonth) {
//        Map<String, Object> testDay = (Map<String, Object>) monthMapList.get(selectedMonth);
//        for (Map.Entry<String, Object> dayEntry : testDay.entrySet()) {
//            fileListSelectDay.add(dayEntry.getKey());
//            Map<String, Object> testHour = (Map<String, Object>) testDay.get(dayEntry.getValue());
//
//            hoursMapList.add(testHour);
//        }
//    }

//    private void setHourSpinner() {
//        for (Map.Entry<String, Object> hourEntry : testHour.entrySet()) {
//            fileListSelectHour.add(hourEntry.getKey());
//            Map<String, Object> testFileDetails = (Map<String, Object>) testHour.get(hourEntry.getValue());
//
//            hoursMapList.add(testFileDetails);
//        }
//    }
//
//    private void setFileDetailsSpinner() {
//        for (Map.Entry<String, Object> detailEntry : testFileDetails.entrySet()) {
//            fileListSelectFileType.add(detailEntry.getValue().toString());
//            AnnotatedFileDetails testDetails = new AnnotatedFileDetails();
//            if (detailEntry.getKey().equals("annotation")) {
//                testDetails.setAnnotation(detailEntry.getValue().toString());
//            } else {
//                testDetails.setData(detailEntry.getValue().toString());
//            }
//            fileDetailsList.add(testDetails);
//        }
//    }
}
