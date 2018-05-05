package lsy;


import udp.Receiver;
import udp.Sender;
public class SR {
    public static final int WindowSize = 3;//设置初始窗口大小
    public static final int Final = 50;//判断是否满足窗口大小应该满足的限制
    public static final int SEGMENTS =20;//设置报文段中数据包的个数
    public static final int Port = 7777;//设定端口号

    public static Boolean flag =true;//用来判断重发的数据的时候，即使再收到新的ACK，仍然等待原來的数据发送完毕
    public static void main(String[] args){
        Receiver receiver = new Receiver();
        receiver.start();
        Sender sender = new Sender();
        sender.start();
    }
}
