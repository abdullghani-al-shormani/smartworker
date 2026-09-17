package com.abdullghani.smartworkerlibrary;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;

import com.abdullghani.smartworker.SmartWorker;
import com.abdullghani.smartworker.callback.WorkStateListener;
import com.abdullghani.smartworkerlibrary.models.UserProfile;
import com.abdullghani.smartworkerlibrary.workers.CacheCleanupWorker;
import com.abdullghani.smartworkerlibrary.workers.DatabaseBackupWorker;
import com.abdullghani.smartworkerlibrary.workers.DownloadAudioWorker;
import com.abdullghani.smartworkerlibrary.workers.DownloadImageWorker;
import com.abdullghani.smartworkerlibrary.workers.FileDownloadWorker;
import com.abdullghani.smartworkerlibrary.workers.ProcessDataWorker;
import com.abdullghani.smartworkerlibrary.workers.UpdateProfileWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Example 1: One-Time Task (Cache Cleanup)
        findViewById(R.id.btClearCache).setOnClickListener(view -> {
            String uniqueWorkId = "app_cache_cleanup_task";

            SmartWorker.with(this)
                    .oneTime(CacheCleanupWorker.class)
                    .requiresCharging()             // Ensures device is charging to save battery
                    .requiresStorageNotLow()        // Ensures device has enough storage space
                    .setDelay(10, TimeUnit.MINUTES) // Initial execution delay
                    .enqueueUnique(uniqueWorkId);

            observeCacheCleanup(uniqueWorkId);
        });

        // Example 2: Periodic Task (File / DB Backup Copy)
        findViewById(R.id.btPeriodicBackup).setOnClickListener(view -> {
            String uniqueWorkId = "periodic_db_backup_task";

            SmartWorker.with(this)
                    .periodic(DatabaseBackupWorker.class, 2, TimeUnit.HOURS)
                    .requiresStorageNotLow()   // Requires sufficient storage space
                    .requiresBatteryNotLow()   // Requires device battery not to be low
                    .setExistingPolicy(ExistingPeriodicWorkPolicy.KEEP) // Keeps existing scheduled task if present
                    .enqueueUnique(uniqueWorkId);

            observePeriodicBackup(uniqueWorkId);
        });

        // Example 3: Custom Object & Data Passing
        findViewById(R.id.btUpdateProfile).setOnClickListener(view -> {
            String uniqueWorkId = "update_user_profile_task";
            UserProfile user = new UserProfile("Abdullghani", "Admin");

            SmartWorker.with(this)
                    .oneTime(UpdateProfileWorker.class)
                    .putString("user_id", "123")
                    .putInt("retry_count", 3)
                    .putObject("user_profile", user) // Passing custom serializable object
                    .enqueueUnique(uniqueWorkId);

            observeUpdateProfile(uniqueWorkId);
        });

        // Example 4: Work Chaining (Parallel -> Sequential)
        findViewById(R.id.btStartChain).setOnClickListener(view -> {
            Toast.makeText(this, "Task chain started in background...", Toast.LENGTH_SHORT).show();

            SmartWorker.chain(this)
                    .beginParallel(DownloadImageWorker.class, DownloadAudioWorker.class) // Run downloads in parallel
                    .then(ProcessDataWorker.class)                                      // Execute processing sequentially after completion
                    .start();
        });

        // Example 5: Long-Running Task (Foreground Service)
        findViewById(R.id.btDownloadFile).setOnClickListener(view -> {
            String uniqueWorkId = "long_file_download_task";

            SmartWorker.with(this)
                    .oneTime(FileDownloadWorker.class)
                    .requiresInternet()
                    .enqueueUnique(uniqueWorkId);

            observeFileDownload(uniqueWorkId);
        });
    }

    private void observeCacheCleanup(String uniqueWorkId) {
        SmartWorker.observe(this, this, uniqueWorkId, new WorkStateListener() {
            @Override
            public void onSuccess(@NonNull Data outputData) {
                long freedBytes = outputData.getLong("freed_bytes", 0);
                long freedMB = freedBytes / (1024 * 1024);
                Toast.makeText(MainActivity.this,
                        "Cache cleared successfully! Freed: " + freedMB + " MB",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Data outputData) {
                Toast.makeText(MainActivity.this, "Failed to clear cache", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(@NonNull Data progressData) {
            }
        });
    }

    private void observePeriodicBackup(String uniqueWorkId) {
        SmartWorker.observe(this, this, uniqueWorkId, new WorkStateListener() {
            @Override
            public void onSuccess(@NonNull Data outputData) {
                String backupPath = outputData.getString("backup_path");
                Toast.makeText(MainActivity.this,
                        "Periodic backup completed! Saved to: " + backupPath,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Data outputData) {
                Toast.makeText(MainActivity.this, "Periodic backup failed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(@NonNull Data progressData) {
            }
        });
    }

    private void observeUpdateProfile(String uniqueWorkId) {
        SmartWorker.observe(this, this, uniqueWorkId, new WorkStateListener() {
            @Override
            public void onSuccess(@NonNull Data outputData) {
                String status = outputData.getString("status");
                Toast.makeText(MainActivity.this, status, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Data outputData) {
                Toast.makeText(MainActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(@NonNull Data progressData) {
            }
        });
    }

    private void observeFileDownload(String uniqueWorkId) {
        SmartWorker.observe(this, this, uniqueWorkId, new WorkStateListener() {
            @Override
            public void onSuccess(@NonNull Data outputData) {
                String url = outputData.getString("download_url");
                Toast.makeText(MainActivity.this, "Download finished: " + url, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Data outputData) {
                Toast.makeText(MainActivity.this, "File download failed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(@NonNull Data progressData) {
                int progress = progressData.getInt("progress", 0);
                Toast.makeText(MainActivity.this, "Download Progress: " + progress + "%", Toast.LENGTH_SHORT).show();
            }
        });
    }
}