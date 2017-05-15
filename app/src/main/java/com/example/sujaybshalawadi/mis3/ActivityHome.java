package com.example.sujaybshalawadi.mis3;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ActivityHome extends Activity implements SensorEventListener {
    public static final int POINT_WINDOW = 128;
    private static final int SHAKE_THRESHOLD = 600;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private GraphView graphView;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    private ReadingsDeques readingsDeques;

    public void pushValues(float x, float y, float z, float m) {
        readingsDeques.x.push(x);
        readingsDeques.y.push(y);
        readingsDeques.z.push(z);
        readingsDeques.m.push(m);

        if (readingsDeques.x.size() > POINT_WINDOW) {
            readingsDeques.x.removeLast();
            readingsDeques.y.removeLast();
            readingsDeques.z.removeLast();
            readingsDeques.m.removeLast();
        }

        Float[] pointsX = readingsDeques.x.toArray(new Float[readingsDeques.x.size()]);
        Float[] pointsY = readingsDeques.y.toArray(new Float[readingsDeques.y.size()]);
        Float[] pointsZ = readingsDeques.z.toArray(new Float[readingsDeques.z.size()]);
        Float[] pointsM = readingsDeques.m.toArray(new Float[readingsDeques.m.size()]);

        ArrayList<Float> listX = new ArrayList<>(Arrays.asList(pointsX));
        ArrayList<Float> listY = new ArrayList<>(Arrays.asList(pointsY));
        ArrayList<Float> listZ = new ArrayList<>(Arrays.asList(pointsZ));
        ArrayList<Float> listM = new ArrayList<>(Arrays.asList(pointsM));

        float[] pointBufferX = ArrayUtils.toPrimitive(pointsX, 0.0F);
        float[] pointBufferY = ArrayUtils.toPrimitive(pointsY, 0.0F);
        float[] pointBufferZ = ArrayUtils.toPrimitive(pointsZ, 0.0F);
        float[] pointBufferM = ArrayUtils.toPrimitive(pointsM, 0.0F);

        graphView.adjustMaxima(Collections.max(listX),
                Collections.max(listY),
                Collections.max(listZ),
                Collections.max(listM));

        graphView.adjustMinima(Collections.min(listX),
                Collections.min(listY),
                Collections.min(listZ),
                Collections.min(listM));

        graphView.setBuffers(pointBufferX, pointBufferY, pointBufferZ, pointBufferM);


        double[] q = new double[pointBufferM.length];
        for (int i = 0; i < pointBufferM.length; i++) {
            q[i] = pointBufferM[i];
        }

        FFT fft = new FFT(1024);
        fft.fft(q, new double[1024]);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readingsDeques = new ReadingsDeques();
        graphView = new GraphView(this);
        setContentView(graphView);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.nanoTime();

            if ((curTime - lastUpdate) > 1300000) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {

                }

                last_x = x;
                last_y = y;
                last_z = z;

                float m = (float) Math.sqrt(Math.pow(last_x, 2) + Math.pow(last_y, 2) + Math.pow(last_z, 2));

                pushValues(x, y, z, m);

//                Log.e(getClass().getName(), String.format("X: %f, Y: %f, Z: %f, Magnitude : %f", last_x, last_y, last_z, m));
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * To unregister the application during hibernations
     */
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private static final class ReadingsDeques {
        ArrayDeque<Float> x;
        ArrayDeque<Float> y;
        ArrayDeque<Float> z;
        ArrayDeque<Float> m;

        ReadingsDeques() {
            x = new ArrayDeque<>();
            y = new ArrayDeque<>();
            z = new ArrayDeque<>();
            m = new ArrayDeque<>();
        }
    }

}