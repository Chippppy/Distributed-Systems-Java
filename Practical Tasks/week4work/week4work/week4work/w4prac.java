import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class w4prac {
    
    public static void main(String[] args) {  
        try {      
            Socket s=new Socket("127.0.0.1",50000);  
            
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            // WRITE HELO
            System.out.println("C: Send HELO\n");
            dout.write(("HELO\n").getBytes());
            dout.flush();
            
            // READ RESPONSE
            String str=(String)dis.readLine();  
            System.out.println("C: Recv " + str + ", FROM SERVER\n");
            
            // WRITE BYE
            String username = System.getProperty("user.name");
            System.out.println("C: Send AUTH with username: "+username+"\n");
            dout.write(("AUTH "+username+"\n").getBytes());
            dout.flush();

            // READ RESPONSE
            str=(String)dis.readLine(); 
            System.out.println("C: Recv " + str + ", FROM SERVER\n");

            // SENDIGN REDY
            System.out.println("C: Send REDY\n");
            dout.write(("REDY\n").getBytes());
            dout.flush();

            // READ RESPONSE
            str=(String)dis.readLine(); 
            System.out.println("C: Recv " + str + ", FROM SERVER\n");

            // QUIT SERVER
            System.out.println("C: Send QUIT to server");
            dout.write(("QUIT\n").getBytes());
            dout.flush();

            // READ RESPONSE
            str=(String)dis.readLine(); 
            System.out.println("C: Recv " + str + ", FROM SERVER\n");

            dout.close();
            s.close();
        } catch(Exception e) {
            System.out.println(e);
        }  
    }

}
