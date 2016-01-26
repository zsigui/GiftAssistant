package com.oplay.giftcool.model.data.resp;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.ApkDownloadDir;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.util.InstallAppUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.util.Util_System_Intent;
import net.youmi.android.libs.common.util.Util_System_Package;
import net.youmi.android.libs.common.v2.network.NetworkStatus;

import java.io.File;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNew extends GameDownloadInfo {

	// 新增礼包数量
	@SerializedName("new_add_count")
	public int newCount;

	// 拥有礼包总数
	@SerializedName("has_gift_count")
	public int totalCount;

	// 在玩人数
	@SerializedName("plays")
	public int playCount;

	// 主推游戏Banner地址
	@SerializedName("stroll_img_url")
	public String banner;

	// 最新礼包名
	@SerializedName("gift_name")
	public String giftName;


}
