package net.ouwan.umipay.android.view;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipayBrowser;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.global.Global_Url_Params;
import net.ouwan.umipay.android.manager.FloatmenuCacheManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.youmi.android.libs.common.util.Util_System_Display;
import net.youmi.android.libs.webjs.view.webview.Flags_Browser_Config;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;


/**
 * Created by mink on 15-11-5.
 */
public class FloatMenuTabButton extends FrameLayout {

	public static final int MSG = 0;
	public static final int ACCOUNT = 1;
	public static final int GIFT = 2;
	public static final int BBS = 3;
	public static final int HELP = 4;

	public static final int[] BTNTYPE = {MSG, ACCOUNT, GIFT, BBS, HELP};

	public static final String[] DEST = {"message", "account", "giftbag", "community", "kfhelp"};

	private static final String[] ICON_DRAWABLE_ID = {"umipay_tab_icon_msg", "umipay_tab_icon_account",
			"umipay_tab_icon_present",
			"umipay_tab_icon_bbs", "umipay_tab_icon_help"};
	private static final String[] ICON_STRING_ID = {"umipay_msg", "umipay_account", "umipay_present",
			"umipay_bbs", "umipay_help"};

	private String mBtnUrl = "";
	private int mBtnType = -1;
	private int mBubble = 0;

	private Context mContext;
	private View mRootLayout;
	private ImageView icon_iv;
	private TextView btn_tv;
	private TextView bubble_tv;
//    private View mDivider;

	public FloatMenuTabButton(Context context, int btnType) {
		super(context);
		mContext = context;
		mBtnType = btnType;
		mBtnUrl = SDKConstantConfig.get_UMIPAY_JUMP_URL(mContext);
		initResource();
	}

	private void initResource() {
		if (mContext == null) {
			return;
		}
		try {
			mRootLayout = inflate(mContext, Util_Resource.getIdByReflection(mContext, "layout",
					"umipay_floatmenu_button_layout"), this);
			icon_iv = (ImageView) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
					"umipay_tab_btn_icon"));

			btn_tv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
					"umipay_tab_btn_text"));

			bubble_tv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
					"umipay_tab_bubble"));


			if (mBtnType > -1 && mBtnType < ICON_DRAWABLE_ID.length && mBtnType < ICON_STRING_ID.length) {
				icon_iv.setBackgroundResource(Util_Resource.getIdByReflection(mContext,
						"drawable",
						ICON_DRAWABLE_ID[mBtnType]));
				btn_tv.setText(Util_Resource.getIdByReflection(mContext, "string",
						ICON_STRING_ID[mBtnType]));
			}
			if (mBtnType == BBS || mBtnType == ACCOUNT || mBtnType == GIFT) {
				FrameLayout.LayoutParams layoutParams = (LayoutParams) bubble_tv.getLayoutParams();
				layoutParams.width = Util_System_Display.dip2px(mContext, 8);
				layoutParams.height = Util_System_Display.dip2px(mContext, 8);
				layoutParams.setMargins(0, Util_System_Display.dip2px(mContext, 3), 0, 0);
				bubble_tv.setLayoutParams(layoutParams);
				bubble_tv.setText("");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

	}

	public int getType() {
		return mBtnType;
	}

	/**
	 * 更新按钮（UI线程中调用）
	 */
	public void update() {
		if (mContext == null) {
			return;
		}
		try {
			int visibility = GONE;
			int bubble_visibility = GONE;

			switch (mBtnType) {
				case MSG:
					visibility = (SDKCacheConfig.getInstance(mContext).isShowMsg()) ? VISIBLE : GONE;
					mBubble = FloatmenuCacheManager.getInstance(mContext).getNoticeBubbleNum() + FloatmenuCacheManager
							.getInstance(mContext).getBoardBubbleNum();
					bubble_visibility =  (mBubble> 0) ? View.VISIBLE : GONE;
					if (bubble_tv != null) {
						bubble_tv.setText("" + mBubble);
					}
					break;

				case ACCOUNT:
					visibility = (SDKCacheConfig.getInstance(mContext).isShowAccount()) ? VISIBLE : GONE;
					bubble_visibility = (FloatmenuCacheManager.getInstance(mContext).getTrumpetBubbleNum() > 0) ?
							View.VISIBLE : GONE;
					break;

				case BBS:
					visibility = (SDKCacheConfig.getInstance(mContext).isShowBbs()) ? VISIBLE : GONE;
					bubble_visibility = (SDKCacheConfig.getInstance(mContext).isEnableBbsRedPoint()) ? View.VISIBLE :
							View.GONE;
					break;

				case GIFT:
					visibility = (SDKCacheConfig.getInstance(mContext).isShowGift()) ? VISIBLE : GONE;
					bubble_visibility = (FloatmenuCacheManager.getInstance(mContext).getGiftBubbleNum() > 0) ? View
							.VISIBLE : GONE;
					break;
				case HELP:
					visibility = (SDKCacheConfig.getInstance(mContext).isShowHelp()) ? VISIBLE : GONE;
					break;
				default:
					break;
			}

			bubble_tv.setVisibility(bubble_visibility);
			setVisibility(visibility);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public void jump() {
		if (UmipayAccountManager.getInstance(mContext).isLogin() != true) {
			return;
		}
		// 显示跳转页面
		try {
			String url = mBtnUrl;
			url = SDKConstantConfig.get_UMIPAY_JUMP_URL(mContext);
			List<NameValuePair> paramsList = Global_Url_Params.getDefaultRequestParams(mContext,
					SDKConstantConfig.get_UMIPAY_ACCOUNT_URL(mContext));
			String title = getContext().getString(Util_Resource.getIdByReflection(mContext, "string",
					ICON_STRING_ID[mBtnType]));
			int payType = UmipayBrowser.NOT_PAY;
			paramsList.add(new BasicNameValuePair("dest", DEST[mBtnType]));
			UmipayBrowser
					.postUrl(
							mContext,
							title,
							url,
							paramsList,
							Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE
									| Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES,
							null, null, payType
					);
			if (mBtnType == ACCOUNT || mBtnType == BBS || mBtnType == GIFT) {
				//打开过一次之后不再显示论坛上的小红点
				if (mBtnType == BBS) {
					SDKCacheConfig.getInstance(mContext).setEnableBbsRedPoint(false);
				}
				if (mBtnType == ACCOUNT) {
					FloatmenuCacheManager.getInstance(mContext).consume(FloatmenuCacheManager.TYPE_TRUMPET, "");
				}
				if (mBtnType == GIFT) {
					FloatmenuCacheManager.getInstance(mContext).consume(FloatmenuCacheManager.TYPE_GIFT, "");
				}
				bubble_tv.setVisibility(GONE);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

	}
}
