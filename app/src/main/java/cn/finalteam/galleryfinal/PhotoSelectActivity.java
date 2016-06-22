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

package cn.finalteam.galleryfinal;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.util.FileUtil;
import com.ta.utdid2.android.utils.StringUtils;

import net.youmi.android.libs.common.util.Util_System_SDCard_Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.finalteam.galleryfinal.adapter.FolderListAdapter;
import cn.finalteam.galleryfinal.adapter.PhotoBottomPreviewAdapter;
import cn.finalteam.galleryfinal.adapter.PhotoListAdapter;
import cn.finalteam.galleryfinal.listener.OnListItemClickListener;
import cn.finalteam.galleryfinal.model.PhotoFolderInfo;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.permission.AfterPermissionGranted;
import cn.finalteam.galleryfinal.permission.EasyPermissions;
import cn.finalteam.galleryfinal.utils.PhotoTools;

/**
 * Desction:图片选择器
 * Author:pengjianbo
 * Date:15/10/10 下午3:54
 */
public class PhotoSelectActivity extends PhotoBaseActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener, OnListItemClickListener, OnItemClickListener<PhotoInfo> {

    private final int HANLDER_TAKE_PHOTO_EVENT = 1000;
    private final int HANDLER_REFRESH_LIST_EVENT = 1002;

    private GridView mGvPhotoList;
    private ListView mLvFolderList;
    private LinearLayout mLlFolderPanel;
    private ImageView mIvTakePhoto;
    private ImageView mIvBack;
    private ImageView mIvClear;
    private ImageView mIvPreView;
    private TextView mTvChooseCount;
    private TextView mTvSubTitle;
    private LinearLayout mLlTitle;
    //    private FloatingActionButton mFabOk;
    private TextView mTvEmptyView;
    private RelativeLayout mTitlebar;
    private TextView mTvTitle;
    private ImageView mIvFolderArrow;
    private TextView mTvOk;
    private RecyclerView mRlPhotoList;

    private List<PhotoFolderInfo> mAllPhotoFolderList;
    private FolderListAdapter mFolderListAdapter;

    private List<PhotoInfo> mCurPhotoList;
    private PhotoListAdapter mPhotoListAdapter;
    private PhotoBottomPreviewAdapter mPhotoBottomPreviewAdapter;


    private ArrayList<PhotoInfo> mPreviewPhotos;
    private ArrayList<PhotoInfo> mSelectedFirst;

    //是否需要刷新相册
    private boolean mHasRefreshGallery = false;
    private ArrayList<PhotoInfo> mSelectPhotoList = new ArrayList<>();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selectPhotoMap", mSelectPhotoList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectPhotoList = (ArrayList<PhotoInfo>) getIntent().getSerializableExtra("selectPhotoMap");
    }

    private Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == HANLDER_TAKE_PHOTO_EVENT) {
                PhotoInfo photoInfo = (PhotoInfo) msg.obj;
                takeRefreshGallery(photoInfo);
                refreshSelectCount();
            } else if (msg.what == HANDLER_REFRESH_LIST_EVENT) {
                refreshSelectCount();
                mPhotoListAdapter.notifyDataSetChanged();
                mFolderListAdapter.notifyDataSetChanged();
                mPhotoBottomPreviewAdapter.notifyItemRangeChanged(0, mSelectPhotoList.size());
                if (mAllPhotoFolderList == null
                        || mAllPhotoFolderList.size() <= 0
                        || mAllPhotoFolderList.get(0).getPhotoList() == null
                        || mAllPhotoFolderList.get(0).getPhotoList().size() == 0) {
                    mTvEmptyView.setText(R.string.no_photo);
                }

                mGvPhotoList.setEnabled(true);
                mLlTitle.setEnabled(true);
                mIvTakePhoto.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GalleryFinal.getFunctionConfig() == null || GalleryFinal.getGalleryTheme() == null) {
            resultFailureDelayed(getString(R.string.please_reopen_gf), true);
        } else {
            setContentView(R.layout.gf_activity_photo_select);
            mPhotoTargetFolder = null;

            findViews();

            mAllPhotoFolderList = new ArrayList<>();
            mFolderListAdapter = new FolderListAdapter(this, mAllPhotoFolderList, GalleryFinal.getFunctionConfig());
            mLvFolderList.setAdapter(mFolderListAdapter);

            mCurPhotoList = new ArrayList<>();
            mPhotoListAdapter = new PhotoListAdapter(this, mCurPhotoList, mSelectPhotoList, mScreenWidth);
            mGvPhotoList.setAdapter(mPhotoListAdapter);

            mPhotoBottomPreviewAdapter = new PhotoBottomPreviewAdapter(this, mSelectPhotoList);
            LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                    false);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                    llm.getOrientation(),
                    getResources().getColor(R.color.co_white),
                    getResources().getDimensionPixelSize(R.dimen.gf_img_padding));
            mRlPhotoList.setLayoutManager(llm);
            mRlPhotoList.addItemDecoration(dividerItemDecoration);
            mRlPhotoList.setAdapter(mPhotoBottomPreviewAdapter);
            setListener();

//            if (GalleryFinal.getFunctionConfig().isMutiSelect()) {
//                mTvChooseCount.setVisibility(View.VISIBLE);
//                mFabOk.setVisibility(View.VISIBLE);
//            }

            setTheme();
            mGvPhotoList.setEmptyView(mTvEmptyView);

            if (GalleryFinal.getFunctionConfig().isCamera()) {
                mIvTakePhoto.setVisibility(View.VISIBLE);
            } else {
                mIvTakePhoto.setVisibility(View.GONE);
            }

            mSelectedFirst = (ArrayList<PhotoInfo>) getIntent().getSerializableExtra(GalleryFinal.PHOTO_LIST);
            refreshSelectCount();
            requestGalleryPermission();

            mGvPhotoList.setOnScrollListener(GalleryFinal.getCoreConfig().getPauseOnScrollListener());
        }

        Global.mPhotoSelectActivity = this;
    }

    private void setTheme() {
        mIvBack.setImageResource(GalleryFinal.getGalleryTheme().getIconBack());
        if (GalleryFinal.getGalleryTheme().getIconBack() == R.drawable.ic_gf_back) {
            mIvBack.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
        }

        mIvFolderArrow.setImageResource(GalleryFinal.getGalleryTheme().getIconFolderArrow());
        if (GalleryFinal.getGalleryTheme().getIconFolderArrow() == R.drawable.ic_gf_triangle_arrow) {
            mIvFolderArrow.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
        }

        mIvClear.setImageResource(GalleryFinal.getGalleryTheme().getIconClear());
        if (GalleryFinal.getGalleryTheme().getIconClear() == R.drawable.ic_gf_clear) {
            mIvClear.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
        }

        mIvPreView.setImageResource(GalleryFinal.getGalleryTheme().getIconPreview());
        if (GalleryFinal.getGalleryTheme().getIconPreview() == R.drawable.ic_gf_preview) {
            mIvPreView.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
        }

        mIvTakePhoto.setImageResource(GalleryFinal.getGalleryTheme().getIconCamera());
        if (GalleryFinal.getGalleryTheme().getIconCamera() == R.drawable.ic_gf_camera) {
            mIvTakePhoto.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
        }
//        mFabOk.setIcon(GalleryFinal.getGalleryTheme().getIconFab());

        mTitlebar.setBackgroundColor(GalleryFinal.getGalleryTheme().getTitleBarBgColor());
        mTvSubTitle.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
        mTvTitle.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
        mTvChooseCount.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
//        mFabOk.setColorPressed(GalleryFinal.getGalleryTheme().getFabPressedColor());
//        mFabOk.setColorNormal(GalleryFinal.getGalleryTheme().getFabNornalColor());
    }

    private void findViews() {
        mGvPhotoList = (GridView) findViewById(R.id.gv_photo_list);
        mLvFolderList = (ListView) findViewById(R.id.lv_folder_list);
        mTvSubTitle = (TextView) findViewById(R.id.tv_sub_title);
        mLlFolderPanel = (LinearLayout) findViewById(R.id.ll_folder_panel);
        mIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);
        mTvChooseCount = (TextView) findViewById(R.id.tv_choose_count);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
//        mFabOk = (FloatingActionButton) findViewById(R.id.fab_ok);
        mTvEmptyView = (TextView) findViewById(R.id.tv_empty_view);
        mLlTitle = (LinearLayout) findViewById(R.id.ll_title);
        mIvClear = (ImageView) findViewById(R.id.iv_clear);
        mTitlebar = (RelativeLayout) findViewById(R.id.titlebar);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mIvFolderArrow = (ImageView) findViewById(R.id.iv_folder_arrow);
        mIvPreView = (ImageView) findViewById(R.id.iv_preview);
        mTvOk = (TextView) findViewById(R.id.tv_ok);
        mRlPhotoList = (RecyclerView) findViewById(R.id.rl_bottom_preview);
    }

    private void setListener() {
        mLlTitle.setOnClickListener(this);
        mIvTakePhoto.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mIvFolderArrow.setOnClickListener(this);

        mLvFolderList.setOnItemClickListener(this);
        mPhotoListAdapter.setListItemClickListener(this);
        mPhotoBottomPreviewAdapter.setListener(this);
        mTvOk.setOnClickListener(this);
        mIvClear.setOnClickListener(this);
        mIvPreView.setOnClickListener(this);
    }

    protected void deleteSelect(int photoId) {
        try {
            for (Iterator<PhotoInfo> iterator = mSelectPhotoList.iterator(); iterator.hasNext(); ) {
                PhotoInfo info = iterator.next();
                if (info != null && info.getPhotoId() == photoId) {
                    iterator.remove();
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        refreshAdapter();
    }

    private void refreshAdapter() {
        mHanlder.sendEmptyMessageDelayed(HANDLER_REFRESH_LIST_EVENT, 100);
    }

    protected void takeRefreshGallery(PhotoInfo photoInfo, boolean selected) {
        if (isFinishing() || photoInfo == null) {
            return;
        }

        Message message = mHanlder.obtainMessage();
        message.obj = photoInfo;
        message.what = HANLDER_TAKE_PHOTO_EVENT;
        mSelectPhotoList.add(photoInfo);
        mHanlder.sendMessageDelayed(message, 100);
    }

    /**
     * 解决在5.0手机上刷新Gallery问题，从startActivityForResult回到Activity把数据添加到集合中然后理解跳转到下一个页面，
     * adapter的getCount与list.size不一致，所以我这里用了延迟刷新数据
     *
     * @param photoInfo
     */
    private void takeRefreshGallery(PhotoInfo photoInfo) {
        mCurPhotoList.add(0, photoInfo);
        mPhotoListAdapter.notifyDataSetChanged();
        if (mSelectPhotoList != null && mSelectPhotoList.size() > 0)
            mPhotoBottomPreviewAdapter.notifyItemRangeChanged(0, mSelectPhotoList.size());

        //添加到集合中
        List<PhotoInfo> photoInfoList = mAllPhotoFolderList.get(0).getPhotoList();
        if (photoInfoList == null) {
            photoInfoList = new ArrayList<>();
        }
        photoInfoList.add(0, photoInfo);
        mAllPhotoFolderList.get(0).setPhotoList(photoInfoList);

        if (mFolderListAdapter.getSelectFolder() != null) {
            PhotoFolderInfo photoFolderInfo = mFolderListAdapter.getSelectFolder();
            List<PhotoInfo> list = photoFolderInfo.getPhotoList();
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(0, photoInfo);
            if (list.size() == 1) {
                photoFolderInfo.setCoverPhoto(photoInfo);
            }
            mFolderListAdapter.getSelectFolder().setPhotoList(list);
        } else {
            String folderA = new File(photoInfo.getPhotoPath()).getParent();
            for (int i = 1; i < mAllPhotoFolderList.size(); i++) {
                PhotoFolderInfo folderInfo = mAllPhotoFolderList.get(i);
                String folderB = null;
                if (!StringUtils.isEmpty(photoInfo.getPhotoPath())) {
                    folderB = new File(photoInfo.getPhotoPath()).getParent();
                }
                if (TextUtils.equals(folderA, folderB)) {
                    List<PhotoInfo> list = folderInfo.getPhotoList();
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(0, photoInfo);
                    folderInfo.setPhotoList(list);
                    if (list.size() == 1) {
                        folderInfo.setCoverPhoto(photoInfo);
                    }
                }
            }
        }

        mFolderListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void takeResult(PhotoInfo photoInfo) {
        Message message = mHanlder.obtainMessage();
        message.obj = photoInfo;
        message.what = HANLDER_TAKE_PHOTO_EVENT;

        if (!GalleryFinal.getFunctionConfig().isMutiSelect()) { //单选
            mSelectPhotoList.clear();
            mSelectPhotoList.add(photoInfo);

            if (GalleryFinal.getFunctionConfig().isEditPhoto()) {//裁剪
                mHasRefreshGallery = true;
                toPhotoEdit();
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<>();
                list.add(photoInfo);
                resultData(list);
            }

            mHanlder.sendMessageDelayed(message, 100);
        } else {//多选
            mSelectPhotoList.add(photoInfo);
            mHanlder.sendMessageDelayed(message, 100);
        }
    }

    /**
     * 执行裁剪
     */
    protected void toPhotoEdit() {
        Intent intent = new Intent(this, PhotoEditActivity.class);
        intent.putExtra(PhotoEditActivity.SELECT_MAP, mSelectPhotoList);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_title || id == R.id.iv_folder_arrow) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
                mLlFolderPanel.setVisibility(View.GONE);
                mLlFolderPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.gf_flip_horizontal_out));
            } else {
                mLlFolderPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.gf_flip_horizontal_in));
                mLlFolderPanel.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.iv_take_photo) {
            //判断是否达到多选最大数量
            if (GalleryFinal.getFunctionConfig().isMutiSelect() && mSelectPhotoList.size() == GalleryFinal.getFunctionConfig().getMaxSize()) {
                toast(getString(R.string.select_max_tips));
                return;
            }

            if (!Util_System_SDCard_Util.IsSdCardCanRead()) {
                toast(getString(R.string.empty_sdcard));
                return;
            }

            takePhotoAction();
        } else if (id == R.id.iv_back) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
                mLlTitle.performClick();
            } else {
                finish();
            }
        } else if (id == R.id.tv_ok) {
            if (mSelectPhotoList.size() > 0) {
                if (!GalleryFinal.getFunctionConfig().isEditPhoto()) {
                    resultData(mSelectPhotoList);
                } else {
                    toPhotoEdit();
                }
            } else {
                toast(getString(R.string.photo_at_least));
            }
        } else if (id == R.id.iv_clear) {
            mSelectPhotoList.clear();
            mPhotoListAdapter.notifyDataSetChanged();
            if (mSelectPhotoList != null && mSelectPhotoList.size() > 0)
                mPhotoBottomPreviewAdapter.notifyItemRangeChanged(0, mSelectPhotoList.size());
            refreshSelectCount();
        } else if (id == R.id.iv_preview) {
            if (mSelectPhotoList == null || mSelectPhotoList.size() == 0) {
                toast(getString(R.string.photo_at_least));
            } else {
                Intent intent = new Intent(this, PhotoPreviewActivity.class);
                intent.putExtra(GalleryFinal.PHOTO_LIST, mSelectPhotoList);
                startActivity(intent);
            }
        }
    }

    /**
     * 相册点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        folderItemClick(position);
    }

    /**
     * 照片列表点击事件
     */
    @Override
    public void onItemClick(View view, int position, long id) {
        photoItemClick(view, position);
    }

    /**
     * 预览照片墙点击事件
     */
    @Override
    public void onItemClick(PhotoInfo item, View view, int position) {
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtra(GalleryFinal.PHOTO_LIST, mSelectPhotoList);
        intent.putExtra(GalleryFinal.PHOTO_INDEX, position);
        intent.putExtra(GalleryFinal.PHOTO_SHOW_TITLE, true);
        intent.putExtra(GalleryFinal.PHOTO_SHOW_TAB, false);
        startActivity(intent);
    }

    private void folderItemClick(int position) {
        mLlFolderPanel.setVisibility(View.GONE);
        mCurPhotoList.clear();
        PhotoFolderInfo photoFolderInfo = mAllPhotoFolderList.get(position);
        if (photoFolderInfo.getPhotoList() != null) {
            mCurPhotoList.addAll(photoFolderInfo.getPhotoList());
        }
        mPhotoListAdapter.notifyDataSetChanged();

        if (position == 0) {
            mPhotoTargetFolder = null;
        } else {
            PhotoInfo photoInfo = photoFolderInfo.getCoverPhoto();
            if (photoInfo != null && !StringUtils.isEmpty(photoInfo.getPhotoPath())) {
                mPhotoTargetFolder = new File(photoInfo.getPhotoPath()).getParent();
            } else {
                mPhotoTargetFolder = null;
            }
        }
        mTvSubTitle.setText(photoFolderInfo.getFolderName());
        mFolderListAdapter.setSelectFolder(photoFolderInfo);
        mFolderListAdapter.notifyDataSetChanged();

        if (mCurPhotoList.size() == 0) {
            mTvEmptyView.setText(R.string.no_photo);
        }
    }

    private void photoItemClick(View view, int position) {
        if (position == 0) {
            // 进行拍照
            takePhotoAction();
            return;
        }
        if (mCurPhotoList == null || mCurPhotoList.size() == 0) {
            return;
        }
        PhotoInfo info = mCurPhotoList.get(position - 1);
        switch (view.getId()) {
            case R.id.ll_check:
                // 进行图片选中处理
                photoChecked(view, info);
                break;
            case R.id.iv_thumb:
                // 进行图片预览
                photoClicked(info);
                break;
        }
    }

    /**
     * 对点击的图片执行预览操作
     */
    private void photoClicked(PhotoInfo info) {
        if (mPreviewPhotos == null) {
            mPreviewPhotos = new ArrayList<>(1);
        } else {
            mPreviewPhotos.clear();
        }
        mPreviewPhotos.add(info);
        mHasRefreshGallery = false;
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtra(GalleryFinal.PHOTO_LIST, mPreviewPhotos);
        intent.putExtra(GalleryFinal.PHOTO_SHOW_TITLE, true);
        intent.putExtra(GalleryFinal.PHOTO_SHOW_TAB, false);
        startActivity(intent);
    }

    /**
     * 对选中的图片进行相应的勾选处理设置
     */
    private void photoChecked(View view, PhotoInfo info) {
        if (!GalleryFinal.getFunctionConfig().isMutiSelect()) {
            mSelectPhotoList.clear();
            mSelectPhotoList.add(info);
            String ext = FileUtil.getExtension(info.getPhotoPath());
            if (GalleryFinal.getFunctionConfig().isEditPhoto() && (ext.equalsIgnoreCase("png")
                    || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))) {
                toPhotoEdit();
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<>();
                list.add(info);
                resultData(list);
            }
            return;
        }
        boolean checked = false;
        int posInBottom = 0;
        if (!mSelectPhotoList.contains(info)) {
            if (GalleryFinal.getFunctionConfig().isMutiSelect() && mSelectPhotoList.size() == GalleryFinal.getFunctionConfig().getMaxSize()) {
                toast(getString(R.string.select_max_tips));
                return;
            } else {
                posInBottom = mSelectPhotoList.size();
                mSelectPhotoList.add(info);
                checked = true;
            }
        } else {
            try {
                for (Iterator<PhotoInfo> iterator = mSelectPhotoList.iterator(); iterator.hasNext(); ) {
                    PhotoInfo pi = iterator.next();
                    if (pi != null && TextUtils.equals(pi.getPhotoPath(), info.getPhotoPath())) {
                        iterator.remove();
                        break;
                    }
                    posInBottom++;
                }
            } catch (Exception e) {
            }
            checked = false;
        }
        refreshSelectCount();

        if (checked) {
            mPhotoBottomPreviewAdapter.notifyItemInserted(posInBottom);
        } else {
            mPhotoBottomPreviewAdapter.notifyItemRemoved(posInBottom);
        }
        ImageView ivCheck = (ImageView) view.getTag();
        if (ivCheck != null) {
            if (checked) {
                ivCheck.setSelected(true);
            } else {
                ivCheck.setSelected(false);
            }
        } else {
            mPhotoListAdapter.notifyDataSetChanged();
        }
    }

    public void refreshSelectCount() {
        mTvOk.setText(getString(R.string.selected, mSelectPhotoList.size(), GalleryFinal.getFunctionConfig().getMaxSize()));
//        mTvChooseCount.setText(getString(R.string.selected, mSelectPhotoList.size(), GalleryFinal.getFunctionConfig().getMaxSize()));
//        if (mSelectPhotoList.size() > 0 && GalleryFinal.getFunctionConfig().isMutiSelect()) {
//            mIvClear.setVisibility(View.VISIBLE);
//        } else {
//            mIvClear.setVisibility(View.GONE);
//        }

        if (GalleryFinal.getFunctionConfig().isEnablePreview()) {
            mIvPreView.setVisibility(View.VISIBLE);
        } else {
            mIvPreView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPermissionsGranted(List<String> list) {
        getPhotos();
    }

    @Override
    public void onPermissionsDenied(List<String> list) {
        mTvEmptyView.setText(R.string.permissions_denied_tips);
        mIvTakePhoto.setVisibility(View.GONE);
    }

    /**
     * 获取所有图片
     */
    @AfterPermissionGranted(GalleryFinal.PERMISSIONS_CODE_GALLERY)
    private void requestGalleryPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            getPhotos();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_tips_gallery),
                    GalleryFinal.PERMISSIONS_CODE_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * 判断列表中是否存在指定key值的图片信息项，返回查找到的项
     */
    public PhotoInfo existKey(List<PhotoInfo> infos, Object key) {
        if (infos != null) {
            if (key instanceof Integer) {
                for (PhotoInfo info : infos) {
                    if (info.getPhotoId() == (int) key) {
                        return info;
                    }
                }
            } else if (key instanceof String) {
                for (PhotoInfo info : infos) {
                    if (info.getPhotoPath().equalsIgnoreCase((String) key)) {
                        return info;
                    }
                }
            } else if (key instanceof PhotoInfo) {
                final PhotoInfo keyInfo = (PhotoInfo) key;
                for (PhotoInfo info : infos) {
                    if (info.equals(keyInfo)
                            || (keyInfo.getPhotoId() != 0
                            && info.getPhotoId() == keyInfo.getPhotoId())
                            || (keyInfo.getPhotoPath() != null
                            && keyInfo.getPhotoPath().equalsIgnoreCase(info.getPhotoPath()))) {
                        return info;
                    }
                }
            }
        }
        return null;
    }

    private void getPhotos() {
        mTvEmptyView.setText(R.string.waiting);
        mGvPhotoList.setEnabled(false);
        mLlTitle.setEnabled(false);
        mIvTakePhoto.setEnabled(false);
        new Thread() {
            @Override
            public void run() {
                super.run();

                mAllPhotoFolderList.clear();
                List<PhotoFolderInfo> allFolderList = PhotoTools.getAllPhotoFolder(PhotoSelectActivity.this, mSelectPhotoList);
                mAllPhotoFolderList.addAll(allFolderList);

                mCurPhotoList.clear();
                if (allFolderList.size() > 0) {
                    if (allFolderList.get(0).getPhotoList() != null) {
                        mCurPhotoList.addAll(allFolderList.get(0).getPhotoList());
                    }
                }
                if (mSelectedFirst != null && mCurPhotoList != null) {
                    for (Object id : mSelectedFirst) {
                        PhotoInfo obj = existKey(mCurPhotoList, id);
                        if (obj != null) {
                            mSelectPhotoList.add(obj);
                        }
                    }
                    mSelectedFirst = null;
                }
                refreshAdapter();
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
                mLlTitle.performClick();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHasRefreshGallery) {
            mHasRefreshGallery = false;
            requestGalleryPermission();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (GalleryFinal.getCoreConfig() != null &&
                GalleryFinal.getCoreConfig().getImageLoader() != null) {
            GalleryFinal.getCoreConfig().getImageLoader().clearMemoryCache();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoTargetFolder = null;
        mSelectPhotoList.clear();
        System.gc();
    }

}
