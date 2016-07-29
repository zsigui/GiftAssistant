package com.oplay.giftcool.sharesdk;

import android.content.Context;

import com.oplay.giftcool.R;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPI;

/**
 * 分享到微信朋友圈
 * Created by yxf on 14-11-7.
 */
public class Share_WX_MM extends Share_WX {


    public Share_WX_MM(Context context, IWXAPI api) {
        super(context, api, R.drawable.ic_share_mm_moments, context.getString(R.string.st_share_mm_moments));
    }

    @Override
    int getShareScene() {
        return SendMessageToWX.Req.WXSceneTimeline;
    }
}
