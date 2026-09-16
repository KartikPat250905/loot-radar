# Add project specific ProGuard rules here.

# Data models should be kept if they are used for serialization
# without the @Serializable annotation, or if they are accessed via reflection.
# It's better to use @androidx.annotation.Keep on specific classes.
-keep @androidx.annotation.Keep class ** { *; }

# Keep Kotlin serialization (standard rules)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep generic signature (for reflection and serialization)
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*

# Suppress warnings for missing libraries that are not used at runtime
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.android.gms.internal.ads.**
-dontwarn com.google.firebase.auth.api.internal.**
-dontwarn android.media.LoudnessCodecController
-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener

# NOTE: the old blanket rule
#   -keep class com.google.android.gms.internal.** { *; }
# has been removed. It covered Ads, Firebase Auth, Maps, etc. internals
# wholesale and was the main reason R8's optimization/obfuscation/shrinking
# rates were stuck around 42-43% - it told R8 not to touch a huge slice
# of the app. Play Services libraries ship their own consumer proguard
# rules (bundled in their AARs), so most of this manual keeping is
# redundant. If you hit a specific reflection/JNI crash after removing
# it, add back a narrow, targeted rule instead of a blanket one, e.g.:
#
# -keepclassmembers class com.google.android.gms.internal.ads.zzcec {
#     <fields>;
#     <init>(...);
# }
#
# only for the exact class that crashes, not the whole package.

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep WorkManager's Room-generated database (fixes reflection lookup crash)
-keep class androidx.work.impl.WorkDatabase
-keep class androidx.work.impl.WorkDatabase_Impl

# Remove all logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile