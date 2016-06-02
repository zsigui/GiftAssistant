package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by zsigui on 16-5-5.
 */
public class GiftDetailPicsAdapter extends BaseRVAdapter<String> implements View.OnClickListener {

    private ArrayList<PhotoInfo> mPics;

    public GiftDetailPicsAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageHolder(LayoutInflater.from(mContext).inflate(
                R.layout.item_list_gift_detail_usage, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ImageHolder imageHolder = (ImageHolder) holder;
        imageHolder.ivPic.setTag(TAG_POSITION, position);
        imageHolder.ivPic.setOnClickListener(this);
        imageHolder.tvNumber.setText(String.valueOf(position + 1));
        ViewUtil.showImage(imageHolder.ivPic, getItem(position));
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) {
            return;
        }
        Integer pos = (Integer) v.getTag(TAG_POSITION);
        GalleryFinal.openMultiPhoto(pos, mPics);
    }

    public void updateData(ArrayList<String> thumbData, ArrayList<String> originData) {
        this.mData = thumbData;
        notifyDataSetChanged();
        if (getItemCount() != 0) {
            if (mPics == null) {
                mPics = new ArrayList<>();
            }
            mPics.clear();
            for (String s : originData) {
                PhotoInfo p = new PhotoInfo();
                p.setPhotoId(s.hashCode());
                p.setPhotoPath(s);
                mPics.add(p);
            }
        }
    }

    @Override
    public void release() {
        super.release();
        mPics = null;
    }

    private static class ImageHolder extends BaseRVHolder {

        private ImageView ivPic;
        private TextView tvNumber;

        public ImageHolder(View itemView) {
            super(itemView);
            ivPic = getViewById(R.id.iv_thumb);
            tvNumber = getViewById(R.id.tv_number);
        }
    }
}
