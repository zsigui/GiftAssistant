package net.youmi.android.libs.common.global.sensor;

import java.util.ArrayList;
import java.util.List;

import net.youmi.android.libs.common.debug.Debug_SDK;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * 只记录墙上的广告的包名状态
 * 
 * @author zhitaocai
 * @author zhitaocai edit on 2014-7-8
 * 
 */
abstract class BaseSensorDBManager {

	private final static boolean isLogOpen = true;

	private BaseSensorDBHelper mDbHelper;

	BaseSensorDBManager(Context context, BaseSensorDBHelper helper) {
		if (context == null) {
			throw new NullPointerException("Context must not be null");
		}
		mDbHelper = helper;
	}

	/**
	 * 检查数据库是否可以使用。
	 * 
	 * @param db
	 * @return
	 */
	private boolean checkDb(SQLiteDatabase db) {
		if (db == null) {
			return false;
		}
		return db.isOpen();
	}

	private void closeCursor(Cursor cursor) {
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
			}
		}
	}

	/**
	 * 关闭数据库
	 * 
	 * @param db
	 */
	private void closeDb(SQLiteDatabase db) {
		try {
			if (checkDb(db)) {
				db.close();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog && isLogOpen) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
			}
		}
	}

	/**
	 * 1、如果数据库没有记录就添加 <br>
	 * 2、如果数据库有记录就返回失败
	 * 
	 * @param model
	 * @return true 成功添加到数据库 <br>
	 *         false 没有成功添加到数据库
	 */
	boolean add(SensorModel model) {
		if (model == null) {
			return false;
		}

		synchronized (this) {

			SQLiteDatabase db = null;
			try {
				db = mDbHelper.getWritableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isGlobalLog && isLogOpen) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, this, "添加传感器数据到数据库失败，数据库不可用");
					}
					return false;
				}
				// 改用insert，方便后续维护
				ContentValues values = new ContentValues();
				values.put(BaseSensorDBHelper.GENERATE_TIME, model.mGenerateTime);
				values.put(BaseSensorDBHelper.SENSORVALUE_0, model.v0);
				values.put(BaseSensorDBHelper.SENSORVALUE_1, model.v1);
				values.put(BaseSensorDBHelper.SENSORVALUE_2, model.v2);
				values.put(BaseSensorDBHelper.SENSORVALUE_3, model.v3);
				values.put(BaseSensorDBHelper.SENSORVALUE_4, model.v4);
				values.put(BaseSensorDBHelper.SENSORVALUE_5, model.v5);
				long insertResult = -1;
				try {
					insertResult = db.insertOrThrow(BaseSensorDBHelper.TABLE_NAME, null, values);
				} catch (Throwable e) {
				}

				if (-1 == insertResult) {
					if (Debug_SDK.isGlobalLog && isLogOpen) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, this, "添加传感器数据到数据库[%s]失败 : %s", mDbHelper.getDbName(),
								model.toString());
					}
					return false;
				}
				if (Debug_SDK.isGlobalLog && isLogOpen) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, this, "添加传感器数据到数据库[%s]成功 : %s", mDbHelper.getDbName(),
							model.toString());
				}
				return true;
			} catch (Throwable e) {
				if (Debug_SDK.isGlobalLog && isLogOpen) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
				}
				return false;
			} finally {
				closeDb(db);
			}
		}
	}

	/**
	 * 根据指定包名删除记录
	 * 
	 * @param pn
	 */
	boolean deleteAll() {
		synchronized (this) {
			SQLiteDatabase db = null;
			try {
				db = mDbHelper.getWritableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isGlobalLog && isLogOpen) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, this, "删除数据失败，数据库不可用");
					}
					return false;
				}

				// 表名， where， whereValues
				if (0 != db.delete(BaseSensorDBHelper.TABLE_NAME, null, null)) {
					if (Debug_SDK.isGlobalLog && isLogOpen) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, this, "删除所有数据成功");
					}
					return true;
				}
				if (Debug_SDK.isGlobalLog && isLogOpen) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, this, "删除所有数据失败");
				}
			} catch (SQLException e) {
				if (Debug_SDK.isGlobalLog && isLogOpen) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
				}
			} finally {
				closeDb(db);
			}
		}
		return false;

	}

	/**
	 * 查询所有记录的所有数据
	 * 
	 * @return List<PnModel>
	 */
	List<SensorModel> queryAll() {

		synchronized (this) {

			SQLiteDatabase db = null;
			Cursor cursor = null;
			try {

				db = mDbHelper.getReadableDatabase();
				if (!checkDb(db)) {
					if (Debug_SDK.isGlobalLog && isLogOpen) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, this, "查询所有数据失败，数据库不可用");
					}
					return null;
				}

				// cursor = db.rawQuery("SELECT * FROM " + PnDBHelper.TABLE_NAME, null);
				cursor = db.query(BaseSensorDBHelper.TABLE_NAME, null, null, null, null, null,
						BaseSensorDBHelper.GENERATE_TIME);
				if (0 == cursor.getCount()) {
					if (Debug_SDK.isGlobalLog && isLogOpen) {
						Debug_SDK.ti(Debug_SDK.mGlobalTag, this, "查询所有数据失败，没有查到记录，数据库可能为空");
					}
					return null;
				}
				List<SensorModel> lists = new ArrayList<SensorModel>();
				while (cursor.moveToNext()) {
					try {
						SensorModel model = new SensorModel();
						model.mGenerateTime = Long.valueOf(cursor.getString(cursor
								.getColumnIndex(BaseSensorDBHelper.GENERATE_TIME)));
						model.v0 = cursor.getFloat(cursor.getColumnIndex(BaseSensorDBHelper.SENSORVALUE_0));
						model.v1 = cursor.getFloat(cursor.getColumnIndex(BaseSensorDBHelper.SENSORVALUE_1));
						model.v2 = cursor.getFloat(cursor.getColumnIndex(BaseSensorDBHelper.SENSORVALUE_2));
						model.v3 = cursor.getFloat(cursor.getColumnIndex(BaseSensorDBHelper.SENSORVALUE_3));
						model.v4 = cursor.getFloat(cursor.getColumnIndex(BaseSensorDBHelper.SENSORVALUE_4));
						model.v5 = cursor.getFloat(cursor.getColumnIndex(BaseSensorDBHelper.SENSORVALUE_5));
						lists.add(model);
					} catch (Throwable e) {
						if (Debug_SDK.isGlobalLog && isLogOpen) {
							Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
						}
					}
				}
				if (Debug_SDK.isGlobalLog && isLogOpen) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, this, "查询所有数据结束，数据库记录数量：%d ，最后列表数量：%d", cursor.getCount(),
							lists.size());
				}
				return lists;

			} catch (Exception e) {
				if (Debug_SDK.isGlobalLog && isLogOpen) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
				}
			} finally {
				closeCursor(cursor);
				closeDb(db);
			}
		}
		return null;
	}

}
