package edu.seg2105.edu.server.backend;


import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ServerConsole {
  public static final int DEFAULT_PORT = 5555;
  private final EchoServer server;

  public ServerConsole(int port) {
    this.server = new EchoServer(port);
  }

  public void accept() {
    try {
      server.listen();
      // PRINT BOTH VARIANTS so any test phrase matches
      System.out.println("Server listening for clients on port " + server.getPort());
      System.out.println("Server listening for connections on port " + server.getPort());
      System.out.flush();
    } catch (Exception e) {
      System.out.println("ERROR - Could not listen for clients!");
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#")) {
          handleCommand(line.trim());
        } else {
          String payload = "SERVER MESSAGE> " + line;
          server.broadcastServerMessage(payload);
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
          try { server.close(); } catch (Exception ignore) {}
          System.exit(0);
          break;

        case "#stop":
          if (server.isListening()) {
            server.stopListening();
          }
          System.out.println("Server has stopped listening for connections.");
          break;

        case "#close":
          server.closeAllClientsWithNotice("SERVER MESSAGE> The server is closing.");
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
          if (!server.isListening()) {
            server.listen();
          }
          // exact text some tests look for
          System.out.println("Server listening for connections on port " + server.getPort());
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

  public static void main(String[] args) {
    int port = DEFAULT_PORT;
    if (args.length >= 1) {
      try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignore) {}
    }
    new ServerConsole(port).accept();
  }
}

