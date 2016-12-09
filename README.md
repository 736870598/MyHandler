# MyHandler
java层自定义实现handler基本原理，更彻底的了解Handler，Looper，MessageQueue的关系及原理

成员:

  1. Looper: looper由ThreadLocal创建并管理 

  2. handler: 每一个handler中有looper和messagQqueue 

  3. MessageQueue：messageQueue由Loop创建并管理
  
  4. Message:消息体
  
原理：

handler的构造函数中默认先looper.myLooper();用于判断是否有looper实例，looper的构造函数中创建了messageQueue。
当ActivityThead创建的时候默认创建了主线程的looper，所以在非UI线程中new Handler的时候要先Looper.prepare()  最后再 Looper.loop()。
通过handler发送消息发送到messageQueue中，looper轮询从messageQueue中获取消息执行handler的回调函数。
messageQueue的入队方法enqueueMessage主要在子线程中由handler.sendMessage()方法调用。
messageQueue的出队方法next由looper的loop()方法在主线程中轮询时调用，轮询得到的message再由message 的target回调出去。

特变声明：

handler的核心是ThreadLocal。。。。
关于ThreadLocal的知识请参考http://blog.csdn.net/lufeng20/article/details/24314381
handler的代码非常复杂，涉及java层，frameworks层等，管道唤醒机制等。下面的代码只是简单的模拟了handler的工作原理。
以下为简单的实现handler的工作原理：

Handler：

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

Message：

          public class Message {
                public Handler target;
                public int what;
                public Object obj;

                @Override
                public String toString() {
                      return obj.toString();
                }
          }


Looper：

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



MessageQueue：

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


测试类：

    public class HandlerTest {
          public static void main(String[] args) {
                Looper.prepare();

                final Handler handler = new Handler(){
                      public void handleMessage(Message msg) {
                            System.out.println(Thread.currentThread().getName() + "--接收--" + msg.toString());
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
                                        System.out.println(Thread.currentThread().getName() + "--发送--" + msg.toString());
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
