package com.oplay.giftcool.config.util;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.model.data.resp.WebData;
import com.oplay.giftcool.model.data.resp.task.TaskInfoOne;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.postbar.PostFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-1-19.
 */
public class BannerTypeUtil {

    public static final int ACTION_DEFAULT = 0;
    public static final int ACTION_WEB = 1;
    public static final int ACTION_GAME_DETAIL = 2;
    public static final int ACTION_SCORE_TASK = 3;
    public static final int ACTION_GIFT_DETAIL = 4;
    public static final int ACTION_GAME_DETAIL_GIFT = 5;
    public static final int ACTION_JOIN_QQ_GROUP = 6;
    public static final int ACTION_POST = 7;
    public static final int ACTION_POST_DETAIL = 8;
    public static final int ACTION_UPGRADE = 9;
    public static final int ACTION_LIKE_AS_TASK = 11011;

    public static void handleBanner(Context context, IndexBanner banner) {
        if (banner == null || context == null) {
            return;
        }
        try {
            switch (banner.type) {
                case ACTION_WEB:
                    if (TextUtils.isEmpty(banner.extData)) {
                        return;
                    }
                    if (TextUtils.isEmpty(banner.title)) {
                        banner.title = context.getResources().getString(R.string.st_web_default_title_name);
                    }
                    WebData data = AssistantApp.getInstance().getGson().fromJson(banner.extData, WebData.class);
                    IntentUtil.jumpActivityWeb(context, data.url, banner.title);
                    break;
                case ACTION_GAME_DETAIL:
                    IndexGameNew game_d = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGameNew
                            .class);
                    IntentUtil.jumpGameDetail(context, game_d.id, GameTypeUtil.JUMP_STATUS_DETAIL);
                    break;
                case ACTION_SCORE_TASK:
                    if (!AccountManager.getInstance().isLogin()) {
                        IntentUtil.jumpLogin(context);
                        return;
                    }
                    IntentUtil.jumpEarnScore(context);
                    break;
                case ACTION_GIFT_DETAIL:
                    IndexGiftNew gift_o = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGiftNew
                            .class);
                    IntentUtil.jumpGiftDetail(context, gift_o.id);
                    break;
                case ACTION_GAME_DETAIL_GIFT:
                    IndexGameNew game_g = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGameNew
                            .class);
                    IntentUtil.jumpGameDetail(context, game_g.id, GameTypeUtil.JUMP_STATUS_GIFT);
                    break;
                case ACTION_JOIN_QQ_GROUP:
                    if (TextUtils.isEmpty(banner.extData)) {
                        return;
                    }
                    IntentUtil.joinQQGroup(context, banner.extData);
                    break;
                case ACTION_POST:
                    if (MainActivity.sGlobalHolder != null) {
                        MainActivity.sGlobalHolder.jumpToIndexPost(PostFragment.INDEX_HEADER);
                    }
                    break;
                case ACTION_POST_DETAIL:
                    IndexPostNew post_p = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexPostNew
                            .class);
                    IntentUtil.jumpPostDetail(context, post_p.id);
                    break;
                case ACTION_UPGRADE:
                    // 添加于1300
                    if (context instanceof FragmentActivity) {
                        final boolean isUpdate =
                                DialogManager.getInstance().showUpdateDialog(context,
                                        ((FragmentActivity) context).getSupportFragmentManager(), true);
                        if (!isUpdate) {
                            ToastUtil.showShort(context.getResources().getString(R.string.st_hint_upgrade_newest));
                        }
                    } else {
                        ToastUtil.showShort(context.getResources().getString(R.string.st_hint_unknown_failed));
                    }
                    break;
                case ACTION_LIKE_AS_TASK:
                    TaskInfoOne infoOne = AssistantApp.getInstance().getGson().fromJson(
                            banner.extData, TaskInfoOne.class);
                    IntentUtil.handleJumpInfo(context, infoOne);
                    break;
                case ACTION_DEFAULT:
                default:
                    ToastUtil.showShort(context.getResources().getString(R.string.st_hint_version_not_support));
                    if (context instanceof FragmentActivity) {
                        DialogManager.getInstance().showUpdateDialog(context,
                                ((FragmentActivity) context).getSupportFragmentManager(), true);
                    } else {
                        IntentUtil.jumpHome(context, KeyConfig.TYPE_ID_INDEX_UPGRADE, 0);
                    }
            }
        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_UTIL, t);
        }
    }
}
