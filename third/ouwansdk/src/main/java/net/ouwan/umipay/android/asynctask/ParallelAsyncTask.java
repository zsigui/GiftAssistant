package net.ouwan.umipay.android.asynctask;

import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.Executors;

/**
 * ParallelAsyncTask
 * <p/>
 * 兼容 >=api11 以后AsyncTask串行执行
 *
 * @author zacklpx
 *         date 15-1-30
 *         description
 */
public abstract class ParallelAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	public AsyncTask<Params, Progress, Result> excuteParallel(Params... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return executeOnExecutor(Executors.newCachedThreadPool(), params);
		} else {
			return execute(params);
		}
	}
}
