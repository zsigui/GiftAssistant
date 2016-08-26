package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter_Download;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.base.FooterHolder;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.config.util.IndexTypeUtil;
import com.oplay.giftcool.listener.FooterListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.ServerInfo;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-8-24.
 */
public class OpenServerAdapter extends BaseRVAdapter_Download<ServerInfo> implements FooterListener,
        OnItemClickListener<ServerInfo> {

    private boolean mHasFooter = false;
    private int mShowType = KeyConfig.TYPE_ID_OPEN_SERVER;
    private final String CONTENT;
    private final String STR_DOWNLOAD;

    public OpenServerAdapter(Context context, int type) {
        this(context, null, type);
    }

    public OpenServerAdapter(Context context, ArrayList<ServerInfo> data, int type) {
        super(context, data);
        mShowType = type;
        setListener(this);
        CONTENT = mContext.getString(R.string.st_server_info_content);
        STR_DOWNLOAD = mContext.getString(R.string.st_game_download);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == IndexTypeUtil.ITEM_FOOTER) {
            return new FooterHolder(LayoutInflater.from(mContext).inflate(R.layout.view_item_footer, parent, false));
        }
        return new ServerHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_server_info, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == IndexTypeUtil.ITEM_FOOTER) {
            return;
        }

        ServerInfo o = getItem(position);
        ServerHolder viewHolder = (ServerHolder) holder;
        switch (mShowType) {
            case KeyConfig.TYPE_ID_OPEN_SERVER:
                viewHolder.tvContent.setText(Html.fromHtml(String.format(CONTENT, o.serverName, o.operator)));
                break;
            case KeyConfig.TYPE_ID_OPEN_TEST:
            default:
                viewHolder.tvContent.setText(Html.fromHtml(String.format(CONTENT, o.testType, o.gameType)));
                break;
        }
        viewHolder.tvName.setText(o.name);
        if (TextUtils.isEmpty(o.downloadUrl)) {
            // 没有下载
            viewHolder.btnDownload.setEnabled(false);
            viewHolder.btnDownload.setText(STR_DOWNLOAD);
        } else {
            ViewUtil.showImage(viewHolder.ivIcon, o.img);
            ViewUtil.initDownloadBtnStatus(viewHolder.btnDownload, o.appStatus);
        }
        viewHolder.tvTime.setText(DateUtil.optDateLong(o.time, DateUtil.getTime(o.time)));
        viewHolder.itemView.setOnClickListener(this);
        viewHolder.itemView.setTag(IndexTypeUtil.TAG_POSITION, position);
        viewHolder.btnDownload.setTag(IndexTypeUtil.TAG_POSITION, position);
        viewHolder.btnDownload.setTag(IndexTypeUtil.TAG_URL, o.downloadUrl);
        viewHolder.btnDownload.setOnClickListener(this);
        mPackageNameMap.put(o.packageName, o);
        mUrlDownloadBtn.put(o.downloadUrl, viewHolder.btnDownload);
    }


    @Override
    protected int getItemFooterCount() {
        return mHasFooter ? 1 : 0;
    }

    @Override
    public void showFooter(boolean isShow) {
        mHasFooter = isShow;
        if (mHasFooter) {
            notifyItemInserted(getItemCount() - 1);
        } else {
            notifyItemRemoved(getItemCount());
        }
    }

    @Override
    public void onItemClick(ServerInfo item, View view, int position) {
        if (view.getId() == R.id.tv_download && !AppStatus.DISABLE.equals(item.appStatus)) {
            if (mContext != null && mContext instanceof FragmentActivity) {
                item.handleOnClick(((FragmentActivity) mContext).getSupportFragmentManager());
            }
        } else {
            IntentUtil.jumpGameDetail(mContext, item.id, GameTypeUtil.JUMP_STATUS_DETAIL);
        }
    }

    public ArrayList<ServerInfo> getData() {
        return mData;
    }

    public void clearData() {
        mData = new ArrayList<>();
        notifyDataSetChanged();
    }

    private static class ServerHolder extends BaseRVHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvTime;
        TextView tvContent;
        TextView btnDownload;

        public ServerHolder(View itemView) {
            super(itemView);
            ivIcon = getViewById(R.id.iv_icon);
            tvName = getViewById(R.id.tv_name);
            tvTime = getViewById(R.id.tv_time);
            tvContent = getViewById(R.id.tv_content);
            btnDownload = getViewById(R.id.tv_download);
        }
    }
}
