package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.ShareAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.sharesdk.base.IShare;
import com.oplay.giftcool.ui.fragment.dialog.ShareDialog;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.ArrayList;

/**
 * shareSDK管理类
 * Created by yxf on 14-11-7.
 */
public class ShareSDKManager {

	private static ShareSDKManager mInstance;
	private Context mContext;
	private IWXAPI mWXApi;


	private ShareSDKManager(Context context) {
		mContext = context.getApplicationContext();
		final String k = new String(Base64.decode(context.getString(R.string.share_key_mm_id).getBytes(),
				Base64.DEFAULT));
		mWXApi = WXAPIFactory.createWXAPI(mContext, k);
		mWXApi.registerApp(k);
	}

	public static synchronized ShareSDKManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ShareSDKManager(context);
		}
		return mInstance;
	}

	public void share(final FragmentManager fragmentManager, final String title, final String description,
	                  final String url, final String iconUrl, final Bitmap iconBitmap) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams(this, "\ntitle;" + title, "\ndescription" + description +
						"\nurl:" + url, "\nimg:" + iconUrl, "\nIconBitmap:" + iconBitmap);
			}
			ArrayList<IShare> shareList = new ArrayList<IShare>();
			shareList.add(new Share_WX_MM(mContext, mWXApi));
			shareList.add(new Share_WX_Friends(mContext, mWXApi));
			shareList.add(new Share_QQ_Friends(mContext));
			shareList.add(new Share_QQ_Zone(mContext));
			shareList.add(new Share_More(mContext));
			final ShareDialog dialog = ShareDialog.newInstance(title);
			ShareAdapter adapter = new ShareAdapter(mContext, shareList, new OnItemClickListener<IShare>() {
				@Override
				public void onItemClick(IShare item, View view, int position) {
					dialog.dismissAllowingStateLoss();
					item.share(title, description, url, iconUrl, iconBitmap);
				}
			});
			dialog.setAdapter(adapter);
			dialog.show(fragmentManager, "share");
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public void shareOuwan(final FragmentManager fragmentManager) {
		try {
			ArrayList<IShare> shareList = new ArrayList<IShare>();
			shareList.add(new Share_WX_MM(mContext, mWXApi));
			shareList.add(new Share_WX_Friends(mContext, mWXApi));
			shareList.add(new Share_QQ_Friends(mContext));
			shareList.add(new Share_QQ_Zone(mContext));
			final ShareDialog dialog = ShareDialog.newInstance(mContext.getString(R.string.st_dialog_gcool_share_title));
			ShareAdapter adapter = new ShareAdapter(mContext, shareList, new OnItemClickListener<IShare>() {
				@Override
				public void onItemClick(IShare item, View view, int position) {
					final String title = mContext.getString(R.string.st_dialog_gcool_share_title);
					String description = null;
					final String url = NetUrl.GIFT_COOL_DOWNLOAD;
					Resources res = mContext.getResources();
					Bitmap bmp = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
					switch (item.getShareType()) {
						case WX_FRIENDS:
						case QQ_FRIENDS:
						case QQ_ZONE: {
							description = mContext.getString(R.string.share_gcool_description_1);
							break;
						}
						case WX_MM: {
							description = mContext.getString(R.string.share_gcool_description_2);
							break;
						}
					}
					dialog.dismissAllowingStateLoss();
					item.share(title, description, url, null, bmp);
				}
			});
			dialog.setAdapter(adapter);
			dialog.show(fragmentManager, "share");
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public IWXAPI getWXApi() {
		return mWXApi;
	}

}
