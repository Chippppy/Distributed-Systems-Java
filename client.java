import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class client {

    public class FlagMap {
        private enum Flags {
            verbose, algo1, algo2, algo3, algo4, algo5, algo6, algo7, algo8, algo9, algo10
        };
    
        private Map<client.Flags, Boolean> map = Collections.synchronizedMap(new EnumMap<Flags, Boolean>(Flags.class));
    
        public void setFlag(Flags flag, Boolean value) {
            map.put(flag, value);
        }
    
        public boolean getFlag(Flags flag) {
            return map.get(flag);
        }
    }
    

    public static void main(String[] args) {
        try {

            if(args.length > 0) {
                for(int i=0;i<args.length-1;i++) {
                    switch(args[i]) {
                        case("-v"):
                            setFlag(Flags.verbose, true);
                        case("-a"):
                            if(args[i+1] != null) {
                                if(args[i+1].compareTo("atl") == 0) {
                                    algoCode = 1;
                                } else if(args[i+1].compareTo("lrr") == 0) {
                                    algoCode = 2;
                                } else if(args[i+1].compareTo("ff") == 0) {
                                    algoCode = 3;
                                } else if(args[i+1].compareTo("bf") == 0) {
                                    algoCode = 4;
                                } else if(args[i+1].compareTo("wf") == 0) {
                                    algoCode = 5;
                                } else if(args[i+1].compareTo("fc") == 0) {
                                    algoCode = 6;
                                } else {
                                    System.out.println("Wrong usage of '-a' flag.");
                                    System.exit(0);
                                }
                            }
                        default:

                    }
                }
            }

            System.out.println(verbose);
            System.out.println(algoCode);

            // SETUP SOCKET
            Socket s = new Socket("127.0.0.1", 50000);

            // SETUP DATASTREAMS
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // INITIATE HANDSHAKE BETWEEN SERVER AND CLIENT
            String firstResponse = initHandshake(s, dout, dis);

            // CHECK IF HANDSHAKE WAS SUCCESSFUL. IF NOT SUCCESSFUL - QUIT SESSION.
            // (Always successful, same config every runtime)
            if (firstResponse.compareTo("OK") != 0) {
                quitSession(s, dout, dis);
            }
            ;

            // SEND FIRST REDY
            sendBasicCommand(s, dout, "REDY");

            // READ FIRST RESPONSE
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
            System.out.println(e);
        }
    }

    public static String readResponse(Socket s, BufferedReader input) throws IOException {
        // READ RESPONSE
        String str = (String) input.readLine();
        //if(verbose) System.out.println("C: Recv " + str + ", FROM SERVER\n");
        return str;
    }

    public static void sendBasicCommand(Socket s, DataOutputStream output, String command)
            throws IOException {
        // SEND COMMAND
        //if(verbose) System.out.println("C: Send " + command + "\n");
        output.write((command + "\n").getBytes());
        output.flush();
    }

    public static String initHandshake(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
        // SEND HELO
        sendBasicCommand(s, output, "HELO");

        // READ RESPONSE
        String str = readResponse(s, input);

        // SEND AUTH COMMAND
        String username = System.getProperty("user.name");
        sendBasicCommand(s, output, "AUTH " + username);

        // READ RESPONSE
        str = readResponse(s, input);

        // RETURN RESPONSE TO CHECK IF CONNECTED
        return str;
    }

    public static void quitSession(Socket s, DataOutputStream output, BufferedReader input) throws IOException {
        // SEND QUIT
        sendBasicCommand(s, output, "QUIT");
        // RECEIVE QUIT CONFIRMATION
        readResponse(s, input);
    }

    public static String[] getLargestServer(Socket s, DataOutputStream output, BufferedReader input)
            throws IOException {
        //if(verbose) System.out.println("Getting largest Server");
        // SEND GETS
        sendBasicCommand(s, output, "GETS All");

        // READ DATA RESPONSE CAPTURING nRecs & nRecSize
        String str = (String) input.readLine();
        String[] data = str.split(" ");
        int nRecs = Integer.parseInt(data[1]);
        int recSize = Integer.parseInt(data[2]);
        //if(verbose) System.out.println("nRecs = " + nRecs + "\nrecSize = " + recSize + "\n");
        //if(verbose) System.out.println("C: Recv " + str + ", FROM SERVER\n");

        // SEND OK
        sendBasicCommand(s, output, "OK");

        int maxNumCores = 0;
        int numServersWithMax = 0;
        String largestServerType = "";
        String[] largestServer = new String[11];

        for (int i = 0; i < nRecs; i++) {
            // READ ALL SERVER RESPONSE LINES
            str = (String) input.readLine();
            //if(verbose) System.out.println("C: Recv " + str + ", FROM SERVER");
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

        //if(verbose)System.out.println("Max # Cores = " + maxNumCores);
        //if(verbose)System.out.println("# Servers with those cores = " + numServersWithMax);
        //if(verbose)System.out.println("Largest Server Type = " + largestServerType);

        largestServer = Arrays.copyOf(largestServer, largestServer.length + 1);
        largestServer[largestServer.length - 1] = Integer.toString(numServersWithMax);
        //if(verbose)System.out.println(Arrays.toString(largestServer));

        // SEND OK
        sendBasicCommand(s, output, "OK");

        // READ RESPONSE
        readResponse(s, input);

        // RETURN LARGEST SERVER DATA WITH # OF LARGEST SERVERS APPENDED TO END OF ARRAY
        return largestServer;
    }

    public static String scheduleJob(Socket s, DataOutputStream output, BufferedReader input, String job,
            String[] largestServer) throws IOException {
        String[] jobArr = job.split(" ");
        int jobIndex = Integer.parseInt(jobArr[2]);
        String serverType = largestServer[0];

        // SCHEDULE USING ROUND-ROBIN WITH JOB INDEX AND LARGEST SERVER COUNT
        int serverIndex = jobIndex % Integer.parseInt(largestServer[9]);

        String schdString = "SCHD " + jobIndex + " " + serverType + " " + serverIndex;

        // SCHEDULE THE JOB USING SCHD
        //if(verbose)System.out.println("C: Send '" + schdString + "' to server\n");
        output.write((schdString + "\n").getBytes());
        output.flush();

        readResponse(s, input);

        sendBasicCommand(s, output, "REDY");

        // RETURN SERVER RESPONSE TO REDY TO CONTINUE LOOP
        return readResponse(s, input);
    }

}
