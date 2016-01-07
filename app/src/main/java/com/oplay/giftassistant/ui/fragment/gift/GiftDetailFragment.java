package com.oplay.giftassistant.ui.fragment.gift;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.util.DensityUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftDetailFragment extends BaseFragment{

    private static final String KEY_DATA = "key_data";

    private ImageView ivIcon;
    private ImageView ivLimit;
    private TextView tvName;
    private TextView tvScore;
    private TextView tvOr;
    private TextView tvBean;
    private TextView tvRemain;
    private TextView tvContent;
    private TextView tvDeadline;
    private TextView tvNote;
    private TextView btnSend;

    public static GiftDetailFragment newInstance(IndexGiftNew gift) {
        GiftDetailFragment fragment = new GiftDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DATA, gift);
        fragment.setArguments(bundle);
        return fragment;
    }

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_gift_detail);
        ivIcon = getViewById(R.id.iv_icon);
        ivLimit = getViewById(R.id.iv_limit);
        tvName = getViewById(R.id.tv_name);
        tvScore = getViewById(R.id.tv_score);
        tvOr = getViewById(R.id.tv_or);
        tvBean = getViewById(R.id.tv_bean);
        tvRemain = getViewById(R.id.tv_new);
        tvContent = getViewById(R.id.et_content);
        tvDeadline = getViewById(R.id.tv_deadline);
        tvNote = getViewById(R.id.tv_note);
        btnSend = getViewById(R.id.btn_send);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            throw new IllegalStateException("need to set data here");
        }
        Serializable s = getArguments().getSerializable(KEY_DATA);
        KLog.e(s);
        if (s != null) {
            IndexGiftNew gift = (IndexGiftNew) s;
            ImageLoader.getInstance().displayImage(gift.img, ivIcon, Global.IMAGE_OPTIONS);
            tvName.setText(String.format("[%s]%s", gift.gameName, gift.name));
            if (gift.isLimit) {
                ivLimit.setVisibility(View.VISIBLE);
                tvName.setPadding(DensityUtil.dip2px(getContext(), 4), 0, 0, 0);
            } else {
                ivLimit.setVisibility(View.GONE);
                tvName.setPadding(DensityUtil.dip2px(getContext(), 18), 0, 0, 0);
            }
            if (gift.priceType == GiftTypeUtil.PAY_TYPE_SCORE) {
                tvBean.setVisibility(View.GONE);
                tvOr.setVisibility(View.GONE);
                tvScore.setText(String.valueOf(gift.score));
            } else if (gift.priceType == GiftTypeUtil.PAY_TYPE_BEAN) {
                tvScore.setVisibility(View.GONE);
                tvOr.setVisibility(View.GONE);
                tvBean.setText(String.valueOf(gift.bean));
            } else {
                tvScore.setVisibility(View.VISIBLE);
                tvBean.setVisibility(View.VISIBLE);
                tvOr.setVisibility(View.VISIBLE);
                tvScore.setText(String.valueOf(gift.score));
                tvBean.setText(String.valueOf(gift.bean));
            }
            tvRemain.setText(Html.fromHtml(String.format("剩余 <font color='#ffaa17'>%d个</font>", gift.remainCount)));
            tvContent.setText(gift.content);
            tvDeadline.setText(gift.useDeadline);
            tvNote.setText(gift.note);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtil.showShort("抢号事件触发");
                }
            });
        }
	}

	@Override
	protected void lazyLoad() {

	}
}
