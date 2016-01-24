package com.oplay.giftcool.ui.fragment.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.UserTypeUtil;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.StringUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-17.
 */
public class UserInfoFragment extends BaseFragment {

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

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 0:
					setData();
			}
		}
	};

	public static UserInfoFragment newInstance() {
		return new UserInfoFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
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
	}

	@Override
	protected void setListener() {
		ObserverManager.getInstance().addUserUpdateListener(this);
		rlAvatar.setOnClickListener(this);
		llNick.setOnClickListener(this);
		llLogin.setOnClickListener(this);
		llBind.setOnClickListener(this);
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
		mHandler.sendEmptyMessage(0);
	}


	private void setData() {
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
				rlModifyPwd.setVisibility(View.VISIBLE);
			} else {
				tvBind.setText("未绑定");
				rlModifyPwd.setVisibility(View.GONE);
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
			case R.id.ll_nick:
				((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
						SetNickFragment.newInstance(), getResources().getString(R.string.st_user_set_nick_title));
				break;
			case R.id.rl_login:
				if (user.loginType == UserTypeUtil.TYPE_POHNE) {
					if (user.bindOuwanStatus == 1) {
						// 更换手机账号
						OuwanSDKManager.getInstance().showBindPhoneView(getActivity());
					} else {
						ToastUtil.showLong("需要先绑定偶玩账号才能更换登录手机号码");
					}
				}
				break;
			case R.id.rl_bind:
				if (user.loginType == UserTypeUtil.TYPE_POHNE) {
					if (user.bindOuwanStatus == 0) {
						// 绑定偶玩账号
						OuwanSDKManager.getInstance().showBindOuwanView(getActivity());
					}
				} else {
					// 调用偶玩绑定号码
					OuwanSDKManager.getInstance().showChangePhoneView(getActivity());
				}
				break;
			case R.id.rl_modify_pwd:
				OuwanSDKManager.getInstance().showOuwanModifyPwdView(getActivity());
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
	public void onUserUpdate() {
		lazyLoad();
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
