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

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.BufferOverflowException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver;

import static edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE;

public class Graph2Activity extends BaseActivity {

    private static final String TAG = Graph2Activity.class.getSimpleName();

    String INPUT_GZIP_FILE;

    FirebaseStorage storage;
    StorageReference storageRef;

    LinearLayout chart;

    private LinearLayout mProgressBarLayout;

    XYSeries seriesX = new XYSeries("X-acceleration vs time");
    XYSeries seriesY = new XYSeries("Y-acceleration vs time");
    XYSeries seriesZ = new XYSeries("Z-acceleration vs time");

    float milliSecond = 0.01f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph2);

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

        chart = (LinearLayout) findViewById(R.id.chart);
        mProgressBarLayout = (LinearLayout) findViewById(R.id.progress_bar_layout);

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
//        storageRef = storage.getReference();

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
//            localFile = File.createTempFile("download.csv", "gz");
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
            int c = 0;
            while ((line = bufferReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (c > 0) {
                    seriesX.add(milliSecond, Double.parseDouble(parts[1]));
                    seriesY.add(milliSecond, Double.parseDouble(parts[2]));
                    seriesZ.add(milliSecond, Double.parseDouble(parts[3]));
                }
                if (c == 40000) // c is used to plot upto first 10000 points
                    break;
                else
                    c++;
                milliSecond++;
                c++;
            }
            plotAccGraph();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Step-5: Method to plot the data on graph
     */
    private void plotAccGraph() {
//        THIS PLOT DOES NOT HAVE THE ZOOM IN/ZOOM OUT FUNCTIONALITY
        Log.d(TAG, "plotXAccGraph: started");

        // Now we add our seriesX
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);
        Log.d(TAG, "plotXAccGraph: dataset containing series created");

        // Now we create the renderer
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setLineWidth(2);
        renderer.setColor(Color.RED);
        // Include low and max value
        renderer.setDisplayBoundingPoints(true);
        // we add point markers
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(3);

        // Now we create the renderer2
        XYSeriesRenderer renderer2 = new XYSeriesRenderer();
        renderer2.setLineWidth(2);
        renderer2.setColor(Color.BLUE);
        // Include low and max value
        renderer2.setDisplayBoundingPoints(true);
        // we add point markers
        renderer2.setPointStyle(PointStyle.CIRCLE);
        renderer2.setPointStrokeWidth(3);

        // Now we create the renderer3
        XYSeriesRenderer renderer3 = new XYSeriesRenderer();
        renderer3.setLineWidth(2);
        renderer3.setColor(Color.GREEN);
        // Include low and max value
        renderer3.setDisplayBoundingPoints(true);
        // we add point markers
        renderer3.setPointStyle(PointStyle.CIRCLE);
        renderer3.setPointStrokeWidth(3);

        // Finaly we create the multiple series renderer to control the graph
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        mRenderer.addSeriesRenderer(renderer2);
        mRenderer.addSeriesRenderer(renderer3);
        Log.d(TAG, "plotXAccGraph: renderer building going on");

        // We want to avoid black border
        // transparent margins
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(3);
        mRenderer.setYAxisMin(-2);
        mRenderer.setShowGrid(true); // we show the grid

        GraphicalView chartView = ChartFactory.
                getLineChartView(this, dataset, mRenderer);

        Log.d(TAG, "plotXAccGraph: graphical chart view created");
        chart.addView(chartView, 0);
        chart.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }
}
