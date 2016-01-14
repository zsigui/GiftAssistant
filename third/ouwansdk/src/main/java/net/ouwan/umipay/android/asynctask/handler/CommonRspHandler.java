package net.ouwan.umipay.android.asynctask.handler;

/**
 * CommonRspHandler
 *
 * @author zacklpx
 *         date 15-4-15
 *         description
 */
public abstract class CommonRspHandler<GsonEntry> {

	public abstract void toHandle(GsonEntry data);
}
