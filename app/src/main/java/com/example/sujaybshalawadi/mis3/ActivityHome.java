package com.example.sujaybshalawadi.mis3;

import android.Manifest;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class ActivityHome extends AppCompatActivity implements SensorEventListener {
    public static final int POINT_WINDOW = 1024;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LocationManager locationMamnager;
    private MusicRetriever musicRetriever;
    private MediaPlayer mediaPlayerJogging;
    private MediaPlayer mediaPlayerCycling;

    private GraphView graphView;
    private SpectrumView spectrumView;
    private long lastUpdate = 0;

    private ReadingsDeques readingsDeques;

    private HammingWindow hammingWindow;
    private FFT fft;
    private float speed = 0.0f;

    private String[] PERMISSIONS = new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_EXTERNAL_STORAGE};

    private int REQUEST_CHECK_SETTINGS = 0xAB;

    private LocationListener locationListener = new LocationListener() {
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
    };

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

        double[] freqReal = new double[POINT_WINDOW];
        double[] freqImag = new double[POINT_WINDOW];
        float[] freqAbs = new float[POINT_WINDOW];

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
            Log.e(getClass().getName(), "JOGGING");
            mediaPlayerJogging.start();
        } else if (detectedActivity.equals(ActivityDetector.DetectedActivity.Cycling)) {
            Log.e(getClass().getName(), "CYCLING");
            mediaPlayerCycling.start();
        } else if (detectedActivity.equals(ActivityDetector.DetectedActivity.Resting)) {
            Log.e(getClass().getName(), "RESTING");
            mediaPlayerJogging.stop();
            mediaPlayerCycling.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Select jogging track");
        menu.add("Select cycling track");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Select jogging track")) {
            displayJoggingOptions();
        } else if (item.getTitle().equals("Select cycling track")) {
            displayCyclingOptions();
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayCyclingOptions() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select a tune for cycling: ");

        ArrayList<MusicRetriever.Item> items = musicRetriever.getItems();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < items.size(); i++) {
            MusicRetriever.Item item = items.get(i);
            arrayAdapter.add(item.title);
        }

        builderSingle.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            mediaPlayerCycling = MediaPlayer.create(this, items.get(which).getURI());
        });

        builderSingle.show();
    }

    private void displayJoggingOptions() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select a tune for jogging: ");

        ArrayList<MusicRetriever.Item> items = musicRetriever.getItems();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < items.size(); i++) {
            MusicRetriever.Item item = items.get(i);
            arrayAdapter.add(item.title);
        }

        builderSingle.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            mediaPlayerJogging = MediaPlayer.create(this, items.get(which).getURI());
        });

        builderSingle.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readingsDeques = new ReadingsDeques();

        setContentView(R.layout.activity_maps);
        graphView = (GraphView) findViewById(R.id.graph_view);
        spectrumView = (SpectrumView) findViewById(R.id.spectrum_view);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationMamnager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        musicRetriever = new MusicRetriever(this.getContentResolver());
        mediaPlayerJogging = MediaPlayer.create(this, R.raw.jogging);
        mediaPlayerCycling = MediaPlayer.create(this, R.raw.cycling);

        tryLoadTracks();
        tryRequestLocationUpdates();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        hammingWindow = new HammingWindow(POINT_WINDOW);
        fft = new FFT(POINT_WINDOW);
    }

    private void tryLoadTracks() {
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS);
            return;
        }

        musicRetriever.prepare();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                tryLoadTracks();
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
                        (view) -> ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS))
                .show();
    }

    private void tryRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS);
            return;
        }

        locationMamnager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 5.0f, locationListener);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.nanoTime();

            if (curTime - lastUpdate > 1300000) {
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationMamnager.removeUpdates(locationListener);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        tryRequestLocationUpdates();
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