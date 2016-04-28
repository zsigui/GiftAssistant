# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\StudySoftware\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class code to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5 #混淆级别
-dontusemixedcaseclassnames
-skipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontnote
-ignorewarnings
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/* # 混淆时所采用的算法

# libs
-dontwarn android.net.http.SslError
-keep public class android.net.http.SslError
-keep class com.tencent.** { *; }
-keep class com.tendcloud.tenddata.** { *; }

-keep class com.nostra13.universalimageloader.** { *; }

-keep class com.facebook.rebound.** { *; }

# Umeng
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Support v4
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

# Support v7
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

#This is extra - added by me to exclude gson obfuscation
-keep class com.google.gson.** { *; }

##---------------End: proguard configuration for Gson  ----------

# entity
-keep public enum com.oplay.giftcool.model.** {*;}

# js
-keep public class android.webkit.WebViewClient
-dontwarn android.webkit.WebView
-dontwarn android.webkit.WebViewClient

-keep class com.oplay.giftcool.listener.WebViewInterface {*;}
-keepclassmembers class * extends android.webkit.WebViewClient {
     *;
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
     *;
}

-keep class com.android.** { *; }
-keep public class com.bigkoo.convenientbanner.ConvenientBanner { public *; }

# protobuf
-dontwarn com.google.**
-keep class com.google.protobuf.** {*;}

# jpush
-dontoptimize
-dontpreverify

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }


# Android
-keep class android.** {*;}
-keep class com.android.** {*;}
-keep class java.** {*;}
# common
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.support.v4.widget
-keep public class * extends com.sqlcrypt.database
-keep public class * extends com.sqlcrypt.database.sqlite
-keep public class * extends com.treecore.**
-keep public class * extends de.greenrobot.dao.**

-keepclasseswithmembernames class * {     # 保持 native 方法不被混淆
    native <methods>;
}

-keepclasseswithmembers class * {         # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {         # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity { #保持类成员
   public void *(android.view.View);
}

-keepclassmembers enum * {                  # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {    # 保持Parcelable不被混淆
  public static final android.os.Parcelable$Creator *;
}

#GalleryFinal begin
-keep class cn.finalteam.galleryfinal.widget.*{*;}
-keep class cn.finalteam.galleryfinal.widget.crop.*{*;}
-keep class cn.finalteam.galleryfinal.widget.zoonview.*{*;}
#GalleryFinal end


#lebian sdk begin
-keep class com.excelliance.** { *; }
-keepclassmembers class * implements java.lang.reflect.InvocationHandler {
	private java.lang.Object *(java.lang.Object, java.lang.reflect.Method, java.lang.Object[]);
}
#lebian sdk end

