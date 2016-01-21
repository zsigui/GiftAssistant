package com.oplay.giftcool.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.socks.library.KLog;

/**
 * DownloadDBHelper
 *
 * @author zacklpx
 *         date 16-1-4
 *         description
 */
public class DownloadDBHelper extends SQLiteOpenHelper implements OnDownloadStatusChangeListener,
		OnProgressUpdateListener {
	private static int VERSION = 1;
	private static String DB_NAME = "gift_assistant.db";
	private static String TABLE_NAME = "download";
	private static DownloadDBHelper mInstance = null;

	private final String KEY_OF_APPNAME = "a";
	private final String KEY_OF_ICONURL = "b";
	private final String KEY_OF_MD5SUM = "c";
	private final String KEY_OF_PACKAGENAME = "d";
	private final String KEY_OF_RAWURL = "e";
	private final String KEY_OF_DOWNLOADSTATUS = "f";
	private final String KEY_OF_PATH_APK = "g";
	private final String KEY_OF_PATH_OPK = "h";
	private final String KEY_OF_VERSIONCODE = "i";
	private final String KEY_OF_IS_OFFER_APP = "j";
	private final String KEY_OF_APP_ID = "k";
	private final String KEY_OF_VERSION_NAME = "l";
	private final String KEY_OF_COMPLETE_PERCENTAGE = "m";
	private final String KEY_OF_TASK_ORIGIN = "n";
	private final String KEY_OF_IS_OPK = "o";
	private final String KEY_OF_IS_DELETE = "p";
	private final String KEY_OF_UPDATE_TIME = "q";
	private final String KEY_OF_APK_FILE_SIZE = "r";
	private final String KEY_OF_DESTURL = "s";

	private final String[] AllColumns = new String[]{KEY_OF_APPNAME, KEY_OF_ICONURL, KEY_OF_MD5SUM, KEY_OF_PACKAGENAME,
			KEY_OF_RAWURL, KEY_OF_DOWNLOADSTATUS, KEY_OF_PATH_APK, KEY_OF_PATH_OPK, KEY_OF_VERSIONCODE,
			KEY_OF_IS_OFFER_APP, KEY_OF_APP_ID, KEY_OF_VERSION_NAME, KEY_OF_COMPLETE_PERCENTAGE, KEY_OF_TASK_ORIGIN,
			KEY_OF_IS_OPK, KEY_OF_IS_DELETE, KEY_OF_UPDATE_TIME, KEY_OF_APK_FILE_SIZE, KEY_OF_DESTURL
	};
	private final String mOrderBy = KEY_OF_UPDATE_TIME + " DESC";
	private Context mAppContext;

	public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
		mAppContext = context.getApplicationContext();
	}

	public static synchronized DownloadDBHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DownloadDBHelper(context, DB_NAME, null, VERSION);
		}
		return mInstance;
	}

	private String getCreateDbSql() {
		return String.format(
				"CREATE TABLE IF NOT EXISTS %s (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s INTEGER,%s TEXT, %s TEXT,"
						+ "%s INTEGER,%s INTEGER,%s INTEGER,%s TEXT,%s BIGINT,"
						+ "%s INTEGER,%s INTEGER,%s INTEGER,%s INTEGER,"
						+ "%s BIGINT,%s TEXT"
						+ ");",
				TABLE_NAME, KEY_OF_APPNAME, KEY_OF_ICONURL, KEY_OF_MD5SUM, KEY_OF_PACKAGENAME, KEY_OF_RAWURL,
				KEY_OF_DOWNLOADSTATUS, KEY_OF_PATH_APK, KEY_OF_PATH_OPK, KEY_OF_VERSIONCODE, KEY_OF_IS_OFFER_APP,
				KEY_OF_APP_ID,
				KEY_OF_VERSION_NAME, KEY_OF_COMPLETE_PERCENTAGE, KEY_OF_TASK_ORIGIN, KEY_OF_IS_OPK,
				KEY_OF_IS_DELETE, KEY_OF_UPDATE_TIME, KEY_OF_APK_FILE_SIZE, KEY_OF_DESTURL
		);
	}

	private boolean updateElseInsert(SQLiteDatabase db, IndexGameNew downloadTask) {
		try {
			if (db == null || downloadTask == null) {
				return false;
			}
			final String selection = String.format("%s=? and %s=?", KEY_OF_PACKAGENAME, KEY_OF_RAWURL);
			final String[] selectionArgs = new String[2];
			selectionArgs[0] = downloadTask.packageName;
			selectionArgs[1] = downloadTask.downloadUrl;

			final int updateAffected = db.update(TABLE_NAME, getUpdateAppContentValues(downloadTask), selection,
					selectionArgs);
			if (updateAffected != 0) {// success update
				if (AppDebugConfig.IS_DEBUG) {
					KLog.v("DBHelper_Download", "Success Update pn:" + downloadTask.packageName);
				}
				return true;
			} else {
				final long newRowId = db.insert(TABLE_NAME, null, getUpdateAppContentValues(downloadTask));
				if (newRowId != -1) {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.v("DBHelper_Download", "Insert NewRowId:" + newRowId + "," +
								"pn:" + downloadTask.packageName);
					}
					return true;// success insert
				}
			}
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d("ERROR! Cannot Update Or Insert pn:" + downloadTask.packageName);
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return false;
	}

	private boolean deleteDownloadTask(IndexGameNew downloadTask) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.i("DBHelper_Download", "DownloadTaskToDeleteInDB:" + downloadTask);
			}
			if (downloadTask == null) {
				return false;
			}
			final String packageName = downloadTask.packageName;
			final String rawUrl = downloadTask.downloadUrl;
			if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(rawUrl)) {
				return false;
			}
			final String selection = String.format("%s=? and %s=?", KEY_OF_PACKAGENAME, KEY_OF_RAWURL);
			final String[] selectionArgs = {packageName, rawUrl};
			final boolean res = getReadableDatabase().delete(TABLE_NAME, selection, selectionArgs) != 0;
			if (AppDebugConfig.IS_DEBUG) {
				KLog.i("DBHelper_Download", String.format("Delete DownloadRecord %s: %b", rawUrl, res));
			}
			return res;
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return false;
	}

	private IndexGameNew getDownloadTaskFromCursor(Cursor cursor) {
		try {
			if (cursor == null) return null;
			final int appId = cursor.getInt(cursor.getColumnIndex(KEY_OF_APP_ID));
			final String rawUrl = cursor.getString(cursor.getColumnIndex(KEY_OF_RAWURL));
			final String packageName = cursor.getString(cursor.getColumnIndex(KEY_OF_PACKAGENAME));
			final String appName = cursor.getString(cursor.getColumnIndex(KEY_OF_APPNAME));
			final int versionCode = cursor.getInt(cursor.getColumnIndex(KEY_OF_VERSIONCODE));
			final String iconUrl = cursor.getString(cursor.getColumnIndex(KEY_OF_ICONURL));
			final String serverFileMd5 = cursor.getString(cursor.getColumnIndex(KEY_OF_MD5SUM));
			final int downloadStatus = cursor.getInt(cursor.getColumnIndex(KEY_OF_DOWNLOADSTATUS));
			final String versionName = cursor.getString(cursor.getColumnIndex(KEY_OF_VERSION_NAME));
			final long percentage = cursor.getLong(cursor.getColumnIndex(KEY_OF_COMPLETE_PERCENTAGE));
			final long apkFileSize = cursor.getLong(cursor.getColumnIndex(KEY_OF_APK_FILE_SIZE));
			final String destUrl = cursor.getString(cursor.getColumnIndex(KEY_OF_DESTURL));
			final IndexGameNew downloadTask = new IndexGameNew();
			downloadTask.id = appId;
			downloadTask.downloadUrl = rawUrl;
			downloadTask.packageName = packageName;
			downloadTask.name = appName;
//			downloadTask.setVersionCode(versionCode);
			downloadTask.img = iconUrl;
			downloadTask.apkMd5 = serverFileMd5;
			downloadTask.downloadStatus = DownloadStatus.value2Name(downloadStatus);
			downloadTask.versionName = versionName;
			downloadTask.completeSize = percentage;
			downloadTask.apkFileSize = apkFileSize;
			downloadTask.destUrl = destUrl;
			return downloadTask;
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return null;
	}

	private ContentValues getUpdateAppContentValues(IndexGameNew downloadTask) {
		final ContentValues contentValues = new ContentValues();
		if (!TextUtils.isEmpty(downloadTask.name)) {
			contentValues.put(KEY_OF_APPNAME, downloadTask.name);
		}
		if (!TextUtils.isEmpty(downloadTask.packageName)) {
			contentValues.put(KEY_OF_PACKAGENAME, downloadTask.packageName);
		}
		if (!TextUtils.isEmpty(downloadTask.img)) {
			contentValues.put(KEY_OF_ICONURL, downloadTask.img);
		}
		if (!TextUtils.isEmpty(downloadTask.downloadUrl)) {
			contentValues.put(KEY_OF_RAWURL, downloadTask.downloadUrl);
		}
		if (!TextUtils.isEmpty(downloadTask.destUrl)) {
			contentValues.put(KEY_OF_DESTURL, downloadTask.destUrl);
		}
		if (!TextUtils.isEmpty(downloadTask.apkMd5)) {
			contentValues.put(KEY_OF_MD5SUM, downloadTask.apkMd5);
		}
		final int status = downloadTask.downloadStatus.ordinal();
		contentValues.put(KEY_OF_DOWNLOADSTATUS, status);
		contentValues.put(KEY_OF_UPDATE_TIME, System.currentTimeMillis());
		contentValues.put(KEY_OF_APK_FILE_SIZE, downloadTask.apkFileSize);
		if (downloadTask.id > 0) {
			contentValues.put(KEY_OF_APP_ID, downloadTask.id);
		}
		if (!TextUtils.isEmpty(downloadTask.versionName)) {
			contentValues.put(KEY_OF_VERSION_NAME, downloadTask.versionName);
		}
		if (downloadTask.completeSize > 0) {
			contentValues.put(KEY_OF_COMPLETE_PERCENTAGE, downloadTask.completeSize);
		}
		return contentValues;
	}

	private boolean updateDownloadProgress(String url, int percent) {
		Cursor cursor = null;
		try {
			if (url == null) return false;
			if (TextUtils.isEmpty(url)) {
				return false;
			}
			final String selection = String.format("%s=?", KEY_OF_RAWURL);
			final String[] selectionArgs = new String[1];
			selectionArgs[0] = url;
			final ContentValues values = new ContentValues(1);
			values.put(KEY_OF_COMPLETE_PERCENTAGE, percent);
			final int updateAffected = getWritableDatabase().update(TABLE_NAME, values, selection, selectionArgs);
			return updateAffected != 0;
		}catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	//关闭数据库
	@Override
	public synchronized void close() {
		try {
			if (getReadableDatabase() != null) {
				getReadableDatabase().close();
			}
		}catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		final String sql = getCreateDbSql();
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			if (getReadableDatabase() != null) {
				getReadableDatabase().close();
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	@Override
	public void onDownloadStatusChanged(IndexGameNew appInfo) {
		try {
			if (appInfo == null || TextUtils.isEmpty(appInfo.downloadUrl)) {
				return;
			}
			final IndexGameNew item = appInfo;
			new Thread(new Runnable() {
				@Override
				public void run() {
					ApkDownloadManager dm = ApkDownloadManager.getInstance(mAppContext);
					String url = item.downloadUrl;
					if (dm.getAppDownloadStatus(url) == null) {
						deleteDownloadTask(item);
					} else {
						updateElseInsert(getWritableDatabase(), item);
					}
				}
			}).start();
		}catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	@Override
	public void onProgressUpdate(String url, int percent, long speedBytesPerS) {
		updateDownloadProgress(url, percent);
	}

	public void getDownloadList() {
		Cursor cursor = null;
		try {
			final SQLiteDatabase db = getReadableDatabase();
			cursor = db.query(TABLE_NAME, AllColumns, null, null, null, null, mOrderBy);
			IndexGameNew info;
			while (cursor.moveToNext()) {
				info = getDownloadTaskFromCursor(cursor);
				DownloadStatus ds = info.downloadStatus;
				info.setContext(mAppContext);
				info.initFile();
				switch (ds) {
					case DOWNLOADING:
					case PENDING:
					case PAUSED:
					case FAILED:
						info.downloadStatus = DownloadStatus.PAUSED;
						ApkDownloadManager.getInstance(mAppContext).addPausedTask(info);
						break;
					case FINISHED:
						if (!info.isFileExists()) {
							deleteDownloadTask(info);
							continue;
						}
						ApkDownloadManager.getInstance(mAppContext).addFinishedTask(info);
						break;

				}
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		} finally {
			try {
				if (cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				KLog.e(e);
			}
		}
	}
}
