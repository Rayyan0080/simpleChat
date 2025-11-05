// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import edu.seg2105.client.common.ChatIF;
import ocsf.client.AbstractClient;

public class ChatClient extends AbstractClient {
private ChatIF clientUI;

public ChatClient(String host, int port, ChatIF clientUI) throws Exception {
 super(host, port);
 this.clientUI = clientUI;
 openConnection();
}

// Forward typed message to server
public void handleMessageFromClientUI(String message) {
 try {
   sendToServer(message);
 } catch (Exception e) {
   clientUI.display("Could not send message to server. Terminating client.");
   quit();
 }
}

// Messages from server
@Override
protected void handleMessageFromServer(Object msg) {
 clientUI.display(msg.toString());
}

// If server shuts down / socket closed
@Override
protected void connectionClosed() {
 System.out.println("Server has shut down. Client will terminate.");
 quit();
}

@Override
protected void connectionException(Exception exception) {
 System.out.println("Server has shut down. Client will terminate.");
 quit();
}

public boolean isConnectedNow() {
 try { return isConnected(); } catch (Exception e) { return false; }
}

public void quit() {
 try { closeConnection(); } catch (Exception ignore) {}
 System.exit(0);
}
}
