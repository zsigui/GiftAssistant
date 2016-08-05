package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.ShareAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.config.util.TaskTypeUtil;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.sharesdk.base.IShare;
import com.oplay.giftcool.ui.fragment.dialog.ShareDialog;
import com.oplay.giftcool.util.BitmapUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

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
                      final String iconUrl, final Bitmap iconBitmap, final int type) {
        try {
            AppDebugConfig.v(AppDebugConfig.TAG_SHARE, "\ntitle;" + title, "\ndescription" + description +
                    "\nurl:" + url, "\nimg:" + iconUrl, "\nIconBitmap:" + iconBitmap);
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
                        item.share(title, b_desc, url, iconUrl, iconBitmap, type);
                    } else {
                        item.share(title, description, url, iconUrl, iconBitmap, type);
                    }
                }
            });
            dialog.setAdapter(adapter);
            dialog.show(fragmentManager, ShareDialog.class.getSimpleName());
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_SHARE, e);
        }
    }

    public void shareGift(Context context, FragmentManager fm, IndexGiftNew gift) {
        ScoreManager.getInstance().setRewardCode(TaskTypeUtil.ID_GIFT_SHARE);
        shareGift(context, fm, gift, "share");
    }

    public void shareGift(Context context, FragmentManager fm, IndexGiftNew gift, String from) {
        // 设置分享成功后奖励类型
        String title;
        String b_desc;
        if (gift.giftType != GiftTypeUtil.GIFT_TYPE_NORMAL
                && gift.giftType != GiftTypeUtil.GIFT_TYPE_NORMAL_FREE) {
            title = String.format(
                    mContext.getResources().getString(R.string.st_share_gift_title_limit),
                    gift.gameName,
                    gift.name);
            b_desc = String.format(
                    mContext.getResources().getString(R.string.st_share_gift_desc_limit),
                    gift.gameName,
                    gift.name);
        } else {
            title = String.format(
                    mContext.getResources().getString(R.string.st_share_gift_title_normal),
                    gift.gameName,
                    gift.name);
            b_desc = String.format(
                    mContext.getResources().getString(R.string.st_share_gift_desc_normal),
                    gift.gameName,
                    gift.name);
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
                String.format(Locale.CHINA, "%s?plan_id=%d&from=%s", WebViewUrl.getWebUrl(WebViewUrl.GIFT_DETAIL),
                        gift.id, from),
                gift.img, (src == null ? null : BitmapUtil.getSmallBitmap(src,
                        ShareSDKConfig.THUMB_SIZE, ShareSDKConfig.THUMB_SIZE)), IShare.TYPE.WEB);
    }

    public void shareGCool(final Context context, final FragmentManager fm) {
        final String title = context.getString(R.string.st_dialog_invite_title);
        final String desc = context.getString(R.string.st_share_gcool_description_1);
        final String b_desc = context.getString(R.string.st_share_gcool_description_1);
        Resources res = context.getResources();
        final Bitmap icon = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
        ShareSDKManager.getInstance(context).share(fm,
                context,
                mContext.getResources().getString(R.string.st_dialog_gcool_share_title),
                title,
                desc,
                b_desc,
                WebViewUrl.getBaseUrl(),
                WebViewUrl.ICON_GCOOL, icon, IShare.TYPE.WEB);
    }

    public void shareActivity(final Context context, final FragmentManager fm, final IndexPostNew data) {
        ScoreManager.getInstance().setRewardCode(TaskTypeUtil.ID_ACTIVITY_SHARE);
        shareActivity(context, fm, data, "share");
    }

    public void shareActivity(final Context context, final FragmentManager fm, final IndexPostNew data, String from) {
        if (data == null) {
            ToastUtil.showShort(ConstString.TOAST_SHARE_ERROR);
            return;
        }
        String title = data.title;
        String desc = data.content;
        String b_desc = data.content;
        String icon = data.img;
        String shareUrl = String.format(Locale.CHINA, WebViewUrl.getWebUrl(WebViewUrl.ACTIVITY_DETAIL), data.id, from);
        Bitmap bitmap = null;

        if (TextUtils.isEmpty(title)) {
            title = context.getResources().getString(R.string.st_share_activity_title);
        }

        if (TextUtils.isEmpty(desc)) {
            b_desc = desc = context.getResources().getString(R.string.st_share_activity_desc);
        } else if (desc.length() > 20) {
            desc = desc.substring(0, 20) + "...";
        }

        if (TextUtils.isEmpty(icon)) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }
        ShareSDKManager.getInstance(context).share(fm,
                context,
                context.getResources().getString(R.string.st_dialog_activity_share_title),
                title,
                desc,
                b_desc,
                shareUrl,
                icon, bitmap, IShare.TYPE.WEB);
    }

    public IWXAPI getWXApi() {
        return mWXApi;
    }

}
