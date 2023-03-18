import java.io.*;  
import java.net.*;  
public class MyClient {  
    public static void main(String[] args) {  
        try {      
            Socket s=new Socket("localhost",6666);  
            
            DataInputStream dis=new DataInputStream(s.getInputStream());
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            // WRITE HELO
            System.out.println("\nWRITING TO SERVER: HELO");
            dout.writeUTF("HELO");
            dout.flush();
            
            // READ RESPONSE
            String str=(String)dis.readUTF();  
            System.out.println("\nRECEIVED: " + str + ", FROM SERVER");
            
            // WRITE BYE
            System.out.println("\nWRITING TO SERVER: BYE");
            dout.writeUTF("BYE");
            dout.flush();

            // READ RESPONSE
            str=(String)dis.readUTF();  
            System.out.println("\nRECEIVED: " + str + ", FROM SERVER");

            dout.close();
            s.close();
        } catch(Exception e) {
            System.out.println(e);
        }  
    }  
}  
