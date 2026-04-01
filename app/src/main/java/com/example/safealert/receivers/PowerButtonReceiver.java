package com.example.safealert.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.safealert.activities.MainActivity;

public class PowerButtonReceiver extends BroadcastReceiver {
    private static int count = 0;
    private static long lastPressTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) || Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPressTime < 1500) count++;
            else count = 1;
            lastPressTime = currentTime;

            if (count >= 3) {
                count = 0;
                if (context instanceof MainActivity)
                    ((MainActivity) context).startTimer();
            }
        }
    }
}