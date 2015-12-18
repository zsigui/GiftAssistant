package net.youmi.android.libs.common.v2.download.model;

import java.io.Serializable;

/**
 * 下载任务的扩展对象,这样子就可以让让每个下载任务对象绑定一个自定义的对象
 * <p/>
 * <b>实现本接口的类必须显式声明一个不传参数的构造函数，并且能通过 {@link #fromSerializableString(String)} 方法反序列化自身</b>
 *
 * @author zhitao
 * @since 2015-09-09 16:53
 */
public interface IFileDownloadTaskExtendObject extends Serializable {

	/**
	 * 判断该数据模型是否有效
	 * 如果没有扩展对象的话，必须返回true
	 *
	 * @return
	 */
	boolean isExtendObjectValid();

	/**
	 * 序列化为一个字符串对象，方便写入到数据库文件中
	 *
	 * @return
	 */
	String toSerializableString();

	/**
	 * 从一个字符串中反序列化出本对象
	 *
	 * @param string
	 */
	void fromSerializableString(String string);
}
