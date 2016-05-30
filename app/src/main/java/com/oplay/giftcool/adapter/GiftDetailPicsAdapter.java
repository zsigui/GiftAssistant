package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    final int LENGTH;

    private ArrayList<PhotoInfo> mPics;

    public GiftDetailPicsAdapter(Context context) {
        super(context);
        LENGTH = context.getResources().getDimensionPixelSize(R.dimen.di_detail_usage_pic_length);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageHolder holder = new ImageHolder(LayoutInflater.from(mContext).inflate(R.layout.view_banner_img, parent,
                false));
        ViewGroup.LayoutParams lp = holder.ivPic.getLayoutParams();
        lp.width = LENGTH;
        lp.height = LENGTH;
        holder.ivPic.setLayoutParams(lp);
        holder.ivPic.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ImageHolder imageHolder = (ImageHolder) holder;
        imageHolder.ivPic.setTag(TAG_POSITION, position);
        imageHolder.ivPic.setOnClickListener(this);
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

    @Override
    public void updateData(ArrayList<String> data) {
        super.updateData(data);
        if (getItemCount() != 0) {
            if (mPics == null) {
                mPics = new ArrayList<>();
            }
            mPics.clear();
            for (String s : data) {
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

        public ImageHolder(View itemView) {
            super(itemView);
            ivPic = getViewById(R.id.iv_image_view);
        }
    }
}
