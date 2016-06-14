package com.oplay.giftcool.ui.fragment;

import android.content.Context;
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

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.DrawerAdapter;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.util.UserTypeUtil;
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
    private TextView tvUnLogin;
    private ImageView ivIcon;
    private RecyclerView rvContent;
    private DrawerLayout drawerLayout;
    //	private SparseArray<DrawerModel> mData;
    private DrawerAdapter mAdapter;
    private ArrayList<DrawerModel> mData;
    private TextView tvScore;
    private TextView tvBean;
    private RelativeLayout rlFooter;

    public static DrawerFragment newInstance(DrawerLayout drawerLayout) {
        DrawerFragment fragment = new DrawerFragment();
        fragment.setup(drawerLayout);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_drawer);
        rlHeader = getViewById(R.id.drawer_header);
        rlFooter = getViewById(R.id.drawer_footer);
        tvUnLogin = getViewById(R.id.tv_un_login);
        tvNick = getViewById(R.id.tv_nick);
        tvName = getViewById(R.id.tv_name);
        ivIcon = getViewById(R.id.iv_icon);
        rvContent = getViewById(R.id.drawer_content);
        tvScore = getViewById(R.id.tv_score);
        tvBean = getViewById(R.id.tv_bean);
    }

    @Override
    protected void setListener() {
        rlHeader.setOnClickListener(this);
        rlFooter.setOnClickListener(this);
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
            tvNick.setText("");
            tvName.setText("");
            tvUnLogin.setVisibility(View.VISIBLE);
            tvScore.setVisibility(View.GONE);
            tvBean.setVisibility(View.GONE);

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
            tvScore.setVisibility(View.VISIBLE);
            tvBean.setVisibility(View.VISIBLE);
            tvUnLogin.setVisibility(View.GONE);
            tvScore.setText(String.valueOf(user.score));
            tvBean.setText(String.valueOf(user.bean));
            ViewUtil.showAvatarImage(user.avatar, ivIcon, AccountManager.getInstance().isLogin());
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Context context = getContext();

        if (!AccountManager.getInstance().isLogin() && v.getId() == R.id.drawer_header) {
            IntentUtil.jumpLoginNoToast(context);
            closeDrawer();
            return;
        }
        switch (v.getId()) {
            // 需要登录
            case R.id.drawer_header:
                IntentUtil.jumpUserInfo(context);
                break;
            case R.id.drawer_footer:
                IntentUtil.jumpSetting(context);
                break;
            case KeyConfig.TYPE_ID_MY_GIFT_CODE:
                IntentUtil.jumpMyGift(context);
                break;
            case KeyConfig.TYPE_ID_MY_COUPON:
                IntentUtil.jumpMyCoupon(context);
                break;
            case KeyConfig.TYPE_ID_WALLET:
                IntentUtil.jumpMyWallet(context);
                break;
            case KeyConfig.TYPE_SIGN_IN_EVERY_DAY:
                IntentUtil.jumpSignIn(context);
                break;
            case KeyConfig.TYPE_ID_MSG:
                IntentUtil.jumpMessageCentral(context);
                AccountManager.getInstance().setUnreadMessageCount(0);
                ObserverManager.getInstance().notifyUserUpdate(ObserverManager.STATUS.USER_UPDATE_TASK);
                break;
            case KeyConfig.TYPE_ID_MY_ATTENTION:
                IntentUtil.jumpMyAttention(context);
                break;
            case KeyConfig.TYPE_ID_DOWNLOAD:
                IntentUtil.jumpDownloadManager(context);
                break;
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
//		SparseArray<DrawerModel> modelArray = new SparseArray<>();
        ArrayList<DrawerModel> result = new ArrayList<>();
        DrawerModel temp = new DrawerModel(KeyConfig.TYPE_ID_MY_GIFT_CODE, R.drawable.ic_drawer_gift,
                getResources().getString(R.string.st_drawer_my_gift), this);
//		modelArray.put(KeyConfig.TYPE_ID_MY_GIFT_CODE, temp);
        result.add(temp);

        temp = new DrawerModel(KeyConfig.TYPE_ID_MY_COUPON, R.drawable
                .ic_drawer_my_coupon, getResources().getString(R.string.st_drawer_my_coupon), this);
//		modelArray.put(KeyConfig.TYPE_ID_MY_ATTENTION, temp);
        result.add(temp);

        temp = new DrawerModel(KeyConfig.TYPE_ID_MY_ATTENTION, R.drawable
                .ic_drawer_my_attention, getResources().getString(R.string.st_drawer_my_attention), this);
//		modelArray.put(KeyConfig.TYPE_ID_MY_ATTENTION, temp);
        result.add(temp);

        temp = new DrawerModel(KeyConfig.TYPE_ID_WALLET, R.drawable.ic_drawer_wallet,
                getResources().getString(R.string.st_drawer_my_wallet), this);
//		modelArray.put(KeyConfig.TYPE_ID_WALLET, temp);
        result.add(temp);

        temp = new DrawerModel(KeyConfig.TYPE_SIGN_IN_EVERY_DAY, R.drawable.ic_drawer_sign_id_everyday,
                getResources().getString(R.string.st_drawer_sign_in_everyday), this, 1);
//		modelArray.put(KeyConfig.TYPE_SIGN_IN_EVERY_DAY, temp);
        result.add(temp);

        temp = new DrawerModel(KeyConfig.TYPE_ID_MSG, R.drawable.ic_drawer_message,
                getResources().getString(R.string.st_drawer_msg), this);
//		modelArray.put(KeyConfig.TYPE_ID_MSG, temp);
        result.add(temp);

        if (AssistantApp.getInstance().isAllowDownload()) {
            temp = new DrawerModel(KeyConfig.TYPE_ID_DOWNLOAD, R.drawable.ic_drawer_download,
                    getResources().getString(R.string.st_drawer_download), this);
            temp.count = ApkDownloadManager.getInstance(getContext()).getEndOfPaused();
//			modelArray.put(KeyConfig.TYPE_ID_DOWNLOAD, temp);
            result.add(temp);
        }
        mData = result;
        return result;
    }

    /**
     * 更新侧边栏提示状态
     */
    public void updateCount(int key, int count) {
        if (mData == null)
            return;
        if (count < 0) {
            count = 0;
        }
//		DrawerModel m = mData.get(key);
        final int pos = indexOfKey(key);
        if (pos != -1) {
            mData.get(pos).count = count;
            mAdapter.notifyItemChanged(pos);
        }
    }

    /**
     * 返回键值所在的下标
     */
    private int indexOfKey(int key) {
        int pos = -1;
        for (DrawerModel d : mData) {
            pos++;
            if (d.id == key) {
                return pos;
            }
        }
        return -1;
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
            case ObserverManager.STATUS.USER_UPDATE_PART:
                if (AccountManager.getInstance().isLogin()) {
                    final UserInfo info = AccountManager.getInstance().getUserInfo();
                    tvScore.setText(String.valueOf(info.score));
                    tvBean.setText(String.valueOf(info.bean));
                } else {
                    tvScore.setText("0");
                    tvScore.setText("0");
                }
                break;
            case ObserverManager.STATUS.USER_UPDATE_ALL:
            case ObserverManager.STATUS.USER_UPDATE_TASK:
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
