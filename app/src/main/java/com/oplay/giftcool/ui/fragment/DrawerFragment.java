package com.oplay.giftcool.ui.fragment;

import android.content.Context;
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
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.DrawerModel;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.StringUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;

/**
 * 侧边栏视图
 * <p/>
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
	private ImageView ivEdit;

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
		ivEdit = getViewById(R.id.iv_edit);
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
	public void onDestroyView() {
		super.onDestroyView();
		ObserverManager.getInstance().removeUserUpdateListener(this);
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
			ivEdit.setVisibility(View.GONE);

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
			ivEdit.setVisibility(View.VISIBLE);
			ViewUtil.showAvatarImage(user.avatar, ivIcon, AccountManager.getInstance().isLogin());
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		Context context = getContext();

		if (!AccountManager.getInstance().isLogin()) {
			switch (v.getId()) {
				// 未登录下的
				case R.id.drawer_header:
					IntentUtil.jumpLoginNoToast(context);
					break;
				case KeyConfig.TYPE_ID_SETTING:
					IntentUtil.jumpSetting(context);
					break;
				case KeyConfig.TYPE_ID_DOWNLOAD:
					IntentUtil.jumpDownloadManager(context, false);
					break;
				default:
					IntentUtil.jumpLogin(context);
			}
		} else {
			switch (v.getId()) {
				// 需要登录
				case R.id.drawer_header:
					IntentUtil.jumpUserInfo(context);
					break;
				case KeyConfig.TYPE_ID_MY_GIFT_CODE:
					IntentUtil.jumpMyGift(context);
					break;
				case KeyConfig.TYPE_ID_WALLET:
					IntentUtil.jumpMyWallet(context);
					break;
				case KeyConfig.TYPE_ID_SCORE_TASK:
					IntentUtil.jumpEarnScore(context);
					break;
				case KeyConfig.TYPE_ID_MSG:
					IntentUtil.jumpMessageCentral(context);
					break;
				case KeyConfig.TYPE_ID_MY_ATTENTION:
					IntentUtil.jumpMyAttention(context);
					break;
				// 与登录无关
				case KeyConfig.TYPE_ID_SETTING:
					IntentUtil.jumpSetting(context);
					break;
				case KeyConfig.TYPE_ID_DOWNLOAD:
					IntentUtil.jumpDownloadManager(context, false);
					break;
			}
		}
		closeDrawer();
	}

	public void setup(DrawerLayout drawer) {
		this.drawerLayout = drawer;
	}

	/**
	 * 关闭侧边栏
	 */
	public void closeDrawer() {
		if (drawerLayout != null) {
			if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
				drawerLayout.closeDrawer(GravityCompat.START);
			}
		}
	}

	/**
	 * 初始化侧边栏列表信息
	 */
	private ArrayList<DrawerModel> initDrawerItem() {
		SparseArray<DrawerModel> modelArray = new SparseArray<>();
		modelArray.put(KeyConfig.TYPE_ID_MY_GIFT_CODE,
				new DrawerModel(KeyConfig.TYPE_ID_MY_GIFT_CODE, R.drawable.ic_drawer_gift, "我的礼包", this));
		modelArray.put(KeyConfig.TYPE_ID_WALLET,
				new DrawerModel(KeyConfig.TYPE_ID_WALLET, R.drawable.ic_drawer_wallet, "我的钱包", this));
		modelArray.put(KeyConfig.TYPE_ID_SCORE_TASK,
				new DrawerModel(KeyConfig.TYPE_ID_SCORE_TASK, R.drawable.ic_drawer_score_task, "赚金币", this));
		modelArray.put(KeyConfig.TYPE_ID_MSG, new DrawerModel(KeyConfig.TYPE_ID_MSG, R.drawable.ic_drawer_message,
				"消息中心", this));
		modelArray.put(KeyConfig.TYPE_ID_MY_ATTENTION, new DrawerModel(KeyConfig.TYPE_ID_MY_ATTENTION, R.drawable
				.ic_drawer_my_attention, "我的关注", this));
		if (AssistantApp.getInstance().isAllowDownload()) {
			DrawerModel dw = new DrawerModel(KeyConfig.TYPE_ID_DOWNLOAD, R.drawable.ic_drawer_download, "下载管理", this);
			dw.count = ApkDownloadManager.getInstance(getContext()).getEndOfPaused();
			modelArray.put(KeyConfig.TYPE_ID_DOWNLOAD, dw);
		}
		modelArray.put(KeyConfig.TYPE_ID_SETTING,
				new DrawerModel(KeyConfig.TYPE_ID_SETTING, R.drawable.ic_drawer_setting, "设置", this));
		mData = modelArray;
		ArrayList<DrawerModel> result = new ArrayList<>();
		for (int i = 0; i < mData.size(); i++) {
			result.add(mData.valueAt(i));
		}
		return result;
	}

	/**
	 * 更新侧边栏提示状态
	 */
	public void updateCount(int key, int count) {
		if (mData == null || count < -1)
			return;
		DrawerModel m = mData.get(key);
		if (m != null) {
			m.count = count;
			mAdapter.notifyItemChanged(mData.indexOfKey(key));
		}
	}


	@Override
	public void release() {
		super.release();
		rlHeader = null;
		tvNick = null;
		ivIcon = null;
		if (rvContent != null) {
			if (mAdapter != null) {
				mAdapter.release();
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
	public void onUserUpdate(int action) {
		super.onUserUpdate(action);
		switch (action) {
			case ObserverManager.STATUS.USER_UPDATE_ALL:
				updateData();
				break;
			case ObserverManager.STATUS.USER_UPDATE_PUSH_MESSAGE:
//				updateCount(KeyConfig.TYPE_ID_MSG, AccountManager.getInstance().getUnreadMessageCount());
				break;
		}
	}

	@Override
	public String getPageName() {
		//侧边栏不做统计
		return "";
	}
}
