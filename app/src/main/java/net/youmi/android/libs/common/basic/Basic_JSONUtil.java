package net.youmi.android.libs.common.basic;

import net.youmi.android.libs.common.debug.Debug_SDK;

import org.json.JSONArray;
import org.json.JSONObject;

public class Basic_JSONUtil {

	// public static final int ValueNotFound_Int = -999999;

	public static boolean isKeyNotNull(JSONObject jsonObject, String key) {
		return jsonObject != null && !jsonObject.isNull(key);
	}

	public static JSONObject toJsonObject(String json) {
		try {
			if (json == null) {
				return null;
			}
			return new JSONObject(json);
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return null;
	}
	
	public static JSONArray toJsonArray(String json) {
		try {
			if (json == null) {
				return null;
			}
			return new JSONArray(json);
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return null;
	}

	/**
	 * 从jsonObject中获取String,<br/>
	 * 如果获取的结果为null或空串，则返回defaultValue
	 * 
	 * @param obj
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getString(JSONObject obj, String key, String defaultValue) {
		try {
			if (isKeyNotNull(obj, key)) {
				String str = obj.getString(key);
				if (str != null) {
					// 确保首尾不带空格
					str = str.trim();
					if (str.length() > 0) {
						return str;
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	public static boolean getBoolean(JSONObject obj, String key, boolean defaultValue) {
		try {
			if (isKeyNotNull(obj, key)) {
				return obj.getBoolean(key);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从json数组中获取string <br/>
	 * 如果获取的结果为null或空串，则返回defaultValue
	 * 
	 * @param ary
	 * @param index
	 * @param defaultValue
	 * @return
	 */
	public static String getString(JSONArray ary, int index, String defaultValue) {
		try {
			if (ary != null) {
				if (ary.length() > index && index > -1) {
					String str = ary.getString(index);
					if (str != null) {
						str = str.trim();

						if (str.length() > 0) {
							return str;
						}
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从jsonObject中获取int
	 * 
	 * @param obj
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static int getInt(JSONObject obj, String key, int defaultValue) {
		try {
			if (isKeyNotNull(obj, key)) {
				return obj.getInt(key);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从JSONArray中获取int
	 * 
	 * @param ary
	 * @param index
	 * @param defaultValue
	 * @return
	 */
	public static int getInt(JSONArray ary, int index, int defaultValue) {
		try {
			if (ary != null) {
				if (ary.length() > index && index > -1) {
					return ary.getInt(index);
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从jsonObject中获取long
	 * 
	 * @param obj
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static long getLong(JSONObject obj, String key, long defaultValue) {
		try {
			if (isKeyNotNull(obj, key)) {
				return obj.getLong(key);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从jsonObject中获取double
	 * 
	 * @param obj
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static double getDouble(JSONObject obj, String key, double defaultValue) {
		try {
			if (isKeyNotNull(obj, key)) {
				return obj.getDouble(key);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从JSONArray中获取long
	 * 
	 * @param ary
	 * @param index
	 * @param defaultValue
	 * @return
	 */
	public static long getLong(JSONArray ary, int index, long defaultValue) {
		try {
			if (ary != null) {
				if (ary.length() > index && index > -1) {
					return ary.getLong(index);
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从jsonObject中获取JsonObject
	 * 
	 * @param obj
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static JSONObject getJsonObject(JSONObject obj, String key, JSONObject defaultValue) {
		try {
			if (isKeyNotNull(obj, key)) {
				return obj.getJSONObject(key);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从JSONArray中获取int
	 * 
	 * @param ary
	 * @param index
	 * @param defaultValue
	 * @return
	 */
	public static JSONObject getJsonObject(JSONArray ary, int index, JSONObject defaultValue) {
		try {
			if (ary != null) {
				if (ary.length() > index && index > -1) {
					return ary.getJSONObject(index);
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从jsonObject中获取JsonArray
	 * 
	 * @param obj
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static JSONArray getJsonArray(JSONObject obj, String key, JSONArray defaultValue) {
		try {
			if (isKeyNotNull(obj, key)) {
				return obj.getJSONArray(key);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 从JSONArray中获取int
	 * 
	 * @param ary
	 * @param index
	 * @param defaultValue
	 * @return
	 */
	public static JSONArray getJsonArray(JSONArray ary, int index, JSONArray defaultValue) {
		try {
			if (ary != null) {
				if (ary.length() > index && index > -1) {
					return ary.getJSONArray(index);
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 写入int到jsonobject
	 * @param jo
	 * @param key
	 * @param value
	 */
	public static void putInt(JSONObject jo, String key, int value) {
		try {
			if (jo != null) {
				jo.put(key, value);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
	}

	/**
	 * 写入long到jsonobject
	 * @param jo
	 * @param key
	 * @param value
	 */
	public static void putLong(JSONObject jo, String key, long value) {
		try {
			if (jo != null) {
				jo.put(key, value);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
	}
	
	/**
	 * 写入string到jsonobject
	 * @param jo
	 * @param key
	 * @param value
	 */
	public static void putString(JSONObject jo, String key, String value) {
		try {
			if (jo != null) {
				if (Basic_StringUtil.isNullOrEmpty(value)) {
					jo.put(key, "");
				} else {
					jo.put(key, value);
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
	}

	public static void putObject(JSONObject jo, String key, Object object) {
		try {
			if (jo != null) {
				jo.put(key, object);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
	}

	/**
	 * 写入jsonObject到jsonobject
	 * @param jo
	 * @param key
	 * @param value
	 */
	public static void putJsonObject(JSONObject jo, String key, JSONObject value) {
		try {
			if (jo != null) {
				jo.put(key, value);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_JSONUtil.class, e);
			}
		}
	}

}