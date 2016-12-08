package com.sxy.handler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageQueue {

	//��Ϣ�������
	private Message[] items;

	/**
	 * �롢��������
	 */
	private int putIndex;
	private int takeIndex;

	//������
	private int count;

	//������
	private Lock lock;
	//��������
	private Condition notEmpty;
	private Condition notFull;

	public MessageQueue(){
		//��Ϣ�����д�С����
		this.items = new Message[50];
		this.lock = new ReentrantLock();
		this.notEmpty = lock.newCondition();
		this.notFull = lock.newCondition();
	}

	/**
	 * �������  ��Ҫ�����߳��е���
	 * ����
	 */
	public void enqueueMessage(Message msg){
		try {
			lock.lock();
			while (count == items.length) {
				try {
					notFull.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			items[putIndex] = msg;
			putIndex = (++putIndex == items.length) ? 0 : putIndex;
			count++;
			//�����˲�Ʒ ֪ͨ������
			notEmpty.signalAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * ������  ��Ҫ�����߳��е���
	 * ����
	 */
	public Message next(){
		Message msg = null;
		try{
			lock.lock();
			while(count == 0){
				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			msg = items[takeIndex];
			items[takeIndex] = null;//Ԫ���ÿ�
			takeIndex = (++takeIndex == items.length) ? 0 : takeIndex;
			count--;
			
			//�����˲�Ʒ��������
			notFull.signalAll();
			
		}finally{
			lock.unlock();
		}
		return msg;
	}
}
