package tests;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Semi-automated test runner for SEG2105 SimpleChat.
 * Spawns your real ServerConsole and ClientConsole, sends commands,
 * and prints captured output for manual comparison.
 */
public class SemiAutoChatRunner {

    // ====== UPDATE THESE IF YOUR PROJECT NAMES DIFFER ======
    // Run this from the simpleChat project. Eclipse usually uses "bin" as output.
    private static final String CLASS_PATH =
            "bin" + File.pathSeparator +                // simpleChat/bin
            ".." + File.separator + "OCSF" + File.separator + "bin" + // OCSF/bin (adjust if your project name differs)
            File.pathSeparator + ".";

    // Your actual mains (from your screenshot)
    private static final String SERVER_MAIN = "edu.seg2105.edu.server.backend.ServerConsole";
    private static final String CLIENT_MAIN = "edu.seg2105.client.ui.ClientConsole";
    // =======================================================

    private static final long STARTUP_WAIT_MS = 1000;  // wait after starting proc
    private static final long SEND_WAIT_MS    = 500;   // wait after sending a command
    private static final long CONNECT_WAIT_MS = 1000;  // wait after client connects

    // ---------- Process wrapper ----------
    private static class Proc {
        final Process process;
        final BufferedWriter stdin;
        final String name;
        final Gobbler out;
        final Gobbler err;

        Proc(Process p, String name) {
            this.process = p;
            this.stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8));
            this.name = name;
            this.out = new Gobbler(p.getInputStream(), name + " OUT");
            this.err = new Gobbler(p.getErrorStream(), name + " ERR");
            this.out.start();
            this.err.start();
        }

        /** Safe send: returns false if process is already dead */
        boolean send(String line) {
            try {
                if (!process.isAlive()) return false;
                stdin.write(line);
                stdin.write(System.lineSeparator());
                stdin.flush();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        void closeInput() {
            try { stdin.flush(); } catch (Exception ignore) {}
            try { stdin.close(); } catch (Exception ignore) {}
        }

        String stdout() { return out.snapshot(); }
        String stderr() { return err.snapshot(); }

        void kill() {
            closeInput();
            try { if (process.isAlive()) process.destroy(); } catch (Exception ignore) {}
            try { if (process.isAlive()) process.destroyForcibly(); } catch (Exception ignore) {}
        }
    }

    private static class Gobbler extends Thread {
        private final InputStream in;
        private final String tag;
        private final StringBuilder buf = new StringBuilder();

        Gobbler(InputStream in, String tag) {
            this.in = in;
            this.tag = tag;
            setDaemon(true);
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    synchronized (buf) { buf.append(line).append('\n'); }
                    System.out.println("[" + tag + "] " + line);
                }
            } catch (IOException ignored) { }
        }

        String snapshot() { synchronized (buf) { return buf.toString(); } }
    }

    // ---------- Launch helpers ----------
    private static Proc launchServer(Integer port) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("java"); cmd.add("-cp"); cmd.add(CLASS_PATH); cmd.add(SERVER_MAIN);
        if (port != null) cmd.add(String.valueOf(port));
        Process p = new ProcessBuilder(cmd).directory(new File(".")).start();
        Proc S = new Proc(p, "SERVER");
        Thread.sleep(STARTUP_WAIT_MS);
        return S;
    }

    private static Proc launchClient(String loginId, String host, Integer port) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("java"); cmd.add("-cp"); cmd.add(CLASS_PATH); cmd.add(CLIENT_MAIN);
        cmd.add(loginId);
        if (host != null) cmd.add(host);
        if (port != null) cmd.add(String.valueOf(port));
        Process p = new ProcessBuilder(cmd).directory(new File(".")).start();
        Proc C = new Proc(p, "CLIENT-" + loginId);
        Thread.sleep(CONNECT_WAIT_MS);
        return C;
    }

    // ---------- Utilities ----------
    private static void sep() {
        System.out.println("=================================================");
    }
    private static void show(String title, String content) {
        sep();
        System.out.println(title);
        sep();
        System.out.println((content == null || content.isEmpty()) ? "(no output captured)" : content.trim());
        System.out.println();
    }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignore) {} }

    // ---------- Runner ----------
    public static void main(String[] args) throws Exception {

        // 2001
        Proc server = null;
        try {
            sep(); System.out.println("TEST 2001: Server startup (default port 5555)");
            server = launchServer(null);
            sleep(SEND_WAIT_MS);
            show("SERVER OUTPUT (2001)", server.stdout() + "\n" + server.stderr());
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2002
        Proc cNoLogin = null;
        try {
            sep(); System.out.println("TEST 2002: Client startup without login");
            List<String> cmd = Arrays.asList("java", "-cp", CLASS_PATH, CLIENT_MAIN);
            cNoLogin = new Proc(new ProcessBuilder(cmd).start(), "CLIENT-NO-LOGIN");
            sleep(CONNECT_WAIT_MS);
            show("CLIENT OUTPUT (2002)", cNoLogin.stdout() + "\n" + cNoLogin.stderr());
        } finally { if (cNoLogin != null) cNoLogin.kill(); sleep(SEND_WAIT_MS); }

        // 2003
        Proc cAlone = null;
        try {
            sep(); System.out.println("TEST 2003: Client with login but no server");
            cAlone = launchClient("cora", null, null);
            sleep(CONNECT_WAIT_MS);
            show("CLIENT OUTPUT (2003)", cAlone.stdout() + "\n" + cAlone.stderr());
        } finally { if (cAlone != null) cAlone.kill(); sleep(SEND_WAIT_MS); }

        // 2004
        try {
            sep(); System.out.println("TEST 2004: Client connection (default), login handshake");
            server = launchServer(null);
            Proc c1 = launchClient("cora", "localhost", 5555);
            sleep(SEND_WAIT_MS);
            show("SERVER OUTPUT (2004)", server.stdout() + "\n" + server.stderr());
            show("CLIENT OUTPUT (2004)", c1.stdout() + "\n" + c1.stderr());
            if (c1 != null) { c1.send("#quit"); sleep(SEND_WAIT_MS); c1.kill(); }
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2005
        try {
            sep(); System.out.println("TEST 2005: Echo (client -> server -> all)");
            server = launchServer(null);
            Proc c1 = launchClient("cora", "localhost", 5555);
            c1.send("hello"); sleep(SEND_WAIT_MS);
            show("SERVER OUTPUT (2005)", server.stdout() + "\n" + server.stderr());
            show("CLIENT OUTPUT (2005)", c1.stdout() + "\n" + c1.stderr());
            c1.send("#quit"); sleep(SEND_WAIT_MS); c1.kill();
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2006
        try {
            sep(); System.out.println("TEST 2006: Multiple clients + server broadcast");
            server = launchServer(null);
            Proc c1 = launchClient("a1", "localhost", 5555);
            Proc c2 = launchClient("a2", "localhost", 5555);

            c1.send("hi from a1"); sleep(SEND_WAIT_MS);
            c2.send("hi from a2"); sleep(SEND_WAIT_MS);

            server.send("hello all"); sleep(SEND_WAIT_MS);

            show("SERVER OUTPUT (2006)", server.stdout() + "\n" + server.stderr());
            show("CLIENT a1 OUTPUT (2006)", c1.stdout() + "\n" + c1.stderr());
            show("CLIENT a2 OUTPUT (2006)", c2.stdout() + "\n" + c2.stderr());

            c1.send("#quit"); sleep(SEND_WAIT_MS); c1.kill();
            c2.send("#quit"); sleep(SEND_WAIT_MS); c2.kill();
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2007
        try {
            sep(); System.out.println("TEST 2007: Server #quit");
            server = launchServer(null);
            server.send("#quit"); sleep(SEND_WAIT_MS);
            show("SERVER OUTPUT (2007)", server.stdout() + "\n" + server.stderr());
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2008
        try {
            sep(); System.out.println("TEST 2008: #stop then #close, client notices shutdown");
            server = launchServer(null);
            Proc c1 = launchClient("bibi", "localhost", 5555);

            server.send("#stop");  sleep(SEND_WAIT_MS);
            server.send("#close"); sleep(SEND_WAIT_MS);

            show("SERVER OUTPUT (2008)", server.stdout() + "\n" + server.stderr());
            show("CLIENT OUTPUT (2008)", c1.stdout() + "\n" + c1.stderr());

            c1.kill();
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2009
        try {
            sep(); System.out.println("TEST 2009: Server restart (#close, #start) and connect");
            server = launchServer(null);
            server.send("#close"); sleep(SEND_WAIT_MS);
            server.send("#start"); sleep(SEND_WAIT_MS);
            Proc c1 = launchClient("didi", "localhost", 5555); sleep(SEND_WAIT_MS);

            show("SERVER OUTPUT (2009)", server.stdout() + "\n" + server.stderr());
            show("CLIENT OUTPUT (2009)", c1.stdout() + "\n" + c1.stderr());

            c1.send("#quit"); sleep(SEND_WAIT_MS); c1.kill();
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2010
        try {
            sep(); System.out.println("TEST 2010: Client #quit");
            server = launchServer(null);
            Proc c1 = launchClient("lola", "localhost", 5555);
            c1.send("#quit"); sleep(SEND_WAIT_MS);
            show("SERVER OUTPUT (2010)", server.stdout() + "\n" + server.stderr());
            show("CLIENT OUTPUT (2010)", c1.stdout() + "\n" + c1.stderr());
            c1.kill();
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2011
        try {
            sep(); System.out.println("TEST 2011: Client #logoff");
            server = launchServer(null);
            Proc c1 = launchClient("mimi", "localhost", 5555);
            c1.send("#logoff"); sleep(SEND_WAIT_MS);
            show("SERVER OUTPUT (2011)", server.stdout() + "\n" + server.stderr());
            show("CLIENT OUTPUT (2011)", c1.stdout() + "\n" + c1.stderr());
            try { c1.send("#quit"); } catch (Exception ignore) {}
            sleep(SEND_WAIT_MS);
            c1.kill();
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2012
        try {
            sep(); System.out.println("TEST 2012: Server on custom port 1234");
            server = launchServer(1234); sleep(SEND_WAIT_MS);
            show("SERVER OUTPUT (2012)", server.stdout() + "\n" + server.stderr());
        } finally { if (server != null) server.send("#quit"); if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        // 2013
        try {
            sep(); System.out.println("TEST 2013: Client connects to custom port 1234");
            server = launchServer(1234);
            Proc c1 = launchClient("nini", "localhost", 1234); sleep(SEND_WAIT_MS);
            show("SERVER OUTPUT (2013)", server.stdout() + "\n" + server.stderr());
            show("CLIENT OUTPUT (2013)", c1.stdout() + "\n" + c1.stderr());
            c1.send("#quit"); sleep(SEND_WAIT_MS); c1.kill();
        } finally { if (server != null) server.kill(); sleep(SEND_WAIT_MS); }

        sep();
        System.out.println("Done. Scroll up to compare each block with your expected wording.");
        sep();
    }
}
