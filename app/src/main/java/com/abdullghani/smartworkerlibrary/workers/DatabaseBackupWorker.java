package com.abdullghani.smartworkerlibrary.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseBackupWorker extends Worker {

    public DatabaseBackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // Simulated file/database paths for periodic backup
        File sourceFile = new File(context.getFilesDir(), "app_database.db");
        File backupDir = new File(context.getExternalFilesDir(null), "Backups");

        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File destFile = new File(backupDir, "backup_database.db");

        try {
            // Create dummy file if source doesn't exist for demo purposes
            if (!sourceFile.exists()) {
                sourceFile.createNewFile();
            }

            copyFile(sourceFile, destFile);

            Data outputData = new Data.Builder()
                    .putString("backup_path", destFile.getAbsolutePath())
                    .putLong("timestamp", System.currentTimeMillis())
                    .build();

            return Result.success(outputData);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void copyFile(File src, File dst) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}