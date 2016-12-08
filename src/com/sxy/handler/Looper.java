package com.sxy.handler;


public final class Looper {
	
	//Loop���󱣴���ThreadLocal�У���֤���߳����ݵĸ���
	private static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>();
	
	//һ��looper�����Ӧһ����Ϣ����
	public MessageQueue mQueue;
	
	private Looper(){
		mQueue = new MessageQueue();
	}
	
	/**
	 * looper��ʼ��
	 */
	public static void prepare(){
		if(sThreadLocal.get() != null){
			  throw new RuntimeException("Only one Looper may be created per thread");
		}
		sThreadLocal.set(new Looper());
	}

	/**
	 * ��ȡ��ǰ�̵߳�looper
	 * @return
	 */
	public static Looper myLooper(){
		return sThreadLocal.get();
	}
	
	/**
	 * ��ѯ��Ϣ����
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
			//ת����handler
			msg.target.dispatchMessage(msg);
		}
	}
}
