package cn.finalteam.galleryfinal;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftcool.R;

import java.util.List;

import cn.finalteam.galleryfinal.adapter.PhotoPreviewAdapter;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.widget.GFViewPager;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2015/12/29 0029 14:43
 * Update:JackieZhuang
 * Date:2016/04/04
 */
//public class PhotoPreviewActivity extends PhotoBaseActivity implements ViewPager.OnPageChangeListener{
public class PhotoPreviewActivity extends PhotoBaseActivity {

    static final String PHOTO_LIST = "photo_list";
    static final String PHOTO_INDEX = "photo_index";

//    private RelativeLayout mTitleBar;
//    private ImageView mIvBack;
//    private TextView mTvTitle;
//    private TextView mTvIndicator;

    private SmartTabLayout mTab;
    private GFViewPager mVpPager;
    private List<PhotoInfo> mPhotoList;
    private PhotoPreviewAdapter mPhotoPreviewAdapter;

    private ThemeConfig mThemeConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        mThemeConfig = GalleryFinal.getGalleryTheme();

        if ( mThemeConfig == null) {
            resultFailureDelayed(getString(R.string.please_reopen_gf), true);
        } else {
            setContentView(R.layout.gf_activity_photo_preview);
            findViews();
            setListener();
            setTheme();

            mPhotoList = (List<PhotoInfo>) getIntent().getSerializableExtra(PHOTO_LIST);
            final int index = getIntent().getIntExtra(PHOTO_INDEX, 0);
            mPhotoPreviewAdapter = new PhotoPreviewAdapter(this, mPhotoList);
            mVpPager.setAdapter(mPhotoPreviewAdapter);
            mTab.setViewPager(mVpPager);
            if (index < mPhotoList.size()) {
                mVpPager.setCurrentItem(index);
            } else {
                mVpPager.setCurrentItem(mPhotoList.size() - 1);
            }
        }
    }

    private void findViews() {
//        mTitleBar = (RelativeLayout) findViewById(R.id.titlebar);
//        mIvBack = (ImageView) findViewById(R.id.iv_back);
//        mTvTitle = (TextView) findViewById(R.id.tv_title);
//        mTvIndicator = (TextView) findViewById(R.id.tv_indicator);
        mTab = (SmartTabLayout) findViewById(R.id.stl_tab);
        mVpPager = (GFViewPager) findViewById(R.id.vp_pager);
    }

    private void setListener() {
//        mTab.setOnPageChangeListener(this);
//        mVpPager.addOnPageChangeListener(this);
//        mIvBack.setOnClickListener(mBackListener);
    }

    private void setTheme() {
//        mIvBack.setImageResource(mThemeConfig.getIconBack());
//        if (mThemeConfig.getIconBack() == R.drawable.ic_gf_back) {
//            mIvBack.setColorFilter(mThemeConfig.getTitleBarIconColor());
//        }

//        mTitleBar.setBackgroundColor(mThemeConfig.getTitleBarBgColor());
//        mTvTitle.setTextColor(mThemeConfig.getTitleBarTextColor());
        if(mThemeConfig.getPreviewBg() != null) {
            mVpPager.setBackgroundDrawable(mThemeConfig.getPreviewBg());
        }
    }

    @Override
    protected void takeResult(PhotoInfo info) {
    }
//
//    @Override
//    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
////        mTvIndicator.setText((position + 1) + "/" + mPhotoList.size());
//    }
//
//    @Override
//    public void onPageSelected(int position) {
//
//    }
//
//    @Override
//    public void onPageScrollStateChanged(int state) {
//
//    }

//    private View.OnClickListener mBackListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            finish();
//        }
//    };

}