package com.oplay.giftcool.ui.fragment;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.DrawerAdapter;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.UserTypeUtil;
import com.oplay.giftcool.listener.OnFinishListener;
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
	private SparseArray<DrawerModel> mData;
	private DrawerAdapter mAdapter;

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
		rvContent.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext(),
				LinearLayoutManager.VERTICAL, false));
		mAdapter = new DrawerAdapter(getContext().getApplicationContext());
		mAdapter.setData(initDrawerItem());
		rvContent.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
		updateData();
	}

	private void updateData() {
		if (!AccountManager.getInstance().isLogin()) {
			ivIcon.setImageResource(R.drawable.ic_avator_unlogin);
			ivIcon.setTag("");
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
		SparseArray<DrawerModel> modelArray = new SparseArray<>();
		modelArray.put(KeyConfig.TYPE_ID_MY_GIFT_CODE,
				new DrawerModel(R.drawable.ic_drawer_gift, "我的礼包", new View.OnClickListener() {
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
		modelArray.put(KeyConfig.TYPE_ID_WALLET,
				new DrawerModel(R.drawable.ic_drawer_wallet, "我的钱包",
				new View.OnClickListener() {
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
		modelArray.put(KeyConfig.TYPE_ID_SCORE_TASK,
				new DrawerModel(R.drawable.ic_drawer_score_task, "每日任务", new View.OnClickListener() {
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
		modelArray.put(KeyConfig.TYPE_ID_MSG, new DrawerModel(R.drawable.ic_drawer_message, "消息中心", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastUtil.showShort("敬请期待");
			}
		}));
		if (AssistantApp.getInstance().isAllowDownload()) {
			modelArray.put(KeyConfig.TYPE_ID_DOWNLOAD,
					new DrawerModel(R.drawable.ic_drawer_download, "下载管理",
					new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					IntentUtil.jumpDownloadManager(getContext());
					closeDrawer();
				}
			}));
		}
		modelArray.put(KeyConfig.TYPE_ID_SETTING,
				new DrawerModel(R.drawable.ic_drawer_setting, "设置", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentUtil.jumpSetting(getContext());
				closeDrawer();
			}
		}));
		mData = modelArray;
		ArrayList<DrawerModel> result = new ArrayList<>();
		for (int i = 0; i < mData.size(); i++) {
			result.add(mData.valueAt(i));
		}
		return result;
	}

	public void updateCount(int key, int count) {
		if (mData == null || count < -1)
			return;
		DrawerModel m = mData.get(key);
		if (m != null) {
			m.count = count;
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void release() {
		super.release();
		rlHeader = null;
		tvNick = null;
		ivIcon = null;
		if (rvContent != null) {
			if (mAdapter != null && mAdapter instanceof OnFinishListener) {
				((OnFinishListener) mAdapter).release();
			}
			rvContent.setAdapter(null);
			mAdapter = null;
		}
		if (mData != null) {
			mData.clear();
			mData = null;
		}
		drawerLayout = null;
	}

	@Override
	public void onUserUpdate() {
		super.onUserUpdate();
		updateData();
	}

	@Override
	public String getPageName() {
		return "侧边栏";
	}
}
