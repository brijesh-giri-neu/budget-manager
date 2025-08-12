package edu.northeastern.numad25su_group9;

import android.app.Application;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import edu.northeastern.numad25su_group9.activities.MainActivity;

public class App extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        setupGlobalExceptionHandler();
    }

    private void setupGlobalExceptionHandler() {
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                // --- 1. Log the crash details ---
                Log.e(TAG, "Uncaught Exception on thread: " + thread.getName(), throwable);

                // Showing a Toast is generally safe on the main thread.
                new Thread(() -> {
                    android.os.Looper.prepare();
                    Toast.makeText(getApplicationContext(), "Oops! Something went wrong. Going back to home screen.", Toast.LENGTH_LONG).show();
                    android.os.Looper.loop();
                }).start();

                // Give the Toast a moment to show.
                try {
                    Thread.sleep(1000); // Wait for 1 seconds
                } catch (InterruptedException e) {
                    Log.e(TAG, "Exception handler sleep interrupted", e);
                }

                // Restart the main activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Process.killProcess(Process.myPid()); // Kill the current process to ensure a clean restart
                System.exit(1);
            }
        });
    }
}