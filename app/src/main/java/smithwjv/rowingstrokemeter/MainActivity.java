package smithwjv.rowingstrokemeter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private boolean listening;
    private double xAxisLastXValue = -1d;
    private double yAxisLastXValue = -1d;
    private double zAxisLastXValue = -1d;
    private int maxDataPoints = 128;
    private Button startStopButton;
    private GraphView xAxisGraphView;
    private GraphView yAxisGraphView;
    private GraphView zAxisGraphView;
    private LineGraphSeries<DataPoint> xAxisSeries;
    private LineGraphSeries<DataPoint> yAxisSeries;
    private LineGraphSeries<DataPoint> zAxisSeries;
    private Sensor linearAccelerationSensor;
    private SensorManager mSensorManager;
    private TextView errorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        errorMessageTextView = (TextView) findViewById(R.id.tv_error_message);

        startStopButton = (Button) findViewById(R.id.btn_start_stop);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listening) {
                    onPause();
                } else {
                    onResume();
                }
            }
        });

        xAxisGraphView = (GraphView) findViewById(R.id.gv_linear_acceleration_x_axis);
        yAxisGraphView = (GraphView) findViewById(R.id.gv_linear_acceleration_y_axis);
        zAxisGraphView = (GraphView) findViewById(R.id.gv_linear_acceleration_z_axis);
        xAxisSeries = new LineGraphSeries<>();
        yAxisSeries = new LineGraphSeries<>();
        zAxisSeries = new LineGraphSeries<>();
        configureGraph(xAxisGraphView, xAxisSeries, "x_axis");
        configureGraph(yAxisGraphView, yAxisSeries, "y_axis");
        configureGraph(zAxisGraphView, zAxisSeries, "z_axis");

        mSensorManager = (SensorManager) getSystemService(MainActivity.SENSOR_SERVICE);
        // check if linear acceleration sensor exists
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            linearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor
                    .TYPE_LINEAR_ACCELERATION);
        } else {
            errorMessageTextView.setText(R.string.error_sensor);
            errorMessageTextView.setVisibility(View.VISIBLE);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager
                .SENSOR_DELAY_FASTEST);
        listening = true;
        startStopButton.setText(R.string.button_stop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        listening = false;
        startStopButton.setText(R.string.button_start);
    }

    private void configureGraph(GraphView graph, LineGraphSeries<DataPoint> series, String title) {
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(maxDataPoints);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-25);
        graph.getViewport().setMaxY(25);
        graph.setTitle(title);
    }
}
