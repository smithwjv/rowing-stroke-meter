package smithwjv.rowingstrokemeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private boolean pause;
    private double xAxisLastXValue;
    private double yAxisLastXValue;
    private double zAxisLastXValue;
    private int defaultYBound = 25;
    private int maxDataPoints = 128;
    private int maxYBound = 32;
    private BufferedWriter mBufferedWriter;
    private Button startStopButton;
    private GraphView xAxisGraphView;
    private GraphView yAxisGraphView;
    private GraphView zAxisGraphView;
    private LineGraphSeries<DataPoint> xAxisSeries;
    private LineGraphSeries<DataPoint> yAxisSeries;
    private LineGraphSeries<DataPoint> zAxisSeries;
    private ProgressBar recordingProgressBar;
    private SeekBar yAxisBoundSeekBar;
    private Sensor linearAccelerationSensor;
    private SensorManager mSensorManager;
    private final String LOG_TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        recordingProgressBar = (ProgressBar) findViewById(R.id.pb_recording);

        startStopButton = (Button) findViewById(R.id.btn_start_stop);
        startStopButton.setText(R.string.button_start);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pause) {
                    pause = false;
                    onResume();
                } else {
                    onPause();
                }
            }
        });

        yAxisBoundSeekBar = (SeekBar) findViewById(R.id.sb_y_axis_bound);
        yAxisBoundSeekBar.setMax(maxYBound);
        yAxisBoundSeekBar.setProgress(defaultYBound);
        yAxisBoundSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                xAxisGraphView.getViewport().setMinY(-value);
                xAxisGraphView.getViewport().setMaxY(value);
                yAxisGraphView.getViewport().setMinY(-value);
                yAxisGraphView.getViewport().setMaxY(value);
                zAxisGraphView.getViewport().setMinY(-value);
                zAxisGraphView.getViewport().setMaxY(value);
            }
        });

        xAxisGraphView = (GraphView) findViewById(R.id.gv_linear_acceleration_x_axis);
        yAxisGraphView = (GraphView) findViewById(R.id.gv_linear_acceleration_y_axis);
        zAxisGraphView = (GraphView) findViewById(R.id.gv_linear_acceleration_z_axis);
        resetGraphs();

        mSensorManager = (SensorManager) getSystemService(MainActivity.SENSOR_SERVICE);
        // check if linear acceleration sensor exists
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            linearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor
                    .TYPE_LINEAR_ACCELERATION);
            pause = true;
        } else {
            Toast.makeText(this, R.string.error_sensor, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // update graphs
        xAxisLastXValue += 1d;
        xAxisSeries.appendData(new DataPoint(xAxisLastXValue,  (double) event.values[0]), true
                , maxDataPoints);
        yAxisLastXValue += 1d;
        yAxisSeries.appendData(new DataPoint(yAxisLastXValue,  (double) event.values[1]), true
                , maxDataPoints);
        zAxisLastXValue += 1d;
        zAxisSeries.appendData(new DataPoint(zAxisLastXValue,  (double) event.values[2]), true
                , maxDataPoints);

        // write to file
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"
                , Locale.UK);
        String data = dateFormat.format(System.currentTimeMillis() - SystemClock.elapsedRealtime()
                + (event.timestamp / 1000000L)) + " "
                + String.format(Locale.UK, "%-16.9f", event.values[0])
                + String.format(Locale.UK, "%-16.9f", event.values[1])
                + String.format(Locale.UK, "%-16.9f", event.values[2]) + "\n";
        try {
            mBufferedWriter.append(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!pause) {
            startStopButton.setText(R.string.button_stop);
            recordingProgressBar.setVisibility(View.VISIBLE);
            if (isExternalStorageWritable()) {
                try {
                    mBufferedWriter = new BufferedWriter(new FileWriter(getFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, R.string.error_write, Toast.LENGTH_SHORT).show();
            }
            mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager
                    .SENSOR_DELAY_GAME);
            pause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        pause = true;
        startStopButton.setText(R.string.button_start);
        recordingProgressBar.setVisibility(View.INVISIBLE);
        resetGraphs();
        try {
            mBufferedWriter.flush();
            mBufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configureGraph(GraphView graph, LineGraphSeries<DataPoint> series, String title) {
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(maxDataPoints);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-defaultYBound);
        graph.getViewport().setMaxY(defaultYBound);
        graph.setTitle(title);
    }

    private boolean isExternalStorageWritable() {
        // checks if external storage is available for read and write
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File getPrivateStorageDir(Context context, String folderName) {
        // get the directory for the app's private directory and create a new subdirectory
        File file = new File(context.getExternalFilesDir(null), folderName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    private File getFile() {
        File dir = getPrivateStorageDir(this, "test");
        return new File(dir, "test.txt");
    }

    private void resetGraphs() {
        xAxisLastXValue = -1d;
        yAxisLastXValue = -1d;
        zAxisLastXValue = -1d;
        xAxisSeries = new LineGraphSeries<>();
        yAxisSeries = new LineGraphSeries<>();
        zAxisSeries = new LineGraphSeries<>();
        xAxisGraphView.removeAllSeries();
        yAxisGraphView.removeAllSeries();
        zAxisGraphView.removeAllSeries();
        configureGraph(xAxisGraphView, xAxisSeries, "x_axis");
        configureGraph(yAxisGraphView, yAxisSeries, "y_axis");
        configureGraph(zAxisGraphView, zAxisSeries, "z_axis");
    }
}
