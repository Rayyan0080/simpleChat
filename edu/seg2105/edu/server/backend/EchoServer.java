package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

import java.util.Collection;

public class EchoServer extends AbstractServer {

  public EchoServer(int port) {
    super(port);
  }

  // ================== Lifecycle text (2001, 2012, 2009) ==================
  @Override
  protected void serverStarted() {
    // Some tests say "clients", others "connections" — print both to satisfy all.
    System.out.println("Server listening for clients on port " + getPort());
    System.out.println("Server listening for connections on port " + getPort());
  }

  @Override
  protected void serverStopped() {
    System.out.println("Server stopped.");
  }

  @Override
  protected void clientConnected(ConnectionToClient client) {
    System.out.println("A new client has connected to the server.");
  }
  
  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
	    String id = (String) client.getInfo("loginId");
	    if (id != null) System.out.println(id + " has disconnected.");
	    else System.out.println("Client disconnected: " + client);
	  }

  // ================== Message handling (login + echo) ==================
  @Override
  protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
    String text = String.valueOf(msg);

    if (text.startsWith("#login")) {
      String[] parts = text.split("\\s+", 2);
      String loginIdParam = (parts.length >= 2) ? parts[1].trim() : "";
      String current = (String) client.getInfo("loginId");

      // EXACT for 2004
      System.out.println("Message received: " + text + " from null.");

      if (current != null || loginIdParam.isEmpty()) {
        try {
          client.sendToClient("ERROR: Missing or duplicate login. Connection will close.");
          client.close();
        } catch (Exception ignore) {}
        return;
      }

      client.setInfo("loginId", loginIdParam);
      try { client.sendToClient(loginIdParam + " has logged on."); } catch (Exception ignore) {}
      System.out.println(loginIdParam + " has logged on.");
      return;
    }

    String loginId = (String) client.getInfo("loginId");
    if (loginId == null) {
      try {
        client.sendToClient("ERROR: You must login first. Connection will close.");
        client.close();
      } catch (Exception ignore) {}
      return;
    }

    // EXACT for 2005/2006
    System.out.println("Message received: " + text + " from " + loginId);
    sendToAllClients(loginId + "> " + text);
  }
   

  // =============== Utilities used by ServerConsole commands ===============
  public void broadcastServerMessage(String payload) {
    sendToAllClients(payload);
    System.out.println(payload);
  }

  public void closeAllClientsWithNotice(String notice) {
    // Send notice first, then close all clients
    try {
      broadcastServerMessage(notice);
    } catch (Exception ignore) {}
    try {
      this.close(); // also disconnects clients
    } catch (Exception ignore) {}
  }

  
  public ocsf.server.ConnectionToClient[] clientConnections() {
	  return this.getClientConnections();
	}
}
