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
import android.widget.Toast;

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
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.CSVAnnotatedModel;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver;

import static edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE;

public class GraphActivity extends BaseActivity {

    private static final String TAG = GraphActivity.class.getSimpleName();

    String INPUT_GZIP_FILE;
    List<CSVAnnotatedModel> beanList;
    List<String[]> strArrList;

    FirebaseStorage storage;
    StorageReference storageRef;

    GraphView chartLyt;

    private LinearLayout mProgressBarLayout;

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
//        StorageReference pathReference =
//                storageRef.child("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv.gz");

        // FILE#4 - 25 points dataset
//        StorageReference pathReference =
//                storageRef.child("test.csv.gz");

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
//            fis = getAssets().open("test.csv.bin");
//            fis = getAssets().open("sensor.csv.bin");
            gis = new GZIPInputStream(new BufferedInputStream(fis));

            Log.d(TAG, "gunzipFile: Gunzipping file");

            InputStreamReader reader = new InputStreamReader(gis);
            BufferedReader bufferReader = new BufferedReader(reader);

            // STEP-4a ::: Method to parse the CSV data into beans
//            parser(bufferReader);

            // STEP-4b ::: Method to parse the CSV data normally specifying the separator
            parser1(bufferReader);

        } catch (IOException | BufferOverflowException e) {
            Log.e(TAG, "gunzipFile: ", e);
        }
    }

    /*
    STEP-4a ::: Method to parse the CSV data into beans
     */
    private void parser(BufferedReader bufferReader) {
        // BeanListProcessor converts each parsed row to an instance of a given class, then stores each instance into a list.
        BeanListProcessor<CSVAnnotatedModel> rowProcessor = new BeanListProcessor<>(CSVAnnotatedModel.class);

        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setRowProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);

        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(bufferReader);

        // The BeanListProcessor provides a list of objects extracted from the input.
        beanList = rowProcessor.getBeans();

        Log.d(TAG, "readData: size = " + beanList.size());

        if(beanList.size() > 0) {
            Toast.makeText(this, "Successful CSV parsing", Toast.LENGTH_LONG).show();

            // Step-5: Method to plot the data on graph
            plotXAccGraph();
        } else {
            Toast.makeText(this, "Unsuccessful CSV parsing", Toast.LENGTH_LONG).show();
            Log.e(TAG, "unzipFile: Parsing unsuccessful...");
        }
    }

    /*
    STEP-4b ::: Method to parse the CSV data normally specifying the separator
     */
    private void parser1(BufferedReader bufferReader) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        // creates a CSV parser
        CsvParser csvParser = new CsvParser(settings);
        // parses all rows in one go.
        strArrList = csvParser.parseAll(bufferReader);

        Log.d(TAG, "readData: size = " + strArrList.size());

        if(strArrList.size() > 0) {
            Toast.makeText(this, "Successful CSV parsing", Toast.LENGTH_LONG).show();

            // Step-5: Method to plot the data on graph
            plotXAccGraph();
        } else {
            Toast.makeText(this, "Unsuccessful CSV parsing", Toast.LENGTH_LONG).show();
            Log.e(TAG, "unzipFile: Parsing unsuccessful...");
        }
    }

    /*
    Step-5: Method to plot the data on graph
     */
    private void plotXAccGraph() {
//        THIS PLOT HAS THE ZOOM IN/ZOOM OUT FUNCTIONALITY
        Log.d(TAG, "plotXAccGraph: started");

        double milliSecond = 0.01d;
        DataPoint[] dataPointArrayX = null;
        List<DataPoint> dataPointListX = new ArrayList<>();
        DataPoint[] dataPointArrayY = null;
        List<DataPoint> dataPointListY = new ArrayList<>();
        DataPoint[] dataPointArrayZ = null;
        List<DataPoint> dataPointListZ = new ArrayList<>();

//        int c = 0;
//        if (beanList.size() > 1) {
//            Log.d(TAG, "plotXAccGraph: making the series");
//            DataPoint dataPointX;
//            DataPoint dataPointY;
//            DataPoint dataPointZ;
//            for (CSVAnnotatedModel str : beanList) {
//                dataPointX = new DataPoint(milliSecond,
//                        Double.parseDouble(str.getX_ACCELERATION_METERS_PER_SECOND_SQUARED()));
//                dataPointListX.add(dataPointX);
//                dataPointY = new DataPoint(milliSecond,
//                        Double.parseDouble(str.getY_ACCELERATION_METERS_PER_SECOND_SQUARED()));
//                dataPointListY.add(dataPointY);
//                dataPointZ = new DataPoint(milliSecond,
//                        Double.parseDouble(str.getZ_ACCELERATION_METERS_PER_SECOND_SQUARED()));
//                dataPointListZ.add(dataPointZ);
//                milliSecond++;
//                if (c == 5000)
//                    break;
//                else
//                    c++;
//            }
//            dataPointArrayX = dataPointListX.toArray(new DataPoint[dataPointListX.size()]);
//            dataPointArrayY = dataPointListY.toArray(new DataPoint[dataPointListY.size()]);
//            dataPointArrayZ = dataPointListZ.toArray(new DataPoint[dataPointListZ.size()]);
//        }

        int c = 0;
        if (strArrList.size() > 1) {
            Log.d(TAG, "plotXAccGraph: making the series");
            DataPoint dataPointX;
            DataPoint dataPointY;
            DataPoint dataPointZ;
            String[] str;
            for (int i = 1; i <= strArrList.size() ; i++) { // i is initialized to 1 to ignore the header row
                str = strArrList.get(i);
                dataPointX = new DataPoint(milliSecond++,
                        Double.parseDouble(str[1]));
                dataPointListX.add(dataPointX);
                dataPointY = new DataPoint(milliSecond++,
                        Double.parseDouble(str[2]));
                dataPointListY.add(dataPointY);
                dataPointZ = new DataPoint(milliSecond++,
                        Double.parseDouble(str[3]));
                dataPointListZ.add(dataPointZ);
                if (c == 1000) // c is used to plot upto first 1000 points
                    break;
                else
                    c++;
            }
            dataPointArrayX = dataPointListX.toArray(new DataPoint[dataPointListX.size()]);
            dataPointArrayY = dataPointListY.toArray(new DataPoint[dataPointListY.size()]);
            dataPointArrayZ = dataPointListZ.toArray(new DataPoint[dataPointListZ.size()]);
        }

        LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<>(dataPointArrayX);
        seriesX.setTitle("X-acceleration");
        seriesX.setColor(Color.RED);
        seriesX.setDrawDataPoints(true);
        seriesX.setDataPointsRadius(5);
        seriesX.setThickness(4);

        LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<>(dataPointArrayY);
        seriesY.setTitle("Y-acceleration");
        seriesY.setColor(Color.BLUE);
        seriesY.setDrawDataPoints(true);
        seriesY.setDataPointsRadius(5);
        seriesY.setThickness(4);

        LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<>(dataPointArrayZ);
        seriesZ.setTitle("Z-acceleration");
        seriesZ.setColor(Color.GREEN);
        seriesZ.setDrawDataPoints(true);
        seriesZ.setDataPointsRadius(5);
        seriesZ.setThickness(4);

        LegendRenderer legendRenderer = new LegendRenderer(chartLyt);
        legendRenderer.setVisible(true);
        legendRenderer.setAlign(LegendRenderer.LegendAlign.TOP);

        chartLyt.addSeries(seriesX);
        chartLyt.addSeries(seriesY);
        chartLyt.addSeries(seriesZ);
        chartLyt.setTitle("Linear Acceleration vs. Time");
        chartLyt.setTitleTextSize(20);
        chartLyt.setTitleColor(Color.BLACK);

        // set manual X bounds
        chartLyt.getViewport().setXAxisBoundsManual(true);
        chartLyt.getViewport().setMinX(4);
        chartLyt.getViewport().setMaxX(100);

        // set manual X bounds
        chartLyt.getViewport().setYAxisBoundsManual(true);
        chartLyt.getViewport().setMinY(-50);
        chartLyt.getViewport().setMaxY(50);

        // enable scaling and scrolling
        chartLyt.getViewport().setScalable(true);
        chartLyt.getViewport().setScalableY(true);

        Log.d(TAG, "plotXAccGraph: graphical chart view created");

        chartLyt.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }
}