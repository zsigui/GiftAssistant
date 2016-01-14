package net.ouwan.umipay.android.asynctask;

import android.os.Process;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CommandTask
 *
 * @author zacklpx
 *         date 15-4-9
 *         description
 */
public abstract class CommandTask<Params, Result> {
	private static final String LOG_TAG = "CommandTask";

	private static volatile Executor sDefaultExecutor = Executors.newCachedThreadPool();
	private final WorkerRunnable<Params, Result> mWorker;
	private final FutureTask<Result> mFuture;

	private volatile Status mStatus = Status.PENDING;

	private final AtomicBoolean mCancelled = new AtomicBoolean();
	private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "CommandTask #" + mCount.getAndIncrement());
		}
	};

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);

	/**
	 * Indicates the current status of the task. Each status will be set only once
	 * during the lifetime of a task.
	 */
	public enum Status {
		/**
		 * Indicates that the task has not been executed yet.
		 */
		PENDING,
		/**
		 * Indicates that the task is running.
		 */
		RUNNING,
		/**
		 * Indicates that the task has finished.
		 */
		FINISHED,
	}

	public CommandTask() {
		mWorker = new WorkerRunnable<Params, Result>() {
			@Override
			public Result call() throws Exception {
				mTaskInvoked.set(true);
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				return postResult(doInBackground(mParams));
			}
		};

		mFuture = new FutureTask<Result>(mWorker) {
			@Override
			protected void done() {
				try {
					postResultIfNotInvoked(get());
				} catch (InterruptedException e) {
					android.util.Log.w(LOG_TAG, e);
				} catch (ExecutionException e) {
					throw new RuntimeException("An error occured while executing doInBackground()",
							e.getCause());
				} catch (CancellationException e) {
					postResultIfNotInvoked(null);
				}
			}
		};
	}

	private void postResultIfNotInvoked(Result result) {
		final boolean wasTaskInvoked = mTaskInvoked.get();
		if (!wasTaskInvoked) {
			postResult(result);
		}
	}

	private Result postResult(Result result) {
		finish(result);
		return result;
	}

	/**
	 * Returns the current status of this task.
	 *
	 * @return The current status.
	 */
	public final Status getStatus() {
		return mStatus;
	}

	protected abstract Result doInBackground(Params... params);

	protected void onPreExecute() {
	}

	protected void onPostExecute(Result result) {
	}

	protected void onCancelled(Result result) {
		onCancelled();
	}

	protected void onCancelled() {
	}

	public final boolean isCancelled() {
		return mCancelled.get();
	}

	public final boolean cancel(boolean mayInterruptIfRunning) {
		mCancelled.set(true);
		return mFuture.cancel(mayInterruptIfRunning);
	}

	public final CommandTask<Params, Result> execute(Params... params) {
		return executeOnExecutor(sDefaultExecutor, params);
	}

	public final CommandTask<Params, Result> executeOnExecutor(Executor exec, Params... params) {
		if (mStatus != Status.PENDING) {
			switch (mStatus) {
				case RUNNING:
					throw new IllegalStateException("Cannot execute task:"
							+ " the task is already running.");
				case FINISHED:
					throw new IllegalStateException("Cannot execute task:"
							+ " the task has already been executed "
							+ "(a task can be executed only once)");
			}
		}

		mStatus = Status.RUNNING;

		onPreExecute();

		mWorker.mParams = params;
		exec.execute(mFuture);

		return this;
	}

	private void finish(Result result) {
		if (isCancelled()) {
			onCancelled(result);
		} else {
			onPostExecute(result);
		}
		mStatus = Status.FINISHED;
	}

	private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
		Params[] mParams;
	}
}
