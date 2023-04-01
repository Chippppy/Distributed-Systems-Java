# COMP6105 Project

Author - Harry Chipman
StudentID - 46358749
Comp6105/3100 project git repository.

---

## Description

This repository is the host of the code for the Macquarie University Assignment 'Stage1' from unit COMP6105/3100.

The Author is a student of the unit. 

This project was to build a client using the programming language Java, to interact in a server-client relationship to simulate Distributed Systems.

The server (ds-sim) was written in the C language, and therefore the aim was to have two systems talk to each other,  completing tasks, with seemingly no compatability. 

The distributed system simulation is a job scheduling simulation where after an initial authentication handshake, the server sends jobs it has to the client. It is then the clients job to work as a 'Backend' to appropriately return what jobs should be allocated to which 'servers'. 


## Client Ability/Status

Currently, the client side is defaulted (and only able) to schedule the jobs with the server using a 'LLR' algorithm (Largest Round Robin). This algorithm schedules all jobs to the first server type with the largest number of cores.

There is currently no flags/options to be used when running the client code.

All print statements have been removed from the client side code (as a requirement), besides the singular 'System.err.println()' statement within the main method catch statement. 


## Usage

The currently pre-compiled 'client.class' file has been pre-compiled on an M1 Mac running an Ubuntu Virtual Machine. As such this file may not run correctly on your machine.

It is suggested that all users recompile the client.java file using the below steps before running the client.class file.

1. Ensure that all necessary Java requirements are installed on your machine for you to compile and run Java code.
2. Open a terminal session and navigate to the directory holding the 'client.java' file (this directory).
3. Compile the 'client.java' file using the command;

   ```bash
   C:Your/Current/Directory javac client.java
   ```
4. Intialise the server runtime appropriately, E.g. run the below;

   ```bash
   C:Directory/That/Holds/ds-sim/server ./ds-server -n -c ../../configs/sample-configs/sample-config01.xml
   ```
5. In a new, seperate, terminal, run the newly compiled 'client.class' file. E.g. run below;

   ```bash
   C:Your/Current/Directory java client
   ```
6. Return to your server terminal session to view the results of the client and server interation.
