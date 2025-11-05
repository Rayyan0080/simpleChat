package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 


//(Keep your existing package line if you have one)

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class EchoServer extends AbstractServer {

public EchoServer(int port) {
 super(port);
}

// Echo messages from clients
@Override
protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
 this.sendToAllClients(msg.toString());
}

// === NEW: announce when a client connects ===
@Override
protected void clientConnected(ConnectionToClient client) {
 System.out.println("Client connected: " + client);
}

// === NEW: announce when a client disconnects ===
@Override
synchronized protected void clientDisconnected(ConnectionToClient client) {
 System.out.println("Client disconnected: " + client);
}

// (Optional, but nice): log server lifecycle
@Override
protected void serverStarted() {
 System.out.println("Server started. Listening on port " + getPort());
}

@Override
protected void serverStopped() {
 System.out.println("Server stopped.");
}

// Simple main: allow port override via args
public static void main(String[] args) {
 int port = 5555;
 try {
   if (args.length >= 1) port = Integer.parseInt(args[0]);
   EchoServer sv = new EchoServer(port);
   sv.listen(); // start listening
 } catch (Exception ex) {
   System.out.println("ERROR - Could not listen for clients!");
 }
}
}
