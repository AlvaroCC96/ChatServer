import cl.ucn.disc.dsm.chat.ChatServer;

import java.io.IOException;

public class Main {
    /**
     * port to start the server
     */
    private static final int PORT = 9000;
    /**
     *Principal
     */
    public static void main(final String[] args) throws IOException {
        ChatServer Server = new ChatServer(PORT); //initialize the server and put it to run
        Server.start();
    }
}
