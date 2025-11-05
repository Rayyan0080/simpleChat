// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

// (Keep your existing package line if you have one)

import ocsf.client.AbstractClient;
import edu.seg2105.client.common.ChatIF

public class ChatClient extends AbstractClient {
  private ChatIF clientUI;

  public ChatClient(String host, int port, ChatIF clientUI) throws Exception {
    super(host, port);
    this.clientUI = clientUI;
    openConnection(); // connect on startup
  }

  // Send messages typed by user
  public void handleMessageFromClientUI(String message) {
    try {
      sendToServer(message);
    } catch (Exception e) {
      clientUI.display("Could not send message to server. Terminating client.");
      quit();
    }
  }

  // Messages arriving from server
  @Override
  protected void handleMessageFromServer(Object msg) {
    clientUI.display(msg.toString());
  }

  // === NEW: react if the connection is closed (e.g., server stops) ===
  @Override
  protected void connectionClosed() {
    System.out.println("Server has shut down. Client will terminate.");
    quit();
  }

  // === NEW: react if an exception occurs on the connection ===
  @Override
  protected void connectionException(Exception exception) {
    System.out.println("Server has shut down. Client will terminate.");
    quit();
  }

  public void quit() {
    try {
      closeConnection();
    } catch (Exception e) {
      // ignore
    }
    System.exit(0);
  }
}
