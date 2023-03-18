import java.io.*;  
import java.net.*;  
public class MyServer {  
    public static void main(String[] args){  
        try {  
            ServerSocket ss=new ServerSocket(6666);  
            Socket s=ss.accept();//establishes connection
            System.out.println("\nCONNECTED TO SOCKET");
            DataInputStream dis=new DataInputStream(s.getInputStream());
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            
            // READ RESPONSE
            String str=(String)dis.readUTF();  
            System.out.println("\nRECEIVED: " + str + ", FROM CLIENT");
            
            // WRITE G'DAY
            System.out.println("\nWRITING TO CLIENT: G'DAY");
            dout.writeUTF("G'DAY");
            dout.flush();

            // READ RESPONSE
            str=(String)dis.readUTF();  
            System.out.println("\nRECEIVED: " + str + ", FROM CLIENT");

            // WRITE BYE
            System.out.println("\nWRITING TO CLIENT: BYE");
            dout.writeUTF("BYE");
            dout.flush();

            dout.close();
            s.close();
            ss.close();
        } catch(Exception e) {
            System.out.println(e);
        }  
    }  
}  
