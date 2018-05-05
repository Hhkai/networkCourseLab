
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class Receiver extends Thread {
    private static DatagramPacket receivePacket;
    private static DatagramPacket SendACK;
    private static DatagramSocket Client;
    private static byte[] receive = new byte[1024];
    private static byte[] send = new byte[20];
    private static int last;
    private static int Segments; 
    public Receiver(){
        last = -1;
        Segments = SR.SEGMENTS;
        try {
            Client = new DatagramSocket(SR.Port);
        }catch (SocketException e){
            System.out.println("error :cannot open!");
        }
    }
    @Override
    public void run(){
        while (true){
            receivePacket = new DatagramPacket(receive,receive.length);
            send = new String("time").getBytes();
            try{
                Client.receive(receivePacket);
            }catch (IOException e){
                System.out.println("error failed");
            }
            int Sequences = -1;
            if(receive[0]=='t'){
                Date day=new Date();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println(df.format(day));
            }
            else if(receive[1] == 'k'){
                Sequences = receive[0] - '0';
            }
            else if(receive[1]=='u'){
                System.out.println("bye");
                return;
            }
            else {
                Sequences = (receive[0]-'0')*10+(receive[1]-'0');
            }
            //
            if(Math.random()<0.8){
                if (Sequences == last+1){
                    //
                    if(Sequences / 10 == 0){
                        send = new String("ACK"+Sequences+"m").getBytes();
                    }
                    else if(Sequences / 100 == 0){
                        send = new String("ACK"+Sequences).getBytes();
                    }
                    InetAddress inetAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    SendACK = new DatagramPacket(send,send.length,inetAddress,clientPort);
                    try{
                        Client.send(SendACK);
                        Segments--;
                        last++;
                    }catch (IOException e){
                    }
                }
                // else if(Sequences >(last+1)){
                else {
                    System.out.println("should accept the "+(last+1)+"th package, and actually accept the "+Sequences+"th package");
                    if((last)/10== 0){
                        send = new String("ACK"+(last)+"m").getBytes();
                    }else if((last)/100 == 0){
                        send = new String("ACK"+(last)).getBytes();
                    }
                    InetAddress inetAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    SendACK = new DatagramPacket(send,send.length,inetAddress,clientPort);
                    try{
                        Client.send(SendACK);
                    }catch (IOException e){
                    }
                }

            }else{
                //
            }
        }
    }
}
