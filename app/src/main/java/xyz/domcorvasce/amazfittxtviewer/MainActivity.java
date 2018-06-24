package xyz.domcorvasce.amazfittxtviewer;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Spinner spinner, zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addDoubleTapEvent(findViewById(R.id.view));

        final int ZOOM_100_ITEM_POSITION = 3;

        this.spinner = findViewById(R.id.spinner);
        this.zoom = findViewById(R.id.zoom_spinner);
        this.zoom.setSelection(ZOOM_100_ITEM_POSITION);

        final TextView content = findViewById(R.id.content);
        final int BASE_TEXT_SIZE = 20;

        ArrayList<String> filenames = getTXTFiles();
        setSpinnerAdapter(filenames);

        this.zoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String zoomLevel = zoom.getSelectedItem().toString();
                float transformRate = 100;

                if (zoomLevel.endsWith("%")) {
                    int endIndex = zoomLevel.length() - 1;
                    transformRate = Float.parseFloat(zoomLevel.substring(0, endIndex));
                }

                float textSize = BASE_TEXT_SIZE * (transformRate / 100);
                content.setTextSize(textSize / getResources().getDisplayMetrics().scaledDensity);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private String text;
            private File targetFile;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filename = getSelectedFile();
                File downloadsPath = getDownloadsPath();
                this.targetFile = getFileByPath(downloadsPath, filename);

                try {
                    readFile();
                } catch (IOException exception) {
                    this.text = "Unable to read the file";
                } finally {
                    content.setText(this.text);
                }
            }


            private String getSelectedFile() {
                return spinner.getSelectedItem().toString();
            }


            private File getFileByPath(File path, String filename) {
                return new File(path, filename);
            }


            private void readFile() throws IOException {
                BufferedReader buffer = new BufferedReader(new FileReader(this.targetFile));
                String line;
                StringBuilder text = new StringBuilder();

                while ((line = buffer.readLine()) != null) {
                    text.append(line);
                    text.append("\n");
                }

                buffer.close();
                this.text = text.toString();
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    private void addDoubleTapEvent(View view) {
        final GestureDetector detector = new GestureDetector(
                getApplicationContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent event) {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(homeIntent);
                        return true;
                    }
                }
        );

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
    }


    private void setSpinnerAdapter(ArrayList<String> filenames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filenames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.spinner.setAdapter(adapter);
    }


    private ArrayList<String> getTXTFiles() {
        File downloadsPath = getDownloadsPath();
        File[] files = downloadsPath.listFiles();

        if (files != null) {
            final String FILE_EXTENSION = ".txt";
            return getFilenamesByExtension(files, FILE_EXTENSION);
        }

        return new ArrayList<>();
    }


    private ArrayList<String> getFilenamesByExtension(File[] files, String extension) {
        ArrayList<String> filenames = new ArrayList<>();

        for (File file : files) {
            String filename = file.getName();

            if (filename.endsWith(extension))
                filenames.add(filename);
        }

        return filenames;
    }


    private File getDownloadsPath() {
        File storage = Environment.getExternalStorageDirectory();
        String storagePath = storage.toString();
        String downloadsPath = storagePath + File.separator + Environment.DIRECTORY_DOWNLOADS;

        return new File(downloadsPath);
    }
}
