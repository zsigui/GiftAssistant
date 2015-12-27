package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGiftLikeAdapter;
import com.oplay.giftassistant.adapter.IndexGiftLimitAdapter;
import com.oplay.giftassistant.adapter.IndexGiftNewAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.model.data.resp.IndexLikeGame;
import com.oplay.giftassistant.model.data.resp.IndexLimitGift;
import com.oplay.giftassistant.model.data.resp.IndexNewGift;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.widget.NestedListView;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;

/**
 *
 * 主页-礼包页面主要内容页，不承担网络请求任务
 *
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class GiftFragment extends BaseFragment implements View.OnClickListener {

    private static final String KEY_BANNER = "key_banner";
    private static final String KEY_LIKE = "key_like";
    private static final String KEY_LIMIT = "key_limit";
    private static final String KEY_NEW = "key_new";

    private List<View> views;

    private ScrollView mScrollView;
    // 活动视图, 3张
    private BGABanner mBanner;
    // 猜你喜欢
    private RelativeLayout mLikeBar;
    private RecyclerView mLikeView;
    // 今日限量
    private RelativeLayout mLimitBar;
    private RecyclerView mLimitView;
    // 今日出炉
    private RelativeLayout mNewBar;
    private NestedListView mNewView;


    private IndexGiftLikeAdapter mLikeAdapter;
    private IndexGiftLimitAdapter mLimitAdapter;
    private IndexGiftNewAdapter mNewAdapter;

    // 数据对象
    private ArrayList<String> mBannerUrls;

    public static GiftFragment newInstance() {
        return new GiftFragment();
    }

    public static GiftFragment newInstance(ArrayList<String> bannerUrls,
                                       ArrayList<IndexLikeGame> likeGames,
                                       ArrayList<IndexLimitGift> limitGifts,
                                       ArrayList<IndexNewGift> newGifts) {
        GiftFragment fragment = new GiftFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_BANNER, bannerUrls);
        bundle.putSerializable(KEY_LIKE, likeGames);
        bundle.putSerializable(KEY_LIMIT, limitGifts);
        bundle.putSerializable(KEY_NEW, newGifts);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_gifts);

        mScrollView = getViewById(R.id.sv_container);
        mBanner = getViewById(R.id.banner);
        mLikeBar = getViewById(R.id.rl_like_all);
        mLikeView = getViewById(R.id.rv_like_content);
        mLimitBar = getViewById(R.id.rl_limit_all);
        mLimitView = getViewById(R.id.rv_limit_content);
        mNewBar = getViewById(R.id.rl_new_all);
        mNewView = getViewById(R.id.rv_new_content);
    }

    @Override
    protected void setListener() {
        mLikeBar.setOnClickListener(this);
        mLimitBar.setOnClickListener(this);
        mNewBar.setOnClickListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processLogic(Bundle savedInstanceState) {

        // 设置Banner
        views = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            View v = View.inflate(mActivity, R.layout.view_banner_img, null);
            views.add(v);
        }
        mBanner.setViews(views);

        // 设置RecyclerView的LayoutManager
        LinearLayoutManager llmLike = new LinearLayoutManager(getContext());
        llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager llmLimit = new LinearLayoutManager(getContext());
        llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);

        mLikeView.setLayoutManager(llmLike);
        mLimitView.setLayoutManager(llmLimit);
        mLikeAdapter = new IndexGiftLikeAdapter(mLikeView);
        mLimitAdapter = new IndexGiftLimitAdapter(mLimitView);
        mNewAdapter = new IndexGiftNewAdapter(getContext());

        // 加载数据

        if (getArguments() != null) {
            if (AppDebugConfig.IS_FRAG_DEBUG) {
                KLog.d(AppDebugConfig.TAG_FRAG, "getArguments = " + getArguments());
            }

            updateBanners(getArguments().getStringArrayList(KEY_BANNER));

            Serializable s;
            s = getArguments().getSerializable(KEY_LIKE);
            if (s != null) {
                mLikeAdapter.setDatas((ArrayList<IndexLikeGame>) s);
            }
            s = getArguments().getSerializable(KEY_LIMIT);
            if (s != null) {
                mLimitAdapter.setDatas((ArrayList<IndexLimitGift>) s);
            }
            s = getArguments().getSerializable(KEY_NEW);
            if (s != null) {
                mNewAdapter.setDatas((ArrayList<IndexNewGift>) s);
            }
        }

        mLikeView.setAdapter(mLikeAdapter);
        mLimitView.setAdapter(mLimitAdapter);
        mNewView.setAdapter(mNewAdapter);

        mIsPrepared = true;
        mScrollView.smoothScrollTo(0, 0);
    }

    private void loadBanner() {
        if (mBannerUrls == null || mBannerUrls.size() != views.size()) {
            if (AppDebugConfig.IS_FRAG_DEBUG) {
                KLog.d(AppDebugConfig.TAG_FRAG, "bannerUrls is not to be null and the size need to be 3 : " +
                        mBannerUrls);
            }
        }
        for (int i = 0; i < views.size(); i++) {
            ImageLoader.getInstance().displayImage(mBannerUrls.get(i), (ImageView) getViewById(views.get(i), R.id
                    .iv_image_view));
        }
    }

    public void updateBanners(ArrayList<String> bannerUrls) {
        this.mBannerUrls = bannerUrls;
        loadBanner();
    }

    public void updateLikeDatas(ArrayList<IndexLikeGame> likeData) {
        if (likeData == null) {
            return;
        }
        mLikeAdapter.updateData(likeData);
    }

    public void updateLimitDatas(ArrayList<IndexLimitGift> limitData) {
        if (limitData == null) {
            return;
        }
        mLimitAdapter.updateData(limitData);
    }

    public void updateNewDatas(ArrayList<IndexNewGift> newData) {
        if (newData == null) {
            return;
        }
        mNewAdapter.updateData(newData);
    }

    @Override
    protected void lazyLoad() {
        KLog.d(AppDebugConfig.TAG_APP, "load");
        mIsLoading = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_like_all:
                showToast("猜你喜欢被点击");
                /*Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("", mLikeDatas);
                startActivity(intent);*/
                break;
            case R.id.rl_limit_all:
                showToast("今日限量被点击");
                break;
            case R.id.rl_new_all:
                showToast("新鲜出炉被点击");
                break;
        }
    }
}
