package com.oplay.giftcool.adapter.base;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.config.util.IndexTypeUtil;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ThreadUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zsigui on 16-1-16.
 */
public abstract class BaseRVAdapter_Download extends BaseRVAdapter<IndexGameNew> implements View.OnClickListener,
        OnDownloadStatusChangeListener {

    protected HashMap<String, IndexGameNew> mPackageNameMap;
    protected HashMap<String, TextView> mUrlDownloadBtn;

    protected BaseRVAdapter_Download(Context context) {
        this(context, null);
    }

    protected BaseRVAdapter_Download(Context context, ArrayList<IndexGameNew> data) {
        this(context, data, null);
    }

    protected BaseRVAdapter_Download(Context context, ArrayList<IndexGameNew> data, OnItemClickListener<IndexGameNew>
            listener) {
        super(context, data, listener);
        mPackageNameMap = new HashMap<>();
        mUrlDownloadBtn = new HashMap<>();
        ApkDownloadManager.getInstance(mContext).addDownloadStatusListener(this);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + getItemHeaderCount() + getItemFooterCount();
    }

    protected int getItemTypeCount() {
        if (getItemHeaderCount() > 0 && getItemFooterCount() > 0) {
            return 3;
        } else if (getItemHeaderCount() > 0) {
            return 2;
        } else if (getItemFooterCount() > 0) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemHeaderCount() > 0 && position < getItemHeaderCount()) {
            return IndexTypeUtil.ITEM_HEADER;
        } else if (getItemFooterCount() > 0
                && position >= getItemHeaderCount() + (mData == null ? 0 : mData.size())) {
            return IndexTypeUtil.ITEM_FOOTER;
        }
        return IndexTypeUtil.ITEM_NORMAL;
    }

    protected int getItemHeaderCount() {
        return 0;
    }

    protected int getItemFooterCount() {
        return 0;
    }

    /**
     * 重写此方法以处理第n个位置视图的点击响应事件
     */
    protected boolean handleOnClick(View v, int position) {
        return false;
    }

    @Override
    public void onClick(View v) {
        try {
            final Object tag = v.getTag(IndexTypeUtil.TAG_POSITION);
            int position = (Integer) tag;
            if (handleOnClick(v, position)) {
                return;
            }
            if (getItemTypeCount() > 0) {
                position -= getItemHeaderCount();
            }
            if (position < 0 || mData == null || position >= mData.size()) {
                AppDebugConfig.w(AppDebugConfig.TAG_ADAPTER, "position = " + position + ", data = " + mData);
                return;
            }
            final IndexGameNew appInfo = mData.get(position);
//			if (mListener != null) {
//				mListener.onItemClick(appInfo, v, position);
//			}
            switch (v.getId()) {
                case R.id.tv_download:
                    if (mContext != null && mContext instanceof FragmentActivity
                            && !AppStatus.DISABLE.equals(appInfo.appStatus)) {
                        appInfo.handleOnClick(((FragmentActivity) mContext).getSupportFragmentManager());
                    }
                    break;
                default:
                    IntentUtil.jumpGameDetail(mContext, appInfo.id, GameTypeUtil.JUMP_STATUS_DETAIL);
                    break;
            }
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_ADAPTER, e);
        }
    }

    public void updateViewByPackageName(String packageName, DownloadStatus status) {
        final IndexGameNew app = mPackageNameMap.get(packageName);
        if (app != null) {
            app.downloadStatus = status;
            app.initAppInfoStatus(mContext);
            notifyDataSetChanged();
        }
    }

    public void updateViewByPackageName(String packageName) {
        final IndexGameNew app = mPackageNameMap.get(packageName);
        if (app != null) {
            app.initAppInfoStatus(mContext);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appInfo != null) {
                    final String packageName = appInfo.packageName;
                    final DownloadStatus status = appInfo.downloadStatus;
                    updateViewByPackageName(packageName, status);
                }
            }
        });
    }

    @Override
    public void release() {
        super.release();
        ApkDownloadManager.getInstance(mContext).removeDownloadStatusListener(this);
    }
}
