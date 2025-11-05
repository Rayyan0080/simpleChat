package edu.seg2105.client.ui;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

// (Keep your existing package line if you have one)

import edu.seg2105.client.backend.ChatClient;
import edu.seg2105.client.common.ChatIF;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientConsole implements ChatIF {
public static final int DEFAULT_PORT = 5555;

private ChatClient client;

public ClientConsole(String host, int port) {
 try {
   client = new ChatClient(host, port, this);
 } catch (Exception e) {
   System.out.println("Error: Can't setup connection! Terminating client.");
   System.exit(1);
 }
}

@Override
public void display(String message) {
 System.out.println("> " + message);
}

public void accept() {
 try {
   BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
   String line;
   while ((line = fromConsole.readLine()) != null) {
     if (line.startsWith("#")) {
       handleCommand(line.trim());
     } else {
       client.handleMessageFromClientUI(line);
     }
   }
 } catch (Exception ex) {
   System.out.println("Unexpected error while reading from console!");
 }
}

private void handleCommand(String cmdLine) {
 String[] parts = cmdLine.split("\\s+");
 String cmd = parts[0].toLowerCase();

 switch (cmd) {
   case "#quit":
     // terminate gracefully
     try { client.closeConnection(); } catch (Exception ignore) {}
     System.out.println("Client quitting.");
     System.exit(0);
     break;

   case "#logoff":
     if (!client.isConnectedNow()) {
       System.out.println("Client is already logged off.");
     } else {
       try {
         client.closeConnection();
         System.out.println("Logged off (connection closed).");
       } catch (Exception e) {
         System.out.println("Error while logging off: " + e.getMessage());
       }
     }
     break;

   case "#sethost":
     if (client.isConnectedNow()) {
       System.out.println("Error: #sethost only allowed when logged off.");
     } else if (parts.length < 2) {
       System.out.println("Usage: #sethost <hostname>");
     } else {
       try {
         client.setHost(parts[1]);
         System.out.println("Host set to: " + client.getHost());
       } catch (Exception e) {
         System.out.println("Failed to set host: " + e.getMessage());
       }
     }
     break;

   case "#setport":
     if (client.isConnectedNow()) {
       System.out.println("Error: #setport only allowed when logged off.");
     } else if (parts.length < 2) {
       System.out.println("Usage: #setport <port>");
     } else {
       try {
         int port = Integer.parseInt(parts[1]);
         client.setPort(port);
         System.out.println("Port set to: " + client.getPort());
       } catch (NumberFormatException nfe) {
         System.out.println("Port must be a number.");
       } catch (Exception e) {
         System.out.println("Failed to set port: " + e.getMessage());
       }
     }
     break;

   case "#login":
     if (client.isConnectedNow()) {
       System.out.println("Error: already connected.");
     } else {
       try {
         client.openConnection();
         System.out.println("Logged in (connection opened).");
       } catch (Exception e) {
         System.out.println("Failed to login: " + e.getMessage());
       }
     }
     break;

   case "#gethost":
     System.out.println("Current host: " + client.getHost());
     break;

   case "#getport":
     System.out.println("Current port: " + client.getPort());
     break;

   default:
     System.out.println("Unknown command: " + cmdLine);
 }
}

// host required, port optional
public static void main(String[] args) {
 String host;
 int port = DEFAULT_PORT;

 try {
   host = args[0];
   if (args.length >= 2) port = Integer.parseInt(args[1]);
 } catch (Exception e) {
   System.out.println("Usage: java ClientConsole <host> [port]");
   System.out.println("Example: java ClientConsole localhost 6000");
   return;
 }

 ClientConsole chat = new ClientConsole(host, port);
 chat.accept();
}
}
