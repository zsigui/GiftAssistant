package com.oplay.giftcool.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.ShareInfo;
import com.oplay.giftcool.sharesdk.ShareSDKConfig;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 16-1-22.
 */
public class ShareUtil {
    public static void shareText(Context context, String shareTitle, String shareContent) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//        sharingIntent.setType("text/html");
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        Intent chooserIntent = Intent.createChooser(sharingIntent, "分享");
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (context instanceof FragmentActivity) {
            ((FragmentActivity) context).startActivityForResult(chooserIntent, ShareSDKConfig.SHARE_REQUEST_CODE);
        } else {
            context.startActivity(chooserIntent);
        }
    }

    public static void shareToPackage(Context context, ShareInfo info, String shareText) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setComponent(new ComponentName(info.getPackageName(), info.getActivityName()));
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        context.startActivity(intent);
    }

    @Deprecated
    public static void shareToFriend(Context context, String text, File file) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND);
//		intent.setType("image/*");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
//		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(intent);
    }

    @Deprecated
    public static void shareToTimeLine(Context context, String text, File file) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
//		intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(intent);
    }

    @Deprecated
    public static void shareMultiplePictureToTimeLine(Context context, File... files) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_TEXT, "adbdbddb");

        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for (File f : files) {
            imageUris.add(Uri.fromFile(f));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);

        context.startActivity(intent);
    }

    private static List<ResolveInfo> getShareIntentList(Context context) {
        final PackageManager pm = context.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        final List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
        return list;
    }

    public static List<ShareInfo> getCanSharePackageList(Context context, List<String> pnFilterList) {
        try {
            final List<ResolveInfo> list = getShareIntentList(context);
            final PackageManager pm = context.getPackageManager();
            if (list != null) {
                final List<ShareInfo> infoList = new ArrayList<ShareInfo>(list.size());
                for (ResolveInfo info : list) {
                    final String pn = info.activityInfo.packageName;
                    boolean hasMatch = false;
                    for (String pnFilter : pnFilterList) {
                        if (pnFilter.equals(pn)) {
                            hasMatch = true;
                        }
                    }
                    if (!hasMatch) {
                        continue;
                    }
                    final String an = info.activityInfo.name;
                    final ApplicationInfo applicationInfo = pm.getApplicationInfo(pn, PackageManager.GET_ACTIVITIES);
                    final CharSequence appName = pm.getApplicationLabel(applicationInfo);
                    final Drawable icon = pm.getApplicationIcon(applicationInfo);
                    infoList.add(new ShareInfo(appName, pn, an, icon));
                    AppDebugConfig.v(AppDebugConfig.TAG_SHARE, String.format("ShareFilter:%s %s %s", pn, an, appName));
                }
                return infoList;
            }
        } catch (PackageManager.NameNotFoundException e) {
            AppDebugConfig.w(AppDebugConfig.TAG_SHARE, e);
        }
        return null;
    }

    public static void toShareOnlyTencentMM(FragmentActivity fragmentActivity, String shareTitle, String shareText) {
        shareToFriend(fragmentActivity, shareText, null);
    }
}
