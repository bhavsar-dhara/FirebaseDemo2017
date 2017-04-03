package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.testing;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.BufferOverflowException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.R;
import edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model.CSVAnnotatedModel;

/**
 * Created by Dhara on 4/2/2017.
 */

public class GZIPTest extends AppCompatActivity {

    private static final String TAG = GZIPTest.class.getSimpleName();

    File fileDir;
    String OUTPUT_FILE_CACHE;
    String OUTPUT_FILE;
    List<CSVAnnotatedModel> beanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

//        gunzipFile();
//        gunzipIt();
//        gunzipFile2();
        gunzipFile3();
    }

    private void gunzipFile3() {

        fileDir = new File(getFilesDir(), "/saved_csv_files");
        boolean isSuccess = fileDir.exists() || fileDir.mkdirs();
        if (isSuccess) {
            OUTPUT_FILE_CACHE = "testing4.csv";
            File outputFile = new File(fileDir, OUTPUT_FILE_CACHE);
            OUTPUT_FILE = fileDir.getAbsolutePath() + "/" + OUTPUT_FILE_CACHE;

            boolean isCreated = false;
            try {
                isCreated = outputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "gunzipFile: IOException: ", e);
            }

            if (!outputFile.exists() && !outputFile.isDirectory() && !isCreated) {

                Log.e(TAG, "gunzipFile: File DOES NOT Exists. Size: " + outputFile.getTotalSpace());
                Toast.makeText(this, "File NOT exists", Toast.LENGTH_LONG).show();
                finish();
            } else {
                InputStream fis;
                GZIPInputStream gis;
                try {
                    fis = getAssets().open("test.csv.bin");
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
                    }

                } catch (IOException | BufferOverflowException e) {
                    Log.e(TAG, "gunzipFile: ", e);
                }
            }
        }
    }

    private void gunzipFile2() {

        try {
            InputStream fis = getAssets().open("test.csv.bin");
//            InputStream fis = getAssets().open("sensor.csv.bin");
            GZIPInputStream gis = new GZIPInputStream(fis);
            InputStreamReader isr = new InputStreamReader(gis);
            BufferedReader br = new BufferedReader(isr);
            CSVReader reader = new CSVReader(br);

            String [] nextLine;

            int c = 1;
            while ((nextLine = reader.readNext()) != null) {
                Log.d(TAG, "gunzipFile2: " + nextLine[0] + " ... " + nextLine[1] + " .. ");
                if (c == 10) {
                    break;
                }
                c++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gunzipFile() {

//        OUTPUT_FILE_CACHE = "sensor.csv";
        OUTPUT_FILE_CACHE = "testing3.csv";

        InputStream fis = null;
        GZIPInputStream gis;

        if (makeDir()) {

            File outputFile = new File(fileDir, OUTPUT_FILE_CACHE);
            OUTPUT_FILE = fileDir.getAbsolutePath() + "/" + OUTPUT_FILE_CACHE;
            Log.d(TAG, "gunzipFile: " + fileDir.getAbsolutePath());
            boolean isCreated = false;
            try {
                isCreated = outputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "gunzipFile: IOException: ", e);
            }

            if (!outputFile.exists() && !outputFile.isDirectory() && !isCreated) {

                Log.e(TAG, "gunzipFile: File DOES NOT Exists. Size: " + outputFile.getTotalSpace());
                Toast.makeText(this, "File NOT exists", Toast.LENGTH_LONG).show();
                finish();
            } else {

                try {
                    fis = getAssets().open("test.csv.bin");
                    gis = new GZIPInputStream(new BufferedInputStream(fis));

                    Log.d(TAG, "gunzipFile: Gunzipping file");

                    InputStreamReader reader = new InputStreamReader(gis);
                    BufferedReader bufferReader = new BufferedReader(reader);
                    OutputStream fout = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    char[] cbuf = new char[1024];
                    int count;
                    while ((count = bufferReader.read(cbuf, 0, 1024)) != -1) {
                        fout.write(buffer, 0, count);
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
                    Log.d(TAG, "gunzipFile: SUCCESSFUL.. count = " + count);
                } catch (IOException | BufferOverflowException e) {
                    Log.e(TAG, "gunzipFile: ", e);
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException ioe) {
                        System.out.println("Error while closing zip file" + ioe);
                    }
                }

                Log.d(TAG, "gunzipFile: DONE.. ");

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(OUTPUT_FILE)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    int c = 1;
                    String line;
                    while (((line = reader.readLine()) != null) && c < 10) {
                        String[] RowData = line.split(",");
                        c++;
                        Log.e(TAG, "gunZipIt: rowData = " + RowData.toString());
                    }
                } catch (IOException ex) {
                    // handle exception
                } finally {
                    try {
                        reader.close();
                    } catch (IOException | NullPointerException e) {
                        // handle exception
                        Log.e(TAG, "gunZipIt: rowData = ", e);
                    }
                }

                readData();
            }
        } else {

            Log.e(TAG, "gunzipFile: file directory generation was unsuccessful");
            Toast.makeText(this, "File directory generation was unsuccessful", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    private boolean makeDir() {

        Log.e(TAG, "makeDir::: mounted = " + Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
        Log.e(TAG, "makeDir::: removed = " + Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED));
        Log.e(TAG, "makeDir::: is storage emulated = " + Environment.isExternalStorageEmulated());
        Log.e(TAG, "makeDir::: is storage removable = " + Environment.isExternalStorageRemovable());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) &&
                Environment.isExternalStorageRemovable()) {
            File extdir = Environment.getExternalStorageDirectory();
            long freeSpaceExt = extdir.getFreeSpace();
            long totalSpaceExt = extdir.getTotalSpace();
            Log.d(TAG, "makeDir: if: free space = " + freeSpaceExt + ", total space = " + totalSpaceExt);
            File root = Environment.getExternalStorageDirectory();
            Log.d(TAG, "makeDir: if: root: " + root);
            fileDir = new File(root, "/saved_csv_files");
            if (!fileDir.exists()) {
                Log.e(TAG, "makeDir: if: directory: " + fileDir.toString() + " not found");
                return fileDir.mkdirs();
            } else {
                Log.d(TAG, "makeDir: if: directory " + fileDir.toString() + " exists");
                return true;
            }
        } else {
            fileDir = new File(getFilesDir(), "/saved_csv_files");
            Log.d(TAG, "makeDir: else: is a directory: " + fileDir.isDirectory());
            Log.d(TAG, "makeDir: else: is a file: " + fileDir.isFile());
            boolean isSuccess = false;
            if (!fileDir.exists()) {
                isSuccess = fileDir.mkdirs();
            } else {
                isSuccess = true;
            }
            Log.d(TAG, "makeDir: else: is file directory generation successful = " + isSuccess
                    + ", fileDir = " + fileDir.toString());
            long freeSpace = fileDir.getFreeSpace();
            long totalSpace = fileDir.getTotalSpace();
            Log.d(TAG, "makeDir: else: free space = " + freeSpace + ", total space = " + totalSpace
                    + ", exists = " + fileDir.exists());
            return fileDir.exists();
        }
    }


    public Reader getReader(String relativePath) {
        Log.d(TAG, "getReader: " + relativePath);
        Log.d(TAG, "getReader: exists = " + new File(relativePath).exists());
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(new File(relativePath)));
//            reader = new InputStreamReader(new FileInputStream (relativePath));
//            reader = new InputStreamReader(getAssets().open("test.csv")); // THIS WORKED YAYYY
//            reader = new InputStreamReader(getAssets().open("sensor.csv")); // THIS WORKED YAYYY
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
        beanList = rowProcessor.getBeans();
//        for (CSVAnnotatedModel bean : beans) {
//            Log.e(TAG, "readData: rowProcessed " + bean.toString());
//        }

        Log.d(TAG, "readData: size = " + beanList.size());
        return beanList;
    }

    public void gunzipIt() {
        fileDir = new File(getFilesDir(), "/saved_csv_files");
        Log.d(TAG, "makeDir: else: is a directory: " + fileDir.isDirectory());
        Log.d(TAG, "makeDir: else: is a file: " + fileDir.isFile());
        boolean isSuccess = false;
        if (!fileDir.exists()) {
            isSuccess = fileDir.mkdirs();
        } else {
            isSuccess = true;
        }
        Log.d(TAG, "makeDir: else: is file directory generation successful = " + isSuccess
                + ", fileDir = " + fileDir.toString());
        long freeSpace = fileDir.getFreeSpace();
        long totalSpace = fileDir.getTotalSpace();
        Log.d(TAG, "makeDir: else: free space = " + freeSpace + ", total space = " + totalSpace
                + ", exists = " + fileDir.exists());

        OUTPUT_FILE_CACHE = "testing4.csv";
        File outputFile = new File(fileDir, OUTPUT_FILE_CACHE);
        OUTPUT_FILE = fileDir.getAbsolutePath() + "/" + OUTPUT_FILE_CACHE;

        byte[] buffer = new byte[1024];

        try {
            InputStream fis = getAssets().open("test.csv.bin");

            GZIPInputStream gzis =
                    new GZIPInputStream(fis);

            FileOutputStream out =
                    new FileOutputStream(OUTPUT_FILE);

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

            System.out.println("Done");

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(OUTPUT_FILE)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                int c = 1;
                String line;
                while (((line = reader.readLine()) != null) && c < 10) {
                    String[] RowData = line.split(",");
                    c++;
                    Log.e(TAG, "gunZipIt: rowData = " + RowData[0] + " .. " + RowData[1]);
                }
            } catch (IOException ex) {
                // handle exception
            } finally {
                try {
                    reader.close();
                } catch (IOException | NullPointerException e) {
                    // handle exception
                    Log.e(TAG, "gunZipIt: rowData = ", e);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
