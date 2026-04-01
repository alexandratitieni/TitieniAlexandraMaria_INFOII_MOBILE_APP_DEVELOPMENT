package com.example.safealert.helpers;

import android.net.Uri;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;

public class FirebaseHelper {
    private final FirebaseStorage storage;
    private final FirebaseDatabase database;

    public FirebaseHelper() {
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    public interface UploadCallback {
        void onUploadSuccess(String downloadUrl);
        void onUploadFailure(String error);
    }

    public void uploadVideoFile(File file, UploadCallback callback) {
        if (file == null || !file.exists()) {
            callback.onUploadFailure("Video file not found");
            return;
        }

        StorageReference storageRef = storage.getReference().child("sos_evidence/" + file.getName());

        storageRef.putFile(Uri.fromFile(file))
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    saveToDatabase(file.getName(), downloadUrl);
                    callback.onUploadSuccess(downloadUrl);
                }))
                .addOnFailureListener(e -> callback.onUploadFailure(e.getMessage()));
    }

    private void saveToDatabase(String fileName, String url) {
        DatabaseReference dbRef = database.getReference("sos_alerts").push();
        dbRef.child("fileName").setValue(fileName);
        dbRef.child("downloadUrl").setValue(url);
        dbRef.child("timestamp").setValue(System.currentTimeMillis());
    }
}