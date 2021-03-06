package com.langsin.im.client;

import com.langsin.im.msg.MsgHead;
 
/**
 *即时通信系统 
 *通信模块的消息处理监听器接口定义
 */
public interface IClientMsgListener {

	/**
	 * 处理接收到的一条消息
	 * @param msg:接收到的消息对象
	 */
	public void fireMsg(MsgHead msg);
	
}
