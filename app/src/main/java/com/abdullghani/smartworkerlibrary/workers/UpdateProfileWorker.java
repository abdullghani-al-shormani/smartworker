package com.abdullghani.smartworkerlibrary.workers;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.abdullghani.smartworker.SmartWorkerHelper;
import com.abdullghani.smartworkerlibrary.models.UserProfile;

public class UpdateProfileWorker extends Worker {

    public UpdateProfileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Extract primitive data
        String userId = getInputData().getString("user_id");
        int retryCount = getInputData().getInt("retry_count", 0);

        // Deserialize custom object safely using SmartWorkerHelper
        UserProfile user = SmartWorkerHelper.getObject(getInputData(), "user_profile", UserProfile.class);

        if (user != null) {
            // Simulated profile update logic
            Data outputData = new Data.Builder()
                    .putString("status", "Profile updated for: " + user.getName() + " (" + user.getRole() + ")")
                    .putString("user_id", userId)
                    .build();

            return Result.success(outputData);
        } else {
            return Result.failure();
        }
    }
}