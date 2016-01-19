package com.oplay.giftassistant.ui.fragment.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.UserTypeUtil;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.manager.OuwanSDKManager;
import com.oplay.giftassistant.model.data.resp.UserInfo;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.StringUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-17.
 */
public class UserInfoFragment extends BaseFragment {

	private RelativeLayout rlAvatar;
	private RelativeLayout rlNick;
	private LinearLayout rlLogin;
	private LinearLayout rlBind;
	private ImageView ivLogin;
	private ImageView ivBind;
	private RelativeLayout rlModifyPwd;
	private ImageView ivIcon;
	private TextView tvNick;
	private TextView tvLoginTitle;
	private TextView tvLogin;
	private TextView tvBindTitle;
	private TextView tvBind;

	public static UserInfoFragment newInstance() {
		return new UserInfoFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_user_info);
		rlAvatar = getViewById(R.id.rl_avatar);
		rlNick = getViewById(R.id.rl_nick);
		rlLogin = getViewById(R.id.rl_login);
		rlBind = getViewById(R.id.rl_bind);
		ivIcon = getViewById(R.id.iv_icon);
		tvNick = getViewById(R.id.tv_nick);
		tvLoginTitle = getViewById(R.id.tv_login_title);
		tvBindTitle = getViewById(R.id.tv_bind_title);
		tvBind = getViewById(R.id.tv_bind);
		tvLogin = getViewById(R.id.tv_login);
		rlModifyPwd = getViewById(R.id.rl_modify_pwd);
		ivLogin = getViewById(R.id.iv_login);
		ivBind = getViewById(R.id.iv_bind);
	}

	@Override
	protected void setListener() {
		rlAvatar.setOnClickListener(this);
		rlNick.setOnClickListener(this);
		rlLogin.setOnClickListener(this);
		rlBind.setOnClickListener(this);
		rlModifyPwd.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (!AccountManager.getInstance().isLogin()) {
			showToast("当前未登录");
			IntentUtil.jumpLogin(getContext());
		}
		ObserverManager.getInstance().addUserUpdateListener(this);
	}

	@Override
	protected void lazyLoad() {
		if (!AccountManager.getInstance().isLogin()) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_FRAG, "no login");
			}
			return;
		}
		refreshInitConfig();
		UserInfo user = AccountManager.getInstance().getUserInfo();
		ImageLoader.getInstance().displayImage(user.avatar, ivIcon, Global.AVATOR_IMAGE_LOADER);
		String nick;
		if (user.loginType == UserTypeUtil.TYPE_POHNE) {
			nick = (TextUtils.isEmpty(user.nick) ?  StringUtil.transePhone(user.phone) : user.nick);
			tvLoginTitle.setText(getResources().getString(R.string.st_user_phone_login));
			tvLogin.setText(StringUtil.transePhone(user.phone));
			tvBindTitle.setText(getResources().getString(R.string.st_user_ouwan_bind));
			ivLogin.setVisibility(View.VISIBLE);
			ivBind.setVisibility(View.GONE);
			if (user.bindOuwanStatus == 1) {
				// 已绑定偶玩账号
				tvBind.setText(user.username);
				rlModifyPwd.setVisibility(View.GONE);
			} else {
				tvBind.setText("未绑定");
				rlModifyPwd.setVisibility(View.VISIBLE);
			}
		} else {
			nick = (TextUtils.isEmpty(user.nick) ?  user.username : user.nick);
			tvLoginTitle.setText(getResources().getString(R.string.st_user_ouwan_login));
			tvLogin.setText(user.username);
			tvBindTitle.setText(getResources().getString(R.string.st_user_phone_bind));
			rlModifyPwd.setVisibility(View.VISIBLE);
			ivLogin.setVisibility(View.GONE);
			ivBind.setVisibility(View.VISIBLE);
			if (!TextUtils.isEmpty(user.phone)) {
				tvBind.setText(StringUtil.transePhone(user.phone));
			} else {
				tvBind.setText("未绑定");
			}

		}
		tvNick.setText(nick);
		refreshSuccessEnd();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		UserInfo user = AccountManager.getInstance().getUserInfo();
		switch (v.getId()) {
			case R.id.rl_avatar:
				((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
						UploadAvatarFragment.newInstance(), getResources().getString(R.string.st_user_set_avatar_title));
				break;
			case R.id.rl_nick:
				((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
						SetNickFragment.newInstance(), getResources().getString(R.string.st_user_set_nick_title));
				break;
			case R.id.rl_login:
				if (user.loginType == UserTypeUtil.TYPE_POHNE) {
					if (user.bindOuwanStatus == 1) {
						// 更换手机账号
					} else {
						ToastUtil.showLong("需要先绑定偶玩账号才能更换登录手机号码");
					}
				}
				break;
			case R.id.rl_bind:
				if (user.loginType == UserTypeUtil.TYPE_POHNE) {
					if (user.bindOuwanStatus == 0) {
						// 绑定偶玩账号
					}
				} else {
					// 调用偶玩绑定号码
					OuwanSDKManager.getInstance().showChangePhoneView();
				}
				break;
			case R.id.rl_modify_pwd:
				OuwanSDKManager.getInstance().showOuwanModifyPwdView();
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		ObserverManager.getInstance().addUserUpdateListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		ObserverManager.getInstance().removeUserUpdateListener(this);
	}


	@Override
	public void onUserUpdate() {
		if (mIsSwipeRefresh) {
			return;
		}
		lazyLoad();
	}
}
