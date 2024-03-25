# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-printmapping out.map

-dontusemixedcaseclassnames
-dontshrink
-dontoptimize

-repackageclasses com.example.sdk.internal
-allowaccessmodification

-keep class kotlin.Metadata
-keep class !com.example.sdk.internal.** {
    public protected <methods>;
    public protected <fields>;
}

# The Android SDK checks at runtime if these classes are available via Class.forName
-keepnames class androidx.fragment.app.FragmentManager$FragmentLifecycleCallbacks

# Keep the BuildConfig
-keep class com.example.sdk.BuildConfig { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep the R
-keepclassmembers class **.R$* {
    public static <fields>;
}