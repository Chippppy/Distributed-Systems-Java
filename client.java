import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class client {
    
    public static void main(String[] args) {  
        try {      
            Socket s=new Socket("127.0.0.1",50000);  
            
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
			String firstResponse = initHandshake(s, dout, dis);
			// The below line runs every time... Even if firstResponse is equal to "OK" Not sure whats happening...
			//if(firstResponse != "OK") {quitSession(s, dout, dis);};
			
			// SEND FIRST REDY
			System.out.println("C: Send REDY\n");
			dout.write(("REDY\n").getBytes());
			dout.flush();

			// READ RESPONSE
			String response=(String)dis.readLine(); 
			System.out.println("C: Recv " + response + ", FROM SERVER\n");

			scheduleJob(s, dout, dis, response);

			// READ RESPONSE
			response=(String)dis.readLine(); 
			System.out.println("C: Recv " + response + ", FROM SERVER\n");

			quitSession(s, dout, dis);

            dout.close();
            s.close();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

	public static String initHandshake(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		// SEND HELO
		System.out.println("C: Send HELO\n");
		output.write(("HELO\n").getBytes());
		output.flush();

		// READ RESPONSE
		String str=(String)input.readLine();  
        System.out.println("C: Recv " + str + ", FROM SERVER\n");

		// SEND AUTH
		String username = System.getProperty("user.name");
		System.out.println("C: Send AUTH with username: "+username+"\n");
		output.write(("AUTH "+username+"\n").getBytes());
		output.flush();

		// READ RESPONSE
		str=(String)input.readLine();   
		System.out.println("C: Recv " + str + ", FROM SERVER\n");

		return str;
	}

	public static void quitSession(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		// SEND QUIT 
		System.out.println("C: Send QUIT to server\n");
		output.write(("QUIT\n").getBytes());
		output.flush();

		// READ RESPONSE
		String str=(String)input.readLine();   
		System.out.println("C: Recv " + str + ", FROM SERVER\n");
	}

	public static String[] getLargestServer(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		System.out.println("Getting largest Server");
		// SEND GETS
		System.out.println("C: Send GETS to server\n");
		output.write(("GETS All\n").getBytes());
		output.flush();

		//READ DATA RESPONSE
		String str = (String)input.readLine();
		String[] data = str.split(" ");
		int nRecs = Integer.parseInt(data[1]);
		int recSize = Integer.parseInt(data[2]);
		System.out.println("nRecs = "+nRecs+"\nrecSize = "+recSize+"\n");
		System.out.println("C: Recv " + str + ", FROM SERVER\n");

		// SEND OK
		System.out.println("C: Send OK to server\n");
		output.write(("OK\n").getBytes());
		output.flush();

		int maxNumCores = 0;
		int numServersWithMax = 0;
		String largestServerType = "";
		String[] largestServer = {};

		for(int i=0;i<nRecs;i++) {
			//READ RESPONSE
			str = (String)input.readLine();
			System.out.println("C: Recv " + str + ", FROM SERVER");
			String[] serverData = str.split(" ");
			int serverCores = Integer.parseInt(serverData[4]);
			if(serverCores > maxNumCores) {
				maxNumCores = serverCores;
				numServersWithMax = 1;
				largestServerType = serverData[0];
				largestServer = serverData;
			} else if(serverCores == maxNumCores) {
				numServersWithMax++;
			}
		}

		System.out.println("Max Cores = "+maxNumCores+"\n# Servers with those cores = "+numServersWithMax+"\nLargest Server Type = "+largestServerType);

		// SEND OK
		System.out.println("C: Send OK to server\n");
		output.write(("OK\n").getBytes());
		output.flush();

		// READ RESPONSE
		str=(String)input.readLine();   
		System.out.println("C: Recv " + str + ", FROM SERVER\n");

		return largestServer;
	}

	public static void scheduleJob(Socket s, DataOutputStream output, BufferedReader input, String job) throws IOException {
		String[] jobArr = job.split(" ");
		int jobIndex = Integer.parseInt(jobArr[2]);
		// int jobRuntime = Integer.parseInt(jobArr[3]);

		String[] largestServer = getLargestServer(s, output, input);
		String schdString = "SCHD "+jobIndex+" "+largestServer[0]+" "+largestServer[1];
		
		// SCHEDULE THE JOB USING SCHD
		System.out.println("C: Send '"+schdString+"' to server\n");
		output.write((schdString+"\n").getBytes());
		output.flush();
	}
}
