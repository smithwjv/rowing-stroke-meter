package smithwjv.rowingstrokemeter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private Sensor linearAccelerationSensor;
    private SensorManager mSensorManager;
    private TextView linearAccelerationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearAccelerationTextView = (TextView) findViewById(R.id.tv_linear_acceleration_values);
        mSensorManager = (SensorManager) getSystemService(MainActivity.SENSOR_SERVICE);
        // check if linear acceleration sensor exists
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            linearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor
                    .TYPE_LINEAR_ACCELERATION);
        } else {
            linearAccelerationTextView.setText(R.string.error_sensor);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // update text view
        linearAccelerationTextView.setText("");
        for (float value : event.values) {
            linearAccelerationTextView.append(String.valueOf(value) + "\n");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager
                .SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
