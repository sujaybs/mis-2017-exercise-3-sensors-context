package com.example.sujaybshalawadi.mis3;

class ActivityDetector {

    static DetectedActivity detect(float velocity, float per) {
        if (per < 0.04f)
            return DetectedActivity.Resting;

        if (velocity > 2.77f && velocity < 8.3f) {
            return DetectedActivity.Cycling;
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
