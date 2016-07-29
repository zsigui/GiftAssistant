package com.oplay.giftcool.sharesdk;

import android.content.Context;

import com.oplay.giftcool.R;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPI;

/**
 * 分享到微信朋友
 * Created by yxf on 14-11-7.
 * update by zsg on 16-1-21
 */
public class Share_WX_Friends extends Share_WX {

    public Share_WX_Friends(Context context, IWXAPI iwxapi) {
        super(context, iwxapi, R.drawable.ic_share_mm_friends, context.getString(R.string.st_share_mm_friends));
    }

    @Override
    int getShareScene() {
        return SendMessageToWX.Req.WXSceneSession;
    }
}
