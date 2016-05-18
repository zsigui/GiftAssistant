package net.youmi.android.libs.common.global.sensor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 用于记录传感器数据状态
 *
 * @author zhitaocai
 * @since 2015-2-2下午9:10:13
 */
class BaseSensorDBHelper extends SQLiteOpenHelper {

	/**
	 * 数据库版本更新情况,以后每次更新都需要记录一下:
	 * <ol>
	 * <li>初始化，key：包名 是否在进行任务汇总</li>
	 * </ol>
	 */
	private final static int DATABASE_VERSION = 1;

	/**
	 * sensor value
	 */
	final static String TABLE_NAME = "sv";

	/**
	 * 生成时间
	 */
	final static String GENERATE_TIME = "gt";

	/**
	 * SensorEvent.values[0]
	 */
	final static String SENSORVALUE_0 = "v0";

	/**
	 * SensorEvent.values[1]
	 */
	final static String SENSORVALUE_1 = "v1";

	/**
	 * SensorEvent.values[2]
	 */
	final static String SENSORVALUE_2 = "v2";

	/**
	 * SensorEvent.values[3]
	 */
	final static String SENSORVALUE_3 = "v3";

	/**
	 * SensorEvent.values[4]
	 */
	final static String SENSORVALUE_4 = "v4";

	/**
	 * SensorEvent.values[5]
	 */
	final static String SENSORVALUE_5 = "v5";

	private String mDbName;

	BaseSensorDBHelper(Context context, String dbName) {
		super(context, dbName, null, DATABASE_VERSION);
		mDbName = dbName;
	}

	/**
	 * 创建数据库，要注意sql语句的使用，容易出错
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = String.format(
				"create table if not exists %s (%s varchar primary key, %s varchar, %s varchar, %s varchar, %s " +
						"varchar, %s " +
						"varchar, %s varchar);",
				TABLE_NAME, GENERATE_TIME, SENSORVALUE_0, SENSORVALUE_1, SENSORVALUE_2, SENSORVALUE_3, SENSORVALUE_4,
				SENSORVALUE_5);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = String.format("drop table if exists %s", TABLE_NAME);
		db.execSQL(sql);
		onCreate(db);
	}

	/**
	 * 测试用
	 *
	 * @return
	 */
	String getDbName() {
		return mDbName;
	}

}