package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver;

import static edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE;

public class GraphActivity extends BaseActivity {

    private static final String TAG = GraphActivity.class.getSimpleName();

    private static final int SAMPLING_RATE = 300000; // based on 80Hz sampling rate data generated

    private String INPUT_GZIP_FILE;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    private GraphView chartLyt;

    private LinearLayout mProgressBarLayout;

    private Double[] doubleX = new Double[SAMPLING_RATE];
    private Double[] doubleY = new Double[SAMPLING_RATE];
    private Double[] doubleZ = new Double[SAMPLING_RATE];

    private Double[] doubleXArr;
    private Double[] doubleYArr;
    private Double[] doubleZArr;

    private double milliSecond = 0.01d;
    private DataPoint[] dataPointArrayX = new DataPoint[SAMPLING_RATE];
    private DataPoint[] dataPointArrayY = new DataPoint[SAMPLING_RATE];
    private DataPoint[] dataPointArrayZ = new DataPoint[SAMPLING_RATE];
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

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

        chartLyt = (GraphView) findViewById(R.id.chart);
        mProgressBarLayout = (LinearLayout) findViewById(R.id.progress_bar_layout);

        Arrays.fill(doubleX, 0.0d);
        Arrays.fill(doubleY, 0.0d);
        Arrays.fill(doubleZ, 0.0d);

        // STEP-2 ::: Method to download the file from the Firebase Storage
        downloadFile();
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

    /*
    STEP-2 ::: Method to download the file from the Firebase Storage
     */
    private void downloadFile() {
        storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl("gs://testapp-102e7.appspot.com");

        // FILE#1 - 10 minute dataset
        StorageReference pathReference = storageRef.child("Crowdsourcing_test_(2017-03-08%5C)RAW_HPF.csv.gz");

        // FILE#2 - Existing annotated data set
//        StorageReference pathReference = storageRef.child("SPADESInLab.alvin-SPADESInLab.2015-10-08-14-10-41-252-M0400.annotation.csv.gz");

        // FILE#3 - 1 hour long dataset
//        StorageReference pathReference = storageRef.child("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv.gz");

        // FILE#4 - 25 points dataset
//        StorageReference pathReference = storageRef.child("test.csv.gz");

        File localFile;
        try {
            localFile = File.createTempFile(pathReference.getName(), null);
            final File finalLocalFile = localFile;
            pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Data for "gz file" is returned, use this as needed
                    Log.d(TAG, "onSuccess: File obtained..... SPACE of internal memory: " + finalLocalFile.getTotalSpace());
                    Log.d(TAG, "onSuccess: File obtained..... NAME: " + finalLocalFile.getName());
                    Log.d(TAG, "onSuccess: PATH: " + finalLocalFile.getPath());

                    // STEP-3 ::: Method to unzip the downloaded file
                    unzipFile(finalLocalFile.getParent(), finalLocalFile.getName());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.e(TAG, "onFailure: ", exception);
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                    Log.d(TAG, "onProgress: " + taskSnapshot.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    STEP-3 ::: Method to unzip the downloaded file
     */
    private void unzipFile(String path, String zipName) {
        INPUT_GZIP_FILE = path + "/" + zipName;

        InputStream fis;
        GZIPInputStream gis;
        try {
            fis = new FileInputStream(INPUT_GZIP_FILE);
            gis = new GZIPInputStream(new BufferedInputStream(fis));

            Log.d(TAG, "gunzipFile: Gunzipping file");

            InputStreamReader reader = new InputStreamReader(gis);
            BufferedReader bufferReader = new BufferedReader(reader);

            // STEP-4a ::: Method to read data directly from compressed CSV file
            parser(bufferReader);

        } catch (IOException | BufferOverflowException e) {
            Log.e(TAG, "gunzipFile: ", e);
        }
    }

    /*
    STEP-4a ::: Method to read data directly from compressed CSV file
     */
    private void parser(BufferedReader bufferReader) {
        String line;
        try {
            String[] parts;
            while ((line = bufferReader.readLine()) != null) {
                parts = line.split(",");
//                if (counter < 10) {
//                    Log.d(TAG, "parser: ... " + parts[0] + " .... " + parts[1] + " ... " + parts[2] + " ... " + parts[3]);
//                }
                if (counter > 0) {
                    doubleX[counter] = Double.parseDouble(parts[1]);
                    doubleY[counter] = Double.parseDouble(parts[2]);
                    doubleZ[counter] = Double.parseDouble(parts[3]);
                }
                counter++;
            }

            Log.e(TAG, "parser: counter  = " + counter);
            doubleXArr = new Double[counter];
            doubleYArr = new Double[counter];
            doubleZArr = new Double[counter];

            System.arraycopy(doubleX, 0, doubleXArr, 0, counter);
            System.arraycopy(doubleY, 0, doubleYArr, 0, counter);
            System.arraycopy(doubleZ, 0, doubleZArr, 0, counter);

            plotXAccGraph();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Step-5: Method to plot the data on graph
     */
    private void plotXAccGraph() {
//        THIS PLOT HAS THE ZOOM IN/ZOOM OUT FUNCTIONALITY
        Log.d(TAG, "plotXAccGraph: started");

        // STEP-5a ::: Method to set data arrays from individual string arrays
        setSeriesData();

        LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<>(dataPointArrayX);
        seriesX.setTitle("X-acceleration");
        seriesX.setColor(Color.RED);
        seriesX.setDrawDataPoints(true);
        seriesX.setDataPointsRadius(2);
        seriesX.setThickness(1);

        LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<>(dataPointArrayY);
        seriesY.setTitle("Y-acceleration");
        seriesY.setColor(Color.BLUE);
        seriesY.setDrawDataPoints(true);
        seriesY.setDataPointsRadius(2);
        seriesY.setThickness(1);

        LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<>(dataPointArrayZ);
        seriesZ.setTitle("Z-acceleration");
        seriesZ.setColor(Color.GREEN);
        seriesZ.setDrawDataPoints(true);
        seriesZ.setDataPointsRadius(2);
        seriesZ.setThickness(1);

        LegendRenderer legendRenderer = new LegendRenderer(chartLyt);
        legendRenderer.setVisible(true);
        legendRenderer.setAlign(LegendRenderer.LegendAlign.TOP);

        Log.e(TAG, "plotXAccGraph: .. " + seriesX.isEmpty() );

        chartLyt.addSeries(seriesX);
        chartLyt.addSeries(seriesY);
        chartLyt.addSeries(seriesZ);
        chartLyt.setTitle("Linear Acceleration vs. Time");
        chartLyt.setTitleTextSize(20);
        chartLyt.setTitleColor(Color.BLACK);

        // set manual X bounds
        chartLyt.getViewport().setXAxisBoundsManual(false);
        chartLyt.getViewport().setMinX(1000);
        chartLyt.getViewport().setMaxX(10000);

        // set manual X bounds
        chartLyt.getViewport().setYAxisBoundsManual(false);
        chartLyt.getViewport().setMinY(0.05);
        chartLyt.getViewport().setMaxY(1);

        // enable scaling and scrolling
        chartLyt.getViewport().setScalable(true);
        chartLyt.getViewport().setScalableY(true);

        Log.d(TAG, "plotXAccGraph: graphical chart view created");

        chartLyt.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }

    /*
    STEP-5a ::: STEP-5c ::: Method to set data arrays from individual string arrays
     */
    private void setSeriesData() {
        Log.e(TAG, "setSeriesData: len = " + counter);

        dataPointArrayX = new DataPoint[counter];
        dataPointArrayY = new DataPoint[counter];
        dataPointArrayZ = new DataPoint[counter];

        for (int i = 0; i < counter ; i++) {
//            Log.e(TAG, "setSeriesData: i = " + i + " .. " + doubleX[i] + " .. " + doubleX[i] + " .. " + doubleX[i] );
            if (doubleX[i] != null)
                dataPointArrayX[i] = new DataPoint(milliSecond, doubleXArr[i]);
            if (doubleY[i] != null)
                dataPointArrayY[i] = new DataPoint(milliSecond, doubleYArr[i]);
            if (doubleZ[i] != null)
                dataPointArrayZ[i] = new DataPoint(milliSecond, doubleZArr[i]);
            milliSecond++;
        }
    }
}