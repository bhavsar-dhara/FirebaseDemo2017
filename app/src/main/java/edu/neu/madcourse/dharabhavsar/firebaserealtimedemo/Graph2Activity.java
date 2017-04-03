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
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

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

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.CSVAnnotatedModel;

public class Graph2Activity extends AppCompatActivity {

    private static final String TAG = Graph2Activity.class.getSimpleName();

    String INPUT_GZIP_FILE;
    List<CSVAnnotatedModel> beanList;

    FirebaseStorage storage;
    StorageReference storageRef;

    LinearLayout chart;

    private static final int PROGRESS = 0x1;

    private LinearLayout mProgressBarLayout;
    private ProgressBar mProgress;
    private int mProgressStatus = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph2);

        chart = (LinearLayout) findViewById(R.id.chart);
        mProgressBarLayout = (LinearLayout) findViewById(R.id.progress_bar_layout);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);

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
//        storageRef = storage.getReferenceFromUrl("gs://testapp-102e7.appspot.com");
        storageRef = storage.getReference();

        StorageReference pathReference =
                storageRef.child("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv.gz");

//        StorageReference pathReference =
//                storageRef.child("test.csv.gz");

        File localFile = null;
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
                    //showProgressStatus(finalLocalFile.getTotalSpace());
//                    TODO
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
                plotAccGraph();
            } else {
                Toast.makeText(this, "Unsuccessful CSV parsing", Toast.LENGTH_LONG).show();
                Log.e(TAG, "unzipFile: Parsing unsuccessful...");
            }

        } catch (IOException | BufferOverflowException e) {
            Log.e(TAG, "gunzipFile: ", e);
        }
    }

    private void plotAccGraph() {
        Log.d(TAG, "plotXAccGraph: started");
        XYSeries series = new XYSeries("X-acceleration vs time");
        XYSeries series2 = new XYSeries("Y-acceleration vs time");
        XYSeries series3 = new XYSeries("Z-acceleration vs time");

        float milliSecond = 0.01f;
        if (beanList.size() > 1) {
            Log.d(TAG, "plotXAccGraph: making the series");
            for (CSVAnnotatedModel str : beanList) {
                series.add(milliSecond++, Double.parseDouble(str.getX_ACCELERATION_METERS_PER_SECOND_SQUARED()));
                series2.add(milliSecond++, Double.parseDouble(str.getY_ACCELERATION_METERS_PER_SECOND_SQUARED()));
                series3.add(milliSecond++, Double.parseDouble(str.getZ_ACCELERATION_METERS_PER_SECOND_SQUARED()));
            }
        }

        // Now we add our series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
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
        chart.addView(chartView,0);
        chart.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }
}
