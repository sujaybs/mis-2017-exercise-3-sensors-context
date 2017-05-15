package com.example.sujaybshalawadi.mis3;

import android.util.Log;

/**
 * Created by Tony on 5/15/2017.
 */

public class ActivityDetector {

    static DetectedActivity detect(float velocity, float spectrum[]) {
        Log.e(ActivityDetector.class.getName(), "Velocity: " + String.valueOf(velocity));
        if (velocity > 2.7f && velocity < 8.3f) {
            float lowerBand = 0f;
            float higherBand = 0f;
            int detectionRange = (int) (spectrum.length * 0.2);
            for (int i = 0; i < detectionRange; i++) {
                if (i > detectionRange / 2) {
                    higherBand += spectrum[i];
                } else {
                    lowerBand += spectrum[i];
                }
            }

            Log.e(ActivityDetector.class.getName(), "Lower band: " + String.valueOf(lowerBand));
            Log.e(ActivityDetector.class.getName(), "Higher band: " + String.valueOf(higherBand));

            if (lowerBand > higherBand) {
                return DetectedActivity.Cycling;
            }
        }

        if (velocity < 2.2f && velocity > 1.1f) {
            return DetectedActivity.Jogging;
        }

        return DetectedActivity.Resting;
    }

    enum DetectedActivity {
        Jogging, Cycling, Resting
    }
}
