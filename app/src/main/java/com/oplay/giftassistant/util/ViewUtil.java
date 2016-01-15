package com.oplay.giftassistant.util;

import android.content.Context;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.model.AppStatus;

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

	public static void initDownloadBtnStatus(TextView view, AppStatus status) {
		switch (status) {
			case OPENABLE:
				view.setText("打开");
				view.setBackgroundResource(R.drawable.selector_btn_blue);
				break;
			case INSTALLABLE:
				view.setText("安装");
				view.setBackgroundResource(R.drawable.selector_btn_blue);
				break;
			case PAUSABLE:
				view.setText("暂停");
				view.setBackgroundResource(R.drawable.selector_btn_grey);
				break;
			case RESUMABLE:
				view.setText("继续");
				view.setBackgroundResource(R.drawable.selector_btn_green);
				break;
			case DOWNLOADABLE:
				view.setText("下载");
				view.setBackgroundResource(R.drawable.selector_btn_green);
				break;
			case RETRYABLE:
				view.setText("重试");
				view.setBackgroundResource(R.drawable.selector_btn_green);
				break;
			default:
				view.setText("失效");
				view.setBackgroundResource(R.drawable.selector_btn_grey);
				break;
		}
	}
}
