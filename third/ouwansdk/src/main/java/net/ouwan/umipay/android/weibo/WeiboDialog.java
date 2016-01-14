package net.ouwan.umipay.android.weibo;

/**
 * Created by mink on 15-12-15.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;

import net.ouwan.umipay.android.Utils.Util_SinaUtility;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.debug.Debug_Log;


public class WeiboDialog extends Dialog{
	static LayoutParams FILL = new LayoutParams(-1, -1);


	private Context mContext;
	private String mUrl;
	private WeiboAuthListener mListener;
	private ProgressDialog mSpinner;
	private WebView mWebView;
	private LinearLayout webViewContainer;
	private LinearLayout mContent;
	private static final String TAG = "Weibo-WebView";
	private static int theme = 16973840;

	public WeiboDialog(Context context, String url, WeiboAuthListener listener) {
		super(context, theme);
		mContext = context;
		mUrl = url;
		mListener = listener;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mSpinner = new ProgressDialog(this.getContext());
		this.mSpinner.requestWindowFeature(1);
		this.mSpinner.setMessage("Loading...");
		this.mSpinner.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				WeiboDialog.this.onBack();
				return false;
			}
		});
		this.setCanceledOnTouchOutside(false);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFeatureDrawableAlpha(0, 0);
		LinearLayout root = new LinearLayout(this.getContext());
		this.mContent = root;
		this.setUpWebView();
		this.addContentView(this.mContent, new android.view.ViewGroup.LayoutParams(-1,-1));
	}


	protected void onBack() {
		try {
			this.mSpinner.dismiss();
			if(this.mWebView != null) {
				this.mWebView.stopLoading();
				this.mWebView.destroy();
			}
		} catch (Exception var2) {
			;
		}

		this.dismiss();
	}

	private void setUpWebView() {
		this.webViewContainer = new LinearLayout(this.getContext());
		this.mWebView = new WebView(this.getContext());
		this.mWebView.setVerticalScrollBarEnabled(false);
		this.mWebView.setHorizontalScrollBarEnabled(false);
		this.mWebView.getSettings().setJavaScriptEnabled(true);
		this.mWebView.setWebViewClient(new WeiboDialog.WeiboWebViewClient());
		this.mWebView.loadUrl(this.mUrl);
		this.mWebView.setLayoutParams(FILL);
		this.mWebView.setVisibility(4);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
		LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(-1, -1);
		this.mContent.setBackgroundColor(0);

		this.webViewContainer.addView(this.mWebView, lp0);
		this.webViewContainer.setGravity(17);
		this.mContent.addView(this.webViewContainer, lp);
	}
	public void handleRedirectUrl(String url) {
		try {
			Bundle values = Util_SinaUtility.parseUrl(url);
			String error = values.getString("error");
			String error_code = values.getString("error_code");
			if(mListener != null) {
				if (error == null && error_code == null) {
					mListener.onComplete(values);
				} else if (error.equals("access_denied")) {
					mListener.onCancel();
				} else if(error_code == null) {
					mListener.onWeiboException(new WeiboException(error, 0));
				} else {
					mListener.onWeiboException(new WeiboException(error, Integer.parseInt(error_code)));
				}
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}

	}

	private class WeiboWebViewClient extends WebViewClient {
		private WeiboWebViewClient() {
		}

		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d("Weibo-WebView", "Redirect URL: " + url);
			if(url.startsWith("sms:")) {
				Intent sendIntent = new Intent("android.intent.action.VIEW");
				sendIntent.putExtra("address", url.replace("sms:", ""));
				sendIntent.setType("vnd.android-dir/mms-sms");
				WeiboDialog.this.getContext().startActivity(sendIntent);
				return true;
			} else {
				return super.shouldOverrideUrlLoading(view, url);
			}
		}

		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			WeiboDialog.this.mListener.onError(new WeiboDialogError(description, errorCode, failingUrl));
			WeiboDialog.this.dismiss();
		}

		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.d("Weibo-WebView", "onPageStarted URL: " + url);
			if(url.startsWith(SDKConstantConfig.SINA_REDIRECT_URL)) {
				WeiboDialog.this.handleRedirectUrl(url);
				view.stopLoading();
				WeiboDialog.this.dismiss();
			} else {
				super.onPageStarted(view, url, favicon);
				WeiboDialog.this.mSpinner.show();
			}
		}

		public void onPageFinished(WebView view, String url) {
			Log.d("Weibo-WebView", "onPageFinished URL: " + url);
			super.onPageFinished(view, url);
			if(WeiboDialog.this.mSpinner.isShowing()) {
				WeiboDialog.this.mSpinner.dismiss();
			}

			WeiboDialog.this.mWebView.setVisibility(0);
		}

		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}
	}

}

