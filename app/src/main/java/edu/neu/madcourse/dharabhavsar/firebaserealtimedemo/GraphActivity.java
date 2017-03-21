package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

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

public class GraphActivity extends AppCompatActivity {

    private static final String TAG = GraphActivity.class.getSimpleName();

    FirebaseStorage storage;
    StorageReference storageRef;

    LinearLayout chartLyt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        chartLyt = (LinearLayout) findViewById(R.id.chart);

        downloadFile();
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

        String root = Environment.getExternalStorageDirectory().toString();
        File fileDir = new File(root + "/saved_csv_files");
        fileDir.mkdirs();

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

        File file = new File(fileDir, OUTPUT_FILE);
        if(file.exists()) {
            //Do something
            Log.d(TAG, "gunZipIt: File Exists. Size: " + file.getTotalSpace());
//            plotXAccGraph();
        } else {
            // Do something else.
            Log.e(TAG, "gunZipIt: File doesn't exists. Oops something went wrong.");
        }
    }

    private void plotXAccGraph() {
        XYSeries series = new XYSeries("X-acceleration vs time");

        float milliSecond = 0.01f;
//        for (HourForecast hf : nextHourForecast) {
//            series.add(milliSecond++, hf.weather.temperature.getTemp());
//        }

        // Now we create the renderer
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setLineWidth(2);
        renderer.setColor(Color.RED);
        // Include low and max value
        renderer.setDisplayBoundingPoints(true);
        // we add point markers
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(3);

        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);

        // We want to avoid black border
        // transparent margins
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(35);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowGrid(true); // we show the grid

//        GraphicalView chartView = ChartFactory.
//                getLineChartView(this, dataset, mRenderer);
//
//        chartLyt.addView(chartView, 0);
    }
}
