package com.oplay.giftcool.ui.fragment.search;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.NestedGameListAdapter;
import com.oplay.giftcool.adapter.NestedGiftListAdapterNew;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.SearchDataResult;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.NestedListView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;

import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-22.
 */
public class ResultFragment extends BaseFragment implements View.OnClickListener, OnItemClickListener<IndexGameNew> {

    private final static String PAGE_NAME = "搜索结果页";

    private static final Spanned TEXT_SEARCH_GAME = Html.fromHtml("搜到的 <font color='#f85454'>游戏</font>");
    private static final Spanned TEXT_SEARCH_GIFT = Html.fromHtml("搜到的 <font color='#f85454'>礼包</font>");
    private static final Spanned TEXT_SEARCH_LIKE = Html.fromHtml("搜到的 <font color='#f85454'>首充券</font>");
    private ScrollView mContainer;
    private NestedListView mGameView;
    private NestedListView mGiftView;
    private NestedListView mGuessGiftView;
    private LinearLayout llGame;
    private LinearLayout llGift;
    private LinearLayout llGuessGift;
    private ImageView ivHopeGift;

    private NestedGameListAdapter mGameAdapter;
    private NestedGiftListAdapterNew mGiftAdapter;
    private NestedGiftListAdapterNew mGuessGiftAdapter;

    private int mId;
    private String mName;

    public static ResultFragment newInstance(SearchDataResult data) {
        ResultFragment fragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KeyConfig.KEY_DATA, data);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_search_data);

        if (!AssistantApp.getInstance().isAllowDownload()) {
            getViewById(R.id.ll_game).setVisibility(View.GONE);
        }
        mContainer = getViewById(R.id.sv_container);
        mGameView = getViewById(R.id.lv_game);
        mGiftView = getViewById(R.id.lv_gift);
        mGuessGiftView = getViewById(R.id.lv_like);
        llGame = getViewById(R.id.ll_game);
        llGift = getViewById(R.id.ll_gift);
        llGuessGift = getViewById(R.id.ll_item);
        ivHopeGift = getViewById(R.id.iv_hope_gift);

        ((TextView) getViewById(R.id.tv_game_title)).setText(TEXT_SEARCH_GAME);
        ((TextView) getViewById(R.id.tv_gift_title)).setText(TEXT_SEARCH_GIFT);
        ((TextView) getViewById(R.id.tv_like_title)).setText(TEXT_SEARCH_LIKE);

    }

    @Override
    protected void setListener() {
        /**
         * 定义礼包项的点击事件
         */
        OnItemClickListener<IndexGiftNew> giftItemClickListener = new OnItemClickListener<IndexGiftNew>() {
            @Override
            public void onItemClick(IndexGiftNew gift, View v, int position) {
                switch (v.getId()) {
                    case R.id.rl_recommend:
                        IntentUtil.jumpGiftDetail(getContext(), gift.id);
                        break;
                    case R.id.btn_send:
                        PayManager.getInstance().seizeGift(getActivity(), gift, (GiftButton) v);
                        break;
                }
            }
        };
        mGiftAdapter.setListener(giftItemClickListener);
        mGuessGiftAdapter.setListener(giftItemClickListener);
        ivHopeGift.setOnClickListener(this);
//		mContainer.setOnTouchListener(new ScrollListener(ivHopeGift));
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mGameAdapter = new NestedGameListAdapter(getContext(), null, this);
        mGiftAdapter = new NestedGiftListAdapterNew(getContext());
        mGuessGiftAdapter = new NestedGiftListAdapterNew(getContext());

        SearchDataResult data = null;
        if (getArguments() != null) {
            data = (SearchDataResult) getArguments().getSerializable(KeyConfig.KEY_DATA);
        }
        mGameView.setAdapter(mGameAdapter);
        mGiftView.setAdapter(mGiftAdapter);
        mGuessGiftView.setAdapter(mGuessGiftAdapter);
        updateData(data, mName, mId);
        mContainer.smoothScrollTo(0, 0);

    }

    @Override
    protected void lazyLoad() {
        mHasData = true;
    }

    public void updateData(SearchDataResult data, String name, int id) {
        mName = name;
        mId = id;
        if (data == null || mGameAdapter == null || mGiftAdapter == null
                || mContainer == null) {
            return;
        }
        mGameAdapter.setData(data.games);
        mGiftAdapter.setData(data.gifts);
        mGuessGiftAdapter.setData(data.coupons);
        if (!AssistantApp.getInstance().isAllowDownload()) {
            data.games = null;
        }
        showDataView(data.games, llGame);
        showDataView(data.gifts, llGift);
        showDataView(data.coupons, llGuessGift);
        mGameAdapter.updateData(data.games);
        mGiftAdapter.updateData(data.gifts);
        mGuessGiftAdapter.updateData(data.coupons);
        mContainer.smoothScrollTo(0, 0);
    }

    private void showDataView(ArrayList data, View v) {
        if (data == null || data.size() == 0) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public void release() {
        super.release();
        mContainer = null;
        mGameView = null;
        mGiftView = null;
        mGuessGiftView = null;
        llGame = null;
        llGift = null;
        llGuessGift = null;
        if (mGameAdapter != null) {
            mGameAdapter.release();
            mGameAdapter = null;
        }
        if (mGiftAdapter != null) {
            mGiftAdapter.release();
            mGiftAdapter = null;
        }
        if (mGuessGiftAdapter != null) {
            mGuessGiftAdapter.release();
            mGuessGiftAdapter = null;
        }
    }

    /**
     * 游戏列表的点击事件响应
     */
    @Override
    public void onItemClick(IndexGameNew item, View view, int position) {
        if (view.getId() == R.id.tv_download) {
            if (item != null && !AppStatus.DISABLE.equals(item.appStatus)) {
                item.handleOnClick(getChildFragmentManager());
            }
        } else {
            IntentUtil.jumpGameDetail(getContext(), item.id, GameTypeUtil.JUMP_STATUS_DETAIL);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_hope_gift:
                // 弹窗提示
                if (!AccountManager.getInstance().isLogin()) {
                    IntentUtil.jumpLogin(getContext());
                    return;
                }
                DialogManager.getInstance().showHopeGift(getChildFragmentManager(), mId, mName, mId == 0);
                break;
        }
    }

}
