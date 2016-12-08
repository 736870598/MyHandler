package com.sxy.handler;

public class Message {

	public Handler target;
	public int what;
	public Object obj;
	
	@Override
	public String toString() {
		return obj.toString();
	}
	
}
