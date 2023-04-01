import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;

public class client {
    
    public static void main(String[] args) {  
        try {    
			// CREATE SOCKET CONNECTION
            Socket s=new Socket("127.0.0.1",50000);  
            
			// SETUP DATASTREAMS
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
			//INITIATE HANDSHAKE BETWEEN SERVER AND CLIENT
			String firstResponse = initHandshake(s, dout, dis);
			if(firstResponse.compareTo("OK") != 0) {
				quitSession(s, dout, dis);
			};
			
			// SEND INITIAL/FIRST REDY COMMAND
			sendBasicCommand(s, dout, dis, "REDY");
			String response = readResponse(s, dout, dis);

			// GET ALL SERVER INFORMATION & SAVE LARGEST TYPE WITH #OF LARGEST SERVERS APPENDED INTO RESPONSE ARRAY
			String[] largestServer = getLargestServer(s, dout, dis);

			// START WHILE LOOP. USING JAVA WHILE LOOP LABELING TO BREAK OUT ON SERVER RESPONSE OF NONE.
			outerLoop: while(true) {
                String commandType = response.split(" ")[0];
                switch(commandType) {
                    case ("JOBN"):
                        response = scheduleJob(s, dout, dis, response, largestServer);
                        break;

                    case ("JOBP"):

                    case ("JCPL"):
                        sendBasicCommand(s, dout, dis, "REDY");
                        response = readResponse(s, dout, dis);
                        break;

                    case ("RESF"):

                    case ("RESR"):

                    case ("CHKQ"):

					case ("NONE"):
						quitSession(s, dout, dis);
						break outerLoop; 

                    default:

                }
            }

			// CLOSE RESOURCES WHEN FINISHED ALL ACTIONS.
            dout.close();
            s.close();
        } catch(Exception e) {
            System.err.println(e);
        }
    }

/**
 * This function reads a line from the server and returns it
 * 
 * @param s the socket that is connected to the server
 * @param output The output stream to the server
 * @param input BufferedReader object that reads from the socket
 * @return The response from the server.
 */
	public static String readResponse(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		String str=(String)input.readLine();  
		return str;
	}

/**
 * This function takes a simple one word string param and sends that string to the server in the correct format and type.
 * 
 * @param s The socket that is connected to the server
 * @param output The output stream to the server
 * @param input The BufferedReader that reads the server's response.
 * @param command The command to send to the server.
 */
	public static void sendBasicCommand (Socket s, DataOutputStream output, BufferedReader input, String command) throws IOException {
		output.write((command+"\n").getBytes());
		output.flush();
	}

/**
 * > This function sends a HELO command to the server, reads the response, sends an AUTH command with
 * the username, and reads the response. It completes the ds-sim handshake.
 * 
 * @param s The socket that is connected to the server
 * @param output The output stream to the server
 * @param input The BufferedReader that reads the input from the server
 * @return The response from the server.
 */
	public static String initHandshake(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		sendBasicCommand(s, output, input, "HELO");

		String str = readResponse(s, output, input);

		String username = System.getProperty("user.name");
		sendBasicCommand(s, output, input, "AUTH "+username);

		str = readResponse(s, output, input);

		return str;
	}

/**
 * Send the QUIT command to the server and reads the response.
 * 
 * @param s The socket that is connected to the server.
 * @param output The output stream to the server
 * @param input The BufferedReader that is reading from the server.
 */
	public static void quitSession(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		sendBasicCommand(s, output, input, "QUIT");
		readResponse(s, output, input);
	}

/**
 * This function gets the largest server type from the server and returns the server data with the number of
 * servers with the same number of cores appended to the end of an array
 * 
 * @param s The socket that is connected to the server
 * @param output The output stream to the server
 * @param input BufferedReader object that reads from the socket
 * @return The largest server data with the number of largest server type appended to the end of the
 * array.
 */
	public static String[] getLargestServer(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
		sendBasicCommand(s, output, input, "GETS All");

		String str = (String)input.readLine();
		String[] data = str.split(" ");
		int nRecs = Integer.parseInt(data[1]);

		sendBasicCommand(s, output, input, "OK");

		int maxNumCores = 0;
		int numServersWithMax = 0;
		String largestServerType = "";
		String[] largestServer = new String[11];

		for(int i=0;i<nRecs;i++) {
			str = (String)input.readLine();
			String[] serverData = str.split(" ");
			int serverCores = Integer.parseInt(serverData[4]);
			if(serverCores > maxNumCores) {
				maxNumCores = serverCores;
				numServersWithMax = 1;
				largestServerType = serverData[0];
				largestServer = serverData;
			} else if(serverCores == maxNumCores && largestServerType.compareTo(serverData[0])==0) {
				numServersWithMax++;
			}
		}

		largestServer = Arrays.copyOf(largestServer, largestServer.length+1);
		largestServer[largestServer.length-1] = Integer.toString(numServersWithMax);

		sendBasicCommand(s, output, input, "OK");

		readResponse(s, output, input);

		// RETURN LARGEST SERVER DATA WITH # OF LARGEST SERVER TYPE APPENDED TO END OF ARRAY
		return largestServer;
	}
	
/**
 * This function schedules a job using the round-robin algorithm using the job index and the largest
 * server count.
 * 
 * @param s The socket that is connected to the server
 * @param output The output stream to the server
 * @param input The input stream from the server
 * @param job The job to be scheduled
 * @param largestServer The server with the largest number of jobs
 * @return The response from the server after the job has been scheduled.
 */
	public static String scheduleJob(Socket s, DataOutputStream output, BufferedReader input, String job, String[] largestServer) throws IOException {
		String[] jobArr = job.split(" ");
		int jobIndex = Integer.parseInt(jobArr[2]);
		String serverType = largestServer[0];
		
		// SCHEDULE USING ROUND-ROBIN WITH JOB INDEX AND LARGEST SERVER COUNT
		int serverIndex = jobIndex % Integer.parseInt(largestServer[9]);

		String schdString = "SCHD "+jobIndex+" "+serverType+" "+serverIndex;
		
		// SCHEDULE THE JOB USING PREPARED SCHD STRING ABOVE.
		output.write((schdString+"\n").getBytes());
		output.flush();

		readResponse(s, output, input);

		sendBasicCommand(s, output, input, "REDY");

		return readResponse(s, output, input);
	}

}
