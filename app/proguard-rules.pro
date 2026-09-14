# ==============================================================================
# ProGuard / R8 Optimization & Security Configuration for Production Release
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Bytecode Optimization & Code Shrinking
# ------------------------------------------------------------------------------
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-repackageclasses ''

# Retain annotations, signatures, and inner classes needed by Compose & Kotlin
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Obfuscate source file names for security while preserving line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ------------------------------------------------------------------------------
# 2. Complete Elimination of Debug & Logging Code
# ------------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int println(int, java.lang.String, java.lang.String);
}

# ------------------------------------------------------------------------------
# 3. Android Core Components & Lifecycle
# ------------------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# App Widgets and Alarm Receivers (Manifest & PendingIntent targets)
-keep class com.example.widget.PrayerWidgetProvider { *; }
-keep class com.example.alarm.PrayerAlarmReceiver { *; }
-keep class com.example.alarm.PrayerStopAudioReceiver { *; }
-keep class com.example.worker.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Sensor Listeners (Qibla Compass)
-keepclassmembers class * implements android.hardware.SensorEventListener {
    public void onSensorChanged(android.hardware.SensorEvent);
    public void onAccuracyChanged(android.hardware.Sensor, int);
}

# ------------------------------------------------------------------------------
# 4. Kotlin & Coroutines Optimization
# ------------------------------------------------------------------------------
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepclassmembers class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class * extends kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# ------------------------------------------------------------------------------
# 5. Jetpack Compose Rules
# ------------------------------------------------------------------------------
-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ------------------------------------------------------------------------------
# 6. Data Models, Enums & Persistence
# ------------------------------------------------------------------------------
# Preserve application data models, preferences, and enum types
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ------------------------------------------------------------------------------
# 7. Network & Serialization (Retrofit, OkHttp, Moshi)
# ------------------------------------------------------------------------------
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Moshi
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclasseswithmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# ------------------------------------------------------------------------------
# 8. Coil Image Loading
# ------------------------------------------------------------------------------
-dontwarn coil.**
-keep class coil.** { *; }

# ------------------------------------------------------------------------------
# 9. Native Methods Protection
# ------------------------------------------------------------------------------
-keepclasseswithmembernames class * {
    native <methods>;
}
