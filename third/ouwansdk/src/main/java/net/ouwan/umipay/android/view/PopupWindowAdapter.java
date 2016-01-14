package net.ouwan.umipay.android.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.entry.UmipayAccount;

import java.util.ArrayList;

/**
 * PopupWindowAdapter
 *
 * @author zacklpx
 *         date 15-3-6
 *         description
 */
public class PopupWindowAdapter extends BaseAdapter {
	public static String SELECT_KEY = "selectKey";
	public static String DELETE_KEY = "deleteKey";

	private ArrayList<UmipayAccount> mAccountList = new ArrayList<UmipayAccount>();
	private Activity mParentActivity;
	private Handler mHandler;

	public PopupWindowAdapter(Activity activity, Handler handler, ArrayList<UmipayAccount> list) {
		this.mParentActivity = activity;
		this.mAccountList = list;
		this.mHandler = handler;
	}

	@Override
	public int getCount() {
		return mAccountList.size();
	}

	@Override
	public Object getItem(int position) {
		return mAccountList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mParentActivity).inflate(Util_Resource.getIdByReflection
					(mParentActivity, "layout", "umipay_account_list_item"), null);
			holder.textView = (TextView) convertView.findViewById(Util_Resource.getIdByReflection(mParentActivity,
					"id", "umipay_account_list_item_text"));
			holder.imageView = (ImageView) convertView.findViewById(Util_Resource.getIdByReflection(mParentActivity,
					"id", "umipay_account_list_item_delete_button"));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.textView.setText(mAccountList.get(position).getUserName());
		// 为下拉框选项文字部分设置事件，最终效果是点击将其文字填充到文本框
		holder.textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				Bundle data = new Bundle();
				// 设置选中索引
				data.putInt(SELECT_KEY, position);
				msg.setData(data);
				msg.what = 1;
				// 发出消息
				mHandler.sendMessage(msg);

			}
		});

		// 为下拉框选项删除图标部分设置事件，最终效果是点击将该选项删除
		holder.imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				Bundle data = new Bundle();
				// 设置删除索引
				data.putInt(DELETE_KEY, position);
				msg.setData(data);
				msg.what = 2;
				// 发出消息
				mHandler.sendMessage(msg);
			}
		});
		return convertView;
	}

	class ViewHolder {
		TextView textView;
		ImageView imageView;
	}
}
