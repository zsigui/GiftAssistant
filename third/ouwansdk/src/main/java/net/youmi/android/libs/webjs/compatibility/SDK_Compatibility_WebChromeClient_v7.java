//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.youmi.android.libs.webjs.compatibility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.widget.Toast;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.webjs.view.webview.Bean_Config_SDKWebView;

import java.io.File;
import java.lang.reflect.Method;

public class SDK_Compatibility_WebChromeClient_v7 extends SDK_Compatibility_WebChromeClient_v5 {
	public static final String CHROMECLIENT_ONACTIVITYRESULT_ACTION = "net.ouwan.umipay.android.chromeclinet_onresult_action";

	public final static int REQUEST_CODE_PICK_IMAGE = 100;
	public final static int REQUEST_CODE_IMAGE_CAPTURE = 101;
	public final static int FILE_SELECTED = 104;
	private ValueCallback<Uri> mUploadMsgForAndroid4;
	private Intent mSourceIntent;
	private ValueCallback<Uri> mUploadMsg;
	private String mCameraFilePath;
	private boolean mCaughtActivityNotFoundException;
	private OnActivityResultBroadcastReceiver mBroadcastReceiver;
	private IntentFilter filter;


	static SDK_Compatibility_WebChromeClient_v7 create(Bean_Config_SDKWebView config) {
		try {
			return new SDK_Compatibility_WebChromeClient_v7(config);
		} catch (Throwable var2) {
			return null;
		}
	}

	SDK_Compatibility_WebChromeClient_v7(Bean_Config_SDKWebView config) {
		super(config);
		mBroadcastReceiver = new OnActivityResultBroadcastReceiver();
		filter = new IntentFilter();
		filter.addAction(CHROMECLIENT_ONACTIVITYRESULT_ACTION);
	}

	public void onShowCustomView(View view, CustomViewCallback callback) {
		super.onShowCustomView(view, callback);
	}

	public void onHideCustomView() {
		super.onHideCustomView();
	}

	public Bitmap getDefaultVideoPoster() {
		return super.getDefaultVideoPoster();
	}

	public View getVideoLoadingProgressView() {
		return super.getVideoLoadingProgressView();
	}

	public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, QuotaUpdater quotaUpdater) {
		try {
			quotaUpdater.updateQuota(spaceNeeded * 2L);
		} catch (Throwable var7) {
			;
		}
	}


	//扩展浏览器上传文件
	//3.0++版本
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		openFileChooserHelper(uploadMsg, acceptType);
	}

	//3.0--版本
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		//实际调用的3.0++版本的方法
		openFileChooserHelper(uploadMsg, "");
	}


	public void openFileChooserHelper(ValueCallback<Uri> uploadMsg, String acceptType) {
		mUploadMsg = uploadMsg;
		showOptions();
	}

	// For Android  > 4.1.1
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
		openFileChooserForAndroid4(uploadMsg, acceptType, capture);
	}

	// For Android > 5.0
	public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, Object fileChooserParams) {
		try {
			if (Build.VERSION.SDK_INT >= 21) {
				Class<?> clazz = fileChooserParams.getClass();
				Method method = clazz.getMethod("getAcceptTypes()", new Class[]{});
				String acceptTypes[] = (String[]) method.invoke(fileChooserParams, new Object[]{});
				String acceptType = "";
				for (int i = 0; i < acceptTypes.length; ++i) {
					if (acceptTypes[i] != null && acceptTypes[i].length() != 0) {
						acceptType += acceptTypes[i] + ";";
					}
				}
				if (acceptType.length() == 0)
					acceptType = "*/*";

				final ValueCallback<Uri[]> finalFilePathCallback = uploadMsg;

				ValueCallback<Uri> vc = new ValueCallback<Uri>() {
					@Override
					public void onReceiveValue(Uri value) {
						Uri[] result;
						if (value != null)
							result = new Uri[]{value};
						else
							result = null;
						finalFilePathCallback.onReceiveValue(result);

					}
				};

				//实际也是调用之前>4.1.1的方法
				openFileChooserForAndroid4(vc, acceptType, "filesystem");
				return true;
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return false;
	}


	public void showOptions() {
		if(mConfig == null){
			return;
		}
		try{
			mConfig.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String[] items = {"相册", "拍照"};

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(mConfig.getActivity());

					alertDialog.setTitle("选择图片");

					alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialogInterface) {
							if (mUploadMsg != null) {
								mUploadMsg.onReceiveValue(null);
								mUploadMsg = null;
							}
						}
					});

					alertDialog.setItems(items, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (mConfig == null) {
										return;
									}
									if (which == 0) {
										mSourceIntent = choosePicture();
										mConfig.getActivity().startActivityForResult(mSourceIntent,
												REQUEST_CODE_PICK_IMAGE);

									} else {
										mSourceIntent = takeBigPicture();
										mConfig.getActivity().startActivityForResult(mSourceIntent,
												REQUEST_CODE_IMAGE_CAPTURE);
									}
									mConfig.getActivity().registerReceiver(mBroadcastReceiver, filter);
								}
							}
					);
					alertDialog.show();
				}
			});
		}catch (Throwable e){
		}
	}

	public void onResult(Intent data) {
		int requestCode = -1;
		int resultCode = -1;
		String tempUri = null;
		try{
			requestCode = data.getExtras().getInt("requestCode",-1);
			resultCode = data.getExtras().getInt("resultCode", -1);
			tempUri = data.getExtras().getString("uri");
			if(tempUri != null) {
				data.setData(Uri.parse(tempUri));
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}
		switch (requestCode){
			case REQUEST_CODE_IMAGE_CAPTURE:
			case REQUEST_CODE_PICK_IMAGE:
				if(mUploadMsg == null){
					return;
				}
				Uri uri = null;
				String sourcePath = null;
				try {
					if(data != null && data.getData()!= null) {
						sourcePath = retrievePath(mConfig.getActivity(), mSourceIntent, data);
						if (!TextUtils.isEmpty(sourcePath) && new File(sourcePath).exists()) {
							uri = Uri.fromFile(new File(sourcePath));
						}
					}
					mUploadMsg.onReceiveValue(uri);
				} catch (Throwable  e) {
					Debug_Log.e(e);
				}
				mUploadMsg = null;
				break;

			case FILE_SELECTED:
				if(mUploadMsgForAndroid4 == null){
					return;
				}
				try {
					if (resultCode == Activity.RESULT_CANCELED || mCaughtActivityNotFoundException) {
						// Couldn't resolve an activity, we are going to try again so skip
						// this result.
						mCaughtActivityNotFoundException = false;
						mUploadMsgForAndroid4.onReceiveValue(null);
						return;
					}
					Uri result = data == null || resultCode != Activity.RESULT_OK ? null
							: data.getData();
					if (resultCode == Activity.RESULT_OK) {
						if (result != null) {
							String source = getPath(mConfig.getActivity(), result);
							if (!TextUtils.isEmpty(source) && new File(source).exists()) {
								result = Uri.fromFile(new File(source));
							}
						} else {
							// As we ask the camera to save the result of the user taking
							// a picture, the camera application does not return anything other
							// than RESULT_OK. So we need to check whether the file we expected
							// was written to disk in the in the case that we
							// did not get an intent returned but did get a RESULT_OK. If it was,
							// we assume that this result has came back from the camera.
							File cameraFile = new File(mCameraFilePath);
							if (cameraFile.exists()) {
								result = Uri.fromFile(cameraFile);
								// Broadcast to the media scanner that we have a new photo
								// so it will be added into the gallery for the user.
								mConfig.getActivity().sendBroadcast(
										new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
							}
						}
					}
					mUploadMsgForAndroid4.onReceiveValue(result);
					mCaughtActivityNotFoundException = false;
				}catch (Throwable e){
					Debug_Log.e(e);
				}
				break;
		}
	}

	void openFileChooserForAndroid4(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
		final String imageMimeType = "image/*";
		final String videoMimeType = "video/*";
		final String audioMimeType = "audio/*";
		final String mediaSourceKey = "capture";
		final String mediaSourceValueCamera = "camera";
		final String mediaSourceValueFileSystem = "filesystem";
		final String mediaSourceValueCamcorder = "camcorder";
		final String mediaSourceValueMicrophone = "microphone";
		// According to the spec, media source can be 'filesystem' or 'camera' or 'camcorder'
		// or 'microphone' and the default value should be 'filesystem'.
		String mediaSource = mediaSourceValueFileSystem;
		if (mUploadMsgForAndroid4 != null) {
			// Already a file picker operation in progress.
			return;
		}
		mUploadMsgForAndroid4 = uploadMsg;
		// Parse the accept type.
		String params[] = acceptType.split(";");
		String mimeType = params[0];
		if (capture.length() > 0) {
			mediaSource = capture;
		}
		if (capture.equals(mediaSourceValueFileSystem)) {
			// To maintain backwards compatibility with the previous implementation
			// of the media capture API, if the value of the 'capture' attribute is
			// "filesystem", we should examine the accept-type for a MIME type that
			// may specify a different capture value.
			for (String p : params) {
				String[] keyValue = p.split("=");
				if (keyValue.length == 2) {
					// Process key=value parameters.
					if (mediaSourceKey.equals(keyValue[0])) {
						mediaSource = keyValue[1];
					}
				}
			}
		}
		//Ensure it is not still set from a previous upload.
		mCameraFilePath = null;
		if (mimeType.equals(imageMimeType)) {
			if (mediaSource.equals(mediaSourceValueCamera)) {
				// Specified 'image/*' and requested the camera, so go ahead and launch the
				// camera directly.
				startActivity(createCameraIntent());
				return;
			} else {
				// Specified just 'image/*', capture=filesystem, or an invalid capture parameter.
				// In all these cases we show a traditional picker filetered on accept type
				// so launch an intent for both the Camera and image/* OPENABLE.
				Intent chooser = createChooserIntent("Image Chooser",createCameraIntent());
				chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
				startActivity(chooser);
				return;
			}
		} else if (mimeType.equals(videoMimeType)) {
			if (mediaSource.equals(mediaSourceValueCamcorder)) {
				// Specified 'video/*' and requested the camcorder, so go ahead and launch the
				// camcorder directly.
				startActivity(createCamcorderIntent());
				return;
			} else {
				// Specified just 'video/*', capture=filesystem or an invalid capture parameter.
				// In all these cases we show an intent for the traditional file picker, filtered
				// on accept type so launch an intent for both camcorder and video/* OPENABLE.
				Intent chooser = createChooserIntent("Video Chooser",createCamcorderIntent());
				chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
				startActivity(chooser);
				return;
			}
		} else if (mimeType.equals(audioMimeType)) {
			if (mediaSource.equals(mediaSourceValueMicrophone)) {
				// Specified 'audio/*' and requested microphone, so go ahead and launch the sound
				// recorder.
				startActivity(createSoundRecorderIntent());
				return;
			} else {
				// Specified just 'audio/*',  capture=filesystem of an invalid capture parameter.
				// In all these cases so go ahead and launch an intent for both the sound
				// recorder and audio/* OPENABLE.
				Intent chooser = createChooserIntent("Audio Chooser",createSoundRecorderIntent());
				chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
				startActivity(chooser);
				return;
			}
		}
		// No special handling based on the accept type was necessary, so trigger the default
		// file upload chooser.
		startActivity(createDefaultOpenableIntent());
	}

	private void startActivity(Intent intent) {
		if(mConfig == null || intent == null){
			return;
		}
		try {
			mConfig.getActivity().startActivityForResult(intent, FILE_SELECTED);
			mConfig.getActivity().registerReceiver(mBroadcastReceiver, filter);
		} catch (ActivityNotFoundException e) {
			// No installed app was able to handle the intent that
			// we sent, so fallback to the default file upload control.
			try {
				mCaughtActivityNotFoundException = true;
				mConfig.getActivity().startActivityForResult(createDefaultOpenableIntent(), FILE_SELECTED);
				mConfig.getActivity().registerReceiver(mBroadcastReceiver, filter);
			} catch (ActivityNotFoundException e2) {
				// Nothing can return us a file, so file upload is effectively disabled.
				Toast.makeText(mConfig.getActivity(), "uploads_disabled",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private Intent createDefaultOpenableIntent() {
		// Create and return a chooser with the default OPENABLE
		// actions including the camera, camcorder and sound
		// recorder where available.
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");
		Intent chooser = createChooserIntent("Default Chooser",createCameraIntent(), createCamcorderIntent(),
				createSoundRecorderIntent());
		chooser.putExtra(Intent.EXTRA_INTENT, i);
		return chooser;
	}

	private Intent createChooserIntent(String title,Intent... intents) {
		Intent chooser = new Intent(Intent.ACTION_CHOOSER);
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
		chooser.putExtra(Intent.EXTRA_TITLE, title);
		return chooser;
	}

	private Intent createOpenableIntent(String type) {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType(type);
		return i;
	}

	private Intent createCameraIntent() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File cameraDataDir = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
				.getAbsolutePath() +File.separator+ "upload_image");
		if(!cameraDataDir.exists()) {
			cameraDataDir.mkdirs();
		}
		mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
				System.currentTimeMillis() + ".jpg";
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
		return cameraIntent;
	}

	private Intent createCamcorderIntent() {
		return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	}

	private Intent createSoundRecorderIntent() {
		return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
	}

	public static Intent choosePicture() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		return Intent.createChooser(intent, null);
	}


	/**
	 * 拍照后返回
	 */

	public  Intent takeBigPicture() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String newPhotoPath = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
				.getAbsolutePath() + "/WebViewUploadImage" + "/" + System.currentTimeMillis() + ".jpg";
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(newPhotoPath)));
		return intent;
	}


	public  String retrievePath(Context context, Intent sourceIntent, Intent dataIntent) {
		String picPath = null;
		Uri uri;
		if (dataIntent != null) {
			uri = dataIntent.getData();
			if (uri != null) {
				picPath = getPath(context, uri);
			}
			if (isFileExists(picPath)) {
				return picPath;
			}
		}

		if (sourceIntent != null) {
			uri = sourceIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
			if (uri != null) {
				String scheme = uri.getScheme();
				if (scheme != null && scheme.startsWith("file")) {
					picPath = uri.getPath();
				}
			}
		}
		return picPath;
	}

	private  boolean isFileExists(String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		File f = new File(path);
		if (!f.exists()) {
			return false;
		}
		return true;
	}

	@SuppressLint("NewApi")
	public  String getPath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= 19;
		// DocumentProvider
		if (isKitKat && isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
				//if the Uri authority is ExternalStorageProvider.
				final String docId = getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return String.format("%s/%s", Environment.getExternalStorageDirectory().getPath(), split[1]);
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
				//if the Uri authority is DownloadsProvider.
				final String id = getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
				//if the Uri authority is MediaProvider.
				final String docId = getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if ("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
				//if Uri authority is Google Photos
				return uri.getLastPathSegment();
			}

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context       The context.
	 * @paramuri           The Uri to query.
	 * @param selection     (Optional) Filter used in the query.
	 * @paramselectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public  String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}
					, selection, selectionArgs, null);
			if (cursor != null &&cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public  boolean isDocumentUri(final Context context, final Uri uri){
		try{
			if(Build.VERSION.SDK_INT >= 19) {
				Class<?> clazz = Class.forName("android.provider.DocumentsContract");
				Method method = clazz.getMethod("isDocumentUri", new Class[]{Context.class, Uri.class});
				Boolean b = (Boolean) method.invoke(clazz, new Object[]{context, uri});
				return b;
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}

		return false;
	}

	public  String getDocumentId(final Uri uri){
		try{
			if(Build.VERSION.SDK_INT >= 19) {
				Class<?> clazz = Class.forName("android.provider.DocumentsContract");
				Method method = clazz.getMethod("getDocumentId", new Class[]{Uri.class});
				return (String) method.invoke(clazz, new Object[]{uri});
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}
		return "";
	}

	public static File getExternalStoragePublicDirectory(String type) {
		return new File(Environment.getExternalStorageDirectory(), type);
	}

	class OnActivityResultBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				if (CHROMECLIENT_ONACTIVITYRESULT_ACTION.equals(action)) {
					onResult(intent);
				}
			} catch (Exception e) {
				Debug_Log.e(e);
			}finally {
				if (mConfig != null) {
					mConfig.getActivity().unregisterReceiver(this);
				}
				mUploadMsg = null;
				mUploadMsgForAndroid4 = null;
			}
		}
	}
}
