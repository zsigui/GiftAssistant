package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.model.data.resp.MyCouponDetail;
import com.oplay.giftcool.ui.fragment.dialog.UsageCouponHintDialog;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的首充券列表内容适配器
 * <p/>
 * Created by zsigui on 16-1-7.
 */
public class MyCouponListAdapter extends BaseListAdapter<MyCouponDetail> implements View.OnClickListener {

    private FragmentManager mManager;

    public MyCouponListAdapter(Context context, List<MyCouponDetail> objects, FragmentManager fm) {
        super(context, objects);
        mManager = fm;
    }

    public void updateData(ArrayList<MyCouponDetail> data) {
        if (data == null) {
            return;
        }
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount() == 0) {
            return null;
        }

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.item_list_my_coupon, parent, false);
            holder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
            holder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
            holder.tvPlatform = ViewUtil.getViewById(convertView, R.id.tv_platform);
            holder.tvPrice = ViewUtil.getViewById(convertView, R.id.tv_price);
            holder.tvDeadline = ViewUtil.getViewById(convertView, R.id.tv_deadline);
            holder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MyCouponDetail o = getItem(position);
        holder.tvName.setText(o.name);
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.tvPlatform.setText(o.platform);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
        holder.tvDeadline.setText(String.format("%s ~ %s", DateUtil.formatTime(o.useStartTime, "yyyy.MM.dd HH:mm"),
                DateUtil.formatTime(o.useEndTime, "yyyy.MM.dd HH:mm")));

        switch (o.usageStatus) {
            case GiftTypeUtil.COUPON_USAGE_NEVER:
                holder.btnSend.setText("去使用");
                holder.btnSend.setEnabled(true);
                break;
            case GiftTypeUtil.COUPON_USAGE_USED:
                holder.btnSend.setText("已使用");
                holder.btnSend.setEnabled(false);
                break;
            case GiftTypeUtil.COUPON_USAGE_OVER:
                holder.btnSend.setText("已过期");
                holder.btnSend.setEnabled(false);
                break;
        }
        holder.btnSend.setOnClickListener(this);
        holder.btnSend.setTag(TAG_POSITION, position);
        convertView.setOnClickListener(this);
        convertView.setTag(TAG_POSITION, position);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (mData == null || v.getTag(TAG_POSITION) == null) {
            return;
        }
        MyCouponDetail item = getItem((Integer) v.getTag(TAG_POSITION));
        switch (v.getId()) {
            case R.id.btn_send:
                if (mManager != null) {
                    UsageCouponHintDialog.newInstance(item).show(
                            mManager,
                            UsageCouponHintDialog.class.getSimpleName());
                }
                break;
            case R.id.rl_recommend:
                IntentUtil.jumpGiftDetail(mContext, item.id);
                break;
        }
    }

    static class ViewHolder {
        TextView tvName;
        ImageView ivIcon;
        TextView tvPlatform;
        TextView tvPrice;
        TextView tvDeadline;
        TextView btnSend;
    }
}
