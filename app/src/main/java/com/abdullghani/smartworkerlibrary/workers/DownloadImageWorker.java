package com.abdullghani.smartworkerlibrary.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DownloadImageWorker extends Worker {

    public DownloadImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Simulate network delay for downloading image
            Thread.sleep(1500);
            return Result.success();
        } catch (InterruptedException e) {
            return Result.failure();
        }
    }
}