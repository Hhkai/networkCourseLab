
public class SR {
    public static final int WindowSize = 3;//
    public static final int Final = 50;//
    public static final int SEGMENTS =20;//
    public static final int Port = 7777;//

    public static Boolean flag =true;//
    public static void main(String[] args){
        Receiver receiver = new Receiver();
        receiver.start();
        Sender sender = new Sender();
        sender.start();
    }
}
