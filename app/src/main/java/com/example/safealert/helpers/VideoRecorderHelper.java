package com.example.safealert.helpers;

import android.content.Context;
import android.util.Log;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;

public class VideoRecorderHelper {
    private Recording recording = null;
    private VideoCapture<Recorder> videoCapture = null;

    @SuppressWarnings("MissingPermission")
    public void startRecording(Context context, LifecycleOwner lifecycleOwner, File outputFile, PreviewView previewView) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setExecutor(ContextCompat.getMainExecutor(context))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture);

                FileOutputOptions options = new FileOutputOptions.Builder(outputFile).build();

                recording = videoCapture.getOutput()
                        .prepareRecording(context, options)
                        .withAudioEnabled()
                        .start(ContextCompat.getMainExecutor(context), recordEvent -> {
                            if (recordEvent instanceof VideoRecordEvent.Start) {
                                Log.d("CameraTest", "Recording started successfully");
                            } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                                Log.d("CameraTest", "Recording finalized to: " + outputFile.getAbsolutePath());
                            }
                        });

            } catch (Exception e) {
                Log.e("CameraError", "Failed to start camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
            Log.d("CameraTest", "Recording stopped");
        }
    }
}