package com.example.safealert.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.safealert.activities.MainActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        assert geofencingEvent != null;
        if (geofencingEvent.hasError()) return;

        int transition = geofencingEvent.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("geofence_exit", true);
            context.startActivity(i);
        }
    }
}
