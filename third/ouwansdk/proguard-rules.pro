# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/zsigui/software/android/android-sdk-linux/tools/proguard/proguard-android.txt
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

#-dontwarn android.net.http.SslError
#-keep class android.net.http.SslError{*;}

-dontnote
-dontpreverify
-dontoptimize
-verbose
-skipnonpubliclibraryclasses # 不混淆jar中非public的类

#泛型
-keepattributes Signature
-dontwarn android.net.http.SslError
-keep class android.net.http.SslError{*;}

# 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

#
-dontnote android.os.SystemProperties
-dontnote com.google.android.gms.ads.identifier.**
-dontnote com.android.internal.telephony.PhoneFactory

-dontnote **ILicensingService
-keep class android.net.** {*;}
-keep class com.android.internal.http.multipart.** {*;}
-keep class org.apache.** {*;}
#-dontwarn net.youmi.android.libs.**
#-keep class net.youmi.android.libs.** {*;}
#-dontwarn net.ouwan.umipay.android.**
#-keep class net.ouwan.umipay.android.** {*;}
-dontwarn com.alipay.**
-keep class com.alipay.** {*;}
-dontwarn
-keep class org.json.alipay.** {*;}
-dontwarn com.google.gson.**
-keep class com.google.gson.** {*;}



# umipay
-keep public class net.ouwan.umipay.android.api.** {
	public <methods>;
	public <fields>;
}
-keep class net.ymfx.android.d.** {*;}
-keepclassmembers class net.youmi.android.libs.webjs.js.base.JsInterface_Impl {
	   public *;
}

# Apache
-dontwarn org.apache.**
-keep class org.apache.** {*;}

# Alipay
-dontwarn com.alipay.**
-keep class com.alipay.** {*;}
-keep class com.ta.utdid2.** {*;}
-keep class com.ut.device.** {*;}
-keep class org.json.alipay.** {*;}

# 银联
-dontwarn com.unionpay.**
-keep class com.payeco.android.plugin.**{*;}
-keep class cn.gov.pdc.tsm.client.mobile.** {*;}
-keep class com.UCMobile.PayPlugin.** {*;}
-keep class com.unionpay.** {*;}

# 支付宝-微信
-keep class com.tencent.** {*;}
-keep class com.ipaynow.plugin.** {*;}
-keep class com.junnet.heepay.** {*;}




