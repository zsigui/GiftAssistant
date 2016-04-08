package com.oplay.giftcool.ui.fragment.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameTagAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.IndexTypeUtil;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameTypeFragment extends BaseFragment {

	private final static String PAGE_NAME = "游戏分类";
	private RecyclerView mTagView;
	private GameTagAdapter mTagAdapter;

	private int[] mResIds = new int[]{R.drawable.ic_tag_arpg, R.drawable.ic_tag_card, R.drawable.ic_tag_supernatural,
			R.drawable.ic_tag_action, R.drawable.ic_tag_stategy, R.drawable.ic_tag_round};

	public static GameTypeFragment newInstance() {
		return new GameTypeFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_game_type);
		mTagView = getViewById(R.id.rv_content);
	}

	@Override
	protected void setListener() {
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		final GridLayoutManager tagLayoutManager = new GridLayoutManager(getContext(),
				IndexTypeUtil.ITEM_GAME_TYPE_GRID_COUNT);

		/*
		 * 判断并合并多个项成为头
		 */
		tagLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return mTagAdapter.getItemViewType(position) == IndexTypeUtil.ITEM_HEADER ?
						tagLayoutManager.getSpanCount() : 1;
			}
		});
		mTagView.setLayoutManager(tagLayoutManager);
		mTagAdapter = new GameTagAdapter(getContext());
		mTagView.setAdapter(mTagAdapter);
		mViewManager.showContent();

		/*
		 * 设置列表项的分隔符
		 */
		mTagView.addItemDecoration(new TagDividerItemDecoration());
	}

	/**
	 * 进行游戏分类页面数据刷新的网络请求声明
	 */
	private Call<JsonRespBase<ArrayList<GameTypeMain>>> mCallRefresh;

	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		if (mCallRefresh != null) {
			mCallRefresh.cancel();
			mCallRefresh = mCallRefresh.clone();
		} else {
			mCallRefresh = Global.getNetEngine().obtainIndexGameType(new JsonReqBase<Void>());
		}
		mCallRefresh.enqueue(new Callback<JsonRespBase<ArrayList<GameTypeMain>>>() {
			@Override
			public void onResponse(Call<JsonRespBase<ArrayList<GameTypeMain>>> call, Response<JsonRespBase
					<ArrayList<GameTypeMain>>> response) {
				if (!mCanShowUI || call.isCanceled()) {
					return;
				}
				if (response != null && response.isSuccessful()) {
					if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
						refreshSuccessEnd();
						updateData(response.body().getData());
						return;
					}
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_APP,
								(response.body() == null ? "解析失败" : response.body().error()));
					}
				}
				refreshFailEnd();
			}

			@Override
			public void onFailure(Call<JsonRespBase<ArrayList<GameTypeMain>>> call, Throwable t) {
				if (!mCanShowUI || call.isCanceled()) {
					return;
				}
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_UTIL, t);
				}
				refreshFailEnd();
			}
		});
	}

	/**
	 * 进行数据更新
	 */
	private void updateData(ArrayList<GameTypeMain> data) {
		if (data == null || data.size() == 0) {
			mViewManager.showEmpty();
			return;
		}
		mHasData = true;
		mViewManager.showContent();
		int headerCount = (data.size() < IndexTypeUtil.ITEM_GAME_TYPE_HEADER_COUNT ?
				data.size() : IndexTypeUtil.ITEM_GAME_TYPE_HEADER_COUNT);
		for (int k = 0; k < headerCount; k++) {
			data.get(k).icon = mResIds[k];
		}
		mTagAdapter.updateData(data);
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	@Override
	public void release() {
		super.release();
		if (mCallRefresh != null) {
			mCallRefresh.cancel();
			mCallRefresh = null;
		}
		if (mTagAdapter != null) {
			mTagAdapter.release();
			mTagAdapter = null;
		}
		if (mTagView != null) {
			mTagView.setAdapter(null);
			mTagView = null;
		}
	}

	/**
	 * 设置表格的分割符样式
	 */
	class TagDividerItemDecoration extends RecyclerView.ItemDecoration {

		private Paint mPaint;
		private int mDividerSize;
		private int mHeaderDividerSize;

		public TagDividerItemDecoration() {
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setColor(getResources().getColor(R.color.co_divider_bg));
			/*设置填充*/
			mPaint.setStyle(Paint.Style.FILL);
			mDividerSize = getResources().getDimensionPixelSize(R.dimen.di_divider_height);
			mHeaderDividerSize = getResources().getDimensionPixelSize(R.dimen.di_index_module_gap);
		}

		/**
		 * 绘制图形，该方法绘制的区域在 Item 视图之上，即所绘制会显示在 RecyclerView 的对于绘制区域处 <br />
		 */
		@Override
		public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
			super.onDrawOver(c, parent, state);
		}

		/**
		 * 绘制图形，该区域处于 Item 视图之下，故绘制区域超过并不会显示，只会造成 OverDraw <br />
		 */
		@Override
		public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
			final int childSize = parent.getChildCount();
			int headCount = 0;
			for (int pos = 0; pos < childSize; pos++) {
				final View child = parent.getChildAt(pos);
				final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
				if (child instanceof RecyclerView) {
					// 画Header的下分割线
					drawHeaderBottom(c, parent, child, lp);
					headCount++;
				} else {
					final int realPos = pos - headCount;
					if (realPos + IndexTypeUtil.ITEM_GAME_TYPE_GRID_COUNT
							>= mTagAdapter.getItemCount() - headCount) {
						if (realPos % IndexTypeUtil.ITEM_GAME_TYPE_GRID_COUNT != 0) {
							drawLeft(c, child, lp);
						}
					} else {
						if (realPos % IndexTypeUtil.ITEM_GAME_TYPE_GRID_COUNT == 0) {
							drawBottom(c, child, lp);
						} else {
							drawLeft(c, child, lp);
							drawBottom(c, child, lp);
						}
					}
				}
			}
		}

		/**
		 * 绘制头部的下端分割线
		 */
		private void drawHeaderBottom(Canvas c, RecyclerView parent, View child, RecyclerView.LayoutParams lp) {
			final int left = parent.getPaddingLeft();
			final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
			final int top = child.getBottom() + lp.topMargin;
			final int bottom = top + mHeaderDividerSize;
			c.drawRect(left, top, right, bottom, mPaint);
		}

		/**
		 * 绘制表格项的下分割线
		 */
		private void drawBottom(Canvas c, View child, RecyclerView.LayoutParams lp) {
			final int left = child.getLeft() - lp.leftMargin - mDividerSize;
			final int right = child.getRight() + lp.rightMargin;
			final int top = child.getBottom() + lp.bottomMargin;
			final int bottom = top + mDividerSize;
			c.drawRect(left, top, right, bottom, mPaint);
		}

		/**
		 * 绘制表格项的左分割线
		 */
		private void drawLeft(Canvas c, View child, RecyclerView.LayoutParams lp) {
			final int right = child.getLeft() - lp.leftMargin;
			final int left = right - mDividerSize;
			final int top = child.getTop() + lp.topMargin;
			final int bottom = child.getBottom() - lp.bottomMargin;
			c.drawRect(left, top, right, bottom, mPaint);
		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			if (view.getLayoutParams() == null) {
				return;
			}
			int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
			if (mTagAdapter.getItemViewType(position) == IndexTypeUtil.ITEM_HEADER) {
				outRect.set(0, 0, 0, mHeaderDividerSize);
			} else {
				position -= mTagAdapter.getHeaderCount();
				if (position + IndexTypeUtil.ITEM_GAME_TYPE_GRID_COUNT >=
						mTagAdapter.getItemCount() - mTagAdapter.getHeaderCount()) {
					// 对于底部的项
					if (position % IndexTypeUtil.ITEM_GAME_TYPE_GRID_COUNT == 0) {
						outRect.set(0, 0, 0, 0);
					} else {
						outRect.set(mDividerSize, 0, 0, 0);
					}
				} else {
					// 对于其他项
					if (position % IndexTypeUtil.ITEM_GAME_TYPE_GRID_COUNT == 0) {
						outRect.set(0, 0, 0, mDividerSize);
					} else {
						outRect.set(mDividerSize, 0, 0, mDividerSize);
					}
				}
			}
		}
	}
}
