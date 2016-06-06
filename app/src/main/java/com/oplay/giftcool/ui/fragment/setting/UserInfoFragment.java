package com.oplay.giftcool.ui.fragment.setting;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.util.UserTypeUtil;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.StringUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-17.
 */
public class UserInfoFragment extends BaseFragment implements ObserverManager.UserActionListener {

    private final static String PAGE_NAME = "用户信息";
    private RelativeLayout rlAvatar;
    private LinearLayout llNick;
    private LinearLayout llLogin;
    private LinearLayout llBind;
    private ImageView ivLogin;
    private ImageView ivBind;
    private RelativeLayout rlModifyPwd;
    private ImageView ivIcon;
    private TextView tvNick;
    private TextView tvLoginTitle;
    private TextView tvLogin;
    private TextView tvBindTitle;
    private TextView tvBind;
    private TextView tvHint;
    private Context mContext = AssistantApp.getInstance().getApplicationContext();

    public static UserInfoFragment newInstance() {
        return new UserInfoFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        if (!AccountManager.getInstance().isLogin()) {
            ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
            IntentUtil.jumpLogin(getContext());
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }
        setContentView(R.layout.fragment_user_info);
        rlAvatar = getViewById(R.id.rl_avatar);
        llNick = getViewById(R.id.ll_nick);
        llLogin = getViewById(R.id.rl_login);
        llBind = getViewById(R.id.rl_bind);
        ivIcon = getViewById(R.id.iv_icon);
        tvNick = getViewById(R.id.tv_nick);
        tvLoginTitle = getViewById(R.id.tv_login_title);
        tvBindTitle = getViewById(R.id.tv_bind_title);
        tvBind = getViewById(R.id.tv_bind);
        tvLogin = getViewById(R.id.tv_login);
        rlModifyPwd = getViewById(R.id.rl_modify_pwd);
        ivLogin = getViewById(R.id.iv_login);
        ivBind = getViewById(R.id.iv_bind);
        tvHint = getViewById(R.id.tv_hint);
    }

    @Override
    protected void setListener() {
        ObserverManager.getInstance().addUserUpdateListener(this);
        ObserverManager.getInstance().addUserActionListener(this);
        rlAvatar.setOnClickListener(this);
        llNick.setOnClickListener(this);
        llLogin.setOnClickListener(this);
        llBind.setOnClickListener(this);
        rlModifyPwd.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
    }

    @Override
    protected void lazyLoad() {
        if (!AccountManager.getInstance().isLogin()) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(AppDebugConfig.TAG_FRAG, "no login");
            }
            return;
        }
        setData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().removeUserUpdateListener(this);
        ObserverManager.getInstance().removeActionListener(this);
    }

    private void setData() {
        UserInfo user = AccountManager.getInstance().getUserInfo();
        ViewUtil.showAvatarImage(user.avatar, ivIcon, true);
        String nick;
        if (!TextUtils.isEmpty(user.thirdOpenId)) {
            nick = (TextUtils.isEmpty(user.nick) ? StringUtil.transePhone(user.phone) : user.nick);
            tvLoginTitle.setText(mContext.getResources().getString(R.string.st_user_phone_login));
            tvLogin.setText(StringUtil.transePhone(user.phone));
            ivLogin.setVisibility(View.GONE);
            tvHint.setText(mContext.getResources().getString(R.string.st_user_phone_hint));
            if (user.bindOuwanStatus == 1) {
                // 已绑定偶玩账号
                tvBindTitle.setText("绑定偶玩账号");
                tvBind.setText(user.username);
                rlModifyPwd.setVisibility(View.VISIBLE);
                ivBind.setVisibility(View.GONE);
            } else {
                tvBindTitle.setText(mContext.getResources().getString(R.string.st_user_ouwan_bind));
                tvBind.setText("未绑定");
                rlModifyPwd.setVisibility(View.GONE);
                ivBind.setImageResource(View.VISIBLE);
            }
        } else {
            nick = (TextUtils.isEmpty(user.nick) ? user.username : user.nick);
            tvLoginTitle.setText(mContext.getResources().getString(R.string.st_user_ouwan_login));
            tvLogin.setText(user.username);
            tvBindTitle.setText(mContext.getResources().getString(R.string.st_user_phone_bind));
            rlModifyPwd.setVisibility(View.VISIBLE);
            ivLogin.setVisibility(View.GONE);
            ivBind.setVisibility(View.VISIBLE);
            tvHint.setText(mContext.getResources().getString(R.string.st_user_ouwan_hint));
            if (!TextUtils.isEmpty(user.phone)) {
                tvBind.setText(StringUtil.transePhone(user.phone));
            } else {
                tvBind.setText("未绑定");
            }

        }
        tvNick.setText(nick);
        mIsNotifyRefresh = false;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        UserInfo user = AccountManager.getInstance().getUserInfo();
        switch (v.getId()) {
            case R.id.rl_avatar:
                if (getContext() != null && getContext() instanceof BaseAppCompatActivity) {
                    ((BaseAppCompatActivity) getContext()).replaceFragWithTitle(R.id.fl_container,
                            UploadAvatarFragment.newInstance(), getResources().getString(R.string
                                    .st_user_set_avatar_title));
                }
                break;
            case R.id.ll_nick:
                if (getContext() != null && getContext() instanceof BaseAppCompatActivity) {
                    ((BaseAppCompatActivity) getContext()).replaceFragWithTitle(R.id.fl_container,
                            SetNickFragment.newInstance(), getResources().getString(R.string.st_user_set_nick_title));
                }
                break;
            case R.id.rl_login:
                if (user.loginType == UserTypeUtil.TYPE_POHNE) {
                    if (user.bindOuwanStatus == 1) {
                        // 更换手机账号
                        OuwanSDKManager.getInstance().showBindPhoneView(getContext());
                    } else {
                        ToastUtil.showLong("需要先绑定偶玩账号才能更换登录手机号码");
                    }
                }
                break;
            case R.id.rl_bind:
                if (!TextUtils.isEmpty(user.thirdOpenId)) {
                    if (user.bindOuwanStatus == 0) {
                        // 绑定偶玩账号
                        OuwanSDKManager.getInstance().showBindOuwanView(getContext());
                    }
                } else {
                    // 调用偶玩绑定手机号码
                    OuwanSDKManager.getInstance().showChangePhoneView(getContext());
                }
                break;
            case R.id.rl_modify_pwd:
                OuwanSDKManager.getInstance().showOuwanModifyPwdView(getContext());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onUserUpdate(int action) {
        if (action == ObserverManager.STATUS.USER_UPDATE_ALL) {
            if (mIsNotifyRefresh) {
                return;
            }
            mIsNotifyRefresh = true;
            lazyLoad();
        }
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public void onUserActionFinish(int action, int code) {
        switch (action) {
            case ObserverManager.UserActionListener.ACTION_BIND_OUWAN:
//				ScoreManager.getInstance().reward(ScoreManager.RewardType.BIND_OUWAN, false);
                AccountManager.getInstance().updateUserInfo();
                break;
            case ObserverManager.UserActionListener.ACTION_BIND_PHONE:
//				ScoreManager.getInstance().reward(ScoreManager.RewardType.BIND_PHONE, false);
                AccountManager.getInstance().updateUserInfo();
                break;
            case ObserverManager.UserActionListener.ACTION_MODIFY_PSW:
                if (code == ObserverManager.UserActionListener.ACTION_CODE_SUCCESS) {
                    ToastUtil.showShort("密码修改成功，请使用新密码重新登录");
                    AccountManager.getInstance().logout();
                }
                return;
        }
        if (code != ObserverManager.UserActionListener.ACTION_CODE_FAILED) {
            onUserUpdate(ObserverManager.STATUS.USER_UPDATE_ALL);
        }
    }
}
