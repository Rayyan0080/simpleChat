package edu.seg2105.edu.server.backend;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import edu.seg2105.client.common.ChatIF;

public class ServerConsole implements ChatIF {
  public static final int DEFAULT_PORT = 5555;

  private EchoServer server;

  public ServerConsole(int port) {
    server = new EchoServer(port);
  }

  @Override
  public void display(String message) {
    System.out.println(message);
  }

  public void accept() {
    // Start server listening
    try {
      server.listen();
    } catch (Exception e) {
      System.out.println("ERROR - Could not listen for clients!");
    }

    // Read console
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#")) {
          handleCommand(line.trim());
        } else {
          String payload = "SERVER MSG> " + line;
          System.out.println(payload);
          server.sendToAllClients(payload);
        }
      }
    } catch (Exception e) {
      System.out.println("Unexpected error while reading from server console!");
    }
  }

  private void handleCommand(String cmdLine) {
    String[] parts = cmdLine.split("\\s+");
    String cmd = parts[0].toLowerCase();

    try {
      switch (cmd) {
        case "#quit":
          // close and exit
          try { server.close(); } catch (Exception ignore) {}
          System.out.println("Server quitting.");
          System.exit(0);
          break;

        case "#stop":
          if (server.isListening()) {
            server.stopListening();
            System.out.println("Server stopped listening for new clients.");
          } else {
            System.out.println("Server already stopped.");
          }
          break;

        case "#close":
          server.close(); // stops listening + disconnects all clients
          System.out.println("Server closed (all clients disconnected).");
          break;

        case "#setport":
          if (parts.length < 2) {
            System.out.println("Usage: #setport <port>");
            break;
          }
          if (server.isListening()) {
            System.out.println("Error: #setport only allowed when server is closed.");
            break;
          }
          int newPort = Integer.parseInt(parts[1]);
          server.setPort(newPort);
          System.out.println("Port set to: " + server.getPort());
          break;

        case "#start":
          if (server.isListening()) {
            System.out.println("Server already listening on port " + server.getPort());
          } else {
            server.listen();
            System.out.println("Server started. Listening on port " + server.getPort());
          }
          break;

        case "#getport":
          System.out.println("Current port: " + server.getPort());
          break;

        default:
          System.out.println("Unknown command: " + cmdLine);
      }
    } catch (Exception e) {
      System.out.println("Command failed: " + e.getMessage());
    }
  }

  // Optional: allow port override as arg
  public static void main(String[] args) {
    int port = DEFAULT_PORT;
    try {
      if (args.length >= 1) port = Integer.parseInt(args[0]);
    } catch (NumberFormatException ignore) {}
    ServerConsole sc = new ServerConsole(port);
    sc.accept();
  }
}
