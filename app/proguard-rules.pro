# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in $ANDROID_SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# OkHttp and Servlet optional dependencies

-dontwarn okio.**
-dontnote okio.**
-dontwarn okhttp3.**
-dontnote okhttp3.**
-dontwarn org.apache.poi.**
-dontnote org.apache.poi.**
-dontwarn com.google.appengine.**
-dontwarn javax.servlet.**
-dontwarn javax.servlet.**

-dontnote com.google.android.gms.**
-dontnote androidx.core.**
-dontnote androidx.media.**
# Support classes for compatibility with older API versions

-dontwarn android.support.**
-dontnote android.support.**

-keep class * {
    public private *;
}