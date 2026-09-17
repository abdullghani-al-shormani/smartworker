package com.abdullghani.smartworkerlibrary.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DownloadAudioWorker extends Worker {

    public DownloadAudioWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Simulate network delay for downloading audio
            Thread.sleep(2000);
            return Result.success();
        } catch (InterruptedException e) {
            return Result.failure();
        }
    }
}