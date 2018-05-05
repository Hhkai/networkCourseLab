
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sender extends Thread {

    private static int Final ;
    private static int begin = 0, end;
    private static int Segments;
    private static int Remain;
    static Timer timer;
    private static InetAddress inetAddress;
    private static DatagramSocket ServerSocket;
    private byte[] receive = new byte[20];
    private byte[] send = new byte[1024];

    public Sender(){
        try {
            inetAddress = InetAddress.getByName("localhost");
        }catch (UnknownHostException e){
        }
        end = begin + SR.WindowSize -1;
        Segments = SR.SEGMENTS;
        Remain = Segments;//
        Final = SR.Final;
        try {
            ServerSocket = new DatagramSocket();
        }catch (SocketException e){
        }
       timer = new Timer(3000,new DelayActionListener(ServerSocket,begin));
        timer.start();
//        for (int i = begin; i <= end; i++){
//            if(i  / 10 == 0 ){
//                send = (new String(i+"k"+"seq")).getBytes();
//            }
//            else if(i  / 100 == 0){
//                send = (new String(i+" "+"seq")).getBytes();
//            }
//            DatagramPacket sendPacket = new DatagramPacket(send,send.length,inetAddress, SR.Port);
//            try {
//                ServerSocket.send(sendPacket);
//                Remain--;
//                System.out.println("**--> "+i);
//            }catch (IOException e){
//            }
//        }
        send = (new String("time")).getBytes();
        DatagramPacket sendPacket = new DatagramPacket(send,send.length,inetAddress,SR.Port);
        try{
            ServerSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        while (true){
            DatagramPacket receivePacket = new DatagramPacket(receive,receive.length);
            try{
                ServerSocket.receive(receivePacket);
                int ackNum = -1;
                if(receive[4] == 'm'){
                    ackNum = receive[3]-'0';
                }else if(receive.toString().equals("time")){
                    Date day=new Date();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    send = df.toString().getBytes();
                    System.out.println(df.format(day));
                    DatagramPacket sendPacket = new DatagramPacket(send,send.length,inetAddress,SR.Port);
                    try {
                        ServerSocket.send(sendPacket);
                        Remain--;
                        System.out.println("** --> sender send the "+end);
                    }catch (IOException e){

                    }

                }else if(receive.toString().equals("quit")){
                    return;
                }
                else {
                    ackNum = (receive[3]-'0')*10 + (receive[4]-'0');
                }
                if(ackNum<0)
                {
                    ackNum=-1;
                }
                if (Math.random() < 0.2) {
                    System.out.println("ACK miss: " + ackNum);
                    ackNum = -1;
                }
                System.out.println("**<-- receive the ACK: "+ackNum);
                if(ackNum == Segments -1){
                    System.out.println("******************* send voer ******************* ");
                    timer.stop();
                    send = (new String("quit")).getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(send,send.length,inetAddress,SR.Port);
                    try{
                        ServerSocket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return;
                }else if(ackNum >= begin && Remain > 0){

                    int increase = (ackNum-begin+1);
                    for(int i=0;i<increase;i++)
                    {
                        
                        timer.stop();
                        //

                        begin++;
                        end++;
                        if(begin>=Segments-1)
                        {
                            begin=Segments-1;
                        }
                        if(end>=Segments-1)
                        {
                            end=Segments-1 ;
                        }
                        if(end / 10 == 0 ){
                            send = (new String(end+"k "+"seq")).getBytes();
                        }
                        else if(end / 100 == 0){
                            send = (new String(end+" "+"seq")).getBytes();
                        }
                        while(SR.flag==false);
                        DatagramPacket sendPacket = new DatagramPacket(send,send.length,inetAddress,SR.Port);
                        try {
                            ServerSocket.send(sendPacket);
                            Remain--;
                            System.out.println("**--> sender send the "+end);
                        }

                        catch (IOException e){
                        }
                    }
                    //
                    timer = new Timer(3000,new DelayActionListener(ServerSocket,begin));
                    timer.start();
                }
            }
            catch (IOException e){
            }
        }
    }

    //
    class DelayActionListener implements ActionListener{
        private DatagramSocket socket;
        private int seqNo;
        public DelayActionListener(DatagramSocket ServerSocket, int seqNo){
            this.socket = ServerSocket;
            this.seqNo = seqNo;
        }
        @Override
        public void actionPerformed(ActionEvent e){
            SR.flag= false;
            Sender.timer.stop();
            Sender.timer = new Timer(3000,new DelayActionListener(socket,seqNo));
            Sender.timer.start();
            int end = seqNo+SR.WindowSize -1;
            System.out.println("!!--> prepare to resend " + seqNo +" -- " + end);
            for(int i = seqNo; i <= end; i++){
                byte[] sendData = null;
                InetAddress clientAddress = null;
                try {
                    clientAddress = InetAddress.getByName("localhost");
                    if(i  / 10 == 0 ){
                        sendData = (new String(i+"k "+"seq")).getBytes();
                    }
                    else if(i / 100 == 0){
                        sendData = (new String(i+" "+"seq")).getBytes();
                    }
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, SR.Port);
                    socket.send(sendPacket);
                    System.out.println("**--> sender send the" + i);
                } catch (Exception e1) {
                }
            }
            SR.flag= true;
        }
    }
}