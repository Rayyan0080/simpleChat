package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class EchoServer extends AbstractServer {

public EchoServer(int port) { super(port); }

// Broadcast what clients send
@Override
protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
 this.sendToAllClients(msg.toString());
}

// Log connects/disconnects
@Override
protected void clientConnected(ConnectionToClient client) {
 System.out.println("Client connected: " + client);
}

@Override
synchronized protected void clientDisconnected(ConnectionToClient client) {
 System.out.println("Client disconnected: " + client);
}

@Override
protected void serverStarted() {
 System.out.println("Server started. Listening on port " + getPort());
}

@Override
protected void serverStopped() {
 System.out.println("Server stopped.");
}
}
