package com.sxy.handler;


public final class Looper {
	
	//Loop对象保存在ThreadLocal中，保证了线程数据的隔离
	private static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>();
	
	//一个looper对象对应一个消息队列
	public MessageQueue mQueue;
	
	private Looper(){
		mQueue = new MessageQueue();
	}
	
	/**
	 * looper初始化
	 */
	public static void prepare(){
		if(sThreadLocal.get() != null){
			  throw new RuntimeException("Only one Looper may be created per thread");
		}
		sThreadLocal.set(new Looper());
	}

	/**
	 * 获取当前线程的looper
	 * @return
	 */
	public static Looper myLooper(){
		return sThreadLocal.get();
	}
	
	/**
	 * 轮询消息队列
	 */
	public static void loop(){
		Looper me = myLooper();
		if(me == null){
			 throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
		}
		MessageQueue queue = me.mQueue;
		for(;;){
			Message msg = queue.next();
			if(msg == null){
				continue;
			}
			//转发给handler
			msg.target.dispatchMessage(msg);
		}
	}
}
