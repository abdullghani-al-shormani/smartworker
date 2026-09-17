package com.abdullghani.smartworker.callback;

import androidx.annotation.NonNull;
import androidx.work.Data;

public interface WorkStateListener {
    void onSuccess(@NonNull Data outputData);
    void onFailure(@NonNull Data outputData);
    void onProgress(@NonNull Data progressData);
}