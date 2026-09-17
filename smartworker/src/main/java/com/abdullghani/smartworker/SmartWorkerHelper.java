package com.abdullghani.smartworker;

import android.app.Notification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;

import com.google.gson.Gson;

public class SmartWorkerHelper {
    private static Gson gson;

    private static synchronized Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public static String serializeObject(@NonNull Object object) {
        return getGson().toJson(object);
    }

    @Nullable
    public static <T> T getObject(@NonNull Data data, @NonNull String key, @NonNull Class<T> clazz) {
        String json = data.getString(key);
        if (json == null || json.isEmpty()) return null;
        try {
            return getGson().fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static void promoteToForeground(@NonNull ListenableWorker worker, int notificationId, @NonNull Notification notification) {
        ForegroundInfo foregroundInfo = new ForegroundInfo(notificationId, notification);
        worker.setForegroundAsync(foregroundInfo);
    }
}