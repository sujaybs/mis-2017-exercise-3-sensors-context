package com.example.sujaybshalawadi.mis3;

import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

@SuppressWarnings("unused")
class Log {

    private static final String APP_MESSAGE_LOG = "app_log.txt";

    private static final int LOG_LEVEL_NOPE = 0xFF;
    private static final int LOG_LEVEL_ERROR = 0x04;
    private static final int LOG_LEVEL_WARNING = 0x03;
    private static final int LOG_LEVEL_DEBUG = 0x02;
    private static final int LOG_LEVEL_INFO = 0x01;
    private static final int LOG_LEVEL_VERBOSE = 0x00;

    private static final int LOG_LEVEL = BuildConfig.DEBUG ? LOG_LEVEL_WARNING : LOG_LEVEL_NOPE;

    private static void writeToLogFile(String msg, String logFile) {
        msg = "\n\n" + Calendar.getInstance().getTime().toLocaleString() + "\n\n" + msg;
        File dir = new File(Environment.getExternalStorageDirectory() + "/MIS3/logs/");
        dir.mkdirs();
        File log = new File(Environment.getExternalStorageDirectory() + "/MIS3/logs/", logFile);
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(log, true));
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (IOException ignored) {
            android.util.Log.e("WRITE OPERATION FAILED", "WRITE OPERATION FAILED");
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static int v(String tag, String msg) {
        if (LOG_LEVEL <= LOG_LEVEL_VERBOSE) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.v(tag, msg);
        } else {
            return 0;
        }
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (LOG_LEVEL <= LOG_LEVEL_VERBOSE) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.v(tag, msg, tr);
        } else {
            return 0;
        }
    }

    public static int d(String tag, String msg) {
        if (LOG_LEVEL <= LOG_LEVEL_DEBUG) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.d(tag, msg);
        } else {
            return 0;
        }
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (LOG_LEVEL <= LOG_LEVEL_DEBUG) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.d(tag, msg, tr);
        } else {
            return 0;
        }
    }

    public static int i(String tag, String msg) {
        if (LOG_LEVEL <= LOG_LEVEL_INFO) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.i(tag, msg);
        } else {
            return 0;
        }
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (LOG_LEVEL <= LOG_LEVEL_INFO) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.i(tag, msg, tr);
        } else {
            return 0;
        }
    }

    public static int w(String tag, String msg) {
        if (LOG_LEVEL <= LOG_LEVEL_WARNING) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.w(tag, msg);
        } else {
            return 0;
        }
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (LOG_LEVEL <= LOG_LEVEL_WARNING) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.w(tag, msg, tr);
        } else {
            return 0;
        }
    }

    public static int w(String tag, Throwable tr) {
        if (LOG_LEVEL <= LOG_LEVEL_WARNING) {
            writeToLogFile(tr.getMessage(), APP_MESSAGE_LOG);
            return android.util.Log.w(tag, tr);
        } else {
            return 0;
        }
    }

    public static int e(String tag, String msg) {
        if (LOG_LEVEL <= LOG_LEVEL_ERROR) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.e(tag, msg);
        } else {
            return 0;
        }
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (LOG_LEVEL <= LOG_LEVEL_ERROR) {
            writeToLogFile(msg, APP_MESSAGE_LOG);
            return android.util.Log.e(tag, msg, tr);
        } else {
            return 0;
        }
    }
}
