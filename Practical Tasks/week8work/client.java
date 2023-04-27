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
            Socket s = new Socket("127.0.0.1", 50000);

            // SETUP DATASTREAMS
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // INITIATE HANDSHAKE BETWEEN SERVER AND CLIENT
            String firstResponse = initHandshake(s, dout, dis);
            if (firstResponse.compareTo("OK") != 0) {
                quitSession(s, dout, dis);
            }
            ;

            // SEND INITIAL/FIRST REDY COMMAND
            sendBasicCommand(s, dout, "REDY");
            String response = readResponse(s, dis);

            // GET ALL SERVER INFORMATION & SAVE LARGEST TYPE WITH #OF LARGEST SERVERS
            // APPENDED INTO RESPONSE ARRAY
            String[] largestServer = getLargestServer(s, dout, dis);

            // START WHILE LOOP. USING JAVA WHILE LOOP LABELING TO BREAK OUT ON SERVER
            // RESPONSE OF NONE.
            outerLoop: while (true) {
                String commandType = response.split(" ")[0];
                switch (commandType) {
                    case ("JOBN"):
                        response = scheduleJob(s, dout, dis, response, largestServer);
                        break;

                    case ("JOBP"):

                    case ("JCPL"):
                        sendBasicCommand(s, dout, "REDY");
                        response = readResponse(s, dis);
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
            dis.close();
            dout.close();
            s.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * This function reads a line from the server and returns it
     * 
     * @param s      the socket that is connected to the server
     * @param output The output stream to the server
     * @param input  BufferedReader object that reads from the socket
     * @return The response from the server.
     */
    public static String readResponse(Socket s, BufferedReader input) throws IOException {
        String str = (String) input.readLine();
        return str;
    }

    /**
     * This function takes a simple one word string param and sends that string to
     * the server in the correct format and type.
     * 
     * @param s       The socket that is connected to the server
     * @param output  The output stream to the server
     * @param command The command to send to the server.
     */
    public static void sendBasicCommand(Socket s, DataOutputStream output, String command) throws IOException {
        output.write((command + "\n").getBytes());
        output.flush();
    }

    /**
     * > This function sends a HELO command to the server, reads the response, sends
     * an AUTH command with
     * the username, and reads the response. It completes the ds-sim handshake.
     * 
     * @param s      The socket that is connected to the server
     * @param output The output stream to the server
     * @return The response from the server.
     */
    public static String initHandshake(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
        sendBasicCommand(s, output, "HELO");

        String str = readResponse(s, input);

        String username = System.getProperty("user.name");
        sendBasicCommand(s, output, "AUTH " + username);

        str = readResponse(s, input);

        return str;
    }

    /**
     * Send the QUIT command to the server and reads the response.
     * 
     * @param s      The socket that is connected to the server.
     * @param output The output stream to the server
     * @param input  The BufferedReader that is reading from the server.
     */
    public static void quitSession(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
        sendBasicCommand(s, output, "QUIT");
        readResponse(s, input);
    }

    /**
     * This function gets the largest server type from the server and returns the
     * server data with the number of
     * servers with the same number of cores appended to the end of an array
     * 
     * @param s      The socket that is connected to the server
     * @param output The output stream to the server
     * @param input  BufferedReader object that reads from the socket
     * @return The largest server data with the number of largest server type
     *         appended to the end of the
     *         array.
     */
    public static String[] getLargestServer(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
        sendBasicCommand(s, output, "GETS All");

        String str = (String) input.readLine();
        String[] data = str.split(" ");
        int nRecs = Integer.parseInt(data[1]);

        sendBasicCommand(s, output, "OK");

        int maxNumCores = 0;
        int numServersWithMax = 0;
        String largestServerType = "";
        String[] largestServer = new String[11];

        for (int i = 0; i < nRecs; i++) {
            str = (String) input.readLine();
            String[] serverData = str.split(" ");
            int serverCores = Integer.parseInt(serverData[4]);
            if (serverCores > maxNumCores) {
                maxNumCores = serverCores;
                numServersWithMax = 1;
                largestServerType = serverData[0];
                largestServer = serverData;
            } else if (serverCores == maxNumCores && largestServerType.compareTo(serverData[0]) == 0) {
                numServersWithMax++;
            }
        }

        largestServer = Arrays.copyOf(largestServer, largestServer.length + 1);
        largestServer[largestServer.length - 1] = Integer.toString(numServersWithMax);

        sendBasicCommand(s, output, "OK");

        readResponse(s, input);

        // RETURN LARGEST SERVER DATA WITH # OF LARGEST SERVER TYPE APPENDED TO END OF
        // ARRAY
        return largestServer;
    }

    /**
     * This function schedules a job using the round-robin algorithm using the job
     * index and the largest
     * server count.
     * 
     * @param s             The socket that is connected to the server
     * @param output        The output stream to the server
     * @param input         The input stream from the server
     * @param job           The job to be scheduled
     * @param largestServer The server with the largest number of jobs
     * @return The response from the server after the job has been scheduled.
     */
    public static String scheduleJob(Socket s, DataOutputStream output, BufferedReader input, String job, String[] largestServer) throws IOException {
        String[] jobArr = job.split(" ");
        int jobIndex = Integer.parseInt(jobArr[2]);
        String[] capableInfo = {jobArr[4], jobArr[5], jobArr[6]};

        String[] capServerData = getCapableServer(s, output, input, capableInfo);

        String serverType = capServerData[0];
        String serverIndex = capServerData[1];

        // SCHEDULE USING ROUND-ROBIN WITH JOB INDEX AND LARGEST SERVER COUNT
        //int serverIndex = jobIndex % Integer.parseInt(largestServer[9]);

        String schdString = "SCHD " + jobIndex + " " + serverType + " " + serverIndex;

        // SCHEDULE THE JOB USING PREPARED SCHD STRING ABOVE.
        output.write((schdString + "\n").getBytes());
        output.flush();

        readResponse(s, input);

        sendBasicCommand(s, output, "REDY");

        return readResponse(s, input);
    }

    public static String[] getCapableServer(Socket s, DataOutputStream output, BufferedReader input, String[] capableSettings) throws IOException {
        String cores = capableSettings[0];
        String memory = capableSettings[1];
        String disk = capableSettings[2];

        sendBasicCommand(s, output, "GETS Capable "+cores+" "+memory+" "+disk);

        String str = (String) input.readLine();
        String[] data = str.split(" ");
        int nRecs = Integer.parseInt(data[1]);

        sendBasicCommand(s, output, "OK");

        String[] serverInfo = new String[9];

        for (int i = 0; i < nRecs; i++) {
            str = (String) input.readLine();
            if(i==0) serverInfo = str.split(" ");
        }

        sendBasicCommand(s, output, "OK");

        readResponse(s, input);

        return serverInfo;
    }

}
