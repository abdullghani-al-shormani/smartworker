package com.abdullghani.smartworker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.abdullghani.smartworker.callback.WorkStateListener;

import java.util.UUID;

/**
 * Main entry point for the SmartWorker library.
 * <p>
 * Provides a simplified and unified interface for initializing work builders,
 * constructing worker chains, and observing background execution states.
 * </p>

 * <b>Usage Examples:</b>
 * <pre>{@code
 * // Standard task builder
 * SmartWorker.with(context)
 *     .oneTime(MyWorker.class)
 *     .enqueue();
 *
 * // Task chain builder
 * SmartWorker.chain(context)
 *     .then(FirstWorker.class)
 *     .then(SecondWorker.class)
 *     .start();
 *
 * // Observe unique work execution
 * SmartWorker.observe(lifecycleOwner, context, "unique_work_name", listener);
 * }</pre>
 *
 * @author Abdullghani Al-Shormani
 * @version 1.0
 */
public class SmartWorker {

    /**
     * Initializes a new {@link SmartWorkerBuilder} instance for configuring individual background work requests.
     *
     * @param context The application or activity context.
     * @return A new {@link SmartWorkerBuilder} instance for fluent API chaining.
     */
    public static SmartWorkerBuilder with(@NonNull Context context) {
        return new SmartWorkerBuilder(context);
    }

    /**
     * Initializes a new {@link SmartChainBuilder} instance for configuring sequential or parallel work chains.
     *
     * @param context The application or activity context.
     * @return A new {@link SmartChainBuilder} instance for fluent API chaining.
     */
    public static SmartChainBuilder chain(@NonNull Context context) {
        return new SmartChainBuilder(context);
    }

    /**
     * Observes the execution state and output data of a unique work request using a {@link LifecycleOwner}.
     * <p>
     * Triggers the appropriate callbacks in {@link WorkStateListener} when the task succeeds, fails,
     * or emits progress updates.
     * </p>
     *
     * <b>Example:</b>
     * <pre>{@code
     * SmartWorker.observe(this, context, "sync_data_work", new WorkStateListener() {
     *     @Override
     *     public void onSuccess(@NonNull Data outputData) {
     *         // Task completed successfully
     *     }

     *     @Override
     *     public void onFailure(@NonNull Data outputData) {
     *         // Task failed
     *     }

     *     @Override
     *     public void onProgress(@NonNull Data progressData) {
     *         // Progress update
     *     }
     * });
     * }</pre>
     *
     * @param owner      The {@link LifecycleOwner} (e.g., Activity or Fragment) controlling the observation lifecycle.
     * @param context    The application or activity context.
     * @param uniqueName The unique work name used during {@link SmartWorkerBuilder#enqueueUnique(String)}.
     * @param listener   The {@link WorkStateListener} callback to handle state changes.
     */
    public static void observe(@NonNull LifecycleOwner owner, @NonNull Context context, @NonNull String uniqueName, @NonNull WorkStateListener listener) {
        WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkLiveData(uniqueName)
                .observe(owner, workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo info = workInfos.get(0);
                        handleWorkState(info, listener);
                    }
                });
    }

    /**
     * Helper method to map {@link WorkInfo.State} to the corresponding methods in {@link WorkStateListener}.
     *
     * @param workInfo The {@link WorkInfo} object containing the current work execution status and data.
     * @param listener The {@link WorkStateListener} callback to receive state events.
     */
    private static void handleWorkState(WorkInfo workInfo, WorkStateListener listener) {
        if (workInfo == null) return;

        WorkInfo.State state = workInfo.getState();

        if (state == WorkInfo.State.SUCCEEDED) {
            listener.onSuccess(workInfo.getOutputData());
        } else if (state == WorkInfo.State.FAILED) {
            listener.onFailure(workInfo.getOutputData());
        } else if (state == WorkInfo.State.RUNNING) {
            listener.onProgress(workInfo.getProgress());
        }
    }

    /**
     * Cancels a unique work request by its unique name.
     *
     * @param context The application context.
     * @param uniqueName The unique string identifier of the work.
     */
    public static void cancelUnique(@NonNull Context context, @NonNull String uniqueName) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName);
    }

    /**
     * Cancels all work requests tagged with the specified tag.
     *
     * @param context The application context.
     * @param tag The tag associated with work requests.
     */
    public static void cancelAllByTag(@NonNull Context context, @NonNull String tag) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tag);
    }

    /**
     * Cancels a work request by its unique UUID.
     *
     * @param context The application context.
     * @param id The UUID of the work request.
     */
    public static void cancelById(@NonNull Context context, @NonNull UUID id) {
        WorkManager.getInstance(context).cancelWorkById(id);
    }
}