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
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    LinearLayout chartLyt;

    private static final int PROGRESS = 0x1;

    private LinearLayout mProgressBarLayout;
    private ProgressBar mProgress;
    private int mProgressStatus = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        chartLyt = (LinearLayout) findViewById(R.id.chart);
        mProgressBarLayout = (LinearLayout) findViewById(R.id.progress_bar_layout);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);

        downloadFile();
    }

    private void showProgressStatus(final long fileSize) {
        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (mProgressStatus < 100) {
                    mProgressStatus = doDownload(fileSize);

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

    private int doDownload(long fileSize) {

        while (fileSize > 610949485) {

            fileSize--;

            if (fileSize == 54900000) {
                Log.d(TAG, "doDownload: 10");
                return 10;
            } else if (fileSize == 488000000) {
                Log.d(TAG, "doDownload: 20");
                return 20;
            } else if (fileSize == 427000000) {
                Log.d(TAG, "doDownload: 30");
                return 30;
            } else if (fileSize == 366000000) {
                Log.d(TAG, "doDownload: 40");
                return 40;
            } else if (fileSize == 305000000) {
                Log.d(TAG, "doDownload: 50");
                return 50;
            } else if (fileSize == 244000000) {
                Log.d(TAG, "doDownload: 60");
                return 60;
            } else if (fileSize == 183000000) {
                Log.d(TAG, "doDownload: 70");
                return 70;
            } else if (fileSize == 122000000) {
                Log.d(TAG, "doDownload: 80");
                return 80;
            } else if (fileSize == 61000000) {
                Log.d(TAG, "doDownload: 90");
                return 90;
            }

        }

        return 100;
    }

    private void downloadFile() {
        storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl("gs://testapp-102e7.appspot.com");

        StorageReference pathReference =
                storageRef.child("ActigraphGT9X-AccelerationCalibrated-NA.TAS1E23150066-AccelerationCalibrated.2015-10-08-14-00-00-000-M0400.sensor.csv.gz");

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

    public CSVReader createReader() {
        Log.d(TAG, "createReader: output file = " + OUTPUT_FILE);
        CSVReader reader = null;
        try {
             reader = new CSVReader(new InputStreamReader(new FileInputStream(OUTPUT_FILE)), ',' , '"' , 1);
//            reader = new CSVReader(new FileReader(OUTPUT_FILE));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "createReader: FileNotFoundException ", e);
        }
        Log.d(TAG, "createReader: reader created successfully .. " + reader.getLinesRead() + " ... " + reader.getRecordsRead());
        return reader;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<CSVAnnotatedModel> readCSVData() {
        Log.d(TAG, "readCSVData: reading CSV data using the opencsv library");
        HeaderColumnNameMappingStrategy<CSVAnnotatedModel> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(CSVAnnotatedModel.class);
//        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
//        strategy.setType(CSVAnnotatedModel.class);
//        String[] columns = new String[] {"HEADER_TIME_STAMP", "X_ACCELERATION_METERS_PER_SECOND_SQUARED",
//                "Y_ACCELERATION_METERS_PER_SECOND_SQUARED", "Z_ACCELERATION_METERS_PER_SECOND_SQUARED"};
//        strategy.setColumnMapping(columns);


        CsvToBean<CSVAnnotatedModel> csvToBean = new CsvToBean<>();
//        CsvToBean csv = new CsvToBean();

        CSVReader reader = createReader();

//        List<String[]> records = null;
//        try {
//            records = reader.readAll();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Iterator<String[]> iterator = records.iterator();
//        while (iterator.hasNext()) {
//            String[] record = iterator.next();
//            CSVAnnotatedModel csvAnnotatedModel = new CSVAnnotatedModel();
//            try {
//                 csvAnnotatedModel.setHEADER_TIME_STAMP(df.parse(record[0]));
//            } catch (ParseException e) {
//                 e.printStackTrace();
//            }
//            csvAnnotatedModel.setX_ACCELERATION_METERS_PER_SECOND_SQUARED(record[1]);
//            csvAnnotatedModel.setY_ACCELERATION_METERS_PER_SECOND_SQUARED(record[2]);
//            csvAnnotatedModel.setZ_ACCELERATION_METERS_PER_SECOND_SQUARED(record[3]);
//            beanList.add(csvAnnotatedModel);
//        }

        DateFormat df = new SimpleDateFormat();
        int insideWhile = 0;
        String[] currRow = null;
        if (reader == null) {
            Log.e(TAG, "readCSVData: reader is null");
        } else {
            try {
                while ((currRow = reader.readNext()) != null) {
                    insideWhile++;
                    String[] record = currRow;
                    Log.d(TAG, "readCSVData: " + record[0] + ".." + record[1] + ".."
                            + record[2] + ".." + record[3]);
                    CSVAnnotatedModel csvAnnotatedModel = new CSVAnnotatedModel();
                    try {
                        csvAnnotatedModel.setHEADER_TIME_STAMP(df.parse(record[0]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    csvAnnotatedModel.setX_ACCELERATION_METERS_PER_SECOND_SQUARED(record[1]);
                    csvAnnotatedModel.setY_ACCELERATION_METERS_PER_SECOND_SQUARED(record[2]);
                    csvAnnotatedModel.setZ_ACCELERATION_METERS_PER_SECOND_SQUARED(record[3]);
                    beanList.add(csvAnnotatedModel);
                }
                Log.d(TAG, "readCSVData: ... " + insideWhile + " ... "
                        + reader.getLinesRead() + " ... " + reader.getRecordsRead());
            } catch (IOException e) {
                e.printStackTrace();
            }
//            beanList = csvToBean.parse(strategy, reader);
//            beanList = csv.parse(strategy, reader);
        }

//        for (CSVAnnotatedModel object : beanList) {
//            Log.d(TAG, "readCSVData::: " + object.toString());
//        }

        if(beanList.size()==0){
            Log.e(TAG, "readCSVData: data not found");
            beanList.add(null);
        } else {
            Log.d(TAG, "readCSVData: data read into beanlist successfully");
        }

        return beanList;
    }

    private void plotXAccGraph() {
        Log.d(TAG, "plotXAccGraph: started");
        XYSeries series = new XYSeries("X-acceleration vs time");

        float milliSecond = 0.01f;
        List<CSVAnnotatedModel> resultString = readCSVData();
        if (resultString.size() > 1) {
            Log.d(TAG, "plotXAccGraph: making the series");
            for (CSVAnnotatedModel str : resultString) {
                series.add(milliSecond++, Double.parseDouble(str.getX_ACCELERATION_METERS_PER_SECOND_SQUARED()));
            }
        }

        // Now we add our series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);
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

        // Finaly we create the multiple series renderer to control the graph
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        Log.d(TAG, "plotXAccGraph: renderer building going on");

        // We want to avoid black border
        // transparent margins
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(35);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowGrid(true); // we show the grid

        GraphicalView chartView = ChartFactory.
                getLineChartView(this, dataset, mRenderer);

        Log.d(TAG, "plotXAccGraph: graphical chart view created");
        chartLyt.addView(chartView, 0);
        chartLyt.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }
}
