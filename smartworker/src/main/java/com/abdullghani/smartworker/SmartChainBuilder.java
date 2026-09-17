package com.abdullghani.smartworker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder class for managing sequential and parallel background task executions (WorkChains).
 * <p>
 * This class simplifies chaining multiple {@link ListenableWorker} instances together, allowing
 * tasks to run either in parallel or sequentially in a specified order.
 * </p>
 *
 * <b>Usage Example:</b>
 * <pre>{@code
 * SmartWorker.chain(context)
 *     .beginParallel(DownloadImageWorker.class, DownloadAudioWorker.class)
 *     .then(ProcessDataWorker.class)
 *     .start();
 * }</pre>
 *
 * @author Abdullghani Al-Shormani
 * @version 1.0
 */
public class SmartChainBuilder {

    private final Context context;
    private WorkContinuation continuation;

    /**
     * Constructs a new {@link SmartChainBuilder} instance.
     *
     * @param context The application or activity context. It is automatically converted to
     *                the application context to prevent memory leaks.
     */
    public SmartChainBuilder(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Starts the work chain by enqueuing multiple workers to run <b>in parallel</b> simultaneously.
     * <p>
     * Subsequent tasks added via {@link #then(Class)} will only execute after <b>all</b>
     * workers passed to this method complete successfully.
     * </p>
     *
     * <b>Example:</b>
     * <pre>{@code
     * SmartWorker.chain(context)
     *     .beginParallel(FetchNewsWorker.class, FetchWeatherWorker.class)
     *     .start();
     * }</pre>
     *
     * @param workerClasses The array of worker classes extending {@link ListenableWorker} to run in parallel.
     * @return The current {@link SmartChainBuilder} instance for fluent API chaining.
     */
    @SafeVarargs
    public final SmartChainBuilder beginParallel(@NonNull Class<? extends ListenableWorker>... workerClasses) {
        List<OneTimeWorkRequest> requests = new ArrayList<>();
        for (Class<? extends ListenableWorker> clazz : workerClasses) {
            requests.add(new OneTimeWorkRequest.Builder(clazz).build());
        }
        continuation = WorkManager.getInstance(context).beginWith(requests);
        return this;
    }

    /**
     * Appends a worker task to be executed <b>sequentially after</b> the preceding tasks finish successfully.
     * <p>
     * If {@link #beginParallel(Class[])} was not called previously, this worker will serve as the starting point
     * of the chain.
     * </p>
     *
     * <b>Example:</b>
     * <pre>{@code
     * SmartWorker.chain(context)
     *     .beginParallel(DownloadFileWorker.class)
     *     .then(UnzipFileWorker.class)       // Executes after download completes
     *     .then(SaveToDatabaseWorker.class) // Executes after unzip completes
     *     .start();
     * }</pre>
     *
     * @param workerClass The worker class extending {@link ListenableWorker} to be appended.
     * @return The current {@link SmartChainBuilder} instance for fluent API chaining.
     */
    public SmartChainBuilder then(@NonNull Class<? extends ListenableWorker> workerClass) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(workerClass).build();
        if (continuation != null) {
            continuation = continuation.then(request);
        } else {
            continuation = WorkManager.getInstance(context).beginWith(request);
        }
        return this;
    }

    /**
     * Enqueues the configured chain of work requests to {@link WorkManager} for execution.
     * <p>
     * <b>Note:</b> This method must be called as the final step in the chain. Without calling this,
     * the tasks will not be scheduled or executed.
     * </p>
     *
     * <b>Example:</b>
     * <pre>{@code
     * SmartWorker.chain(context)
     *     .then(MyWorker.class)
     *     .start(); // Triggers actual execution
     * }</pre>
     */
    public void start() {
        if (continuation != null) {
            continuation.enqueue();
        }
    }
}