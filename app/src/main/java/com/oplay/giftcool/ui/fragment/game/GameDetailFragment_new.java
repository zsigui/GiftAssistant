//package com.oplay.giftcool.ui.fragment.game;
//
//import android.databinding.DataBindingUtil;
//import android.os.Bundle;
//import android.support.annotation.LayoutRes;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.text.Html;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.CheckedTextView;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.ogaclejapan.smarttablayout.SmartTabLayout;
//import com.oplay.giftcool.AssistantApp;
//import com.oplay.giftcool.R;
//import com.oplay.giftcool.config.AppDebugConfig;
//import com.oplay.giftcool.config.Global;
//import com.oplay.giftcool.config.NetStatusCode;
//import com.oplay.giftcool.config.TypeStatusCode;
//import com.oplay.giftcool.config.WebViewUrl;
//import com.oplay.giftcool.config.util.GameTypeUtil;
//import com.oplay.giftcool.databinding.FragmentGameDetail2Binding;
//import com.oplay.giftcool.download.ApkDownloadManager;
//import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
//import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
//import com.oplay.giftcool.model.data.resp.GameDetail;
//import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
//import com.oplay.giftcool.model.json.base.JsonRespBase;
//import com.oplay.giftcool.ui.activity.GameDetailActivity;
//import com.oplay.giftcool.ui.fragment.WebFragment;
//import com.oplay.giftcool.ui.fragment.base.BaseFragment;
//import com.oplay.giftcool.ui.widget.LoadAndRetryViewManager;
//import com.oplay.giftcool.ui.widget.button.DownloadButtonView;
//import com.oplay.giftcool.util.ThreadUtil;
//import com.oplay.giftcool.util.ViewUtil;
//
//import java.util.Locale;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
///**
// * Created by zsigui on 16-5-4.
// */
//public class GameDetailFragment_new extends BaseFragment implements OnDownloadStatusChangeListener,
//        OnProgressUpdateListener {
//
//    private static final String PAGE_NAME = "游戏详情页";
//    private static final String KEY_ID = "key_data_id";
//    private static final String KEY_COLOR = "key_data_color";
//    private static final String KEY_STATUS = "key_data_status";
//
//    private String[] mTabTitle = new String[]{"详情", "礼包", "评论"};
//
//    private FragmentGameDetail2Binding mBinding;
//
//    // 子页面
//    private Fragment mInfoFragment;
//    private Fragment mGiftListFragment;
//    private Fragment mCommentFragment;
//
//    private int mId;
//    private GameDetail mAppInfo;
//    private int[] mThemeColor = {R.color.co_rainbow_color_1, R.color.co_rainbow_color_2,
//            R.color.co_rainbow_color_3, R.color.co_rainbow_color_4, R.color.co_rainbow_color_5};
//
//    public static GameDetailFragment_new newInstance(int id, int status, String color) {
//        GameDetailFragment_new fragment = new GameDetailFragment_new();
//        Bundle bundle = new Bundle();
//        bundle.putInt(KEY_ID, id);
//        bundle.putString(KEY_COLOR, color);
//        bundle.putInt(KEY_STATUS, status);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//    protected void initViewManger(@LayoutRes int layoutResID) {
//        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), layoutResID, null, false);
//        mViewManager = LoadAndRetryViewManager.generate(getContext());
//        mViewManager.setContentView(mBinding.getRoot());
//        mContentView = mViewManager.getContainer();
//    }
//
//    @Override
//    protected void initView(Bundle savedInstanceState) {
//        initViewManger(R.layout.fragment_game_detail2);
//
//    }
//
//    @Override
//    protected void setListener() {
//        btnDownload.setOnClickListener(this);
//        ApkDownloadManager.getInstance(getContext()).addDownloadStatusListener(this);
//        ApkDownloadManager.getInstance(getContext()).addProgressUpdateListener(this);
//        ctvFocus.setOnClickListener(this);
//    }
//
//    @Override
//    protected void processLogic(Bundle savedInstanceState) {
//        if (getArguments() == null) {
//            mViewManager.showEmpty();
//            return;
//        }
//        mId = getArguments().getInt(KEY_ID);
//        int status = getArguments().getInt(KEY_STATUS, GameTypeUtil.JUMP_STATUS_DETAIL);
//        String statusBarColor = getArguments().getString(KEY_COLOR, "f85454");
//
//        rlHeader.setBac;
//        vpContent.setAdapter(new GameDetailPagerAdapter(getChildFragmentManager()));
//        vpContent.setOffscreenPageLimit(1);
//        stlTab.setViewPager(vpContent);
//        vpContent.setCurrentItem(status == GameTypeUtil.JUMP_STATUS_DETAIL ? 0 : 1);
////		vpContent.addOnPageChangeListener(this);
//        if (AssistantApp.getInstance().isAllowDownload()) {
//            downloadLayout.setVisibility(View.VISIBLE);
//        } else {
//            downloadLayout.setVisibility(View.GONE);
//        }
//    }
//
//    private Call<JsonRespBase<GameDetail>> mCallRefresh;
//
//    @Override
//    protected void lazyLoad() {
//        Global.THREAD_POOL.execute(new Runnable() {
//            @Override
//            public void run() {
//                refreshInitConfig();
//                // 判断网络情况
////        if (!NetworkUtil.isConnected(getContext())) {
////            refreshFailEnd();
////            return;
////        }
//                if (mCallRefresh != null) {
//                    mCallRefresh.cancel();
//                }
//                mCallRefresh = ((GameDetailActivity) getActivity()).getEngine().obtainGameDetail(mId);
//                mCallRefresh.enqueue(new Callback<JsonRespBase<GameDetail>>() {
//                    @Override
//                    public void onResponse(Call<JsonRespBase<GameDetail>> call, Response<JsonRespBase<GameDetail>>
//                            response) {
//                        if (!mCanShowUI || call.isCanceled()) {
//                            return;
//                        }
//                        if (response != null && response.isSuccessful()) {
//                            Global.sServerTimeDiffLocal = System.currentTimeMillis() - response.headers().getDate
//                                    ("Date").getTime();
//                            if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
//                                // 获取数据成功
//                                refreshSuccessEnd();
//                                updateData(response.body().getData());
//                                return;
//                            }
//                        }
//                        AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
//                        refreshFailEnd();
//                    }
//
//                    @Override
//                    public void onFailure(Call<JsonRespBase<GameDetail>> call, Throwable t) {
//                        if (!mCanShowUI || call.isCanceled()) {
//                            return;
//                        }
//                        AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
//                        refreshFailEnd();
//                    }
//                });
//            }
//        });
//    }
//
//    private void updateData(GameDetail data) {
//        if (mInfoFragment == null) {
//            mInfoFragment = GameDetailInfoFragment.newInstance("http://lbapi.ouwan.com/");
//        }
////		mInfoFragment.update(data);
//        ViewUtil.showImage(ivIcon, data.img);
//        tvName.setText(data.name);
//        if (data.playCount < 10000) {
//            tvPlay.setText(Html.fromHtml(String.format(Locale.CHINA, "%d人在玩", data.playCount)));
//        } else {
//            tvPlay.setText(Html.fromHtml(String.format(Locale.CHINA, "%.1f万人在玩", (float) data.playCount / 10000)));
//        }
//        tvSize.setText(data.size);
//        tvNewAdd.setText(String.valueOf(data.newCount));
//        tvTotal.setText(String.valueOf(data.totalCount));
//        if (data.isFocus == TypeStatusCode.FOCUS_ON) {
//            ctvFocus.setChecked(false);
//            ctvFocus.setText("取消关注");
//        } else {
//            ctvFocus.setChecked(true);
//            ctvFocus.setText("关注");
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        super.onClick(v);
//        switch (v.getId()) {
//            case R.id.tv_focus:
//
//                break;
//            case R.id.btn_download:
//
//                break;
//        }
//    }
//
//    @Override
//    public String getPageName() {
//        return PAGE_NAME;
//    }
//
//    @Override
//    public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
//        ThreadUtil.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
//                    mAppInfo.downloadStatus = appInfo.downloadStatus;
//                    mAppInfo.initAppInfoStatus(getContext());
//                    btnDownload.setStatus(mAppInfo.appStatus, "");
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onProgressUpdate(String url, final int percent, long speedBytesPers) {
//        ThreadUtil.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (downloadLayout != null && downloadLayout.getVisibility() == View.VISIBLE) {
//                    btnDownload.setProgress(percent);
//                }
//            }
//        });
//    }
//
//    public class GameDetailPagerAdapter extends FragmentPagerAdapter {
//
//        public GameDetailPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            if (position == 0) {
//                if (mInfoFragment == null) {
//                    mInfoFragment = GameDetailInfoFragment.newInstance(WebViewUrl.getWebUrl(WebViewUrl
//                            .OUWAN_BEAN_DETAIL));
//                }
//                return mInfoFragment;
//            } else if (position == 1) {
//                if (mGiftListFragment == null) {
//                    mGiftListFragment = GameDetailInfoFragment.newInstance(WebViewUrl.getWebUrl(WebViewUrl
//                            .OUWAN_BEAN_DETAIL_NOTE));
//                }
//                return mGiftListFragment;
//            } else if (position == 2) {
//                if (mCommentFragment == null) {
//                    mCommentFragment = WebFragment.newInstance("http://lbapi.ouwan.com/");
//                }
//                return mCommentFragment;
//            }
//            return null;
//        }
//
//        @Override
//        public int getCount() {
//            return mTabTitle.length;
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return mTabTitle[position];
//        }
//    }
//}
