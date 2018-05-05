import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProxyTask implements Runnable {
    private Socket socketIn;
    private Socket socketOut;
    
    

    private long totalUpload=0l;
    private long totalDownload=0l;

    public ProxyTask(Socket socket) {
        this.socketIn = socket;
    }

    private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
   
    private static final String AUTHORED = "HTTP/1.1 200 Connection established\r\n\r\n";
    
    //private static final String UNAUTHORED="HTTP/1.1 407 Unauthorized\r\n\r\n";  
    
    private static final String SERVERERROR = "HTTP/1.1 500 Connection FAILED\r\n\r\n";
    
    private static final boolean banUser = true;
    // private static final boolean banUser = false;

    @Override
    public void run() {

        StringBuilder builder=new StringBuilder();
        try {
            builder.append("\r\n").append("Request Time  :" + sdf.format(new Date()));

            InputStream isIn = socketIn.getInputStream();
            OutputStream osIn = socketIn.getOutputStream();
            
            HttpHeader header = HttpHeader.readHeader(isIn);

           
            builder.append("\r\n").append("From    Host  :" + socketIn.getInetAddress());
            builder.append("\r\n").append("From    Port  :" + socketIn.getPort());
            builder.append("\r\n").append("Proxy   Method:" + header.getMethod());
            builder.append("\r\n").append("Request Host  :" + header.getHost());
            builder.append("\r\n").append("Request Port :" + header.getPort());

            if (banUser && "\127.0.0.1".equals(socketIn.getInetAddress())) {
                return;
            }
            if ("bbs.hupu.com".equals(header.getHost())) {
                builder.append("\r\n").append("this host is denied:" + header.getHost());
                return ;
            }
            if ("www.google.cn".equals(header.getHost())) {
                builder.append("\r\n").append("fishing you");
                header.setHost("jwts.hit.edu.cn");
                header.setMethod("GET");
                header.setPort("80");
            }
            if (header.getHost() == null || header.getPort() == null) {
                osIn.write(SERVERERROR.getBytes());
                osIn.flush();
                return ;
            }

            
            socketOut = new Socket(header.getHost(), Integer.parseInt(header.getPort()));
            socketOut.setKeepAlive(true);
            InputStream isOut = socketOut.getInputStream();
            OutputStream osOut = socketOut.getOutputStream();
             
            Thread ot = new DataSendThread(isOut, osIn);
            ot.start();
            if (header.getMethod().equals(HttpHeader.METHOD_CONNECT)) {
                
                osIn.write(AUTHORED.getBytes());
                osIn.flush();
            }else{
                 
                byte[] headerData=header.toString().getBytes();
                totalUpload+=headerData.length;
                osOut.write(headerData);
                osOut.flush();
            }
             
            readForwardDate(isIn, osOut);
             
            ot.join();
        } catch (Exception e) {
            e.printStackTrace();
            if(!socketIn.isOutputShutdown()){
                
                try {
                    socketIn.getOutputStream().write(SERVERERROR.getBytes());
                } catch (IOException e1) {}
            }
        } finally {
            try {
                if (socketIn != null) {
                    socketIn.close();
                }
            } catch (IOException e) {}
            if (socketOut != null) {
                try {
                    socketOut.close();
                } catch (IOException e) {}
            }
            
            builder.append("\r\n").append("Up    Bytes :" + totalUpload);
            builder.append("\r\n").append("Down  Bytes :" + totalDownload);
            builder.append("\r\n").append("Closed Time :" + sdf.format(new Date()));
            builder.append("\r\n");
            logRequestMsg(builder.toString());
        }
    }

    /**
     * 
     * @param msg
     */
    private synchronized void logRequestMsg(String msg){
        System.out.println(msg);
    }

    /**
     * 
     *
     * @param isIn
     * @param osOut
     */
    private void readForwardDate(InputStream isIn, OutputStream osOut) {
        byte[] buffer = new byte[4096];
        try {
            int len;
            while ((len = isIn.read(buffer)) != -1) {
                if (len > 0) {
                    osOut.write(buffer, 0, len);
                    osOut.flush();
                }
                totalUpload+=len;
                if (socketIn.isClosed() || socketOut.isClosed()) {
                    break;
                }
            }
        } catch (Exception e) {
            try {
                socketOut.close();//  
            } catch (IOException e1) {

            }
        }
    }

    /**
     * 
     *
     * @param isOut
     * @param osIn
     */
    class DataSendThread extends Thread {
        private InputStream isOut;
        private OutputStream osIn;

        DataSendThread(InputStream isOut, OutputStream osIn) {
            this.isOut = isOut;
            this.osIn = osIn;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[4096];
            try {
                int len;
                while ((len = isOut.read(buffer)) != -1) {
                    if (len > 0) {
                        // logData(buffer, 0, len);  
                        osIn.write(buffer, 0, len);
                        osIn.flush();
                        totalDownload+=len;
                    }
                    if (socketIn.isOutputShutdown() || socketOut.isClosed()) {
                        break;
                    }
                }
            } catch (Exception e) {}
        }
    }

}  