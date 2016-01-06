package com.oplay.giftassistant.ui.fragment.gift;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.TimeViewPagerAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.data.resp.TimeDataList;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftMutilDayFragment extends BaseFragment {

    private static final String KEY_DATA = "key_data";
    private static final String KEY_URL = "key_url";

    private ViewPager mPager;
    private SmartTabLayout mTabLayout;
    private String mUrl;

    public static GiftMutilDayFragment newInstance(ArrayList<TimeDataList<IndexGiftNew>> data, String url) {
        GiftMutilDayFragment fragment = new GiftMutilDayFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DATA, data);
        bundle.putString(KEY_URL, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_vp_container);
        mPager = getViewById(R.id.vp_container);
        mTabLayout = getViewById(R.id.tab_layout);
    }

    @Override
    protected void setListener() {
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            throw new IllegalStateException("need to put argument's type of ArrayList<ArrayList<IndexGiftNew>> on it");
        }
        Serializable s = getArguments().getSerializable(KEY_DATA);
        if (s == null) {
            throw new IllegalArgumentException("need to put argument's type of ArrayList<ArrayList<IndexGiftNew>> on it");
        }
        ArrayList<TimeDataList<IndexGiftNew>> data = (ArrayList<TimeDataList<IndexGiftNew>>) s;
        mUrl = getArguments().getString(KEY_URL);
        Collections.sort(data, new Comparator<TimeDataList<IndexGiftNew>>() {
            @Override
            public int compare(TimeDataList<IndexGiftNew> lhs, TimeDataList<IndexGiftNew> rhs) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                //将字符串形式的时间转化为Date类型的时间
                try {
                    Date a = sdf.parse(lhs.date);
                    Date b = sdf.parse(rhs.date);

                    //Date类的一个方法，如果a早于b返回true，否则返回false
                    if (a.before(b))
                        return 1;
                    else
                        return -1;
                } catch (Throwable e) {
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.e(AppDebugConfig.TAG_FRAG, e);
                    }
                }
                return -1;
            }
        });
        ArrayList<Fragment> fragments = new ArrayList<>(data.size());
        for (TimeDataList<IndexGiftNew> d : data) {
            Fragment f = GiftListDataFragment.newInstance(d.data, d.date, mUrl);
            fragments.add(f);
        }

        TimeViewPagerAdapter adapter = new TimeViewPagerAdapter(getChildFragmentManager(), fragments);
        mPager.setAdapter(adapter);
        mTabLayout.setViewPager(mPager);
    }

    @Override
    protected void lazyLoad() {
        mHasData = true;
    }

}
