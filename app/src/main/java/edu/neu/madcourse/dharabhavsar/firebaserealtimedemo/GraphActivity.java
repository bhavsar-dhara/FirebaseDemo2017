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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class GraphActivity extends AppCompatActivity {

    private static final String TAG = GraphActivity.class.getSimpleName();

    private static final String X_ACC_HEADER = "X_ACCELERATION_METERS_PER_SECOND_SQUARED";
    private static final String Y_ACC_HEADER = "Y_ACCELERATION_METERS_PER_SECOND_SQUARED";
    private static final String Z_ACC_HEADER = "Z_ACCELERATION_METERS_PER_SECOND_SQUARED";

    File fileDir;
    String INPUT_GZIP_FILE;
    String OUTPUT_FILE;

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

        if(makeDir()) {
            File file = new File(fileDir, OUTPUT_FILE);
            if(file.exists()) {
                Log.d(TAG, "gunZipIt: File Exists. Size: " + file.getTotalSpace());
                Toast.makeText(this, "File exists", Toast.LENGTH_LONG).show();
            } else {
                try {
                    InputStream fis = new FileInputStream(INPUT_GZIP_FILE);
                    GZIPInputStream gis = new GZIPInputStream(new BufferedInputStream(fis));

                    Log.d(TAG, "gunZipIt: Downloading file as it is not on sdcard");

                    InputStreamReader reader = new InputStreamReader(gis);
                    BufferedReader bufferReader = new BufferedReader(reader);
                    OutputStream fout = new FileOutputStream(OUTPUT_FILE);
                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;

                    while ((count = bufferReader.read()) != -1) {
                        total += count;
                        fout.write(data, 0, count);
                    }

//                    while ((count = gis.read(data)) > 0) {
//                        fout.write(data, 0, count);
//                    }

                    fout.flush();
                    fout.close();
                    bufferReader.close();
                    reader.close();
                    gis.close();
                    fis.close();
                    Log.d(TAG, "gunZipIt: SUCCESSFUL.. total = " + total);
                } catch(IOException | BufferOverflowException e) {
                    Log.e(TAG, "gunZipIt: ", e);
                }

                Log.d(TAG, "gunZipIt: DONE.. ");
//                file = new File(fileDir, OUTPUT_FILE);
                if(file.exists()) {
                    Log.d(TAG, "gunZipIt: File downloaded successfully. Size: " + file.getTotalSpace());
                    Toast.makeText(this, "File downloaded successfully", Toast.LENGTH_LONG).show();
                    // TODO : read and plot
                    plotXAccGraph();
                } else {
                    Log.e(TAG, "gunZipIt: File doesn't exists. Oops something went wrong.");
                    Toast.makeText(this, "File not downloaded successfully", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.e(TAG, "gunZipIt: file directory generation was unsuccessful");
            Toast.makeText(this, "File directory generation was unsuccessful", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    private List<Double> readExcelData(String key) {
        List<Double> resultSet = new ArrayList<Double>();

        File inputWorkbook = new File(OUTPUT_FILE);
        if(inputWorkbook.exists()) {
            Workbook w;
            try {
                w = Workbook.getWorkbook(inputWorkbook);
                // Get the first sheet
                Sheet sheet = w.getSheet(0);
                // Loop over column and lines
                int row = sheet.getRows();
                for (int j = 0; j < row; j++) {
                    Cell cell = sheet.getCell(0, j);
                    if(cell.getContents().equalsIgnoreCase(key)){
                        for (int i = 1; i <= sheet.getColumns(); i++) {
                            Cell cel = sheet.getCell(i, j);
                            resultSet.add(Double.parseDouble(cel.getContents()));
                        }
                    }
                    continue;
                }
            } catch (BiffException e) {
                e.printStackTrace();
                Log.e(TAG, "readExcelData: BiffException: ", e);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "readExcelData: Exception: ", e);
            }
        } else {
            Log.e(TAG, "readExcelData: file not found");
            resultSet.add(0d);
        }

        if(resultSet.size()==0){
            Log.e(TAG, "readExcelData: data not found");
            resultSet.add(0d);
        }

        return resultSet;

//        try {
//            AssetManager am=getAssets();
//            InputStream is=am.open("book.xls");
//            Workbook wb=Workbook.getWorkbook(is);
//            Sheet s=wb.getSheet(0);
//            int row=s.getRows();
//            int col=s.getColumns();
//
//            String xx="";
//            for (int i=0;i<row;i++)
//            {
//
//                for(int c=0;c<col;c++)
//                {
//                    Cell z=s.getCell(c,i);
//                    xx=xx+z.getContents();
//
//                }
//
//                xx=xx+"\n";
//            }
//            //display(xx);
//        } catch (Exception e){
//            Log.e(TAG, "readExcelData: ", e);
//        }

    }

    private void plotXAccGraph() {
        XYSeries series = new XYSeries("X-acceleration vs time");

        float milliSecond = 0.01f;
        List<Double> resultString = readExcelData(X_ACC_HEADER);
        if (resultString.size() > 1) {
            for (Double str : resultString) {
                series.add(milliSecond++, str);
            }
        }

        // Now we add our series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

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

        chartLyt.addView(chartView, 0);
        chartLyt.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }
}
