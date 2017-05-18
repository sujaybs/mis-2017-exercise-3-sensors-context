# mis-2017-exercise-3-sensors-context

This application visualises accelerometer data of three axis in red, blue and green lines and the magnitude of three axis in white line. It also visualises the FFT transformed absolute magnitude in the second view. 

Two seekbars are used to control the sampling rate (110 Hz - 770 Hz) and the FFT window size (128 to 1024). 

The application is used to detect different activities like resting, jogging and cycling depending on the speed and frequency profile. It can also play different music when users are running or cycling and stop when resting (or moving in a car).

The user also can select music from the stored tracks on the device for running or cycling.   

## Implementation 

[Location.getSpeed()](https://developer.android.com/reference/android/location/Location.html) is used to get the velocity of user movements. Thresholds are set for jogging (1.1 to 2.2) and cycling (2.7 to 8.3).

However, velocity is not sufficient to distinguish between cycling and moving in a car. Therefore, we calculate the ratio between peak frequency component and the overall energy of oscillation (PER). Higher values of PER indicate the presence of the established cycles of movements. Threshold of 0.04 is set to distinguish between physical activity and resting. Hence, the application does not play music while moving in a car.

[Hamming window](https://en.wikipedia.org/wiki/Window_function) is used to reduce [Spectral leakage](https://en.wikipedia.org/wiki/Spectral_leakage).

## Threshold validation

Thresholds were estimated and evaluated through real world testing. For the proposed velocity thresholds the PER values were stored. Captured PER data is available in the file "app_log.txt". Based on this capture, PER threshold was established.
