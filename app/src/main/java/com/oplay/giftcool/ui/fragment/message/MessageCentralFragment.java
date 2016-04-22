package com.oplay.giftcool.ui.fragment.message;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.MessageCentralAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

/**
 * 消息中心页面
 * <p/>
 * Created by zsigui on 16-3-7.
 */
public class MessageCentralFragment extends BaseFragment {

	private MessageCentralAdapter mAdapter;
	private RecyclerView rvData;

	public static MessageCentralFragment newInstance() {
		return new MessageCentralFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.view_recycle_view);
		rvData = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		LinearLayoutManager llm = new LinearLayoutManager(getContext(),
				LinearLayoutManager.VERTICAL,
				false);
		DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), llm.getOrientation());
		itemDecoration.setWriteBottom(true);
		mAdapter = new MessageCentralAdapter(getContext());
		mAdapter.setData(Global.getMsgCentralData(getContext()));
		rvData.setLayoutManager(llm);
		rvData.addItemDecoration(itemDecoration);
		rvData.setAdapter(mAdapter);
		KLog.d(AppDebugConfig.TAG_WARN, "processLogic");
		Global.mMsgCentralTobeRefresh = false;
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onResume() {
		super.onResume();
		KLog.d(AppDebugConfig.TAG_WARN, "onResume = " + Global.mMsgCentralTobeRefresh);
		if (Global.mMsgCentralTobeRefresh) {
			if (MainActivity.sGlobalHolder != null) {
				MainActivity.sGlobalHolder.updateHintState(KeyConfig.TYPE_ID_MSG, 0);
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void release() {
		super.release();
		if (mAdapter != null) {
			mAdapter.release();
			mAdapter = null;
		}
	}

	@Override
	public String getPageName() {
		return "消息中心";
	}
}
