package edu.seg2105.client.backend;

import ocsf.client.AbstractClient;
import edu.seg2105.client.common.ChatIF;

public class ChatClient extends AbstractClient {
  private final ChatIF clientUI;
  private final String loginId;

  private volatile boolean shuttingDown = false;
  private volatile boolean userInitiatedClose = false; // for #logoff
  private volatile boolean exitRequested = false;       // for #quit

  public ChatClient(String loginId, String host, int port, ChatIF clientUI) throws Exception {
    super(host, port);
    this.clientUI = clientUI;
    this.loginId = loginId;
    openConnection(); // triggers connectionEstablished()
  }

  @Override
  protected void connectionEstablished() {
    try { sendToServer("#login " + loginId); }
    catch (Exception e) {
      clientUI.display("Failed to send login id to server: " + e.getMessage());
      quit();
    }
  }

  @Override
  protected void handleMessageFromServer(Object msg) {
    clientUI.display(msg.toString());
  }

  @Override
  protected void connectionClosed() {
    if (shuttingDown) return;
    shuttingDown = true;

    if (exitRequested) {
      // we're quitting the app; suppress any prints
      System.exit(0);
      return;
    }
    if (userInitiatedClose) {
      System.out.println("Connection closed.");
      // allow future re-logins
      shuttingDown = false;
      userInitiatedClose = false;
    } else {
      System.out.println("The server has shut down.");
      System.exit(0);
    }
  }

  @Override
  protected void connectionException(Exception exception) {
    // treat like connectionClosed()
    connectionClosed();
  }

  /** called by UI for normal user message */
  public void handleMessageFromClientUI(String message) {
    try { sendToServer(message); }
    catch (Exception e) {
      clientUI.display("Could not send message to server. Terminating client.");
      quit();
    }
  }

  /** #logoff: disconnect but keep app alive */
  public void logoff() {
    userInitiatedClose = true;
    try { closeConnection(); } catch (Exception ignore) {}
  }

  /** #quit: exit the app; suppress “server has shut down.” noise */
  public void quit() {
    exitRequested = true;
    try { closeConnection(); } catch (Exception ignore) {}
    // connectionClosed() will System.exit(0) without printing
    // but if it doesn’t fire promptly, ensure exit anyway:
    System.exit(0);
  }

  public String getLoginId() { return loginId; }
  public boolean isConnectedNow() { try { return isConnected(); } catch (Exception e) { return false; } }
}
