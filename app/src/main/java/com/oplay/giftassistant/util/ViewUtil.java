package com.oplay.giftassistant.util;

import android.content.Context;
import android.support.annotation.IdRes;
import android.view.View;

import com.oplay.giftassistant.R;

import cn.bingoogolapple.refreshlayout.BGAMeiTuanRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/28
 */
public class ViewUtil {
    public static  <V extends View> V getViewById(View v, @IdRes int id) {
        View child = v.findViewById(id);
        return (child != null ? (V)child : null);
    }

    public static void initRefreshLayout(Context context, BGARefreshLayout refreshLayout) {
        initRefreshLayout(context, refreshLayout, true);
    }

	public static void initRefreshLayout(Context context, BGARefreshLayout refreshLayout, boolean loadMore) {
		BGAMeiTuanRefreshViewHolder refreshViewHolder = new BGAMeiTuanRefreshViewHolder(context, loadMore);
		refreshViewHolder.setLoadingMoreText("加载更多中...");
		refreshViewHolder.setPullDownImageResource(R.mipmap.bga_refresh_mt_pull_down);
		refreshViewHolder.setRefreshingAnimResId(R.anim.bga_refresh_mt_refreshing);
		refreshViewHolder.setChangeToReleaseRefreshAnimResId(R.anim.bga_refresh_mt_change_to_release_refresh);
		refreshLayout.setRefreshViewHolder(refreshViewHolder);
	}
}
