import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;

public class client {
    
    public static void main(String[] args) {  
        try {    
			// SETUP SOCKET  
            Socket s=new Socket("127.0.0.1",50000);  
            
			// SETUP DATASTREAMS
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
			//INITIATE HANDSHAKE BETWEEN SERVER AND CLIENT
			String firstResponse = initHandshake(s, dout, dis);

			// CHECK IF HANDSHAKE WAS SUCCESSFUL. IF NOT SUCCESSFUL - QUIT SESSION.
			// (Always successful, same config every runtime)
			if(firstResponse.compareTo("OK") != 0) {
				quitSession(s, dout, dis);
			};
			
			// SEND FIRST REDY
			sendBasicCommand(s, dout, dis, "REDY");

			// READ FIRST RESPONSE
			String response = readResponse(s, dout, dis);

			// GET ALL SERVER INFORMATION & SAVE LARGEST TYPE WITH #OF LARGEST SERVERS APPENDED INTO RESPONSE ARRAY
			String[] largestServer = getLargestServer(s, dout, dis);

			// START LOOP BETWEEN SERVER AND CLIENT WITH RECURSION & FIRST REDY RESPONSE
			commandHandler(s, dout, dis, response, largestServer);

			// COMMAND HELPER WILL ALWAYS FINISH WITH THE 'quitSession' FUNCTION & THEN CLOSE RESOURCES BELOW.
            dout.close();
            s.close();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

	public static String readResponse(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		// READ RESPONSE
		String str=(String)input.readLine();  
        System.out.println("C: Recv " + str + ", FROM SERVER\n");
		return str;
	}

	public static void sendBasicCommand (Socket s, DataOutputStream output, BufferedReader input, String command) throws IOException {
		// SEND COMMAND
		System.out.println("C: Send "+command+"\n");
		output.write((command+"\n").getBytes());
		output.flush();
	}

	public static String initHandshake(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		// SEND HELO
		sendBasicCommand(s, output, input, "HELO");

		// READ RESPONSE
		String str = readResponse(s, output, input);

		// SEND AUTH COMMAND
		String username = System.getProperty("user.name");
		sendBasicCommand(s, output, input, "AUTH "+username);

		// READ RESPONSE
		str = readResponse(s, output, input);

		// RETURN RESPONSE TO CHECK IF CONNECTED
		return str;
	}

	public static void quitSession(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		// SEND QUIT 
		sendBasicCommand(s, output, input, "QUIT");
		// RECEIVE QUIT CONFIRMATION
		readResponse(s, output, input);
	}

	public static String[] getLargestServer(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		System.out.println("Getting largest Server");
		// SEND GETS
		sendBasicCommand(s, output, input, "GETS All");

		//READ DATA RESPONSE CAPTURING nRecs & nRecSize
		String str = (String)input.readLine();
		String[] data = str.split(" ");
		int nRecs = Integer.parseInt(data[1]);
		int recSize = Integer.parseInt(data[2]);
		System.out.println("nRecs = "+nRecs+"\nrecSize = "+recSize+"\n");
		System.out.println("C: Recv " + str + ", FROM SERVER\n");

		// SEND OK
		sendBasicCommand(s, output, input, "OK");

		int maxNumCores = 0;
		int numServersWithMax = 0;
		String largestServerType = "";
		String[] largestServer = new String[11];

		for(int i=0;i<nRecs;i++) {
			//READ ALL SERVER RESPONSE LINES
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

		System.out.println("Max # Cores = "+maxNumCores);
		System.out.println("# Servers with those cores = "+numServersWithMax);
		System.out.println("Largest Server Type = "+largestServerType);

		largestServer = Arrays.copyOf(largestServer, largestServer.length+1);
		largestServer[largestServer.length-1] = Integer.toString(numServersWithMax);
		System.out.println(Arrays.toString(largestServer));

		// SEND OK
		sendBasicCommand(s, output, input, "OK");

		// READ RESPONSE
		readResponse(s, output, input);

		// RETURN LARGEST SERVER DATA WITH # OF LARGEST SERVERS APPENDED TO END OF ARRAY
		return largestServer;
	}

	public static void commandHandler(Socket s, DataOutputStream output, BufferedReader input, String command, String[] largestServer) throws IOException {
		String commandType = command.split(" ")[0];
		String response = "";
		switch(commandType) {
			case ("JOBN"):
				response = scheduleJob(s, output, input, command, largestServer);
				commandHandler(s, output, input, response, largestServer);
				break;

			case ("JOBP"):

			case ("JCPL"):
				sendBasicCommand(s, output, input, "REDY");
				response = readResponse(s, output, input);
				commandHandler(s, output, input, response, largestServer);
				break;

			case ("RESF"):

			case ("RESR"):

			case ("CHKQ"):

			case ("NONE"):
				quitSession(s, output, input);
				break;

			default:
				System.out.println("Switch Case Failed to Default");
		}
	}
	
	public static String scheduleJob(Socket s, DataOutputStream output, BufferedReader input, String job, String[] largestServer) throws IOException {
		String[] jobArr = job.split(" ");
		int jobIndex = Integer.parseInt(jobArr[2]);
		String serverType = largestServer[0];
		
		// SCHEDULE USING ROUND-ROBIN WITH JOB INDEX AND LARGEST SERVER COUNT
		int serverIndex = jobIndex % Integer.parseInt(largestServer[9]);

		String schdString = "SCHD "+jobIndex+" "+serverType+" "+serverIndex;
		
		// SCHEDULE THE JOB USING SCHD
		System.out.println("C: Send '"+schdString+"' to server\n");
		output.write((schdString+"\n").getBytes());
		output.flush();

		readResponse(s, output, input);

		sendBasicCommand(s, output, input, "REDY");

		// RETURN SERVER RESPONSE TO REDY TO CONTINUE LOOP
		return readResponse(s, output, input);
	}

}
