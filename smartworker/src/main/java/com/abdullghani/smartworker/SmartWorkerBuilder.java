package com.abdullghani.smartworker;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Fluent builder class for configuring and enqueuing background work requests using {@link WorkManager}.
 * <p>
 * Supports both standard one-time tasks and periodic tasks, custom object serialization,
 * foreground execution, runtime constraints, and unique work policies.
 * </p>
 *
 * <b>Usage Example:</b>
 * <pre>{@code
 * SmartWorker.with(context)
 *     .oneTime(MySyncWorker.class)
 *     .requiresInternet()
 *     .requiresCharging()
 *     .setDelay(10, TimeUnit.MINUTES)
 *     .putString("user_id", "123")
 *     .enqueue();
 * }</pre>
 *
 * @author Abdullghani Al-Shormani
 * @version 1.0
 */
public class SmartWorkerBuilder {

    private final List<String> tags = new ArrayList<>();
    private final Context context;
    private Class<? extends ListenableWorker> workerClass;

    private final Constraints.Builder constraintsBuilder = new Constraints.Builder();
    private final Data.Builder dataBuilder = new Data.Builder();
    private final Map<String, Object> customObjects = new HashMap<>();

    // Periodicity
    private boolean isPeriodic = false;
    private long repeatInterval = 0;
    private TimeUnit repeatUnit = TimeUnit.MINUTES;

    // Delays & Policies
    private long delayAmount = 0;
    private TimeUnit delayUnit = TimeUnit.MILLISECONDS;
    private ExistingWorkPolicy existingOneTimePolicy = ExistingWorkPolicy.REPLACE;
    private ExistingPeriodicWorkPolicy existingPeriodicPolicy = ExistingPeriodicWorkPolicy.KEEP;

    private BackoffPolicy backoffPolicy = BackoffPolicy.EXPONENTIAL;
    private long backoffDelay = WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS;
    private TimeUnit backoffTimeUnit = TimeUnit.MILLISECONDS;

    // Foreground Info
    private boolean isForeground = false;
    private int notificationId = -1;
    private Notification notification;

    /**
     * Constructs a new {@link SmartWorkerBuilder} instance.
     *
     * @param context The application or activity context. Automatically converted to application context.
     */
    public SmartWorkerBuilder(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Configures the builder for a single, non-repeating execution of the specified worker.
     *
     * @param workerClass The class extending {@link ListenableWorker} to be executed once.
     * @return The current {@link SmartWorkerBuilder} instance for fluent API chaining.
     */
    public SmartWorkerBuilder oneTime(@NonNull Class<? extends ListenableWorker> workerClass) {
        this.workerClass = workerClass;
        this.isPeriodic = false;
        return this;
    }

    /**
     * Configures the builder for periodic repeating executions of the specified worker.
     * <p>
     * <b>Note:</b> The minimum periodic interval enforced by {@link WorkManager} is 15 minutes.
     * </p>
     *
     * @param workerClass The class extending {@link ListenableWorker} to be executed periodically.
     * @param interval    The duration of the periodic interval.
     * @param unit        The time unit for the interval duration.
     * @return The current {@link SmartWorkerBuilder} instance for fluent API chaining.
     */
    public SmartWorkerBuilder periodic(@NonNull Class<? extends ListenableWorker> workerClass, long interval, @NonNull TimeUnit unit) {
        this.workerClass = workerClass;
        this.isPeriodic = true;
        this.repeatInterval = Math.max(interval, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS);
        this.repeatUnit = unit;
        return this;
    }

    // --- Constraints ---

    /**
     * Sets a constraint requiring an active network connection for the worker to run.
     *
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder requiresInternet() {
        constraintsBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        return this;
    }

    /**
     * Sets a constraint requiring an unmetered network connection (e.g., Wi-Fi) for the worker to run.
     *
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder requiresUnmeteredNetwork() {
        constraintsBuilder.setRequiredNetworkType(NetworkType.UNMETERED);
        return this;
    }

    /**
     * Sets a constraint requiring the device to be charging for the worker to run.
     *
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder requiresCharging() {
        constraintsBuilder.setRequiresCharging(true);
        return this;
    }

    /**
     * Sets a constraint requiring the device battery not to be low for the worker to run.
     *
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder requiresBatteryNotLow() {
        constraintsBuilder.setRequiresBatteryNotLow(true);
        return this;
    }

    /**
     * Sets a constraint requiring available device storage not to be low for the worker to run.
     *
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder requiresStorageNotLow() {
        constraintsBuilder.setRequiresStorageNotLow(true);
        return this;
    }

    /**
     * Sets an initial execution delay for the work request.
     *
     * @param delay The delay duration before execution starts.
     * @param unit  The time unit of the delay duration.
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder setDelay(long delay, @NonNull TimeUnit unit) {
        this.delayAmount = delay;
        this.delayUnit = unit;
        return this;
    }

    // --- Data & Custom Objects Passing ---

    /**
     * Puts a string value into the worker's input data map.
     *
     * @param key   The string key.
     * @param value The string value.
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder putString(@NonNull String key, String value) {
        dataBuilder.putString(key, value);
        return this;
    }

    /**
     * Puts an integer value into the worker's input data map.
     *
     * @param key   The string key.
     * @param value The integer value.
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder putInt(@NonNull String key, int value) {
        dataBuilder.putInt(key, value);
        return this;
    }

    /**
     * Serializes a custom object to JSON and puts it into the worker input data.
     *
     * @param key    The string key to associate with the serialized object.
     * @param object The custom Object instance to serialize and pass.
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder putObject(@NonNull String key, Object object) {
        if (object != null) {
            customObjects.put(key, object);
        }
        return this;
    }

    // --- Unique Work Policies ---

    /**
     * Sets the conflict resolution policy for unique one-time work requests.
     *
     * @param policy The {@link ExistingWorkPolicy} strategy (e.g., REPLACE, KEEP, APPEND).
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder setExistingPolicy(@NonNull ExistingWorkPolicy policy) {
        this.existingOneTimePolicy = policy;
        return this;
    }

    /**
     * Sets the conflict resolution policy for unique periodic work requests.
     *
     * @param policy The {@link ExistingPeriodicWorkPolicy} strategy (e.g., REPLACE, KEEP).
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder setExistingPolicy(@NonNull ExistingPeriodicWorkPolicy policy) {
        this.existingPeriodicPolicy = policy;
        return this;
    }

    // --- Foreground Support ---

    /**
     * Configures the worker request to run as a long-running Foreground Service with a persistent notification.
     *
     * @param notificationId The unique ID for the foreground notification.
     * @param notification   The persistent {@link Notification} instance to show.
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder asForeground(int notificationId, @NonNull Notification notification) {
        this.isForeground = true;
        this.notificationId = notificationId;
        this.notification = notification;
        return this;
    }

    // --- Internal Request Building ---

    /**
     * Builds and configures an internal {@link OneTimeWorkRequest}.
     *
     * @return The configured {@link OneTimeWorkRequest} instance.
     */
    public OneTimeWorkRequest buildOneTimeRequest() {
        prepareCustomObjects();
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(workerClass)
                .setConstraints(constraintsBuilder.build())
                .setInputData(dataBuilder.build());

        if (delayAmount > 0) {
            builder.setInitialDelay(delayAmount, delayUnit);
        }

        return builder.build();
    }

    // --- Enqueuing Operations ---

    /**
     * Schedules the work request in {@link WorkManager}.
     *
     * @return The generated unique {@link UUID} of the enqueued work request.
     */
    public UUID enqueue() {
        prepareCustomObjects();
        WorkManager wm = WorkManager.getInstance(context);

        if (isPeriodic) {
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(workerClass, repeatInterval, repeatUnit)
                    .setConstraints(constraintsBuilder.build())
                    .setInputData(dataBuilder.build())
                    .build();
            wm.enqueue(request);
            return request.getId();
        } else {
            OneTimeWorkRequest request = buildOneTimeRequest();
            wm.enqueue(request);
            return request.getId();
        }
    }

    /**
     * Schedules a unique work request identified by a unique name to avoid redundant task executions.
     *
     * @param uniqueName A unique string identifier for this work sequence.
     * @return The generated unique {@link UUID} of the enqueued work request.
     */
    public UUID enqueueUnique(@NonNull String uniqueName) {
        prepareCustomObjects();
        WorkManager wm = WorkManager.getInstance(context);

        if (isPeriodic) {
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(workerClass, repeatInterval, repeatUnit)
                    .setConstraints(constraintsBuilder.build())
                    .setInputData(dataBuilder.build())
                    .build();
            wm.enqueueUniquePeriodicWork(uniqueName, existingPeriodicPolicy, request);
            return request.getId();
        } else {
            OneTimeWorkRequest request = buildOneTimeRequest();
            wm.enqueueUniqueWork(uniqueName, existingOneTimePolicy, request);
            return request.getId();
        }
    }

    private void prepareCustomObjects() {
        for (Map.Entry<String, Object> entry : customObjects.entrySet()) {
            dataBuilder.putString(entry.getKey(), SmartWorkerHelper.serializeObject(entry.getValue()));
        }
    }



    /**
     * Adds a tag to the work request for grouping and query/cancellation operations.
     *
     * @param tag A string tag to associate with this work request.
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder addTag(@NonNull String tag) {
        tags.add(tag);
        return this;
    }

    /**
     * Sets the backoff policy and delay for retrying failed work requests.
     *
     * @param policy The {@link BackoffPolicy} (e.g., LINEAR or EXPONENTIAL).
     * @param delay The backoff delay duration.
     * @param unit The time unit for the backoff duration.
     * @return The current {@link SmartWorkerBuilder} instance.
     */
    public SmartWorkerBuilder setRetryPolicy(@NonNull BackoffPolicy policy, long delay, @NonNull TimeUnit unit) {
        this.backoffPolicy = policy;
        this.backoffDelay = delay;
        this.backoffTimeUnit = unit;
        return this;
    }
}