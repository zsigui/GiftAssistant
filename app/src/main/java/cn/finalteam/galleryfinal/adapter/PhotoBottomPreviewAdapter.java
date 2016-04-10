package cn.finalteam.galleryfinal.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;

import java.util.ArrayList;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.widget.GFImageView;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/4/10
 */
public class PhotoBottomPreviewAdapter extends BaseRVAdapter<PhotoInfo> implements View.OnClickListener {

    private final int imgLength;

    public PhotoBottomPreviewAdapter(Context context) {
        this(context, null);
    }

    public PhotoBottomPreviewAdapter(Context context, ArrayList<PhotoInfo> data) {
        super(context, data);
        imgLength = context.getResources().getDimensionPixelSize(R.dimen.gf_img_height);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PreviewViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.gf_adapter_bottom_photo_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PreviewViewHolder viewHolder = (PreviewViewHolder) holder;
        if (position == getItemCount() - 1) {
            viewHolder.mIvThumb.setImageResource(R.drawable.shape_dash_border);
        } else {
            PhotoInfo photoInfo = getItem(position);
            String path = "";
            if (photoInfo != null) {
                path = photoInfo.getPhotoPath();
            }
            GalleryFinal.getCoreConfig().getImageLoader().displayImage((Activity) mContext, path, viewHolder.mIvThumb,
                    null, imgLength, imgLength);
            viewHolder.mIvThumb.setOnClickListener(this);
            viewHolder.mIvThumb.setTag(TAG_POSITION, position);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) {
            return;
        }
        if (mListener != null) {
            final int pos = (int) v.getTag(TAG_POSITION);
            mListener.onItemClick(getItem(pos), v, pos);
        }
    }

    static class PreviewViewHolder extends BaseRVHolder{

        GFImageView mIvThumb;

        public PreviewViewHolder(View view) {
            super(view);
            mIvThumb = (GFImageView) view.findViewById(R.id.iv_thumb);
        }
    }
}
