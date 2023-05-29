# COMP6105 Project

Author - Harry Chipman
StudentID - 46358749
Comp6105/3100 project git repository.

---

## Description

This repository is the host of the code for the Macquarie University Assignment 'Stage1' and 'Stage2' from unit COMP6105/3100.

The Author is a student of the unit.

This project was to build a client using the programming language Java, to interact in a server-client relationship to simulate Distributed Systems.

The server (ds-sim) was written in the C language, and therefore the aim was to have two systems talk to each other,  completing tasks, with seemingly no compatability.

The distributed system simulation is a job scheduling simulation where after an initial authentication handshake, the server sends jobs it has to the client. It is then the clients job to work as a 'Backend' to appropriately return what jobs should be allocated to which 'servers'.

## Client Ability/Status

The user is able to select their own algorithm from those implemented using the appropriate flags.

Current algorithms available are;

1. 'lrr' - Largest-Round-Robin - Schedules jobs to the largest servers in a Round-Robin fashion.
2. 'fc' - First-Capable - Schedules jobs to the first capable server found.
3. 'ss' - Start-Servers - Schedules jobs to the first available server, if none found, first capable server.

Choosing an algorithm can be done via the flag ``-a`` Example: ``java client -a fc``

The user is also able to turn on 'verbose' mode on the client side. This will print all commands being sent to the server from the client, as well as printing all responses sent from the server.

Verbose mode can be turned on via the use of the ``-v`` flag. Example: ``java client -a ss -v``

## Usage

The currently pre-compiled 'client.class' file has been pre-compiled on an M1 Mac running an Ubuntu Virtual Machine. As such this file may not run correctly on your machine.

**Note to marker: The created algorithm for stage2 is the "Start-Server" algorithm. When checking this algorithm please use the algorithm flag with the algorithm code 'ss' to run the student created algorithm. 'lrr' and 'fc' were personal project implementations.**

It is suggested that all users recompile the client.java file using the below steps before running the client.class file with java.

1. Ensure that all necessary Java requirements are installed on your machine for you to compile and run Java code.
2. Open a terminal session and navigate to the directory holding the 'client.java' file (this directory).
3. Compile the 'client.java' file using the command;

   ```bash
   C:Your/Current/Directory javac client.java
   ```
4. Intialise the server runtime appropriately, E.g. run the below;

   ```bash
   C:Directory/That/Holds/ds-sim/server ./ds-server -n -c ../../configs/sample-configs/sample-config02.xml
   ```
5. In a new, seperate, terminal, run the newly compiled 'client.class' file. E.g. run below;

   **Note**: The below configuration will run the Client with the algorithm "Start-Server". It will also activate verbose mode, printing the communication between server-client.

   ```bash
   C:Your/Current/Directory java client -a ss -v
   ```
6. Return to your server terminal session to view the results of the client and server interation.
