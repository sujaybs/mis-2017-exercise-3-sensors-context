package com.example.sujaybshalawadi.mis3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.SeekBar;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ActivityHome extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LocationManager locationMamnager;
    private MusicRetriever musicRetriever;
    private MediaPlayer mediaPlayerJogging;
    private MediaPlayer mediaPlayerCycling;

    private GraphView graphView;
    private SpectrumView spectrumView;
    private long lastUpdate = 0;
    private SeekBar seekBarSampling;
    private SeekBar seekBarWindow;

    private ReadingsDeques readingsDeques;

    private HammingWindow hammingWindow;
    private FFT fft;
    private float speed = 0.0f;

    private String[] LOCATION_PERMISSIONS = new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
    private int REQUEST_CHECK_SETTINGS = 0xAB;

    private static int windowIndex = 5;
    private static int samplingIndex = 7;

    public void pushValues(float x, float y, float z, float m) {
        readingsDeques.x.push(x);
        readingsDeques.y.push(y);
        readingsDeques.z.push(z);
        readingsDeques.m.push(m);

        if (readingsDeques.x.size() > getPointWindow()) {
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

        double[] freqReal = new double[getPointWindow()];
        double[] freqImag = new double[getPointWindow()];
        float[] freqAbs = new float[getPointWindow()];

        hammingWindow = new HammingWindow(getPointWindow());
        fft = new FFT(getPointWindow());

        for (int i = 0; i < pointBufferM.length; i++) {
            freqReal[i] = hammingWindow.getValue(i) * pointBufferM[i];
        }

        fft.fft(freqReal, freqImag);

        for (int i = 0; i < freqReal.length; i++) {
            freqAbs[i] = (float) Math.sqrt(Math.pow(freqReal[i], 2) + Math.pow(freqImag[i], 2));
        }

        // Suppress side peaks
        freqAbs[0] = 0f;
        freqAbs[1] = 0f;
        freqAbs[freqAbs.length - 2] = 0f;
        freqAbs[freqAbs.length - 1] = 0f;

        ArrayList<Float> freqsAbs = new ArrayList<>(Arrays.asList(ArrayUtils.toObject(freqAbs)));
        spectrumView.adjustMaximum(Collections.max(freqsAbs));
        spectrumView.setBuffer(freqAbs);

        ActivityDetector.DetectedActivity detectedActivity = ActivityDetector.detect(speed, freqAbs);

        if (detectedActivity.equals(ActivityDetector.DetectedActivity.Jogging)) {
            Log.e(getClass().getName(),"JOGGING");
            mediaPlayerJogging.start();
        } else if (detectedActivity.equals(ActivityDetector.DetectedActivity.Cycling)) {
            Log.e(getClass().getName(),"CYCLING");
            mediaPlayerCycling.start();
        } else if (detectedActivity.equals(ActivityDetector.DetectedActivity.Resting)) {
            Log.e(getClass().getName(),"RESTING");
            mediaPlayerJogging.stop();
            mediaPlayerCycling.stop();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readingsDeques = new ReadingsDeques();

        setContentView(R.layout.activity_home);
        graphView = (GraphView) findViewById(R.id.graph_view);
        spectrumView = (SpectrumView) findViewById(R.id.spectrum_view);
        seekBarSampling = (SeekBar) findViewById(R.id.seekBar);
        seekBarWindow = (SeekBar) findViewById(R.id.seekBar2);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationMamnager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        musicRetriever = new MusicRetriever(this.getContentResolver());
        mediaPlayerJogging = MediaPlayer.create(this, R.raw.jogging);
        mediaPlayerCycling = MediaPlayer.create(this, R.raw.cycling);

        tryRequestLocationUpdates();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        seekBarWindow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                windowIndex = seekBar.getProgress();
            }
        });

        seekBarSampling.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                samplingIndex = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    
    long getSamplingTimeout(){
        return 1300000 * (6-samplingIndex);
    }
    
    static int getPointWindow(){
        return (int) Math.pow(2,7+windowIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                tryRequestLocationUpdates();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                askForPermissions();
            }
        }
    }

    private void askForPermissions() {
        Snackbar.make(findViewById(R.id.graph_view), "Location permission required", Snackbar.LENGTH_INDEFINITE)
                .setAction(
                        "Grant",
                        (view) -> ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, REQUEST_CHECK_SETTINGS))
                .show();
    }

    private void tryRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, REQUEST_CHECK_SETTINGS);
            return;
        }

        locationMamnager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000L, 5.0f, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        speed = location.getSpeed();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        // ignore
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // ignore
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // ignore
                    }
                });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.nanoTime();

            if (curTime - lastUpdate > getSamplingTimeout()) {
                lastUpdate = curTime;

                float m = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                pushValues(x, y, z, m);
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
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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