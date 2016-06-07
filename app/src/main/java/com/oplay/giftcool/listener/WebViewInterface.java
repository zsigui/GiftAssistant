package com.oplay.giftcool.listener;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.JsonSyntaxException;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.sharesdk.ShareSDKManager;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.game.GameDetailFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.ui.fragment.postbar.PostCommentFragment;
import com.oplay.giftcool.ui.fragment.postbar.PostDetailFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import org.json.JSONObject;

import java.util.Observable;

import cn.finalteam.galleryfinal.GalleryFinal;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-14.
 */
public class WebViewInterface extends Observable {

    public static final int RET_SUCCESS = 0;
    public static final int RET_INTERNAL_ERR = 1;
    public static final int RET_PARAM_ERR = 2;
    public static final int RET_OTHER_ERR = 3;

    private FragmentActivity mHostActivity;
    private Fragment mHostFragment;
    private WebView mWebView;

    public WebViewInterface(FragmentActivity hostActivity, Fragment hostFragment, WebView webView) {
        mHostActivity = hostActivity;
        mHostFragment = hostFragment;
        mWebView = webView;
    }

    @JavascriptInterface
    public int jumpToGift(int id) {
        if (id <= 0) {
            return RET_PARAM_ERR;
        }
        IntentUtil.jumpGiftDetail(mHostActivity, id);
        return RET_SUCCESS;
    }

    @JavascriptInterface
    public int jumpToGame(int id) {
        if (id <= 0) {
            return RET_PARAM_ERR;
        }
        IntentUtil.jumpGameDetail(mHostActivity, id);
        return RET_SUCCESS;
    }

    @JavascriptInterface
    public int seizeGiftCode(String giftJson) {
        if (TextUtils.isEmpty(giftJson)) {
            return RET_PARAM_ERR;
        }
        try {
            IndexGiftNew gift = AssistantApp.getInstance().getGson().fromJson(giftJson, IndexGiftNew.class);
            return PayManager.getInstance().seizeGift(mHostActivity, gift, null);
        } catch (JsonSyntaxException e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(AppDebugConfig.TAG_WEBVIEW, e);
            }
        }
        return RET_INTERNAL_ERR;
    }

    @JavascriptInterface
    public int setDownloadBtn(boolean isShow, String params) {
        try {
            if (mHostActivity == null || mHostFragment == null || !(mHostFragment instanceof GameDetailFragment)) {
                return RET_INTERNAL_ERR;
            }
            IndexGameNew appInfo = null;
            if (isShow) {
                try {
                    appInfo = AssistantApp.getInstance().getGson().fromJson(params, IndexGameNew.class);
                } catch (Throwable e) {
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.e(e);
                    }
                }
                if (appInfo == null || !appInfo.isValid()) {
                    return RET_PARAM_ERR;
                }
            }
            if (mHostFragment instanceof ShowBottomBarListener) {
                ((ShowBottomBarListener) mHostFragment).showBar(isShow, appInfo);
                return RET_SUCCESS;
            } else {
                return RET_OTHER_ERR;
            }
        } catch (Throwable e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(e);
            }
            return RET_INTERNAL_ERR;
        }
    }

    @JavascriptInterface
    public int login(int loginType) {
        if (loginType != KeyConfig.TYPE_ID_OUWAN_LOGIN && loginType != KeyConfig.TYPE_ID_PHONE_LOGIN) {
            loginType = KeyConfig.TYPE_ID_PHONE_LOGIN;
        }
        try {
            IntentUtil.jumpLogin(mHostActivity, loginType);
        } catch (Exception e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.d(AppDebugConfig.TAG_WEBVIEW, e);
            }
            return RET_INTERNAL_ERR;
        }
        return RET_SUCCESS;
    }

    @JavascriptInterface
    public int shareGift(String giftJson) {
        try {
            if (mHostActivity == null || mHostFragment == null) {
                return RET_INTERNAL_ERR;
            }
            try {
                IndexGiftNew gift = AssistantApp.getInstance().getGson().fromJson(giftJson, IndexGiftNew.class);
                if (gift.status == GiftTypeUtil.STATUS_FINISHED) {
                    return RET_PARAM_ERR;
                }
                ShareSDKManager.getInstance(mHostActivity).shareGift(mHostActivity,
                        mHostFragment.getChildFragmentManager(), gift);
            } catch (Throwable e) {
                return RET_PARAM_ERR;
            }
            return RET_SUCCESS;
        } catch (Throwable e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(e);
            }
            return RET_INTERNAL_ERR;
        }
    }

    @JavascriptInterface
    public int shareGCool() {
        try {
            if (mHostActivity == null || mHostFragment == null) {
                return RET_INTERNAL_ERR;
            }
            ShareSDKManager.getInstance(mHostActivity).shareGCool(mHostActivity, mHostFragment.getChildFragmentManager
                    ());
            return RET_SUCCESS;
        } catch (Throwable e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(e);
            }
            return RET_INTERNAL_ERR;
        }
    }

    /**
     * 根据类型跳转对应列表界面
     */
    @JavascriptInterface
    public int jumpByType(int type) {
        if (type > 10 || type < 0) {
            return RET_PARAM_ERR;
        }
        try {
            switch (type) {
                case 0:
                    IntentUtil.jumpGiftNewList(mHostActivity);
                    break;
                case 1:
                    IntentUtil.jumpGiftLimitList(mHostActivity, false);
                    break;
                case 2:
                    IntentUtil.jumpGiftHotList(mHostActivity, "");
                    break;
                case 3:
                    IntentUtil.jumpGameHotList(mHostActivity);
                    break;
                case 4:
                    IntentUtil.jumpGameNewList(mHostActivity);
                    break;
                case 5:
                    IntentUtil.jumpLogin(mHostActivity);
                    break;
                // 以下几个需要登录
                case 6:
                    IntentUtil.jumpMyGift(mHostActivity);
                    break;
                case 7:
                    IntentUtil.jumpEarnScore(mHostActivity);
                    break;
                case 8:
                    IntentUtil.jumpMyWallet(mHostActivity);
                    break;
                case 9:
                    IntentUtil.jumpFeedBack(mHostActivity);
                    break;
                case 10:
                    // 分享普通礼包
                    if (MainActivity.sGlobalHolder == null) {
                        IntentUtil.jumpGiftNewList(mHostActivity);
                    } else {
                        MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_NEW);
                        mHostActivity.finish();
                    }
                    break;
            }
            return RET_SUCCESS;
        } catch (Throwable e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.d(AppDebugConfig.TAG_WEBVIEW, e);
            }
            return RET_INTERNAL_ERR;
        }
    }

    /**
     * 跳转活动评论页面
     */
    @JavascriptInterface
    public int jumpCommentDetail(int postId, int commentId) {
        if (commentId == 0) {
            return RET_PARAM_ERR;
        }
        IntentUtil.jumpPostReplyDetail(mHostActivity, postId, commentId);
        return RET_SUCCESS;
    }

    /**
     * 显示多张预览图片
     *
     * @param selectedIndex 选择最初显示图片的下标，从0开始
     * @param picsPath      传入图片地址的字符串数组
     */
    @JavascriptInterface
    public int showMultiPic(int selectedIndex, String... picsPath) {
        int ret = GalleryFinal.openMultiPhoto(selectedIndex, picsPath);
        switch (ret) {
            case GalleryFinal.Error.RET_INIT_FAIL:
                return RET_INTERNAL_ERR;
            case GalleryFinal.Error.RET_NO_SELECTED_PHOTO:
                return RET_PARAM_ERR;
            case GalleryFinal.Error.SUCCESS:
                return RET_SUCCESS;
            default:
                return RET_OTHER_ERR;
        }
    }

    /**
     * 显示底部的回复栏
     */
    @JavascriptInterface
    public int showBottomBar(final boolean isShow) {
        if (mHostFragment == null) {
            return RET_INTERNAL_ERR;
        }
        if (mHostFragment instanceof ShowBottomBarListener) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ShowBottomBarListener) mHostFragment).showBar(isShow, null);
                }
            });
            return RET_SUCCESS;
        } else {
            return RET_OTHER_ERR;
        }
    }

    /**
     * 获取APP的启动时间
     */
    @JavascriptInterface
    public long getLastLaunchTimeInMilli() {
        return AssistantApp.getInstance().getLastLaunchTime();
    }

    /**
     * 显示关注游戏的引导页
     */
    @JavascriptInterface
    public int showFocusGameGuidePage() {
        if (mHostActivity != null) {
            DialogManager.getInstance().showGuidePage(mHostActivity);
            return RET_SUCCESS;
        }
        return RET_INTERNAL_ERR;
    }

    private Call<Object> mCall;

    /**
     * 进行异步网络请求
     */
    @JavascriptInterface
    public int asyncNativeRequest(final String reqUrl, final String reqParam,
                                  final String reqMethod, final String callbackJsName) {
        if (TextUtils.isEmpty(reqUrl) || TextUtils.isEmpty(reqParam)) {
            return RET_PARAM_ERR;
        }
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {


                if (!NetworkUtil.isConnected(mHostActivity)) {
                    execJs(callbackJsName, initJsonError(NetStatusCode.ERR_NETWORK, "网络异常"));
                    return;
                }
                try {
                    JSONObject realReqParam = new JSONObject(reqParam);
                    Object a = AssistantApp.getInstance().getGson().fromJson(realReqParam.toString(), Object.class);
                    if (mCall != null) {
                        mCall.cancel();
                    }
                    if (TextUtils.isEmpty(reqMethod) || reqMethod.equalsIgnoreCase("POST")) {
                        mCall = Global.getNetEngine().asyncPostForJsCall(reqUrl, new JsonReqBase<Object>(a));
                    } else {
                        mCall = Global.getNetEngine().asyncGetForJsCall(reqUrl, new JsonReqBase<Object>(a));
                    }
                    mCall.enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            if (call.isCanceled()) {
                                return;
                            }
                            if (response == null) {
                                execJs(callbackJsName, initJsonError(NetStatusCode.ERR_EMPTY_RESPONSE, "response为空"));
                                return;
                            }
                            if (!response.isSuccessful()) {
                                execJs(callbackJsName, initJsonError(response.code(), response.message()));
                                return;
                            }
                            String s = AssistantApp.getInstance().getGson().toJson(response.body());
                            execJs(callbackJsName, s);
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            if (call.isCanceled()) {
                                return;
                            }
                            String returnData = initJsonError(NetStatusCode.ERR_EXEC_FAIL, t.getMessage());
                            execJs(callbackJsName, returnData);
                        }
                    });
                } catch (Exception e) {
                    if (AppDebugConfig.IS_DEBUG) {
                        e.printStackTrace();
                    }
                    execJs(callbackJsName, initJsonError(NetStatusCode.ERR_EXEC_FAIL, "执行异常"));
                }
            }
        });
        return RET_SUCCESS;
    }

    /**
     * 设置回复谁
     *
     * @param commentId 回复对象ID
     * @param name      回复对象名称
     * @return
     */
    @JavascriptInterface
    public int setReplyTo(final int commentId, final String name) {
        if (mHostFragment != null) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mHostFragment instanceof PostCommentFragment) {
                        PostCommentFragment fragment = (PostCommentFragment) mHostFragment;
                        fragment.setReplyTo(commentId, name);
                    } else if (mHostFragment instanceof PostDetailFragment) {
                        ((PostDetailFragment) mHostFragment).toReply();
                    }
                }
            });
            return RET_SUCCESS;
        }
        return RET_OTHER_ERR;
    }

    /**
     * 执行Js操作
     */
    private void execJs(final String callbackJsName, final String returnData) {
        if (mWebView == null) {
            ToastUtil.showShort(ConstString.TEXT_EXECUTE_ERROR);
            return;
        }
        if (callbackJsName != null) {
            // WebView执行需要在主线程中
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    String js = String.format("%s('%s')", callbackJsName, returnData);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                        mWebView.loadUrl("javascript:" + js);
                    } else {
                        mWebView.evaluateJavascript(js, null);
                    }
                }
            });
        }
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
    }

    /**
     * 构造空数据的错误返回Json字符串
     */
    private String initJsonError(int code, String msg) {
        JsonRespBase<Void> result = new JsonRespBase<Void>();
        result.setCode(code);
        result.setMsg(msg);
        return AssistantApp.getInstance().getGson().toJson(result);
    }
}
