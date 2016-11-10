package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.R;

import java.lang.ref.WeakReference;

/**
 * Created by zsigui on 16-11-2.
 */

public class WithFooterRecyclerView extends RecyclerView {

    private final static int TOUCH_SLOP = 50;

    private OnLoadMoreListener mOnLoadListener;
    private int mDownY;
    private int mLastY;

    /**
     * ListView的加载中footer
     */
    private View mViewFooterLoading;
    private View mViewFooterNoMore;
    private View mViewFooterError;

    /**
     * 是否显示加载更多
     */
    private boolean mCanShowFooter = true;

    private int mCurrentState = STATE_NONE;
    // 加载更多的几个状态
    /**
     * 当前不处于底部加载中，不显示FOOTER VIEW
     */
    public static final int STATE_NONE = 0;
    /**
     * 当前显示加载中
     */
    public static final int STATE_LOADING = 1;
    /**
     * 当前显示没有更多可以加载
     */
    public static final int STATE_NO_MORE = 2;
    /**
     * 当前提示加载更多时失败
     */
    public static final int STATE_ERROR = 3;

    public WithFooterRecyclerView(Context context) {
        this(context, null);
    }

    public WithFooterRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WithFooterRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        configureScroll();
    }

    private void configureScroll() {
        addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    judgeLoad();
                }
            }

            private void judgeLoad() {
                if (canLoad()) {
                    if (mOnLoadListener != null) {
                        setLoadState(STATE_LOADING);
                        mOnLoadListener.onLoadMore();
                    }
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = mDownY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mLastY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastY = (int) ev.getY();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    public void setViewFooterLoading(View view) {
        mViewFooterLoading = view;
    }

    /**
     * 该方法需要保证在 {@link #setLayoutManager(LayoutManager)} 之后调用
     */
    public void setViewFooterLoading(@LayoutRes int layoutId) {
        setViewFooterLoading(LayoutInflater.from(getContext()).inflate(layoutId, this, false));
    }

    public void setViewFooterNoMore(View view) {
        mViewFooterNoMore = view;
    }

    /**
     * 该方法需要保证在 {@link #setLayoutManager(LayoutManager)} 之后调用
     */
    public void setViewFooterNoMore(@LayoutRes int layoutId) {
        setViewFooterNoMore(LayoutInflater.from(getContext()).inflate(layoutId, this, false));
    }

    public void setViewFooterError(View view) {
        mViewFooterError = view;
    }

    /**
     * 该方法需要保证在 {@link #setLayoutManager(LayoutManager)} 之后调用
     */
    public void setViewFooterError(@LayoutRes int layoutId) {
        setViewFooterError(LayoutInflater.from(getContext()).inflate(layoutId, this, false));
    }

    private View getViewFooterLoading() {
        if (mViewFooterLoading == null) {
            setViewFooterLoading(R.layout.sg_default_footer_loading);
        }
        return mViewFooterLoading;
    }

    private View getViewFooterNoMore() {
        if (mViewFooterNoMore == null) {
            setViewFooterNoMore(R.layout.sg_default_footer_no_more);
        }
        return mViewFooterNoMore;
    }

    private View getViewFooterError() {
        if (mViewFooterError == null) {
            setViewFooterError(R.layout.sg_default_footer_error);
        }
        return mViewFooterError;
    }

    public void setLoadState(int state) {
        switch (state) {
            case STATE_ERROR:
            case STATE_NO_MORE:
            case STATE_LOADING:
            case STATE_NONE:
                break;
            default:
                throw new IllegalStateException("不支持的加载状态State，状态值: " + state);
        }
        if (mCurrentState == state) {
            // 状态没有变更，不重复处理
            return;
        }
        int lastState = mCurrentState;
        mCurrentState = state;
        setRVLoadState(state, lastState);
    }

    private void setRVLoadState(int nowState, int lastState) {
        if (nowState == STATE_NONE) {
            // lastState != nowState 的前提下，最新状态为 STATE_NONE，则移除 FOOT_VIEW
            getAdapter().notifyItemRemoved(getAdapter().getItemCount());
        } else {
            switch (lastState) {
                case STATE_NONE:
                    // lastState != nowState 的前提下，此前状态为 STATE_NONE，则需要添加 FOOT_VIEW
                    getAdapter().notifyItemInserted(getAdapter().getItemCount() - 1);
                    smoothScrollToPosition(getAdapter().getItemCount() - 1);
                    break;
                default:
                    // lastState != nowState 的前提下，此前状态为 非STATE_NONE，则需要变换 FOOT_VIEW
                    getAdapter().notifyItemChanged(getAdapter().getItemCount() - 1);
            }
        }
    }

    private boolean canLoad() {
        return isCanShowFooter() && (mCurrentState != STATE_LOADING && mCurrentState != STATE_NO_MORE)
                && isBottom() && isPullUp();
    }

    private boolean isPullUp() {
        return mDownY - mLastY > TOUCH_SLOP;
    }

    private boolean isBottom() {
        if (getAdapter() == null) {
            // 没有设置Adapter，不进行判断
            Log.d("WithFooterRecyclerView", "Need to set Not Null Adapter before judging isBottom()");
            return false;
        }
        if (getLayoutManager() == null) {
            // LayoutManager，不进行判断
            Log.d("WithFooterRecyclerView", "Need to set Not Null LayoutManager before judging isBottom()");
            return false;
        }
        int lastItemPos = getAdapter().getItemCount() - 1;
        if (getLayoutManager() instanceof LinearLayoutManager
                || getLayoutManager() instanceof GridLayoutManager) {
            int lastVisiblePosition;
            if (getLayoutManager() instanceof LinearLayoutManager) {
                lastVisiblePosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
            } else {
                lastVisiblePosition = ((GridLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
            }
            return lastVisiblePosition >= lastItemPos;

        } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = ((StaggeredGridLayoutManager) getLayoutManager());
            int[] lastPoss = new int[layoutManager.getSpanCount()];
            layoutManager.findLastVisibleItemPositions(lastPoss);
            for (int pos : lastPoss) {
                if (pos >= lastItemPos) {
                    return true;
                }
            }
        } else {
            View lastChildView = getLayoutManager().findViewByPosition(lastItemPos);
            if (lastChildView != null) {
                return getLayoutManager().getPosition(lastChildView) >= lastItemPos;
            }
        }
        return false;
    }

    public boolean isCanShowFooter() {
        return mCanShowFooter;
    }

    public void setCanShowFooter(boolean canShowFooter) {
        mCanShowFooter = canShowFooter;
    }


    @Override
    public void setAdapter(Adapter adapter) {
        LoadMoreAdapter loadMoreAdapter = null;
        if (adapter != null) {
            if (adapter instanceof LoadMoreAdapter) {
                loadMoreAdapter = (LoadMoreAdapter) adapter;
            } else {
                loadMoreAdapter = new LoadMoreAdapter(adapter);
            }
            adapter.notifyDataSetChanged();
        }
        super.setAdapter(loadMoreAdapter);
    }

    /**
     * 设置滑动到底部加载更多时的回调函数
     */
    public void setOnLoadListener(OnLoadMoreListener loadListener) {
        mOnLoadListener = loadListener;
    }

    /**
     * 加载更多适配器，用于封装 RecyclerView
     */
    private class LoadMoreAdapter extends RecyclerView.Adapter {

        private static final int TYPE_FOOTER_LOADING = 0xF00F12AB;
        private static final int TYPE_FOOTER_NO_MORE = 0xF00F12AC;
        private static final int TYPE_FOOTER_ERROR = 0xF00F12AD;

        private RecyclerView.Adapter mAdapter;

        LoadMoreAdapter(RecyclerView.Adapter adapter) {
            mAdapter = adapter;
            registerAdapterDataObserver(new AdapterDataObserver() {
            });
        }

        @Override
        public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
            super.registerAdapterDataObserver(observer);
            mAdapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
            super.unregisterAdapterDataObserver(observer);
            mAdapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_FOOTER_LOADING:
                    return getViewFooterLoading() == null ? null : new LoadMoreHolder(getViewFooterLoading());
                case TYPE_FOOTER_NO_MORE:
                    return getViewFooterNoMore() == null ? null : new LoadMoreHolder(getViewFooterNoMore());
                case TYPE_FOOTER_ERROR:
                    return getViewFooterError() == null ? null : new LoadMoreHolder(getViewFooterError());
            }
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type == TYPE_FOOTER_ERROR
                    || type == TYPE_FOOTER_LOADING
                    || type == TYPE_FOOTER_NO_MORE) {
                if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
                    ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                    if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                        ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
                    }
                } else if (getLayoutManager() instanceof GridLayoutManager) {
                    GridLayoutManager glm = (GridLayoutManager) getLayoutManager();
                    if (!(glm.getSpanSizeLookup() instanceof LoadMoreSpanSizeLookup)) {
                        glm.setSpanSizeLookup(new LoadMoreSpanSizeLookup(WithFooterRecyclerView.this));
                    }
                }
            } else {
                mAdapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mCurrentState != STATE_NONE && position == getItemCount() - 1) {
                switch (mCurrentState) {
                    case STATE_NO_MORE:
                        return TYPE_FOOTER_NO_MORE;
                    case STATE_ERROR:
                        return TYPE_FOOTER_ERROR;
                    case STATE_LOADING:
                        return TYPE_FOOTER_LOADING;
                    default:
                        throw new IllegalStateException("当前加载状态State错误，未识别值: " + mCurrentState);
                }
            }
            return mAdapter.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return mAdapter.getItemCount() + (mCurrentState != STATE_NONE ? 1 : 0);
        }
    }

    static class LoadMoreSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

        private WeakReference<RecyclerView> rv;

        LoadMoreSpanSizeLookup(RecyclerView recyclerView) {
            rv = new WeakReference<>(recyclerView);
        }

        @Override
        public int getSpanSize(int position) {
            RecyclerView view = this.rv.get();
            if (view == null) {
                return 0;
            }
            GridLayoutManager glm = (GridLayoutManager) view.getLayoutManager();
            int type = view.getAdapter().getItemViewType(position);
            if (type == LoadMoreAdapter.TYPE_FOOTER_ERROR
                    || type == LoadMoreAdapter.TYPE_FOOTER_LOADING
                    || type == LoadMoreAdapter.TYPE_FOOTER_NO_MORE) {
                return glm.getSpanCount();
            }
            return 1;
        }
    }

    /**
     * RecyclerView 底部加载更多视图 ViewHolder
     */
    static class LoadMoreHolder extends RecyclerView.ViewHolder {

        LoadMoreHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}