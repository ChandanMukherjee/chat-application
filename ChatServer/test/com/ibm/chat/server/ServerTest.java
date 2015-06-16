package com.ibm.chat.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerTest {
	static BufferedReader in, inb;
    static PrintWriter out, outb;
    static Socket socket, socketb;
    static boolean firstpartyready;
    
    //Set up server socket on a separate thread
    //Set up one client connected to the server to validate the test cases
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("In Class");
		try {
		Thread serverThread = new Thread(new ChatServer(9001));
		serverThread.start();
		socket = new Socket("127.0.0.1", 9001);
		in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        String line = in.readLine();
        if (line.startsWith("SUBMITNAME")) {
            out.println("Chandan");
        }
        line = in.readLine();
        firstpartyready = line.startsWith("NAMEACCEPTED");
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("After Class");
		out.println("LOGOUT");
		in.close();
		out.close();
		socket.close();
	}
	//Set up the second client before every test
	@Before
	public void setUp() throws Exception {
		socketb = new Socket("127.0.0.1", 9001);
		inb = new BufferedReader(new InputStreamReader(
            socketb.getInputStream()));
		outb = new PrintWriter(socketb.getOutputStream(), true);
	}

	@After
	public void tearDown() throws Exception {
		outb.println("LOGOUT");
		inb.close();
		outb.close();
		socketb.close();
	}
	//Tests whether servers accepts only unique names
	@Test
	public void testUniqueNames() {
		System.out.println("In Test 1");
		try {
			String lineb = inb.readLine();
	        if (lineb.startsWith("SUBMITNAME")) {
	            outb.println("Chandan");
	        }
	        lineb = inb.readLine();
	        System.out.println(lineb);
	        assertTrue(firstpartyready && lineb.startsWith("SUBMITNAME")) ;
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
	}
	//Tests the functionality of listing online contacts
	@Test
	public void testListFunction() {
		System.out.println("In Test 2");
		try {
			String lineb = inb.readLine();
	        if (lineb.startsWith("SUBMITNAME")) {
	            outb.println("Rob");
	        }
	        lineb = inb.readLine();
	        if (lineb.startsWith("NAMEACCEPTED")) {
	        	outb.println("LIST");
	        }
	        //Thread.sleep(500);
	        lineb = inb.readLine();
	        System.out.println(lineb);
	        assertTrue(firstpartyready && lineb.startsWith("MESSAGE") && (lineb.contains("Chandan;"))) ;
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
	}
	//Tests message sending between two chat clients connected to the server
	@Test
	public void testMessageSend() {
		System.out.println("In Test 3");
		try {
			String lineb = inb.readLine();
	        if (lineb.startsWith("SUBMITNAME")) {
	            outb.println("Peter");
	        }
	        lineb = inb.readLine();
	        if (lineb.startsWith("NAMEACCEPTED")) {
	        	outb.println("Hi Chandan");
	        }
	        String line = in.readLine();
	        System.out.println(line);
	        assertTrue(firstpartyready && line.equalsIgnoreCase("MESSAGE Peter: Hi Chandan")) ;
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}
