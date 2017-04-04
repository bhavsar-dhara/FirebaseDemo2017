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
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver;

import static edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.receiver.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE;

public class Graph2Activity extends BaseActivity {

    private static final String TAG = Graph2Activity.class.getSimpleName();

    String INPUT_GZIP_FILE;
    List<CSVAnnotatedModel> beanList;
    List<String[]> strArrList;

    FirebaseStorage storage;
    StorageReference storageRef;

    LinearLayout chart;

    private LinearLayout mProgressBarLayout;

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

    private void downloadFile() {
        storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl("gs://testapp-102e7.appspot.com");
//        storageRef = storage.getReference();

        StorageReference pathReference = storageRef.child("Crowdsourcing_test_(2017-03-08%5C)RAW_HPF.csv.gz");

//        StorageReference pathReference =
//                storageRef.child("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv.gz");

//        StorageReference pathReference =
//                storageRef.child("test.csv.gz");

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
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onProgress: " + taskSnapshot.toString());
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
//            fis = getAssets().open("sensor.csv");
            gis = new GZIPInputStream(new BufferedInputStream(fis));

            Log.d(TAG, "gunzipFile: Gunzipping file");

            InputStreamReader reader = new InputStreamReader(gis);
            BufferedReader bufferReader = new BufferedReader(reader);

//            parser(bufferReader);

        parser1(bufferReader);

        } catch (IOException | BufferOverflowException e) {
            Log.e(TAG, "gunzipFile: ", e);
        }
    }

    private void parser(BufferedReader bufferReader) {
//         BeanListProcessor converts each parsed row to an instance of a given class, then stores each instance into a list.
        BeanListProcessor<CSVAnnotatedModel> rowProcessor = new BeanListProcessor<>(CSVAnnotatedModel.class);

        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setRowProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);

        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(bufferReader);

        // The BeanListProcessor provides a list of objects extracted from the input.
        beanList = rowProcessor.getBeans();

        Log.d(TAG, "readData: size = " + beanList.size());

        if (beanList.size() > 0) {
            Toast.makeText(this, "Successful CSV parsing", Toast.LENGTH_LONG).show();
            plotAccGraph();
        } else {
            Toast.makeText(this, "Unsuccessful CSV parsing", Toast.LENGTH_LONG).show();
            Log.e(TAG, "unzipFile: Parsing unsuccessful...");
        }
    }

    private void parser1(BufferedReader bufferReader) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        // creates a CSV parser
        CsvParser csvParser = new CsvParser(settings);
        // parses all rows in one go.
        strArrList = csvParser.parseAll(bufferReader);

        Log.d(TAG, "readData: size = " + strArrList.size());

        if (strArrList.size() > 0) {
            Toast.makeText(this, "Successful CSV parsing", Toast.LENGTH_LONG).show();
            plotAccGraph();
        } else {
            Toast.makeText(this, "Unsuccessful CSV parsing", Toast.LENGTH_LONG).show();
            Log.e(TAG, "unzipFile: Parsing unsuccessful...");
        }
    }

    private void plotAccGraph() {
//        THIS PLOT DOES NOT HAVE THE ZOOM IN/ZOOM OUT FUNCTIONALITY
        Log.d(TAG, "plotXAccGraph: started");
        XYSeries series = new XYSeries("X-acceleration vs time");
        XYSeries series2 = new XYSeries("Y-acceleration vs time");
        XYSeries series3 = new XYSeries("Z-acceleration vs time");

        float milliSecond = 0.01f;

//        if (beanList.size() > 1) {
//            Log.d(TAG, "plotXAccGraph: making the series");
//            for (CSVAnnotatedModel str : beanList) {
//                series.add(milliSecond++, Double.parseDouble(str.getX_ACCELERATION_METERS_PER_SECOND_SQUARED()));
//                series2.add(milliSecond++, Double.parseDouble(str.getY_ACCELERATION_METERS_PER_SECOND_SQUARED()));
//                series3.add(milliSecond++, Double.parseDouble(str.getZ_ACCELERATION_METERS_PER_SECOND_SQUARED()));
//            }
//        }

        int c = 0;
        if (strArrList.size() > 1) {
            Log.d(TAG, "plotAccGraph: making the series");
            String[] str;
            for (int i = 1; i <= strArrList.size(); i++) {
                str = strArrList.get(i);
                series.add(milliSecond++, Double.parseDouble(str[1]));
                series2.add(milliSecond++, Double.parseDouble(str[2]));
                series3.add(milliSecond++, Double.parseDouble(str[3]));
                if (c == 1000)
                    break;
                else
                    c++;
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
        chart.addView(chartView, 0);
        chart.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }
}
