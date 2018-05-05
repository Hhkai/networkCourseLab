import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketProxy {
    private static final int listenport = 10240;
    public static void main(String args[]) throws IOException {
        SimpleDateFormat datatime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ServerSocket serverSocket = new ServerSocket(listenport);
        final ExecutorService tpe = Executors.newCachedThreadPool();
        System.out.println("Proxy Server Start At "+datatime.format(new Date()));
        System.out.println("listening port:"+listenport+"......");
        System.out.println();
        System.out.println();
        while(true){
            Socket socket = null;
            try{
                socket = serverSocket.accept();
                socket.setKeepAlive(true);
                //System.out.println("got it");
                tpe.execute(new ProxyTask(socket));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
