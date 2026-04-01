package com.example.safealert.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.safealert.R;
import com.example.safealert.fragments.GeofenceFragment;
import com.example.safealert.fragments.ScheduleFragment;
import com.example.safealert.fragments.SosFragment;
import com.example.safealert.helpers.FirebaseHelper;
import com.example.safealert.receivers.BatteryReceiver;
import com.example.safealert.receivers.PowerButtonReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private PowerButtonReceiver powerBtnReceiver;
    private CountDownTimer timer;
    private boolean activeAlert = false;
    private static final long INACTIVITY_LIMIT = 30 * 1000;
    private boolean isAlertFromInactivity = false;
    private GeofencingClient client;
    private final List<Geofence> geofenceList = new ArrayList<>();
    private boolean isGeofenceExit = false;
    private boolean isWeatherAlert = false;
    private String weatherCondition = "";
    private boolean isBatteryAlert = false;
    private boolean batteryAlertSent = false;
    private androidx.camera.view.PreviewView previewView;
    private final com.example.safealert.helpers.VideoRecorderHelper videoHelper = new com.example.safealert.helpers.VideoRecorderHelper();
    private final FirebaseHelper firebaseHelper = new FirebaseHelper();
    private android.media.MediaPlayer alertPlayer;
    private CountDownTimer scheduledTimer;
    private boolean isScheduleActive = false;
    private String scheduledMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            checkPermissions();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_sos) selectedFragment = new SosFragment();
            else if (id == R.id.nav_geofence) selectedFragment = new GeofenceFragment();
            else if (id == R.id.nav_schedule) selectedFragment = new ScheduleFragment();

            if (selectedFragment != null)
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

            return true;
        });

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SosFragment()).commit();

        powerButtonShortcut();

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_VOICE_COMMAND.equals(action) || intent.hasCategory(Intent.CATEGORY_VOICE))
                startTimer();
        }

        Intent serviceIntent = new Intent(this, com.example.safealert.services.InactivityService.class);
        startForegroundService(serviceIntent);

        client = LocationServices.getGeofencingClient(this);
        addSafeZone("DEFAULT_ZONE", 44.4268, 26.1025, 200);
        checkWeatherAlerts();

        registerReceiver(new BatteryReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };

        for (String permission : permissions)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_CODE);
                break;
            }
    }

    private void powerButtonShortcut() {
        powerBtnReceiver = new PowerButtonReceiver();
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(powerBtnReceiver, filter);
    }

    public void sendAlert() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                String locationLink;
                if (location != null)
                    locationLink = "http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                else
                    locationLink = "Invalid location";

                String sms;
                if (isScheduleActive)
                    sms = scheduledMessage + " Location: " + locationLink;
                else if (isAlertFromInactivity) {
                    int minutes = (int) (INACTIVITY_LIMIT / 60000);
                    sms = "Inactivity alert! Inactive for " + minutes + " minutes. Location: " + locationLink;
                }
                else if (isGeofenceExit) {
                    sms = "Geofence alert! Exit detected. Location: " + locationLink;
                    isGeofenceExit = false;
                }
                else if (isWeatherAlert) {
                    sms = "Weather warning: " + weatherCondition + ". Location: " + locationLink;
                    isWeatherAlert = false;
                }
                else if (isBatteryAlert) {
                    sms = "Critical battery alert! Location: " + locationLink;
                    isBatteryAlert = false;
                }
                else sms = "Emergency! I need help. Location: " + locationLink;

                String[] contacts = {"0749061546"}; //0744783662, 0744270138, 0759127060, 0743136291
                try {
                    SmsManager smsManager = this.getSystemService(SmsManager.class);
                    for (String contact : contacts)
                        smsManager.sendTextMessage(contact, null, sms, null, null);
                    Toast.makeText(this, "Alert sent!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("SmsError", Objects.requireNonNull(e.getMessage()));
                }
                isAlertFromInactivity = false;
            });
        }
    }

    public void startTimer() {
        if (activeAlert) return;
        activeAlert = true;
        if (timer != null) timer.cancel();

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                "Alert will be sent in 10 seconds!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Cancel", v -> {
            if (timer != null) timer.cancel();
            activeAlert = false;
            Toast.makeText(this, "Alert canceled!", Toast.LENGTH_SHORT).show();
        });
        snackbar.show();

        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                snackbar.setText("Alert starts in " + millisUntilFinished / 1000 + " seconds");
            }
            @Override
            public void onFinish() {
                snackbar.dismiss();
                sendAlert();
                File videoFile = new File(getExternalFilesDir(null), "SOS_EVIDENCE_" + System.currentTimeMillis() + ".mp4");

                Fragment sosFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (sosFragment instanceof SosFragment && sosFragment.getView() != null) {
                    previewView = sosFragment.getView().findViewById(R.id.previewView);
                    if (previewView != null)
                        previewView.setVisibility(View.VISIBLE);
                }

                try {
                    if (previewView != null) {
                        videoHelper.startRecording(MainActivity.this, MainActivity.this, videoFile, previewView);
                        Log.d("CameraTest", "Video recording started");
                    }
                } catch (Exception e) {
                    Log.e("CameraError", Objects.requireNonNull(e.getMessage()));
                    activeAlert = false;
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        videoHelper.stopRecording();
                        if (previewView != null) previewView.setVisibility(View.GONE);
                        if (!isAlertFromInactivity) makeEmergencyCall();
                        firebaseHelper.uploadVideoFile(videoFile, new FirebaseHelper.UploadCallback() {
                            @Override public void onUploadSuccess(String url) {
                                Log.d("Firebase", "Upload successful: " + url);
                            }
                            @Override public void onUploadFailure(String err) {
                                Log.e("Firebase", "Upload failed: " + err);
                            }
                        });
                        shareVideo(videoFile);
                    } finally {
                        activeAlert = false;
                        isAlertFromInactivity = false;
                    }
                }, 7000);
            }
        }.start();
    }

    public void startScheduledAlert(String message, long minutes) {
        isScheduleActive = true;
        Snackbar snack = Snackbar.make(findViewById(android.R.id.content),
                "Message scheduled for " + minutes + " min", Snackbar.LENGTH_INDEFINITE);
        snack.setAction("I am safe", v -> {
            if (scheduledTimer != null) scheduledTimer.cancel();
            isScheduleActive = false;
            Toast.makeText(this, "Scheduled alert canceled!", Toast.LENGTH_SHORT).show();
        });
        snack.show();

        scheduledTimer = new CountDownTimer(minutes * 60 * 1000, 1000) {
            @Override public void onTick(long l) {}
            @Override public void onFinish() {
                if (isScheduleActive) {
                    scheduledMessage = message;
                    sendAlert();
                    isScheduleActive = false;
                }
            }
        }.start();
    }

    public void addSafeZone(String id, double lat, double lon, float radius) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(lat, lon, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        geofenceList.add(geofence);

        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .addGeofences(geofenceList)
                .build();

        Intent intent = new Intent(this, com.example.safealert.receivers.GeofenceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.addGeofences(request, pendingIntent)
                    .addOnSuccessListener(v -> Log.d("Geofence", "Safe zone added: " + id))
                    .addOnFailureListener(e -> Log.e("Geofence", "Failed to add zone: " + e.getMessage()));
        }
    }

    private void makeEmergencyCall() {
        String emergencyNumber = "0749061546";
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + emergencyNumber));
            startActivity(intent);
        } catch (SecurityException e) {
            Log.e("CallError", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void sendGeofenceAlert() {
        isGeofenceExit = true;
        if (alertPlayer != null && alertPlayer.isPlaying()) return;

        try {
            Uri notification = android.media.RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            alertPlayer = android.media.MediaPlayer.create(this, notification);

            if (alertPlayer != null) {
                alertPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    alertPlayer = null;
                });

                alertPlayer.start();
                Toast.makeText(this, "Geofence alert!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("AlertError", "Alarm errot: " + e.getMessage());
        }

        sendAlert();
    }

    private void checkWeatherAlerts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(location -> {
                if (location != null)
                    fetchWeatherData(location.getLatitude(), location.getLongitude());
            });
        }
    }

    private void fetchWeatherData(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=f8e001e04184b71b918c4db2a1a6b72a";
        com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(this);
        com.android.volley.toolbox.JsonObjectRequest jsonObjectRequest = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("WeatherAPI", "OpenWeather API response: " + response.toString());
                        String weather = response.getJSONArray("weather").getJSONObject(0).getString("main");
                        String[] dangerous = {"Storm", "Extreme", "Tornado"};
                        for (String d : dangerous) {
                            if (weather.contains(d)) {
                                isWeatherAlert = true;
                                weatherCondition = d;
                                sendAlert();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("WeatherError", Objects.requireNonNull(e.getMessage()));
                    }
                }, error -> Log.e("WeatherApiError", Objects.requireNonNull(error.getMessage()))
        );
        queue.add(jsonObjectRequest);
    }

    private void suggestBatterySaverMode() {
        Snackbar.make(findViewById(android.R.id.content), "Low battery level!", Snackbar.LENGTH_LONG)
                .setAction("SETTINGS", v -> startActivity(new Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS))).show();
    }

    private void shareVideo(File videoFile) {
        if (!videoFile.exists()) return;

        Uri videoUri = androidx.core.content.FileProvider.getUriForFile(this, "com.example.safealert.provider", videoFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/mp4");
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share evidence"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("trigger_sos", false)) {
            getIntent().removeExtra("trigger_sos");
            isAlertFromInactivity = true;
            startTimer();
        }
        if (getIntent().getBooleanExtra("geofence_exit", false)) {
            getIntent().removeExtra("geofence_exit");
            Intent intent = new Intent(this, MainActivity.class);
            setIntent(intent);

            sendGeofenceAlert();
        }
        if (getIntent().getBooleanExtra("battery_low", false)) {
            getIntent().removeExtra("battery_low");

            if (!batteryAlertSent) {
                isBatteryAlert = true;
                sendAlert();
                suggestBatterySaverMode();
                batteryAlertSent = true;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra("trigger_sos", false)) {
            isAlertFromInactivity = true;
            startTimer();
            intent.removeExtra("trigger_sos");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (powerBtnReceiver != null) unregisterReceiver(powerBtnReceiver);
    }
}