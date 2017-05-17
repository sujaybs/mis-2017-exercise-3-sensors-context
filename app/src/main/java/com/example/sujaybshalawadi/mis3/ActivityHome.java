package com.example.sujaybshalawadi.mis3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

public class ActivityHome extends Activity {

    private static int windowIndex = 0;
    private static int samplingIndex = 0;

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
    private String[] PERMISSIONS = new String[]{
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
    };
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

    private boolean isPlottingStopped = false;
    private double[] freqReal;
    private double[] freqImag;
    private Float[] freqAbs;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Sensor mySensor = sensorEvent.sensor;

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                long curTime = System.nanoTime();

                if (curTime - lastUpdate > getSamplingTimeout() && !isPlottingStopped) {
                    lastUpdate = curTime;

                    float m = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                    try {
                        pushValues(x, y, z, m);
                    } catch (IOException e) {
                        Log.e(getClass().getName(), "", e);
                    }
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // ignore
        }
    };

    static int getPointWindow() {
        return (int) Math.pow(2, 7 + windowIndex);
    }

    public void pushValues(float x, float y, float z, float m) throws IOException {
        int pointWindow = getPointWindow();

        while (readingsDeques.x.length - 1 > pointWindow) {
            readingsDeques.x = ArrayUtils.remove(readingsDeques.x, readingsDeques.x.length - 1);
            readingsDeques.y = ArrayUtils.remove(readingsDeques.y, readingsDeques.y.length - 1);
            readingsDeques.z = ArrayUtils.remove(readingsDeques.z, readingsDeques.z.length - 1);
            readingsDeques.m = ArrayUtils.remove(readingsDeques.m, readingsDeques.m.length - 1);
        }

        readingsDeques.x = ArrayUtils.add(readingsDeques.x, 0, x);
        readingsDeques.y = ArrayUtils.add(readingsDeques.y, 0, y);
        readingsDeques.z = ArrayUtils.add(readingsDeques.z, 0, z);
        readingsDeques.m = ArrayUtils.add(readingsDeques.m, 0, m);

        graphView.adjustMaxima(max(readingsDeques.x),
                max(readingsDeques.y),
                max(readingsDeques.z),
                max(readingsDeques.m));

        graphView.adjustMinima(min(readingsDeques.x),
                min(readingsDeques.y),
                min(readingsDeques.z),
                min(readingsDeques.m));

        graphView.setBuffers(readingsDeques.x,
                readingsDeques.y,
                readingsDeques.z,
                readingsDeques.m);

        for (int i = 0; i < freqReal.length; i++) {
            freqReal[i] = hammingWindow.getValue(i) * readingsDeques.m[i];
            freqImag[i] = 0.0f;
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

        float oscillationEnergy = 0;

        for (float freqAb : freqAbs) {
            oscillationEnergy += freqAb;
        }

        float freqsMax = max(freqAbs);
        spectrumView.adjustMaximum(freqsMax);
        spectrumView.setBuffer(freqAbs);

        ActivityDetector.DetectedActivity detectedActivity = ActivityDetector.detect(speed, freqsMax / oscillationEnergy);

//        Log.d(getClass().getName(), "Oscillation energy: " + String.valueOf(oscillationEnergy));
//        Log.d(getClass().getName(), "Oscillation peak: " + String.valueOf(freqsMax));

        if (detectedActivity.equals(ActivityDetector.DetectedActivity.Jogging)) {
            Log.w(getClass().getName(), "JOGGING, PER: " + String.valueOf(freqsMax / oscillationEnergy));
            if (!mediaPlayerJogging.isPlaying()) {
                mediaPlayerJogging.prepare();
                mediaPlayerJogging.start();
            }
        } else if (detectedActivity.equals(ActivityDetector.DetectedActivity.Cycling)) {
            Log.w(getClass().getName(), "CYCLING, PER: " + String.valueOf(freqsMax / oscillationEnergy));
            if (!mediaPlayerCycling.isPlaying()) {
                mediaPlayerCycling.prepare();
                mediaPlayerCycling.start();
            }
        } else if (detectedActivity.equals(ActivityDetector.DetectedActivity.Resting)) {
            Log.w(getClass().getName(), "RESTING, PER: " + String.valueOf(freqsMax / oscillationEnergy));
            if (mediaPlayerJogging.isPlaying()) {
                mediaPlayerJogging.stop();
            }
            if (mediaPlayerCycling.isPlaying()) {
                mediaPlayerCycling.stop();
            }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a tune for cycling: ");

        ArrayList<MusicRetriever.Item> items = musicRetriever.getItems();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < items.size(); i++) {
            MusicRetriever.Item item = items.get(i);
            arrayAdapter.add(item.title);
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    try {
                        mediaPlayerCycling.reset();
                        mediaPlayerCycling.setDataSource(this, items.get(which).getURI());
                        mediaPlayerCycling.prepare();
                    } catch (IOException e) {
                        Log.e(getClass().getName(), "", e);
                    }
                }).show();
    }

    private void displayJoggingOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a tune for jogging: ");

        ArrayList<MusicRetriever.Item> items = musicRetriever.getItems();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < items.size(); i++) {
            MusicRetriever.Item item = items.get(i);
            arrayAdapter.add(item.title);
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    try {
                        mediaPlayerJogging.reset();
                        mediaPlayerJogging.setDataSource(this, items.get(which).getURI());
                        mediaPlayerJogging.prepare();
                    } catch (IOException e) {
                        Log.e(getClass().getName(), "", e);
                    }
                }).show();
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
        mediaPlayerJogging.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayerCycling = MediaPlayer.create(this, R.raw.cycling);
        mediaPlayerCycling.setAudioStreamType(AudioManager.STREAM_MUSIC);

        initArrays();

        tryLoadTracks();
        tryRequestLocationUpdates();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        seekBarWindow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                isPlottingStopped = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlottingStopped = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                windowIndex = seekBar.getProgress();
                initArrays();
                isPlottingStopped = false;
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

    private void initArrays() {
        int pointWindow = getPointWindow();

        freqReal = new double[pointWindow];
        freqImag = new double[pointWindow];
        freqAbs = new Float[pointWindow];

        Arrays.fill(freqAbs, 0.0f);

        readingsDeques.initDeques();

        hammingWindow = new HammingWindow(pointWindow);
        fft = new FFT(pointWindow);
    }

    long getSamplingTimeout() {
        return 1300000 * (6 - samplingIndex);
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
        Snackbar.make(findViewById(R.id.graph_view), "Permissions required", Snackbar.LENGTH_INDEFINITE)
                .setAction("Grant",
                        (view) -> ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS))
                .show();
    }

    private void tryRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS);
            return;
        }

        locationMamnager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100L, 5.0f, locationListener);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationMamnager.removeUpdates(locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayerCycling != null) mediaPlayerCycling.release();
        if (mediaPlayerJogging != null) mediaPlayerJogging.release();
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        tryRequestLocationUpdates();
    }

    private static final class ReadingsDeques {
        Float[] x;
        Float[] y;
        Float[] z;
        Float[] m;

        ReadingsDeques() {
            initDeques();
        }

        private void initDeques() {
            int pointWindow = getPointWindow();

            x = new Float[pointWindow];
            y = new Float[pointWindow];
            z = new Float[pointWindow];
            m = new Float[pointWindow];

            Arrays.fill(x, 0.0f);
            Arrays.fill(y, 0.0f);
            Arrays.fill(z, 0.0f);
            Arrays.fill(m, 0.0f);
        }
    }

}