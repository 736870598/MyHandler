package com.sxy.handler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageQueue {

	//消息保存队列
	private Message[] items;

	/**
	 * 入、出队索引
	 */
	private int putIndex;
	private int takeIndex;

	//计数器
	private int count;

	//互斥锁
	private Lock lock;
	//条件变量
	private Condition notEmpty;
	private Condition notFull;

	public MessageQueue(){
		//消息队列有大小限制
		this.items = new Message[50];
		this.lock = new ReentrantLock();
		this.notEmpty = lock.newCondition();
		this.notFull = lock.newCondition();
	}

	/**
	 * 加入对列  主要在子线程中调用
	 * 生产
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
			//生成了产品 通知消费者
			notEmpty.signalAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 出队列  主要在主线程中调用
	 * 消费
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
			items[takeIndex] = null;//元素置空
			takeIndex = (++takeIndex == items.length) ? 0 : takeIndex;
			count--;
			
			//消费了产品继续生成
			notFull.signalAll();
			
		}finally{
			lock.unlock();
		}
		return msg;
	}
}
