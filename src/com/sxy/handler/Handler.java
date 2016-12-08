package com.sxy.handler;

public class Handler {

	private Looper mLooper;
	private MessageQueue mQueue;


	/**
	 * �����߳��д���
	 */
	public Handler(){
		//��ȡ���߳�looper��
		mLooper = Looper.myLooper();
		this.mQueue = mLooper.mQueue;
	}
	/**
	 * ������Ϣ��ѹ�����
	 * @param msg
	 */
	public void sendMessage(Message msg){
		msg.target = this;
		mQueue.enqueueMessage(msg);
	}

	/**
	 * �ص�����
	 * @param msg
	 */
	public void handleMessage(Message msg) {
		

	}

	public void dispatchMessage(Message msg) {
		handleMessage(msg);
	}

}
