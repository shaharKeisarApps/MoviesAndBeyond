# ========================
# MoviesAndBeyond ProGuard/R8 Rules
# ========================

# Optimization passes
-optimizationpasses 5

# Print mapping file for crash deobfuscation
-printmapping build/outputs/mapping/release/mapping.txt

# Keep source file and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========================
# Room
# ========================
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ========================
# Moshi
# ========================
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class **JsonAdapter { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keepnames @com.squareup.moshi.JsonClass class *

# ========================
# Kotlin Serialization
# ========================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    *** INSTANCE;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========================
# Retrofit
# ========================
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# ========================
# Ktor
# ========================
-dontwarn io.ktor.**

# ========================
# Kotlin Coroutines
# ========================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========================
# Hilt / Dagger
# ========================
-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

# ========================
# DataStore Proto
# ========================
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
