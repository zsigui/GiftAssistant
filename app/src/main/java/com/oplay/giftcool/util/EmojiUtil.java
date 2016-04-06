package com.oplay.giftcool.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Emoji表情显示工具
 *
 * Created by zsigui on 16-4-6.
 */
public class EmojiUtil {

	private static final String EMOJI_PREFIX = "emoji_";

	private SpannableString replaceTextToEmoji(Context context, String content) {
		final String PATTERN_EMOJI = "[(0x\\d{4,6})]";
		SpannableString ss = new SpannableString(content);
		Matcher matcher = Pattern.compile(PATTERN_EMOJI).matcher(content);
		while (matcher.find()) {
			try {
				final int start = matcher.start();
				final int end = matcher.end();
				final String imageName = EMOJI_PREFIX + matcher.group(1);
				final Drawable d = context.getResources().getDrawable(ReflectUtil.getDrawableId(context, imageName));
				if (d != null) {
					d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
					final ImageSpan img = new ImageSpan(d);
					ss.setSpan(img, start, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			} catch (Exception e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_UTIL, e);
				}
			}
		}
	}

}
