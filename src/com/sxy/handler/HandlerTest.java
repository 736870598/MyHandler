package com.sxy.handler;

import java.util.UUID;

public class HandlerTest {

	public static void main(String[] args) {

		Looper.prepare();
		
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				System.out.println(Thread.currentThread().getName() + "--Ω” ’--" + msg.toString());
			};
		};

		for (int i = 0; i < 10; i++) {
			new Thread(){
				public void run() {
					while(true){
						Message msg = new Message();
						msg.what = 1;
						synchronized (UUID.class) {
							msg.obj = UUID.randomUUID() + "--";
						}
						System.out.println(Thread.currentThread().getName() + "--∑¢ÀÕ--" + msg.toString());
						handler.sendMessage(msg);

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
			}.start();
		}

		Looper.loop();
	}
}
