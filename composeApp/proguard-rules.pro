# Add project specific ProGuard rules here.

# Data models should be kept if they are used for serialization
# without the @Serializable annotation, or if they are accessed via reflection.
# It's better to use @androidx.annotation.Keep on specific classes.
-keep @androidx.annotation.Keep class ** { *; }

# Keep Kotlin serialization (standard rules)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Remove broad library keeps - these libraries provide their own rules:
# - io.ktor.**
# - com.google.firebase.**
# - androidx.compose.**
# - coil3.**
# - app.cash.sqldelight.**

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
-dontwarn android.media.LoudnessCodecController
-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener

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
