# SmartWorker 🚀

**SmartWorker** is a powerful, lightweight Android library wrapper designed to simplify working with Android's official **WorkManager**. It provides a developer-friendly **Fluent API** that shields developers from repetitive boilerplate code and edge-case bugs, while making it effortless to manage one-time, periodic, chained tasks, complex object serialization, and foreground services.

---

## 🌟 Why SmartWorker?

* ⚡ **Fluent API**: Clean, expressive, and self-documenting method calls.
* 📦 **Custom Object Serialization**: Pass Java/Kotlin objects directly via `WorkData` powered by Gson.
* 🔗 **Work Chaining & Parallel Execution**: Easily build complex workflows combining parallel and sequential tasks.
* 📊 **Simplified Task Observation**: Monitor work states (`Success`, `Failure`, `Progress`) without dealing with complex `LiveData` setup.
* 🛡️ **Foreground Service Support**: Smoothly promote long-running background tasks to Foreground Services.

---

## 📦 Installation

Add the JitPack repository to your `settings.gradle` file:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url '[https://jitpack.io](https://jitpack.io)' }
    }
}
```
Add the dependency to your app's build.gradle:

```groovy
dependencies {
    implementation 'com.github.abdullghani-al-shormani:smartworker:1.0.0'
}
```
📖 Complete Usage Guide
1. One-Time Work Execution
Used for single-run background operations (e.g., file upload, data sync, or quick background processing) with full support for system constraints and initial delays.

```java
UUID workId = SmartWorker.with(this)
        .oneTime(MySyncWorker.class)
        .requiresInternet()         // Require active internet connection
        .requiresCharging()         // Require device to be plugged in
        .setDelay(5, TimeUnit.MINUTES) // Delay execution by 5 minutes
        .enqueue();
```

2. Periodic Work Execution
Used for recurring background operations (e.g., daily database backups). Respects the Android WorkManager minimum interval limit of 15 minutes.

```java
SmartWorker.with(this)
        .periodic(DatabaseBackupWorker.class, 2, TimeUnit.HOURS)
        .requiresStorageNotLow()    // Require sufficient storage space
        .requiresBatteryNotLow()    // Require battery not to be low
        .setExistingPolicy(ExistingPeriodicWorkPolicy.KEEP) // Keep existing task if present
        .enqueueUnique("daily_db_backup");
```

3. Data & Custom Object Serialization
Overcomes standard WorkManager limitations that only support primitive data types by allowing custom Java/Kotlin objects to be passed and serialized automatically via Gson.

Sending data from an Activity or Fragment:
```java
UserProfile user = new UserProfile("Abdullghani", "Admin");

SmartWorker.with(this)
        .oneTime(UpdateProfileWorker.class)
        .putString("user_id", "123")
        .putInt("retry_count", 3)
        .putObject("user_profile", user) // Pass custom object directly
        .enqueue();
```

Extracting data inside your Worker class:
```java
public class UpdateProfileWorker extends Worker {
    public UpdateProfileWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Retrieve standard primitive data
        String userId = getInputData().getString("user_id");

        // Effortlessly retrieve the custom object
        UserProfile user = SmartWorkerHelper.getObject(getInputData(), "user_profile", UserProfile.class);

        return Result.success();
    }
}
```

4. Work Chaining & Parallel Execution
Build complex task workflows by running multiple tasks in parallel first, followed by sequential steps once completed.
```java
SmartWorker.chain(this)
        .beginParallel(DownloadImageWorker.class, DownloadAudioWorker.class) // Execute in parallel
        .then(ProcessDataWorker.class)                                      // Execute after downloads complete
        .then(UploadServerWorker.class)                                     // Final execution step
        .start();
```

5. Task State & Progress Observation
Monitor task activity, progress updates, and completion results directly with clean callback interfaces without boilerplate LiveData or WorkInfo management.

```java
SmartWorker.observe(this, context, "daily_db_backup", new WorkStateListener() {
    @Override
    public void onSuccess(@NonNull Data outputData) {
        // Task completed successfully
    }

    @Override
    public void onFailure(@NonNull Data outputData) {
        // Task failed
    }

    @Override
    public void onProgress(@NonNull Data progressData) {
        // Extract real-time progress data
        int progress = progressData.getInt("progress_key", 0);
    }
});
```

6. Foreground Services Support
Designed for critical long-running tasks that must continue running uninterrupted by attaching an ongoing Notification.

Scheduling the worker:
```java
SmartWorker.with(context)
        .oneTime(FileDownloadWorker.class)
        .asForeground(NOTIFICATION_ID, notificationObject)
        .enqueue();
```
Inside the Worker during execution:
```java
SmartWorkerHelper.promoteToForeground(this, NOTIFICATION_ID, notificationObject);
```
📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
