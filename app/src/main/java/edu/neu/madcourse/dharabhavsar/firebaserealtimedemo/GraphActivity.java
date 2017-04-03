package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
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

public class GraphActivity extends AppCompatActivity {

    private static final String TAG = GraphActivity.class.getSimpleName();

    String INPUT_GZIP_FILE;
    List<CSVAnnotatedModel> beanList;

    FirebaseStorage storage;
    StorageReference storageRef;

    GraphView chartLyt;

    private static final int PROGRESS = 0x1;

    private LinearLayout mProgressBarLayout;
    private ProgressBar mProgress;
    private int mProgressStatus = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        chartLyt = (GraphView) findViewById(R.id.chart);
        mProgressBarLayout = (LinearLayout) findViewById(R.id.progress_bar_layout);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);

        downloadFile();
//        gunzipFile();
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

    private void showProgressStatus(final long fileSize) {
        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (mProgressStatus < 100) {
//                    mProgressStatus = doDownload(fileSize);

                    // phone is too fast, sleep 1 second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgress.setProgress(mProgressStatus);
                        }
                    });
                }

                // ok, file is downloaded,
                if (mProgressStatus >= 100) {

                    // sleep 2 seconds, so that you can see the 100%
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // close the progress bar dialog
                    mProgressBarLayout.setVisibility(View.GONE);
                }
            }
        }).start();
    }

    private void downloadFile() {
        storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl("gs://testapp-102e7.appspot.com");

        StorageReference pathReference =
                storageRef.child("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv.gz");

//        StorageReference pathReference =
//                storageRef.child("test.csv.gz");

        File localFile = null;
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
                    //showProgressStatus(finalLocalFile.getTotalSpace());
                    unzipFile(finalLocalFile.getParent(), finalLocalFile.getName());
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

    private void unzipFile(String path, String zipName) {
        INPUT_GZIP_FILE = path + "/" + zipName;

        InputStream fis;
        GZIPInputStream gis;
        try {
            fis = new FileInputStream(INPUT_GZIP_FILE);
//            fis = getAssets().open("test.csv.bin");
//                    fis = getAssets().open("sensor.csv.bin");
            gis = new GZIPInputStream(new BufferedInputStream(fis));

            Log.d(TAG, "gunzipFile: Gunzipping file");

            InputStreamReader reader = new InputStreamReader(gis);
            BufferedReader bufferReader = new BufferedReader(reader);

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
                plotXAccGraph();
            } else {
                Toast.makeText(this, "Unsuccessful CSV parsing", Toast.LENGTH_LONG).show();
                Log.e(TAG, "unzipFile: Parsing unsuccessful...");
            }

        } catch (IOException | BufferOverflowException e) {
            Log.e(TAG, "gunzipFile: ", e);
        }
    }

    private void plotXAccGraph() {
        Log.d(TAG, "plotXAccGraph: started");

        double milliSecond = 0.01d;
        DataPoint[] dataPointArrayX = null;
        List<DataPoint> dataPointListX = new ArrayList<>();
        DataPoint[] dataPointArrayY = null;
        List<DataPoint> dataPointListY = new ArrayList<>();
        DataPoint[] dataPointArrayZ = null;
        List<DataPoint> dataPointListZ = new ArrayList<>();

        if (beanList.size() > 1) {
            Log.d(TAG, "plotXAccGraph: making the series");
            DataPoint dataPointX;
            DataPoint dataPointY;
            DataPoint dataPointZ;
            for (CSVAnnotatedModel str : beanList) {
                dataPointX = new DataPoint(milliSecond,
                        Double.parseDouble(str.getX_ACCELERATION_METERS_PER_SECOND_SQUARED()));
                dataPointListX.add(dataPointX);
                dataPointY = new DataPoint(milliSecond,
                        Double.parseDouble(str.getY_ACCELERATION_METERS_PER_SECOND_SQUARED()));
                dataPointListY.add(dataPointY);
                dataPointZ = new DataPoint(milliSecond,
                        Double.parseDouble(str.getZ_ACCELERATION_METERS_PER_SECOND_SQUARED()));
                dataPointListZ.add(dataPointZ);
                milliSecond++;
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