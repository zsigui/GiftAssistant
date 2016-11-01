package cn.finalteam.galleryfinal.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.widget.zoonview.PhotoView;
import cn.finalteam.galleryfinal.widget.zoonview.PhotoViewAttacher;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2015/12/29 0029 15:53
 */
public class PhotoPreviewAdapter extends ViewHolderRecyclingPagerAdapter<PhotoPreviewAdapter.PreviewViewHolder,
        PhotoInfo> implements PhotoViewAttacher.OnPhotoTapListener {

    private Activity mActivity;
    private DisplayMetrics mDisplayMetrics;

    public PhotoPreviewAdapter(Activity activity, List<PhotoInfo> list) {
        super(activity, list);
        this.mActivity = activity;
        this.mDisplayMetrics = mActivity.getResources().getDisplayMetrics();
    }

    @Override
    public PreviewViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = getLayoutInflater().inflate(R.layout.gf_adapter_preview_viewpgaer_item, parent, false);
        return new PreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PreviewViewHolder holder, int position) {
        PhotoInfo photoInfo = getDatas().get(position);
        String path = "";
        if (photoInfo != null) {
            path = photoInfo.getPhotoPath();
        }
//        holder.mImageView.setImageResource(R.drawable.ic_gf_default_photo);
//        Drawable defaultDrawable = mActivity.getResources().getDrawable(R.drawable.ic_gf_default_photo);
        GalleryFinal.getCoreConfig().getImageLoader().displayImage(mActivity, path, holder.pvView, null,
                mDisplayMetrics.widthPixels / 2, mDisplayMetrics.heightPixels / 2,
                new ImageLoadListener(holder.pvView, holder.pbLoad));
        holder.pvView.setOnPhotoTapListener(this);
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        if (mActivity != null) {
            mActivity.finish();
        }
    }

    static class PreviewViewHolder extends ViewHolderRecyclingPagerAdapter.ViewHolder {
        PhotoView pvView;
        ProgressBar pbLoad;

        public PreviewViewHolder(View view) {
            super(view);
            pvView = (PhotoView) view.findViewById(R.id.pv_view);
            pbLoad = (ProgressBar) view.findViewById(R.id.pb_load);
        }
    }

    static class ImageLoadListener implements ImageLoadingListener {

        WeakReference<ImageView> iv;
        WeakReference<ProgressBar> pb;

        ImageLoadListener(ImageView iv, ProgressBar pb) {
            this.iv = new WeakReference<ImageView>(iv);
            this.pb = new WeakReference<ProgressBar>(pb);
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            ImageView iv = this.iv.get();
            if (iv != null) {
                iv.setVisibility(View.INVISIBLE);
            }
            ProgressBar pb = this.pb.get();
            if (pb != null) {
                pb.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            ProgressBar pb = this.pb.get();
            if (pb != null) {
                pb.setVisibility(View.GONE);
            }
            ImageView iv = this.iv.get();
            if (iv != null) {
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(R.drawable.ic_gf_default_photo);
            }
            AppDebugConfig.d(AppDebugConfig.TAG_ADAPTER, "PhotoPreviewAdapter.loadFailReason = " + failReason);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ProgressBar pb = this.pb.get();
            if (pb != null) {
                pb.setVisibility(View.GONE);
            }
            ImageView iv = this.iv.get();
            if (iv != null) {
                iv.setVisibility(View.VISIBLE);
                iv.setImageBitmap(loadedImage);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }
}
