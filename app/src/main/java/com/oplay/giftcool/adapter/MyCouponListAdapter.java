package com.oplay.giftcool.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的首充券列表内容适配器
 *
 * Created by zsigui on 16-1-7.
 */
public class MyCouponListAdapter extends BaseListAdapter<IndexGiftNew> implements View.OnClickListener {

	private int mType;

    public MyCouponListAdapter(Context context, List<IndexGiftNew> objects, int type) {
        super(context, objects);
        this.mType = type;
    }

    public void updateData(ArrayList<IndexGiftNew> data) {
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
            convertView = mLayoutInflater.inflate(R.layout.item_list_my_gift, parent, false);
            holder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
            holder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
            holder.tvPlatform = ViewUtil.getViewById(convertView, R.id.tv_platform);
            holder.tvPrice = ViewUtil.getViewById(convertView, R.id.tv_price);
            holder.tvDeadline = ViewUtil.getViewById(convertView, R.id.tv_deadline);
            holder.tvGiftCode = ViewUtil.getViewById(convertView, R.id.tv_gift_code);
            holder.btnCopy = ViewUtil.getViewById(convertView, R.id.btn_copy);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        IndexGiftNew o = getItem(position);
        holder.tvName.setText(String.format("[%s]%s", o.gameName, o.name));
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.tvPlatform.setText(o.platform);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
        holder.tvDeadline.setText(String.format("%s ~ %s", DateUtil.formatTime(o.useStartTime, "yyyy.MM.dd HH:mm"),
                DateUtil.formatTime(o.useEndTime, "yyyy.MM.dd HH:mm")));
        holder.tvGiftCode.setText(Html.fromHtml(String.format(ConstString.TEXT_GIFT_CODE, o.code)));
        if (mType == KeyConfig.TYPE_KEY_OVERTIME) {
            holder.btnCopy.setEnabled(false);
            holder.btnCopy.setText("已结束");
        } else {
            holder.btnCopy.setEnabled(true);
            holder.btnCopy.setText("复制");
        }
        holder.btnCopy.setOnClickListener(this);
        holder.btnCopy.setTag(TAG_POSITION, position);
        convertView.setOnClickListener(this);
        convertView.setTag(TAG_POSITION, position);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (mData == null || v.getTag(TAG_POSITION) == null) {
            return;
        }
        IndexGiftNew item = getItem((Integer)v.getTag(TAG_POSITION));
        switch (v.getId()) {
            case R.id.btn_copy:
                ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setPrimaryClip(ClipData.newPlainText("礼包码", item.code));
                ToastUtil.showShort("已复制");
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
        TextView tvGiftCode;
        TextView btnCopy;
    }
}
