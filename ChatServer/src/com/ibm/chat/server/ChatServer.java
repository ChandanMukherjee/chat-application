package com.ibm.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

//import com.sun.istack.internal.logging.Logger;


/**
 * A multithreaded chat server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * server can accept two commands LIST and LOGOUT and send messages
 * to other clients. When server receives a LIST command it sends the 
 * list of names of all connected contacts except the user requesting it.
 * When the server receives a LOGOUT Command it closes the connection 
 * associated with the contact name requesting logout. The servers extracts 
 * client name preceding a message to send to the respective client with same screen name.
 * The messages are prefixed with the word "MESSAGE ".
 * Without any name mentioned the servers broadcasts the message to everyone.
 */
public class ChatServer implements Runnable{

    /**
     * The default port that the server listens on.
     */
    private static int PORT = 9001;

    /**
     * The set of all names of clients connected to the server
     * to check if new clients are not registering names already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients as name value pairs
     * to send messages to respective client.
     */
    private static Hashtable<String, PrintWriter> writers = new Hashtable<String, PrintWriter>();
    
    /**
     * The Logger handle for the Chat Server
     */
    private static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());
    /**
     * The application main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
    	LOGGER.log(Level.INFO, "The chat server is running.");
    	startserver(PORT);
    }
    
    protected static void startserver(int port) throws Exception{
        ServerSocket listener = new ServerSocket(port);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
    
    public ChatServer (int port) {
    	PORT = port;
    }
    
    public void run () {
    	try {
    		startserver(PORT);
    	}
    	catch (Exception e){
    		LOGGER.log(Level.SEVERE, e.getMessage());
    	}
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and sending its messages and handling its commands.
     */
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs. 
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive messages.
                out.println("NAMEACCEPTED " + name);
                writers.put(name, out);
                LOGGER.log(Level.INFO, name + " Accepted");

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if ((input == null)||input.isEmpty()) {
                        continue;
                    }
                    StringTokenizer pinput = new StringTokenizer(input);
                    String cmd = pinput.nextToken();
                    if (cmd.equalsIgnoreCase(name)) {
                    	continue;
                    }
                    String msg = "";                   
                    //Handle LOGOUT Command by removing the contact name and 
                    //its socket connection and print writer
                    if (cmd.equalsIgnoreCase("LOGOUT")) {
                    	synchronized (names) {
                    		if (names.contains(name)) {
                    		names.remove(name);
                    		writers.remove(name);
                    		//System.out.println("Log: " + name +" Logged Out ...");
                    		LOGGER.log(Level.INFO, name +" Logged Out ...");
                            break;
                    		}
                    	}                       
                    }
                    //Handle LIST command by sending the list of online contact 
                    //names to the connected client
                    else if (cmd.equalsIgnoreCase("LIST")) {
                    	msg = "";
                    	for (String nm :names){
                    		if (!(nm==name)) msg = nm + "; " + msg;
                    	}
                    	out.println("MESSAGE Contact List: " + msg);
                    	//System.out.println("Log: " + name + ": " + cmd);
                    	LOGGER.log(Level.INFO, name + ": " + cmd);
                    }
                    //Look for the name of requested contact and send text message
                    //Do not send to the same person
                    else if (names.contains(cmd)) {
                    	msg = "";
                    	while (pinput.hasMoreTokens()) {
                    		msg = msg + " " + pinput.nextToken();
                    	}
                    	writers.get(cmd).println("MESSAGE " + name + ": " + msg);
                       	//System.out.println("Log: " + name + ": " + input);
                       	LOGGER.log(Level.INFO, name + ": " + input);
                    }
                    //Broadcast message to everyone 
                    else {
                    	msg = cmd;
                    	while (pinput.hasMoreTokens()) {
                    		msg = msg + " " + pinput.nextToken();
                    	}
                    	Set<String> keys = writers.keySet();
                    	for(String all :keys) {
                    		if (!(all==name)) writers.get(all).println("MESSAGE " + name + ": " + msg);
                           	
                    	}
                    	//System.out.println("Log: " + name + " broadcasted: " + input);
                    	LOGGER.log(Level.INFO, name + " broadcasted: " + input);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                    writers.remove(name);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}