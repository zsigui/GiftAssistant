package com.oplay.giftcool.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.EmojiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Emoji表情显示工具
 * <p/>
 * Created by zsigui on 16-4-6.
 */
public class EmojiUtil {

    /**
     * 定义Emoji表情的前缀
     */
    private static final String EMOJI_PREFIX = "emoji_";
    private static final int EMOJI_WORD_AT_LEAST_LENGTH = 8;

    /**
     * 替换文本中的所有规定Emoji编码串为指定Emoji表情
     */
    public SpannableString replaceTextEmoji(Context context, String content) {
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
                AppDebugConfig.w(AppDebugConfig.TAG_UTIL, e);
            }
        }
        return ss;
    }

    public SpannableString replaceTextEmoji(Context context, String content, List<EmojiModel> emojis) {
        SpannableString ss = new SpannableString(content);
        for (EmojiModel model : emojis) {
            try {
                if (content.length() < model.getEndIndex()
                        || content.charAt(model.getStartIndex()) != '['
                        || content.charAt(model.getEndIndex()) != ']') {
                    // 已获取的不合位置要求，说明出错
                    continue;
                }
                Drawable d = context.getResources().getDrawable(ReflectUtil.getDrawableId(context, model.getImageName
                        ()));
                if (d != null) {
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    final ImageSpan span = new ImageSpan(d);
                    ss.setSpan(span, model.getStartIndex(), model.getEndIndex() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } catch (Exception e) {
                AppDebugConfig.w(AppDebugConfig.TAG_UTIL, e);
            }
        }
        return ss;
    }

    /**
     * 从给定文本中提取所有符合Emoji规则串的可能Emoji模型
     */
    public ArrayList<EmojiModel> getAllPotentialEmojiModel(String content) {
        final String PATTERN_EMOJI = "[(0x\\d{4,6})]";
        ArrayList<EmojiModel> emojis = new ArrayList<>();
        Matcher matcher = Pattern.compile(PATTERN_EMOJI).matcher(content);
        while (matcher.find()) {
            try {
                final String imageName = EMOJI_PREFIX + matcher.group(1);
                EmojiModel model = new EmojiModel();
                model.setStartIndex(matcher.start());
                model.setEndIndex(matcher.end());
                model.setPhrase(matcher.group());
                model.setImageName(imageName);
                emojis.add(model);
            } catch (Exception e) {
                AppDebugConfig.w(AppDebugConfig.TAG_UTIL, e);
            }
        }
        return emojis;
    }

    /**
     * 将指定Emoji规格串转化为文本表情图片
     */
    public static SpannableString textToImgSpan(Context context, EmojiModel model, String content) {
        final SpannableString ss = new SpannableString(content);
        int starts = content.indexOf("[");
        int end = content.indexOf("]", starts);
        if (starts != -1 && end != -1) {
//            String phrase = content.substring(starts, end + 1);
            try {
                final Drawable d = context.getResources().getDrawable(
                        ReflectUtil.getDrawableId(context, model.getImageName()));
                if (d != null) {
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    final ImageSpan span = new ImageSpan(d);
                    ss.setSpan(span, starts, end + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } catch (Throwable e) {
                AppDebugConfig.w(AppDebugConfig.TAG_UTIL, e);
            }
        }
        return ss;
    }
}
