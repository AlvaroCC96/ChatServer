package cl.ucn.disc.dsm.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatServer {
    /**
     * bd representation
     */
    private static final List<ChatMessage> messages = new ArrayList<ChatMessage>();
    /**
     * Logger and port for socket
     */
    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);
    private final int port;
    /**
     * Constructor
     * @param PORT
     */
    public ChatServer(final int PORT) throws IOException {
        if (PORT < 1024 || PORT > 65535) { //available port restrictions
            throw new IllegalArgumentException("Please use other port");
        }
        this.port = PORT;
    }

    /**
     * Method that starts the server and waits for connections
     */
    public void start() throws IOException {
        log.debug("Starting server ...");
        final ServerSocket serverSocket = new ServerSocket(port);
        log.debug("Server started in port {}, waiting for connections ..", port);
        while (true) { //always running
            try {
                final Socket socket = serverSocket.accept();
                // The remote connection address.
                final InetAddress address = socket.getInetAddress();
                log.debug("========================================================================================");
                log.debug("Connection from {} in port {}.", address.getHostAddress(), socket.getPort());
                processConnection(socket);
            } catch (IOException e) {
                log.error("Error", e);
                throw e;
            }
        }
    }

    private void processConnection(Socket socket)  throws IOException{
        // Reading the inputstream
        final List<String> lines = contentFromSocket(socket);
        final String request = lines.get(0);
        log.debug("Request: {}", request);
        final PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("HTTP/1.1 200 OK");
        pw.println("Server: DSM v0.0.1");
        pw.println("Date: " + new Date());
        pw.println("Content-Type: text/html; charset=UTF-8");
        pw.println();
    }

    private List<String> contentFromSocket(Socket socket) {

        return null;
    }


}
