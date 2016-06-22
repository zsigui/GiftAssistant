package com.oplay.giftcool.ui.fragment.search;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.SearchHistoryAdapter;
import com.oplay.giftcool.listener.OnSearchListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.widget.layout.flowlayout.FlowLayout;
import com.oplay.giftcool.ui.widget.layout.flowlayout.TagFlowLayout;
import com.oplay.giftcool.util.IntentUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 15-12-22.
 */
public class HistoryFragment extends BaseFragment implements TagFlowLayout.OnTagClickListener {

    private static final int TYPE_LOAD = 1;
    private static final int TYPE_ERROR = 2;
    private static final int TYPE_EMPTY = 3;
    private static final int TYPE_CONTENT = 4;

    private final static String PAGE_NAME = "历史搜索页";
    private static final String KEY_DATA = "key_history_data";
//    private final int PAGE_ITEM_SIZE = 6;
//    private final int FIRST_PAGE = 1;

    private TextView tvClear;
    private TextView tvEmptyHistory;
    //    private TextView tvChange;
    private TagFlowLayout tagHistoryView;
    //    private RecyclerView rvHotView;
//    private LoadingView ivLoading;
//    private ImageView ivHotError;
    private boolean mHotInError = false;
    private ImageView ivHopeGift;
    private SearchHistoryAdapter mHistoryAdapter;

    // data
//    private JsonReqBase<ReqSearchHot> mReqBase;
//    private int mLastPage = FIRST_PAGE;

//    private Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> mCall;
//    private SearchHistoryHotAdapter mHotAdapter;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    public static HistoryFragment newInstance(ArrayList<String> data) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_DATA, data);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_search_history);
        tvClear = getViewById(R.id.tv_clear);
        tvEmptyHistory = getViewById(R.id.tv_history_empty);
        tagHistoryView = getViewById(R.id.rl_history);
//        tvChange = getViewById(R.id.tv_change);
//        rvHotView = getViewById(R.id.rv_hot);
//        ivHotError = getViewById(R.id.iv_hot_err);
//        ivLoading = getViewById(R.id.iv_hot_load);
        ivHopeGift = getViewById(R.id.iv_hope_gift);
    }

    @Override
    protected void setListener() {
        tvClear.setOnClickListener(this);
        tagHistoryView.setOnTagClickListener(this);
        ivHopeGift.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        ArrayList<String> data = null;
        if (getArguments() != null) {
            data = getArguments().getStringArrayList(KEY_DATA);
        }

        mHistoryAdapter = new SearchHistoryAdapter(getContext(), data);
        tagHistoryView.setAdapter(mHistoryAdapter);
        updateHistoryData(data);
    }

    @Override
    protected void lazyLoad() {
    }

    /**
     * 根据传入的 type 显示历史记录页面
     *
     * @param type {@link #TYPE_CONTENT}, {@link #TYPE_EMPTY}
     */
    private boolean showHistoryState(int type) {
        if (tvEmptyHistory == null || tagHistoryView == null || tvClear == null) {
            return false;
        }
        switch (type) {
            case TYPE_EMPTY:
                tvEmptyHistory.setVisibility(View.VISIBLE);
                tagHistoryView.setVisibility(View.GONE);
                tvClear.setEnabled(false);
                return true;
            case TYPE_CONTENT:
                tvEmptyHistory.setVisibility(View.GONE);
                tagHistoryView.setVisibility(View.VISIBLE);
                tvClear.setEnabled(true);
                return true;
        }
        return false;
    }

    /**
     * 更新历史记录列表数据
     */
    public void updateHistoryData(List<String> data) {
        if (data == null || data.isEmpty()) {
            showHistoryState(TYPE_EMPTY);
            return;
        }
        showHistoryState(TYPE_CONTENT);
        if (mHistoryAdapter != null) {
            mHistoryAdapter.updateData(data);
        }
    }

//    /**
//     * 更新热门推荐数据
//     */
//    public void updateHotData(ArrayList<IndexGameNew> data) {
//        mHasData = true;
//        if (data == null || data.isEmpty()) {
//            showHotState(TYPE_EMPTY);
//            return;
//        }
//        showHotState(TYPE_CONTENT);
//        if (mHotAdapter != null) {
//            mHotAdapter.updateData(data);
//        }
//    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
//            case R.id.iv_hot_err:
//                // 出错，继续重新请求
//                if (mHotInError) {
//                    lazyLoad();
//                }
//                break;
//            case R.id.tv_change:
//                // 换一批，请求下一页
//                mLastPage += 1;
//                lazyLoad();
//                break;
            case R.id.tv_clear:
                handleClear();
                break;
            case R.id.iv_hope_gift:
                // 弹窗提示
                if (!AccountManager.getInstance().isLogin()) {
                    IntentUtil.jumpLogin(getContext());
                    return;
                }
                DialogManager.getInstance().showHopeGift(getChildFragmentManager(), 0, "", true);
                break;
        }
    }

    /**
     * 弹窗确认是否清空搜索记录
     */
    private void handleClear() {
        final ConfirmDialog clearDialog = ConfirmDialog.newInstance();
        clearDialog.setContent("确定清空搜索记录?");
        clearDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
            @Override
            public void onCancel() {
                clearDialog.dismissAllowingStateLoss();
            }

            @Override
            public void onConfirm() {
                if (getContext() instanceof OnSearchListener) {
                    ((OnSearchListener) getContext()).clearHistory();
                }
                updateHistoryData(null);
                clearDialog.dismissAllowingStateLoss();
            }
        });
        clearDialog.show(getChildFragmentManager(), "history_clear");
    }

    @Override
    public void release() {
        super.release();
//        if (mCall != null) {
//            mCall.cancel();
//            mCall = null;
//        }
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public boolean onTagClick(View view, int position, FlowLayout parent) {
        String keyword = mHistoryAdapter.getItem(position);
        if (getContext() != null && getContext() instanceof OnSearchListener) {
            ((OnSearchListener) getContext()).sendSearchRequest(keyword, 0);
        }
        return true;
    }
}
