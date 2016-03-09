package net.ouwan.umipay.android.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.heepay.plugin.activity.Constant;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.global.Global_Data_Offers;
import net.ouwan.umipay.android.handler.JsHandler_Pay_With_UPMP;
import net.ouwan.umipay.android.handler.JsHandler_Pay_With_WECHAT;
import net.ouwan.umipay.android.handler.JsModel_Browser_PayExtent_Js_Interface_Factory;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_UrlCoder;
import net.youmi.android.libs.common.util.Util_System_Runtime;
import net.youmi.android.libs.webjs.download.main.DefaultSDKApkDownloadManager;
import net.youmi.android.libs.webjs.js.JS_SDK_Handler_Result;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.interfaces.Interface_Js_View_TitleSetable;
import net.youmi.android.libs.webjs.js.model.JsModel_AppItem;
import net.youmi.android.libs.webjs.js.model.JsModel_CallBack_Item;
import net.youmi.android.libs.webjs.view.webview.Flags_Browser_Config;
import net.youmi.android.libs.webjs.view.webview.View_SDKWebPage;
import net.youmi.android.libs.webjs.view.webview.interfaces.Interface_Default_SDK_Handler_Proxy;
import net.youmi.android.libs.webjs.view.webview.interfaces.Interface_View_YoumiWebViewClient;

import org.apache.http.NameValuePair;
import org.apache.http.util.EncodingUtils;

import java.util.List;

/**
 * UmipayBrowser
 *
 * @author zacklpx
 *         date 15-3-17
 *         description
 */
public class UmipayBrowser extends Activity implements Interface_SDK_Handler, Interface_Js_View_TitleSetable,
		View.OnClickListener {

	public static final int NOT_PAY = 0;//不是充值
	public static final int PAY_GAME = 1;//游戏充值
	public static final int PAY_OUWAN = 2;//偶玩豆充值

	public static final int ACTION_DEFAULT = 0;
	public static final int ACTION_MODIFY_PSW = 1;
	public static final int ACTION_CHANGE_PHONE = 2;
	public static final int ACTION_BIND_PHONE = 3;
	public static final int ACTION_BIND_OUWAN = 4;

	public static final int ACTION_CODE_DEFAULT = -1;
	public static final int ACTION_CODE_FAILED = 0;
	public static final int ACTION_CODE_SUCCESS = 1;

	/**
	 * session of Activity，会话，如果会话不存在则自动关闭Activity，这样的做法是为了防止进程结束重启的情况导致业务流程错误。
	 */
	static final String KEY_ACTIVITY_SESSION = "pSZB3rrNTwaq";
	/**
	 * url的key,sdk定义
	 */
	private static final String KEY_URL = "ZdCHFuh4r7uZ";
	/**
	 * flags配置项的key,sdk定义，配置项的定义详见 View_YoumiWebView
	 */
	private static final String KEY_FLAGS = "sAuS4NfoHH2l";
	private static final String KEY_BROWSER_LOAD_JS_CODE = "wQp6MB0shXAL";
	private static final String KEY_BROWSER_LOAD_JS_FILE_URL = "gVygD7wQw8Li";
	private static final String KEY_POST_DATA = "SZdsfBrNTwaq";
	private static final String KEY_PAY_TYPE = "paytype";
	private static final String KEY_ACTION_TYPE = "action";
	/**
	 * 标题栏
	 */
	private static final String KEY_TITLE = "OuBJ7oKLYq31";
	private String mBaseUrl;
	private String mTitle;
	private String mAllPageJsCode;
	private String mAllPageLoadJsFileUrl;
	private String mPostData;
	private int mFlags;
	private int mPayType;
	/**
	 * 标识标题是否跟随页面变动
	 */
	private boolean mIsAutoChangeTitle = false;

	private View mRightBtn;
	private TextView mTitileView;
	private LinearLayout mContentLayout;
	private View_SDKWebPage mWebPage;
	//支付状态
	private int mPayCode = 0;
	//操作状态
	private int mActionCode = 0;
	private int mActionType = 0;

	public static void preLoadUrl(Context context, String url) {
		View_SDKWebPage preLoadWebPage = new View_SDKWebPage(context, null,
				Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES, null,
				null, null);
		CookieManager.getInstance().setAcceptCookie(true);
		preLoadWebPage.loadUrl(url);
	}

	public static void loadUrl(Context context, String title, String url,
	                           int flags, String allPageLoadJsCode, String allPageLoadJsFileUrl) {
		try {
			Intent intent = new Intent(context, UmipayBrowser.class);
			intent.putExtra(KEY_TITLE, title);
			intent.putExtra(KEY_URL, url);
			intent.putExtra(KEY_FLAGS, flags);
			intent.putExtra(KEY_ACTIVITY_SESSION,
					Global_Data_Offers.getUmipayOffersActivitySession());
			if (allPageLoadJsCode != null) {
				intent.putExtra(KEY_BROWSER_LOAD_JS_CODE, allPageLoadJsCode);
			}
			if (allPageLoadJsFileUrl != null) {
				intent.putExtra(KEY_BROWSER_LOAD_JS_FILE_URL,
						allPageLoadJsFileUrl);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void postUrl(Context context, String title, String url, List<NameValuePair> paramsList,
	                           int flags, String allPageLoadJsCode, String allPageLoadJsFileUrl, int payType) {
		try {
			Intent intent = new Intent(context, UmipayBrowser.class);
			intent.putExtra(KEY_TITLE, title);
			intent.putExtra(KEY_URL, url);
			intent.putExtra(KEY_FLAGS, flags);
			intent.putExtra(KEY_ACTIVITY_SESSION,
					Global_Data_Offers.getUmipayOffersActivitySession());
			intent.putExtra(KEY_PAY_TYPE, payType);
			if (allPageLoadJsCode != null) {
				intent.putExtra(KEY_BROWSER_LOAD_JS_CODE, allPageLoadJsCode);
			}
			if (allPageLoadJsFileUrl != null) {
				intent.putExtra(KEY_BROWSER_LOAD_JS_FILE_URL,
						allPageLoadJsFileUrl);
			}

			if (paramsList != null && paramsList.size() > 0) {
				intent.putExtra(KEY_POST_DATA, generatePostData(paramsList));
			}

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public static void postUrl(Context context, String title, String url, List<NameValuePair> paramsList, int flags,
	                           String allPageLoadJsCode, String allPageLoadJsFileUrl, int payType, int actionType) {
		try {
			Intent intent = new Intent(context, UmipayBrowser.class);
			intent.putExtra(KEY_TITLE, title);
			intent.putExtra(KEY_URL, url);
			intent.putExtra(KEY_FLAGS, flags);
			intent.putExtra(KEY_ACTIVITY_SESSION,
					Global_Data_Offers.getUmipayOffersActivitySession());
			intent.putExtra(KEY_PAY_TYPE, payType);
			intent.putExtra(KEY_ACTION_TYPE, actionType);
			if (allPageLoadJsCode != null) {
				intent.putExtra(KEY_BROWSER_LOAD_JS_CODE, allPageLoadJsCode);
			}
			if (allPageLoadJsFileUrl != null) {
				intent.putExtra(KEY_BROWSER_LOAD_JS_FILE_URL,
						allPageLoadJsFileUrl);
			}

			if (paramsList != null && paramsList.size() > 0) {
				intent.putExtra(KEY_POST_DATA, generatePostData(paramsList));
			}

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private static String generatePostData(List<NameValuePair> params) {

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < params.size(); i++) {
			NameValuePair nameValuePair = params.get(i);
			if (nameValuePair == null) {
				continue;
			}
			if (i == 0) {
				builder.append(nameValuePair.getName()).append("=").append(Coder_UrlCoder.urlEncode(nameValuePair
						.getValue()));
			} else {
				if (!Basic_StringUtil.isNullOrEmpty(nameValuePair.getValue())) {
					builder.append("&").append(nameValuePair.getName()).append("=").append(Coder_UrlCoder.urlEncode
							(nameValuePair.getValue()));
				}
			}
		}
		String paramsStr = builder.toString();
		return paramsStr;
	}

	private boolean checkSession(Intent intent) {
		try {
			String session = intent.getStringExtra(KEY_ACTIVITY_SESSION);
			session = Basic_StringUtil.getNotEmptyStringElseReturnNull(session);
			if (session == null) {
				return false;
			}
			return session.equals(Global_Data_Offers.getUmipayOffersActivitySession());
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Intent intent = getIntent();
			if (intent == null) {
				this.finish();
				return;
			}
			if (!checkSession(intent)) {
				this.finish();
				return;
			}
			try {
				mBaseUrl = intent.getStringExtra(KEY_URL);
				if (mBaseUrl == null) {
					this.finish();
					return;
				}
				mTitle = intent.getStringExtra(KEY_TITLE);
				if (TextUtils.isEmpty(mTitle)) {
					mTitle = "浏览页";
				}
				mAllPageJsCode = intent.getStringExtra(KEY_BROWSER_LOAD_JS_CODE);
				mAllPageLoadJsFileUrl = intent.getStringExtra(KEY_BROWSER_LOAD_JS_FILE_URL);
				mPostData = intent.getStringExtra(KEY_POST_DATA);
				mFlags = intent.getIntExtra(KEY_FLAGS, 0);
				mPayType = intent.getIntExtra(KEY_PAY_TYPE, 0);
				mActionType = intent.getIntExtra(KEY_ACTION_TYPE, ACTION_DEFAULT);
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
			setupViews();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

	}

	protected void setupViews() {
		setContentView(Util_Resource.getIdByReflection(this, "layout", "umipay_layout_webview"));
		mRightBtn = findViewById(Util_Resource.getIdByReflection(this, "id", "umipay_titlebar_rightbtn"));
		mRightBtn.setVisibility(View.VISIBLE);
		mTitileView = (TextView) findViewById(Util_Resource.getIdByReflection(this, "id", "umipay_titlebar_title"));
		mContentLayout = (LinearLayout) findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_webview_contentview"));
		mTitileView.setText(mTitle);
		this.mIsAutoChangeTitle = ((mFlags & 4) != 0);
		mFlags |= Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES | Flags_Browser_Config.FLAG_SUPPORT_CUSTOM_ALERT |
				Flags_Browser_Config.FLAG_SUPPORT_CUSTOM_CONFIRM;

		JsModel_Browser_PayExtent_Js_Interface_Factory factory = new JsModel_Browser_PayExtent_Js_Interface_Factory();
		mWebPage = new View_SDKWebPage(this, this, mFlags, this, mAllPageJsCode, mAllPageLoadJsFileUrl, 0, factory);
		mWebPage.setYoumiWebViewClient(new UmipayWebViewClinet());
		mWebPage.getCurrentView().setPadding(0, 0, 0, 0);
		if (Build.VERSION.SDK_INT >= 19) {
			//android4.4以上webview硬件加速时会导致渲染异常，背景设置透明失效，先禁止硬件加速
			mWebPage.getCurrentView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		mContentLayout.addView(mWebPage.getCurrentView(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams
				.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
		CookieManager.getInstance().setAcceptCookie(true);
		if (TextUtils.isEmpty(mPostData)) {
			mWebPage.loadUrl(mBaseUrl);
		} else {
			mWebPage.postUrl(mBaseUrl, EncodingUtils.getBytes(mPostData, "BASE64"));
		}
		initListener();
	}

	private void initListener() {
		mRightBtn.setOnClickListener(this);
		mTitileView.setOnClickListener(this);
	}

	@Override
	public void setWebTitle(String title) {
		try {
			if (mIsAutoChangeTitle) {
				this.mTitle = title;
				mTitileView.setText(title);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private boolean isPayPage() {
		return !TextUtils.isEmpty(mBaseUrl) && mBaseUrl.equalsIgnoreCase(SDKConstantConfig.get_UMIPAY_PAY_URL(this));
	}

	private void showClosePayDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setCustomTitle(null)
				.setMessage("确定关闭当前窗口？")
				.setPositiveButton("退出", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				})
				.setNegativeButton("返回", null);
		builder.create().show();
	}

	public void setCloseViewVisibility(final int mVisibility) {
		if (mWebPage == null) {
			return;
		}
		mWebPage.js_SDK_Handler_RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (mRightBtn != null) {
					mRightBtn.setVisibility(mVisibility == 0 ? View.GONE : View.VISIBLE);
				}
			}
		});
	}

	public void setPayCode(int code) {
		if (isPayPage()) {
			mPayCode = code;
		}
	}

	public boolean setActionCode(int actionType, int actionCode) {
		if (actionType != mActionType) {
			return false;
		}
		mActionCode = actionCode;
		return true;
	}

	public void logout_CloseBrowser() {
		if (mWebPage == null) {
			return;
		}
		Util_System_Runtime.getInstance().runInUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					UmipaySDKManager.logoutAccount(UmipayBrowser.this, null);
					finish();
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			}
		});
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public String getWebAssetsRootPathUri() {
		//TODO
		return null;
	}

	@Override
	public void setSdkHandlerProxy(Interface_Default_SDK_Handler_Proxy proxy) {

	}

	@Override
	public JS_SDK_Handler_Result js_SDK_Handler_CloseCurrentWindow() {
		try {
			finish();
			return JS_SDK_Handler_Result.Success;
		} catch (Throwable e) {
			Debug_Log.e(e);
			return JS_SDK_Handler_Result.Exception;
		}
	}

	@Override
	public int js_SDK_Handler_GetSdkVersion() {
		return SDKConstantConfig.UMIPAY_SDK_VERSION;
	}

	@Override
	public int js_SDK_Handler_GetTargetSdkVersion() {
		return 0;
	}

	@Override
	public int js_SDK_Handler_GetSdkID() {
		return SDKConstantConfig.SDK_ID;
	}

	@Override
	public String js_SDK_Handler_GetGwExtendUrl_RandomKey(String restUrl, String extend) {
		return null;
	}

	@Override
	public JS_SDK_Handler_Result js_SDK_Handler_ReloadPage() {
		try {
			if (mWebPage != null) {
				if (TextUtils.isEmpty(mPostData)) {
					mWebPage.loadUrl(mBaseUrl);
				} else {
					mWebPage.postUrl(mBaseUrl, EncodingUtils.getBytes(mPostData, "BASE64"));
				}
				return JS_SDK_Handler_Result.Success;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
			return JS_SDK_Handler_Result.Exception;
		}
		return JS_SDK_Handler_Result.UnSupport;
	}

	@Override
	public JS_SDK_Handler_Result js_SDK_Handler_AsyncStartAppDownload(final JsModel_AppItem item, final
	JsModel_CallBack_Item
			callback) {
		try {
			if (Util_System_Runtime.getInstance().runInUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						DefaultSDKApkDownloadManager.getInstance(getApplicationContext()).downloadApp(item, callback);
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			})) {
				return JS_SDK_Handler_Result.Success;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return JS_SDK_Handler_Result.Exception;
	}

	@Override
	public JS_SDK_Handler_Result js_SDK_Handler_AsyncResponseGwAppsLog(Context context, List<String> pns) {
		return JS_SDK_Handler_Result.UnSupport;
	}

	@Override
	public JS_SDK_Handler_Result js_Sdk_Handler_TrackEvent(String catalog, String operating, String label, long
			value) {
		return JS_SDK_Handler_Result.UnSupport;
	}

	@Override
	public JS_SDK_Handler_Result js_Sdk_Handler_TrackView(String view) {
		return JS_SDK_Handler_Result.UnSupport;
	}

	@Override
	public JS_SDK_Handler_Result js_Sdk_Handler_SetVisibility(int visibility) {
		return JS_SDK_Handler_Result.UnSupport;
	}

	@Override
	public JS_SDK_Handler_Result js_Sdk_Handler_SetVisibilityandPicType(int visibility, int picType, float
			proportion) {
		return JS_SDK_Handler_Result.UnSupport;
	}

	@Override
	public void onClick(View v) {
		if (v.equals(mRightBtn)) {
			if (isPayPage()) {
				showClosePayDialog(this);
			} else {
				this.finish();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			try {
				if (Build.VERSION.SDK_INT > 18) {
					return true;
				}
				if (mWebPage != null && mWebPage.canGoBack()) {
					mWebPage.goToBack();
					return true;
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		try {
			if (resultCode == Constant.RESULTCODE) {
				//来自汇付宝的activity的返回
				if (null != data) {
					data.setAction(JsHandler_Pay_With_WECHAT.WECHAT_PUKGUIN_PAYEND_ACTION);
					sendBroadcast(data);
				}
			} else {
				//来自UPMP的activity的返回
				String msg = "default";
				if (null != data) {
					msg = data.getExtras().getString("pay_result");
				}
				Intent intent = new Intent(JsHandler_Pay_With_UPMP.UPMP_PLUGIN_PAYEND_ACTION);
				intent.putExtra("msg", msg);
				sendBroadcast(intent);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		if (isPayPage() && mPayType == PAY_GAME) {
			ListenerManager.callbackPay(mPayCode);
		}
		if (mActionType != ACTION_DEFAULT) {
			ListenerManager.callbackAction(mActionType, mActionCode);
		}
		if (mWebPage != null) {
			mWebPage.exitBrowser();
			mWebPage.destroyBrowser();
		}
		super.onDestroy();
	}

	class UmipayWebViewClinet implements Interface_View_YoumiWebViewClient {

		@Override
		public void onWebPageStarted(WebView view, String url, Bitmap favicon) {
			Debug_Log.dd("onWebPageStarted : " + url);
		}

		@Override
		public void onWebReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			Debug_Log.dd("onWebReceivedError : " + failingUrl);
		}

		@Override
		public void onWebPageFinished(WebView view, String url) {
			Debug_Log.dd("onWebPageFinished : " + url);
		}
	}
}
