package net.youmi.android.libs.common;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 用来检查动态混淆字符串之后的新java文件和旧java文件的类，一般不会用到，请忽略
 *
 * @author zhitao
 * @since 2015-08-06 19:43
 */
public class FileChecker {

	public static void logAll_PreString_PublicMethod(Object object, String preMethodString) {
		if (Debug_SDK.isDebug) {

			try {
				long start = System.currentTimeMillis();
				String classNameString;
				if (object instanceof Class) {
					classNameString = ((Class<?>) object).getName();
				} else {
					classNameString = object.getClass().getName();
				}
				Debug_SDK.ti("test_", object, classNameString);

				Class<?> c = Class.forName(classNameString);
				Method[] methods = c.getMethods();
				List<String> temp = new ArrayList<String>();
				for (Method method : methods) {
					if (method.getName().startsWith(preMethodString)) {
						temp.add(method.getName() + " : " + method.invoke(c));
					}
				}
				for (int i = 0; i < temp.size(); ++i) {
					if (i != temp.size() - 1) {
						Debug_SDK.tv("test_", object, "┣━━ %s", temp.get(i));
					} else {
						Debug_SDK.tv("test_", object, "┗━━ %s", temp.get(i));
					}
				}
				Debug_SDK.tv("test_", object, "┗━━ 总耗时: %d ms", System.currentTimeMillis() - start);
			} catch (Exception e) {
				Debug_SDK.te("test_", object, e);
			}
		}
	}

	public static void diff(Object obj1, Object obj2, String preMethodString) {
		if (Debug_SDK.isDebug) {

			HashMap<String, String> map1 = getClassData(obj1, preMethodString);
			HashMap<String, String> map2 = getClassData(obj2, preMethodString);

			Iterator<Map.Entry<String, String>> iter1 = map1.entrySet().iterator();
			while (iter1.hasNext()) {
				Map.Entry<String, String> entry = iter1.next();
				String methodName = entry.getKey();
				String methodContent = entry.getValue();

				boolean isPass = false;
				if (map2.containsKey(methodName) && map2.containsValue(methodContent) && map2.get(methodName).equals
						(methodContent)) {
					Debug_SDK.ti("test_", obj1, "true ━━ %s ", methodName);
					isPass = true;
				} else {
					Debug_SDK.te("test_", obj1, "false ━━ %s", methodName);
				}
					Debug_SDK.tv("test_", obj1, "     ┗━ src: %s : %s", methodName, methodContent);
					Debug_SDK.tv("test_", obj1, "     ┗━ aft: %s : %s", methodName, map2.get(methodName));
				if (!isPass){
					Debug_SDK.te("test_", obj1, "校验失败");
					return;
				}
			}
			Debug_SDK.ti("test_", obj1, "校验通过");

		}

	}

	public static HashMap<String, String> getClassData(Object object, String preMethodString) {
		if (Debug_SDK.isDebug) {

			try {
				String classNameString;
				if (object instanceof Class) {
					classNameString = ((Class<?>) object).getName();
				} else {
					classNameString = object.getClass().getName();
				}
				Class<?> c = Class.forName(classNameString);
				Method[] methods = c.getMethods();
				HashMap<String, String> map = new HashMap<String, String>();
				for (Method method : methods) {
					if (method.getName().startsWith(preMethodString)) {
						map.put(method.getName(), String.valueOf(method.invoke(c)));
					}
				}
				return map;
			} catch (Exception e) {
				Debug_SDK.te("test_", object, e);
			}
		}
		return null;
	}

}
