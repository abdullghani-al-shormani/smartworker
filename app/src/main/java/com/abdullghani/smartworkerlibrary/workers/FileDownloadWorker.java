package com.abdullghani.smartworkerlibrary.workers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.abdullghani.smartworker.SmartWorkerHelper;

public class FileDownloadWorker extends Worker {

    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 1001;

    public FileDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        createNotificationChannel(context);

        // 1. Build the initial foreground notification
        Notification notification = createNotification(context, "Downloading file...", 0);

        // 2. Promote the worker to foreground service using SmartWorkerHelper
        SmartWorkerHelper.promoteToForeground(this, NOTIFICATION_ID, notification);

        try {
            // Simulate file download loop
            for (int progress = 10; progress <= 100; progress += 20) {
                Thread.sleep(1000);

                // Update Progress Data for observers
                Data progressData = new Data.Builder().putInt("progress", progress).build();
                setProgressAsync(progressData);

                // Update active notification progress
                Notification updatedNotification = createNotification(context, "Downloading file...", progress);
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.notify(NOTIFICATION_ID, updatedNotification);
                }
            }

            Data outputData = new Data.Builder()
                    .putString("download_url", "https://example.com/files/large_file.zip")
                    .build();

            return Result.success(outputData);

        } catch (InterruptedException e) {
            return Result.failure();
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "File Downloads",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(Context context, String title, int progress) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(progress + "% downloaded")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setProgress(100, progress, false)
                .build();
    }
}