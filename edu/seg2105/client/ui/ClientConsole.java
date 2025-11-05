package edu.seg2105.client.ui;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

// (Keep your existing package line if you have one)

import java.io.BufferedReader;
import java.io.InputStreamReader;
import edu.seg2105.client.common.ChatIF
import edu.seg2105.client.backend.ChatClient

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
      String message;
      while ((message = fromConsole.readLine()) != null) {
        client.handleMessageFromClientUI(message);
      }
    } catch (Exception ex) {
      System.out.println("Unexpected error while reading from console!");
    }
  }

  // === UPDATED main: host arg required; port arg optional ===
  public static void main(String[] args) {
    String host;
    int port = DEFAULT_PORT;

    try {
      host = args[0];
      if (args.length >= 2) {
        port = Integer.parseInt(args[1]);
      }
    } catch (Exception e) {
      System.out.println("Usage: java ClientConsole <host> [port]");
      System.out.println("Example: java ClientConsole localhost 6000");
      return;
    }

    ClientConsole chat = new ClientConsole(host, port);
    chat.accept(); // wait for user input
  }
}
