package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.CSVAnnotatedModel;

public class GraphActivity extends AppCompatActivity {

    private static final String TAG = GraphActivity.class.getSimpleName();

    File fileDir;
    String INPUT_GZIP_FILE;
    String OUTPUT_FILE;
    List<CSVAnnotatedModel> beanList;

    FirebaseStorage storage;
    StorageReference storageRef;

    LinearLayout chart;
    GraphView chartLyt;
    GraphView chartLyt2;
    GraphView chartLyt3;

    private static final int PROGRESS = 0x1;

    private LinearLayout mProgressBarLayout;
    private ProgressBar mProgress;
    private int mProgressStatus = 0;

    private Handler mHandler = new Handler();

    CSVAnnotatedModel[] resultList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        chartLyt = (GraphView) findViewById(R.id.chart);
        chartLyt2 = (GraphView) findViewById(R.id.chart2);
        chartLyt3 = (GraphView) findViewById(R.id.chart3);
        mProgressBarLayout = (LinearLayout) findViewById(R.id.progress_bar_layout);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);

        downloadFile();
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

//        StorageReference pathReference =
//                storageRef.child("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv.gz");

        StorageReference pathReference =
                storageRef.child("test.csv.gz");

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

    private boolean makeDir() {
        Log.e(TAG, "makeDir::: mounted = " + Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
        Log.e(TAG, "makeDir::: removed = " + Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED));
        Log.e(TAG, "makeDir::: is storage emulated = " + Environment.isExternalStorageEmulated());
        Log.e(TAG, "makeDir::: is storage emulated = " + Environment.isExternalStorageRemovable());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) &&
                Environment.isExternalStorageRemovable()) {
            File extdir = Environment.getExternalStorageDirectory();
            long freeSpaceExt = extdir.getFreeSpace();
            long totalSpaceExt = extdir.getTotalSpace();
            Log.d(TAG, "makeDir: free space = " + freeSpaceExt + ", total space = " + totalSpaceExt);
            String root = Environment.getExternalStorageDirectory().toString();
            fileDir = new File(root + "/saved_csv_files");
            if (!fileDir.exists()) {
                Log.e(TAG, "makeDir: directory " + fileDir.toString() + " not found");
                return fileDir.mkdirs();
            } else {
                Log.d(TAG, "makeDir: directory " + fileDir.toString() + " exists");
                return true;
            }
        } else {
            fileDir = new File(getFilesDir() + "/saved_csv_files");
            Log.d(TAG, "makeDir: is a directory: " + fileDir.isDirectory());
            Log.d(TAG, "makeDir: is a file: " + fileDir.isFile());
            boolean isSuccess = fileDir.mkdirs();
            Log.d(TAG, "makeDir: is file directory generation successful = " + isSuccess
                    + ", fileDir = " + fileDir.toString());
            long freeSpace = fileDir.getFreeSpace();
            long totalSpace = fileDir.getTotalSpace();
            Log.d(TAG, "makeDir: free space = " + freeSpace + ", total space = " + totalSpace
                    + ", exists = " + fileDir.exists());
            return fileDir.exists();
        }
    }

    private void gunZipIt(String path, String zipName) {
        INPUT_GZIP_FILE = path + "/" + zipName;
        OUTPUT_FILE = INPUT_GZIP_FILE.replaceAll("(.gz)+([^\\s])+(.tmp)", "");
        Log.d(TAG, "gunZipIt: OUTPUT_FILE: " + OUTPUT_FILE);

        InputStream fis = null;
        GZIPInputStream gis;
        if(makeDir()) {
            File file = new File(fileDir, OUTPUT_FILE);
            if(file.exists()) {
                Log.d(TAG, "gunZipIt: File Exists. Size: " + file.getTotalSpace());
                Toast.makeText(this, "File exists", Toast.LENGTH_LONG).show();
            } else {
                try {
                    fis = new FileInputStream(INPUT_GZIP_FILE);
                    gis = new GZIPInputStream(new BufferedInputStream(fis));

                    Log.d(TAG, "gunZipIt: Downloading file as it is not on sdcard");

                    InputStreamReader reader = new InputStreamReader(gis);
                    BufferedReader bufferReader = new BufferedReader(reader);
                    OutputStream fout = new FileOutputStream(OUTPUT_FILE);
                    BufferedOutputStream bos = new BufferedOutputStream(fout, 1024);
                    byte[] buffer = new byte[1024];
                    char[] cbuf = new char[1024];
//                    long total = 0;
                    int count;
                    while ((count = bufferReader.read(cbuf, 0, 1024)) != -1) {
//                        Log.d(TAG, "gunZipIt: coiunt = " + count);
//                        total += count;
//                        fout.write(buffer, 0, count);
                        bos.write(buffer, 0, count);

                    }

//                    while ((count = gis.read(data)) > 0) {
//                        fout.write(data, 0, count);
//                    }

                    bos.close();
                    fout.flush();
                    fout.close();
                    bufferReader.close();
                    reader.close();
                    gis.close();
                    fis.close();
                    Log.d(TAG, "gunZipIt: SUCCESSFUL.. count = " + count);
                } catch(IOException | BufferOverflowException e) {
                    Log.e(TAG, "gunZipIt: ", e);
                } finally {
                    try {
                        if (fis!=null) {
                            fis.close();
                        }
                    } catch (IOException ioe) {
                        System.out.println("Error while closing zip file" + ioe);
                    }
                }

                Log.d(TAG, "gunZipIt: DONE.. ");
                plotXAccGraph();
//                plotAccGraph();
//                readData();

//                file = new File(fileDir, OUTPUT_FILE);
//                if(file.exists()) {
//                    Log.d(TAG, "gunZipIt: File downloaded successfully. Size: " + file.getTotalSpace());
//                    Toast.makeText(this, "File downloaded successfully", Toast.LENGTH_LONG).show();
//                    // TODO : read and plot
//                    plotXAccGraph();
//                } else {
//                    Log.e(TAG, "gunZipIt: File doesn't exists. Oops something went wrong.");
//                    Toast.makeText(this, "File not downloaded successfully", Toast.LENGTH_LONG).show();
//                }
            }
        } else {
            Log.e(TAG, "gunZipIt: file directory generation was unsuccessful");
            Toast.makeText(this, "File directory generation was unsuccessful", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    public Reader getReader(String relativePath) {
        Log.d(TAG, "getReader: " + relativePath);
        Log.d(TAG, "getReader: exists = " + new File(relativePath).exists());
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(relativePath))));
//            reader = new InputStreamReader(new FileInputStream (relativePath));
//            reader = new InputStreamReader(getAssets().open("test.csv")); // THIS WORKED YAYYY
//            reader = new InputStreamReader(getAssets().open("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv")); // THIS WORKED YAYYY
        } /*catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getReader: FileNotFoundException ", e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/ catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getReader: IOException ", e);
        }
        return reader;
    }

    public List readData() {
        // BeanListProcessor converts each parsed row to an instance of a given class, then stores each instance into a list.
        BeanListProcessor<CSVAnnotatedModel> rowProcessor = new BeanListProcessor<>(CSVAnnotatedModel.class);

        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setRowProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);

        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(getReader(OUTPUT_FILE));

        // The BeanListProcessor provides a list of objects extracted from the input.
        List<CSVAnnotatedModel> beans = rowProcessor.getBeans();
        for (CSVAnnotatedModel bean : beans) {
            Log.e(TAG, "readData: rowProcessed " + bean.toString());
        }

        Log.d(TAG, "readData: size = " + beans.size());
        return beans;
    }

    private void plotAccGraph() {
        Log.d(TAG, "plotXAccGraph: started");
        XYSeries series = new XYSeries("X-acceleration vs time");
        XYSeries series2 = new XYSeries("Y-acceleration vs time");
        XYSeries series3 = new XYSeries("Z-acceleration vs time");

        float milliSecond = 0.01f;
        List<CSVAnnotatedModel> resultString = readData();
        if (resultString.size() > 1) {
            Log.d(TAG, "plotXAccGraph: making the series");
            for (CSVAnnotatedModel str : resultString) {
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
//        chartLyt.addView();
        chartLyt.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }

    private void plotXAccGraph() {
        Log.d(TAG, "plotXAccGraph: started");
        List<CSVAnnotatedModel> resultString = readData();

        double milliSecond = 0.01d;
        DataPoint[] dataPointArrayX = null;
        List<DataPoint> dataPointListX = new ArrayList<>();
        DataPoint[] dataPointArrayY = null;
        List<DataPoint> dataPointListY = new ArrayList<>();
        DataPoint[] dataPointArrayZ = null;
        List<DataPoint> dataPointListZ = new ArrayList<>();

        if (resultString.size() > 1) {
            Log.d(TAG, "plotXAccGraph: making the series");
            for (CSVAnnotatedModel str : resultString) {
                DataPoint dataPointX = new DataPoint(milliSecond,
                        Double.parseDouble(str.getX_ACCELERATION_METERS_PER_SECOND_SQUARED()));
                dataPointListX.add(dataPointX);
                DataPoint dataPointY = new DataPoint(milliSecond,
                        Double.parseDouble(str.getY_ACCELERATION_METERS_PER_SECOND_SQUARED()));
                dataPointListY.add(dataPointY);
                DataPoint dataPointZ = new DataPoint(milliSecond,
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
//        chartLyt.setLegendRenderer(legendRenderer);
//        chartLyt.setTitle("X Acceleration vs. Time");
//        chartLyt.setTitleTextSize(14);
//        chartLyt.setTitleColor(Color.BLACK);
//
//        chartLyt2.addSeries(seriesY);
////        chartLyt2.setLegendRenderer(legendRenderer);
//        chartLyt2.setTitle("Y Acceleration vs. Time");
//        chartLyt2.setTitleTextSize(14);
//        chartLyt2.setTitleColor(Color.BLACK);
//
//        chartLyt3.addSeries(seriesZ);
////        chartLyt3.setLegendRenderer(legendRenderer);
//        chartLyt3.setTitle("Z Acceleration vs. Time");
//        chartLyt3.setTitleTextSize(14);
//        chartLyt3.setTitleColor(Color.BLACK);


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
//        chartLyt2.setVisibility(View.VISIBLE);
//        chartLyt3.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }
}