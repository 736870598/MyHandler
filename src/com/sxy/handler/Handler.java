package com.sxy.handler;

public class Handler {

	private Looper mLooper;
	private MessageQueue mQueue;


	/**
	 * 在主线程中创建
	 */
	public Handler(){
		//获取主线程looper；
		mLooper = Looper.myLooper();
		this.mQueue = mLooper.mQueue;
	}
	/**
	 * 发送消息，压入队列
	 * @param msg
	 */
	public void sendMessage(Message msg){
		msg.target = this;
		mQueue.enqueueMessage(msg);
	}

	/**
	 * 回调函数
	 * @param msg
	 */
	public void handleMessage(Message msg) {
		

	}

	public void dispatchMessage(Message msg) {
		handleMessage(msg);
	}

}
