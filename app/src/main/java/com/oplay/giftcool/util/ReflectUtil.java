package com.oplay.giftcool.util;

import android.content.Context;

/**
 * 反射工具类
 * <p/>
 * Created by zsigui on 15-8-18.
 */
@SuppressWarnings("unchecked")
public final class ReflectUtil {


	/**
	 * 反射获取资源ID
	 *
	 * @param context
	 * @param name
	 * @param defType
	 * @return
	 */
	public static int getIdentifier(Context context, String name, String defType) {
		return context.getResources().getIdentifier(name, defType, context.getPackageName());
	}

	public static int getDrawableId(Context context, String name) {
		return getIdentifier(context, name, "drawable");
	}
}