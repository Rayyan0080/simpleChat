package edu.seg2105.client.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import edu.seg2105.client.backend.ChatClient;
import edu.seg2105.client.common.ChatIF;

/**
 * ClientConsole handles UI input for the client.
 * Supports: #quit, #logoff, #login, #gethost, #getport
 * Includes a main(String[] args) so the runner can launch it directly.
 *
 * Args:
 *   args[0] = loginId   (REQUIRED)
 *   args[1] = host      (optional, default: "localhost")
 *   args[2] = port      (optional, default: 5555)
 */
public class ClientConsole implements ChatIF {

  private ChatClient client;
  private BufferedReader fromConsole;

  public ClientConsole(String loginId, String host, int port) {
    try {
      client = new ChatClient(loginId, host, port, this);
    } catch (Exception e) {
      // Must match expected wording exactly:
      System.out.println("ERROR - Can't setup connection! Terminating client.");
      System.exit(1);
    }
    fromConsole = new BufferedReader(new InputStreamReader(System.in));
  }

  @Override
  public void display(String message) {
    System.out.println(message);
  }

  /** Main console loop */
  public void accept() {
    try {
      String message;
      while (true) {
        message = fromConsole.readLine();
        if (message == null) continue;

        if (message.startsWith("#")) {
          handleCommand(message);
        } else {
          client.handleMessageFromClientUI(message);
        }
      }
    } catch (Exception ex) {
      System.out.println("Unexpected error while reading from console.");
    }
  }

  /** Handle commands beginning with '#' */
  private void handleCommand(String cmd) {
    switch (cmd.toLowerCase()) {
      case "#quit":
        client.quit();
        break;

      case "#logoff":
        client.logoff();
        break;

      case "#login":
        if (client.isConnectedNow()) {
          System.out.println("Already connected.");
        } else {
          try {
            client.openConnection();
          } catch (Exception e) {
            System.out.println("Cannot connect to server.");
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
        System.out.println("Unknown command: " + cmd);
    }
  }

  // ---------- Entry point (needed by your test runner) ----------
  public static void main(String[] args) {
    // args[0] must be loginId
    if (args == null || args.length < 1) {
      // Must match expected wording exactly:
      System.out.println("ERROR - No login ID specified.  Connection aborted.");
      System.exit(1);
    }

    String loginId = args[0];
    String host = (args.length >= 2) ? args[1] : "localhost";
    int port = 5555;
    if (args.length >= 3) {
      try {
        port = Integer.parseInt(args[2]);
      } catch (NumberFormatException nfe) {
        // If bad port provided, keep default 5555
      }
    }

    ClientConsole console = new ClientConsole(loginId, host, port);
    console.accept();
  }
}
