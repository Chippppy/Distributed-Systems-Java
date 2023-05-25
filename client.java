import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class client {

    private static int algorithm = -1;
    private static Boolean verbose = false;
    private static List<String[]> serverInfo = new ArrayList<String[]>();

    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a") && i + 1 < args.length) {
                String algorithmStr = args[i + 1];
                switch (algorithmStr) {
                    case "llr":
                        algorithm = 0;
                        break;
                    case "fc":
                        algorithm = 1;
                        break;
                    case "ss":
                        algorithm = 2;
                        break;
                    default:
                        System.err.println("Invalid Algorithm chosen: " + algorithmStr);
                        System.exit(1);
                }
            }
            if (args[i].equals("-v")) {
                verbose = true;
            }
        }

        if(verbose)System.out.println("Algorithm code: "+algorithm);
        if(verbose)System.out.println("Verbose mode: "+verbose);

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

            sendBasicCommand(s, dout, "REDY");
            String response = readResponse(s, dis);

            if(algorithm == 0) getServers(s, dout, dis);

            outerLoop: while (true) {
                String commandType = response.split(" ")[0];
                switch (commandType) {
                    case ("JOBN"):
                        response = scheduleJob(s, dout, dis, response);
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

            dis.close();
            dout.close();
            s.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static String readResponse(Socket s, BufferedReader input) throws IOException {
        String str = (String) input.readLine();
        if(verbose) System.out.println("SERVER: "+str);
        return str;
    }

    public static void sendBasicCommand(Socket s, DataOutputStream output, String command) throws IOException {
        if(verbose) System.out.println("CLIENT: "+command);
        output.write((command + "\n").getBytes());
        output.flush();
    }

    public static String initHandshake(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
        sendBasicCommand(s, output, "HELO");

        String str = readResponse(s, input);

        String username = System.getProperty("user.name");
        sendBasicCommand(s, output, "AUTH " + username);

        str = readResponse(s, input);

        return str;
    }

    public static void quitSession(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
        sendBasicCommand(s, output, "QUIT");
        readResponse(s, input);
    }

    public static void getServers(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
        sendBasicCommand(s, output, "GETS All");

        String str = (String) input.readLine();
        String[] data = str.split(" ");
        int nRecs = Integer.parseInt(data[1]);

        sendBasicCommand(s, output, "OK");

        for (int i = 0; i < nRecs; i++) {
            str = (String) input.readLine();
            if(verbose) System.out.println("SERVER: "+str);
            serverInfo.add(str.split(" "));
        }

        sendBasicCommand(s, output, "OK");

        readResponse(s, input);
    }

    public static String[] getLargestServer() {
        int maxNumCores = 0;
        int numServersWithMax = 0;
        String largestServerType = "";
        String[] largestServer = new String[11];

        for (int i = 0; i < serverInfo.size(); i++) {
            String[] serverData = serverInfo.get(i);
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

        if(verbose) System.out.println("Largest Server: "+Arrays.toString(largestServer));

        return largestServer;
    }

    public static String[][] getCapableServers(Socket s, DataOutputStream output, BufferedReader input, String[] capableSettings) throws IOException {
        String cores = capableSettings[0];
        String memory = capableSettings[1];
        String disk = capableSettings[2];

        sendBasicCommand(s, output, "GETS Capable " + cores + " " + memory + " " + disk);

        String str = (String) input.readLine();
        String[] data = str.split(" ");
        if(verbose) System.out.println(Arrays.toString(data));
        int nRecs = Integer.parseInt(data[1]);

        if(nRecs == 0) {
            sendBasicCommand(s, output, "OK");
            readResponse(s, input);
            return null;
        }

        sendBasicCommand(s, output, "OK");

        String[][] capableServers = new String[nRecs][];

        for (int i = 0; i < nRecs; i++) {
            str = (String) input.readLine();
            capableServers[i] = str.split(" ");
        }

        if(verbose) System.out.println("First Capable Server: "+Arrays.toString(capableServers[0]));

        sendBasicCommand(s, output, "OK");

        readResponse(s, input);

        return capableServers;
    }

    public static String[][] getAvailableServers(Socket s, DataOutputStream output, BufferedReader input, String[] availableSettings) throws IOException {
        String cores = availableSettings[0];
        String memory = availableSettings[1];
        String disk = availableSettings[2];

        sendBasicCommand(s, output, "GETS Avail " + cores + " " + memory + " " + disk);

        String str = (String) input.readLine();
        String[] data = str.split(" ");
        if(verbose) System.out.println(Arrays.toString(data));
        int nRecs = Integer.parseInt(data[1]);

        if(nRecs == 0) {
            sendBasicCommand(s, output, "OK");
            readResponse(s, input);
            return null;
        }

        sendBasicCommand(s, output, "OK");

        String[][] availableServers = new String[nRecs][];

        for (int i = 0; i < nRecs; i++) {
            str = (String) input.readLine();
            availableServers[i] = str.split(" ");
        }

        if(verbose) System.out.println("First Available Server: "+Arrays.toString(availableServers[0]));

        sendBasicCommand(s, output, "OK");

        readResponse(s, input);

        return availableServers;
    }

    public static String scheduleJob(Socket s, DataOutputStream output, BufferedReader input, String job) throws IOException {
        String[] jobArr = job.split(" ");
        int jobIndex = Integer.parseInt(jobArr[2]);

        String[] jobInfo = {jobArr[4], jobArr[5], jobArr[6]};
        
        String serverType = "";
        int serverIndex = 0;

        String[][] capableServers = new String[serverInfo.size()][];
        String[][] availableServers = new String[serverInfo.size()][];
        
        switch(algorithm) {
            case 0:
                //LLR
                String[] largestServer = getLargestServer();
                serverType = largestServer[0];
                serverIndex = jobIndex % Integer.parseInt(largestServer[9]);
                break;
            case 1:
                //FC
                capableServers = getCapableServers(s, output, input, jobInfo);
                serverType = capableServers[0][0];
                serverIndex = 0;
                break;
            case 2:
                //SPECIAL SAUCE
                availableServers = getAvailableServers(s, output, input, jobInfo);
                if(availableServers == null) {
                    capableServers = getCapableServers(s, output, input, jobInfo);
                    serverType = capableServers[0][0];
                    serverIndex = 0;
                } else {
                    serverType = availableServers[0][0];
                    serverIndex = Integer.parseInt(availableServers[0][1]);
                }
                break;
            default:
                serverType = serverInfo.get(-1)[0];
                serverIndex = 0;
                break;
        }

        String schdString = "SCHD " + jobIndex + " " + serverType + " " + serverIndex;

        if(verbose) System.out.println("CLIENT: "+schdString);

        // SCHEDULE THE JOB USING PREPARED SCHD STRING ABOVE.
        output.write((schdString + "\n").getBytes());
        output.flush();

        readResponse(s, input);

        sendBasicCommand(s, output, "REDY");

        return readResponse(s, input);
    }
}
