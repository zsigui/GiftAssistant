package net.ouwan.umipay.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipayActivity;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.global.Global_Url_Params;
import net.ouwan.umipay.android.handler.JsModel_Browser_PayExtent_Js_Interface_Factory;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_UrlCoder;
import net.youmi.android.libs.common.util.Util_System_Display;
import net.youmi.android.libs.common.util.Util_System_Runtime;
import net.youmi.android.libs.webjs.download.main.DefaultSDKApkDownloadManager;
import net.youmi.android.libs.webjs.js.JS_SDK_Handler_Result;
import net.youmi.android.libs.webjs.js.base.extendjs.JsModel_Browser_Target_Basic_Extend_Js_Interface_Factory;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.interfaces.Interface_Js_View_TitleSetable;
import net.youmi.android.libs.webjs.js.model.JsModel_AppItem;
import net.youmi.android.libs.webjs.js.model.JsModel_CallBack_Item;
import net.youmi.android.libs.webjs.view.webview.Flags_Browser_Config;
import net.youmi.android.libs.webjs.view.webview.View_SDKWebPage;
import net.youmi.android.libs.webjs.view.webview.interfaces.Interface_Default_SDK_Handler_Proxy;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import java.util.List;

/**
 * 官方公告页面
 * Created by jimmy on 2016/8/14.
 */
public class UmipayAnnouncementFragment extends BaseFragment implements Interface_Js_View_TitleSetable, Interface_SDK_Handler, View.OnClickListener {
	/**
	 * 对话框主体webpage
	 */
	View_SDKWebPage mWebViewPage;

	/**
	 * 外围布局
	 */
	private ViewGroup mRootLayout;
	private TextView mTitleTv;
	private Button mConfirmBtn;
	private ProgressBar mProgressBar;
	private int  mFlag = Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES;
	private String mUrl;
	private String mPostData;
	private String mAllPageJsCode = null;
	private String mAllPageLoadJsFileUrl = null;
	public static UmipayAnnouncementFragment newInstance() {
		UmipayAnnouncementFragment fragment = new UmipayAnnouncementFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof  UmipayActivity) {
			((UmipayActivity) activity).resize();
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// 初始化view
		initeViews();
		initListener();
		mUrl = SDKConstantConfig.get_UMIPAY_ANNOUNCEMENT_URL(getActivity());
		List<NameValuePair> paramsList = Global_Url_Params.getDefaultRequestParams(getActivity(),
				SDKConstantConfig.get_UMIPAY_ANNOUNCEMENT_URL(getActivity()));
		paramsList.add(new BasicNameValuePair("uid", Integer.toString(UmipayAccountManager.getInstance(getActivity()).getCurrentAccount().getUid())));
		mPostData = generatePostData(paramsList);
		if(mWebViewPage != null && !TextUtils.isEmpty(mUrl) && !TextUtils.isEmpty(mPostData)) {
			mWebViewPage.postUrl(mUrl, EncodingUtils.getBytes(mPostData, "BASE64"));
		}
		return mRootLayout;
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


	private void initeViews() {

		try {
			mRootLayout = (ViewGroup) ViewGroup.inflate(getActivity(), Util_Resource.getIdByReflection(getActivity(), "layout",

					"umipay_announcement_layout"), null);
			mTitleTv = (TextView) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_title_tv"));
			mProgressBar = (ProgressBar) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_announcement_dialog_progressbar"));
			mConfirmBtn = (Button) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_confirm_btn"));
			JsModel_Browser_Target_Basic_Extend_Js_Interface_Factory factory = new JsModel_Browser_PayExtent_Js_Interface_Factory();

			mWebViewPage = new View_SDKWebPage(getActivity(), this, mFlag, this,
					mAllPageJsCode, mAllPageLoadJsFileUrl, View_SDKWebPage.TYPE_SINGLE_MODE, factory) {
				@Override
				public void onWebProgressChanged(WebView view, final int newProgress) {
					Util_System_Runtime.getInstance().runInUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								if (mProgressBar != null) {
									if (newProgress <= 0 || newProgress >= 100) {
										mProgressBar.setVisibility(View.GONE);
									} else {
										mProgressBar.setVisibility(View.VISIBLE);
										mProgressBar.setProgress(newProgress);
									}
								}
							} catch (Throwable e) {
								Debug_Log.e(e);
							}
						}
					});
				}
			};
			if (mWebViewPage != null) {
				int padding  = Util_System_Display.dip2px(getActivity(),8);
				mWebViewPage.getCurrentView().setPadding(padding, 0, padding, 0);
				if (Build.VERSION.SDK_INT >= 19) {
					//android4.4以上webview硬件加速时会导致渲染异常，背景设置透明失效，先禁止硬件加速
					mWebViewPage.getCurrentView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
				}
			}

			if (mRootLayout != null && mRootLayout.getChildCount() > 2) {
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				lp.addRule(RelativeLayout.BELOW, mProgressBar.getId());
				lp.addRule(RelativeLayout.ABOVE, mConfirmBtn.getId());
				mRootLayout.addView(mWebViewPage.getCurrentView(), lp);
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}
	}

	private void initListener() {
		try {
			if (mConfirmBtn != null) {
				mConfirmBtn.setOnClickListener(this);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	@Override
	public void onDestroy() {
		if (mWebViewPage != null) {
			mWebViewPage.exitBrowser();
			mWebViewPage.destroyBrowser();
		}
		super.onDestroy();
	}

	@Override
	public void setWebTitle(String title) {
		try {
			if (mTitleTv != null && !TextUtils.isEmpty(title)) {
				mTitleTv.setText(title);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}



	@Override
	public Context getApplicationContext() {
		return getActivity().getApplicationContext();
	}


	@Override
	public String getWebAssetsRootPathUri() {
		//TODO
		return null;
	}

	@Override
	public void setSdkHandlerProxy(Interface_Default_SDK_Handler_Proxy interface_default_sdk_handler_proxy) {

	}

	@Override
	public JS_SDK_Handler_Result js_SDK_Handler_CloseCurrentWindow() {
		try {
			getActivity().finish();
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
	public JS_SDK_Handler_Result js_SDK_Handler_ReloadPage(){
		try {

			if(mWebViewPage != null && !TextUtils.isEmpty(mUrl) && !TextUtils.isEmpty(mPostData)) {
				mWebViewPage.postUrl(mUrl, EncodingUtils.getBytes(mPostData, "BASE64"));
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
	protected void handleOnClick(View v) {
		try {
			if (v.equals(mConfirmBtn)) {
				getActivity().finish();
				return;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

}
