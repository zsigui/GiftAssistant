package net.ouwan.umipay.android.Utils;

import android.os.Bundle;
import android.util.Log;


import org.apache.http.NameValuePair;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by mink on 15-12-11.
 */
public class Util_SinaUtility {
		public static Bundle parseUrl(String url) {
			try {
				URL e = new URL(url);
				Bundle b = decodeUrl(e.getQuery());
				b.putAll(decodeUrl(e.getRef()));
				return b;
			} catch (MalformedURLException var3) {
				return new Bundle();
			}
		}

		public static Bundle decodeUrl(String s) {
			Bundle params = new Bundle();
			if(s != null) {
				String[] array = s.split("&");
				String[] var6 = array;
				int var5 = array.length;

				for(int var4 = 0; var4 < var5; ++var4) {
					String parameter = var6[var4];
					String[] v = parameter.split("=");
					params.putString(URLDecoder.decode(v[0]), URLDecoder.decode(v[1]));
				}
			}

			return params;
		}

		public static String encodeUrl(List<NameValuePair> parameters) {
			if(parameters == null) {
				return "";
			} else {
				StringBuilder sb = new StringBuilder();
				boolean first = true;

				for(int loc = 0; loc < parameters.size(); ++loc) {
					if(first) {
						first = false;
					} else {
						sb.append("&");
					}

					NameValuePair p =  parameters.get(loc);
					String _key = p.getName();
					String _value = p.getValue();
					if(_value == null) {
						Log.i("encodeUrl", "key:" + _key + " \'s value is null");
					} else {
						sb.append(URLEncoder.encode( p.getName()) + "=" + URLEncoder.encode(p.getValue()));
					}
				}

				return sb.toString();
			}
		}

}
