package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.graphics.Bitmap;

import com.oplay.giftcool.R;
import com.oplay.giftcool.sharesdk.base.IShare;
import com.oplay.giftcool.util.ShareUtil;


/**
 * @author CsHeng
 * @Date 14-11-7
 * update by zsg 16-1-21
 */
public class Share_More extends IShare {

	public Share_More(Context context) {
		super(context, R.drawable.ic_share_more, context.getString(R.string.st_share_more));
	}


	public void share(String title, String description, String url, String iconUrl, Bitmap icon, int type) {
		ShareUtil.shareText(mContext, title, description + " " + url);
	}

	@Override
	public boolean isSupport() {
		return true;
	}
}