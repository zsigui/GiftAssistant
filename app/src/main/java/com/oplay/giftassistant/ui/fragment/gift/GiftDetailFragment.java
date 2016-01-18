package com.oplay.giftassistant.ui.fragment.gift;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.manager.PayManager;
import com.oplay.giftassistant.model.data.req.ReqGiftDetail;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.widget.button.GiftButton;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.DensityUtil;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftDetailFragment extends BaseFragment {

	private ImageView ivIcon;
	private ImageView ivLimit;
	private TextView tvName;
	private TextView tvConsume;
	private TextView tvScore;
	private TextView tvOr;
	private TextView tvBean;
	private TextView tvRemain;
	private TextView tvContent;
	private TextView tvDeadline;
	private TextView tvNote;
	private GiftButton btnSend;
	private ProgressBar pbPercent;
	private TextView tvCode;
	private TextView btnCopy;

	private IndexGiftNew mData;
	private int mId;


	public static GiftDetailFragment newInstance(int id) {
		GiftDetailFragment fragment = new GiftDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KeyConfig.KEY_DATA, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_gift_detail);
		ivIcon = getViewById(R.id.iv_icon);
		ivLimit = getViewById(R.id.iv_limit);
		tvName = getViewById(R.id.tv_name);
		tvConsume = getViewById(R.id.tv_consume);
		tvScore = getViewById(R.id.tv_score);
		tvOr = getViewById(R.id.tv_or);
		tvBean = getViewById(R.id.tv_bean);
		tvRemain = getViewById(R.id.tv_remain);
		tvContent = getViewById(R.id.et_content);
		tvDeadline = getViewById(R.id.tv_deadline);
		tvNote = getViewById(R.id.tv_note);
		btnSend = getViewById(R.id.btn_send);
		pbPercent = getViewById(R.id.pb_percent);
		tvCode = getViewById(R.id.tv_gift_code);
		btnCopy = getViewById(R.id.btn_copy);
	}

	@Override
	protected void setListener() {
		btnCopy.setOnClickListener(this);
		btnSend.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			throw new IllegalStateException("need to set data here");
		}
		ObserverManager.getInstance().addGiftUpdateListener(this);
		mId = getArguments().getInt(KeyConfig.KEY_DATA);
	}

	private void updateData(IndexGiftNew data) {
		if (data == null)
			return;
		mHasData = true;
		mViewManager.showContent();
		mData = data;

		ImageLoader.getInstance().displayImage(mData.img, ivIcon, Global.IMAGE_OPTIONS);
		tvName.setText(String.format("[%s]%s", mData.gameName, mData.name));
		if (mData.isLimit) {
			tvName.setPadding(DensityUtil.dip2px(getContext(), 4), 0, 0, 0);
			tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_limit_tag, 0, 0, 0);
		} else {
			tvName.setPadding(0, 0, 0, 0);
			tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		int state = GiftTypeUtil.getItemViewType(mData);

		tvOr.setVisibility(View.GONE);
		tvRemain.setVisibility(View.GONE);
		pbPercent.setVisibility(View.GONE);
		tvBean.setVisibility(View.GONE);
		tvCode.setVisibility(View.GONE);
		btnCopy.setVisibility(View.GONE);
		tvScore.setVisibility(View.GONE);

		if (mData.seizeStatus == GiftTypeUtil.SEIZE_TYPE_NEVER) {
			tvConsume.setVisibility(View.VISIBLE);
			if (mData.priceType == GiftTypeUtil.PAY_TYPE_SCORE) {
				tvScore.setVisibility(View.VISIBLE);
				tvScore.setText(String.valueOf(mData.score));
			} else if (mData.priceType == GiftTypeUtil.PAY_TYPE_BEAN) {
				tvBean.setVisibility(View.VISIBLE);
				tvBean.setText(String.valueOf(mData.bean));
			} else {
				tvScore.setVisibility(View.VISIBLE);
				tvBean.setVisibility(View.VISIBLE);
				tvOr.setVisibility(View.VISIBLE);
				tvScore.setText(String.valueOf(mData.score));
				tvBean.setText(String.valueOf(mData.bean));
			}
			tvRemain.setVisibility(View.VISIBLE);
			switch (state) {
				case GiftTypeUtil.TYPE_LIMIT_SEIZE:
					tvRemain.setText(Html.fromHtml(String.format("剩余 <font color='#ffaa17'>%d个</font>",
							mData.remainCount)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_SEARCH:
					tvRemain.setText(Html.fromHtml(String.format("已淘号 <font color='#ffaa17'>%d</font>", mData.searchCount)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_SEIZE:
					int progress = mData.remainCount * 100 / mData.totalCount;
					tvRemain.setText(Html.fromHtml(String.format("剩余%d%%", progress)));
					pbPercent.setVisibility(View.VISIBLE);
					pbPercent.setProgress(progress);
					break;
				case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
					tvRemain.setVisibility(View.VISIBLE);
					tvRemain.setText(Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", mData.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
					tvRemain.setText(Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>", mData.searchTime)));
					break;
			}

		} else {
			tvConsume.setVisibility(View.GONE);
			tvCode.setVisibility(View.VISIBLE);
			btnCopy.setVisibility(View.VISIBLE);
			tvCode.setText(Html.fromHtml(String.format("礼包码: <font color='#ffaa17'>%s</font>", mData.code)));
		}
		btnSend.setState(state);
		tvContent.setText(mData.content);
		tvDeadline.setText(DateUtil.formatTime(mData.useStartTime, "yyyy.MM.dd HH:mm") + " ~ "
				+ DateUtil.formatTime(mData.useEndTime, "yyyy.MM.dd HH:mm"));
		tvNote.setText(mData.note);
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					refreshFailEnd();
					return;
				}
				ReqGiftDetail data = new ReqGiftDetail();
				data.id = mId;
				Global.getNetEngine().obtainGiftDetail(new JsonReqBase<ReqGiftDetail>(data))
						.enqueue(new Callback<JsonRespBase<IndexGiftNew>>() {
							@Override
							public void onResponse(Response<JsonRespBase<IndexGiftNew>> response, Retrofit retrofit) {
								if (!mCanShowUI) {
									return;
								}
								if (response != null && response.code() == 200) {
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										refreshSuccessEnd();
										updateData(response.body().getData());
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_FRAG, "body = " + response.body());
									}
								}
								// 加载错误页面也行
								refreshSuccessEnd();
							}

							@Override
							public void onFailure(Throwable t) {
								if (!mCanShowUI) {
									return;
								}
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
								refreshFailEnd();
								updateData(initStashGiftDetail());
							}
						});
			}
		});
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_copy:
				ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				cmb.setPrimaryClip(ClipData.newPlainText("礼包码", mData.code));
				ToastUtil.showShort("已复制");
				break;
			case R.id.btn_send:
				switch (btnSend.getStatus()) {
					case GiftTypeUtil.TYPE_LIMIT_SEIZE:
					case GiftTypeUtil.TYPE_NORMAL_SEIZE:
						PayManager.getInstance().chargeGift(getContext(), mData, btnSend);
						break;
					case GiftTypeUtil.TYPE_NORMAL_SEARCH:
						PayManager.getInstance().searchGift(getContext(), mData, btnSend);
						break;
				}
				break;
		}
	}

	private IndexGiftNew initStashGiftDetail() {
		// 先暂时使用缓存数据假定
		ArrayList<IndexGiftNew> newData = new ArrayList<>();
		IndexGiftNew ng1 = new IndexGiftNew();
		ng1.gameName = "全民神将-攻城战";
		ng1.id = 335;
		ng1.status = GiftTypeUtil.STATUS_WAIT_SEARCH;
		ng1.priceType = GiftTypeUtil.PAY_TYPE_BOTH;
		ng1.img = "http://owan-avatar.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
		ng1.name = "至尊礼包";
		ng1.isLimit = true;
		ng1.bean = 30;
		ng1.score = 3000;
		ng1.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
		ng1.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 5);
		ng1.useStartTime = "2015.12.10 12:00";
		ng1.useEndTime = "2016.12.10 12:00";
		ng1.searchCount = 0;
		ng1.remainCount = 10;
		ng1.totalCount = 10;
		ng1.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
		ng1.note = "[1] 点击主界面右下角“设置”按钮\n[2] 点击“兑换礼包”";
		newData.add(ng1);
		IndexGiftNew ng2 = new IndexGiftNew();
		ng2.gameName = "鬼吹灯之挖挖乐";
		ng2.id = 336;
		ng2.status = GiftTypeUtil.STATUS_SEIZE;
		ng2.priceType = GiftTypeUtil.PAY_TYPE_BEAN;
		ng2.img = "http://owan-avatar.ymapp.com/app/11061/icon/icon_1450325761.png_140_140_100.png";
		ng2.name = "高级礼包";
		ng2.isLimit = true;
		ng2.bean = 30;
		ng2.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
		ng2.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 5);
		ng2.useStartTime = "2015.12.10 12:00";
		ng2.useEndTime = "2016.12.10 12:00";
		ng2.searchCount = 0;
		ng2.remainCount = 159;
		ng2.totalCount = 350;
		ng2.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
		ng2.note = "[1] 点击主界面右下角“设置”按钮\n[2] 点击“兑换礼包”";
		newData.add(ng2);
		IndexGiftNew ng3 = new IndexGiftNew();
		ng3.gameName = "兽人战争";
		ng3.id = 337;
		ng3.status = GiftTypeUtil.STATUS_SEIZE;
		ng3.priceType = GiftTypeUtil.PAY_TYPE_SCORE;
		ng3.img = "http://owan-avatar.ymapp.com/app/11058/icon/icon_1450059064.png_140_140_100.png";
		ng3.name = "高级礼包";
		ng3.useStartTime = "2015.12.10 12:00";
		ng3.useEndTime = "2016.12.10 12:00";
		ng3.isLimit = false;
		ng3.score = 1500;
		ng3.searchTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 3);
		ng3.seizeTime = DateUtil.getDate("yyyy-MM-dd HH:mm", 5);
		ng3.searchCount = 355;
		ng3.remainCount = 0;
		ng3.totalCount = 350;
		ng3.content = "30钻石，5000金币，武器经验卡x6，100块神魂石，10000颗迷魂珠";
		ng3.note = "[1] 点击主界面右下角“设置”按钮\n[2] 点击“兑换礼包”";
		newData.add(ng3);

		for (IndexGiftNew gift : newData) {
			if (gift.id == mId) {
				return gift;
			}
		}
		return newData.get((int) (Math.random() * 3));
	}
}
