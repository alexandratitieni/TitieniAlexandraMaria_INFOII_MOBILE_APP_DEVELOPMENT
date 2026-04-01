package com.example.safealert.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.example.safealert.activities.MainActivity;

public class InactivityService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private long lastMoveTime;
    private static final long INACTIVITY_LIMIT = 30 * 1000; 

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        lastMoveTime = System.currentTimeMillis();
        startForegroundService();
    }

    private void startForegroundService() {
        String channelId = "InactivityChannel";
        NotificationChannel channel = new NotificationChannel(channelId, "SafeAlert Service", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("SafeAlert is running")
                .setContentText("Monitoring user activity")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0], y = event.values[1], z = event.values[2];
        double acceleration = Math.sqrt(x * x + y * y + z * z);
        if (acceleration > 11.5 || acceleration < 8.0) {
            lastMoveTime = System.currentTimeMillis();
        } else {
            long timeDiff = System.currentTimeMillis() - lastMoveTime;
            if (timeDiff > INACTIVITY_LIMIT) {
                Log.d("Inactivity", "Alert from inactivity detected");

                Intent dialogIntent = new Intent(this, MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                dialogIntent.putExtra("trigger_sos", true);
                startActivity(dialogIntent);

                lastMoveTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}