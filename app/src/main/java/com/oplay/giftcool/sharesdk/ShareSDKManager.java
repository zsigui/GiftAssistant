package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.ShareAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.sharesdk.base.IShare;
import com.oplay.giftcool.ui.fragment.dialog.ShareDialog;
import com.oplay.giftcool.util.BitmapUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;
import java.util.ArrayList;

/**
 * shareSDK管理类
 * Created by yxf on 14-11-7.
 * update by zsg on 16-1-24
 */
public class ShareSDKManager {

	private static ShareSDKManager mInstance;
	private Context mContext;
	private IWXAPI mWXApi;


	private ShareSDKManager(Context context) {
		mContext = context.getApplicationContext();
		mWXApi = WXAPIFactory.createWXAPI(mContext, ShareSDKConfig.SHARE_WEXIN_APP_ID, false);
		mWXApi.registerApp(ShareSDKConfig.SHARE_WEXIN_APP_ID);
	}

	public static synchronized ShareSDKManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ShareSDKManager(context);
		}
		return mInstance;
	}





	public void share(final FragmentManager fragmentManager, final Context context, final String dialog_title,
	                  final String title, final String description, final String b_desc, final String url,
	                  final String iconUrl, final Bitmap iconBitmap) {
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
//			shareList.add(new Share_More(context));
			final ShareDialog dialog = ShareDialog.newInstance(dialog_title);
			ShareAdapter adapter = new ShareAdapter(mContext, shareList, new OnItemClickListener<IShare>() {
				@Override
				public void onItemClick(IShare item, View view, int position) {
					dialog.dismissAllowingStateLoss();
					if (item instanceof Share_More) {
						item.share(title, b_desc, url, iconUrl, iconBitmap);
					} else {
						item.share(title, description, url, iconUrl, iconBitmap);
					}
				}
			});
			dialog.setAdapter(adapter);
			dialog.show(fragmentManager, ShareDialog.class.getSimpleName());
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public void shareGift(Context context, FragmentManager fm, IndexGiftNew gift) {
		// 设置分享成功后奖励类型
		String title;
		String b_desc;
		if (gift.giftType != GiftTypeUtil.GIFT_TYPE_NORMAL
				&& gift.giftType != GiftTypeUtil.GIFT_TYPE_NORMAL_FREE) {
			ScoreManager.getInstance().setRewardType(ScoreManager.RewardType.SHARE_LIMIT);
			title = String.format("[%s]%s(限今天)", gift.gameName, gift.name);
			b_desc = String.format("[%s]%s，价值珍贵，限量领取",
					gift.gameName, gift.name);
		} else {
			ScoreManager.getInstance().setRewardType(ScoreManager.RewardType.SHARE_NORMAL);
			title = String.format("[%s]%s", gift.gameName, gift.name);
			b_desc = String.format("[%s]%s，快抢呀",
					gift.gameName, gift.name);
		}

		String src = null;
		try {
			File file = ImageLoader.getInstance().getDiskCache().get(gift.img);
			src = (file != null ? file.getAbsolutePath() : null);
		} catch (Exception e) {
			// ImageLoader未初始化完成
		}
		ShareSDKManager.getInstance(context).share(fm,
				context,
				mContext.getResources().getString(R.string.st_dialog_gift_share_title),
				title,
				gift.content,
				b_desc,
				WebViewUrl.GIFT_DETAIL + "?plan_id=" + gift.id,
				gift.img, (src == null ? null : BitmapUtil.getSmallBitmap(src,
						ShareSDKConfig.THUMB_SIZE, ShareSDKConfig.THUMB_SIZE)));
	}

	public void shareGCool(final Context context, final FragmentManager fm) {
		ScoreManager.getInstance().setRewardType(ScoreManager.RewardType.SHARE_GCOOL);
		final String title = context.getString(R.string.st_dialog_invite_title);
		final String desc = context.getString(R.string.st_share_gcool_description_1);
		final String b_desc = context.getString(R.string.st_share_gcool_description_1);
		Resources res = context.getResources();
		final Bitmap icon =  BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
		ShareSDKManager.getInstance(context).share(fm,
				context,
				mContext.getResources().getString(R.string.st_dialog_gcool_share_title),
				title,
				desc,
				b_desc,
				WebViewUrl.getBaseUrl(),
				WebViewUrl.ICON_GCOOL, icon);
	}

	public IWXAPI getWXApi() {
		return mWXApi;
	}

}
