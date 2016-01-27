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

-keepclassmembers class net.owan.android.a.js.JsInterface_Impl {
	   public *;
}

#-dontwarn android.net.http.SslError
#-keep class android.net.http.SslError{*;}

#owanlib
-keep class net.owan.android.main

#umipay
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class net.umipay.android.GameParaInfo {
    public <methods>;
}

-keep public class net.umipay.android.UmiPaymentInfo {
    public <methods>;
}

-keep public class net.umipay.android.GameUserInfo {
    public <methods>;
}

-keep public class net.umipay.android.UmipayOrderInfo {
    public <methods>;
}

#-keep public class net.umipay.android.UmipayBrowser {
#    public <methods>
#}

-keep public class net.umipay.android.UmiPaySDKManager{
	    public <methods>;
}

-keep public class net.umipay.android.UmipaySDKStatusCode{
		public <fields>;
}

-keep class net.umipay.android.interfaces.** {*;}

-keep public class net.owan.android.Djy{
		public <methods>;
}

-keep class net.umipay.android.d.aa{
	*;
}

-keep class net.umipay.android.handler.** {*;}

-keep class net.umipay.android.poll.Push_Alarm_Receiver{
	*;
}
-keep class net.umipay.android.poll.Push_Boot_Receiver{
	public *;
}
-keep class net.umipay.android.poll.Push_Poll_service{
	public *;
}

-keep class com.payeco.android.plugin.** {
*;
}
-keep class com.tencent.** {
*;
}
-keep class com.weibo.sdk.android.** {
*;
}

#泛型
-keepattributes Signature

#js
-keepclassmembers class net.youmi.android.libs.webjs.js.base.JsInterface_Impl {
	   public *;
}

-dontwarn android.net.http.SslError
-keep class android.net.http.SslError{*;}

#umipay
#-keep class net.ouwan.umipay.android.api.**
-keep public class net.ouwan.umipay.android.api.** {
	public <methods>;
	public <fields>;
}
-keep class net.ymfx.android.d.** {
	*;
}
-dontwarn com.payeco.android.plugin.**
-keep class com.payeco.android.plugin.** {
	*;
}
-keep class com.tencent.** {
	*;
}



#alipay
#-libraryjars libs/alipaysdk.jar
#-libraryjars libs/alipaysecsdk.jar
#-libraryjars libs/alipayutdid.jar
-keep class com.alipay.android.app.IAlixPay{
	*;
}
-keep class com.alipay.android.app.IAlixPay$Stub{
	*;
}
-keep class com.alipay.android.app.IRemoteServiceCallback{
	*;
}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{
	*;
}
-keep class com.alipay.sdk.app.PayTask{
	public *;
}
-keep class com.alipay.sdk.auth.AlipaySDK{
    public *;
}
-keep class com.alipay.sdk.auth.APAuthInfo{
	public *;
}
-keep class com.alipay.mobilesecuritysdk.*
-keep class com.ut.*
#易联
-keep class  com.payeco.android.plugin.**{
	*;
}
#汇付宝-微信
#-libraryjars libs/HeepayService.jar
-dontwarn com.junnet.heepay.**
-keep public class com.ipaynow.plugin.utils.MerchantTools{ <fields>; <methods>; }
-keep public class com.ipaynow.plugin.utils.PreSignMessageUtil{ <fields>; <methods>; }
-keep public class com.ipaynow.plugin.api.IpaynowPlugin{ <fields>; <methods>; }
-keep class com.junnet.heepay.** { *; }