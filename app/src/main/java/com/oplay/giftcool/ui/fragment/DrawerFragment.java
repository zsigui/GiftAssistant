package com.oplay.giftcool.ui.fragment;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.DrawerAdapter;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.UserTypeUtil;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.DrawerModel;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.StringUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-21.
 */
public class DrawerFragment extends BaseFragment {

	private RelativeLayout rlHeader;
	private TextView tvNick;
	private TextView tvName;
	private ImageView ivIcon;
	private RecyclerView rvContent;
	private DrawerLayout drawerLayout;


	public static DrawerFragment newInstance(DrawerLayout drawerLayout) {
		DrawerFragment fragment = new DrawerFragment();
		fragment.setup(drawerLayout);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_drawer);
		rlHeader = getViewById(R.id.drawer_header);
		tvNick = getViewById(R.id.tv_nick);
		tvName = getViewById(R.id.tv_name);
		ivIcon = getViewById(R.id.iv_icon);
		rvContent = getViewById(R.id.drawer_content);
	}

	@Override
	protected void setListener() {
		rlHeader.setOnClickListener(this);
		ObserverManager.getInstance().addUserUpdateListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		rvContent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		DrawerAdapter adapter = new DrawerAdapter(getContext());
		adapter.setData(initDrawerItem());
		rvContent.setAdapter(adapter);
	}

	@Override
	protected void lazyLoad() {
		updateData();
	}

	private void updateData() {
		if (!AccountManager.getInstance().isLogin()) {
			ivIcon.setImageResource(R.drawable.ic_avator_unlogin);
			tvNick.setText("点击注册/登录");
			tvName.setText("");
		} else {
			String nick;
			String name;
			UserInfo user = AccountManager.getInstance().getUserInfo();
			if (user.loginType == UserTypeUtil.TYPE_POHNE
					|| (user.loginType != UserTypeUtil.TYPE_OUWAN && user.bindOuwanStatus == 0)) {
				nick = (TextUtils.isEmpty(user.nick) ? StringUtil.transePhone(user.phone) : user.nick);
				name = "登陆手机：" + StringUtil.transePhone(user.phone);
			} else {
				nick = (TextUtils.isEmpty(user.nick) ? user.username : user.nick);
				name = "偶玩账号：" + user.username;
			}
			tvNick.setText(nick);
			tvName.setText(name);
			ViewUtil.showAvatarImage(user.avatar, ivIcon, AccountManager.getInstance().isLogin());
			ImageLoader.getInstance().displayImage(user.avatar, ivIcon, Global.AVATAR_IMAGE_LOADER);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.drawer_header:
				if (AccountManager.getInstance().isLogin()) {
					IntentUtil.jumpUserInfo(getContext());
				} else {
					IntentUtil.jumpLogin(getContext());
				}
				closeDrawer();
				break;
		}
	}

	public void setup(DrawerLayout drawer) {
		this.drawerLayout = drawer;
	}

	public void closeDrawer() {
		if (drawerLayout != null) {
			if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
				drawerLayout.closeDrawer(GravityCompat.START);
			}
		}
	}

	private ArrayList<DrawerModel> initDrawerItem() {
		ArrayList<DrawerModel> models = new ArrayList<>();
		models.add(new DrawerModel(R.drawable.ic_drawer_gift, "我的礼包", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AccountManager.getInstance().isLogin()) {
					IntentUtil.jumpMyGift(getContext());
				} else {
					IntentUtil.jumpLogin(getContext());
				}
				closeDrawer();

			}
		}));
		models.add(new DrawerModel(R.drawable.ic_drawer_wallet, "我的钱包", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AccountManager.getInstance().isLogin()) {
					IntentUtil.jumpMyWallet(getContext());
				} else {
					IntentUtil.jumpLogin(getContext());
				}
				closeDrawer();
			}
		}));
		models.add(new DrawerModel(R.drawable.ic_drawer_score_task, "每日任务", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AccountManager.getInstance().isLogin()) {
					IntentUtil.jumpEarnScore(getContext());
				} else {
					IntentUtil.jumpLogin(getContext());
				}
				closeDrawer();
			}
		}));
		models.add(new DrawerModel(R.drawable.ic_drawer_message, "消息中心", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AccountManager.getInstance().isLogin()) {
					ToastUtil.showShort("敬请期待");
				} else {
					IntentUtil.jumpLogin(getContext());
				}
				closeDrawer();
			}
		}));
		if (AssistantApp.getInstance().isAllowDownload()) {
			models.add(new DrawerModel(R.drawable.ic_drawer_download, "下载管理", new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					IntentUtil.jumpDownloadManager(getContext());
					closeDrawer();
				}
			}));
		}
		models.add(new DrawerModel(R.drawable.ic_drawer_setting, "设置", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentUtil.jumpSetting(getContext());
				closeDrawer();
			}
		}));
		return models;
	}

	@Override
	public void onUserUpdate() {
		super.onUserUpdate();
		updateData();
	}

	@Override
	public String getPageName() {
		return null;
	}
}
