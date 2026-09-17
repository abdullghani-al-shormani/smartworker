package com.abdullghani.smartworkerlibrary.workers;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;

public class CacheCleanupWorker extends Worker {

    public CacheCleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        File cacheDir = context.getCacheDir();

        if (cacheDir == null || !cacheDir.exists()) {
            return Result.success();
        }

        long freedBytes = deleteOldFiles(cacheDir);

        // إرجاع حجم المساحة المحررة بالبايت ليتم استقبالها في الواجهة
        Data outputData = new Data.Builder()
                .putLong("freed_bytes", freedBytes)
                .build();

        return Result.success(outputData);
    }

    private long deleteOldFiles(File dir) {
        long deletedSize = 0;
        File[] files = dir.listFiles();

        if (files != null) {
            long currentTime = System.currentTimeMillis();
            long dayInMillis = 24 * 60 * 60 * 1000L;

            for (File file : files) {
                if (file.isDirectory()) {
                    deletedSize += deleteOldFiles(file);
                } else {
                    // حذف الملفات التي مضى على إنشائها أكثر من يوم واحد
                    if (currentTime - file.lastModified() > dayInMillis) {
                        long fileSize = file.length();
                        if (file.delete()) {
                            deletedSize += fileSize;
                        }
                    }
                }
            }
        }
        return deletedSize;
    }
}
