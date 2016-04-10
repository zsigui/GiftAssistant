/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.finalteam.galleryfinal.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.oplay.giftcool.R;

import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.listener.OnListItemClickListener;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.widget.GFImageView;

/**
 * Desction:
 * Author:pengjianbo
 * Date:15/10/10 下午4:59
 */
public class PhotoListAdapter extends ViewHolderAdapter<PhotoListAdapter.PhotoViewHolder, PhotoInfo> implements View.OnClickListener {
    private final int TAG_POSITION = 0x234fff3f;

    private OnListItemClickListener mListItemClickListener;
    private List<PhotoInfo> mSelectList;
    private int mScreenWidth;
    private int mRowWidth;

    private Activity mActivity;

    public PhotoListAdapter(Activity activity, List<PhotoInfo> list, List<PhotoInfo> selectList, int screenWidth) {
        super(activity, list);
        this.mSelectList = selectList;
        this.mScreenWidth = screenWidth;
        this.mRowWidth = mScreenWidth / 3;
        this.mActivity = activity;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = inflate(R.layout.gf_adapter_photo_list_item, parent);
        setHeight(view);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {

        if (position == 0) {
            holder.mLlCheck.setVisibility(View.GONE);
            holder.mIvThumb.setImageResource(R.drawable.ic_photo_take);
            holder.mView.setBackgroundResource(R.color.co_opacity_70);
            holder.mIvThumb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {

            PhotoInfo photoInfo = getDatas().get(position - 1);

            String path = "";
            if (photoInfo != null) {
                path = photoInfo.getPhotoPath();
            }

            GalleryFinal.getCoreConfig().getImageLoader().displayImage(mActivity, path, holder.mIvThumb, null, mRowWidth, mRowWidth);
//        holder.mView.setAnimation(null);
//        if (GalleryFinal.getCoreConfig().getAnimation() > 0) {
//            holder.mView.setAnimation(AnimationUtils.loadAnimation(mActivity, GalleryFinal.getCoreConfig().getAnimation()));
//        }
            holder.mIvThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.mIvCheck.setImageResource(GalleryFinal.getGalleryTheme().getIconCheck());
            if (GalleryFinal.getFunctionConfig().isMutiSelect()) {
                holder.mLlCheck.setVisibility(View.VISIBLE);
                holder.mLlCheck.setTag(holder.mIvCheck);
                holder.mLlCheck.setTag(TAG_POSITION, position);
                holder.mLlCheck.setOnClickListener(this);
                if (mSelectList.contains(photoInfo)) {
                    holder.mIvCheck.setSelected(true);
                } else {
                    holder.mIvCheck.setSelected(false);
                }
            } else {
                holder.mLlCheck.setVisibility(View.GONE);
            }
        }
        holder.mIvThumb.setTag(TAG_POSITION, position);
        holder.mIvThumb.setOnClickListener(this);
    }

    public void setListItemClickListener(OnListItemClickListener listItemClickListener) {
        mListItemClickListener = listItemClickListener;
    }

    private void setHeight(final View convertView) {
        int height = mScreenWidth / 3 - 8;
        convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) {
            return;
        }
        final int pos = (int) v.getTag(TAG_POSITION);
        if (mListItemClickListener != null) {
            mListItemClickListener.onItemClick(v, pos, getItemId(pos));
        }
    }

    public static class PhotoViewHolder extends ViewHolderAdapter.ViewHolder {

        public GFImageView mIvThumb;
        public ImageView mIvCheck;
        public LinearLayout mLlCheck;
        View mView;

        public PhotoViewHolder(View view) {
            super(view);
            mView = view;
            mIvThumb = (GFImageView) view.findViewById(R.id.iv_thumb);
            mIvCheck = (ImageView) view.findViewById(R.id.iv_check);
            mLlCheck = (LinearLayout) view.findViewById(R.id.ll_check);
        }
    }
}
