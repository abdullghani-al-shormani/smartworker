package com.abdullghani.smartworkerlibrary.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ProcessDataWorker extends Worker {

    public ProcessDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Runs only after both image and audio downloads complete successfully
        Data outputData = new Data.Builder()
                .putString("result", "Image & Audio downloaded and processed successfully!")
                .build();

        return Result.success(outputData);
    }
}