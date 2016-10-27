package com.oplay.giftcool.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.databinding.FragmentMyBinding;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.StringUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 16-10-26.
 */
public class MyFragment extends BaseFragment implements ObserverManager.UserUpdateListener {

    private FragmentMyBinding mBinding;

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.fragment_my, null, false);
        mContentView = mBinding.getRoot();
    }

    @Override
    protected void setListener() {
        mBinding.llUserInfo.setOnClickListener(this);
        mBinding.llWallet.setOnClickListener(this);
        mBinding.llMyGift.setOnClickListener(this);
        mBinding.llMyCoupon.setOnClickListener(this);
        mBinding.llMyAttention.setOnClickListener(this);
        mBinding.llMsg.setOnClickListener(this);
        mBinding.llDownload.setOnClickListener(this);
        mBinding.llSetting.setOnClickListener(this);
        ObserverManager.getInstance().addUserUpdateListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
    }

    @Override
    protected void lazyLoad() {
        updateTotal();
    }

    @Override
    public void onUserUpdate(final int action) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (action) {
                    case ObserverManager.STATUS.USER_UPDATE_ALL:
                        updateTotal();
                        break;
                    case ObserverManager.STATUS.USER_UPDATE_PART:
                        updateMoney(AccountManager.getInstance().getUserInfo());
                        break;
                    case ObserverManager.STATUS.USER_UPDATE_PUSH_MESSAGE:
                        updateMsgHint(AccountManager.getInstance().getUnreadMessageCount());
                        break;
                }
            }
        });
    }

    /**
     * 更新‘设置界面用户信息’信息 <br />
     *
     * P.S. 需要在主线程执行
     */
    private void updateTotal() {
        if (mBinding == null) {
            return;
        }
        boolean isLogin = AccountManager.getInstance().isLogin();
        if (AccountManager.getInstance().isLogin()) {
            UserInfo info = AccountManager.getInstance().getUserInfo();
            updateMoney(info);
            String nick;
            String name;
            if (info.bindOuwanStatus == 0) {
                nick = (TextUtils.isEmpty(info.nick) ? StringUtil.transePhone(info.phone) : info.nick);
                name = "登录手机：" + StringUtil.transePhone(info.phone);
            } else {
                nick = (TextUtils.isEmpty(info.nick) ? info.username : info.nick);
                name = "偶玩账号：" + info.username;
            }
            ViewUtil.showAvatarImage(info.avatar, mBinding.ivAvatar, isLogin);
            mBinding.tvName.setText(name);
            mBinding.tvNick.setText(nick);
        } else {
            mBinding.ivAvatar.setImageResource(R.drawable.ic_avatar_un_login);
        }
        mBinding.tvUnLogin.setVisibility(isLogin ? View.GONE : View.VISIBLE);
        mBinding.llName.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        mBinding.llWallet.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        mBinding.llMoney.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        updateMsgHint(AccountManager.getInstance().getUnreadMessageCount());
    }

    /**
     * 更新‘个人金币和偶玩豆’信息 <br />
     *
     * P.S. 需要在主线程执行
     */
    private void updateMoney(UserInfo info) {
        if (mBinding == null) {
            return;
        }
        if (info != null) {
            mBinding.tvBean.setText(String.valueOf(info.bean));
            mBinding.tvScore.setText(String.valueOf(info.score));
        }
    }

    /**
     * 更新‘我的消息’块小红点提示 <br />
     *
     * P.S. 需要在主线程执行
     */
    public void updateMsgHint(int count) {
        if (mBinding == null) {
            return;
        }
        if (count == 0) {
            mBinding.tvMsgHint.setVisibility(View.GONE);
        } else {
            mBinding.tvMsgHint.setVisibility(View.VISIBLE);
            mBinding.tvMsgHint.setText(String.valueOf(count));
        }
    }

    /**
     * 更新‘下载管理’块小红点提示 <br />
     *
     * P.S. 需要在主线程执行
     */
    public void updateDownloadHint(int count) {
        if (mBinding == null) {
            return;
        }
        if (count == 0) {
            mBinding.tvDownloadHint.setVisibility(View.GONE);
        } else {
            mBinding.tvDownloadHint.setVisibility(View.VISIBLE);
            mBinding.tvDownloadHint.setText(String.valueOf(count));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        boolean isLogin = AccountManager.getInstance().isLogin();
        switch (v.getId()) {
            case R.id.ll_download:
                IntentUtil.jumpDownloadManager(getContext());
                return;
            case R.id.ll_setting:
                IntentUtil.jumpSetting(getContext());
                return;
        }
        if (isLogin) {
            switch (v.getId()) {
                case R.id.ll_user_info:
                    IntentUtil.jumpUserInfo(getContext());
                    break;
                case R.id.ll_my_gift:
                    IntentUtil.jumpMyGift(getContext());
                    break;
                case R.id.ll_my_coupon:
                    IntentUtil.jumpMyCoupon(getContext());
                    break;
                case R.id.ll_my_attention:
                    IntentUtil.jumpMyAttention(getContext());
                    break;
                case R.id.ll_msg:
                    IntentUtil.jumpMessageCentral(getContext());
                    AccountManager.getInstance().setUnreadMessageCount(0);
                    if (MainActivity.sGlobalHolder != null) {
                        MainActivity.sGlobalHolder.updateHintState(KeyConfig.TYPE_ID_MSG, 0);
                    }
                    break;
                case R.id.ll_wallet:
                    IntentUtil.jumpMyWallet(getContext());
                    break;
            }
        } else {
            IntentUtil.jumpLoginNoToast(getContext());
        }

    }

    @Override
    public void release() {
        super.release();
        mBinding = null;
    }

    @Override
    public String getPageName() {
        return "我的";
    }

    public static MyFragment newInstance() {
        return new MyFragment();
    }
}
